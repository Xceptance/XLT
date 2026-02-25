package com.xceptance.xlt.agentcontroller.xtc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.lang.ThreadUtils;

/**
 * A client to the tunneling part of the XLT Relay server running at XTC.
 */
public class RelayClient
{
    private static final Logger log = LoggerFactory.getLogger(RelayClient.class);

    private final String relayHost;

    private final int relayPort;

    private final int agentControllerPort;

    private final String machineName;

    public RelayClient(final String relayHost, final int relayPort, final int agentControllerPort, final String machineName)
    {
        this.relayHost = relayHost;
        this.relayPort = relayPort;
        this.agentControllerPort = agentControllerPort;
        this.machineName = machineName;
    }

    public void start()
    {
        log.info("Starting relay client");
        Thread.ofVirtual().name(RelayClient.class.getSimpleName()).start(this::run);
    }

    private void run()
    {
        while (true)
        {
            log.debug("Connecting to tunnel server");

            try (Socket tunnelSocket = new Socket(relayHost, relayPort))
            {
                log.debug("Connected to tunnel server: {}", tunnelSocket);

                // tunnelSocket.setTcpNoDelay(false);

                final PushbackInputStream tunnelIn = new PushbackInputStream(tunnelSocket.getInputStream());
                final OutputStream tunnelOut = tunnelSocket.getOutputStream();

                // tell the tunnel server our machine details
                // TODO: length check, allow utf-8, more infos, json?
                tunnelOut.write(machineName.length());
                tunnelOut.write(machineName.getBytes(StandardCharsets.US_ASCII));

                // wait for the first byte to arrive before trying to connect to the AC
                final int firstByte = tunnelIn.read();
                if (firstByte == -1)
                {
                    // tunnel connection was closed by the relay server -> reconnect
                    continue;
                }

                tunnelIn.unread(firstByte);

                // connect to the local agent controller
                log.debug("Connecting to local agent controller");
                try (Socket acSocket = new Socket("localhost", agentControllerPort))
                {
                    log.debug("Connected to agent controller: {}", acSocket);

                    // acSocket.setTcpNoDelay(false);

                    // start bidirectional data forwarding
                    final InputStream acIn = acSocket.getInputStream();
                    final OutputStream acOut = acSocket.getOutputStream();

                    final Thread t = Thread.ofVirtual().start(() -> transfer(acIn, tunnelOut, "AC -> MC", tunnelSocket));
                    transfer(tunnelIn, acOut, "MC -> AC", acSocket);

                    //
                    t.join();
                    log.debug("Connection to local agent controller ended: {}", acSocket);
                }
                catch (final Exception e)
                {
                    log.error("Failed to connect to local agent controller: {}", e.getMessage());
                }

                log.debug("Connection to tunnel server ended: {}", tunnelSocket);
            }
            catch (final Exception e)
            {
                log.error("Failed to connect to tunnel server: {}", e.getMessage());

                // in case of errors wait a little before trying to reconnect
                // TODO
                ThreadUtils.sleep(5000L);
            }
        }
    }

    private void transfer(final InputStream in, final OutputStream out, final String direction, final Socket socket)
    {
        try
        {
            // send data
            // in.transferTo(out);

            final byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1)
            {
                log.debug("{}: {} bytes", direction, bytesRead);

                out.write(buffer, 0, bytesRead);
                out.flush();
            }

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
}
