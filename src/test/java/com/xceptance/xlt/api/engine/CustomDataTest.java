package com.xceptance.xlt.api.engine;

import org.junit.Assert;
import org.junit.Test;

public class CustomDataTest
{

    @Test
    public final void testCustomData()
    {
        final CustomData c = new CustomData();
        Assert.assertEquals("C", c.getTypeCode());
    }

    @Test
    public final void testCustomDataString()
    {
        final CustomData c = new CustomData("Test99");
        Assert.assertEquals("C", c.getTypeCode());
        Assert.assertEquals("Test99", c.getName());
    }

}
