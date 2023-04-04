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

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.clientperformance.ClientPerformanceExtensionConnector.CommunicationException;
import com.xceptance.xlt.clientperformance.ClientPerformanceUtils;
import com.xceptance.xlt.clientperformance.WebExtConnectionHandler;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.WebDriverActionDirector;

/**
 * An extended {@link ChromeDriver} which allows to record data about requests and browser events or to run Chrome with
 * a virtual display.
 * <p>
 * <b>Collected Data</b>
 * <p>
 * To collect data about requests and browser events, a special extension will be installed into the browser. This
 * extension provides access to the following information:
 * <ul>
 * <li>for requests:
 * <ul>
 * <li>start and total processing time</li>
 * <li>URL, status code, response content type</li>
 * <li>sent and received bytes</li>
 * <li>network timings (DNS time, connect time, send time, server busy time, receive time, time to first byte, time to
 * last byte)</li>
 * </ul>
 * </li>
 * <li>for browser events:
 * <ul>
 * <li>the time after which the event occurred when loading a new page</li>
 * </ul>
 * </li>
 * </ul>
 * The following browser events will be reported:
 * <ul>
 * <li>DomLoading</li>
 * <li>DomInteractive</li>
 * <li>DomComplete</li>
 * <li>DomContentLoadedEventStart</li>
 * <li>DomContentLoadedEventEnd</li>
 * <li>LoadEventStart</li>
 * <li>LoadEventEnd</li>
 * <li>FirstPaint</li>
 * <li>FirstContentfulPaint</li>
 * </ul>
 * All this data will be available in the XLT load test report. Request data is shown in the Requests section and
 * browser events can be found in the Page Load Timings section.
 * <p>
 * <b>Headless Mode</b>
 * <p>
 * On Unix machines, it is possible to run the browser in "headless" mode, i.e. with a virtual display. To put the
 * browser in headless mode, simply set the following property in the configuration of your test project:
 *
 * <pre>
 * xlt.webDriver.chrome_clientperformance.screenless = true
 * </pre>
 *
 * Note that for the headless mode to work, the <code>xvfb</code> binary must be installed on the target machine and
 * must be findable via the PATH variable. If this is not the case, the browser will be run with the default display.
 */
public class XltChromeDriver extends ChromeDriver
{
    private static final String PROPERTY_DOMAIN = "xlt.webDriver.chrome_clientperformance.";

    /**
     * The name of the field in {@link ChromeDriverService} that holds the environment variable map.
     */
    private static final String FIELD_NAME_ENVIRONMENT = "environment";

    /**
     * The class logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(XltChromeDriver.class);

    /**
     * The XLT property to enable headless mode if it is available at all.
     */
    private static final String PROPERTY_HEADLESS = PROPERTY_DOMAIN + "screenless";

    /**
     * Whether headless mode is enabled.
     */
    private static final boolean HEADLESS_ENABLED;

    /**
     * The XLT property to enable recording of incomplete/aborted requests.
     */
    private static final String PROPERTY_RECORD_INCOMPLETE = PROPERTY_DOMAIN + "recordIncomplete";

    /**
     * Whether recording of incomplete/aborted requests is enabled.
     */
    private static final boolean RECORD_INCOMPLETE_ENABLED;

    /**
     * The base name of the extension file.
     */
    private static final String EXTENSION_FILE_NAME = "xlt-timerrecorder-chrome";

    /**
     * The file name extension of the extension file.
     */
    private static final String EXTENSION_FILE_ENDING = ".crx";

    /**
     * The file to which the extension is extracted.
     */
    private static File extensionFile;

    /**
     * The number of connect retries for the initial connect. See {@link XltChromeDriver#initConnect(int)}.
     */
    private static final int CONNECT_RETRY_COUNT = 5;

    /**
     * The timeout to start with during the initial connect. See {@link XltChromeDriver#initConnect(int)}.
     */
    private static final long CONNECT_RETRY_BASE_TIMEOUT = 500;

