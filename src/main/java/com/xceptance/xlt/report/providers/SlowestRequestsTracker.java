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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.api.engine.RequestData;

/**
 * Stores URL and runtime of the N slowest requests encountered. Maintains unique URLs only and only the maximum runtime
 * per URL.
 */
public class SlowestRequestsTracker
{
    /**
     * The number of requests to remember.
     */
    private final int capacity;

    /**
     * The smallest of the stored runtime values.
     */
    private long minimumStoredRuntime = -1;

    /**
     * The slowest requests, sorted by runtime (descending) and URL (ascending).
     */
    private final TreeSet<SlowRequestReport> slowestRequests = new TreeSet<>();

    /**
     * The slowest requests, keyed by their URL.
     */
    private final Map<String, SlowRequestReport> slowestRequestsByUrl = new HashMap<>();

    /**
     * Creates a new tracker instance with the given capacity.
     * 
     * @param capacity
     *            the maximum number of requests to store
     */
    public SlowestRequestsTracker(final int capacity)
    {
        ParameterCheckUtils.isGreaterThan(capacity, 0, "capacity");

        this.capacity = capacity;
    }

    /**
     * Examines the passed request and updates the list of slowest requests as necessary.
     * 
     * @param requestData
     *            the request data
     */
    public void update(final RequestData requestData)
    {
        final long runtime = requestData.getRunTime();

        // reject most requests early with a simple check
        if (runtime > minimumStoredRuntime)
        {
            final String url = requestData.getUrl().toString();

            // get entry for this URL or create a new one
            SlowRequestReport requestReport = slowestRequestsByUrl.get(url);
            if (requestReport == null)
            {
                // we need to create a new entry for this URL

                // first check if the data structure is "full"
                if (slowestRequests.size() == capacity)
                {
                    // yes, make room for the new element
                    final SlowRequestReport old = slowestRequests.last();

                    slowestRequests.remove(old);
                    slowestRequestsByUrl.remove(old.url);
                }

                // create and add the new entry
                requestReport = new SlowRequestReport();
                requestReport.url = url;
                requestReport.runtime = runtime;

                slowestRequests.add(requestReport);
                slowestRequestsByUrl.put(url, requestReport);
            }
            else
            {
                // found an entry with this URL -> just update the runtime if necessary
                if (runtime > requestReport.runtime)
                {
                    // first remove
                    slowestRequests.remove(requestReport);

                    // only then change
                    requestReport.runtime = runtime;

                    // only then add
                    slowestRequests.add(requestReport);
                }
            }

            // only if the data structure is full (now) -> remember the now minimum runtime
            if (slowestRequests.size() == capacity)
            {
                minimumStoredRuntime = slowestRequests.last().runtime;
            }
        }
    }

    /**
     * Returns URL and runtime of the slowest requests, sorted by runtime (descending) and URL (ascending).
     * 
     * @return the list of requests
     */
    public List<SlowRequestReport> getSlowestRequests()
    {
        return new ArrayList<SlowRequestReport>(slowestRequests);
    }
}
