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
package com.xceptance.xlt.engine.socket;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * Test class for SocketStatistics
 * 
 * @author Rene Schwietzke (Xceptance Software Technologies GmbH)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(
    {
        TimerUtils.class
    })
public class SocketStatisticsTest
{
    @Test
    public final void testEmptyStatistics()
    {
        final SocketMonitor mon = new SocketMonitor();

        final SocketStatistics s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(0, s.getBytesSent());
        Assert.assertEquals(0, s.getReceiveTime());
        Assert.assertEquals(0, s.getBytesReceived());
        Assert.assertEquals(0, s.getServerBusyTime());
        Assert.assertEquals(0, s.getTimeToFirstBytes());
        Assert.assertEquals(0, s.getTimeToLastBytes());
    }

    @Test
    public final void testDnsLookUp()
    {
        mockStatic(TimerUtils.class);

        expect(TimerUtils.getTime()).andReturn(100000L);
        expect(TimerUtils.getTime()).andReturn(100100L);
        replayAll();

        final SocketMonitor mon = new SocketMonitor();

        mon.dnsLookupStarted(); // 000
        mon.dnsLookupDone(); // 100

        final SocketStatistics s = mon.getSocketStatistics();
        Assert.assertEquals(100, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(0, s.getBytesSent());
        Assert.assertEquals(0, s.getReceiveTime());
        Assert.assertEquals(0, s.getBytesReceived());
        Assert.assertEquals(0, s.getServerBusyTime());
        Assert.assertEquals(0, s.getTimeToFirstBytes());
        Assert.assertEquals(0, s.getTimeToLastBytes());
    }

    @Test
    public final void testConnecting()
    {
        mockStatic(TimerUtils.class);

        expect(TimerUtils.getTime()).andReturn(100000L);
        expect(TimerUtils.getTime()).andReturn(100100L);
        expect(TimerUtils.getTime()).andReturn(100400L);
        expect(TimerUtils.getTime()).andReturn(100600L);
        replayAll();

        final SocketMonitor mon = new SocketMonitor();

        mon.dnsLookupStarted(); // 000
        mon.dnsLookupDone(); // 100
        mon.connectingStarted(); // 400
        mon.connected(); // 600

        final SocketStatistics s = mon.getSocketStatistics();
        Assert.assertEquals(100, s.getDnsLookupTime());
        Assert.assertEquals(200, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(0, s.getBytesSent());
        Assert.assertEquals(0, s.getReceiveTime());
        Assert.assertEquals(0, s.getBytesReceived());
        Assert.assertEquals(0, s.getServerBusyTime());
        Assert.assertEquals(0, s.getTimeToFirstBytes());
        Assert.assertEquals(0, s.getTimeToLastBytes());
    }

    @Test
    public final void testConnecting_NoDns()
    {
        mockStatic(TimerUtils.class);

        expect(TimerUtils.getTime()).andReturn(100000L);
        expect(TimerUtils.getTime()).andReturn(100100L);
        replayAll();

        final SocketMonitor mon = new SocketMonitor();

        mon.connectingStarted(); // 000
        mon.connected(); // 100

        final SocketStatistics s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(100, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(0, s.getBytesSent());
        Assert.assertEquals(0, s.getReceiveTime());
        Assert.assertEquals(0, s.getBytesReceived());
        Assert.assertEquals(0, s.getServerBusyTime());
        Assert.assertEquals(0, s.getTimeToFirstBytes());
        Assert.assertEquals(0, s.getTimeToLastBytes());
    }

    @Test
    public final void testWriting()
    {
        mockStatic(TimerUtils.class);

        expect(TimerUtils.getTime()).andReturn(100201L);
        expect(TimerUtils.getTime()).andReturn(100250L);
        replayAll();

        final SocketMonitor mon = new SocketMonitor();

        mon.wrote(11); // 201

        SocketStatistics s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(11, s.getBytesSent());
        Assert.assertEquals(0, s.getBytesReceived());
        Assert.assertEquals(0, s.getReceiveTime());
        Assert.assertEquals(0, s.getTimeToFirstBytes());
        Assert.assertEquals(0, s.getTimeToLastBytes());

        mon.wrote(21); // 250

        s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(250 - 201, s.getSendTime());
        Assert.assertEquals(32, s.getBytesSent());
        Assert.assertEquals(0, s.getBytesReceived());
        Assert.assertEquals(0, s.getReceiveTime());
        Assert.assertEquals(0, s.getTimeToFirstBytes());
        Assert.assertEquals(0, s.getTimeToLastBytes());
    }

    @Test
    public final void testReading()
    {
        mockStatic(TimerUtils.class);

        expect(TimerUtils.getTime()).andReturn(200101L);
        expect(TimerUtils.getTime()).andReturn(200149L);
        replayAll();

        final SocketMonitor mon = new SocketMonitor();

        mon.read(65); // 101

        SocketStatistics s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(0, s.getBytesSent());
        Assert.assertEquals(65, s.getBytesReceived());
        Assert.assertEquals(0, s.getReceiveTime());
        Assert.assertEquals(0, s.getTimeToFirstBytes());
        Assert.assertEquals(0, s.getTimeToLastBytes());

        mon.read(21); // 149

        s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(0, s.getBytesSent());
        Assert.assertEquals(86, s.getBytesReceived());
        Assert.assertEquals(48, s.getReceiveTime());
        Assert.assertEquals(0, s.getTimeToFirstBytes());
        Assert.assertEquals(48, s.getTimeToLastBytes());
    }

    @Test
    public final void testGetServerBusyTime()
    {
        mockStatic(TimerUtils.class);

        expect(TimerUtils.getTime()).andReturn(300101L);
        expect(TimerUtils.getTime()).andReturn(300301L);
        expect(TimerUtils.getTime()).andReturn(300402L);
        replayAll();

        final SocketMonitor mon = new SocketMonitor();

        mon.wrote(11); // 101

        mon.read(65); // 301

        SocketStatistics s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(11, s.getBytesSent());
        Assert.assertEquals(0, s.getReceiveTime());
        Assert.assertEquals(65, s.getBytesReceived());
        Assert.assertEquals(200, s.getServerBusyTime());
        Assert.assertEquals(200, s.getTimeToFirstBytes());
        Assert.assertEquals(200, s.getTimeToLastBytes());

        mon.read(121); // 402

        s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(11, s.getBytesSent());
        Assert.assertEquals(186, s.getBytesReceived());
        Assert.assertEquals(101, s.getReceiveTime());
        Assert.assertEquals(200, s.getServerBusyTime());
        Assert.assertEquals(200, s.getTimeToFirstBytes());
        Assert.assertEquals(301, s.getTimeToLastBytes());
    }

    @Test
    public final void testGetServerBusyTime_ReadBeforeWrite()
    {
        mockStatic(TimerUtils.class);

        expect(TimerUtils.getTime()).andReturn(300200L);
        expect(TimerUtils.getTime()).andReturn(300301L);
        expect(TimerUtils.getTime()).andReturn(300402L);
        replayAll();

        final SocketMonitor mon = new SocketMonitor();

        mon.read(65); // 200
        mon.wrote(11); // 301

        SocketStatistics s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(11, s.getBytesSent());
        Assert.assertEquals(0, s.getReceiveTime());
        Assert.assertEquals(65, s.getBytesReceived());
        Assert.assertEquals(0, s.getServerBusyTime());
        Assert.assertEquals(0, s.getTimeToFirstBytes());
        Assert.assertEquals(0, s.getTimeToLastBytes());

        mon.read(121); // 402

        s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(11, s.getBytesSent());
        Assert.assertEquals(186, s.getBytesReceived());
        Assert.assertEquals(402 - 200, s.getReceiveTime());
        Assert.assertEquals(0, s.getServerBusyTime());
        Assert.assertEquals(0, s.getTimeToFirstBytes());
        Assert.assertEquals(202, s.getTimeToLastBytes());
    }

    @Test
    public final void testGetTimeToBytes()
    {
        mockStatic(TimerUtils.class);

        expect(TimerUtils.getTime()).andReturn(500000L);
        expect(TimerUtils.getTime()).andReturn(500045L);
        expect(TimerUtils.getTime()).andReturn(500101L);
        expect(TimerUtils.getTime()).andReturn(500245L);
        expect(TimerUtils.getTime()).andReturn(500299L);
        expect(TimerUtils.getTime()).andReturn(500304L);
        replayAll();

        final SocketMonitor mon = new SocketMonitor();

        mon.connectingStarted(); // 000
        mon.connected(); // 045

        mon.wrote(11); // 101

        mon.read(65); // 245

        SocketStatistics s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(45, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(11, s.getBytesSent());
        Assert.assertEquals(0, s.getReceiveTime());
        Assert.assertEquals(65, s.getBytesReceived());
        Assert.assertEquals(144, s.getServerBusyTime());
        Assert.assertEquals(245, s.getTimeToFirstBytes());
        Assert.assertEquals(245, s.getTimeToLastBytes());

        mon.read(121); // 299

        s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(45, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(11, s.getBytesSent());
        Assert.assertEquals(54, s.getReceiveTime());
        Assert.assertEquals(65 + 121, s.getBytesReceived());
        Assert.assertEquals(144, s.getServerBusyTime());
        Assert.assertEquals(245, s.getTimeToFirstBytes());
        Assert.assertEquals(299, s.getTimeToLastBytes());

        mon.read(1210); // 304

        s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(45, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(11, s.getBytesSent());
        Assert.assertEquals(304 - 245, s.getReceiveTime());
        Assert.assertEquals(65 + 121 + 1210, s.getBytesReceived());
        Assert.assertEquals(144, s.getServerBusyTime());
        Assert.assertEquals(245, s.getTimeToFirstBytes());
        Assert.assertEquals(304, s.getTimeToLastBytes());
    }

    @Test
    public final void testGetTimeToBytes_NoConnect()
    {
        mockStatic(TimerUtils.class);

        expect(TimerUtils.getTime()).andReturn(500045L);
        expect(TimerUtils.getTime()).andReturn(500101L);
        expect(TimerUtils.getTime()).andReturn(500209L);
        expect(TimerUtils.getTime()).andReturn(500245L);
        replayAll();

        final SocketMonitor mon = new SocketMonitor();

        mon.wrote(11); // 045

        mon.read(65); // 101

        SocketStatistics s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(11, s.getBytesSent());
        Assert.assertEquals(0, s.getReceiveTime());
        Assert.assertEquals(65, s.getBytesReceived());
        Assert.assertEquals(101 - 45, s.getServerBusyTime());
        Assert.assertEquals(101 - 45, s.getTimeToFirstBytes());
        Assert.assertEquals(101 - 45, s.getTimeToLastBytes());

        mon.read(121); // 209

        s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(11, s.getBytesSent());
        Assert.assertEquals(209 - 101, s.getReceiveTime());
        Assert.assertEquals(65 + 121, s.getBytesReceived());
        Assert.assertEquals(101 - 45, s.getServerBusyTime());
        Assert.assertEquals(101 - 45, s.getTimeToFirstBytes());
        Assert.assertEquals(209 - 45, s.getTimeToLastBytes());

        mon.read(1210); // 245

        s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(11, s.getBytesSent());
        Assert.assertEquals(245 - 101, s.getReceiveTime());
        Assert.assertEquals(65 + 121 + 1210, s.getBytesReceived());
        Assert.assertEquals(101 - 45, s.getServerBusyTime());
        Assert.assertEquals(101 - 45, s.getTimeToFirstBytes());
        Assert.assertEquals(245 - 45, s.getTimeToLastBytes());
    }

    @Test
    public final void testGetTimeToBytes_KeepAlive_ConnectZero()
    {
        mockStatic(TimerUtils.class);

        expect(TimerUtils.getTime()).andReturn(500000L);
        expect(TimerUtils.getTime()).andReturn(500000L);
        expect(TimerUtils.getTime()).andReturn(500045L);
        expect(TimerUtils.getTime()).andReturn(500101L);
        expect(TimerUtils.getTime()).andReturn(500209L);
        expect(TimerUtils.getTime()).andReturn(500245L);
        replayAll();

        final SocketMonitor mon = new SocketMonitor();

        mon.connectingStarted(); // 000
        mon.connected(); // 000

        mon.wrote(11); // 045

        mon.read(65); // 101

        SocketStatistics s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(11, s.getBytesSent());
        Assert.assertEquals(0, s.getReceiveTime());
        Assert.assertEquals(65, s.getBytesReceived());
        Assert.assertEquals(101 - 45, s.getServerBusyTime());
        Assert.assertEquals(101, s.getTimeToFirstBytes());
        Assert.assertEquals(101, s.getTimeToLastBytes());

        mon.read(121); // 209

        s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(11, s.getBytesSent());
        Assert.assertEquals(209 - 101, s.getReceiveTime());
        Assert.assertEquals(65 + 121, s.getBytesReceived());
        Assert.assertEquals(101 - 45, s.getServerBusyTime());
        Assert.assertEquals(101, s.getTimeToFirstBytes());
        Assert.assertEquals(209, s.getTimeToLastBytes());

        mon.read(1210); // 245

        s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(11, s.getBytesSent());
        Assert.assertEquals(245 - 101, s.getReceiveTime());
        Assert.assertEquals(65 + 121 + 1210, s.getBytesReceived());
        Assert.assertEquals(101 - 45, s.getServerBusyTime());
        Assert.assertEquals(101, s.getTimeToFirstBytes());
        Assert.assertEquals(245, s.getTimeToLastBytes());
    }

    @Test
    public void testReset()
    {
        mockStatic(TimerUtils.class);

        expect(TimerUtils.getTime()).andReturn(500000L);
        expect(TimerUtils.getTime()).andReturn(500003L);
        expect(TimerUtils.getTime()).andReturn(500005L);
        expect(TimerUtils.getTime()).andReturn(500045L);
        expect(TimerUtils.getTime()).andReturn(500101L);
        expect(TimerUtils.getTime()).andReturn(500209L);
        replayAll();

        final SocketMonitor mon = new SocketMonitor();

        mon.connectingStarted(); // 000
        mon.connected(); // 003

        mon.wrote(13); // 005
        mon.wrote(11); // 045

        mon.read(65); // 101
        mon.read(121); // 209

        SocketStatistics s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertTrue(0 != s.getConnectTime());
        Assert.assertTrue(0 != s.getSendTime());
        Assert.assertTrue(0 != s.getBytesSent());
        Assert.assertTrue(0 != s.getReceiveTime());
        Assert.assertTrue(0 != s.getBytesReceived());
        Assert.assertTrue(0 != s.getServerBusyTime());
        Assert.assertTrue(0 != s.getTimeToFirstBytes());
        Assert.assertTrue(0 != s.getTimeToLastBytes());

        mon.reset();

        s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(0, s.getReceiveTime());
        Assert.assertEquals(0, s.getBytesSent());
        Assert.assertEquals(0, s.getBytesReceived());
        Assert.assertEquals(0, s.getServerBusyTime());
        Assert.assertEquals(0, s.getTimeToFirstBytes());
        Assert.assertEquals(0, s.getTimeToLastBytes());
    }

    @Test
    public final void testConnectAndRead()
    {
        mockStatic(TimerUtils.class);

        expect(TimerUtils.getTime()).andReturn(300000L);
        expect(TimerUtils.getTime()).andReturn(300100L);
        expect(TimerUtils.getTime()).andReturn(300201L);
        expect(TimerUtils.getTime()).andReturn(300302L);
        replayAll();

        final SocketMonitor mon = new SocketMonitor();

        mon.connectingStarted(); // 000
        mon.connected(); // 100
        mon.read(65); // 201
        mon.read(45); // 302

        final SocketStatistics s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(100, s.getConnectTime());
        Assert.assertEquals(0, s.getSendTime());
        Assert.assertEquals(0, s.getBytesSent());
        Assert.assertEquals(101, s.getReceiveTime());
        Assert.assertEquals(110, s.getBytesReceived());
        Assert.assertEquals(101, s.getServerBusyTime());
        Assert.assertEquals(201, s.getTimeToFirstBytes());
        Assert.assertEquals(302, s.getTimeToLastBytes());
    }

    @Test
    public final void testConnectAndWrite()
    {
        mockStatic(TimerUtils.class);

        expect(TimerUtils.getTime()).andReturn(300000L);
        expect(TimerUtils.getTime()).andReturn(300100L);
        expect(TimerUtils.getTime()).andReturn(300201L);
        expect(TimerUtils.getTime()).andReturn(300302L);
        replayAll();

        final SocketMonitor mon = new SocketMonitor();

        mon.connectingStarted(); // 000
        mon.connected(); // 100
        mon.wrote(65); // 201
        mon.wrote(45); // 302

        final SocketStatistics s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(100, s.getConnectTime());
        Assert.assertEquals(101, s.getSendTime());
        Assert.assertEquals(110, s.getBytesSent());
        Assert.assertEquals(0, s.getReceiveTime());
        Assert.assertEquals(0, s.getBytesReceived());
        Assert.assertEquals(0, s.getServerBusyTime());
        Assert.assertEquals(0, s.getTimeToFirstBytes());
        Assert.assertEquals(0, s.getTimeToLastBytes());
    }

    @Test
    public final void testWriteAndRead()
    {
        mockStatic(TimerUtils.class);

        expect(TimerUtils.getTime()).andReturn(300000L);
        expect(TimerUtils.getTime()).andReturn(300100L);
        expect(TimerUtils.getTime()).andReturn(300201L);
        expect(TimerUtils.getTime()).andReturn(300302L);
        replayAll();

        final SocketMonitor mon = new SocketMonitor();

        mon.wrote(65); // 000
        mon.wrote(65); // 100
        mon.read(45); // 201
        mon.read(45); // 302

        final SocketStatistics s = mon.getSocketStatistics();
        Assert.assertEquals(0, s.getDnsLookupTime());
        Assert.assertEquals(0, s.getConnectTime());
        Assert.assertEquals(100, s.getSendTime());
        Assert.assertEquals(130, s.getBytesSent());
        Assert.assertEquals(101, s.getReceiveTime());
        Assert.assertEquals(90, s.getBytesReceived());
        Assert.assertEquals(101, s.getServerBusyTime());
        Assert.assertEquals(201, s.getTimeToFirstBytes());
        Assert.assertEquals(302, s.getTimeToLastBytes());
    }
}
