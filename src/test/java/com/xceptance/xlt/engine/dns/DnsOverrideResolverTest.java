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

import com.google.common.net.InetAddresses;
import com.xceptance.common.util.ParseUtilsTest;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.XltEngine;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import util.JUnitParamsUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

@RunWith(JUnitParamsRunner.class)
public class DnsOverrideResolverTest
{
    private HostNameResolver fallbackResolver;

    @Before
    public void setUp() throws UnknownHostException
    {
        fallbackResolver = Mockito.mock(HostNameResolver.class);
    }

    @After
    public void cleanUp()
    {
        XltEngine.reset();
    }

    @Test
    public void resolve_NoOverrides() throws UnknownHostException
    {
        final String hostname = "example.org";
        final InetAddress[] fallbackAddresses =
            {
                InetAddresses.forString("192.0.2.255")
            };

        Mockito.doReturn(fallbackAddresses).when(fallbackResolver).resolve(hostname);

        // no overrides, so host name is resolved using the fallback resolver
        final InetAddress[] addresses = new DnsOverrideResolver(fallbackResolver).resolve(hostname);
        Assert.assertNotNull(addresses);
        Assert.assertArrayEquals(fallbackAddresses, addresses);

        Mockito.verify(fallbackResolver, Mockito.times(1)).resolve(hostname);
        Mockito.verify(fallbackResolver, Mockito.times(1)).resolve(Mockito.any());
    }

    @Test
    public void resolve_ResolveNull() throws UnknownHostException
    {
        final InetAddress[] fallbackAddresses =
            {
                InetAddresses.forString("192.0.2.255")
            };

        Mockito.doReturn(fallbackAddresses).when(fallbackResolver).resolve(null);

        // no overrides, so "null" is passed on to the fallback resolver
        final InetAddress[] addresses = new DnsOverrideResolver(fallbackResolver).resolve(null);
        Assert.assertNotNull(addresses);
        Assert.assertArrayEquals(fallbackAddresses, addresses);

        Mockito.verify(fallbackResolver, Mockito.times(1)).resolve(null);
        Mockito.verify(fallbackResolver, Mockito.times(1)).resolve(Mockito.any());
    }

    @Test
    @Parameters(source = JUnitParamsUtils.BlankStringParamProvider.class)
    public void resolve_BlankOverride(final String blankString) throws UnknownHostException
    {
        final String hostname = "example.org";
        final InetAddress[] fallbackAddresses =
            {
                InetAddresses.forString("192.0.2.255")
            };

        setOverrideProperty(hostname, blankString);
        Mockito.doReturn(fallbackAddresses).when(fallbackResolver).resolve(hostname);

        // overrides with empty IP String are ignored, so host name is resolved using the fallback resolver
        final InetAddress[] addresses = new DnsOverrideResolver(fallbackResolver).resolve(hostname);
        Assert.assertNotNull(addresses);
        Assert.assertArrayEquals(fallbackAddresses, addresses);

        Mockito.verify(fallbackResolver, Mockito.times(1)).resolve(hostname);
        Mockito.verify(fallbackResolver, Mockito.times(1)).resolve(Mockito.any());
    }

    @Test
    @Parameters(value =
        {
            "192.0.2.100", //
            "2001:db8:1111:2222:aaaa:bbbb:1a2b:cd34" //
    })
    public void resolve_OverrideWithSingleIp(final String ip) throws UnknownHostException
    {
        final String hostname = "example.org";
        setOverrideProperty(hostname, ip);

        final InetAddress[] addresses = new DnsOverrideResolver(fallbackResolver).resolve(hostname);
        Assert.assertNotNull(addresses);
        Assert.assertEquals(1, addresses.length);
        Assert.assertEquals(ip, addresses[0].getHostAddress());

        Mockito.verify(fallbackResolver, Mockito.never()).resolve(Mockito.any());
    }

    @Test
    public void resolve_OverrideWithMultipleIps() throws UnknownHostException
    {
        final String hostname = "example.org";
        final String ip1 = "192.0.2.100";
        final String ip2 = "2001:db8:1111:2222:aaaa:bbbb:1a2b:cd34";
        setOverrideProperty(hostname, String.join(",", ip1, ip2));

        InetAddress[] addresses = new DnsOverrideResolver(fallbackResolver).resolve(hostname);
        Assert.assertNotNull(addresses);
        Assert.assertEquals(2, addresses.length);
        Assert.assertEquals(ip1, addresses[0].getHostAddress());
        Assert.assertEquals(ip2, addresses[1].getHostAddress());

        Mockito.verify(fallbackResolver, Mockito.never()).resolve(Mockito.any());
    }

