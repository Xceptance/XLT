/*
 * Copyright (c) 2002-2024 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.htmlunit.util;

import java.io.IOException;

import org.htmlunit.WebClient;
import org.htmlunit.WebConnection;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;

/**
 * <p>Provides a convenient implementation of the {@link WebConnection} interface that can be subclassed by developers
 * wishing to adapt a particular WebConnection.</p>
 *
 * <p>This class implements the Wrapper or Decorator pattern. Methods default to calling through to the wrapped
 * web connection object.</p>
 *
 * @author Marc Guillemot
 * @author Ahmed Ashour
 */
public class WebConnectionWrapper implements WebConnection {
    private final WebConnection wrappedWebConnection_;

    /**
     * Constructs a WebConnection object wrapping provided WebConnection.
     * @param webConnection the webConnection that does the real work
     * @throws IllegalArgumentException if the connection is {@code null}
     */
    public WebConnectionWrapper(final WebConnection webConnection) throws IllegalArgumentException {
        if (webConnection == null) {
            throw new IllegalArgumentException("Wrapped connection can't be null");
        }
        wrappedWebConnection_ = webConnection;
    }

    /**
     * Constructs a WebConnection object wrapping the connection of the WebClient and places itself as
     * connection of the WebClient.
     * @param webClient the WebClient which WebConnection should be wrapped
     * @throws IllegalArgumentException if the WebClient is {@code null}
     */
    public WebConnectionWrapper(final WebClient webClient) throws IllegalArgumentException {
        if (webClient == null) {
            throw new IllegalArgumentException("WebClient can't be null");
        }
        wrappedWebConnection_ = webClient.getWebConnection();
        webClient.setWebConnection(this);
    }

    /**
     * {@inheritDoc}
     * The default behavior of this method is to return {@link WebConnection#getResponse(WebRequest)}
     * on the wrapped connection object.
     */
    @Override
    public WebResponse getResponse(final WebRequest request) throws IOException {
        return wrappedWebConnection_.getResponse(request);
    }

    /**
     * Gets the wrapped {@link WebConnection}.
     * @return the wrapped connection
     */
    public WebConnection getWrappedWebConnection() {
        return wrappedWebConnection_;
    }

    /**
     * {@inheritDoc}
     * The default behavior of this method is to return {@link WebConnection#close()} on the wrapped connection object.
     */
    @Override
    public void close() throws IOException {
        wrappedWebConnection_.close();
    }
}
