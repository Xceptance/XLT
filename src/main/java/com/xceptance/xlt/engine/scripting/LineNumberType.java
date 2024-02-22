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

import org.apache.commons.lang3.StringUtils;

/**
 * Type of line number.
 * 
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public enum LineNumberType
{
    scriptdeveloper, file;

    /**
     * Get the type for given name.
     * 
     * @param name
     *            name of type
     * @return type with given name
     * @throws RuntimeException
     *             if there is no type mapped to the given name
     */
    public static LineNumberType get(final String name)
    {
        if (StringUtils.isNotBlank(name))
        {
            for (final LineNumberType type : values())
            {
                if (type.toString().equalsIgnoreCase(name))
                {
                    return type;
                }
            }
        }

        throw new RuntimeException("Unsupported line number type: " + name);
    }
}
