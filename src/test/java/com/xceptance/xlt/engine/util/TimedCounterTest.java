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

import com.xceptance.xlt.AbstractXLTTestCase;

/**
 * Tests the implementation of {@link TimedCounter}.
 */
public class TimedCounterTest extends AbstractXLTTestCase
{
    /**
     * Assert that initial value is zero.
     */
    @Test
    public void test_initialize()
    {
        final TimedCounter c = new TimedCounter();
        Assert.assertEquals(0, c.get());
    }

    /**
     * Create with reset timer period. Assert that initial value is zero.
     */
    @Test
    public void test_initializeWithPeriod()
    {
        final TimedCounter c = new TimedCounter(5000);
        Assert.assertEquals(0, c.get());
    }

    /**
     * Assert that counter gets incremented.
     */
    @Test
    public void test_increment()
    {
        final TimedCounter c = new TimedCounter();
        c.increment();
        Assert.assertEquals(1, c.get());
    }

    /**
     * Assert that reset aware counter gets incremented.
     */
    @Test
    public void test_incrementWithPeriod()
    {
        final TimedCounter c = new TimedCounter(2000);
        c.increment();
        Assert.assertEquals(1, c.get());
    }

    /**
     * Check that counter is reset.
     * 
     * @throws Throwable
     */
    @Test
    public void test_reset() throws Throwable
    {
        final TimedCounter c = new TimedCounter(1000);
        c.increment();

        // wait for reset period is over
        Thread.sleep(1200);

        Assert.assertEquals(0, c.get());
    }

    /**
     * Check that counter is reset not too early.
     * 
     * @throws Throwable
     */
    @Test
    public void test_resetNotBeforePeriod() throws Throwable
    {
        final TimedCounter c = new TimedCounter(2000);
        c.increment();
        Assert.assertEquals(1, c.get());

        Thread.sleep(1000);
        Assert.assertEquals(1, c.get());
        
        Thread.sleep(1200);
        Assert.assertEquals(0, c.get());
    }

    /**
     * Check setting a value.
     * 
     * @throws Throwable
     */
    @Test
    public void test_set() throws Throwable
    {
        final TimedCounter c = new TimedCounter();
        c.set(5);
        Assert.assertEquals(5, c.get());
    }
}
