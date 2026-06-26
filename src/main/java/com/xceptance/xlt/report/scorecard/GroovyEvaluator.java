/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;

import com.xceptance.xlt.report.scorecard.Scorecard.LogEntry;
import com.xceptance.xlt.report.scorecard.groovy.GroovySecurityUtils;
import com.xceptance.xlt.report.scorecard.groovy.MetricsHelper;
import com.xceptance.xlt.report.scorecard.groovy.ScorecardData;
import com.xceptance.xlt.report.scorecard.groovy.ScorecardLogger;
import com.xceptance.xlt.report.scorecard.groovy.ScorecardProperties;
import com.xceptance.xlt.report.scorecard.groovy.builder.ScorecardBuilder;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.UnprefixedElementMatchingPolicy;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmNode;

/**
 * Evaluator for Groovy-based scorecard configurations.
 */
public class GroovyEvaluator extends AbstractEvaluator
{
    public GroovyEvaluator(final File configFile, final Processor processor)
    {
        super(configFile, processor);
    }

    private Binding binding;

    @Override
    public Scorecard evaluate(final File documentFile)
    {
        Scorecard scorecard = null;
        try
        {
            final XdmNode docNode = processor.newDocumentBuilder().build(documentFile);
            final XPathCompiler xpathCompiler = processor.newXPathCompiler();
            xpathCompiler.setUnprefixedElementMatchingPolicy(UnprefixedElementMatchingPolicy.DEFAULT_NAMESPACE);

            final Configuration config = parseGroovyConfiguration(docNode, xpathCompiler);
            scorecard = doEvaluate(config, docNode, xpathCompiler);
        }
        catch (final Exception ex)
        {
            scorecard = Scorecard.error(ex);
        }

        // If we have an error and logs, bundle them together
        if (binding != null && scorecard != null)
        {
            final ScorecardLogger logger = (ScorecardLogger) binding.getVariable("log");
            if (logger != null && !logger.getLogs().isEmpty())
            {
                // Copy logs to the result for test access
                scorecard.result.setLogs(logger.getLogs());

                // If there's already an error, update it with the log
                if (!scorecard.result.getErrors().isEmpty())
                {
                    final String logText = logger.getLogs().stream().map(LogEntry::toString).collect(Collectors.joining("\n"));
                    scorecard.result.updateFirstErrorLog(logText);
                }
            }
        }

        return scorecard;
    }

    protected Configuration parseGroovyConfiguration(final XdmNode document, final XPathCompiler compiler)
        throws ValidationException, IOException
    {
        final CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(GroovySecurityUtils.createSecureCustomizer());

        final ScorecardBuilder builder = new ScorecardBuilder();
        final ScorecardLogger logger = new ScorecardLogger();

        binding = new Binding();
        binding.setVariable("xpath", new ScorecardData(document, compiler));
        binding.setVariable("properties", new ScorecardProperties());
        binding.setVariable("builder", builder);
        binding.setVariable("metrics", new MetricsHelper());
        binding.setVariable("log", logger);

        final GroovyShell shell = new GroovyShell(binding, config);

        try
        {
            // populate the builder
            shell.evaluate(configFile);

            // let the builder create the configuration
            return builder.build();
        }
        catch (final Exception e)
        {
            logger.error("Failed to evaluate Groovy configuration", e);
            throw new ValidationException("Failed to evaluate Groovy configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Performs the actual evaluation of the scorecard configuration against the document.
     *
     * @param config
     *            the parsed configuration
     * @param docNode
     *            the XML document to evaluate against
     * @param xpathCompiler
     *            the XPath compiler to use
     * @return the resulting scorecard
     * @throws SaxonApiException
     *             thrown if XPath evaluation fails
     */
    protected Scorecard doEvaluate(final Configuration config, final XdmNode docNode, final XPathCompiler xpathCompiler)
        throws SaxonApiException
    {
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
            // collect issues from erroneous rules
            for (final Scorecard.Group g : erroneousGroups)
            {
                for (final Scorecard.Rule r : g.getRules())
                {
                    if (r.getStatus().isError() && StringUtils.isNotBlank(r.getMessage()))
                    {
                        final String location = String.format("group '%s' / rule '%s'", g.getId(), r.getId());
                        result.addIssue(new Scorecard.Issue("ERROR", r.getMessage(), location));
                    }
                }
            }
        }

        // set overall number of achieved and achievable points
        result.setPoints(points);
        result.setTotalPoints(totalPoints);

        String rating = null;

        // check for manually active ratings first
        final RatingDefinition activeRating = config.getRatings().stream().filter(r -> r.isActive() && r.isEnabled()).findFirst()
                                                    .orElse(null);

        if (activeRating != null)
        {
            // manual rating selection - use the first active rating
            rating = activeRating.getId();
            testFailed = testFailed || activeRating.isFailsTest();
            result.setPointsPercentage(null);  // Points percentage meaningless for manual
        }
        else
        {
            // auto-calculate rating based on points percentage
            final double pointsPercentage = getPercentage(points, totalPoints);
            for (final RatingDefinition ratingDef : config.getRatings())
            {
                if (ratingDef.isEnabled() && pointsPercentage <= ratingDef.getValue())
                {
                    rating = ratingDef.getId();
                    testFailed = testFailed || ratingDef.isFailsTest();
                    break;
                }
            }
            result.setPointsPercentage(pointsPercentage);
        }

        // set final values
        result.setTestFailed(testFailed);
        result.setRating(rating);

        return scorecard;
    }
}
