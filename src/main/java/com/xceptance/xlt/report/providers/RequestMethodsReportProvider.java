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
 * Provides basic statistics for the HTTP request methods used during the test.
 */
public class RequestMethodsReportProvider extends AbstractReportProvider
{
    /**
     * The key to use if the request method was not recorded.
     */
    private static final XltCharBuffer UNKNOWN_REQUEST_METHOD = XltCharBuffer.valueOf("(unknown)");

    /**
     * A mapping from request methods to their corresponding {@link RequestMethodReport} objects.
     */
    private final FastHashMap<XltCharBuffer, RequestMethodReport> requestMethodReports = new FastHashMap<>();

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
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof RequestData)
        {
            final RequestData reqData = (RequestData) data;

            XltCharBuffer method = reqData.getHttpMethod();
            if (method == null || method.length() == 0)
            {
                // legacy result set or method not recorded
                method = UNKNOWN_REQUEST_METHOD;
            }

            RequestMethodReport requestMethodReport = requestMethodReports.get(method);
            if (requestMethodReport == null)
            {
                requestMethodReport = new RequestMethodReport();
                requestMethodReport.method = method.toString();

                requestMethodReports.put(method, requestMethodReport);
            }

            requestMethodReport.count++;
        }
    }
}
