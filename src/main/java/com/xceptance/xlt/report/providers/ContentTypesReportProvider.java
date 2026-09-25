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

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data stat)
    {
        if (stat instanceof RequestData)
        {
            final RequestData reqStats = (RequestData) stat;

            final XltCharBuffer contentType = reqStats.getContentType();
            
            // the content type is never null, it might be just "" and if this is " " or similar
            // we don't care and keep the speed, (none is set where it is produced)
//            if (contentType.length() == 0)
//            {
//                contentType = "(none)";
//            }

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(final com.xceptance.xlt.api.report.ReportProvider other)
    {
        if (other instanceof ContentTypesReportProvider)
        {
            merge((ContentTypesReportProvider) other);
        }
    }

    /**
     * Merges another {@link ContentTypesReportProvider} into this instance.
     *
     * @param other
     *            the other provider to merge
     */
    public void merge(final ContentTypesReportProvider other)
    {
        if (other == null)
        {
            return;
        }

        for (final XltCharBuffer contentType : other.contentTypeReports.keys())
        {
            final ContentTypeReport otherReport = other.contentTypeReports.get(contentType);
            ContentTypeReport myReport = contentTypeReports.get(contentType);
            if (myReport == null)
            {
                myReport = new ContentTypeReport();
                myReport.contentType = otherReport.contentType;
                myReport.count = otherReport.count;
                contentTypeReports.put(contentType, myReport);
            }
            else
            {
                myReport.count += otherReport.count;
            }
        }
    }
}
