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

import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.api.report.ReportProvider;
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
     * Cached cutoff runtime per bucket. When a bucket has reached {@code requestsPerBucket}, any request
     * with runtime <= minStoredRuntime can be skipped immediately without tree traversal or object allocation.
     */
    private final Map<String, Long> bucketMinRuntimes = new HashMap<>();

    /** Cached last bucket name for consecutive request access. */
    private String lastBucketName;

    /** Cached last bucket's TreeSet. */
    private TreeSet<SlowRequestReport> lastBucketRequests;

    /** Cached cutoff runtime for the last accessed bucket. */
    private long lastMinStoredRuntime = -1;

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
     * High-performance batch record processing override for slowest requests tracking.
     * <p>
     * <b>Performance Optimizations:</b>
     * <ul>
     *   <li>Retrieves raw internal object array directly from {@link PostProcessedDataContainer#dataList}
     *       to avoid per-record collection overhead and virtual dispatch.</li>
     *   <li>Performs primitive runtime threshold checks ({@code runtime >= minRuntime && runtime <= maxRuntime})
     *       directly on unboxed values before allocating or acquiring any bucket structures.</li>
     * </ul>
     *
     * @param dataContainer
     *            the container holding post-processed records for this chunk
     */
    @Override
    public void processAll(final com.xceptance.xlt.api.report.PostProcessedDataContainer dataContainer)
    {
        if (dataContainer.typeCode != 'R')
        {
            super.processAll(dataContainer);
            return;
        }

        final com.xceptance.xlt.api.util.SimpleArrayList<Data> list = dataContainer.dataList;
        final Object[] array = list.getInternalArray();
        final int size = list.size();

        for (int p = 0; p < size; p++)
        {
            final RequestData req = (RequestData) array[p];
            final long runtime = req.getRunTime();

            if (runtime >= minRuntime && runtime <= maxRuntime)
            {
                processRequestData(req, runtime);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof RequestData)
        {
            final RequestData reqData = (RequestData) data;
            final long runtime = reqData.getRunTime();

            // only process requests that are within the runtime thresholds
            if (runtime >= minRuntime && runtime <= maxRuntime)
            {
                processRequestData(reqData, runtime);
            }
        }
    }

    /**
     * Processes a single validated request record that falls within the runtime bounds.
     *
     * @param reqData
     *            the request data record
     * @param runtime
     *            the pre-extracted runtime duration
     */
    private void processRequestData(final RequestData reqData, final long runtime)
    {
        final String bucketName = reqData.getName();
        TreeSet<SlowRequestReport> requests;
        long minStoredRuntime;

        if (bucketName != null && bucketName == lastBucketName)
        {
            requests = lastBucketRequests;
            minStoredRuntime = lastMinStoredRuntime;
        }
        else
        {
            requests = slowestRequestsByBucket.get(bucketName);
            if (requests == null)
            {
                requests = new TreeSet<>(SlowRequestReport.BUCKET_COMPARATOR);
                slowestRequestsByBucket.put(bucketName, requests);
                minStoredRuntime = -1;
            }
            else
            {
                final Long cached = bucketMinRuntimes.get(bucketName);
                minStoredRuntime = (cached != null) ? cached.longValue() : (requests.size() >= requestsPerBucket ? requests.last().runtime : -1);
            }
            lastBucketName = bucketName;
            lastBucketRequests = requests;
            lastMinStoredRuntime = minStoredRuntime;
        }

        if (requests.size() < requestsPerBucket)
        {
            // if bucket limit isn't reached, add any request; request set is sorted automatically
            requests.add(new SlowRequestReport(reqData, processingOrder++));
            if (requests.size() >= requestsPerBucket)
            {
                minStoredRuntime = requests.last().runtime;
                bucketMinRuntimes.put(bucketName, minStoredRuntime);
                lastMinStoredRuntime = minStoredRuntime;
            }
        }
        else if (runtime > minStoredRuntime)
        {
            // add the request; the request set is sorted automatically
            requests.add(new SlowRequestReport(reqData, processingOrder++));

            // remove request that is the last after sorting to stay within bucket limit
            requests.remove(requests.last());

            // update cached cutoff
            minStoredRuntime = requests.last().runtime;
            bucketMinRuntimes.put(bucketName, minStoredRuntime);
            lastMinStoredRuntime = minStoredRuntime;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(final ReportProvider other)
    {
        if (other instanceof SlowestRequestsReportProvider)
        {
            merge((SlowestRequestsReportProvider) other);
        }
    }

    /**
     * Merges another {@link SlowestRequestsReportProvider} into this instance.
     *
     * @param other
     *            the other provider to merge
     */
    public void merge(final SlowestRequestsReportProvider other)
    {
        if (other == null)
        {
            return;
        }

        for (final Map.Entry<String, TreeSet<SlowRequestReport>> entry : other.slowestRequestsByBucket.entrySet())
        {
            final String bucketName = entry.getKey();
            TreeSet<SlowRequestReport> requests = slowestRequestsByBucket.get(bucketName);
            if (requests == null)
            {
                requests = new TreeSet<>(SlowRequestReport.BUCKET_COMPARATOR);
                slowestRequestsByBucket.put(bucketName, requests);
            }

            for (final SlowRequestReport req : entry.getValue())
            {
                requests.add(req);
                if (requests.size() > requestsPerBucket)
                {
                    requests.remove(requests.last());
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean acceptsType(final char typeCode)
    {
        // Slowest requests are extracted solely from HTTP Request records ('R')
        return typeCode == 'R';
    }
}

