package com.xceptance.xlt.engine;

import com.xceptance.xlt.engine.dns.DnsMonitor;
import com.xceptance.xlt.engine.socket.SocketMonitor;

/**
 * A thread-local context object to store any data gathered during the execution of a single request. This includes:
 * <ul>
 * <li>DNS info</li>
 * <li>socket timings</li>
 * </ul>
 */
public final class RequestExecutionContext
{
    /**
     * The thread-local context instance.
     */
    private static final ThreadLocal<RequestExecutionContext> context = ThreadLocal.withInitial(RequestExecutionContext::new);

    /**
     * Returns the thread-local context instance.
     * 
     * @return the context
     */
    public static RequestExecutionContext getCurrent()
    {
        return context.get();
    }

    /**
     * The DNS monitor.
     */
    private final DnsMonitor dnsMonitor = new DnsMonitor();

    /**
     * The socket monitor for the current thread.
     */
    private final SocketMonitor socketMonitor = new SocketMonitor();

    /**
     * Private constructor.
     */
    private RequestExecutionContext()
    {
    }

    /**
     * Returns the DNS monitor of this context.
     * 
     * @return the DNS monitor
     */
    public DnsMonitor getDnsMonitor()
    {
        return dnsMonitor;
    }

    /**
     * Returns the socket monitor of this context.
     * 
     * @return the socket monitor
     */
    public SocketMonitor getSocketMonitor()
    {
        return socketMonitor;
    }

    /**
     * Resets the data stored in this context and all contained sub objects. This method should be called right before
     * beginning the execution of a request.
     */
    public void reset()
    {
        dnsMonitor.reset();
        socketMonitor.reset();
    }
}
