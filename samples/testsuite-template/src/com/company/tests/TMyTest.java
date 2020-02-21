package com.company.tests;

import org.junit.Test;

import com.xceptance.xlt.api.tests.AbstractTestCase;

/**
 * A sample test case.
 */
public class TMyTest extends AbstractTestCase
{
    @Test
    public void test()
    {
        String userName = getProperty("userName");
        String password = getProperty("password");

        // add some test code
    }
}
