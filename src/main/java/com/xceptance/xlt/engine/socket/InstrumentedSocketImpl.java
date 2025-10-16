/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;

import com.xceptance.xlt.engine.RequestExecutionContext;

/**
 * A {@link SocketImpl} enhanced to provide important statistics while being used.
 * <p>
 * Since {@link SocketImpl} is rather basic and fully implementing a socket is complicated, we rely on either
 * <code>java.net.PlainSocketImpl</code> or <code>sun.nio.ch.NioSocketImpl</code> (since Java 13) to do most of the job
 * for us. However, these classes are either package-private or final, so we have to use reflection to nevertheless get
 * full access to it.
 */
class InstrumentedSocketImpl extends SocketImpl
{
    private static final Constructor<?> CONSTRUCTOR;

    private static final Method FACTORY_METHOD;

    private static final Method ACCEPT_METHOD;

    private static final Method AVAILABLE_METHOD;

    private static final Method BIND_METHOD;

    private static final Method CLOSE_METHOD;

    private static final Method CONNECT_HOSTNAME_METHOD;

    private static final Method CONNECT_INETADDRESS_METHOD;

    private static final Method CONNECT_SOCKETADDRESS_METHOD;

    private static final Method CREATE_METHOD;

    private static final Method GETFILEDESCRIPTOR_METHOD;

    private static final Method GETINETADDRESS_METHOD;

    private static final Method GETINPUTSTREAM_METHOD;

    private static final Method GETLOCALPORT_METHOD;

    private static final Method GETOUTPUTSTREAM_METHOD;

    private static final Method GETPORT_METHOD;

    private static final Method LISTEN_METHOD;

    private static final Method SENDURGENTDATA_METHOD;

    private static final Method SETPERFORMANCEPREFERNCES_METHOD;

    private static final Method SHUTDOWNINPUT_METHOD;

    private static final Method SHUTDOWNOUTPUT_METHOD;

    private static final Method SUPPORTSURGENTDATA_METHOD;

    static
    {
        try
        {
            // get the constructor/the factory method to create a platform SocketImpl and make it accessible
            if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_13))
            {
                // use SocketImpl.createPlatformSocketImpl() instead of messing around with internal SocketImpl classes
                FACTORY_METHOD = SocketImpl.class.getDeclaredMethod("createPlatformSocketImpl", boolean.class);
                FACTORY_METHOD.setAccessible(true);
                CONSTRUCTOR = null;
            }
            else
            {
                CONSTRUCTOR = Class.forName("java.net.PlainSocketImpl").getDeclaredConstructor();
                CONSTRUCTOR.setAccessible(true);
                FACTORY_METHOD = null;
            }

            // get the protected methods of SocketImpl and make them callable
            final Class<?> ABSTRACT_CLASS = Class.forName("java.net.SocketImpl");

            ACCEPT_METHOD = ABSTRACT_CLASS.getDeclaredMethod("accept", SocketImpl.class);
            ACCEPT_METHOD.setAccessible(true);

            AVAILABLE_METHOD = ABSTRACT_CLASS.getDeclaredMethod("available");
            AVAILABLE_METHOD.setAccessible(true);

            BIND_METHOD = ABSTRACT_CLASS.getDeclaredMethod("bind", InetAddress.class, int.class);
            BIND_METHOD.setAccessible(true);

            CLOSE_METHOD = ABSTRACT_CLASS.getDeclaredMethod("close");
            CLOSE_METHOD.setAccessible(true);

            CONNECT_HOSTNAME_METHOD = ABSTRACT_CLASS.getDeclaredMethod("connect", String.class, int.class);
            CONNECT_HOSTNAME_METHOD.setAccessible(true);

            CONNECT_INETADDRESS_METHOD = ABSTRACT_CLASS.getDeclaredMethod("connect", InetAddress.class, int.class);
            CONNECT_INETADDRESS_METHOD.setAccessible(true);

            CONNECT_SOCKETADDRESS_METHOD = ABSTRACT_CLASS.getDeclaredMethod("connect", SocketAddress.class, int.class);
            CONNECT_SOCKETADDRESS_METHOD.setAccessible(true);

