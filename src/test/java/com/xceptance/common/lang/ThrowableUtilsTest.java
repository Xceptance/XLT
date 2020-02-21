package com.xceptance.common.lang;

import org.junit.Assert;
import org.junit.Test;

public class ThrowableUtilsTest
{
    @Test
    public final void constructor()
    {
        ReflectionUtils.classHasOnlyPrivateConstructors(ThrowableUtils.class);
    }

    @Test
    public final void testGetMessage()
    {
        final Throwable t = new Throwable("Old");
        Assert.assertEquals("Old", t.getMessage());

        Assert.assertEquals(t.getMessage(), ThrowableUtils.getMessage(t));
    }

    @Test
    public final void testSetMessage()
    {
        final Throwable t = new Throwable("Old");
        Assert.assertEquals("Old", t.getMessage());

        ThrowableUtils.setMessage(t, "New");
        Assert.assertEquals("New", t.getMessage());
    }

    @Test
    public final void testGetStackTrace()
    {
        final Throwable t = new Throwable();
        StackTraceElement[] stackTrace = new StackTraceElement[]
            {
                new StackTraceElement(java.lang.Object.class.getName(), "foo1", "bar1", 1),
                new StackTraceElement(java.lang.Object.class.getName(), "foo2", "bar2", 2)
            };
        t.setStackTrace(stackTrace);

        final String lineSeparator = System.getProperty("line.separator", "\n");
        final String expected = "java.lang.Throwable" + lineSeparator + "\tat java.lang.Object.foo1(bar1:1)" + lineSeparator +
                                "\tat java.lang.Object.foo2(bar2:2)" + lineSeparator;
        final String current = ThrowableUtils.getStackTrace(t);

        Assert.assertEquals(expected, current);
    }

    @SuppressWarnings("restriction")
    @Test
    public final void testGetMinifiedStackTrace()
    {
        final Throwable t = new Throwable();
        StackTraceElement[] stackTrace = new StackTraceElement[]
            {
                new StackTraceElement(java.lang.Object.class.getName(), "foo1", "bar1", 1),
                new StackTraceElement(java.lang.Object.class.getName(), "foo2", "bar2", 2),
                new StackTraceElement(sun.reflect.Reflection.class.getName(), "foo3", "bar3", 3),
                new StackTraceElement(java.lang.reflect.Array.class.getName(), "foo4", "bar4", 4),
                new StackTraceElement("org.junit.runners.model.FrameworkMethod$1.runReflectiveCall", "foo5", "bar5", 5)
            };
        t.setStackTrace(stackTrace);

        final String lineSeparator = System.getProperty("line.separator", "\n");
        final String expected = "java.lang.Throwable" + lineSeparator + "\tat java.lang.Object.foo1(bar1:1)" + lineSeparator +
                                "\tat java.lang.Object.foo2(bar2:2)" + lineSeparator + "\t...";
        final String current = ThrowableUtils.getMinifiedStackTrace(t);

        Assert.assertEquals(expected, current);
    }

    @SuppressWarnings("restriction")
    @Test
    public final void testGetMinifiedStackTraceCausedBy()
    {
        final Throwable t = new Throwable();
        StackTraceElement[] stackTrace = new StackTraceElement[]
            {
                new StackTraceElement(java.lang.Object.class.getName(), "foo1", "bar1", 1),
                new StackTraceElement(java.lang.Object.class.getName(), "foo2", "bar2", 2),
                new StackTraceElement(sun.reflect.Reflection.class.getName(), "foo3", "bar3", 3),
                new StackTraceElement(java.lang.reflect.Array.class.getName(), "foo4", "bar4", 4),
                new StackTraceElement("org.junit.runners.model.FrameworkMethod$1.runReflectiveCall", "foo5", "bar5", 5)
            };
        t.setStackTrace(stackTrace);

        // add cause
        final Throwable cause = new Throwable();
        cause.setStackTrace(new StackTraceElement[]
            {
                new StackTraceElement(java.lang.Object.class.getName(), "foo1", "bar1", 1)
            });
        t.initCause(cause);

        final String lineSeparator = System.getProperty("line.separator", "\n");
        final String expected = "java.lang.Throwable" + lineSeparator + "\tat java.lang.Object.foo1(bar1:1)" + lineSeparator +
                                "\tat java.lang.Object.foo2(bar2:2)" + lineSeparator + "\t..." + lineSeparator +
                                "Caused by: java.lang.Throwable" + lineSeparator + "\tat java.lang.Object.foo1(bar1:1)" + lineSeparator;
        final String current = ThrowableUtils.getMinifiedStackTrace(t);

        Assert.assertEquals(expected, current);
    }
}
