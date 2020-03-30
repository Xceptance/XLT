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
package com.gargoylesoftware.htmlunit;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * @see https://lab.xceptance.de/issues/2129
 * @see http://sourceforge.net/p/htmlunit/bugs/1604/
 */
public class _2129_MemoryLeakWhenAddingFrameByJsTest
{
    @Test
    @Ignore("To be run manually only")
    public void test() throws IOException, InterruptedException
    {
        for (int i = 0; i < 10000; i++)
        {
            System.err.printf("### %d\n", i);

            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getPage(getClass().getResource(getClass().getSimpleName() + ".html"));
            webClient.close();
        }
    }
}
