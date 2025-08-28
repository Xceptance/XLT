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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.util.List;

import org.junit.Test;

import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.AgentNameExcludePattern;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.AgentNamePattern;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.ContentTypeExcludePattern;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.ContentTypePattern;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.ContinueOnMatchAtId;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.ContinueOnNoMatchAtId;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.DropOnMatch;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.HttpMethodExcludePattern;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.HttpMethodPattern;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.NewName;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.RequestNameExcludePattern;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.RequestNamePattern;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.RunTimeRanges;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.StatusCodeExcludePattern;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.StatusCodePattern;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.StopOnMatch;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.TransactionNameExcludePattern;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.TransactionNamePattern;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.UrlExcludePattern;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.UrlPattern;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.UrlPrecheckText;

/**
 * Tests the request renaming magic implemented by {@link RequestProcessingRule}
 * and processing several rules including skip by {@link DataParserThread}. .
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 * @author Rene Schwietzke (Xceptance GmbH)
 */
public class RequestProcessingTest
{
    /*
     * For simplicity the following tests use content type pattern only since all pattern-based request filters share
     * the same logic.
     */

    @Test
    public void testContinueOnMatch() throws Exception
    {
        final var rule = new RequestProcessingRule(42, 
                                                   new NewName("fooBar"),
                                                   new RequestNamePattern("request"), 
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(false), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""),
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""),
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""),
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(99), 
                                                   new ContinueOnNoMatchAtId(42),         
                                                   new DropOnMatch(false)); 

        final var data = new RequestData("request");
        assertEquals(99, rule.process(data));
        assertEquals("fooBar", data.getName());
    }

    @Test
    public void testContinueOnNoMatch() throws Exception
    {
        final var rule = new RequestProcessingRule(42, 
                                                   new NewName("fooBar"),
                                                   new RequestNamePattern("request"), 
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(false), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""),
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""),
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""),
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(42), 
                                                   new ContinueOnNoMatchAtId(77),         
                                                   new DropOnMatch(false)); 

        final var data = new RequestData("not-matching");
        assertEquals(77, rule.process(data));
        assertEquals("not-matching", data.getName());
    }


    @Test
    public void testPatternIncludeOnly_NoMatch() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("fooBar ({c:0})"),
                                                   new RequestNamePattern(""), 
                                                   new UrlPattern(""),
                                                   new ContentTypePattern("text/html"), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""),
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""),
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""),
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 

        final var name = "original";
        final var data = new RequestData(name);
        data.setContentType("image/jpeg");

        // return own id because we told it so
        assertEquals(0, rule.process(data));
        // no match, unchanged
        assertEquals(name, data.getName());
    }

    @Test
    public void testPatternIncludeOnly_MatchAndStop() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("fooBar ({c:0})"),
                                                   new RequestNamePattern(""), 
                                                   new UrlPattern(""),
                                                   new ContentTypePattern("text/html"), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""),
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""),
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""),
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 

        final var name = "original";
        final var data = new RequestData(name);
        data.setContentType("text/html");

        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("fooBar (text/html)", data.getName());
    }

    @Test
    public void testPatternIncludeOnly_MatchAndDontStop() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("fooBar ({c:0})"),
                                                   new RequestNamePattern(""), 
                                                   new UrlPattern(""),
                                                   new ContentTypePattern("text/html"), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(false), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""),
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""),
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""),
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 

        final var name = "original";
        final var data = new RequestData(name);
        data.setContentType("text/html");

        assertEquals(0, rule.process(data));
        assertEquals("fooBar (text/html)", data.getName());
    }

    @Test
    public void testPatternExcludeOnly_StopOnMatch_EmptyInclude() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("fooBar ({c:0})"),
                                                   new RequestNamePattern(""), 
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""),
                                                   new ContentTypeExcludePattern("text/html"), 
                                                   new StatusCodeExcludePattern(""),
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""),
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 
        final String name = "baz";
        final RequestData data = new RequestData(name);
        data.setContentType("image/jpeg");

        // exclude does not match, hence all matches; hence we stop
        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("fooBar (image/jpeg)", data.getName());

        data.setName(name);
        data.setContentType("text/html");

        // exclude matches, hence the rules does not apply
        assertEquals(0, rule.process(data));
        assertEquals(name, data.getName());
    }

    @Test
    public void testPatternBoth_StopOnMatch() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("fooBar ({c:0})"),
                                                   new RequestNamePattern(""),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern("text/jpeg"), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""),
                                                   new ContentTypeExcludePattern("text/html"), 
                                                   new StatusCodeExcludePattern(""),
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""),
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 
        final String name = "baz";
        final RequestData data = new RequestData(name);
        data.setContentType("text/jpeg");

        // exclude does not match, hence all matches; hence we stop
        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("fooBar (text/jpeg)", data.getName());

        data.setName(name);
        data.setContentType("text/html");

        // exclude matches, hence the rules does not apply
        assertEquals(0, rule.process(data));
        assertEquals(name, data.getName());
    }

    @Test
    public void testPatternBoth_BothMatch() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("fooBar ({c:0})"),
                                                   new RequestNamePattern(""),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern("text/"), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(false), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""),
                                                   new ContentTypeExcludePattern("text/html"), 
                                                   new StatusCodeExcludePattern(""),
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""),
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 
        final String name = "baz";
        final RequestData data = new RequestData(name);
        data.setContentType("text/html");

        // both match, we continue and don't rename
        assertEquals(0, rule.process(data));
        assertEquals(name, data.getName());
    }



    @Test
    public void testPatternBoth_NoneMatch() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("fooBar ({c:0})"),
                                                   new RequestNamePattern(""),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern("html"), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""),
                                                   new ContentTypeExcludePattern("xhtml"), 
                                                   new StatusCodeExcludePattern(""),
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""),
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 
        final String name = "baz";
        final RequestData data = new RequestData(name);
        data.setContentType("image/jpeg");

        // include fails, exclude fails, just process normally
        assertEquals(0, rule.process(data));
        assertEquals(name, data.getName());

        // include matches, stop applies
        data.setContentType("text/html");
        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("fooBar (html)", data.getName());

        // reset
        data.setName(name);
        data.setContentType("text/xhtml");
        // include does match, exclude does match -> NOT MATCH
        assertEquals(0, rule.process(data));
        assertEquals(name, data.getName()); // no change
    }

    /**
     * All empty. Stop false. Empty rules means, that everything matches.
     */
    @Test
    public void testRules_allEmpty_stopFalse() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("New"),
                                                   new RequestNamePattern(""),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(false), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""),
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""),
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""),
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 

        final RequestData data = new RequestData("Old");
        assertEquals(0, rule.process(data));
        assertEquals("New", data.getName());
    }

    /**
     * All empty. Stop true
     */
    @Test
    public void allEmpty_butApply_Stop() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("New"),
                                                   new RequestNamePattern(""),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""),
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""),
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""),
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 
        // match, change, stop
        final RequestData data = new RequestData("Old");
        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("New", data.getName());

        final RequestData data2 = new RequestData("Old");
        data2.setAgentName("Agent-007");
        data2.setHttpMethod("GET");
        data2.setUrl("https://www.foo.bar/all");

        // match, change, STOP
        assertEquals(RequestProcessingRule.STOP, rule.process(data2));
        assertEquals("New", data2.getName());
    }

    /**
     * Some data set, all empty, drop and stop are set and fired, drop prevails
     */
    @Test
    public void allRulesEmpty_StopAndDrop() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("New"),
                                                   new RequestNamePattern(""),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""),
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""),
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""),
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(true)); 

        final RequestData data = new RequestData("Old");
        data.setAgentName("Agent-007");
        data.setHttpMethod("GET");
        data.setUrl("https://www.foo.bar/all");

        // match, no change, drop
        assertEquals(RequestProcessingRule.DROP, rule.process(data));
        // did not change anything, because we dropped it
        assertEquals("Old", data.getName());
    }

    /**
     * Some data set, all empty, continue, no drop or stop set
     */
    @Test
    public void allEmpty_NoStop_NoDrop() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("New"),
                                                   new RequestNamePattern(""),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(false), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""),
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""),
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""),
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 

        final RequestData data = new RequestData("Old");
        data.setAgentName("Agent-007");
        data.setHttpMethod("GET");

        // match, change, continue
        assertEquals(0, rule.process(data));
        assertEquals("New", data.getName());
    }

    /**
     * Some data, all empty, drop is set and stop is false
     */
    @Test
    public void allAllEmpty_butApply_Drop2() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("New"),
                                                   new RequestNamePattern(""),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(false), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""),
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""),
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""),
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(true)); 

        final RequestData data = new RequestData("Old");
        data.setAgentName("Agent-007");
        data.setHttpMethod("GET");
        data.setUrl("https://www.foo.bar/all");

        // match, change, drop
        assertEquals(RequestProcessingRule.DROP, rule.process(data));
        assertEquals("Old", data.getName());
    }

    /**
     * All include patterns used and match
     */
    @Test
    public void testAllIncludePatternsSetAndMatch() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("New"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern("url"),
                                                   new ContentTypePattern("content"), 
                                                   new StatusCodePattern("200"), 
                                                   new AgentNamePattern("agent"),   
                                                   new TransactionNamePattern("transaction"), 
                                                   new HttpMethodPattern("method"),  
                                                   new RunTimeRanges("1000,2000"),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""),
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""),
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""),
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false));         

        final RequestData data = new RequestData("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(999);

        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("New", data.getName());
    }

    /**
     * All include patterns used and match. Name captured all
     */
    @Test
    public void testAllIncludePatternsSetAndMatch_CaptureAll() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n:0}-{u:0}-{c:0}-{s:0}-{a:0}-{t:0}-{m:0}-{r:0}"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern("url"),
                                                   new ContentTypePattern("content"), 
                                                   new StatusCodePattern("200"), 
                                                   new AgentNamePattern("agent"),   
                                                   new TransactionNamePattern("transaction"), 
                                                   new HttpMethodPattern("method"),  
                                                   new RunTimeRanges("1000,2000"),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""),
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""),
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""),
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false));         

        final RequestData data = new RequestData("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("request-url-content-200-agent-transaction-method-0..999", data.getName());
    }

    /**
     * All include patterns used and match. Name captured all, short  attribute definition
     */
    @Test
    public void testAllIncludePatternsSetAndMatch_CaptureAll_NoPosAttribute() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n}-{u}-{c}-{s}-{a}-{t}-{m}-{r}"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern("url"),
                                                   new ContentTypePattern("content"), 
                                                   new StatusCodePattern("200"), 
                                                   new AgentNamePattern("agent"),   
                                                   new TransactionNamePattern("transaction"), 
                                                   new HttpMethodPattern("method"),  
                                                   new RunTimeRanges("1000,2000"),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""),
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""),
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""),
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 

        final RequestData data = new RequestData("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("request-url-content-200-agent-transaction-method-0..999", data.getName());
    }

    /**
     * All exclude patterns used and don't match
     */
    @Test
    public void testAllExcludePatternsSetAndMatch() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("New"),
                                                   new RequestNamePattern(""),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern("not-request"), 
                                                   new UrlExcludePattern("not-url"),
                                                   new ContentTypeExcludePattern("not-content"), 
                                                   new StatusCodeExcludePattern("404"),
                                                   new AgentNameExcludePattern("not-agent"), 
                                                   new TransactionNameExcludePattern("not-transaction"),
                                                   new HttpMethodExcludePattern("not-method"), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 

        final RequestData data = new RequestData("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(999);

        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("New", data.getName());
    }

    /**
     * All exclude patterns used and match. Name captured all
     */
    @Test
    public void testAllExcludePatternsSetAndMatch_CaptureAll() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n:0}{u:0}{c:0}{s:0}{a:0}{t:0}{m:0}{r:0}"),
                                                   new RequestNamePattern(""),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges("1000"),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern("not-request"), 
                                                   new UrlExcludePattern("not-url"),
                                                   new ContentTypeExcludePattern("not-content"), 
                                                   new StatusCodeExcludePattern("404"),
                                                   new AgentNameExcludePattern("not-agent"), 
                                                   new TransactionNameExcludePattern("not-transaction"),
                                                   new HttpMethodExcludePattern("not-method"), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false));         

        final RequestData data = new RequestData("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("requesturlcontent200agenttransactionmethod0..999", data.getName());
    }

    /**
     * All exclude patterns used and exclude all. Name captured all
     * Include matches and exclude, hence include rules don't fire
     */
    @Test
    public void testAllExcludePatternsSetAndExcludeMatch_CaptureAll() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n:0}{u:0}{c:0}{s:0}{a:0}{t:0}{m:0}{r:0}-any"),
                                                   new RequestNamePattern(""),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges("1000"),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern("not-request"), 
                                                   new UrlExcludePattern("not-url"),
                                                   new ContentTypeExcludePattern("not-content"), 
                                                   new StatusCodeExcludePattern("not-200"),
                                                   new AgentNameExcludePattern("not-agent"), 
                                                   new TransactionNameExcludePattern("not-transaction"),
                                                   new HttpMethodExcludePattern("not-method"), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false));          

        final RequestData data = new RequestData("not-request");
        data.setUrl("not-url");
        data.setContentType("not-content");
        data.setResponseCode(200);
        data.setAgentName("not-agent");
        data.setTransactionName("not-transaction");
        data.setHttpMethod("not-method");
        data.setRunTime(500);

        assertEquals(0, rule.process(data));
        assertEquals("not-request", data.getName());
    }

    /**
     * namePattern [n] .......... reg-ex defining a matching request name
     */
    @Test
    public void testNamePattern() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n:2}-{n:0}-{n:1}"),
                                                   new RequestNamePattern("re(q)ue(st)"),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges("1000"),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 

        final RequestData data = new RequestData("request");

        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("st-request-q", data.getName());
    }

    /**
     * namePattern [n] .......... reg-ex defining a matching request name Index does not exist
     */
    @Test(expected = InvalidRequestProcessingRuleException.class)
    public void testNamePattern_IndexDoesNotExist() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n:2}-{n:0}-{n:10}"),
                                                   new RequestNamePattern("re(q)ue(st)"),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges("1000"),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 
        // match
        final RequestData data = new RequestData("request");
        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("st-request-q", data.getName());
    }

    /**
     * # transactionPattern [t] ... reg-ex defining a matching transaction name
     */
    @Test
    public void testTransactionPattern_Normal() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n:0}-{t:1} {t:0}"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern("T.*bar-([0-9])"), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 
        // match
        final RequestData data = new RequestData("request");
        data.setTransactionName("TFoobar-2");

        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("request-2 TFoobar-2", data.getName());

        // do not match
        final RequestData data2 = new RequestData("request");
        data2.setTransactionName("TLateStuff");

        assertEquals(0, rule.process(data2));
        assertEquals("request", data2.getName());
    }

    /**
     * # agentPattern [a] ......... reg-ex defining a matching agent name
     */
    @Test
    public void testAgentPattern_Normal() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n:0}-{a:1} {a:0}"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern("A.*bar-([0-9])"),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 

        // match
        final RequestData data = new RequestData("request");
        data.setAgentName("AFoobar-2");

        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("request-2 AFoobar-2", data.getName());

        // do not match
        final RequestData data2 = new RequestData("request");
        data2.setAgentName("TLateStuff");

        assertEquals(0, rule.process(data2));
        assertEquals("request", data2.getName());
    }

    /**
     * # contentTypePattern [c] ... reg-ex defining a matching response content type
     */
    @Test
    public void testContentPattern_Normal() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n:0}-{c:1} {c:0}"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern("image/([a-z]{3,4})$"), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 

        // match
        final RequestData data = new RequestData("request");
        data.setContentType("image/jpeg");

        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("request-jpeg image/jpeg", data.getName());

        // do not match
        final RequestData data2 = new RequestData("request");
        data2.setContentType("image/coffeelatte");

        assertEquals(0, rule.process(data2));
        assertEquals("request", data2.getName());
    }

    /**
     * # statusCodePattern [s] .... reg-ex defining a matching status code
     */
    @Test
    public void testStatusCodePattern_Normal() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n:0}-{s:1} {s:0}"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern("(30[12])"), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 

        // match
        final RequestData data = new RequestData("request");
        data.setResponseCode(302);

        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("request-302 302", data.getName());

        // do not match
        final RequestData data2 = new RequestData("request");
        data2.setResponseCode(304);

        assertEquals(0, rule.process(data2));
        assertEquals("request", data2.getName());
    }

    /**
     * # urlPattern [u] ........... reg-ex defining a matching request URL
     */
    @Test
    public void testURLPattern_Normal() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n:0}-{u:1} {u:0}"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern("https?://foobar.com/([^/]+)/"),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 
        // match
        final RequestData data = new RequestData("request");
        data.setUrl("https://foobar.com/tiger/");
        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("request-tiger https://foobar.com/tiger/", data.getName());

        // do not match
        final RequestData data2 = new RequestData("request");
        data2.setUrl("https://foobar.de/tiger/");

        assertEquals(0, rule.process(data2));
        assertEquals("request", data2.getName());
    }

    /**
     * Test an url pattern including a precheck speedup
     */
    @Test
    public void testURLPattern_Normal_Prechecked() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{u:1}"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern("https://foobar.com/([^/]+)/"),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false),
                                                   new UrlPrecheckText("foobar")); 
        // precheck match, url match
        final RequestData data = new RequestData("request");
        data.setUrl("https://foobar.com/tiger/");
        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("tiger", data.getName());

        // precheck match, url not match
        final RequestData data2 = new RequestData("request");
        data2.setUrl("https://foobar.online/tiger/");
        assertEquals(0, rule.process(data2));
        assertEquals("request", data2.getName());

        final var rule2 = new RequestProcessingRule(0, 
                                                   new NewName("{u:1}"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern("https://foobar.com/([^/]+)/"),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false),
                                                   new UrlPrecheckText("xmas")); 
        
        // precheck not match, url would match
        final RequestData data3 = new RequestData("request");
        data3.setUrl("https://foobar.com/tiger/");
        assertEquals(0, rule2.process(data3));
        assertEquals("request", data3.getName());
        
        // precheck not match, url not match
        final RequestData data4 = new RequestData("request");
        data4.setUrl("https://myhost.com/tiger/");
        assertEquals(0, rule2.process(data4));
        assertEquals("request", data4.getName());
    }

    /**
     * Test an url pattern including a precheck speedup as exclude
     */
    @Test
    public void testURLPattern_Normal_Prechecked_Exclude() throws Exception
    {
        final var rule = new RequestProcessingRule(10, 
                                                   new NewName("{n}"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern("https://foobar.com/([^/]+)/"), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(10), 
                                                   new ContinueOnNoMatchAtId(10),         
                                                   new DropOnMatch(false),
                                                   new UrlPrecheckText("foobar")); 
        // precheck match, url match
        final RequestData data = new RequestData("request");
        data.setUrl("https://foobar.com/tiger/");
        assertEquals(10, rule.process(data));
        assertEquals("request", data.getName());

        // precheck match, url not match
        final RequestData data2 = new RequestData("request");
        data2.setUrl("https://foobar.online/tiger/");
        assertEquals(RequestProcessingRule.STOP, rule.process(data2));
        assertEquals("request", data2.getName());

        final var rule2 = new RequestProcessingRule(0, 
                                                   new NewName("{u:1}"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern("https://foobar.com/([^/]+)/"),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false),
                                                   new UrlPrecheckText("xmas")); 
        
        // precheck not match, url would match
        final RequestData data3 = new RequestData("request");
        data3.setUrl("https://foobar.com/tiger/");
        assertEquals(0, rule2.process(data3));
        assertEquals("request", data3.getName());
        
        // precheck not match, url not match
        final RequestData data4 = new RequestData("request");
        data4.setUrl("https://myhost.com/tiger/");
        assertEquals(0, rule2.process(data4));
        assertEquals("request", data4.getName());
    }
    
    /**
     * # httpMethodPattern [m] ........... reg-ex defining a matching HTTP method
     */
    @Test
    public void testHttpMethodPattern_Normal() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n:0} {m:1}"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern("(GET)"),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false));         

        // match
        final RequestData data = new RequestData("request");
        data.setHttpMethod("GET");

        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("request GET", data.getName());

        // do not match
        final RequestData data2 = new RequestData("request");
        data2.setHttpMethod("PUT");

        assertEquals(0, rule.process(data2));
        assertEquals("request", data2.getName());
    }

    /**
     * Small helper for runtime ranges
     */
    private void responseTimeRanges(final RequestProcessingRule rule, final int runtime, final String expected)
    {
        final RequestData data = new RequestData("request");
        data.setRunTime(runtime);
        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals(expected, data.getName());
    }

    /**
     * # runTimeRanges [r] ........ list of run time segment boundaries
     */
    @Test
    public void testResponseTimeRanges_Normal() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n:0} [{r}]"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges("100, 3000, 5000"),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false));  

        responseTimeRanges(rule, 0, "request [0..99]");

        responseTimeRanges(rule, 99, "request [0..99]");
        responseTimeRanges(rule, 100, "request [100..2999]");
        responseTimeRanges(rule, 101, "request [100..2999]");

        responseTimeRanges(rule, 2999, "request [100..2999]");
        responseTimeRanges(rule, 3000, "request [3000..4999]");
        responseTimeRanges(rule, 3001, "request [3000..4999]");

        responseTimeRanges(rule, 4999, "request [3000..4999]");
        responseTimeRanges(rule, 5000, "request [>=5000]");
        responseTimeRanges(rule, 5001, "request [>=5000]");

        responseTimeRanges(rule, 50701, "request [>=5000]");

    }

    /**
     * # Complex regexp
     */
    @Test
    public void testComplexRegExp() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n:1}-{u:1}-{c:1}-{s:1}-{a:1}-{t:2}{t:3}{t:1}-{m:1}-{r}"),
                                                   new RequestNamePattern("r(.*)"),
                                                   new UrlPattern("([urlURL]{3})"),
                                                   new ContentTypePattern("^(.+)$"), 
                                                   new StatusCodePattern("2([01])[0-9]"), 
                                                   new AgentNamePattern("(a?)g?e?n?t?"),   
                                                   new TransactionNamePattern("((.+)(action))$"), 
                                                   new HttpMethodPattern("^(.+)$"),  
                                                   new RunTimeRanges("1000, 2000"),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern("not-request"), 
                                                   new UrlExcludePattern("not-url"), 
                                                   new ContentTypeExcludePattern("not-content"), 
                                                   new StatusCodeExcludePattern("404"), 
                                                   new AgentNameExcludePattern("not-agent"), 
                                                   new TransactionNameExcludePattern("not-transaction"), 
                                                   new HttpMethodExcludePattern("not-method"), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false));  

        final RequestData data = new RequestData("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("equest-url-content-0-a-transactiontransaction-method-0..999", data.getName());
    }

    /**
     * Include and exclude set. Match include Please note that an include pattern as well as an exclude pattern can be
     * specified for a pattern type at the same time. In this case, a request is selected if and only if it matches the
     * include pattern, but does not match the exclude pattern.
     */
    @Test
    public void testAllIncludeAndExclude_MatchIncludeNotExclude_CaptureAll() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n:0}{u:0}{c:0}{s:0}{a:0}{t:0}-{m:0}-{r:0}"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern("url"),
                                                   new ContentTypePattern("content"), 
                                                   new StatusCodePattern("200"), 
                                                   new AgentNamePattern("agent"),   
                                                   new TransactionNamePattern("transaction"), 
                                                   new HttpMethodPattern("method"),  
                                                   new RunTimeRanges("1000, 2000"),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern("not-request"), 
                                                   new UrlExcludePattern("not-url"), 
                                                   new ContentTypeExcludePattern("not-content"), 
                                                   new StatusCodeExcludePattern("404"), 
                                                   new AgentNameExcludePattern("not-agent"), 
                                                   new TransactionNameExcludePattern("not-transaction"), 
                                                   new HttpMethodExcludePattern("not-method"), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false));  

        final RequestData data = new RequestData("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        assertEquals(RequestProcessingRule.STOP, rule.process(data));
        assertEquals("requesturlcontent200agenttransaction-method-0..999", data.getName());

    }

    /**
     * Include and exclude set. Match Include and Exclude
     */
    @Test
    public void testAllIncludeAndExclude_MatchIncludeExclude_CaptureAll() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n:0}{u:0}{c:0}{s:0}{a:0}{t:0}{m:0}{r:0}"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern("url"),
                                                   new ContentTypePattern("content"), 
                                                   new StatusCodePattern("200"), 
                                                   new AgentNamePattern("agent"),   
                                                   new TransactionNamePattern("transaction"), 
                                                   new HttpMethodPattern("method"),  
                                                   new RunTimeRanges("1000, 2000"),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern("request"), 
                                                   new UrlExcludePattern("url"), 
                                                   new ContentTypeExcludePattern("content"), 
                                                   new StatusCodeExcludePattern("200"), 
                                                   new AgentNameExcludePattern("agent"), 
                                                   new TransactionNameExcludePattern("transaction"), 
                                                   new HttpMethodExcludePattern("method"), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false));  

        final RequestData data = new RequestData("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        assertEquals(0, rule.process(data));
        assertEquals("request", data.getName());

    }

    /**
     * Include and exclude set. Match not Include but Exclude Please note that an include pattern as well as an exclude
     * pattern can be specified for a pattern type at the same time. In this case, a request is selected if and only if
     * it matches the include pattern, but does not match the exclude pattern.
     */
    @Test
    public void testAllIncludeAndExclude_MatchNotIncludeButExclude_CaptureAll() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n:0}{u:0}{c:0}{s:0}{a:0}{t:0}{m:0}{r:0}"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern("url"),
                                                   new ContentTypePattern("content"), 
                                                   new StatusCodePattern("200"), 
                                                   new AgentNamePattern("agent"),   
                                                   new TransactionNamePattern("transaction"), 
                                                   new HttpMethodPattern("method"),  
                                                   new RunTimeRanges("1000, 2000"),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern("not-request"), 
                                                   new UrlExcludePattern("not-url"), 
                                                   new ContentTypeExcludePattern("not-content"), 
                                                   new StatusCodeExcludePattern("404"), 
                                                   new AgentNameExcludePattern("not-agent"), 
                                                   new TransactionNameExcludePattern("not-transaction"), 
                                                   new HttpMethodExcludePattern("not-method"), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 

        final RequestData data = new RequestData("not-request");
        data.setUrl("not-url");
        data.setContentType("not-content");
        data.setResponseCode(404);
        data.setAgentName("not-agent");
        data.setTransactionName("not-transaction");
        data.setHttpMethod("not-method");
        data.setRunTime(500);

        assertEquals(0, rule.process(data));
        assertEquals("not-request", data.getName());
    }

    @Test
    public void allowMatchingHighGroupNumbers() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n:12}"),
                                                   new RequestNamePattern("(1)(2)(3)(4)(5)(6)(7)(8)(9)(10)(11)(12)"),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(false), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 

        final RequestData data = new RequestData("123456789101112");

        assertEquals(0, rule.process(data));
        assertEquals("12", data.getName());
    }
    
    /**
     * Include and exclude set. Match not Include but Exclude Please note that an include pattern as well as an exclude
     * pattern can be specified for a pattern type at the same time. In this case, a request is selected if and only if
     * it matches the include pattern, but does not match the exclude pattern.
     */
    @Test
    public void includeDataWithouPositionNumber() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("{n}-{u}-{c}-{s}-{a}-{t}-{r}"),
                                                   new RequestNamePattern("request"),
                                                   new UrlPattern("url"),
                                                   new ContentTypePattern("content"), 
                                                   new StatusCodePattern("200"), 
                                                   new AgentNamePattern("agent"),   
                                                   new TransactionNamePattern("transaction"), 
                                                   new HttpMethodPattern("GET"),  
                                                   new RunTimeRanges("300, 2000"),
                                                   new StopOnMatch(false), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(false)); 

        final RequestData data = new RequestData("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("GET");
        data.setRunTime(500);

        assertEquals(0, rule.process(data));
        assertEquals("request-url-content-200-agent-transaction-300..1999", data.getName());

    }

    @Test(expected = InvalidRequestProcessingRuleException.class)
    public void testInvalidRegex() throws Exception
    {
        new RequestProcessingRule(0, 
                                  new NewName("{n:1}"),
                                  new RequestNamePattern("([]-]"),
                                  new UrlPattern(""),
                                  new ContentTypePattern(""), 
                                  new StatusCodePattern(""), 
                                  new AgentNamePattern(""),   
                                  new TransactionNamePattern(""), 
                                  new HttpMethodPattern(""),  
                                  new RunTimeRanges(""),
                                  new StopOnMatch(true), 
                                  new RequestNameExcludePattern(""), 
                                  new UrlExcludePattern(""), 
                                  new ContentTypeExcludePattern(""), 
                                  new StatusCodeExcludePattern(""), 
                                  new AgentNameExcludePattern(""), 
                                  new TransactionNameExcludePattern(""), 
                                  new HttpMethodExcludePattern(""), 
                                  new ContinueOnMatchAtId(0), 
                                  new ContinueOnNoMatchAtId(0),         
                                  new DropOnMatch(false)); 
    }

    /**
     * It matches all include but DROP fires first
     */
    @Test
    public void stopAndDrop() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName(""),
                                                   new RequestNamePattern(""),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(true), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(true));         

        final RequestData data = new RequestData("Old");

        assertEquals(RequestProcessingRule.DROP, rule.process(data));
    }

    /**
     * I can specify a name (not a must) when a drop is wanted. This does not do a things of
     * course, but might help to avoid confusion when setting up rules as well as helps to
     * name rules kinda.
     */
    @Test
    public void canGiveNameDespiteDrop() throws Exception
    {
        final var rule = new RequestProcessingRule(0, 
                                                   new NewName("My new name"),
                                                   new RequestNamePattern(""),
                                                   new UrlPattern("url"),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(false), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(0), 
                                                   new ContinueOnNoMatchAtId(0),         
                                                   new DropOnMatch(true));  

        final RequestData data = new RequestData("Old");
        data.setUrl("url");

        assertEquals(RequestProcessingRule.DROP, rule.process(data));
    }

    @Test
    public void fullTextPlaceholders_emptyFilterPatterns() throws Exception
    {
        final var rule = new RequestProcessingRule(1, 
                                                   new NewName("{n} {u} {c} {s} {a} {t} {m} {r}"),
                                                   new RequestNamePattern(""),
                                                   new UrlPattern(""),
                                                   new ContentTypePattern(""), 
                                                   new StatusCodePattern(""), 
                                                   new AgentNamePattern(""),   
                                                   new TransactionNamePattern(""), 
                                                   new HttpMethodPattern(""),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(false), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(1), 
                                                   new ContinueOnNoMatchAtId(1),         
                                                   new DropOnMatch(false));  

        final RequestData data = new RequestData("name");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        assertEquals(1, rule.process(data));
        assertEquals("name url content 200 agent transaction method >=0", data.getName());
    }

    @Test
    public void fullTextPlaceholders_nonEmptyFilterPatterns() throws Exception
    {
        final var rule = new RequestProcessingRule(1, 
                                                   new NewName("{n} {u} {c} {s} {a} {t} {m} {r}"),
                                                   new RequestNamePattern("na"),
                                                   new UrlPattern("ur"),
                                                   new ContentTypePattern("co"), 
                                                   new StatusCodePattern("20"), 
                                                   new AgentNamePattern("ag"),   
                                                   new TransactionNamePattern("tr"), 
                                                   new HttpMethodPattern("me"),  
                                                   new RunTimeRanges(""),
                                                   new StopOnMatch(false), 
                                                   new RequestNameExcludePattern(""), 
                                                   new UrlExcludePattern(""), 
                                                   new ContentTypeExcludePattern(""), 
                                                   new StatusCodeExcludePattern(""), 
                                                   new AgentNameExcludePattern(""), 
                                                   new TransactionNameExcludePattern(""), 
                                                   new HttpMethodExcludePattern(""), 
                                                   new ContinueOnMatchAtId(1), 
                                                   new ContinueOnNoMatchAtId(2),         
                                                   new DropOnMatch(false));

        final RequestData data = new RequestData("name");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        assertEquals(1, rule.process(data));
        assertEquals("name url content 200 agent transaction method >=0", data.getName());
    }

    @Test(expected = InvalidRequestProcessingRuleException.class)
    public void validate_emptyFilterPatterns_invalidCapturingGroupIndex_1() throws Exception
    {
        // cannot refer to the group 1 in an empty pattern
        new RequestProcessingRule(1, 
                                  new NewName("{n:1}"),
                                  new RequestNamePattern(""),
                                  new UrlPattern(""),
                                  new ContentTypePattern(""), 
                                  new StatusCodePattern(""), 
                                  new AgentNamePattern(""),   
                                  new TransactionNamePattern(""), 
                                  new HttpMethodPattern(""),  
                                  new RunTimeRanges(""),
                                  new StopOnMatch(false), 
                                  new RequestNameExcludePattern(""), 
                                  new UrlExcludePattern(""), 
                                  new ContentTypeExcludePattern(""), 
                                  new StatusCodeExcludePattern(""), 
                                  new AgentNameExcludePattern(""), 
                                  new TransactionNameExcludePattern(""), 
                                  new HttpMethodExcludePattern(""), 
                                  new ContinueOnMatchAtId(1), 
                                  new ContinueOnNoMatchAtId(2),         
                                  new DropOnMatch(false));
    }

    @Test
    public void validate_nonEmptyFilterPatterns_validCapturingGroupIndexes() throws Exception
    {
        new RequestProcessingRule(1, 
                                  new NewName("{n}"),
                                  new RequestNamePattern("foo(bar)"),
                                  new UrlPattern(""),
                                  new ContentTypePattern(""), 
                                  new StatusCodePattern(""), 
                                  new AgentNamePattern(""),   
                                  new TransactionNamePattern(""), 
                                  new HttpMethodPattern(""),  
                                  new RunTimeRanges(""),
                                  new StopOnMatch(false), 
                                  new RequestNameExcludePattern(""), 
                                  new UrlExcludePattern(""), 
                                  new ContentTypeExcludePattern(""), 
                                  new StatusCodeExcludePattern(""), 
                                  new AgentNameExcludePattern(""), 
                                  new TransactionNameExcludePattern(""), 
                                  new HttpMethodExcludePattern(""), 
                                  new ContinueOnMatchAtId(1), 
                                  new ContinueOnNoMatchAtId(2),         
                                  new DropOnMatch(false));
        new RequestProcessingRule(1, 
                                  new NewName("{n:0}"),
                                  new RequestNamePattern("foo(bar)"),
                                  new UrlPattern(""),
                                  new ContentTypePattern(""), 
                                  new StatusCodePattern(""), 
                                  new AgentNamePattern(""),   
                                  new TransactionNamePattern(""), 
                                  new HttpMethodPattern(""),  
                                  new RunTimeRanges(""),
                                  new StopOnMatch(false), 
                                  new RequestNameExcludePattern(""), 
                                  new UrlExcludePattern(""), 
                                  new ContentTypeExcludePattern(""), 
                                  new StatusCodeExcludePattern(""), 
                                  new AgentNameExcludePattern(""), 
                                  new TransactionNameExcludePattern(""), 
                                  new HttpMethodExcludePattern(""), 
                                  new ContinueOnMatchAtId(1), 
                                  new ContinueOnNoMatchAtId(2),         
                                  new DropOnMatch(false));
        new RequestProcessingRule(1, 
                                  new NewName("{n:1}"),
                                  new RequestNamePattern("foo(bar)"),
                                  new UrlPattern(""),
                                  new ContentTypePattern(""), 
                                  new StatusCodePattern(""), 
                                  new AgentNamePattern(""),   
                                  new TransactionNamePattern(""), 
                                  new HttpMethodPattern(""),  
                                  new RunTimeRanges(""),
                                  new StopOnMatch(false), 
                                  new RequestNameExcludePattern(""), 
                                  new UrlExcludePattern(""), 
                                  new ContentTypeExcludePattern(""), 
                                  new StatusCodeExcludePattern(""), 
                                  new AgentNameExcludePattern(""), 
                                  new TransactionNameExcludePattern(""), 
                                  new HttpMethodExcludePattern(""), 
                                  new ContinueOnMatchAtId(1), 
                                  new ContinueOnNoMatchAtId(2),         
                                  new DropOnMatch(false));
    }

    @Test(expected = InvalidRequestProcessingRuleException.class)
    public void validate_nonEmptyFilterPatterns_invalidCapturingGroupIndex() throws Exception
    {
        // cannot refer to the group 2 in a pattern with just one group
        new RequestProcessingRule(1, 
                                  new NewName("{n:2}"),
                                  new RequestNamePattern("foo(bar)"),
                                  new UrlPattern(""),
                                  new ContentTypePattern(""), 
                                  new StatusCodePattern(""), 
                                  new AgentNamePattern(""),   
                                  new TransactionNamePattern(""), 
                                  new HttpMethodPattern(""),  
                                  new RunTimeRanges(""),
                                  new StopOnMatch(false), 
                                  new RequestNameExcludePattern(""), 
                                  new UrlExcludePattern(""), 
                                  new ContentTypeExcludePattern(""), 
                                  new StatusCodeExcludePattern(""), 
                                  new AgentNameExcludePattern(""), 
                                  new TransactionNameExcludePattern(""), 
                                  new HttpMethodExcludePattern(""), 
                                  new ContinueOnMatchAtId(1), 
                                  new ContinueOnNoMatchAtId(2),         
                                  new DropOnMatch(false));        
    }

    /**
     * Test that we process all rules when given and we don't want to skip anything
     */
    @Test
    public void testProcessAllRules() throws Exception
    {
        final var rule1 = new RequestProcessingRule(1, 
                                                    new NewName("1{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(1), 
                                                    new ContinueOnNoMatchAtId(1),         
                                                    new DropOnMatch(false));  
        final var rule2 = new RequestProcessingRule(2, 
                                                    new NewName("2{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(2), 
                                                    new ContinueOnNoMatchAtId(2),         
                                                    new DropOnMatch(false));  
        final var rule3 = new RequestProcessingRule(3, 
                                                    new NewName("3{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(3), 
                                                    new ContinueOnNoMatchAtId(3),         
                                                    new DropOnMatch(false));  
        final var data = new RequestData("request");
        final var rp = new RequestProcessing(List.of(rule1, rule2, rule3), false);
        
        var result = rp.postprocess(data);
        assertEquals("321request", result.getName());
    }

    /**
     * Test that we want to skip a rule and hit exactly the next.
     */
    @Test
    public void testProcessRules_SkipAndHitExactly() throws Exception
    {
        final var rule1 = new RequestProcessingRule(100, 
                                                    new NewName("1{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(300), 
                                                    new ContinueOnNoMatchAtId(100),         
                                                    new DropOnMatch(false));  
        final var rule2 = new RequestProcessingRule(200, 
                                                    new NewName("2{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(200), 
                                                    new ContinueOnNoMatchAtId(200),         
                                                    new DropOnMatch(false));  
        final var rule3 = new RequestProcessingRule(300, 
                                                    new NewName("3{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(300), 
                                                    new ContinueOnNoMatchAtId(300),         
                                                    new DropOnMatch(false));  
        final var data = new RequestData("request");
        final var rp = new RequestProcessing(List.of(rule1, rule2, rule3), false);
        
        var result = rp.postprocess(data);
        assertEquals("31request", result.getName());
    }
    
    /**
     * Skip and end beyond the last rule
     */
    @Test
    public void testProcessRules_SkipAndBeyondTheLast() throws Exception
    {
        final var rule1 = new RequestProcessingRule(100, 
                                                    new NewName("1{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(100), 
                                                    new ContinueOnNoMatchAtId(100),         
                                                    new DropOnMatch(false));  
        final var rule2 = new RequestProcessingRule(200, 
                                                    new NewName("2{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(2000), 
                                                    new ContinueOnNoMatchAtId(2000),         
                                                    new DropOnMatch(false));  
        final var rule3 = new RequestProcessingRule(300, 
                                                    new NewName("3{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(300), 
                                                    new ContinueOnNoMatchAtId(300),         
                                                    new DropOnMatch(false));  
        final var data = new RequestData("request");
        final var rp = new RequestProcessing(List.of(rule1, rule2, rule3), false);
        
        var result = rp.postprocess(data);
        assertEquals("21request", result.getName());
    }

    /**
     * Skip and jump close to the next wanted
     */
    @Test
    public void testProcessRules_SkipAndJumpClose() throws Exception
    {
        final var rule1 = new RequestProcessingRule(100, 
                                                    new NewName("1{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(250), 
                                                    new ContinueOnNoMatchAtId(100),         
                                                    new DropOnMatch(false));  
        final var rule2 = new RequestProcessingRule(200, 
                                                    new NewName("2{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(200), 
                                                    new ContinueOnNoMatchAtId(200),         
                                                    new DropOnMatch(false));  
        final var rule3 = new RequestProcessingRule(300, 
                                                    new NewName("3{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(300), 
                                                    new ContinueOnNoMatchAtId(300),         
                                                    new DropOnMatch(false));  
        final var data = new RequestData("request");
        final var rp = new RequestProcessing(List.of(rule1, rule2, rule3), false);
        
        var result = rp.postprocess(data);
        assertEquals("31request", result.getName());
    }
    
    /**
     * Skip when we don't match 
     */
    @Test
    public void testProcessRules_SkipOnNoMatch() throws Exception
    {
        final var rule1 = new RequestProcessingRule(100, 
                                                    new NewName("1{n}"),
                                                    new RequestNamePattern("foobar"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(100), 
                                                    new ContinueOnNoMatchAtId(250),         
                                                    new DropOnMatch(false));  
        final var rule2 = new RequestProcessingRule(200, 
                                                    new NewName("2{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(200), 
                                                    new ContinueOnNoMatchAtId(200),         
                                                    new DropOnMatch(false));  
        final var rule3 = new RequestProcessingRule(300, 
                                                    new NewName("3{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(300), 
                                                    new ContinueOnNoMatchAtId(300),         
                                                    new DropOnMatch(false));  
        final var data = new RequestData("request");
        final var rp = new RequestProcessing(List.of(rule1, rule2, rule3), false);
        
        var result = rp.postprocess(data);
        assertEquals("3request", result.getName());
    }
    
    /**
     * Check that we stop on the first match and don't continue to the next rules 
     */
    @Test
    public void testProcessRules_StopOnMatch() throws Exception
    {
        final var rule1 = new RequestProcessingRule(100, 
                                                    new NewName("1{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(100), 
                                                    new ContinueOnNoMatchAtId(250),         
                                                    new DropOnMatch(false));  
        final var rule2 = new RequestProcessingRule(200, 
                                                    new NewName("2{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(true), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(200), 
                                                    new ContinueOnNoMatchAtId(200),         
                                                    new DropOnMatch(false));  
        final var rule3 = new RequestProcessingRule(300, 
                                                    new NewName("3{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(300), 
                                                    new ContinueOnNoMatchAtId(300),         
                                                    new DropOnMatch(false));  
        final var data = new RequestData("request");
        final var rp = new RequestProcessing(List.of(rule1, rule2, rule3), false);
        
        var result = rp.postprocess(data);
        assertEquals("21request", result.getName());
    }
    
    /**
     * Check that we correctly indicate that we want to drop a request
     */
    @Test
    public void testProcessRules_DropOnMatch() throws Exception
    {
        final var rule1 = new RequestProcessingRule(100, 
                                                    new NewName("1{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(100), 
                                                    new ContinueOnNoMatchAtId(250),         
                                                    new DropOnMatch(false));  
        final var rule2 = new RequestProcessingRule(200, 
                                                    new NewName("2{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(200), 
                                                    new ContinueOnNoMatchAtId(200),         
                                                    new DropOnMatch(true));  
        final var rule3 = new RequestProcessingRule(300, 
                                                    new NewName("3{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(300), 
                                                    new ContinueOnNoMatchAtId(300),         
                                                    new DropOnMatch(false));  
        final var data = new RequestData("request");
        final var rp = new RequestProcessing(List.of(rule1, rule2, rule3), false);
        
        var result = rp.postprocess(data);
        assertNull(result);
    }
    
    /**
     * Check that we have the right order. This is meant ot aid programming issues and
     * is not exposed to the end user.
     * @throws InvalidRequestProcessingRuleException 
     */
    @Test
    public void testProcessRules_incorrectOrder() throws Exception
    {
        final var rule1 = new RequestProcessingRule(100, 
                                                    new NewName("1{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(100), 
                                                    new ContinueOnNoMatchAtId(250),         
                                                    new DropOnMatch(false));  
        final var rule2 = new RequestProcessingRule(99, 
                                                    new NewName("2{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(200), 
                                                    new ContinueOnNoMatchAtId(200),         
                                                    new DropOnMatch(true));  
        var ex = assertThrows(IllegalArgumentException.class, () -> {
            new RequestProcessing(List.of(rule1, rule2), false);
        });
        assertEquals("Request processing rules must be sorted by ID in ascending order.", ex.getMessage());
    }
    /**
     * Check that we have the right order. This is meant ot aid programming issues and
     * is not exposed to the end user.
     * @throws InvalidRequestProcessingRuleException 
     */
    @Test
    public void testProcessRules_incorrectOrderSameId() throws Exception
    {
        final var rule1 = new RequestProcessingRule(100, 
                                                    new NewName("1{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(100), 
                                                    new ContinueOnNoMatchAtId(250),         
                                                    new DropOnMatch(false));  
        final var rule2 = new RequestProcessingRule(100, 
                                                    new NewName("2{n}"),
                                                    new RequestNamePattern("request"),
                                                    new UrlPattern(""),
                                                    new ContentTypePattern(""), 
                                                    new StatusCodePattern(""), 
                                                    new AgentNamePattern(""),   
                                                    new TransactionNamePattern(""), 
                                                    new HttpMethodPattern(""),  
                                                    new RunTimeRanges(""),
                                                    new StopOnMatch(false), 
                                                    new RequestNameExcludePattern(""), 
                                                    new UrlExcludePattern(""), 
                                                    new ContentTypeExcludePattern(""), 
                                                    new StatusCodeExcludePattern(""), 
                                                    new AgentNameExcludePattern(""), 
                                                    new TransactionNameExcludePattern(""), 
                                                    new HttpMethodExcludePattern(""), 
                                                    new ContinueOnMatchAtId(200), 
                                                    new ContinueOnNoMatchAtId(200),         
                                                    new DropOnMatch(true));  
        var ex = assertThrows(IllegalArgumentException.class, () -> {
            new RequestProcessing(List.of(rule1, rule2), false);
        });
        assertEquals("Request processing rules must be sorted by ID in ascending order.", ex.getMessage());
    }
    
    @Test
    public void testInvalidContinueOnMatch() throws Exception
    {
        var ex = assertThrows(InvalidRequestProcessingRuleException.class, () -> {
            new RequestProcessingRule(42, 
                                      new NewName("fooBar"),
                                      new RequestNamePattern("request"), 
                                      new UrlPattern(""),
                                      new ContentTypePattern(""), 
                                      new StatusCodePattern(""), 
                                      new AgentNamePattern(""),   
                                      new TransactionNamePattern(""), 
                                      new HttpMethodPattern(""),  
                                      new RunTimeRanges(""),
                                      new StopOnMatch(false), 
                                      new RequestNameExcludePattern(""), 
                                      new UrlExcludePattern(""),
                                      new ContentTypeExcludePattern(""), 
                                      new StatusCodeExcludePattern(""),
                                      new AgentNameExcludePattern(""), 
                                      new TransactionNameExcludePattern(""),
                                      new HttpMethodExcludePattern(""), 
                                      new ContinueOnMatchAtId(9), 
                                      new ContinueOnNoMatchAtId(42),         
                                      new DropOnMatch(false));
        }); 
        assertEquals("Continue on match rule ID (9) must be greater or same than the rule ID (42)", ex.getMessage());
    }
    
    @Test
    public void testInvalidContinueOnNoMatch() throws Exception
    {
        var ex = assertThrows(InvalidRequestProcessingRuleException.class, () -> {
            new RequestProcessingRule(42, 
                                      new NewName("fooBar"),
                                      new RequestNamePattern("request"), 
                                      new UrlPattern(""),
                                      new ContentTypePattern(""), 
                                      new StatusCodePattern(""), 
                                      new AgentNamePattern(""),   
                                      new TransactionNamePattern(""), 
                                      new HttpMethodPattern(""),  
                                      new RunTimeRanges(""),
                                      new StopOnMatch(false), 
                                      new RequestNameExcludePattern(""), 
                                      new UrlExcludePattern(""),
                                      new ContentTypeExcludePattern(""), 
                                      new StatusCodeExcludePattern(""),
                                      new AgentNameExcludePattern(""), 
                                      new TransactionNameExcludePattern(""),
                                      new HttpMethodExcludePattern(""), 
                                      new ContinueOnMatchAtId(42), 
                                      new ContinueOnNoMatchAtId(4),         
                                      new DropOnMatch(false));
        }); 
        assertEquals("Continue on no match rule ID (4) must be greater or same than the rule ID (42)", ex.getMessage());
    }
    
    /**
     * Ensure that we see that the group index is not parseable
     */
    @Test
    public void cannotParseGroupIndex() throws Exception
    {
        var ex = assertThrows(InvalidRequestProcessingRuleException.class, () -> {
            new RequestProcessingRule(42, 
                                      new NewName("{n:a}"),
                                      new RequestNamePattern("request"), 
                                      new UrlPattern(""),
                                      new ContentTypePattern(""), 
                                      new StatusCodePattern(""), 
                                      new AgentNamePattern(""),   
                                      new TransactionNamePattern(""), 
                                      new HttpMethodPattern(""),  
                                      new RunTimeRanges(""),
                                      new StopOnMatch(false), 
                                      new RequestNameExcludePattern(""), 
                                      new UrlExcludePattern(""),
                                      new ContentTypeExcludePattern(""), 
                                      new StatusCodeExcludePattern(""),
                                      new AgentNameExcludePattern(""), 
                                      new TransactionNameExcludePattern(""),
                                      new HttpMethodExcludePattern(""), 
                                      new ContinueOnMatchAtId(42), 
                                      new ContinueOnNoMatchAtId(42),         
                                      new DropOnMatch(false));
        }); 
        assertEquals("Failed to parse the matching group index 'a' as integer", ex.getMessage());
    }
}
