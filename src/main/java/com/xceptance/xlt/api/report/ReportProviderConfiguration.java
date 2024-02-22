/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.report;

import java.io.File;
import java.util.Properties;

/**
 * The {@link ReportProviderConfiguration} interface provides access to general report generator settings as well as to
 * report provider specific properties, which are both stored in the global configuration file.
 */
public interface ReportProviderConfiguration
{
    /**
     * Returns the directory to which charts are saved.
     * 
     * @return the chart directory
     */
    public File getChartDirectory();

    /**
     * Returns the maximum date/time value to be shown in charts.
     * <p>
     * If a report provider generates chart images, then this value should be the upper limit of the time range shown.
     * Setting the time range this way guarantees that all charts in the report show the same time period and,
     * therefore, can be compared more easily.
     * <p>
     * Note that this value is valid only after all data record files have been read, i.e. you should call this method
     * from {@link ReportProvider#createReportFragment()} only.
     * 
     * @return the maximum date
     */
    public long getChartEndTime();

    /**
     * Returns the preferred height of chart images (in pixels).
     * 
     * @return the chart height
     */
    public int getChartHeight();

    /**
     * Returns the minimum date/time value to be shown in charts.
     * <p>
     * If a report provider generates chart images, then this value should be the lower limit of the time range shown.
     * Setting the time range this way guarantees that all charts in the report show the same time period and,
     * therefore, can be compared more easily.
     * <p>
     * Note that this value is valid only after all data record files have been read, i.e. you should call this method
     * from {@link ReportProvider#createReportFragment()} only.
     * 
     * @return the minimum date
     */
    public long getChartStartTime();

    /**
     * Returns the preferred width of chart images (in pixels).
     * 
     * @return the chart width
     */
    public int getChartWidth();

    /**
     * Returns the directory to which CSV files are saved.
     * 
     * @return the CSV directory
     */
    public File getCsvDirectory();

    /**
     * Returns the preferred percentage of the available values used to calculate moving average values. For example,
     * with 5 percent and 1000 values, the moving average is generated from the last 50 values.
     * 
     * @return the percentage
     */
    public int getMovingAveragePercentage();

    /**
     * Returns all the settings from the file "xlt/config/reportgenerator.properties" as raw properties. Use these
     * properties to get access to the provider-specific configuration if there is one.
     * 
     * @return the properties
     */
    public Properties getProperties();

    /**
     * Returns the test report's root directory. This is the directory to which the report will be generated.
     * 
     * @return the report directory
     */
    public File getReportDirectory();

    /**
     * Returns whether or not charts should be generated.
     * 
     * @return <code>true</code> if charts should be generated, <code>false</code> otherwise
     */
    public boolean shouldChartsGenerated();
}
