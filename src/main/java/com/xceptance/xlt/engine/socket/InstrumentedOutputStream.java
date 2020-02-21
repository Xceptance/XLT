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
