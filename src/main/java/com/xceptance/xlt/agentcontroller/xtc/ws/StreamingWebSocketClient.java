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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLSocketFactory;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.lang.ReflectionUtils;

/**
 * A {@link WebSocketClient} that opens a standard web socket connection, but exposes it as a {@link StreamingWebSocket}
 * instance.
 */
public class StreamingWebSocketClient extends WebSocketClient
{
    private static final Logger log = LoggerFactory.getLogger(StreamingWebSocketClient.class);

    private final StreamingWebSocket socket;

    private int framesReceived;

    public StreamingWebSocketClient(final String host, final int port, final SSLSocketFactory sslSocketFactory)
        throws URISyntaxException, IOException, InterruptedException
    {
        super(new URI("wss://" + host + ":" + port + "/"));

        setSocketFactory(sslSocketFactory);

        // disable sending out ping frames and waiting for pongs (the relay will control this)
        setConnectionLostTimeout(0);

        final WebSocketImpl webSocketImpl = ReflectionUtils.readInstanceField(this, "engine");
        socket = new StreamingWebSocket(webSocketImpl);

        connectBlocking();
    }

    public StreamingWebSocket asSocket() throws IOException
    {
        return socket;
    }

    @Override
    public void onOpen(final ServerHandshake handshakedata)
    {
        log.trace("Web socket opened");
    }

    @Override
    public void onMessage(final ByteBuffer bytes)
    {
        framesReceived++;
        log.trace("Received binary message: frame #{} with {} bytes", framesReceived, bytes.remaining());

        socket.handleIncomingData(bytes);
    }

    @Override
    public void onMessage(final String message)
    {
        log.trace("Received text message: {}", message);

        throw new UnsupportedOperationException("Unexpected text message received: " + message);
    }

    @Override
    public void onClose(final int code, final String reason, final boolean remote)
    {
        if (code == CloseFrame.NORMAL)
        {
            log.trace("Web socket closed normally (code: {}, reason: '{}', remote: {})", code, reason, remote);
        }
        else
        {
            log.error("Web socket closed due to issues (code: {}, reason: '{}', remote: {})", code, reason, remote);
        }

        socket.close();
    }

    @Override
    public void onError(final Exception ex)
    {
        log.error("Web socket error: {}", ex.toString());
    }

    @Override
    public void onWebsocketPing(WebSocket conn, Framedata f)
    {
        super.onWebsocketPing(conn, f);

        log.trace("Ping received");
    }

    @Override
    public void onWebsocketPong(WebSocket conn, Framedata f)
    {
        super.onWebsocketPong(conn, f);

        log.trace("Pong received");
    }
}
