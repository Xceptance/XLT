/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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

import java.util.Date;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("general")
public class GeneralReport
{
    /**
     * The total number of bytes sent.
     */
    public long bytesSent;

    /**
     * The total number of bytes received.
     */
    public long bytesReceived;

    /**
     * The total number of hits.
     */
    public long hits;

    /**
     * The start time of the test.
     */
    public Date startTime;

    /**
     * The end time of the test.
     */
    public Date endTime;

    /**
     * The total run time of the test.
     */
    public int duration;

    /**
     * Infos about the slowest requests.
     */
    public List<SlowRequestReport> slowestRequests;
}
