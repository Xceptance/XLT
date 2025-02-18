/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.htmlunit.apache;

import java.net.URI;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * Allows sending a DELETE request with or without a request body.
 */
class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase
{
    public HttpDeleteWithBody(final URI uri)
    {
        super();
        setURI(uri);
    }

    @Override
    public String getMethod()
    {
        return HttpDelete.METHOD_NAME;
    }
}
