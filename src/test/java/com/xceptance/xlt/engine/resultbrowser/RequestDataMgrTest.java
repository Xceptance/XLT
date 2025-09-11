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
package com.xceptance.xlt.engine.resultbrowser;

import java.net.URL;
import java.util.List;

import org.htmlunit.StringWebResponse;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.AbstractXLTTestCase;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.common.XltConstants;

/**
 * Tests the implementation of {@link RequestDataMgr}.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class RequestDataMgrTest extends AbstractXLTTestCase
{
    /**
     * Test instance.
     */
    private RequestDataMgr instance;

    /**
     * Test fixture setup.
     */
    @Before
    public void intro()
    {
        instance = new RequestDataMgr();
    }

    /**
     * Tests the implementation of {@link RequestDataMgr#generateTransaction()} for the case that there are no pending
     * requests.
     */
    @Test
    public void testGenerateTransaction_NoPendingRequests() throws Throwable
    {
        final Object transaction = instance.generateTransaction();
        Assert.assertNotNull(transaction);

        final List<?> actions = (List<?>) getField("actions", transaction);
        Assert.assertNotNull(actions);
        Assert.assertTrue(actions.isEmpty());
    }

    /**
     * Tests the implementation of {@link RequestDataMgr#generateTransaction()} for
     *
     * @throws Throwable
     */
    @Test
    public void testGenerateTransaction_PendingRequest() throws Throwable
    {
        final URL url = new URL("http://localhost/");
        final WebResponse response = new StringWebResponse(XltConstants.EMPTYSTRING, url);
        final WebRequest settings = new WebRequest(url);

        instance.requestDumped("AnyFile", new Request("AnyName", settings, response, new RequestData()));

        final Object transaction = instance.generateTransaction();
        Assert.assertNotNull(transaction);

        final List<?> actions = (List<?>) getField("actions", transaction);
        Assert.assertNotNull(actions);
        Assert.assertEquals(1, actions.size());

        final Object action = actions.get(0);
        Assert.assertNotNull(action);

        Assert.assertEquals("n/a", getField("name", action));

        final List<?> requests = (List<?>) getField("requests", action);
        Assert.assertNotNull(requests);
        Assert.assertEquals(1, requests.size());

        final Object request = requests.get(0);
        Assert.assertEquals("/", getField("name", request));
        Assert.assertEquals(url.toString(), getField("url", request));
    }

    /**
     * Tests the implementation of {@link RequestDataMgr#requestDumped(String, Request)}.
     */
    @Test
    public void testRequestDumped() throws Throwable
    {
        final URL url = new URL("http://localhost/");
        final WebResponse response = new StringWebResponse(XltConstants.EMPTYSTRING, url);
        final WebRequest settings = new WebRequest(url);

        instance.requestDumped("AnyFile", new Request("AnyName", settings, response, new RequestData()));

        final List<?> requests = (List<?>) getField("pendingRequests", instance);
        Assert.assertNotNull(requests);
        Assert.assertEquals(1, requests.size());

        final Object request = requests.get(0);
        Assert.assertEquals("/", getField("name", request));
        Assert.assertEquals(url.toString(), getField("url", request));
    }

    /**
     * Tests the implementation of {@link RequestDataMgr#pageDumped(String, Page)} for the case that there are no
     * pending requests.
     */
    @Test
    public void testPageDumped_NoPendingRequests() throws Throwable
    {
        final WebResponse response = new StringWebResponse(XltConstants.EMPTYSTRING, new URL("http://localhost"));
        final LightWeightPage lwPage = new LightWeightPage(response, "LightWeightPage");
        final Page page = new Page(lwPage.getTimerName(), lwPage);
        final String fileName = lwPage.getTimerName() + ".html";

        instance.pageDumped(fileName, page);

        final List<?> actions = (List<?>) getField("actions", instance);
        Assert.assertNotNull(actions);
        Assert.assertEquals(1, actions.size());

        final Object action = actions.get(0);
        Assert.assertNotNull(action);
        Assert.assertEquals(lwPage.getTimerName(), getField("name", action));
        Assert.assertEquals(XltConstants.DUMP_PAGES_DIR + "/" + fileName, getField("fileName", action));

        final List<?> requests = (List<?>) getField("requests", action);
        Assert.assertNotNull(requests);
        Assert.assertTrue(requests.isEmpty());
    }

    /**
     * Tests the implementation of {@link RequestDataMgr#pageDumped(String, Page)} for the case that there is one
     * pending request.
     */
    @Test
    public void testPageDumped_PendingRequest() throws Throwable
    {
        final URL url = new URL("http://localhost/");
        final WebResponse response = new StringWebResponse(XltConstants.EMPTYSTRING, url);
        final WebRequest settings = new WebRequest(url);

        instance.requestDumped("AnyFile", new Request("AnyName", settings, response, new RequestData()));

        final LightWeightPage lwPage = new LightWeightPage(response, "LightWeightPage");
        final Page page = new Page(lwPage.getTimerName(), lwPage);
        final String fileName = lwPage.getTimerName() + ".html";

        instance.pageDumped(fileName, page);

        final List<?> actions = (List<?>) getField("actions", instance);
        Assert.assertNotNull(actions);
        Assert.assertEquals(1, actions.size());

        final Object action = actions.get(0);
        Assert.assertNotNull(action);
        Assert.assertEquals(lwPage.getTimerName(), getField("name", action));
        Assert.assertEquals(XltConstants.DUMP_PAGES_DIR + "/" + fileName, getField("fileName", action));

        final List<?> requests = (List<?>) getField("requests", action);
        Assert.assertNotNull(requests);
        Assert.assertEquals(1, requests.size());

        final Object request = requests.get(0);
        Assert.assertNotNull(request);
        Assert.assertEquals("/", getField("name", request));
        Assert.assertEquals(url.toString(), getField("url", request));
    }
}
