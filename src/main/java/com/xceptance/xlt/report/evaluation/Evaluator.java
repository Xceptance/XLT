package com.xceptance.xlt.report.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.io.Files;
import com.thoughtworks.xstream.XStream;
import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.report.util.xstream.SanitizingDomDriver;

import dev.harrel.jsonschema.Validator;
import dev.harrel.jsonschema.ValidatorFactory;
import dev.harrel.jsonschema.providers.OrgJsonNode;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.UnprefixedElementMatchingPolicy;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

public class Evaluator
{
    private static final String SCHEMA_RESOURCE_PATH = "evaluation-schema.json";

    private final File configFile;

    private final Processor processor;

    /**
     * Creates a new evaluator instance that uses the given configuration JSON file.
     *
     * @param configFile
     *            the evaluation configuration JSON file to use
     */
    public Evaluator(final File configFile)
    {
        ParameterCheckUtils.isReadableFile(configFile, "configFile");

        this.configFile = configFile;
        this.processor = new Processor(false);
    }

    /**
     * Evaluates the given XML file and returns the evaluations outcome.
     *
     * @param documentFile
     *            the XML file to evaluate
     * @return evaluation outcome
     */
    public Evaluation evaluate(final File documentFile)
    {
        ParameterCheckUtils.isNotNull(documentFile, "documentFile");

        try
        {
            final Configuration config = parseConfiguration();
            return doEvaluate(config, documentFile);
        }
        catch (final Exception ex)
        {
            return Evaluation.error(ex);
        }

    }

