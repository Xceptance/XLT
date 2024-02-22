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
