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
package com.xceptance.xlt.engine.httprequest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.xceptance.common.util.ParameterCheckUtils;

/**
 * Encapsulates an HTTP response and provides methods for basic validation.
 */
public class HttpResponse
{
    /**
     * The encapsulated HtmlUnit response.
     */
    private final WebResponse webResponse;

    /**
     * Creates a new HTTP response for the given HtmlUnit response.
     *
     * @param webResponse
     *            the HtmlUnit response
     */
    public HttpResponse(final WebResponse webResponse)
    {
        ParameterCheckUtils.isNotNull(webResponse, "webResponse");
        this.webResponse = webResponse;
    }

    /**
     * Asserts that this response' status code is the same as the given one.
     *
     * @param expectedStatusCode
     *            the expected status code
     */
    public HttpResponse checkStatusCode(final int expectedStatusCode)
    {
        Assert.assertEquals("Status code does not match", expectedStatusCode, webResponse.getStatusCode());

        return this;
    }

    /**
     * Asserts that this response' status message is the same as the given one.
     *
     * @param expectedStatusMessage
     *            the expected status message
     */
    public HttpResponse checkStatusMessage(final String expectedStatusMessage)
    {
        Assert.assertEquals("Status message does not match", expectedStatusMessage, webResponse.getStatusMessage());

        return this;
    }

    /**
     * Asserts that the content type of this response is the same as the given one.
     *
     * @param expectedContentType
     *            the expected content type
     */
    public HttpResponse checkContentType(final String expectedContentType)
    {
        Assert.assertEquals("Content type does not match", expectedContentType, webResponse.getContentType());

        return this;
    }

    /**
     * Checks that the server sent an HTTP header with the given name and value.
     *
     * @param name
     *            the name of the HTTP header
     * @param expectedValue
     *            the value of the HTTP header
     */
    public HttpResponse checkHeaderValue(final String name, final String expectedValue)
    {
        Assert.assertFalse("Header name can not be blank", StringUtils.isBlank(name));
        Assert.assertEquals("Header " + name + " does not match", expectedValue, webResponse.getResponseHeaderValue(name));

        return this;
    }

    /**
     * Returns the status code of this response.
     *
     * @return this response' status code
     */
    public int getStatusCode()
    {
        return webResponse.getStatusCode();
    }

    /**
     * Returns the status message of this response.
     *
     * @return this response' status message
     */
    public String getStatusMessage()
    {
        return webResponse.getStatusMessage();
    }

    /**
     * Returns the character set this response' content is encoded with.
     *
     * @return this response' content character set
     */
    public Charset getContentCharset()
    {
        return webResponse.getContentCharset();
    }

    /**
     * Returns the content type of this response.
     *
     * @return this response' content type
     */
    public String getContentType()
    {
        return webResponse.getContentType();
    }

    /**
     * Returns the content length of this response.
     *
     * @return this response' content length
     */
    public long getContentLength()
    {
        return webResponse.getContentLength();
    }

    /**
     * Returns the value of the given HTTP header.
     *
     * @param name
     *            the name of the HTTP header
     * @return value of the HTTP header for the given name if it exists, {@code null} otherwise
     */
    public String getHeaderValue(final String name)
    {
        return webResponse.getResponseHeaderValue(name);
    }

    /**
     * Returns the HTTP headers of this response.
     *
     * @return this response' HTTP headers
     */
    public List<NameValuePair> getHeaders()
    {
        return webResponse.getResponseHeaders();
    }

    /**
     * Returns the content of this response (decoded using this response' character set).
     *
     * @return this response' content
     */
    public String getContentAsString()
    {
        return webResponse.getContentAsString();
    }

    /**
     * Returns the content of this response decoded using the given character set.
     *
     * @param encoding
     *            the character to use for decoding
     * @return this response' content decoded using the given character set
     */
    public String getContentAsString(final Charset encoding)
    {
        return webResponse.getContentAsString(encoding);
    }

    /**
     * Parses the content of this response as JSON and returns the parsed object.
     *
     * @return this response' content as JSON object
     */
    public JSONObject getContentAsJSONObject()
    {
        return new JSONObject(webResponse.getContentAsString());
    }

    /**
     * Parses the content of this response as XML and returns the resulting document.
     *
     * @return this response' content as XML document
     * @throws ParserConfigurationException
     *             thrown on XML parser configuration failure
     * @throws SAXException
     *             thrown on XML parser error
     * @throws IOException
     *             thrown on I/O error
     */
    public Document getContentAsDocument() throws ParserConfigurationException, SAXException, IOException
    {
        final DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

        return documentBuilder.parse(webResponse.getContentAsStream());
    }

    /**
     * Returns the underlying HtmlUnit response.
     *
     * @return the underlying HtmlUnit response
     */
    public WebResponse getWebResponse()
    {
        return webResponse;
    }
}
