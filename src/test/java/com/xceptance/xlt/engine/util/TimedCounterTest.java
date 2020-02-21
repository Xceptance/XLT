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
     * Check that counter is reseted.
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
     * Check that counter is reseted not too early.
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
