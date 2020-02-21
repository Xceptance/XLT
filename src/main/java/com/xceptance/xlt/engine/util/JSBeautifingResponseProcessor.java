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
