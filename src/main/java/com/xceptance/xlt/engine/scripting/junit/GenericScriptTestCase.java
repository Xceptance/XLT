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
package com.xceptance.xlt.engine.scripting.junit;

import com.xceptance.xlt.api.engine.scripting.AbstractScriptTestCase;

/**
 * A common test case class, which can be parameterized to run any test script.
 */
public class GenericScriptTestCase extends AbstractScriptTestCase
{
    /**
     * Returns the name of the configured script as the test name. Otherwise all script test cases would be named
     * "GenericScriptTestCase" in the results.
     */
    @Override
    protected String getTestName()
    {
        return getScriptName();
    }
}
