/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.common.util;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.common.lang.ThreadUtils;

import it.unimi.dsi.util.FastRandom;

/**
 * This test is more a prove that it mostly works that a real 100% for sure test. Did not want to mock.
 * 
 * @author rschwietzke
 */
public class SynchronizingCounterTest
{
    
    
    class Waiter implements Runnable
    {
        private final SynchronizingCounter counter;

        private int waitFor = 0;

        public Waiter(final SynchronizingCounter counter)
        {
            this.counter = counter;
        }

        public Waiter(final SynchronizingCounter counter, final int waitFor)
        {
            this.counter = counter;
            this.waitFor = waitFor;
        }

        @Override
        public void run()
        {
            try
            {
                if (waitFor == 0)
                {
                    counter.awaitZero();
                }
                else
                {
                    counter.awaitZero(waitFor);
                }
            }
            catch (final InterruptedException e)
            {
                Assert.fail("Thread stopped before 0");
            }
        }
    }

    class Counter implements Runnable
    {
        private final FastRandom r = FastRandom.get(101L);
        private final SynchronizingCounter counter;
        private final int from;
        private final int to;
        private final int steps;

        public Counter(final SynchronizingCounter counter, final int from, final int to, final int steps)
        {
            this.counter = counter;
            this.from = from;
            this.to = to;
            this.steps = steps;
        }

        @Override
        public void run()
        {
            for (int i = from; i <= to; i++)
            {
                if (steps == 1)
                {
                    counter.increment();
                }
                else if (steps == -1)
                {
                    counter.decrement();
                }
                else
                {
                    counter.add(steps);
                }
                ThreadUtils.sleep(r.nextInt(10) + 1);
            }
        }
    }

    /**
     * Test method for {@link com.xceptance.common.util.SynchronizingCounter#awaitZero()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public final void testAwaitZero_MultipleThreads() throws InterruptedException
    {
        final FastRandom r = FastRandom.get(102L);
        
        final SynchronizingCounter counter = new SynchronizingCounter(1000);
        final Thread t1 = new Thread(new Waiter(counter));
        t1.start();

        // wait for thread start
        Thread.sleep(r.nextInt(500, 1000));

        // check state
        Assert.assertEquals(Thread.State.WAITING, t1.getState());

        // ok, get some counter threads moving
        final Thread c1 = new Thread(new Counter(counter, 1, 100, 1));
        c1.start();
        final Thread c2 = new Thread(new Counter(counter, 1, 100, -1));
        c2.start();
        final Thread c3 = new Thread(new Counter(counter, 1, 50, 2));
        c3.start();
        final Thread c4 = new Thread(new Counter(counter, 1, 50, -3));
        c4.start();

        Assert.assertTrue(0 != counter.get());

        // wait for the counter threads
        c1.join();
        c2.join();
        c3.join();
        c4.join();

        Assert.assertTrue(0 != counter.get());

        // end the test
        counter.set(0);

        // wait for him
        t1.join(1000);

        // check state
        Assert.assertEquals(Thread.State.TERMINATED, t1.getState());
    }

    /**
     * Test method for {@link com.xceptance.common.util.SynchronizingCounter#awaitZero()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public final void testAwaitZero_CountedTo0() throws InterruptedException
    {
        final FastRandom r = FastRandom.get(132L);
        final SynchronizingCounter counter = new SynchronizingCounter(100);
        final Thread t1 = new Thread(new Waiter(counter));
        t1.start();

        // wait for thread start
        Thread.sleep(r.nextInt(500, 1000));

        // check state
        Assert.assertEquals(Thread.State.WAITING, t1.getState());

        // ok, get some counter threads moving, this will take is to 0 by definition somehow
        final Thread c1 = new Thread(new Counter(counter, 1, 100, 1));
        c1.start();
        final Thread c2 = new Thread(new Counter(counter, 1, 100, -1));
        c2.start();
        final Thread c3 = new Thread(new Counter(counter, 1, 100, -2));
        c3.start();
        final Thread c4 = new Thread(new Counter(counter, 1, 100, 2));
        c4.start();
        final Thread c5 = new Thread(new Counter(counter, 1, 100, -1));
        c5.start();

        // wait for the counter threads
        c1.join();
        c2.join();
        c3.join();
        c4.join();
        c5.join();

        Assert.assertTrue(0 == counter.get());

        // wait for him
        t1.join(1000);

        // check state
        Assert.assertEquals(Thread.State.TERMINATED, t1.getState());
    }

    /**
     * Test method for {@link com.xceptance.common.util.SynchronizingCounter#awaitZero()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public final void testAwaitZero_Start0Set0() throws InterruptedException
    {
        final SynchronizingCounter counter = new SynchronizingCounter();

        // ok, get some counter threads moving, this will take is to 0 by definition somehow
        final Thread c1 = new Thread(new Counter(counter, 1, 100, 1));
        c1.start();

        while (counter.get() == 0)
        {
            // noop
        }
        final Thread t1 = new Thread(new Waiter(counter));
        t1.start();

        // wait for thread start
        final FastRandom r = FastRandom.get(10222L);
        Thread.sleep(r.nextInt(50, 100));

        // check state
        Assert.assertEquals(Thread.State.WAITING, t1.getState());

        // wait for the counter threads
        c1.join();

        counter.set(0);
        Assert.assertTrue(0 == counter.get());

        // wait for him
        t1.join(1000);

        // check state
        Assert.assertEquals(Thread.State.TERMINATED, t1.getState());
    }

    /**
     * Test method for {@link com.xceptance.common.util.SynchronizingCounter#awaitZero()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public final void testAwaitZero_WaitFor() throws InterruptedException
    {
        final SynchronizingCounter counter = new SynchronizingCounter(1);

        final Thread t1 = new Thread(new Waiter(counter, 1000));
        t1.start();

        // wait for thread start
        final FastRandom r = FastRandom.get(128102L);
        Thread.sleep(r.nextInt(50, 100));

        // check state
        Assert.assertEquals(Thread.State.TIMED_WAITING, t1.getState());

        Assert.assertTrue(0 != counter.get());

        // wait for him
        t1.join();

        Assert.assertTrue(0 != counter.get());

        // check state
        Assert.assertEquals(Thread.State.TERMINATED, t1.getState());
    }

    /**
     * Test method for {@link com.xceptance.common.util.SynchronizingCounter#awaitZero()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public final void testAwaitZero_WaitForAndSet0() throws InterruptedException
    {
        final SynchronizingCounter counter = new SynchronizingCounter(1);

        final Thread t1 = new Thread(new Waiter(counter, 30000));
        t1.start();

        // wait for thread start
        final FastRandom r = FastRandom.get(1987602L);
        Thread.sleep(r.nextInt(50, 100));

        // check state
        Assert.assertEquals(Thread.State.TIMED_WAITING, t1.getState());

        Assert.assertTrue(0 != counter.get());
        counter.set(0);
        Assert.assertTrue(0 == counter.get());

        // wait for him
        final long start = System.currentTimeMillis();
        t1.join();
        final long end = System.currentTimeMillis();

        // make sure we woke up before end of waitfor
        Assert.assertTrue((end - start) < 5000); // this is not math, but good enough

        // check state
        Assert.assertEquals(Thread.State.TERMINATED, t1.getState());
    }
}
