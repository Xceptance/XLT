/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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

import org.apache.http.impl.EnglishReasonPhraseCatalog;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.report.AbstractReportProvider;

/**
 * 
 */
public class ResponseCodesReportProvider extends AbstractReportProvider
{
    /**
     * A mapping from response codes to their corresponding {@link ResponseCodeReport} objects.
     */
    private final Map<Integer, ResponseCodeReport> responseCodeReports = new HashMap<Integer, ResponseCodeReport>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final ResponseCodesReport report = new ResponseCodesReport();

        report.responseCodes = new ArrayList<ResponseCodeReport>(responseCodeReports.values());

        return report;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data stat)
    {
        if (stat instanceof RequestData)
        {
            final RequestData reqStats = (RequestData) stat;

            final int code = reqStats.getResponseCode();

            ResponseCodeReport responseCodeReport = responseCodeReports.get(code);
            if (responseCodeReport == null)
            {
                responseCodeReport = new ResponseCodeReport();
                responseCodeReport.code = code;
                responseCodeReport.statusText = getStatusText(code);

                responseCodeReports.put(code, responseCodeReport);
            }

            responseCodeReport.count++;
        }
    }

    /**
     * Returns the corresponding status text for the given HTTP status code.
     * 
     * @param statusCode
     *            the status code
     * @return the status text
     */
    private String getStatusText(final int statusCode)
    {
        String statusText;

        if (statusCode == 0)
        {
            // our status code for network-related problems
            statusText = "No Response Available";
        }
        else
        {
            try
            {
                statusText = EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode, null);
            }
            catch (final IllegalArgumentException e)
            {
                // status code was other than 1xx...5xx
                statusText = null;
            }

            statusText = (statusText == null) ? "???" : statusText;
        }

        return statusText;
    }
}
