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

import java.util.ArrayList;

/**
 * Utility class for parsing and calculating load functions.
 */
public final class LoadFunctionUtils
{
    /**
     * An hour has 3600 seconds.
     */
    private static final long SECONDS_PER_HOUR = 3600;

    /**
     * A minute has 60 seconds.
     */
    private static final long SECONDS_PER_MINUTE = 60;

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private LoadFunctionUtils()
    {
    }

    /**
     * Determines whether or not the passed time/value pairs define a complex load function.
     * 
     * @param timeValuePairs
     *            the load function points
     * @return whether or not the load function is complex
     */
    public static boolean isComplexLoadFunction(final int[][] timeValuePairs)
    {
        return !isSimpleLoadFunction(timeValuePairs);
    }

    /**
     * Determines whether or not the passed time/value pairs define a simple load function. A load function is simple if
     * it has only the trivial pair (0,v).
     * 
     * @param timeValuePairs
     *            the load function points
     * @return whether or not the load function is simple
     */
    public static boolean isSimpleLoadFunction(final int[][] timeValuePairs)
    {
        return timeValuePairs.length == 1 && timeValuePairs[0][0] == 0;
    }

    /**
     * Checks that the passed time/value pairs define a valid load function. A load function is valid if all its (ti,vi)
     * pairs are sorted by ti in ascending order, and neither ti, nor vi is negative.
     * 
     * @param timeValuePairs
     *            the load function points
     * @throws IllegalArgumentException
     *             if the load function is not valid
     */
    public static void checkLoadFunction(final int[][] timeValuePairs) throws IllegalArgumentException
    {
        if (timeValuePairs.length == 0)
        {
            throw new IllegalArgumentException("The function does not specify any time/value pairs.");
        }

        int lastT = 0;
        for (int i = 0; i < timeValuePairs.length; i++)
        {
            final int t = timeValuePairs[i][0];
            final int v = timeValuePairs[i][1];

            if (t < 0 || v < 0)
            {
                throw new IllegalArgumentException("Either time or value is negative.");
            }

            if (t < lastT)
            {
                throw new IllegalArgumentException("The time/value pairs must be sorted in ascending order.");
            }
            else
            {
                lastT = t;
            }
        }
    }

    /**
     * Completes a potentially incomplete load function. A load function is incomplete if its first time/value pair has
     * a time greater than 0. In this case, the load function is completed by adding an additional pair 0/1 at the
     * beginning.
     * 
     * @param timeValuePairs
     *            the load function
     * @return the completed load function
     */
    public static int[][] completeLoadFunctionIfNecessary(final int[][] timeValuePairs)
    {
        final int firstTime = timeValuePairs[0][0];
        if (firstTime > 0)
        {
            // add an additional pair 0/1 at the beginning
            final int[][] completedTimeValuePairs = new int[timeValuePairs.length + 1][];

            completedTimeValuePairs[0] = new int[]
                {
                    0, 1
                };
            System.arraycopy(timeValuePairs, 0, completedTimeValuePairs, 1, timeValuePairs.length);

            return completedTimeValuePairs;
        }
        else
        {
            // the load function is complete -> leave it untouched
            return timeValuePairs;
        }
    }

