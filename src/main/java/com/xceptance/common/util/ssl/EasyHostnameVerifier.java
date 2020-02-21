package com.xceptance.common.util.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * The EasyHostnameVerifier class enables the SSL engine to establish connections to a server even though the server's
 * actual host name does not match the host name recorded in the server's SSL certificate.
 * <p style="color:red">
 * WARNING: Use with care!
 * </p>
 * 
 * @author Jörg Werner (Xceptance Softare Technologies GmbH)
 */
public class EasyHostnameVerifier implements HostnameVerifier
{
    /**
     * Checks whether the host name is acceptable.
     * 
     * @param hostname
     *            the host name to check
     * @param session
     *            the SSL session in question
     * @return true in any case
     */
    @Override
    public boolean verify(final String hostname, final SSLSession session)
    {
        return true;
    }

}
