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

import org.htmlunit.util.UrlUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * @see https://lab.xceptance.de/issues/3247
 */
@RunWith(Parameterized.class)
public class _3247_UrlResolvingSpecialCases
{
    @Parameters
    public static Object[][] data()
    {
        return new Object[][]
            {
                {
                    "/..there-used-to-be-a-time/index.html"
                },
                {
                    "/...there-used-to-be-a-time/index.html"
                }
            };
    }

    @Parameter
    public String relativeUrl;

    @Test
    public void test()
    {
        final String baseUrl = "http://www.example.com";

        final String url = UrlUtils.resolveUrl(baseUrl + "/some/path", relativeUrl);

        Assert.assertEquals(baseUrl + relativeUrl, url);
    }
}
