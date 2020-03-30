/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.engine;

import java.text.ParseException;
import java.util.Properties;

import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.common.util.ParseUtils;
import com.xceptance.xlt.api.util.XltLogger;

/**
 * The {@link AbstractCustomSampler} provides the common functionality of custom samplers.
 * 
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractCustomSampler
{
    private Properties properties = new Properties();

    private long interval = -1;

    private String name = null;

    /**
     * Executed once at the start of the sampler.
     */
    public void initialize()
    {
    }

    /**
     * Execute the sampler.
     */
    abstract public double execute();

    /**
     * Executed once when the sampler get shut down.
     */
    public void shutdown()
    {
    }

    /**
     * Set the execution interval.
     * 
     * @param interval
     *            positive interval value in milliseconds (0 or higher)
     * @see AbstractCustomSampler#setInterval(String)
     */
    public void setInterval(final long interval)
    {
        ParameterCheckUtils.isGreaterThan((int) interval, -1, "interval");
        this.interval = interval;
    }

    /**
     * Set the execution interval. If using the convenience approach to set time periods (for example with &quot;3h 5m
     * 7s&quot;) in XLT the resulting number of milliseconds is computed internally.
     * 
     * @param interval
     *            the milliseconds as in {@link #setInterval(long)} or a time period as with the convenient way
     */
    public void setInterval(final String interval)
    {
        final long samplingInterval;
        if (interval.matches("\\d+"))
        {
            XltLogger.runTimeLogger.info("The interval property now supports the common XLT way to specify a duration which we recommend!");
            samplingInterval = Long.parseLong(interval);
        }
        else
        {
            try
            {
                samplingInterval = ParseUtils.parseTimePeriod(interval) * 1000L;
            }
            catch (final ParseException e)
            {
                throw new IllegalArgumentException("Invalid value for interval : \"" + interval + "\"", e);
            }
        }
        setInterval(samplingInterval);
    }

    /**
     * Get the execution interval.
     * 
     * @return the execution interval
     */
    public long getInterval()
    {
        return interval;
    }

    /**
     * Set the sampler name. Setting the name after the sampler has started will have no effect.
     * 
     * @param name
     *            sampler name
     */
    public void setName(final String name)
    {
        ParameterCheckUtils.isNonEmptyString(name, "name");
        this.name = name;
    }

    /**
     * Get the sampler name.
     * 
     * @return sampler name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get properties of this sampler.
     * 
     * @return all properties
     */
    public Properties getProperties()
    {
        return properties;
    }

    /**
     * Set properties of this sampler.
     * 
     * @param properties
     *            properties for that sampler
     */
    public void setProperties(final Properties properties)
    {
        this.properties = properties;
    }
}
