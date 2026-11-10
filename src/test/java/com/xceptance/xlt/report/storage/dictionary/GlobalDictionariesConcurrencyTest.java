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
package com.xceptance.xlt.report.storage.dictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.common.util.StringMatcher;

/**
 * Reproduction tests for C2 concurrency and caching defects in {@link GlobalDictionaries}.
 */
public class GlobalDictionariesConcurrencyTest
{
    /**
     * Reproduces the mutable {@link CharSequence} reference-aliasing bug in the thread-local caches
     * of {@link GlobalDictionaries#getOrCreateTimerNameId(CharSequence)},
     * {@link GlobalDictionaries#getOrCreateAgentTestCaseId(CharSequence, CharSequence)}, and
     * {@link GlobalDictionaries#getOrCreateStringId(CharSequence)}.
     */
    @Test
    public void testMutableCharSequenceReuseReturnsDistinctIds()
    {
        final GlobalDictionaries dict = new GlobalDictionaries();

        // 1. Timer name with reused StringBuilder
        final StringBuilder timerBuf = new StringBuilder("CheckoutStep1");
        final int t1 = dict.getOrCreateTimerNameId(timerBuf);

        timerBuf.setLength(0);
        timerBuf.append("CheckoutStep2");
        final int t2 = dict.getOrCreateTimerNameId(timerBuf);

        Assert.assertNotEquals("Reused mutable CharSequence with different timer name must not return stale ID", t1, t2);
        Assert.assertEquals("CheckoutStep1", dict.getTimerName(t1));
        Assert.assertEquals("CheckoutStep2", dict.getTimerName(t2));

        // 2. Agent + TestCase pair with reused StringBuilders
        final StringBuilder agentBuf = new StringBuilder("Agent-01");
        final StringBuilder tcBuf = new StringBuilder("TBrowse");
        final int p1 = dict.getOrCreateAgentTestCaseId(agentBuf, tcBuf);

        tcBuf.setLength(0);
        tcBuf.append("TOrder");
        final int p2 = dict.getOrCreateAgentTestCaseId(agentBuf, tcBuf);

        Assert.assertNotEquals("Reused mutable CharSequence with different scenario must not return stale pair ID", p1, p2);
        Assert.assertEquals("TBrowse", dict.getTestCaseName(p1));
        Assert.assertEquals("TOrder", dict.getTestCaseName(p2));

        // 3. General string with reused StringBuilder
        final StringBuilder strBuf = new StringBuilder("application/xml");
        final int s1 = dict.getOrCreateStringId(strBuf);

        strBuf.setLength(0);
        strBuf.append("application/pdf");
        final int s2 = dict.getOrCreateStringId(strBuf);

        Assert.assertNotEquals("Reused mutable CharSequence with different string must not return stale string ID", s1, s2);
        Assert.assertEquals("application/xml", dict.getString(s1));
        Assert.assertEquals("application/pdf", dict.getString(s2));
    }