    /**
     * The factor used to increase the timeout during the initial connect for each retry. See
     * {@link XltChromeDriver#initConnect(int)}.
     */
    private static final float CONNECT_RETRY_TIMEOUT_FACTOR = 1.5f;

    /**
     * If set to true then the test run will succeed also if we were not able to get the performance data due to a
     * connection issue. In that case a session event is logged. Otherwise an exception is thrown which will break the
     * test. Default is true. {@link XltChromeDriver#preQuit()}.
     */
    private static final String PROPERTY_IGNORE_MISSING_DATA = PROPERTY_DOMAIN + "ignoreMissingData";

    /**
     * Hold the value read from xlt properties. See {@link XltChromeDriver#PROPERTY_CONNECT_TIMEOUT_IGNORE}
     */
    private static final boolean IGNORE_MISSING_DATA;

    /**
     * Handle extension communication, send and receive messages and handle connection state changes
     */
    private final WebExtConnectionHandler connectionHandler = WebExtConnectionHandler.newInstance(PROPERTY_DOMAIN);

    static
    {
        // read in and remember settings
        final XltProperties props = XltProperties.getInstance();

        HEADLESS_ENABLED = props.getProperty(PROPERTY_HEADLESS, false);
        RECORD_INCOMPLETE_ENABLED = props.getProperty(PROPERTY_RECORD_INCOMPLETE, false);
        IGNORE_MISSING_DATA = props.getProperty(PROPERTY_IGNORE_MISSING_DATA, true);

        // unpack the extension file to the temp directory
        try
        {
            final File tmpFile = File.createTempFile(EXTENSION_FILE_NAME, EXTENSION_FILE_ENDING);
            tmpFile.deleteOnExit();

            final URL extensionUrl = ClientPerformanceUtils.class.getResource(EXTENSION_FILE_NAME + EXTENSION_FILE_ENDING);
            if (extensionUrl == null)
            {
                LOG.error("Failed to locate Chrome extension file in class path");
            }
            else
            {
                FileUtils.copyURLToFile(extensionUrl, tmpFile);

                extensionFile = tmpFile;
            }
        }
        catch (final Exception e)
        {
            LOG.error("Failed to unpack Chrome extension to temp folder", e);
        }
    }

    /**
     * Creates a new {@link XltChromeDriver} instance with default settings.
     */
    public XltChromeDriver()
    {
        this(null, null, HEADLESS_ENABLED);
    }

    /**
     * Creates a new {@link XltChromeDriver} instance with the given parameters and otherwise default settings.
     *
     * @param options
     *            the options to use (may be <code>null</code>)
     */
    public XltChromeDriver(final ChromeOptions options)
    {
        this(null, options, HEADLESS_ENABLED);
    }

    /**
     * Creates a new {@link XltChromeDriver} instance with the given parameters and otherwise default settings.
     *
     * @param options
     *            the options to use (may be <code>null</code>)
     * @param screenless
     *            whether to run in headless mode (using Xvfb)
     */
    public XltChromeDriver(final ChromeOptions options, final boolean screenless)
    {
        this(null, options, screenless);
    }

    /**
     * Creates a new {@link XltChromeDriver} instance with the given parameters and otherwise default settings.
     *
     * @param service
     *            the driver service (may be <code>null</code>)
     */
    public XltChromeDriver(final ChromeDriverService service)
    {
        this(service, null, HEADLESS_ENABLED);
    }

    /**
     * Creates a new {@link XltChromeDriver} instance with the given parameters and otherwise default settings.
     *
     * @param service
     *            the driver service (may be <code>null</code>)
     * @param options
     *            the options to use (may be <code>null</code>)
     */
    public XltChromeDriver(final ChromeDriverService service, final ChromeOptions options)
    {
        this(service, options, HEADLESS_ENABLED);
    }

