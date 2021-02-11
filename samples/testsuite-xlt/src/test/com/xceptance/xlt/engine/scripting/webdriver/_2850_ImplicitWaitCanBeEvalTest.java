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
package test.com.xceptance.xlt.engine.scripting.webdriver;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.engine.scripting.TestContext;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * Checks that certain commands return immediately when searching for non-existing elements.
 */
public class _2850_ImplicitWaitCanBeEvalTest extends AbstractWebDriverScriptTestCase
{
    @BeforeClass
    public static void beforeClass()
    {
        TestContext.getCurrent().setImplicitTimeout(2000);
    }

    public _2850_ImplicitWaitCanBeEvalTest()
    {
        super(new XltDriver(false), "http://localhost:8080");
    }

    @Test
    public void test()
    {
        final String locator = "css=#foorz";
        final String xpath = "id('foorz')";
        final long maxRuntime = 4000;

        open("testpages/examplePage_1.html");

        long start = TimerUtils.getTime();
        {
            assertNotElementPresent(locator);
            assertNotElementCount(locator, 5);
            assertNotXpathCount(xpath, 5);
            assertElementCount(locator, 0);
            assertXpathCount(xpath, 0);

            waitForNotElementPresent(locator);
            waitForNotElementCount(locator, 5);
            waitForNotXpathCount(xpath, 5);
            waitForElementCount(locator, 0);
            waitForXpathCount(xpath, 0);
        }
        long runtime = TimerUtils.getTime() - start;

        Assert.assertTrue(String.format("Test runtime (%d ms) exceeded maximum runtime (%d ms)", runtime, maxRuntime),
                          runtime <= maxRuntime);
    }

    @AfterClass
    public static void afterClass()
    {
        final TestContext testContext = TestContext.getCurrent();
        testContext.setImplicitTimeout(testContext.getDefaultImplicitTimeout());
    }
}
