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
package com.xceptance.xlt.api.engine;

import java.util.List;

import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * The {@link CustomValue} can store a single 'double' value.
 * 
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class CustomValue extends AbstractData
{
    /**
     * The type code ("V").
     */
    private static final char TYPE_CODE = 'V';

    /**
     * The value.
     */
    private double value;

    /**
     * Creates a new {@link CustomValue} object and gives it the specified name. Furthermore, the start time attribute
     * is set to the current time.
     * 
     * @param name
     *            the statistics name
     */
    public CustomValue(final String name)
    {
        super(name, TYPE_CODE);
    }

    /**
     * Creates a new {@link CustomValue} object.
     */
    public CustomValue()
    {
        super(TYPE_CODE);
    }


    /**
     * Sets the value.
     * 
     * @param value
     *            the value
     */
    public void setValue(final double value)
    {
        this.value = value;
    }

    /**
     * Returns the value.
     * 
     * @return the value
     */
    public double getValue()
    {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> addValues()
    {
        final List<String> values = super.addValues();
        values.add(Double.toString(value));
        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getMinNoCSVElements()
    {
        return 4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void parseValues(final List<XltCharBuffer> values)
    {
        value = Double.parseDouble(values.get(3).toString());
    }
}
