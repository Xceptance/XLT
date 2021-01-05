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
package com.xceptance.xlt.report.external.util;

import java.math.BigDecimal;

import com.xceptance.xlt.report.util.ReportUtils;

/**
 * @author matthias.ullrich
 */
public class StatsValueSet extends ValueSet
{
    protected double min = Double.MAX_VALUE;

    protected double max = -Double.MAX_VALUE;

    protected double sum = 0;

    protected int count = 0;

    /**
     * The sum of the square of all values.
     */
    private double sumOfSquares;

    /**
     * {@inheritDoc}
     */
    @Override
    public void addOrUpdate(final long time, final double value)
    {
        super.addOrUpdate(time, value);

        final double v = value;

        min = Math.min(min, v);
        max = Math.max(max, v);
        sumOfSquares += v * v;
        sum += v;
        count++;
    }

    /**
     * get lowest value
     * 
     * @return lowest value
     */
    public BigDecimal getMin()
    {
        return ReportUtils.convertToBigDecimal(count > 0 ? min : 0);
    }

    /**
     * get highest value
     * 
     * @return highest value
     */
    public BigDecimal getMax()
    {
        return ReportUtils.convertToBigDecimal(count > 0 ? max : 0);
    }

    /**
     * get the value count
     * 
     * @return value count
     */
    public int getCount()
    {
        return count;
    }

    /**
     * get average value
     * 
     * @return average value
     */
    public BigDecimal getAvg()
    {
        return ReportUtils.convertToBigDecimal(calculateMean());
    }

    private double calculateMean()
    {
        return count > 0 ? (sum / count) : 0;
    }

    /**
     * get the standard deviation
     * 
     * @return
     */
    public BigDecimal getStandardDeviation()
    {
        final double mean = calculateMean();
        return ReportUtils.convertToBigDecimal(Math.sqrt(sumOfSquares / count - mean * mean));
    }
}
