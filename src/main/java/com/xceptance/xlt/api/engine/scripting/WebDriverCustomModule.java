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
package com.xceptance.xlt.api.engine.scripting;

import org.openqa.selenium.WebDriver;

/**
 * Defines the operations of custom modules, which can be used in script-based test cases.
 */
public interface WebDriverCustomModule
{
    /**
     * Executes the custom module using the given WebDriver instance.
     * 
     * @param webDriver
     *            the web driver
     * @param parameters
     *            the module parameters
     */
    public void execute(WebDriver webDriver, String... parameters);
}
