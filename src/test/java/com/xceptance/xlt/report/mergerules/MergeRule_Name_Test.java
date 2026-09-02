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
package com.xceptance.xlt.report.mergerules;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnNoMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.DropOnMatch;
import com.xceptance.xlt.report.mergerules.MergeRule.NameExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.NamePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.NewName;
import com.xceptance.xlt.report.mergerules.MergeRule.StopOnMatch;

/**
 * Name Pattern Tests
 *
 * @author Rene Schwietzke (Xceptance GmbH)
 */
public class MergeRule_Name_Test extends MergeRuleTestBase
{
    // =================================================================================
    // Name Pattern Tests
    // =================================================================================

    @Test
    public void include() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new NamePattern("MyName.*"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // match 
        {
            var data = new RequestData("MyName123");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }

        // no match
        {
            RequestData data2 = new RequestData("Any Name");
            assertEquals(1, rule.process(data2));
            assertEquals("Any Name", data2.getName());
        }
    }
    
    @Test
    public void exclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new NameExcludePattern("MyName.*"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);
        
        // match 
        RequestData data1 = new RequestData("MyName");
        assertEquals(1, rule.process(data1));
        assertEquals("MyName", data1.getName());

        // no match
        RequestData data2 = new RequestData("originalName");
        assertEquals(1, rule.process(data2));
        assertEquals("newName", data2.getName());
    }

    @Test
    public void include_exclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("NewName"),
                                new NamePattern("MyName.*"),
                                new NameExcludePattern("Fo+bar"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);
        
        // both match, exclude wins
        {
            var data = new RequestData("MyNameFoobar");
            assertEquals(1, rule.process(data));
            assertEquals("MyNameFoobar", data.getName());
        }
        // no match
        {
            var data = new RequestData("originalName");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // include only match
        {
            var data = new RequestData("MyName123");
            assertEquals(1, rule.process(data));
            assertEquals("NewName", data.getName());
        }
        // exclude only match
        {
            var data = new RequestData("Foobar");
            assertEquals(1, rule.process(data));
            assertEquals("Foobar", data.getName());
        }

    }

    @Test
    public void placeholderFull() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Name:{n}"),
                                new NamePattern("MyName(\\d+)"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // match
        var data = new RequestData("originalName");
        data.setName("MyName123");
        assertEquals(1, rule.process(data));
        assertEquals("Name:MyName123", data.getName());
    }

    @Test
    public void placeholderButNoPattern() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Name:{n}"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // match
        var data = new RequestData("originalName");
        data.setName("MyName123");
        assertEquals(1, rule.process(data));
        assertEquals("Name:MyName123", data.getName());
    }
    
    @Test
    public void placeholderGroup0() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Name:{n:0}"),
                                new NamePattern("MyName(\\d+)"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // match
        var data = new RequestData("originalName");
        data.setName("MyName123");
        assertEquals(1, rule.process(data));
        assertEquals("Name:MyName123", data.getName());
    }

    @Test
    public void placeholderAnyGroup() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("{n:1}:{n:2}:{n:1}"),
                                new NamePattern("My(Name)(\\d+)"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // match
        var data = new RequestData("originalName");
        data.setName("MyName123");
        assertEquals(1, rule.process(data));
        assertEquals("Name:123:Name", data.getName());
    }

    @Test
    public void stopOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Name"),
                                new NamePattern("NameX.*"),
                                new StopOnMatch(true)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        var data = new RequestData("originalName");
        data.setName("NameX42");
        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("Name", data.getName());
    }

    @Test
    public void dropOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Name:{n}"),
                                new NamePattern("NameY.*"),
                                new StopOnMatch(false),
                                new DropOnMatch(true)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        var data = new RequestData("NameY99");
        assertEquals(MergeRule.DROP, rule.process(data));
        assertEquals("NameY99", data.getName());
    }

    @Test
    public void stopAndDrop() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Name:{n}"),
                                new NamePattern("NameZ.*"),
                                new StopOnMatch(true),
                                new DropOnMatch(true)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        var data = new RequestData("NameZ42");
        // Drop takes precedence over stop
        assertEquals(MergeRule.DROP, rule.process(data));
        assertEquals("NameZ42", data.getName());
    }
    
    @Test
    public void continueOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Name:{n:1}"),
                                new NamePattern("NameZ(.*)"),
                                new StopOnMatch(false),
                                new ContinueOnMatchAtId(99)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        var data = new RequestData("originalName");
        data.setName("NameZ42");
        assertEquals(99, rule.process(data));
        assertEquals("Name:42", data.getName());
    }

    @Test
    public void continueOnNoMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Name:{n:1}"),
                                new NamePattern("NameZ(.*)"),
                                new StopOnMatch(false),
                                new ContinueOnNoMatchAtId(90)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        var data = new RequestData("RocketZ42");
        assertEquals(90, rule.process(data));
        assertEquals("RocketZ42", data.getName());
    }
}
