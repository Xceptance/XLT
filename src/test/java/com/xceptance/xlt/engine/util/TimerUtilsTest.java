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
