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
package com.xceptance.xlt.report.trendreport;

import java.util.Date;

/**
 *
 */
public class TrendValue implements Comparable<TrendValue>
{
    public Double countPerSecond;

    public Integer errors;

    public Integer maximum;

    public Double mean;

    public Double median;

    public Integer minimum;

    public String reportName;

    public Date reportDate;

    public String reportComment;

    /**
     * Constructor.
     * 
     * @param median
     * @param mean
     * @param minimum
     * @param maximum
     */
    public TrendValue(final Double median, final Double mean, final Integer minimum, final Integer maximum, final String reportName,
                      final Date reportDate, final String reportComment, final Integer errors, final Double countPerSecond)
    {
        this.median = median;
        this.mean = mean;
        this.minimum = minimum;
        this.maximum = maximum;
        this.reportName = reportName;
        this.reportDate = reportDate;
        this.reportComment = reportComment;
        this.errors = errors;
        this.countPerSecond = countPerSecond;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final TrendValue value)
    {
        int result = reportDate.compareTo(value.reportDate);

        if (result == 0)
        {
            result = reportName.compareTo(value.reportName);
        }

        return result;
    }
}
