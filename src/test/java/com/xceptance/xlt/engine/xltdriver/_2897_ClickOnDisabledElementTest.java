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
/*
 * File: _2897_ClickOnDisabledElementTest.java
 * Created on: Apr 10, 2017
 */
package com.xceptance.xlt.engine.xltdriver;

import org.htmlunit.MockWebConnection;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidElementStateException;

import com.xceptance.xlt.api.webdriver.XltDriver;

/**
 * Test for issue 2897 (click on disabled element throws {@link InvalidElementStateException}).
 */
public class _2897_ClickOnDisabledElementTest
{
    @Test(expected = org.openqa.selenium.InvalidElementStateException.class)
    public void ensureInvalidElementStateException() throws Throwable
    {
        final String html = "<form action='/foo' method='POST'><input type=hidden name=bar value=bum />" +
                            "<button disabled name=submit value=submit id=btn>Click Me</button></form>";
        final XltDriver driver = new XltDriver(false);
        final MockWebConnection mockConn = new MockWebConnection();
        mockConn.setDefaultResponse(html);

        driver.getWebClient().setWebConnection(mockConn);

        driver.get("http://example.org");

        driver.findElement(By.id("btn")).click();
    }
}
