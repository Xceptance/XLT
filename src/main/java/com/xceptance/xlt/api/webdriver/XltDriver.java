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
package com.xceptance.xlt.api.webdriver;

import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.xltdriver.HtmlUnitDriver;
import com.xceptance.xlt.engine.xltdriver.WebDriverXltWebClient;

/**
 * Extended version of {@link HtmlUnitDriver} which just uses the XLT web client. XltDriver can act as a full
 * replacement for the HtmlUnitDriver and offers additional capabilities which are important for load testing.
 * <p>
 * This class is final to avoid changes to the web client which would render the XLT functionality obsolete.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public final class XltDriver extends HtmlUnitDriver
{
    /**
     * Constructs a new instance. What browser version will be used and whether JavaScript will be enabled is configured
     * in the XLT settings.
     */
    public XltDriver()
    {
        this(null, null);
    }

    /**
     * Constructs a new instance with the specified JavaScript support. What browser version will be used is configured
     * in the XLT settings.
     * 
     * @param enableJavaScript
     *            whether to enable JavaScript support or not
     */
    public XltDriver(final boolean enableJavaScript)
    {
        this(null, Boolean.valueOf(enableJavaScript));
    }

    /**
     * Constructs a new instance with the specified browser version. Whether JavaScript will be enabled is configured in
     * the XLT settings.
     *
     * @param version
     *            the browser version to use
     */
    public XltDriver(final BrowserVersion version)
    {
        this(version, null);
    }

    /**
     * Constructs a new instance with the specified browser version and JavaScript support.
     *
     * @param version
     *            the browser version to use
     * @param enableJavaScript
     *            whether to enable JavaScript support or not
     */
    public XltDriver(final BrowserVersion version, final boolean enableJavaScript)
    {
        this(version, Boolean.valueOf(enableJavaScript));
    }

    /**
     * Helper constructor that handles <code>null</code> values.
     * 
     * @param version
     *            the browser version to use (may be <code>null</code>)
     * @param enableJavaScript
     *            whether to enable JavaScript support or not (may be <code>null</code>)
     */
    private XltDriver(final BrowserVersion version, Boolean enableJavaScript)
    {
        // pass null values, XltWebClient handles them correctly
        super(version);

        // if enableJavaScript was not defined, get the default setting from the XLT configuration
        if (enableJavaScript == null)
        {
            enableJavaScript = XltProperties.getInstance().getProperty("com.xceptance.xlt.javaScriptEnabled", false);
        }

        setJavascriptEnabled(enableJavaScript);
    }

    /**
     * Returns a new web client instance to be used by this driver. Overwritten to return an enhanced XLT web client
     * instead of HtmlUnit's web client.
     * 
     * @param version
     *            which browser to emulate
     * @return the web client
     */
    @Override
    protected WebClient newWebClient(final BrowserVersion version)
    {
        return new WebDriverXltWebClient(version);
    }

    /**
     * Returns the underlying {@link WebClient} instance to work with it directly. Mostly used for testing purposes of
     * the framework itself.
     * 
     * @return the web client
     */
    @Override
    public WebClient getWebClient()
    {
        return super.getWebClient();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void quit()
    {
        final WebDriverXltWebClient client = (WebDriverXltWebClient) getWebClient();
        if (client != null)
        {
            client.quit();
        }

        /*
         * Dont't call super#quit() since this closes all windows immediately which in turn causes
         * CurrentWindowTracker#webWindowClosed(WebWindowEvent) to be called whose implementation might add a new
         * top-level window as current window
         */
    }

    /*
     * #1347: Override remaining WebDriver interface implementations of HtmlUnitDriver in order to let
     * WebDriverException#getDriverName(StackTraceElement[]) work as expected.
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public WebElement findElement(final By by)
    {
        return super.findElement(by);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WebElement> findElements(final By by)
    {
        return super.findElements(by);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Navigation navigate()
    {
        return super.navigate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Options manage()
    {
        return super.manage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close()
    {
        super.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPageSource()
    {
        return super.getPageSource();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWindowHandle()
    {
        return super.getWindowHandle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getWindowHandles()
    {
        return super.getWindowHandles();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentUrl()
    {
        return super.getCurrentUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void get(final String url)
    {
        super.get(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle()
    {
        return super.getTitle();
    }
}
