/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
