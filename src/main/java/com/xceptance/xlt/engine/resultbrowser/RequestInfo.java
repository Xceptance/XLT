/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import java.util.ArrayList;
import java.util.List;

import org.htmlunit.util.NameValuePair;

import com.xceptance.xlt.api.engine.RequestData;

/**
 * Container that holds all information about a request necessary to be processed by the results browser.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class RequestInfo implements Comparable<RequestInfo>
{
    public String fileName;

    public long startTime;

    public long loadTime;

    public String mimeType;

    public String name;

    public final List<NameValuePair> requestHeaders = new ArrayList<NameValuePair>();

    public String requestMethod;

    public final List<NameValuePair> requestParameters = new ArrayList<NameValuePair>();

    public int responseCode;

    public final List<NameValuePair> responseHeaders = new ArrayList<NameValuePair>();

    public String status;

    public String url;

    public String requestBodyRaw;

    public String protocol;

    /**
     * Encoding used for form data submission.
     */
    public String formDataEncoding;

    /**
     * Request timings (used for HAR export).
     */
    public transient TimingInfo timings;

    public void setTimings(final RequestData request)
    {
        TimingInfo timing = null;
        if (request != null)
        {
            timing = new TimingInfo();
            timing.bytesReceived = request.getBytesReceived();
            timing.bytesSent = request.getBytesSent();
            timing.connectTime = request.getConnectTime();
            timing.dnsTime = request.getDnsTime();
            timing.receiveTime = request.getReceiveTime();
            timing.sendTime = request.getSendTime();
            timing.serverBusyTime = request.getServerBusyTime();
            timing.timeToFirstByte = request.getTimeToFirstBytes();
            timing.timeToLastByte = request.getTimeToLastBytes();

        }

        timings = timing;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final RequestInfo other)
    {
        // sort by start time, then by load time (in ascending order)
        int result = Long.compare(startTime, other.startTime);
        if (result == 0)
        {
            result = Long.compare(loadTime, other.loadTime);
        }
        return result;
    }

    public static class TimingInfo
    {

        /* Value '-1' means: not available */

        public int bytesSent = -1;

        public int bytesReceived = -1;

        public int connectTime = -1;

        public int receiveTime = -1;

        public int sendTime = -1;

        public int serverBusyTime = -1;

        public int dnsTime = -1;

        public int timeToFirstByte = -1;

        public int timeToLastByte = -1;

        TimingInfo()
        {
        }
    }

}
