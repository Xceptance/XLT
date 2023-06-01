/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.clientperformance.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLServerSocketFactory;

/**
 * A HTTP/HTTPS connector that manages incoming connections.
 */
public class HttpConnector implements AutoCloseable
{
    private ServerSocket serverSocket;

    private ExecutorService threadPool;

    public HttpConnector(int port, boolean ssl) throws IOException
    {
        // create a thread pool that processes connections
        threadPool = Executors.newFixedThreadPool(5);

        // create plain/SSL server socket
        serverSocket = ssl ? SSLServerSocketFactory.getDefault().createServerSocket(port) : new ServerSocket(port);

        // start listening on the server socket
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                listen();
            }
        }).start();
    }

    /**
     * Listens for new connections.
     */
    protected void listen()
    {
        while (true)
        {
            // wait for a new connection
            Socket socket = null;
            try
            {
                socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
            }
            catch (IOException e)
            {
                // e.printStackTrace();
                return;
            }

            // process the connection asynchronously
            threadPool.execute(new HttpRequestHandler(socket));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close()
    {
        try
        {
            if (serverSocket != null)
            {
                serverSocket.close();
            }
        }
        catch (IOException e)
        {
            // e.printStackTrace();
        }

        threadPool.shutdownNow();
    }
}
