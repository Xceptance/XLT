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
import java.util.List;

/**
 * The {@link RendererConfiguration} interface provides access to the configuration settings
 * required by a {@link ReportRenderer}.
 *
 * @author rschwietzke
 */
public interface RendererConfiguration
{
    /**
     * Returns the configuration directory.
     *
     * @return the configuration directory
     */
    File getConfigDirectory();

    /**
     * Returns the list of XSL style sheet file names to use for rendering.
     *
     * @return the style sheet file names
     */
    List<String> getStyleSheetFileNames();

    /**
     * Returns the list of template file names to use for rendering.
     *
     * @return the template file names
     */
    List<String> getTemplateFileNames();

    /**
     * Returns the list of output file names to use for rendering.
     *
     * @return the output file names
     */
    List<String> getOutputFileNames();

    /**
     * Returns the root directory where XSL style sheets are located.
     *
     * @return the XSL style sheet root directory
     */
    File getXsltStyleSheetRootDirectory();
}
