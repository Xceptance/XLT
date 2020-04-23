/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.htmlunit.jetty;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.util.Attributes;

import com.xceptance.xlt.api.util.XltException;

/**
 * A special {@link BasicAuthentication} that applies to any URI in any realm.
 */
public class AnyUriAnyRealmBasicAuthentication extends BasicAuthentication
{
    /**
     * A dummy URI to make Jetty's authentication framework happy.
     */
    private static final URI ANY_URI = URI.create("http://__any__");

    /**
     * The user name to use for authentication.
     */
    private final String user;

    /**
     * The password to use for authentication.
     */
    private final String password;

    /**
     * Constructor.
     * 
     * @param user
     *            the user name
     * @param password
     *            the password
     */
    public AnyUriAnyRealmBasicAuthentication(final String user, final String password)
    {
        super(ANY_URI, Authentication.ANY_REALM, user, password);

        this.user = user;
        this.password = password;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(final String type, final URI uri, final String realm)
    {
        // ignore URI and realm
        return getType().equalsIgnoreCase(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Result authenticate(final Request request, final ContentResponse response, final HeaderInfo headerInfo, final Attributes context)
    {
        try
        {
            // derive base URI from request URI
            final URI baseUri = new URIBuilder(request.getURI()).setPath(null).removeQuery().setFragment(null).build();

            // associate the authentication result with the base URI, not the actual URI, for fewer results entries
            return new BasicResult(baseUri, headerInfo.getHeader(), user, password);
        }
        catch (final URISyntaxException e)
        {
            throw new XltException("Failed to create base URI from " + request.getURI(), e);
        }
    }
}
