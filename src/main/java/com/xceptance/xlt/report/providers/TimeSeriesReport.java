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

import java.util.ArrayList;
import java.util.List;

/**
 * A coarse time series over the whole run, covering transactions, actions and requests together.
 * <p>
 * Every other figure in the report is an aggregate over the entire test, which leaves questions like "when did it get
 * slow" and "did the errors arrive in a burst" unanswerable. This series answers them without carrying the per-second
 * data the charts use, which would be far too large.
 * <p>
 * The three scopes share one set of rows on purpose. Their time columns would otherwise be repeated three times, and
 * side by side on the same row they say which layer slowed down: requests rising means the server, actions rising while
 * requests stay flat means client-side work, transactions rising while actions stay flat means think time or test code.
 */
public class TimeSeriesReport
{
    /**
     * The bucket size in seconds. Derived from the test duration, so it is stated rather than assumed.
     */
    public int interval;

    /**
     * The number of buckets.
     */
    public int buckets;

    /**
     * The resolution of the underlying data in seconds. The interval is never finer than this.
     */
    public int sourceResolution;

    /**
     * One row per bucket.
     */
    public List<TimeSeriesRowReport> rows = new ArrayList<TimeSeriesRowReport>();
}
