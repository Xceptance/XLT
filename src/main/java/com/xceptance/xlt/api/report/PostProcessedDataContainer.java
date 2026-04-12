package com.xceptance.xlt.api.report;

import java.util.ArrayList;
import java.util.List;

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
    private final ArrayList<Data> customData;

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
        this.customData = new ArrayList<>(initialCapacity);
    }

    public boolean isEmpty()
    {
        return transactions.isEmpty() && actions.isEmpty() && requests.isEmpty() && events.isEmpty() 
            && pageLoadTimings.isEmpty() && webVitals.isEmpty() && customValues.isEmpty() 
            && customTimers.isEmpty() && jvmResourceUsage.isEmpty() && customData.isEmpty();
    }

    public void add(final Data d)
    {
        if (d instanceof TransactionData)
        {
            transactions.add((TransactionData) d);
        }
        else if (d instanceof ActionData)
        {
            actions.add((ActionData) d);
        }
        else if (d instanceof RequestData)
        {
            requests.add((RequestData) d);
        }
        else if (d instanceof EventData)
        {
            events.add((EventData) d);
        }
        else if (d instanceof PageLoadTimingData)
        {
            pageLoadTimings.add((PageLoadTimingData) d);
        }
        else if (d instanceof WebVitalData)
        {
            webVitals.add((WebVitalData) d);
        }
        else if (d instanceof CustomValue)
        {
            customValues.add((CustomValue) d);
        }
        else if (d instanceof CustomData)
        {
            customTimers.add((CustomData) d);
        }
        else if (d instanceof JvmResourceUsageData)
        {
            jvmResourceUsage.add((JvmResourceUsageData) d);
        }
        else
        {
            customData.add(d);
        }

        // maintain statistics
        final long time = d.getTime();

        minimumTime = Math.min(minimumTime, time);
        maximumTime = Math.max(maximumTime, time);
    }

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

    public ArrayList<Data> getCustomData()
    {
        return customData;
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
