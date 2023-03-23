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
package com.xceptance.xlt.engine.xltdriver;

import org.htmlunit.BrowserVersion;
import org.htmlunit.Page;
import org.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.WebDriverActionDirector;
import com.xceptance.xlt.engine.XltWebClient;
import com.xceptance.xlt.engine.resultbrowser.RequestHistory;

/**
 * Extended version of {@link XltWebClient} for use in XLT web driver.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class WebDriverXltWebClient extends XltWebClient
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Specialization of WebDriverActionAction which uses this driver client to perform necessary routines.
     */
    private final class XltDriverActionDirector extends WebDriverActionDirector
    {
        @Override
        protected void setTimerName(final String timerName)
        {
            WebDriverXltWebClient.this.setTimerName(timerName);
        }

        @Override
        protected void doWaitForPageLoad()
        {
            // when driver has quit, do nothing
            if (!isQuit())
            {
                finishPotentialPageLoad(true);
            }
        }

        @Override
        protected void takeScreenshot()
        {
            dumpCurrentPage();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void preQuit()
        {
            // Empty
        }
    }

    /**
     * Property name for background activity waiting time.
     */
    private static final String WAITINGTIME_PROPERTY = XltConstants.XLT_PACKAGE_PATH + ".js.backgroundActivity.waitingTime";

    /**
     * The background activity waiting time.
     */
    private final long waitingTime;

    /**
     * Flag which indicates if {@link #quit()} has already been called.
     */
    private boolean quit = false;

    /**
     * Constructor.
     */
    public WebDriverXltWebClient(final BrowserVersion browserVersion)
    {
        super(browserVersion);

        // set a default timer name
        setTimerName("UnsetTimerName");

        waitingTime = XltProperties.getInstance().getProperty(WAITINGTIME_PROPERTY, -1);

        Session.getCurrent().removeShutdownListener(this);
        final WebDriverActionDirector actionDirector = new XltDriverActionDirector();

        ((SessionImpl) Session.getCurrent()).setWebDriverActionDirector(actionDirector);
    }

    /**
     * Quits this client due to quit of the embedding webdriver.
     */
    public void quit()
    {
        if (!quit)
        {
            quit = true;
            shutdown();
        }
    }

    public boolean isQuit()
    {
        return quit;
    }

    /**
     * Finishes a potential page load which consists of the following steps:
     * <ul>
     * <li>Check if current thread has been interrupted</li>
     * <li>Optionally wait for background JavaScript jobs</li>
     * <li>Load new static content</li>
     * <li>Dump HTML page</li>
     * <li>Clear network data manager</li>
     * </ul>
     * 
     * @param waitForBackgroundJSJobs
     *            whether or not to wait for background JavaScript jobs to be finished
     */
    private void finishPotentialPageLoad(final boolean waitForBackgroundJSJobs)
    {
        final SessionImpl session = SessionImpl.getCurrent();

        // check for interruption
        session.checkState();

        Page page = getCurrentWindow().getTopWindow().getEnclosedPage();

        if (waitForBackgroundJSJobs)
        {
            // wait for any background thread to finish
            waitForBackgroundThreads(page, waitingTime);

            // something might have changed caused by background jobs, including a reload via location
            page = getCurrentWindow().getTopWindow().getEnclosedPage();
        }

        // check for any new static content to load
        HtmlPage htmlPage = null;
        if (page instanceof HtmlPage)
        {
            htmlPage = (HtmlPage) page;
            loadNewStaticContent(htmlPage);
        }
    }

    /**
     * Adds the HTML page of the current top-level window to the request history.
     */
    private void dumpCurrentPage()
    {
        final SessionImpl session = SessionImpl.getCurrent();
        final RequestHistory requestHistory = session.getRequestHistory();
        final String timerName = getTimerName();
        final Page page = getCurrentWindow().getTopWindow().getEnclosedPage();

        // log the page
        if (page instanceof HtmlPage)
        {
            requestHistory.add(timerName, (HtmlPage) page);
        }
        else
        {
            // dump at least an empty page to let the action appear in the result browser
            requestHistory.add(timerName);
        }

        session.getNetworkDataManager().clear();
    }
}
