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
package com.xceptance.xlt.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.xceptance.common.collection.ConcurrentLRUCache;
import com.xceptance.xlt.api.util.XltLogger;

import freemarker.core.HTMLOutputFormat;
import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * The {@link FreeMarkerReportRenderer} class implements the {@link ReportRenderer} interface using the
 * FreeMarker template engine.
 *
 * @author rschwietzke
 */
public class FreeMarkerReportRenderer implements ReportRenderer
{
    private final RendererConfiguration config;

    private final Configuration freemarkerConfig;

    private final ConcurrentLRUCache<File, NodeModel> xmlCache = new ConcurrentLRUCache<>(10);

    public FreeMarkerReportRenderer(final RendererConfiguration config)
    {
        this.config = config;

        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_34);
        freemarkerConfig.setDefaultEncoding("UTF-8");
        freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        freemarkerConfig.setLogTemplateExceptions(false);
        freemarkerConfig.setWrapUncheckedExceptions(true);
        freemarkerConfig.setFallbackOnNullLoopVariable(false);
        freemarkerConfig.setOutputFormat(HTMLOutputFormat.INSTANCE);

        try
        {
            freemarkerConfig.setDirectoryForTemplateLoading(new File(config.getConfigDirectory(), "report-templates"));
        }
        catch (final Exception e)
        {
            XltLogger.reportLogger.error("Failed to set FreeMarker template directory", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(final File inputXmlFile, final File outputDir, final Map<String, Object> parameters) throws Exception
    {
        final NodeModel xmlModel = parseXml(inputXmlFile);
        parameters.put("report", xmlModel);

        final List<String> templateFileNames = config.getTemplateFileNames();
        final List<String> outputFileNames = config.getOutputFileNames();

        for (int i = 0; i < templateFileNames.size(); i++)
        {
            final String templateFileName = templateFileNames.get(i);
            if (templateFileName != null)
            {
                final File outputFile = new File(outputDir, outputFileNames.get(i));
                processTemplate(templateFileName, parameters, outputFile);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(final File inputXmlFile, final File outputFile, final String templateOrStyleSheet, final Map<String, Object> parameters) throws Exception
    {
        final NodeModel xmlModel = getXmlModel(inputXmlFile);
        parameters.put("report", xmlModel);

        processTemplate(templateOrStyleSheet, parameters, outputFile);
    }

    /**
     * Returns the {@link NodeModel} for the given XML file, using a cache if possible.
     *
     * @param file
     *            the XML file
     * @return the node model
     * @throws Exception
     *             if an error occurs during parsing
     */
    private NodeModel getXmlModel(final File file) throws Exception
    {
        NodeModel model = xmlCache.get(file);
        if (model == null)
        {
            model = parseXml(file);
            xmlCache.put(file, model);
        }
        return model;
    }

    /**
     * Parses the given XML file into a {@link NodeModel} for FreeMarker.
     *
     * @param file
     *            the XML file to parse
     * @return the node model
     * @throws Exception
     *             if an error occurs during parsing
     */
    private NodeModel parseXml(final File file) throws Exception
    {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(file);

        return NodeModel.wrap(doc);
    }

    /**
     * Processes the specified FreeMarker template and writes the result to the given output file.
     *
     * @param templateName
     *            the name of the template file
     * @param dataModel
     *            the data model to use for rendering
     * @param outputFile
     *            the output file to write to
     * @throws Exception
     *             if an error occurs during processing
     */
    private void processTemplate(final String templateName, final Map<String, Object> dataModel, final File outputFile) throws Exception
    {
        final Template template = freemarkerConfig.getTemplate(templateName);
        try (Writer out = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))
        {
            template.process(dataModel, out);
        }
}
    }
