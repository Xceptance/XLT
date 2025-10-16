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
package com.xceptance.xlt;

import java.io.IOException;

import org.htmlunit.MockWebConnection;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;

import com.xceptance.xlt.engine.XltWebClient;

/**
 * Mocked web connection which provides the same functionalities as its HtmlUnit counterpart.
 * <p>
 * The only difference is the post-processing of responses performed by the {@link XltWebClient}.
 * </p>
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class XltMockWebConnection extends MockWebConnection
{
    /**
     * XLT web client.
     */
    private final XltWebClient webClient;

    /**
     * Constructor.
     * 
     * @param client
     *            XLT web client to use
     */
    public XltMockWebConnection(final XltWebClient client)
    {
        webClient = client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebResponse getResponse(final WebRequest settings) throws IOException
    {
        return webClient.processResponse(super.getResponse(settings));
    }
}
