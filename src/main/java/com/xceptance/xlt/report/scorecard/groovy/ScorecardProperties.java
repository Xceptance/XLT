/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.scorecard.groovy;

import com.xceptance.xlt.api.util.XltProperties;

/**
 * Access to XLT properties from Groovy scorecard configurations.
 */
public class ScorecardProperties
{
    private final XltProperties properties;

    public ScorecardProperties()
    {
        this.properties = XltProperties.getInstance();
    }

    /**
     * Returns the property value for the given key.
     *
     * @param key
     *                the property key
     * @return the property value or null if not found
     */
    public String get(final String key)
    {
        return properties.getProperty(key);
    }

    /**
     * Returns the property value for the given key, or the default value if not found.
     *
     * @param key
     *                         the property key
     * @param defaultValue
     *                         the default value
     * @return the property value or default value
     */
    public String get(final String key, final String defaultValue)
    {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Returns boolean value of a property.
     */
    public boolean getBoolean(final String key, final boolean defaultValue)
    {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Returns int value of a property.
     */
    public int getInt(final String key, final int defaultValue)
    {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Returns long value of a property.
     */
    public long getLong(final String key, final long defaultValue)
    {
        return properties.getProperty(key, defaultValue);
    }
}
