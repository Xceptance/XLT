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
package com.xceptance.xlt.engine.webdriver;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.XltEngine;
import com.xceptance.xlt.engine.util.DefaultWebDriverFactory;
import com.xceptance.xlt.engine.util.TimerUtils;

import util.httpserver.FaultyHttpServer;
import util.httpserver.FaultyHttpServer.Behavior;

/**
 * Shows the behavior of certain browsers in case page load timeout / script timeout is configured, but the
 * corresponding exceptions are ignored.
 */
@Ignore("To be run manually only")
@RunWith(Parameterized.class)
public class IgnorePageLoadTimeoutTest
{
    private static final long PAGE_LOAD_TIMEOUT = 3000;

    private final FaultyHttpServer httpServer;

    private final WebDriver driver;

    @BeforeClass
    public static void beforeClass()
    {
        XltEngine.reset();
        XltProperties.getInstance().setProperty("xlt.webDriver.chrome_clientperformance.recordIncomplete", "true");
        XltProperties.getInstance().setProperty("xlt.webDriver.firefox_clientperformance.recordIncomplete", "true");

        // XltProperties.getInstance().setProperty("xlt.webDriver.firefox_clientperformance.overrideResponseTimeout",
        // "true");
        // XltProperties.getInstance().setProperty("com.xceptance.xlt.timeout", "4000");

        // XltProperties.getInstance().setProperty("xlt.webDriver.firefox_clientperformance.legacyMode", "true");

        // XltProperties.getInstance().setProperty("xlt.webDriver.reuseDriver", "true");
    }

    @AfterClass
    public static void afterClass() throws IOException
    {
        XltEngine.reset();
    }

    @Parameters
    public static Collection<Object[]> parameters()
    {
        final String[] webDriverNames = new String[]
            {
                "chrome_clientperformance", "firefox_clientperformance"
            };

        final Behavior[] behaviors = new Behavior[]
            {
                // Behavior.UNAVAILABLE,
                Behavior.DEAF, Behavior.LISTENS_ONLY, Behavior.READS_REQUEST, Behavior.READS_REQUEST_WRITES_PARTIAL_RESPONSE
            };

        // now create the wanted parameter combinations
        final Collection<Object[]> parameters = new ArrayList<>();

        for (int n = 0; n < webDriverNames.length; n++)
        {
            for (int b = 0; b < behaviors.length; b++)
            {
                parameters.add(new Object[]
                    {
                        webDriverNames[n], behaviors[b]
                    });
            }
        }

        return parameters;
    }

    public IgnorePageLoadTimeoutTest(final String webDriverName, final FaultyHttpServer.Behavior behavior) throws IOException
    {
        System.out.printf("### %s / %s ###############################################\n", webDriverName, behavior);

        // create/set up the driver
        XltProperties.getInstance().setProperty("xlt.webDriver", webDriverName);

        driver = DefaultWebDriverFactory.getWebDriver();

        driver.manage().timeouts().pageLoadTimeout(Duration.ofMillis(PAGE_LOAD_TIMEOUT));
        driver.manage().timeouts().scriptTimeout(Duration.ofMillis(PAGE_LOAD_TIMEOUT));

        // create the HTTP server
        httpServer = new FaultyHttpServer(behavior, 4712);
    }

    @After
    public void after() throws IOException
    {
        driver.quit();
        httpServer.close();
    }

    // @Test
    // public void slowImageRequest()
    // {
    // load(getClass().getResource(getClass().getSimpleName() + "_image.html").toString());
    // }
    //
    // @Test
    // public void slowAsyncXHR()
    // {
    // load(getClass().getResource(getClass().getSimpleName() + "_xhrAsync.html").toString());
    // }
    //
    // @Test
    // public void slowSyncXHR()
    // {
    // load(getClass().getResource(getClass().getSimpleName() + "_xhrSync.html").toString());
    // }
    //
    // @Test
    // public void slowScriptRequest()
    // {
    // load(getClass().getResource(getClass().getSimpleName() + "_script.html").toString());
    // }
    //
    // @Test
    // public void slowCssRequest()
    // {
    // load(getClass().getResource(getClass().getSimpleName() + "_css.html").toString());
    // }
    //
    // @Test
    // public void slowMainRequest()
    // {
    // load("http://localhost:4712/");
    // }

    @Test
    public void all()
    {
        // loadWithTimeoutException(getClass().getResource(getClass().getSimpleName() + "_xhrSync.html").toString(),
        // "SyncXHR");
        loadWithTimeoutException(getClass().getResource(getClass().getSimpleName() + "_css.html").toString(), "CSS");
        loadWithTimeoutException(getClass().getResource(getClass().getSimpleName() + "_image.html").toString(), "Image");
        loadWithTimeoutException(getClass().getResource(getClass().getSimpleName() + "_script.html").toString(), "Script");
        // load(getClass().getResource(getClass().getSimpleName() + "_xhrAsync.html").toString(), "AsyncXHR");
        loadWithTimeoutException("http://localhost:4712/", "Main");
        load(getClass().getResource(getClass().getSimpleName() + "_xhrAsync.html").toString(), "AsyncXHR");

        load("https://www.google.de/", "Google");
        load("https://www.google.com/", "Google");
    }

    private void loadWithTimeoutException(final String url, final String action)
    {
        System.out.printf("### %s\n", url);

        final long start = TimerUtils.get().getStartTime();

        try
        {
            Session.getCurrent().startAction(action);
            driver.get(url);

            Assert.fail("Expected TimeoutException, but none was thrown");
        }
        catch (final TimeoutException e)
        {
            final long runtime = TimerUtils.get().getElapsedTime(start);

            // now check the timings
            final long minRuntime = PAGE_LOAD_TIMEOUT;
            final long maxRuntime = minRuntime + 500;

            Assert.assertTrue(String.format("Unexpected runtime: %d not in range [%d..%d]", runtime, minRuntime, maxRuntime),
                              runtime >= minRuntime && runtime <= maxRuntime);
        }

        // checkDriverIsResponding();
    }

    private void load(final String url, final String action)
    {
        System.out.printf("### %s\n", url);

        final long start = TimerUtils.get().getStartTime();

        Session.getCurrent().startAction(action);
        driver.get(url);

        final long runtime = TimerUtils.get().getElapsedTime(start);

        // now check the timings
        final long maxRuntime = 2000;
        Assert.assertTrue(String.format("Unexpected runtime: %d exceeds maximum value %d", runtime, maxRuntime), runtime <= maxRuntime);

        // checkDriverIsResponding();
    }

    private void checkDriverIsResponding()
    {
        System.out.printf("### %s\n", "Checking browser responsiveness");

        // ensure that the driver is responding almost immediately
        final long start = TimerUtils.get().getStartTime();

        // take a screenshot
        ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

        final long runtime = TimerUtils.get().getElapsedTime(start);

        // now check the timings
        final long maxRuntime = 1500;
        Assert.assertTrue(String.format("Unexpected runtime: %d exceeds maximum value %d", runtime, maxRuntime), runtime <= maxRuntime);
    }
}
