package com.xceptance.xlt.engine;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.replayAll;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.xceptance.xlt.api.engine.GlobalClock;

@RunWith(PowerMockRunner.class)
@PrepareForTest(
    {
        GlobalClockImpl.class
    })
public class GlobalClockImplTest
{
    @Test
    public final void testGetTime()
    {
        PowerMock.mockStatic(System.class);

        expect(System.currentTimeMillis()).andReturn(890000L);
        replayAll();

        ((GlobalClockImpl) GlobalClock.getInstance()).setReferenceTimeDifference(0);
        Assert.assertEquals(890000L, GlobalClock.getInstance().getTime());
    }

    @Test
    public final void testSetReferenceTimeDifference()
    {
        PowerMock.mockStatic(System.class);

        expect(System.currentTimeMillis()).andReturn(890000L);
        replayAll();

        ((GlobalClockImpl) GlobalClock.getInstance()).setReferenceTimeDifference(111);
        Assert.assertEquals(890111L, GlobalClock.getInstance().getTime());
    }

}