    /**
     * Writes the given evaluation outcome as serialized XML to the given output file.
     *
     * @param evaluation
     *            the evaluation outcome to be written
     * @param outputFile
     *            the target output file
     * @throws IOException
     *             thrown when evaluation outcome could be not be written to the given file
     */
    public void writeEvaluationToFile(final Evaluation evaluation, final File outputFile) throws IOException
    {
        try (final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outputFile), XltConstants.UTF8_ENCODING))
        {
            writeEvaluation(evaluation, osw);
        }
    }

    /**
     * Writes the given evaluation outcome as serialized XML to the given destination writer.
     *
     * @param evaluation
     *            the evaluation outcome to be written
     * @param writer
     *            the destination to write serialized XML to
     * @throws IOException
     *             thrown evaluation outcome could not be written
     */
    public void writeEvaluation(final Evaluation evaluation, final Writer writer) throws IOException
    {
        // writer the XML preamble
        writer.write(XltConstants.XML_HEADER);

        // create and configure XStream instance
        final XStream xstream = new XStream(new SanitizingDomDriver());
        xstream.autodetectAnnotations(true);
        xstream.aliasSystemAttribute(null, "class");
        xstream.setMode(XStream.NO_REFERENCES);

        // let XStream do its job
        xstream.toXML(evaluation, writer);
    }

    protected Configuration parseConfiguration() throws ValidationError, FileNotFoundException, IOException
    {
        final JSONObject schemaJSON;
        try (final InputStream is = getClass().getResourceAsStream(SCHEMA_RESOURCE_PATH))
        {
            schemaJSON = new JSONObject(new JSONTokener(is));
        }
        catch (final JSONException je)
        {
            throw new ValidationError("Failed to parse JSON schema file", je);
        }

        final JSONObject configJSON;
        try (final BufferedReader reader = Files.newReader(configFile, StandardCharsets.UTF_8))
        {
            configJSON = new JSONObject(new JSONTokener(reader));
        }
        catch (final JSONException je)
        {
            throw new ValidationError("Could not parse configuration file '" + configFile.getName() + "' as JSON", je);
        }

        final Validator validator = new ValidatorFactory().withJsonNodeFactory(new OrgJsonNode.Factory()).createValidator();

        final URI schemaURI = validator.registerSchema(schemaJSON);
        final Validator.Result validationResult = validator.validate(schemaURI, configJSON);
        if (!validationResult.isValid())
        {
            throw new ValidationError("Configuration file '" + configFile.getName() + "' is malformed -> " +
                                      validationResult.getErrors().stream().map(e -> e.getInstanceLocation() + ": " + e.getError())
                                                      .collect(Collectors.joining(", ")));
        }

        try
        {
            return Configuration.fromJSON(configJSON);
        }
        catch (final Exception e)
        {
            throw new ValidationError("Configuration file '" + configFile.getName() + "' is malformed", e);
        }

    }

    protected Evaluation doEvaluate(final Configuration config, final File documentFile) throws SaxonApiException
    {
        final XdmNode docNode = processor.newDocumentBuilder().build(documentFile);
        final XPathCompiler xpathCompiler = processor.newXPathCompiler();
        xpathCompiler.setUnprefixedElementMatchingPolicy(UnprefixedElementMatchingPolicy.DEFAULT_NAMESPACE);
        xpathCompiler.setCaching(true);

        int points = 0, totalPoints = 0; // counters for achieved and achievable points
        boolean testFailed = false; // whether to mark test as failed (due to "hard rule fail" or due to "rating fail")

        // initialize evaluation and its result objects
        final Evaluation evaluation = new Evaluation(config);
        final Evaluation.Result result = evaluation.result;

        // loop through the list of configured groups (in definition order)
        for (final GroupDefinition groupDef : config.getGroups())
        {
            // create a group result object
            final Evaluation.Group group = new Evaluation.Group(groupDef);
            // loop through the group's rule IDs (in definition order)
            for (final String ruleId : groupDef.getRuleIds())
            {
                // lookup the rule's definition for this ID
                final RuleDefinition ruleDef = config.getRule(ruleId);
                // create a rule result object
                final Evaluation.Rule rule = new Evaluation.Rule(ruleDef, groupDef.isEnabled());
                // evaluate the rule
                evaluateRule(rule, xpathCompiler, docNode);
                // check for "hard rule fail"
                if (rule.getStatus().isFailed() && rule.getDefinition().isFailsTest())
                {
                    testFailed = true;
                }

                // add rule result to group result
                group.addRule(rule);

            }

            // conclude the evaluation of the group
            conclude(group);

            // add number of group's achieved and achievable points to the counters
            points += group.getPoints();
            totalPoints += group.getTotalPoints();

            // add group result to evaluation result
            result.addGroup(group);
        }

        // set overall number of achieved and achievable points
        result.setPoints(points);
        result.setTotalPoints(totalPoints);

        // compute final score
        final double pointsPercentage = getPercentage(points, totalPoints);

        // determine the test's rating and whether it has failed
        String rating = null;
        for (final RatingDefinition ratingDefn : config.getRatings())
        {
            if (ratingDefn.isEnabled() && pointsPercentage <= ratingDefn.getValue())
            {
                rating = ratingDefn.getName();
                testFailed = testFailed || ratingDefn.isFailsTest();

                break;
            }

        }

        // set final values
        result.setTestFailed(testFailed);
        result.setPointsPercentage(pointsPercentage);
        result.setRating(rating);

        return evaluation;
    }

    private void evaluateRule(final Evaluation.Rule rule, final XPathCompiler compiler, final XdmNode document)
    {
        for (final RuleDefinition.Check check : rule.getDefinition().getChecks())
        {
            final Evaluation.Rule.Check ruleCheck = new Evaluation.Rule.Check(check, rule.isEnabled());
            if (ruleCheck.isEnabled())
            {
                evaluateRuleCheck(ruleCheck, compiler, document);
            }
            rule.addCheck(ruleCheck);
        }

        conclude(rule);
    }

    private void evaluateRuleCheck(final Evaluation.Rule.Check check, final XPathCompiler compiler, final XdmNode document)
    {
        Status status = Status.FAILED;
        String message = null, value = null;
        try
        {
            final String selector = check.getDefinition().getSelector();
            final XdmValue result = compiler.evaluate(selector, document);
            if (result.isEmpty())
            {
                status = Status.ERROR;
                message = "No item found for selector '" + selector + "'";
            }
            else if (result.size() > 1)
            {
                status = Status.ERROR;
                message = "Selector must match a single item but found " + result.size() + " items instead";
            }

            else
            {
                final XdmItem node = result.itemAt(0);
                if (!(node.isAtomicValue() || node.isNode()))
                {
                    status = Status.ERROR;
                    message = "Selected item is neither a node nor an atomic value";
                }
                else
                {
                    value = node.getStringValue();
                    final boolean matches = evaluateConditionSafe(check.getDefinition().getCondition(), compiler, node);
                    if (matches)
                    {
                        status = Status.PASSED;
                    }
                }

            }
        }
        catch (final SaxonApiException sae)
        {
            status = Status.ERROR;
            message = sae.getMessage();
        }
        check.setStatus(status);
        check.setErrorMessage(message);
        if (check.getDefinition().isDisplayValue())
        {
            check.setValue(value);
        }
    }

    private boolean evaluateConditionSafe(final String condition, final XPathCompiler compiler, final XdmValue contextValue)
    {
        // strip any leading/trailing whitespace
        String expr = StringUtils.strip(condition);
        // if expression starts with a comparison operator, put a '.' in front of it
        if (StringUtils.startsWithAny(expr, "=", "<", ">", "!="))
        {
            expr = ". " + expr;
        }
        try
        {
            return contextValue.select(compiler.compile(expr).asStep()).asAtomic().getBooleanValue();
        }
        catch (final ClassCastException | SaxonApiException e)
        {
            return false;
        }
    }

    private void conclude(final Evaluation.Rule rule)
    {
        // nothing to do for disabled rules
        if (!rule.isEnabled())
        {
            return;
        }

        Status lastStatus = null;
        // loop through rule's checks
        for (final Evaluation.Rule.Check c : rule.getChecks())
        {
            final Status checkStatus = c.getStatus();
            // ignore skipped checks
            if (checkStatus.isSkipped())
            {
                continue;
            }

            // remember most recent check status that doesn't indicate a passed check or just the very first check
            // status if all checks did pass
            if (lastStatus == null || !checkStatus.isPassed())
            {
                lastStatus = checkStatus;
                // encountered erroneous check -> set rule message to the check's error message and stop looping
                if (checkStatus.isError())
                {
                    rule.setMessage(c.getErrorMessage());
                    break;
                }
            }
        }
        // take action according to rule's final status
        // (pass: set message to success message and points to all achievable points, fail: set message to fail message)
        if (lastStatus != null)
        {
            rule.setStatus(lastStatus);
            if (lastStatus.isPassed())
            {
                rule.setMessage(rule.getDefinition().getSuccessMessage());
                rule.setPoints(rule.getDefinition().getPoints());
            }
            else if (lastStatus.isFailed())
            {
                rule.setMessage(rule.getDefinition().getFailMessage());
            }
        }

    }

    private void conclude(final Evaluation.Group group)
    {
        // nothing to do for disabled groups
        if (!group.getDefinition().isEnabled())
        {
            return;
        }

        Integer firstMatch = null, lastMatch = null;
        int maxPoints = 0, sumPoints = 0, sumPointsMatching = 0;
        // loop through group's rule and determine
        // - the points of the first matching rule,
        // - the points of the last matching rule,
        // - the points of all matching rules (sum of),
        // - the maximum number of all rules' points and
        // - the overall sum of all rules' points
        // - the rules' messages
        final List<String> messages = new LinkedList<String>();
        for (final Evaluation.Rule rule : group.getRules())
        {
            // rules must be enabled in order to participate in point calculation
            if (!rule.getDefinition().isEnabled())
            {
                continue;
            }

            final int rulePoints = rule.getPoints();
            final int rulePointsMax = rule.getDefinition().getPoints();
            maxPoints = Math.max(maxPoints, rulePointsMax);
            if (rule.getStatus().isPassed())
            {
                if (firstMatch == null)
                {
                    firstMatch = Integer.valueOf(rulePoints);
                }
                lastMatch = Integer.valueOf(rulePoints);

                sumPointsMatching += rulePoints;

                // TODO Clarify why rule's fail-message is not considered
                if(rule.getMessage() != null)
                {
                    messages.add(rule.getMessage());
                }

            }

            sumPoints += rulePointsMax;
            
        }

        // pick the correct values for group's points and total points according to its points source
        final int points, totalPoints;
        switch (group.getDefinition().getPointSource())
        {
            case FIRST:
                points = Optional.ofNullable(firstMatch).orElse(0);
                totalPoints = maxPoints;
                if(!messages.isEmpty())
                {
                    group.addMessage(messages.get(0));
                }
                break;
            case LAST:
                points = Optional.ofNullable(lastMatch).orElse(0);
                totalPoints = maxPoints;
                if(!messages.isEmpty())
                {
                    group.addMessage(messages.get(messages.size()-1));
                }
                break;
            case ALL:
                points = sumPointsMatching;
                totalPoints = sumPoints;
                messages.forEach(group::addMessage);
                break;
            default:
                points = 0;
                totalPoints = 0;
        }

        group.setPoints(points);
        group.setTotalPoints(totalPoints);
    }

    /**
     * Returns the ratio in percent rounded to one decimal place.
     *
     * @param numerator
     * @param denominator
     * @return given ratio in percent
     */
    private static double getPercentage(final int numerator, final int denominator)
    {
        return denominator > 0 ? (Math.round((numerator * 1000.0) / denominator) / 10.0) : 0.0;
    }

}
