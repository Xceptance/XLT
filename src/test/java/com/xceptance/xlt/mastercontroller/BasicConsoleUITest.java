package com.xceptance.xlt.mastercontroller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(
    {
        BasicConsoleUI.class
    })
public class BasicConsoleUITest
{
    /**
     * Create a new instance of {@link BasicConsoleUI#BasicConsoleUI(MasterController)} with mocked mastercontroller as
     * constructor parameter.
     * 
     * @return a new instance of {@link BasicConsoleUI}
     */
    private static BasicConsoleUI createNewTestObject()
    {
        MasterController masterControllerMock = mock(MasterController.class);

        return new BasicConsoleUI(masterControllerMock)
        {
            @Override
            public void run()
            {
                throw new NotImplementedException("");
            }
        };
    }

    /**
     * Test for bug #2624 in {@link BasicConsoleUI#getUserFriendlyExceptionMessage(Exception)} which was throwing a null
     * pointer exception when the message of the exception parameter was null like for the NullPointerException.
     */
    @Test
    public final void getUserFriendlyExceptionMessage_exceptionMessageIsNull_2624()
    {
        // GIVEN: a fresh default BasicConsoleUI instance
        BasicConsoleUI consoleUI = createNewTestObject();

        // WHEN: calling getUserFriendlyExceptionMessage with an exception where the message is null
        String friendlyMessage = consoleUI.getUserFriendlyExceptionMessage(new NullPointerException(null));

        // THEN: no exception should be thrown and the friendly message should be the empty string
        assertEquals("", friendlyMessage);
    }

    /**
     * Test for method {@link BasicConsoleUI#getUserFriendlyExceptionMessage(Exception)} which throw a
     * NullPointerException if the exception parameter is null
     */
    @Test(expected = NullPointerException.class)
    public final void getUserFriendlyExceptionMessage_exceptionParameterIsNull()
    {
        // GIVEN: a fresh default BasicConsoleUI instance
        BasicConsoleUI consoleUI = createNewTestObject();

        // WHEN: calling getUserFriendlyExceptionMessage with null as exception parameter
        consoleUI.getUserFriendlyExceptionMessage(null);

        // THEN: a NullPointerException should be thrown
        // should not be reached
    }

    /**
     * Test for method {@link BasicConsoleUI#getUserFriendlyExceptionMessage(Exception)} which should return the
     * friendly representation when the exception message starts with "401:"
     */
    @Test
    public final void getUserFriendlyExceptionMessage_exceptionMessageIs401()
    {
        // TEST_PARAMETER:
        final String message = "401: foo bar";

        // GIVEN: a fresh default BasicConsoleUI instance
        BasicConsoleUI consoleUI = createNewTestObject();

        // WHEN: calling getUserFriendlyExceptionMessage with an exception where the message starts with 401:
        String friendlyMessage = consoleUI.getUserFriendlyExceptionMessage(new Exception(message));

        // THEN: the friendly message should be returned
        assertEquals("Authentication failed. The agent controller rejected the master controller's password.", friendlyMessage);
    }

    /**
     * Test for method {@link BasicConsoleUI#getUserFriendlyExceptionMessage(Exception)} which should return the plain
     * exception message if it is not one of the special messages
     */
    @Test
    public final void getUserFriendlyExceptionMessage_exceptionMessageIsNotSpecial()
    {
        // TEST_PARAMETER:
        final String message = "foo bar";

        // GIVEN: a fresh default BasicConsoleUI instance
        BasicConsoleUI consoleUI = createNewTestObject();

        // WHEN: calling getUserFriendlyExceptionMessage with an exception where the message is no special text
        String friendlyMessage = consoleUI.getUserFriendlyExceptionMessage(new Exception(message));

        // THEN: the friendly message should be the same as the plain exception message
        assertEquals(message, friendlyMessage);
    }

    @Test
    public final void testPrintLoadTestSettingsNullProfile()
    {
        BasicConsoleUI consoleUI = createNewTestObject();
        String output = consoleUI.getLoadTestSettings(null);
        assertTrue(output != null);
    }

