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
package com.xceptance.xlt.api.report.external;

/**
 * A {@link NamedData} holds a value and its abstract name.
 *
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 * @deprecated As of XLT 4.6.0: This class is not used at all in the XLT API and will therefore be removed soon.
 */
@Deprecated
public class NamedData
{
    /**
     * The value.
     */
    private final double value;

    /**
     * Abstract name of the value.
     */
    private final String name;

    /**
     * Creates an instance of {@link NamedData}.
     *
     * @param name
     *            the name of the value
     * @param value
     *            the value
     */
    public NamedData(final String name, final double value)
    {
        this.name = name;
        this.value = value;
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the value.
     *
     * @return value
     */
    public double getValue()
    {
        return value;
    }
}
