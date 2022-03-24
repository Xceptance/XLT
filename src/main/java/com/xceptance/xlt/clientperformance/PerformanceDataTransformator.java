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
package com.xceptance.xlt.clientperformance;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.api.engine.PageLoadTimingData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.engine.GlobalClockImpl;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.util.URLCleaner;
import com.xceptance.xlt.engine.util.UrlUtils;

public final class PerformanceDataTransformator
{
    /**
     * Class logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PerformanceDataTransformator.class);

    public static List<ClientPerformanceData> getTransformedPerformanceDataList(String json)
    {
        if (StringUtils.isBlank(json))
        {
            LOG.warn("No performance data available");
            return new ArrayList<>();
        }
        return new PerformanceDataTransformator().getPerformanceDataList(json);
    }

    private List<ClientPerformanceData> getPerformanceDataList(String json)
    {
        final List<ClientPerformanceData> dataList = new ArrayList<>();

        final JSONArray jsonData;
        try
        {
            jsonData = new JSONArray(json);
        }
        catch (final JSONException jsonEx)
        {
            if (LOG.isWarnEnabled())
            {
                LOG.warn("Failed to parse '" + json + "' as JSON array: " + jsonEx.getMessage());
            }

            return dataList;
        }

        if (LOG.isTraceEnabled())
        {
            LOG.trace("Creating data entries from json data - " + jsonData);
        }

        for (int i = 0; i < jsonData.length(); i++)
        {
            final JSONObject timingData = jsonData.optJSONObject(i);
            if (timingData == null)
            {
                continue;
            }

            final ClientPerformanceData performanceData = new ClientPerformanceData();

            // get request data from json
            final JSONArray requests = timingData.optJSONArray("requests");
            if (requests != null && requests.length() > 0)
            {
                performanceData.getRequestList().addAll(getRequestList(requests));
            }
            else
            {
                LOG.warn("Entry without request");
            }

            // get additional timing data from json
            final JSONObject timings = timingData.optJSONObject("timings");
            if (timings != null)
            {
                performanceData.getCustomDataList().addAll(getCustomDataList(timings));
            }
            else
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Entry without timings data: " + timingData);
                }
            }
            dataList.add(performanceData);
        }
        return dataList;
    }

    private List<ClientPerformanceRequest> getRequestList(JSONArray requests)
    {
        List<ClientPerformanceRequest> requestDataList = new ArrayList<>();

        for (int requestIndex = 0; requestIndex < requests.length(); requestIndex++)
        {
            final JSONObject eachRequest = requests.optJSONObject(requestIndex);
            if (eachRequest != null)
            {
                try
                {
                    final ClientPerformanceRequest performanceRequest = buildRequest(eachRequest);
                    requestDataList.add(performanceRequest);
                }
                catch (final JSONException jsonEx)
                {
                    if (LOG.isWarnEnabled())
                    {
                        LOG.warn("Failed to process request entry '" + eachRequest.toString() + "': " + jsonEx.getMessage());
                    }
                }
            }
        }
        return requestDataList;
    }

    /**
     * @param requestJSON
     * @return
     */
    private ClientPerformanceRequest buildRequest(final JSONObject requestJSON)
    {
        final ClientPerformanceRequest performanceRequest = new ClientPerformanceRequest();
        String contentType = null;
        performanceRequest.setStatusMessage(requestJSON.optString("statusText", null));

        // request headers
        if (!requestJSON.isNull("header"))
        {
            final JSONArray requestHeader = requestJSON.getJSONArray("header");
            for (final NameValuePair kv : getNameValuePairs(requestHeader))
            {
                if (kv.getName().toLowerCase().equals("content-type"))
                {
                    if (contentType != null)
                    {
                        LOG.debug("More than one 'Content-Type' header found");
                    }
                    contentType = kv.getValue();
                }
                performanceRequest.getRequestHeaders().add(kv);
            }
        }

        // response headers
        if (!requestJSON.getJSONObject("response").isNull("header"))
        {
            final JSONArray responseHeader = requestJSON.getJSONObject("response").getJSONArray("header");
            performanceRequest.getResponseHeaders().addAll(getNameValuePairs(responseHeader));
        }

        final String httpMethod = requestJSON.getString("method");
        performanceRequest.setHttpMethod(httpMethod);

        // request body (form data or raw)
        final JSONObject requestBodys = requestJSON.getJSONObject("body");
        if (!requestBodys.equals(JSONObject.NULL))
        {
            if ("POST".equals(StringUtils.defaultString(httpMethod).trim().toUpperCase()))
            {
                final String encoding = cleanContentType(contentType);
                final String encodingLC = encoding.toLowerCase();

                // check for form submission
                if (FormEncodingType.URL_ENCODED.getName().equals(encodingLC) || FormEncodingType.MULTIPART.getName().equals(encodingLC))
                {
                    performanceRequest.setFormDataEncoding(encoding);

                    // URL encoded form data
                    final JSONObject postParameters = requestBodys.optJSONObject("formData");
                    final List<NameValuePair> parameterList = getNameValuePairs(postParameters);

                    performanceRequest.getFormDataParameters().addAll(parameterList);
                    performanceRequest.setFormData(UrlUtils.getUrlEncodedParameters(parameterList));
                }
            }

            performanceRequest.setRawBody(getRawBodyText(requestBodys.optJSONArray("raw")));
        }

        // request data
        fillRequestData(performanceRequest, requestJSON);

        return performanceRequest;
    }

