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
package com.xceptance.xlt.engine.htmlunit.apache5;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.core5.http.protocol.HttpContext;

/**
 * A {@link CredentialsProvider} that is backed by HtmlUnit's credentials provider.
 */
class CredentialsProviderImpl implements CredentialsProvider
{
    private final org.apache.http.client.CredentialsProvider credentialsProvider;

    /**
     */
    public CredentialsProviderImpl(final org.apache.http.client.CredentialsProvider credentialsProvider)
    {
        this.credentialsProvider = credentialsProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Credentials getCredentials(AuthScope authScope, HttpContext context)
    {
        return null;
    }
}
