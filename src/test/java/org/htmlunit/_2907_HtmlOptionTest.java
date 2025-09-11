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
package org.htmlunit;

import java.util.List;

import org.htmlunit.html.HtmlOption;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSelect;
import org.junit.Assert;
import org.junit.Test;

public class _2907_HtmlOptionTest
{
    @Test
    public void selectMultipleOptionsInMultiSelect() throws Throwable
    {
        try (WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            // setup
            String page = "<select multiple><option value='a'>a</option><option value='b'>b</option></select>";

            MockWebConnection conn = new MockWebConnection();
            conn.setDefaultResponse(page);
            webClient.setWebConnection(conn);

            // test
            HtmlPage htmlPage = webClient.getPage("http://dummy.net");

            HtmlSelect select = (HtmlSelect) htmlPage.getElementsByTagName("select").get(0);
            Assert.assertTrue(select.isMultipleSelectEnabled());

            List<HtmlOption> options = select.getOptions();
            for (HtmlOption option : options)
            {
                option.setSelected(true);
            }

            for (HtmlOption option : options)
            {
                Assert.assertTrue("Option not selected: " + option, option.isSelected());
            }
        }
    }
}
