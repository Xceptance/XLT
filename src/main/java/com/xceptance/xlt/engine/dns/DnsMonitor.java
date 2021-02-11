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
package com.xceptance.xlt.engine.dns;

import org.apache.commons.lang3.ArrayUtils;

import com.xceptance.xlt.engine.socket.SocketMonitor;

/**
 * Class to monitor DNS activities and record results. Make sure you call {@link #reset()} after querying the
 * information!
 * <p>
 * Note: Address resolution time is recorded by {@link SocketMonitor}.
 */
public class DnsMonitor
{
    /**
     * The resolved IP address(es).
     */
    private String[] ipAddresses = ArrayUtils.EMPTY_STRING_ARRAY;

    /**
     * Sets the resolved IP address(es).
     */
    public void dnsLookupDone(String[] addresses)
    {
        ipAddresses = addresses;
    }

    /**
     * Returns the current DNS information.
     */
    public DnsInfo getDnsInfo()
    {
        return new DnsInfo(ipAddresses);
    }

    /**
     * Resets the stored data.
     */
    public void reset()
    {
        ipAddresses = ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
