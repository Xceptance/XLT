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
package com.xceptance.xlt.engine.resultbrowser;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;

/**
 * Manager responsible for request data handling.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
class RequestDataMgr
{
    /**
     * List of pending request information objects.
     */
    private final List<RequestInfo> pendingRequests;

    /**
     * List of action information objects.
     */
    private final List<ActionInfo> actions;

    /**
     * Maximum allowed request body size.
     */
    private static final int MAX_BODY_SIZE;

    static
    {
        final String maxBodySizePropertyName = XltConstants.XLT_PACKAGE_PATH + ".output2disk.maxRequestBodySize";
        final int maxBodySizeDefault = 8 * 1024;
        int maxBodySize = XltProperties.getInstance().getProperty(maxBodySizePropertyName, maxBodySizeDefault);
        if (maxBodySize < 4)
        {
            XltLogger.runTimeLogger.warn("Specified request body size limit is less than the minimum of '4' characters. Will use the minimum size.");
            maxBodySize = 4;
        }
        MAX_BODY_SIZE = maxBodySize;
    }

    /**
     * Constructor.
     */
    RequestDataMgr()
    {
        pendingRequests = new LinkedList<RequestInfo>();
        actions = new LinkedList<ActionInfo>();
    }

    /**
     * Generates and returns a transaction information object base on the current pending action and request information
     * objects.
     *
     * @return transaction information
     */
    public TransactionInfo generateTransaction()
    {
        // check whether we have pending request info objects
        if (!pendingRequests.isEmpty())
        {
            // yes, add an empty action info with the pending request infos
            addActionInfo(new ActionInfo(), "n/a", null);
        }

        // sort the requests of each action by start and load time (in ascending order)
        for (ActionInfo actionInfo : actions)
        {
            Collections.sort(actionInfo.requests);
        }

        // create the transaction info
        final TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.user = Session.getCurrent().getUserName();
        transactionInfo.date = GlobalClock.getInstance().getTime();
        transactionInfo.actions.addAll(actions);

        // store the session's value log (as NameValuePairs so we can reuse some code in the result browser)
        final Map<String, Object> sortedValueLog = new TreeMap<>(Session.getCurrent().getValueLog());
        for (final Entry<String, Object> entry : sortedValueLog.entrySet())
        {
            final String value = (entry.getValue() == null) ? null : entry.getValue().toString();

            transactionInfo.valueLog.add(new NameValuePair(entry.getKey(), value));
        }

        return transactionInfo;
    }

    /**
     * Called by {@link DumpMgr} to notify that a page with the given name was dumped to a file with the given file
     * name.
     *
     * @param page
     *            the page that was dumped
     * @param fileName
     *            name of the file the page was dumped to
     */
    public void pageDumped(final String fileName, final Page page)
    {
        final ActionInfo actionInfo = page.getActionInfo() != null ? page.getActionInfo() : new ActionInfo();

        addActionInfo(actionInfo, page.getName(), XltConstants.DUMP_PAGES_DIR + "/" + fileName);
    }

    /**
     * Adds the given action info to the list of actions and populates it with the passed action and file name and any
     * pending request.
     * 
     * @param actionInfo
     *            the action info to add
     * @param actionName
     *            the name of the action
     * @param fileName
     *            the name of the file with the action result
     */
    private void addActionInfo(final ActionInfo actionInfo, final String actionName, final String fileName)
    {
        actionInfo.name = actionName;
        actionInfo.fileName = fileName;
        actionInfo.requests.addAll(pendingRequests);

        synchronized (this)
        {
            actions.add(actionInfo);
            pendingRequests.clear();
        }
    }

    /**
     * Called by {@link DumpMgr} to notify that the given request was dumped to a file with the given name.
     *
     * @param fileName
     *            name of the file the given request was dumped to
     * @param request
     *            request that was dumped
     */
    public void requestDumped(final String fileName, final Request request)
    {
        final RequestInfo requestInfo = getRequestInfo(fileName, request);

        synchronized (this)
        {
            pendingRequests.add(requestInfo);
        }
    }

    /**
     * Generates and returns a request information object for the given request/response pair where the response was
     * dumped to a file with the given file name.
     *
     * @param fileName
     *            name of the file the given response was dumped to
     * @param webRequest
     *            the request
     * @param webResponse
     *            the response
     * @param request
     *            the request
     * @return request information
     */
    private RequestInfo getRequestInfo(final String fileName, final Request request)
    {

        final WebRequest webRequest = request.webRequest;
        final WebResponse webResponse = request.webResponse;
        final HttpMethod httpMethod = webRequest.getHttpMethod();

        final RequestInfo requestInfo = new RequestInfo();

        requestInfo.name = getFileName(webRequest.getUrl());
        requestInfo.url = webRequest.getUrl().toString();
        requestInfo.requestMethod = httpMethod.name();
        requestInfo.requestParameters.addAll(webRequest.getRequestParameters());

        if (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT || httpMethod == HttpMethod.PATCH)
        {
            requestInfo.formDataEncoding = webRequest.getEncodingType().getName();
        }

        requestInfo.startTime = request.requestData.getTime();
        requestInfo.fileName = XltConstants.DUMP_RESPONSES_DIR + "/" + fileName;

        // request headers
        final Map<String, String> requestHeaders = webRequest.getAdditionalHeaders();
        for (final Entry<String, String> entry : requestHeaders.entrySet())
        {
            requestInfo.requestHeaders.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }

        // Limit size of request body raw string.
        requestInfo.requestBodyRaw = StringUtils.abbreviate(StringUtils.defaultString(webRequest.getRequestBody()), MAX_BODY_SIZE);

        // response
        if (webResponse != null)
        {
            requestInfo.mimeType = webResponse.getContentType();
            requestInfo.responseCode = webResponse.getStatusCode();
            requestInfo.status = webResponse.getStatusCode() + " - " + webResponse.getStatusMessage();
            requestInfo.loadTime = webResponse.getLoadTime();
            requestInfo.responseHeaders.addAll(webResponse.getResponseHeaders());
            requestInfo.protocol = webResponse.getProtocolVersion();
        }
        else
        {
            requestInfo.mimeType = "text/html";
            requestInfo.responseCode = 0;
            requestInfo.status = "n/a";
            requestInfo.loadTime = 0;
            requestInfo.protocol = "n/a";
        }

        requestInfo.setTimings(request.requestData);

        return requestInfo;
    }

    /**
     * Derives the simple file name from the passed URL. Usually, this will be the last part of the URL's path. For
     * example, from the URL "http://localhost/foo/bar/baz/bum.jpg" the file name "bum.jpg" would be derived. If the
     * last part is a directory as in "http://localhost/foo/bar/baz/", the result would be "baz/".
     *
     * @param url
     *            the input URL
     * @return the file name
     */
    private String getFileName(final URL url)
    {
        // get the path only, i.e. no host, no query string, no reference
        String path = url.getPath();

        // remove any session ID information (";sid=3278327878")
        path = StringUtils.substringBefore(path, ";");

        // return the last path element (file ["aaa.js"] or directory ["foo/"])
        final int l = path.length();
        final int i = path.lastIndexOf('/', l - 2);

        if (i >= 0)
        {
            path = path.substring(i + 1);
        }

        return path;
    }

    /**
     * Clears the internal lists of pending requests and actions.
     */
    public synchronized void clear()
    {
        pendingRequests.clear();
        actions.clear();
    }
}
