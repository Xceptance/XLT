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
package com.xceptance.xlt.api.engine;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * This is a centralized global clock. It automaticlaly is inited with the
 * default system clock.
 *
 * @author Rene Schwietzke (Xceptance)
 */
public class GlobalClock
{
    // our clock instance
    private static Clock clock = Clock.systemUTC();

    // a varhandle to the clock instance to be able to change it in a safe way without overhead under normal circumstances
    private static final VarHandle clockHandle;
    private static final VarHandle offsetMillisHandle;

    // keep the offset if any in mind
    private static long offsetMillis;

    static
    {
        try
        {
            clockHandle = MethodHandles.lookup().findStaticVarHandle(GlobalClock.class, "clock", Clock.class);
            offsetMillisHandle = MethodHandles.lookup().findStaticVarHandle(GlobalClock.class, "offsetMillis", long.class);
        }
        catch (ReflectiveOperationException e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Clock get()
    {
        // this is not fully safe, it is rather called mixed mode access because
        // we set it volatile, but read it normally. Because we do this for testing only
        // and when running in the program, we will do that before any thread start and
        // use it, we should be safe... guess so
        return clock;
    }

    public static long millis()
    {
        return clock.millis();
    }

    public static long offset()
    {
        return offsetMillis;
    }

    /**
     * You an install and clock based on the java.time.Clock class and make it the
     * central clock.
     *
     * @param clock the clock to use, can be even a static one for testing purposes
     * @return the new clock in case you want to chain
     */
    public static Clock install(final Clock clock)
    {
        // set the clock in a safe way
        clockHandle.setVolatile(clock);
        offsetMillisHandle.setVolatile(0L);
        return clock;
    }

    /**
     * Install a clock that has an offset. Make sure we remember the offset.
     *
     * @param offsetinMillis
     * @return
     */
    public static Clock installWithOffset(final long offsetinMillis)
    {
        clockHandle.setVolatile(Clock.offset(clock, Duration.ofMillis(offsetinMillis)));
        offsetMillisHandle.setVolatile(offsetinMillis);
        return clock;
    }

    /**
     * Install the fixed time clock
     *
     * @param epochMillis the time to return when calling
     * @return the installed clocked
     */
    public static Clock installFixed(final long epochMillis)
    {
        clockHandle.setVolatile(Clock.fixed(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC));
        offsetMillisHandle.setVolatile(0L);
        return clock;
    }

    /**
     * Installs the default system clock again
     *
     * @return the installed clock
     */
    public static Clock reset()
    {
        return install(Clock.systemUTC());
    }
}
