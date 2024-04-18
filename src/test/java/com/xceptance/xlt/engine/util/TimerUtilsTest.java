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
package com.xceptance.xlt.engine.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests the implementation of {@link TimerUtils}.
 *
 * @author sebastianloob
 */
public class TimerUtilsTest
{
    @Test
    public void testTimer()
    {
        assertTrue("The high precision timer should used by default!", TimerUtils.get().isHighPrecision());
    }

    @Test
    public void highPrecTimer()
    {
        var hpt = TimerUtils.getHighPrecisionTimer();
        assertTrue(hpt.isHighPrecision());

        // check that we get measurements close to nanoTime();
        assertEquals(System.nanoTime() / 1000_000L, hpt.getStartTime(), 10);
    }

    @Test
    public void lowPrecTimer()
    {
        var lpt = TimerUtils.getLowPrecisionTimer();
        assertFalse(lpt.isHighPrecision());

        // check that we get measurements close to currentTimeMillis();
        assertEquals(System.currentTimeMillis(), lpt.getStartTime(), 10);
    }
}