    /**
     * Computes a load function from the given ramp-up parameters. Pass -1 if a certain parameter is not known. But note
     * that there are two restrictions:
     * <ol>
     * <li>The parameter "rampUpTargetValue" is required (must not be -1).</li>
     * <li>You may not use "rampUpPeriod" and "rampUpSteadyPeriod" at the same time.</li>
     * </ol>
     * All other parameters have reasonable defaults, and the method tries to do the right thing in case of
     * contradicting parameters.
     * 
     * @param rampUpInitialValue
     *            the initial value to start with at the beginning of the ramp-up period
     * @param rampUpTargetValue
     *            the value to reach at the end of the ramp-up period
     * @param rampUpPeriod
     *            the length of the ramp-up period in seconds
     * @param rampUpStepSize
     *            the value to add at a ramp-up step
     * @param rampUpSteadyPeriod
     *            the time period in seconds between two ramp-up steps
     * @return an array of arrays with the timestamp at index 0 and the value at index 1
     */
    public static int[][] computeLoadFunction(final int rampUpInitialValue, final int rampUpTargetValue, final int rampUpPeriod,
                                              final int rampUpStepSize, final int rampUpSteadyPeriod)
    {
        // System.out.printf("Passed values -> initial: %d target: %d period: %d steadyPeriod: %d stepSize: %d\n",
        // rampUpInitialValue, rampUpTargetValue, rampUpPeriod, rampUpSteadyPeriod, rampUpStepSize);

        // checking basic parameters
        if (rampUpTargetValue < 0)
        {
            throw new IllegalArgumentException("Ramp-up target value is not specified.");
        }

        if (rampUpPeriod >= 0 && rampUpSteadyPeriod >= 0)
        {
            throw new IllegalArgumentException("Both ramp-up period and ramp-up steady period are given, but they are mutually exclusive.");
        }

        // set/calculate defaults if values are missing or not plausible
        final int targetValue = rampUpTargetValue;
        final int stepSize = (rampUpStepSize <= 0) ? 1 : rampUpStepSize;
        final int initialValue = (rampUpInitialValue < 0) ? stepSize : rampUpInitialValue;

        final int steps;
        if (initialValue < targetValue)
        {
            steps = (int) Math.ceil((targetValue - initialValue) / (double) stepSize);
        }
        else
        {
            steps = 0;
        }

        final int period;
        final double steadyPeriod;
        if (rampUpPeriod <= 0 && rampUpSteadyPeriod <= 0 || steps == 0)
        {
            // neither period nor steady period is given or no steps defined
            period = 0;
            steadyPeriod = 0;
        }
        else if (rampUpPeriod > 0)
        {
            // period is given -> derive the steady period from it
            period = rampUpPeriod;
            steadyPeriod = (double) period / steps;
        }
        else
        {
            // steady period is given -> derive the period from it
            period = rampUpSteadyPeriod * steps;
            steadyPeriod = rampUpSteadyPeriod;
        }

        // System.out.printf("Used values -> initial: %d target: %d period: %d steadyPeriod: %g steps: %d stepSize:
        // %d\n",
        // initialValue, targetValue, period, steadyPeriod, steps, stepSize);

        /*
         * Build the function.
         */
        if (period == 0)
        {
            return new int[][]
                {
                    {
                        0, targetValue
                    }
                };
        }
        else if (stepSize == 1)
        {
            return new int[][]
                {
                    {
                        0, initialValue
                    },
                    {
                        period, targetValue
                    }
                };
        }
        else
        {
            final ArrayList<int[]> loadFunction = new ArrayList<int[]>();

            // add the initial pair
            loadFunction.add(new int[]
                {
                    0, initialValue
                });

            // add the remaining pairs
            int lastValue = initialValue;
            for (int elapsedTime = 1; elapsedTime <= period; elapsedTime++)
            {
                final int numSteadyPeriods = (int) (elapsedTime / steadyPeriod);
                final int value = Math.min(targetValue, initialValue + numSteadyPeriods * stepSize);

                // check if the value has increased
                if (value > lastValue)
                {
                    // add a pair with the old value
                    loadFunction.add(new int[]
                        {
                            elapsedTime, lastValue
                        });

                    // add another pair with the new value
                    loadFunction.add(new int[]
                        {
                            elapsedTime, value
                        });

                    // remember the new value
                    lastValue = value;
                }
            }

            return loadFunction.toArray(new int[loadFunction.size()][2]);
        }
    }

