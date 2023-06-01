/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.report;

import java.util.List;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.util.SimpleArrayList;

public class PostProcessedDataContainer
{
    public final List<Data> data;

    public int droppedLines;

    public final int sampleFactor;

    /**
     * Creation time of last data record.
     */
    private long maximumTime = 0;

    /**
     * Creation time of first data record.
     */
    private long minimumTime = Long.MAX_VALUE;

    public PostProcessedDataContainer(final int size, final int sampleFactor)
    {
        data = new SimpleArrayList<>(size);
        this.sampleFactor = sampleFactor;
    }

    public List<Data> getData()
    {
        return data;
    }

    public void add(final Data d)
    {
        data.add(d);

        // maintain statistics
        final long time = d.getTime();

        minimumTime = Math.min(minimumTime, time);
        maximumTime = Math.max(maximumTime, time);
    }

    /**
     * Returns the maximum time.
     *
     * @return maximum time
     */
    public final long getMaximumTime()
    {
        return maximumTime;
    }

    /**
     * Returns the minimum time.
     *
     * @return minimum time
     */
    public final long getMinimumTime()
    {
        return (minimumTime == Long.MAX_VALUE) ? 0 : minimumTime;
    }
}
