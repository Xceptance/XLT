package com.xceptance.xlt.agentcontroller.xtc;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.lang.ThreadUtils;
import com.xceptance.xlt.agentcontroller.xtc.ws.StreamingWebSocketClient;

/**
 * A client to the tunneling part of the XLT Relay server running at XTC.
 */
public class RelayClient
{
    private static final Logger log = LoggerFactory.getLogger(RelayClient.class);

    private static final int MAGIC_NUMBER = 0xe23b490f;

    private static final long RECONNECT_WAITING_PERIOD = 5_000L;

    private final String relayHost;

    private final int relayPort;

    private final int agentControllerPort;

    private final String hostName;

    private final SSLSocketFactory sslSocketFactory;

    public RelayClient(final String relayHost, final int relayPort, final int agentControllerPort, final String hostName,
                       final SSLSocketFactory sslSocketFactory)
    {
        this.relayHost = relayHost;
        this.relayPort = relayPort;
        this.agentControllerPort = agentControllerPort;
        this.hostName = hostName;
        this.sslSocketFactory = sslSocketFactory;
    }

    public void start()
    {
        log.info("Starting XTC Relay client");
        Thread.ofVirtual().name(RelayClient.class.getSimpleName()).start(this::run);
    }

    private void run()
    {
        while (true)
        {
            log.debug("Connecting to tunnel server");

            // try (Socket tunnelSocket = new Socket(relayHost, relayPort))
            try (Socket tunnelSocket = new StreamingWebSocketClient(relayHost, relayPort, sslSocketFactory).asSocket())
            {
                log.debug("Connected to tunnel server: {}", tunnelSocket);

                // get the streams
                final PushbackInputStream tunnelIn = new PushbackInputStream(tunnelSocket.getInputStream());
                final DataOutputStream tunnelOut = new DataOutputStream(tunnelSocket.getOutputStream());

                // tell the tunnel server the magic number and our host name
                tunnelOut.writeInt(MAGIC_NUMBER);
                tunnelOut.writeUTF(hostName);

                // wait for the first byte to arrive before trying to connect to the AC
                final int firstByte = tunnelIn.read();
                if (firstByte == -1)
                {
                    // tunnel connection was closed by the relay server -> reconnect
                    throw new EOFException();
                }

                tunnelIn.unread(firstByte);

                // connect to the local agent controller
                log.debug("Connecting to agent controller");
                try (Socket acSocket = new Socket("localhost", agentControllerPort))
                {
                    log.debug("Connected to agent controller: {}", acSocket);

                    // start bidirectional data forwarding
                    final InputStream acIn = acSocket.getInputStream();
                    final OutputStream acOut = acSocket.getOutputStream();

                    final Thread t = Thread.ofVirtual().start(() -> transferData(acIn, tunnelOut, "AC -> MC", tunnelSocket));
                    transferData(tunnelIn, acOut, "MC -> AC", acSocket);

                    // wait for bidirectional data forwarding to end
                    t.join();
                    log.debug("Connection to local agent controller ended: {}", acSocket);
                }
                catch (final Exception e)
                {
                    log.error("Failed to connect to local agent controller: {}", e.toString());
                }

                log.debug("Connection to tunnel server ended: {}", tunnelSocket);
            }
            catch (final Exception e)
            {
                log.error("Failed to connect to tunnel server: {}", e.toString());

                // in case of errors wait a little before trying to reconnect
                ThreadUtils.sleep(RECONNECT_WAITING_PERIOD);
            }
        }
    }

    private void transferData(final InputStream in, final OutputStream out, final String direction, final Socket socket)
    {
        try
        {
            // send data
            in.transferTo(out);
            out.flush();
            // debug version of the above
            // transferTo(in, out, direction);

            // indicate that we won't send any more data
            socket.shutdownOutput();
        }
        catch (final IOException e)
        {
            // connection likely closed by one of the parties
            log.error("{}: Data forwarding error: {}", direction, e.getMessage());
        }
        finally
        {
            log.debug("{}: connection closed", direction);
        }
    }

    /**
     * Transfers data similar to {@link InputStream#transferTo(OutputStream)}, but with debug output.
     * <p>
     * Please don't remove!
     * 
     * @param in
     * @param out
     * @param direction
     * @throws IOException
     */
    @SuppressWarnings("unused")
    private static void transferTo(final InputStream in, final OutputStream out, final String direction) throws IOException
    {
        final byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = in.read(buffer)) != -1)
        {
            log.debug("{}: {} bytes", direction, bytesRead);

            out.write(buffer, 0, bytesRead);
            out.flush();
        }

        log.debug("{}: Data forwarding done", direction);
        log.debug("-------------------------------------------");
    }
}
