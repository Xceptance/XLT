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
package com.xceptance.common.net;

/**
 * Constants used in HTTP headers.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public final class HttpHeaderConstants
{
    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private HttpHeaderConstants()
    {
    }

    /**
     * The name of the authorization header ("Authorization").
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * GZIP header value.
     */
    public static final String GZIP = "gzip";

    /**
     * The name of the content-encoding header ("Content-Encoding").
     */
    public static final String CONTENT_ENCODING = "Content-Encoding";

    /**
     * The name of the content-length header ("Content-Length").
     */
    public static final String CONTENT_LENGTH = "Content-Length";

    /**
     * The name of the cache control header ("Cache-Control").
     */
    public static final String CACHE_CONTROL = "Cache-Control";

    /**
     * The name of the date header ("Date").
     */
    public static final String DATE = "Date";

    /**
     * The name of the expires header ("Expires").
     */
    public static final String EXPIRES = "Expires";

    /**
     * The name of the if-modified-since header ("If-Modified-Since").
     */
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    /**
     * The name of the last-modified header ("Last-Modified").
     */
    public static final String LAST_MODIFIED = "Last-Modified";

    /**
     * The name of the pragma header ("Pragma").
     */
    public static final String PRAGMA = "Pragma";

    /**
     * The name of the max age header value key ("max-age").
     */
    public static final String MAX_AGE = "max-age";

    /**
     * The name of the no-cache header value ("no-cache").
     */
    public static final String NO_CACHE = "no-cache";

    /**
     * The name of the no-store header value ("no-store").
     */
    public static final String NO_STORE = "no-store";

    /**
     * The name of the must-revalidate header value ("must-revalidate").
     */
    public static final String MUST_REVALIDATE = "must-revalidate";

    /**
     * The name of the etag header ("ETag").
     */
    public static final String ETAG = "ETag";

    /**
     * The name of the if-none-match header ("If-None-Match").
     */
    public static final String IF_NONE_MATCH = "If-None-Match";

}
