package com.xceptance.xlt.report.scorecard;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.yaml.snakeyaml.Yaml;

import com.google.common.io.Files;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.UnprefixedElementMatchingPolicy;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import java.net.URI;
import dev.harrel.jsonschema.Validator;
import dev.harrel.jsonschema.ValidatorFactory;
import dev.harrel.jsonschema.providers.OrgJsonNode;

/**
 * Evaluator for static (JSON/YAML) scorecard configurations.
 */
public class StaticEvaluator extends AbstractEvaluator
{
    private static final String SCHEMA_RESOURCE_PATH = "configuration-schema.json";

    public StaticEvaluator(final File configFile, final Processor processor)
    {
        super(configFile, processor);
    }

    @Override
    public Scorecard evaluate(final File documentFile)
    {
        try
        {
            final Configuration config = parseConfiguration();
            return doEvaluate(config, documentFile);
        }
        catch (final Exception ex)
        {
            return Scorecard.error(ex);
        }
    }

    protected Configuration parseConfiguration() throws ValidationException, IOException
    {
        final JSONObject schemaJSON;
        try (final InputStream is = Evaluator.class.getResourceAsStream(SCHEMA_RESOURCE_PATH))
        {
            schemaJSON = new JSONObject(new JSONTokener(is));
        }
        catch (final JSONException je)
        {
            throw new ValidationException("Failed to parse JSON schema file", je);
        }

        final JSONObject configJSON;
        final String fileName = configFile.getName().toLowerCase();

        if (fileName.endsWith(".yaml") || fileName.endsWith(".yml"))
        {
            try (final BufferedReader reader = Files.newReader(configFile, StandardCharsets.UTF_8))
            {
                final Yaml yamlParser = new Yaml();
                final Map<String, Object> map = yamlParser.load(reader);
                configJSON = new JSONObject(map);
            }
            catch (final Exception e)
            {
                throw new ValidationException("Could not parse configuration file '" + configFile.getName() + "' as YAML", e);
            }
        }
        else
        {
            try (final BufferedReader reader = Files.newReader(configFile, StandardCharsets.UTF_8))
            {
                configJSON = new JSONObject(new JSONTokener(reader));
            }
            catch (final JSONException je)
            {
                throw new ValidationException("Could not parse configuration file '" + configFile.getName() + "' as JSON", je);
            }
        }

        final Validator validator = new ValidatorFactory().withJsonNodeFactory(new OrgJsonNode.Factory()).createValidator();
        final URI schemaURI = validator.registerSchema(schemaJSON);
        final Validator.Result validationResult = validator.validate(schemaURI, configJSON);

        if (!validationResult.isValid())
        {
            throw new ValidationException("Configuration file '" + configFile.getName() + "' is malformed -> " +
                                          validationResult.getErrors().stream().map(e -> e.getInstanceLocation() + ": " + e.getError())
                                                          .collect(Collectors.joining(", ")));
        }

        try
        {
            return Configuration.fromJSON(configJSON);
        }
        catch (final Exception e)
        {
            throw new ValidationException("Configuration file '" + configFile.getName() + "' is malformed", e);
        }
    }

