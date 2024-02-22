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
 * An abstract super class for functions.
 */
public abstract class AbstractFunction implements Function
{
    /**
     * The x value of the first (x,y) pair.
     */
    protected final double x1;

    /**
     * The y value of the first (x,y) pair.
     */
    protected final double y1;

    /**
     * The x value of the last (x,y) pair.
     */
    protected final double x2;

    /**
     * The y value of the last (x,y) pair.
     */
    protected final double y2;

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
    public AbstractFunction(final double x1, final double y1, final double x2, final double y2)
    {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * Checks whether this function is responsible for the given x. Basically it checks whether x is in the interval
     * [x1,x2).
     * 
     * @param x
     *            the x value
     * @return <code>true</code> if this functions handles x, <code>false</code> otherwise
     */
    public boolean isResponsibleFor(final double x)
    {
        return x1 <= x && x < x2;
    }
}
