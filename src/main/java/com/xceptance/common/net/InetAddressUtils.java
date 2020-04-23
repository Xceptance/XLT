/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.common.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Utility class for {@link InetAddress} instances.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public final class InetAddressUtils
{
    /**
     * The IP of the local host.
     */
    public static final String LOCALHOST_IP = "127.0.0.1";

    /**
     * The name of the local host.
     */
    public static final String LOCALHOST_NAME = "localhost";

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private InetAddressUtils()
    {
    }

    /**
     * Returns an InetAddress representing the address of the localhost. Every attempt is made to find an address for
     * this host that is not the loopback address. If no other address can be found, the loopback will be returned.
     * 
     * @return the address of localhost
     * @throws UnknownHostException
     *             if there is a problem determining the address
     */
    public static InetAddress getLocalHost() throws UnknownHostException
    {
        InetAddress localHost = null;

        try
        {
            localHost = InetAddress.getLocalHost();
            if (!localHost.isLoopbackAddress())
            {
                return localHost;
            }
        }
        catch (final UnknownHostException e)
        {
            // Ignore, we will try the network interfaces.
            // Note that localHost is null.
        }

        try
        {
            for (final InetAddress address : getAllLocalInetAddresses())
            {
                if (!address.isLoopbackAddress())
                {
                    return address;
                }
            }
        }
        catch (final SocketException e)
        {
            // Ignore, we will return the loop-back address we already have (might be null).
        }

        if (localHost == null)
        {
            throw new UnknownHostException();
        }

        return localHost;
    }

    /**
     * Returns all InetAddresses for this machine, including loopback addresses.
     * 
     * @return all addresses assigned to the local machine
     * @throws SocketException
     *             if there is a problem determining addresses
     */
    public static List<InetAddress> getAllLocalInetAddresses() throws SocketException
    {
        final List<InetAddress> addresses = new ArrayList<InetAddress>();

        // check all network interfaces
        final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements())
        {
            // add all addresses assigned to the current network interface
            final NetworkInterface networkInterface = networkInterfaces.nextElement();
            for (final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses(); inetAddresses.hasMoreElements();)
            {
                addresses.add(inetAddresses.nextElement());
            }
        }

        return addresses;
    }

    /**
     * Checks whether the passed address is one of the local machine's addresses.
     * 
     * @return <code>true</code> if the passed address is one of the local machine's addresses, <code>false</code>
     *         otherwise
     * @throws SocketException
     *             if there is a problem determining addresses
     */
    public static boolean isLocalAddress(final InetAddress inetAddress) throws SocketException
    {
        return getAllLocalInetAddresses().contains(inetAddress);
    }

    /**
     * Checks whether the passed host name or IP address is one of the local machine's addresses.
     * 
     * @return <code>true</code> if the passed address is one of the local machine's addresses, <code>false</code>
     *         otherwise
     * @throws SocketException
     *             if there is a problem determining addresses
     * @throws UnknownHostException
     *             if the given host name cannot be resolved
     */
    public static boolean isLocalAddress(final String hostNameOrAddress) throws SocketException, UnknownHostException
    {
        return isLocalAddress(InetAddress.getByName(hostNameOrAddress));
    }
}
