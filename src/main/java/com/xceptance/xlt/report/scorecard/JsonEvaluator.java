package com.xceptance.xlt.report.scorecard;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.io.Files;

import dev.harrel.jsonschema.Validator;
import dev.harrel.jsonschema.ValidatorFactory;
import dev.harrel.jsonschema.providers.OrgJsonNode;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.UnprefixedElementMatchingPolicy;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmNode;

/**
 * Evaluator for static JSON scorecard configurations.
 */
public class JsonEvaluator extends AbstractEvaluator
{
    private static final String SCHEMA_RESOURCE_PATH = "configuration-schema.json";

    public JsonEvaluator(final File configFile, final Processor processor)
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

    /**
     * Performs the actual evaluation of the scorecard configuration against the document.
     *
     * @param config
     *            the parsed configuration
     * @param documentFile
     *            the XML file to evaluate against
     * @return the resulting scorecard
     * @throws SaxonApiException
     *             thrown if XPath evaluation fails
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
}
