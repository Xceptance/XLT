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
package com.xceptance.xlt.api.actions;

import java.io.IOException;
import java.net.URL;

import org.htmlunit.StringWebResponse;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.xceptance.xlt.TestWrapper;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.engine.LightWeightPageImpl;
import com.xceptance.xlt.engine.XltWebClient;

/**
 * Test the implementation of {@link AbstractLightWeightPageAction}
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class AbstractLightWeightPageActionTest
{
    /**
     * Action object used for tests.
     */
    private TestLightWeightPageAction action;

    /**
     * Test initialization.
     */
    @Before
    public void intro() throws Throwable
    {
        action = new TestLightWeightPageAction(new StringWebResponse("", new URL("http://example.net")));
    }

    /**
     * Tests {@link AbstractLightWeightPageAction#getLightWeightPage()}.
     */
    @Test
    public void testGetLightWeightPage() throws Throwable
    {
        Assert.assertNull(action.getLightWeightPage());
        action.getPage("http://localhost");
        Assert.assertNotNull(action.getLightWeightPage());
        Assert.assertEquals("http://localhost/", action.getURL().toString());
    }

    /**
     * Tests {@link AbstractLightWeightPageAction#getPreviousAction()}.
     */
    @Test
    public void testGetPreviousAction() throws Throwable
    {
        Assert.assertNull(action.getPreviousAction());
        Assert.assertEquals(action, new TestLightWeightPageAction(action).getPreviousAction());
    }

    /**
     * Tests {@link AbstractLightWeightPageAction#getContent()}.
     */
    @Test
    public void testGetContent() throws Throwable
    {
        Assert.assertNull(action.getContent());
        action.response = new StringWebResponse("Test 123", new URL("http://localhost"));
        action.getPage("http://localhost");

        final String content = action.getContent();
        Assert.assertNotNull(content);
        Assert.assertEquals("Test 123", content);
    }

    /**
     * Tests {@link AbstractLightWeightPageAction#getHttpResponseCode()}.
     */
    @Test
    public void testGetHttpResponseCode() throws Throwable
    {
        Assert.assertEquals(0, action.getHttpResponseCode());

        action.response = new StringWebResponse("Test 123", new URL("http://localhost"));
        action.getPage("http://localhost");

        Assert.assertEquals(200, action.getHttpResponseCode());
    }

    @Test
    public void testTimerNameConstructor()
    {
        final String timerName = "myTimer";
        final TestLightWeightPageAction current = new TestLightWeightPageAction(timerName);
        Assert.assertEquals("Timer name mismatch. The constructor got somehow damaged!", timerName, current.getTimerName());
        current.closeWebClient();
    }

    @Test
    public void testRun()
    {
        new TestWrapper(InterruptedException.class, "Actually, I wasn't interrupted!")
        {
            @Override
            protected void run() throws Throwable
            {
                final AnotherTestLightWeightPageAction action = new AnotherTestLightWeightPageAction("Huhu");
                action.run();
            }
        }.execute();
    }

    /**
     * Dummy implementation of {@link AbstractLightWeightPageAction} to be able to instantiate it.
     */
    private class TestLightWeightPageAction extends AbstractLightWeightPageAction
    {

        private WebResponse response;

        protected TestLightWeightPageAction(final String timerName)
        {
            super(timerName);
        }

        protected TestLightWeightPageAction(final WebResponse response)
        {
            this((AbstractLightWeightPageAction) null);
            this.response = response;
        }

        protected TestLightWeightPageAction(final AbstractLightWeightPageAction prevAction)
        {
            super(prevAction, TestLightWeightPageAction.class.getName());
        }

        @Override
        protected void execute() throws Exception
        {
        }

        @Override
        protected void postValidate() throws Exception
        {
        }

        @Override
        public void preValidate() throws Exception
        {
        }

        public void getPage(final String url) throws Exception
        {
            this.loadPage(url);
        }

        @Override
        public WebClient getWebClient()
        {
            try
            {
                final XltWebClient wc = Mockito.mock(XltWebClient.class);
                final LightWeightPage page = new LightWeightPageImpl(response, getTimerName(), wc);

                Mockito.when(wc.getLightWeightPage((WebRequest) ArgumentMatchers.any())).thenReturn(page);

                return wc;
            }
            catch (final IOException ioe)
            {
                return null;
            }
        }
    }

    private class AnotherTestLightWeightPageAction extends AbstractLightWeightPageAction
    {
        /**
         * @param prevAction
         */
        protected AnotherTestLightWeightPageAction(final String timerName)
        {
            super(timerName);
        }

        @Override
        protected void executeThinkTime() throws InterruptedException
        {
            throw new InterruptedException("Actually, I wasn't interrupted!");
        }

        @Override
        public void preValidate() throws Exception
        {
        }

        @Override
        protected void execute() throws Exception
        {
        }

        @Override
        protected void postValidate() throws Exception
        {
        }
    }
}
