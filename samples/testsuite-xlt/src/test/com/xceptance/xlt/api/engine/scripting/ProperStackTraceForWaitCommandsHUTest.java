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
package test.com.xceptance.xlt.api.engine.scripting;

import java.util.Arrays;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import org.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

/**
 * Simple test to ensure that stack traces of '*AndWait' commands are helpful (originate from the main thread). See
 * improvement #3089 for details.
 */
public class ProperStackTraceForWaitCommandsHUTest extends AbstractHtmlUnitScriptTestCase
{

    public ProperStackTraceForWaitCommandsHUTest()
    {
        super("http://localhost:8080");
    }

    @Test
    public void ensureStackTraceIsHelpful() throws Throwable
    {
        AbstractHtmlPageAction action = new action.modules.Open_ExamplePage().run(null);

        new HUSA(action).run();
    }

    private static String rndString()
    {
        return RandomStringUtils.randomAlphanumeric(5, 10);
    }

    private static boolean traceIncludes(final Throwable t, final String className, final String methodName)
    {
        t.printStackTrace();
        return Arrays.stream(t.getStackTrace()).anyMatch((e) -> className.equals(e.getClassName()) && methodName.equals(e.getMethodName()));
    }

    private static class HUSA extends AbstractHtmlUnitScriptAction
    {
        public HUSA(final AbstractHtmlPageAction prevAction)
        {
            super(prevAction, "DummyAction");
        }

        @Override
        protected void postValidate() throws Exception
        {
        }

        @Override
        public void preValidate() throws Exception
        {
        }

        protected void execute() throws Exception
        {
            final String prefix = "id=_rnd_";
            shouldFail(() -> clickAndWait(prefix + rndString()));
            shouldFail(() -> uncheckAndWait(prefix + rndString()));
            shouldFail(() -> selectAndWait(prefix + rndString(), rndString()));
            shouldFail(() -> doubleClickAndWait(prefix + rndString()));
            shouldFail(() -> checkAndWait(prefix + rndString()));
            shouldFail(() -> typeAndWait(prefix + rndString(), rndString()));
            shouldFail(() -> submitAndWait(prefix + rndString()));
        };

        public void shouldFail(final Callable<HtmlPage> r) throws Exception
        {
            Throwable throwable = null;
            try
            {
                setHtmlPage(r.call());
            }
            catch (final Throwable t)
            {
                throwable = t;
            }

            Assert.assertNotNull("Expected error but none was thrown", throwable);
            Assert.assertTrue("Trace does not include test class", traceIncludes(throwable, getClass().getName(), "execute"));
        }
    }

}
