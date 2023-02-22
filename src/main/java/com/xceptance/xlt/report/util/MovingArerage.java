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

/**
 * Hold the data for additional moving averages.
 */
public class MovingArerage
{
    /**
     * Additional moving averages. 
     */
    public enum MovingAverages
    {
        /**
         * Percentage of all values.
         */
        PERCENTAGE_OF_VALUES,
        
        /**
         * Amount of time for moving average.
         */
        TIME_TO_USE,
        
        /**
         * Amount of requests for moving average.
         */
        AMOUNT_OF_REQUESTS;
    }
    
    /**
     * The value of used for requests and percentage. 
     */
    private int intValue = 0;
    
    /**
     * The value used for the time.
     */
    private long longValue = 0;
    
    /**
     * Given property value for the time. 
     */
    private String timeString = "";
    
    /**
     * Current used type for moving average.
     */
    private MovingAverages type;
    
    /**
     * Constructor.
     * 
     * @param type
     *          used type for chart generation
     * @param value
     *          read property value
     */
    public MovingArerage(MovingAverages type, int value)
    {
        this.type = type;
        this.intValue = value;
    }
    
    /**
     * Constructor.
     * 
     * @param type
     *          used type for chart generation
     * @param timeValue
     *          parsed time value
     * @param timeString
     *          read property value
     */
    public MovingArerage(MovingAverages type, long timeValue, String timeString)
    {
        this.type = type;
        this.longValue = timeValue;
        this.timeString = timeString;
    }
    
    /**
     * Get the value.
     * @return
     *          integer value
     */
    public int getValue()
    {
        return intValue;
    }
    
    /**
     * Get the time value in ms.
     * @return
     *          long value
     */
    public long getLongValue()
    {
        return longValue;
    }
    
    /**
     * The property value of the time.
     * @return
     *          read property value
     */
    public String getTimeString()
    {
        return timeString;
    }
    
    /**
     * The type {@link MovingAverages} of the moving average.
     * @return
     *          type
     */
    public MovingAverages getType()
    {
        return type;
    }
}