    /**
     * Performs the actual evaluation of the scorecard configuration against the document.
     *
     * @param config
     *                         the parsed configuration
     * @param documentFile
     *                         the XML file to evaluate against
     * @return the resulting scorecard
     * @throws SaxonApiException
     *                               thrown if XPath evaluation fails
     */
    protected Scorecard doEvaluate(final Configuration config, final File documentFile) throws SaxonApiException
    {
        final XdmNode docNode = processor.newDocumentBuilder().build(documentFile);
        final XPathCompiler xpathCompiler = processor.newXPathCompiler();
        xpathCompiler.setUnprefixedElementMatchingPolicy(UnprefixedElementMatchingPolicy.DEFAULT_NAMESPACE);
        xpathCompiler.setCaching(true);

        int points = 0, totalPoints = 0; // counters for achieved and achievable points
        boolean testFailed = false; // whether to mark test as failed

        // initialize scorecard and its result objects
        final Scorecard scorecard = new Scorecard(config);
        final Scorecard.Result result = scorecard.result;
        // remember the erroneous groups to decide whether evaluation has a meaningful result later on
        final List<Scorecard.Group> erroneousGroups = new ArrayList<>();

        // loop through the list of configured groups (in definition order)
        for (final GroupDefinition groupDef : config.getGroups())
        {
            // create a group result object
            final Scorecard.Group group = new Scorecard.Group(groupDef);
            // loop through the group's rule IDs (in definition order)
            for (final String ruleId : groupDef.getRuleIds())
            {
                // lookup the rule's definition for this ID
                final RuleDefinition ruleDef = config.getRule(ruleId);
                // create a rule result object
                final Scorecard.Rule rule = new Scorecard.Rule(ruleDef, groupDef.isEnabled());
                // evaluate the rule
                evaluateRule(rule, xpathCompiler, docNode, config::getSelector);
                // add rule result to group
                group.addRule(rule);
            }

            // conclude the evaluation of the group
            testFailed = conclude(group) | testFailed;

            // add number of group's achieved and achievable points to the counters
            points += group.getPoints();
            totalPoints += group.getTotalPoints();

            if (group.getStatus().isError())
            {
                erroneousGroups.add(group);
            }
            // add group result to scorecard result
            result.addGroup(group);
        }

        // determine if evaluation failed due to erroneous groups
        if (!erroneousGroups.isEmpty())
        {
            // do not "overwrite" any previous error
            if (StringUtils.isBlank(result.getError()))
            {
                // collect error messages from erroneous groups/rules and join them with two new-line characters
                final String errorMessagesJoined = erroneousGroups.stream().flatMap(g -> g.getRules().stream())
                                                                  .filter(r -> r.getStatus().isError() &&
                                                                               StringUtils.isNotBlank(r.getMessage()))
                                                                  .map(r -> String.format("Error evaluating rule '%s': %s", r.getId(),
                                                                                          r.getMessage()))
                                                                  .collect(Collectors.joining("\n\n"));
                if (StringUtils.isNotBlank(errorMessagesJoined))
                {
                    result.addError(errorMessagesJoined, null);
                }
            }
        }
        else
        {
            // set overall number of achieved and achievable points
            result.setPoints(points);
            result.setTotalPoints(totalPoints);

            // compute final score
            final double pointsPercentage = getPercentage(points, totalPoints);

            // determine the test's rating and whether it has failed
            String rating = null;
            for (final RatingDefinition ratingDef : config.getRatings())
            {
                if (ratingDef.isEnabled() && pointsPercentage <= ratingDef.getValue())
                {
                    rating = ratingDef.getId();
                    testFailed = testFailed || ratingDef.isFailsTest();
                    break;
                }
            }

            // set final values
            result.setTestFailed(testFailed);
            result.setPointsPercentage(pointsPercentage);
            result.setRating(rating);
        }

        return scorecard;
    }

    /**
     * Evaluates a single rule by processing all its checks and determining the final rule status.
     *
     * @param rule
     *                           the rule to evaluate
     * @param compiler
     *                           the XPath compiler
     * @param document
     *                           the document to evaluate against
     * @param selectorLookup
     *                           function to lookup selector definitions by ID
     */
    private void evaluateRule(final Scorecard.Rule rule, final XPathCompiler compiler, final XdmNode document,
                              final Function<String, SelectorDefinition> selectorLookup)
    {
        for (final RuleDefinition.Check check : rule.getDefinition().getChecks())
        {
            final Scorecard.Rule.Check ruleCheck = new Scorecard.Rule.Check(check, rule.isEnabled());
            if (ruleCheck.isEnabled())
            {
                evaluateRuleCheck(ruleCheck, compiler, document, selectorLookup);
            }
            rule.addCheck(ruleCheck);
        }
        conclude(rule);
    }

