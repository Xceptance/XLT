/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
 * Provides basic statistics for the hosts visited during the test.
 */
public class HostsReportProvider extends AbstractReportProvider
{
    /**
     * The value to show if the host could not be determined from a URL.
     */
    private static final String UNKNOWN_HOST = "(unknown)";

    /**
     * A mapping from host names to their corresponding {@link HostReport} objects.
     */
    private final Map<String, HostReport> hostReports = new HashMap<String, HostReport>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final HostsReport report = new HostsReport();

        report.hosts = new ArrayList<HostReport>(hostReports.values());

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

            // get/create the respective host report
            HostReport hostReport = hostReports.get(hostName);
            if (hostReport == null)
            {
                hostReport = new HostReport();
                hostReport.name = hostName;

                hostReports.put(hostName, hostReport);
            }

            // update the statistics
            hostReport.count++;
        }
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
