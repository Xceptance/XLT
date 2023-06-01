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
 * The super class of all code module script files.
 */
public class CodeModule extends Script
{
    /**
     * The names of the parameters to pass to the module when the module is executed.
     */
    private final List<String> parameterNames;

    /**
     * Constructor.
     * 
     * @param scriptFile
     *            the file the script was read from
     * @param parameterNames
     *            the module parameter names
     */
    public CodeModule(final File scriptFile, final List<String> parameterNames)
    {
        super(scriptFile);
        this.parameterNames = parameterNames;
    }

    /**
     * Returns the names of the parameters to pass to the module when the module is executed.
     * 
     * @return the module parameters
     */
    public List<String> getParameterNames()
    {
        return parameterNames;
    }
}
