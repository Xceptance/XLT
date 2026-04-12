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
 * Provides basic content type statistics.
 */
public class ContentTypesReportProvider extends AbstractReportProvider
{
    /**
     * A mapping from content types to their corresponding {@link ContentTypeReport} objects.
     */
    private final FastHashMap<XltCharBuffer, ContentTypeReport> contentTypeReports = new FastHashMap<>(11, 0.5f);

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final ContentTypesReport report = new ContentTypesReport();
        report.contentTypes = contentTypeReports.values();

        return report;
    }

    @Override
    public void processAll(final com.xceptance.xlt.api.report.PostProcessedDataContainer dataContainer)
    {
        final java.util.ArrayList<RequestData> requests = dataContainer.getRequests();
        final int size = requests.size();
        for (int i = 0; i < size; i++)
        {
            final RequestData reqStats = requests.get(i);

            final XltCharBuffer contentType = reqStats.getContentType();
            
            ContentTypeReport contentTypeReport = contentTypeReports.get(contentType);
            if (contentTypeReport == null)
            {
                contentTypeReport = new ContentTypeReport();
                contentTypeReport.contentType = contentType.toString();

                contentTypeReports.put(contentType, contentTypeReport);
            }

            contentTypeReport.count++;
        }
    }
}
