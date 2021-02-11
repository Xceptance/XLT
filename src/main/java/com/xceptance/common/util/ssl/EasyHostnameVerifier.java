/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * The EasyHostnameVerifier class enables the SSL engine to establish connections to a server even though the server's
 * actual host name does not match the host name recorded in the server's SSL certificate.
 * <p style="color:red">
 * WARNING: Use with care!
 * </p>
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
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
