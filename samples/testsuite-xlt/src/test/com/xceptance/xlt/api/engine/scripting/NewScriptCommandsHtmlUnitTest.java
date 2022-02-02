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
package test.com.xceptance.xlt.api.engine.scripting;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;
import com.xceptance.xlt.engine.scripting.TestContext;

import action.modules.StartAppear;
import action.modules.StartDisappear;

/**
 * Test implementation of new methods introduced to Scripting API as part of improvement #3007.
 */
public class NewScriptCommandsHtmlUnitTest extends AbstractHtmlUnitScriptTestCase
{

    public NewScriptCommandsHtmlUnitTest()
    {
        super("http://localhost:8080");
    }

    @Before
    public void setup()
    {
        TestContext.getCurrent().setTimeout(3000L);
    }

    @Test
    public void testAttribute() throws Throwable
    {
        AbstractHtmlPageAction action = null;
        action = new action.modules.Open_ExamplePage().run(action);

        action = new AHUAxn(action, "assertAttribute")
        {

            @Override
            protected void execute() throws Exception
            {
                assertAttribute("xpath=id('ws8_a')/input[1]@value", "foobar");
                assertAttribute("xpath=id('ws8_a')/input[1]", "value", "foobar");

                shouldFail(() -> assertAttribute("xpath=id('doesnotexist')", "foo", "sometext"));
                shouldFail(() -> assertAttribute("in_txt_1", null, "sometext"));
                shouldFail(() -> assertAttribute("in_txt_1", "", "sometext"));
                shouldFail(() -> assertAttribute("in_txt_1", "  ", "sometext"));
            }

        };
        action.run();

        action = new AHUAxn(action, "assertNotAttribute")
        {
            @Override
            protected void execute() throws Exception
            {
                assertNotAttribute("xpath=id('ws8_a')/input[1]@value", "foo");
                assertNotAttribute("xpath=id('ws8_a')/input[1]", "value", "foo");

                shouldFail(() -> assertNotAttribute("xpath=id('doesnotexist')", "foo", "sometext"));
                shouldFail(() -> assertNotAttribute("in_txt_1", null, "sometext"));
                shouldFail(() -> assertNotAttribute("in_txt_1", "", "sometext"));
                shouldFail(() -> assertNotAttribute("in_txt_1", "  ", "sometext"));

            };
        };
        action.run();

        action = new AHUAxn(action, "storeAttribute")
        {
            @Override
            protected void execute() throws Exception
            {
                storeAttribute("xpath=id('ws8_a')/input[1]", "value", "att");

                assertAttribute("xpath=id('ws8_a')/input[1]@value", "exact:${att}");

                shouldFail(() -> storeAttribute("xpath=id('doesnotexist')", "foo", "att"));
                shouldFail(() -> storeAttribute("in_txt_1", null, "att"));
                shouldFail(() -> storeAttribute("in_txt_1", "", "att"));
                shouldFail(() -> storeAttribute("in_txt_1", "  ", "att"));

            }
        };
        action.run();

        action = new AHUAxn(action, "waitForAttribute")
        {
            @Override
            protected void execute() throws Exception
            {
                HtmlPage page = getPreviousAction().getHtmlPage();
                page = new StartAppear("100").run(page);

                page = waitForAttribute("xpath=id('appear_9')@name", "text");
                page = waitForAttribute("xpath=id('appear_9')", "name", "text");

                shouldFail(() -> waitForAttribute("xpath=id('doesnotexist')", "foo", "att"));
                shouldFail(() -> waitForAttribute("in_txt_1", null, "att"));
                shouldFail(() -> waitForAttribute("in_txt_1", "", "att"));
                shouldFail(() -> waitForAttribute("in_txt_1", "  ", "att"));

                setHtmlPage(page);
            }
        };
        action.run();

        action = new AHUAxn(action, "waitForNotAttribute")
        {
            @Override
            protected void execute() throws Exception
            {
                HtmlPage page = getPreviousAction().getHtmlPage();

                page = new StartDisappear("100").run(page);
                page = waitForNotAttribute("xpath=id('disapp_10')@name", "disapp_10");
                page = waitForNotAttribute("xpath=id('disapp_10')", "name", "disapp_10");

                shouldFail(() -> waitForNotAttribute("xpath=id('doesnotexist')", "foo", "att"));
                shouldFail(() -> waitForNotAttribute("in_txt_1", null, "att"));
                shouldFail(() -> waitForNotAttribute("in_txt_1", "", "att"));
                shouldFail(() -> waitForNotAttribute("in_txt_1", "  ", "att"));

                setHtmlPage(page);
            }
        };
        action.run();

    }

    private abstract static class AHUAxn extends AbstractHtmlUnitScriptAction
    {
        /**
         * @param prevAction
         * @param timerName
         */
        public AHUAxn(AbstractHtmlPageAction prevAction, String timerName)
        {
            super(prevAction, timerName);

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void preValidate() throws Exception
        {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void postValidate() throws Exception
        {
        }

        protected void shouldFail(final Runnable r)
        {
            try
            {
                r.run();
                Assert.fail("Exception expected but none was thrown!");
            }
            catch (final Exception xe)
            {
                // Expected
            }
        }
    }
}
