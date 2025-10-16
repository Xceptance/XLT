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
package com.xceptance.xlt.agent;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import com.xceptance.xlt.agent.unipro.CompositeFunction;
import com.xceptance.xlt.agent.unipro.Function;
import com.xceptance.xlt.agentcontroller.TestUserConfiguration;

/**
 * Tests the {@link PeriodicExecutionTimer} class.
 * 
 * @author sebastianloob
 */
public class ArrivalRateControllerTimerTaskTest
{
    /**
     * The execution timer, that executes the test user configuration which contains the arrival rate.
     */
    private PeriodicExecutionTimer executionTimer;

    /**
     * Handles the actual start times of all user types.
     */
    private final HashMap<String, LinkedList<Long>> actualUserTypeStartTime = new HashMap<String, LinkedList<Long>>();

    /**
     * Handles the expected start times of all user types.
     */
    private final HashMap<String, LinkedList<Long>> expectedUserTypeStartTime = new HashMap<String, LinkedList<Long>>();

    @Before
    public void setUp()
    {
        actualUserTypeStartTime.clear();
        // create a mock, that executes the arrival rate controller timer task
        executionTimer = PowerMockito.mock(PeriodicExecutionTimer.class);
        PowerMockito.when(executionTimer.getThreads()).thenReturn(Collections.<Thread>emptySet());
    }

