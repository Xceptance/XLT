package com.xceptance.common.util;

/**
 * Getter for some kind of data.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public interface Getter<T>
{
    /**
     * Returns the data.
     * 
     * @return data
     */
    public T get();
}
