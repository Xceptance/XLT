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
package com.xceptance.xlt.engine.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.io.TemporaryFilesystem;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaDriverService;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.safari.SafariDriver;

import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.webdriver.XltChromeDriver;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.webdriver.XltFirefoxDriver;

/**
 * Simple factory that creates {@link WebDriver} instances as configured in the XLT settings.
 */
public class DefaultWebDriverFactory
{
    /**
     * The supported web drivers. Do NOT rename as these names are used as such in the XLT properties.
     */
    private enum WebDriverType
    {
     chrome,
     firefox,
     ie,
     safari,
     xlt,
     firefox_clientperformance,
     chrome_clientperformance,
     edge,
     opera
    }

    /**
     * The prefix of all factory-related configuration settings.
     */
    private static final String PROP_PREFIX_WEB_DRIVER = "xlt.webDriver";

    /**
     * Matches browser arguments. Both quoted and unquoted arguments are supported. Unquoted arguments cannot contain
     * whitespace. For quoted arguments, either single quotes or double quotes can be used.
     */
    private static final Pattern BROWSER_ARGS_PATTERN = Pattern.compile("\"(.*?)\"|'(.*?)'|\\S+");

    /**
     * The {@link WebDriver} instances to reuse for further tests. The map key is a compound value built from the
     * respective {@link WebDriverType} and the ID of the current thread.
     */
    private static final ConcurrentHashMap<String, WebDriver> reusableWebDrivers = new ConcurrentHashMap<>();

    static
    {
        // remove Selenium's shutdown hook as it would run in parallel with ours
        // (see https://github.com/SeleniumHQ/selenium/issues/950 for more info)
        final TemporaryFilesystem tempFS = TemporaryFilesystem.getDefaultTmpFS();
        final Thread seleniumShutdownHook = ReflectionUtils.readInstanceField(tempFS, "shutdownHook");
        Runtime.getRuntime().removeShutdownHook(seleniumShutdownHook);

        // register a task to quit any reusable driver when the JVM quits
        Runtime.getRuntime().addShutdownHook(new Thread(DefaultWebDriverFactory.class.getSimpleName() + "-shutdown")
        {
            @Override
            public void run()
            {
                for (final WebDriver reusableWebDriver : reusableWebDrivers.values())
                {
                    // quit the wrapped driver
                    try
                    {
                        ((WrapsDriver) reusableWebDriver).getWrappedDriver().quit();
                    }
                    catch (final Exception e)
                    {
                        // ignore, continue with next driver
                    }
                }

                // only now can we run Selenium's shutdown hook
                seleniumShutdownHook.run();
            }
        });
    }

    /**
     * Creates a new {@link WebDriver} instance.
     * 
     * @param webDriverType
     *            the type of web driver to create
     * @param pathToDriverServer
     *            the path to the driver server if the driver requires one (may be blank)
     * @param pathToBrowser
     *            the path to the browser binary
     * @param browserArgs
     *            additional browser command line arguments
     * @param pageLoadStrategy
     *            the page load strategy to use (may be blank)
     * @return the new {@link WebDriver} instance
     */
    public static WebDriver createWebDriver(final WebDriverType webDriverType, final String pathToDriverServer, final String pathToBrowser,
                                            final String browserArgs, final String pageLoadStrategy)
    {
        final WebDriver webDriver;

        // create the respective driver
        if (webDriverType == WebDriverType.chrome || webDriverType == WebDriverType.chrome_clientperformance)
        {
            setPathToDriverServer(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, pathToDriverServer);

            final ChromeOptions options = createChromeOptions(pathToBrowser, browserArgs, pageLoadStrategy);

            webDriver = (webDriverType == WebDriverType.chrome) ? new ChromeDriver(options) : new XltChromeDriver(options);
        }
        else if (webDriverType == WebDriverType.firefox || webDriverType == WebDriverType.firefox_clientperformance)
        {
            setPathToDriverServer(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY, pathToDriverServer);

            final FirefoxOptions options = createFirefoxOptions(pathToBrowser, browserArgs, pageLoadStrategy);

            webDriver = (webDriverType == WebDriverType.firefox) ? new FirefoxDriver(options) : new XltFirefoxDriver(options);
        }
        else if (webDriverType == WebDriverType.ie)
        {
            setPathToDriverServer(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY, pathToDriverServer);

            webDriver = new InternetExplorerDriver();
        }
        else if (webDriverType == WebDriverType.edge)
        {
            setPathToDriverServer(EdgeDriverService.EDGE_DRIVER_EXE_PROPERTY, pathToDriverServer);

            webDriver = new EdgeDriver();
        }
        else if (webDriverType == WebDriverType.opera)
        {
            setPathToDriverServer(OperaDriverService.OPERA_DRIVER_EXE_PROPERTY, pathToDriverServer);
            final OperaOptions options = createOperaOptions(pathToBrowser, browserArgs);

            webDriver = new OperaDriver(options);
        }
        else if (webDriverType == WebDriverType.safari)
        {
            webDriver = new SafariDriver();
        }
        else
        {
            webDriver = new XltDriver();
        }

        return webDriver;
    }

