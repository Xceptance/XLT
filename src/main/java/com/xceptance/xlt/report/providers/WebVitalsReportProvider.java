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
            NameParts nameParts = NameParts.extractNameParts(data.getName());

            getProcessor(nameParts.pageName).processDataRecord(data);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        WebVitalsReports webVitalsReports = new WebVitalsReports();

        for (final WebVitalsDataProcessor processor : getProcessors())
        {
            webVitalsReports.webVitals.add(processor.getReportFragment());
        }

        return webVitalsReports;
    }

    /*
     * @Override public Object createReportFragment2() { Map<String, WebVitalsReport> reports = new TreeMap<>(); for
     * (final WebVitalsDataProcessor processor : getProcessors()) { String name = processor.getName(); NameParts
     * nameParts = extractNameParts(name); final WebVitalsReport webVitalReport =
     * reports.computeIfAbsent(nameParts.pageName, WebVitalsReport::new); double value = processor.getValue(); switch
     * (nameParts.webVitalName) { case "CLS": webVitalReport.cls = ReportUtils.convertToBigDecimal(value); break; case
     * "FCP": webVitalReport.fcp = (int) Math.round(value); break; case "FID": webVitalReport.fid = (int)
     * Math.round(value); break; case "INP": webVitalReport.inp = (int) Math.round(value); break; case "LCP":
     * webVitalReport.lcp = (int) Math.round(value); break; case "TTFB": webVitalReport.ttfb = (int) Math.round(value);
     * break; default: // unknown value -> ignore break; } } WebVitalsReports webVitalsReports = new WebVitalsReports();
     * webVitalsReports.webVitals.addAll(reports.values()); return webVitalsReports; }
     */

    private static class NameParts
    {
        public final String pageName;

        public final String webVitalName;

        public NameParts(String pageName, String webVitalName)
        {
            this.pageName = pageName;
            this.webVitalName = webVitalName;
        }

        public static NameParts extractNameParts(String name)
        {
            String pageName = StringUtils.substringBeforeLast(name, " ");
            String suffix = StringUtils.substringAfterLast(name, " ");
            String webVitalName = StringUtils.substringBetween(suffix, "[", "]");

            return new NameParts(pageName, webVitalName);
        }
    }
}
