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
