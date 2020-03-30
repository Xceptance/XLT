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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.engine.RequestExecutionContext;

public class InstrumentedInputStreamTest
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
    public final void testReadInt() throws InterruptedException, IOException
    {
        final int LENGTH = 2048;
        final byte[] bytes = new byte[LENGTH];
        XltRandom.nextBytes(bytes);

        final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        final InstrumentedInputStream iis = new InstrumentedInputStream(bis);

        final SocketMonitor socketMonitor = RequestExecutionContext.getCurrent().getSocketMonitor();
        socketMonitor.reset(); // test cases are sharing the thread

        SocketStatistics socketStatistics = socketMonitor.getSocketStatistics();
        Assert.assertEquals(0, socketStatistics.getBytesReceived());
        Assert.assertEquals(0, socketStatistics.getBytesSent());

        final int loopLength = XltRandom.nextInt(101) + 10;
        for (int i = 0; i < loopLength; i++)
        {
            final int b = iis.read();

            socketStatistics = socketMonitor.getSocketStatistics();
            Assert.assertEquals(i + 1, socketStatistics.getBytesReceived());
            Assert.assertEquals(0, socketStatistics.getBytesSent());

            Assert.assertEquals(bytes[i], (byte) b); // check correct read
        }

        socketStatistics = socketMonitor.getSocketStatistics();
        Assert.assertEquals(loopLength, socketStatistics.getBytesReceived());
        Assert.assertEquals(0, socketStatistics.getBytesSent());

        Assert.assertEquals(LENGTH - loopLength, bis.available());

        iis.close();

        checkContext();
    }

    @Test
    public final void testReadByteArray() throws IOException, InterruptedException
    {
        final int LENGTH = 2048;
        final byte[] bytes = new byte[LENGTH];
        XltRandom.nextBytes(bytes);

        final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        final InstrumentedInputStream iis = new InstrumentedInputStream(bis);

        final SocketMonitor socketMonitor = RequestExecutionContext.getCurrent().getSocketMonitor();
        socketMonitor.reset(); // test cases are sharing the thread

        SocketStatistics socketStatistics = socketMonitor.getSocketStatistics();
        Assert.assertEquals(0, socketStatistics.getBytesReceived());
        Assert.assertEquals(0, socketStatistics.getBytesSent());

        final int loopLength = XltRandom.nextInt(76) + 10;
        int totalLength = 0;
        for (int i = 0; i < loopLength; i++)
        {
            final byte[] localArray = new byte[XltRandom.nextInt(11) + 1];
            final int b = iis.read(localArray);
            totalLength += b;

            socketStatistics = socketMonitor.getSocketStatistics();
            Assert.assertEquals(totalLength, socketStatistics.getBytesReceived());
            Assert.assertEquals(0, socketStatistics.getBytesSent());

            final byte[] orig = new byte[b];
            final byte[] local = new byte[b];
            System.arraycopy(bytes, totalLength - b, orig, 0, b);
            System.arraycopy(localArray, 0, local, 0, b);
            Assert.assertTrue(Arrays.equals(orig, local));
        }

        socketStatistics = socketMonitor.getSocketStatistics();
        Assert.assertEquals(totalLength, socketStatistics.getBytesReceived());
        Assert.assertEquals(0, socketStatistics.getBytesSent());

        Assert.assertEquals(LENGTH - totalLength, bis.available());

        iis.close();

        checkContext();
    }

    @Test
    public final void testWriteByteArrayInt() throws IOException, InterruptedException
    {
        final int LENGTH = 2048;
        final byte[] bytes = new byte[LENGTH];
        XltRandom.nextBytes(bytes);

        final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        final InstrumentedInputStream iis = new InstrumentedInputStream(bis);

        final SocketMonitor socketMonitor = RequestExecutionContext.getCurrent().getSocketMonitor();
        socketMonitor.reset(); // test cases are sharing the thread

        SocketStatistics socketStatistics = socketMonitor.getSocketStatistics();
        Assert.assertEquals(0, socketStatistics.getBytesReceived());
        Assert.assertEquals(0, socketStatistics.getBytesSent());

        final int loopLength = XltRandom.nextInt(76) + 10;
        int totalLength = 0;
        for (int i = 0; i < loopLength; i++)
        {
            final byte[] localArray = new byte[XltRandom.nextInt(11) + 1];
            final int b = iis.read(localArray, 0, localArray.length);
            totalLength += b;

            socketStatistics = socketMonitor.getSocketStatistics();
            Assert.assertEquals(totalLength, socketStatistics.getBytesReceived());
            Assert.assertEquals(0, socketStatistics.getBytesSent());

            final byte[] orig = new byte[b];
            final byte[] local = new byte[b];
            System.arraycopy(bytes, totalLength - b, orig, 0, b);
            System.arraycopy(localArray, 0, local, 0, b);
            Assert.assertTrue(Arrays.equals(orig, local));
        }

        socketStatistics = socketMonitor.getSocketStatistics();
        Assert.assertEquals(totalLength, socketStatistics.getBytesReceived());
        Assert.assertEquals(0, socketStatistics.getBytesSent());

        Assert.assertEquals(LENGTH - totalLength, bis.available());

        iis.close();

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
