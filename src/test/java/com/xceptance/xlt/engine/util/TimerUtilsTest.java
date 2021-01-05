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
package com.xceptance.xlt.engine.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the implementation of {@link TimerUtils}.
 * 
 * @author sebastianloob
 */
public class TimerUtilsTest
{

    @Test
    public void testTimer() throws InterruptedException
    {
        Assert.assertTrue("The high precision timer should used by default!", TimerUtils.isHighPrecisionTimerUsed());
        TimerUtils.setUseHighPrecisionTimer(false);
        Assert.assertFalse("The high precision timer should not be used!", TimerUtils.isHighPrecisionTimerUsed());
        final long start = TimerUtils.getTime();
        Thread.sleep(1000);
        final long runtime = TimerUtils.getTime() - start;
        // the runtime should be one second
        Assert.assertEquals(1000L, runtime, 20);
    }
}
