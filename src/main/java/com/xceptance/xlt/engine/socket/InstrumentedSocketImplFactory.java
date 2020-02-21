package com.xceptance.xlt.engine.socket;

import java.net.SocketImpl;
import java.net.SocketImplFactory;

/**
 * A {@link SocketImplFactory} implementation that creates {@link InstrumentedSocketImpl} objects, which can be
 * monitored.
 */
class InstrumentedSocketImplFactory implements SocketImplFactory
{
    /**
     * {@inheritDoc}
     */
    @Override
    public SocketImpl createSocketImpl()
    {
        return new InstrumentedSocketImpl();
    }
}
