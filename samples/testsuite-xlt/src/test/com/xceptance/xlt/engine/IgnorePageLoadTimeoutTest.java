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
package test.com.xceptance.xlt.engine;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.webdriver.XltFirefoxDriver;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * Running this test you are able to create "PageLoadTimeoutException". You have to make sure
 * "com.xceptance.xlt.scripting.ignorePageLoadTimeouts" in "dev.properties" is set to false to see the exception.
 */
@Ignore("Can be run manually only")
public class IgnorePageLoadTimeoutTest extends AbstractWebDriverScriptTestCase
{
    private static final long PAGE_LOAD_TIMEOUT = 3000;

    private static String ignorePageLoadTimeouts;

    private static String pageLoadTimeout;

    /**
     * Constructor.
     */
    public IgnorePageLoadTimeoutTest()
    {
        // super(new ChromeDriver(), "http://localhost:8080/");
        // super(new FirefoxDriver(), "http://localhost:8080/");
        // super(new XltChromeDriver(), "http://localhost:8080/");
        super(new XltFirefoxDriver(), "http://localhost:8080/");
    }

    @BeforeClass
    public static void beforeClass()
    {
        ignorePageLoadTimeouts = XltProperties.getInstance().getProperty("com.xceptance.xlt.scripting.ignorePageLoadTimeouts", "false");
        pageLoadTimeout = XltProperties.getInstance().getProperty("com.xceptance.xlt.scripting.defaultTimeout", "30000");

        XltProperties.getInstance().setProperty("com.xceptance.xlt.scripting.ignorePageLoadTimeouts", "true");
        XltProperties.getInstance().setProperty("com.xceptance.xlt.scripting.defaultTimeout", Long.toString(PAGE_LOAD_TIMEOUT));
    }

    @AfterClass
    public static void afterClass()
    {
        XltProperties.getInstance().setProperty("com.xceptance.xlt.scripting.ignorePageLoadTimeouts", ignorePageLoadTimeouts);
        XltProperties.getInstance().setProperty("com.xceptance.xlt.scripting.defaultTimeout", pageLoadTimeout);
    }

    /**
     * Checks if timeout exceptions are ignored correctly if the request for an embedded image is slow.
     */
    @Test
    public void slowImageRequest() throws Throwable
    {
        // page contains an image that takes 40s to load
        load("testpages/examplePage_2.html");
        assertTitle("timing example page");
    }

    /**
     * Checks if timeout exceptions are ignored correctly if the main request is slow.
     */
    @Test(expected = NoSuchElementException.class)
    public void slowMainRequest() throws Throwable
    {
        load("performance/timing/sleep.jsp?sleep=40000&fake=2");
        assertText("css=body>h1", "Performance Timing Test");
    }

    /**
     * Clean up.
     */
    @After
    public void after()
    {
        // Shutdown WebDriver.
        getWebDriver().quit();
    }

    private void load(String url)
    {
        startAction("Start");

        long start = TimerUtils.get().getStartTime();
        open(url);
        long runtime = TimerUtils.get().getElapsedTime(start);

        // now check the timings
        long minRuntime = PAGE_LOAD_TIMEOUT;
        long maxRuntime = minRuntime + 500;

        Assert.assertTrue(String.format("Unexpected runtime: %d not in range [%d..%d]", runtime, minRuntime, maxRuntime),
                          runtime >= minRuntime && runtime <= maxRuntime);
    }
}
