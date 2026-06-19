/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.agentcontroller.xtc.ws;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocketImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends bytes written to this output stream as binary messages to the wrapped web socket.
 */
class WebSocketOutputStream extends OutputStream
{
    static final Logger log = LoggerFactory.getLogger(WebSocketOutputStream.class);

    private final WebSocketImpl webSocket;

    private int framesSent;

    private long bytesSent;

    public WebSocketOutputStream(final WebSocketImpl webSocket)
    {
        this.webSocket = webSocket;
    }

    @Override
    public void close() throws IOException
    {
        log.debug("Closing web socket output stream: {} frames/{} bytes sent", framesSent, bytesSent);
    }

    @Override
    public void flush() throws IOException
    {
        int queueSize;
        while ((queueSize = webSocket.outQueue.size()) > 0)
        {
            log.debug("Flushing web socket output stream: {} frames left", queueSize);
            sleep(10L);
        }

        super.flush();
    }

    @Override
    public void write(final int b)
    {
        this.write(new byte[]
            {
                (byte) b
            });
    }

    @Override
    public void write(final byte[] bytes)
    {
        this.write(bytes, 0, bytes.length);
    }

    @Override
    public void write(final byte[] b, final int off, final int len)
    {
        while (webSocket.outQueue.size() > 50)
        {
            sleep(10L);
        }

        webSocket.send(ByteBuffer.wrap(b, off, len));

        framesSent++;
        bytesSent += len;

        log.trace("Sent binary message: frame #{} with {} bytes", framesSent, len);
    }

    private void sleep(final long millis)
    {
        try
        {
            Thread.sleep(10);
        }
        catch (final InterruptedException ex)
        {
        }
    }
}
