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
import java.util.Map;

/**
 * The {@link ReportRenderer} interface defines the contract for rendering a report from an XML data file
 * using a specific template engine (e.g., XSLT or FreeMarker).
 *
 * @author rschwietzke
 */
public interface ReportRenderer
{
    /**
     * Renders the report using the specified input XML, output directory, and parameters. The implementation is
     * responsible for determining the output files and templates/stylesheets to use (e.g., from configuration).
     *
     * @param inputXmlFile
     *            the input XML file
     * @param outputDir
     *            the output directory
     * @param parameters
     *            the rendering parameters
     * @throws Exception
     *             if an error occurs during rendering
     */
    void render(File inputXmlFile, File outputDir, Map<String, Object> parameters) throws Exception;

    /**
     * Renders a single output file using the specified template or stylesheet.
     *
     * @param inputXmlFile
     *            the input XML file
     * @param outputFile
     *            the output file to create
     * @param templateOrStyleSheet
     *            the name of the template or the path to the stylesheet
     * @param parameters
     *            the rendering parameters
     * @throws Exception
     *             if an error occurs during rendering
     */
    void render(File inputXmlFile, File outputFile, String templateOrStyleSheet, Map<String, Object> parameters) throws Exception;
}
