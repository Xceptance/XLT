/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.mastercontroller;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.util.XltPropertiesImpl;

public class TestLoadProfileConfigurationTest
{
    final String LOADTESTS = "com.xceptance.xlt.loadtests";

    final String DEFAULT = LOADTESTS + ".default.";

    final String PREFIX_LOADTESTS = LOADTESTS + ".";

    @Test
    public void testLengthOfTestCaseNameIsZero()
    {
        final Properties properties = getDefaultProperties();
        properties.put(LOADTESTS, "");

        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertTrue(loadProfile.getLoadTestConfiguration().isEmpty());
    }

    @Test
    public void testNoTestCaseNameDefined()
    {
        final Properties properties = getDefaultProperties();
        properties.remove(LOADTESTS);

        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertTrue(loadProfile.getLoadTestConfiguration().isEmpty());
    }

    @Test(expected = XltException.class)
    public void testNoTestClassDefined()
    {
        final Properties properties = getDefaultProperties();
        properties.remove(DEFAULT + "class");

        new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
    }

    @Test(expected = XltException.class)
    public void testNoUserCountDefined()
    {
        final Properties properties = getDefaultProperties();
        properties.remove(DEFAULT + "users");

        new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
    }

    @Test(expected = XltException.class)
    public void testNoMeasurementPeriodDefined()
    {
        final Properties properties = getDefaultProperties();
        properties.remove(DEFAULT + "measurementPeriod");

        new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRampUpAndSteadyPeriodAreDefined()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "rampUpSteadyPeriod", "30");
        properties.put(DEFAULT + "rampUpPeriod", "20");
        new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
    }

    @Test(expected = RuntimeException.class)
    public void testParameterWithEmptyIntegerValue()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "iterations", "");
        new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
    }

    @Test(expected = RuntimeException.class)
    public void testParameterWithEmptyTimePeriodValue()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "shutdownPeriod", "");
        new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParameterWithEmptyLoadFunction()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "arrivalRate", "");
        new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
    }

    @Test
    public void testSimpleUserCountAndArrivalRate()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "arrivalRate", "3600");

        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        // arrival rate should be used
        Assert.assertArrayEquals(new int[][]
            {
                    {
                        0, 3600
                    }
            }, loadProfile.getLoadTestConfiguration().get(0).getArrivalRate());
    }

    @Test(expected = XltException.class)
    public void testComplexUserCountAndArrivalRate()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "users", "5/10, 20/15");
        properties.put(DEFAULT + "arrivalRate", "3600");

        new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComplexUserCountUnsortedOrder()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "users",  "1h/1, 5s/15");

        new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComplexArrivalRateUnsortedOrder()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "arrivalRate",  "1h/3600, 5s/2000");

        new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
    }

    @Test(expected = XltException.class)
    public void testIterationsAndArrivalRate()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "iterations", "20");
        properties.put(DEFAULT + "arrivalRate", "3600");

        new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
    }

    @Test
    public void testSimpleUserCountAndRampUp()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "rampUpPeriod", "20");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        // the ramp-up period should be applied to the user count
        Assert.assertTrue(loadProfile.getLoadTestConfiguration().get(0).getNumberOfUsers() != new int[][]
            {
                    {
                        0, 50
                    }
            });
    }

    @Test
    public void testComplexUserCountOverridesRampUp()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "users", "0/50, 20/30");
        properties.put(DEFAULT + "rampUpPeriod", "20");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertArrayEquals(new int[][]
            {
                    {
                        0, 50
                    },
                    {
                        20, 30
                    }
            }, loadProfile.getLoadTestConfiguration().get(0).getNumberOfUsers());
    }

    @Test
    public void testSimpleUserCountAndSimpleArrivalRateAndRampUp()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "arrivalRate", "3600");
        properties.put(DEFAULT + "rampUpPeriod", "20");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertArrayEquals(new int[][]
            {
                    {
                        0, 50
                    }
            }, loadProfile.getLoadTestConfiguration().get(0).getNumberOfUsers());
        // the ramp-up period should be applied to the arrival rate
        Assert.assertTrue(loadProfile.getLoadTestConfiguration().get(0).getArrivalRate() != new int[][]
            {
                    {
                        0, 3600
                    }
            });
    }

    @Test
    public void testSimpleUserCountAndComplexArrivalRateOverridesRampUp()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "arrivalRate", "0/360, 20/3600");
        properties.put(DEFAULT + "rampUpPeriod", "20");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertArrayEquals(new int[][]
            {
                    {
                        0, 50
                    }
            }, loadProfile.getLoadTestConfiguration().get(0).getNumberOfUsers());
        Assert.assertArrayEquals(new int[][]
            {
                    {
                        0, 360
                    },
                    {
                        20, 3600
                    }
            }, loadProfile.getLoadTestConfiguration().get(0).getArrivalRate());
    }

    @Test
    public void testGetStringPropertyDefaultAndSpecificValue()
    {
        final Properties properties = getDefaultProperties();
        properties.put(PREFIX_LOADTESTS + "test.class", "anotherTest");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertEquals("anotherTest", loadProfile.getLoadTestConfiguration().get(0).getTestCaseClassName());
    }

    @Test
    public void testGetStringPropertySpecificValue()
    {
        final Properties properties = getDefaultProperties();
        properties.remove(DEFAULT + "class");
        properties.put(PREFIX_LOADTESTS + "test.class", "anotherTest");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertEquals("anotherTest", loadProfile.getLoadTestConfiguration().get(0).getTestCaseClassName());
    }

    @Test
    public void testGetStringPropertyDefaultValue()
    {
        final Properties properties = getDefaultProperties();
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertEquals("test", loadProfile.getLoadTestConfiguration().get(0).getTestCaseClassName());
    }

    @Test
    public void testGetIntPropertyDefaultAndSpecificValue()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "iterations", "20");
        properties.put(PREFIX_LOADTESTS + "test.iterations", "30");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertEquals(30, loadProfile.getLoadTestConfiguration().get(0).getNumberOfIterations());
    }

    @Test
    public void testGetIntPropertySpecificValue()
    {
        final Properties properties = getDefaultProperties();
        properties.put(PREFIX_LOADTESTS + "test.iterations", "30");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertEquals(30, loadProfile.getLoadTestConfiguration().get(0).getNumberOfIterations());
    }

    @Test
    public void testGetIntPropertyDefaultValue()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "iterations", "20");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertEquals(20, loadProfile.getLoadTestConfiguration().get(0).getNumberOfIterations());
    }

    @Test
    public void testGetIntPropertyNoDefaultAndNoSpecificValue()
    {
        final Properties properties = getDefaultProperties();
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertEquals(0, loadProfile.getLoadTestConfiguration().get(0).getNumberOfIterations());
    }

    @Test
    public void testGetTimePeriodPropertyDefaultAndSpecificValue()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "warmUpPeriod", "20");
        properties.put(PREFIX_LOADTESTS + "test.warmUpPeriod", "30");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertEquals(30, loadProfile.getLoadTestConfiguration().get(0).getWarmUpPeriod());
    }

    @Test
    public void testGetTimePeriodPropertySpecificValue()
    {
        final Properties properties = getDefaultProperties();
        properties.put(PREFIX_LOADTESTS + "test.warmUpPeriod", "30");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertEquals(30, loadProfile.getLoadTestConfiguration().get(0).getWarmUpPeriod());
    }

    @Test
    public void testGetTimePeriodPropertyDefaultValue()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "warmUpPeriod", "20");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertEquals(20, loadProfile.getLoadTestConfiguration().get(0).getWarmUpPeriod());
    }

    @Test
    public void testGetTimePeriodPropertyNoDefaultAndNoSpecificValue()
    {
        final Properties properties = getDefaultProperties();
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertEquals(0, loadProfile.getLoadTestConfiguration().get(0).getWarmUpPeriod());
    }

    @Test
    public void testGetLoadFunctionDefaultAndSpecificValue()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "arrivalRate", "0/360,20/3600");
        properties.put(PREFIX_LOADTESTS + "test.arrivalRate", "0/720,20/7200");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertArrayEquals(new int[][]
            {
                    {
                        0, 720
                    },
                    {
                        20, 7200
                    }
            }, loadProfile.getLoadTestConfiguration().get(0).getArrivalRate());
    }

    @Test
    public void testGetLoadFunctionSpecificValue()
    {
        final Properties properties = getDefaultProperties();
        properties.put(PREFIX_LOADTESTS + "test.arrivalRate", "0/720,20/7200");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertArrayEquals(new int[][]
            {
                    {
                        0, 720
                    },
                    {
                        20, 7200
                    }
            }, loadProfile.getLoadTestConfiguration().get(0).getArrivalRate());
    }

    @Test
    public void testGetLoadFunctionDefaultValue()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "arrivalRate", "0/360,20/3600");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertArrayEquals(new int[][]
            {
                    {
                        0, 360
                    },
                    {
                        20, 3600
                    }
            }, loadProfile.getLoadTestConfiguration().get(0).getArrivalRate());
    }

    @Test
    public void testGetLoadFunctionNoDefaultAndNoSpecificValue()
    {
        final Properties properties = getDefaultProperties();
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertNull(loadProfile.getLoadTestConfiguration().get(0).getArrivalRate());
    }

    @Test
    public void testDefaultRampUpInitialValue()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "rampUpPeriod", "10");
        properties.put(DEFAULT + "rampUpInitialValue", "20");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertEquals(20, loadProfile.getLoadTestConfiguration().get(0).getNumberOfUsers()[0][1]);
    }

    @Test
    public void testSpecificRampUpInitialValue()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "rampUpPeriod", "10");
        properties.put(PREFIX_LOADTESTS + "test.rampUpInitialValue", "20");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertEquals(20, loadProfile.getLoadTestConfiguration().get(0).getNumberOfUsers()[0][1]);
    }

    @Test
    public void testSpecificRampUpInitialValueOverridesDefaultRampUpInitialValue()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "rampUpPeriod", "10");
        properties.put(PREFIX_LOADTESTS + "test.rampUpInitialValue", "10");
        properties.put(DEFAULT + "rampUpInitialValue", "30");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertEquals(10, loadProfile.getLoadTestConfiguration().get(0).getNumberOfUsers()[0][1]);
    }

    @Test
    public void testNonSpecifiedValues()
    {
        final Properties properties = getDefaultProperties();
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertEquals(0, loadProfile.getLoadTestConfiguration().get(0).getWarmUpPeriod());
        Assert.assertEquals(-1, loadProfile.getLoadTestConfiguration().get(0).getRampUpPeriod());
        Assert.assertNull(loadProfile.getLoadTestConfiguration().get(0).getArrivalRate());
        Assert.assertEquals(0, loadProfile.getLoadTestConfiguration().get(0).getNumberOfIterations());
        Assert.assertEquals(0, loadProfile.getLoadTestConfiguration().get(0).getInitialDelay());
        Assert.assertEquals(0, loadProfile.getLoadTestConfiguration().get(0).getShutdownPeriod());
    }

    @Test(expected = XltException.class)
    public void testOutdatedProperties_defaultDuration()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "duration", "20");

        new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
    }

    @Test(expected = XltException.class)
    public void testOutdatedProperties_specificDuration()
    {
        final Properties properties = getDefaultProperties();
        properties.put(PREFIX_LOADTESTS + "test.duration", "20");

        new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
    }

    @Test(expected = XltException.class)
    public void testOutdatedProperties_defaultRampUpInitialusers()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "rampUpInitialUsers", "20");

        new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
    }

    @Test(expected = XltException.class)
    public void testOutdatedProperties_specificRampUpInitialusers()
    {
        final Properties properties = getDefaultProperties();
        properties.put(PREFIX_LOADTESTS + "test.rampUpInitialUsers", "20");

        new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
    }

    @Test
    public void testSimpleUserCountAndSimpleLoadFactor()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "loadFactor", "2.0");
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertArrayEquals(new int[][]
            {
                    {
                        0, 100
                    }
            }, loadProfile.getLoadTestConfiguration().get(0).getNumberOfUsers());
    }

    @Test
    public void testSimpleUserCountAndComplexLoadFactor()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "loadFactor", "0/1.0 20/2.0");

        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertArrayEquals(new int[][]
            {
                    {
                        0, 50
                    },
                    {
                        20, 100
                    }
            }, loadProfile.getLoadTestConfiguration().get(0).getNumberOfUsers());
    }

    @Test
    public void testComplexUserCountAndSimpleLoadFactor()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "users", "0/50, 20/30");
        properties.put(DEFAULT + "loadFactor", "2.0");

        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertArrayEquals(new int[][]
            {
                    {
                        0, 100
                    },
                    {
                        20, 60
                    }
            }, loadProfile.getLoadTestConfiguration().get(0).getNumberOfUsers());
    }

    @Test(expected = XltException.class)
    public void testComplexUserCountAndComplexLoadFactor()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "users", "0/50, 20/30");
        properties.put(DEFAULT + "loadFactor", "0/1.0 20/2.0");

        new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
    }

    @Test
    public void testSimpleArrivalRateAndSimpleLoadFactor()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "arrivalRate", "3600");
        properties.put(DEFAULT + "loadFactor", "2.0");

        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertArrayEquals(new int[][]
            {
                    {
                        0, 100
                    }
            }, loadProfile.getLoadTestConfiguration().get(0).getNumberOfUsers());
        Assert.assertArrayEquals(new int[][]
            {
                    {
                        0, 7200
                    }
            }, loadProfile.getLoadTestConfiguration().get(0).getArrivalRate());
    }

    @Test
    public void testSimpleArrivalRateAndComplexLoadFactor()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "arrivalRate", "3600");
        properties.put(DEFAULT + "loadFactor", "0/1.0 20/2.0");

        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertArrayEquals(new int[][]
            {
                    {
                        0, 50
                    },
                    {
                        20, 100
                    }
            }, loadProfile.getLoadTestConfiguration().get(0).getNumberOfUsers());
        Assert.assertArrayEquals(new int[][]
            {
                    {
                        0, 3600
                    },
                    {
                        20, 7200
                    }
            }, loadProfile.getLoadTestConfiguration().get(0).getArrivalRate());
    }

    @Test
    public void testComplexArrivalRateAndSimpleLoadFactor()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "arrivalRate", "0/360, 20/3600");
        properties.put(DEFAULT + "loadFactor", "2.0");

        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
        Assert.assertArrayEquals(new int[][]
            {
                    {
                        0, 100
                    }
            }, loadProfile.getLoadTestConfiguration().get(0).getNumberOfUsers());
        Assert.assertArrayEquals(new int[][]
            {
                    {
                        0, 720
                    },
                    {
                        20, 7200
                    }
            }, loadProfile.getLoadTestConfiguration().get(0).getArrivalRate());
    }

    @Test(expected = XltException.class)
    public void testComplexArrivalRateAndComplexLoadFactor()
    {
        final Properties properties = getDefaultProperties();
        properties.put(DEFAULT + "arrivalRate", "0/360, 20/3600");
        properties.put(DEFAULT + "loadFactor", "0/1.0 20/2.0");

        new TestLoadProfileConfiguration(new XltPropertiesImpl(properties));
    }

    /*
     * Returns properties which are necessary to create a test load profile configuration.
     */
    private Properties getDefaultProperties()
    {
        final Properties properties = new Properties();
        properties.put(LOADTESTS, "test");
        properties.put(DEFAULT + "class", "test");
        properties.put(DEFAULT + "users", "0/50");
        properties.put(DEFAULT + "measurementPeriod", "60");
        return properties;
    }
}
