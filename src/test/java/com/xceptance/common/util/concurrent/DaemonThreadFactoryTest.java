package com.xceptance.common.util.concurrent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.common.util.Getter;

/**
 * @author rschwietzke
 */
public class DaemonThreadFactoryTest
{
    class TestCallable implements Callable<String>
    {
        @Override
        public String call() throws Exception
        {
            Assert.assertTrue(Thread.currentThread().isDaemon());
            return Thread.currentThread().getName();
        }
    }

    /**
     * Test method for {@link com.xceptance.common.util.concurrent.DaemonThreadFactory#DaemonThreadFactory()}.
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public final void testDaemonThreadFactory() throws InterruptedException, ExecutionException
    {
        final Set<String> results = new HashSet<String>();

        ExecutorService executorService = null;
        try
        {
            final ThreadFactory threadFactory = new DaemonThreadFactory();
            executorService = Executors.newFixedThreadPool(5, threadFactory);

            results.add(executorService.submit(new TestCallable()).get());
            results.add(executorService.submit(new TestCallable()).get());
            results.add(executorService.submit(new TestCallable()).get());
        }
        finally
        {
            executorService.shutdown();
        }

        Assert.assertEquals(3, results.size());
        Assert.assertTrue(results.contains("0"));
        Assert.assertTrue(results.contains("1"));
        Assert.assertTrue(results.contains("2"));
    }

    /**
     * Test method for
     * {@link com.xceptance.common.util.concurrent.DaemonThreadFactory#DaemonThreadFactory(java.lang.String)}.
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public final void testDaemonThreadFactoryString() throws InterruptedException, ExecutionException
    {
        final Set<String> results = new HashSet<String>();

        ExecutorService executorService = null;
        try
        {
            final ThreadFactory threadFactory = new DaemonThreadFactory("foo-");
            executorService = Executors.newFixedThreadPool(5, threadFactory);

            results.add(executorService.submit(new TestCallable()).get());
            results.add(executorService.submit(new TestCallable()).get());
            results.add(executorService.submit(new TestCallable()).get());
        }
        finally
        {
            executorService.shutdown();
        }

        Assert.assertEquals(3, results.size());
        Assert.assertTrue(results.contains("foo-0"));
        Assert.assertTrue(results.contains("foo-1"));
        Assert.assertTrue(results.contains("foo-2"));
    }

    /**
     * Test method for
     * {@link com.xceptance.common.util.concurrent.DaemonThreadFactory#DaemonThreadFactory(com.xceptance.common.util.Getter)}
     * .
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public final void testDaemonThreadFactoryGetterOfString() throws InterruptedException, ExecutionException
    {
        final Set<String> results = new HashSet<String>();

        ExecutorService executorService = null;
        try
        {
            final ThreadFactory threadFactory = new DaemonThreadFactory(new Getter<String>()
            {
                @Override
                public String get()
                {
                    return "A38-";
                }
            });

            executorService = Executors.newFixedThreadPool(5, threadFactory);

            results.add(executorService.submit(new TestCallable()).get());
            results.add(executorService.submit(new TestCallable()).get());
            results.add(executorService.submit(new TestCallable()).get());
        }
        finally
        {
            executorService.shutdown();
        }

        Assert.assertEquals(3, results.size());
        Assert.assertTrue(results.contains("A38-0"));
        Assert.assertTrue(results.contains("A38-1"));
        Assert.assertTrue(results.contains("A38-2"));
    }
}
