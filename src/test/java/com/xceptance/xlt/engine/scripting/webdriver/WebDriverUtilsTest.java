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
/*
 * File: WebDriverUtilsTest.java
 * Created on: Apr 10, 2017
 */
package com.xceptance.xlt.engine.scripting.webdriver;

import org.htmlunit.MockWebConnection;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.engine.XltEngine;

/**
 * Tests the implementation of {@link WebDriverUtils}.
 */
public class WebDriverUtilsTest
{
    @After
    public void resetXltEngine()
    {
        XltEngine.reset(); // also initializes XLT properties again 
    }
    
    private WebDriver getWebDriver(final boolean jsEnabled)
    {
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.javaScriptEngineEnabled", String.valueOf(jsEnabled));
        props.setProperty("com.xceptance.xlt.javaScriptEnabled", String.valueOf(jsEnabled));
        
        final XltDriver driver = new XltDriver();
        final MockWebConnection conn = new MockWebConnection();
        conn.setDefaultResponse("<h1>Hello</h1><p>... World!</p>");
        driver.getWebClient().setWebConnection(conn);
        driver.getWebClient().getOptions().setThrowExceptionOnScriptError(true);

        driver.get("http://any.server.com");

        return driver;
    }

    @Test(expected = UnsupportedOperationException.class)
    public void executeJS_NoJS() throws Throwable
    {
        final WebDriver driver = getWebDriver(false);
        WebDriverUtils.executeJavaScript(driver, "void 0");
    }

    @Test(expected = WebDriverException.class)
    public void executeJS_JSSupportIsOnButExprThrowsError() throws Throwable
    {
        final WebDriver driver = getWebDriver(true);

        // JS execution must work
        Assert.assertNotNull(WebDriverUtils.executeJavaScript(driver, "return window.location"));

        // provoke a TypeError
        try
        {
            WebDriverUtils.executeJavaScript(driver, "foo.bar");
        }
        catch (final Throwable t)
        {
            Assert.assertTrue(t.getMessage().startsWith("Failed to evaluate JavaScript"));
            throw t;
        }
    }

    @Test
    public void executeJSIfPossible_NoJS() throws Throwable
    {
        final WebDriver driver = getWebDriver(false);
        Assert.assertNull(WebDriverUtils.executeJavaScriptIfPossible(driver, "void 0"));
        Assert.assertNull(WebDriverUtils.executeJavaScriptIfPossible(driver, "window.location"));
    }

    @Test(expected = WebDriverException.class)
    public void executeJSIfPossible_JSSupportIsOnButExprThrowsError() throws Throwable
    {
        final WebDriver driver = getWebDriver(true);

        // make sure, that JS execution works at all
        Assert.assertNotNull(WebDriverUtils.executeJavaScriptIfPossible(driver, "return window.location"));

        // provoke a TypeError
        try
        {
            WebDriverUtils.executeJavaScriptIfPossible(driver, "foo.bar");
        }
        catch (final Throwable t)
        {
            Assert.assertTrue(t.getMessage().startsWith("Failed to evaluate JavaScript"));
            throw t;
        }
    }
}