    /**
     * Sets the path to the driver server in the system environment, but only if the path is not blank.
     * 
     * @param propertyName
     *            the name of the system property
     * @param path
     *            the path
     */
    private static void setPathToDriverServer(final String propertyName, final String path)
    {
        if (StringUtils.isNotBlank(path))
        {
            System.setProperty(propertyName, path);
        }
    }

    /**
     * Creates a {@link ChromeOptions} object and sets the path, but only if the path is not blank.
     * 
     * @param pathToBrowser
     *            the path to the browser binary
     * @param browserArgs
     *            additional browser command line arguments
     * @param pageLoadStrategy
     *            the page loading strategy
     * @return the Chrome options
     */
    private static ChromeOptions createChromeOptions(final String pathToBrowser, final String browserArgs, String pageLoadStrategy)
    {
        final ChromeOptions options = new ChromeOptions();

        if (StringUtils.isNotBlank(pathToBrowser))
        {
            options.setBinary(pathToBrowser);
        }

        if (StringUtils.isNotBlank(browserArgs))
        {
            final List<String> args = parseBrowserArgs(browserArgs);
            options.addArguments(args);
        }

        if (StringUtils.isNotBlank(pageLoadStrategy))
        {
            options.setPageLoadStrategy(PageLoadStrategy.fromString(pageLoadStrategy));
        }

        return options;
    }

    /**
     * Parses a line of quoted or unquoted browser arguments into a list of separate arguments. Unless quoted, the input
     * is split on whitespace characters. Both single and double quotes may be used as quoting character, but in pairs
     * only. The quotes around quoted arguments are removed.
     * 
     * <pre>
     * browserArgs: -a --b c "--d=foo bar" 'baz bum' 
     *      result: [-a, --b, c, --d=foo bar, baz bum ]
     * </pre>
     * 
     * @param browserArgs
     *            the browser arguments as a single string
     * @return the list of parsed browser arguments
     */
    static List<String> parseBrowserArgs(final String browserArgs)
    {
        final List<String> args = new ArrayList<>();

        final Matcher matcher = BROWSER_ARGS_PATTERN.matcher(browserArgs);
        while (matcher.find())
        {
            // (1) get the content of a double-quoted argument
            String arg = matcher.group(1);
            if (arg == null)
            {
                // (2) get the content of a single-quoted argument
                arg = matcher.group(2);
                if (arg == null)
                {
                    // (3) get the full argument text
                    arg = matcher.group();
                }
            }

            args.add(arg);
        }

        return args;
    }

    /**
     * Creates a {@link OperaOptions} object and sets the path, but only if the path is not blank.
     * 
     * @param pathToBrowser
     *            the path to the browser binary
     * @param browserArgs
     *            additional browser command line arguments
     * @return the Opera options
     */
    private static OperaOptions createOperaOptions(final String pathToBrowser, final String browserArgs)
    {
        final OperaOptions options = new OperaOptions();

        if (StringUtils.isNotBlank(pathToBrowser))
        {
            options.setBinary(pathToBrowser);
        }

        if (StringUtils.isNotBlank(browserArgs))
        {
            final String[] args = StringUtils.split(browserArgs);
            options.addArguments(args);
        }

        return options;
    }

