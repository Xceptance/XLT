package com.xceptance.common.lang;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Test the ThreadUtils
 * 
 * @author Rene Schwietzke
 * @see ThreadUtils
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(
    {
        ThreadUtils.class
    })
public class ThreadUtilsPowerMockedTest
{
    /**
     * Start infinite sleep
     */
    @Test
    public final void testSleep() throws InterruptedException
    {
        mockStatic(Thread.class);
        Thread.sleep(Long.MAX_VALUE);
        expectLastCall().once();
        replayAll();

        ThreadUtils.sleep();

        verifyAll();
    }

    /**
     * Start a certain sleep use long, no call!
     * 
     * @throws InterruptedException
     */
    @Test
    public final void testSleepLong_0() throws InterruptedException
    {
        mockStatic(Thread.class);
        replayAll();

        ThreadUtils.sleep(0L);

        verifyAll();
    }

    /**
     * Start a certain sleep use long and create an exception to check the correct catch inside, because we suppress it.
     * 
     * @throws InterruptedException
     */
    @Test
    public final void testSleepLong_Exception() throws InterruptedException
    {
        mockStatic(Thread.class);
        Thread.sleep(10L);
        expectLastCall().andThrow(new InterruptedException());
        replayAll();

        ThreadUtils.sleep(10L);

        verifyAll();
    }

    /**
     * Start a certain sleep use long, no call!
     * 
     * @throws InterruptedException
     */
    @Test
    public final void testSleepLong_Negative() throws InterruptedException
    {
        mockStatic(Thread.class);
        replayAll();

        ThreadUtils.sleep(-42L);

        verifyAll();
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
