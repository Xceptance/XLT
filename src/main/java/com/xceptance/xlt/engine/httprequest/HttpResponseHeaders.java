/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.httprequest;

/**
 * Frequently used HTTP response header names.
 */
public interface HttpResponseHeaders
{
    /**
     * The name of the accept-ranges header ("Accept-Ranges").
     */
    public final String ACCEPT_RANGES = "Accept-Ranges";

    /**
     * The name of the age header ("Age").
     */
    public final String AGE = "Age";

    /**
     * The name of the allow header ("Allow").
     */
    public final String ALLOW = "Allow";

    /**
     * The name of the cache-control header ("Cache-Control").
     */
    public final String CACHE_CONTROL = "Cache-Control";

    /**
     * The name of the connection header ("Connection").
     */
    public final String CONNECTION = "Connection";

    /**
     * The name of the content-encoding header ("Content-Encoding").
     */
    public final String CONTENT_ENCODING = "Content-Encoding";

    /**
     * The name of the content-language header ("Content-Language").
     */
    public final String CONTENT_LANGUAGE = "Content-Language";

    /**
     * The name of the content-length header ("Content-Length").
     */
    public final String CONTENT_LENGTH = "Content-Length";

    /**
     * The name of the content-location header ("Content-Location").
     */
    public final String CONTENT_LOCATION = "Content-Location";

    /**
     * The name of the content-md5 header ("Content-MD5").
     */
    public final String CONTENT_MD5 = "Content-MD5";

    /**
     * The name of the content-disposition header ("Content-Disposition").
     */
    public final String CONTENT_DISPOSITION = "Content-Disposition";

    /**
     * The name of the content-range header ("Content-Range").
     */
    public final String CONTENT_RANGE = "Content-Range";

    /**
     * The name of the content-security-policy header ("Content-Security-Policy").
     */
    public final String CONTENT_SECURITY_POLICY = "Content-Security-Policy";

    /**
     * The name of the content-type header ("Content-Type").
     */
    public final String CONTENT_TYPE = "Content-Type";

    /**
     * The name of the date header ("Date").
     */
    public final String DATE = "Date";

    /**
     * The name of the etag header ("ETag").
     */
    public final String ETAG = "ETag";

    /**
     * The name of the expires header ("Expires").
     */
    public final String EXPIRES = "Expires";

    /**
     * The name of the last-modified header ("Last-Modified").
     */
    public final String LAST_MODIFIED = "Last-Modified";

    /**
     * The name of the link header ("Link").
     */
    public final String LINK = "Link";

    /**
     * The name of the location header ("Location").
     */
    public final String LOCATION = "Location";

    /**
     * The name of the platform-for-privacy-preferences header ("P3P").
     */
    public final String P3P = "P3P";

    /**
     * The name of the pragma header ("Pragma").
     */
    public final String PRAGMA = "Pragma";

    /**
     * The name of the proxy-authentication header ("Proxy-Authenticate").
     */
    public final String PROXY_AUTHENTICATE = "Proxy-Authenticate";

    /**
     * The name of the (page) refresh header ("Refresh").
     */
    public final String REFRESH = "Refresh";

    /**
     * The name of the retry-after header ("Retry-After").
     */
    public final String RETRY_AFTER = "Retry-After";

    /**
     * The name of the server header ("Server").
     */
    public final String SERVER = "Server";

    /**
     * The name of the set-cookie header ("Set-Cookie").
     */
    public final String SET_COOKIE = "Set-Cookie";

    /**
     * The name of the trailer header ("Trailer").
     */
    public final String TRAILER = "Trailer";

    /**
     * The name of the transfer-encoding header ("Transfer-Encoding").
     */
    public final String TRANSFER_ENCODING = "Transfer-Encoding";

    /**
     * The name of the vary header ("Vary").
     */
    public final String VARY = "Vary";

    /**
     * The name of the via header ("Via").
     */
    public final String VIA = "Via";

    /**
     * The name of the warning header ("Warning").
     */
    public final String WARNING = "Warning";

    /**
     * The name of the authentication-challenges header ("WWW-Authenticate") - 401 (Unauthorized) responses only.
     */
    public final String WWW_AUTHENTICATE = "WWW-Authenticate";

    // Not in standard
    /**
     * The name of the custom frame options header ("X-Frame-Options").
     */
    public final String X_FRAME_OPTIONS = "X-Frame-Options";

    /**
     * The name of the custom cross-site-scripting-protection header ("X-XSS-Protection").
     */
    public final String X_XSS_PROTECTION = "X-XSS-Protection";

    /**
     * The name of the custom content-type-options header ("X-Content-Type-Options").
     */
    public final String X_CONTENT_TYPE_OPERATIONS = "X-Content-Type-Options";

    /**
     * The name of the custom powered-by header ("X-Powered-By").
     */
    public final String X_POWERED_BY = "X-Powered-By";

    /**
     * The name of the custom user-agent-compatible header ("X-UA-Compatible").
     */
    public final String X_UA_COMPATIBLE = "X-UA-Compatible";

    /**
     * The name of the custom robots-tag header ("X-Robots-Tag").
     */
    public final String X_ROBOTS_TAG = "X-Robots-Tag";
}
