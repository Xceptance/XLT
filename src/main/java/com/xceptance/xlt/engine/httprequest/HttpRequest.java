/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.htmlunit.FormEncodingType;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.util.NameValuePair;
import org.htmlunit.util.UrlUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.engine.XltWebClient;

/**
 * Encapsulation of an HTTP request.
 * <p>
 * Starting point of XLT's fluent HTTP request API.
 */
public class HttpRequest
{
    /**
     * Class logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(HttpRequest.class);

    /**
     * The web client that is used by default for performing the requests.
     */
    private static final ThreadLocal<WebClient> WEB_CLIENT = new ThreadLocal<WebClient>()
    {
        @Override
        protected WebClient initialValue()
        {
            // Ensure the thread-local is cleared at the end of a session.
            // There is no need to close the web client as it will do so by itself.
            Session.getCurrent().addShutdownListener(() -> WEB_CLIENT.remove());

            return new XltWebClient();
        }
    };

    /**
     * The timer name of this request.
     */
    protected String timerName;

    /**
     * Base URL used by this request for resolving relative URLs.
     */
    protected String baseUrl;

    /**
     * Relative URL of this request's target.
     */
    protected String relativeUrl;

    /**
     * Character set this request's content is encoded with.
     */
    protected Charset contentCharset;

    /**
     * In case this request submits a HTML form, this field describes how the form is encoded.
     */
    protected FormEncodingType encodingType;

    /**
     * The HTTP method of this request.
     */
    protected HttpMethod httpMethod;

    /**
     * The (additional) HTTP headers of this request.
     */
    protected final Map<String, String> headers = new HashMap<>();

    /**
     * The parameters of this request.
     */
    protected final List<NameValuePair> parameters = new LinkedList<>();

    /**
     * The body of this request as a string. Only valid for POST, PUT or PATCH requests. Setting a string body will
     * unset a bytes body and vice versa.
     */
    protected String body;

    /**
     * The body of this request as a byte array. Only valid for POST, PUT or PATCH requests. Setting a string body will
     * unset a bytes body and vice versa.
     */
    protected byte[] bytesBody;

    /**
     * Whether or not to allow this request to be cached during a page load.
     */
    protected boolean cachingEnabled;

    /**
     * Returns the default web client used for performing the requests. If no default web client has been set so far, a
     * fresh web client instance will be created when this method is called for the first time.
     *
     * @return the web client used to perform the request
     */
    public static WebClient getDefaultWebClient()
    {
        return WEB_CLIENT.get();
    }

    /**
     * Sets the default web client used for performing the requests.
     *
     * @return the web client used to perform the request
     */
    public static void setDefaultWebClient(final WebClient webClient)
    {
        WEB_CLIENT.set(webClient);
    }

    /**
     * Creates a new request.
     */
    public HttpRequest()
    {
        // Empty
    }

    /**
     * Creates a new request using the given time name.
     *
     * @param timerName
     *            the timer name to use
     */
    public HttpRequest(final String timerName)
    {
        this.timerName = timerName;
    }

    /**
     * Creates a new request by using another request as template.
     *
     * @param other
     *            the request to use as template
     */
    public HttpRequest(final HttpRequest other)
    {
        timerName = other.timerName;
        baseUrl = other.baseUrl;
        relativeUrl = other.relativeUrl;
        contentCharset = other.contentCharset;
        encodingType = other.encodingType;
        httpMethod = other.httpMethod;

        headers.putAll(other.headers);
        parameters.addAll(other.parameters);

        body = other.body;
        cachingEnabled = other.cachingEnabled;
    }

    /**
     * Sets the timer name to be used by this request.
     *
     * @param timerName
     *            the timer name
     */
    public HttpRequest timerName(final String timerName)
    {
        this.timerName = timerName;

        return this;
    }

    /**
     * Sets the base URL to be used by this request.
     *
     * @param baseUrl
     *            the base URL as string
     */
    public HttpRequest baseUrl(final String baseUrl)
    {
        this.baseUrl = baseUrl;

        return this;
    }

    /**
     * Sets the relative URL to be used by this request.
     *
     * @param relativeUrl
     *            the relative URL as string
     */
    public HttpRequest relativeUrl(final String relativeUrl)
    {
        this.relativeUrl = relativeUrl;

        return this;
    }

