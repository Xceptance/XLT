package com.xceptance.xlt.engine.htmlunit.jetty;

import org.eclipse.jetty.client.ContentDecoder;

/**
 * A {@link ContentDecoder.Factory} that creates no-op content decoders.
 * 
 * @see NoOpContentDecoder
 */
public class NoOpContentDecoderFactory extends ContentDecoder.Factory
{
    /**
     * A factory for no-op content decoders dealing with the "deflate" encoding.
     */
    public static final NoOpContentDecoderFactory DEFLATE = new NoOpContentDecoderFactory("deflate");

    /**
     * A factory for no-op content decoders dealing with the "gzip" encoding.
     */
    public static final NoOpContentDecoderFactory GZIP = new NoOpContentDecoderFactory("gzip");

    /**
     * Creates a new no-op content decoder factory with the passed encoding name.
     * 
     * @param encoding
     *            the encoding name
     */
    public NoOpContentDecoderFactory(final String encoding)
    {
        super(encoding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContentDecoder newContentDecoder()
    {
        return new NoOpContentDecoder();
    }
}
