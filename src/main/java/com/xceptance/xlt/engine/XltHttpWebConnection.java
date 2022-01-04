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
package com.xceptance.xlt.engine;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebResponseData;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.xceptance.xlt.api.engine.NetworkData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.dns.DnsInfo;
import com.xceptance.xlt.engine.socket.SocketStatistics;
import com.xceptance.xlt.engine.util.TimerUtils;
import com.xceptance.xlt.engine.util.UrlUtils;

/**
 * Caching web connection used by XLT web client which is responsible for
 * <ul>
 * <li>HTTP Request filtering,</li>
 * <li>Logging of request and network data and</li>
 * <li>Response transformation.</li>
 * </ul>
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class XltHttpWebConnection extends CachingHttpWebConnection
{
    /**
     * A constant for an empty response body.
     */
    private static final byte[] EMPTY_RESPONSE_BODY = new byte[0];

    /**
     * A constant for an empty header list.
     */
    private static final List<NameValuePair> EMPTY_RESPONSE_HEADER_LIST;

    /**
     * Indicates whether a request ID will be sent to the server.
     */
    private static final boolean requestIdActive;

    /**
     * The specified length of the randomly generated request ID.
     */
    private static final int requestIdLength;

    /**
     * The name of the header for the request ID.
     */
    private static final String requestIdHeader;

    /**
     * Whether to append the request ID to the User-Agent header.
     */
    private static final boolean requestIdAppendToUserAgent;

    /**
     * Indicates whether a response ID sent by the server will be extracted.
     */
    private static final boolean responseIdActive;

    /**
     * The name of the header for the response ID.
     */
    private static final String responseIdHeader;

    /**
     * Indicates whether to log an event when a request has failed.
     */
    private static final boolean logEventOnRequestFailure;

    static
    {
        EMPTY_RESPONSE_HEADER_LIST = new ArrayList<NameValuePair>();
        EMPTY_RESPONSE_HEADER_LIST.add(new NameValuePair("Content-Type", "text/html; charset=UTF-8"));

        // request ID handling
        final XltProperties props = XltProperties.getInstance();
        final String requestIdPropertyPrefix = XltConstants.XLT_PACKAGE_PATH + ".http.requestId.";

        requestIdActive = props.getProperty(requestIdPropertyPrefix + "enabled", false);
        requestIdLength = props.getProperty(requestIdPropertyPrefix + "length", 15);
        requestIdHeader = props.getProperty(requestIdPropertyPrefix + "headerName", "X-XLT-RequestId");
        requestIdAppendToUserAgent = props.getProperty(requestIdPropertyPrefix + "appendToUserAgent", false);

        // response ID handling
        final String responseIdPropertyPrefix = XltConstants.XLT_PACKAGE_PATH + ".http.responseId.";

        responseIdActive = props.getProperty(responseIdPropertyPrefix + "enabled", false);
        responseIdHeader = props.getProperty(responseIdPropertyPrefix + "headerName", "X-XLT-ResponseId");

        logEventOnRequestFailure = props.getProperty(XltConstants.XLT_PACKAGE_PATH + ".http.requestFailure.logEvent", true);
    }

    /**
     * The web client that uses this web connection.
     */
    private final XltWebClient webClient;

    /**
     * Creates a new XltHttpWebConnection and initializes it with the given web client.
     * 
     * @param webClient
     *            the web client to use
     * @param webConnection
     *            the underlying web connection to use
     */
    public XltHttpWebConnection(final XltWebClient webClient, final WebConnection webConnection)
    {
        super(webConnection);

        this.webClient = webClient;
    }

    /**
     * Loads the web response for a given set of request parameters. Overrides the super class method to add request
     * filtering.
     * 
     * @param webRequest
     *            the request parameters
     * @return the loaded web response
     * @throws IOException
     *             thrown when something went wrong.
     */
    @Override
    @SuppressWarnings("deprecation")
    public WebResponse getResponse(final WebRequest webRequest) throws IOException
    {
        final URL url = webRequest.getUrl();

        // get current request stack object
        final RequestStack requestStack = RequestStack.getCurrent();
        requestStack.setTimerName(webClient.getTimerName());
        requestStack.pushRequest();

        try
        {
            // the response
            WebResponse webResponse = null;
            // URL is accepted by web client
            if (webClient.isAcceptedUrl(url))
            {
                // load URL and get response
                webResponse = super.getResponse(webRequest);

                // update page size statistics (use response's raw size #1233)
                PageStatistics.getPageStatistics().addToTotalBytes(webResponse.getRawSize());
            }
            else
            {
                final Logger logger = XltLogger.runTimeLogger;
                if (logger.isDebugEnabled())
                {
                    logger.debug("Skipping download of URL: " + url);
                }

                // create a dummy response data with an appropriate (?) status
                // code
                final WebResponseData webResponseData = new WebResponseData(EMPTY_RESPONSE_BODY, HttpStatus.SC_OK,
                                                                            EnglishReasonPhraseCatalog.INSTANCE.getReason(HttpStatus.SC_OK,
                                                                                                                          null),
                                                                            EMPTY_RESPONSE_HEADER_LIST);
                // create response using dummy data
                webResponse = new WebResponse(webResponseData, url, webRequest.getHttpMethod(), 0);
            }

            // return response
            return webResponse;
        }
        finally
        {
            // request handled -> pop it from stack
            requestStack.popRequest();
        }
    }

    /**
     * Loads the web response for a given set of request parameters. Overrides the super class method to add request and
     * statistics logging.
     * 
     * @param webRequest
     *            the request parameters
     * @param lastModifiedHeader
     *            the last-modified date
     * @param etag
     *            the etag value
     * @return the loaded web response
     * @throws IOException
     *             thrown when something went wrong
     */
    @Override
    @SuppressWarnings("deprecation")
    protected WebResponse getResponse(final WebRequest webRequest, final String lastModifiedHeader, final String etag) throws IOException
    {
        /*
         * During request processing, the web request will be populated with the final/complete set of request headers
         * since we need them for the result browser. This might disturb HtmlUnit (for example, in case of redirects),
         * so we have to restore the original set of headers after the request is processed.
         */
        // remember the request headers, we will need to restore them later
        final Map<String, String> originalRequestHeaders = webRequest.getAdditionalHeaders();

        RequestData requestData = null;
        WebResponse response = null;

        // get current request stack
        final RequestStack requestStack = RequestStack.getCurrent();
        // get hierarchical timer name
        final String timerName = requestStack.getHierarchicalTimerName();
        // get hierarchical request name for URL
        final String requestName = requestStack.getHierarchicalRequestName(webRequest.getUrl());

        try
        {
            // create new statistics and set request data
            requestData = new RequestData(timerName);
            requestData.setUrl(removeUserInfoIfNecessary(webRequest.getUrl()));

            putAdditionalRequestData(requestData, webRequest);

            final long startTime = TimerUtils.getTime();
            final long runTime;

            // request ID handling
            String requestId = null;
            if (requestIdActive)
            {
                // set the request ID header, if configured
                requestId = RandomStringUtils.randomAlphanumeric(requestIdLength);
                webRequest.setAdditionalHeader(requestIdHeader, requestId);

                if (requestIdAppendToUserAgent)
                {
                    // first check if we have a custom user agent for this request before falling back to the default
                    String currentUserAgent = webRequest.getAdditionalHeader(HttpHeaders.USER_AGENT);
                    if (currentUserAgent == null)
                    {
                        currentUserAgent = webClient.getBrowserVersion().getUserAgent();
                    }

                    final String newUserAgent = currentUserAgent + " " + requestId;
                    webRequest.setAdditionalHeader(HttpHeaders.USER_AGENT, newUserAgent);
                }
            }

            String responseId = null;

            try
            {
                // reset the request execution context now (clear socket timings and DNS information)
                RequestExecutionContext.getCurrent().reset();

                // get response
                response = super.getResponse(webRequest, lastModifiedHeader, etag);

                // response ID handling
                if (responseIdActive)
                {
                    responseId = response.getResponseHeaderValue(responseIdHeader);
                }
            }
            catch (final IOException e)
            {
                // in this case, there is no response ... :-(
                requestData.setFailed(true);

                throw e;
            }
            finally
            {
                // set runtime
                runTime = TimerUtils.getTime() - startTime;
                requestData.setRunTime(runTime);

                // set request/response ID
                requestData.setRequestId(requestId);
                requestData.setResponseId(responseId);
            }

            final Logger logger = XltLogger.runTimeLogger;
            if (logger.isInfoEnabled())
            {
                logger.info(response.getWebRequest().getHttpMethod().name() + " - " + response.getStatusCode() + " - " + runTime +
                            " ms - " + removeUserInfoIfNecessary(response.getWebRequest().getUrl()) + " " + response.getWebRequest().getRequestParameters());
            }

            // set response data
            requestData.setResponseCode(response.getStatusCode());
            requestData.setFailed(response.getStatusCode() >= 500);
            requestData.setContentType(response.getContentType());

            // perform response processing
            response = webClient.processResponse(response);

            return response;
        }
        finally
        {
            // create a clone of the web request that is used for the request history only
            final WebRequest clonedWebRequest = cloneWebRequest(webRequest);

            // now restore the original request headers
            webRequest.setAdditionalHeaders(originalRequestHeaders);

            // network statistics
            final SocketStatistics socketStats = RequestExecutionContext.getCurrent().getSocketMonitor().getSocketStatistics();
            // System.out.println("[" + Thread.currentThread().getName() + "]: " + socketStats);
            requestData.setBytesSent(socketStats.getBytesSent());
            requestData.setBytesReceived(socketStats.getBytesReceived());
            requestData.setConnectTime(socketStats.getConnectTime());
            requestData.setSendTime(socketStats.getSendTime());
            requestData.setServerBusyTime(socketStats.getServerBusyTime());
            requestData.setReceiveTime(socketStats.getReceiveTime());
            requestData.setTimeToFirstBytes(socketStats.getTimeToFirstBytes());
            requestData.setTimeToLastBytes(socketStats.getTimeToLastBytes());
            requestData.setDnsTime(socketStats.getDnsLookupTime());

            // DNS information
            final DnsInfo dnsInfo = RequestExecutionContext.getCurrent().getDnsMonitor().getDnsInfo();
            requestData.setIpAddresses(dnsInfo.getIpAddresses());

            // log statistics and add request to history
            final SessionImpl session = (SessionImpl) Session.getCurrent();
            session.getDataManager().logDataRecord(requestData);
            session.getRequestHistory().add(requestName, clonedWebRequest, response, requestData);

            // Feature #471: API: Make the network data available for validation
            session.getNetworkDataManager().addData(new NetworkData(clonedWebRequest, response));

            // log an event if response has failed
            logEventIfNecessary(clonedWebRequest, response);

            // update page size statistics
            PageStatistics.getPageStatistics().addToBytes(requestData.getBytesReceived());

            // set raw size of response (#1233)
            // IMPORTANT: null check is necessary since response may be null due to IOException
            if (response != null)
            {
                response.setRawSize(requestData.getBytesReceived());
            }
        }
    }

    /**
     * Creates a clone of the passed {@link WebRequest} object.
     * 
     * @param webRequest
     *            the settings to clone
     * @return the cloned settings
     */
    protected WebRequest cloneWebRequest(final WebRequest webRequest)
    {
        URL requestUrl = webRequest.getUrl();
        // remove user-info from request URL if we need to (GH #57)
        if (SessionImpl.REMOVE_USERINFO_FROM_REQUEST_URL)
        {
            try
            {
                requestUrl = new URL(UrlUtils.removeUserInfo(requestUrl));
            }
            catch (final MalformedURLException mue)
            {
                final Logger logger = XltLogger.runTimeLogger;
                if (logger.isInfoEnabled())
                {
                    logger.info(String.format("Failed to remove user-info from request URL '%s'", requestUrl), mue);
                }
            }
        }

        final WebRequest newWebRequestSettings = new WebRequest(requestUrl);

        newWebRequestSettings.setAdditionalHeaders(webRequest.getAdditionalHeaders());
        newWebRequestSettings.setCharset(webRequest.getCharset());
        newWebRequestSettings.setCredentials(webRequest.getCredentials());
        newWebRequestSettings.setEncodingType(webRequest.getEncodingType());
        newWebRequestSettings.setHttpMethod(webRequest.getHttpMethod());
        newWebRequestSettings.setProxyHost(webRequest.getProxyHost());
        newWebRequestSettings.setProxyPort(webRequest.getProxyPort());

        newWebRequestSettings.setOriginalURL(webRequest.getOriginalURL());

        // can set only one of these
        if (webRequest.getRequestBody() != null)
        {
            newWebRequestSettings.setRequestBody(webRequest.getRequestBody());
        }
        else
        {
            newWebRequestSettings.setRequestParameters(webRequest.getRequestParameters());
        }

        return newWebRequestSettings;
    }

    /**
     * Logs an event if the given response has failed to be loaded (status code >= 400).
     * 
     * @param request
     *            the request settings
     * @param response
     *            the response to be processed
     */
    protected void logEventIfNecessary(final WebRequest request, final WebResponse response)
    {
        if (!logEventOnRequestFailure)
        {
            return;
        }

        final int statusCode;
        final URL url;

        // get URL and status code
        if (response == null)
        {
            statusCode = 0;
            url = request.getUrl();
        }
        else
        {
            statusCode = response.getStatusCode();
            url = response.getWebRequest().getUrl();
        }

        // check status code
        if (statusCode == 0 || statusCode >= 400)
        {
            final String eventName = "Failed to download resource";
            final String message = String.format("[%d] %s", statusCode, removeUserInfoIfNecessary(url));

            // log event
            Session.getCurrent().getDataManager().logEvent(eventName, message);
        }
    }

    /**
     * Get additional request data from a WebRequest and put them into the RequestData
     * 
     * @param requestData
     *            the request to put the data into
     * @param webRequest
     *            the request to get the data from
     */
    protected static void putAdditionalRequestData(RequestData requestData, WebRequest webRequest)
    {
        if (SessionImpl.COLLECT_ADDITIONAL_REQUEST_DATA)
        {
            final HttpMethod method = webRequest.getHttpMethod();
            requestData.setHttpMethod(method.toString());

            if (method == HttpMethod.POST)
            {
                final FormEncodingType encodingType = webRequest.getEncodingType();
                requestData.setFormDataEncoding(encodingType.getName());

                if (encodingType == FormEncodingType.URL_ENCODED)
                {
                    // Request body and request parameters are mutually exclusive!
                    // check request body first
                    String formData = webRequest.getRequestBody();
                    if (formData == null)
                    {
                        // no request body -> form data must be in request parameters
                        formData = UrlUtils.getUrlEncodedParameters(webRequest.getRequestParameters());
                    }
                    // set whatever we found
                    requestData.setFormData(formData);
                }
            }
        }
    }

    protected static String removeUserInfoIfNecessary(final URL url)
    {
        // remove user-info from request URL if we need to (GH #57)
        if(SessionImpl.REMOVE_USERINFO_FROM_REQUEST_URL)
        {
            return UrlUtils.removeUserInfo(url);
        }

        // return URL w/ user-info as string
        return url.toExternalForm();
    }
}
