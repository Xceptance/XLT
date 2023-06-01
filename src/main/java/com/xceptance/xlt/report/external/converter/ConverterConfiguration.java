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
package com.xceptance.xlt.report.external.converter;

import java.io.File;
import java.io.IOException;

import com.xceptance.common.util.AbstractConfiguration;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.XltExecutionContext;
import com.xceptance.xlt.report.external.config.DataFileConfig;

/**
 * Configuration of external data converters.
 * 
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class ConverterConfiguration extends AbstractConfiguration
{
    private static final Object SYNC = new Object();

    private static boolean INITIALIZED = false;

    private static final String PROP_PREFIX = XltConstants.XLT_PACKAGE_PATH + ".reportgenerator.";

    private static final String PROP_CHARTS_PREFIX = PROP_PREFIX + "charts.";

    private static final String PROP_CHARTS_HEIGHT = PROP_CHARTS_PREFIX + "height";

    private static final String PROP_CHARTS_WIDTH = PROP_CHARTS_PREFIX + "width";

    private static int chartsWidth;

    private static int chartsHeight;

    private File externalChartsDir;

    private long minimumChartTime;

    private long maximumChartTime;

    private DataFileConfig dataFileConfig;

    private boolean noCharts;

    /**
     * @throws IOException
     *             if loading of the properties fails
     */
    public ConverterConfiguration() throws IOException
    {
        init();
    }

    /**
     * initialize the converter<br>
     * initialization is done only once (lazy, at instantiation)
     * 
     * @throws IOException
     *             if loading of the properties fails
     */
    private void init() throws IOException
    {
        if (!INITIALIZED)
        {
            synchronized (SYNC)
            {
                if (!INITIALIZED)
                {
                    final File configDirectory = XltExecutionContext.getCurrent().getXltConfigDir();

                    loadProperties(new File(configDirectory, XltConstants.LOAD_REPORT_PROPERTY_FILENAME));

                    chartsWidth = getIntProperty(PROP_CHARTS_WIDTH, 600);
                    chartsHeight = getIntProperty(PROP_CHARTS_HEIGHT, 300);

                    INITIALIZED = true;
                }
            }
        }
    }

    /**
     * sets the chart end time
     * 
     * @param maximumChartTime
     *            the chart end time
     */
    public void setChartEndTime(final long maximumChartTime)
    {
        this.maximumChartTime = maximumChartTime;
    }

    /**
     * sets the chart start time
     * 
     * @param minimumChartTime
     *            the chart start time
     */
    public void setChartStartTime(final long minimumChartTime)
    {
        this.minimumChartTime = minimumChartTime;
    }

    /**
     * set the directory for charts of external data
     * 
     * @param chartsDir
     *            directory for charts of external data
     */
    public void setChartsDir(final File chartsDir)
    {
        externalChartsDir = chartsDir;
    }

    /**
     * returns the chart end time (in ms)
     * 
     * @return chart end time
     */
    public long getChartEndTime()
    {
        return maximumChartTime;
    }

    /**
     * returns the chart start time (in ms)
     * 
     * @return chart start time
     */
    public long getChartStartTime()
    {
        return minimumChartTime;
    }

    /**
     * returns the chart height
     * 
     * @return chart height
     */
    public int getChartHeight()
    {
        return chartsHeight;
    }

    /**
     * return the chart width
     * 
     * @return chart width
     */
    public int getChartWidth()
    {
        return chartsWidth;
    }

    /**
     * returns the charts directory
     * 
     * @return charts directory
     */
    public File getExternalChartsDir()
    {
        return externalChartsDir;
    }

    /**
     * @return parser class name
     */
    public String getParserClassName()
    {
        return dataFileConfig.getParserClassName();
    }

    public DataFileConfig getDataFile()
    {
        return dataFileConfig;
    }

    public void setDataFile(final DataFileConfig dataFileConfig)
    {
        this.dataFileConfig = dataFileConfig;
    }

    public boolean shouldChartsGenerated()
    {
        return !noCharts;
    }

    public void disableChartGeneration()
    {
        noCharts = true;
    }
}
