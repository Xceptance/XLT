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
import com.xceptance.xlt.report.mergerules.MergeRule.ContentTypeExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.ContentTypePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnNoMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.DropOnMatch;
import com.xceptance.xlt.report.mergerules.MergeRule.NewName;
import com.xceptance.xlt.report.mergerules.MergeRule.StopOnMatch;

/**
 * Content Type Pattern Tests
 *
 * @author Rene Schwietzke (Xceptance GmbH)
 */
public class MergeRule_Content_Test extends MergeRuleTestBase
{
    @Test
    public void include() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new ContentTypePattern("MyContent.*"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match 
        {
            var data = new RequestData("originalName");
            data.setContentType("MyContent123");
            assertEquals(1, rule.process(data));
            assertEquals("newName", data.getName());
        }

        // no match
        {
            RequestData data2 = new RequestData("originalName");
            data2.setContentType("Any Content");
            assertEquals(1, rule.process(data2));
            assertEquals("originalName", data2.getName());
        }
    }
    
    @Test
    public void exclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("newName"),
                                new ContentTypeExcludePattern("MyContent.*"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // match 
        RequestData data1 = new RequestData("originalName");
        data1.setContentType("MyContent123");
        assertEquals(1, rule.process(data1));
        assertEquals("originalName", data1.getName());

        // no match
        RequestData data2 = new RequestData("originalName");
        data2.setContentType("Content123");
        assertEquals(1, rule.process(data2));
        assertEquals("newName", data2.getName());
    }

    @Test
    public void include_exclude() throws Exception
    {
        var rule = getMergeRule(1,
                                new NewName("NewName"),
                                new ContentTypePattern("MyContent.*"),
                                new ContentTypeExcludePattern("Fo+bar"),
                                new StopOnMatch(false),
                                new DropOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // both match, exclude wins
        {
            var data = new RequestData("originalName");
            data.setContentType("MyContentFoobar");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // no match
        {
            var data = new RequestData("originalName");
            data.setContentType("AnythingElse");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
        // include only match
        {
            var data = new RequestData("originalName");
            data.setContentType("MyContent123");
            assertEquals(1, rule.process(data));
            assertEquals("NewName", data.getName());
        }
        // exclude only match
        {
            var data = new RequestData("originalName");
            data.setContentType("Foobar");
            assertEquals(1, rule.process(data));
            assertEquals("originalName", data.getName());
        }
    }

    @Test
    public void placeholderFull() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Content:{c}"),
                                new ContentTypePattern("MyContent(\\d+)"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        var data = new RequestData("originalName");
        data.setContentType("MyContent123");
        assertEquals(1, rule.process(data));
        assertEquals("Content:MyContent123", data.getName());
    }
    
    @Test
    public void placeholderButNoPattern() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Content:{c}"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        var data = new RequestData("originalName");
        data.setContentType("MyContent123");
        assertEquals(1, rule.process(data));
        assertEquals("Content:MyContent123", data.getName());
    }

    @Test
    public void placeholderGroup0() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Content:{c:0}"),
                                new ContentTypePattern("MyContent(\\d+)"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        var data = new RequestData("originalName");
        data.setContentType("MyContent123");
        assertEquals(1, rule.process(data));
        assertEquals("Content:MyContent123", data.getName());
    }

    @Test
    public void placeholderAnyGroup() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("{c:1}:{c:2}:{c:1}"),
                                new ContentTypePattern("My(Content)(\\d+)"),
                                new StopOnMatch(false)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        var data = new RequestData("originalName");
        data.setContentType("MyContent123");
        assertEquals(1, rule.process(data));
        assertEquals("Content:123:Content", data.getName());
    }

    @Test
    public void stopOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Content"),
                                new ContentTypePattern("ContentX.*"),
                                new StopOnMatch(true)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("originalName");
        data.setContentType("ContentX42");
        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("Content", data.getName());
    }

    @Test
    public void dropOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Content:{c}"),
                                new ContentTypePattern("ContentY.*"),
                                new StopOnMatch(false),
                                new DropOnMatch(true)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("originalName");
        data.setContentType("ContentY99");
        assertEquals(MergeRule.DROP, rule.process(data));
        assertEquals("originalName", data.getName());
    }

    @Test
    public void stopAndDrop() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Content:{c}"),
                                new ContentTypePattern("ContentZ.*"),
                                new StopOnMatch(true),
                                new DropOnMatch(true)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("originalName");
        data.setContentType("ContentZ42");
        // Drop takes precedence over stop
        assertEquals(MergeRule.DROP, rule.process(data));
        assertEquals("originalName", data.getName());
    }
    
    @Test
    public void continueOnMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Content:{c:1}"),
                                new ContentTypePattern("ContentZ(.*)"),
                                new StopOnMatch(false),
                                new ContinueOnMatchAtId(99)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("originalName");
        data.setContentType("ContentZ42");
        assertEquals(99, rule.process(data));
        assertEquals("Content:42", data.getName());
    }

    @Test
    public void continueOnNoMatch() throws Exception
    {
        var rule = getMergeRule(
                                1,
                                new NewName("Content:{c:1}"),
                                new ContentTypePattern("ContentZ(.*)"),
                                new StopOnMatch(false),
                                new ContinueOnNoMatchAtId(90)
            );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("originalName");
        data.setContentType("RocketZ42");
        assertEquals(90, rule.process(data));
        assertEquals("originalName", data.getName());
    }
}
