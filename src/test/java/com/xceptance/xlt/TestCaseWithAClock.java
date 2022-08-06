package com.xceptance.xlt;

import org.junit.BeforeClass;

import com.xceptance.xlt.api.engine.ClockSwitcher;
import com.xceptance.xlt.api.engine.TestClockImpl;

public class TestCaseWithAClock
{
    @BeforeClass
    public static void setupTime()
    {
        ClockSwitcher.init(TestClockImpl.getInstance());
    }
}
