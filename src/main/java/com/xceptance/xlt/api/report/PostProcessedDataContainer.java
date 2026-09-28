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

import java.util.List;

import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.engine.CustomValue;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.util.SimpleArrayList;

public class PostProcessedDataContainer
{
    /**
     * List of data records accumulated for this chunk. Preserves public API and bytecode compatibility
     * as {@code List<Data>} for external callers and compiled modules.
     */
    public final List<Data> data;

    /**
     * Backing fast-path array list representation providing direct array access without interface virtual calls.
     */
    public final SimpleArrayList<Data> dataList;

    public int droppedLines;

    public final int sampleFactor;

    /**
     * Flag indicating whether this chunk contains any failed records or HTTP errors (5xx/0).
     * Used by error report providers to skip clean chunks entirely.
     */
    public boolean hasFailedRecords = false;

    /**
     * The type code of the records stored in this container (e.g. 'R' for Request, 'A' for Action,
     * 'T' for Transaction, etc.), or 0 if unknown or mixed.
     */
    public char typeCode = 0;

    /**
     * Returns the number of records currently in this container.
     *
     * @return the number of records
     */
    public int size()
    {
        return dataList.size();
    }

    /**
     * Creation time of last data record.
     */
    private long maximumTime = 0;

    /**
     * Creation time of first data record.
     */
    private long minimumTime = Long.MAX_VALUE;

    /**
     * Pre-allocated pool of RequestData instances for zero-allocation chunk processing.
     */
    private RequestData[] requestDataPool;

    /**
     * Pre-allocated pool of TransactionData instances for zero-allocation chunk processing.
     */
    private TransactionData[] transactionDataPool;

    /**
     * Pre-allocated pool of ActionData instances for zero-allocation chunk processing.
     */
    private ActionData[] actionDataPool;

    /**
     * Pre-allocated pool of CustomData instances for zero-allocation chunk processing.
     */
    private CustomData[] customDataPool;

    /**
     * Pre-allocated pool of CustomValue instances for zero-allocation chunk processing.
     */
    private CustomValue[] customValuePool;

    public PostProcessedDataContainer(final int size, final int sampleFactor)
    {
        final SimpleArrayList<Data> list = new SimpleArrayList<>(size);
        this.dataList = list;
        this.data = list;
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
     * Adds a data record directly without per-record min/max comparison overhead.
     * Used when the scanning loop tracks or provides chunk time bounds in bulk via {@link #updateTimeRange(long, long)}.
     *
     * @param d
     *            the data record to add
     */
    public void addFast(final Data d)
    {
        data.add(d);
    }

    /**
     * Updates the container's minimum and maximum timestamps in bulk.
     *
     * @param min
     *            minimum timestamp
     * @param max
     *            maximum timestamp
     */
    public void updateTimeRange(final long min, final long max)
    {
        if (min < minimumTime)
        {
            minimumTime = min;
        }
        if (max > maximumTime)
        {
            maximumTime = max;
        }
    }

    /**
     * Obtains a reusable {@link RequestData} instance from this container's pre-allocated pool,
     * avoiding millions of heap allocations during high-throughput columnar scanning.
     *
     * @param index
     *            the row index within the current chunk
     * @return the pooled {@link RequestData} instance
     */
    public RequestData getOrCreateRequestData(final int index)
    {
        if (requestDataPool == null)
        {
            requestDataPool = new RequestData[65536];
        }
        else if (index >= requestDataPool.length)
        {
            requestDataPool = java.util.Arrays.copyOf(requestDataPool, Math.max(requestDataPool.length * 2, index + 1));
        }

        RequestData req = requestDataPool[index];
        if (req == null)
        {
            req = new RequestData();
            requestDataPool[index] = req;
        }
        return req;
    }

    /**
     * Obtains a reusable {@link TransactionData} instance from this container's pre-allocated pool,
     * avoiding heap allocations during transaction chunk scanning.
     *
     * @param index
     *            the row index within the current chunk
     * @return the pooled {@link TransactionData} instance
     */
    public TransactionData getOrCreateTransactionData(final int index)
    {
        if (transactionDataPool == null)
        {
            transactionDataPool = new TransactionData[65536];
        }
        else if (index >= transactionDataPool.length)
        {
            transactionDataPool = java.util.Arrays.copyOf(transactionDataPool, Math.max(transactionDataPool.length * 2, index + 1));
        }

        TransactionData txn = transactionDataPool[index];
        if (txn == null)
        {
            txn = new TransactionData();
            transactionDataPool[index] = txn;
        }
        return txn;
    }

    /**
     * Obtains a reusable {@link ActionData} instance from this container's pre-allocated pool,
     * avoiding millions of heap allocations during action chunk scanning.
     *
     * @param index
     *            the row index within the current chunk
     * @return the pooled {@link ActionData} instance
     */
    public ActionData getOrCreateActionData(final int index)
    {
        if (actionDataPool == null)
        {
            actionDataPool = new ActionData[65536];
        }
        else if (index >= actionDataPool.length)
        {
            actionDataPool = java.util.Arrays.copyOf(actionDataPool, Math.max(actionDataPool.length * 2, index + 1));
        }

        ActionData act = actionDataPool[index];
        if (act == null)
        {
            act = new ActionData();
            actionDataPool[index] = act;
        }
        return act;
    }

    /**
     * Obtains a reusable {@link CustomData} instance from this container's pre-allocated pool,
     * avoiding heap allocations during custom timer chunk scanning.
     *
     * @param index
     *            the row index within the current chunk
     * @return the pooled {@link CustomData} instance
     */
    public CustomData getOrCreateCustomData(final int index)
    {
        if (customDataPool == null)
        {
            customDataPool = new CustomData[65536];
        }
        else if (index >= customDataPool.length)
        {
            customDataPool = java.util.Arrays.copyOf(customDataPool, Math.max(customDataPool.length * 2, index + 1));
        }

        CustomData c = customDataPool[index];
        if (c == null)
        {
            c = new CustomData();
            customDataPool[index] = c;
        }
        return c;
    }

    /**
     * Obtains a reusable {@link CustomValue} instance from this container's pre-allocated pool,
     * avoiding heap allocations during custom value chunk scanning.
     *
     * @param index
     *            the row index within the current chunk
     * @return the pooled {@link CustomValue} instance
     */
    public CustomValue getOrCreateCustomValue(final int index)
    {
        if (customValuePool == null)
        {
            customValuePool = new CustomValue[65536];
        }
        else if (index >= customValuePool.length)
        {
            customValuePool = java.util.Arrays.copyOf(customValuePool, Math.max(customValuePool.length * 2, index + 1));
        }

        CustomValue cv = customValuePool[index];
        if (cv == null)
        {
            cv = new CustomValue();
            customValuePool[index] = cv;
        }
        return cv;
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

    /**
     * Clears all accumulated data records and resets min/max timestamps for container reuse.
     */
    public void reset()
    {
        data.clear();
        minimumTime = Long.MAX_VALUE;
        maximumTime = 0;
        droppedLines = 0;
        hasFailedRecords = false;
        typeCode = 0;
    }
}

