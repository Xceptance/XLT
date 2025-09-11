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
package com.xceptance.xlt.report.mergerules;

import com.xceptance.xlt.api.engine.RequestData;

/**
 * Base class for all request filters.
 */
public abstract class AbstractRequestFilter
{
    /**
     * The type code of this request filter.
     */
    private final String typeCode;

    /**
     * The hash of the string for faster comparison of the type code
     */
    private final int typeCodeHashCode;

    /**
     * The last state of the evaluation, so we don't have look anything up. All filters are already
     * stateful, so we can do that. 
     */
    protected Object lastFilterState;
    
    /**
     * Constructor.
     *
     * @param typeCode
     *            the type code of this request filter
     */
    public AbstractRequestFilter(final String typeCode)
    {
        this.typeCode = typeCode;
        this.typeCodeHashCode = typeCode.hashCode();
    }

    /**
     * Returns the replacement text derived from the passed request data object.
     *
     * @param requestData
     *            the request data object
     * @param capturingGroupIndex
     *            the capturing group index specified in the placeholder
     * @param filterState
     *            the filter state object returned by {@link #appliesTo(RequestData)}
     * @return the replacement text
     */
    public abstract CharSequence getReplacementText(RequestData requestData, int capturingGroupIndex);

    /**
     * Whether or not the passed request data object is accepted by this request filter.
     *
     * @param requestData
     *            the request data object
     * @return in case the filter accepted the passed request data: a state object representing the filter state (can be
     *         a dummy object), otherwise: <code>null</code>
     */
    public abstract Object appliesTo(RequestData requestData);

    /**
     * Returns the type code of this request filter.
     *
     * @return the type code
     */
    public String getTypeCode()
    {
        return typeCode;
    }

    /**
     * Compares two types codes efficiently
     *
     * @param typeCode the type code to compare to
     * @param typeCodeHashCode the hash of the type code for performance reason
     * @return true, if the type codes match, false otherwise
     */
    public boolean isSameTypeCode(final String typeCode, final int typeCodeHashCode)
    {
        if (this.typeCodeHashCode == typeCodeHashCode)
        {
            return this.typeCode.equals(typeCode);
        }
        else
        {
            return false;
        }
    }
}
