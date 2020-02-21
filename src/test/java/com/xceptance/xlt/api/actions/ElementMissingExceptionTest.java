package com.xceptance.xlt.api.actions;

import org.junit.Assert;
import org.junit.Test;

public class ElementMissingExceptionTest
{

    @Test
    public final void testElementMissingException()
    {
        try
        {
            throw new ElementMissingException();
        }
        catch (final ElementMissingException e)
        {
            Assert.assertNull(e.getMessage());
        }
    }

    @Test
    public final void testElementMissingExceptionString()
    {
        try
        {
            throw new ElementMissingException("This is a Test");
        }
        catch (final ElementMissingException e)
        {
            Assert.assertTrue(e.getMessage().equals("This is a Test"));
        }
    }

    @Test
    public final void testElementMissingExceptionThrowable()
    {
        try
        {
            throw new ElementMissingException(new Throwable("Throwable Test"));
        }
        catch (final ElementMissingException e)
        {
            Assert.assertTrue(e.getCause().getMessage().equals("Throwable Test"));
        }
    }

    @Test
    public final void testElementMissingExceptionStringThrowable()
    {
        try
        {
            throw new ElementMissingException("Message", new Throwable("Throwable Test"));
        }
        catch (final ElementMissingException e)
        {
            Assert.assertTrue(e.getMessage().equals("Message"));
            Assert.assertTrue(e.getCause().getMessage().equals("Throwable Test"));
        }
    }

}
