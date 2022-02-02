/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.engine;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.xceptance.common.util.ParameterCheckUtils;

/**
 * The {@link NetworkData} class holds a certain web request and the corresponding web response if there was any.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class NetworkData
{
    /**
     * The request.
     */
    private final WebRequest request;

    /**
     * The response.
     */
    private final WebResponse response;

    /**
     * Constructor.
     * 
     * @param request
     *            the request
     * @param response
     *            the response (may be <code>null</code>)
     */
    public NetworkData(final WebRequest request, final WebResponse response)
    {
        ParameterCheckUtils.isNotNull(request, "request");

        this.request = request;
        this.response = response;
    }

    /**
     * Returns the request method.
     * 
     * @return the request method
     */
    public HttpMethod getRequestMethod()
    {
        return request.getHttpMethod();
    }

    /**
     * Returns the request body.
     * 
     * @return the request body
     */
    public String getRequestBody()
    {
        return request.getRequestBody();
    }

    /**
     * Returns the request parameters.
     * 
     * @return the request parameters
     */
    public List<NameValuePair> getRequestParameters()
    {
        return Collections.unmodifiableList(request.getRequestParameters());
    }

    /**
     * Returns the additional request HTTP headers.
     * 
     * @return the additional request HTTP headers
     */
    public Map<String, String> getAdditionalRequestHeaders()
    {
        return Collections.unmodifiableMap(request.getAdditionalHeaders());
    }

    /**
     * Returns the response content.
     * 
     * @return the response content
     */
    public String getContentAsString()
    {
        return response != null ? response.getContentAsString() : null;
    }

    /**
     * Returns the content type of the response.
     * 
     * @return the content type of the response
     */
    public String getContentType()
    {
        return response != null ? response.getContentType() : null;
    }

    /**
     * Returns the response status message.
     * 
     * @return the response status message
     */
    public String getResponseStatusMessage()
    {
        return response != null ? response.getStatusMessage() : null;
    }

    /**
     * Returns the response status code.
     * 
     * @return the response status code
     */
    public int getResponseStatusCode()
    {
        return response != null ? response.getStatusCode() : 0;
    }

    /**
     * Returns the response HTTP headers.
     * 
     * @return the response HTTP headers
     */
    public List<NameValuePair> getResponseHeaders()
    {
        return response != null ? Collections.unmodifiableList(response.getResponseHeaders()) : Collections.<NameValuePair>emptyList();
    }

    /**
     * Returns the request URL.
     * 
     * @return the request URL
     */
    public URL getURL()
    {
        return request.getUrl();
    }

    /**
     * Returns the underlying request object for direct access.
     * 
     * @return the request
     */
    public WebRequest getRequest()
    {
        return request;
    }

    /**
     * Returns the underlying response object for direct access.
     * 
     * @return the response
     */
    public WebResponse getResponse()
    {
        return response;
    }
}
