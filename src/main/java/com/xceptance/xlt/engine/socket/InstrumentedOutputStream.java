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
package com.xceptance.xlt.engine.socket;

import java.io.IOException;
import java.io.OutputStream;

import com.xceptance.xlt.engine.RequestExecutionContext;

/**
 * {@link InstrumentedOutputStream} wraps an ordinary (socket) output stream to add monitoring functionality.
 * 
 * @see SocketStatistics
 */
public class InstrumentedOutputStream extends OutputStream
{
    /**
     * The wrapped output stream.
     */
    private final OutputStream out;

    /**
     * Constructor.
     * 
     * @param out
     *            the original output stream
     */
    public InstrumentedOutputStream(final OutputStream out)
    {
        this.out = out;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException
    {
        out.write(b, off, len);

        RequestExecutionContext.getCurrent().getSocketMonitor().wrote(len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final int b) throws IOException
    {
        out.write(b);

        RequestExecutionContext.getCurrent().getSocketMonitor().wrote(1);
    }
}
