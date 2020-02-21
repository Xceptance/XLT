package com.xceptance.xlt.report.util;

import java.awt.Color;

import org.jfree.data.time.TimeSeries;

public class TimeSeriesConfiguration
{
    public enum Style
    {
        LINE, DASH, DOT
    }

    private final TimeSeries timeSeries;

    private final Color color;

    private final Style style;

    public TimeSeriesConfiguration(final TimeSeries timeSeries)
    {
        this(timeSeries, null, Style.LINE);
    }

    public TimeSeriesConfiguration(final TimeSeries timeSeries, final Color color, final Style style)
    {
        this.timeSeries = timeSeries;
        this.color = color;
        this.style = style;
    }

    public TimeSeries getTimeSeries()
    {
        return timeSeries;
    }

    public Color getColor()
    {
        return color;
    }

    public Style getStyle()
    {
        return style;
    }
}