    /**
     * Evaluates a single check within a rule.
     *
     * @param check
     *                           the check to evaluate
     * @param compiler
     *                           the XPath compiler
     * @param document
     *                           the document to evaluate against
     * @param selectorLookup
     *                           function to lookup selector definitions by ID
     */
    private void evaluateRuleCheck(final Scorecard.Rule.Check check, final XPathCompiler compiler, final XdmNode document,
                                   final Function<String, SelectorDefinition> selectorLookup)
    {
        // check for manually set status first
        final Status manualStatus = check.getDefinition().getManualStatus();
        if (manualStatus != null)
        {
            check.setStatus(manualStatus);
            check.setErrorMessage(check.getDefinition().getManualErrorMessage());
            if (check.getDefinition().isDisplayValue())
            {
                check.setValue(formatValue(check.getDefinition().getManualValue(), check.getDefinition().getFormatter()));
            }
            return;
        }

        // pick the right selector (specified directly or referenced by ID)
        final String selector;
        final String selectorId = check.getDefinition().getSelectorId();
        if (selectorId != null)
        {
            selector = selectorLookup.apply(selectorId).getExpression();
        }
        else
        {
            selector = check.getDefinition().getSelector();
        }

        Status status = Status.FAILED;
        String message = null, value = null;
        try
        {
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
            check.setValue(formatValue(value, check.getDefinition().getFormatter()));
        }
    }

    /**
     * Safely evaluates a condition expression against a context value. If the expression starts with a comparison operator,
     * it prepends '.' to make it a valid XPath.
     *
     * @param condition
     *                         the condition expression
     * @param compiler
     *                         the XPath compiler
     * @param contextValue
     *                         the context value to evaluate against
     * @return true if the condition evaluates to true, false otherwise
     */
    private boolean evaluateConditionSafe(final String condition, final XPathCompiler compiler, final XdmValue contextValue)
    {
        // strip any leading/trailing whitespace
        String expr = StringUtils.strip(condition);
        // if expression starts with a comparison operator, put a '.' in front of it
        if (startsWithAny(expr, "=", "<", ">", "!="))
        {
            expr = ". " + expr;
        }
        try
        {
            return contextValue.select(compiler.compile(expr).asStep()).asAtomic().getBooleanValue();
        }
        catch (final Exception e)
        {
            return false;
        }
    }

