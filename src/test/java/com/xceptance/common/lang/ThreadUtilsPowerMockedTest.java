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
package com.xceptance.common.lang;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

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
        try (MockedStatic<ThreadUtils> threadUtilsMock = Mockito.mockStatic(ThreadUtils.class, Mockito.CALLS_REAL_METHODS))
        {
            threadUtilsMock.when(() -> ThreadUtils.doSleep(100)).thenReturn(null);
            
            ThreadUtils.sleep();
            
            threadUtilsMock.verify(() -> ThreadUtils.doSleep(100), times(1));
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
        try (MockedStatic<ThreadUtils> threadUtilsMock = Mockito.mockStatic(ThreadUtils.class, Mockito.CALLS_REAL_METHODS))
        {
            ThreadUtils.sleep(0L);
            
            threadUtilsMock.verify(() -> ThreadUtils.doSleep(0), never());
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
        try (MockedStatic<ThreadUtils> threadUtilsMock = Mockito.mockStatic(ThreadUtils.class, Mockito.CALLS_REAL_METHODS))
        {
            threadUtilsMock.when(() -> ThreadUtils.doSleep(10)).thenThrow(new InterruptedException());
            
            ThreadUtils.sleep(10);
            
            threadUtilsMock.verify(() -> ThreadUtils.doSleep(10), times(1));
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
        try (MockedStatic<ThreadUtils> threadUtilsMock = Mockito.mockStatic(ThreadUtils.class, Mockito.CALLS_REAL_METHODS))
        {
            ThreadUtils.sleep(-42L);
            
            threadUtilsMock.verify(() -> ThreadUtils.doSleep(-42L), never());
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
        mockStatic(Thread.class);
        Thread.sleep(7161L);
        expectLastCall().once();
        replayAll();

        ThreadUtils.sleep(7161);

        verifyAll();
    }

    /**
     * Thread was not interrupted.
     * 
     * @throws InterruptedException
     */
    @Test
    public final void testCheckIfInterrupted() throws InterruptedException
    {
        mockStatic(Thread.class);
        expect(Thread.interrupted()).andReturn(false);
        replayAll();

        ThreadUtils.checkIfInterrupted();

        verifyAll();
    }

    /**
     * Thread was interrupted.
     * 
     * @throws InterruptedException
     */
    @Test(expected = InterruptedException.class)
    public final void testCheckIfInterrupted_Yes() throws InterruptedException
    {
        mockStatic(Thread.class);
        expect(Thread.interrupted()).andReturn(true);
        replayAll();

        ThreadUtils.checkIfInterrupted();
    }

}
