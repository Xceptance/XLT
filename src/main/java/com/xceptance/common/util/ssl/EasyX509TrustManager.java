/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.common.util.ssl;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * The EasyX509TrustManager, unlike the default {@link X509TrustManager}, accepts self-signed certificates.
 * <p>
 * WARNING: This trust manager SHOULD NOT be used for productive systems due to security reasons, unless it is a
 * conscious decision and you are perfectly aware of security implications of accepting self-signed certificates
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class EasyX509TrustManager implements X509TrustManager
{
    /**
     * Creates a new EasyX509TrustManager object and initializes it with the given key store.
     * 
     * @param keyStore
     *            the key store to use
     */
    public EasyX509TrustManager(final KeyStore keyStore)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        return new X509Certificate[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType)
    {
    }
}
