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
package com.xceptance.xlt.engine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.htmlunit.WebConnection;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.WebResponseData;
import org.htmlunit.util.NameValuePair;

/**
 * Class description.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class XltOfflineWebConnection implements WebConnection
{
    /**
     * Static page fragment
     */
    private static final String SIMPLE_HTML_PAGE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">" +
                                                   "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\"> " + "<head>" +
                                                   "   <title>XLT-Test</title>" + "</head>" + "<body>" + "   <div id=\"container\">" +
                                                   "        <div id=\"header\">" + "            <ul>" +
                                                   "                <li><a title=\"Link1 Text\" href=\"http://link1.com/\">Link1</li>" +
                                                   "                <li><a title=\"Link1 Text\" href=\"http://link2.com/\">Link2</li>" +
                                                   "                <li><a title=\"Link1 Text\" href=\"http://link3.com/\">Link3</li>" +
                                                   "            </ul>" + "        </div>" + "       " + "       <div id=\"main\">" +
                                                   "           <h1>Headline1</h1>" +
                                                   "           <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna " +
                                                   "              aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                                                   "              Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint " +
                                                   "              occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p>" +
                                                   "       </div>" + "   </div>" + "</body>" + "</html>";

    /**
     * A faked html response
     */
    private static final byte[] HTML_RESPONSE_BODY = SIMPLE_HTML_PAGE.getBytes(StandardCharsets.UTF_8);

    /**
     * A constant for an empty header list.
     */
    private static final List<NameValuePair> HTML_RESPONSE_HEADER_LIST;

    static
    {
        HTML_RESPONSE_HEADER_LIST = new ArrayList<NameValuePair>();
        HTML_RESPONSE_HEADER_LIST.add(new NameValuePair("Content-Type", "text/html; charset=UTF-8"));
        HTML_RESPONSE_HEADER_LIST.add(new NameValuePair("Content-Length", String.valueOf(HTML_RESPONSE_BODY.length)));
        HTML_RESPONSE_HEADER_LIST.add(new NameValuePair("Cache-Control", "no-cache"));
        HTML_RESPONSE_HEADER_LIST.add(new NameValuePair("Expires", "Thu, 01 Dec 1994 16:00:00 GMT"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebResponse getResponse(WebRequest webRequest)
    {

        // create a dummy response data with an appropriate (?) status code
        final WebResponseData webResponseData = new WebResponseData(HTML_RESPONSE_BODY, HttpStatus.SC_OK,
                                                                    EnglishReasonPhraseCatalog.INSTANCE.getReason(HttpStatus.SC_OK, null),
                                                                    HTML_RESPONSE_HEADER_LIST);
        try
        {
            Thread.sleep(45);
        }
        catch (final InterruptedException e1)
        {
        }

        // create response using dummy data
        return new WebResponse(webResponseData, webRequest.getUrl(), webRequest.getHttpMethod(), 45);
    }
}
