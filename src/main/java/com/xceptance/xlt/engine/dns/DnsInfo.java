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

import java.util.Arrays;

/**
 * Class to hold all DNS information gathered during the execution of a single request.
 */
public class DnsInfo
{
    /**
     * The resolved IP address(es).
     */
    private final String[] ipAddresses;

    /**
     * Constructor.
     * 
     * @param ipAddresses
     *            the resolved IP addresses
     */
    public DnsInfo(final String[] ipAddresses)
    {
        this.ipAddresses = ipAddresses;
    }

    /**
     * Returns the resolved IP addresses.
     * 
     * @return the IP addresses
     */
    public String[] getIpAddresses()
    {
        return ipAddresses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("%s{ipAddresses=%s}", getClass().getSimpleName(), Arrays.toString(ipAddresses));
    }
}