    /**
     * Reproduces `ArrayIndexOutOfBoundsException` and `NullPointerException` when reader threads call
     * {@link GlobalDictionaries#getTimerName(int)}, {@link GlobalDictionaries#getAgentTestCase(int)},
     * {@link GlobalDictionaries#getString(int)}, and {@link GlobalDictionaries#filterAgentTestCaseIds}
     * concurrently while writer threads expand the dictionaries across capacity boundaries (256, 512, 1024).
     */
    @Test
    public void testConcurrentDictionaryExpansionAndLockFreeReads() throws Exception
    {
        final int threads = 6;
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        try
        {
            final AtomicReference<Throwable> failure = new AtomicReference<>();
            final StringMatcher matcher = new StringMatcher(".*", null, true);

            for (int round = 0; round < 50 && failure.get() == null; round++)
            {
                final GlobalDictionaries dict = new GlobalDictionaries();
                final CyclicBarrier startBarrier = new CyclicBarrier(threads);
                final AtomicBoolean writersDone = new AtomicBoolean(false);
                final List<Future<?>> futures = new ArrayList<>(threads);

                // 3 Writer threads inserting across 256 / 512 / 1024 resize boundaries
                for (int w = 0; w < 3; w++)
                {
                    final int writerIdx = w;
                    futures.add(pool.submit((Callable<Void>) () -> {
                        startBarrier.await();
                        try
                        {
                            for (int i = 0; i < 400 && failure.get() == null; i++)
                            {
                                final String key = "W" + writerIdx + "-" + i;
                                final int tid = dict.getOrCreateTimerNameId(key);
                                if (!key.equals(dict.getTimerName(tid)))
                                {
                                    failure.compareAndSet(null, new AssertionError(
                                        "Timer lookup mismatch for ID " + tid + ": expected " + key + " but got " + dict.getTimerName(tid)));
                                }

                                final int pid = dict.getOrCreateAgentTestCaseId("Ag-" + writerIdx, key);
                                final GlobalDictionaries.AgentTestCase pair = dict.getAgentTestCase(pid);
                                if (pair == null || !key.equals(pair.testCaseName()))
                                {
                                    failure.compareAndSet(null, new AssertionError(
                                        "Pair lookup mismatch for ID " + pid + ": expected " + key + " but got " + pair));
                                }

                                final int sid = dict.getOrCreateStringId(key);
                                if (!key.equals(dict.getString(sid)))
                                {
                                    failure.compareAndSet(null, new AssertionError(
                                        "String lookup mismatch for ID " + sid + ": expected " + key + " but got " + dict.getString(sid)));
                                }
                            }
                        }
                        catch (final Throwable t)
                        {
                            failure.compareAndSet(null, t);
                        }
                        finally
                        {
                            writersDone.set(true);
                        }
                        return null;
                    }));
                }

                // 3 Reader threads continuously scanning latest IDs and running filterAgentTestCaseIds
                for (int r = 0; r < 3; r++)
                {
                    futures.add(pool.submit((Callable<Void>) () -> {
                        startBarrier.await();
                        try
                        {
                            while (!writersDone.get() && failure.get() == null)
                            {
                                final int tCount = dict.getTimerNameCount();
                                if (tCount > 0)
                                {
                                    final String tName = dict.getTimerName(tCount - 1);
                                    if (tName == null)
                                    {
                                        failure.compareAndSet(null, new AssertionError(
                                            "Observed null timerName at valid index " + (tCount - 1) + " (count=" + tCount + ")"));
                                    }
                                }

                                final int sCount = dict.getStringCount();
                                if (sCount > 0)
                                {
                                    final String sVal = dict.getString(sCount - 1);
                                    if (sVal == null)
                                    {
                                        failure.compareAndSet(null, new AssertionError(
                                            "Observed null string at valid index " + (sCount - 1) + " (count=" + sCount + ")"));
                                    }
                                }

                                // Exercises loop over [0 .. agentTestCaseCount) dereferencing pair.agentName()
                                dict.filterAgentTestCaseIds(matcher, matcher);
                            }
                        }
                        catch (final Throwable t)
                        {
                            failure.compareAndSet(null, t);
                        }
                        return null;
                    }));
                }

                for (final Future<?> f : futures)
                {
                    f.get();
                }
            }

            if (failure.get() != null)
            {
                final Throwable err = failure.get();
                if (err instanceof AssertionError ae)
                {
                    throw ae;
                }
                throw new AssertionError("Concurrent dictionary expansion/read failed with: " + err, err);
            }
        }
        finally
        {
            pool.shutdownNow();
        }
    }

    /**
     * Reproduces broken double-checked locking / compaction race when multiple threads concurrently
     * intern entries on a compacted {@link GlobalDictionaries} instance.
     */
    @Test
    public void testConcurrentLazyMapRebuildAndCompactionConsistency() throws Exception
    {
        final int threads = 6;
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        try
        {
            for (int round = 0; round < 100; round++)
            {
                final GlobalDictionaries dict = new GlobalDictionaries();
                final int seedTimerId = dict.getOrCreateTimerNameId("SeedTimer");
                dict.addSampleUrl(seedTimerId, "https://example.com/seed");
                dict.compactForReadOnly();

                final CyclicBarrier barrier = new CyclicBarrier(threads);
                final List<Future<Integer>> futures = new ArrayList<>(threads);
                for (int t = 0; t < threads; t++)
                {
                    final int threadIdx = t;
                    futures.add(pool.submit(() -> {
                        barrier.await();
                        if (threadIdx == 0)
                        {
                            dict.compactForReadOnly();
                        }
                        final GlobalDictionaries.CachedSampleUrl[] samples = dict.getCachedSampleUrls(seedTimerId);
                        Assert.assertNotNull("getCachedSampleUrls must never return null for populated timer across compaction", samples);
                        return dict.getOrCreateTimerNameId("SharedPostCompactTimer");
                    }));
                }

                int expectedId = -1;
                for (final Future<Integer> f : futures)
                {
                    final int id = f.get();
                    if (expectedId == -1)
                    {
                        expectedId = id;
                    }
                    else
                    {
                        Assert.assertEquals("All threads interning the same timer name must receive the same ID", expectedId, id);
                    }
                }
                Assert.assertEquals("Subsequent lookup must resolve to the same ID",
                                    expectedId, dict.getOrCreateTimerNameId("SharedPostCompactTimer"));
            }
        }
        finally
        {
            pool.shutdownNow();
        }
    }
}