    /**
     * Concludes the evaluation of a rule by determining its final status based on its checks. Sets the rule's status,
     * message, and points accordingly.
     *
     * @param rule
     *                 the rule to conclude
     */
    private void conclude(final Scorecard.Rule rule)
    {
        // nothing to do for disabled rules
        if (!rule.isEnabled())
        {
            return;
        }

        Status lastStatus = Status.PASSED;
        // loop through rule's checks
        for (final Scorecard.Rule.Check c : rule.getChecks())
        {
            final Status checkStatus = c.getStatus();
            // ignore skipped checks
            if (checkStatus.isSkipped())
            {
                continue;
            }

            // remember most recent check status that doesn't indicate a passed check
            // or just the very first check status if all checks did pass
            if (!checkStatus.isPassed())
            {
                lastStatus = checkStatus;
                // encountered erroneous check -> set rule message to the check's error message and stop looping
                if (checkStatus.isError())
                {
                    rule.setMessage(String.format("[Check #%d] %s", c.getIndex(), c.getErrorMessage()));
                    break;
                }
            }
        }

        // take action according to rule's final status
        // (pass: set message to success message and points to all achievable points, fail: set message to fail message)

        // negate rule status if desired
        if (rule.getDefinition().isNegateResult())
        {
            lastStatus = lastStatus.negate();
        }
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

    /**
     * Concludes the evaluation of a group by determining its final status based on its rules. Sets the group's status,
     * message, points, and determines if the test should fail.
     *
     * @param group
     *                  the group to conclude
     * @return true if the test should fail due to this group's evaluation
     */
    private boolean conclude(final Scorecard.Group group)
    {
        // nothing to do for disabled groups
        if (!group.isEnabled())
        {
            return false;
        }

        Scorecard.Rule firstMatch = null, lastMatch = null;
        int maxPoints = 0, sumPointsTotal = 0, sumPointsMatching = 0;
        boolean somePassed = false, someFailed = false, someError = false;

        // loop through group's rules and determine
        // - the points of the first matching rule,
        // - the points of the last matching rule,
        // - the points of all matching rules (sum of),
        // - the maximum number of all rules' points and
        // - the overall sum of all rules' points
        // - the rules' messages
        // - the overall status of the group
        // - + PASSED if at least one rule passed and mode is 'first' or 'last' OR if all rules did pass and mode is 'all'
        // - + FAILED if at least one rule failed if mode is 'all' OR all rules did fail and mode is 'first' or 'last'
        // - + ERROR if some rule was erroneous
        final List<Scorecard.Rule> rules = group.getRules();
        for (final Scorecard.Rule rule : rules)
        {
            // rules must be enabled in order to participate in point calculation
            // N.B. Rule status is SKIPPED if and only if rule is disabled
            if (!rule.getDefinition().isEnabled()) // no need to check group for being enabled
            {
                continue;
            }

            final int pointsAchieved = rule.getPoints();
            final int rulePoints = rule.getDefinition().getPoints();
            final Status ruleStatus = rule.getStatus();

            if (ruleStatus.isError())
            {
                someError = true;
                break;
            }

            maxPoints = Math.max(maxPoints, rulePoints);
            sumPointsTotal += rulePoints;

            if (ruleStatus.isPassed())
            {
                somePassed = true;
                if (firstMatch == null)
                {
                    firstMatch = rule;
                }
                lastMatch = rule;
                sumPointsMatching += pointsAchieved;
            }
            else if (ruleStatus.isFailed())
            {
                someFailed = true;
            }
        }

        if (someError)
        {
            group.setStatus(Status.ERROR);
            group.setPoints(0);
            group.setTotalPoints(0);
            return false;
        }

        // pick the correct values for group's points and total points according to its mode
        final int points, totalPoints;
        final Status groupStatus;
        final List<Scorecard.Rule> rulesThatMayFailTest;
        final GroupDefinition.Mode mode = group.getDefinition().getMode();

        if (mode == GroupDefinition.Mode.allPassed)
        {
            groupStatus = someFailed ? Status.FAILED : somePassed ? Status.PASSED : Status.SKIPPED;
            points = sumPointsMatching;
            totalPoints = sumPointsTotal;
            rulesThatMayFailTest = rules;
        }
        else if (mode == GroupDefinition.Mode.firstPassed || mode == GroupDefinition.Mode.lastPassed)
        {
            final Scorecard.Rule triggerRule = (mode == GroupDefinition.Mode.firstPassed) ? firstMatch : lastMatch;
            final int idx = triggerRule != null ? rules.indexOf(triggerRule) : -1;

            groupStatus = somePassed ? Status.PASSED : someFailed ? Status.FAILED : Status.SKIPPED;
            points = triggerRule != null ? triggerRule.getPoints() : 0;
            totalPoints = maxPoints;
            rulesThatMayFailTest = idx < 0 ? rules : rules.subList(0, idx + 1);
        }
        else
        {
            groupStatus = Status.ERROR;
            points = 0;
            totalPoints = 0;
            rulesThatMayFailTest = Collections.emptyList();
        }

        boolean testFailed = rulesThatMayFailTest.stream().filter((rule) -> {
            final boolean ruleFailedTest = rule.mayFailTest();
            if (ruleFailedTest)
            {
                rule.setTestFailed();
            }
            return ruleFailedTest;
        }).count() > 0L;

        if (group.mayFailTest())
        {
            group.setTestFailed();
            testFailed = true;
        }

        group.setStatus(groupStatus);
        group.setPoints(points);
        group.setTotalPoints(totalPoints);

        final String groupMessage = groupStatus.isPassed() ? group.getDefinition().getSuccessMessage()
                                                           : groupStatus.isFailed() ? group.getDefinition().getFailMessage() : null;
        if (groupMessage != null)
        {
            group.setMessage(groupMessage);
        }

        return testFailed;
    }
}
