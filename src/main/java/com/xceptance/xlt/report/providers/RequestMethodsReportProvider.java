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

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.report.AbstractReportProvider;

/**
 * 
 */
public class RequestMethodsReportProvider extends AbstractReportProvider
{
    /**
     * A mapping from request methods to their corresponding {@link ResponseCodeReport} objects.
     */
    private final Map<String, RequestMethodReport> requestMethodReports = new HashMap<String, RequestMethodReport>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final RequestMethodsReport report = new RequestMethodsReport();

        report.requestMethods = new ArrayList<RequestMethodReport>(requestMethodReports.values());

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

            final String method = reqStats.getHttpMethod();

            RequestMethodReport requestMethodReport = requestMethodReports.get(method);
            if (requestMethodReport == null)
            {
                requestMethodReport = new RequestMethodReport();
                requestMethodReport.method = method;

                requestMethodReports.put(method, requestMethodReport);
            }

            requestMethodReport.count++;
        }
    }
}
