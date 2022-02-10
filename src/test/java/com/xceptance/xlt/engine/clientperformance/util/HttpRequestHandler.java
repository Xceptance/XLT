/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.lang.ThreadUtils;

/**
 * Processes an HTTP request. Via the request URL certain response parameters can be controlled:
 * <ul>
 * <li>response length</li>
 * <li>read/process/write timings</li>
 * <li>keep-alive</li>
 * </ul>
 * Currently, the server supports GET requests only.
 * 
 * @see HttpRequestHandlerConfiguration
 */
public class HttpRequestHandler implements Runnable
{
    private static final String CRLF = "\r\n";

    private static final String DEFAULT_FILLER = "XXXXXXXXXXXX";

    private static final String HEADER_CONTENT_LENGTH = "Content-Length: ";

    private static final String HEADER_CONTENT_TYPE = "Content-Type: text/html";

    private static final String HEADER_CONNECTION_CLOSE = "Connection: close";

    private static final String HEADER_CONNECTION_KEEP_ALIVE = "Connection: keep-alive";

    private static final String HEADER_FILLER = "X-Filler: " + DEFAULT_FILLER;

    private final Socket socket;

    public HttpRequestHandler(Socket socket)
    {
        this.socket = socket;
    }

    /**
     * {@inheritDoc}
     */
    public void run()
    {
        try (InputStream in = socket.getInputStream())
        {
            // loop if keep-alive enabled
            while (true)
            {
                // read the first request line
                String line = readLine(in);
                System.out.printf("### Received request: %s\n", line);

                // determine configuration from the request URL
                String url = StringUtils.split(line, ' ')[1];
                HttpRequestHandlerConfiguration config = new HttpRequestHandlerConfiguration();
                config.parseUrl(url);

                // simulate read delay
                sleep(config.getServerReadTime());

                // now read away the request headers
                while (true)
                {
                    line = readLine(in);

                    if (line == null)
                    {
                        throw new EOFException();
                    }

                    // System.err.printf("### %s\n", line);

                    if (line.length() == 0)
                    {
                        // we have reached the empty line after the headers -> done
                        break;
                    }
                }

                // simulate request processing
                sleep(config.getServerBusyTime());

                // build response headers
                String firstLine = "HTTP/1.1 200 OK" + CRLF;
                String headers = (config.isKeepAlive() ? HEADER_CONNECTION_KEEP_ALIVE : HEADER_CONNECTION_CLOSE) + CRLF;
                headers += HEADER_CONTENT_TYPE + CRLF;
                headers += HEADER_CONTENT_LENGTH + CRLF;
                headers += HEADER_FILLER + CRLF;
                headers += CRLF;

                // now insert the content length value, ...
                int remainingBytes = Math.max(0, config.getResponseLength() - firstLine.length() - headers.length());
                String contentLengthValue = String.valueOf(remainingBytes);
                headers = headers.replace(HEADER_CONTENT_LENGTH, HEADER_CONTENT_LENGTH + contentLengthValue);

                // ... but reduce the filler header value by the same number of bytes
                String newFiller = StringUtils.repeat('X', DEFAULT_FILLER.length() - contentLengthValue.length());
                headers = headers.replace(DEFAULT_FILLER, newFiller);

                // System.err.printf("### %s\n", headers);

                // create the response body
                // String body1 = "<html><head></head><body>";
                // String body2 = "</body></html>";
                String body1 = "";
                String body2 = "";

                remainingBytes = Math.max(0, remainingBytes - body1.length() - body2.length());

                body1 = body1 + StringUtils.repeat('A', (int) Math.floor(remainingBytes / 2.0));
                body2 = StringUtils.repeat('A', (int) Math.ceil(remainingBytes / 2.0)) + body2;

                // send the first line and the headers
                OutputStream socketOut = socket.getOutputStream();
                // OutputStream gzipOut = config.isGzipEnabled() ? new GZIPOutputStream(socketOut) : socketOut;
                OutputStream gzipOut = socketOut;
                PrintStream out = new PrintStream(gzipOut, true, "US-ASCII");
                out.print(firstLine);
                out.print(headers);

                // send the first half of the body
                out.print(body1);
                out.flush();

                // simulate write delay if that makes sense
                if (body1.length() > 0 && body2.length() > 0)
                {
                    sleep(config.getServerWriteTime());
                }

                // send the second half of the body
                out.print(body2);
                out.flush();

                // leave loop if not keep-alive
                if (!config.isKeepAlive())
                {
                    break;
                }
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    /**
     * Reads a line from the socket's input stream and returns it without the trailing CRLF.
     */
    private String readLine(InputStream in) throws IOException
    {
        StringBuilder line = new StringBuilder();

        while (true)
        {
            int b = in.read();

            if (b == -1)
            {
                throw new EOFException();
            }

            if (b == '\r')
            {
                // CR seen, now read the LF
                in.read();

                // we are done
                break;
            }

            line.append((char) b);
        }

        return line.toString();
    }

    /**
     * Sleeps for the given number of milliseconds.
     */
    private void sleep(long msecs)
    {
        // check if sleep() returned a bit too early (happens at least on Windows) and try to fix it

        long endTimeNS = System.nanoTime() + msecs * 1000000;

        ThreadUtils.sleep(msecs);

        while (endTimeNS - System.nanoTime() > 0)
        {
            Thread.yield();
        }

        // System.out.printf("### %,d\n", endTimeNS - System.nanoTime());
    }
}
