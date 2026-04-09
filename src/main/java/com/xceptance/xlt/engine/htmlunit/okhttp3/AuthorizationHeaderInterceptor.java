/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
import java.net.Proxy.Type;
import java.util.function.Supplier;

import com.xceptance.xlt.engine.httprequest.HttpRequestHeaders;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Request interceptor that adds cached (Proxy-)Authorization request header values to {@link Request} instances.
 * <p>
 * OkHttp performs authentication if the server requested that (reactive authentication), but it does not automatically
 * add the authentication result to subsequent requests to that server (preemptive authentication). This interceptor
 * tries to close this gap.
 *
 * @see AuthenticatorImpl
 */
class AuthorizationHeaderInterceptor implements Interceptor
{
    private final AuthenticatorImpl authenticatorImpl;

    AuthorizationHeaderInterceptor(final AuthenticatorImpl authenticatorImpl)
    {
        this.authenticatorImpl = authenticatorImpl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response intercept(final Chain chain) throws IOException
    {
        final Request originalRequest = chain.request();
        Request request = originalRequest;

        // add Authorization header
        request = addAuthorizationHeaderIfNeeded(request, HttpRequestHeaders.AUTHORIZATION,
                                                 () -> authenticatorImpl.getCachedAuthHeaderValue(originalRequest));

        // check if the request is performed using a proxy
        final Route route = chain.connection().route();
        if (route.proxy().type() != Type.DIRECT)
        {
            // yes -> add Proxy-Authorization header
            request = addAuthorizationHeaderIfNeeded(request, HttpRequestHeaders.PROXY_AUTHORIZATION,
                                                     () -> authenticatorImpl.getCachedAuthHeaderValue(route));
        }

        return chain.proceed(request);
    }

    private Request addAuthorizationHeaderIfNeeded(Request request, final String authHeaderName, final Supplier<String> headerValueSupplier)
    {
        // check if the request already carries the authorization header
        if (request.header(authHeaderName) == null)
        {
            // no -> try to get the header value from the cache and add it if one was found for the target server
            final String authHeaderValue = headerValueSupplier.get();
            if (authHeaderValue != null)
            {
                request = request.newBuilder().header(authHeaderName, authHeaderValue).build();
            }
        }

        return request;
    }
}
