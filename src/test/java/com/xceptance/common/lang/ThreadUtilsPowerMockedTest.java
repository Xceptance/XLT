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
package com.xceptance.common.lang;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Test the ThreadUtils
 * 
 * @author Rene Schwietzke
 * @see ThreadUtils
 */
public class ThreadUtilsPowerMockedTest
{
    /**
     * Start infinite sleep
     */
    @Test
    public final void testSleep() throws InterruptedException
    {
        try (MockedStatic<Thread> thread = Mockito.mockStatic(Thread.class))
        {
            ThreadUtils.sleep();
            thread.verify(() -> Thread.sleep(Long.MAX_VALUE));
        }
    }

    /**
     * Start a certain sleep use long, no call!
     * 
     * @throws InterruptedException
     */
    @Test
    public final void testSleepLong_0() throws InterruptedException
    {
        try (MockedStatic<Thread> thread = Mockito.mockStatic(Thread.class))
        {
            ThreadUtils.sleep(0L);
            thread.verifyNoInteractions();
        }
    }

    /**
     * Start a certain sleep use long and create an exception to check the correct catch inside, because we suppress it.
     * 
     * @throws InterruptedException
     */
    @Test
    public final void testSleepLong_Exception() throws InterruptedException
    {
        try (MockedStatic<Thread> thread = Mockito.mockStatic(Thread.class))
        {
            thread.when(() -> Thread.sleep(10L)).thenThrow(new InterruptedException());
            ThreadUtils.sleep(10L);
            thread.verify(() -> Thread.sleep(10L));
        }
    }

    /**
     * Start a certain sleep use long, no call!
     * 
     * @throws InterruptedException
     */
    @Test
    public final void testSleepLong_Negative() throws InterruptedException
    {
        try (MockedStatic<Thread> thread = Mockito.mockStatic(Thread.class))
        {
            ThreadUtils.sleep(-42L);
            thread.verifyNoInteractions();
        }
    }

    /**
     * Regular test.
     * 
     * @throws InterruptedException
     */
    @Test
    public final void testSleepLong() throws InterruptedException
    {
        try (MockedStatic<Thread> thread = Mockito.mockStatic(Thread.class))
        {
            ThreadUtils.sleep(7161);
            thread.verify(() -> Thread.sleep(7161L));
        }
    }

    /**
     * Thread was not interrupted.
     * 
     * @throws InterruptedException
     */
    @Test
    public final void testCheckIfInterrupted() throws InterruptedException
    {
        try (MockedStatic<Thread> thread = Mockito.mockStatic(Thread.class))
        {
            thread.when(Thread::interrupted).thenReturn(false);
            ThreadUtils.checkIfInterrupted();
            thread.verify(Thread::interrupted);
        }
    }

    /**
     * Thread was interrupted.
     * 
     * @throws InterruptedException
     */
    @Test(expected = InterruptedException.class)
    public final void testCheckIfInterrupted_Yes() throws InterruptedException
    {
        try (MockedStatic<Thread> thread = Mockito.mockStatic(Thread.class))
        {
            thread.when(Thread::interrupted).thenReturn(true);
            ThreadUtils.checkIfInterrupted();
        }
    }
}
