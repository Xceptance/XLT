/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
