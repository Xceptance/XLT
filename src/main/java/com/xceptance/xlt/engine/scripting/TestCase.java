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
package com.xceptance.xlt.engine.scripting;

import java.io.File;
import java.util.List;

/**
 * Represents a test case script read from a script file.
 */
public class TestCase extends CommandScript
{
    /**
     * The base URL for this test case.
     */
    private final String baseUrl;

    private final boolean disabled;

    private final List<ScriptElement> postSteps;

    /**
     * Constructor.
     * 
     * @param scriptFile
     *            the file the script was read from
     * @param scriptElements
     *            the script elements
     * @param baseUrl
     *            the base URL
     */
    public TestCase(final File scriptFile, final List<ScriptElement> scriptElements, final List<ScriptElement> postSteps,
                    final String baseUrl, final String disabledFlag)
    {
        super(scriptFile, scriptElements, null);
        this.postSteps = postSteps;
        this.baseUrl = baseUrl;
        disabled = Boolean.parseBoolean(disabledFlag);
    }

    /**
     * Returns the base URL for this test case.
     * 
     * @return the base URL
     */
    public String getBaseUrl()
    {
        return baseUrl;
    }

    /**
     * Returns whether or not this test case is disabled.
     * 
     * @return <code>true</code> if this test case is disabled, <code>false</code> otherwise
     */
    public boolean isDisabled()
    {
        return disabled;
    }

    /**
     * @return the afterSteps
     */
    public List<ScriptElement> getPostSteps()
    {
        return postSteps;
    }
}
