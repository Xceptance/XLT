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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.xceptance.xlt.report.util.CustomMapConverter;

/**
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
@XStreamAlias("customValue")
public class CustomValueReport
{
    /**
     * The sampler name.
     */
    public String name;

    /**
     * The sampler description.
     */
    public String description;

    /**
     * The chart file name.
     */
    public String chartFilename;

    /**
     * The number how often the value was submitted in total.
     */
    public int count;

    /**
     * The number how often the value was submitted per second.
     */
    public BigDecimal countPerSecond;

    /**
     * The number how often the value was submitted per second.
     */
    public BigDecimal countPerMinute;

    /**
     * The number how often the value was submitted per second.
     */
    public BigDecimal countPerHour;

    /**
     * The number how often the value was submitted per second.
     */
    public BigDecimal countPerDay;

    /**
     * The minimum value.
     */
    public BigDecimal min;

    /**
     * The maximum value.
     */
    public BigDecimal max;

    /**
     * The mean value.
     */
    public BigDecimal mean;

    /**
     * The standard deviation.
     */
    public BigDecimal standardDeviation;

    /**
     * The configured percentiles of the timer runtime.
     */
    @XStreamConverter(CustomMapConverter.class)
    public Map<String, BigDecimal> percentiles = new LinkedHashMap<>();
}
