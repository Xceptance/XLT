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
package util.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.io.IOUtils;

/**
 * A dummy HTTP server that simulates faults during request processing.
 */
public class FaultyHttpServer implements AutoCloseable, HttpServerUtils
{
    /**
     * The faulty behaviors possible right now.
     */
    public enum Behavior
    {
        /** The server is down (no socket). */
        UNAVAILABLE,

        /** The socket exists, but no one cares about requests. */
        DEAF,

        /** The request is accepted, but not dealt with. */
        LISTENS_ONLY,

        /** The request is read, but not answered. */
        READS_REQUEST,

        /** The request is answered partially only. */
        READS_REQUEST_WRITES_PARTIAL_RESPONSE
    };

    private ServerSocket serverSocket;

    public FaultyHttpServer(Behavior behavior) throws IOException
    {
        // use a random port
        this(behavior, 0);
    }

    public FaultyHttpServer(Behavior behavior, int port) throws IOException
    {
        serverSocket = (behavior == Behavior.UNAVAILABLE) ? null : new ServerSocket(port);

        new RequestHandler(serverSocket, behavior).start();
    }

    public void close()
    {
        IOUtils.closeQuietly(serverSocket);
    }

    public int getPort()
    {
        return serverSocket.getLocalPort();
    }

    /**
     * Deals with requests (or not), but only one request after the other.
     */
    private static class RequestHandler extends Thread
    {
        private static final String RESPONSE_HEADERS;

        static
        {
            StringBuilder buf = new StringBuilder();

            buf.append("HTTP/1.1 200 OK").append(CRLF);
            buf.append(HEADER_CONNECTION_CLOSE).append(CRLF);
            buf.append(HEADER_CONTENT_TYPE).append(CRLF);
            buf.append(HEADER_CONTENT_LENGTH).append(1000).append(CRLF);
            buf.append(CRLF);

            RESPONSE_HEADERS = buf.toString();
        }

        private ServerSocket serverSocket;

        private Behavior behavior;

        public RequestHandler(ServerSocket serverSocket, Behavior behavior)
        {
            this.serverSocket = serverSocket;
            this.behavior = behavior;
        }

        @Override
        public void run()
        {
            if (behavior == Behavior.UNAVAILABLE || behavior == Behavior.DEAF)
            {
                // nothing to do here
            }
            else
            {
                try
                {
                    while (true)
                    {
                        final Socket socket = serverSocket.accept();

                        if (behavior == Behavior.READS_REQUEST || behavior == Behavior.READS_REQUEST_WRITES_PARTIAL_RESPONSE)
                        {
                            String req = HttpServerUtils.readRequestHeaders(socket.getInputStream());
                            System.out.println(req);

                            if (behavior == Behavior.READS_REQUEST_WRITES_PARTIAL_RESPONSE)
                            {
                                System.out.println(RESPONSE_HEADERS);

                                final OutputStream out = socket.getOutputStream();
                                out.write(RESPONSE_HEADERS.getBytes("UTF-8"));
                                out.flush();
                            }
                        }

                        // keep the socket open
                    }
                }
                catch (IOException e)
                {
                }
            }
        }
    }
}
