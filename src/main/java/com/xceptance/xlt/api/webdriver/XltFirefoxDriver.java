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
package com.xceptance.xlt.api.webdriver;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FileExtension;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.service.DriverCommandExecutor;
import org.openqa.selenium.remote.service.DriverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.clientperformance.ClientPerformanceExtensionConnector.CommunicationException;
import com.xceptance.xlt.clientperformance.ClientPerformanceUtils;
import com.xceptance.xlt.clientperformance.WebExtConnectionHandler;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.WebDriverActionDirector;

/**
 * An extended {@link FirefoxDriver} which allows to record data about requests and browser events or to run Firefox
 * with a virtual display.
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
 * xlt.webDriver.firefox_clientperformance.screenless = true
 * </pre>
 * 
 * Note that for the headless mode to work, the <code>xvfb</code> binary must be installed on the target machine and
 * must be located in one of the directories listed in the PATH environment variable. If this is not the case, the
 * browser will be run with the default display.
 */
public final class XltFirefoxDriver extends FirefoxDriver
{
    /**
     * The XLT property domain used to configure all settings specific to our client-performance Firefox WebDriver.
     */
    private static final String PROPERTY_DOMAIN = "xlt.webDriver.firefox_clientperformance.";

    /**
     * The XLT property to enable headless mode if it is available at all.
     */
    private static final String PROPERTY_HEADLESS = PROPERTY_DOMAIN + "screenless";

    /**
     * The XLT property to enable recording of incomplete/aborted requests.
     */
    private static final String PROPERTY_RECORD_INCOMPLETE = PROPERTY_DOMAIN + "recordIncomplete";

    /**
     * The XLT property to enable override of HTTP response timeout in Firefox profile used for test.
     */
    private static final String PROPERTY_RESPONSE_TIMEOUT = PROPERTY_DOMAIN + "overrideResponseTimeout";

    /**
     * Whether headless mode is enabled.
     */
    private static final boolean HEADLESS_ENABLED;

    /**
     * Whether recording of incomplete/aborted requests is enabled.
     */
    private static final boolean RECORD_INCOMPLETE_ENABLED;

    /**
     * Whether HTTP response timeout should be configured for the used Firefox profile.
     */
    private static final boolean OVERRIDE_RESPONSE_TIMEOUT;

    /**
     * Class logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(XltFirefoxDriver.class);

    /**
     * Name of {@link DriverService}'s instance field that references the environment variable mapping.
     */
    private static final String FIELD_NAME_ENVIRONMENT = "environment";

    /**
     * Name of {@link DriverCommandExecutor}'s instance field that references the driver service.
     */
    private static final String FIELD_NAME_SERVICE = "service";

    /**
     * Name of capability used to pass headless setting temporarily from c'tor to
     * {@link #startClient(Capabilities, Capabilities)}.
     */
    private static final String HEADLESS_CAPABILITY = "xlt:headless";

    /**
     * Name of timerrecorder web-extension for Firefox.
     */
    private static final String EXTENSION_FILE_NAME = "xlt-timerrecorder";

    /**
     * File suffix of timerrecorder web-extension.
     */
    private static final String EXTENSION_FILE_ENDING = ".xpi";

    /**
     * Timerrecorder web-extension for Firefox copied from classpath to be used by all client-performance FirefoxDrivers
     * started by this VM.
     */
    private static File extensionFile;

    static
    {
        final XltProperties props = XltProperties.getInstance();
        HEADLESS_ENABLED = props.getProperty(PROPERTY_HEADLESS, false);
        RECORD_INCOMPLETE_ENABLED = props.getProperty(PROPERTY_RECORD_INCOMPLETE, false);
        OVERRIDE_RESPONSE_TIMEOUT = props.getProperty(PROPERTY_RESPONSE_TIMEOUT, false);

        // copy the extension file to the temp directory
        try
        {
            final File tmpFile = File.createTempFile(EXTENSION_FILE_NAME, EXTENSION_FILE_ENDING);
            tmpFile.deleteOnExit();

            final URL extensionUrl = ClientPerformanceUtils.class.getResource(EXTENSION_FILE_NAME + EXTENSION_FILE_ENDING);
            if (extensionUrl == null)
            {
                LOG.error("Failed to locate Firefox extension file in class path");
            }
            else
            {
                FileUtils.copyURLToFile(extensionUrl, tmpFile);

                extensionFile = tmpFile;
            }
        }
        catch (final Exception e)
        {
            LOG.error("Failed to copy Firefox extension to temp folder", e);
        }
    }

