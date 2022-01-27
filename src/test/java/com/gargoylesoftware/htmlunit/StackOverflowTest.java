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
package com.gargoylesoftware.htmlunit;

import java.net.URL;

import org.junit.Test;

/**
 * See https://sourceforge.net/tracker/?func=detail&atid=448266&aid=2089341&group_id=47038 for more details.
 */

public class StackOverflowTest
{
    @Test
    public void test() throws Exception
    {
        // setup
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(true);

            final URL url = getClass().getResource(getClass().getSimpleName() + "_test.html");

            // test (throws StackOverflowError until fixed)
            webClient.getPage(url);
        }
    }
}
