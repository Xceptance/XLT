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
package com.xceptance.xlt.report.trendreport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.xceptance.common.util.AbstractConfiguration;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.XltExecutionContext;

/**
 * The trend report generator's configuration.
 */
public class TrendReportGeneratorConfiguration extends AbstractConfiguration
{
    private static final String PROP_PREFIX = XltConstants.XLT_PACKAGE_PATH + ".trendreportgenerator.";

    private static final String PROP_CHARTS_PREFIX = PROP_PREFIX + "charts.";

    private static final String PROP_CHARTS_HEIGHT = PROP_CHARTS_PREFIX + "height";

    private static final String PROP_CHARTS_WIDTH = PROP_CHARTS_PREFIX + "width";

    private static final String PROP_CHARTS_AUTO_RANGE_INCLUDES_ZERO = PROP_CHARTS_PREFIX + "autoRangeIncludesZero";

    private static final String PROP_REPORTS_ROOT_DIR = PROP_PREFIX + "reports";

    private static final String PROP_THREAD_COUNT = PROP_PREFIX + "threads";

    private static final String PROP_TRANSFORMATIONS_PREFIX = PROP_PREFIX + "transformations.";

    private static final String PROP_TRANSFORMATIONS_STYLE_SHEET_FILE_SUFFIX = ".styleSheetFileName";

    private static final String PROP_TRANSFORMATIONS_OUTPUT_FILE_SUFFIX = ".outputFileName";

    private final int chartsHeight;

    private final int chartsWidth;

    private final boolean chartsAutoRangeIncludesZero;

    private final File configDirectory;

    private final File homeDirectory;

    private final File reportsRootDirectory;

    private final List<String> styleSheetFileNames;

    private final List<String> outputFileNames;

    private final int threadCount;

    public TrendReportGeneratorConfiguration() throws IOException
    {
        homeDirectory = XltExecutionContext.getCurrent().getXltHomeDir();
        configDirectory = XltExecutionContext.getCurrent().getXltConfigDir();

        loadProperties(new File(configDirectory, XltConstants.TREND_REPORT_PROPERTY_FILENAME));

        File reportsRootDir = getFileProperty(PROP_REPORTS_ROOT_DIR, new File(homeDirectory, XltConstants.REPORT_ROOT_DIR));
        if (!reportsRootDir.isAbsolute())
        {
            reportsRootDir = new File(homeDirectory, reportsRootDir.getPath());
        }

        reportsRootDirectory = reportsRootDir;

        chartsWidth = getIntProperty(PROP_CHARTS_WIDTH, 600);
        chartsHeight = getIntProperty(PROP_CHARTS_HEIGHT, 300);
        chartsAutoRangeIncludesZero = getBooleanProperty(PROP_CHARTS_AUTO_RANGE_INCLUDES_ZERO, false);

        threadCount = getIntProperty(PROP_THREAD_COUNT, -1);

        // load the transformation configuration
        outputFileNames = new ArrayList<String>();
        styleSheetFileNames = new ArrayList<String>();

        readTransformations(outputFileNames, styleSheetFileNames);
    }

    /**
     * Returns the configured height of charts (in pixels).
     * 
     * @return the chart height
     */
    public int getChartHeight()
    {
        return chartsHeight;
    }

    /**
     * Returns the configured width of charts (in pixels).
     * 
     * @return the chart width
     */
    public int getChartWidth()
    {
        return chartsWidth;
    }

    /**
     * Returns configuration of whether zero is part of automatically discovered range.
     *
     * @return true if zero is part of automatically discovered range
     */
    public boolean getChartAutoRangeIncludesZero()
    {
        return chartsAutoRangeIncludesZero;
    }

    /**
     * Returns XLT's configuration directory.
     * 
     * @return the configuration directory
     */
    public File getConfigDirectory()
    {
        return configDirectory;
    }

    /**
     * Returns XLT's home directory.
     * 
     * @return the home directory
     */
    public File getHomeDirectory()
    {
        return homeDirectory;
    }

    /**
     * Returns XLT's reports directory.
     * 
     * @return the reports directory
     */
    public File getReportsRootDirectory()
    {
        return reportsRootDirectory;
    }

    /**
     * Return a list of output files names
     * 
     * @return a list of files names
     */
    public List<String> getOutputFileNames()
    {
        return outputFileNames;
    }

    /**
     * Return a list of style sheets for rendering
     * 
     * @return a list of style sheet file names
     */
    public List<String> getStyleSheetFileNames()
    {
        return styleSheetFileNames;
    }

    /**
     * Returns the number of threads to use during report generation.
     * 
     * @return the thread count
     */
    public int getThreadCount()
    {
        return threadCount;
    }

    /**
     * Reads the transformation configurations. The style sheet file names and output file names for each transformation
     * are added to the respective lists passed as parameters.
     * 
     * @param outputFileNames
     *            the list of output file names
     * @param styleSheetFileNames
     *            the list of style sheet file names
     */
    private void readTransformations(final List<String> outputFileNames, final List<String> styleSheetFileNames)
    {
        final Set<String> keys = getPropertyKeyFragment(PROP_TRANSFORMATIONS_PREFIX);
        for (final String key : keys)
        {
            final String propertyPrefix = PROP_TRANSFORMATIONS_PREFIX + key;

            final File outputFile = getFileProperty(propertyPrefix + PROP_TRANSFORMATIONS_OUTPUT_FILE_SUFFIX);
            final File styleSheetFile = getFileProperty(propertyPrefix + PROP_TRANSFORMATIONS_STYLE_SHEET_FILE_SUFFIX);

            outputFileNames.add(outputFile.getPath());
            styleSheetFileNames.add(styleSheetFile.getPath());
        }
    }
}
