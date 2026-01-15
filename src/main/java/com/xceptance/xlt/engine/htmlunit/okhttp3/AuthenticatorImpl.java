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
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
 *
 * @see AuthorizationHeaderInterceptor
 */
class AuthenticatorImpl implements Authenticator
{
    /**
     * The result of an authentication attempt ready to be stored in the authentication cache.
     * 
     * @param authScope
     *            the scope for which the authentication was originally performed
     * @param headerValue
     *            the authorization request header value
     */
    static record AuthResult(AuthScope authScope, String headerValue)
    {
    }

    /**
     * A simple cache that stores authorization request header values (such as "Basic MTox") by the target host/port
     * (such as "localhost:8443"). The cache will be used for preemptive authentication of all following requests in the
     * current session for both origin servers and HTTP proxies.
     */
    private final Map<String, AuthResult> authenticationCache = new ConcurrentHashMap<>();

    /**
     * HtmlUnit's credentials provider.
     */
    private final CredentialsProvider credentialsProvider;

    /**
     * Constructor.
     * 
     * @param credentialsProvider
     *            the underlying credentials provider to use
     */
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
        final String authHeaderName = getAuthHeaderName(isProxyAuth);

        // check if a previous (proxy) authentication attempt failed
        if (response.request().header(authHeaderName) != null)
        {
            // give up, we've already failed to authenticate
            return null;
        }

        // check each provided challenge
        for (final Challenge challenge : response.challenges())
        {
            // preemptive basic authentication for HTTPS proxies using a special scheme used by OkHttp only
            if (challenge.scheme().equalsIgnoreCase("OkHttp-Preemptive"))
            {
                return createBasicAuthRequest(challenge, route, response.request(), true, false);
            }
            // reactive basic authentication for origin servers and HTTP proxies
            else if (challenge.scheme().equalsIgnoreCase("Basic"))
            {
                return createBasicAuthRequest(challenge, route, response.request(), isProxyAuth, true);
            }
            else
            {
                // TODO: support other authentication schemes?
            }
        }

        // no credentials found for the requested realm/scheme
        return null;
    }

    /**
     * Creates an authentication scope that matches the passed parameters.
     * 
     * @param challenge
     *            the authentication challenge
     * @param route
     *            the route
     * @param isProxyAuth
     *            whether the challenge is for proxy server authentication (instead of origin server authentication)
     * @return the authentication scope
     */
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

    /**
     * Creates a {@link Request} object with the authorization header set that can be used to retry the original
     * request.
     * 
     * @param challenge
     *            the authentication challenge
     * @param route
     *            the route
     * @param request
     *            the original request
     * @param isProxyAuth
     *            whether the request is for proxy server authentication (instead of origin server authentication)
     * @param cacheAuthHeaderValue
     *            whether to cache the authentication result (not wanted for preemptive HTTPS proxy authentication)
     * @return a copy of the passed request with the authorization header set, or <code>null</code> if no matching
     *         credentials could be found
     */
    private @Nullable Request createBasicAuthRequest(final Challenge challenge, final Route route, final Request request,
                                                     final boolean isProxyAuth, final boolean cacheAuthHeaderValue)
    {
        final AuthScope authScope = createAuthScope(challenge, route, isProxyAuth);

        final Credentials credentials = credentialsProvider.getCredentials(authScope);
        if (credentials != null)
        {
            final String basicAuthHeaderName = getAuthHeaderName(isProxyAuth);
            final String basicAuthHeaderValue = okhttp3.Credentials.basic(credentials.getUserPrincipal().getName(),
                                                                          credentials.getPassword());

            // optionally cache the authentication result for later use in subsequent requests
            if (cacheAuthHeaderValue)
            {
                final String cacheKey = isProxyAuth ? getCacheKey(route) : getCacheKey(request);
                authenticationCache.put(cacheKey, new AuthResult(authScope, basicAuthHeaderValue));
            }

            // create a new request with the authorization header set
            return request.newBuilder().header(basicAuthHeaderName, basicAuthHeaderValue).build();
        }

        return null;
    }

    /**
     * Returns the cached authorization header value for the given {@link Request} object.
     *
     * @param request
     *            the request
     * @return the header value or <code>null</code> if nothing was found in the cache
     */
    String getCachedAuthHeaderValue(final Request request)
    {
        return getCachedAuthHeaderValue(getCacheKey(request));
    }

    /**
     * Returns the cached authorization header value for the given {@link Route} object.
     *
     * @param route
     *            the route
     * @return the header value or <code>null</code> if nothing was found in the cache
     */
    String getCachedAuthHeaderValue(final Route route)
    {
        return getCachedAuthHeaderValue(getCacheKey(route));
    }

    /**
     * Returns the cached authorization header value for the given host and port cache key.
     *
     * @param hostAndPort
     *            the cache key
     * @return the header value or <code>null</code> if nothing was found in the cache
     */
    private String getCachedAuthHeaderValue(final String hostAndPort)
    {
        final AuthResult authResult = authenticationCache.get(hostAndPort);
        if (authResult == null)
        {
            // nothing found in the cache
            return null;
        }

        // revalidate that the credentials provider still has credentials for the originally used auth scope
        final Credentials credentials = credentialsProvider.getCredentials(authResult.authScope);
        if (credentials == null)
        {
            // credentials have been deleted -> remove the auth result
            authenticationCache.remove(hostAndPort);
            return null;
        }

        // return the cached header value
        return authResult.headerValue;
    }

    /**
     * Derives the correct authentication cache key from the passed {@link Request} object.
     *
     * @param request
     *            the request
     * @return the matching cache key for the authentication cache
     */
    private static String getCacheKey(final Request request)
    {
        return getCacheKey(request.url().host(), request.url().port());
    }

    /**
     * Derives the correct authentication cache key from the passed {@link Route} object.
     *
     * @param route
     *            the route
     * @return the matching cache key for the authentication cache
     */
    private static String getCacheKey(final Route route)
    {
        return getCacheKey(route.socketAddress().getHostString(), route.socketAddress().getPort());
    }

    /**
     * Builds the authentication cache key from the passed host and port.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     * @return the cache key for the authentication cache
     */
    private static String getCacheKey(final String host, final int port)
    {
        return host + ":" + port;
    }

    /**
     * Returns the correct authorization header name, "Proxy-Authorization" or "Authorization".
     * 
     * @param isProxyAuth
     *            whether or not it's about proxy server authentication (instead of origin server authentication)
     * @return the header name
     */
    private static String getAuthHeaderName(final boolean isProxyAuth)
    {
        return isProxyAuth ? HttpRequestHeaders.PROXY_AUTHORIZATION : HttpRequestHeaders.AUTHORIZATION;
    }
}
