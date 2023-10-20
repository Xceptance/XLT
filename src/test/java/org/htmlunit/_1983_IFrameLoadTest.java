/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package org.htmlunit;

import java.net.URL;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.engine.XltEngine;
import com.xceptance.xlt.engine.XltWebClient;

/**
 * Tests correct loading of iframe elements. See XLT#1983 for details.
 */
public class _1983_IFrameLoadTest extends AbstractTestCase
{
    @After
    public void resetXltEngine()
    {
        XltEngine.reset(); // also initializes XLT properties again 
    }

    @Test
    public void test() throws Exception
    {
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.javaScriptEngineEnabled", "true");
        props.setProperty("com.xceptance.xlt.javaScriptEnabled", "true");
        
        final XltDriver driver = new XltDriver();
        ((XltWebClient) driver.getWebClient()).setLoadStaticContent(true);

        final MockWebConnection webConnection = new MockWebConnection();
        final URL u = new URL("http://example.org/");
        webConnection.setResponse(u, "<html><body>" + "<script type=\"text/javascript\">" + "var f = document.createElement('iframe');"
                                     + "f.setAttribute('id', 'container');" + "f.setAttribute('name', 'container');"
                                     + "document.body.appendChild(f);" + "var rootDoc = f.contentWindow.document;"
                                     + "var div = rootDoc.createElement('div');" + "div.setAttribute('id', 'randomID');"
                                     + "rootDoc.body.appendChild(div);" + "function track(){"
                                     + "var frame = rootDoc.createElement('iframe');" + "frame.setAttribute('id', 'frame');"
                                     + "frame.setAttribute('name', 'frame');" + "rootDoc.body.appendChild(frame);"
                                     + "var form = rootDoc.createElement('form');" + "form.setAttribute('target', 'frame');"
                                     + "form.setAttribute('method','post');" + "form.setAttribute('action','http://example.org/doAction');"
                                     + "rootDoc.body.appendChild(form);" + "var input = rootDoc.createElement('input');"
                                     + "input.setAttribute('type', 'hidden');" + "input.setAttribute('name','hiddenInput');"
                                     + "input.setAttribute('value','someValue');" + "form.appendChild(input);" + "form.submit();};"
                                     + "track();" + "</script>" + "</body></html>");
        webConnection.setResponse(new URL("http://example.org/doAction"),
                                  "<html><body>"
                                      + "<script type=\"text/javascript\">"
                                      + "setTimeout(function(){var f = document.createElement('iframe');"
                                      + "f.setAttribute('id', 'theFrame');"
                                      + "document.body.appendChild(f);"
                                      + "f.contentWindow['theFrame'] = '<html><body><h1>FOO</h1><img src=\"http://example.org/foo.gif\" /></body></html>';"
                                      + "f.src = 'javascript:window[\"theFrame\"];';}, 250);" + "</script>" + "</body></html>");
        webConnection.setDefaultResponse("EMPTY");

        driver.getWebClient().setWebConnection(webConnection);

        driver.get(u.toExternalForm());

        Thread.sleep(3000L);
        Assert.assertEquals(3, webConnection.getRequestCount());
        Assert.assertEquals("/foo.gif", webConnection.getLastWebRequest().getUrl().getFile());
    }
}
