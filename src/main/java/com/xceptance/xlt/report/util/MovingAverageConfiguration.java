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
package com.xceptance.xlt.report.util;

import java.util.Arrays;
import java.util.List;

/**
 * Holds the configuration for a moving average.
 */
public class MovingAverageConfiguration
{
    /**
     * Moving average type.
     */
    public enum MovingAverageType
    {
        /**
         * Average over a percentage of data points. E.g. if we have 1000 data points, a percentage average of 5% will calculate the average over the last 50 data points.
         */
        PERCENTAGE("percentage"),
        
        /**
         * Average over a time interval. E.g. a time average of 30s will calculate the average over all data points in the last 30s.
         */
        TIME("time");

        /**
         * The name used to reference the type, e.g. in property files.
         */
        private final String name;

        MovingAverageType(final String name)
        {
            this.name = name;
        }

        /**
         * Get the name of the moving average type.
         *
         * @return the name of the moving average type
         */
        public String getName()
        {
            return name;
        }

        /**
         * Get the names of all available moving average types.
         *
         * @return a list of all moving average type names
         */
        public static List<String> getNames()
        {
            return Arrays.stream(values()).map(MovingAverageType::getName).toList();
        }
    }

    /**
     * The type of the moving average configuration.
     */
    private final MovingAverageType type;

    /**
     * The value of the moving average configuration. For "percentage" averages this is an integer percentage value (e.g. a value of "25" means "25%"). For "time" averages this is a time interval in seconds.
     */
    private final int value;
    
    /**
     * The name of the moving average configuration.
     */
    private final String name;

    private MovingAverageConfiguration(final MovingAverageType type, final int value, final String name)
    {
        this.type = type;
        this.value = value;
        this.name = name;
    }

    /**
     * Get the type of the moving average configuration.
     *
     * @return
     *          the type of the moving average configuration
     */
    public MovingAverageType getType()
    {
        return type;
    }

    /**
     * Get the value of the moving average configuration.
     *
     * @return
     *          the value of the moving average configuration. For "percentage" averages this is a percentage integer value (e.g. a value of "25" means "25%"). For "time" averages this is a time interval in seconds.
     */
    public int getValue()
    {
        return value;
    }

    /**
     * Get the name of the moving average configuration.
     *
     * @return the name of the moving average configuration
     */
    public String getName()
    {
        return name;
    }

    /**
     * Create a moving average configuration of type "percentage" with the given percentage value.
     *
     * @param percentage an integer value representing the percentage (e.g. a value of "25" means "25%")
     * @return the "percentage" moving average configuration
     */
    public static MovingAverageConfiguration createPercentageConfig(final int percentage)
    {
        return new MovingAverageConfiguration(MovingAverageType.PERCENTAGE, percentage, percentage + "%");
    }

    /**
     * Create a moving average configuration of type "time" with the given time interval in seconds.
     *
     * @param seconds an integer value representing a time interval in seconds (e.g. a value of "30" means that the average should be calculated over the last 30 seconds)
     * @return the "time" moving average configuration
     */
    public static MovingAverageConfiguration createTimeConfig(final int seconds)
    {
        return createTimeConfig(seconds, seconds + "s");
    }

    /**
     * Create a moving average configuration of type "time" with the given time interval in seconds and the given custom name. This can be used to provide a name that is easier to comprehend than a number of seconds (e.g. the name "5m30s" might be more helpful than "330s").
     *
     * @param seconds an integer value representing the time interval in seconds (e.g. a value of "30" means that the average should be calculated over the last 30 seconds)
     * @param name the custom name for this configuration
     * @return the "time" moving average configuration
     */
    public static MovingAverageConfiguration createTimeConfig(final int seconds, final String name)
    {
        return new MovingAverageConfiguration(MovingAverageType.TIME, seconds, name);
    }

    @Override
    public String toString()
    {
        return String.format("MovingAverageConfiguration [type=%s, value=%d, name=%s]", type, value, name);
    }
}
