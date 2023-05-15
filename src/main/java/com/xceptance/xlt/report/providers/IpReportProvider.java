/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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

    private void updateIpCount(XltCharBuffer ip, XltCharBuffer host)
    {
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
    }
}
