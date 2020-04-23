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
package com.xceptance.xlt.engine.util;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.util.AbstractResponseProcessor;

/**
 * Response processor that beautifies JavaScript code.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class JSBeautifingResponseProcessor extends AbstractResponseProcessor
{
    /**
     * Returns whether or not the given content-type denotes JavaScript content.
     * 
     * @param contentType
     *            the given content-type
     * @return <code>true</code> if the given content-type denotes JavaScript content, <code>false</code> otherwise.
     */
    protected boolean isJSContent(final String contentType)
    {
        return RegExUtils.isMatching(contentType, "text/javascript|application/((x-)?java|ecma)script");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebResponse processResponse(final WebResponse webResponse)
    {
        if (webResponse != null && isJSContent(webResponse.getContentType()))
        {
            return createWebResponse(webResponse, JSBeautifier.beautify(webResponse.getContentAsString()));
        }

        return webResponse;
    }

}
