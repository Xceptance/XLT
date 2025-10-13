/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.mergerules;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.report.mergerules.MergeRule.AgentNameExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.AgentNamePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnNoMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.DropOnMatch;
import com.xceptance.xlt.report.mergerules.MergeRule.NewName;
import com.xceptance.xlt.report.mergerules.MergeRule.StopOnMatch;

/**
 * Agent Name Pattern Tests
 *
 * @author Rene Schwietzke (Xceptance GmbH)
 */
public class MergeRule_Agent_Test extends MergeRuleTestBase
{
    // =================================================================================
    // Agent Name Pattern Tests
    // =================================================================================

    @Test
    public void include() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new AgentNamePattern("MyAgent.*"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(0, rule.getExcludeConditions().length);
        assertEquals(1, rule.getIncludeConditions().length);

        // match 
        {
            var data = new RequestData("originalName");
            data.setAgentName("MyAgent123");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }

        // no match
        {
            RequestData data2 = new RequestData("originalName");
            data2.setAgentName("Any Agent");
            assertEquals(1, rule.process(data2));
            assertEquals("originalName", data2.getName());
        }
    }
    
    @Test
    public void exclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new AgentNameExcludePattern("MyAgent.*"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // match 
        RequestData data1 = new RequestData("originalName");
        data1.setAgentName("MyAgent123");
        assertEquals(1, rule.process(data1));
        assertEquals("originalName", data1.getName());

        // no match
        RequestData data2 = new RequestData("originalName");
        data2.setAgentName("Agent123");
        assertEquals(1, rule.process(data2));
        assertEquals("newName", data2.getName());
    }

    @Test
    public void include_exclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("NewName"),
                                new AgentNamePattern("MyAgent.*"),
                                new AgentNameExcludePattern("Fo+bar"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // both match, exclude wins
        {
            var data = new RequestData("originalName");
            data.setAgentName("MyAgentFoobar");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // no match
        {
            var data = new RequestData("originalName");
            data.setAgentName("AnythingElse");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // include only match
        {
            var data = new RequestData("originalName");
            data.setAgentName("MyAgent123");
            assertEquals(1, rule.process(data));
            assertEquals("NewName", data.getName());
        }
        // exclude only match
        {
            var data = new RequestData("originalName");
            data.setAgentName("Foobar");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }

    }

    @Test
    public void placeholderFull() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Agent:{a}"),
                                new AgentNamePattern("MyAgent(\\d+)"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        var data = new RequestData("originalName");
        data.setAgentName("MyAgent123");
        assertEquals(1, rule.process(data));
        assertEquals("Agent:MyAgent123", data.getName());
    }

    @Test
    public void placeholderButNoPattern() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Agent:{a}"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        var data = new RequestData("originalName");
        data.setAgentName("MyAgent123");
        assertEquals(1, rule.process(data));
        assertEquals("Agent:MyAgent123", data.getName());
    }
    
    @Test
    public void placeholderGroup0() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Agent:{a:0}"),
                                new AgentNamePattern("MyAgent(\\d+)"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        var data = new RequestData("originalName");
        data.setAgentName("MyAgent123");
        assertEquals(1, rule.process(data));
        assertEquals("Agent:MyAgent123", data.getName());
    }

    @Test
    public void placeholderAnyGroup() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("{a:1}:{a:2}:{a:1}"),
                                new AgentNamePattern("My(Agent)(\\d+)"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        var data = new RequestData("originalName");
        data.setAgentName("MyAgent123");
        assertEquals(1, rule.process(data));
        assertEquals("Agent:123:Agent", data.getName());
    }

    @Test
    public void stopOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Agent"),
                                new AgentNamePattern("AgentX.*"),
                                new StopOnMatch(true)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("originalName");
        data.setAgentName("AgentX42");
        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("Agent", data.getName());
    }

    @Test
    public void dropOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Agent:{a}"),
                                new AgentNamePattern("AgentY.*"),
                                new StopOnMatch(false),
                                new DropOnMatch(true)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("originalName");
        data.setAgentName("AgentY99");
        assertEquals(MergeRule.DROP, rule.process(data));
        assertEquals("originalName", data.getName());
    }

    @Test
    public void stopAndDrop() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Agent:{a}"),
                                new AgentNamePattern("AgentZ.*"),
                                new StopOnMatch(true),
                                new DropOnMatch(true)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("originalName");
        data.setAgentName("AgentZ42");
        // Drop takes precedence over stop
        assertEquals(MergeRule.DROP, rule.process(data));
        assertEquals("originalName", data.getName());
    }
    
    @Test
    public void continueOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Agent:{a:1}"),
                                new AgentNamePattern("AgentZ(.*)"),
                                new StopOnMatch(false),
                                new ContinueOnMatchAtId(99)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("originalName");
        data.setAgentName("AgentZ42");
        assertEquals(99, rule.process(data));
        assertEquals("Agent:42", data.getName());
    }

    @Test
    public void continueOnNoMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Agent:{a:1}"),
                                new AgentNamePattern("AgentZ(.*)"),
                                new StopOnMatch(false),
                                new ContinueOnNoMatchAtId(90)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("originalName");
        data.setAgentName("RocketZ42");
        assertEquals(90, rule.process(data));
        assertEquals("originalName", data.getName());
    }
}


