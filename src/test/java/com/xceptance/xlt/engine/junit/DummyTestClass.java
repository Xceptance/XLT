package com.xceptance.xlt.engine.junit;

import java.lang.reflect.Method;

/**
 * A simple class for tests. It contains also some convenience methods to keep the class with the tests smaller.
 * 
 * @author Sebastian Oerding
 */
public class DummyTestClass
{
    public DummyTestClass()
    {

    }

    /**
     * Just dummy method for test invocation. Used as argument via reflection.
     */
    public void dummyMethod()
    {
    }

    /**
     * @return the {@link #dummyMethod()} from this class, this should never throw an exception as long as the method
     *         remains unchanged
     */
    Method getDummyMethod()
    {
        try
        {
            return DummyTestClass.class.getMethod("dummyMethod");
        }
        catch (final SecurityException e)
        {
            throw new IllegalStateException("Method has been changed / removed. Revert the change!");
        }
        catch (final NoSuchMethodException e)
        {
            throw new IllegalStateException("Method has been changed / removed. Revert the change!");
        }
    }
}
