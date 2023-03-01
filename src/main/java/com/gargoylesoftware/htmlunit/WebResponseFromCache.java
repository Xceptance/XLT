/*
 * Copyright (c) 2002-2022 Gargoyle Software Inc.
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
package com.gargoylesoftware.htmlunit;

import com.gargoylesoftware.htmlunit.util.WebResponseWrapper;

/**
 * A {@link WebResponse} implementation to deliver with content from cache. The response
 * is the same but the request may have some variation like an anchor.
 *
 * @author Marc Guillemot
 */
class WebResponseFromCache extends WebResponseWrapper {

    private final WebRequest request_;

    /**
     * Wraps the provide response for the given request
     * @param cachedResponse the response from cache
     * @param currentRequest the new request
     */
    WebResponseFromCache(final WebResponse cachedResponse, final WebRequest currentRequest) {
        super(cachedResponse);
        request_ = currentRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebRequest getWebRequest() {
        return request_;
    }
}
