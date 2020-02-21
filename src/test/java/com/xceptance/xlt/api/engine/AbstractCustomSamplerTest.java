package com.xceptance.xlt.api.engine;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sebastian Oerding
 */
public class AbstractCustomSamplerTest
{
    @Test
    public void testSetter()
    {
        final AbstractCustomSampler sampler = new DummySampler();
        sampler.setInterval(0);
        try
        {
            sampler.setInterval(-1);
            Assert.assertTrue("This code should be unreachable cause an exception is expected!", false);
        }
        catch (final IllegalArgumentException e)
        {
            Assert.assertEquals("Unexpected error message.", "Parameter 'interval' is invalid, because its value is less than -> [0]",
                                e.getMessage());
        }

        sampler.setName("blame");
        try
        {
            sampler.setName("");
            Assert.assertTrue("This code should be unreachable cause an exception is expected!", false);
        }
        catch (final IllegalArgumentException e)
        {
            Assert.assertEquals("Unexpected error message.", "Parameter 'name' is invalid, because its value is empty", e.getMessage());
        }
    }

    @Test
    public void testSetIntervalNewApproach()
    {
        final AbstractCustomSampler sampler = new DummySampler();
        sampler.setInterval("1234");
        Assert.assertEquals("Unexpected sampler interval", 1234, sampler.getInterval());
        sampler.setInterval("1h 1m 1s");
        Assert.assertEquals("Unexpected sampler interval", 3661000, sampler.getInterval());
    }

    @Test
    public void testGetter()
    {
        final DummySampler ds = new DummySampler();
        ds.initialize();

        Assert.assertEquals("Unexpected default interval!", -1, ds.getInterval());
        Assert.assertEquals("Unexpected default name!", null, ds.getName());
        final Properties p = new Properties();
        ds.setProperties(p);
        Assert.assertEquals("Unexpected properties!", p, ds.getProperties());
        ds.shutdown();
    }

    /**
     * Dummy implementation to test the class {@link AbstractCustomSampler}.
     */
    private class DummySampler extends AbstractCustomSampler
    {
        /**
         * @return 0
         */
        @Override
        public double execute()
        {
            return 0;
        }
    }
}
