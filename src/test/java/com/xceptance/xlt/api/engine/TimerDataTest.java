package com.xceptance.xlt.api.engine;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.api.util.XltRandom;

/**
 * Test the implementation of {@link TimerData}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class TimerDataTest extends AbstractDataTest
{
    /**
     * TimerData test instance.
     */
    private TimerData instance = null;

    /**
     * The type code to use for creating new instances of class TimerData.
     */
    private static final String TYPE_CODE = "TS";

    /**
     * Runtime of data record.
     */
    protected final long runTime = 1L + XltRandom.nextInt(1000);

    /**
     * Failed status of data record.
     */
    protected final boolean failed = XltRandom.nextBoolean();

    /**
     * Common CSV representation (equal to {@link AbstractData#toCSV()}).
     */
    private final String commonCSV = getCommonCSV();

    /**
     * Test fixture setup.
     * 
     * @throws Exception
     *             thrown when setup failed.
     */
    @Before
    public void setupTimerStatisticsInstance() throws Exception
    {
        instance = new TimerData(TYPE_CODE)
        {
        };
    }

    /**
     * Tests the implementation of {@link TimerData#fromCSV(String)}.
     * <p>
     * Passed CSV string misses the values for the runtime and failed fields.
     * </p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void csvMissesRuntimeAndFailed()
    {
        instance.fromCSV(commonCSV);
    }

    /**
     * Tests the implementation of {@link TimerData#fromCSV(String)}.
     * <p>
     * Passed CSV string misses the value for the failed field.
     * </p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void csvMissesFailed()
    {
        instance.fromCSV(StringUtils.join(new Object[]
            {
                commonCSV, runTime
            }, Data.DELIMITER));
    }

    /**
     * Tests the implementation of {@link TimerData#fromCSV(String)}.
     * <p>
     * Passed CSV string misses the value for the failed field.
     * </p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void csvMissesRuntime()
    {
        instance.fromCSV(StringUtils.join(new Object[]
            {
                commonCSV, failed
            }, Data.DELIMITER));
    }

    /**
     * Tests the implementation of {@link TimerData#fromCSV(String)}.
     * <p>
     * The value of the field <code>runTime</code> is not a valid string representation of a long value. Expecting a
     * NumberFormatException.
     * </p>
     */
    @Test(expected = NumberFormatException.class)
    public void runTimeInCVSNotLong()
    {
        instance.fromCSV(StringUtils.join(new Object[]
            {
                commonCSV, "NotALong", failed
            }, Data.DELIMITER));
    }

    /**
     * Tests the implementation of {@link TimerData#fromCSV(String)}.
     * <p>
     * The value of the field <code>runTime</code> is negative. Expecting a RuntimeException.
     * </p>
     */
    @Test(expected = RuntimeException.class)
    public void runTimeInCVSNegative()
    {
        instance.fromCSV(StringUtils.join(new Object[]
            {
                commonCSV, -runTime, failed
            }, Data.DELIMITER));
    }

    /**
     * Tests the implementation of {@link TimerData#fromCSV(String)}.
     * <p>
     * The value of the field <code>failed</code> is not a valid string representation of a boolean value.
     * </p>
     */
    @Test
    public void failedInCSVNotBoolean()
    {
        final String failed = "NotaBool";
        instance.fromCSV(StringUtils.join(new Object[]
            {
                commonCSV, runTime, failed
            }, Data.DELIMITER));

        Assert.assertFalse(instance.hasFailed());
    }

    /**
     * Tests the implementation of {@link TimerData#fromCSV(String)}.
     * <p>
     * Test uses a compatible CSV representation and checks if all values have been applied.
     * </p>
     */
    @Override
    @Test
    public void testFromCSV_CompatibleCSV()
    {
        // read in CSV representation and parse it
        instance.fromCSV(StringUtils.join(new Object[]
            {
                commonCSV, runTime, failed
            }, Data.DELIMITER));

        // validate data record fields
        Assert.assertEquals(TYPE_CODE, instance.getTypeCode());
        Assert.assertEquals(name, instance.getName());
        Assert.assertEquals(time, instance.getTime());
        Assert.assertEquals(runTime, instance.getRunTime());
        Assert.assertEquals(failed, instance.hasFailed());
    }

    /**
     * Tests the implementation of {@link TimerData#toCSV()}.
     */
    @Test
    public void testToCSV()
    {
        // set fields
        instance.setName(name);
        instance.setTime(time);
        instance.setRunTime(runTime);
        instance.setFailed(failed);

        // validate output of 'toCSV()'
        Assert.assertEquals(StringUtils.join(new Object[]
            {
                commonCSV, runTime, failed
            }, Data.DELIMITER), instance.toCSV());
    }

    /**
     * Tests the implementation of {@link TimerData#setRunTime()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSetRunTime() throws InterruptedException
    {
        final long time = GlobalClock.getInstance().getTime();
        // set the time to current time
        instance.setTime(time);
        // wait one second
        Thread.sleep(1000);
        // set the new run time
        instance.setRunTime();
        // the run time should be one second
        Assert.assertEquals(1000, instance.getRunTime(), 20);
    }

    /**
     * Returns the common CSV representation.
     * 
     * @return common CSV representation
     */
    private String getCommonCSV()
    {
        final AbstractData stat = new AbstractData(TYPE_CODE)
        {
        };
        stat.setName(name);
        stat.setTime(time);

        return stat.toCSV();
    }
}
