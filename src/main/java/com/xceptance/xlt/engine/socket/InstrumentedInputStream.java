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
package com.xceptance.xlt.engine.socket;

import java.io.IOException;
import java.io.InputStream;

import com.xceptance.xlt.engine.RequestExecutionContext;

/**
 * {@link InstrumentedInputStream} wraps an ordinary (socket) input stream to add monitoring functionality.
 * 
 * @see SocketStatistics
 */
public class InstrumentedInputStream extends InputStream
{
    /**
     * The wrapped input stream.
     */
    private final InputStream in;

    /**
     * Constructor.
     * 
     * @param in
     *            the original input stream
     */
    public InstrumentedInputStream(final InputStream in)
    {
        this.in = in;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException
    {
        final int result = in.read();

        // update statistics only if not EOF
        if (result != -1)
        {
            RequestExecutionContext.getCurrent().getSocketMonitor().read(1);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(final byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException
    {
        final int bytesRead = in.read(b, off, len);

        // update statistics only if at least one byte was read
        if (bytesRead > 0)
        {
            RequestExecutionContext.getCurrent().getSocketMonitor().read(bytesRead);
        }

        return bytesRead;
    }
}
