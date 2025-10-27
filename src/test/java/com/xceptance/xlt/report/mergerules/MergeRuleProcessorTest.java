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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.util.List;

import org.junit.Test;

import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.report.mergerules.MergeRule.AgentNameExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.AgentNamePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.ContentTypeExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.ContentTypePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnNoMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.DropOnMatch;
import com.xceptance.xlt.report.mergerules.MergeRule.HttpMethodExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.HttpMethodPattern;
import com.xceptance.xlt.report.mergerules.MergeRule.NameExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.NamePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.NewName;
import com.xceptance.xlt.report.mergerules.MergeRule.RunTimeRanges;
import com.xceptance.xlt.report.mergerules.MergeRule.StatusCodeExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.StatusCodePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.StopOnMatch;
import com.xceptance.xlt.report.mergerules.MergeRule.TransactionNameExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.TransactionNamePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.UrlExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.UrlPattern;
import com.xceptance.xlt.report.mergerules.MergeRule.UrlText;
import com.xceptance.xlt.report.mergerules.MergeRule.UrlTextExclude;

/**
 * Tests the request naming as part of a process chain via {@link MergeRuleProcessor}.
 *
 * @author Rene Schwietzke (Xceptance GmbH)
 */
public class MergeRuleProcessorTest extends MergeRuleTestBase
{
    /**
     * Test that we process all rules when given and we don't want to skip anything
     */
    @Test
    public void testProcessAllRules() throws Exception
    {
        final var rule1 = getMergeRule(1, 
                                       new NewName("1{n}"), 
                                       new NamePattern("request"), 
                                       new ContinueOnMatchAtId(1), 
                                       new ContinueOnNoMatchAtId(1),
                                       new StopOnMatch(false),
                                       new DropOnMatch(false));
        final var rule2 = getMergeRule(2, 
                                       new NewName("2{n}"), 
                                       new NamePattern("request"), 
                                       new ContinueOnMatchAtId(2), 
                                       new ContinueOnNoMatchAtId(2),
                                       new StopOnMatch(false),
                                       new DropOnMatch(false));
        final var rule3 = getMergeRule(3, 
                                       new NewName("3{n}"), 
                                       new NamePattern("request"), 
                                       new ContinueOnMatchAtId(3), 
                                       new ContinueOnNoMatchAtId(3),
                                       new StopOnMatch(false),
                                       new DropOnMatch(false));
        final var rp = new MergeRuleProcessor(List.of(rule1, rule2, rule3), false);

        final var data = new RequestData("request");
        var result = rp.postprocess(data);
        assertEquals("321request", result.getName());
    }

