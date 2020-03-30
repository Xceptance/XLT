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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.engine.RequestExecutionContext;

public class InstrumentedOutputStreamTest
{
    private void checkContext() throws InterruptedException
    {
        // check any concurrent context to be clean
        final ContextChecker contextChecker = new ContextChecker();
        final Thread t = new Thread(contextChecker);
        t.start();
        t.join();

        final SocketStatistics socketStatisticsConcurrent = contextChecker.getSocketMonitor().getSocketStatistics();
        Assert.assertEquals(0, socketStatisticsConcurrent.getBytesReceived());
        Assert.assertEquals(0, socketStatisticsConcurrent.getBytesSent());
    }

    @Test
    public final void testWriteInt() throws InterruptedException, IOException
    {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final InstrumentedOutputStream ios = new InstrumentedOutputStream(bos);

        final SocketMonitor socketMonitor = RequestExecutionContext.getCurrent().getSocketMonitor();
        socketMonitor.reset(); // test cases are sharing the thread

        SocketStatistics socketStatistics = socketMonitor.getSocketStatistics();
        Assert.assertEquals(0, socketStatistics.getBytesReceived());
        Assert.assertEquals(0, socketStatistics.getBytesSent());

        final int loopLength = XltRandom.nextInt(101) + 10;
        for (int i = 0; i < loopLength; i++)
        {
            ios.write(XltRandom.nextInt(256));

            socketStatistics = socketMonitor.getSocketStatistics();
            Assert.assertEquals(0, socketStatistics.getBytesReceived());
            Assert.assertEquals(i + 1, socketStatistics.getBytesSent());
        }

        socketStatistics = socketMonitor.getSocketStatistics();
        Assert.assertEquals(0, socketStatistics.getBytesReceived());
        Assert.assertEquals(loopLength, socketStatistics.getBytesSent());

        Assert.assertEquals(loopLength, bos.toByteArray().length);

        ios.close();

        checkContext();
    }

    @Test
    public final void testWriteByteArray() throws IOException, InterruptedException
    {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final InstrumentedOutputStream ios = new InstrumentedOutputStream(bos);

        final SocketMonitor socketMonitor = RequestExecutionContext.getCurrent().getSocketMonitor();
        socketMonitor.reset(); // test cases are sharing the thread

        SocketStatistics socketStatistics = socketMonitor.getSocketStatistics();
        Assert.assertEquals(0, socketStatistics.getBytesReceived());
        Assert.assertEquals(0, socketStatistics.getBytesSent());

        final int loopLength = XltRandom.nextInt(55) + 10;
        int totalBytes = 0;

        for (int i = 0; i < loopLength; i++)
        {
            final int localBytes = XltRandom.nextInt(254) + 1;
            totalBytes += localBytes;

            final byte[] bytes = new byte[localBytes];
            XltRandom.nextBytes(bytes);

            ios.write(bytes);

            socketStatistics = socketMonitor.getSocketStatistics();
            Assert.assertEquals(0, socketStatistics.getBytesReceived());
            Assert.assertEquals(totalBytes, socketStatistics.getBytesSent());
        }

        socketStatistics = socketMonitor.getSocketStatistics();
        Assert.assertEquals(0, socketStatistics.getBytesReceived());
        Assert.assertEquals(totalBytes, socketStatistics.getBytesSent());

        Assert.assertEquals(totalBytes, bos.toByteArray().length);

        ios.close();

        checkContext();
    }

    @Test
    public final void testWriteByteArrayIntInt() throws IOException, InterruptedException
    {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final InstrumentedOutputStream ios = new InstrumentedOutputStream(bos);

        final SocketMonitor socketMonitor = RequestExecutionContext.getCurrent().getSocketMonitor();
        socketMonitor.reset(); // test cases are sharing the thread

        SocketStatistics socketStatistics = socketMonitor.getSocketStatistics();
        Assert.assertEquals(0, socketStatistics.getBytesReceived());
        Assert.assertEquals(0, socketStatistics.getBytesSent());

        final int loopLength = XltRandom.nextInt(55) + 10;
        int totalBytes = 0;

        for (int i = 0; i < loopLength; i++)
        {
            final int localBytes = XltRandom.nextInt(254) + 25;
            final int writelocalBytes = XltRandom.nextInt(localBytes);
            totalBytes += writelocalBytes;

            final byte[] bytes = new byte[localBytes];
            XltRandom.nextBytes(bytes);

            ios.write(bytes, 0, writelocalBytes);

            socketStatistics = socketMonitor.getSocketStatistics();
            Assert.assertEquals(0, socketStatistics.getBytesReceived());
            Assert.assertEquals(totalBytes, socketStatistics.getBytesSent());
        }

        socketStatistics = socketMonitor.getSocketStatistics();
        Assert.assertEquals(0, socketStatistics.getBytesReceived());
        Assert.assertEquals(totalBytes, socketStatistics.getBytesSent());

        Assert.assertEquals(totalBytes, bos.toByteArray().length);

        ios.close();

        checkContext();
    }

    private static class ContextChecker implements Runnable
    {
        private volatile SocketMonitor socketMonitor;

        @Override
        public void run()
        {
            socketMonitor = RequestExecutionContext.getCurrent().getSocketMonitor();
        }

        public SocketMonitor getSocketMonitor()
        {
            return socketMonitor;
        }
    }
}
