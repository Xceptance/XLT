package com.xceptance.xlt.api.report.external;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sebastian Oerding
 */
@SuppressWarnings("deprecation")
public class NamedDataTest
{
    @Test
    public void testNamedData()
    {
        final String name = "nd1:name";
        final double value = Double.MAX_VALUE;
        final NamedData data = new NamedData(name, value);
        Assert.assertEquals("Wrong name!", name, data.getName());
        Assert.assertEquals("Wrong value!", value, data.getValue(), 0.0);
    }
}
