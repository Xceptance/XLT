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
package com.xceptance.xlt.engine.htmlunit.okhttp3;

import java.io.IOException;

import com.xceptance.xlt.engine.httprequest.HttpRequestHeaders;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Request interceptor that extracts/adds Authorization request header values from/to {@link Request} instances.
 * <p>
 * OkHttp performs authentication if the server requested that, but it does not automatically add the authentication
 * result to follow-up requests to that server. This interceptor tries to close this gap.
 * 
 * @see AuthenticationCache
 * @see AuthenticatorImpl
 */
class AuthorizationHeaderInterceptor implements Interceptor
{
    private final AuthenticationCache authenticationCache;

    AuthorizationHeaderInterceptor(final AuthenticationCache authenticationCache)
    {
        this.authenticationCache = authenticationCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response intercept(final Chain chain) throws IOException
    {
        Request request = chain.request();

        final String hostAndPort = request.url().host() + ":" + request.url().port();

        // check if the request already carries an Authorization header
        String authHeaderValue = request.header(HttpRequestHeaders.AUTHORIZATION);
        if (authHeaderValue == null)
        {
            // no -> consult the auth cache and add the header if one was found for the target server
            authHeaderValue = authenticationCache.getAuthHeaderValue(hostAndPort);
            if (authHeaderValue != null)
            {
                request = request.newBuilder().header(HttpRequestHeaders.AUTHORIZATION, authHeaderValue).build();
            }
        }
        else
        {
            // yes -> cache the auth header for later use
            authenticationCache.putAuthHeaderValue(hostAndPort, authHeaderValue);
        }

        return chain.proceed(request);
    }
}
