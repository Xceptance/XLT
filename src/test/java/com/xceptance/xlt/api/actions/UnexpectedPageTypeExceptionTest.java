package com.xceptance.xlt.api.actions;

import org.junit.Assert;
import org.junit.Test;

/**
 * Trivial tests for {@link UnexpectedPageTypeException}.
 * 
 * @author Sebastian Oerding
 */
public class UnexpectedPageTypeExceptionTest
{
    @Test
    public void testStringThrowableConstructor()
    {
        final String message = "UnknownType";
        final String causeMessage = "D'oh";
        final UnexpectedPageTypeException upte = new UnexpectedPageTypeException(message, new IllegalStateException(causeMessage));

        Assert.assertEquals("Message changed by UnexpectedPageTypeException", message, upte.getMessage());
        Assert.assertEquals("Message for cause has been modified by UnexpectedPageTypeException", causeMessage, upte.getCause()
                                                                                                                    .getMessage());
        Assert.assertTrue("Cause has been modified by UnexpectedPageTypeException",
                          upte.getCause().getClass() == IllegalStateException.class);
    }

    @Test
    public void testThrowableConstructor()
    {
        final String causeMessage = "D'oh";
        final UnexpectedPageTypeException upte = new UnexpectedPageTypeException(new IllegalStateException(causeMessage));

        Assert.assertEquals("Message changed by UnexpectedPageTypeException", upte.getCause().toString(), upte.getMessage());
        Assert.assertEquals("Message for cause has been modified by UnexpectedPageTypeException", causeMessage, upte.getCause()
                                                                                                                    .getMessage());
        Assert.assertTrue("Cause has been modified by UnexpectedPageTypeException",
                          upte.getCause().getClass() == IllegalStateException.class);
    }
}
