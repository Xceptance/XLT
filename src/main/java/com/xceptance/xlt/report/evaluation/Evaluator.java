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

    public Evaluator(final File configFile)
    {
        ParameterCheckUtils.isReadableFile(configFile, "configFile");

        this.configFile = configFile;
        this.processor = new Processor(false);
    }

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

    public void storeEvaluationToFile(final Evaluation evaluation, final File outputFile) throws IOException
    {
        try (final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outputFile), XltConstants.UTF8_ENCODING))
        {
            storeEvaluation(evaluation, osw);
        }
    }

    public void storeEvaluation(final Evaluation evaluation, final Writer writer) throws IOException
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

    protected Evaluation doEvaluate(final Configuration config, final File documentFile) throws SaxonApiException
    {
        final XdmNode docNode = processor.newDocumentBuilder().build(documentFile);
        final XPathCompiler xpathCompiler = processor.newXPathCompiler();
        xpathCompiler.setUnprefixedElementMatchingPolicy(UnprefixedElementMatchingPolicy.DEFAULT_NAMESPACE);
        xpathCompiler.setCaching(true);

        int points = 0, totalPoints = 0;
        boolean testFailed = false;
        final Evaluation evaluation = new Evaluation(config);
        final Evaluation.Result result = evaluation.result;
        for (final GroupDefinition groupDef : config.getGroups())
        {
            final Evaluation.Group group = new Evaluation.Group(groupDef);

            for (final String ruleId : groupDef.getRuleIds())
            {
                final RuleDefinition ruleDef = config.getRule(ruleId);
                final Evaluation.Rule rule = new Evaluation.Rule(ruleDef, groupDef.isEnabled());

                evaluateRule(rule, xpathCompiler, docNode);

                if (rule.getStatus().isFailed() && rule.getDefinition().isFailsTest())
                {
                    testFailed = true;
                }

                group.addRule(rule);

            }

            group.computePoints();
            points += group.getPoints();
            totalPoints += group.getTotalPoints();

            result.addGroup(group);
        }

        result.setPoints(points);
        result.setTotalPoints(totalPoints);

        final double pointsPercentage = getPercentage(points, totalPoints);

        // determine the test's rating and whether it has failed
        String rating = null;
        for (final RatingDefinition ratingDef : config.getRatings())
        {
            if (ratingDef.isEnabled() && pointsPercentage <= ratingDef.getValue())
            {
                rating = ratingDef.getName();
                testFailed = testFailed || ratingDef.isFailsTest();

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

        rule.conclude();
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

    private Configuration parseConfiguration() throws ValidationError, FileNotFoundException, IOException
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
