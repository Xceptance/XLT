/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.agent.unipro;

import org.junit.Assert;
import org.junit.Test;

public class FunctionTest
{
    @Test(expected = IllegalArgumentException.class)
    public void testTimeValuePairIsNull()
    {
        final int[][] timeValuePairs = null;
        new CompositeFunction(timeValuePairs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTimeValuePairIsEmpty_01()
    {
        final int[][] timeValuePairs = {};
        new CompositeFunction(timeValuePairs);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testTimeValuePairIsEmpty_02()
    {
        final int[][] timeValuePairs =
            {
                {}
            };
        new CompositeFunction(timeValuePairs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFirstTimeNotZero()
    {
        final int[][] timeValuePairs =
            {
                    {
                        3, 5
                    }
            };
        new CompositeFunction(timeValuePairs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinusX()
    {
        final int[][] timeValuePairs =
            {
                    {
                        0, 5
                    }
            };
        final Function functions = new CompositeFunction(timeValuePairs);
        functions.calculateY(-4);
    }

    @Test
    public void testOneTimeValuePair()
    {
        final int[][] timeValuePairs =
            {
                    {
                        0, 149
                    }
            };
        final Function functions = new CompositeFunction(timeValuePairs);
        Assert.assertEquals("Failure at time 0: ", 149.0, functions.calculateY(0), 0.0);
        Assert.assertEquals("Failure at time 149: ", 149.0, functions.calculateY(149), 0.0);
        Assert.assertEquals("Failure at time maxValue: ", 149.0, functions.calculateY(Integer.MAX_VALUE), 0.0);
    }

    @Test
    public void testConstantFunction_01()
    {
        final int[][] timeValuePairs =
            {
                    {
                        0, 0
                    },
                    {
                        30, 0
                    },
                    {
                        30, 68
                    }
            };
        final Function functions = new CompositeFunction(timeValuePairs);
        Assert.assertEquals("Failure at time 0: ", 0.0, functions.calculateY(0), 0.0);
        Assert.assertEquals("Failure at time 29: ", 0.0, functions.calculateY(29), 0.0);
        Assert.assertEquals("Failure at time 30: ", 68.0, functions.calculateY(30), 0.0);
        Assert.assertEquals("Failure at time maxValue: ", 68.0, functions.calculateY(Integer.MAX_VALUE), 0.0);
    }

    @Test
    public void testConstantFunction_02()
    {
        final int[][] timeValuePairs =
            {
                    {
                        0, 68
                    },
                    {
                        30, 68
                    },
                    {
                        30, 0
                    }
            };
        final Function functions = new CompositeFunction(timeValuePairs);
        Assert.assertEquals("Failure at time 0: ", 68.0, functions.calculateY(0), 0.0);
        Assert.assertEquals("Failure at time 29: ", 68.0, functions.calculateY(29), 0.0);
        Assert.assertEquals("Failure at time 30: ", 0.0, functions.calculateY(30), 0.0);
        Assert.assertEquals("Failure at time maxValue: ", 0.0, functions.calculateY(Integer.MAX_VALUE), 0.0);
    }

    @Test
    public void testConstantFunction_03()
    {
        final int[][] timeValuePairs =
            {
                    {
                        0, 3600
                    },
                    {
                        20, 3600
                    },
                    {
                        20, 720
                    },
                    {
                        70, 720
                    },
                    {
                        70, 0
                    },
                    {
                        100, 0
                    },
                    {
                        100, 1800
                    },
                    {
                        123, 1800
                    },
                    {
                        123, Integer.MAX_VALUE
                    },
                    {
                        134, Integer.MAX_VALUE
                    },
                    {
                        134, 17
                    }
            };
        final Function functions = new CompositeFunction(timeValuePairs);
        Assert.assertEquals("Failure at time 0: ", 3600.0, functions.calculateY(0), 0.0);
        Assert.assertEquals("Failure at time 7: ", 3600.0, functions.calculateY(7), 0.0);
        Assert.assertEquals("Failure at time 19: ", 3600.0, functions.calculateY(19), 0.0);
        Assert.assertEquals("Failure at time 20: ", 720.0, functions.calculateY(20), 0.0);
        Assert.assertEquals("Failure at time 21: ", 720.0, functions.calculateY(21), 0.0);
        Assert.assertEquals("Failure at time 55: ", 720.0, functions.calculateY(55), 0.0);
        Assert.assertEquals("Failure at time 70: ", 0.0, functions.calculateY(70), 0.0);
        Assert.assertEquals("Failure at time 99: ", 0.0, functions.calculateY(99), 0.0);
        Assert.assertEquals("Failure at time 100: ", 1800.0, functions.calculateY(100), 0.0);
        Assert.assertEquals("Failure at time 130: ", Integer.MAX_VALUE, functions.calculateY(130), 0.0);
        Assert.assertEquals("Failure at time maxValue: ", 17.0, functions.calculateY(Integer.MAX_VALUE), 0.0);
    }

    @Test
    public void testLinearFunction_01()
    {
        final int[][] timeValuePairs =
            {
                    {
                        0, 0
                    },
                    {
                        20, 3600
                    }
            };
        final Function functions = new CompositeFunction(timeValuePairs);
        Assert.assertEquals("Failure at time 0: ", 0.0, functions.calculateY(0), 0.0);
        Assert.assertEquals("Failure at time 5: ", 900.0, functions.calculateY(5), 0.0);
        Assert.assertEquals("Failure at time 10: ", 1800.0, functions.calculateY(10), 0.0);
        Assert.assertEquals("Failure at time 20: ", 3600.0, functions.calculateY(20), 0.0);
        Assert.assertEquals("Failure at time maxValue: ", 3600.0, functions.calculateY(Integer.MAX_VALUE), 0.0);
    }

    @Test
    public void testLinearFunction_02()
    {
        final int[][] timeValuePairs =
            {
                    {
                        0, 0
                    },
                    {
                        20, 0
                    },
                    {
                        40, 3600
                    }
            };
        final Function functions = new CompositeFunction(timeValuePairs);
        Assert.assertEquals("Failure at time 0: ", 0.0, functions.calculateY(0), 0.0);
        Assert.assertEquals("Failure at time 19: ", 0.0, functions.calculateY(0), 0.0);
        Assert.assertEquals("Failure at time 20: ", 0.0, functions.calculateY(20), 0.0);
        Assert.assertEquals("Failure at time 21: ", 180.0, functions.calculateY(21), 0.0);
        Assert.assertEquals("Failure at time 25: ", 900.0, functions.calculateY(25), 0.0);
        Assert.assertEquals("Failure at time 30: ", 1800.0, functions.calculateY(30), 0.0);
        Assert.assertEquals("Failure at time 40: ", 3600.0, functions.calculateY(40), 0.0);
        Assert.assertEquals("Failure at time 4000: ", 3600.0, functions.calculateY(4000), 0.0);
    }

    @Test
    public void testLinearFunction_03()
    {
        final int[][] timeValuePairs =
            {
                    {
                        0, 3600
                    },
                    {
                        20, 0
                    }
            };
        final Function functions = new CompositeFunction(timeValuePairs);
        Assert.assertEquals("Failure at time 0: ", 3600.0, functions.calculateY(0), 0.0);
        Assert.assertEquals("Failure at time 5: ", 2700.0, functions.calculateY(5), 0.0);
        Assert.assertEquals("Failure at time 10: ", 1800.0, functions.calculateY(10), 0.0);
        Assert.assertEquals("Failure at time 20: ", 0.0, functions.calculateY(20), 0.0);
        Assert.assertEquals("Failure at time maxValue: ", 0.0, functions.calculateY(Integer.MAX_VALUE), 0.0);
    }

    @Test
    public void testCompositeFunction()
    {
        final int[][] timeValuePairs =
            {
                    {
                        0, 3600
                    },
                    {
                        40, 1800
                    },
                    {
                        50, 1800
                    },
                    {
                        60, 0
                    },
                    {
                        60, 14267
                    },
                    {
                        70, 14267
                    },
                    {
                        70, 720
                    },
                    {
                        170, 1440
                    }
            };
        final Function functions = new CompositeFunction(timeValuePairs);
        Assert.assertEquals("Failure at time 0: ", 3600.0, functions.calculateY(0), 0.0);
        Assert.assertEquals("Failure at time 20: ", 2700.0, functions.calculateY(20), 0.0);
        Assert.assertEquals("Failure at time 40: ", 1800.0, functions.calculateY(40), 0.0);
        Assert.assertEquals("Failure at time 50: ", 1800.0, functions.calculateY(50), 0.0);
        Assert.assertEquals("Failure at time 55: ", 900.0, functions.calculateY(55), 0.0);
        Assert.assertEquals("Failure at time 60: ", 14267.0, functions.calculateY(60), 0.0);
        Assert.assertEquals("Failure at time 63: ", 14267.0, functions.calculateY(63), 0.0);
        Assert.assertEquals("Failure at time 70: ", 720.0, functions.calculateY(70), 0.0);
        Assert.assertEquals("Failure at time 95: ", 900.0, functions.calculateY(95), 0.0);
        Assert.assertEquals("Failure at time 170: ", 1440.0, functions.calculateY(170), 0.0);
        Assert.assertEquals("Failure at time maxValue: ", 1440.0, functions.calculateY(Integer.MAX_VALUE), 0.0);
    }
}
