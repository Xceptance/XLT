/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import com.xceptance.xlt.AbstractXLTTestCase;
import com.xceptance.xlt.engine.XltWebClient;

/**
 * Test the implementation of {@link AbstractXmlPageAction}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class AbstractXmlPageActionTest extends AbstractXLTTestCase
{

    /** AbstractXmlPageAction test instance. */
    protected TestAction instance = null;

    /**
     * Mocked AbstractWebAction instance to be able to inject XltWebClient mock.
     */
    protected AbstractWebAction mock = null;

    /**
     * Test fixture setup.
     * 
     * @throws Exception
     *             thrown when setup failed.
     */
    @Before
    public void intro() throws Exception
    {
        // mock the webclient to be able to inject cutom pages
        final XltWebClient wc = Mockito.mock(XltWebClient.class);

        // create mocked web action and let it return the webclient mock
        mock = Mockito.mock(AbstractWebAction.class);
        Mockito.doReturn(wc).when(mock).getWebClient();

        // create test instance
        instance = new TestAction(mock);
    }

    /**
     * Tests the implementation of {@link AbstractXmlPageAction#getXmlPage()} by using a trivial Page instance as
     * webclient's response.
     */
    @Test
    public void testGetPage_InvalidPage()
    {
        // create trivial (untyped) page
        final Page dummyPage = Mockito.mock(Page.class);

        // get webclient mock
        final XltWebClient wc = (XltWebClient) mock.getWebClient();
        try
        {
            // let it return the mocked page
            Mockito.doReturn(dummyPage).when(wc).getPage((URL) Matchers.anyObject());
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }

        // URL to load
        URL url = null;
        try
        {
            // construct simple URL
            url = new URL("http://localhost");
        }
        catch (final MalformedURLException mue)
        {
            failOnUnexpected(mue);
        }

        // error message to look for
        final String errMsg = "The page type of '" + url + "' is not XML as expected.";
        try
        {
            // run the test instance
            instance.run();
            // fail, cause page is not XML
            Assert.fail("AbstractXmlPageAction.loadXmlPage(URL) must throw an " + "UnexpectedPageType since result page isn't an "
                        + "instance of XmlPage.");
        }
        catch (final UnexpectedPageTypeException upt)
        {
            // make sure, error message can be found
            Assert.assertTrue(upt.getMessage().contains(errMsg));
        }
        catch (final Throwable t)
        {
            // unknown error -> fail
            failOnUnexpected(t);
        }
    }

    /**
     * Makes just a few simple calls to avoid the need for further checking the code coverage in detail.
     */
    @Test
    public void testSimpleCalls()
    {
        final AbstractWebAction previous = new MyAbstractXmlPageAction();
        final AbstractXmlPageAction parent = new MyAbstractXmlPageAction(previous);
        Assert.assertEquals("Wrong previous action!", previous, parent.getPreviousAction());
    }

    /**
     * Tests the implementation of {@link AbstractXmlPageAction#getXmlPage()} by using a XmlPage instance as webclient's
     * response.
     */
    @Test
    public void testGetPage_ValidPage()
    {

        // create XML page
        final XmlPage page = Mockito.mock(XmlPage.class);

        // get webclient mock
        final XltWebClient wc = (XltWebClient) mock.getWebClient();
        try
        {
            // let it return the XML page
            Mockito.doReturn(page).when(wc).getPage((URL) Matchers.anyObject());
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }

        try
        {
            // run the test instance
            instance.run();
        }
        catch (final Throwable t)
        {
            // fail
            failOnUnexpected(t);
        }
    }

    /**
     * Tests the implementation of {@link AbstractXmlPageAction#validateHttpResponseCode(int)}.
     */
    @Test
    public void testValidateResponse()
    {
        // create XML page
        final XmlPage dummyPage = mock(XmlPage.class);
        // create web response object
        final WebResponse dummyResponse = mock(WebResponse.class);
        // let web response return the status code '200'
        Mockito.doReturn(200).when(dummyResponse).getStatusCode();
        // let XML page return the mocked web response
        Mockito.doReturn(dummyResponse).when(dummyPage).getWebResponse();

        // get webclient mock
        final XltWebClient wc = (XltWebClient) mock.getWebClient();
        try
        {
            // let it return the XML page
            Mockito.doReturn(dummyPage).when(wc).getPage((URL) Matchers.anyObject());
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }

        // create new test instance by overriding 'postValidate'.
        TestAction action = new TestAction(mock)
        {
            @Override
            public void postValidate() throws Exception
            {
                validateHttpResponseCode(201);
            }
        };

        try
        {
            // run the action
            action.run();
            // fail, cause status code is 200 and not 201
            Assert.fail("AbstractXmlPageAction.validateHttpResponseCode(int) " + "must throw an AssertionError since validation fails.");
        }
        catch (final Throwable t)
        {
            // ignore
        }

        // create new test instance by overriding 'postValidate'
        action = new TestAction(mock)
        {
            @Override
            public void postValidate() throws Exception
            {
                validateHttpResponseCode(200);
            }
        };

        try
        {
            // run the action
            action.run();
        }
        // fail
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }
    }

    /**
     * Private helper class which extends {@link AbstractXmlPageAction} to be able to instantiate the test instance.
     * 
     * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
     */
    private class TestAction extends AbstractXmlPageAction
    {
        /** URL to load on execute(). */
        private URL url = null;

        /**
         * Default constructor.
         * 
         * @param prevAction
         *            Previous action.
         */
        TestAction(final AbstractWebAction prevAction)
        {
            super(prevAction, "testAction");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void execute() throws Exception
        {
            loadXMLPage(url);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void postValidate() throws Exception
        {
            // ignore
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void preValidate() throws Exception
        {
            url = new URL("http://localhost");

        }
    }

    private static class MyAbstractXmlPageAction extends AbstractXmlPageAction
    {
        private MyAbstractXmlPageAction()
        {
            super("MyTimer");
        }

        /**
         * @param previous
         */
        public MyAbstractXmlPageAction(final AbstractWebAction previous)
        {
            super(previous, null);
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
