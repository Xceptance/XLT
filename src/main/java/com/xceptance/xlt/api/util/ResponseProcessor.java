package com.xceptance.xlt.api.util;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.xceptance.xlt.api.actions.AbstractWebAction;

/**
 * Using response content processors, one can modify the content of a response body right after download but still
 * before the content is being parsed or compiled. This can be useful if the original content screws up HtmlUnit and the
 * application cannot be fixed right now.
 * 
 * @see AbstractWebAction#addResponseProcessor(ResponseProcessor)
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public interface ResponseProcessor
{
    /**
     * Processes the content of a response.
     * 
     * @param webResponse
     *            the web response to modify
     * @return the (potentially) modified web response
     */
    public WebResponse processResponse(WebResponse webResponse);
}
