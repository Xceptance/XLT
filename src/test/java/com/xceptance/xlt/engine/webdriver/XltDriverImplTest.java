package com.xceptance.xlt.engine.webdriver;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.engine.XltWebClient;

/**
 * Tests the implementation of {@link XltDriver}.
 */
public class XltDriverImplTest
{
    /**
     * Tests the implementation of {@link XltDriver#getWebClient()}.
     */
    @Test
    public void testGetWebClient()
    {
        Assert.assertTrue("The return type of XltDriverImpl#getWebClient() is not XltWebClient",
                          new XltDriver().getWebClient() instanceof XltWebClient);
    }

    @Test
    public void ensureCurrentURLIsNullAfterInit()
    {
        Assert.assertNull("Current URL should be <null> right after construction of XltDriver", new XltDriver().getCurrentUrl());
    }
}
