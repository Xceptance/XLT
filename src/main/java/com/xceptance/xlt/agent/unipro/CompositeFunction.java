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

import java.util.ArrayList;
import java.util.List;

/**
 * A implementation of {@link Function} that itself is built of multiple functions.
 */
public class CompositeFunction implements Function
{
    /**
     * The list of functions.
     */
    private final List<AbstractFunction> functions = new ArrayList<AbstractFunction>();

    /**
     * Constructor.
     * 
     * @param timeValuePairs
     *            the values that define the points
     */
    public CompositeFunction(final int[][] timeValuePairs)
    {
        if (timeValuePairs == null || timeValuePairs.length == 0)
        {
            throw new IllegalArgumentException("At least one time/value pair must be given.");
        }

        if (timeValuePairs[0][0] > 0)
        {
            throw new IllegalArgumentException("The first time/value pair must have a time of 0.");
        }

        for (int i = 0; i < timeValuePairs.length - 1; i++)
        {
            final double x1 = timeValuePairs[i][0];
            final double y1 = timeValuePairs[i][1];
            final double x2 = timeValuePairs[i + 1][0];
            final double y2 = timeValuePairs[i + 1][1];

            if (x1 > x2)
            {
                throw new IllegalArgumentException("The time/value pairs must be sorted in ascending order.");
            }

            AbstractFunction f = null;
            if (y1 == y2)
            {
                f = new ConstantFunction(x1, x2, y1);
            }
            else if (x1 != x2)
            {
                f = new LinearFunction(x1, y1, x2, y2);
            }

            if (f != null)
            {
                functions.add(f);
            }
        }

        // add an additional function to extend the time range of the composite function to "infinity"
        final double x_last = timeValuePairs[timeValuePairs.length - 1][0];
        final double y_last = timeValuePairs[timeValuePairs.length - 1][1];
        functions.add(new ConstantFunction(x_last, Double.MAX_VALUE, y_last));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculateY(final double x)
    {
        final AbstractFunction f = getResponsibleFunction(x);
        final double y = f.calculateY(x);

        return y;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculateX2(final double x, final double area)
    {
        double remainingArea = area;

        double x1 = x;
        double x2 = 0.0;

        // repeat until an acceptable precision is reached
        while (remainingArea >= 0.001)
        {
            final AbstractFunction f = getResponsibleFunction(x1);

            x2 = f.calculateX2(x1, remainingArea);
            remainingArea -= f.integrate(x1, x2);

            // System.out.printf("x1: %g   x2: %g   a: %g\n", x1, x2, remainingArea);

            x1 = x2;
        }

        return x2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double integrate(final double x1, final double x2)
    {
        double area = 0;
        double xi = x1;

        // repeat until an acceptable precision is reached
        while ((x2 - x1) >= 0.001)
        {
            final AbstractFunction f = getResponsibleFunction(xi);
            final double xMax = Math.min(x2, f.x2);

            area += f.integrate(xi, xMax);

            xi = xMax;
        }

        return area;
    }

    /**
     * Returns the function that is responsible for the given x value.
     * 
     * @param x
     *            the x value
     * @return the function
     */
    private AbstractFunction getResponsibleFunction(final double x)
    {
        for (final AbstractFunction f : functions)
        {
            if (f.isResponsibleFor(x))
            {
                return f;
            }
        }

        // impossible, but hey ...
        throw new IllegalArgumentException("No responsible function found for x value: " + x);
    }

    public boolean isSpecialPoint(final int x)
    {
        if (x > 0)
        {
            final int lastIdx = functions.size() - 1;
            for (int i = 0; i < lastIdx; i++)
            {
                final AbstractFunction iFunc = functions.get(i);
                final AbstractFunction i1Func = functions.get(i + 1);

                if (iFunc.x2 == x && iFunc.y2 != i1Func.y1)
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String toString()
    {
        String erg = "";
        for (int i = 0; i < getSize(); i++)
        {
            erg += functions.get(i).toString() + "\n";
        }
        return erg;
    }

    public int getSize()
    {
        return functions.size();
    }
}