    @Test
    public final void testPrintLoadTestSettingsOneEasyTestCase()
    {
        BasicConsoleUI consoleUI = createNewTestObject();

        final int[][] users1 =
            {
                {
                    0, 1
                }
            };
        final int[][] arrivalRate60 =
            {
                {
                    0, 60
                }
            };
        final int[][] loadFactor100p =
            {
                {
                    0, 1000
                }
            };

        final String dash = StringUtils.repeat("-", 79);
        String expectedOutput = dash +
                                String.format("\nTest Case | Arrival Rate [eff] | Users [eff] | Load Factor | Measurement Period\n" + dash +
                                              "\nTVisit    |                 60 |           1 |       %,.3f |            1:00:00\n" + dash +
                                              "\n                            60 |           1 |       %,.3f |            1:00:00\n", 1.0,
                                              1.0);

        // First load test profile: 1 user places every minute an order (for one hour)
        TestCaseLoadProfileConfiguration testCaseLoadProfile1 = createTestCaseLoadProfile("TVisit", arrivalRate60, users1, loadFactor100p,
                                                                                          3600);

        final TestLoadProfileConfiguration testLoadProfile = new TestLoadProfileConfiguration();
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile1);

        String output = consoleUI.getLoadTestSettings(testLoadProfile);
        assertEquals(expectedOutput, output);
    }

    @Test
    public final void testPrintLoadTestSettingsSameTestTwice()
    {
        BasicConsoleUI consoleUI = createNewTestObject();

        final int[][] users1 =
            {
                {
                    0, 1
                }
            };
        final int[][] arrivalRate60 =
            {
                {
                    0, 60
                }
            };
        final int[][] loadFactor100p =
            {
                {
                    0, 1000
                }
            };

        final String dash = StringUtils.repeat("-", 79);
        String expectedOutput = dash +
                                String.format("\nTest Case | Arrival Rate [eff] | Users [eff] | Load Factor | Measurement Period\n" + dash +
                                              "\nTestCase1 |                 60 |           1 |       %,.3f |            1:00:00" +
                                              "\nTestCase2 |                 60 |           1 |       %,.3f |            1:00:00\n" + dash +
                                              "\n                           120 |           2 |       %,.3f |            1:00:00\n", 1.0,
                                              1.0, 1.0);

        TestCaseLoadProfileConfiguration testCaseLoadProfile1 = createTestCaseLoadProfile("TestCase1", arrivalRate60, users1,
                                                                                          loadFactor100p, 3600);
        TestCaseLoadProfileConfiguration testCaseLoadProfile2 = createTestCaseLoadProfile("TestCase2", arrivalRate60, users1,
                                                                                          loadFactor100p, 3600);

        final TestLoadProfileConfiguration testLoadProfile = new TestLoadProfileConfiguration();
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile1);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile2);

        String output = consoleUI.getLoadTestSettings(testLoadProfile);
        assertEquals(expectedOutput, output);
    }

    @Test
    public final void testPrintLoadTestSettingsDifferentTestCases()
    {
        BasicConsoleUI consoleUI = createNewTestObject();

        final int[][] users1 =
            {
                {
                    0, 1
                }
            };
        final int[][] users50 =
            {
                {
                    0, 50
                }
            };
        final int[][] users94 =
            {
                {
                    0, 94
                }
            };
        final int[][] users580 =
            {
                {
                    0, 580
                }
            };
        final int[][] users1234 =
            {
                {
                    0, 1234
                }
            };
        final int[][] userSet =
            {
                {
                    0, 85
                },
                {
                    1, 12
                },
                {
                    5, 11
                },
                {
                    2, 1
                },
                {
                    123, 565701326
                },
                {
                    9853, 132
                }
            };

        final int[][] arrivalRate1 =
            {
                {
                    0, 1
                }
            };
        final int[][] arrivalRate60 =
            {
                {
                    0, 60
                }
            };
        final int[][] arrivalRate120 =
            {
                {
                    0, 120
                }
            };
        final int[][] arrivalRate3580 =
            {
                {
                    0, 3580
                }
            };
        final int[][] arrivalRateSet =
            {
                {
                    0, 123
                },
                {
                    10, 150
                },
                {
                    60, 2350
                }
            };

        final int[][] loadFactor100p =
            {
                {
                    0, 1000
                }
            };
        final int[][] loadFactor50p =
            {
                {
                    0, 500
                }
            };
        final int[][] loadFactor25p =
            {
                {
                    0, 250
                }
            };
        final int[][] loadFactor1850p =
            {
                {
                    0, 1850
                }
            };
        final int[][] loadFactorSet =
            {
                {
                    0, 150
                },
                {
                    10, 8500
                },
                {
                    9, 90000
                }
            };

        final String dash = StringUtils.repeat("-", 94);

        String expectedOutput = dash +
                                String.format("\n     Test Case      | Arrival Rate [eff] |  Users [eff]   |  Load Factor  | Measurement Period\n" +
                                              dash +
                                              "\nA                   |                n/a | 1..%,d |           n/a |            0:59:18" +
                                              "\nExtra-Long-TestCase |                n/a |          %,d |         %,.3f |            0:00:00" +
                                              "\nScenario Name       |         123..%,d | 1..%,d | %,.3f..%,.3f |           12:36:52" +
                                              "\nTest                |              %,d |            580 |         %,.3f |          129:12:12" +
                                              "\nTestCase1           |                  1 |              1 |         %,.3f |            1:00:00" +
                                              "\nTestCase2           |                 60 |             50 |         %,.3f |            1:01:01" +
                                              "\nTestCase3           |                120 |             94 |         %,.3f |            0:20:34" +
                                              "\nTestCase4           |                120 |             94 |         %,.3f |            1:59:59" +
                                              "\nTesting             |         123..%,d | 1..%,d |           n/a |           12:36:52\n" +
                                              dash +
                                              "\n                                   %,d |  %,d |               |          129:12:12\n",
                                              565701326, 1234, 1.85, 2350, 565701326, 0.15, 90.0, 3580, 1.85, 1.00, 0.50, 0.25, 0.25, 2350,
                                              565701326, 8581, 1697106031);

        TestCaseLoadProfileConfiguration testCaseLoadProfile1 = createTestCaseLoadProfile("TestCase1", arrivalRate1, users1, loadFactor100p,
                                                                                          3600);
        TestCaseLoadProfileConfiguration testCaseLoadProfile2 = createTestCaseLoadProfile("TestCase2", arrivalRate60, users50,
                                                                                          loadFactor50p, 3661);
        TestCaseLoadProfileConfiguration testCaseLoadProfile3 = createTestCaseLoadProfile("TestCase3", arrivalRate120, users94,
                                                                                          loadFactor25p, 1234);
        TestCaseLoadProfileConfiguration testCaseLoadProfile4 = createTestCaseLoadProfile("TestCase4", arrivalRate120, users94,
                                                                                          loadFactor25p, 7199);
        TestCaseLoadProfileConfiguration testCaseLoadProfile5 = createTestCaseLoadProfile("Test", arrivalRate3580, users580,
                                                                                          loadFactor1850p, 465132);
        TestCaseLoadProfileConfiguration testCaseLoadProfile6 = createTestCaseLoadProfile("Extra-Long-TestCase", null, users1234,
                                                                                          loadFactor1850p, 0);
        TestCaseLoadProfileConfiguration testCaseLoadProfile7 = createTestCaseLoadProfile("A", null, userSet, null, 3558);
        TestCaseLoadProfileConfiguration testCaseLoadProfile8 = createTestCaseLoadProfile("Testing", arrivalRateSet, userSet, null, 45412);
        TestCaseLoadProfileConfiguration testCaseLoadProfile9 = createTestCaseLoadProfile("Scenario Name", arrivalRateSet, userSet,
                                                                                          loadFactorSet, 45412);

        final TestLoadProfileConfiguration testLoadProfile = new TestLoadProfileConfiguration();
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile1);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile2);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile3);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile4);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile5);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile6);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile7);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile8);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile9);

        String output = consoleUI.getLoadTestSettings(testLoadProfile);
        // System.out.println(output);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testVeryLargeLoadProfiles()
    {
        BasicConsoleUI consoleUI = createNewTestObject();

        final int[][] users1 =
            {
                {
                    0, 1
                }
            };
        final int[][] maxUserSet =
            {
                {
                    0, Integer.MAX_VALUE
                },
                {
                    1, Integer.MAX_VALUE
                }
            };

        final int[][] arrivalRate1 =
            {
                {
                    0, 1
                }
            };
        final int[][] maxArrivalRate =
            {
                {
                    0, Integer.MAX_VALUE
                },
                {
                    1, Integer.MAX_VALUE
                }
            };

        final int[][] loadFactor100p =
            {
                {
                    0, 1000
                }
            };
        final int[][] maxLoadFactor =
            {
                {
                    0, Integer.MAX_VALUE
                },
                {
                    1, Integer.MAX_VALUE
                }
            };

        int[][] maxRange =
            {
                {
                    0, Integer.MAX_VALUE - 1
                },
                {
                    1, Integer.MAX_VALUE
                }
            };

        final String dash = StringUtils.repeat("-", 125);

        String expectedOutput = dash +
                                String.format("\n Test Case  |      Arrival Rate [eff]      |         Users [eff]          |         Load Factor          | Measurement Period\n" +
                                              dash +
                                              "\nMax.Arrival |                %,d |                            1 |                        %,.3f |            3:25:54" +
                                              "\nMax.Time    |                            1 |                            1 |                        %,.3f |       596523:14:07" +
                                              "\nZ           |                %,d |                %,d |                %,.3f |       596523:14:07" +
                                              "\nmax-range   | %,d..%,d | %,d..%,d | %,.3f..%,.3f |       596523:14:07" +
                                              "\nnull        |                          n/a |                          n/a |                          n/a |            0:00:00\n" +
                                              dash +
                                              "\n                            #OUT OF RANGE# |               #OUT OF RANGE# |                              |       596523:14:07\n",
                                              Integer.MAX_VALUE, 1.0, 1.0, Integer.MAX_VALUE, Integer.MAX_VALUE,
                                              (double) Integer.MAX_VALUE / 1000.0, Integer.MAX_VALUE - 1, Integer.MAX_VALUE,
                                              Integer.MAX_VALUE - 1, Integer.MAX_VALUE, ((double) Integer.MAX_VALUE - 1) / 1000.0,
                                              (double) Integer.MAX_VALUE / 1000.0);

        // MAX. Values
        TestCaseLoadProfileConfiguration testCaseLoadProfile1 = createTestCaseLoadProfile("Max.Time", arrivalRate1, users1, loadFactor100p,
                                                                                          Integer.MAX_VALUE);
        TestCaseLoadProfileConfiguration testCaseLoadProfile2 = createTestCaseLoadProfile("Max.Arrival", maxArrivalRate, users1,
                                                                                          loadFactor100p, 12354);
        TestCaseLoadProfileConfiguration testCaseLoadProfile3 = createTestCaseLoadProfile("Z", maxArrivalRate, maxUserSet, maxLoadFactor,
                                                                                          Integer.MAX_VALUE);
        TestCaseLoadProfileConfiguration testCaseLoadProfile4 = createTestCaseLoadProfile("null", null, null, null, 0);
        TestCaseLoadProfileConfiguration testCaseLoadProfile5 = createTestCaseLoadProfile("max-range", maxRange, maxRange, maxRange,
                                                                                          Integer.MAX_VALUE);

        final TestLoadProfileConfiguration testLoadProfile = new TestLoadProfileConfiguration();

        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile1);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile2);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile3);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile4);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile5);

        String output = consoleUI.getLoadTestSettings(testLoadProfile);
        assertEquals(expectedOutput, output);
    }

    private TestCaseLoadProfileConfiguration createTestCaseLoadProfile(String userName, int[][] arrivalRate, int[][] numberOfUsers,
                                                                       int[][] loadFactor, int measurementPeriod)
    {
        final TestCaseLoadProfileConfiguration testCaseLoadProfile = new TestCaseLoadProfileConfiguration();
        testCaseLoadProfile.setUserName(userName);
        testCaseLoadProfile.setArrivalRate(arrivalRate);
        testCaseLoadProfile.setNumberOfUsers(numberOfUsers);
        testCaseLoadProfile.setLoadFactor(loadFactor);
        testCaseLoadProfile.setMeasurementPeriod(measurementPeriod);

        return testCaseLoadProfile;
    }

}