            CREATE_METHOD = ABSTRACT_CLASS.getDeclaredMethod("create", boolean.class);
            CREATE_METHOD.setAccessible(true);

            GETFILEDESCRIPTOR_METHOD = ABSTRACT_CLASS.getDeclaredMethod("getFileDescriptor");
            GETFILEDESCRIPTOR_METHOD.setAccessible(true);

            GETINETADDRESS_METHOD = ABSTRACT_CLASS.getDeclaredMethod("getInetAddress");
            GETINETADDRESS_METHOD.setAccessible(true);

            GETINPUTSTREAM_METHOD = ABSTRACT_CLASS.getDeclaredMethod("getInputStream");
            GETINPUTSTREAM_METHOD.setAccessible(true);

            GETLOCALPORT_METHOD = ABSTRACT_CLASS.getDeclaredMethod("getLocalPort");
            GETLOCALPORT_METHOD.setAccessible(true);

            GETOUTPUTSTREAM_METHOD = ABSTRACT_CLASS.getDeclaredMethod("getOutputStream");
            GETOUTPUTSTREAM_METHOD.setAccessible(true);

            GETPORT_METHOD = ABSTRACT_CLASS.getDeclaredMethod("getPort");
            GETPORT_METHOD.setAccessible(true);

            LISTEN_METHOD = ABSTRACT_CLASS.getDeclaredMethod("listen", int.class);
            LISTEN_METHOD.setAccessible(true);

            SENDURGENTDATA_METHOD = ABSTRACT_CLASS.getDeclaredMethod("sendUrgentData", int.class);
            SENDURGENTDATA_METHOD.setAccessible(true);

            SETPERFORMANCEPREFERNCES_METHOD = ABSTRACT_CLASS.getDeclaredMethod("setPerformancePreferences", int.class, int.class,
                                                                               int.class);
            SETPERFORMANCEPREFERNCES_METHOD.setAccessible(true);

            SHUTDOWNINPUT_METHOD = ABSTRACT_CLASS.getDeclaredMethod("shutdownInput");
            SHUTDOWNINPUT_METHOD.setAccessible(true);

            SHUTDOWNOUTPUT_METHOD = ABSTRACT_CLASS.getDeclaredMethod("shutdownOutput");
            SHUTDOWNOUTPUT_METHOD.setAccessible(true);

            SUPPORTSURGENTDATA_METHOD = ABSTRACT_CLASS.getDeclaredMethod("supportsUrgentData");
            SUPPORTSURGENTDATA_METHOD.setAccessible(true);

