package com.xceptance.xlt.report.scorecard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;

import com.xceptance.xlt.report.scorecard.builder.ScorecardBuilder;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.UnprefixedElementMatchingPolicy;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

/**
 * Evaluator for Groovy-based scorecard configurations.
 */
public class GroovyEvaluator extends AbstractEvaluator
{
    public GroovyEvaluator(final File configFile, final Processor processor)
    {
        super(configFile, processor);
    }

    @Override
    public Scorecard evaluate(final File documentFile)
    {
        try
        {
            final XdmNode docNode = processor.newDocumentBuilder().build(documentFile);
            final XPathCompiler xpathCompiler = processor.newXPathCompiler();
            xpathCompiler.setUnprefixedElementMatchingPolicy(UnprefixedElementMatchingPolicy.DEFAULT_NAMESPACE);

            final Configuration config = parseGroovyConfiguration(docNode, xpathCompiler);
            return doEvaluate(config, docNode, xpathCompiler);
        }
        catch (final Exception ex)
        {
            return Scorecard.error(ex);
        }
    }

    protected Configuration parseGroovyConfiguration(final XdmNode document, final XPathCompiler compiler)
        throws ValidationException, IOException
    {
        final CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(GroovySecurityUtils.createSecureCustomizer());

        final Binding binding = new Binding();
        binding.setVariable("xpath", new ScorecardData(document, compiler));
        binding.setVariable("properties", new ScorecardProperties());
        binding.setVariable("builder", new ScorecardBuilder());

        final GroovyShell shell = new GroovyShell(binding, config);

        try
        {
            final Object result = shell.evaluate(configFile);
            if (result instanceof Configuration)
            {
                return (Configuration) result;
            }
            if (result instanceof ScorecardBuilder)
            {
                return ((ScorecardBuilder) result).build();
            }

            // Fallback: check if 'builder' variable was used
            final Object builderVar = binding.getVariable("builder");
            if (builderVar instanceof ScorecardBuilder)
            {
                return ((ScorecardBuilder) builderVar).build();
            }

            throw new ValidationException("Groovy script must return a Configuration object or ScorecardBuilder, or use the 'builder' binding.");
        }
        catch (final Exception e)
        {
            throw new ValidationException("Failed to evaluate Groovy configuration: " + e.getMessage(), e);
        }
    }

    protected Scorecard doEvaluate(final Configuration config, final XdmNode docNode, final XPathCompiler xpathCompiler)
        throws SaxonApiException
    {
        int points = 0, totalPoints = 0;
        boolean testFailed = false;

        final Scorecard scorecard = new Scorecard(config);
        final Scorecard.Result result = scorecard.result;
        final List<Scorecard.Group> erroneousGroups = new ArrayList<>();

        for (final GroupDefinition groupDef : config.getGroups())
        {
            final Scorecard.Group group = new Scorecard.Group(groupDef);
            for (final String ruleId : groupDef.getRuleIds())
            {
                final RuleDefinition ruleDef = config.getRule(ruleId);
                final Scorecard.Rule rule = new Scorecard.Rule(ruleDef, groupDef.isEnabled());
                evaluateRule(rule, xpathCompiler, docNode, config::getSelector);
                group.addRule(rule);
            }

            testFailed = conclude(group) | testFailed;
            points += group.getPoints();
            totalPoints += group.getTotalPoints();

            if (group.getStatus().isError())
            {
                erroneousGroups.add(group);
            }
            result.addGroup(group);
        }

        if (!erroneousGroups.isEmpty())
        {
            if (StringUtils.isBlank(result.getError()))
            {
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
            result.setPoints(points);
            result.setTotalPoints(totalPoints);
            final double pointsPercentage = getPercentage(points, totalPoints);

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
            check.setValue(value);
        }
    }

    private boolean evaluateConditionSafe(final String condition, final XPathCompiler compiler, final XdmValue contextValue)
    {
        String expr = StringUtils.strip(condition);
        if (StringUtils.startsWithAny(expr, "=", "<", ">", "!="))
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

    private void conclude(final Scorecard.Rule rule)
    {
        if (!rule.isEnabled())
        {
            return;
        }

        Status lastStatus = Status.PASSED;
        for (final Scorecard.Rule.Check c : rule.getChecks())
        {
            final Status checkStatus = c.getStatus();
            if (checkStatus.isSkipped())
            {
                continue;
            }

            if (!checkStatus.isPassed())
            {
                lastStatus = checkStatus;
                if (checkStatus.isError())
                {
                    rule.setMessage(String.format("[Check #%d] %s", c.getIndex(), c.getErrorMessage()));
                    break;
                }
            }
        }

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
        if (!group.isEnabled())
        {
            return false;
        }

        Scorecard.Rule firstMatch = null, lastMatch = null;
        int maxPoints = 0, sumPointsTotal = 0, sumPointsMatching = 0;
        boolean somePassed = false, someFailed = false, someError = false;

        final List<Scorecard.Rule> rules = group.getRules();
        for (final Scorecard.Rule rule : rules)
        {
            if (!rule.getDefinition().isEnabled())
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
