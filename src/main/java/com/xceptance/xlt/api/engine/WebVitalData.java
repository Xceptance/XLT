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
package com.xceptance.xlt.api.engine;

import java.util.List;

import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * The {@link WebVitalData} stores a single observation for a certain Web Vital in a certain action as a 'double' value.
 * Which action and Web Vital this data object was reported for is encoded in the name field. For example, "Foo
 * Action [CLS]" and "Bar Action [LCP]" indicate measurements of CLS for the "Foo Action" and LCP for the "Bar Action".
 * <p>
 * Up to now, the following Web Vitals are supported: CLS, FCP, FID, INP, LCP, and TTFB. See the links below for more
 * information on Web Vitals.
 * 
 * @see https://web.dev/articles/vitals
 * @see https://github.com/GoogleChrome/web-vitals
 */
public class WebVitalData extends AbstractData
{
    /**
     * The type code ("W").
     */
    private static final char TYPE_CODE = 'W';

    /**
     * The value.
     */
    private double value;

    /**
     * Creates a new {@link WebVitalData} object and gives it the specified name.
     * 
     * @param name
     *            the statistics name
     */
    public WebVitalData(final String name)
    {
        super(name, TYPE_CODE);
    }

    /**
     * Creates a new {@link WebVitalData} object.
     */
    public WebVitalData()
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
    public List<String> toList()
    {
        final List<String> values = super.toList();
        values.add(Double.toString(value));
        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRemainingValues(final List<XltCharBuffer> values)
    {
        value = Double.parseDouble(values.get(3).toString());
    }
}
