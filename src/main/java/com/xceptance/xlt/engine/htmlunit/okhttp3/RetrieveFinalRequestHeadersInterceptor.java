/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.htmlunit.okhttp3;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.htmlunit.WebRequest;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Request interceptor that stores the final set of request headers (i.e. including those automatically added by the
 * HTTP client) at the original {@link WebRequest} instance for later display in the result browser.
 */
class RetrieveFinalRequestHeadersInterceptor implements Interceptor
{
    private final WebRequest webRequest;

    RetrieveFinalRequestHeadersInterceptor(final WebRequest webRequest)
    {
        this.webRequest = webRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response intercept(final Chain chain) throws IOException
    {
        final Request request = chain.request();

        webRequest.setAdditionalHeaders(toMap(request.headers()));

        return chain.proceed(request);
    }

    /**
     * Converts an OkHttp {@link Headers} object to a corresponding map.
     */
    private static Map<String, String> toMap(final Headers okHttpHeaders)
    {
        final Map<String, String> htmlUnitHeaders = new LinkedHashMap<>();

        for (int i = 0; i < okHttpHeaders.size(); i++)
        {
            htmlUnitHeaders.put(okHttpHeaders.name(i), okHttpHeaders.value(i));
        }

        return htmlUnitHeaders;
    }
}
