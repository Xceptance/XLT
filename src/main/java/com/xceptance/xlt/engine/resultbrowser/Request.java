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
package com.xceptance.xlt.engine.resultbrowser;

import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.xceptance.xlt.api.engine.RequestData;

/**
 * The Request class holds the artifacts that come into play during one web request. These are the settings for request
 * made as well as the response returned.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class Request
{
    /**
     * The request's name.
     */
    public final String name;

    /**
     * The web request settings.
     */
    public final WebRequest webRequest;

    /**
     * The web response returned.
     */
    public final WebResponse webResponse;

    /**
     * The web request data.
     */
    public final RequestData requestData;

    /**
     * Creates a new Request object and initializes it with the given request and response objects.
     *
     * @param name
     *            the name
     * @param webRequest
     *            the web request
     * @param webResponse
     *            the web response
     * @param requestData
     *            the web request data
     */
    public Request(final String name, final WebRequest webRequest, final WebResponse webResponse, final RequestData requestData)
    {
        this.name = name;
        this.webRequest = webRequest;
        this.webResponse = webResponse;
        this.requestData = requestData;
    }
}
