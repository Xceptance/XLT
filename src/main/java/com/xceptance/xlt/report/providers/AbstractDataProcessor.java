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
package com.xceptance.xlt.report.providers;

import java.io.File;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.api.report.ReportProviderConfiguration;
import com.xceptance.xlt.report.ReportGeneratorConfiguration.ChartCappingInfo;

/**
 * The {@link AbstractDataProcessor} class provides common functionality of a typical data processor. A data processor
 * is responsible to calculate statistic information for exactly one timer series.
 */
public abstract class AbstractDataProcessor
{
    private int chartHeight;

    private File chartsDir;

    private int chartWidth;

    private File csvDir;

    private int movingAveragePercentage;

    private ChartCappingInfo chartCappingInfo;

    /**
     * The name of the timer processed by this data processor.
     */
    private String name;

    /**
     * The report provider to which this data processor belongs.
     */
    private final AbstractReportProvider reportProvider;

    /**
     * Creates a new {@link AbstractDataProcessor} instance.
     * 
     * @param name
     *            the timer name
     * @param reportProvider
     *            the report provider owning this data processor
     */
    protected AbstractDataProcessor(final String name, final AbstractReportProvider reportProvider)
    {
        this.name = name;
        this.reportProvider = reportProvider;

        final ReportProviderConfiguration config = reportProvider.getConfiguration();

        setChartDir(config.getChartDirectory());
        setCsvDir(config.getCsvDirectory());

        setChartWidth(config.getChartWidth());
        setChartHeight(config.getChartHeight());

        setMovingAveragePercentage(config.getMovingAveragePercentage());
    }

    /**
     * Returns the value of the 'chartHeight' attribute.
     * 
     * @return the value of chartHeight
     */
    public int getChartHeight()
    {
        return chartHeight;
    }

    /**
     * Returns the value of the 'chartsDir' attribute.
     * 
     * @return the value of chartsDir
     */
    public File getChartDir()
    {
        return chartsDir;
    }

    /**
     * Returns the value of the 'chartWidth' attribute.
     * 
     * @return the value of chartWidth
     */
    public int getChartWidth()
    {
        return chartWidth;
    }

    /**
     * Returns the report generator's configuration. Useful to get access to provider-specific properties stored in the
     * global configuration file.
     * 
     * @return the report generator configuration
     */
    public ReportProviderConfiguration getConfiguration()
    {
        return reportProvider.getConfiguration();
    }

    /**
     * Returns the value of the 'csvDir' attribute.
     * 
     * @return the value of csvDir
     */
    public File getCsvDir()
    {
        return csvDir;
    }

    /**
     * Returns the value of the 'endTime' attribute.
     * 
     * @return the value of endTime
     */
    public long getEndTime()
    {
        return getConfiguration().getChartEndTime();
    }

    /**
     * Returns the value of the 'movingAveragePercentage' attribute.
     * 
     * @return the value of movingAveragePercentage
     */
    public int getMovingAveragePercentage()
    {
        return movingAveragePercentage;
    }

    /**
     * Returns the timer name.
     * 
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the timer name.
     * 
     * @param name
     *            the new name
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * Returns the report provider owning this data processor.
     * 
     * @return the report provider
     */
    public AbstractReportProvider getReportProvider()
    {
        return reportProvider;
    }

    /**
     * Returns the value of the 'startTime' attribute.
     * 
     * @return the value of startTime
     */
    public long getStartTime()
    {
        return getConfiguration().getChartStartTime();
    }

    /**
     * Processes one data record.
     * 
     * @param data
     *            the data record
     */
    public abstract void processDataRecord(Data data);

    /**
     * Sets the new value of the 'chartHeight' attribute.
     * 
     * @param chartHeight
     *            the new chartHeight value
     */
    public void setChartHeight(final int chartHeight)
    {
        this.chartHeight = chartHeight;
    }

    /**
     * Sets the new value of the 'chartsDir' attribute.
     * 
     * @param chartsDir
     *            the new chartsDir value
     */
    public void setChartDir(final File chartsDir)
    {
        this.chartsDir = chartsDir;

        chartsDir.mkdirs();
    }

    /**
     * Sets the new value of the 'chartWidth' attribute.
     * 
     * @param chartWidth
     *            the new chartWidth value
     */
    public void setChartWidth(final int chartWidth)
    {
        this.chartWidth = chartWidth;
    }

    /**
     * Sets the new value of the 'csvDir' attribute.
     * 
     * @param csvDir
     *            the new csvDir value
     */
    public void setCsvDir(final File csvDir)
    {
        this.csvDir = csvDir;

        csvDir.mkdirs();
    }

    /**
     * Sets the new value of the 'movingAveragePercentage' attribute.
     * 
     * @param movingAveragePercentage
     *            the new movingAveragePercentage value
     */
    public void setMovingAveragePercentage(final int movingAveragePercentage)
    {
        this.movingAveragePercentage = movingAveragePercentage;
    }

    /**
     * Sets the chart capping info that describes how to cap a run time chart.
     * 
     * @param cappingInfo
     *            the capping info
     */
    protected void setChartCappingInfo(final ChartCappingInfo cappingInfo)
    {
        this.chartCappingInfo = cappingInfo;
    }

    /**
     * @return the chartCappingInfo
     */
    protected ChartCappingInfo getChartCappingInfo()
    {
        return chartCappingInfo;
    }
}
