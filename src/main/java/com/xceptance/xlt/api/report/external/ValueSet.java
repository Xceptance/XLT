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
package com.xceptance.xlt.api.report.external;

import java.util.HashMap;
import java.util.Map;

import com.xceptance.common.util.ParameterCheckUtils;

/**
 * A {@link #ValueSet} describes a collection of data, accompanied by an optional timestamp.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class ValueSet
{
    /**
     * Named values of data set.
     */
    private final Map<String, Object> data = new HashMap<String, Object>();

    /**
     * Timestamp of data set.
     */
    private final long timestamp;

    /**
     * Creates a new {link #ValueSet} instance.
     *
     * @param timestamp
     *            the timestamp, or -1 if not applicable
     */
    public ValueSet(final long timestamp)
    {
        this.timestamp = timestamp;
    }

    /**
     * Adds a named value.
     *
     * @param name
     *            the name of the value, must not be <code>null</code>
     * @param value
     *            the value
     */
    public void addValue(final String name, final Object value)
    {
        ParameterCheckUtils.isNotNullOrEmpty(name, "name");
        data.put(name, value);
    }

    /**
     * Returns the timestamp of this value set.
     *
     * @return the timestamp of this value set
     */
    public long getTime()
    {
        return timestamp;
    }

    /**
     * Returns the key/value pairs.
     *
     * @return the key/value pairs
     */
    public Map<String, Object> getValues()
    {
        return data;
    }
}