    /**
     * Creates a {@link FirefoxOptions} object and sets the path to the browser's binary, but only if the path is not
     * blank.
     * 
     * @param pathToBrowser
     *            the path to the browser binary
     * @param browserArgs
     *            additional browser command line arguments
     * @param pageLoadStrategy
     *            the page loading strategy
     * @return the Firefox options
     */
    private static FirefoxOptions createFirefoxOptions(final String pathToBrowser, final String browserArgs, String pageLoadStrategy)
    {
        final FirefoxOptions options = new FirefoxOptions();

        if (StringUtils.isNotBlank(pathToBrowser))
        {
            options.setBinary(pathToBrowser);
        }

        if (StringUtils.isNotBlank(browserArgs))
        {
            final String[] args = StringUtils.split(browserArgs);
            options.addArguments(args);
        }

        if (StringUtils.isNotBlank(pageLoadStrategy))
        {
            options.setPageLoadStrategy(PageLoadStrategy.fromString(pageLoadStrategy));
        }

        return options;
    }

    /**
     * Creates either a new {@link WebDriver} instance or returns a pre-created singleton instance to be reused.
     * 
     * @return the {@link WebDriver} instance
     */
    public static WebDriver getWebDriver()
    {
        // Note: always look up the properties freshly to get test-case-specific settings

        final XltProperties props = XltProperties.getInstance();

        // determine the configured driver type
        WebDriverType webDriverType;
        final String driverTypeName = props.getProperty(PROP_PREFIX_WEB_DRIVER, WebDriverType.xlt.name());
        try
        {
            webDriverType = WebDriverType.valueOf(driverTypeName.toLowerCase());
        }
        catch (final IllegalArgumentException e)
        {
            XltLogger.runTimeLogger.warn("The configured WebDriver type '" + driverTypeName + "' is unknown. Falling back to 'xlt'.");
            webDriverType = WebDriverType.xlt;
        }

        // get the web-driver-specific path to the browser
        final String pathToBrowser = props.getProperty(PROP_PREFIX_WEB_DRIVER + "." + webDriverType.name() + ".pathToBrowser", null);

        // get the web-driver-specific browser command line arguments
        final String browserArgs = props.getProperty(PROP_PREFIX_WEB_DRIVER + "." + webDriverType.name() + ".browserArgs", null);

        // get the web-driver-specific path to the driver server
        final String pathToDriverServer = props.getProperty(PROP_PREFIX_WEB_DRIVER + "." + webDriverType.name() + ".pathToDriverServer",
                                                            null);

        // get the web-driver-specific page load strategy
        final String pageLoadStrategy = props.getProperty(PROP_PREFIX_WEB_DRIVER + "." + webDriverType.name() + ".pageLoadStrategy", null);

        // whether we shall reuse the driver (XltDriver instances will always be created freshly)
        final boolean reuseDriver = webDriverType != WebDriverType.xlt && props.getProperty(PROP_PREFIX_WEB_DRIVER + ".reuseDriver", false);

        // get/create the driver
        final WebDriver webDriver;
        if (reuseDriver)
        {
            // get/create the reusable driver for the current thread
            webDriver = getOrCreateReusableWebDriver(webDriverType, pathToDriverServer, pathToBrowser, browserArgs, pageLoadStrategy);
        }
        else
        {
            // always create a fresh driver
            webDriver = createWebDriver(webDriverType, pathToDriverServer, pathToBrowser, browserArgs, pageLoadStrategy);
        }

        // get the configured size of browser window and whether it should be maximized
        final int windowWidth = props.getProperty(PROP_PREFIX_WEB_DRIVER + ".window.width", -1);
        final int windowHeight = props.getProperty(PROP_PREFIX_WEB_DRIVER + ".window.height", -1);
        final boolean maximizeWindow = props.getProperty(PROP_PREFIX_WEB_DRIVER + ".window.maximize", false);

        // maximize window
        if (maximizeWindow)
        {
            webDriver.manage().window().maximize();
        }
        // resize browser window iff sizes are defined
        else if (windowWidth > 0 && windowHeight > 0)
        {
            final Dimension windowSize = new Dimension(windowWidth, windowHeight);
            webDriver.manage().window().setSize(windowSize);
        }
        // log actual size of browser window to runtime logger
        logWindowSize(webDriver);

        return webDriver;
    }

