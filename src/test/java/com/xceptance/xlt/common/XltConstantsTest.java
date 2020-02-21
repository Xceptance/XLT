package com.xceptance.xlt.common;

import org.junit.Test;

import com.xceptance.common.lang.ReflectionUtils;

/**
 * @author Sebastian Oerding
 */
public class XltConstantsTest
{
    @Test
    public void testHasOnlyPrivateConstructors()
    {
        ReflectionUtils.classHasOnlyPrivateConstructors(XltConstants.class);
    }
}