    @Test
    @Parameters(value =
        {
            "example.org | host.with-dash.test", //
            "example.org | example.com", //
            "example.org | EXAMPLE.ORG" // same host name in uppercase and lowercase
    })
    public void resolve_MultipleOverrides(final String hostname1, final String hostname2) throws UnknownHostException
    {
        final String ip1a = "192.0.2.100";
        final String ip1b = "2001:db8:1:2:3:4:5:6";

        final String ip2a = "2001:db8:1111:2222:aaaa:bbbb:1a2b:cd34";
        final String ip2b = "203.0.113.200";
        final String ip2c = "2001:db8:0:0:0:0:1:0";

        setOverrideProperty(hostname1, String.join(",", ip1a, ip1b));
        // add a few extra delimiters to the second override to verify this is read correctly as well
        setOverrideProperty(hostname2, String.format(" ,; \t %s , ;\t; %s ,,, %s \t, ;; ", ip2a, ip2b, ip2c));

        final DnsOverrideResolver resolver = new DnsOverrideResolver(fallbackResolver);

        InetAddress[] addresses = resolver.resolve(hostname1);
        Assert.assertNotNull(addresses);
        Assert.assertEquals(2, addresses.length);
        Assert.assertEquals(ip1a, addresses[0].getHostAddress());
        Assert.assertEquals(ip1b, addresses[1].getHostAddress());

        addresses = resolver.resolve(hostname2);
        Assert.assertNotNull(addresses);
        Assert.assertEquals(3, addresses.length);
        Assert.assertEquals(ip2a, addresses[0].getHostAddress());
        Assert.assertEquals(ip2b, addresses[1].getHostAddress());
        Assert.assertEquals(ip2c, addresses[2].getHostAddress());

        Mockito.verify(fallbackResolver, Mockito.never()).resolve(Mockito.any());
    }

    @Test
    @Parameters(value =
        {
            "test", //
            "www.example.org", //
            "host.with-dash.test", //
            "203.0.113.200", //
            "..." //
    })
    public void resolve_OverrideWithDifferentHostNameFormats(final String hostname) throws UnknownHostException
    {
        final String ip = "192.0.2.100";

        setOverrideProperty(hostname, ip);

        final InetAddress[] addresses = new DnsOverrideResolver(fallbackResolver).resolve(hostname);
        Assert.assertNotNull(addresses);
        Assert.assertEquals(1, addresses.length);
        Assert.assertEquals(ip, addresses[0].getHostAddress());

        Mockito.verify(fallbackResolver, Mockito.never()).resolve(Mockito.any());
    }

    @Test
    public void resolve_OverrideWithOtherIpFormats() throws UnknownHostException
    {
        final String hostname = "example.org";

        setOverrideProperty(hostname, "2001:dB8:1111:2222:aaaa:BBBB:1a2B:Cd34,2001:db8::1:0");

        InetAddress[] addresses = new DnsOverrideResolver(fallbackResolver).resolve(hostname);
        Assert.assertNotNull(addresses);
        Assert.assertEquals(2, addresses.length);
        Assert.assertEquals("2001:db8:1111:2222:aaaa:bbbb:1a2b:cd34", addresses[0].getHostAddress());
        Assert.assertEquals("2001:db8:0:0:0:0:1:0", addresses[1].getHostAddress());

        Mockito.verify(fallbackResolver, Mockito.never()).resolve(Mockito.any());
    }

    @Test
    @Parameters(source = ParseUtilsTest.InvalidIpOverrideParamProvider.class)
    public void resolve_OverridesWithInvalidIpAddresses(final String invalidIpString) throws UnknownHostException
    {
        setOverrideProperty("example.org", invalidIpString);

        Assert.assertThrows(XltException.class, () -> new DnsOverrideResolver(fallbackResolver));

        Mockito.verify(fallbackResolver, Mockito.never()).resolve(Mockito.any());
    }

    @Test
    public void resolve_FallbackResolverIsNull()
    {
        Assert.assertThrows(IllegalArgumentException.class, () -> new DnsOverrideResolver(null));
    }

    @Test
    public void readHostNameOverrides_NoOverrides()
    {
        final Map<String, InetAddress[]> overrides = DnsOverrideResolver.readHostNameOverrides();
        Assert.assertNotNull(overrides);
        Assert.assertEquals(0, overrides.size());
    }

