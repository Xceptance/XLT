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
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.report.mergerules.MergeRule.*;

public class MergeRule_Naming_Test extends MergeRuleTestBase
{
    @Test
    public void noNewName() throws Exception
    {
        var rule = getMergeRule(42, new NewName("fooBar"), new StopOnMatch(false));
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("request");
        assertEquals(42, rule.process(data));
        assertEquals("fooBar", data.getName());
    }

    @Test
    public void captureWithoutARuleBut0Position() throws Exception
    {
        var rule = getMergeRule(42, new NewName("foo {a:0}"), new StopOnMatch(false));
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("request");
        data.setAgentName("agent007");
        assertEquals(42, rule.process(data));
        assertEquals("foo agent007", data.getName());
    }

    @Test
    public void captureWithoutARule() throws Exception
    {
        var rule = getMergeRule(42, new NewName("foo {a}"), new StopOnMatch(false));
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("request");
        data.setAgentName("agent007");
        assertEquals(42, rule.process(data));
        assertEquals("foo agent007", data.getName());
    }

    @Test
    public void allIncludePatternsSetAndMatch_CaptureAll() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n:0}-{u:0}-{c:0}-{s:0}-{a:0}-{t:0}-{m:0}-{r:0}"),
            new NamePattern("request"),
            new UrlPattern("url"),
            new ContentTypePattern("content"),
            new StatusCodePattern("200"),
            new AgentNamePattern("agent"),
            new TransactionNamePattern("transaction"),
            new HttpMethodPattern("method"),
            new RunTimeRanges("1000,2000"),
            new StopOnMatch(true)
        );
        var data = new RequestData("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("request-url-content-200-agent-transaction-method-0..999", data.getName());
    }

    @Test
    public void allIncludePatternsSetAndMatch_CaptureAll_NoPosAttribute() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n}-{u}-{c}-{s}-{a}-{t}-{m}-{r}"),
            new NamePattern("request"),
            new UrlPattern("url"),
            new ContentTypePattern("content"),
            new StatusCodePattern("200"),
            new AgentNamePattern("agent"),
            new TransactionNamePattern("transaction"),
            new HttpMethodPattern("method"),
            new RunTimeRanges("1000,2000"),
            new StopOnMatch(true)
        );
        var data = new RequestData("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("request-url-content-200-agent-transaction-method-0..999", data.getName());
    }

    @Test
    public void allExcludePatternsSetAndMatch() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("New"),
            new NameExcludePattern("not-request"),
            new UrlExcludePattern("not-url"),
            new ContentTypeExcludePattern("not-content"),
            new StatusCodeExcludePattern("404"),
            new AgentNameExcludePattern("not-agent"),
            new TransactionNameExcludePattern("not-transaction"),
            new HttpMethodExcludePattern("not-method"),
            new StopOnMatch(true)
        );
        var data = new RequestData("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(999);

        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("New", data.getName());
    }

    @Test
    public void allExcludePatternsSetAndMatch_CaptureAll() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n:0}{u:0}{c:0}{s:0}{a:0}{t:0}{m:0}{r:0}"),
            new RunTimeRanges("1000"),
            new NameExcludePattern("not-request"),
            new UrlExcludePattern("not-url"),
            new ContentTypeExcludePattern("not-content"),
            new StatusCodeExcludePattern("404"),
            new AgentNameExcludePattern("not-agent"),
            new TransactionNameExcludePattern("not-transaction"),
            new HttpMethodExcludePattern("not-method"),
            new StopOnMatch(true)
        );
        var data = new RequestData("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("requesturlcontent200agenttransactionmethod0..999", data.getName());
    }

    @Test
    public void allExcludePatternsSetAndExcludeMatch_CaptureAll() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n:0}{u:0}{c:0}{s:0}{a:0}{t:0}{m:0}{r:0}-any"),
            new RunTimeRanges("1000"),
            new NameExcludePattern("not-request"),
            new UrlExcludePattern("not-url"),
            new ContentTypeExcludePattern("not-content"),
            new StatusCodeExcludePattern("not-200"),
            new AgentNameExcludePattern("not-agent"),
            new TransactionNameExcludePattern("not-transaction"),
            new HttpMethodExcludePattern("not-method"),
            new StopOnMatch(true)
        );
        var data = new RequestData("not-request");
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

    @Test
    public void namePattern() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n:2}-{n:0}-{n:1}"),
            new NamePattern("re(q)ue(st)"),
            new RunTimeRanges("1000"),
            new StopOnMatch(true)
        );
        var data = new RequestData("request");

        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("st-request-q", data.getName());
    }

    @Test(expected = InvalidMergeRuleException.class)
    public void namePattern_IndexDoesNotExist() throws Exception
    {
        getMergeRule(0,
            new NewName("{n:2}-{n:0}-{n:10}"),
            new NamePattern("re(q)ue(st)"),
            new RunTimeRanges("1000"),
            new StopOnMatch(true)
        );
    }

    @Test
    public void transactionPattern_Normal() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n:0}-{t:1} {t:0}"),
            new NamePattern("request"),
            new TransactionNamePattern("T.*bar-([0-9])"),
            new StopOnMatch(true)
        );
        var data = new RequestData("request");
        data.setTransactionName("TFoobar-2");

        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("request-2 TFoobar-2", data.getName());

        var data2 = new RequestData("request");
        data2.setTransactionName("TLateStuff");

        assertEquals(0, rule.process(data2));
        assertEquals("request", data2.getName());
    }

    @Test
    public void agentPattern_Normal() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n:0}-{a:1} {a:0}"),
            new NamePattern("request"),
            new AgentNamePattern("A.*bar-([0-9])"),
            new StopOnMatch(true)
        );
        var data = new RequestData("request");
        data.setAgentName("AFoobar-2");

        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("request-2 AFoobar-2", data.getName());

        var data2 = new RequestData("request");
        data2.setAgentName("TLateStuff");

        assertEquals(0, rule.process(data2));
        assertEquals("request", data2.getName());
    }

    @Test
    public void contentPattern_Normal() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n:0}-{c:1} {c:0}"),
            new NamePattern("request"),
            new ContentTypePattern("image/([a-z]{3,4})$"),
            new StopOnMatch(true)
        );
        var data = new RequestData("request");
        data.setContentType("image/jpeg");

        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("request-jpeg image/jpeg", data.getName());

        var data2 = new RequestData("request");
        data2.setContentType("image/coffeelatte");

        assertEquals(0, rule.process(data2));
        assertEquals("request", data2.getName());
    }

    @Test
    public void statusCodePattern_Normal() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n:0}-{s:1} {s:0}"),
            new NamePattern("request"),
            new StatusCodePattern("(30[12])"),
            new StopOnMatch(true)
        );
        var data = new RequestData("request");
        data.setResponseCode(302);

        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("request-302 302", data.getName());

        var data2 = new RequestData("request");
        data2.setResponseCode(304);

        assertEquals(0, rule.process(data2));
        assertEquals("request", data2.getName());
    }

    @Test
    public void urlPattern_Stop() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n:0}-{u:1} {u:0}"),
            new UrlPattern("https?://foobar.com/([^/]+)/"),
            new StopOnMatch(true)
        );
        var data = new RequestData("request");
        data.setUrl("https://foobar.com/tiger/");
        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("request-tiger https://foobar.com/tiger/", data.getName());

        var data2 = new RequestData("request");
        data2.setUrl("https://foobar.de/tiger/");
        assertEquals(0, rule.process(data2));
        assertEquals("request", data2.getName());
    }

    @Test
    public void urlPattern_NoStop() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n:0}-{u:1} {u:0}"),
            new UrlPattern("https?://foobar.com/([^/]+)/"),
            new StopOnMatch(false),
            new ContinueOnMatchAtId(10),
            new ContinueOnNoMatchAtId(20)
        );
        var data = new RequestData("request");
        data.setUrl("https://foobar.com/tiger/");
        assertEquals(10, rule.process(data));
        assertEquals("request-tiger https://foobar.com/tiger/", data.getName());

        var data2 = new RequestData("request");
        data2.setUrl("https://foobar.de/tiger/");
        assertEquals(20, rule.process(data2));
        assertEquals("request", data2.getName());
    }

    @Test
    public void urlPatternAndText_NoStop() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{u:1}"),
            new UrlPattern("https://foobar.com/(.*)/"),
            new StopOnMatch(false),
            new ContinueOnMatchAtId(10),
            new ContinueOnNoMatchAtId(20),
            new UrlText("foobar")
        );
        var data = new RequestData("request");
        data.setUrl("https://foobar.com/tiger/");
        assertEquals(10, rule.process(data));
        assertEquals("tiger", data.getName());

        var data2 = new RequestData("request");
        data2.setUrl("https://foobar.online/tiger/");
        assertEquals(20, rule.process(data2));
        assertEquals("request", data2.getName());
    }

    @Test
    public void urlPattern_Normal_Exclude_Text() throws Exception
    {
        var rule = getMergeRule(10,
            new NewName("{n}"),
            new NamePattern("request"),
            new StopOnMatch(true),
            new UrlExcludePattern("https://foobar.com/([^/]+)/"),
            new ContinueOnMatchAtId(20),
            new ContinueOnNoMatchAtId(30),
            new UrlText("foobar")
        );
        var data = new RequestData("request");
        data.setUrl("https://foobar.com/tiger/");
        assertEquals(30, rule.process(data));
        assertEquals("request", data.getName());

        var data2 = new RequestData("request");
        data2.setUrl("https://foobar.online/tiger/");
        assertEquals(MergeRule.STOP, rule.process(data2));
        assertEquals("request", data2.getName());

        var rule2 = getMergeRule(0,
            new NewName("{u:1}"),
            new NamePattern("request"),
            new UrlPattern("https://foobar.com/([^/]+)/"),
            new StopOnMatch(true),
            new UrlText("xmas")
        );
        var data3 = new RequestData("request");
        data3.setUrl("https://foobar.com/tiger/");
        assertEquals(0, rule2.process(data3));
        assertEquals("request", data3.getName());

        var data4 = new RequestData("request");
        data4.setUrl("https://myhost.com/tiger/");
        assertEquals(0, rule2.process(data4));
        assertEquals("request", data4.getName());
    }

    @Test
    public void urlText_Include_NoStop() throws Exception
    {
        var rule = getMergeRule(10,
            new NewName("a name"),
            new UrlText("foobar"),
            new StopOnMatch(false),
            new ContinueOnMatchAtId(20),
            new ContinueOnNoMatchAtId(30)
        );
        var data = new RequestData("original");
        data.setUrl("https://foobar.com/tiger/");
        assertEquals(20, rule.process(data));
        assertEquals("a name", data.getName());

        var data2 = new RequestData("original");
        data2.setUrl("https://yoga.com/tiger/");
        assertEquals(30, rule.process(data2));
        assertEquals("original", data2.getName());
    }

    @Test
    public void urlText_Include_Stop() throws Exception
    {
        var rule = getMergeRule(10,
            new NewName("new name"),
            new UrlText("foobar"),
            new StopOnMatch(true),
            new ContinueOnMatchAtId(20),
            new ContinueOnNoMatchAtId(30)
        );
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        var data = new RequestData("original");
        data.setUrl("https://foobar.com/tiger/");
        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("new name", data.getName());

        var data2 = new RequestData("original");
        data2.setUrl("https://yoga.com/tiger/");
        assertEquals(30, rule.process(data2));
        assertEquals("original", data2.getName());
    }

    @Test
    public void httpMethodPattern_Normal() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n:0} {m:1}"),
            new NamePattern("request"),
            new HttpMethodPattern("(GET)"),
            new StopOnMatch(true)
        );
        var data = new RequestData("request");
        data.setHttpMethod("GET");

        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("request GET", data.getName());

        var data2 = new RequestData("request");
        data2.setHttpMethod("PUT");

        assertEquals(0, rule.process(data2));
        assertEquals("request", data2.getName());
    }

    private void responseTimeRanges(final MergeRule rule, final int runtime, final String expected)
    {
        var data = new RequestData("request");
        data.setRunTime(runtime);
        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals(expected, data.getName());
    }

    @Test
    public void responseTimeRanges_Normal() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n:0} [{r}]"),
            new NamePattern("request"),
            new RunTimeRanges("100, 3000, 5000"),
            new StopOnMatch(true)
        );
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

    @Test
    public void complexRegExp() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n:1}-{u:1}-{c:1}-{s:1}-{a:1}-{t:2}{t:3}{t:1}-{m:1}-{r}"),
            new NamePattern("r(.*)"),
            new UrlPattern("([urlURL]{3})"),
            new ContentTypePattern("^(.+)$"),
            new StatusCodePattern("2([01])[0-9]"),
            new AgentNamePattern("(a?)g?e?n?t?"),
            new TransactionNamePattern("((.+)(action))$"),
            new HttpMethodPattern("^(.+)$"),
            new RunTimeRanges("1000, 2000"),
            new NameExcludePattern("not-request"),
            new UrlExcludePattern("not-url"),
            new ContentTypeExcludePattern("not-content"),
            new StatusCodeExcludePattern("404"),
            new AgentNameExcludePattern("not-agent"),
            new TransactionNameExcludePattern("not-transaction"),
            new HttpMethodExcludePattern("not-method"),
            new StopOnMatch(true)
        );
        var data = new RequestData("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("equest-url-content-0-a-transactiontransaction-method-0..999", data.getName());
    }

    @Test
    public void allIncludeAndExclude_MatchIncludeNotExclude_CaptureAll() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n:0}{u:0}{c:0}{s:0}{a:0}{t:0}-{m:0}-{r:0}"),
            new NamePattern("request"),
            new UrlPattern("url"),
            new ContentTypePattern("content"),
            new StatusCodePattern("200"),
            new AgentNamePattern("agent"),
            new TransactionNamePattern("transaction"),
            new HttpMethodPattern("method"),
            new RunTimeRanges("1000, 2000"),
            new NameExcludePattern("not-request"),
            new UrlExcludePattern("not-url"),
            new ContentTypeExcludePattern("not-content"),
            new StatusCodeExcludePattern("404"),
            new AgentNameExcludePattern("not-agent"),
            new TransactionNameExcludePattern("not-transaction"),
            new HttpMethodExcludePattern("not-method"),
            new StopOnMatch(true)
        );
        var data = new RequestData("request");
        data.setUrl("url");
        data.setContentType("content");
        data.setResponseCode(200);
        data.setAgentName("agent");
        data.setTransactionName("transaction");
        data.setHttpMethod("method");
        data.setRunTime(500);

        assertEquals(MergeRule.STOP, rule.process(data));
        assertEquals("requesturlcontent200agenttransaction-method-0..999", data.getName());
    }

    @Test
    public void allIncludeAndExclude_MatchIncludeExclude_CaptureAll() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n:0}{u:0}{c:0}{s:0}{a:0}{t:0}{m:0}{r:0}"),
            new NamePattern("request"),
            new UrlPattern("url"),
            new ContentTypePattern("content"),
            new StatusCodePattern("200"),
            new AgentNamePattern("agent"),
            new TransactionNamePattern("transaction"),
            new HttpMethodPattern("method"),
            new RunTimeRanges("1000, 2000"),
            new NameExcludePattern("request"),
            new UrlExcludePattern("url"),
            new ContentTypeExcludePattern("content"),
            new StatusCodeExcludePattern("200"),
            new AgentNameExcludePattern("agent"),
            new TransactionNameExcludePattern("transaction"),
            new HttpMethodExcludePattern("method"),
            new StopOnMatch(true)
        );
        var data = new RequestData("request");
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

    @Test
    public void allIncludeAndExclude_MatchNotIncludeButExclude_CaptureAll() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n:0}{u:0}{c:0}{s:0}{a:0}{t:0}{m:0}{r:0}"),
            new NamePattern("request"),
            new UrlPattern("url"),
            new ContentTypePattern("content"),
            new StatusCodePattern("200"),
            new AgentNamePattern("agent"),
            new TransactionNamePattern("transaction"),
            new HttpMethodPattern("method"),
            new RunTimeRanges("1000, 2000"),
            new NameExcludePattern("not-request"),
            new UrlExcludePattern("not-url"),
            new ContentTypeExcludePattern("not-content"),
            new StatusCodeExcludePattern("404"),
            new AgentNameExcludePattern("not-agent"),
            new TransactionNameExcludePattern("not-transaction"),
            new HttpMethodExcludePattern("not-method"),
            new StopOnMatch(true)
        );
        var data = new RequestData("not-request");
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
        var rule = getMergeRule(0,
            new NewName("{n:12}"),
            new NamePattern("(1)(2)(3)(4)(5)(6)(7)(8)(9)(10)(11)(12)"),
            new StopOnMatch(false)
        );
        var data = new RequestData("123456789101112");

        assertEquals(0, rule.process(data));
        assertEquals("12", data.getName());
    }

    @Test
    public void includeDataWithouPositionNumber() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("{n}-{u}-{c}-{s}-{a}-{t}-{r}"),
            new NamePattern("request"),
            new UrlPattern("url"),
            new ContentTypePattern("content"),
            new StatusCodePattern("200"),
            new AgentNamePattern("agent"),
            new TransactionNamePattern("transaction"),
            new HttpMethodPattern("GET"),
            new RunTimeRanges("300, 2000"),
            new StopOnMatch(false)
        );
        var data = new RequestData("request");
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

    @Test(expected = InvalidMergeRuleException.class)
    public void invalidRegex() throws Exception
    {
        getMergeRule(0,
            new NewName("{n:1}"),
            new NamePattern("([]-]"),
            new StopOnMatch(true)
        );
    }

    @Test
    public void stopAndDrop() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName(""),
            new DropOnMatch(true),
            new StopOnMatch(true)
        );
        var data = new RequestData("Old");

        assertEquals(MergeRule.DROP, rule.process(data));
    }

    @Test
    public void canGiveNameDespiteDrop() throws Exception
    {
        var rule = getMergeRule(0,
            new NewName("My new name"),
            new UrlPattern("url"),
            new DropOnMatch(true),
            new StopOnMatch(false)
        );
        var data = new RequestData("Old");
        data.setUrl("url");

        assertEquals(MergeRule.DROP, rule.process(data));
    }

    @Test
    public void fullTextPlaceholders_emptyFilterPatterns() throws Exception
    {
        var rule = getMergeRule(1,
            new NewName("{n} {u} {c} {s} {a} {t} {m} {r}"),
            new StopOnMatch(false),
            new ContinueOnMatchAtId(1),
            new ContinueOnNoMatchAtId(1)
        );
        var data = new RequestData("name");
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
        var rule = getMergeRule(1,
            new NewName("{n} {u} {c} {s} {a} {t} {m} {r}"),
            new NamePattern("na"),
            new UrlPattern("ur"),
            new ContentTypePattern("co"),
            new StatusCodePattern("20"),
            new AgentNamePattern("ag"),
            new TransactionNamePattern("tr"),
            new HttpMethodPattern("me"),
            new StopOnMatch(false),
            new ContinueOnMatchAtId(1),
            new ContinueOnNoMatchAtId(2)
        );
        var data = new RequestData("name");
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

    @Test(expected = InvalidMergeRuleException.class)
    public void validate_emptyFilterPatterns_invalidCapturingGroupIndex_1() throws Exception
    {
        getMergeRule(1,
            new NewName("{n:1}"),
            new StopOnMatch(false),
            new ContinueOnMatchAtId(1),
            new ContinueOnNoMatchAtId(2)
        );
    }

    @Test
    public void validate_nonEmptyFilterPatterns_validCapturingGroupIndexes() throws Exception
    {
        getMergeRule(1, new NewName("{n}"), new NamePattern("foo(bar)"), new StopOnMatch(false), new ContinueOnMatchAtId(1), new ContinueOnNoMatchAtId(2));
        getMergeRule(1, new NewName("{n:0}"), new NamePattern("foo(bar)"), new StopOnMatch(false), new ContinueOnMatchAtId(1), new ContinueOnNoMatchAtId(2));
        getMergeRule(1, new NewName("{n:1}"), new NamePattern("foo(bar)"), new StopOnMatch(false), new ContinueOnMatchAtId(1), new ContinueOnNoMatchAtId(2));
    }

    @Test(expected = InvalidMergeRuleException.class)
    public void validate_nonEmptyFilterPatterns_invalidCapturingGroupIndex() throws Exception
    {
        getMergeRule(1, new NewName("{n:2}"), new NamePattern("foo(bar)"), new StopOnMatch(false), new ContinueOnMatchAtId(1), new ContinueOnNoMatchAtId(2));
    }

    @Test
    public void invalidContinueOnMatch() throws Exception
    {
        var ex = assertThrows(InvalidMergeRuleException.class, () -> {
            getMergeRule(42,
                new NewName("fooBar"),
                new NamePattern("request"),
                new StopOnMatch(false),
                new ContinueOnMatchAtId(9),
                new ContinueOnNoMatchAtId(42)
            );
        });
        assertEquals("Continue on match rule ID (9) must be greater or same than the rule ID (42)", ex.getMessage());
    }

    @Test
    public void invalidContinueOnNoMatch() throws Exception
    {
        var ex = assertThrows(InvalidMergeRuleException.class, () -> {
            getMergeRule(42,
                new NewName("fooBar"),
                new NamePattern("request"),
                new StopOnMatch(false),
                new ContinueOnMatchAtId(42),
                new ContinueOnNoMatchAtId(4)
            );
        });
        assertEquals("Continue on no match rule ID (4) must be greater or same than the rule ID (42)", ex.getMessage());
    }

    @Test
    public void cannotParseGroupIndex() throws Exception
    {
        var ex = assertThrows(InvalidMergeRuleException.class, () -> {
            getMergeRule(42, new NewName("{n:a}"), new NamePattern("request"), new StopOnMatch(false), new ContinueOnMatchAtId(42), new ContinueOnNoMatchAtId(42));
        });
        assertEquals("Failed to parse the matching group index 'a' as integer", ex.getMessage());
    }
}


