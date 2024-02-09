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
import com.xceptance.xlt.api.engine.WebVitalData;
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

        for (final ClientPerformanceData eachPerformanceData : dataList)
        {
            updatePerformanceData(session, eachPerformanceData);
        }
    }

    private static void updatePerformanceData(final SessionImpl session, final ClientPerformanceData data)
    {
        for (final ClientPerformanceRequest eachRequest : data.getRequestList())
        {
            updateAndLogRequestData(session, eachRequest);
        }

        for (final PageLoadTimingData eachData : data.getCustomDataList())
        {
            updateAndLogPageLoadTimingData(session, eachData);
        }

        postProcessAndLogWebVitalsData(data.getWebVitalsList(), session);
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

    private static RequestInfo getRequestInfo(final ClientPerformanceRequest request)
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

        requestInfo.mimeType = requestData.getContentType().toString();
        requestInfo.name = getRequestName(requestData);
        requestInfo.requestMethod = request.getHttpMethod();
        requestInfo.responseCode = statusCode;
        requestInfo.startTime = requestData.getTime();
        requestInfo.url = requestData.getUrl().toString();
        requestInfo.requestHeaders.addAll(request.getRequestHeaders());
        requestInfo.responseHeaders.addAll(request.getResponseHeaders());
        requestInfo.requestParameters.addAll(request.getFormDataParameters());
        requestInfo.requestBodyRaw = request.getRawBody();
        requestInfo.formDataEncoding = request.getFormDataEncoding();

        requestInfo.setTimings(requestData);

        return requestInfo;
    }

    private static String getRequestName(final RequestData requestData)
    {
        final String urlPath = UrlUtils.parseUrlString(requestData.getUrl().toString()).getPath();
        final String pathWithoutEndSeparator = StringUtils.removeEnd(urlPath, "/");

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

    /**
     * Post-processes the passed Web vitals and logs the resulting data.
     * <p>
     * Some Web vitals are generated only once after a page load (FCP, FID, TTFB), others may be reported multiple times
     * (CLS, INP, LCP). The latter happens if the metric has worsened over the lifetime of the page. In these cases, the
     * metric typically represents an accumulated value. For the statistics, only the last (i.e. highest) reported value
     * is relevant, hence we will usually log only the last observation.
     * <p>
     * But remember that we have actions that trigger a page load and actions that don't (a.k.a. "non-page-view"
     * actions). This has these two implications:
     * <ul>
     * <li>We receive the reportings for the last page-view action and all following non-page-view actions in a single
     * chunk.</li>
     * <li>All Web vitals are typically be reported for the page-view action, however, non-page-view actions may cause
     * additional reportings of CLS/INP/LCP values.</li>
     * </ul>
     * <p>
     * If we would log only the highest reported CLS/INP/LCP value, then we might attribute that value to a later
     * action, even though the biggest part of the accumulated metric value was contributed by a previous action. To not
     * blame the wrong action, we "reset" the value when action boundaries have been crossed. This means we introduce
     * artificial measurements which report the delta to the value from the previous action. This approach is
     * experimental.
     *
     * @param webVitalList
     *            the web vitals to process
     * @param session
     *            the current session object
     */
    private static void postProcessAndLogWebVitalsData(final List<WebVitalData> webVitalList, final SessionImpl session)
    {
        // LCP, FID, TTFB occur only once, so report them for the action they lie within
        logWebVitals("FCP", webVitalList, session);
        logWebVitals("FID", webVitalList, session);
        logWebVitals("TTFB", webVitalList, session);

        // CLS, INP, and LCP may occur multiple times with ever increasing values
        // * report only the latest/greatest value per action
        // * "reset" values at action boundaries
        postProcessAndLogWebVitals("CLS", webVitalList, session);
        postProcessAndLogWebVitals("INP", webVitalList, session);
        postProcessAndLogWebVitals("LCP", webVitalList, session);
    }

    /**
     * Logs only those web vitals with the given name, such as "FCP".
     */
    private static void logWebVitals(final String webVitalName, final List<WebVitalData> webVitalDataList, final SessionImpl session)
    {
        for (final WebVitalData webVitalData : webVitalDataList)
        {
            if (webVitalData.getName().equals(webVitalName))
            {
                final Entry<Long, ActionInfo> entry = session.getWebDriverActionStartTimes().floorEntry(webVitalData.getTime());
                final ActionInfo actionInfo = entry != null ? entry.getValue() : null;

                logTimerData(session, actionInfo, webVitalData);
            }
        }
    }

    /**
     * Post-processes and logs only those web vitals with the given name, such as "CLS".
     */
    private static void postProcessAndLogWebVitals(final String webVitalName, final List<WebVitalData> webVitalDataList, final SessionImpl session)
    {
        ActionInfo lastActionInfo = null;
        WebVitalData lastWebVitalData = null;
        double lastMaxValue = 0.0;

        for (final WebVitalData webVitalData : webVitalDataList)
        {
            if (webVitalData.getName().equals(webVitalName))
            {
                final Entry<Long, ActionInfo> entry = session.getWebDriverActionStartTimes().floorEntry(webVitalData.getTime());
                final ActionInfo actionInfo = entry != null ? entry.getValue() : null;

                if (lastWebVitalData != null && lastActionInfo != actionInfo)
                {
                    // action boundary crossed -> report the last known web vital now

                    // adjust the value and log
                    final double value = lastWebVitalData.getValue();
                    lastWebVitalData.setValue(value - lastMaxValue);
                    logTimerData(session, lastActionInfo, lastWebVitalData);

                    // remember the value
                    lastMaxValue = value;
                }

                lastActionInfo = actionInfo;
                lastWebVitalData = webVitalData;
            }
        }

        if (lastWebVitalData != null)
        {
            // adjust the value and log
            final double value = lastWebVitalData.getValue();
            lastWebVitalData.setValue(value - lastMaxValue);
            logTimerData(session, lastActionInfo, lastWebVitalData);
        }
    }

    private static void logTimerData(final SessionImpl session, final ActionInfo actionInfo, final Data data)
    {
        final String actionName = actionInfo != null ? actionInfo.name : "UnknownAction";
        final String dName = data.getName();
        final StringBuilder sb = new StringBuilder(actionName);
        if (StringUtils.isNotBlank(dName))
        {
            if (data instanceof PageLoadTimingData || data instanceof WebVitalData)
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
