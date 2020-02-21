package com.xceptance.xlt.api.engine;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sebastian Oerding
 */
public class CustomValueTest
{
    @Test
    public void testAddValues()
    {
        final TestCustomValue value = new TestCustomValue();
        value.setValue(0.0);
        final List<String> strings = value.addValues();
        Assert.assertEquals("Wrong number of values!", 4, strings.size());
        Assert.assertEquals("Wrong double value!", "0.0", strings.get(3));
    }

    @Test
    public void testParseValues()
    {
        final TestCustomValue value = new TestCustomValue();
        value.parseValues(new String[]
            {
                "V", "null", "123000", "0.0"
            });
        Assert.assertTrue("Wrong double value! Expected 0.0d but got " + value.getValue(), Double.compare(0.0, value.getValue()) == 0);
    }

    @Test
    public void testConstructorWithName()
    {
        final CustomValue cv = new CustomValue("Huhu");
        Assert.assertEquals("Wrong name, ", "Huhu", cv.getName());
    }

    private class TestCustomValue extends CustomValue
    {
        @Override
        protected List<String> addValues()
        {
            return super.addValues();
        }

        @Override
        protected void parseValues(final String[] values)
        {
            super.parseValues(values);
        }
    }
}
