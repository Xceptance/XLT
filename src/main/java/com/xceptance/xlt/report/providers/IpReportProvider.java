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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.report.AbstractReportProvider;

/**
 * Provides basic statistics for the IP addresses visited during the test.
 */
public class IpReportProvider extends AbstractReportProvider
{
    /**
     * A mapping from host names to their corresponding {@link IpReport} objects.
     */
    private final Map<String, IpReport> ipReports = new HashMap<String, IpReport>();
    
    /**
     * The value to show if the IP list for the request was empty.
     */
    private static final String UNKNOWN_IP = "(unknown)";
    /**
    * The value to show if the host could not be determined from a URL.
    */
    private static final String UNKNOWN_HOST = "(unknown)";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final IpsReport report = new IpsReport();

        report.ips = new ArrayList<IpReport>(ipReports.values());

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
            String hostName;
            final String url = reqData.getUrl();
            if (StringUtils.isBlank(url))
            {
                hostName = UNKNOWN_HOST;
            }
            else
            {
                hostName = extractHostNameFromUrl(url);
            }
            
            String ip = reqData.getUsedIpAddress();
            
            if (ip == null || ip.isEmpty())
            {
                updateIpCount(UNKNOWN_IP, hostName);
            }
            else
            {
                updateIpCount(ip, hostName);
            }

            
        }
    }
    
    private void updateIpCount(String ip, String host)
    {
        IpReport ipReport = ipReports.get(ip+host);
        if (ipReport == null)
        {
            ipReport = new IpReport();
            ipReport.ip = ip;
            ipReport.host = host;

            ipReports.put(ip+host, ipReport);
        }

        // update the statistics
        ipReport.count++;
    }

    private String extractHostNameFromUrl(final String url)
    {
        String tmp = url;

        // strip protocol if present
        final int startIndex = tmp.indexOf("://");
        if (startIndex != -1)
        {
            tmp = StringUtils.substring(tmp, startIndex + 3);
        }

        // strip path/query/fragment if present (whatever comes first)
        final int endIndex = StringUtils.indexOfAny(tmp, "/?#");
        if (endIndex != -1)
        {
            tmp = StringUtils.substring(tmp, 0, endIndex);
        }

        return tmp;
    }
}
