/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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

import com.google.common.net.InetAddresses;
import com.xceptance.xlt.engine.RequestExecutionContext;
import com.xceptance.xlt.engine.XltEngine;
import junitparams.JUnitParamsRunner;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.util.XltProperties;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;

@RunWith(JUnitParamsRunner.class)
public class XltDnsResolverTest
{
    @After
    public void tearDown()
    {
        XltEngine.reset();
    }

    @Test
    public void resolve_WithOverride_RemoveIPv4Addresses() throws UnknownHostException
    {
        final String hostname = "example.org";
        final String ip1 = "192.0.2.100";
        final String ip2 = "2001:db8:1111:2222:aaaa:bbbb:1a2b:cd34";
        final String ip3 = "2001:db8:0:0:0:0:1:0";
        final String ip4 = "203.0.113.200";

        setOverrideProperty(hostname, String.join(",", ip1, ip2, ip3, ip4));
        setXltProperty(XltDnsResolver.PROP_IGNORE_IPV4_ADDRESSES, "true");

        final InetAddress[] addresses = new XltDnsResolver().resolve(hostname);
        Assert.assertNotNull(addresses);
        Assert.assertEquals(2, addresses.length);
        Assert.assertEquals(ip2, addresses[0].getHostAddress());
        Assert.assertEquals(ip3, addresses[1].getHostAddress());
    }

    @Test
    public void resolve_WithOverride_RemoveIPv6Addresses() throws UnknownHostException
    {
        final String hostname = "example.org";
        final String ip1 = "192.0.2.100";
        final String ip2 = "2001:db8:1111:2222:aaaa:bbbb:1a2b:cd34";
        final String ip3 = "2001:db8:0:0:0:0:1:0";
        final String ip4 = "203.0.113.200";

        setOverrideProperty(hostname, String.join(",", ip1, ip2, ip3, ip4));
        setXltProperty(XltDnsResolver.PROP_IGNORE_IPV6_ADDRESSES, "true");

        final InetAddress[] addresses = new XltDnsResolver().resolve(hostname);
        Assert.assertNotNull(addresses);
        Assert.assertEquals(2, addresses.length);
        Assert.assertEquals(ip1, addresses[0].getHostAddress());
        Assert.assertEquals(ip4, addresses[1].getHostAddress());
    }

    @Test(expected = UnknownHostException.class)
    public void resolve_WithOverride_RemoveAllIpAddresses() throws UnknownHostException
    {
        final String hostname = "example.org";
        final String ip1 = "192.0.2.100";
        final String ip2 = "2001:db8:1:2:3:4:5:6";

        setOverrideProperty(hostname, String.join(",", ip1, ip2));
        setXltProperty(XltDnsResolver.PROP_IGNORE_IPV4_ADDRESSES, "true");
        setXltProperty(XltDnsResolver.PROP_IGNORE_IPV6_ADDRESSES, "true");

        new XltDnsResolver().resolve(hostname);
    }

    @Test
    public void resolve_WithOverride_PickOneAddressRandomly() throws UnknownHostException
    {
        final String hostname = "example.org";
        final String ip1 = "192.0.2.100";
        final String ip2 = "2001:db8:1:2:3:4:5:6";
        final String ip3 = "203.0.113.200";

        setOverrideProperty(hostname, String.join(",", ip1, ip2, ip3));
        setXltProperty(XltDnsResolver.PROP_PICK_ONE_ADDRESS_RANDOMLY, "true");

        final InetAddress[] addresses = new XltDnsResolver().resolve(hostname);

        Assert.assertNotNull(addresses);
        Assert.assertEquals(1, addresses.length);
        MatcherAssert.assertThat(addresses[0].getHostAddress(), anyOf(is(ip1), is(ip2), is(ip3)));
    }

    @Test
    public void resolve_WithOverride_RecordAddresses() throws UnknownHostException
    {
        final String hostname = "example.org";
        final String ip = "192.0.2.100";

        setOverrideProperty(hostname, ip);
        setXltProperty(XltDnsResolver.PROP_RECORD_ADDRESSES, "true");

        final DnsMonitor dnsMonitor = RequestExecutionContext.getCurrent().getDnsMonitor();
        Assert.assertEquals(0, dnsMonitor.getDnsInfo().getIpAddresses().length);

        final InetAddress[] addresses = new XltDnsResolver().resolve(hostname);
        Assert.assertNotNull(addresses);
        Assert.assertEquals(1, addresses.length);
        Assert.assertEquals(ip, addresses[0].getHostAddress());
        Assert.assertArrayEquals(new String[]
            {
                ip
            }, dnsMonitor.getDnsInfo().getIpAddresses());
    }

