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
package com.xceptance.xlt.report.providers;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.xceptance.xlt.api.engine.RequestData;

import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

/**
 * Represents request data of a single slow request.
 */
@XStreamAlias("request")
public class SlowRequestReport
{
    /**
     * The request name.
     */
    public final String name;

    /**
     * The request runtime.
     */
    public final long runtime;

    /**
     * The request start time.
     */
    public final Date time;

    /**
     * The request ID.
     */
    public final String requestId;

    /**
     * The request HTTP method.
     */
    public final String httpMethod;

    /**
     * The request URL.
     */
    public final String url;

    /**
     * The request form data encoding.
     */
    public final String formDataEncoding;

    /**
     * The request form data.
     */
    public final String formData;

    /**
     * The response ID.
     */
    public final String responseId;

    /**
     * The HTTP response code.
     */
    public final int responseCode;

    /**
     * The response content type.
     */
    public final String contentType;

    /**
     * The size of the request message.
     */
    public final int bytesSent;

    /**
     * The size of the response message.
     */
    public final int bytesReceived;

    /**
     * The time it took to look up the IP address.
     */
    public final int dnsTime;

    /**
     * The time it took to connect to the server.
     */
    public final int connectTime;

    /**
     * The time it took to send the request to the server.
     */
    public final int sendTime;

    /**
     * The time it took the server to process the request.
     */
    public final int serverBusyTime;

    /**
     * The time it took to receive the response from the server.
     */
    public final int receiveTime;

    /**
     * The time until the first response bytes arrived.
     */
    public final int timeToFirstBytes;

    /**
     * The time needed to read all response bytes.
     */
    public final int timeToLastBytes;

    /**
     * The IP address used for the request.
     */
    public final String usedIpAddress;

    /**
     * The IP addresses reported by the DNS.
     */
    public final String[] ipAddresses;

    @XStreamOmitField
    private long processingOrder;

    /**
     * General comparator for slow requests. Compares requests by runtime, bucket name, start time and processing order.
     */
    @XStreamOmitField
    public static final Comparator<SlowRequestReport> COMPARATOR = (o1, o2) -> {
        // first reverse-compare by runtime
        int result = Long.compare(o2.runtime, o1.runtime);

        if (result == 0)
        {
            // compare the names
            result = o1.name.compareTo(o2.name);

            if (result == 0)
            {
                // compare which request started first
                result = o1.time.compareTo(o2.time);

                if (result == 0)
                {
                    // compare which request was processed first to break ties
                    result = Long.compare(o1.processingOrder, o2.processingOrder);
                }
            }
        }

        return result;
    };

    /**
     * Comparator for slow requests within the same bucket. Compares requests by runtime, start time and processing
     * order. Comparing by (bucket) name is not necessary.
     */
    @XStreamOmitField
    public static final Comparator<SlowRequestReport> BUCKET_COMPARATOR = (o1, o2) -> {
        // first reverse-compare by runtime
        int result = Long.compare(o2.runtime, o1.runtime);

        if (result == 0)
        {
            // compare which request started first
            result = o1.time.compareTo(o2.time);

            if (result == 0)
            {
                // compare which request was processed first to break ties
                result = Long.compare(o1.processingOrder, o2.processingOrder);
            }
        }

        return result;
    };

    public SlowRequestReport(final RequestData requestData, final long processingOrder)
    {
        this.name = requestData.getName();
        this.runtime = requestData.getRunTime();
        this.time = new Date(requestData.getTime());

        // request information
        this.requestId = requestData.getRequestId();
        this.httpMethod = Objects.toString(requestData.getHttpMethod(), null);
        this.url = Objects.toString(requestData.getUrl(), null);
        this.formDataEncoding = Objects.toString(requestData.getFormDataEncoding(), null);
        this.formData = Objects.toString(requestData.getFormData(), null);

        // response information
        this.responseId = requestData.getResponseId();
        this.responseCode = requestData.getResponseCode();
        this.contentType = Objects.toString(requestData.getContentType(), null);

        // network timings
        this.dnsTime = requestData.getDnsTime();
        this.connectTime = requestData.getConnectTime();
        this.sendTime = requestData.getSendTime();
        this.serverBusyTime = requestData.getServerBusyTime();
        this.receiveTime = requestData.getReceiveTime();
        this.timeToFirstBytes = requestData.getTimeToFirstBytes();
        this.timeToLastBytes = requestData.getTimeToLastBytes();

        // bandwidth
        this.bytesSent = requestData.getBytesSent();
        this.bytesReceived = requestData.getBytesReceived();

        // IP address information
        this.ipAddresses = requestData.getIpAddresses();
        this.usedIpAddress = Objects.toString(requestData.getUsedIpAddress(), null);

        this.processingOrder = processingOrder;
    }
}