    /**
     * Creates a new {@link XltChromeDriver} instance with the given parameters.
     *
     * @param service
     *            the driver service (may be <code>null</code>)
     * @param options
     *            the options to use (may be <code>null</code>)
     * @param screenless
     *            whether to run in headless mode (using Xvfb)
     */
    public XltChromeDriver(final ChromeDriverService service, final ChromeOptions options, final boolean screenless)
    {
        super(modifyService(service, screenless), modifyOptions(options));
        init();
    }

    private void init()
    {
        try
        {
            LOG.debug("Starting extension communication server");
            connectionHandler.start();
            initConnect(CONNECT_RETRY_COUNT);
        }
        catch (final CommunicationException e)
        {
            throw new WebDriverException("Starting extension communication failed", e);
        }
    }

    /**
     * Send the connect properties to the extension and wait for the connect. Retry a few times if no connection was
     * made within a certain time as defined by {@link XltChromeDriver#CONNECT_RETRY_BASE_TIMEOUT}. For each retry
     * increase the timeout as defined by {@link XltChromeDriver#CONNECT_RETRY_TIMEOUT_FACTOR}. If no connection was
     * made then this will just continue so that we can retry later.
     * 
     * @param retryCount
     *            - how often should we try to get a working connection
     */
    private void initConnect(final int retryCount)
    {
        final String url = "data:,xltParameters?xltPort=" + connectionHandler.getPort() + "&clientID=" + connectionHandler.getID() +
                           "&recordIncompleted=" + RECORD_INCOMPLETE_ENABLED;

        long timeout = CONNECT_RETRY_BASE_TIMEOUT;

        int tries = 0;
        do
        {
            tries++;

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Try: #" + tries + ". Sending connect parameters: " + url);
            }

            get(url);

            try
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Waiting for " + timeout + " ms.");
                }

