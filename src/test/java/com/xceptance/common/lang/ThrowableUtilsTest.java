/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    @Test
    public final void testGetMinifiedStackTrace()
    {
        final Throwable t = new Throwable();
        StackTraceElement[] stackTrace = new StackTraceElement[]
            {
                new StackTraceElement(java.lang.Object.class.getName(), "foo1", "bar1", 1),
                new StackTraceElement(java.lang.Object.class.getName(), "foo2", "bar2", 2),
                new StackTraceElement("sun.reflect.Reflection", "foo3", "bar3", 3),
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

    @Test
    public final void testGetMinifiedStackTraceCausedBy()
    {
        final Throwable t = new Throwable();
        StackTraceElement[] stackTrace = new StackTraceElement[]
            {
                new StackTraceElement(java.lang.Object.class.getName(), "foo1", "bar1", 1),
                new StackTraceElement(java.lang.Object.class.getName(), "foo2", "bar2", 2),
                new StackTraceElement("sun.reflect.Reflection", "foo3", "bar3", 3),
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
