package com.xceptance.xlt.agentcontroller.xtc.ws;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLSocketFactory;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link WebSocketClient} that opens a standard web socket connection, but exposes it as a {@link StreamingWebSocket}
 * instance.
 */
public class StreamingWebSocketClient extends WebSocketClient
{
    private static final Logger log = LoggerFactory.getLogger(StreamingWebSocketClient.class);

    private final StreamingWebSocket socket;

    public StreamingWebSocketClient(final String host, final int port, final SSLSocketFactory sslSocketFactory)
        throws URISyntaxException, IOException, InterruptedException
    {
        super(new URI("wss://" + host + ":" + port + "/"));

        setSocketFactory(sslSocketFactory);

        socket = new StreamingWebSocket(this);

        connectBlocking();
    }

    public StreamingWebSocket asSocket() throws IOException
    {
        return socket;
    }

    @Override
    public void onOpen(final ServerHandshake handshakedata)
    {
        log.debug("Web socket opened");
    }

    @Override
    public void onMessage(final ByteBuffer bytes)
    {
        log.debug("Received binary message with {} bytes", bytes.remaining());

        socket.handleIncomingData(bytes);
    }

    @Override
    public void onMessage(final String message)
    {
        log.debug("Received text message: {}", message);

        throw new UnsupportedOperationException("Unexpected text message received: " + message);
    }

    @Override
    public void onClose(final int code, final String reason, final boolean remote)
    {
        log.debug("Web socket closed (code: {}, reason: {}, remote: {})", code, reason, remote);

        socket.handleClose();
    }

    @Override
    public void onError(final Exception ex)
    {
        log.error("Websocket error: {}", ex.toString());
    }
}
