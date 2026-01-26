package com.xceptance.xlt.report.scorecard.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.xceptance.xlt.report.scorecard.GroupDefinition;
import com.xceptance.xlt.report.scorecard.TestFailTrigger;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Groovy DSL builder for constructing {@link GroupDefinition} objects.
 * <p>
 * Groups organize related rules into logical categories and define how their combined results contribute to the overall
 * score. They also control when the test should be marked as failed.
 * </p>
 * <p>
 * Groups support different evaluation modes that affect point calculation:
 * </p>
 * <ul>
 * <li><b>allPassed:</b> All rules contribute; total points = sum of passed rule points</li>
 * <li><b>firstPassed:</b> First passing rule determines points; like a priority list</li>
 * <li><b>lastPassed:</b> Last passing rule determines points; most specific match wins</li>
 * </ul>
 * <p>
 * Example usage in Groovy DSL:
 * </p>
 * 
 * <pre>{@code
 * group {
 *     id "stability"
 *     name "Stability Checks"
 *     description "Validates test stability metrics"
 *     mode "allPassed"
 *     failsTest true
 *     failsOn "PASSED"
 *
 *     rules ["no-errors", "no-timeouts", "success-rate"]
 *
 *     messages {
 *         success "All stability checks passed"
 *         fail "Some stability checks failed"
 *     }
 * }
 * }</pre>
 *
 * @see GroupDefinition
 * @see GroupsBuilder
 * @see RuleBuilder
 */
public class GroupBuilder
{
    /** Unique identifier for this group */
    private String id;

    /** Human-readable display name shown in reports */
    private String name;

    /** Optional detailed description of what this group validates */
    private String description;

    /** Whether this group is active; disabled groups contribute no points */
    private boolean enabled = true;

    /** Whether this group can cause the test to fail (combined with failsOn) */
    private boolean failsTest = false;

    /**
     * Defines when the group triggers a test failure. Default is PASSED (group causes failure when it passes). Can be set
     * to NOTPASSED for groups that must pass.
     */
    private TestFailTrigger testFailTrigger = TestFailTrigger.PASSED;

    /**
     * Evaluation mode determining how points are calculated. Valid values: allPassed, firstPassed, lastPassed
     */
    private String mode = "allPassed";

    /** List of rule IDs that belong to this group, evaluated in order */
    private final List<String> ruleIds = new ArrayList<>();

    /** Builder for success/fail messages */
    private final MessagesBuilder messagesBuilder = new MessagesBuilder();

    /**
     * Sets the unique identifier for this group.
     *
     * @param id
     *               the group identifier
     */
    public void id(String id)
    {
        this.id = id;
    }

    /**
     * Sets the display name for this group.
     *
     * @param name
     *                 human-readable name shown in reports
     */
    public void name(String name)
    {
        this.name = name;
    }

    /**
     * Sets an optional description for this group.
     *
     * @param description
     *                        detailed explanation of what the group validates
     */
    public void description(String description)
    {
        this.description = description;
    }

    /**
     * Enables or disables this group.
     *
     * @param enabled
     *                    if false, this group is skipped during evaluation
     */
    public void enabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * Sets whether this group can trigger test failure.
     *
     * @param failsTest
     *                      if true, this group may cause test failure based on failsOn trigger
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
     * <li>{@code "PASSED"} - test fails when group passes (default, for "not acceptable" groups)</li>
     * <li>{@code "NOTPASSED"} - test fails when group does not pass</li>
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
     * Sets the evaluation mode for this group.
     * <p>
     * Valid values:
     * </p>
     * <ul>
     * <li>{@code "allPassed"} - all rules contribute; points = sum of passed rule points</li>
     * <li>{@code "firstPassed"} - first passing rule determines points</li>
     * <li>{@code "lastPassed"} - last passing rule determines points</li>
     * </ul>
     *
     * @param mode
     *                 the evaluation mode name
     */
    public void mode(String mode)
    {
        this.mode = mode;
    }

    /**
     * Sets the list of rule IDs that belong to this group.
     * <p>
     * Rules are evaluated in the order specified. Each rule ID must reference a rule defined in the {@code rules} section
     * of the configuration.
     * </p>
     * <p>
     * Note: Accepts {@code List<?>} to handle Groovy GString interpolation. All elements are converted to String via
     * {@code toString()}.
     * </p>
     *
     * @param rules
     *                  list of rule IDs to include in this group
     */
    public void rules(List<?> rules)
    {
        // Convert any GString instances to String to avoid ClassCastException
        // when Groovy string interpolation is used (e.g., "${someVar}-rule")
        for (Object rule : rules)
        {
            this.ruleIds.add(rule.toString());
        }
    }

    /**
     * Defines custom success and failure messages for this group.
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
     * Builds and returns the configured {@link GroupDefinition}.
     * <p>
     * Assembles the group from all configured properties and rule references.
     * </p>
     *
     * @return a new GroupDefinition instance with all configured properties
     */
    public GroupDefinition build()
    {
        // Create group with required fields
        GroupDefinition group = new GroupDefinition(id, name, ruleIds);

        // Apply optional configuration
        group.setDescription(description);
        group.setEnabled(enabled);
        group.setFailsTest(failsTest);
        group.setFailsOn(testFailTrigger);
        group.setMode(GroupDefinition.Mode.valueOf(mode));

        // Apply custom messages if defined
        Map<String, String> messages = messagesBuilder.build();
        if (messages.containsKey("success"))
        {
            group.setSuccessMessage(messages.get("success"));
        }
        if (messages.containsKey("fail"))
        {
            group.setFailMessage(messages.get("fail"));
        }

        return group;
    }
}
