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
package com.xceptance.xlt.agent.unipro;

/**
 * The constant constant function y = f(x) = c for a certain interval [x1,x2].
 */
public class ConstantFunction extends AbstractFunction
{
    /**
     * Constructor.
     * 
     * @param x1
     *            the first x value
     * @param x2
     *            the last x value
     * @param y
     *            the constant y value
     */
    public ConstantFunction(final double x1, final double x2, final double y)
    {
        super(x1, y, x2, y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculateY(final double x)
    {
        return y1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculateX2(final double x1, final double area)
    {
        final double x;
        if (y1 == 0)
        {
            x = Double.POSITIVE_INFINITY;
        }
        else
        {
            x = area / y1 + x1;
        }

        // limit x by x2
        return Math.min(x, x2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double integrate(final double x1, final double x2)
    {
        return (x2 - x1) * y1;
    }

    @Override
    public String toString()
    {
        return "ConstantFunction: x1=" + x1 + " y1=" + y1 + " x2=" + x2 + " y2=" + y2;
    }
}
