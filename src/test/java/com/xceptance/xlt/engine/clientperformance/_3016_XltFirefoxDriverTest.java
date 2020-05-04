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
package com.xceptance.xlt.engine.clientperformance;

import org.openqa.selenium.WebDriver;

import com.xceptance.xlt.api.webdriver.XltFirefoxDriver;

public class _3016_XltFirefoxDriverTest extends _3016_XltChromeDriverTest
{
    @Override
    public String getWebdriverName()
    {
        return "firefox_clientperformance";
    }

    @Override
    public Class<? extends WebDriver> getWebdriverClass()
    {
        return XltFirefoxDriver.class;
    }

}