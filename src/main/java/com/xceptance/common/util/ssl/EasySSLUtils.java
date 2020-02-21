package com.xceptance.common.util.ssl;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Utilities to deal with HTTPS servers that use invalid/self-signed certificates.
 * 
 * @author jwerner
 */
public final class EasySSLUtils
{
    /**
     * A SSL socket factory instance that accepts invalid/self-signed certificates.
     */
    public static final SSLSocketFactory EASY_SSL_SOCKET_FACTORY = new EasySSLSocketFactory();

    /**
     * A host name verifier that accepts any host name in the certificate.
     */
    public static final HostnameVerifier EASY_HOST_NAME_VERIFIER = new EasyHostnameVerifier();

    /**
     * Constructor.
     */
    private EasySSLUtils()
    {
    }

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
    public static URLConnection openEasyConnection(final URL url) throws IOException
    {
        final URLConnection urlConnection = url.openConnection();

        if (urlConnection instanceof HttpsURLConnection)
        {
            final HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) urlConnection;

            httpsUrlConnection.setSSLSocketFactory(EASY_SSL_SOCKET_FACTORY);
            httpsUrlConnection.setHostnameVerifier(EASY_HOST_NAME_VERIFIER);
        }

        return urlConnection;
    }
}