    /**
     * test the start of a test case with a constant arrival rate
     */
    @Test
    public void testConstantArrivalRate0()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testConstantArrivalRate0");
        final int arrivalRate[][] =
            {
                    {
                        0, 0
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case with a constant arrival rate
     */
    @Test
    public void testConstantArrivalRate360()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testConstantArrivalRate360");
        final int arrivalRate[][] =
            {
                    {
                        0, 360
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case with a constant arrival rate
     */
    @Test
    public void testConstantArrivalRate700()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testConstantArrivalRate700");
        final int arrivalRate[][] =
            {
                    {
                        0, 700
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case with a constant arrival rate
     */
    @Test
    public void testConstantArrivalRate3600()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testConstantArrivalRate3600");
        final int arrivalRate[][] =
            {
                    {
                        0, 3600
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case with a constant arrival rate
     */
    @Test
    public void testConstantArrivalRate7000()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testConstantArrivalRate7000");
        final int arrivalRate[][] =
            {
                    {
                        0, 7000
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case with a constant arrival rate
     */
    @Test
    public void testConstantArrivalRate7200()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testConstantArrivalRate7200");
        final int arrivalRate[][] =
            {
                    {
                        0, 7200
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case with a constant arrival rate
     */
    @Test
    public void testConstantArrivalRate14400()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testConstantArrivalRate14400");
        final int arrivalRate[][] =
            {
                    {
                        0, 14400
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case with a constant arrival rate after 50 seconds
     */
    @Test
    public void testConstantArrivalRateAfter50sFrom0To720()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testConstantArrivalRateAfter50sFrom0To720");
        final int arrivalRate[][] =
            {
                    {
                        0, 0
                    },
                    {
                        50, 0
                    },
                    {
                        50, 720
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case with a constant arrival rate after 50 seconds
     */
    @Test
    public void testConstantArrivalRateAfter50sFrom0To3600()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testConstantArrivalRateAfter50sFrom0To3600");
        final int arrivalRate[][] =
            {
                    {
                        0, 0
                    },
                    {
                        50, 0
                    },
                    {
                        50, 3600
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case with a constant arrival rate after 50 seconds
     */
    @Test
    public void testConstantArrivalRateAfter50sFrom0To7200()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testConstantArrivalRateAfter50sFrom0To7200");
        final int arrivalRate[][] =
            {
                    {
                        0, 0
                    },
                    {
                        50, 0
                    },
                    {
                        50, 7200
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case with a constant arrival rate after 50 seconds
     */
    @Test
    public void testConstantArrivalRateAfter50sFrom1To720()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testConstantArrivalRateAfter50sFrom1To720");
        final int arrivalRate[][] =
            {
                    {
                        0, 1
                    },
                    {
                        50, 1
                    },
                    {
                        50, 720
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case with a constant arrival rate after 50 seconds
     */
    @Test
    public void testConstantArrivalRateAfter50sFrom1To3600()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testConstantArrivalRateAfter50sFrom1To3600");
        final int arrivalRate[][] =
            {
                    {
                        0, 1
                    },
                    {
                        50, 1
                    },
                    {
                        50, 3600
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case with a constant arrival rate after 50 seconds
     */
    @Test
    public void testConstantArrivalRateAfter50sFrom1To7200()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testConstantArrivalRateAfter50sFrom1To7200");
        final int arrivalRate[][] =
            {
                    {
                        0, 1
                    },
                    {
                        50, 1
                    },
                    {
                        50, 7200
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate is like a ramp up
     */
    @Test
    public void testArrivalRateWithRampUp()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateWithRampUp");
        final int arrivalRate[][] =
            {
                    {
                        0, 120
                    },
                    {
                        10, 120
                    },
                    {
                        10, 150
                    },
                    {
                        20, 150
                    },
                    {
                        20, 180
                    },
                    {
                        30, 180
                    },
                    {
                        30, 210
                    },
                    {
                        40, 210
                    },
                    {
                        40, 240
                    },
                    {
                        50, 240
                    },
                    {
                        50, 270
                    },
                    {
                        60, 270
                    },
                    {
                        60, 300
                    },
                    {
                        70, 300
                    },
                    {
                        70, 330
                    },
                    {
                        80, 330
                    },
                    {
                        80, 360
                    },
                    {
                        90, 360
                    },
                    {
                        90, 390
                    },
                    {
                        100, 390
                    },
                    {
                        100, 420
                    },
                    {
                        110, 420
                    },
                    {
                        110, 450
                    },
                    {
                        120, 450
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases to 0 and increases to the old arrival rate
     */
    @Test
    public void testStartAfterArrivalRate0()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testStartAfterArrivalRate0");
        final int arrivalRate[][] =
            {
                    {
                        0, 720
                    },
                    {
                        11, 720
                    },
                    {
                        11, 0
                    },
                    {
                        16, 0
                    },
                    {
                        16, 720
                    },

            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases to 0 and increases to the old arrival rate
     */
    @Test
    public void testWaitToStartAfterArrivalRate0()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testWaitToStartAfterArrivalRate0");
        final int arrivalRate[][] =
            {
                    {
                        0, 720
                    },
                    {
                        11, 720
                    },
                    {
                        11, 0
                    },
                    {
                        13, 0
                    },
                    {
                        13, 720
                    },

            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases to 1 and increases to the old arrival rate
     */
    @Test
    public void testStartAfterArrivalRate1()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testStartAfterArrivalRate1");
        final int arrivalRate[][] =
            {
                    {
                        0, 720
                    },
                    {
                        11, 720
                    },
                    {
                        11, 1
                    },
                    {
                        16, 1
                    },
                    {
                        16, 720
                    },

            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases to 1 and increases to the old arrival rate
     */
    @Test
    public void testWaitToStartAfterArrivalRate1()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testWaitToStartAfterArrivalRate1");
        final int arrivalRate[][] =
            {
                    {
                        0, 720
                    },
                    {
                        11, 720
                    },
                    {
                        11, 1
                    },
                    {
                        13, 1
                    },
                    {
                        13, 720
                    },

            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * check, if no releases will make up after 10 seconds
     */
    @Test
    public void testDontMakeUpReleasesFromOldArrivalRate()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testDontMakeUpReleasesFromOldArrivalRate");
        final int arrivalRate[][] =
            {
                    {
                        0, 3600
                    },
                    {
                        10, 3600
                    },
                    {
                        10, 0
                    }

            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * check, if one releases will make up after 10 seconds
     */
    @Test
    public void testMakeUpReleasesFromOldArrivalRate()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testMakeUpReleasesFromOldArrivalRate");
        final int arrivalRate[][] =
            {
                    {
                        0, 3601
                    },
                    {
                        10, 3601
                    },
                    {
                        10, 0
                    }

            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * check, that just one user will released, if a new section of the arrival rate starts
     */
    @Test
    public void testStartOneUserInNewSectionAfterArrivalRate0()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testStartOneUserInNewSectionAfterArrivalRate0");
        final int arrivalRate[][] =
            {
                    {
                        0, 3600
                    },
                    {
                        11, 3600
                    },
                    {
                        11, 0
                    },
                    {
                        30, 0
                    },
                    {
                        30, 18000
                    },

            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * check, that just one user will released, if a new section of the arrival rate starts
     */
    @Test
    public void testStartOneUserInNewSectionAfterArrivalRate1()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testStartOneUserInNewSectionAfterArrivalRate0");
        final int arrivalRate[][] =
            {
                    {
                        0, 3600
                    },
                    {
                        11, 3600
                    },
                    {
                        11, 1
                    },
                    {
                        30, 1
                    },
                    {
                        30, 18000
                    },

            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate increases over a period
     */
    @Test
    public void testArrivalRateIncreasesTo360OverPeriod() throws InterruptedException
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateIncreasesTo360OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 0
                    },
                    {
                        60, 360
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate increases over a period
     */
    @Test
    public void testArrivalRateIncreasesTo3600OverPeriod() throws InterruptedException
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateIncreasesTo3600OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 0
                    },
                    {
                        60, 3600
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate increases over a period
     */
    @Test
    public void testArrivalRateIncreasesTo20000OverPeriod() throws InterruptedException
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateIncreasesTo20000OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 0
                    },
                    {
                        60, 20000
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate increases over a period after 30 seconds
     */
    @Test
    public void testArrivalRateIncreasesTo360OverPeriodAfter30Seconds() throws InterruptedException
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateIncreasesTo360OverPeriodAfter30Seconds");
        final int arrivalRate[][] =
            {
                    {
                        0, 0
                    },
                    {
                        30, 0
                    },
                    {
                        90, 360
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate increases over a period after 30 seconds
     */
    @Test
    public void testArrivalRateIncreasesTo3600OverPeriodAfter30Seconds() throws InterruptedException
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateIncreasesTo3600OverPeriodAfter30Seconds");
        final int arrivalRate[][] =
            {
                    {
                        0, 0
                    },
                    {
                        30, 0
                    },
                    {
                        60, 3600
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate increases over a period after 30 seconds
     */
    @Test
    public void testArrivalRateIncreasesTo20000OverPeriodAfter30Seconds() throws InterruptedException
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateIncreasesTo20000OverPeriodAfter30Seconds");
        final int arrivalRate[][] =
            {
                    {
                        0, 0
                    },
                    {
                        30, 0
                    },
                    {
                        60, 20000
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate increases from 3600 over a period
     */
    @Test
    public void testArrivalRateIncreasesFrom3600To3960OverPeriod() throws InterruptedException
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateIncreasesFrom3600To3960OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 3600
                    },
                    {
                        50, 3960
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate increases from 3600 over a period
     */
    @Test
    public void testArrivalRateIncreasesFrom3600To7200OverPeriod() throws InterruptedException
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateIncreasesFrom3600To7200OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 3600
                    },
                    {
                        50, 7200
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate increases from 3600 over a period
     */
    @Test
    public void testArrivalRateIncreasesFrom3600To23600OverPeriod() throws InterruptedException
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateIncreasesFrom3600To23600OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 3600
                    },
                    {
                        50, 23600
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases to 1 and increases to 360 over a period
     */
    @Test
    public void testArrivalRateDecreasesTo0IncreasesTo360OverPeriod()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateDecreasesTo0IncreasesTo360OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 3600
                    },
                    {
                        11, 3600
                    },
                    {
                        11, 0
                    },
                    {
                        30, 360
                    }

            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases to 1 and increases to 20000 over a period
     */
    @Test
    public void testArrivalRateDecreasesTo0IncreasesTo20000OverPeriod()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateDecreasesTo0IncreasesTo20000OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 3600
                    },
                    {
                        11, 3600
                    },
                    {
                        11, 0
                    },
                    {
                        30, 20000
                    }

            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases over a period
     */
    @Test
    public void testArrivalRateDecreasesFrom360To0OverPeriod()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateDecreasesFrom360To0OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 360
                    },
                    {
                        50, 0
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases over a period
     */
    @Test
    public void testArrivalRateDecreasesFrom700To0OverPeriod()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateDecreasesFrom700To0OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 700
                    },
                    {
                        50, 0
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases over a period
     */
    @Test
    public void testArrivalRateDecreasesFrom3600To0OverPeriod() throws InterruptedException
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateDecreasesFrom3600To0OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 3600
                    },
                    {
                        50, 0
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases over a period
     */
    @Test
    public void testArrivalRateDecreasesFrom20000To0OverPeriod()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateDecreasesFrom20000To0OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 20000
                    },
                    {
                        50, 0
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases over a period after 50 seconds
     */
    @Test
    public void testArrivalRateDecreasesFrom360To0OverPeriodAfter50Seconds()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateDecreasesFrom360To0OverPeriodAfter50Seconds");
        final int arrivalRate[][] =
            {
                    {
                        0, 360
                    },
                    {
                        50, 360
                    },
                    {
                        100, 0
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases over a period after 50 seconds
     */
    @Test
    public void testArrivalRateDecreasesFrom700To0OverPeriodAfter50Seconds()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateDecreasesFrom700To0OverPeriodAfter50Seconds");
        final int arrivalRate[][] =
            {
                    {
                        0, 700
                    },
                    {
                        50, 700
                    },
                    {
                        100, 0
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases over a period after 50 seconds
     */
    @Test
    public void testArrivalRateDecreasesFrom3600To0OverPeriodAfter50Seconds() throws InterruptedException
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateDecreasesFrom3600To0OverPeriodAfter50Seconds");
        final int arrivalRate[][] =
            {
                    {
                        0, 3600
                    },
                    {
                        50, 3600
                    },
                    {
                        100, 0
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases over a period after 50 seconds
     */
    @Test
    public void testArrivalRateDecreasesFrom20000To0OverPeriodAfter50Seconds()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateDecreasesFrom20000To0OverPeriodAfter50Seconds");
        final int arrivalRate[][] =
            {
                    {
                        0, 20000
                    },
                    {
                        50, 20000
                    },
                    {
                        100, 0
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases over a period and increases immediately
     */
    @Test
    public void testArrivalRateDecreasesFrom3600To0OverPeriodIncreasesTo360()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateDecreasesFrom3600To0OverPeriodIncreasesTo360");
        final int arrivalRate[][] =
            {
                    {
                        0, 3600
                    },
                    {
                        50, 3600
                    },
                    {
                        100, 0
                    },
                    {
                        100, 360
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases over a period and increases immediately
     */
    @Test
    public void testArrivalRateDecreasesFrom3600To0OverPeriodIncreasesTo3600()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateDecreasesFrom3600To0OverPeriodIncreasesTo3600");
        final int arrivalRate[][] =
            {
                    {
                        0, 3600
                    },
                    {
                        50, 3600
                    },
                    {
                        100, 0
                    },
                    {
                        100, 3600
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases over a period and increases immediately
     */
    @Test
    public void testArrivalRateDecreasesFrom3600To0OverPeriodIncreasesTo20000()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateDecreasesFrom3600To0OverPeriodIncreasesTo20000");
        final int arrivalRate[][] =
            {
                    {
                        0, 3600
                    },
                    {
                        50, 3600
                    },
                    {
                        100, 0
                    },
                    {
                        100, 20000
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases over a period and increases over a period
     */
    @Test
    public void testArrivalRateDecreasesTo0OverPeriodIncreasesTo360OverPeriod()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateDecreasesTo0OverPeriodIncreasesTo360OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 3600
                    },
                    {
                        50, 3600
                    },
                    {
                        100, 0
                    },
                    {
                        150, 360
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases over a period and increases over a period
     */
    @Test
    public void testArrivalRateDecreasesTo0OverPeriodIncreasesTo3600OverPeriod()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateDecreasesTo0OverPeriodIncreasesTo3600OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 3600
                    },
                    {
                        50, 3600
                    },
                    {
                        100, 0
                    },
                    {
                        150, 3600
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate decreases over a period and increases over a period
     */
    @Test
    public void testArrivalRateDecreasesTo0OverPeriodIncreasesTo20000OverPeriod()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateDecreasesTo0OverPeriodIncreasesTo20000OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 3600
                    },
                    {
                        50, 3600
                    },
                    {
                        100, 0
                    },
                    {
                        150, 20000
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate increases over a period and decreases over a period
     */
    @Test
    public void testArrivalRateIncreasesTo360OverPeriodDecreasesBackTo0OverPeriod()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateIncreasesTo360OverPeriodDecreasesBackTo0OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 0
                    },
                    {
                        50, 0
                    },
                    {
                        100, 360
                    },
                    {
                        150, 0
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate increases over a period and decreases over a period
     */
    @Test
    public void testArrivalRateIncreasesTo3600OverPeriodDecreasesBackTo0OverPeriod()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateIncreasesTo3600OverPeriodDecreasesBackTo0OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 0
                    },
                    {
                        50, 0
                    },
                    {
                        100, 3600
                    },
                    {
                        150, 0
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * test the start of a test case, if the arrival rate increases over a period and decreases over a period
     */
    @Test
    public void testArrivalRateIncreasesTo20000OverPeriodDecreasesBackTo0OverPeriod()
    {
        final TestUserConfiguration config = getDefaultConfig();
        config.setUserName("testArrivalRateIncreasesTo20000OverPeriodDecreasesBackTo0OverPeriod");
        final int arrivalRate[][] =
            {
                    {
                        0, 0
                    },
                    {
                        50, 0
                    },
                    {
                        100, 20000
                    },
                    {
                        150, 0
                    }
            };
        config.setArrivalRates(arrivalRate);
        startCheck(config);
    }

    /**
     * checks the starting times of the user
     */
    private void startCheck(final TestUserConfiguration config)
    {
        // start the test and get the actual start times
        final PeriodicExecutionTimer.ArrivalRateControllerTimerTask timerTask = new PeriodicExecutionTimer.ArrivalRateControllerTimerTask(
                                                                                                                                          config.getArrivalRates(),
                                                                                                                                          config.getAgentIndex(),
                                                                                                                                          config.getWeightFunction(),
                                                                                                                                          executionTimer,
                                                                                                                                          0);

        // run the timer task every second of the measurement period
        final int endTime = config.getMeasurementPeriod() / 1000;
        for (int time = 0; time <= endTime; time++)
        {
            // get the number of user, the timer task release at the current time
            int lastReleases = timerTask.computeReleases(time);
            // release the user one by one
            while (lastReleases > 0)
            {
                setActualUserTypeStartTime(config.getUserName(), (long) time);
                lastReleases--;
            }
        }

        // compute the expected start times
        computeExpectedArrivalRateStarts(config);
        // check, if the expected and the actual start times are equal
        checkIfStartsAreEqual();
    }

    /**
     * returns a default TestUserConfiguration
     */
    private TestUserConfiguration getDefaultConfig()
    {
        final TestUserConfiguration config = new TestUserConfiguration();
        config.setAgentIndex(0);
        config.setInitialDelay(0);
        config.setMeasurementPeriod(300000);
        config.setUsers(new int[][]
            {
                    {
                        0, 100
                    }
            });
        config.setWeightFunction(new double[]
            {
                1.0
            });
        return config;
    }

    /**
     * adds a new actual start time
     */
    private void setActualUserTypeStartTime(final String userName, final Long time)
    {
        LinkedList<Long> startTimes = actualUserTypeStartTime.get(userName);
        if (startTimes == null)
        {
            startTimes = new LinkedList<Long>();
            actualUserTypeStartTime.put(userName, startTimes);
        }
        startTimes.addLast(time);
    }

    /**
     * adds a new expected start time
     */
    private void setExpectedUserTypeStartTime(final String userType, final Long time)
    {
        LinkedList<Long> startTimes = expectedUserTypeStartTime.get(userType);
        if (startTimes == null)
        {
            startTimes = new LinkedList<Long>();
            expectedUserTypeStartTime.put(userType, startTimes);
        }
        startTimes.addLast(time);
    }

    /**
     * Computes start times of an user type and remembers these time stamps in the expected user type start time map.
     * These start times are defined by the arrival rate, which is set in the test user configuration.
     * 
     * @param config
     */
    private void computeExpectedArrivalRateStarts(final TestUserConfiguration config)
    {
        // the name of the test case
        final String user = config.getUserName();
        // the measurement period of the test case
        final long measurementPeriod = config.getMeasurementPeriod() / 1000;
        // the time, a user was released last
        double lastReleaseTime = 0.0;
        // the current time in the test case
        long currentTime = 0L;
        // the arrival rate function of the test case
        final Function arrivalRateFunction = new CompositeFunction(config.getArrivalRates());
        // compute the releases for each second of the measurement period
        while (currentTime <= measurementPeriod)
        {
            // get the current arrival rate
            final double currentArrivalRate = arrivalRateFunction.calculateY(currentTime);
            // special case: current time is 0
            if (currentTime == 0)
            {
                // just release one user, if the arrival rate is greater than 0
                if (currentArrivalRate > 0)
                {
                    setExpectedUserTypeStartTime(user, currentTime);
                }
            }
            // special case, if we are in a new section of the arrival rate
            else if (isSpecialPoint(config.getArrivalRates(), currentTime))
            {
                // make up the releases of the old arrival rate
                final double lastArrivalRate = arrivalRateFunction.calculateY(currentTime - 1);
                final double lastPeriod = 3600 / lastArrivalRate;
                while ((lastReleaseTime + lastPeriod) < currentTime)
                {
                    // release one user
                    setExpectedUserTypeStartTime(user, currentTime);
                    // adjust the last release time
                    lastReleaseTime += lastPeriod;
                }
                // get the new period
                final double currentPeriod = 3600 / currentArrivalRate;
                // check, if at least one user can be released, but limit the released user to 1
                if ((lastReleaseTime + currentPeriod) <= currentTime)
                {
                    // just release one user
                    setExpectedUserTypeStartTime(user, currentTime);
                    // adjust the last release time
                    lastReleaseTime = currentTime;
                }
            }
            // between two special points of the arrival rate
            else
            {
                // make up the releases of the old arrival rate
                final double lastArrivalRate = arrivalRateFunction.calculateY(currentTime - 1);
                final double lastPeriod = 3600 / lastArrivalRate;
                while ((lastReleaseTime + lastPeriod) < currentTime)
                {
                    // release one user
                    setExpectedUserTypeStartTime(user, currentTime);
                    // adjust the last release time
                    lastReleaseTime += lastPeriod;
                }
                // get the new period
                final double period = 3600 / currentArrivalRate;
                // release user, if the next release time is in the past
                while ((lastReleaseTime + period) <= currentTime)
                {
                    // release one user
                    setExpectedUserTypeStartTime(user, currentTime);
                    // adjust last release time
                    lastReleaseTime += period;
                }
            }
            // we have to adjust the last release time, if the arrival rate increases from 0
            if (currentArrivalRate == 0)
            {
                final long nextSecond = currentTime + 1;
                final double nextArrivalRate = arrivalRateFunction.calculateY(nextSecond);
                // adjust last release time, if the arrival rate will increase in the next second
                if (nextArrivalRate > 0)
                {
                    final double nextPeriod = 3600 / nextArrivalRate;
                    // use maximum to prevent a rewind of the last release time
                    lastReleaseTime = Math.max(lastReleaseTime, nextSecond - nextPeriod - 0.000001);
                }
            }
            // go to the next second
            currentTime++;
        }
    }

    /**
     * Returns true, if a value at the current time is explicit specified by the arrival rate function.
     * 
     * @param arrivalRate
     * @param currentTime
     * @return true, if a value at the current time is explicit specified by the arrival rate
     */
    private boolean isSpecialPoint(final int[][] arrivalRate, final long currentTime)
    {
        boolean isSpecial = false;
        for (int i = 1; i < arrivalRate.length; i++)
        {
            if (arrivalRate[i][0] == currentTime)
            {
                isSpecial = true;
                break;
            }
        }
        return isSpecial;
    }

    /*
     * check, if the actual start times are equal to the expected start times
     */
    private void checkIfStartsAreEqual()
    {
        for (final String user : expectedUserTypeStartTime.keySet())
        {
            for (int i = 0; i < expectedUserTypeStartTime.get(user).size(); i++)
            {
                Assert.assertEquals(expectedUserTypeStartTime.get(user).get(i), actualUserTypeStartTime.get(user).get(i));
            }
        }
        // both maps have to be equal, if each release time in the maps is equal
        Assert.assertEquals(expectedUserTypeStartTime, actualUserTypeStartTime);
    }
}
