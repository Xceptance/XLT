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

import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.api.report.ReportProviderConfiguration;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Report provider for the slowest requests in the test run.
 */
public class SlowestRequestsReportProvider extends AbstractReportProvider
{
    /**
     * The maximum number of slow requests to remember per bucket.
     */
    private int requestsPerBucket;

    /**
     * The maximum number of slow requests to return in total.
     */
    private int requestsTotal;

    /**
     * The minimum runtime of requests to remember.
     */
    private int minRuntime;

    /**
     * The maximum runtime of requests to remember.
     */
    private int maxRuntime;

    /**
     * The slowest requests of each bucket. The bucket name is used as the map key.
     */
    private final Map<String, TreeSet<SlowRequestReport>> slowestRequestsByBucket = new HashMap<>();

    /**
     * The number indicating how many requests were processed (and not skipped) by the provider. Used to keep track of
     * the order in which requests were processed.
     */
    private long processingOrder = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final TreeSet<SlowRequestReport> slowestRequests = new TreeSet<>(SlowRequestReport.COMPARATOR);

        // iterate over all stored requests of all buckets to determine the slowest requests overall
        for (final TreeSet<SlowRequestReport> bucketRequests : slowestRequestsByBucket.values())
        {
            for (final SlowRequestReport request : bucketRequests)
            {
                if (slowestRequests.size() < requestsTotal)
                {
                    // if the request total isn't reached, add any request; request set is sorted automatically
                    slowestRequests.add(request);
                }
                else
                {
                    // if request total is reached, only add requests that are at least as slow as the fastest stored
                    // request; requests with the same runtime as the fastest stored request will be added and sorted
                    // based on the TreeSet's comparator
                    if (request.runtime >= slowestRequests.last().runtime)
                    {
                        // add request (which automatically sorts the set); then remove last request after sorting
                        slowestRequests.add(request);
                        slowestRequests.remove(slowestRequests.last());
                    }
                }
            }
        }

        SlowestRequestsReport report = new SlowestRequestsReport();
        report.slowestRequests = new ArrayList<>(slowestRequests);

        return report;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof RequestData)
        {
            final long runtime = ((RequestData) data).getRunTime();

            // only process requests that are within the runtime thresholds
            if (runtime >= minRuntime && runtime <= maxRuntime)
            {
                final String bucketName = data.getName();

                // get entry for this bucket or create a new one
                TreeSet<SlowRequestReport> requests = slowestRequestsByBucket.get(bucketName);
                if (requests == null)
                {
                    requests = new TreeSet<>(SlowRequestReport.BUCKET_COMPARATOR);
                    slowestRequestsByBucket.put(bucketName, requests);
                }

                if (requests.size() < requestsPerBucket)
                {
                    // if bucket limit isn't reached, add any request; request set is sorted automatically
                    requests.add(new SlowRequestReport((RequestData) data, processingOrder));
                    processingOrder++;
                }
                else
                {
                    // if bucket is full, only add requests that are slower than the fastest stored request; requests
                    // with the exact same runtime as the fastest stored request are skipped
                    if (runtime > requests.last().runtime)
                    {
                        // add the request; the request set is sorted automatically
                        requests.add(new SlowRequestReport((RequestData) data, processingOrder));
                        processingOrder++;

                        // remove request that is the last after sorting to stay within bucket limit
                        requests.remove(requests.last());
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfiguration(final ReportProviderConfiguration config)
    {
        super.setConfiguration(config);

        requestsPerBucket = ((ReportGeneratorConfiguration) config).getSlowestRequestsPerBucket();
        requestsTotal = ((ReportGeneratorConfiguration) config).getSlowestRequestsTotal();
        minRuntime = ((ReportGeneratorConfiguration) config).getSlowestRequestsMinRuntime();
        maxRuntime = ((ReportGeneratorConfiguration) config).getSlowestRequestsMaxRuntime();

        ParameterCheckUtils.isGreaterThan(requestsPerBucket, 0, "slowestRequestsPerBucket");
        ParameterCheckUtils.isGreaterThan(requestsTotal, 0, "slowestRequestsTotal");
        ParameterCheckUtils.isGreaterThan(minRuntime, 0, "slowRequestMinRuntime");
        ParameterCheckUtils.isGreaterThan(maxRuntime, 0, "slowRequestMaxRuntime");

        if (minRuntime > maxRuntime)
        {
            throw new IllegalArgumentException("'slowestRequestMinRuntime' must not be greater than 'slowestRequestMaxRuntime'");
        }
    }
}
