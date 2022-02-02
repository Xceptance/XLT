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
package com.xceptance.xlt.engine.socket;

import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * Class to monitor and measure socket activities. Make sure you call {@link #reset()} after querying the statistics!
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class SocketMonitor
{
    /**
     * The total number of bytes sent so far.
     */
    private int bytesReceived;

    /**
     * The total number of bytes sent so far.
     */
    private int bytesSent;

    /**
     * The time-stamp when connection establishment is finished.
     */
    private long connectEndTime;

    /**
     * The time-stamp when starting connection establishment.
     */
    private long connectStartTime;

    /**
     * The time-stamp when DNS look-up is finished.
     */
    private long dnsLookupEndTime;

    /**
     * The time-stamp when starting DNS look-up.
     */
    private long dnsLookupStartTime;

    /**
     * The time-stamp of the first successful read operation.
     */
    private long firstBytesReceivedTime;

    /**
     * The time-stamp of the first successful write operation.
     */
    private long firstBytesSentTime;

    /**
     * The time-stamp of the last successful read operation.
     */
    private long lastBytesReceivedTime;

    /**
     * The time-stamp of the last successful write operation.
     */
    private long lastBytesSentTime;

    /**
     * Sets the time when a socket is connected.
     */
    public void connected()
    {
        connectEndTime = TimerUtils.getTime();
    }

    /**
     * Sets the time when the socket is about to be connected.
     */
    public void connectingStarted()
    {
        connectStartTime = connectEndTime = TimerUtils.getTime();
    }

    /**
     * Sets the time when looking up the IP address for a host name is finished.
     */
    public void dnsLookupDone()
    {
        dnsLookupEndTime = TimerUtils.getTime();
    }

    /**
     * Sets the time when looking up the IP address for a host name is about to begin.
     */
    public void dnsLookupStarted()
    {
        dnsLookupStartTime = dnsLookupEndTime = TimerUtils.getTime();
    }

    /**
     * Returns the socket statistics as calculated so far.
     */
    public SocketStatistics getSocketStatistics()
    {
        // calculate statistics
        final int dnsLookupTime = (int) (dnsLookupEndTime - dnsLookupStartTime);
        final int connectTime = (int) (connectEndTime - connectStartTime);
        final int sendTime = (int) (lastBytesSentTime - firstBytesSentTime);
        final int receiveTime = (int) (lastBytesReceivedTime - firstBytesReceivedTime);

        // server-busy time
        final int serverBusyTime;
        final long referenceTime = getMaxTime(connectEndTime, lastBytesSentTime);

        if (firstBytesReceivedTime > 0 && firstBytesReceivedTime > referenceTime)
        {
            serverBusyTime = (int) (firstBytesReceivedTime - referenceTime);
        }
        else
        {
            // nothing received or overlapping send/receive phases
            serverBusyTime = 0;
        }

        // time-to-first/last
        final int timeToFirst;
        final int timeToLast;

        if (firstBytesReceivedTime == 0)
        {
            timeToFirst = timeToLast = 0;
        }
        else
        {
            final long start = getMinTime(dnsLookupStartTime, connectStartTime, firstBytesSentTime, firstBytesReceivedTime);

            timeToFirst = (int) (firstBytesReceivedTime - start);
            timeToLast = (int) (lastBytesReceivedTime - start);
        }

        return new SocketStatistics(dnsLookupTime, connectTime, sendTime, serverBusyTime, receiveTime, timeToFirst, timeToLast, bytesSent,
                                    bytesReceived);
    }

    /**
     * Adds the given amount of bytes to the total number of bytes read so far.
     * 
     * @param bytes
     *            the number of bytes read
     */
    public void read(final int bytes)
    {
        lastBytesReceivedTime = TimerUtils.getTime();
        bytesReceived += bytes;

        if (firstBytesReceivedTime == 0)
        {
            firstBytesReceivedTime = lastBytesReceivedTime;
        }
    }

    /**
     * Resets the statistics.
     */
    public void reset()
    {
        bytesSent = 0;
        bytesReceived = 0;

        dnsLookupStartTime = 0;
        dnsLookupEndTime = 0;

        connectStartTime = 0;
        connectEndTime = 0;

        firstBytesSentTime = 0;
        lastBytesSentTime = 0;

        firstBytesReceivedTime = 0;
        lastBytesReceivedTime = 0;
    }

    /**
     * Adds the given amount of bytes to the total number of bytes written so far.
     * 
     * @param bytes
     *            the number of bytes written
     */
    public void wrote(final int bytes)
    {
        lastBytesSentTime = TimerUtils.getTime();
        bytesSent += bytes;

        if (firstBytesSentTime == 0)
        {
            firstBytesSentTime = lastBytesSentTime;
        }
    }

    /**
     * Determines the largest/latest timestamp of the given ones. If a timestamp is not valid (value 0) it will be
     * excluded from the check.
     * 
     * @param timestamps
     *            the timestamps to check
     * @return the largest timestamp, or 0 if none of the given timestamps were valid
     */
    private static long getMaxTime(long... timestamps)
    {
        long result = 0;

        for (long timestamp : timestamps)
        {
            // check if the timestamp has a valid value
            if (timestamp > 0)
            {
                // check if the timestamp is smaller
                if (result == 0 || timestamp > result)
                {
                    result = timestamp;
                }
            }
        }

        return result;
    }

    /**
     * Determines the smallest/earliest timestamp of the given ones. If a timestamp is not valid (value 0) it will be
     * excluded from the check.
     * 
     * @param timestamps
     *            the timestamps to check
     * @return the smallest timestamp, or 0 if none of the given timestamps were valid
     */
    private static long getMinTime(long... timestamps)
    {
        long result = 0;

        for (long timestamp : timestamps)
        {
            // check if the timestamp has a valid value
            if (timestamp > 0)
            {
                // check if the timestamp is smaller
                if (result == 0 || timestamp < result)
                {
                    result = timestamp;
                }
            }
        }

        return result;
    }
}
