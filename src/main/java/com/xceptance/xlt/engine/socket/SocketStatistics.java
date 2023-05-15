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
package com.xceptance.xlt.engine.socket;

/**
 * Class to hold certain socket statistics.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class SocketStatistics
{
    /**
     * The total number of bytes received so far.
     */
    private final int bytesReceived;

    /**
     * The total number of bytes sent so far.
     */
    private final int bytesSent;

    /**
     * The time [ms] taken for connection establishment. Will always be 0 for keep-alive connections.
     */
    private final int connectTime;

    /**
     * The time [ms] taken to look up the IP address for a host name.
     */
    private final int dnsLookupTime;

    /**
     * The time [ms] between the first and the last successful read operation.
     */
    private final int receiveTime;

    /**
     * The time [ms] between the first and the last successful write operation.
     */
    private final int sendTime;

    /**
     * The time [ms] the server needed to process a request.
     */
    private final int serverBusyTime;

    /**
     * The time [ms] taken from connecting a socket to the first bytes sent.
     */
    private final int timeToFirstBytes;

    /**
     * The time [ms] taken from connecting a socket to the last bytes sent.
     */
    private final int timeToLastBytes;

    /**
     * @param dnsLookupTime
     * @param connectTime
     * @param sendTime
     * @param serverBusyTime
     * @param receiveTime
     * @param timeToFirstBytes
     * @param timeToLastBytes
     * @param bytesSent
     * @param bytesReceived
     */
    public SocketStatistics(final int dnsLookupTime, final int connectTime, final int sendTime, final int serverBusyTime,
                            final int receiveTime, final int timeToFirstBytes, final int timeToLastBytes, final int bytesSent,
                            final int bytesReceived)
    {
        this.dnsLookupTime = dnsLookupTime;
        this.connectTime = connectTime;
        this.sendTime = sendTime;
        this.serverBusyTime = serverBusyTime;
        this.receiveTime = receiveTime;
        this.timeToFirstBytes = timeToFirstBytes;
        this.timeToLastBytes = timeToLastBytes;
        this.bytesSent = bytesSent;
        this.bytesReceived = bytesReceived;
    }

    /**
     * Returns the total number of bytes received so far.
     * 
     * @return the bytes received
     */
    public int getBytesReceived()
    {
        return bytesReceived;
    }

    /**
     * Returns the total number of bytes sent so far.
     * 
     * @return the bytes sent
     */
    public int getBytesSent()
    {
        return bytesSent;
    }

    /**
     * Returns the time [ms] taken for connection establishment. Will always be 0 for keep-alive connections.
     * 
     * @return the connect time
     */
    public int getConnectTime()
    {
        return connectTime;
    }

    /**
     * Returns the time [ms] taken for host name resolution. Will always be 0 for keep-alive connections.
     * 
     * @return the look-up time
     */
    public int getDnsLookupTime()
    {
        return dnsLookupTime;
    }

    /**
     * Returns the time [ms] between the first and the last successful read operation.
     * 
     * @return the receive time
     */
    public int getReceiveTime()
    {
        return receiveTime;
    }

    /**
     * Returns the time [ms] between the first and the last successful write operation.
     * 
     * @return the send time
     */
    public int getSendTime()
    {
        return sendTime;
    }

    /**
     * Returns the time [ms] the server needed to process a request. This is the time difference between writing the
     * last byte of the request and reading the first byte of the response. The busy time will be meaningless (0) in
     * case the application does not follow the request/response principle, but also reads data before it writes data.
     * 
     * @return the server busy time
     */
    public int getServerBusyTime()
    {
        return serverBusyTime;
    }

    /**
     * Returns the time [ms] taken from connecting a socket to the first bytes sent.
     * 
     * @return the time until the first bytes are received
     */
    public int getTimeToFirstBytes()
    {
        return timeToFirstBytes;
    }

    /**
     * Returns the time [ms] taken from connecting a socket to the last bytes sent.
     * 
     * @return the time until the last bytes are received
     */
    public int getTimeToLastBytes()
    {
        return timeToLastBytes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("%s{dnsLookupTime=%d, connectTime=%d, sendTime=%d, serverBusyTime=%d, receiveTime=%d, timeToFirstBytes=%d, timeToLastBytes=%d, bytesSent=%d, bytesReceived=%d}",
                             getClass().getSimpleName(), dnsLookupTime, connectTime, sendTime, serverBusyTime, receiveTime,
                             timeToFirstBytes, timeToLastBytes, bytesSent, bytesReceived);
    }
}
