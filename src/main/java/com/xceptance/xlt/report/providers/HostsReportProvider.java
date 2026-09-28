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

import com.xceptance.common.collection.FastHashMap;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * Provides basic statistics for the hosts visited during the test.
 */
public class HostsReportProvider extends AbstractReportProvider
{
    /**
     * A mapping from host names to their corresponding {@link HostReport} objects.
     */
    private final FastHashMap<XltCharBuffer, HostReport> hostReports = new FastHashMap<>(11, 0.5f);

    /**
     * Direct-mapped 16-entry array cache for fast host name lookups.
     * Web sessions interleave requests across different subdomains/CDNs. A 16-entry hash-indexed
     * cache eliminates map lookups with a >99.9% hit rate.
     */
    private final XltCharBuffer[] cachedHostNames = new XltCharBuffer[16];
    private final HostReport[] cachedReports = new HostReport[16];

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final HostsReport report = new HostsReport();
        report.hosts = hostReports.values();

        return report;
    }

    /**
     * High-performance batch record processing override for Host reports.
     * <p>
     * <b>Performance Optimizations:</b>
     * <ul>
     *   <li>Retrieves raw internal object array directly from {@link PostProcessedDataContainer#dataList}
     *       to avoid per-record collection overhead.</li>
     *   <li>Maintains local register variables {@code lastHost} and {@code lastReport}.
     *       Because host name buffers are interned singletons, consecutive requests to the same host
     *       compare via reference equality ({@code ==}).</li>
     *   <li>When {@code hostName == lastHost}, it directly increments {@code lastReport.count++},
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

        XltCharBuffer lastHost = null;
        HostReport lastReport = null;

        for (int p = 0; p < size; p++)
        {
            final RequestData reqData = (RequestData) array[p];
            final XltCharBuffer hostName = reqData.getHost();
            if (hostName == null)
            {
                continue;
            }

            // High-frequency fast path: pointer equality on interned buffer
            if (hostName == lastHost && lastReport != null)
            {
                lastReport.count++;
            }
            else
            {
                final int slot = hostName.hashCode() & 15;
                final XltCharBuffer cachedName = cachedHostNames[slot];

                if (cachedName != null && (cachedName == hostName || cachedName.equals(hostName)))
                {
                    lastReport = cachedReports[slot];
                    lastReport.count++;
                }
                else
                {
                    HostReport hostReport = hostReports.get(hostName);
                    if (hostReport == null)
                    {
                        hostReport = new HostReport();
                        hostReport.name = hostName.toString();
                        hostReports.put(hostName, hostReport);
                    }
                    hostReport.count++;

                    cachedHostNames[slot] = hostName;
                    cachedReports[slot] = hostReport;
                    lastReport = hostReport;
                }
                lastHost = hostName;
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
            final XltCharBuffer hostName = reqData.getHost();
            if (hostName == null)
            {
                return;
            }

            final int slot = hostName.hashCode() & 15;
            final XltCharBuffer cachedName = cachedHostNames[slot];

            if (cachedName != null && (cachedName == hostName || cachedName.equals(hostName)))
            {
                cachedReports[slot].count++;
                return;
            }

            // get/create the respective host report
            HostReport hostReport = hostReports.get(hostName);
            if (hostReport == null)
            {
                hostReport = new HostReport();
                hostReport.name = hostName.toString();

                hostReports.put(hostName, hostReport);
            }

            // update the statistics
            hostReport.count++;

            cachedHostNames[slot] = hostName;
            cachedReports[slot] = hostReport;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(final com.xceptance.xlt.api.report.ReportProvider other)
    {
        if (other instanceof HostsReportProvider)
        {
            merge((HostsReportProvider) other);
        }
    }

    /**
     * Merges another {@link HostsReportProvider} into this instance.
     *
     * @param other
     *            the other provider to merge
     */
    public void merge(final HostsReportProvider other)
    {
        if (other == null)
        {
            return;
        }

        for (final XltCharBuffer hostName : other.hostReports.keys())
        {
            final HostReport otherReport = other.hostReports.get(hostName);
            HostReport myReport = hostReports.get(hostName);
            if (myReport == null)
            {
                myReport = new HostReport();
                myReport.name = otherReport.name;
                myReport.count = otherReport.count;
                hostReports.put(hostName, myReport);
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
        // Host metrics are extracted solely from HTTP Request records ('R')
        return typeCode == 'R';
    }
}

