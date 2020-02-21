package com.xceptance.common.lang;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the ThreadUtils constructor only.
 * 
 * @author Rene Schwietzke
 * @see ThreadUtils
 */
public class ThreadUtilsTest
{
    /**
     * Check private constructor
     */
    @Test
    public final void testConstructor()
    {
        Assert.assertTrue(ReflectionUtils.classHasOnlyPrivateConstructors(ThreadUtils.class));
    }
}
