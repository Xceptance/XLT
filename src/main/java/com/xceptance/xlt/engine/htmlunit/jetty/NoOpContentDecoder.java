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
