package com.xceptance.xlt.report.util;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.util.XltRandom;

/**
 * Tests the implementation of {@link TaskManager}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class TaskManagerTest
{
    @Test
    public void testWaitForTasksToComplete() throws Throwable
    {
        final TaskManager taskmgr = TaskManager.getInstance();
        taskmgr.setMaximumThreadCount(8);

        // total number of tasks to perform
        final int nbTasks = XltRandom.nextInt(100, 200);
        // create count-down latch used to determine when all tasks have been completed
        final CountDownLatch latch = new CountDownLatch(nbTasks);

        // add the tasks
        for (int i = 0; i < nbTasks; i++)
        {
            taskmgr.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        try
                        {
                            Thread.sleep(50 + XltRandom.nextInt(0, 150));
                        }
                        catch (final InterruptedException e)
                        {
                            Thread.currentThread().interrupt();
                        }
                    }
                    finally
                    {
                        latch.countDown();
                    }
                }
            });
        }

        try
        {
            // wait 'til all tasks have been completed (or waiting was interrupted)
            taskmgr.waitForAllTasksToComplete();
        }
        finally
        {
            try
            {
                // check number of active tasks using reflection since executor is hidden
                final Field field = TaskManager.class.getDeclaredField("executor");
                field.setAccessible(true);
                final ThreadPoolExecutor executor = (ThreadPoolExecutor) field.get(taskmgr);
                Assert.assertEquals(0, executor.getActiveCount());
                // check if all tasks are really gone using the count-down latch
                Assert.assertEquals(0, latch.getCount());
            }
            finally
            {
                // just to make sure
                latch.await();
            }
        }
    }
}
