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
package com.xceptance.xlt.agent;

import java.util.List;

import com.xceptance.common.lang.ParseNumbers;
import com.xceptance.xlt.api.engine.AbstractData;
import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * The {@link JvmResourceUsageData} class holds resource usage statistics for the current agent JVM. Currently, this
 * includes:
 * <ul>
 * <li>CPU usage</li>
 * <li>process memory usage</li>
 * <li>JVM heap memory usage</li>
 * <li>number of threads</li>
 * <li>GC information</li>
 * </ul>
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class JvmResourceUsageData extends AbstractData
{
    private int blockedThreadCount;

    private long committedMemorySize;

    private double cpuUsage;

    private long fullGcCount;

    private int fullGcCountDiff;

    private double fullGcCpuUsage;

    private long fullGcTime;

    private int fullGcTimeDiff;

    private double heapUsage;

    private double memoryUsage;

    private long minorGcCount;

    private int minorGcCountDiff;

    private double minorGcCpuUsage;

    private long minorGcTime;

    private int minorGcTimeDiff;

    private int runnableThreadCount;

    private double totalCpuUsage;

    private long totalHeapSize;

    private long usedHeapSize;

    private int waitingThreadCount;

    /**
     * Default constructor.
     */
    public JvmResourceUsageData()
    {
        super('J');
    }

    /**
     * Constructor.
     *
     * @param name
     *            name to use
     */
    public JvmResourceUsageData(final String name)
    {
        super(name, 'J');
    }

    /**
     * Returns the value of the 'blockedThreadCount' attribute.
     *
     * @return the value of blockedThreadCount
     */
    public int getBlockedThreadCount()
    {
        return blockedThreadCount;
    }

    /**
     * Returns the value of the 'committedMemorySize' attribute.
     *
     * @return the value of committedMemorySize
     */
    public long getCommittedMemorySize()
    {
        return committedMemorySize;
    }

    /**
     * Returns the value of the 'cpuUsage' attribute.
     *
     * @return the value of cpuUsage
     */
    public double getCpuUsage()
    {
        return cpuUsage;
    }

    /**
     * Returns the value of the 'fullGcCount' attribute.
     *
     * @return the value of fullGcCount
     */
    public long getFullGcCount()
    {
        return fullGcCount;
    }

    /**
     * Returns the value of the 'fullGcCountDiff' attribute.
     *
     * @return the value of fullGcCountDiff
     */
    public int getFullGcCountDiff()
    {
        return fullGcCountDiff;
    }

    /**
     * Returns the value of the 'fullGcCpuUsage' attribute.
     *
     * @return the value of fullGcCpuUsage
     */
    public double getFullGcCpuUsage()
    {
        return fullGcCpuUsage;
    }

    /**
     * Returns the value of the 'fullGcTime' attribute.
     *
     * @return the value of fullGcTime
     */
    public long getFullGcTime()
    {
        return fullGcTime;
    }

    /**
     * Returns the value of the 'fullGcTimeDiff' attribute.
     *
     * @return the value of fullGcTimeDiff
     */
    public int getFullGcTimeDiff()
    {
        return fullGcTimeDiff;
    }

    /**
     * Returns the value of the 'heapUsage' attribute.
     *
     * @return the value of heapUsage
     */
    public double getHeapUsage()
    {
        return heapUsage;
    }

    /**
     * Returns the value of the 'memoryUsage' attribute.
     *
     * @return the value of memoryUsage
     */
    public double getMemoryUsage()
    {
        return memoryUsage;
    }

    /**
     * Returns the value of the 'minorGcCount' attribute.
     *
     * @return the value of minorGcCount
     */
    public long getMinorGcCount()
    {
        return minorGcCount;
    }

    /**
     * Returns the value of the 'minorGcCountDiff' attribute.
     *
     * @return the value of minorGcCountDiff
     */
    public int getMinorGcCountDiff()
    {
        return minorGcCountDiff;
    }

    /**
     * Returns the value of the 'minorGcCpuUsage' attribute.
     *
     * @return the value of minorGcCpuUsage
     */
    public double getMinorGcCpuUsage()
    {
        return minorGcCpuUsage;
    }

    /**
     * Returns the value of the 'minorGcTime' attribute.
     *
     * @return the value of minorGcTime
     */
    public long getMinorGcTime()
    {
        return minorGcTime;
    }

    /**
     * Returns the value of the 'minorGcTimeDiff' attribute.
     *
     * @return the value of minorGcTimeDiff
     */
    public int getMinorGcTimeDiff()
    {
        return minorGcTimeDiff;
    }

    /**
     * Returns the value of the 'runnableThreadCount' attribute.
     *
     * @return the value of runnableThreadCount
     */
    public int getRunnableThreadCount()
    {
        return runnableThreadCount;
    }

    /**
     * Returns the value of the 'totalCpuUsage' attribute.
     *
     * @return the value of totalCpuUsage
     */
    public double getTotalCpuUsage()
    {
        return totalCpuUsage;
    }

    /**
     * Returns the value of the 'totalHeapSize' attribute.
     *
     * @return the value of totalHeapSize
     */
    public long getTotalHeapSize()
    {
        return totalHeapSize;
    }

    /**
     * Returns the value of the 'usedHeapSize' attribute.
     *
     * @return the value of usedHeapSize
     */
    public long getUsedHeapSize()
    {
        return usedHeapSize;
    }

    /**
     * Returns the value of the 'waitingThreadCount' attribute.
     *
     * @return the value of waitingThreadCount
     */
    public int getWaitingThreadCount()
    {
        return waitingThreadCount;
    }

    /**
     * Sets the new value of the 'blockedThreadCount' attribute.
     *
     * @param blockedThreadCount
     *            the new blockedThreadCount value
     */
    public void setBlockedThreadCount(final int blockedThreadCount)
    {
        this.blockedThreadCount = blockedThreadCount;
    }

    /**
     * Sets the new value of the 'committedMemorySize' attribute.
     *
     * @param committedMemorySize
     *            the new committedMemorySize value
     */
    public void setCommittedMemorySize(final long committedMemorySize)
    {
        this.committedMemorySize = committedMemorySize;
    }

    /**
     * Sets the new value of the 'cpuUsage' attribute.
     *
     * @param cpuUsage
     *            the new cpuUsage value
     */
    public void setCpuUsage(final double cpuUsage)
    {
        this.cpuUsage = cpuUsage;
    }

    /**
     * Sets the new value of the 'fullGcCount' attribute.
     *
     * @param fullGcCount
     *            the new fullGcCount value
     */
    public void setFullGcCount(final long fullGcCount)
    {
        this.fullGcCount = fullGcCount;
    }

    /**
     * Sets the new value of the 'fullGcCountDiff' attribute.
     *
     * @param fullGcCountDiff
     *            the new fullGcCountDiff value
     */
    public void setFullGcCountDiff(final int fullGcCountDiff)
    {
        this.fullGcCountDiff = fullGcCountDiff;
    }

    /**
     * Sets the new value of the 'fullGcCpuUsage' attribute.
     *
     * @param fullGcCpuUsage
     *            the new fullGcCpuUsage value
     */
    public void setFullGcCpuUsage(final double fullGcCpuUsage)
    {
        this.fullGcCpuUsage = fullGcCpuUsage;
    }

    /**
     * Sets the new value of the 'fullGcTime' attribute.
     *
     * @param fullGcTime
     *            the new fullGcTime value
     */
    public void setFullGcTime(final long fullGcTime)
    {
        this.fullGcTime = fullGcTime;
    }

    /**
     * Sets the new value of the 'fullGcTimeDiff' attribute.
     *
     * @param fullGcTimeDiff
     *            the new fullGcTimeDiff value
     */
    public void setFullGcTimeDiff(final int fullGcTimeDiff)
    {
        this.fullGcTimeDiff = fullGcTimeDiff;
    }

    /**
     * Sets the new value of the 'heapUsage' attribute.
     *
     * @param heapUsage
     *            the new heapUsage value
     */
    public void setHeapUsage(final double heapUsage)
    {
        this.heapUsage = heapUsage;
    }

    /**
     * Sets the new value of the 'memoryUsage' attribute.
     *
     * @param memoryUsage
     *            the new memoryUsage value
     */
    public void setMemoryUsage(final double memoryUsage)
    {
        this.memoryUsage = memoryUsage;
    }

    /**
     * Sets the new value of the 'minorGcCount' attribute.
     *
     * @param minorGcCount
     *            the new minorGcCount value
     */
    public void setMinorGcCount(final long minorGcCount)
    {
        this.minorGcCount = minorGcCount;
    }

    /**
     * Sets the new value of the 'minorGcCountDiff' attribute.
     *
     * @param minorGcCountDiff
     *            the new minorGcCountDiff value
     */
    public void setMinorGcCountDiff(final int minorGcCountDiff)
    {
        this.minorGcCountDiff = minorGcCountDiff;
    }

    /**
     * Sets the new value of the 'minorGcCpuUsage' attribute.
     *
     * @param minorGcCpuUsage
     *            the new minorGcCpuUsage value
     */
    public void setMinorGcCpuUsage(final double minorGcCpuUsage)
    {
        this.minorGcCpuUsage = minorGcCpuUsage;
    }

    /**
     * Sets the new value of the 'minorGcTime' attribute.
     *
     * @param minorGcTime
     *            the new minorGcTime value
     */
    public void setMinorGcTime(final long minorGcTime)
    {
        this.minorGcTime = minorGcTime;
    }

    /**
     * Sets the new value of the 'minorGcTimeDiff' attribute.
     *
     * @param minorGcTimeDiff
     *            the new minorGcTimeDiff value
     */
    public void setMinorGcTimeDiff(final int minorGcTimeDiff)
    {
        this.minorGcTimeDiff = minorGcTimeDiff;
    }

    /**
     * Sets the new value of the 'runnableThreadCount' attribute.
     *
     * @param runnableThreadCount
     *            the new runnableThreadCount value
     */
    public void setRunnableThreadCount(final int runnableThreadCount)
    {
        this.runnableThreadCount = runnableThreadCount;
    }

    /**
     * Sets the new value of the 'totalCpuUsage' attribute.
     *
     * @param cpuUsage
     *            the new totalCpuUsage value
     */
    public void setTotalCpuUsage(double totalCpuUsage)
    {
        this.totalCpuUsage = totalCpuUsage;
    }

    /**
     * Sets the new value of the 'totalHeapSize' attribute.
     *
     * @param totalHeapSize
     *            the new totalHeapSize value
     */
    public void setTotalHeapSize(final long totalHeapSize)
    {
        this.totalHeapSize = totalHeapSize;
    }

    /**
     * Sets the new value of the 'usedHeapSize' attribute.
     *
     * @param usedHeapSize
     *            the new usedHeapSize value
     */
    public void setUsedHeapSize(final long usedHeapSize)
    {
        this.usedHeapSize = usedHeapSize;
    }

    /**
     * Sets the new value of the 'waitingThreadCount' attribute.
     *
     * @param waitingThreadCount
     *            the new waitingThreadCount value
     */
    public void setWaitingThreadCount(final int waitingThreadCount)
    {
        this.waitingThreadCount = waitingThreadCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAllValues()
    {
        final List<String> fields = super.getAllValues();

        fields.add(Double.toString(cpuUsage));
        fields.add(Long.toString(committedMemorySize));
        fields.add(Double.toString(memoryUsage));
        fields.add(Long.toString(usedHeapSize));
        fields.add(Long.toString(totalHeapSize));
        fields.add(Double.toString(heapUsage));

        fields.add(Integer.toString(runnableThreadCount));
        fields.add(Integer.toString(blockedThreadCount));
        fields.add(Integer.toString(waitingThreadCount));

        fields.add(Long.toString(minorGcCount));
        fields.add(Long.toString(minorGcTime));
        fields.add(Double.toString(minorGcCpuUsage));
        fields.add(Long.toString(fullGcCount));
        fields.add(Long.toString(fullGcTime));
        fields.add(Double.toString(fullGcCpuUsage));
        fields.add(Integer.toString(minorGcTimeDiff));
        fields.add(Integer.toString(fullGcTimeDiff));
        fields.add(Integer.toString(minorGcCountDiff));
        fields.add(Integer.toString(fullGcCountDiff));

        fields.add(Double.toString(totalCpuUsage));

        return fields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initRemainingValues(final List<XltCharBuffer> values)
    {
        // read and check the values
        cpuUsage = Double.parseDouble(values.get(3).toString());
        committedMemorySize = Long.parseLong(values.get(4).toString());
        memoryUsage = Double.parseDouble(values.get(5).toString());
        usedHeapSize = Long.parseLong(values.get(6).toString());
        totalHeapSize = Long.parseLong(values.get(7).toString());
        heapUsage = Double.parseDouble(values.get(8).toString());

        /**
         * Modern stuff first, legacy second if needed at all
         */
        if (values.size() >= 23)
        {
            runnableThreadCount = ParseNumbers.parseInt(values.get(9));
            blockedThreadCount = ParseNumbers.parseInt(values.get(10));
            waitingThreadCount = ParseNumbers.parseInt(values.get(11));
            minorGcCount = ParseNumbers.parseLong(values.get(12));
            minorGcTime = ParseNumbers.parseLong(values.get(13));
            minorGcCpuUsage = Double.parseDouble(values.get(14).toString());
            fullGcCount = ParseNumbers.parseLong(values.get(15));
            fullGcTime = ParseNumbers.parseLong(values.get(16));
            fullGcCpuUsage = Double.parseDouble(values.get(17).toString());
            minorGcTimeDiff = ParseNumbers.parseInt(values.get(18));
            fullGcTimeDiff = ParseNumbers.parseInt(values.get(19));
            minorGcCountDiff = ParseNumbers.parseInt(values.get(20));
            fullGcCountDiff = ParseNumbers.parseInt(values.get(21));
            totalCpuUsage = Double.parseDouble(values.get(22).toString());
        }
        else
        {
            // allow older reports to be regenerated
            if (values.size() >= 12)
            {
                runnableThreadCount = Integer.parseInt(values.get(9).toString());
                blockedThreadCount = Integer.parseInt(values.get(10).toString());
                waitingThreadCount = Integer.parseInt(values.get(11).toString());
            }

            if (values.size() >= 18)
            {
                minorGcCount = ParseNumbers.parseLong(values.get(12));
                minorGcTime = ParseNumbers.parseLong(values.get(13));
                minorGcCpuUsage = Double.parseDouble(values.get(14).toString());
                fullGcCount = ParseNumbers.parseLong(values.get(15));
                fullGcTime = ParseNumbers.parseLong(values.get(16));
                fullGcCpuUsage = Double.parseDouble(values.get(17).toString());
            }

            if (values.size() >= 20)
            {
                minorGcTimeDiff = ParseNumbers.parseInt(values.get(18));
                fullGcTimeDiff = ParseNumbers.parseInt(values.get(19));
            }

            if (values.size() >= 22)
            {
                minorGcCountDiff = ParseNumbers.parseInt(values.get(20));
                fullGcCountDiff = ParseNumbers.parseInt(values.get(21));
            }

            if (values.size() >= 23)
            {
                totalCpuUsage = Double.parseDouble(values.get(22).toString());
            }
        }
    }
}
