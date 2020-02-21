package com.xceptance.common.util.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * The EasySSLSocketFactory class is used to create SSL sockets that accept self-signed certificates.
 * <p>
 * WARNING: This socket factory SHOULD NOT be used for productive systems due to security reasons, unless it is a
 * conscious decision and you are perfectly aware of security implications of accepting self-signed certificates.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class EasySSLSocketFactory extends SSLSocketFactory
{
    /**
     * The actual socket factory. All method calls are delegated to it.
     */
    private SSLSocketFactory factory;

    /**
     * Creates a new EasySSLSocketFactory object.
     */
    public EasySSLSocketFactory()
    {
        try
        {
            final SSLContext context = SSLContext.getInstance("TLS");

            context.init(null, new TrustManager[]
                {
                    new EasyX509TrustManager(null)
                }, null);

            factory = context.getSocketFactory();
        }
        catch (final Exception ex)
        {
            throw new RuntimeException("Failed to create EasySSLSocketFactory", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket createSocket() throws IOException
    {
        return factory.createSocket();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket createSocket(final InetAddress arg0, final int arg1, final InetAddress arg2, final int arg3) throws IOException
    {
        return factory.createSocket(arg0, arg1, arg2, arg3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket createSocket(final InetAddress arg0, final int arg1) throws IOException
    {
        return factory.createSocket(arg0, arg1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket createSocket(final Socket arg0, final String arg1, final int arg2, final boolean arg3) throws IOException
    {
        return factory.createSocket(arg0, arg1, arg2, arg3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket createSocket(final String arg0, final int arg1, final InetAddress arg2, final int arg3) throws IOException
    {
        return factory.createSocket(arg0, arg1, arg2, arg3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket createSocket(final String arg0, final int arg1) throws IOException
    {
        return factory.createSocket(arg0, arg1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj)
    {
        return factory.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getDefaultCipherSuites()
    {
        return factory.getDefaultCipherSuites();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getSupportedCipherSuites()
    {
        return factory.getSupportedCipherSuites();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return factory.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return factory.toString();
    }
}
