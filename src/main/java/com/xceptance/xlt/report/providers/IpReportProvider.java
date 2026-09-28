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
import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * Provides basic statistics for the IP addresses visited during the test.
 */
public class IpReportProvider extends AbstractReportProvider
{
    /**
     * The key to use if the IP to make the request was not recorded.
     */
    private static final XltCharBuffer UNKNOWN_IP = XltCharBuffer.valueOf("(unknown)");
    
    /**
     * A mapping from IP/host names to their corresponding {@link IpReport} objects.
     */
    private final FastHashMap<XltCharBuffer, IpReport> ipReports = new FastHashMap<>();

    /**
     * Direct-mapped 16-entry array cache for fast IP/host combination lookups.
     * Prevents key string buffer allocations and map queries when requests interleave between
     * different hosts/IP addresses.
     */
    private final XltCharBuffer[] cachedIps = new XltCharBuffer[16];
    private final XltCharBuffer[] cachedHosts = new XltCharBuffer[16];
    private final IpReport[] cachedIpReports = new IpReport[16];

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final IpsReport report = new IpsReport();

        report.ips = new ArrayList<>(ipReports.values());

        return report;
    }

    /**
     * High-performance batch record processing override for IP reports.
     * <p>
     * <b>Performance Optimizations:</b>
     * <ul>
     *   <li>Retrieves raw internal object array directly from {@link PostProcessedDataContainer#dataList}
     *       to avoid per-record collection overhead.</li>
     *   <li>Maintains local register variables {@code lastIp}, {@code lastHost}, and {@code lastReport}.
     *       Because IP address buffers and host name buffers are interned singletons from dictionaries,
     *       consecutive requests to the same endpoint compare via reference equality ({@code ==}).</li>
     *   <li>When {@code ip == lastIp && hostName == lastHost}, it directly increments {@code lastReport.count++},
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

        XltCharBuffer lastIp = null;
        XltCharBuffer lastHost = null;
        IpReport lastReport = null;

        for (int p = 0; p < size; p++)
        {
            final RequestData reqData = (RequestData) array[p];
            final XltCharBuffer hostName = reqData.getHost();
            XltCharBuffer ip = reqData.getUsedIpAddress();
            if (ip == null || ip.length() == 0)
            {
                ip = UNKNOWN_IP;
            }

            // High-frequency fast path: pointer equality on interned buffers
            if (ip == lastIp && hostName == lastHost && lastReport != null)
            {
                lastReport.count++;
            }
            else
            {
                updateIpCount(ip, hostName);
                lastIp = ip;
                lastHost = hostName;
                final int slot = (ip.hashCode() ^ hostName.hashCode()) & 15;
                lastReport = cachedIpReports[slot];
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

            // determine the host name
            final XltCharBuffer hostName = reqData.getHost(); // never null or empty

            // determine used IP address
            XltCharBuffer ip = reqData.getUsedIpAddress();
            if (ip == null || ip.length() == 0)
            {
                // legacy result set or IP not recorded
                ip = UNKNOWN_IP;
            }

            // update statistics
            updateIpCount(ip, hostName);
        }
    }

    /**
     * Updates the request count for the given IP address and host. Uses a 16-entry direct-mapped cache
     * to eliminate key buffer allocation and hash map lookups across interleaved requests.
     *
     * @param ip
     *            the IP address buffer
     * @param host
     *            the target host name buffer
     */
    private void updateIpCount(final XltCharBuffer ip, final XltCharBuffer host)
    {
        final int slot = (ip.hashCode() ^ host.hashCode()) & 15;
        final XltCharBuffer cachedIp = cachedIps[slot];
        final XltCharBuffer cachedHost = cachedHosts[slot];

        if (cachedIp != null && (cachedIp == ip || cachedIp.equals(ip)) &&
            cachedHost != null && (cachedHost == host || cachedHost.equals(host)))
        {
            cachedIpReports[slot].count++;
            return;
        }

        final XltCharBuffer key = XltCharBuffer.valueOf(ip, host);

        IpReport ipReport = ipReports.get(key);
        if (ipReport == null)
        {
            ipReport = new IpReport();
            ipReport.ip = ip.toString();
            ipReport.host = host.toString();

            ipReports.put(key, ipReport);
        }

        // update the statistics
        ipReport.count++;

        cachedIps[slot] = ip;
        cachedHosts[slot] = host;
        cachedIpReports[slot] = ipReport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(final com.xceptance.xlt.api.report.ReportProvider other)
    {
        if (other instanceof IpReportProvider)
        {
            merge((IpReportProvider) other);
        }
    }

    /**
     * Merges another {@link IpReportProvider} into this instance.
     *
     * @param other
     *            the other provider to merge
     */
    public void merge(final IpReportProvider other)
    {
        if (other == null)
        {
            return;
        }

        for (final XltCharBuffer key : other.ipReports.keys())
        {
            final IpReport otherReport = other.ipReports.get(key);
            IpReport myReport = ipReports.get(key);
            if (myReport == null)
            {
                myReport = new IpReport();
                myReport.ip = otherReport.ip;
                myReport.host = otherReport.host;
                myReport.count = otherReport.count;
                ipReports.put(key, myReport);
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
        // IP address metrics are extracted solely from HTTP Request records ('R')
        return typeCode == 'R';
    }
}

