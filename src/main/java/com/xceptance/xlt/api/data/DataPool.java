/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.data;

import java.util.LinkedList;

import com.xceptance.xlt.api.util.XltRandom;

/**
 * This is a utility class that stores a limited amount of data objects and provides a way to reuse it later.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class DataPool<T>
{
    /**
     * How many elements should be kept.
     */
    private volatile int max = 10;

    /**
     * 0 means none at all will expire, 100 means all will expire, rest is a rate in percentage.
     */
    private volatile int expirationRate = 50;

    /**
     * Static structure for data.
     */
    private final LinkedList<T> dataPool = new LinkedList<T>();

    /**
     * Constructor
     * 
     * @param max
     *            amount of data to store, will be set to 1 if argument is equal to or less than zero
     * @param expirationRate
     *            0 means nothing will expire, 100 means all will expire, rest is a rate in percent
     */
    public DataPool(final int max, final int expirationRate)
    {
        this.max = Math.max(1, max);
        this.expirationRate = expirationRate;
    }

    /**
     * Constructor.
     */
    public DataPool()
    {
    }

    /**
     * <p>
     * Get a data row exclusively, return null otherwise. The element will be removed from the data storage and cannot
     * be accessed by others. To reuse, it has to be added again.
     * <p>
     * The first element of the data pool will be returned.
     * 
     * @return a data row or null, if none available
     */
    public synchronized T getDataElement()
    {
        return dataPool.poll();
    }

    /**
     * <p>
     * Stores a data element if the max size of the pool is not yet reached. If the max size is reached, an expiration
     * limit determines, whether or not the data is kept or added.
     * <p>
     * If the pool expiration rate is 100, elements will never be added. If the pool expiration rate is 0, elements will
     * always be added. If the provided expiration rate is bigger than the expiration rate of the pool, the element will
     * be added, otherwise it will not.
     * <p>
     * So if the set expiration rate of the pool is 60 (like 60%) and the provided rate is 61, the data will be added,
     * if the rate is 60 or below that, the data will not be added.
     * <p>
     * If the data pool is full, the first element is removed before new data is added.
     * 
     * @param element
     *            the data to store
     * @param rate
     *            the rate to use (will be compared to the set expiration rate)
     * @return true if data was stored, false otherwise
     */
    public synchronized boolean add(final T element, final int rate)
    {
        if (expirationRate == 100)
        {
            // do not store o
            return false;
        }

        // // if the limit is not yet reached, we store it
        // if (data.size() < max)
        // {
        // data.offer(o);
        //
        // return true;
        // }

        // put the o back, according to expiration rate
        // in case of failure, we will lose it anyway
        if (expirationRate == 0 || rate > expirationRate)
        {
            // remove the first, add a new one at the end
            if (dataPool.size() >= max)
            {
                dataPool.poll();
            }
            dataPool.offer(element);

            return true;
        }

        return false;
    }

    /**
     * Store an element with a random expiration number between 0 and 99, that will be compared to the set expiration
     * number.
     * 
     * @param element
     *            a data element to store
     * @return true if element was stored, false otherwise
     */
    public boolean add(final T element)
    {
        return add(element, XltRandom.nextInt(100));
    }

    /**
     * @return Returns the expireRate.
     */
    public int getExpireRate()
    {
        return expirationRate;
    }

    /**
     * Set a new expiration rate. The current content is not affected.
     * 
     * @param rate
     *            the new rate to use
     */
    public void setExpireRate(final int rate)
    {
        expirationRate = rate;
    }

    /**
     * Returns the current maximum permitted pool size.
     * 
     * @return the max number of elements to be stored.
     */
    public int getMax()
    {
        return max;
    }

    /**
     * Returns the current size of the data pool.
     * 
     * @return the current size of the data pool
     */
    public synchronized int getSize()
    {
        return dataPool.size();
    }

    /**
     * Sets the new max, will shrink the data pool if needed. The elements will be removed according to FIFO.
     * 
     * @param max
     *            the new maximum size of the pool, will be set to 1 if argument is equal to or less than zero
     */
    public synchronized void setMax(final int max)
    {
        this.max = Math.max(1, max);

        while (this.max < dataPool.size())
        {
            dataPool.poll();
        }
    }

    /**
     * Clear the content
     */
    public synchronized void clear()
    {
        dataPool.clear();
    }
}
