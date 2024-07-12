/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.report.scorecard;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.io.Files;
import com.thoughtworks.xstream.XStream;
import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.report.scorecard.GroupDefinition.Mode;
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
    private static final String SCHEMA_RESOURCE_PATH = "configuration-schema.json";

    private final File configFile;

    private final Processor processor;

    /**
     * Creates a new evaluator instance that uses the given configuration JSON file.
     *
     * @param configFile
     *            the configuration JSON file to use
     */
    public Evaluator(final File configFile)
    {
        ParameterCheckUtils.isReadableFile(configFile, "configFile");

        this.configFile = configFile;
        this.processor = new Processor(false);
    }

    /**
     * Evaluates the given XML file.
     *
     * @param documentFile
     *            the XML file to evaluate
     * @return resulting scorecard
     */
    public Scorecard evaluate(final File documentFile)
    {
        ParameterCheckUtils.isNotNull(documentFile, "documentFile");

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

    /**
     * Writes the given scorecard as serialized XML to the given output file.
     *
     * @param scorecard
     *            the scorecard to be written
     * @param outputFile
     *            the target output file
     * @throws IOException
     *             thrown upon failure to write to given file
     */
    public void writeScorecardToFile(final Scorecard scorecard, final File outputFile) throws IOException
    {
        try (final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outputFile), XltConstants.UTF8_ENCODING))
        {
            writeScorecard(scorecard, osw);
        }
    }

    /**
     * Writes the given scorecard as serialized XML to the given destination writer.
     *
     * @param scorecard
     *            the scorecard to be written
     * @param writer
     *            the destination to write serialized XML to
     * @throws IOException
     *             thrown if scorecard could not be written
     */
    public void writeScorecard(final Scorecard scorecard, final Writer writer) throws IOException
    {
        // writer the XML preamble
        writer.write(XltConstants.XML_HEADER);

        // create and configure XStream instance
        final XStream xstream = new XStream(new SanitizingDomDriver());
        xstream.autodetectAnnotations(true);
        xstream.aliasSystemAttribute(null, "class");
        xstream.setMode(XStream.NO_REFERENCES);

        // let XStream do its job
        xstream.toXML(scorecard, writer);
    }

    protected Configuration parseConfiguration() throws ValidationException, FileNotFoundException, IOException
    {
        final JSONObject schemaJSON;
        try (final InputStream is = getClass().getResourceAsStream(SCHEMA_RESOURCE_PATH))
        {
            schemaJSON = new JSONObject(new JSONTokener(is));
        }
        catch (final JSONException je)
        {
            throw new ValidationException("Failed to parse JSON schema file", je);
        }

        final JSONObject configJSON;
        try (final BufferedReader reader = Files.newReader(configFile, StandardCharsets.UTF_8))
        {
            configJSON = new JSONObject(new JSONTokener(reader));
        }
        catch (final JSONException je)
        {
            throw new ValidationException("Could not parse configuration file '" + configFile.getName() + "' as JSON", je);
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

    protected Scorecard doEvaluate(final Configuration config, final File documentFile) throws SaxonApiException
    {
        final XdmNode docNode = processor.newDocumentBuilder().build(documentFile);
        final XPathCompiler xpathCompiler = processor.newXPathCompiler();
        xpathCompiler.setUnprefixedElementMatchingPolicy(UnprefixedElementMatchingPolicy.DEFAULT_NAMESPACE);
        xpathCompiler.setCaching(true);

        Integer points = 0, totalPoints = 0; // counters for achieved and achievable points
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

        final boolean evaluationFailed = !erroneousGroups.isEmpty();
        if (evaluationFailed)
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
                    result.setError(errorMessagesJoined);
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

    private void evaluateRuleCheck(final Scorecard.Rule.Check check, final XPathCompiler compiler, final XdmNode document,
                                   final Function<String, SelectorDefinition> selectorLookup)
    {
        final String selector;
        // pick the right selector (specified directly or referenced by ID)
        {
            final String selectorId = check.getDefinition().getSelectorId();
            if (selectorId != null)
            {
                selector = selectorLookup.apply(selectorId).getExpression();
            }
            else
            {
                selector = check.getDefinition().getSelector();
            }
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

            // remember most recent check status that doesn't indicate a passed check or just the very first check
            // status if all checks did pass
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
        // - + PASSED if at least one rule passed and mode is 'first' or 'last' OR if all rules did pass and mode is
        // 'all'
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
        final Mode mode = group.getDefinition().getMode();
        if (mode == Mode.allPassed)
        {
            groupStatus = someFailed ? Status.FAILED : somePassed ? Status.PASSED : Status.SKIPPED;
            points = sumPointsMatching;
            totalPoints = sumPointsTotal;

            rulesThatMayFailTest = rules;
        }
        else if (mode == Mode.firstPassed || mode == Mode.lastPassed)
        {
            final Scorecard.Rule triggerRule = (mode == Mode.firstPassed) ? firstMatch : lastMatch;
            final int idx = triggerRule != null ? rules.indexOf(triggerRule) : -1;

            groupStatus = somePassed ? Status.PASSED : someFailed ? Status.FAILED : Status.SKIPPED;
            points = Optional.ofNullable(triggerRule).map(Scorecard.Rule::getPoints).orElse(0);
            totalPoints = maxPoints;
            // need to inspect the group's status trigger rule as well
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
