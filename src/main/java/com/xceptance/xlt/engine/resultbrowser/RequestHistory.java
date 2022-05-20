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
package com.xceptance.xlt.engine.resultbrowser;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.codec.digest.DigestUtils;

import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.common.collection.ConcurrentLRUCache;
import com.xceptance.common.collection.LRUList;
import com.xceptance.common.lang.ThrowableUtils;
import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.common.util.ParseUtils;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.util.TimedCounter;

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
     * The property level for the dump limiter properties
     */
    public static final String LIMITER_PROPERTY = OUTPUT2DISK_ERROR_PROPERTY + ".limiter";

    /**
     * The property for the number of maximum dumps
     */
    private static final String MAX_DUMP_COUNT_PROPERTY = LIMITER_PROPERTY + ".maxDumps";

    /**
     * The property for the counter reset interval.
     */
    private static final String COUNTER_RESET_INTERVAL_PROPERTY = LIMITER_PROPERTY + ".resetInterval";

    /**
     * The property for the number of maximally handled different errors.
     */
    private static final String MAX_DIFFERENT_ERRORS_PROPERTY = LIMITER_PROPERTY + ".maxDifferentErrors";

    /**
     * Default size for LRU dump cache.
     */
    private static final int MAX_DIFFERENT_ERRORS_DEFAULT = 1000;

    /**
     * The counter reset interval.
     */
    private static final long COUNTER_RESET_INTERVAL = getConfiguredCounterResetInterval();

    /**
     * Testcase specific error keys and corresponding number of already dumped results.
     */
    private static final ConcurrentLRUCache<String, TimedCounter> DUMP_COUNT = getDumpCounter();

    /**
     * Get the configured counter reset interval in milliseconds
     *
     * @return the configured counter reset interval in milliseconds
     */
    private static long getConfiguredCounterResetInterval()
    {
        // get the value from configuration
        final String intervalString = XltProperties.getInstance().getProperty(COUNTER_RESET_INTERVAL_PROPERTY, "0");

        // parse seconds for the counter reset interval
        long tmpInterval = 0;
        try
        {
            tmpInterval = ParseUtils.parseTimePeriod(intervalString);
        }
        catch (final ParseException e)
        {
            throw new RuntimeException(String.format("The value '%s' of property '%s' cannot be resolved to a %s.", intervalString,
                                                     COUNTER_RESET_INTERVAL_PROPERTY, "time period"));
        }
        catch (final IllegalArgumentException e)
        {
            throw new RuntimeException(String.format("The value '%s' of property '%s' cannot be resolved to a %s.", intervalString,
                                                     COUNTER_RESET_INTERVAL_PROPERTY, "time period"));
        }

        // return the interval in milliseconds
        return tmpInterval * 1000;
    }

    private static ConcurrentLRUCache<String, TimedCounter> getDumpCounter()
    {
        int maxDiffErrors = XltProperties.getInstance().getProperty(MAX_DIFFERENT_ERRORS_PROPERTY, MAX_DIFFERENT_ERRORS_DEFAULT);

        // check minimum required size for LRU cache
        if (maxDiffErrors < 10)
        {
            maxDiffErrors = 10;
        }

        return new ConcurrentLRUCache<String, TimedCounter>(maxDiffErrors * 3);
    }

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
         *
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
     * Maximal number of dumps.
     */
    private final int maxDumpCount;

    /**
     * Creates a new RequestHistory object.
     */
    public RequestHistory()
    {
        int historySize = XltProperties.getInstance().getProperty(OUTPUT2DISK_SIZE_PROPERTY, 3);
        if (historySize < 1)
        {
            XltLogger.runTimeLogger.warn(OUTPUT2DISK_SIZE_PROPERTY + " must be larger than 1, setting 3 as default now.");
            historySize = 3;
        }

        pages = new LRUList<Page>(historySize);
        pendingRequests = new LinkedList<Request>();

        // get dump mode
        final String dumpModeValue = XltProperties.getInstance().getProperty(OUTPUT2DISK_PROPERTY, "onError");

        dumpMode = DumpMode.valueFrom(dumpModeValue);

        dumpMgr = new DumpMgr();
        dumpMgr.setHarExportEnabled(XltProperties.getInstance().getProperty(OUTPUT2DISK_WRITEHAR_PROPERTY, false));

        maxDumpCount = XltProperties.getInstance().getProperty(MAX_DUMP_COUNT_PROPERTY, -1);
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
        // permission denied by default
        boolean permissionGranted = false;

        switch (dumpMode)
        {
            case ALWAYS:
            {
                permissionGranted = true;
                break;
            }
            case ON_ERROR:
            {
                if (Session.getCurrent().hasFailed())
                {
                    // no limit
                    if (maxDumpCount < 0)
                    {
                        permissionGranted = true;
                    }
                    // do not dump
                    else if (maxDumpCount == 0)
                    {
                        // contradiction: log in case of error but max 0 dumps?
                        XltLogger.runTimeLogger.warn("Dump mode is " + dumpMode + " but maximum dump count is 0.");
                    }
                    // limited dump
                    else
                    {
                        // get the error key
                        final String key = getErrorKey();

                        // get the key's dump counter
                        final TimedCounter count = getErrorCount(key);

                        // if dumping for this hash is OK increase dump counter and grant permission
                        if (count.get() < maxDumpCount)
                        {
                            count.increment();
                            permissionGranted = true;
                        }
                    }
                }
                break;
            }
            default:
            {
                // do not grant permission
            }
        }

        return permissionGranted;
    }

    /**
     * Create the key for the session's failure reason.
     *
     * @return the session failure's key
     */
    private static String getErrorKey()
    {
        final SessionImpl session = (SessionImpl) Session.getCurrent();

        // get the error stack trace
        final Throwable t = session.getFailReason();

        String key = t != null ? ThrowableUtils.getMinifiedStackTrace(t) : "";

        // remove the hint
        key = RegExUtils.removeAll(key, ThrowableUtils.DIRECTORY_HINT_REGEX);

        // take the testcase name into account
        key = session.getUserName() + "|" + key;

        // hash the current key to reduce memory usage
        key = DigestUtils.md5Hex(key);

        return key;
    }

    /**
     * Get the dump counter for the given key or create a new one (initialized with <code>0</code>) if the key is
     * unknown.
     *
     * @param key
     *            the error key
     * @return the key's counter
     */
    private static TimedCounter getErrorCount(final String key)
    {
        TimedCounter count = DUMP_COUNT.get(key);
        if (count == null)
        {
            synchronized (DUMP_COUNT)
            {
                count = DUMP_COUNT.get(key);
                if (count == null)
                {
                    count = new TimedCounter(COUNTER_RESET_INTERVAL);
                    DUMP_COUNT.put(key, count);
                }
            }
        }
        return count;
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
