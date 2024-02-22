/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test local inet address without mocking just by using it. Asserations are hard because it depends on the machine. So
 * just running it is fine for now.
 * 
 * @author rschwietzke
 */
public class InetAddressUtilsTest
{
    @Test
    public final void testGetAllLocal() throws SocketException
    {
        final List<InetAddress> addr = InetAddressUtils.getAllLocalInetAddresses();

        // should have gotten anything
        Assert.assertTrue(addr.size() > 0);
    }

    @Test
    public final void testGetLocalHost() throws UnknownHostException
    {
        final InetAddress addr = InetAddressUtils.getLocalHost();
        Assert.assertTrue(addr != null && addr.toString().length() > 0);
    }

    @Test
    public final void testIsLocalAddress() throws UnknownHostException, SocketException
    {
        Assert.assertTrue(InetAddressUtils.isLocalAddress(InetAddressUtils.LOCALHOST_NAME));
        Assert.assertTrue(InetAddressUtils.isLocalAddress(InetAddressUtils.LOCALHOST_IP));
        Assert.assertTrue(InetAddressUtils.isLocalAddress(InetAddressUtils.getLocalHost()));
        Assert.assertFalse(InetAddressUtils.isLocalAddress("www.xceptance.de"));
    }
}
