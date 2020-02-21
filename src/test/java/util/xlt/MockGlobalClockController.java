package util.xlt;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.xceptance.xlt.api.engine.GlobalClock;

/**
 * A utility class that implements mocking of the {@link GlobalClock} singleton. <br>
 * <b>NOTE</b>: Any test class that wants to use this must specify a {@link RunWith} annotation with value
 * <code>PowerMockRunner.class</code> and a {@link PrepareForTest} annotation containing <code>GlobalClock.class</code>
 * in its value.<br>
 * Also, {@link #prepareMockingOfGlobalClockSingletonInstance()} must be called during test setup (e.g. in a
 * {@link Before} method)
 * 
 * @author Deniz Altin
 */
public class MockGlobalClockController
{
    private static long time = 314159265;

    private static long increment = 0;

    private MockGlobalClockController()
    {
    }

    public static synchronized void setIncrement(final long increment)
    {
        MockGlobalClockController.increment = increment;
    }

    public static synchronized long getIncrement()
    {
        return MockGlobalClockController.increment;
    }

    public static synchronized void setTime(final long time)
    {
        MockGlobalClockController.time = time;
    }

    public static synchronized long getTime()
    {
        return MockGlobalClockController.time;
    }

    private static synchronized long getTimeThenIncrement()
    {
        long previousTime = time;
        time += increment;
        return previousTime;
    }

    public static void prepareMockingOfGlobalClockSingletonInstance()
    {
        final GlobalClock mockGlobalClockInstance = Mockito.mock(GlobalClock.class);
        Mockito.when(mockGlobalClockInstance.getTime()).thenAnswer((i) -> MockGlobalClockController.getTimeThenIncrement());
        PowerMockito.mockStatic(GlobalClock.class);
        PowerMockito.when(GlobalClock.getInstance()).thenReturn(mockGlobalClockInstance);
    }
}
