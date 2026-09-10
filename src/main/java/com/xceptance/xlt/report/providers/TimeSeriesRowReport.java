/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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

import java.math.BigDecimal;
import java.util.Date;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * One bucket of the {@link TimeSeriesReport}.
 */
@XStreamAlias("row")
public class TimeSeriesRowReport
{
    /**
     * Seconds since the start of the run. Comparing this against the configured ramp-up period needs no arithmetic,
     * which is the common case a reader wants.
     */
    public long elapsed;

    /**
     * Wall clock time of the bucket start. Needed to line a spike up against server logs or APM data.
     */
    public Date time;

    /**
     * Mean transaction runtime in this bucket, in milliseconds.
     */
    public int transactionMean;

    /**
     * Completed transactions per second in this bucket.
     */
    public BigDecimal transactionCountPerSecond;

    /**
     * Failed transactions per second in this bucket. Tells a burst apart from a steady trickle.
     */
    public BigDecimal transactionErrorsPerSecond;

    /**
     * Mean action runtime in this bucket, in milliseconds.
     */
    public int actionMean;

    /**
     * Mean request runtime in this bucket, in milliseconds.
     */
    public int requestMean;

    /**
     * Requests per second in this bucket.
     */
    public BigDecimal requestCountPerSecond;
}
