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
