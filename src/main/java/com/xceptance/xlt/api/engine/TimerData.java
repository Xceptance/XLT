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
package com.xceptance.xlt.api.engine;

import java.util.List;

import com.xceptance.common.lang.ParseBoolean;
import com.xceptance.common.lang.ParseNumbers;
import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * The {@link TimerData} class is the super class for all timer-based data records.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public abstract class TimerData extends AbstractData
{
    /**
     * The runtime of the request.
     */
    private int runTime;

    /**
     * Indicates whether or not the request was successful.
     */
    private boolean failed;

    /**
     * Creates a new {@link TimerData} object and gives it the specified type code.
     *
     * @param typeCode
     *            the type code
     */
    public TimerData(final char typeCode)
    {
        super(typeCode);
    }

    /**
     * Creates a new {@link TimerData} object and gives it the specified name and type code. Furthermore, the start time
     * attribute is set to the current time.
     *
     * @param name
     *            the request name
     * @param typeCode
     *            the type code
     */
    public TimerData(final String name, final char typeCode)
    {
        super(name, typeCode);
    }

    /**
     * Returns the end time. Calculated from start time and run time.
     *
     * @return the end time
     */
    public long getEndTime()
    {
        return getTime() + runTime;
    }

    /**
     * Returns the run time.
     *
     * @return the run time
     */
    public int getRunTime()
    {
        return runTime;
    }

    /**
     * Indicates whether or not a failure had occurred.
     *
     * @return the failure status
     */
    public boolean hasFailed()
    {
        return failed;
    }

    /**
     * Sets whether or not a failure had occurred.
     *
     * @param failed
     *            the new status
     */
    public void setFailed(final boolean failed)
    {
        this.failed = failed;
    }

    /**
     * Sets the run time. Convenience method for long values, but note that internally the value is cast to int.
     *
     * @param runTime
     *            the runTime
     */
    public void setRunTime(final long runTime)
    {
        this.runTime = (int) runTime;
    }

    /**
     * Sets the run time.
     *
     * @param runTime
     *            the runTime
     */
    public void setRunTime(final int runTime)
    {
        this.runTime = runTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> toList()
    {
        final List<String> fields = super.toList();

        fields.add(Long.toString(runTime));
        fields.add(Boolean.toString(failed));

        return fields;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRemainingValues(final List<XltCharBuffer> values)
    {
        // read and check the values
        runTime = ParseNumbers.parseInt(values.get(3));

        if (runTime < 0)
        {
            throw new IllegalArgumentException("Invalid value for the 'runtime' attribute.");
        }

        failed = ParseBoolean.parse(values.get(4));
    }
}
