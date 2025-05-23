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
package com.xceptance.xlt.engine;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.htmlunit.StringWebResponse;
import org.htmlunit.WebResponse;
import org.junit.Test;

/**
 * Tests the implementation of {@link LightWeightPageImpl}.
 */
public class LightWeightPageImplTest
{
    @Test
    public void testIssue2240() throws Throwable
    {
        // base URL is malformed,but commented out
        final WebResponse webResponse = new StringWebResponse("<!-- <base href=\"/en/\"> -->", StandardCharsets.UTF_8,
                                                              new URL("http://localhost/"));

        // must not throw MalformedURLException
        new LightWeightPageImpl(webResponse, "OpenStartPage", null);
    }
}
