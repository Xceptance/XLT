package com.xceptance.xlt.report.scorecard.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.xceptance.xlt.report.scorecard.GroupDefinition;
import com.xceptance.xlt.report.scorecard.TestFailTrigger;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Builder for {@link GroupDefinition}.
 */
public class GroupBuilder
{
    private String id;

    private String name;

    private String description;

    private boolean enabled = true;

    private boolean failsTest = false;

    private TestFailTrigger testFailTrigger = TestFailTrigger.PASSED; // Default

    private String mode = "allPassed";

    private final List<String> ruleIds = new ArrayList<>();

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

    public void mode(String mode)
    {
        this.mode = mode;
    }

    public void rules(List<?> rules)
    {
        // Convert any GString instances to String to avoid ClassCastException
        // when Groovy string interpolation is used
        for (Object rule : rules)
        {
            this.ruleIds.add(rule.toString());
        }
    }

    public void messages(@DelegatesTo(MessagesBuilder.class) Closure<?> closure)
    {
        closure.setDelegate(messagesBuilder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
    }

    public GroupDefinition build()
    {
        GroupDefinition group = new GroupDefinition(id, name, ruleIds);
        group.setDescription(description);
        group.setEnabled(enabled);
        group.setFailsTest(failsTest);
        group.setFailsOn(testFailTrigger);
        group.setMode(GroupDefinition.Mode.valueOf(mode));

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