    /**
     * Logs the actual size of the browser window to the runtime logger at level INFO.
     * 
     * @param webDriver
     *            the {@link WebDriver} instance
     */
    private static void logWindowSize(final WebDriver webDriver)
    {
        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            final Dimension windowDim = webDriver.manage().window().getSize();
            XltLogger.runTimeLogger.info("Size of browser window: " + windowDim.getWidth() + " x " + windowDim.getHeight());
        }
    }

    /**
     * Returns a {@link WebDriver} instance that is exclusive to the current thread, but can be reused for multiple
     * tests. If such a driver does not yet exist for the current thread, it will be created.
     * 
     * @param webDriverType
     *            the type of web driver to create
     * @param pathToDriverServer
     *            the path to the driver server if the driver requires one (may be blank)
     * @param pathToBrowser
     *            the path to the browser binary
     * @param browserArgs
     *            additional browser command line arguments
     * @param pageLoadStrategy
     *            the page load strategy to use (may be blank)
     * @return the reusable {@link WebDriver} instance
     */
    public static WebDriver getOrCreateReusableWebDriver(final WebDriverType webDriverType, final String pathToDriverServer,
                                                         final String pathToBrowser, final String browserArgs,
                                                         final String pageLoadStrategy)
    {
        final String key = webDriverType.name() + Thread.currentThread().getId();

        WebDriver webDriver = reusableWebDrivers.get(key);
        if (webDriver == null)
        {
            // get/create a fresh driver that cannot be quit
            webDriver = createWebDriver(webDriverType, pathToDriverServer, pathToBrowser, browserArgs, pageLoadStrategy);
            webDriver = makeDriverUnquittable(webDriver);
            reusableWebDrivers.put(key, webDriver);
        }
        else
        {
            // clean up the driver before next use
            resetWebDriver(webDriver);
        }

        return webDriver;
    }

    /**
     * Does a best effort to reset the passed {@link WebDriver} instance.
     * 
     * @param webDriver
     *            the web driver to reset
     */
    private static void resetWebDriver(final WebDriver webDriver)
    {
        // remove all cookies, but for the current domain only :-(
        webDriver.manage().deleteAllCookies();

        // close any but the current window if there are any
        final Set<String> windows = webDriver.getWindowHandles();
        if (windows.size() > 1)
        {
            // remember the current window
            final String currentWindow = webDriver.getWindowHandle();

            // close all other windows
            for (final String window : windows)
            {
                if (!window.equals(currentWindow))
                {
                    webDriver.switchTo().window(window);
                    webDriver.close();
                }
            }

            // return to the current window
            webDriver.switchTo().window(currentWindow);
        }
    }

    /**
     * Creates a proxy object that implements all interfaces of the argument's class and that delegates all calls to it
     * except of {@link WebDriver#close()} and {@link WebDriver#quit()}. Furthermore, it is forced to implement
     * {@link WrapsDriver} which can be used to retrieve the delegate.
     * 
     * @param webDriver
     *            the web-driver instance to delegate calls to
     * @return un-quittable web-driver that delegates all calls (except of {@link WebDriver#close()} and
     *         {@link WebDriver#quit()}) to the web-driver instance passed as parameter
     */
    private static WebDriver makeDriverUnquittable(final WebDriver webDriver)
    {
        final InvocationHandler handler = new InvocationHandler()
        {
            private final WebDriver wrappedDriver = webDriver;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
            {
                final String methodName = method.getName();

                // prevent any method call that would cause the browser to quit
                if (methodName.equals("quit") || (methodName.equals("close") && wrappedDriver.getWindowHandles().size() <= 1))
                {
                    return null;
                }
                // handle access to the wrapped driver
                else if (methodName.equals("getWrappedDriver"))
                {
                    return wrappedDriver;
                }
                // otherwise invoke the method on the wrapped driver
                else
                {
                    try
                    {
                        return method.invoke(wrappedDriver, args);
                    }
                    catch (final InvocationTargetException e)
                    {
                        // unwrap and throw causing throwable
                        throw e.getCause();
                    }
                }
            }
        };

        final List<Class<?>> ifaces = ClassUtils.getAllInterfaces(webDriver.getClass());

        final Class<?>[] proxyIFaces = new Class<?>[ifaces.size() + 1];
        proxyIFaces[0] = WrapsDriver.class;

        int i = 1;
        for (final Class<?> clazz : ifaces)
        {
            proxyIFaces[i++] = clazz;
        }

        return (WebDriver) Proxy.newProxyInstance(DefaultWebDriverFactory.class.getClassLoader(), proxyIFaces, handler);
    }
}
