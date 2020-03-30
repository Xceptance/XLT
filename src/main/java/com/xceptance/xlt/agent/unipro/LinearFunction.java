/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
 * The linear function y = f(x) = m*x + n for a certain interval [x1,x2].
 */
public class LinearFunction extends AbstractFunction
{
    /**
     * The slope m.
     */
    private final double m;

    /**
     * The constant displacement n.
     */
    private final double n;

    /**
     * The x for which f(x) = 0.
     */
    private final double x0;

    /**
     * Constructor.
     * 
     * @param x1
     *            the first x value
     * @param y1
     *            the first y value
     * @param x2
     *            the last x value
     * @param y2
     *            the last y value
     */
    public LinearFunction(final double x1, final double y1, final double x2, final double y2)
    {
        super(x1, y1, x2, y2);

        // calculate the slope m
        m = (y2 - y1) / (x2 - x1);

        // calculate the constant displacement n
        n = y1 - m * x1;

        // the zero/root of the function
        x0 = -n / m;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculateY(final double x)
    {
        return m * x + n;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculateX2(final double x1, final double area)
    {
        // the coefficients for the quadratic formula
        final double a = m;
        final double b = 2 * n;
        final double c = -(m * x1 * x1 + 2 * n * x1 + 2 * area);

        // the quadratic formula (see http://en.wikipedia.org/wiki/Quadratic_equation#Quadratic_formula)
        final double bb_4ac = b * b - 4 * a * c;
        final double x;

        if (bb_4ac < 0)
        {
            // area is too large to fit below the function before the function crosses the x-axis, so limit x by x0
            x = x0;
        }
        else
        {
            x = (-b + Math.sqrt(bb_4ac)) / (2 * a);
        }

        // System.out.printf("%g %g %g -> %g\n", a, b, c, x);

        // limit x by x2
        return Math.min(x, x2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double integrate(final double x1, final double x2)
    {
        final double dx = x2 - x1;
        final double x = (x1 + x2) / 2;
        final double y = calculateY(x);

        return dx * y;
    }

    @Override
    public String toString()
    {
        return "LinearFunction: x1=" + x1 + " y1=" + y1 + " x2=" + x2 + " y2=" + y2;
    }
}
