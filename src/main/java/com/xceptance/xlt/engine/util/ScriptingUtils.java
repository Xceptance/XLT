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
package com.xceptance.xlt.engine.util;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.api.engine.scripting.ScriptName;

/**
 * Some helper methods used in the scripting framework.
 */
public final class ScriptingUtils
{
    /**
     * Name of the default script package.
     */
    public static final String DEFAULT_PACKAGE = "";

    /**
     * Constructor.
     */
    private ScriptingUtils()
    {
    }

    /**
     * Checks the given class for the {@link ScriptName} annotation and returns the value specified there. If the class
     * is not annotated this way, the method returns the simple class name.
     * 
     * @param testClass
     *            the class to check
     * @return the script name specified (or derived from the class name)
     */
    public static String getScriptName(final Class<?> testClass)
    {
        final ScriptName scriptNameAnnotation = testClass.getAnnotation(ScriptName.class);
        if (scriptNameAnnotation != null)
        {
            final String scriptName = scriptNameAnnotation.value();
            if (scriptName.length() > 0)
            {
                return scriptName;
            }
        }

        return testClass.getName();
    }

    /**
     * Returns the name of the given fully qualified script where the script package portion has been stripped off.
     * 
     * @param scriptName
     *            fully qualified script name
     * @return script name where script package portion has been stripped of
     */
    public static String getScriptBaseName(final String scriptName)
    {
        final int idx = scriptName.lastIndexOf(".");
        if (idx != -1)
        {
            return scriptName.substring(idx + 1);
        }
        return scriptName;
    }

    /**
     * Returns the script package portion of the given fully qualified script name.
     * 
     * @param scriptName
     *            fully qualified script name
     * @return script package
     */
    public static String getScriptPackage(final String scriptName)
    {
        final int idx = scriptName.lastIndexOf(".");
        if (idx != -1)
        {
            return scriptName.substring(0, idx);
        }
        return ScriptingUtils.DEFAULT_PACKAGE;
    }

    /**
     * Returns the name of the parent package.
     * 
     * @param packageName
     *            the name of the package
     * @return name of the parent package
     */
    public static String getParentPackageName(final String packageName)
    {
        return getScriptPackage(StringUtils.defaultString(packageName));
    }
}
