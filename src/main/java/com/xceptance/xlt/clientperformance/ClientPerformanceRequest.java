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
