package com.caucho.hessian.client;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import com.xceptance.common.util.ssl.EasyHostnameVerifier;
import com.xceptance.common.util.ssl.EasySSLSocketFactory;
import com.xceptance.common.util.ssl.EasySSLUtils;

/**
 * A special {@link HessianConnectionFactory} which uses an {@link EasySSLSocketFactory} and an
 * {@link EasyHostnameVerifier} to make communication with servers possible that use an invalid/self-signed certificate.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class EasyHessianURLConnectionFactory implements HessianConnectionFactory
{
    private HessianProxyFactory proxyFactory;

    /**
     * Opens a connection to the given URL accepting invalid/self-signed certificates when the target URL specifies
     * HTTPS as the protocol.
     * 
     * @param url
     *            the target URL
     * @return an open connection
     * @throws IOException
     *             if a error occurred when opening the connection
     */
    @Override
    public HessianConnection open(final URL url) throws IOException
    {
        final URLConnection conn = EasySSLUtils.openEasyConnection(url);

        final long connTimeout = proxyFactory.getConnectTimeout();
        if (connTimeout >= 0L)
        {
            conn.setConnectTimeout((int) connTimeout);
        }

        conn.setDoOutput(true);

        final long readTimeout = proxyFactory.getReadTimeout();
        if (readTimeout > 0)
        {
            try
            {
                conn.setReadTimeout((int) readTimeout);
            }
            catch (final Throwable e)
            {
            }
        }

        return new HessianURLConnection(url, conn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHessianProxyFactory(final HessianProxyFactory hessianProxyFactory)
    {
        proxyFactory = hessianProxyFactory;
    }
}