            // Method[] methods = ABSTRACT_CLASS.getDeclaredMethods();
            // for (Method method : methods)
            // {
            // System.err.println(method);
            // }
        }
        catch (final Exception ex)
        {
            throw new RuntimeException("Failed to initialize class", ex);
        }
    }

    /**
     * Initializes this class for instrumentation.
     */
    public static void initialize()
    {
        // Calling this method the first time implicitly triggers the static initializer block above to be executed
        // once. Apart from that, there is nothing more to do here.
    }

    /**
     * The actual socket implementation.
     */
    private final SocketImpl socketImpl;

    /**
     * Constructor.
     */
    public InstrumentedSocketImpl()
    {
        try
        {
            // create a SocketImpl instance
            if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_13))
            {
                socketImpl = (SocketImpl) FACTORY_METHOD.invoke(null, false);
            }
            else
            {
                socketImpl = (SocketImpl) CONSTRUCTOR.newInstance();
            }
        }
        catch (final Exception ex)
        {
            throw new RuntimeException("Failed to create new socket impl instance", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj)
    {
        return socketImpl.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getOption(final int optID) throws SocketException
    {
        return socketImpl.getOption(optID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return socketImpl.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOption(final int optID, final Object value) throws SocketException
    {
        socketImpl.setOption(optID, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return socketImpl.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void accept(final SocketImpl s) throws IOException
    {
        invoke(ACCEPT_METHOD, s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int available() throws IOException
    {
        return (Integer) invoke(AVAILABLE_METHOD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void bind(final InetAddress host, final int port) throws IOException
    {
        invoke(BIND_METHOD, host, port);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void close() throws IOException
    {
        invoke(CLOSE_METHOD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void connect(final InetAddress address, final int port) throws IOException
    {
        final SocketMonitor socketMonitor = RequestExecutionContext.getCurrent().getSocketMonitor();

        try
        {
            socketMonitor.connectingStarted();
            invoke(CONNECT_INETADDRESS_METHOD, address, port);
        }
        finally
        {
            socketMonitor.connected();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void connect(final SocketAddress address, final int timeout) throws IOException
    {
        final SocketMonitor socketMonitor = RequestExecutionContext.getCurrent().getSocketMonitor();

        try
        {
            socketMonitor.connectingStarted();
            invoke(CONNECT_SOCKETADDRESS_METHOD, address, timeout);
        }
        finally
        {
            socketMonitor.connected();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void connect(final String host, final int port) throws IOException
    {
        final SocketMonitor socketMonitor = RequestExecutionContext.getCurrent().getSocketMonitor();

        try
        {
            socketMonitor.connectingStarted();
            invoke(CONNECT_HOSTNAME_METHOD, host, port);
        }
        finally
        {
            socketMonitor.connected();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void create(final boolean stream) throws IOException
    {
        invoke(CREATE_METHOD, stream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FileDescriptor getFileDescriptor()
    {
        return (FileDescriptor) invoke2(GETFILEDESCRIPTOR_METHOD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected InetAddress getInetAddress()
    {
        return (InetAddress) invoke2(GETINETADDRESS_METHOD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected InputStream getInputStream() throws IOException
    {
        return new InstrumentedInputStream((InputStream) invoke(GETINPUTSTREAM_METHOD));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getLocalPort()
    {
        return (Integer) invoke2(GETLOCALPORT_METHOD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected OutputStream getOutputStream() throws IOException
    {
        return new InstrumentedOutputStream((OutputStream) invoke(GETOUTPUTSTREAM_METHOD));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getPort()
    {
        return (Integer) invoke2(GETPORT_METHOD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void listen(final int backlog) throws IOException
    {
        invoke(LISTEN_METHOD, backlog);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sendUrgentData(final int data) throws IOException
    {
        invoke(SENDURGENTDATA_METHOD, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setPerformancePreferences(final int connectionTime, final int latency, final int bandwidth)
    {
        invoke2(SETPERFORMANCEPREFERNCES_METHOD, connectionTime, latency, bandwidth);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void shutdownInput() throws IOException
    {
        invoke(SHUTDOWNINPUT_METHOD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void shutdownOutput() throws IOException
    {
        invoke(SHUTDOWNOUTPUT_METHOD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean supportsUrgentData()
    {
        return (Boolean) invoke2(SUPPORTSURGENTDATA_METHOD);
    }

    /**
     * Invokes the given method with the specified parameters on the wrapped socket implementation.
     * 
     * @param method
     *            the method
     * @param args
     *            the method arguments
     * @return the method result
     * @throws IOException
     *             if the passed method threw such an exception
     */
    private Object invoke(final Method method, final Object... args) throws IOException
    {
        try
        {
            return method.invoke(socketImpl, args);
        }
        catch (final Exception ex)
        {
            if (ex instanceof InvocationTargetException)
            {
                // throw it as is
                throw (IOException) ex.getCause();
            }
            else
            {
                // cannot throw it directly -> wrap it
                throw new RuntimeException("Failed to execute method: " + method, ex);
            }
        }
    }

    /**
     * Invokes the given method with the specified parameters on the wrapped socket implementation.
     * 
     * @param method
     *            the method
     * @param args
     *            the method arguments
     * @return the method result
     */
    private Object invoke2(final Method method, final Object... args)
    {
        try
        {
            return method.invoke(socketImpl, args);
        }
        catch (final Exception ex)
        {
            throw new RuntimeException("Failed to execute method: " + method, ex);
        }
    }
}
