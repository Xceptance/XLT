/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the implementation of {@link LoadFunctionUtils}.
 */
public class LoadFunctionUtilsTest
{
    // ======================
    // Various more complex load function scenarios
    // ======================
    @Test
    public void testCalcLoadFunctionForRampUp_1()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(0, 10, 1, -1, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 0
                },
                {
                    1, 10
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_3()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(0, 10, -1, -1, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 10
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_4()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(0, 10, 1, 5, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 0
                },
                {
                    1, 0
                },
                {
                    1, 10
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_5()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(1, 10, 1, 5, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 1
                },
                {
                    1, 1
                },
                {
                    1, 10
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_6()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(0, 20, 2, 5, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 0
                },
                {
                    1, 0
                },
                {
                    1, 10
                },
                {
                    2, 10
                },
                {
                    2, 20
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_6b()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(1, 20, 2, 5, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 1
                },
                {
                    1, 1
                },
                {
                    1, 11
                },
                {
                    2, 11
                },
                {
                    2, 20
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_7()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(0, 10, 5, 2, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 0
                },
                {
                    1, 0
                },
                {
                    1, 2
                },
                {
                    2, 2
                },
                {
                    2, 4
                },
                {
                    3, 4
                },
                {
                    3, 6
                },
                {
                    4, 6
                },
                {
                    4, 8
                },
                {
                    5, 8
                },
                {
                    5, 10
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_8()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(0, 10, 10, 2, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 0
                },
                {
                    2, 0
                },
                {
                    2, 2
                },
                {
                    4, 2
                },
                {
                    4, 4
                },
                {
                    6, 4
                },
                {
                    6, 6
                },
                {
                    8, 6
                },
                {
                    8, 8
                },
                {
                    10, 8
                },
                {
                    10, 10
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_9()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(0, 10, 10, 3, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 0
                },
                {
                    3, 0
                },
                {
                    3, 3
                },
                {
                    5, 3
                },
                {
                    5, 6
                },
                {
                    8, 6
                },
                {
                    8, 9
                },
                {
                    10, 9
                },
                {
                    10, 10
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_10()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(1, 9, 5, 2, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 1
                },
                {
                    2, 1
                },
                {
                    2, 3
                },
                {
                    3, 3
                },
                {
                    3, 5
                },
                {
                    4, 5
                },
                {
                    4, 7
                },
                {
                    5, 7
                },
                {
                    5, 9
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_11()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(3, 7, 9, 5, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 3
                },
                {
                    9, 3
                },
                {
                    9, 7
                },
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_12()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(3, 10, 9, 5, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 3
                },
                {
                    5, 3
                },
                {
                    5, 8
                },
                {
                    9, 8
                },
                {
                    9, 10
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_13()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(0, 10, -1, 5, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 10
                },
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_14()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(3, 10, -1, 5, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 10
                },
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_15()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(3, 15, 135, 4, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 3
                },
                {
                    45, 3
                },
                {
                    45, 7
                },
                {
                    90, 7
                },
                {
                    90, 11
                },
                {
                    135, 11
                },
                {
                    135, 15
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_16()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(0, 12, 135, 4, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 0
                },
                {
                    45, 0
                },
                {
                    45, 4
                },
                {
                    90, 4
                },
                {
                    90, 8
                },
                {
                    135, 8
                },
                {
                    135, 12
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_17()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(0, 10, 10, 1, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 0
                },
                {
                    10, 10
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_18()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(0, 10, 10, -1, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 0
                },
                {
                    10, 10
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUp_19()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(5, 20, 100, -1, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 5
                },
                {
                    100, 20
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUpUsingRampUpSteadyPeriod_1()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(0, 20, -1, 5, 3);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 0
                },
                {
                    3, 0
                },
                {
                    3, 5
                },
                {
                    6, 5
                },
                {
                    6, 10
                },
                {
                    9, 10
                },
                {
                    9, 15
                },
                {
                    12, 15
                },
                {
                    12, 20
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUpUsingRampUpSteadyPeriod_3()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(1, 20, -1, 5, 3);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 1
                },
                {
                    3, 1
                },
                {
                    3, 6
                },
                {
                    6, 6
                },
                {
                    6, 11
                },
                {
                    9, 11
                },
                {
                    9, 16
                },
                {
                    12, 16
                },
                {
                    12, 20
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUpUsingRampUpSteadyPeriod_4()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(0, 12, -1, 4, 45);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 0
                },
                {
                    45, 0
                },
                {
                    45, 4
                },
                {
                    90, 4
                },
                {
                    90, 8
                },
                {
                    135, 8
                },
                {
                    135, 12
                }
            }, function);
    }

    @Test
    public void testCalcLoadFunctionForRampUpUsingRampUpSteadyPeriod_5()
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(3, 15, -1, 4, 45);
        // printLoadFunction((function);
        Assert.assertArrayEquals("Unexpected resulting load function", new int[][]
            {
                {
                    0, 3
                },
                {
                    45, 3
                },
                {
                    45, 7
                },
                {
                    90, 7
                },
                {
                    90, 11
                },
                {
                    135, 11
                },
                {
                    135, 15
                }
            }, function);
    }

    // ====================================
    // rampUpPeriod
    // ====================================

    @Test
    public void computeLoadFunction_rampUpPeriod_0()
    {
        final int rampUpPeriod = 0;
        final int[][] function = LoadFunctionUtils.computeLoadFunction(-1, 10, rampUpPeriod, -1, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, 10
                },
            }, function);
    }

    @Test
    public void computeLoadFunction_rampUpPeriod_1()
    {
        final int rampUpPeriod = 1;
        final int[][] function = LoadFunctionUtils.computeLoadFunction(-1, 10, rampUpPeriod, -1, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, 1
                },
                {
                    1, 10
                },
            }, function);
    }

    @Test
    public void computeLoadFunction_rampUpPeriod_10()
    {
        final int rampUpPeriod = 10;
        final int[][] function = LoadFunctionUtils.computeLoadFunction(-1, 10, rampUpPeriod, -1, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, 1
                },
                {
                    10, 10
                },
            }, function);
    }

    @Test(expected = IllegalArgumentException.class)
    public void computeLoadFunction_rampUpPeriod_steadyPeriodGiven()
    {
        LoadFunctionUtils.computeLoadFunction(-1, 10, 10, -1, 1);
    }

    // ====================================
    // rampUpSteadyPeriod
    // ====================================

    @Test
    public void computeLoadFunction_rampUpSteadyPeriod_0()
    {
        final int rampUpSteadyPeriod = 0;
        final int[][] function = LoadFunctionUtils.computeLoadFunction(-1, 10, -1, -1, rampUpSteadyPeriod);
        // printLoadFunction((function);
        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, 10
                },
            }, function);
    }

    @Test
    public void computeLoadFunction_rampUpSteadyPeriod_1()
    {
        final int rampUpSteadyPeriod = 1;
        final int[][] function = LoadFunctionUtils.computeLoadFunction(-1, 10, -1, -1, rampUpSteadyPeriod);
        // printLoadFunction((function);
        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, 1
                },
                {
                    9, 10
                },
            }, function);
    }

    @Test
    public void computeLoadFunction_rampUpSteadyPeriod_2()
    {
        final int rampUpSteadyPeriod = 2;
        final int[][] function = LoadFunctionUtils.computeLoadFunction(-1, 10, -1, -1, rampUpSteadyPeriod);
        // printLoadFunction((function);
        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, 1
                },
                {
                    18, 10
                },
            }, function);
    }

    // ====================================
    // rampUpStepSize
    // ====================================

    @Test
    public void computeLoadFunction_rampUpStepSize_unspecified()
    {
        computeLoadFunctionForRampUpStepSize(-1, 1);
    }

    @Test
    public void computeLoadFunction_rampUpStepSize_0()
    {
        computeLoadFunctionForRampUpStepSize(0, 1);
    }

    @Test
    public void computeLoadFunction_rampUpStepSize_1()
    {
        computeLoadFunctionForRampUpStepSize(1, 1);
    }

    @Test
    public void computeLoadFunction_rampUpStepSize_2()
    {
        final int rampUpStepSize = 2;
        final int[][] function = LoadFunctionUtils.computeLoadFunction(-1, 10, 10, rampUpStepSize, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, 2
                },
                {
                    3, 2
                },
                {
                    3, 4
                },
                {
                    5, 4
                },
                {
                    5, 6
                },
                {
                    8, 6
                },
                {
                    8, 8
                },
                {
                    10, 8
                },
                {
                    10, 10
                },
            }, function);
    }

    @Test
    public void computeLoadFunction_rampUpStepSize_3()
    {
        final int rampUpStepSize = 3;
        final int[][] function = LoadFunctionUtils.computeLoadFunction(-1, 10, 10, rampUpStepSize, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, 3
                },
                {
                    4, 3
                },
                {
                    4, 6
                },
                {
                    7, 6
                },
                {
                    7, 9
                },
                {
                    10, 9
                },
                {
                    10, 10
                },
            }, function);
    }

    private void computeLoadFunctionForRampUpStepSize(final int rampUpStepSize, final int expectedInitialValue)
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(-1, 10, 10, rampUpStepSize, -1);
        // printLoadFunction((function);
        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, expectedInitialValue
                },
                {
                    10, 10
                },
            }, function);
    }

    // ====================================
    // rampUpInitialValue
    // ====================================

    @Test
    public void computeLoadFunction_rampUpInitialValue_unspecified()
    {
        computeLoadFunctionForRampUpInitialValue(-1, 1);
    }

    @Test
    public void computeLoadFunction_rampUpInitialValue_0()
    {
        computeLoadFunctionForRampUpInitialValue(0, 0);
    }

    @Test
    public void computeLoadFunction_rampUpInitialValue_1()
    {
        computeLoadFunctionForRampUpInitialValue(1, 1);
    }

    @Test
    public void computeLoadFunction_rampUpInitialValue_2()
    {
        computeLoadFunctionForRampUpInitialValue(2, 2);
    }

    @Test
    public void computeLoadFunction_rampUpInitialValue_3()
    {
        computeLoadFunctionForRampUpInitialValue(3, 3);
    }

    @Test
    public void computeLoadFunction_rampUpInitialValue_equalToTargetValue()
    {
        computeLoadFunctionForRampUpInitialValue(10, -1);
    }

    @Test
    public void computeLoadFunction_rampUpInitialValue_greaterThanTargetValue()
    {
        computeLoadFunctionForRampUpInitialValue(11, -1);
    }

    private void computeLoadFunctionForRampUpInitialValue(final int rampUpInitialValue, final int expectedInitialValue)
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(rampUpInitialValue, 10, 10, -1, -1);
        // printLoadFunction((function);

        final int[][] expectedFunction;
        if (expectedInitialValue == -1)
        {
            expectedFunction = new int[][]
                {
                    {
                        0, 10
                    },
                };
        }
        else
        {
            expectedFunction = new int[][]
                {
                    {
                        0, expectedInitialValue
                    },
                    {
                        10, 10
                    },
                };
        }

        Assert.assertArrayEquals(expectedFunction, function);
    }

    // ====================================
    // rampUpTargetValue
    // ====================================

    @Test(expected = IllegalArgumentException.class)
    public void computeLoadFunction_rampUpTargetValue_unspecified()
    {
        computeLoadFunctionForRampUpTargetValue(-1);
    }

    @Test
    public void computeLoadFunction_rampUpTargetValue_0()
    {
        computeLoadFunctionForRampUpTargetValue(0);
    }

    @Test
    public void computeLoadFunction_rampUpTargetValue_1()
    {
        computeLoadFunctionForRampUpTargetValue(1);
    }

    @Test
    public void computeLoadFunction_rampUpTargetValue_2()
    {
        computeLoadFunctionForRampUpTargetValue(2);
    }

    @Test
    public void computeLoadFunction_rampUpTargetValue_10()
    {
        computeLoadFunctionForRampUpTargetValue(10);
    }

    private void computeLoadFunctionForRampUpTargetValue(final int rampUpTargetValue)
    {
        final int[][] function = LoadFunctionUtils.computeLoadFunction(-1, rampUpTargetValue, 10, -1, -1);
        // printLoadFunction((function);

        final int[][] expectedFunction;
        if (rampUpTargetValue <= 1)
        {
            expectedFunction = new int[][]
                {
                    {
                        0, rampUpTargetValue
                    },
                };
        }
        else
        {
            expectedFunction = new int[][]
                {
                    {
                        0, 1
                    },
                    {
                        10, rampUpTargetValue
                    },
                };
        }

        Assert.assertArrayEquals(expectedFunction, function);
    }

    // ====================================
    // loadFunctionToString
    // ====================================

    @Test
    public void loadFunctionToString_empty()
    {
        final int[][] function = new int[][] {};
        Assert.assertEquals("", LoadFunctionUtils.loadFunctionToString(function));
    }

    @Test
    public void loadFunctionToString_simple()
    {
        final int[][] function = new int[][]
            {
                new int[]
                {
                    0, 1
                }
            };
        Assert.assertEquals("1", LoadFunctionUtils.loadFunctionToString(function));
    }

    @Test
    public void loadFunctionToString_notSoSimple()
    {
        final int[][] function = new int[][]
            {
                new int[]
                {
                    2100, 1
                }
            };
        Assert.assertEquals("35m/1", LoadFunctionUtils.loadFunctionToString(function));
    }

    @Test
    public void loadFunctionToString_complex()
    {
        final int[][] function = new int[][]
            {
                {
                    3600 +
                 600 + 10, 1
                },
                {
                    3600 +
                 600, 2
                },
                {
                    3600 +
                 10, 3
                },
                {
                    3600, 4
                },
                {
                    600 +
                 10, 5
                },
                {
                    600, 6
                },
                {
                    10, 7
                },
                {
                    0, 8
                }
            };
        Assert.assertEquals("1h10m10s/1 1h10m/2 1h10s/3 1h/4 10m10s/5 10m/6 10s/7 0s/8", LoadFunctionUtils.loadFunctionToString(function));
    }

    // ====================================
    // checkLoadFunction
    // ====================================

    @Test(expected = IllegalArgumentException.class)
    public void testNoTimeValuePairIsDefined()
    {
        LoadFunctionUtils.checkLoadFunction(new int[][] {});
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testEmptyTimeValuePairIsDefined()
    {
        LoadFunctionUtils.checkLoadFunction(new int[][]
            {
                {}
            });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTimeIsNegative_01()
    {
        LoadFunctionUtils.checkLoadFunction(new int[][]
            {
                {
                    -5, 10
                }
            });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTimeIsNegative_02()
    {
        LoadFunctionUtils.checkLoadFunction(new int[][]
            {
                {
                    0, 10
                },
                {
                    5, 20
                },
                {
                    -10, 30
                }
            });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueIsNegative_01()
    {
        LoadFunctionUtils.checkLoadFunction(new int[][]
            {
                {
                    5, -10
                }
            });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueIsNegative_02()
    {
        LoadFunctionUtils.checkLoadFunction(new int[][]
            {
                {
                    0, 10
                },
                {
                    5, 20
                },
                {
                    10, -30
                }
            });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTimeNotInAscendingOrder()
    {
        LoadFunctionUtils.checkLoadFunction(new int[][]
            {
                {
                    0, 10
                },
                {
                    10, 20
                },
                {
                    5, 30
                }
            });
    }

    @Test
    public void testValidFunction()
    {
        LoadFunctionUtils.checkLoadFunction(new int[][]
            {
                {
                    0, 10
                },
                {
                    10, 20
                },
                {
                    20, 30
                }
            });
    }

    // ====================================
    // completeLoadFunctionIfNecessary
    // ====================================

    @Test
    public void testLoadFunctionIsComplete_01()
    {
        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, 5
                }
            }, LoadFunctionUtils.completeLoadFunctionIfNecessary(new int[][]
            {
                {
                    0, 5
                }
            }));
    }

    @Test
    public void testLoadFunctionIsComplete_02()
    {
        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, 5
                },
                {
                    20, 20
                }
            }, LoadFunctionUtils.completeLoadFunctionIfNecessary(new int[][]
            {
                {
                    0, 5
                },
                {
                    20, 20
                }
            }));
    }

    @Test
    public void testFunctionIsIncompleteAndFirstValueIsOne()
    {
        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, 1
                },
                {
                    10, 1
                },
                {
                    20, 20
                }
            }, LoadFunctionUtils.completeLoadFunctionIfNecessary(new int[][]
            {
                {
                    10, 1
                },
                {
                    20, 20
                }
            }));
    }

    @Test
    public void testFunctionIsIncomplete_01()
    {
        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, 1
                },
                {
                    10, 2
                },
                {
                    20, 20
                }
            }, LoadFunctionUtils.completeLoadFunctionIfNecessary(new int[][]
            {
                {
                    10, 2
                },
                {
                    20, 20
                }
            }));
    }

    @Test
    public void testFunctionIsIncomplete_02()
    {
        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, 1
                },
                {
                    10, 0
                },
                {
                    20, 20
                }
            }, LoadFunctionUtils.completeLoadFunctionIfNecessary(new int[][]
            {
                {
                    10, 0
                },
                {
                    20, 20
                }
            }));
    }

    // ====================================
    // scaleLoadFunction
    // ====================================

    @Test
    public void testScaleLoadFunction_specialCases()
    {
        final int[][] emptyLoadFunction = new int[0][];
        final int[][] loadFunction = new int[][]
            {
                {
                    0, 50
                },
                {
                    20, 20
                }
            };

        Assert.assertArrayEquals(null, LoadFunctionUtils.scaleLoadFunction(null, null));
        Assert.assertArrayEquals(null, LoadFunctionUtils.scaleLoadFunction(null, loadFunction));
        Assert.assertArrayEquals(loadFunction, LoadFunctionUtils.scaleLoadFunction(loadFunction, null));

        Assert.assertArrayEquals(emptyLoadFunction, LoadFunctionUtils.scaleLoadFunction(emptyLoadFunction, emptyLoadFunction));
        Assert.assertArrayEquals(emptyLoadFunction, LoadFunctionUtils.scaleLoadFunction(emptyLoadFunction, loadFunction));
        Assert.assertArrayEquals(loadFunction, LoadFunctionUtils.scaleLoadFunction(loadFunction, emptyLoadFunction));
    }

    @Test
    public void testScaleLoadFunction_simpleValue_simpleLoadFactor()
    {
        final int[][] loadFunction = new int[][]
            {
                {
                    0, 50
                }
            };

        final int[][] loadFactorFunction = new int[][]
            {
                {
                    0, 1_500
                }
            };

        final int[][] result = LoadFunctionUtils.scaleLoadFunction(loadFunction, loadFactorFunction);

        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, 75
                }
            }, result);
    }

    @Test
    public void testScaleLoadFunction_complexValue_simpleLoadFactor()
    {
        final int[][] loadFunction = new int[][]
            {
                {
                    0, 50
                },
                {
                    20, 20
                }
            };

        final int[][] loadFactorFunction = new int[][]
            {
                {
                    0, 1500
                }
            };

        final int[][] result = LoadFunctionUtils.scaleLoadFunction(loadFunction, loadFactorFunction);

        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, 75
                },
                {
                    20, 30
                }
            }, result);
    }

    @Test
    public void testScaleLoadFunction_simpleValue_complexLoadFactor()
    {
        final int[][] loadFunction = new int[][]
            {
                {
                    0, 17
                }
            };

        final int[][] loadFactorFunction = new int[][]
            {
                {
                    0, 150
                },
                {
                    15, 300
                },
                {
                    30, 500
                },
                {
                    90, 750
                },
                {
                    300, 2500
                },
                {
                    600, 5
                }
            };

        final int[][] result = LoadFunctionUtils.scaleLoadFunction(loadFunction, loadFactorFunction);

        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, 3
                },
                {
                    15, 6
                },
                {
                    30, 9
                },
                {
                    90, 13
                },
                {
                    300, 43
                },
                {
                    600, 1
                }
            }, result);
    }

    @Test
    public void testScaleLoadFunction_complexValue_complexLoadFactor()
    {
        final int[][] loadFunction = new int[][]
            {
                {
                    0, 75
                },
                {
                    20, 20
                }
            };

        final int[][] loadFactorFunction = new int[][]
            {
                {
                    0, 150
                },
                {
                    0, 250
                }
            };

        final int[][] result = LoadFunctionUtils.scaleLoadFunction(loadFunction, loadFactorFunction);

        Assert.assertArrayEquals(new int[][]
            {
                {
                    0, 75
                },
                {
                    20, 20
                }
            }, result);
    }

    @SuppressWarnings("unused")
    private static void printLoadFunction(final int[][] function)
    {
        for (int i = 0; i < function.length; i++)
        {
            System.out.printf("%d/%d ", function[i][0], function[i][1]);
        }
        System.out.printf("-> %d entries\n", function.length);
    }
}
