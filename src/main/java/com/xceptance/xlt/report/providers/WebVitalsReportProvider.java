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

import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.WebVitalData;

/**
 * A report provider responsible for processing {@link WebVitalData} objects and providing the result data as a
 * {@link WebVitalsReports} report fragment.
 */
public class WebVitalsReportProvider extends AbstractDataProcessorBasedReportProvider<WebVitalsDataProcessor>
{
    /**
     * Constructor.
     */
    public WebVitalsReportProvider()
    {
        super(WebVitalsDataProcessor.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof WebVitalData)
        {
            // All web vital data records for a certain action are processed by the same data processor, hence we need
            // to extract the action name from the full name, e.g. "Foo Action [CLS]" -> "Foo Action".
            final String name = StringUtils.substringBeforeLast(data.getName(), " ");

            getProcessor(name).processDataRecord(data);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final WebVitalsReports webVitalsReports = new WebVitalsReports();

        for (final WebVitalsDataProcessor processor : getProcessors())
        {
            webVitalsReports.webVitals.add(processor.createWebVitalsReport());
        }

        return webVitalsReports;
    }
}
