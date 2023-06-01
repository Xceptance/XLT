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

import java.io.IOException;

import org.htmlunit.html.HtmlPage;
import org.junit.Assert;
import org.junit.Test;

public class _2916_BaseUrlTest
{
    private static final String pageUrl = "http://host/path/to/page.html";

    private static final String pageTemplate = "<html><head><base href='%s'></head><body></body></html>";

    @Test
    public void fullUrl() throws FailingHttpStatusCodeException, IOException
    {
        test("http://otherhost/img/", "http://otherhost/img/");
    }

    @Test
    public void absolutePath() throws FailingHttpStatusCodeException, IOException
    {
        test("/img/", "http://host:80/img/");
    }

    @Test
    public void relativePath_1() throws FailingHttpStatusCodeException, IOException
    {
        test("img/", "http://host/path/to/img/");
    }

    @Test
    public void relativePath_2() throws FailingHttpStatusCodeException, IOException
    {
        test("img", "http://host/path/to/img");
    }

    @Test
    public void relativePath_3() throws FailingHttpStatusCodeException, IOException
    {
        test("../../../../img/", "http://host/img/");
    }

    private void test(String baseElementHref, String expectedBaseUrl) throws FailingHttpStatusCodeException, IOException
    {
        String pageContent = String.format(pageTemplate, baseElementHref);

        MockWebConnection conn = new MockWebConnection();
        conn.setDefaultResponse(pageContent);

        try (WebClient wc = new WebClient())
        {
            wc.setWebConnection(conn);

            HtmlPage page = wc.getPage(pageUrl);

            String actualBaseUrl = page.getBaseURL().toString();
            System.out.println(actualBaseUrl);

            Assert.assertEquals(expectedBaseUrl, actualBaseUrl);
        }
    }
}
