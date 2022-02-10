/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.external.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author matthias.ullrich
 */
public class ChartConfig
{
    @XmlTransient
    private static final String DEFAULT_CHART_TITLE = "Unnamed";

    @XmlTransient
    private static final String DEFAULT_Y_AXIS_TITLE = "Values";

    @XmlTransient
    private static final String DEFAULT_X_AXIS_TITLE = "Time";

    private String title = DEFAULT_CHART_TITLE;

    private String yAxisTitle = DEFAULT_Y_AXIS_TITLE;

    private String yAxisTitle2 = DEFAULT_Y_AXIS_TITLE;

    private String xAxisTitle = DEFAULT_X_AXIS_TITLE;

    private final List<SeriesConfig> seriesCollection = new ArrayList<SeriesConfig>();

    /**
     * title of the y-axis
     * 
     * @return title of the y-axis
     */
    @XmlAttribute(name = "yAxisTitle", required = false)
    public String getYAxisTitle()
    {
        return yAxisTitle != null ? yAxisTitle : DEFAULT_Y_AXIS_TITLE;
    }

    /**
     * title of the second y-axis
     * 
     * @return title of the second y-axis
     */
    @XmlAttribute(name = "yAxisTitle2", required = false)
    public String getYAxisTitle2()
    {
        return yAxisTitle2 != null ? yAxisTitle2 : DEFAULT_Y_AXIS_TITLE;
    }

    /**
     * title of the x-axis
     * 
     * @return title of the x-axis
     */
    @XmlAttribute(name = "xAxisTitle", required = false)
    public String getXAxisTitle()
    {
        return xAxisTitle != null ? xAxisTitle : DEFAULT_X_AXIS_TITLE;
    }

    /**
     * chart title
     * 
     * @return chart title
     */
    @XmlAttribute(name = "title", required = true)
    public String getTitle()
    {
        return title != null ? title : DEFAULT_CHART_TITLE;
    }

    /**
     * @return
     */
    @XmlElementWrapper(name = "seriesCollection", required = true)
    @XmlElement(name = "series", required = true)
    public List<SeriesConfig> getSeriesCollection()
    {
        return seriesCollection;
    }

    /*
     * DO NOT REMOVE METHODS BELOW !
     */

    @SuppressWarnings("unused")
    private void setTitle(final String title)
    {
        this.title = title;
    }

    @SuppressWarnings("unused")
    private void setYAxisTitle(final String yAxisTitle)
    {
        this.yAxisTitle = yAxisTitle;
    }

    @SuppressWarnings("unused")
    private void setYAxisTitle2(final String yAxisTitle2)
    {
        this.yAxisTitle2 = yAxisTitle2;
    }

    @SuppressWarnings("unused")
    private void setXAxisTitle(final String xAxisTitle)
    {
        this.xAxisTitle = xAxisTitle;
    }
}