    /**
     * Extension communication handler.
     */
    private final WebExtConnectionHandler connectionHandler = WebExtConnectionHandler.newInstance(PROPERTY_DOMAIN);

    /**
     * Creates a new {@link XltFirefoxDriver} instance with default settings.
     */
    public XltFirefoxDriver()
    {
        this((FirefoxOptions) null, HEADLESS_ENABLED);
    }

    /**
     * Creates a new {@link XltFirefoxDriver} instance with the given parameters and otherwise default settings.
     * 
     * @param options
     *            the driver options (may be <code>null</code>)
     */
    public XltFirefoxDriver(final FirefoxOptions options)
    {
        this(options, HEADLESS_ENABLED);
    }

    /**
     * Creates a new {@link XltFirefoxDriver} instance with the given parameters.
     * 
     * @param options
     *            the driver options (may be <code>null</code>)
     * @param screenless
     *            whether to run the browser in screenless mode (overrides the {@value #PROPERTY_HEADLESS} setting in
     *            the configuration)
     */
    public XltFirefoxDriver(final FirefoxOptions options, final boolean screenless)
    {
        super(modifyOptions(options, screenless));
        init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startSession(final Capabilities desiredCapabilities)
    {
        // get headless setting passed to c'tor and drop it from caps
        final boolean headless = desiredCapabilities.is(HEADLESS_CAPABILITY);
        final Capabilities caps = dropHeadlessCap(desiredCapabilities);

        final DriverCommandExecutor e = (DriverCommandExecutor) getCommandExecutor();
        final DriverService service = ReflectionUtils.readInstanceField(e, FIELD_NAME_SERVICE);

        // honor headless mode (add DISPLAY variable to driver-service' environment if necessary)
        modifyService(service, headless);

        super.startSession(caps);
    }

    private void init()
    {
        try
        {
            connectionHandler.start();
        }
        catch (CommunicationException e)
        {
            throw new WebDriverException("Starting extension communication failed", e);
        }
        get("data:,xltParameters?xltPort=" + connectionHandler.getPort() + "&clientID=" + connectionHandler.getID() +
            "&recordIncompleted=" + RECORD_INCOMPLETE_ENABLED);
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
        catch (final Throwable t)
        {
            LOG.warn("Failed to quit driver", t);
        }
        finally
        {
            LOG.debug("Closing driver");
            super.quit();

            LOG.debug("Firefox client performance driver closed");
        }
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
            LOG.debug("Fetch and dump remaining client-performance metrics");
            connectionHandler.reportRemainingPerformanceData();
        }
    }

    private boolean hasWindow()
    {
        try
        {
            return !getWindowHandles().isEmpty();
        }
        catch (final Throwable t)
        {
            return false;
        }
    }

    /**
     * Checks whether the driver is still connected with the browser.
     */
    private boolean isConnected()
    {
        return getSessionId() != null;
    }

    /**
     * Drops the headless setting from the given capabilities.
     */
    private static Capabilities dropHeadlessCap(final Capabilities caps)
    {
        return new MutableCapabilities(Maps.filterKeys(caps.asMap(), (cap) -> !HEADLESS_CAPABILITY.equals(cap)));
    }

