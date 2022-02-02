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

import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.PageLoadTimingData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.metrics.Metrics;
import com.xceptance.xlt.engine.resultbrowser.ActionInfo;
import com.xceptance.xlt.engine.resultbrowser.RequestInfo;
import com.xceptance.xlt.engine.util.UrlUtils;

/**
 * Helper class to report any client performance metrics to the XLT metrics system. Client performance data is gathered
 * externally by our timer recorder plug-ins for Firefox and Chrome. This helper class processes the generated data
 * either just in time or at the end of a user session.
 *
 * @see Metrics
 */
public class ClientPerformanceMetrics
{
    /**
     * The log facility.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ClientPerformanceMetrics.class);

    /**
     * Write a list of {@link Data} entries to a timer data file for the given session.
     * 
     * @param session
     *            - for which to write the data to the timer file
     * @param dataList
     *            - the list of {@link Data} which should be written to the timer file
     */
    public static void updatePerformanceData(final SessionImpl session, final List<ClientPerformanceData> dataList)
    {
        LOG.debug("Writing timer data file and reporting client performance metrics");

        for (ClientPerformanceData eachPerformanceData : dataList)
        {
            updatePerformanceData(session, eachPerformanceData);
        }
    }

    private static void updatePerformanceData(final SessionImpl session, final ClientPerformanceData data)
    {
        for (ClientPerformanceRequest eachRequest : data.getRequestList())
        {
            updateAndLogRequestData(session, eachRequest);
        }

        for (PageLoadTimingData eachData : data.getCustomDataList())
        {
            updateAndLogPageLoadTimingData(session, eachData);
        }
    }

    private static void updateAndLogRequestData(final SessionImpl session, final ClientPerformanceRequest request)
    {
        final Entry<Long, ActionInfo> entry = session.getWebDriverActionStartTimes().floorEntry(request.getRequestData().getTime());
        final ActionInfo actionInfo = entry != null ? entry.getValue() : null;
        if (actionInfo != null)
        {
            actionInfo.requests.add(getRequestInfo(request));
        }

        // write request data to timer file
        logTimerData(session, actionInfo, request.getRequestData());
    }

    private static void updateAndLogPageLoadTimingData(final SessionImpl session, final PageLoadTimingData data)
    {
        final Entry<Long, ActionInfo> entry = session.getWebDriverActionStartTimes().floorEntry(data.getTime());
        final ActionInfo actionInfo = entry != null ? entry.getValue() : null;
        if (actionInfo != null)
        {
            actionInfo.events.add(new ActionInfo.PageLoadEventInfo(data.getName(), data.getTime(), data.getRunTime()));
        }

        // write page-load timing data to timer file
        logTimerData(session, actionInfo, data);
    }

    private static RequestInfo getRequestInfo(ClientPerformanceRequest request)
    {
        final RequestInfo requestInfo = new RequestInfo();
        final RequestData requestData = request.getRequestData();
        final int statusCode = requestData.getResponseCode();

        final String statusMessage = StringUtils.defaultIfBlank(request.getStatusMessage(), "n/a");
        requestInfo.loadTime = requestData.getRunTime();

        if (statusCode > 0)
        {
            requestInfo.status = String.valueOf(statusCode) + " - ";
        }
        requestInfo.status += statusMessage;

        requestInfo.mimeType = requestData.getContentType();
        requestInfo.name = getRequestName(requestData);
        requestInfo.requestMethod = request.getHttpMethod();
        requestInfo.responseCode = statusCode;
        requestInfo.startTime = requestData.getTime();
        requestInfo.url = requestData.getUrl();
        requestInfo.requestHeaders.addAll(request.getRequestHeaders());
        requestInfo.responseHeaders.addAll(request.getResponseHeaders());
        requestInfo.requestParameters.addAll(request.getFormDataParameters());
        requestInfo.requestBodyRaw = request.getRawBody();
        requestInfo.formDataEncoding = request.getFormDataEncoding();

        requestInfo.setTimings(requestData);

        return requestInfo;
    }

    private static String getRequestName(RequestData requestData)
    {
        String urlPath = UrlUtils.parseUrlString(requestData.getUrl()).getPath();
        String pathWithoutEndSeparator = StringUtils.removeEnd(urlPath, "/");

        String name = FilenameUtils.getName(pathWithoutEndSeparator);
        if (!StringUtils.equals(urlPath, pathWithoutEndSeparator))
        {
            name = name + "/";
        }

        if (StringUtils.isBlank(name))
        {
            name = "-";
        }
        return name;
    }

    private static void logTimerData(final SessionImpl session, final ActionInfo actionInfo, final Data data)
    {
        final String actionName = actionInfo != null ? actionInfo.name : "UnknownAction";
        final String dName = data.getName();
        final StringBuilder sb = new StringBuilder(actionName);
        if (StringUtils.isNotBlank(dName))
        {
            if (data instanceof PageLoadTimingData)
            {
                sb.append(" [").append(dName).append("]");
            }
            else
            {
                final int idx = dName.lastIndexOf('.');
                if (idx < dName.length())
                {
                    sb.append('.').append(dName.substring(idx + 1));
                }
            }
        }

        data.setName(sb.toString());

        session.getDataManager().logDataRecord(data);
    }
}