    /**
     * Generates a string representation of the argument load function. To remain close to earlier representations the
     * returned string is just the string representation of the load parameter value if the argument load function
     * contains only one pair (which means that we have a constant load parameter value during the whole test
     * execution). Otherwise the returned string consists of pair separated by spaces and each pair being given by a
     * duration and the corresponding load parameter value separated by a slash. To increase readability the duration
     * values are always suffixed where &quot;s&quot; stands for seconds, &quot;m&quot; stands for minutes and
     * &quot;h&quot; stands for hours.
     * 
     * @param loadFunction
     *            the load function as a two dimensional array where the second dimension contains the timestamp/load
     *            parameter value pairs
     * @return the string representation of the argument load function reformatted/simplified to achieve maximum
     *         readability
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     * @throws ArrayIndexOutOfBoundsException
     *             if there is any pair (array in the second dimension) with a length less than two
     */
    public static String loadFunctionToString(final int[][] loadFunction)
    {
        if (loadFunction.length == 0)
        {
            return "";
        }
        else if (loadFunction.length == 1 && loadFunction[0][0] == 0)
        {
            return String.valueOf(loadFunction[0][1]);
        }
        else
        {
            return multiPairLoadFunctionToString(loadFunction);
        }
    }

    private static String multiPairLoadFunctionToString(final int[][] loadFunction)
    {
        final StringBuilder returnValue = new StringBuilder();

        for (int i = 0; i < loadFunction.length; i++)
        {
            final int[] pair = loadFunction[i];
            int timeInSeconds = pair[0];
            final int value = pair[1];

            if (timeInSeconds == 0)
            {
                returnValue.append("0s");
            }
            else
            {
                if (timeInSeconds >= SECONDS_PER_HOUR)
                {
                    returnValue.append(timeInSeconds / SECONDS_PER_HOUR).append('h');
                    timeInSeconds %= SECONDS_PER_HOUR;
                }

                if (timeInSeconds >= SECONDS_PER_MINUTE)
                {
                    returnValue.append(timeInSeconds / SECONDS_PER_MINUTE).append('m');
                    timeInSeconds %= SECONDS_PER_MINUTE;
                }

                if (timeInSeconds > 0)
                {
                    returnValue.append(timeInSeconds).append('s');
                }
            }

            returnValue.append('/').append(value);

            if (i < loadFunction.length - 1)
            {
                returnValue.append(' ');
            }
        }

        return returnValue.toString();
    }

    /**
     * Scales the given load function by the passed load factor function. Note that scaling is currently not supported
     * if both functions are complex.
     * <p>
     * The load factor function is expected to be given as two-dimensional array of non-negative integral values whereas
     * the 1st dimension specifies the time offset in seconds and the 2nd dimension specifies the load factor in per mil
     * at the given time offset.
     * 
     * @param loadFunction
     *            the load function (may be <code>null</code>)
     * @param loadFactorFunction
     *            the load factor function (may be <code>null</code>)
     * @return the scaled load function
     */
    public static int[][] scaleLoadFunction(final int[][] loadFunction, final int[][] loadFactorFunction)
    {
        final int[][] result;

        if (loadFunction == null || loadFactorFunction == null)
        {
            result = loadFunction;
        }
        else if (loadFunction.length == 0 || loadFactorFunction.length == 0)
        {
            result = loadFunction;
        }
        else if (isComplexLoadFunction(loadFunction) && isComplexLoadFunction(loadFactorFunction))
        {
            result = loadFunction;
        }
        else
        {
            final int[][] function;
            final int permill;

            if (isSimpleLoadFunction(loadFactorFunction))
            {
                function = loadFunction;
                permill = loadFactorFunction[0][1];
            }
            else
            {
                function = loadFactorFunction;
                permill = loadFunction[0][1];
            }

            result = new int[function.length][];
            for (int i = 0; i < result.length; i++)
            {
                int time = function[i][0];
                int value = function[i][1];

                value = (int) Math.ceil(value * (permill / 1000.0));

                result[i] = new int[]
                    {
                        time, value
                    };
            }
        }

        return result;
    }
}
