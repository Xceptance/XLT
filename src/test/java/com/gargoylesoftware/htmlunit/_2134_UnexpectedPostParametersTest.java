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
package com.gargoylesoftware.htmlunit;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

/**
 * @see https://lab.xceptance.de/issues/2134
 * @see https://sourceforge.net/p/htmlunit/bugs/1619/
 */
public class _2134_UnexpectedPostParametersTest
{
    @Test
    public void test() throws Exception
    {
        try (WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            // load the test page
            HtmlPage htmlPage = webClient.getPage(getClass().getResource(getClass().getSimpleName() + ".html"));
            System.err.println(htmlPage.asXml());

            // check the web request that would have been sent when the form is submitted
            HtmlForm form = htmlPage.getFormByName("form1");
            HtmlInput submit = form.getInputByName("submit1");

            WebRequest webRequest = form.getWebRequest(submit);

            List<NameValuePair> postParams = webRequest.getRequestParameters();
            for (NameValuePair postParam : postParams)
            {
                System.err.println(postParam);
            }

            Assert.assertEquals("Unexpected number of POST parameters:", 2, postParams.size());
        }
    }
}
