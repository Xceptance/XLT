/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
 * Represents a Java module defined in a script file.
 */
public class JavaModule extends CodeModule
{
    /**
     * The name of the Java class to be run when this module is executed.
     */
    private final String className;

    /**
     * Constructor.
     * 
     * @param scriptFile
     *            the file the script was read from
     * @param parameterNames
     *            the module parameter names
     * @param className
     *            the class name
     */
    public JavaModule(final File scriptFile, final List<String> parameterNames, final String className)
    {
        super(scriptFile, parameterNames);
        this.className = className;
    }

    /**
     * Returns the name of the Java class to be run when this module is executed.
     * 
     * @return the class name
     */
    public String getClassName()
    {
        return className;
    }
}
