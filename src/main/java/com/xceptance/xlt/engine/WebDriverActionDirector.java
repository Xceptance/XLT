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
package com.xceptance.xlt.engine;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WrapsDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.tests.AbstractWebDriverTestCase;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.api.webdriver.XltChromeDriver;
import com.xceptance.xlt.api.webdriver.XltFirefoxDriver;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.resultbrowser.RequestHistory;
import com.xceptance.xlt.engine.resultbrowser.RequestHistory.DumpMode;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * Controls the lifecycle of an action when running tests using a webdriver.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class WebDriverActionDirector
{
    private static final Logger LOG = LoggerFactory.getLogger(WebDriverActionDirector.class);

    /**
     * Property name for think time.
     */
    private static final String THINKTIME_PROPERTY = XltConstants.XLT_PACKAGE_PATH + ".thinktime.action";

    /**
     * Property name for think time deviation.
     */
    private static final String THINKTIMEDEVIATION_PROPERTY = XltConstants.XLT_PACKAGE_PATH + ".thinktime.action.deviation";

    /**
     * The "no screenshot available" screenshot used when taking the screenshot is not supported or fails for any
     * reason.
     */
    private static final byte[] dummyScreenshot = loadDummyScreenshot();

    /**
     * The think time.
     */
    private final int thinkTime;

    /**
     * The think time deviation.
     */
    private final int thinkTimeDeviation;

    /**
     * The information about the current action.
     */
    private ActionData actionData;

    /**
     * The time the action was started.
     */
    private long actionStartTime;

    /**
     * Whether we are still in the first action.
     */
    private boolean isFirstAction = true;

    private WeakReference<WebDriver> webDriver;

    /**
     * Constructor.
     */
    public WebDriverActionDirector()
    {
        // get the default think time parameters from the configuration
        thinkTime = XltProperties.getInstance().getProperty(THINKTIME_PROPERTY, 0);
        thinkTimeDeviation = XltProperties.getInstance().getProperty(THINKTIMEDEVIATION_PROPERTY, 0);
    }

    /**
     * Dumps the measured data for the current action as well as the corresponding page to disk.
     */
    public void finishCurrentAction()
    {
        // check if there is a current action
        if (actionData != null)
        {
            doWaitForPageLoad();

            // close the action
            Session session = Session.getCurrent();

            actionData.setRunTime((int) (TimerUtils.get().getElapsedTime(actionStartTime)));
            actionData.setFailed(session.hasFailed());

            // log the action measurements
            session.getDataManager().logDataRecord(actionData);

            if (XltLogger.runTimeLogger.isInfoEnabled())
            {
                XltLogger.runTimeLogger.info(String.format("### Action '%s' finished after %d ms", actionData.getName(),
                                                           actionData.getRunTime()));
            }

            takeScreenshot();

            actionData = null;
        }
    }

    /**
     * Starts a new action using the given timer name. The current action (if any) will be finished before.
     *
     * @param timerName
     *            the new action's timer name
     */
    public void startNewAction(final String timerName)
    {
        // first finish the previous action if any
        finishCurrentAction();

        // simulate think time except for the first action
        if (isFirstAction)
        {
            isFirstAction = false;
        }
        else
        {
            doThink();
        }

        // start the new action
        actionData = new ActionData(timerName);
        actionData.setTime(GlobalClock.millis());
        actionStartTime = TimerUtils.get().getStartTime();

        setTimerName(timerName);

        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            XltLogger.runTimeLogger.info(String.format("### Action '%s' started", timerName));
        }
    }

    /**
     * Finishes the current action (if any). This method has to be called immediately before any session shutdown
     * listener is called.
     */
    public void shutdown()
    {
        if (!SessionImpl.getCurrent().wasMarkedAsExpired())
        {
            finishCurrentAction();
            preQuit();
        }
    }

    protected void preQuit()
    {
        if (webDriver != null)
        {
            WebDriver wd = webDriver.get();
            while (wd instanceof WrapsDriver)
            {
                wd = ((WrapsDriver) wd).getWrappedDriver();
            }

            if (wd instanceof XltChromeDriver || wd instanceof XltFirefoxDriver)
            {
                ReflectionUtils.callMethod(wd, "preQuit");
            }
        }
    }

    /**
     * Simulates the thinking time between actions.
     */
    protected void doThink()
    {
        final long resultingThinkTime = Math.max(0, XltRandom.nextIntWithDeviation(thinkTime, thinkTimeDeviation));
        if (resultingThinkTime > 0)
        {
            if (XltLogger.runTimeLogger.isInfoEnabled())
            {
                XltLogger.runTimeLogger.info("Executing action think time wait (" + resultingThinkTime + " ms)...");
            }

            try
            {
                Thread.sleep(resultingThinkTime);
            }
            catch (final InterruptedException ie)
            {
                throw new XltException("Sleep interrupted", ie);
            }
        }
    }

    protected void takeScreenshot()
    {
        if (webDriver == null)
        {
            webDriver = new WeakReference<WebDriver>(getDriver());
        }

        final WebDriver wd = webDriver.get();
        final RequestHistory requestHistory = SessionImpl.getCurrent().getRequestHistory();

        // taking screenshots is expensive so do not even try if not needed
        if (wd != null && requestHistory.getDumpMode() != DumpMode.NEVER)
        {
            byte[] bytes = dummyScreenshot;
            if (wd instanceof TakesScreenshot)
            {
                try
                {
                    bytes = ((TakesScreenshot) wd).getScreenshotAs(OutputType.BYTES);
                }
                catch (final Exception e)
                {
                    LOG.error("Failed to create screenshot", e);
                }
            }

            // add the screenshot, possibly an empty one
            requestHistory.add(SessionImpl.getCurrent().getCurrentActionInfo(), bytes);
        }
    }

    protected void doWaitForPageLoad()
    {
    }

    protected void setTimerName(final String timerName)
    {
    }

    private static WebDriver getDriver()
    {
        final Object instance = SessionImpl.getCurrent().getTestInstance();
        try
        {
            if (instance instanceof AbstractWebDriverTestCase)
            {
                return ((AbstractWebDriverTestCase) instance).getWebDriver();
            }
            if (instance instanceof AbstractWebDriverScriptTestCase)
            {
                return (WebDriver) ReflectionUtils.callMethod(AbstractWebDriverScriptTestCase.class, instance, "getWebDriver");
            }
        }
        catch (final Throwable t)
        {
            LOG.error("Failed to obtain webdriver", t);
        }

        return null;
    }

    /**
     * Loads the dummy screenshot PNG image from the class path.
     *
     * @return the image bytes
     */
    private static synchronized byte[] loadDummyScreenshot()
    {
        try
        {
            final URL resourceUrl = WebDriverActionDirector.class.getResource("DummyScreenshot.png");

            return IOUtils.toByteArray(resourceUrl);
        }
        catch (final IOException | NullPointerException e)
        {
            LOG.error("Failed to load dummy screenshot", e);

            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
    }
}
