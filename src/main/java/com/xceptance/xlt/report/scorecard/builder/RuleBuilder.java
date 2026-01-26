package com.xceptance.xlt.report.scorecard.builder;

import java.util.Map;

import com.xceptance.xlt.report.scorecard.RuleDefinition;
import com.xceptance.xlt.report.scorecard.RuleDefinition.Check;
import com.xceptance.xlt.report.scorecard.TestFailTrigger;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Groovy DSL builder for constructing {@link RuleDefinition} objects.
 * <p>
 * A rule defines a set of checks that validate specific conditions in the test report XML document. Rules can award
 * points when checks pass and optionally trigger test failure based on configurable conditions.
 * </p>
 * <p>
 * Rules support the following features:
 * </p>
 * <ul>
 * <li><b>Multiple checks:</b> Each rule can contain multiple checks evaluated in order</li>
 * <li><b>Points system:</b> Rules award configured points when all checks pass</li>
 * <li><b>Result negation:</b> The {@code negateResult} flag inverts pass/fail status</li>
 * <li><b>Test failure:</b> Rules can fail the overall test based on configurable triggers</li>
 * </ul>
 * <p>
 * Example usage in Groovy DSL:
 * </p>
 * 
 * <pre>{@code
 * rule {
 *     id "no-errors"
 *     name "No Errors"
 *     description "Ensures no errors occurred during the test"
 *     points 10
 *     failsTest true
 *     failsOn "NOTPASSED"
 *
 *     checks {
 *         check {
 *             selectorId "error_count"
 *             condition "= 0"
 *         }
 *     }
 *
 *     messages {
 *         success "No errors detected"
 *         fail "Errors were found in the test run"
 *     }
 * }
 * }</pre>
 *
 * @see RuleDefinition
 * @see RulesBuilder
 * @see CheckBuilder
 */
public class RuleBuilder
{
    /** Unique identifier for this rule, used for referencing from groups */
    private String id;

    /** Human-readable display name shown in reports */
    private String name;

    /** Optional detailed description of what this rule validates */
    private String description;

    /** Whether this rule is active; disabled rules are skipped and award no points */
    private boolean enabled = true;

    /** Whether this rule can cause the test to fail (combined with failsOn) */
    private boolean failsTest = false;

    /**
     * Defines when the rule triggers a test failure. Default is NOTPASSED (fails when rule doesn't pass). Alternative is
     * PASSED (fails when rule passes, for "shouldn't happen" rules).
     */
    private TestFailTrigger testFailTrigger = TestFailTrigger.NOTPASSED;

    /** If true, inverts the pass/fail status (passed becomes failed and vice versa) */
    private boolean negateResult = false;

    /** Points awarded when this rule passes */
    private int points = 0;

    /** Builder for nested check definitions */
    private final ChecksBuilder checksBuilder = new ChecksBuilder();

    /** Builder for success/fail messages */
    private final MessagesBuilder messagesBuilder = new MessagesBuilder();

    /**
     * Sets the unique identifier for this rule.
     *
     * @param id
     *               the rule identifier, must be unique and used for group references
     */
    public void id(String id)
    {
        this.id = id;
    }

    /**
     * Sets the display name for this rule.
     *
     * @param name
     *                 human-readable name shown in reports
     */
    public void name(String name)
    {
        this.name = name;
    }

    /**
     * Sets an optional description for this rule.
     *
     * @param description
     *                        detailed explanation of what the rule validates
     */
    public void description(String description)
    {
        this.description = description;
    }

    /**
     * Enables or disables this rule.
     *
     * @param enabled
     *                    if false, this rule is skipped during evaluation
     */
    public void enabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * Sets whether this rule can trigger test failure.
     * <p>
     * When combined with {@link #failsOn(String)}, determines whether and when a failed rule causes the entire test to
     * fail.
     * </p>
     *
     * @param failsTest
     *                      if true, this rule may cause test failure based on failsOn trigger
     */
    public void failsTest(boolean failsTest)
    {
        this.failsTest = failsTest;
    }

    /**
     * Sets the condition that triggers test failure.
     * <p>
     * Valid values:
     * </p>
     * <ul>
     * <li>{@code "NOTPASSED"} - test fails when rule does not pass (default)</li>
     * <li>{@code "PASSED"} - test fails when rule passes (for "shouldn't happen" rules)</li>
     * </ul>
     *
     * @param trigger
     *                    the trigger condition name
     */
    public void failsOn(String trigger)
    {
        this.testFailTrigger = TestFailTrigger.valueOf(trigger);
    }

    /**
     * Sets whether to negate (invert) the rule result.
     * <p>
     * When true, a passing rule becomes failing and vice versa. This is useful for rules that check for the absence of
     * something.
     * </p>
     *
     * @param negateResult
     *                         if true, inverts pass/fail status
     */
    public void negateResult(boolean negateResult)
    {
        this.negateResult = negateResult;
    }

    /**
     * Sets the points awarded when this rule passes.
     *
     * @param points
     *                   non-negative point value
     */
    public void points(int points)
    {
        this.points = points;
    }

    /**
     * Defines the checks section containing validation conditions.
     * <p>
     * Each check within this section is evaluated in order. The rule fails if any enabled check fails.
     * </p>
     *
     * @param closure
     *                    the closure defining checks using {@link ChecksBuilder}
     */
    public void checks(@DelegatesTo(ChecksBuilder.class) Closure<?> closure)
    {
        closure.setDelegate(checksBuilder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
    }

    /**
     * Defines custom success and failure messages for this rule.
     *
     * @param closure
     *                    the closure defining messages using {@link MessagesBuilder}
     */
    public void messages(@DelegatesTo(MessagesBuilder.class) Closure<?> closure)
    {
        closure.setDelegate(messagesBuilder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
    }

    /**
     * Builds and returns the configured {@link RuleDefinition}.
     * <p>
     * Assembles the rule from all configured properties, checks, and messages.
     * </p>
     *
     * @return a new RuleDefinition instance with all configured properties
     */
    public RuleDefinition build()
    {
        // Build the array of checks from the nested builder
        Check[] checks = checksBuilder.build();

        // Create the rule with required fields
        RuleDefinition rule = new RuleDefinition(id, name, checks);

        // Apply optional configuration
        rule.setDescription(description);
        rule.setEnabled(enabled);
        rule.setFailsTest(failsTest);
        rule.setFailsOn(testFailTrigger);
        rule.setNegateResult(negateResult);
        rule.setPoints(points);

        // Apply custom messages if defined
        Map<String, String> messages = messagesBuilder.build();
        if (messages.containsKey("success"))
        {
            rule.setSuccessMessage(messages.get("success"));
        }
        if (messages.containsKey("fail"))
        {
            rule.setFailMessage(messages.get("fail"));
        }

        return rule;
    }
}
