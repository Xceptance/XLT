/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.xceptance.xlt.api.report.ReportCreator;
import com.xceptance.xlt.common.XltConstants;

/**
 * Load test report generator.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class XmlReportGenerator
{
    private static final Logger LOG = LoggerFactory.getLogger(XmlReportGenerator.class);

    private final List<ReportCreator> processors = new ArrayList<>();

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

            final XStream xstream = new XStream(new SanitizingDomDriver());
            xstream.autodetectAnnotations(true);
            xstream.registerConverter(new DateConverter(TimeZone.getDefault()));
            xstream.aliasSystemAttribute(null, "class");
            xstream.setMode(XStream.NO_REFERENCES);

            xstream.toXML(testReport, osw);
        }
    }

    /**
     * A custom {@link DomDriver} that uses a {@link SanitizingWriter} to write an XML file.
     */
    private static class SanitizingDomDriver extends DomDriver
    {
        @Override
        public HierarchicalStreamWriter createWriter(final Writer out)
        {
            return new SanitizingWriter(out, getNameCoder());
        }
    }

    /**
     * A custom {@link PrettyPrintWriter} that silently removes invalid XML 1.0 characters when writing text nodes.
     */
    private static class SanitizingWriter extends PrettyPrintWriter
    {
        public SanitizingWriter(final Writer writer, final NameCoder nameCoder)
        {
            super(writer, nameCoder);
        }

        @Override
        protected void writeText(final QuickWriter writer, final String text)
        {
            // escape special chars and remove invalid chars
            final String sanitizedText = StringEscapeUtils.escapeXml10(text);

            // don't call super.writeText() as this would escape the already escaped chars once more
            writer.write(sanitizedText);
        }
    }
}
