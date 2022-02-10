/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.SessionImpl;

/**
 * Testcases to prove the correct functionality of {@link XltRandom}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class XltRandomTest
{

    /**
     * Tests the implementation of {@link XltRandom#getRandom(int[])} by passing an invalid parameter.
     */
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetRandom_InvalidParam_Null()
    {
        XltRandom.getRandom(null);
    }

    /**
     * Tests the implementation of {@link XltRandom#getRandom(int[])} by passing an invalid parameter.
     */
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetRandom_InvalidParam_array0()
    {
        XltRandom.getRandom(new int[0]);
    }

    /**
     * Tests the implementation of {@link XltRandom#getRandom(int[])} by passing a valid parameter.
     */
    @Test
    public void testGetRandom()
    {
        final int[] testData = new int[]
            {
                1, 3, 2, 8
            };

        final int i = XltRandom.getRandom(testData);
        if (i > 4)
        {
            Assert.assertEquals(8, i);
        }
        else
        {
            Assert.assertEquals(1, Math.min(1, i));
            Assert.assertEquals(3, Math.max(3, i));
        }
    }

    /**
     * Tests the implementation of {@link XltRandom#nextInt(int)}.
     */
    @Test
    public void testNextInt_Int()
    {
        Assert.assertEquals(0, XltRandom.nextInt(0));

        final int i = XltRandom.nextInt(100);

        Assert.assertEquals(0, Math.min(0, i));
        Assert.assertEquals(99, Math.max(99, i));
    }

    /**
     * Tests the implementation of {@link XltRandom#nextIntWithDeviation(int, int)}.
     */
    @Test
    public void testNextIntWithDeviation()
    {
        Assert.assertEquals(10, XltRandom.nextIntWithDeviation(10, 0));
        assertInRange(XltRandom.nextIntWithDeviation(10, 1), 9, 11);
        assertInRange(XltRandom.nextIntWithDeviation(10, 5), 5, 15);
        assertInRange(XltRandom.nextIntWithDeviation(10, -5), 5, 15);
        assertInRange(XltRandom.nextIntWithDeviation(-10, 5), -15, -5);
        assertInRange(XltRandom.nextIntWithDeviation(-10, -5), -15, -5);
    }

    /**
     * Tests the implementation of {@link XltRandom#nextInt(int, int)}.
     */
    @Test
    public void testNextInt()
    {
        Assert.assertEquals(10, XltRandom.nextInt(10, 10));
        assertInRange(XltRandom.nextInt(10, 11), 10, 11);
        assertInRange(XltRandom.nextInt(10, 20), 10, 20);
        assertInRange(XltRandom.nextInt(-20, -10), -20, -10);
        assertInRange(XltRandom.nextInt(-10, 10), -10, 10);
    }

    /**
     * Tests the implementation of {@link XltRandom#nextInt(int, int)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNextInt_MinGreaterMax()
    {
        XltRandom.nextInt(12, 10);
    }

    /**
     * Tests the implementation of {@link XltRandom#nextInt(int, int)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNextInt_DifferenceTooBig()
    {
        XltRandom.nextInt(Integer.MIN_VALUE, 1);
    }

    /**
     * Tests simple methods just to increase code coverage
     */
    @Test
    public void testSimpleCalls()
    {
        XltRandom.nextBoolean(50);
        XltRandom.nextDouble();
        XltRandom.nextFloat();
        XltRandom.nextGaussian();
        XltRandom.nextLong();
        Assert.assertNotNull("Internal random should not be null!", XltRandom.getRandom());
    }

    /**
     * Tests the implementation of {@link XltRandom#nextBoolean(int)}.
     */
    @Test
    public void testGetNextBoolean_Int()
    {
        Assert.assertFalse(XltRandom.nextBoolean(-1));
        Assert.assertFalse(XltRandom.nextBoolean(0));

        Assert.assertTrue(XltRandom.nextBoolean(100));
        Assert.assertTrue(XltRandom.nextBoolean(101));

        // Note: This may fail now and then, even if the code works correctly.
        //
        // int hits = 0;
        // for (int i = 0; i < 10; i++)
        // {
        // if (XltRandom.nextBoolean(90))
        // {
        // hits++;
        // }
        // }
        //
        // Assert.assertTrue(hits > 5);
    }

    private void assertInRange(final int v, final int min, final int max)
    {
        Assert.assertTrue("Value " + v + "is not in range [" + min + "," + max + "]" + v, min <= v && v <= max);
    }

    /**
     * Helper to create random numbers from a generator
     * 
     * @param random
     *            the random generator to use
     * @count the amount of numbers
     * @return an array of random integers
     */
    private int[] createRandomIntegers(final Random random, final int count)
    {
        final int[] randomData = new int[count];
        for (int i = 0; i < randomData.length; i++)
        {
            randomData[i] = random.nextInt();
        }
        return randomData;
    }

    /**
     * Test that the seed is correctly applied. Same seed, different threads.
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test(expected = AssertionError.class)
    public void testCorrectUsageOfSeed() throws InterruptedException, ExecutionException
    {
        final int testCount = 100;

        XltProperties.getInstance().setProperty(XltConstants.RANDOM_INIT_VALUE_PROPERTY, "42");
        final List<Random> randoms = testThreadedRandomGet(2, "Foo", true);

        Assert.assertArrayEquals(createRandomIntegers(randoms.get(0), testCount), createRandomIntegers(randoms.get(1), testCount));
    }

    /**
     * Test that the seed is correctly applied and creates the same random when the thread name is identical. Same
     * thread name, same seed.
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testCorrectUsageOfSeed_SameSeed() throws InterruptedException, ExecutionException
    {
        final int testCount = 100;

        XltProperties.getInstance().setProperty(XltConstants.RANDOM_INIT_VALUE_PROPERTY, "420");
        final List<Random> randoms1 = testThreadedRandomGet(1, "Fixed", true);
        final List<Random> randoms2 = testThreadedRandomGet(1, "Fixed", true);

        Assert.assertArrayEquals(createRandomIntegers(randoms1.get(0), testCount), createRandomIntegers(randoms2.get(0), testCount));
    }

    /**
     * Test that a new seed creates a different random set even with the same thread name.
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test(expected = AssertionError.class)
    public void testCorrectUsageOfSeed_DifferentSeed() throws InterruptedException, ExecutionException
    {
        final int testCount = 100;

        XltProperties.getInstance().setProperty(XltConstants.RANDOM_INIT_VALUE_PROPERTY, "9876");
        final List<Random> randoms1 = testThreadedRandomGet(1, "Fixed", true);

        XltProperties.getInstance().setProperty(XltConstants.RANDOM_INIT_VALUE_PROPERTY, "1121");
        final List<Random> randoms2 = testThreadedRandomGet(1, "Fixed", true);

        Assert.assertArrayEquals(createRandomIntegers(randoms1.get(0), testCount), createRandomIntegers(randoms2.get(0), testCount));
    }

    /**
     * Test that the random generator is started with time millis when seed is not set
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test(expected = AssertionError.class)
    public void testCorrectUsageOfSeed_NoSeedSet() throws InterruptedException, ExecutionException
    {
        final int testCount = 100;

        XltProperties.getInstance().setProperty(XltConstants.RANDOM_INIT_VALUE_PROPERTY, "");

        final List<Random> randoms1 = testThreadedRandomGet(1, "Fixed", true);

        // wait a moment to avoid identical time millis for init of generator
        Thread.sleep(100);

        final List<Random> randoms2 = testThreadedRandomGet(1, "Fixed", true);

        Assert.assertArrayEquals(createRandomIntegers(randoms1.get(0), testCount), createRandomIntegers(randoms2.get(0), testCount));
    }

    /**
     * Helper for random numbers per thread
     * 
     * @param threadCount
     *            the number of threads to use
     * @param threadNamePrefix
     *            the name prefix for the threads
     * @param fixedNames
     *            gives the threads fixed name to have repeatable executions, when false it leaves it to the system to
     *            handle it
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private List<Random> testThreadedRandomGet(final int threadCount, final String threadNamePrefix, final boolean fixedNames)
        throws InterruptedException, ExecutionException
    {
        final Callable<Random> task = new Callable<Random>()
        {
            @Override
            public Random call()
            {
                SessionImpl.getCurrent().setLoadTest(true);

                return XltRandom.getRandom();
            }
        };

        class NamedThreadFactory implements ThreadFactory
        {
            final AtomicInteger counter = new AtomicInteger(0);

            final ThreadGroup threadGroup = new ThreadGroup("NamedThreadFactory");

            @Override
            public Thread newThread(final Runnable r)
            {
                // put the thread in an own thread group to separate its session from the main thread's session
                final Thread thread = new Thread(threadGroup, r);

                if (fixedNames)
                {
                    thread.setName(threadNamePrefix + counter.getAndIncrement());
                }

                return thread;
            }
        }

        final List<Callable<Random>> tasks = Collections.nCopies(threadCount, task);
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount, new NamedThreadFactory());
        final List<Future<Random>> futures = executorService.invokeAll(tasks);

        // Get data
        final List<Random> resultList = new ArrayList<Random>(futures.size());
        for (final Future<Random> future : futures)
        {
            // Throws an exception if an exception was thrown by the task.
            resultList.add(future.get());
        }

        return resultList;
    }

    /**
     * Test that the seed value that was set programmatically is correctly returned.
     */
    @Test
    public void testGetSeed()
    {
        final long seed = 1234567890;

        XltRandom.setSeed(seed);

        Assert.assertEquals("Unexpected seed value returned:", seed, XltRandom.getSeed());
    }

    /**
     * Test that setting the seed programmatically causes the random generator to indeed produce the same sequence of
     * numbers.
     */
    @Test
    public void testSetSeed()
    {
        final long seed = 1234567890;

        // create the 1st sequence
        XltRandom.setSeed(seed);

        int[] expectedValues = new int[100];
        for (int i = 0; i < expectedValues.length; i++)
        {
            expectedValues[i] = XltRandom.nextInt();
        }

        // create the 2nd sequence
        XltRandom.setSeed(seed);

        int[] actualValues = new int[expectedValues.length];
        for (int i = 0; i < actualValues.length; i++)
        {
            actualValues[i] = XltRandom.nextInt();
        }

        // test
        Assert.assertArrayEquals("Unexpected value encountered", expectedValues, actualValues);
    }
}
