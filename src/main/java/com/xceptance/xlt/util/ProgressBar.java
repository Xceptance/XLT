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
package com.xceptance.xlt.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple one way console progress bar.<br>
 * <br>
 * Example:
 * 
 * <pre>
 * int total = 10;
 * ProgressBar progressBar = new ProgressBar(total);
 * progressBar.start();
 * for (int i = 0; i &lt; total; i++)
 * {
 *     progressBar.increaseCount();
 * }
 * ;
 * </pre>
 * 
 * Example Output:
 * 
 * <pre>
 * 0% ... 10% ... 20% ... 30% ... 40% ... 50% ... 60% ... 70% ... 80% ... 90% ... 100%
 * </pre>
 * 
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class ProgressBar
{
    private final int total;

    private int nextStep = 0;

    private int currentCount = 0;

    private final boolean isStarted = false;

    private static final Logger log = LoggerFactory.getLogger(ProgressBar.class);

    
    /**
     * @param total
     *            expected total (greater than or equals 0)
     */
    public ProgressBar(final int total)
    {
        if (total < 0)
        {
            throw new IllegalArgumentException("Given total must be 0 or higher but was : " + total);
        }

        this.total = total;
    }

    /**
     * Start progress bar.<br>
     * If not started
     */
    public synchronized void start()
    {
        if (!isStarted)
        {
            setCount(0);
        }
    }

    /**
     * Increases the current progress counter.<br>
     * Either work with 'percentage' or with 'count'. Both in combination will not work properly.
     */
    public synchronized void increaseCount()
    {
        if (currentCount < total)
        {
            currentCount++;
            
            setCount(currentCount);
        }
        else
        {
            // don't do anything, we reached the end and are not a Windows progress bar
            // and stack at the end, we at least warn
            log.warn("ProgressBar reached max value.");
        }
    }

    /**
     * Increases the current progress counter by given amount.<br>
     * Either work with 'percentage' or with 'count'. Both in combination will not work properly.
     */
    public synchronized void increaseCount(final int add)
    {
        if (add < 0)
        {
            throw new IllegalArgumentException(String.format("Added value must be positive but was '%d'", add));
        }
        setCount(currentCount + add);
    }

    /**
     * Sets the progress totally.<br>
     * Either work with 'percentage' or with 'count'. Both in combination will not work properly.
     * 
     * @param count
     *            count
     */
    public synchronized void setCount(final int count)
    {
        if (count < 0)
        {
            throw new IllegalArgumentException("Count must be >= 0");
        }
        
        if (count <= total)
        {
            currentCount = count;

            final int percent = count * 100 / total;
            setPercent(percent);
        }
        else 
        {
            // we reached the end... silently ignoring all new data and just warning
            log.warn(String.format("ProgressBar started to overflow, total: %d, count: %d", total, count));
            return;
        }
    }

    /**
     * @return set progress bar to 'completed'
     */
    public synchronized void setCompleted()
    {
        setCount(total);
    }

    /**
     * @return the expected total count
     */
    public synchronized int getTotal()
    {
        return total;
    }

    /**
     * @return the current count
     */
    public synchronized int getCurrentCount()
    {
        return currentCount;
    }

    /**
     * Sets the progress in percent.<br>
     * Either work with 'percentage' or with 'count'. Both in combination will not work properly.
     * 
     * @param percent
     *            percentage
     */
    public synchronized void setPercent(final int percent)
    {
        if (percent < 0 || percent > 100)
        {
            throw new IllegalArgumentException(String.format("Percent must be one of [0 .. 100] but was '%d'", percent));
        }

        if (percent >= nextStep)
        {
            final boolean isStart = (nextStep == 0);

            while (percent >= nextStep)
            {
                nextStep += 10;
            }

            final int currentStep = isStart ? 0 : nextStep - 10;

            final String keepOn = percent < 100 ? "... " : "";
            final String step = String.format("%d%s %s", currentStep, "%", keepOn);

            System.out.print(step);
        }
    }
}