    /**
     * Modifies the given options for client-performance measurements and headless operation.
     * 
     * @param options
     *            the driver options (may be <code>null</code>)
     * @param screenless
     *            whether to run the browser in screenless mode
     * @return the modified driver options
     */
    private static FirefoxOptions modifyOptions(FirefoxOptions options, final boolean screenless)
    {
        options = ObjectUtils.defaultIfNull(options, new FirefoxOptions());

        // inject our extension and profile prefs
        options.setProfile(modifyProfile(options.getProfile()));
        // inject headless setting -> will be removed by 'dropHeadlessCap' later on
        options.setCapability(HEADLESS_CAPABILITY, screenless);

        return options;
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
    private static DriverService modifyService(final DriverService service, final boolean headless)
    {
        // modify the service's environment for headless mode
        if (service != null && headless)
        {
            final String display = ClientPerformanceUtils.getDisplay();
            if (display != null)
            {
                /*
                 * HACK: we can access the service's environment settings via reflection only
                 */

                // read the environment settings
                final ImmutableMap<String, String> environment = ReflectionUtils.readField(DriverService.class, service,
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
                ReflectionUtils.writeField(DriverService.class, service, FIELD_NAME_ENVIRONMENT, newEnvironment);
            }
        }

        return service;
    }

    /**
     * Modifies the given profile for client-performance measurements.
     *
     * @param profile
     *            the Firefox profile to prepare for use in client-performance tests
     * @return the modified profile
     */
    private static FirefoxProfile modifyProfile(FirefoxProfile profile)
    {
        // check if the extension file is (still) available
        if (extensionFile == null || !extensionFile.isFile())
        {
            throw new WebDriverException("Firefox client-performance extension not available (path: " + extensionFile + ")");
        }

        profile = ObjectUtils.defaultIfNull(profile, new FirefoxProfile());
        profile.addExtension(EXTENSION_FILE_NAME, new FileExtension(extensionFile));

        // Always accept untrusted certificates
        profile.setAcceptUntrustedCertificates(true);

        // Disable auto-update of Firefox
        profile.setPreference("app.update.enabled", false);
        // Disable auto-update of FF extensions
        profile.setPreference("extensions.update.enabled", false);

        // Override response timeout if configured to do so
        if (OVERRIDE_RESPONSE_TIMEOUT)
        {
            final int timeoutInSeconds = getGlobalTimeoutInSeconds();
            profile.setPreference("network.http.response.timeout", timeoutInSeconds);

            profile.setPreference("network.http.tcp_keepalive.short_lived_connections", false);
            profile.setPreference("network.http.tcp_keepalive.long_lived_connections", false);
        }

        return profile;
    }

    /**
     * Reads in the global (connect/read) timeout and returns its value in seconds.
     * <p>
     * In case the value is zero or negative, an exception will be thrown.
     * </p>
     * 
     * @return the value of the global timeout in seconds (rounded to next second)
     */
    private static int getGlobalTimeoutInSeconds()
    {
        final String timeoutProp = XltConstants.XLT_PACKAGE_PATH + ".timeout";
        final long to = XltProperties.getInstance().getProperty(timeoutProp, 10000L);
        if (to <= 0)
        {
            throw new WebDriverException(String.format("Value '%d' of property '%s' must be specified as positive integer", to,
                                                       timeoutProp));
        }

        int toInSec = (int) (to / 1000L);
        if (to % 1000 > 0)
        {
            ++toInSec;

            if (LOG.isWarnEnabled())
            {
                LOG.warn(String.format("Global timeout value of '%dms' will be rounded to the next second", to));
            }
        }

        if (LOG.isInfoEnabled())
        {
            LOG.info(String.format("Will use '%ds' as response timeout for Firefox", toInSec));
        }

        return toInSec;
    }

    /**
     * Returns a {@link Builder} object to create a new {@link XltFirefoxDriver} instance.
     *
     * @return the builder
     */
    public static Builder xltBuilder()
    {
        return new Builder();
    }

    /**
     * Builder class to create {@link XltFirefoxDriver} instances. First set the desired properties and then call
     * {@link #build()} to get the configured driver instance.
     */
    public static final class Builder
    {
        private FirefoxBinary binary;

        private FirefoxProfile profile;

        private boolean headless = HEADLESS_ENABLED;

        private FirefoxOptions options;

        /**
         * Sets the desired binary and clears the GeckoDriver service setting.
         *
         * @param binary
         *            the binary
         * @return this builder instance
         */
        public Builder setBinary(final FirefoxBinary binary)
        {
            this.binary = binary;
            return this;
        }

        /**
         * Sets the desired profile.
         *
         * @param profile
         *            the profile
         * @return this builder instance
         */
        public Builder setProfile(final FirefoxProfile profile)
        {
            this.profile = profile;
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
         * Creates a new {@link XltFirefoxDriver} instance configured with all the previously set properties.
         *
         * @return the driver instance
         */
        public XltFirefoxDriver build()
        {
            final FirefoxOptions opts = ObjectUtils.defaultIfNull(this.options, new FirefoxOptions());
            if (binary != null)
            {
                opts.setBinary(binary);
            }
            if (profile != null)
            {
                opts.setProfile(profile);
            }

            return new XltFirefoxDriver(opts, headless);
        }
    }
}