    /**
     * Returns a textual representation of the given request raw body parts.
     * 
     * @param optJSONArray
     *            the raw request body parts (may be {@code null}
     * @return textual representation of the given raw request body
     */
    private String getRawBodyText(final JSONArray jsonArray)
    {
        final StringBuilder sb = new StringBuilder();
        if (jsonArray != null)
        {
            for (int i = 0, l = jsonArray.length(); i < l; i++)
            {
                final JSONObject bodyPart = jsonArray.optJSONObject(i);
                if (bodyPart == null)
                {
                    continue;
                }

                String text;
                if (bodyPart.has("file"))
                {
                    text = bodyPart.optString("file");
                    if (StringUtils.isNotBlank(text))
                    {
                        text = "[file] " + text;
                    }
                }
                else if (bodyPart.has("base64"))
                {
                    text = bodyPart.optString("base64");
                    if (StringUtils.isNotBlank(text))
                    {
                        text = "[base64] " + text;
                    }
                }
                else
                {
                    text = bodyPart.optString("text");
                }

                if (text != null)
                {
                    if (sb.length() > 0)
                    {
                        sb.append("\r\n\r\n");
                    }

                    sb.append(text);
                }
            }
        }

        return sb.length() > 0 ? sb.toString() : null;
    }

    private void fillRequestData(ClientPerformanceRequest performanceRequest, JSONObject request)
    {
        final RequestData requestData = performanceRequest.getRequestData();

        requestData.setName(request.getString("requestId"));
        requestData.setUrl(URLCleaner.removeUserInfoIfNecessaryAsString(request.getString("url")));
        requestData.setHttpMethod(performanceRequest.getHttpMethod());

        requestData.setContentType(cleanContentType(request.optString("contentType")));
        final int statusCode = request.optInt("statusCode", 0);
        requestData.setResponseCode(statusCode);
        requestData.setFailed(request.optBoolean("error") || statusCode == 0 || statusCode >= 500);

        requestData.setBytesReceived(request.optInt("responseSize", 0));
        requestData.setBytesSent(request.optInt("requestSize", 0));

        requestData.setTime(!request.isNull("startTime") ? request.optLong("startTime", 0) + timeDiff : 0);
        requestData.setRunTime(request.optLong("duration", 0));
        requestData.setConnectTime(request.optInt("connectTime", 0));
        requestData.setSendTime(request.optInt("sendTime", 0));
        requestData.setTimeToFirstBytes(request.optInt("firstBytesTime", 0));
        requestData.setTimeToLastBytes(request.optInt("lastBytesTime", 0));
        requestData.setReceiveTime(request.optInt("receiveTime", 0));
        requestData.setServerBusyTime(request.optInt("busyTime", 0));
        requestData.setDnsTime(request.optInt("dnsTime", 0));

        // set additional data only if we need to
        if (SessionImpl.COLLECT_ADDITIONAL_REQUEST_DATA)
        {
            requestData.setFormData(performanceRequest.getFormData());
            requestData.setFormDataEncoding(performanceRequest.getFormDataEncoding());
        }
    }

    /**
     * Cleans the given content-type string from any content-encoding so we get a clean mime-type.
     *
     * @param contentType
     *            the content-type string
     * @return cleaned content-type (mime-type only)
     */
    private String cleanContentType(final String contentType)
    {
        return StringUtils.substringBefore(StringUtils.defaultString(contentType), ";").trim();
    }

    private List<PageLoadTimingData> getCustomDataList(JSONObject timings)
    {
        final List<PageLoadTimingData> customDataList = new ArrayList<>();

        final String[] keys = JSONObject.getNames(timings);
        if (keys.length == 0)
        {
            LOG.warn("Timings data without values");
        }

        for (final String eachKey : keys)
        {
            final JSONObject timingEntry = timings.optJSONObject(eachKey);
            if (timingEntry != null)
            {
                final long startTime = timingEntry.optLong("startTime", 0);
                final long runTime = timingEntry.optLong("duration", 0);

                // only process valid records
                if (startTime > 0 && runTime > 0)
                {
                    final PageLoadTimingData customData = new PageLoadTimingData();
                    customData.setName(StringUtils.capitalize(eachKey));
                    customData.setTime(startTime + timeDiff);
                    customData.setRunTime(runTime);
                    // add to list
                    customDataList.add(customData);
                }
                else
                {
                    LOG.debug("Page-load timing entry for '" + eachKey + "' is incomplete and will be skipped");
                }
            }
        }
        return customDataList;
    }

    private static List<NameValuePair> getNameValuePairs(JSONObject postParameters)
    {
        final List<NameValuePair> list = new ArrayList<>();
        if (postParameters != null)
        {
            final String[] keys = JSONObject.getNames(postParameters);
            for (String eachKey : keys)
            {
                final JSONArray values = postParameters.getJSONArray(eachKey);
                for (int valueIndex = 0; valueIndex < values.length(); valueIndex++)
                {
                    list.add(new NameValuePair(eachKey, values.getString(valueIndex)));
                }
            }
        }
        return list;
    }

    private static String getHeaderValue(JSONObject header)
    {
        String value = null;
        if (!header.isNull("value"))
        {
            value = header.getString("value");
        }
        else if (!header.isNull("binaryValue"))
        {
            value = header.get("binaryValue").toString();
        }
        return value;
    }

    private static List<NameValuePair> getNameValuePairs(JSONArray headers)
    {
        final List<NameValuePair> list = new ArrayList<>();
        for (int headerIndex = 0; headerIndex < headers.length(); headerIndex++)
        {
            final JSONObject eachHeader = headers.getJSONObject(headerIndex);
            final String name = eachHeader.getString("name");
            final String value = getHeaderValue(eachHeader);

            list.add(new NameValuePair(name, value));
        }
        return list;
    }

    private final long timeDiff = ((GlobalClockImpl) GlobalClock.getInstance()).getReferenceTimeDifference();

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private PerformanceDataTransformator()
    {
    }
}
