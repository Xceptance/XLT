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
package com.xceptance.xlt.engine.resultbrowser;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.html.HtmlPage;

import com.xceptance.common.collection.LRUList;
import com.xceptance.common.lang.ParseNumbers;
import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.SessionImpl;

/**
 * The RequestHistory stores all the requests that have been processed so far in a session. This includes page requests
 * as well as static content requests. On request, the requests may be dumped to the file system.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class RequestHistory
{
    /**
     * The property for output to disk
     */
    public static final String OUTPUT2DISK_PROPERTY = XltConstants.XLT_PACKAGE_PATH + ".output2disk";

    /**
     * The property for HAR export.
     */
    private static final String OUTPUT2DISK_WRITEHAR_PROPERTY = OUTPUT2DISK_PROPERTY + ".writeHarFile";

    /**
     * The property for the size of the output to disk
     */
    private static final String OUTPUT2DISK_SIZE_PROPERTY = OUTPUT2DISK_PROPERTY + ".size";

    /**
     * The property level for the output to disk error properties
     */
    public static final String OUTPUT2DISK_ERROR_PROPERTY = OUTPUT2DISK_PROPERTY + ".onError";


    /**
     * The possible dump mode values.
     */
    public enum DumpMode
    {
     NEVER,
     ON_ERROR,
     ALWAYS;

        /**
         * Returns the dumpMode according to the argument string. Will return {@link DumpMode#ALWAYS} if none of the
         * expected strings is matched. Note that this implementation differs from the default implementation of
         * {@link Enum#valueOf(Class, String)}!

         * @return the DumpMode according to the argument string, won't return <code>null</code>
         */
        public static DumpMode valueFrom(final String propertyValue)
        {
            if ("never".equals(propertyValue))
            {
                return DumpMode.NEVER;
            }
            else if ("onError".equals(propertyValue) || "onErrors".equals(propertyValue))
            {
                return DumpMode.ON_ERROR;
            }
            else
            {
                return DumpMode.ALWAYS;
            }
        }
    }

    /**
     * The list of HTML pages, which have not been dumped so far.
     */
    private final LRUList<Page> pages;

    /**
     * The list of requests, for which a corresponding HTML page not yet exists. Once such a page is added to the
     * request history, any pending request is attached to this page.
     */
    private final List<Request> pendingRequests;

    /**
     * The configured dump mode.
     */
    private DumpMode dumpMode;

    /**
     * The dumping manager.
     */
    private DumpMgr dumpMgr;

    /**
     * We keep the session this history is running with
     */
    private final SessionImpl session;

    /**
     * Creates a new RequestHistory object.
     */
    public RequestHistory(final SessionImpl session, final XltProperties properties)
    {
        this.session = session;

        int historySize = properties.getProperty(session, OUTPUT2DISK_SIZE_PROPERTY).flatMap(ParseNumbers::parseOptionalInt).orElse(3);
        if (historySize < 1)
        {
            XltLogger.runTimeLogger.warn(OUTPUT2DISK_SIZE_PROPERTY + " must be larger than 1, setting 3 as default now.");
            historySize = 3;
        }

        pages = new LRUList<Page>(historySize);
        pendingRequests = new LinkedList<Request>();

        // get dump mode
        final String dumpModeValue = properties.getProperty(session, OUTPUT2DISK_PROPERTY).orElse("onError");

        dumpMode = DumpMode.valueFrom(dumpModeValue);

        dumpMgr = new DumpMgr();
        dumpMgr.setHarExportEnabled(properties.getProperty(session, OUTPUT2DISK_WRITEHAR_PROPERTY).map(Boolean::valueOf).orElse(false));
    }

    /**
     * Adds the given request/response information to the internal in-memory request list if the current dump mode is
     * {@link DumpMode#ON_ERROR}. Otherwise the values are either ignored ({@link DumpMode#NEVER}) or dumped immediately
     * ({@link DumpMode#ALWAYS}).
     *
     * @param name
     *            the request name
     * @param webRequest
     *            the request settings
     * @param webResponse
     *            the response
     * @param requestData
     *            the request data
     */
    public synchronized void add(final String name, final WebRequest webRequest, final WebResponse webResponse,
                                 final RequestData requestData)
    {
        ParameterCheckUtils.isNotNull(name, "name");
        ParameterCheckUtils.isNotNull(webRequest, "webRequestSettings");

        if (dumpMode == DumpMode.NEVER)
        {
            // do nothing
        }
        else if (dumpMode == DumpMode.ON_ERROR)
        {
            // add a new pending request
            pendingRequests.add(new Request(name, webRequest, webResponse, requestData));
        }
        else if (dumpMode == DumpMode.ALWAYS)
        {
            // immediately dump it, no need to keep it in memory
            dumpMgr.dump(new Request(name, webRequest, webResponse, requestData));
        }
    }

    /**
     * Adds the given lightweight page to the internal in-memory page list if the current dump mode is
     * {@link DumpMode#ON_ERROR}. Otherwise the page is either ignored ({@link DumpMode#NEVER}) or dumped immediately (
     * {@link DumpMode#ALWAYS}).
     *
     * @param lwPage
     *            the lightweight page
     */
    public void add(final LightWeightPage lwPage)
    {
        add(() -> new Page(lwPage.getTimerName(), lwPage));
    }

    /**
     * Adds the given HTML page to the internal in-memory page list if the current dump mode is
     * {@link DumpMode#ON_ERROR}. Otherwise the page is either ignored ({@link DumpMode#NEVER}) or dumped immediately (
     * {@link DumpMode#ALWAYS}).
     *
     * @param name
     *            the page's name
     * @param htmlPage
     *            the page
     */
    public void add(final String name, final HtmlPage htmlPage)
    {
        ParameterCheckUtils.isNotNull(name, "name");

        add(() -> new Page(name, htmlPage));
    }

    /**
     * Adds the given screenshot page to the internal in-memory page list if the current dump mode is
     * {@link DumpMode#ON_ERROR}. Otherwise the page is either ignored ({@link DumpMode#NEVER}) or dumped immediately (
     * {@link DumpMode#ALWAYS}).
     *
     * @param name
     *            the page's name
     * @param image
     *            the screenshot page
     */
    public void add(final ActionInfo actionInfo, final byte[] image)
    {
        ParameterCheckUtils.isNotNull(actionInfo.name, "name");

        add(() -> new Page(actionInfo, image));
    }

    /**
     * Adds an empty page to the internal in-memory page list if the current dump mode is {@link DumpMode#ON_ERROR}.
     * Otherwise the page is either ignored ({@link DumpMode#NEVER}) or dumped immediately ( {@link DumpMode#ALWAYS}).
     *
     * @param name
     *            the page's name
     */
    public void add(final String name)
    {
        ParameterCheckUtils.isNotNull(name, "name");

        add(() -> new Page(name));
    }

    /**
     * Adds the page provided by the given page supplier to the internal in-memory page list if the current dump mode is
     * {@link DumpMode#ON_ERROR}. Otherwise the page is either ignored ({@link DumpMode#NEVER}) or dumped immediately
     * ({@link DumpMode#ALWAYS}).
     *
     * @param pageSupplier
     *            the supplier that provides the page on demand
     */
    private synchronized void add(final Supplier<Page> pageSupplier)
    {
        if (dumpMode == DumpMode.NEVER)
        {
            // do nothing
            return;
        }

        // now we need it
        final Page page = pageSupplier.get();

        if (dumpMode == DumpMode.ON_ERROR)
        {
            // add a new page and attach all pending requests to it
            pages.add(page);

            page.getRequests().addAll(pendingRequests);
            pendingRequests.clear();
        }
        else if (dumpMode == DumpMode.ALWAYS)
        {
            // immediately dump it, no need to keep it in memory
            dumpMgr.dump(page);
        }
    }

    /**
     * Dumps the collected requests and pages to the file system.
     */
    public void dumpToDisk()
    {
        if (requestDumpPermission())
        {
            dump();
        }
    }

    /**
     * Dumping might be restricted to a certain amount per error.
     *
     * @return <code>true</code> if permission to dump is granted, <code>false</code> otherwise
     */
    private boolean requestDumpPermission()
    {
        switch (dumpMode)
        {
            case ALWAYS:
            {
                return true;
            }
            case ON_ERROR:
            {
                if (session.hasFailed())
                {
                    return ErrorCounter.get().countDumpIfOpen(session);
                }
            }
            default:
            {
                // do not grant permission
            }
        }

        return false;
    }


    /**
     * Dump the requests to the file system.
     */
    private void dump()
    {
        final List<Page> pagesCopy;
        final List<Request> requestsCopy;

        synchronized (this)
        {
            pagesCopy = new LinkedList<Page>(pages);
            requestsCopy = new LinkedList<Request>(pendingRequests);

            pages.clear();
            pendingRequests.clear();
        }

        dumpMgr.dumpToDisk(pagesCopy, requestsCopy);
    }

    /**
     * Clears the request list.
     */
    public synchronized void clear()
    {
        pages.clear();
        pendingRequests.clear();

        dumpMgr.clear();
    }

    /**
     * Returns the current dump mode.
     *
     * @return the dumpMode
     */
    public DumpMode getDumpMode()
    {
        return dumpMode;
    }

    /**
     * Sets the new dump mode.
     *
     * @param dumpMode
     *            the dumpMode to set
     */
    public void setDumpMode(final DumpMode dumpMode)
    {
        this.dumpMode = dumpMode;
    }

    /**
     * Sets the new dump manager.
     *
     * @param dumpManager
     *            new dump manager
     */
    public void setDumpManager(final DumpMgr dumpManager)
    {
        if (dumpManager != null)
        {
            dumpMgr = dumpManager;
        }
    }

    /**
     * Returns the dump manager.
     *
     * @return dump manager
     */
    public DumpMgr getDumpManager()
    {
        return dumpMgr;
    }
}
