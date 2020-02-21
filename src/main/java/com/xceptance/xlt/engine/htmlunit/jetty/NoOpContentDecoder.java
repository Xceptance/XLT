package com.xceptance.xlt.engine.htmlunit.jetty;

import java.nio.ByteBuffer;

import org.eclipse.jetty.client.ContentDecoder;

/**
 * A special "decoder" for compressed response content that simply returns the response data unchanged. This class is
 * necessary as HtmlUnit tries to decode the data itself once a Content-Encoding header is present in the response.
 * 
 * @see NoOpContentDecoderFactory
 */
public class NoOpContentDecoder implements ContentDecoder
{
    /**
     * {@inheritDoc}
     */
    @Override
    public ByteBuffer decode(final ByteBuffer buffer)
    {
        // make a copy of the original content
        final ByteBuffer result = buffer.duplicate();

        // now "consume" the original buffer
        buffer.position(buffer.limit());

        return result;
    }
}
