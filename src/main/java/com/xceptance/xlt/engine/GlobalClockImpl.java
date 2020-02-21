package com.xceptance.xlt.engine;

import com.xceptance.xlt.api.engine.GlobalClock;

/**
 * An implementation of the {@link GlobalClock} interface.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class GlobalClockImpl extends GlobalClock
{
    /**
     * The difference (in ms) between the global reference time and the local time.
     */
    private long referenceTimeDifference;

    /**
     * Returns the difference to the global reference time.
     * 
     * @return the difference in milliseconds
     */
    public long getReferenceTimeDifference()
    {
        return referenceTimeDifference;
    }

    /**
     * Sets the difference to the global reference time.
     * 
     * @param referenceTimeDifference
     *            the difference in milliseconds
     */
    public void setReferenceTimeDifference(final long referenceTimeDifference)
    {
        this.referenceTimeDifference = referenceTimeDifference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTime()
    {
        return System.currentTimeMillis() + referenceTimeDifference;
    }
}
