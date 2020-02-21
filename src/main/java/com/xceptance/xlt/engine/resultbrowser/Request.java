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
