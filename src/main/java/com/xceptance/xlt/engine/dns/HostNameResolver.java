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
package com.xceptance.xlt.engine.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The interface of a host name resolver.
 */
interface HostNameResolver
{
    /**
     * Determines all IP addresses for the given host name.
     *
     * @param host
     *            the host name to look up
     * @return all known IP addresses
     * @exception UnknownHostException
     *                if the host name does not have any addresses
     */
    public InetAddress[] resolve(final String host) throws UnknownHostException;
}
