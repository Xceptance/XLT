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
package com.xceptance.xlt.clientperformance;

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.xceptance.xlt.api.engine.RequestData;

public class ClientPerformanceRequest
{
    private final RequestData requestData = new RequestData();

    private String statusMessage;

    private String rawBody;

    private String httpMethod;

    private final List<NameValuePair> requestHeaders = new ArrayList<>();

    private final List<NameValuePair> responseHeaders = new ArrayList<>();

    private final List<NameValuePair> formDataParameters = new ArrayList<>();

    private String formData;

    private String formDataEncoding;

    public RequestData getRequestData()
    {
        return requestData;
    }

    public void setHttpMethod(String httpMethod)
    {
        this.httpMethod = httpMethod;
    }

    public String getHttpMethod()
    {
        return httpMethod;
    }

    public void setStatusMessage(String statusMessage)
    {
        this.statusMessage = statusMessage;
    }

    public String getStatusMessage()
    {
        return statusMessage;
    }

    public void setFormData(String formData)
    {
        this.formData = formData;
    }

    public String getFormData()
    {
        return formData;
    }

    public void setRawBody(String rawBody)
    {
        this.rawBody = rawBody;
    }

    public String getRawBody()
    {
        return rawBody;
    }

    public List<NameValuePair> getRequestHeaders()
    {
        return requestHeaders;
    }

    public List<NameValuePair> getResponseHeaders()
    {
        return responseHeaders;
    }

    public List<NameValuePair> getFormDataParameters()
    {
        return formDataParameters;
    }

    public String getFormDataEncoding()
    {
        return formDataEncoding;
    }

    public void setFormDataEncoding(String formDataEncoding)
    {
        this.formDataEncoding = formDataEncoding;
    }
}
