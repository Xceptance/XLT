/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;

/**
 * Simple test to ensure that stack traces of '*AndWait' commands are helpful (originate from the main thread). See
 * improvement #3089 for details.
 */
public class ProperStackTraceForWaitCommandsWDTest extends AbstractWebDriverScriptTestCase
{

    public ProperStackTraceForWaitCommandsWDTest()
    {
        super(new XltDriver(true), "http://localhost:8080");
    }

    @Test
    public void ensureStackTraceIsHelpful() throws Throwable
    {
        new scripting.modules.Open_ExamplePage().execute();

        new WDModule().execute();
    }

    private static String rndString()
    {
        return RandomStringUtils.randomAlphanumeric(5, 10);
    }

    private static boolean traceIncludes(final Throwable t, final String className, final String methodName)
    {
        return Arrays.stream(t.getStackTrace()).anyMatch((e) -> className.equals(e.getClassName()) && methodName.equals(e.getMethodName()));
    }

    private static class WDModule extends AbstractWebDriverModule
    {
        protected void doCommands(String... parameters) throws Exception
        {
            final String prefix = "id=_rnd_";
            shouldFail(() -> {
                clickAndWait(prefix + rndString());
                return null;
            });
            shouldFail(() -> {
                uncheckAndWait(prefix + rndString());
                return null;
            });
            shouldFail(() -> {
                selectAndWait(prefix + rndString(), rndString());
                return null;
            });
            shouldFail(() -> {
                doubleClickAndWait(prefix + rndString());
                return null;
            });
            shouldFail(() -> {
                checkAndWait(prefix + rndString());
                return null;
            });
            shouldFail(() -> {
                typeAndWait(prefix + rndString(), rndString());
                return null;
            });
            shouldFail(() -> {
                submitAndWait(prefix + rndString());
                return null;
            });
        }

        protected void shouldFail(final Callable<Void> r) throws Exception
        {
            Throwable throwable = null;
            try
            {
                r.call();
            }
            catch (final Throwable t)
            {
                throwable = t;
            }

            Assert.assertNotNull("Expected error but none was thrown", throwable);
            Assert.assertTrue("Trace does not include test class", traceIncludes(throwable, getClass().getName(), "doCommands"));
        }
    }

}
