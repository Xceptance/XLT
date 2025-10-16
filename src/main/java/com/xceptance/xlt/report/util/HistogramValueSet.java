/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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

import org.jfree.data.xy.XYIntervalSeries;

/**
 * Note: We cannot use JFreeChart classes here since they want all data at once, but we need incremental updates.
 */

public class HistogramValueSet
{
    private final int countPerBin[];

    private final double maxValue;

    private final double minValue;

    private final int numberOfBins;

    private final double binWidth;

    public HistogramValueSet(final double minValue, final double maxValue, final int numberOfBins)
    {
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.numberOfBins = numberOfBins;

        countPerBin = new int[numberOfBins];
        binWidth = (maxValue - minValue) / numberOfBins;
    }

    public void addValue(final double value)
    {
        final int binIndex;

        if (value <= binWidth)
        {
            binIndex = 0;
        }
        else if (value > maxValue)
        {
            binIndex = numberOfBins - 1;
        }
        else
        {
            binIndex = ((int) Math.ceil(value / binWidth)) - 1;
        }

        countPerBin[binIndex]++;
    }

    public double getBinWidth()
    {
        return binWidth;
    }

    public int[] getCountPerBin()
    {
        return countPerBin;
    }

    public double getMaxValue()
    {
        return maxValue;
    }

    public double getMinValue()
    {
        return minValue;
    }

    public int getNumberOfBins()
    {
        return numberOfBins;
    }

    public XYIntervalSeries toSeries(final String seriesName)
    {
        final XYIntervalSeries series = new XYIntervalSeries(seriesName);

        double xLow;
        double xHigh = minValue;

        for (int i = 0; i < countPerBin.length; i++)
        {
            xLow = xHigh;
            xHigh = xLow + binWidth;
            final double y = countPerBin[i];

            series.add(xLow, xLow, xHigh, y, 0, y);
        }

        return series;
    }
}
