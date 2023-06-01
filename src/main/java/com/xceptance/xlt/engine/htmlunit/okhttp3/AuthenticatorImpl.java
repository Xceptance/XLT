/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
import java.net.InetSocketAddress;

import javax.annotation.Nullable;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;

import com.xceptance.xlt.engine.httprequest.HttpRequestHeaders;

import okhttp3.Authenticator;
import okhttp3.Challenge;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * An OkHttp {@link Authenticator} that is backed by HtmlUnit's credentials provider.
 */
class AuthenticatorImpl implements Authenticator
{
    private final CredentialsProvider credentialsProvider;

    public AuthenticatorImpl(final CredentialsProvider credentialsProvider)
    {
        this.credentialsProvider = credentialsProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable Request authenticate(@Nullable final Route route, final Response response) throws IOException
    {
        // whether to authenticate with a proxy or an origin server
        final boolean isProxyAuth = response.code() == 407; // must be 401 otherwise

        // check if a previous (proxy) authentication attempt failed
        if (!isProxyAuth && response.request().header(HttpRequestHeaders.AUTHORIZATION) != null
            || isProxyAuth && response.request().header(HttpRequestHeaders.PROXY_AUTHORIZATION) != null)
        {
            // give up, we've already failed to authenticate
            return null;
        }

        // check each provided challenge
        for (final Challenge challenge : response.challenges())
        {
            // a special scheme that OkHttp uses for preemptive HTTPS proxy authentication
            if (challenge.scheme().equalsIgnoreCase("OkHttp-Preemptive"))
            {
                final AuthScope authScope = createAuthScope(challenge, route, true);

                final Credentials credentials = credentialsProvider.getCredentials(authScope);
                if (credentials != null)
                {
                    return createBasicAuthRequest(credentials, response, true);
                }
            }
            // basic authentication for origin servers and HTTP proxies
            else if (challenge.scheme().equalsIgnoreCase("Basic"))
            {
                final AuthScope authScope = createAuthScope(challenge, route, isProxyAuth);

                final Credentials credentials = credentialsProvider.getCredentials(authScope);
                if (credentials != null)
                {
                    return createBasicAuthRequest(credentials, response, isProxyAuth);
                }
            }
            else
            {
                // TODO: support other authentication schemes?
            }
        }

        // no credentials found for the requested realm/scheme
        return null;
    }

    private AuthScope createAuthScope(final Challenge challenge, @Nullable final Route route, final boolean isProxyAuth)
    {
        final String host;
        final int port;

        if (route == null)
        {
            host = AuthScope.ANY_HOST;
            port = AuthScope.ANY_PORT;
        }
        else if (isProxyAuth)
        {
            final InetSocketAddress socketAddress = route.socketAddress();
            host = socketAddress.getHostString();
            port = socketAddress.getPort();
        }
        else
        {
            final HttpUrl httpUrl = route.address().url();
            host = httpUrl.host();
            port = httpUrl.port();
        }

        return new AuthScope(host, port, challenge.realm(), challenge.scheme());
    }

    private Request createBasicAuthRequest(final Credentials credentials, final Response response, final boolean isProxyAuth)
    {
        final String basicAuthHeaderName = isProxyAuth ? HttpRequestHeaders.PROXY_AUTHORIZATION : HttpRequestHeaders.AUTHORIZATION;
        final String basicAuthHeaderValue = okhttp3.Credentials.basic(credentials.getUserPrincipal().getName(), credentials.getPassword());

        return response.request().newBuilder().header(basicAuthHeaderName, basicAuthHeaderValue).build();
    }
}
