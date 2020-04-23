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
package com.xceptance.xlt.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.xceptance.xlt.api.report.ReportCreator;
import com.xceptance.xlt.common.XltConstants;

/**
 * Load test report generator.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class XmlReportGenerator
{
    private static final Log LOG = LogFactory.getLog(XmlReportGenerator.class);

    private final List<ReportCreator> processors = new ArrayList<ReportCreator>();

    public void createReport(final File xmlFile) throws IOException
    {
        //
        final TestReport testReport = new TestReport();

        for (final ReportCreator processor : processors)
        {
            try
            {
                final Object report = processor.createReportFragment();
                if (report != null)
                {
                    testReport.addReportFragment(report);
                }
            }
            catch (final Throwable t)
            {
                LOG.warn("Failed to create report fragment", t);
                System.err.println("\nFailed to create report fragment: " + t.getMessage());
            }
        }

        saveTestReport(testReport, xmlFile);
    }

    <T extends ReportCreator> void registerStatisticsProviders(final List<T> processors)
    {
        for (final T t : processors)
        {
            registerStatisticsProvider(t);
        }
    }

    public void registerStatisticsProvider(final ReportCreator processor)
    {
        processors.add(processor);
    }

    public void unregisterStatisticsProcessor(final ReportCreator processor)
    {
        processors.remove(processor);
    }

    private void saveTestReport(final TestReport testReport, final File xmlFile) throws IOException
    {
        try (final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(xmlFile), XltConstants.UTF8_ENCODING))
        {
            osw.write(XltConstants.XML_HEADER);

            final XStream xstream = new XStream(new DomDriver());
            xstream.autodetectAnnotations(true);
            xstream.registerConverter(new DateConverter(TimeZone.getDefault()));
            xstream.aliasSystemAttribute(null, "class");
            xstream.setMode(XStream.NO_REFERENCES);

            xstream.toXML(testReport, osw);
        }
    }
}
