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
 * Simple Holder class that holds a value. Use this class to imitate a mutable reference.
 */
public class Holder<T>
{
    /**
     * The value.
     */
    private T value;

    /**
     * Creates a new Holder instance and initializes it with a <code>null</code> value.
     */
    public Holder()
    {
        this(null);
    }

    /**
     * Creates a new Holder instance and initializes it with the given value.
     * 
     * @param value
     *            the initial value of the holder
     */
    public Holder(final T value)
    {
        this.value = value;
    }

    public T get()
    {
        return value;
    }

    public void set(T value)
    {
        this.value = value;
    }

    public T remove()
    {
        final T oldValue = value;
        value = null;

        return oldValue;
    }
}