    /**
     * Sets the HTTP method of this request.
     *
     * @param httpMethod
     *            the HTTP method
     */
    public HttpRequest method(final HttpMethod httpMethod)
    {
        this.httpMethod = httpMethod;

        return this;
    }

    /**
     * Sets the content's character set.
     *
     * @param contentCharset
     *            the character set the content sent by this request is content with
     */
    public HttpRequest charset(final Charset contentCharset)
    {
        this.contentCharset = contentCharset;

        return this;
    }

    /**
     * Sets the HTML form encoding type.
     *
     * @param encodingType
     *            the form encoding type
     */
    public HttpRequest encodingType(final FormEncodingType encodingType)
    {
        this.encodingType = encodingType;

        return this;
    }

    /**
     * Sets the HTTP request header with the given name to the given value.
     *
     * @param name
     *            the name of the header
     * @param value
     *            the value of the header
     */
    public HttpRequest header(final String name, String value)
    {
        if (StringUtils.isBlank(name))
        {
            throw new IllegalArgumentException("Header name must not be blank.");
        }

        if (value == null)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Header value 'null' was converted into empty string for header name " + name);
            }

            value = StringUtils.EMPTY;
        }

        headers.put(name, value);

        return this;
    }

    /**
     * Sets the given request headers.
     *
     * @param additionalHeaders
     *            the request headers to set
     */
    public HttpRequest headers(final List<NameValuePair> additionalHeaders)
    {
        additionalHeaders.stream().forEach(p -> header(p.getName(), p.getValue()));

        return this;
    }

    /**
     * Sets the given request headers.
     *
     * @param additionalHeaders
     *            the request headers to set
     */
    public HttpRequest headers(final Map<String, String> additionalHeaders)
    {
        additionalHeaders.forEach((n, v) -> header(n, v));

        return this;
    }

    /**
     * Removes the request header for the given name.
     *
     * @param name
     *            the name of the header to remove
     */
    public HttpRequest removeHeader(final String name)
    {
        headers.remove(name);

        return this;
    }

    /**
     * Removes all of the given request headers.
     *
     * @param headerNames
     *            the name of the headers to remove
     */
    public HttpRequest removeHeaders(final List<String> headerNames)
    {
        headerNames.forEach(n -> removeHeader(n));

        return this;
    }

    /**
     * Removes all request headers.
     */
    public HttpRequest removeHeaders()
    {
        headers.clear();

        return this;
    }

    /**
     * Adds a request parameter with the given name and value.
     *
     * @param name
     *            the name of the parameter to add
     * @param value
     *            the value of the parameter to add
     */
    public HttpRequest param(final String name, String value)
    {
        return param(new NameValuePair(name, value));
    }

    /**
     * Adds a request parameter with the given name/value pair.
     *
     * @param nameValuePair
     *            the name/value pair representing the parameter to add
     */
    public HttpRequest param(NameValuePair nameValuePair)
    {
        String name = nameValuePair.getName();

        if (StringUtils.isBlank(name))
        {
            throw new IllegalArgumentException("Name of parameter must not be blank.");
        }

        // validate name/value pairs only, but not subclasses like key/data pairs, etc.
        if (nameValuePair.getClass() == NameValuePair.class)
        {
            if (nameValuePair.getValue() == null)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Value of parameter '" + name + "' was converted from 'null' to an empty string");
                }

                nameValuePair = new NameValuePair(name, StringUtils.EMPTY);
            }
        }

        parameters.add(nameValuePair);

        return this;
    }

    /**
     * Adds all of the given request parameters.
     *
     * @param params
     *            the parameters to add
     */
    public HttpRequest params(final List<NameValuePair> params)
    {
        params.forEach(this::param);

        return this;
    }

    /**
     * Adds all of the given request parameters.
     *
     * @param params
     *            the parameters to add
     */
    public HttpRequest params(final Map<String, String> params)
    {
        params.forEach((n, v) -> param(n, v));

        return this;
    }

    /**
     * Removes the request parameter with the given name.
     *
     * @param name
     *            the name of the parameter to remove
     */
    public HttpRequest removeParam(final String name)
    {
        parameters.removeIf(p -> p.getName().equals(name));

        return this;
    }

    /**
     * Removes all of the given request parameters.
     *
     * @param paramNames
     *            the names of the parameters to remove
     */
    public HttpRequest removeParams(final List<String> paramNames)
    {
        paramNames.forEach(n -> removeParam(n));

        return this;
    }

    /**
     * Removes all request parameters.
     */
    public HttpRequest removeParams()
    {
        parameters.clear();

        return this;
    }

    /**
     * Sets the given string as the textual body of this request. Any textual or binary body set previously will be
     * discarded.
     *
     * @param body
     *            the request body as string
     */
    public HttpRequest body(final String body)
    {
        this.body = body;
        this.bytesBody = null;

        return this;
    }

    /**
     * Sets the given byte array as the binary body of this request. Any textual or binary body set previously will be
     * discarded.
     *
     * @param bytes
     *            the request body as byte array
     */
    public HttpRequest body(final byte[] bytes)
    {
        this.bytesBody = bytes;
        this.body = null;

        return this;
    }

    /**
     * Sets the content of the given file as the binary body of this request. Any textual or binary body set previously
     * will be discarded.
     * <p>
     * Note: This method reads the file completely into memory and afterwards calls {@link #body(byte[])} with the data
     * read.
     *
     * @param file
     *            the file from which to read the request body
     * @throws IOException
     *             if the file could not be read
     */
    public HttpRequest body(final File file) throws IOException
    {
        final byte[] bytes = FileUtils.readFileToByteArray(file);

        return body(bytes);
    }

    /**
     * Sets the content of the given input stream as the binary body of this request. Any textual or binary body set
     * previously will be discarded.
     * <p>
     * Note: This method reads the input stream completely into memory and afterwards calls {@link #body(byte[])} with
     * the data read. The input stream will not be closed.
     *
     * @param inputStream
     *            the input stream from which to read the request body
     * @throws IOException
     *             if the input stream could not be read
     */
    public HttpRequest body(final InputStream inputStream) throws IOException
    {
        final byte[] bytes = IOUtils.toByteArray(inputStream);

        return body(bytes);
    }

    /**
     * Whether or not to allow this request to be cached during a page load
     *
     * @param cachingEnabled
     *            whether or not to allow this request to be cached during a page load
     */
    public HttpRequest caching(final boolean cachingEnabled)
    {
        this.cachingEnabled = cachingEnabled;

        return this;
    }

    /**
     * Performs this request and returns the resulting response.
     *
     * @return response of this request
     * @throws IOException
     *             thrown on HTTP transmission failure
     * @throws URISyntaxException
     *             thrown on failure to build the request URl
     */
    public HttpResponse fire() throws IOException, URISyntaxException
    {
        return fire(getDefaultWebClient());
    }

    /**
     * Performs this request using the given web client and returns the resulting response.
     *
     * @param webClient
     *            the web client to use for performing the request
     * @return response of this request
     * @throws IOException
     *             thrown on transmission failure
     * @throws URISyntaxException
     *             thrown on failure to build the request URl
     */
    public HttpResponse fire(final WebClient webClient) throws IOException, URISyntaxException
    {
        if (webClient == null)
        {
            throw new IllegalArgumentException("Can not utilize invalid web client.");
        }

        if (StringUtils.isNotBlank(timerName) && (webClient instanceof XltWebClient))
        {
            ((XltWebClient) webClient).setTimerName(timerName.trim());
        }

        final WebRequest webRequest = buildWebRequest();
        final WebResponse webResponse = webClient.loadWebResponse(webRequest);

        return new HttpResponse(webResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpRequest clone()
    {
        return new HttpRequest(this);
    }

    /**
     * Builds a new HtmlUnit request using this request's settings.
     *
     * @return this request as HtmlUnit request
     * @throws MalformedURLException
     *             thrown on failure to build this request's target URL
     * @throws URISyntaxException
     *             thrown on failure to transform the request URL to an URI
     */
    protected WebRequest buildWebRequest() throws MalformedURLException, URISyntaxException
    {
        final boolean methodSupportsBody = (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT ||
                                            httpMethod == HttpMethod.PATCH || httpMethod == HttpMethod.DELETE);

        // basic parameter validation
        Assert.assertTrue("Base URL must not be null or blank", StringUtils.isNotBlank(baseUrl));
        Assert.assertTrue("Can not use request parameters in conjunction with request body in POST, PUT, PATCH, or DELETE requests",
                          !methodSupportsBody || (body == null && bytesBody == null) || parameters.isEmpty());

        // Evaluate URL and create web request
        final URL url;
        if (StringUtils.isBlank(relativeUrl))
        {
            url = new URL(baseUrl);
        }
        else
        {
            url = new URL(UrlUtils.resolveUrl(baseUrl, relativeUrl));
        }

        final WebRequest webRequest = new WebRequest(url);

        // Handle method
        if (httpMethod != null)
        {
            webRequest.setHttpMethod(httpMethod);
        }

        // Handle headers
        if (contentCharset != null)
        {
            webRequest.setCharset(contentCharset);
        }

        if (methodSupportsBody && encodingType != null)
        {
            webRequest.setEncodingType(encodingType);
        }

        if (!headers.isEmpty())
        {
            webRequest.setAdditionalHeaders(headers);
        }

        // Handle parameters
        handleParameters(webRequest, parameters, methodSupportsBody);

        // Handle body
        if (methodSupportsBody)
        {
            // Assumes no parameters have been specified

            if (body != null)
            {
                webRequest.setRequestBody(body);
            }
            else if (bytesBody != null)
            {
                // Since HtmlUnit accepts only strings as body, we have to cheat a little here.

                // set bytes as ISO-8859-1-encoded string which, on the wire, looks the same as the bytes
                final String bytesAsString = new String(bytesBody, StandardCharsets.ISO_8859_1);
                webRequest.setRequestBody(bytesAsString);

                // override any custom charset
                webRequest.setCharset(StandardCharsets.ISO_8859_1);
            }
        }

        if (!cachingEnabled)
        {
            webRequest.setDocumentRequest();
        }

        return webRequest;
    }

    /**
     * Sets the given custom parameters at the web request taking already existing URL query parameters into
     * consideration.
     *
     * @param webRequest
     *            the web request
     * @param parameters
     *            the custom request parameters
     * @param methodSupportsBody
     *            whether the HTTP method may have a request body
     */
    private void handleParameters(final WebRequest webRequest, final List<NameValuePair> parameters, final boolean methodSupportsBody)
        throws URISyntaxException, MalformedURLException
    {
        if (!parameters.isEmpty())
        {
            if (methodSupportsBody)
            {
                // remove any parameter from the URL that is also part of the custom parameters
                if (StringUtils.isNotEmpty(webRequest.getUrl().getQuery()))
                {
                    adjustUrl(webRequest, parameters, false);
                }

                // set custom parameters
                webRequest.setRequestParameters(parameters);
            }
            else
            {
                /*
                 * #3795: Don't set custom parameters at the web request in this case. The final URL would be built in
                 * transient form only and only right before sending the request to the wire, so XLT is not able to pick
                 * it up. Hence, we build the final URL now.
                 */

                // remove any parameter from the URL that is also part of the custom parameters and append all custom
                // parameters to the URL
                adjustUrl(webRequest, parameters, true);
            }
        }
    }

    /**
     * Updates the URL stored at the given web request such that:
     * <ol>
     * <li>Any query parameter that is contained in the passed list of custom parameters is removed from the request
     * URL.</li>
     * <li>Optionally, all custom parameters are appended to the query string of the request URL.</li>
     * </ol>
     * 
     * @param webRequest
     *            the web request
     * @param parameters
     *            the custom request parameters
     * @param addParameters
     *            whether to add the custom request parameters to the URL
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    private void adjustUrl(WebRequest webRequest, final List<NameValuePair> parameters, boolean addParameters)
        throws MalformedURLException, URISyntaxException
    {
        final URL url = webRequest.getUrl();
        final URIBuilder uriBuilder = new URIBuilder(url.toURI());

        // remove URL parameters that are contained in the custom parameters
        final List<org.apache.http.NameValuePair> urlParameters = uriBuilder.getQueryParams();
        parameters.forEach(p -> urlParameters.removeIf(u -> u.getName().equals(p.getName())));
        uriBuilder.setParameters(urlParameters);

        // optionally add all custom parameters to the URL
        if (addParameters)
        {
            parameters.forEach(p -> uriBuilder.addParameter(p.getName(), p.getValue()));
        }

        // finally build and set the new URL
        webRequest.setUrl(new URL(uriBuilder.toString()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.toString(this);
    }
}
