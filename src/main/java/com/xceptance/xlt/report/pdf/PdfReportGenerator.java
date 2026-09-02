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
package com.xceptance.xlt.report.pdf;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openhtmltopdf.extend.FSStream;
import com.openhtmltopdf.extend.FSStreamFactory;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.xceptance.common.util.ParameterCheckUtils;

/**
 * Generates a PDF summary report from the load test XML report using OpenHTMLtoPDF.
 *
 * @author Xceptance Software Technologies GmbH
 */
public class PdfReportGenerator
{
    private static final Logger LOG = LoggerFactory.getLogger(PdfReportGenerator.class);

    /**
     * Default output PDF file name.
     */
    public static final String DEFAULT_PDF_FILENAME = "load-report.pdf";

    /**
     * Generates a PDF report from the given XML input using the specified XSL stylesheet.
     *
     * @param inputXmlFile
     *            the XML report file (e.g., loadreport.xml)
     * @param outputDir
     *            the report output directory
     * @param styleSheetFile
     *            the XSL stylesheet file (e.g., pdf.xsl)
     * @param outputPdfFile
     *            the output PDF file (e.g., load-report.pdf)
     * @param parameters
     *            parameters to pass to the XSL transformation
     * @throws Exception
     *             if transformation or PDF rendering fails
     */
    public static void generatePdfReport(final File inputXmlFile, final File outputDir, final File styleSheetFile,
                                         final File outputPdfFile, final Map<String, Object> parameters) throws Exception
    {
        ParameterCheckUtils.isReadableFile(inputXmlFile, "inputXmlFile");
        ParameterCheckUtils.isReadableFile(styleSheetFile, "styleSheetFile");
        ParameterCheckUtils.isNotNull(outputDir, "outputDir");
        ParameterCheckUtils.isNotNull(outputPdfFile, "outputPdfFile");

        // 1. Transform XML to XHTML
        final TransformerFactory factory = TransformerFactory.newInstance();
        final Transformer transformer = factory.newTransformer(new StreamSource(styleSheetFile));

        if (parameters != null)
        {
            for (final Entry<String, Object> entry : parameters.entrySet())
            {
                transformer.setParameter(entry.getKey(), entry.getValue());
            }
        }

        final StringWriter htmlWriter = new StringWriter();
        transformer.transform(new StreamSource(inputXmlFile), new StreamResult(htmlWriter));
        final String htmlContent = htmlWriter.toString();

        // Base URI for resolving relative paths (CSS, images)
        final String baseUri = outputDir.toURI().toString();

        // 2. Render HTML to PDF via OpenHTMLtoPDF
        try (final OutputStream os = new BufferedOutputStream(new FileOutputStream(outputPdfFile)))
        {
            final PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useProtocolsStreamImplementation(new WebpConvertingStreamFactory(), "file", "http", "https");
            builder.withHtmlContent(htmlContent, baseUri);
            builder.toStream(os);
            builder.run();
        }

        LOG.info("PDF report generated successfully: {}", outputPdfFile.getAbsolutePath());
    }

    /**
     * An {@link FSStreamFactory} that intercepts .webp image requests and transcodes them to PNG on the fly,
     * allowing OpenHTMLtoPDF to render WebP charts cleanly.
     */
    private static class WebpConvertingStreamFactory implements FSStreamFactory
    {
        @Override
        public FSStream getUrl(final String uri)
        {
            return new FSStream()
            {
                private byte[] data = null;
                private boolean loaded = false;

                private synchronized void loadData()
                {
                    if (loaded)
                    {
                        return;
                    }
                    loaded = true;

                    if (uri == null)
                    {
                        return;
                    }

                    try
                    {
                        final InputStream in;
                        final URI parsedUri = URI.create(uri);
                        if ("file".equalsIgnoreCase(parsedUri.getScheme()))
                        {
                            final File file = new File(parsedUri);
                            if (!file.exists())
                            {
                                return;
                            }
                            in = new FileInputStream(file);
                        }
                        else if (parsedUri.getScheme() != null)
                        {
                            in = parsedUri.toURL().openStream();
                        }
                        else
                        {
                            final File file = new File(uri);
                            if (!file.exists())
                            {
                                return;
                            }
                            in = new FileInputStream(file);
                        }

                        try (in)
                        {
                            if (uri.toLowerCase().endsWith(".webp"))
                            {
                                final BufferedImage image = ImageIO.read(in);
                                if (image != null)
                                {
                                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    ImageIO.write(image, "PNG", baos);
                                    data = baos.toByteArray();
                                    return;
                                }
                            }
                            data = in.readAllBytes();
                        }
                    }
                    catch (final Exception e)
                    {
                        LOG.debug("Could not read resource at URI: " + uri, e);
                    }
                }

                @Override
                public InputStream getStream()
                {
                    loadData();
                    return data != null ? new ByteArrayInputStream(data) : null;
                }

                @Override
                public Reader getReader()
                {
                    final InputStream in = getStream();
                    return in != null ? new InputStreamReader(in, StandardCharsets.UTF_8) : null;
                }
            };
        }
    }
}
