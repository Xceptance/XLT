/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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

import java.net.URL;

import org.htmlunit.MockWebConnection;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSelect;
import org.junit.Test;

import com.xceptance.xlt.AbstractXLTTestCase;
import com.xceptance.xlt.api.util.XltException;

/**
 * Test the implementation of {@link AbstractHtmlPageAction}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class AbstractHtmlPageActionTest extends AbstractXLTTestCase
{
    @Test
    public void testLoadPage() throws Throwable
    {
        final URL url = new URL("http://localhost/?test=&foo=2");
        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(url, "");

        final AbstractHtmlPageAction action = new TestAction(null, "TestLoadPage")
        {

            @Override
            protected void execute() throws Exception
            {
                getWebClient().setWebConnection(conn);
                loadPage("http://localhost?test=&amp;foo=2");
            }
        };
        action.run();
    }

    @Test(expected=XltException.class)
    public void testLoadPage_NoHTMLResponse() throws Throwable
    {
        final URL url = new URL("http://localhost/?test=&foo=2");
        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(url, "Content of /etc/passwd:... Gotcha!!", "text/plain");

        final AbstractHtmlPageAction action = new TestAction(null, "TestLoadPage")
        {

            @Override
            protected void execute() throws Exception
            {
                getWebClient().setWebConnection(conn);
                loadPage(url);
            }
        };
        action.run();
    }

    @Test
    public void testLoadPageByClick() throws Throwable
    {
        final URL test1 = new URL("http://localhost/test1");
        final URL test2 = new URL("http://localhost/test2");

        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(test1, "<html><head><title>Foobar</title></head><body><a id='link' href='test2'>The link</a></body></html>");
        conn.setResponse(test2, "");

        final AbstractHtmlPageAction action1 = new TestAction(null, "ClickTest1")
        {
            @Override
            protected void execute() throws Exception
            {
                getWebClient().setWebConnection(conn);
                loadPage(test1);
            }
        };
        action1.run();

        final AbstractHtmlPageAction action2 = new TestAction(action1, "ClickTest2")
        {
            @Override
            protected void execute() throws Exception
            {
                getWebClient().setWebConnection(conn);
                loadPageByClick((HtmlElement) getPreviousAction().getHtmlPage().getElementById("link"));
            }
        };
        action2.run();
    }

    @Test
    public void testLoadPageByDragNDrop() throws Throwable
    {
        final URL test1 = new URL("http://localhost/test1");
        final URL test2 = new URL("http://localhost/test2");

        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(test1,
                         "<html><head><title>Foobar</title></head><body><div id='draggable'>The draggable</div><div id='target' onmouseup=\"location.href='http://localhost/test2'\">The target</div></body></html>");
        conn.setResponse(test2, "");

        final AbstractHtmlPageAction action1 = new TestAction(null, "DnDTest1")
        {
            @Override
            protected void execute() throws Exception
            {
                getWebClient().setWebConnection(conn);
                loadPage(test1);
            }
        };
        action1.run();

        final AbstractHtmlPageAction action2 = new TestAction(action1, "DnDTest2")
        {
            @Override
            protected void execute() throws Exception
            {
                getWebClient().setWebConnection(conn);
                getWebClient().getOptions().setJavaScriptEnabled(true);
                final HtmlPage page = getPreviousAction().getHtmlPage();

                loadPageByDragAndDrop((HtmlElement) page.getElementById("draggable"), (HtmlElement) page.getElementById("target"));
            }
        };
        action2.run();
    }

    @Test
    public void testLoadPageByFormClick() throws Throwable
    {
        final URL test1 = new URL("http://localhost/test1");
        final URL test2 = new URL("http://localhost/test2?name=secret&name=testtext");

        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(test1,
                         "<html><head><title>Foobar</title></head><body><form id='myForm' action='/test2'><input name='name' type='hidden' value='secret' /><input name='name' type='submit' value='testtext'/></form></body></html>");

        conn.setResponse(test2, "");

        final AbstractHtmlPageAction action1 = new TestAction(null, "FormClickTest1")
        {
            @Override
            protected void execute() throws Exception
            {
                getWebClient().setWebConnection(conn);
                loadPage(test1);
            }
        };
        action1.run();

        final AbstractHtmlPageAction action2 = new TestAction(action1, "FormClickTest2")
        {
            @Override
            protected void execute() throws Exception
            {
                getWebClient().setWebConnection(conn);
                final HtmlPage page = getPreviousAction().getHtmlPage();

                loadPageByFormClick((HtmlForm) page.getElementById("myForm"), "name");
            }
        };
        action2.run();
    }

    @Test
    public void testLoadPageByFormSubmit() throws Throwable
    {
        final URL test1 = new URL("http://localhost/test1");
        final URL test2 = new URL("http://localhost/test2?name=John&lastname=Doe");

        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(test1,
                         "<html><head><title>Foobar</title></head><body><form id='myForm' action='/test2'><input name='name' type='text' value='John' /><input name='lastname' type='text' value='Doe'/></form></body></html>");

        conn.setResponse(test2, "");

        final AbstractHtmlPageAction action1 = new TestAction(null, "FormSubmitTest1")
        {
            @Override
            protected void execute() throws Exception
            {
                getWebClient().setWebConnection(conn);
                loadPage(test1);
            }
        };
        action1.run();

        final AbstractHtmlPageAction action2 = new TestAction(action1, "FormSubmitTest2")
        {
            @Override
            protected void execute() throws Exception
            {
                getWebClient().setWebConnection(conn);
                final HtmlPage page = getPreviousAction().getHtmlPage();

                loadPageByFormSubmit((HtmlForm) page.getElementById("myForm"));
            }
        };
        action2.run();
    }

    @Test
    public void testLoadBySelect() throws Throwable
    {
        final URL test1 = new URL("http://localhost/test1");
        final URL test2 = new URL("http://localhost/test2");

        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(test1,
                         "<html><head><title>Foobar</title></head><body><form name='myForm' action=''><select name='mySelect' onchange='location.href=options[selectedIndex].value'><option label='option1' value='someValue'/> <option label='option2' value='http://localhost/test2' /></form></body></html>");

        conn.setResponse(test2, "");

        final AbstractHtmlPageAction action1 = new TestAction(null, "SelectTest1")
        {
            @Override
            protected void execute() throws Exception
            {
                getWebClient().setWebConnection(conn);
                loadPage(test1);
            }
        };
        action1.run();

        final AbstractHtmlPageAction action2 = new TestAction(action1, "SelectTest2")
        {
            @Override
            protected void execute() throws Exception
            {
                getWebClient().setWebConnection(conn);
                getWebClient().getOptions().setJavaScriptEnabled(true);
                final HtmlPage page = getPreviousAction().getHtmlPage();

                final HtmlSelect select = page.getFormByName("myForm").getSelectByName("mySelect");
                loadPageBySelect(select, select.getOptions().get(1));
            }
        };
        action2.run();
    }

    @Test
    public void testLoadPageByTypeKeys() throws Throwable
    {
        final URL test1 = new URL("http://localhost/test1");
        final URL test2 = new URL("http://localhost/test2?name=Bob");

        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(test1,
                         "<html><head><title>Foobar</title></head><body><form name='myForm' action='/test2'><input type='text' value='' name='name' /></form></body></html>");

        conn.setResponse(test2, "");

        final AbstractHtmlPageAction action1 = new TestAction(null, "TypeTest1")
        {
            @Override
            protected void execute() throws Exception
            {
                getWebClient().setWebConnection(conn);
                loadPage(test1);
            }
        };
        action1.run();

        final AbstractHtmlPageAction action2 = new TestAction(action1, "TypeTest2")
        {
            @Override
            protected void execute() throws Exception
            {
                getWebClient().setWebConnection(conn);
                getWebClient().getOptions().setJavaScriptEnabled(true);
                final HtmlPage page = getPreviousAction().getHtmlPage();

                loadPageByTypingKeys(page.getFormByName("myForm").getInputByName("name"), "Bob\n");
            }
        };
        action2.run();
    }

    private abstract class TestAction extends AbstractHtmlPageAction
    {

        /**
         * @param previousAction
         * @param timerName
         */
        protected TestAction(final AbstractHtmlPageAction previousAction, final String timerName)
        {
            super(previousAction, timerName);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void preValidate() throws Exception
        {
        }

        @Override
        public void postValidate() throws Exception
        {
        }
    }
}
