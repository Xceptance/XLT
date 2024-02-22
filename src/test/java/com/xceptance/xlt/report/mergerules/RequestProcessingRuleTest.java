/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * Tests the request renaming magic implemented by {@link RequestProcessingRule}.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 * @author Rene Schwietzke (Xceptance GmbH)
 */
public class RequestProcessingRuleTest
{
    /*
     * For simplicity the following tests use content type pattern only since all pattern-based request filters share
     * the same logic.
     */

    @Test
    public void testPatternIncludeOnly() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("fooBar ({c:0})",
                                                                     "", "", "text/html", "", "", "", "", "", true,
                                                                     "", "", "", "", "", "", "", false);

        final String name = "baz";
        final RequestData data = new RequestData(name);
        data.setContentType("image/jpeg");

        // no match
        Assert.assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data));
        // unchanged
        Assert.assertEquals(name, data.getName());

        data.setContentType("text/html");

        // match and stop requested on match
        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        // changed
        Assert.assertEquals("fooBar (text/html)", data.getName());
    }

    @Test
    public void testPatternExcludeOnly_StopOnMatch() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("fooBar ({c:0})",
                                                                     "", "", "", "", "", "", "", "", true,
                                                                     "", "", "text/html", "", "", "", "", false);

        final String name = "baz";
        final RequestData data = new RequestData(name);
        data.setContentType("image/jpeg");

        // exclude does not match, hence all matches; hence we stop
        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("fooBar (image/jpeg)", data.getName());

        data.setName(name);
        data.setContentType("text/html");

        // exlude matches, hence the rules does not apply
        Assert.assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data));
        Assert.assertEquals(name, data.getName());
    }

    @Test
    public void testPatternExcludeOnly_NoStopOnMatch() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("fooBar ({c:0})",
                                                                     "", "", "", "", "", "", "", "", false,
                                                                     "", "", "text/html", "", "", "", "", false);

        final String name = "baz";
        final RequestData data = new RequestData(name);
        data.setContentType("image/jpeg");

        // exclude does not match, hence all matches; hence we continue
        Assert.assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data));
        Assert.assertEquals("fooBar (image/jpeg)", data.getName());

        data.setName(name);
        data.setContentType("text/html");

        // exlude matches, hence the rule does not apply
        Assert.assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data));
        Assert.assertEquals(name, data.getName());
    }


    @Test
    public void testPatternIncludeExclude() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("fooBar ({c:0})", "", "", "html", "", "", "", "", "", true,
                                                                     "", "", "xhtml", "", "", "", "", false);

        final String name = "baz";
        final RequestData data = new RequestData(name);
        data.setContentType("image/jpeg");

        // include fails, exclude fails, just process normally
        Assert.assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data));
        Assert.assertEquals(name, data.getName());

        // include matches, stop applies
        data.setContentType("text/html");
        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("fooBar (html)", data.getName());

        // reset
        data.setName(name);
        data.setContentType("text/xhtml");
        // include does match, exclude does match -> NOT MATCH
        Assert.assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data));
        Assert.assertEquals(name, data.getName()); // no change
    }

    /**
     * <pre>
     * ################################################################################
     * #
     * # Project-Specific Report Generator Settings
     * #
     * # When creating the report, all requests with the same name will go into the
     * # same bucket. This initial bucketing can be customized by renaming requests.
     * # Via renaming you can either create fewer, but more general buckets or more,
     * # but more specific buckets. Alternatively, you may also choose to delete
     * # certain requests altogether.
     * #
     * # This process is controlled by "request processing rules" (formerly known as
     * # "request merge rules"). A rule knows how to select all requests of interest
     * # and how to process them.
     * #
     * # You may specify one or more rules as outlined below:
     * #
     * #   com.xceptance.xlt.reportgenerator.requestMergeRules.<num>.<param> = <value>
     * #
     * # The rules are sorted numerically based on <num> and are applied in ascending
     * # order.
     * #
     * # The supported values for "<param>" are:
     * #
     * #   newName .................. The new request name (required, unless
     * #                              dropOnMatch is true).
     * #
     * #   namePattern [n] .......... Reg-ex defining a matching request name
     * #   transactionPattern [t] ... Reg-ex defining a matching transaction name
     * #   agentPattern [a] ......... Reg-ex defining a matching agent name
     * #   contentTypePattern [c] ... Reg-ex defining a matching response content type
     * #   statusCodePattern [s] .... Reg-ex defining a matching status code
     * #   urlPattern [u] ........... Reg-ex defining a matching request URL
     * #   httpMethodPattern [m] .... Reg-ex defining a matching HTTP method
     * #   runTimeRanges [r] ........ List of run time segment boundaries
     * #
     * #   stopOnMatch .............. Whether or not to process the next rule even if
     * #                              the current rule applied (defaults to true).
     * #
     * #   dropOnMatch .............. Whether or not to discard a matching request
     * #                              instead of renaming it (defaults to false). If
     * #                              the rule applied, request processing will stop.
     * #
     * # At least one of namePattern, transactionPattern, agentPattern,
     * # contentTypePattern, statusCodePattern, urlPattern, httpMethodpattern or
     * # runTimeRanges must be specified. If more than one pattern is given, all given
     * # patterns must match.
     * #
     * # Note that newName may contain placeholders, which are replaced with the
     * # specified capturing group from the respective pattern. The placeholder
     * # format is as follows: {<category>:<capturingGroupIndex>}, where <category> is
     * # the type code of the respective pattern (given in brackets above) and
     * # <capturingGroupIndex> denotes the respective capturing group in the selected
     * # pattern (does not apply to runTimeRanges).
     * #
     * # Excluding instead of Including
     * #
     * #   com.xceptance.xlt.reportgenerator.requestMergeRules.<num>.<param>.exclude = <value>
     * #
     * # All requests that match the exclude pattern will not be selected. For example,
     * # to create a bucket for all non-JavaScript resources, you would setup a rule like that.
     * #
     * #   com.xceptance.xlt.reportgenerator.requestMergeRules.1.newName = {n:0} NonJS
     * #   com.xceptance.xlt.reportgenerator.requestMergeRules.1.namePattern = .+
     * #   com.xceptance.xlt.reportgenerator.requestMergeRules.1.contentTypePattern.exclude = javascript
     * #   com.xceptance.xlt.reportgenerator.requestMergeRules.1.stopOnMatch = false
     * #
     * # Please note that an include pattern as well as an exclude pattern can be specified for
     * # a pattern type at the same time. In this case, a request is selected if and only if it
     * # matches the include pattern, but does not match the exclude pattern.
     * #
     * ################################################################################
     * </pre>
     */

    /**
     * All empty. Stop false. Empty rules means, that everything matches.
     */
    @Test
    public void testRules_allEmpty_stopFalse() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("New", // newName
                                                                     "", // requestNamePattern
                                                                     "", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     false, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

        final RequestData data = new RequestData("Old");
        Assert.assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data));
        Assert.assertEquals("New", data.getName());
    }

    /**
     * All empty. Stop true
     */
    @Test
    public void allEmpty_butApply_Stop() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("New", // newName
                                                                     "", // requestNamePattern
                                                                     "", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch
        // match, change, stop
        final RequestData data = new RequestData("Old");
        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("New", data.getName());
    }

    /**
     * All empty but data has something set. Stop
     */
    @Test
    public void allEmpty_butApply_Stop_DataSet() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("New", // newName
                                                                     "", // requestNamePattern
                                                                     "", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

        final RequestData data = new RequestData("Old");
        data.setAgentName("Agent-007");
        data.setHttpMethod("GET");
        data.setUrl("https://www.foo.bar/all");

        // match, change, STOP
        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("New", data.getName());
    }

    /**
     * Some data set, all empty, drop is set and is fired
     */
    @Test
    public void allAllEmpty_butApply_Drop1() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("New", // newName
                                                                     "", // requestNamePattern
                                                                     "", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     true); // dropOnMatch

        final RequestData data = new RequestData("Old");
        data.setAgentName("Agent-007");
        data.setHttpMethod("GET");
        data.setUrl("https://www.foo.bar/all");

        // match, change, drop
        Assert.assertEquals(RequestProcessingRule.ReturnState.DROP, rule.process(data));
        Assert.assertEquals("Old", data.getName());
    }

    /**
     * Some data set, all empty, continue
     */
    @Test
    public void allAllEmpty_butApply_Continue() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("New", // newName
                                                                     "", // requestNamePattern
                                                                     "", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     false, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

        final RequestData data = new RequestData("Old");
        data.setAgentName("Agent-007");
        data.setHttpMethod("GET");
        data.setUrl(XltCharBuffer.valueOf("https://www.foo.bar/all"));

        // match, change, drop
        Assert.assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data));
        Assert.assertEquals("New", data.getName());
    }

    /**
     * Some data, all empty, drop is set and stop is false
     */
    @Test
    public void allAllEmpty_butApply_Drop2() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("New", // newName
                                                                     "", // requestNamePattern
                                                                     "", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     false, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     true); // dropOnMatch

        final RequestData data = new RequestData("Old");
        data.setAgentName("Agent-007");
        data.setHttpMethod("GET");
        data.setUrl("https://www.foo.bar/all");

        // match, change, drop
        Assert.assertEquals(RequestProcessingRule.ReturnState.DROP, rule.process(data));
        Assert.assertEquals("Old", data.getName());
    }

    /**
     * All include patterns used and match
     */
    @Test
    public void testAllIncludePatternsSetAndMatch() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("New", // newName
                                                                     "request", // requestNamePattern
                                                                     "url", // urlPattern
                                                                     "content", // contentTypePattern
                                                                     "200", // statusCodePattern
                                                                     "agent", // agentNamePattern
                                                                     "transaction", // transactionNamePattern
                                                                     "method", // httpMethodPattern
                                                                     "1000, 2000", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

        final RequestData data = new RequestData("request");
        // data.setName("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(999);

        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("New", data.getName());
    }

    /**
     * All include patterns used and match. Name captured all
     */
    @Test
    public void testAllIncludePatternsSetAndMatch_CaptureAll() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n:0}-{u:0}-{c:0}-{s:0}-{a:0}-{t:0}-{m:0}-{r:0}", // newName
                                                                     "request", // requestNamePattern
                                                                     "url", // urlPattern
                                                                     "content", // contentTypePattern
                                                                     "200", // statusCodePattern
                                                                     "agent", // agentNamePattern
                                                                     "transaction", // transactionNamePattern
                                                                     "method", // httpMethodPattern
                                                                     "1000, 2000", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

        final RequestData data = new RequestData("request");
        // data.setName("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("request-url-content-200-agent-transaction-method-0..999", data.getName());
    }

    /**
     * All include patterns used and match. Name captured all, short  attribute definition
     */
    @Test
    public void testAllIncludePatternsSetAndMatch_CaptureAll_NoPosAttribute() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n}-{u}-{c}-{s}-{a}-{t}-{m}-{r}", // newName
                                                                     "request", // requestNamePattern
                                                                     "url", // urlPattern
                                                                     "content", // contentTypePattern
                                                                     "200", // statusCodePattern
                                                                     "agent", // agentNamePattern
                                                                     "transaction", // transactionNamePattern
                                                                     "method", // httpMethodPattern
                                                                     "1000, 2000", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

        final RequestData data = new RequestData("request");
        // data.setName("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("request-url-content-200-agent-transaction-method-0..999", data.getName());
    }

    /**
     * All include patterns used and match
     */
    @Test
    public void testAllExcludePatternsSetAndMatch() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("New", // newName
                                                                     "", "", "", "", "", "", "", "", true, // stopOnMatch
                                                                     "not-request", "not-url", "not-content", "404", "not-agent", "not-transaction", "not-method", false);

        final RequestData data = new RequestData("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(999);

        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("New", data.getName());
    }

    /**
     * All exclude patterns used and match. Name captured all
     */
    @Test
    public void testAllExcludePatternsSetAndMatch_CaptureAll() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n:0}{u:0}{c:0}{s:0}{a:0}{t:0}{m:0}{r:0}", // newName
                                                                     "", "", "", "", "", "", "", "1000", true, // stopOnMatch
                                                                     "not-request", "not-url", "not-content", "404", "not-agent",
                                                                     "not-transaction", "not-method", false);

        final RequestData data = new RequestData("request");
        // data.setName("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("requesturlcontent200agenttransactionmethod0..999", data.getName());
    }

    /**
     * All exclude patterns used and exclude all. Name captured all
     * Include matches and exclude, hence include rules don't fire
     */
    @Test
    public void testAllExcludePatternsSetAndExcludeMatch_CaptureAll() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n:0}{u:0}{c:0}{s:0}{a:0}{t:0}{m:0}{r:0}", // newName
                                                                     "", "", "", "", "", "", "", "1000", true, // stopOnMatch
                                                                     "not-request", "not-url", "not-content", "not-200", "not-agent",
                                                                     "not-transaction", "not-method", false);

        final RequestData data = new RequestData("not-request");
        // data.setName("request");
        data.setUrl("not-url");
        data.setContentType("not-content");
        data.setResponseCode(200);
        data.setAgentName("not-agent");
        data.setTransactionName("not-transaction");
        data.setHttpMethod("not-method");
        data.setRunTime(500);

        Assert.assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data));
        Assert.assertEquals("not-request", data.getName());
    }

    /**
     * namePattern [n] .......... reg-ex defining a matching request name
     */
    @Test
    public void testNamePattern() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n:2}-{n:0}-{n:1}", // newName
                                                                     "re(q)ue(st)", // requestNamePattern
                                                                     "", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

        final RequestData data = new RequestData("request");

        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("st-request-q", data.getName());
    }

    /**
     * namePattern [n] .......... reg-ex defining a matching request name Index does not exist
     */
    @Test(expected = InvalidRequestProcessingRuleException.class)
    public void testNamePattern_IndexDoesNotExist() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n:2}-{n:0}-{n:10}", // newName
                                                                     "re(q)ue(st)", // requestNamePattern
                                                                     "", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

        // match
        final RequestData data = new RequestData("request");
        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("st-request-q", data.getName());
    }

    /**
     * namePattern [n] .......... reg-ex defining a matching request name Index does not exist
     */
    @Test
    public void testNamePattern_Normal() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n:2}-{n:0}-{n:1}", // newName
                                                                     "^re(q)ue(st)", // requestNamePattern
                                                                     "", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

        // match
        final RequestData data1 = new RequestData("request");

        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data1));
        Assert.assertEquals("st-request-q", data1.getName());

        // do not match
        final RequestData data2 = new RequestData("noperequest");

        Assert.assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data2));
        Assert.assertEquals("noperequest", data2.getName());
    }

    /**
     * # transactionPattern [t] ... reg-ex defining a matching transaction name
     */
    @Test
    public void testTransactionPattern_Normal() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n:0}-{t:1} {t:0}", // newName
                                                                     "request", // requestNamePattern
                                                                     "", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "T.*bar-([0-9])", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

        // match
        final RequestData data = new RequestData("request");
        data.setTransactionName("TFoobar-2");

        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("request-2 TFoobar-2", data.getName());

        // do not match
        final RequestData data2 = new RequestData("request");
        data2.setTransactionName("TLateStuff");

        Assert.assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data2));
        Assert.assertEquals("request", data2.getName());
    }

    /**
     * # agentPattern [a] ......... reg-ex defining a matching agent name
     */
    @Test
    public void testAgentPattern_Normal() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n:0}-{a:1} {a:0}", // newName
                                                                     "request", // requestNamePattern
                                                                     "", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "A.*bar-([0-9])", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

        // match
        final RequestData data = new RequestData("request");
        data.setAgentName("AFoobar-2");

        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("request-2 AFoobar-2", data.getName());

        // do not match
        final RequestData data2 = new RequestData("request");
        data2.setAgentName("TLateStuff");

        assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data2));
        Assert.assertEquals("request", data2.getName());
    }

    /**
     * # contentTypePattern [c] ... reg-ex defining a matching response content type
     */
    @Test
    public void testContentPattern_Normal() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n:0}-{c:1} {c:0}", // newName
                                                                     "request", // requestNamePattern
                                                                     "", // urlPattern
                                                                     "image/([a-z]{3,4})$", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

        // match
        final RequestData data = new RequestData("request");
        data.setContentType("image/jpeg");

        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("request-jpeg image/jpeg", data.getName());

        // do not match
        final RequestData data2 = new RequestData("request");
        data2.setContentType("image/coffeelatte");

        assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data2));
        Assert.assertEquals("request", data2.getName());
    }

    /**
     * # statusCodePattern [s] .... reg-ex defining a matching status code
     */
    @Test
    public void testStatusCodePattern_Normal() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n:0}-{s:1} {s:0}", // newName
                                                                     "request", // requestNamePattern
                                                                     "", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "(30[12])", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

        // match
        final RequestData data = new RequestData("request");
        data.setResponseCode(302);

        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("request-302 302", data.getName());

        // do not match
        final RequestData data2 = new RequestData("request");
        data2.setResponseCode(304);

        assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data2));
        Assert.assertEquals("request", data2.getName());
    }

    /**
     * # urlPattern [u] ........... reg-ex defining a matching request URL
     */
    @Test
    public void testURLPattern_Normal() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n:0}-{u:1} {u:0}", // newName
                                                                     "request", // requestNamePattern
                                                                     "https?://foobar.com/([^/]+)/", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

        // match
        final RequestData data = new RequestData("request");
        data.setUrl("https://foobar.com/tiger/");
        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("request-tiger https://foobar.com/tiger/", data.getName());

        // do not match
        final RequestData data2 = new RequestData("request");
        data2.setUrl("https://foobar.de/tiger/");

        assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data2));
        Assert.assertEquals("request", data2.getName());
    }

    /**
     * # httpMethodPattern [m] ........... reg-ex defining a matching HTTP method
     */
    @Test
    public void testHttpMethodPattern_Normal() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n:0} {m:1}", // newName
                                                                     "request", // requestNamePattern
                                                                     "", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "(GET)", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

        // match
        final RequestData data = new RequestData("request");
        data.setHttpMethod("GET");

        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("request GET", data.getName());

        // do not match
        final RequestData data2 = new RequestData("request");
        data2.setHttpMethod("PUT");

        Assert.assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data2));
        Assert.assertEquals("request", data2.getName());
    }

    /**
     * Small helper for runtime ranges
     */
    private void responseTimeRanges(final RequestProcessingRule rule, final int runtime, final String expected)
    {
        final RequestData data = new RequestData("request");
        data.setRunTime(runtime);
        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals(expected, data.getName());
    }

    /**
     * # runTimeRanges [r] ........ list of run time segment boundaries
     */
    @Test
    public void testResponseTimeRanges_Normal() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n:0} [{r}]", // newName
                                                                     "request", // requestNamePattern
                                                                     "", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "100, 3000, 5000", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

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
        final RequestProcessingRule rule = new RequestProcessingRule("{n:1}-{u:1}-{c:1}-{s:1}-{a:1}-{t:2}{t:3}{t:1}-{m:1}-{r}", // newName
                                                                     "r(.*)", // requestNamePattern
                                                                     "([urlURL]{3})", // urlPattern
                                                                     "^(.+)$", // contentTypePattern
                                                                     "2([01])[0-9]", // statusCodePattern
                                                                     "(a?)g?e?n?t?", // agentNamePattern
                                                                     "((.+)(action))$", // transactionNamePattern
                                                                     "^(.+)$", // httpMethodPattern
                                                                     "1000, 2000", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "not-request", "not-url", "not-content", "404", "not-agent",
                                                                     "not-transaction", "not-method", false);

        final RequestData data = new RequestData("request");
        // data.setName("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("equest-url-content-0-a-transactiontransaction-method-0..999", data.getName());
    }

    /**
     * Include and exclude set. Match include Please note that an include pattern as well as an exclude pattern can be
     * specified for a pattern type at the same time. In this case, a request is selected if and only if it matches the
     * include pattern, but does not match the exclude pattern.
     */
    @Test
    public void testAllIncludeAndExclude_MatchIncludeNotExclude_CaptureAll() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n:0}{u:0}{c:0}{s:0}{a:0}{t:0}-{m:0}-{r:0}", // newName
                                                                     "request", // requestNamePattern
                                                                     "url", // urlPattern
                                                                     "content", // contentTypePattern
                                                                     "200", // statusCodePattern
                                                                     "agent", // agentNamePattern
                                                                     "transaction", // transactionNamePattern
                                                                     "method", // httpMethodPattern
                                                                     "1000, 2000", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "not-request", "not-url", "not-content", "404", "not-agent",
                                                                     "not-transaction", "not-method", false);

        final RequestData data = new RequestData("request");
        // data.setName("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        Assert.assertEquals(RequestProcessingRule.ReturnState.STOP, rule.process(data));
        Assert.assertEquals("requesturlcontent200agenttransaction-method-0..999", data.getName());

    }

    /**
     * Include and exclude set. Match Include and Exclude
     */
    @Test
    public void testAllIncludeAndExclude_MatchIncludeExclude_CaptureAll() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n:0}{u:0}{c:0}{s:0}{a:0}{t:0}{m:0}{r:0}", // newName
                                                                     "request", // requestNamePattern
                                                                     "url", // urlPattern
                                                                     "content", // contentTypePattern
                                                                     "200", // statusCodePattern
                                                                     "agent", // agentNamePattern
                                                                     "transaction", // transactionNamePattern
                                                                     "method", // httpMethodPattern
                                                                     "1000, 2000", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "request", "url", "content", "200", "agent", "transaction", "method",
                                                                     false);

        final RequestData data = new RequestData("request");
        // data.setName("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data));
        Assert.assertEquals("request", data.getName());

    }

    /**
     * Include and exclude set. Match not Include but Exclude Please note that an include pattern as well as an exclude
     * pattern can be specified for a pattern type at the same time. In this case, a request is selected if and only if
     * it matches the include pattern, but does not match the exclude pattern.
     */
    @Test
    public void testAllIncludeAndExclude_MatchNotIncludeButExclude_CaptureAll() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n:0}{u:0}{c:0}{s:0}{a:0}{t:0}{m:0}{r:0}", // newName
                                                                     "request", // requestNamePattern
                                                                     "url", // urlPattern
                                                                     "content", // contentTypePattern
                                                                     "200", // statusCodePattern
                                                                     "agent", // agentNamePattern
                                                                     "transaction", // transactionNamePattern
                                                                     "method", // httpMethodPattern
                                                                     "1000, 2000", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "not-request", "not-url", "not-content", "404", "not-agent",
                                                                     "not-transaction", "not-method", false);

        final RequestData data = new RequestData("not-request");
        // data.setName("request");
        data.setUrl("not-url");
        data.setContentType("not-content");
        data.setResponseCode(404);
        data.setAgentName("not-agent");
        data.setTransactionName("not-transaction");
        data.setHttpMethod("not-method");
        data.setRunTime(500);

        assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data));
        Assert.assertEquals("not-request", data.getName());
    }

    /**
     * Include and exclude set. Match not Include but Exclude Please note that an include pattern as well as an exclude
     * pattern can be specified for a pattern type at the same time. In this case, a request is selected if and only if
     * it matches the include pattern, but does not match the exclude pattern.
     */
    @Test
    public void includeDataWithouPositionNumber() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n}-{u}-{c}-{s}-{a}-{t}-{r}", // newName
                                                                     "request", // requestNamePattern
                                                                     "url", // urlPattern
                                                                     "content", // contentTypePattern
                                                                     "200", // statusCodePattern
                                                                     "agent", // agentNamePattern
                                                                     "transaction", // transactionNamePattern
                                                                     "GET",
                                                                     "300, 2000", // responseTimeRanges
                                                                     false, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "",
                                                                     false); // dropOnMatch

        final RequestData data = new RequestData("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("GET");
        data.setRunTime(500);

        assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data));
        Assert.assertEquals("request-url-content-200-agent-transaction-300..1999", data.getName());

    }

    @Test(expected = InvalidRequestProcessingRuleException.class)
    public void testInvalidRegex() throws Exception
    {
        new RequestProcessingRule("{n:1}", "([]-]", "", "", "", "", "", "", "", true, "", "", "", "", "", "", "", false);
    }

    /**
     * It matches all include but DROP fires first
     */
    @Test
    public void stopAndDrop() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("", // newName
                                                                     "", // requestNamePattern
                                                                     "", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     true, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     true); // dropOnMatch

        final RequestData data = new RequestData("Old");

        Assert.assertEquals(RequestProcessingRule.ReturnState.DROP, rule.process(data));
    }

    /**
     * I can specify a name (not a must) when a drop is wanted. This does not do a things of
     * course, but might help to avoid confusion when setting up rules as well as helps to
     * name rules kinda.
     */
    @Test
    public void canGiveNameDespiteDrop() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("My new name", // newName
                                                                     "", // requestNamePattern
                                                                     "url", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     false, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     true); // dropOnMatch

        final RequestData data = new RequestData("Old");
        data.setUrl(XltCharBuffer.valueOf("url"));

        Assert.assertEquals(RequestProcessingRule.ReturnState.DROP, rule.process(data));
    }

    @Test
    public void fullTextPlaceholders_emptyFilterPatterns() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n} {u} {c} {s} {a} {t} {m} {r}", // newName
                                                                     "", // requestNamePattern
                                                                     "", // urlPattern
                                                                     "", // contentTypePattern
                                                                     "", // statusCodePattern
                                                                     "", // agentNamePattern
                                                                     "", // transactionNamePattern
                                                                     "", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     false, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

        final RequestData data = new RequestData("name");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data));
        Assert.assertEquals("name url content 200 agent transaction method >=0", data.getName());
    }

    @Test
    public void fullTextPlaceholders_nonEmptyFilterPatterns() throws Exception
    {
        final RequestProcessingRule rule = new RequestProcessingRule("{n} {u} {c} {s} {a} {t} {m} {r}", // newName
                                                                     "na", // requestNamePattern
                                                                     "ur", // urlPattern
                                                                     "co", // contentTypePattern
                                                                     "20", // statusCodePattern
                                                                     "ag", // agentNamePattern
                                                                     "tr", // transactionNamePattern
                                                                     "me", // httpMethodPattern
                                                                     "", // responseTimeRanges
                                                                     false, // stopOnMatch
                                                                     "", // requestNameExcludePattern
                                                                     "", // urlExcludePattern
                                                                     "", // contentTypeExcludePattern
                                                                     "", // statusCodeExcludePattern
                                                                     "", // agentNameExcludePattern
                                                                     "", // transactionNameExcludePattern
                                                                     "", // httpMethodExcludePattern
                                                                     false); // dropOnMatch

        final RequestData data = new RequestData("name");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        assertEquals(RequestProcessingRule.ReturnState.CONTINUE, rule.process(data));
        Assert.assertEquals("name url content 200 agent transaction method >=0", data.getName());
    }

    // TODO: #3252
    // @Test(expected = InvalidRequestProcessingRuleException.class)
    // public void validate_emptyFilterPatterns_invalidCapturingGroupIndex_0() throws Exception
    // {
    // // cannot refer to the group 0 in an empty pattern
    // new RequestProcessingRule("{n:0}", "", "", "", "", "", "", "", false, "", "", "", "", "", "", false);
    // }

    @Test(expected = InvalidRequestProcessingRuleException.class)
    public void validate_emptyFilterPatterns_invalidCapturingGroupIndex_1() throws Exception
    {
        // cannot refer to the group 1 in an empty pattern
        new RequestProcessingRule("{n:1}", "", "", "", "", "", "", "", "", false, "", "", "", "", "", "", "", false);
    }

    @Test
    public void validate_nonEmptyFilterPatterns_validCapturingGroupIndexes() throws Exception
    {
        new RequestProcessingRule("{n}", "foo(bar)", "", "", "", "", "", "", "", false, "", "", "", "", "", "", "", false);
        new RequestProcessingRule("{n:0}", "foo(bar)", "", "", "", "", "", "", "", false, "", "", "", "", "", "", "", false);
        new RequestProcessingRule("{n:1}", "foo(bar)", "", "", "", "", "", "", "", false, "", "", "", "", "", "", "", false);
    }

    @Test(expected = InvalidRequestProcessingRuleException.class)
    public void validate_nonEmptyFilterPatterns_invalidCapturingGroupIndex() throws Exception
    {
        // cannot refer to the group 2 in a pattern with just one group
        new RequestProcessingRule("{n:2}", "foo(bar)", "", "", "", "", "", "", "", false, "", "", "", "", "", "", "", false);
    }
}
