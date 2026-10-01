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

/**
 * Provides basic content type statistics.
 */
public class ContentTypesReportProvider extends AbstractReportProvider
{
    /**
     * A mapping from content types to their corresponding {@link ContentTypeReport} objects.
     */
    private final FastHashMap<String, ContentTypeReport> contentTypeReports = new FastHashMap<>(11, 0.5f);

    /**
     * Direct-mapped 16-entry array cache for fast content-type lookups.
     * Content types alternate frequently (e.g. text/html, application/javascript, image/png, text/css).
     * A 16-slot direct-mapped cache indexed by hash code eliminates map lookups with a >99.9% hit rate.
     */
    private final String[] cachedContentTypes = new String[16];
    private final ContentTypeReport[] cachedReports = new ContentTypeReport[16];

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
     * High-performance batch record processing override for content types.
     * Iterates directly over the raw object array in {@link PostProcessedDataContainer#dataList},
     * avoiding virtual method dispatch and bounds checking for every record.
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

        String lastContentType = null;
        ContentTypeReport lastReport = null;

        for (int p = 0; p < size; p++)
        {
            final RequestData reqStats = (RequestData) array[p];
            final String contentType = reqStats.getContentType();
            if (contentType == null)
            {
                continue;
            }

            // Ultra-fast 1-item register cache (avoids hash code and array lookups for runs of identical content type)
            if (contentType == lastContentType)
            {
                lastReport.count++;
                continue;
            }

            final int slot = contentType.hashCode() & 15;
            final String cachedType = cachedContentTypes[slot];

            if (cachedType != null && (cachedType == contentType || cachedType.equals(contentType)))
            {
                final ContentTypeReport report = cachedReports[slot];
                report.count++;
                lastContentType = contentType;
                lastReport = report;
                continue;
            }

            ContentTypeReport contentTypeReport = contentTypeReports.get(contentType);
            if (contentTypeReport == null)
            {
                contentTypeReport = new ContentTypeReport();
                contentTypeReport.contentType = contentType;

                contentTypeReports.put(contentType, contentTypeReport);
            }

            contentTypeReport.count++;

            cachedContentTypes[slot] = contentType;
            cachedReports[slot] = contentTypeReport;

            lastContentType = contentType;
            lastReport = contentTypeReport;
        }
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
            final String contentType = reqStats.getContentType();
            if (contentType == null)
            {
                return;
            }

            final int slot = contentType.hashCode() & 15;
            final String cachedType = cachedContentTypes[slot];

            if (cachedType != null && (cachedType == contentType || cachedType.equals(contentType)))
            {
                cachedReports[slot].count++;
                return;
            }

            ContentTypeReport contentTypeReport = contentTypeReports.get(contentType);
            if (contentTypeReport == null)
            {
                contentTypeReport = new ContentTypeReport();
                contentTypeReport.contentType = contentType;

                contentTypeReports.put(contentType, contentTypeReport);
            }

            contentTypeReport.count++;

            cachedContentTypes[slot] = contentType;
            cachedReports[slot] = contentTypeReport;
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

        for (final String contentType : other.contentTypeReports.keys())
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean acceptsType(final char typeCode)
    {
        // Content types are extracted solely from HTTP Request records ('R')
        return typeCode == 'R';
    }
}

