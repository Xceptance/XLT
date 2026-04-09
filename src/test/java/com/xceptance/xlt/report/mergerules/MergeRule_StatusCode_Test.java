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
import com.xceptance.xlt.report.mergerules.MergeRule.NewName;
import com.xceptance.xlt.report.mergerules.MergeRule.StatusCodeExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.StatusCodePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.StopOnMatch;

/**
 * HTTP Method Pattern Tests
 *
 * @author Rene Schwietzke (Xceptance GmbH)
 */
public class MergeRule_StatusCode_Test extends MergeRuleTestBase
{
    @Test
    public void include() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new StatusCodePattern("200"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // match 
        {
            var data = new RequestData("originalName");
            data.setResponseCode(200);
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }

        // no match
        {
            RequestData data2 = new RequestData("originalName");
            data2.setResponseCode(301);
            assertEquals(1, rule.process(data2));
            assertEquals("originalName", data2.getName());
        }
    }
    
    @Test
    public void exclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new StatusCodeExcludePattern("200"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);
        
        // match 
        RequestData data1 = new RequestData("originalName");
        data1.setResponseCode(200);
        assertEquals(1, rule.process(data1));
        assertEquals("originalName", data1.getName());

        // no match
        RequestData data2 = new RequestData("originalName");
        data2.setResponseCode(300);
        assertEquals(1, rule.process(data2));
        assertEquals("newName", data2.getName());
    }

    @Test
    public void include_exclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("NewName"),
                                new StatusCodePattern("20[0-9]"),
                                new StatusCodeExcludePattern("201"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);
        
        // both match, exclude wins
        {
            var data = new RequestData("originalName");
            data.setResponseCode(201);
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // no match
        {
            var data = new RequestData("originalName");
            data.setResponseCode(502);
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // include only match
        {
            var data = new RequestData("originalName");
            data.setResponseCode(202);
            assertEquals(1, rule.process(data));
            assertEquals("NewName", data.getName());
        }
        // exclude only match
        {
            var data = new RequestData("originalName");
            data.setResponseCode(300);
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
    }

    @Test
    public void placeholderFull() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Code:{s}"),
                                new StatusCodePattern("300"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // match
        var data = new RequestData("originalName");
        data.setResponseCode(300);
        assertEquals(1, rule.process(data));
        assertEquals("Code:300", data.getName());
    }

    @Test
    public void placeholderButNoPattern() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Code:{s}"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // match
        var data = new RequestData("originalName");
        data.setResponseCode(300);
        assertEquals(1, rule.process(data));
        assertEquals("Code:300", data.getName());
    }
    
    @Test
    public void placeholderGroup0() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Code:{s:0}"),
                                new StatusCodePattern("([0-9]0)0"),
                                new StopOnMatch(false)
            );

        // match
        var data = new RequestData("originalName");
        data.setResponseCode(200);
        assertEquals(1, rule.process(data));
        assertEquals("Code:200", data.getName());
    }

    @Test
    public void placeholderAnyGroup() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("{s:1}:{s:2}:{s:1}"),
                                new StatusCodePattern("([0-9]0)(0)"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        // match
        var data = new RequestData("originalName");
        data.setResponseCode(200);
        assertEquals(1, rule.process(data));
        assertEquals("20:0:20", data.getName());
    }

    @Test
    public void stopOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Code"),
                                new StatusCodePattern("200"),
                                new StopOnMatch(true)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        var data = new RequestData("originalName");
        data.setResponseCode(200);
        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("Code", data.getName());
    }

    @Test
    public void dropOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Code:{s}"),
                                new StatusCodePattern("200"),
                                new StopOnMatch(false),
                                new DropOnMatch(true)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        var data = new RequestData("originalName");
        data.setResponseCode(200);
        assertEquals(MergeRule.DROP, rule.process(data));
        assertEquals("originalName", data.getName());
    }

    @Test
    public void stopAndDrop() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Code:{s}"),
                                new StatusCodePattern("500"),
                                new StopOnMatch(true),
                                new DropOnMatch(true)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        var data = new RequestData("originalName");
        data.setResponseCode(500);
        // Drop takes precedence over stop
        assertEquals(MergeRule.DROP, rule.process(data));
        assertEquals("originalName", data.getName());
    }
    
    @Test
    public void continueOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Code:{s:1}"),
                                new StatusCodePattern("20(0)"),
                                new StopOnMatch(false),
                                new ContinueOnMatchAtId(99)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        var data = new RequestData("originalName");
        data.setResponseCode(200);
        assertEquals(99, rule.process(data));
        assertEquals("Code:0", data.getName());
    }

    @Test
    public void continueOnNoMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Code:{s:1}"),
                                new StatusCodePattern("PATCH(.*)"),
                                new StopOnMatch(false),
                                new ContinueOnNoMatchAtId(90)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        
        var data = new RequestData("originalName");
        data.setResponseCode(200);
        assertEquals(90, rule.process(data));
        assertEquals("originalName", data.getName());
    }
}
