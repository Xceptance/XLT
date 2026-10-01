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

import com.xceptance.common.collection.FastHashMap;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.report.AbstractReportProvider;

/**
 * Provides basic statistics for the HTTP request methods used during the test.
 */
public class RequestMethodsReportProvider extends AbstractReportProvider
{
    /**
     * The key to use if the request method was not recorded.
     */
    private static final String UNKNOWN_REQUEST_METHOD = "(unknown)";

    /**
     * A mapping from request methods to their corresponding {@link RequestMethodReport} objects.
     */
    private final FastHashMap<String, RequestMethodReport> requestMethodReports = new FastHashMap<>();

    /**
     * Direct-mapped 8-entry array cache for fast HTTP method lookups.
     * With fewer than 8 distinct standard HTTP methods (GET, POST, PUT, DELETE, etc.),
     * an 8-entry hash-indexed array guarantees virtually 100% cache hit rate with zero map queries.
     */
    private final String[] cachedMethods = new String[8];
    private final RequestMethodReport[] cachedReports = new RequestMethodReport[8];

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final RequestMethodsReport report = new RequestMethodsReport();

        report.requestMethods = new ArrayList<>(requestMethodReports.values());

        return report;
    }

    /**
     * High-performance batch record processing override for HTTP request methods.
     * <p>
     * <b>Performance Optimizations:</b>
     * <ul>
     *   <li>Retrieves raw internal object array directly from {@link PostProcessedDataContainer#dataList}
     *       to avoid per-record collection overhead.</li>
     *   <li>Maintains local register variables {@code lastMethod} and {@code lastReport}.
     *       Because method buffers ("GET", "POST", etc.) are interned singletons, consecutive
     *       requests compare via reference equality ({@code ==}).</li>
     *   <li>When {@code method == lastMethod}, it directly increments {@code lastReport.count++},
     *       completely bypassing hash calculations, bitwise masks, and array cache lookups.</li>
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

        String lastMethod = null;
        RequestMethodReport lastReport = null;

        for (int p = 0; p < size; p++)
        {
            final RequestData reqData = (RequestData) array[p];
            String method = reqData.getHttpMethod();
            if (method == null || method.length() == 0)
            {
                method = UNKNOWN_REQUEST_METHOD;
            }

            // High-frequency fast path: pointer equality on interned buffer
            if (method == lastMethod && lastReport != null)
            {
                lastReport.count++;
            }
            else
            {
                final int slot = method.hashCode() & 7;
                final String cachedMethod = cachedMethods[slot];

                if (cachedMethod != null && (cachedMethod == method || cachedMethod.equals(method)))
                {
                    lastReport = cachedReports[slot];
                    lastReport.count++;
                }
                else
                {
                    RequestMethodReport requestMethodReport = requestMethodReports.get(method);
                    if (requestMethodReport == null)
                    {
                        requestMethodReport = new RequestMethodReport();
                        requestMethodReport.method = method;
                        requestMethodReports.put(method, requestMethodReport);
                    }
                    requestMethodReport.count++;

                    cachedMethods[slot] = method;
                    cachedReports[slot] = requestMethodReport;
                    lastReport = requestMethodReport;
                }
                lastMethod = method;
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

            String method = reqData.getHttpMethod();
            if (method == null || method.length() == 0)
            {
                // legacy result set or method not recorded
                method = UNKNOWN_REQUEST_METHOD;
            }

            final int slot = method.hashCode() & 7;
            final String cachedMethod = cachedMethods[slot];

            if (cachedMethod != null && (cachedMethod == method || cachedMethod.equals(method)))
            {
                cachedReports[slot].count++;
                return;
            }

            RequestMethodReport requestMethodReport = requestMethodReports.get(method);
            if (requestMethodReport == null)
            {
                requestMethodReport = new RequestMethodReport();
                requestMethodReport.method = method;

                requestMethodReports.put(method, requestMethodReport);
            }

            requestMethodReport.count++;

            cachedMethods[slot] = method;
            cachedReports[slot] = requestMethodReport;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(final com.xceptance.xlt.api.report.ReportProvider other)
    {
        if (other instanceof RequestMethodsReportProvider)
        {
            merge((RequestMethodsReportProvider) other);
        }
    }

    /**
     * Merges another {@link RequestMethodsReportProvider} into this instance.
     *
     * @param other
     *            the other provider to merge
     */
    public void merge(final RequestMethodsReportProvider other)
    {
        if (other == null)
        {
            return;
        }

        for (final String method : other.requestMethodReports.keys())
        {
            final RequestMethodReport otherReport = other.requestMethodReports.get(method);
            RequestMethodReport myReport = requestMethodReports.get(method);
            if (myReport == null)
            {
                myReport = new RequestMethodReport();
                myReport.method = otherReport.method;
                myReport.count = otherReport.count;
                requestMethodReports.put(method, myReport);
            }
            else
            {
                myReport.count += otherReport.count;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean acceptsType(final char typeCode)
    {
        // HTTP request methods are extracted solely from HTTP Request records ('R')
        return typeCode == 'R';
    }
}

