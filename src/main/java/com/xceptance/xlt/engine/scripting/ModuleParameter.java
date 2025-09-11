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
package com.xceptance.xlt.engine.scripting;

/**
 * Represents a module parameter in a script file.
 */
public class ModuleParameter
{
    /**
     * The parameter's name.
     */
    private final String name;

    /**
     * The parameter's value.
     */
    private final String value;

    /**
     * Constructor.
     * 
     * @param name
     *            the parameter name
     * @param value
     *            the parameter value
     */
    public ModuleParameter(final String name, final String value)
    {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the value of the 'name' attribute.
     * 
     * @return the value of name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the value of the 'value' attribute.
     * 
     * @return the value of value
     */
    public String getValue()
    {
        return value;
    }
}
