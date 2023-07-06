/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.htmlunit.FormEncodingType;
import org.htmlunit.HttpHeader;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebConnection;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.WebResponseData;
import org.htmlunit.util.NameValuePair;
import org.slf4j.Logger;

import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.api.engine.NetworkData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.dns.DnsInfo;
import com.xceptance.xlt.engine.socket.SocketStatistics;
import com.xceptance.xlt.engine.util.TimerUtils;
import com.xceptance.xlt.engine.util.URLCleaner;
import com.xceptance.xlt.engine.util.UrlUtils;
import com.xceptance.xlt.util.XltPropertiesImpl;

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
     * A constant for an empty response body used for fake responses from urls that were filtered out.
     */
    private static final byte[] FAKE_RESPONSE_BODY = new byte[0];

    /**
     * A constant for an empty header list used for fake responses from urls that were filtered out.
     */
    private static final List<NameValuePair> FAKE_RESPONSE_HEADER_LIST;

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
        FAKE_RESPONSE_HEADER_LIST = new ArrayList<NameValuePair>();
        FAKE_RESPONSE_HEADER_LIST.add(new NameValuePair("Content-Type", "text/html; charset=UTF-8"));
        FAKE_RESPONSE_HEADER_LIST.add(new NameValuePair("X-XLT-REQUEST-TO-FILTERED-DOMAIN", "true"));

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
                final WebResponseData webResponseData = new WebResponseData(FAKE_RESPONSE_BODY, HttpStatus.SC_OK,
                                                                            EnglishReasonPhraseCatalog.INSTANCE.getReason(HttpStatus.SC_OK,
                                                                                                                          null),
                                                                            FAKE_RESPONSE_HEADER_LIST);
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
            requestData.setUrl(URLCleaner.removeUserInfoIfNecessaryAsString(webRequest.getUrl()));
            requestData.setHttpMethod(webRequest.getHttpMethod().toString());

            putAdditionalRequestData(requestData, webRequest);

            final long startTime = TimerUtils.get().getStartTime();
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

            // capture the start time, since XLT 7, this is not longer done automatically
            requestData.setTime(GlobalClock.millis());

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
                runTime = TimerUtils.get().getElapsedTime(startTime);
                requestData.setRunTime((int) runTime);

                // set request/response ID
                requestData.setRequestId(requestId);
                requestData.setResponseId(responseId);
            }

            final Logger logger = XltLogger.runTimeLogger;
            if (logger.isInfoEnabled())
            {
                logger.info(response.getWebRequest().getHttpMethod().name() + " - " + response.getStatusCode() + " - " + runTime +
                            " ms - " + URLCleaner.removeUserInfoIfNecessaryAsString(response.getWebRequest().getUrl()) + " " +
                            response.getWebRequest().getRequestParameters());
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
            final RequestExecutionContext requestExecutionContext = RequestExecutionContext.getCurrent();
            final SocketStatistics socketStats = requestExecutionContext.getSocketMonitor().getSocketStatistics();
            requestData.setBytesSent(socketStats.getBytesSent());
            requestData.setBytesReceived(socketStats.getBytesReceived());
            requestData.setConnectTime(socketStats.getConnectTime());
            requestData.setSendTime(socketStats.getSendTime());
            requestData.setServerBusyTime(socketStats.getServerBusyTime());
            requestData.setReceiveTime(socketStats.getReceiveTime());
            requestData.setTimeToFirstBytes(socketStats.getTimeToFirstBytes());
            requestData.setTimeToLastBytes(socketStats.getTimeToLastBytes());
            requestData.setDnsTime(socketStats.getDnsLookupTime());

            // IP address info (all available and the used one)
            final DnsInfo dnsInfo = requestExecutionContext.getDnsMonitor().getDnsInfo();
            requestData.setIpAddresses(dnsInfo.getIpAddresses());
            requestData.setUsedIpAddress(requestExecutionContext.getTargetAddress());

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
        final WebRequest newWebRequestSettings = new WebRequest(URLCleaner.removeUserInfoIfNecessaryAsURL(webRequest.getUrl()));

        newWebRequestSettings.setAdditionalHeaders(cleanHeaders(webRequest.getAdditionalHeaders()));
        newWebRequestSettings.setCharset(webRequest.getCharset());
        newWebRequestSettings.setCredentials(webRequest.getCredentials());
        newWebRequestSettings.setEncodingType(webRequest.getEncodingType());
        newWebRequestSettings.setHttpMethod(webRequest.getHttpMethod());
        newWebRequestSettings.setProxyHost(webRequest.getProxyHost());
        newWebRequestSettings.setProxyPort(webRequest.getProxyPort());

        newWebRequestSettings.setOriginalURL(URLCleaner.removeUserInfoIfNecessaryAsURL(webRequest.getOriginalURL()));

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
            final String message = String.format("[%d] %s", statusCode, URLCleaner.removeUserInfoIfNecessaryAsString(url));

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
        if (XltPropertiesImpl.getInstance().collectAdditonalRequestData())
        {
            if (webRequest.getHttpMethod() == HttpMethod.POST)
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

    private static Map<String, String> cleanHeaders(final Map<String, String> headers)
    {
        if (headers != null)
        {
            final Map<String, String> cleanHeaders = new HashMap<>();
            for (final Map.Entry<String, String> entry : headers.entrySet())
            {
                final String name = entry.getKey();
                String value = entry.getValue();
                if (StringUtils.equalsAny(name, HttpHeader.REFERER, HttpHeader.ORIGIN))
                {
                    value = URLCleaner.removeUserInfoIfNecessaryAsString(value);
                }

                cleanHeaders.put(name, value);
            }

            return cleanHeaders;
        }

        return headers;
    }
}
