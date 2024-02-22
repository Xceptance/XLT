/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.metrics.graphite;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import javax.net.SocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client to a Graphite Carbon server that uses the plain-text protocol. The format of a line is as follows:
 *
 * <pre>
 * metric.name value timestamp
 * </pre>
 */
public class PlainTextCarbonClient
{
    private static final Logger log = LoggerFactory.getLogger(PlainTextCarbonClient.class);

    private static final Charset US_ASCII = Charset.forName("US-ASCII");

    private final InetSocketAddress address;

    private Socket socket;

    private Writer writer;

    /**
     * Creates a new client which connects to the Carbon server at the given host/port.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     * @throws UnknownHostException
     *             if the host cannot be resolved
     * @throws IllegalArgumentException
     *             if the port number is invalid
     */
    public PlainTextCarbonClient(final String host, final int port) throws UnknownHostException, IllegalArgumentException
    {
        address = new InetSocketAddress(host, port);

        // check if host name can be resolved
        if (address.isUnresolved())
        {
            throw new UnknownHostException(host);
        }
    }

    /**
     * Connects to the server.
     *
     * @throws IllegalStateException
     *             if the client is already connected
     * @throws IOException
     *             if there is an error connecting
     */
    public void connect() throws IllegalStateException, IOException
    {
        if (socket != null)
        {
            throw new IllegalStateException("Already connected");
        }

        socket = SocketFactory.getDefault().createSocket(address.getAddress(), address.getPort());
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), US_ASCII));
    }

    /**
     * Closes a previously established connection.
     */
    public void close()
    {
        if (socket != null)
        {
            try
            {
                writer.flush();
                socket.close();
                socket = null;
                writer = null;
            }
            catch (final IOException e)
            {
                log.debug("Failed to close connection to Graphite server", e);
            }
        }
    }

    /**
     * Sends the given measurement to the server.
     *
     * @param name
     *            the name of the metric
     * @param value
     *            the value of the metric
     * @param timestamp
     *            the timestamp
     * @throws IOException
     *             if there was an error sending the metric
     */
    public void send(final String name, final String value, final long timestamp) throws IOException
    {
        final String line = name + ' ' + value + ' ' + timestamp + '\n';

        writer.write(line);
    }
}
