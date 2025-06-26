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
package com.xceptance.common.util;

/**
 * Represents a numeric value with an additional flag indicating if the value is meant to be interpreted as an absolute
 * or relative value.
 *
 * @param <T>
 *            a subtype of {@link Number}
 */
public class AbsoluteOrRelativeNumber<T extends Number>
{
    final private boolean isRelativeNumber;

    final private T value;

    public AbsoluteOrRelativeNumber(final boolean isRelative, final T value)
    {
        this.isRelativeNumber = isRelative;
        this.value = value;
    }

    /**
     * Indicates if the number should be interpreted as a relative value.
     *
     * @return "true" if the number should be interpreted as a relative value, "false" otherwise
     */
    public boolean isRelativeNumber()
    {
        return isRelativeNumber;
    }

    /**
     * Get the value of the number.
     *
     * @return the value of the number
     */
    public T getValue()
    {
        return value;
    }
}
