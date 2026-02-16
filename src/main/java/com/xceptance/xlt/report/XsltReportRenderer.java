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
import java.util.Map;

import com.xceptance.xlt.common.XltConstants;
import com.xceptance.common.xml.XSLTUtils;

/**
 * The {@link XsltReportRenderer} class implements the {@link ReportRenderer} interface using the XSLT
 * transformation engine.
 *
 * @author rschwietzke
 */
public class XsltReportRenderer implements ReportRenderer
{
    private final ReportGeneratorConfiguration config;

    public XsltReportRenderer(final ReportGeneratorConfiguration config)
    {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(final File inputXmlFile, final File outputDir, final Map<String, Object> parameters) throws Exception
    {
        final List<String> styleSheetFileNames = config.getStyleSheetFileNames();
        final List<String> outputFileNames = config.getOutputFileNames();

        for (int i = 0; i < styleSheetFileNames.size(); i++)
        {
            final String styleSheetFileName = styleSheetFileNames.get(i);
            if (styleSheetFileName != null)
            {
                final File outputFile = new File(outputDir, outputFileNames.get(i));
                final File styleSheetFile = new File(new File(config.getConfigDirectory(), XltConstants.LOAD_REPORT_XSL_PATH),
                                                     styleSheetFileName);

                XSLTUtils.transform(inputXmlFile, outputFile, styleSheetFile, parameters);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(final File inputXmlFile, final File outputFile, final String templateOrStyleSheet, final Map<String, Object> parameters) throws Exception
    {
        final File styleSheetFile = new File(templateOrStyleSheet);
        XSLTUtils.transform(inputXmlFile, outputFile, styleSheetFile, parameters);
    }
}
