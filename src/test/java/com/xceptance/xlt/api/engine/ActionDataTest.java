package com.xceptance.xlt.api.engine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the implementation of {@link ActionData}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class ActionDataTest extends TimerDataTest
{

    /**
     * ActionData test instance.
     */
    private ActionData instance = null;

    /**
     * Test initialization.
     */
    @Before
    public void setupActionDataInstance()
    {
        instance = new ActionData("Test");
    }

    /**
     * Tests the proper CSV encoding of the type code.
     */
    @Test
    public void testTypeCode()
    {
        Assert.assertTrue(instance.toCSV().startsWith(instance.getTypeCode()));
    }

    @Test
    public void testDefaultConstructor()
    {
        instance = new ActionData();
        Assert.assertEquals("Wrong name, ", null, instance.getName());
    }
}
