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
package com.xceptance.xlt.api.tests;

import org.junit.After;
import org.openqa.selenium.WebDriver;

import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.engine.util.DefaultWebDriverFactory;

/**
 * A super class for {@link WebDriver}-based test cases.
 * <p>
 * Current features:
 * <ul>
 * <li>auto-manages {@link WebDriver} instance creation and disposal</li>
 * <li>the type of {@link WebDriver} to be used for the test can be changed by configuration</li>
 * </ul>
 * <p>
 * For available configuration options, see the corresponding properties with key <code>xlt.webDriver.*</code> in the
 * XLT test suite settings.
 * <p>
 * For special purposes, the {@link WebDriver} instance can also be explicitly set programmatically, in which case any
 * automatic clean-up will be disabled.
 */
public abstract class AbstractWebDriverTestCase extends AbstractTestCase
{
    /**
     * The {@link WebDriver} instance to use for the test.
     */
    private WebDriver webDriver;

    /**
     * Whether or not the used {@link WebDriver} instance will be quit automatically once the test case is finished.
     */
    private boolean autoClose;

    /**
     * Returns the {@link WebDriver} instance to use for the test. If none was set so far via
     * {@link #setWebDriver(WebDriver)}, a new instance will be created, and this instance will also be returned for
     * subsequent calls to {@link #getWebDriver()}. What instance will be created depends on the configuration.
     * 
     * @return the web driver
     */
    public WebDriver getWebDriver()
    {
        if (webDriver == null)
        {
            // none set -> set the default web driver, i.e. the one configured in the settings
            setWebDriver(DefaultWebDriverFactory.getWebDriver());

            // we created it, we have to close it
            autoClose = true;
        }

        return webDriver;
    }

    /**
     * Sets the {@link WebDriver} instance to use for the test. Note that when explicitly setting the driver instance
     * via this method, you also have to close it by yourself. Furthermore, you can set a driver instance only once.
     * 
     * @param webDriver
     *            the web driver to use
     */
    public void setWebDriver(final WebDriver webDriver)
    {
        ParameterCheckUtils.isNotNull(webDriver, "webDriver");

        // check whether we already have an instance
        if (this.webDriver != null)
        {
            throw new IllegalStateException("You may set a web driver instance only once");
        }

        this.webDriver = webDriver;
    }

    /**
     * Quits the {@link WebDriver} instance, but only if it was created implicitly. If set explicitly, the caller is
     * responsible to quit the driver instance properly.
     */
    @After
    public final void __quitWebDriver()
    {
        if (autoClose && webDriver != null)
        {
            webDriver.quit();
        }
    }
}
