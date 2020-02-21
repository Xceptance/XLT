package com.xceptance.common.lang;

import org.junit.Assert;
import org.junit.Test;

/**
 * Quick parameter test.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 * @see com.xceptance.common.lang.Parameter
 */
public class ParameterTest
{

    @Test
    public final void testValueOfPrimitive()
    {
        final Parameter<Integer> p = Parameter.valueOf(42, int.class);
        Assert.assertEquals("Value : 42, declared parameter class : int", p.toString());
    }

    @Test
    public final void testValueOfObject()
    {
        final Parameter<String> p = Parameter.valueOf("i4242", String.class);
        Assert.assertEquals("Value : i4242, declared parameter class : java.lang.String", p.toString());
    }

}
