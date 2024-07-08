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
package com.xceptance.xlt.report.providers;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.xceptance.xlt.report.util.CustomMapConverter;
import com.xceptance.xlt.report.util.LabelSetConverter;

/**
 * Represents the timer statistics in a test report. The statistics is generated from a series of timer events.
 */
@XStreamAlias("timer")
public class TimerReport
{
    /**
     * The timer name.
     */
    public String name;

    /**
     * The labels.
     */
    @XStreamConverter(LabelSetConverter.class)
    public Set<String> labels;

    /**
     * The number how often the timer has fired.
     */
    public int count;

    /**
     * The calculated number how often the timer has fired per second.
     */
    public BigDecimal countPerSecond;

    /**
     * The calculated number how often the timer has fired per minute.
     */
    public BigDecimal countPerMinute;

    /**
     * The calculated number how often the timer has fired per hour.
     */
    public BigDecimal countPerHour;

    /**
     * The calculated number how often the timer has fired per day.
     */
    public BigDecimal countPerDay;

    /**
     * The number how often the timed item has failed.
     */
    public int errors;
    
    /**
     * The calculated number which percentage of the total timed items (see count) has failed.
     */
    public BigDecimal errorPercentage;

    /**
     * The minimum timer runtime.
     */
    public long min;

    /**
     * The maximum timer runtime.
     */
    public long max;

    /**
     * The median of the timer runtime.
     */
    public BigDecimal median;

    /**
     * The mean of the timer runtime.
     */
    public BigDecimal mean;

    /**
     * The deviation of the timer runtime.
     */
    public BigDecimal deviation;

    /**
     * Additional information is required to colorize the requests later.
     */
    public String colorizationGroupName;

    /**
     * The configured percentiles of the timer runtime.
     */
    @XStreamConverter(CustomMapConverter.class)
    public Map<String, BigDecimal> percentiles = new LinkedHashMap<>();
}