    @Test
    public void resolve_WithOverride_DoNotRecordAddresses() throws UnknownHostException
    {
        final String hostname = "example.org";
        final String ip = "192.0.2.100";

        setOverrideProperty(hostname, ip);
        setXltProperty(XltDnsResolver.PROP_RECORD_ADDRESSES, "false");

        final DnsMonitor dnsMonitor = RequestExecutionContext.getCurrent().getDnsMonitor();
        Assert.assertEquals(0, dnsMonitor.getDnsInfo().getIpAddresses().length);

        final InetAddress[] addresses = new XltDnsResolver().resolve(hostname);
        Assert.assertNotNull(addresses);
        Assert.assertEquals(1, addresses.length);
        Assert.assertEquals(ip, addresses[0].getHostAddress());
        Assert.assertEquals(0, dnsMonitor.getDnsInfo().getIpAddresses().length);
    }

    @Test
    public void resolve_CacheAddresses() throws UnknownHostException
    {
        final String hostname1 = "example.org";
        final InetAddress[] addresses1 = new InetAddress[]
            {
                InetAddresses.forString("192.0.2.100")
            };
        final String hostname2 = "host.with-dash.test";
        final InetAddress[] addresses2 = new InetAddress[]
            {
                InetAddresses.forString("2001:db8:1111:2222:aaaa:bbbb:1a2b:cd34")
            };

        setXltProperty(XltDnsResolver.PROP_CACHE_ADDRESSES, "true");

        // mock the actual host name resolving
        final XltDnsResolver resolver = Mockito.spy(new XltDnsResolver());
        Mockito.doReturn(addresses1).when(resolver).doResolve(Mockito.eq(hostname1), Mockito.any());
        Mockito.doReturn(addresses2).when(resolver).doResolve(Mockito.eq(hostname2), Mockito.any());

        // resolve first host name; IP addresses are resolved normally
        Assert.assertArrayEquals(addresses1, resolver.resolve(hostname1));
        Mockito.verify(resolver, Mockito.times(1)).doResolve(Mockito.eq(hostname1), Mockito.any());
        Mockito.verify(resolver, Mockito.never()).doResolve(Mockito.eq(hostname2), Mockito.any());
        Mockito.verify(resolver, Mockito.times(1)).doResolve(Mockito.any(), Mockito.any());
        Mockito.clearInvocations(resolver);

        // resolve first host name again; IP addresses are returned from cache instead of resolving them again
        Assert.assertArrayEquals(addresses1, resolver.resolve(hostname1));
        Mockito.verify(resolver, Mockito.never()).doResolve(Mockito.any(), Mockito.any());

        // resolve second host name; IP addresses are resolved normally
        Assert.assertArrayEquals(addresses2, resolver.resolve(hostname2));
        Mockito.verify(resolver, Mockito.never()).doResolve(Mockito.eq(hostname1), Mockito.any());
        Mockito.verify(resolver, Mockito.times(1)).doResolve(Mockito.eq(hostname2), Mockito.any());
        Mockito.verify(resolver, Mockito.times(1)).doResolve(Mockito.any(), Mockito.any());
    }

    @Test(expected = UnknownHostException.class)
    public void resolve_NoAddressesFound() throws UnknownHostException
    {
        final String hostname = "example.org";

        // mock the actual IP resolving
        final XltDnsResolver resolver = Mockito.spy(new XltDnsResolver());
        Mockito.doThrow(new UnknownHostException()).when(resolver).doResolve(Mockito.eq(hostname), Mockito.any());

        resolver.resolve(hostname);
    }

    @Test
    public void doResolve() throws UnknownHostException
    {
        final String hostname = "example.org";
        final InetAddress[] addresses =
            {
                InetAddresses.forString("192.0.2.100")
            };

        final HostNameResolver mockResolver = Mockito.mock(HostNameResolver.class);
        Mockito.doReturn(addresses).when(mockResolver).resolve(hostname);

        Assert.assertArrayEquals(addresses, new XltDnsResolver().doResolve(hostname, mockResolver));
    }

    @Test(expected = UnknownHostException.class)
    public void doResolve_NoAddressesFound() throws UnknownHostException
    {
        final String hostname = "example.org";

        final HostNameResolver mockResolver = Mockito.mock(HostNameResolver.class);
        Mockito.doThrow(new UnknownHostException()).when(mockResolver).resolve(hostname);

        new XltDnsResolver().doResolve(hostname, mockResolver);
    }

    private void setXltProperty(final String key, final String value)
    {
        XltProperties.getInstance().setProperty(key, value);
    }

    private void setOverrideProperty(final String hostname, final String ipString)
    {
        setXltProperty(DnsOverrideResolver.PROP_DNS_OVERRIDE_PREFIX + "." + hostname, ipString);
    }
}
