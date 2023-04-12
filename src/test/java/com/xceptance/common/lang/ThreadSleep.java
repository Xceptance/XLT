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
package com.xceptance.common.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

import com.xceptance.xlt.api.engine.GlobalClock;

/**
 * Class description goes here.
 *
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class ThreadSleep
{
    public static final Random random = new Random();

    private static final int ITERATIONS = 200;

    private static final int SLEEPTIME = 100;

    private static final int THREADS = 200;

    static class SleepThread extends Thread
    {
        private static final Queue<SleepThread> threads = new ConcurrentLinkedQueue<SleepThread>();

        public long endSleepTime = 0;

        private void sleep(final int duration)
        {
            long now = GlobalClock.millis();

            final SleepThread first = threads.peek();
            if (first != null)
            {
                if (first.endSleepTime >= GlobalClock.millis())
                {
                    threads.remove(first);
                    LockSupport.unpark(first);
                }
            }

            // retire for a moment
            endSleepTime = GlobalClock.millis() + duration;
            while (now < endSleepTime)
            {
                LockSupport.parkUntil(endSleepTime);
                now = GlobalClock.millis();;
            }
        }

        @Override
        public void run()
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (final InterruptedException e1)
            {
                e1.printStackTrace();
            }

            final StringBuilder a = new StringBuilder();
            long min = Integer.MAX_VALUE;
            long max = 0;
            final long start = GlobalClock.millis();
            for (int i = 0; i < ITERATIONS; i++)
            {
                final long s = GlobalClock.millis();
                sleep(SLEEPTIME);
                final long e = GlobalClock.millis();

                final long last = e - s;

                if (last < min)
                {
                    min = last;
                }
                if (last > max)
                {
                    max = last;
                }

                final int l = random.nextInt(500) + 1500;
                for (int j = 0; j < l; j++)
                {
                    a.append("a" + "b" + i);
                }
            }
            final long end = GlobalClock.millis();

            sleep(1000);
            final long runtime = end - start;
            final long avg = runtime / ITERATIONS;
            System.out.println("Runtime " + getName() + ": " + runtime + " / " + avg + " / min: " + min + " / max: " + max);
        }
    }

    public static void main(final String[] args)
    {
        final List<SleepThread> list = new ArrayList<SleepThread>();
        for (int i = 0; i < THREADS; i++)
        {
            final SleepThread t = new ThreadSleep.SleepThread();
            list.add(t);

            t.start();
        }
        System.out.println("Setup done");
        for (int i = 0; i < THREADS; i++)
        {
            final SleepThread t = list.get(i);
            try
            {
                t.join();
            }
            catch (final InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