    @Test
    public void readHostNameOverrides_OverridesWithoutHostnameOrIps()
    {
        final String ip = "192.0.2.100";

        final XltProperties props = XltProperties.getInstance();
        // set overrides with no/empty host name
        props.setProperty(DnsOverrideResolver.PROP_DNS_OVERRIDE_PREFIX, ip);
        props.setProperty(DnsOverrideResolver.PROP_DNS_OVERRIDE_PREFIX + ".", ip);
        props.setProperty(DnsOverrideResolver.PROP_DNS_OVERRIDE_PREFIX + ". ", ip);
        props.setProperty(DnsOverrideResolver.PROP_DNS_OVERRIDE_PREFIX + ". \t ", ip);
        // set overrides with no/empty IPs
        props.setProperty(DnsOverrideResolver.PROP_DNS_OVERRIDE_PREFIX + ".empty1.test", "");
        props.setProperty(DnsOverrideResolver.PROP_DNS_OVERRIDE_PREFIX + ".empty2.test", " ");
        props.setProperty(DnsOverrideResolver.PROP_DNS_OVERRIDE_PREFIX + ".empty3.test", " \t ");

        final Map<String, InetAddress[]> overrides = DnsOverrideResolver.readHostNameOverrides();
        Assert.assertNotNull(overrides);
        Assert.assertEquals(0, overrides.size());
    }

    @Test
    public void readHostNameOverrides_OverrideWithSingleIp()
    {
        final String hostname = "example.org";
        final String ip = "192.0.2.100";

        setOverrideProperty(hostname, ip);

        final Map<String, InetAddress[]> overrides = DnsOverrideResolver.readHostNameOverrides();
        Assert.assertNotNull(overrides);
        Assert.assertEquals(1, overrides.size());
        Assert.assertTrue(overrides.containsKey(hostname));
        Assert.assertEquals(1, overrides.get(hostname).length);
        Assert.assertEquals(ip, overrides.get(hostname)[0].getHostAddress());
    }

    @Test
    public void readHostNameOverrides_OverrideWithMultipleIps()
    {
        final String hostname = "example.org";
        final String ip1 = "192.0.2.100";
        final String ip2 = "2001:db8:1:2:3:4:5:6";

        setOverrideProperty(hostname, String.join(",", ip1, ip2));

        final Map<String, InetAddress[]> overrides = DnsOverrideResolver.readHostNameOverrides();
        Assert.assertNotNull(overrides);
        Assert.assertEquals(1, overrides.size());
        Assert.assertTrue(overrides.containsKey(hostname));
        Assert.assertEquals(2, overrides.get(hostname).length);
        Assert.assertEquals(ip1, overrides.get(hostname)[0].getHostAddress());
        Assert.assertEquals(ip2, overrides.get(hostname)[1].getHostAddress());
    }

    @Test
    public void readHostNameOverrides_MultipleOverrides()
    {
        final String hostname1 = "example.org";
        final String ip1a = "192.0.2.100";
        final String ip1b = "2001:db8:1:2:3:4:5:6";

        final String hostname2 = "host.with-dash.test";
        final String ip2a = "2001:db8:1111:2222:aaaa:bbbb:1a2b:cd34";
        final String ip2b = "203.0.113.200";
        final String ip2c = "2001:db8:0:0:0:0:1:0";

        setOverrideProperty(hostname1, String.join(",", ip1a, ip1b));
        setOverrideProperty(hostname2, String.format(" ,; \t %s , ;\t; %s ,,, %s \t, ;; ", ip2a, ip2b, ip2c));

        final Map<String, InetAddress[]> overrides = DnsOverrideResolver.readHostNameOverrides();
        Assert.assertNotNull(overrides);
        Assert.assertEquals(2, overrides.size());
        Assert.assertTrue(overrides.containsKey(hostname1));
        Assert.assertTrue(overrides.containsKey(hostname2));
        Assert.assertEquals(2, overrides.get(hostname1).length);
        Assert.assertEquals(ip1a, overrides.get(hostname1)[0].getHostAddress());
        Assert.assertEquals(ip1b, overrides.get(hostname1)[1].getHostAddress());
        Assert.assertEquals(3, overrides.get(hostname2).length);
        Assert.assertEquals(ip2a, overrides.get(hostname2)[0].getHostAddress());
        Assert.assertEquals(ip2b, overrides.get(hostname2)[1].getHostAddress());
        Assert.assertEquals(ip2c, overrides.get(hostname2)[2].getHostAddress());
    }

    private void setOverrideProperty(final String hostname, final String ipString)
    {
        XltProperties.getInstance().setProperty(DnsOverrideResolver.PROP_DNS_OVERRIDE_PREFIX + "." + hostname, ipString);
    }
}
