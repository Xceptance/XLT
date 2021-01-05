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
package com.xceptance.xlt.report.providers;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Represents the request timer statistics in a test report. The statistics is generated from a series of request data.
 */
@XStreamAlias("request")
public class RequestReport extends TimerReport
{
    /**
     * The statistics for the "bytesSent" values.
     */
    public ExtendedStatisticsReport bytesSent;

    /**
     * The statistics for the "bytesReceived" values.
     */
    public ExtendedStatisticsReport bytesReceived;

    /**
     * The statistics for the "dnsTime" values.
     */
    public StatisticsReport dnsTime;

    /**
     * The statistics for the "connectTime" values.
     */
    public StatisticsReport connectTime;

    /**
     * The statistics for the "sendTime" values.
     */
    public StatisticsReport sendTime;

    /**
     * The statistics for the "serverBusyTime" values.
     */
    public StatisticsReport serverBusyTime;

    /**
     * The statistics for the "receiveTime" values.
     */
    public StatisticsReport receiveTime;

    /**
     * The statistics for the "timeToFirstBytes" values.
     */
    public StatisticsReport timeToFirstBytes;

    /**
     * The statistics for the "timeToLastBytes" values.
     */
    public StatisticsReport timeToLastBytes;

    /**
     * The number of timer values per configured runtime interval.
     */
    public int[] countPerInterval;

    /**
     * A list of the top URLs.
     */
    public UrlData urls;
}