                connectionHandler.waitForConnect(timeout);
            }
            catch (CommunicationException | InterruptedException e)
            {
                LOG.warn("Error while waiting for connection.", e);
            }
            catch (java.util.concurrent.TimeoutException e)
            {
                LOG.debug("Timeout while waiting for connect.");
            }
            timeout = (long) (timeout * CONNECT_RETRY_TIMEOUT_FACTOR);
        }
        while (!connectionHandler.isConnected() && tries < retryCount);
    }

    /**
     * Modifies the passed driver service for headless operation.
     *
     * @param service
     *            the driver service (may be <code>null</code>)
     * @param headless
     *            whether to run the browser in headless mode
     * @return the modified service
     */
    private static ChromeDriverService modifyService(ChromeDriverService service, final boolean headless)
    {
        // get/create the driver service
        service = ObjectUtils.defaultIfNull(service, ChromeDriverService.createDefaultService());

        // modify the service's environment for headless mode
        if (headless)
        {
            final String display = ClientPerformanceUtils.getDisplay();
            if (display != null)
            {
                // HACK: we can access the service's environment settings via reflection only

                // read the environment settings
                final Map<String, String> environment = ReflectionUtils.readField(ChromeDriverService.class, service,
                                                                                           FIELD_NAME_ENVIRONMENT);

                // create the new environment settings including the DISPLAY variable
                final ImmutableMap.Builder<String, String> mapBuilder = new ImmutableMap.Builder<>();
                if (environment != null)
                {
                    mapBuilder.putAll(environment);
                }
                mapBuilder.put("DISPLAY", display);
                mapBuilder.put("DBUS_SESSION_BUS_ADDRESS", "/dev/null");

                final ImmutableMap<String, String> newEnvironment = mapBuilder.build();

                // finally write the new environment settings
                ReflectionUtils.writeField(ChromeDriverService.class, service, FIELD_NAME_ENVIRONMENT, newEnvironment);
            }
        }

        return service;
    }

    /**
     * Modifies the given options for client-performance measurements and headless operation.
     * 
     * @param options
     *            the options
     * @return the modified options
     */
    private static ChromeOptions modifyOptions(ChromeOptions options)
    {
        // check if the extension file is (still) available
        if (extensionFile == null || !extensionFile.isFile())
        {
            throw new WebDriverException("Chrome client performance extension not available (path: " + extensionFile + ")");
        }

        // get/create the options
        options = ObjectUtils.defaultIfNull(options, new ChromeOptions());

        // modify the options as needed
        options.addExtensions(extensionFile);
        options.addArguments("--unlimited-storage");

        return options;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close()
    {
        if (isConnected() && getWindowHandles().size() == 1)
        {
            quit();
        }
        else
        {
            super.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void quit()
    {
        if (!isConnected())
        {
            LOG.debug("Driver already closed");
            return;
        }

        try
        {
            preQuit();

            LOG.debug("Closing extension communication");
            connectionHandler.stop();
        }
        catch (final WebDriverException t)
        {
            LOG.warn("Error during driver shutdown", t);
            throw t;
        }
        catch (final Throwable t)
        {
            LOG.warn("Error during driver shutdown", t);
        }
        finally
        {
            LOG.debug("Closing driver");
            super.quit();

            LOG.debug("Chrome client performance driver closed");
        }
    }

    private boolean isConnected()
    {
        return getSessionId() != null;
    }

    /**
     * Performs any house-keeping actions needed before a driver can really be quit. Currently, this includes retrieving
     * (final) request and performance data from the browser and reporting it to XLT.
     * <p>
     * Note that this method will also be called directly from {@link WebDriverActionDirector}. However, to avoid making
     * this method public API, it will be private and hence needs to be called reflectively. For this reason, don't
     * rename it without adjusting {@link WebDriverActionDirector} accordingly.
     */
    private void preQuit()
    {
        if (!isConnected())
        {
            return;
        }

        if (!hasWindow())
        {
            LOG.error("Failed to get client-performance metrics. All browser windows already closed.");
        }
        else
        {
            LOG.debug("Try to fetch and dump remaining client-performance metrics");
            if (!connectionHandler.isConnected())
            {
                LOG.debug("Not connected. Try reconnect...");
                initConnect(CONNECT_RETRY_COUNT);
            }
            if (connectionHandler.isConnected())
            {
                connectionHandler.reportRemainingPerformanceData();
            }
            else
            {
                final String logMessage = "No connection to fetch remaining data. Maybe not all performance data is available.";
                if (IGNORE_MISSING_DATA)
                {
                    LOG.error(logMessage);
                    SessionImpl.logEvent(getClass().getSimpleName(), logMessage);
                }
                else
                {
                    throw new WebDriverException(logMessage);
                }
            }
        }
    }

    private boolean hasWindow()
    {
        try
        {
            return getWindowHandles().size() > 0;
        }
        catch (final Throwable e)
        {
            return false;
        }
    }

    /**
     * Returns a {@link Builder} object to create a new {@link XltChromeDriver} instance.
     *
     * @return the builder
     */
    public static Builder xltBuilder()
    {
        return new Builder();
    }

    /**
     * Builder class to create {@link XltChromeDriver} instances. First set the desired properties and then call
     * {@link #build()} to get the configured driver instance.
     */
    public static final class Builder
    {
        private ChromeDriverService service;

        private ChromeOptions options;

        private boolean headless = HEADLESS_ENABLED;

        /**
         * Sets the desired driver service.
         *
         * @param service
         *            the service
         * @return this builder instance
         */
        public Builder setService(final ChromeDriverService service)
        {
            this.service = service;
            return this;
        }

        /**
         * Sets the desired options.
         *
         * @param options
         *            the options
         * @return this builder instance
         */
        public Builder setOptions(final ChromeOptions options)
        {
            this.options = options;
            return this;
        }

        /**
         * Whether to run the browser in headless mode.
         *
         * @param headless
         *            whether headless mode is enabled
         * @return this builder instance
         */
        public Builder setHeadless(final boolean headless)
        {
            this.headless = headless;
            return this;
        }

        /**
         * Creates a new {@link XltChromeDriver} instance configured with all the previously set properties.
         *
         * @return the driver instance
         */
        public XltChromeDriver build()
        {
            return new XltChromeDriver(service, options, headless);
        }
    }
}
