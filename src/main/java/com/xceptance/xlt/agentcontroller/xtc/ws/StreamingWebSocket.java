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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper around a web socket that maps the event-based nature of the web socket to the streaming I/O nature of a
 * plain socket.
 */
public class StreamingWebSocket
    // TODO: remove this inheritance when we have proof that tunneling via web socket works reliably
    extends Socket
{
    private static final Logger log = LoggerFactory.getLogger(StreamingWebSocket.class);

    private final StreamingWebSocketClient streamingWebSocketClient;

    private final PipedOutputStream pipeOut;

    private final PipedInputStream in;

    private final WebSocketOutputStream out;

    private boolean outputShutDown;

    StreamingWebSocket(final StreamingWebSocketClient streamingWebSocketClient) throws IOException
    {
        this.streamingWebSocketClient = streamingWebSocketClient;

        pipeOut = new PipedOutputStream();
        in = new PipedInputStream(pipeOut);
        out = new WebSocketOutputStream(streamingWebSocketClient);
    }

    @Override
    public void close() throws IOException
    {
        try
        {
            streamingWebSocketClient.closeBlocking();
        }
        catch (final InterruptedException ex)
        {
            throw new IOException("Interrupted while closing underlying web socket", ex);
        }
    }

    @Override
    public InputStream getInputStream()
    {
        return in;
    }

    @Override
    public OutputStream getOutputStream()
    {
        return out;
    }

    @Override
    public void shutdownInput()
    {
    }

    @Override
    public void shutdownOutput()
    {
        if (!outputShutDown)
        {
            // send an empty data array to indicate EOF
            streamingWebSocketClient.send(new byte[0]);
            outputShutDown = true;
        }
    }

    void handleIncomingData(final ByteBuffer bytes)
    {
        try
        {
            final byte[] data = new byte[bytes.remaining()];
            bytes.get(data);

            if (data.length == 0)
            {
                // an empty data array indicates EOF
                pipeOut.close();
            }
            else
            {
                pipeOut.write(data);
                pipeOut.flush();
            }
        }
        catch (final IOException e)
        {
            log.error("Error piping data to input stream", e);
        }
    }

    void handleClose()
    {
        try
        {
            pipeOut.close();
        }
        catch (final IOException ignored)
        {
        }
    }

    /**
     * Sends bytes written to this output stream as binary messages to the wrapped web socket.
     */
    private static class WebSocketOutputStream extends OutputStream
    {
        private final WebSocket webSocket;

        public WebSocketOutputStream(final WebSocket webSocket)
        {
            this.webSocket = webSocket;
        }

        @Override
        public void write(final int b) throws IOException
        {
            webSocket.send(new byte[]
                {
                    (byte) b
                });
        }

        @Override
        public void write(final byte[] b, final int off, final int len)
        {
            webSocket.send(ByteBuffer.wrap(b, off, len));
        }
    }
}
