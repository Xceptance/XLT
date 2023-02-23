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

import com.xceptance.xlt.engine.dns.DnsMonitor;
import com.xceptance.xlt.engine.socket.SocketMonitor;

/**
 * A thread-local context object to store any data gathered during the execution of a single request. This includes:
 * <ul>
 * <li>DNS info</li>
 * <li>socket timings</li>
 * </ul>
 */
public final class RequestExecutionContext
{
    /**
     * The thread-local context instance.
     */
    private static final ThreadLocal<RequestExecutionContext> context = ThreadLocal.withInitial(RequestExecutionContext::new);

    /**
     * Returns the thread-local context instance.
     * 
     * @return the context
     */
    public static RequestExecutionContext getCurrent()
    {
        return context.get();
    }

    /**
     * The DNS monitor.
     */
    private final DnsMonitor dnsMonitor = new DnsMonitor();

    /**
     * The socket monitor for the current thread.
     */
    private final SocketMonitor socketMonitor = new SocketMonitor();

    /**
     * The target IP address of the system under test that was used when making the request.
     */
    private String targetAddress;

    /**
     * Private constructor.
     */
    private RequestExecutionContext()
    {
    }

    /**
     * Returns the DNS monitor of this context.
     * 
     * @return the DNS monitor
     */
    public DnsMonitor getDnsMonitor()
    {
        return dnsMonitor;
    }

    /**
     * Returns the socket monitor of this context.
     * 
     * @return the socket monitor
     */
    public SocketMonitor getSocketMonitor()
    {
        return socketMonitor;
    }

    /**
     * Resets the data stored in this context and all contained sub objects. This method should be called right before
     * beginning the execution of a request.
     */
    public void reset()
    {
        dnsMonitor.reset();
        socketMonitor.reset();
        targetAddress = null;
    }

    /**
     * Returns the target IP address of the system under test that was used when making the request.
     * 
     * @return the target IP address
     */
    public String getTargetAddress()
    {
        return targetAddress;
    }

    /**
     * Sets the target IP address of the system under test that was used when making the request.
     * 
     * @param targetAddress
     *            the target IP address
     */
    public void setTargetAddress(final String targetAddress)
    {
        this.targetAddress = targetAddress;
    }
}
