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
package com.xceptance.xlt.engine;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * A {@link WebConnection} that wraps another web connection and delegates all the hard work to it.
 */
public class WebConnectionWrapper implements WebConnection
{
    /**
     * The wrapped web connection.
     */
    private final WebConnection wrappedWebConnection;

    /**
     * Creates a new {@link WebConnectionWrapper} instance.
     * 
     * @param webConnection
     *            the web connection to be wrapped
     */
    public WebConnectionWrapper(final WebConnection webConnection)
    {
        this.wrappedWebConnection = webConnection;
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException
    {
        wrappedWebConnection.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebResponse getResponse(final WebRequest webRequest) throws IOException
    {
        return wrappedWebConnection.getResponse(webRequest);
    }

    /**
     * Returns the wrapped {@link WebConnection} object.
     * 
     * @return the wrapped web connection
     */
    public WebConnection getWrappedWebConnection()
    {
        return wrappedWebConnection;
    }
}
