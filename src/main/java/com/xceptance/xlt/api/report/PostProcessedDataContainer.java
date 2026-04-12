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
package com.xceptance.xlt.api.report;

import java.util.ArrayList;

import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.engine.CustomValue;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.PageLoadTimingData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.engine.WebVitalData;
import com.xceptance.xlt.agent.JvmResourceUsageData;

/**
 * A strongly-typed container for post-processed data records, organized by concrete data type.
 * <p>
 * Each typed {@code add*()} method inserts a record into the appropriate backing list and
 * updates the container's time boundaries (min/max). This design eliminates the need for
 * {@code instanceof} checks during downstream processing — providers simply iterate the
 * typed list they care about.
 * <p>
 * The container is designed to be reused across chunks: call {@link #clear()} between uses.
 */
public class PostProcessedDataContainer
{

    private final ArrayList<TransactionData> transactions;
    private final ArrayList<ActionData> actions;
    private final ArrayList<RequestData> requests;
    private final ArrayList<EventData> events;
    private final ArrayList<PageLoadTimingData> pageLoadTimings;
    private final ArrayList<WebVitalData> webVitals;
    private final ArrayList<CustomValue> customValues;
    private final ArrayList<CustomData> customTimers;
    private final ArrayList<JvmResourceUsageData> jvmResourceUsage;
    private final ArrayList<Data> otherData;

    /**
     * Creation time of last data record.
     */
    private long maximumTime = 0;

    /**
     * Creation time of first data record.
     */
    private long minimumTime = Long.MAX_VALUE;

    public PostProcessedDataContainer(final int size)
    {
        final int initialCapacity = Math.max(16, size / 10);
        
        this.transactions = new ArrayList<>(initialCapacity);
        this.actions = new ArrayList<>(initialCapacity);
        this.requests = new ArrayList<>(initialCapacity);
        this.events = new ArrayList<>(initialCapacity);
        this.pageLoadTimings = new ArrayList<>(initialCapacity);
        this.webVitals = new ArrayList<>(initialCapacity);
        this.customValues = new ArrayList<>(initialCapacity);
        this.customTimers = new ArrayList<>(initialCapacity);
        this.jvmResourceUsage = new ArrayList<>(initialCapacity);
        this.otherData = new ArrayList<>(initialCapacity);
    }

    public boolean isEmpty()
    {
        return transactions.isEmpty() && actions.isEmpty() && requests.isEmpty() && events.isEmpty() 
            && pageLoadTimings.isEmpty() && webVitals.isEmpty() && customValues.isEmpty() 
            && customTimers.isEmpty() && jvmResourceUsage.isEmpty() && otherData.isEmpty();
    }

    public void clear()
    {
        transactions.clear();
        actions.clear();
        requests.clear();
        events.clear();
        pageLoadTimings.clear();
        webVitals.clear();
        customValues.clear();
        customTimers.clear();
        jvmResourceUsage.clear();
        otherData.clear();

        maximumTime = 0;
        minimumTime = Long.MAX_VALUE;
    }

    /**
     * Updates the time boundaries (min/max) for this container. Called internally by
     * the typed add methods to track the overall time range of data in the container.
     *
     * @param time
     *            the timestamp of the data record being added
     */
    private void updateTime(final long time)
    {
        minimumTime = Math.min(minimumTime, time);
        maximumTime = Math.max(maximumTime, time);
    }

    public void addTransaction(final TransactionData d) { transactions.add(d); updateTime(d.getTime()); }
    public void addAction(final ActionData d) { actions.add(d); updateTime(d.getTime()); }
    public void addRequest(final RequestData d) { requests.add(d); updateTime(d.getTime()); }
    public void addEvent(final EventData d) { events.add(d); updateTime(d.getTime()); }
    public void addPageLoadTiming(final PageLoadTimingData d) { pageLoadTimings.add(d); updateTime(d.getTime()); }
    public void addWebVital(final WebVitalData d) { webVitals.add(d); updateTime(d.getTime()); }
    public void addCustomValue(final CustomValue d) { customValues.add(d); updateTime(d.getTime()); }
    public void addCustomTimer(final CustomData d) { customTimers.add(d); updateTime(d.getTime()); }
    public void addJvmResourceUsage(final JvmResourceUsageData d) { jvmResourceUsage.add(d); updateTime(d.getTime()); }
    public void addOtherData(final Data d) { otherData.add(d); updateTime(d.getTime()); }

    public ArrayList<TransactionData> getTransactions()
    {
        return transactions;
    }

    public ArrayList<ActionData> getActions()
    {
        return actions;
    }

    public ArrayList<RequestData> getRequests()
    {
        return requests;
    }

    public ArrayList<EventData> getEvents()
    {
        return events;
    }

    public ArrayList<PageLoadTimingData> getPageLoadTimings()
    {
        return pageLoadTimings;
    }

    public ArrayList<WebVitalData> getWebVitals()
    {
        return webVitals;
    }

    public ArrayList<CustomValue> getCustomValues()
    {
        return customValues;
    }

    public ArrayList<CustomData> getCustomTimers()
    {
        return customTimers;
    }

    public ArrayList<JvmResourceUsageData> getJvmResourceUsage()
    {
        return jvmResourceUsage;
    }

    public ArrayList<Data> getOtherData()
    {
        return otherData;
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
