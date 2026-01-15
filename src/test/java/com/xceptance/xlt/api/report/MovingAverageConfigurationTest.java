package com.xceptance.xlt.api.report;

import org.junit.Assert;
import org.junit.Test;

public class MovingAverageConfigurationTest
{
    @Test
    public void createPercentageConfig()
    {
        final MovingAverageConfiguration config = MovingAverageConfiguration.createPercentageConfig(25);
        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.PERCENTAGE, config.getType());
        Assert.assertEquals(25, config.getValue());
        Assert.assertEquals("25%", config.getName());
    }

    @Test
    public void createTimeConfig()
    {
        final MovingAverageConfiguration config = MovingAverageConfiguration.createTimeConfig(300);
        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.TIME, config.getType());
        Assert.assertEquals(300, config.getValue());
        Assert.assertEquals("300s", config.getName());
    }

    @Test
    public void createTimeConfig_withCustomName()
    {
        final MovingAverageConfiguration config = MovingAverageConfiguration.createTimeConfig(300, "5m");
        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.TIME, config.getType());
        Assert.assertEquals(300, config.getValue());
        Assert.assertEquals("5m", config.getName());
    }
}
