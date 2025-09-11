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
package com.xceptance.xlt.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.xceptance.xlt.api.engine.GlobalClock;

public class GlobalClockTest
{
    /*
     * We have a default system clock, make sure this installed
     * We cannot really test that reliably because someone else might have
     * used the clock in this VM already
     */
    @Test
    public final void base()
    {
        GlobalClock.reset();

        assertEquals("java.time.Clock$SystemClock", GlobalClock.get().getClass().getName());
        assertEquals(0L, GlobalClock.offset());
    }

    /*
     * We have a default system clock, make sure this installed
     */
    @Test
    public final void fixed() throws InterruptedException
    {
        GlobalClock.reset();
        GlobalClock.installFixed(1010L);

        assertEquals(1010L, GlobalClock.millis());
        Thread.sleep(676L);
        assertEquals(1010L, GlobalClock.millis());
    }

    /*
     * We have a default system clock, make sure this installed
     */
    @Test
    public final void offset() throws InterruptedException
    {
        GlobalClock.reset();
        GlobalClock.installWithOffset(10000L);

        assertEquals(10000L, GlobalClock.offset());

        // 10020 is used to avoid failures during maven builds

        long sum = 0;
        final long ITERATIONS = 13;

        // not sure what this test really proves but likely we want to see that we stick to the offset
        for (int i = 0; i < ITERATIONS; i++)
        {
            var t1 = GlobalClock.millis();
            sum+= t1 - System.currentTimeMillis();

            Thread.sleep((long) (Math.random() * 100));
        }

        var avg = sum / ITERATIONS;
        assertTrue(String.format("Was total %d, avg diff was %d", sum, avg), Math.abs(avg - 10000L) < 20);
    }

    /*
     * We have a default system clock, make sure this installed
     */
    @Test
    public final void reset()
    {
        GlobalClock.installFixed(1000L);
        assertEquals("java.time.Clock$FixedClock", GlobalClock.get().getClass().getName());

        GlobalClock.reset();
        assertEquals("java.time.Clock$SystemClock", GlobalClock.get().getClass().getName());
        assertEquals(0L, GlobalClock.offset());

        GlobalClock.installWithOffset(2000L);
        assertEquals("java.time.Clock$OffsetClock", GlobalClock.get().getClass().getName());
        assertEquals(2000L, GlobalClock.offset());

        GlobalClock.reset();
        assertEquals("java.time.Clock$SystemClock", GlobalClock.get().getClass().getName());
        assertEquals(0L, GlobalClock.offset());
    }
}
