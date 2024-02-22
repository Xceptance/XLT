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
package com.xceptance.common.util;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author rschwietzke
 */
public class TimerUtilsTest
{
    /**
     * Test method for {@link com.xceptance.common.util.TimerUtils#getTimer()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public final void testNameAndState() throws InterruptedException
    {
        final CountDownLatch signal = new CountDownLatch(1);
        final AtomicBoolean done = new AtomicBoolean(false);

        final Timer timer1 = TimerUtils.getTimer();
        timer1.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                Assert.assertTrue(Thread.currentThread().isDaemon());
                Assert.assertEquals("GeneralPurposeTimer", Thread.currentThread().getName());

                done.set(true);
                signal.countDown();
            }
        }, 0);

        signal.await(2, TimeUnit.SECONDS);
        Assert.assertTrue(done.get());
    }

    /**
     * Test that we get the same timer back all the time
     * 
     * @throws InterruptedException
     */
    @Test
    public final void testSingleton() throws InterruptedException
    {
        final Timer timer1 = TimerUtils.getTimer();
        final Timer timer2 = TimerUtils.getTimer();
        Assert.assertEquals(timer1, timer2);
    }

}
