package com.xceptance.xlt.report.scorecard.builder;

import java.util.Map;

import com.xceptance.xlt.report.scorecard.RuleDefinition;
import com.xceptance.xlt.report.scorecard.RuleDefinition.Check;
import com.xceptance.xlt.report.scorecard.TestFailTrigger;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Builder for {@link RuleDefinition}.
 */
public class RuleBuilder
{
    private String id;

    private String name;

    private String description;

    private boolean enabled = true;

    private boolean failsTest = false;

    private TestFailTrigger testFailTrigger = TestFailTrigger.PASSED; // Default

    private boolean negateResult = false;

    private int points = 0;

    private final ChecksBuilder checksBuilder = new ChecksBuilder();

    private final MessagesBuilder messagesBuilder = new MessagesBuilder();

    public void id(String id)
    {
        this.id = id;
    }

    public void name(String name)
    {
        this.name = name;
    }

    public void description(String description)
    {
        this.description = description;
    }

    public void enabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void failsTest(boolean failsTest)
    {
        this.failsTest = failsTest;
    }

    public void failsOn(String trigger)
    {
        this.testFailTrigger = TestFailTrigger.valueOf(trigger);
    }

    public void negateResult(boolean negateResult)
    {
        this.negateResult = negateResult;
    }

    public void points(int points)
    {
        this.points = points;
    }

    public void checks(@DelegatesTo(ChecksBuilder.class) Closure<?> closure)
    {
        closure.setDelegate(checksBuilder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
    }

    public void messages(@DelegatesTo(MessagesBuilder.class) Closure<?> closure)
    {
        closure.setDelegate(messagesBuilder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
    }

    public RuleDefinition build()
    {
        Check[] checks = checksBuilder.build();
        RuleDefinition rule = new RuleDefinition(id, name, checks);
        rule.setDescription(description);
        rule.setEnabled(enabled);
        rule.setFailsTest(failsTest);
        rule.setFailsOn(testFailTrigger);
        rule.setNegateResult(negateResult);
        rule.setPoints(points);

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