    /**
     * Test that we want to skip a rule and hit exactly the next.
     */
    @Test
    public void testProcessRules_SkipAndHitExactly() throws Exception
    {
        final var rule1 = getMergeRule(100, 
                                       new NewName("1{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(300), 
                                       new ContinueOnNoMatchAtId(100),         
                                       new DropOnMatch(false));  
        final var rule2 = getMergeRule(200, 
                                       new NewName("2{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(200), 
                                       new ContinueOnNoMatchAtId(200),         
                                       new DropOnMatch(false));  
        final var rule3 = getMergeRule(300, 
                                       new NewName("3{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(300), 
                                       new ContinueOnNoMatchAtId(300),         
                                       new DropOnMatch(false));  
        final var data = new RequestData("request");
        final var rp = new MergeRuleProcessor(List.of(rule1, rule2, rule3), false);

        var result = rp.postprocess(data);
        assertEquals("31request", result.getName());
    }

    /**
     * Skip and end beyond the last rule
     */
    @Test
    public void testProcessRules_SkipAndBeyondTheLast() throws Exception
    {
        final var rule1 = getMergeRule(100, 
                                       new NewName("1{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(100), 
                                       new ContinueOnNoMatchAtId(100),         
                                       new DropOnMatch(false));  
        final var rule2 = getMergeRule(200, 
                                       new NewName("2{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(2000), 
                                       new ContinueOnNoMatchAtId(2000),         
                                       new DropOnMatch(false));  
        final var rule3 = getMergeRule(300, 
                                       new NewName("3{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(300), 
                                       new ContinueOnNoMatchAtId(300),         
                                       new DropOnMatch(false));  
        final var data = new RequestData("request");
        final var rp = new MergeRuleProcessor(List.of(rule1, rule2, rule3), false);

        var result = rp.postprocess(data);
        assertEquals("21request", result.getName());
    }

    /**
     * Skip and jump close to the next wanted
     */
    @Test
    public void testProcessRules_SkipAndJumpClose() throws Exception
    {
        final var rule1 = getMergeRule(100, 
                                       new NewName("1{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(250), 
                                       new ContinueOnNoMatchAtId(100),         
                                       new DropOnMatch(false));  
        final var rule2 = getMergeRule(200, 
                                       new NewName("2{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(200), 
                                       new ContinueOnNoMatchAtId(200),         
                                       new DropOnMatch(false));  
        final var rule3 = getMergeRule(300, 
                                       new NewName("3{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(300), 
                                       new ContinueOnNoMatchAtId(300),         
                                       new DropOnMatch(false));  
        final var data = new RequestData("request");
        final var rp = new MergeRuleProcessor(List.of(rule1, rule2, rule3), false);

        var result = rp.postprocess(data);
        assertEquals("31request", result.getName());
    }

    /**
     * Skip when we don't match 
     */
    @Test
    public void testProcessRules_SkipOnNoMatch() throws Exception
    {
        final var rule1 = getMergeRule(100, 
                                       new NewName("1{n}"),
                                       new NamePattern("foobar"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(100), 
                                       new ContinueOnNoMatchAtId(250),         
                                       new DropOnMatch(false));  
        final var rule2 = getMergeRule(200, 
                                       new NewName("2{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(200), 
                                       new ContinueOnNoMatchAtId(200),         
                                       new DropOnMatch(false));  
        final var rule3 = getMergeRule(300, 
                                       new NewName("3{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(300), 
                                       new ContinueOnNoMatchAtId(300),         
                                       new DropOnMatch(false));  
        final var data = new RequestData("request");
        final var rp = new MergeRuleProcessor(List.of(rule1, rule2, rule3), false);

        var result = rp.postprocess(data);
        assertEquals("3request", result.getName());
    }

    /**
     * Check that we stop on the first match and don't continue to the next rules 
     */
    @Test
    public void testProcessRules_StopOnMatch() throws Exception
    {
        final var rule1 = getMergeRule(100, 
                                       new NewName("1{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(100), 
                                       new ContinueOnNoMatchAtId(250),         
                                       new DropOnMatch(false));  
        final var rule2 = getMergeRule(200, 
                                       new NewName("2{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(true), 
                                       new ContinueOnMatchAtId(200), 
                                       new ContinueOnNoMatchAtId(200),         
                                       new DropOnMatch(false));  
        final var rule3 = getMergeRule(300, 
                                       new NewName("3{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(300), 
                                       new ContinueOnNoMatchAtId(300),         
                                       new DropOnMatch(false));  
        final var data = new RequestData("request");
        final var rp = new MergeRuleProcessor(List.of(rule1, rule2, rule3), false);

        var result = rp.postprocess(data);
        assertEquals("21request", result.getName());
    }

    /**
     * Check that we correctly indicate that we want to drop a request
     */
    @Test
    public void testProcessRules_DropOnMatch() throws Exception
    {
        final var rule1 = getMergeRule(100, 
                                       new NewName("1{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(100), 
                                       new ContinueOnNoMatchAtId(250),         
                                       new DropOnMatch(false)); 
        final var rule2 = getMergeRule(200, 
                                       new NewName("2{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(200), 
                                       new ContinueOnNoMatchAtId(200),         
                                       new DropOnMatch(true)); 
        final var rule3 = getMergeRule(300, 
                                       new NewName("3{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(300), 
                                       new ContinueOnNoMatchAtId(300),         
                                       new DropOnMatch(false));  
        final var data = new RequestData("request");
        final var rp = new MergeRuleProcessor(List.of(rule1, rule2, rule3), false);

        var result = rp.postprocess(data);
        assertNull(result);
    }

    /**
     * Check that we have the right order. This is meant to avoid programming issues and
     * is not exposed to the end user.
     * @throws InvalidMergeRuleException 
     */
    @Test
    public void testProcessRules_incorrectOrder() throws Exception
    {
        final var rule1 = getMergeRule(100, 
                                       new NewName("1{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(100), 
                                       new ContinueOnNoMatchAtId(250),         
                                       new DropOnMatch(false)); 
        final var rule2 = getMergeRule(99, 
                                       new NewName("2{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(200), 
                                       new ContinueOnNoMatchAtId(200),         
                                       new DropOnMatch(true)); 
        var ex = assertThrows(IllegalArgumentException.class, () -> {
            new MergeRuleProcessor(List.of(rule1, rule2), false);
        });
        assertEquals("Request processing rules must be sorted by ID in ascending order.", ex.getMessage());
    }

    /**
     * Check that we have the right order. This is meant to avoid programming issues and
     * is not exposed to the end user.
     * @throws InvalidMergeRuleException 
     */
    @Test
    public void testProcessRules_incorrectOrderSameId() throws Exception
    {
        final var rule1 = getMergeRule(100, 
                                       new NewName("1{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(100), 
                                       new ContinueOnNoMatchAtId(250),         
                                       new DropOnMatch(false));  
        final var rule2 = getMergeRule(100, 
                                       new NewName("2{n}"),
                                       new NamePattern("request"),
                                       new StopOnMatch(false), 
                                       new ContinueOnMatchAtId(200), 
                                       new ContinueOnNoMatchAtId(200),         
                                       new DropOnMatch(true));

        var ex = assertThrows(IllegalArgumentException.class, () -> {
            new MergeRuleProcessor(List.of(rule1, rule2), false);
        });
        assertEquals("Request processing rules must be sorted by ID in ascending order.", ex.getMessage());
    }

    /**
     * Check that we stop processing when we hit a rule with StopOnMatch true
     * @throws Exception
     */
    @Test
    public void testContainsAndStop() throws Exception
    {
        final var rule1 = getMergeRule(100, 
                                       new NewName("__Analytics-Start"),
                                       new UrlText("/__Analytics-Start?"),
                                       new StopOnMatch(true), 
                                       new DropOnMatch(false));  
        final var rule2 = getMergeRule(101, 
                                       new NewName("Renamed"),
                                       new UrlText("Start"),
                                       new StopOnMatch(false), 
                                       new DropOnMatch(false));  

        final var rp = new MergeRuleProcessor(List.of(rule1, rule2), true);

        {
            // first rule applies and stops the chain
            final var data = new RequestData("Test1");
            data.setUrl("https://www.foo.com/__Analytics-Start?any=data");

            var result = rp.postprocess(data);
            assertEquals("__Analytics-Start", result.getName());
        }
        {
            // second rule applies too, first failed
            final var data = new RequestData("Test1");
            data.setUrl("https://www.foo.com/__Start?any=data");

            var result = rp.postprocess(data);
            assertEquals("Renamed", result.getName());
        }
    }

    /**
     * We check that we also deal with index removal in names
     */
    /**
     * Test that we process all rules when given and we don't want to skip anything
     */
    @Test
    public void removeIndexInNames() throws Exception
    {
        final var rule = getMergeRule(1, 
                                      new NewName("{n}"), 
                                      new NamePattern("request$"), 
                                      new ContinueOnMatchAtId(1), 
                                      new ContinueOnNoMatchAtId(1),
                                      new StopOnMatch(false),
                                      new DropOnMatch(false));

        final var rp = new MergeRuleProcessor(List.of(rule), true);

        {
            final var data = new RequestData("request.1");
            var result = rp.postprocess(data);
            assertEquals("request", result.getName());
        }
        {
            final var data = new RequestData("request.10");
            var result = rp.postprocess(data);
            assertEquals("request", result.getName());
        }
        {
            final var data = new RequestData("request.1.1");
            var result = rp.postprocess(data);
            assertEquals("request", result.getName());
        }
    }

    /**
     * Error handling in case something goes wrong
     * @throws InvalidMergeRuleException 
     */
    @Test
    public void postprocess_ExceptionHandling_RestoresName() throws InvalidMergeRuleException {
        // Arrange: Create a MergeRule that throws an exception
        MergeRule faultyRule = new MergeRule(1, 
                                             new NewName("1{n}"),
                                             new NamePattern("request"),
                                             new UrlPattern(""),
                                             new ContentTypePattern(""), 
                                             new StatusCodePattern(""), 
                                             new AgentNamePattern(""),   
                                             new TransactionNamePattern(""), 
                                             new HttpMethodPattern(""),  
                                             new RunTimeRanges(""),
                                             new StopOnMatch(false), 
                                             new NameExcludePattern(""), 
                                             new UrlExcludePattern(""), 
                                             new ContentTypeExcludePattern(""), 
                                             new StatusCodeExcludePattern(""), 
                                             new AgentNameExcludePattern(""), 
                                             new TransactionNameExcludePattern(""), 
                                             new HttpMethodExcludePattern(""), 
                                             new ContinueOnMatchAtId(100), 
                                             new ContinueOnNoMatchAtId(100),         
                                             new DropOnMatch(false),
                                             new UrlText(""),
                                             new UrlTextExclude("")) 
        {
            @Override
            public int process(RequestData data) 
            {
                throw new RuntimeException("Test exception");
            }
        };
        
        MergeRuleProcessor processor = new MergeRuleProcessor(List.of(faultyRule), false);

        var r = new RequestData("originalName");
        r.setName("originalName");
        RequestData result = processor.postprocess(r);

        // Assert: Name is restored, result is not null
        assertNotNull(result);
        assertEquals("originalName", result.getName());
    }

}
