package com.xceptance.xlt.report.external.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author matthias.ullrich
 */
public class SeriesConfig
{
    @XmlTransient
    private static final int DEFAULT_AXIS = 1;

    @XmlTransient
    private static final String DEFAULT_DATA_TYPE = "double";

    private String valueName;

    private String title;

    private String description;

    private String color;

    private int axis = DEFAULT_AXIS;

    private String dataType = DEFAULT_DATA_TYPE;

    private String average;

    private String averageColor;

    /**
     * Get the series name.
     * 
     * @return series name
     */
    @XmlAttribute(name = "valueName", required = true)
    public String getValueName()
    {
        return valueName;
    }

    /**
     * series title
     * 
     * @return series title
     */
    @XmlAttribute(name = "title", required = false)
    public String getTitle()
    {
        return title != null ? title : valueName;
    }

    /**
     * series description
     * 
     * @return series description
     */
    @XmlAttribute(name = "description", required = false)
    public String getDescription()
    {
        return description;
    }

    /**
     * series color
     * 
     * @return series color
     */
    @XmlAttribute(name = "color", required = false)
    public String getColor()
    {
        return color;
    }

    /**
     * Get series axis (1=left or 2=right).
     * 
     * @return series axis (1=left or 2=right)
     */
    @XmlAttribute(name = "axis", required = false)
    public int getAxis()
    {
        return (axis == 1 || axis == 2) ? axis : DEFAULT_AXIS;
    }

    /**
     * series data type (int, long, double).
     * 
     * @return series data type (int, long, double)
     */
    @XmlAttribute(name = "dataType", required = false)
    public String getDataType()
    {
        return dataType != null ? dataType : DEFAULT_DATA_TYPE;
    }

    /**
     * Get data amount to calculate series average line
     * 
     * @return data amount to calculate series average line
     */
    @XmlAttribute(name = "average", required = false)
    public String getAverage()
    {
        return average;
    }

    /**
     * Get data amount to calculate series average line
     * 
     * @return data amount to calculate series average line
     */
    @XmlAttribute(name = "averageColor", required = false)
    public String getAverageColor()
    {
        return averageColor;
    }

    /*
     * DO NOT REMOVE METHODS BELOW !
     */

    @SuppressWarnings("unused")
    private void setValueName(final String valueName)
    {
        this.valueName = valueName;
    }

    @SuppressWarnings("unused")
    private void setTitle(final String title)
    {
        this.title = title;
    }

    @SuppressWarnings("unused")
    private void setDescription(final String description)
    {
        this.description = description;
    }

    @SuppressWarnings("unused")
    private void setDataType(final String dataType)
    {
        this.dataType = dataType;
    }

    @SuppressWarnings("unused")
    private void setAverage(final String average)
    {
        this.average = average;
    }

    @SuppressWarnings("unused")
    private void setAverageColor(final String averageColor)
    {
        this.averageColor = averageColor;
    }

    @SuppressWarnings("unused")
    private void setColor(final String color)
    {
        this.color = color;
    }

    @SuppressWarnings("unused")
    private void setAxis(final int axis)
    {
        this.axis = axis;
    }
}
