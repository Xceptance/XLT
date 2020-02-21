package com.xceptance.xlt.agent.unipro;

/**
 * The abstract interface of a mathematical function.
 */
public interface Function
{
    /**
     * Calculates a value y for the given x.
     * 
     * @param x
     *            the x value
     * @return the corresponding y value
     */
    public abstract double calculateY(double x);

    /**
     * Calculates the value x2 from the given x1 and the given area between the function graph and the x-axis.
     * 
     * @param x1
     *            the x1 value
     * @param area
     *            the area
     * @return the corresponding x2 value
     */
    public abstract double calculateX2(double x1, double area);

    /**
     * Calculates the area below the function for the interval [x1,x2].
     * 
     * @param x1
     *            the x1 value
     * @param x2
     *            the x2 value
     * @return the resulting area
     */
    public abstract double integrate(double x1, double x2);
}
