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
package com.xceptance.xlt.engine.resultbrowser;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.digest.DigestUtils;

import com.xceptance.common.lang.ParseNumbers;
import com.xceptance.common.lang.ThrowableUtils;
import com.xceptance.common.util.ParseUtils;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.XltEngine;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 * Stores and unifies errors to avoid logging the same error too often
 *
 * @author Rene Schwietzke
 * @since 7.0.0
 *
 */
public class ErrorCounter
{
    /**
     * The property level for the dump limiter properties
     */
    private static final String LIMITER_PROPERTY = RequestHistory.OUTPUT2DISK_ERROR_PROPERTY + ".limiter";

    /**
     * The property for the number of maximum dumps
     */
    private static final String MAX_DUMP_COUNT_PROPERTY = LIMITER_PROPERTY + ".maxDumps";

    /**
     * The property for the counter reset interval.
     */
    private static final String COUNTER_RESET_INTERVAL_PROPERTY = LIMITER_PROPERTY + ".resetInterval";

    /**
     * The property for the number of maximally handled different errors.
     */
    private static final String MAX_DIFFERENT_ERRORS_PROPERTY = LIMITER_PROPERTY + ".maxDifferentErrors";

    /**
     * Default size for LRU dump cache.
     */
    private static final int MAX_DIFFERENT_ERRORS_DEFAULT = 0;

    /**
     * The counter reset interval.
     */
    private final long resetInterval;

    /**
     * Maximal error count
     */
    private final int maxDiffErrors;

    /**
     * Maximal number of dumps.
     */
    private final int maxDumpCount;

    /**
     * Unique error keys and corresponding number of already dumped results.
     */
    private final ConcurrentHashMap<String, AtomicInteger> errorCounter = new ConcurrentHashMap<>();

    /**
     * The global reset timer responsible for execution of all reset tasks.
     */
    private final Timer timer;

    /**
     * Returns our centralized instance. This is the production mode
     *
     * @return the singleton instance
     */
    public static ErrorCounter get()
    {
        return XltEngine.get().errorCounter;
    }

    /**
     * Returns our instance
     *
     * @return the singleton instance
     */
    public static ErrorCounter createInstance(final XltPropertiesImpl properties)
    {
        return new ErrorCounter(properties);
    }

    /**
     * Builds a new instance
     */
    private ErrorCounter(final XltPropertiesImpl properties)
    {
        final int max =
            properties.getPropertySessionLess(MAX_DIFFERENT_ERRORS_PROPERTY)
            .flatMap(ParseNumbers::parseOptionalInt).orElse(MAX_DIFFERENT_ERRORS_DEFAULT);

        this.maxDiffErrors = max < 1 ? -1 : max;
        this.maxDumpCount = properties.getPropertySessionLess(MAX_DUMP_COUNT_PROPERTY).flatMap(ParseNumbers::parseOptionalInt).orElse(-1);

        // get the value from configuration
        final String intervalString = properties.getPropertySessionLess(COUNTER_RESET_INTERVAL_PROPERTY).orElse("0");

        // parse seconds for the counter reset interval
        long tmpInterval = 0;
        try
        {
            tmpInterval = ParseUtils.parseTimePeriod(intervalString);
        }
        catch (final Exception e)
        {
            XltLogger.runTimeLogger.warn(
                                         String.format("The value '%s' of property '%s' cannot be resolved or parsed as time period. Disabling error limiter reset interval, keeping count limitation.",
                                                       intervalString,
                                                       COUNTER_RESET_INTERVAL_PROPERTY));
        }

        // interval in milliseconds
        this.resetInterval = tmpInterval * 1000;
        if (resetInterval > 0)
        {
            timer = new Timer("TimedCounter-ResetTimer");
        }
        else
        {
            timer = null;
        }
    }

    /**
     * Create the key for failure reason
     *
     * @return the session failure's key
     */
    public static String getErrorKey(final String userName, final Throwable reason)
    {
        String key;
        if (reason != null)
        {
            final String r = ThrowableUtils.getMinifiedStackTrace(reason);
            // remove the hint
            key = RegExUtils.removeAll(r, ThrowableUtils.DIRECTORY_HINT_REGEX);
            key = userName + "|" + key;
        }
        else
        {
            key = userName;
        }

        // key example 097aedc899ad058e35075e27b8609cb5
        // hash the current key to reduce memory usage
        return DigestUtils.md5Hex(key);
    }

    /**
     * Check if we can dump for that error and user, if so, count and return true,
     * false otherwise
     *
     * @param session the session with the error information
     * @return true if we can dump and have it counted, false otherwise
     */
    public boolean countDumpIfOpen(final SessionImpl session)
    {
        // no limit is set up
        if (maxDiffErrors <= 0)
        {
            return true;
        }

        // ok, we still have room
        if (session.hasFailed())
        {
            // get the error key
            final String key = getErrorKey(session.getUserName(), session.getFailReason());

            // check if we know it
            AtomicInteger count = errorCounter.get(key);

            // do we know it?
            if (count == null)
            {
                // we are full, don't dump anymore aka don't add more entries
                if (errorCounter.size() >= maxDiffErrors)
                {
                    return false;
                }
                else
                {
                    // ok, we got still capacity
                    count = errorCounter.computeIfAbsent(key, k ->
                    {
                        // when the rest interval is larger than 0, we got a timer task instance
                        if (timer != null)
                        {
                            timer.schedule(new RemovalTask(key), resetInterval);
                        }
                        return new AtomicInteger();
                    });
                }
            }

            // if dumping for this error key is OK increase dump counter,
            // if we can count like hell (with -1), do that but don't increase any counters
            // because we don't care about the amount per error, only about the total number
            if (maxDumpCount == -1)
            {
                return true;
            }
            else if (count.get() < maxDumpCount)
            {
                count.incrementAndGet();
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the reset interval in use
     *
     * @return the reset interval in msec
     */
    public long getResetInterval()
    {
        return resetInterval;
    }

    /**
     * Returns the max different errors permitted
     *
     * @return the max errors permitted
     */
    public long getMaxDifferentErrors()
    {
        return maxDiffErrors;
    }

    /**
     * Returns how many dump per error are permitted
     *
     * @return the per error dump count
     */
    public long getMaxDumpCount()
    {
        return maxDumpCount;
    }

    /**
     * Returns the current count of active errors. This will
     * be zero if we are not limiting errors.
     *
     * @return the current count of different errors
     */
    public long getDifferentErrorCount()
    {
        return errorCounter.size();
    }

    /**
     * Our small time task to remove us from the counter list
     */
    class RemovalTask extends TimerTask
    {
        private final String key;

        public RemovalTask(final String key)
        {
            this.key = key;
        }

        @Override
        public void run()
        {
            errorCounter.remove(key);
        }
    }
}
