package com.xceptance.xlt.api.data;

import org.junit.Test;

/**
 * @author Sebastian Oerding
 */
public class DataSetProviderExceptionTest
{
    @Test
    public void test()
    {
        new DataSetProviderException();
        new DataSetProviderException(new IllegalArgumentException("I won't be thrown!"));
    }
}
