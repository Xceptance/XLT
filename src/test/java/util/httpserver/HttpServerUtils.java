/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Some utilities that come in handy when building test HTTP servers.
 */
public interface HttpServerUtils
{
    public static final String CRLF = "\r\n";

    public static final String HEADER_CONTENT_LENGTH = "Content-Length: ";

    public static final String HEADER_CONTENT_TYPE = "Content-Type: text/html";

    public static final String HEADER_CONNECTION_CLOSE = "Connection: close";

    public static final String HEADER_CONNECTION_KEEP_ALIVE = "Connection: keep-alive";

    /**
     * Reads all the headers of an HTTP request from the passed input stream.
     * 
     * @param in
     *            the input stream
     * @return the lines read
     * @throws IOException
     */
    public static String readRequestHeaders(InputStream in) throws IOException
    {
        StringBuilder requestHeaders = new StringBuilder();

        while (true)
        {
            final String line = readLine(in);

            if (line == null)
            {
                throw new EOFException();
            }

            requestHeaders.append(line).append('\n');

            if (line.length() == 0)
            {
                // we have reached the empty line after the headers -> done
                break;
            }
        }

        return requestHeaders.toString();
    }

    /**
     * Reads a header line from an HTTP request and returns it without the trailing CRLF.
     * 
     * @param in
     *            the input stream
     * @return the line
     * @throws IOException
     */
    public static String readLine(InputStream in) throws IOException
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
}
