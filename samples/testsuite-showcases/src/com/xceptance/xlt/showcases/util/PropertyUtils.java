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
package com.xceptance.xlt.showcases.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility method used for handling with properties.
 */
public class PropertyUtils
{
    /**
     * helper method to get a property as list
     * 
     * @param propertyValue
     *            the name of the property
     * @return if property or property value isn't set an empty list otherwise a list with the values
     */
    public static List<String> propertyToList(final String propertyValue)
    {
        return propertyToList(propertyValue, "\\|");
    }

    public static List<String> propertyToList(final String propertyValue, final String deliminator)
    {
        if (propertyValue.equals(""))
        {
            return new LinkedList<String>();
        }
        else
        {
            return Arrays.asList(propertyValue.split(deliminator));
        }
    }
}
