/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.mockito.Mockito;

import com.tngtech.archunit.thirdparty.com.google.common.io.Files;
import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.api.util.XltProperties;

/**
 * Base class for all test cases that rely on a certain setup of the XLT engine.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractXLTTestCase
{
    /**
     * Name of the property file to use in test context.
     */
    public static final String TEST_PROPERTY_FILE = "src/test/resources/test.properties";

    /**
     * Location of the system-dependent temporary files directory.
     */
    protected static final String TEMP_DIR_NAME = System.getProperty("java.io.tmpdir");

    /**
     * File handle for the system-dependent temporary files directory.
     */
    private static final File TEMP_DIR = new File(TEMP_DIR_NAME);

    /**
     * Performs setup steps when class is loaded.
     */
    @BeforeClass
    public static void classSetUp()
    {
        try
        {
            var p = new Properties();
            p.load(Files.newReader(new File(TEST_PROPERTY_FILE), StandardCharsets.UTF_8));

            XltProperties.getInstance().setProperties(p);
        }
        catch (final IOException ioe)
        {
            Assert.fail(ioe.getMessage());
        }

        ParameterCheckUtils.isWritableDirectory(TEMP_DIR, AbstractXLTTestCase.class.getName() + ".TEMP_DIR");
    }

    /**
     * Creates a new mock object for the given class.
     *
     * @param <T>
     *            Class type of new mock object.
     * @param clazzToMock
     *            Class object of new mock object.
     * @return New mock object as instance of given class.
     */
    protected static <T> T mock(final Class<T> clazzToMock)
    {
        final T mock = Mockito.mock(clazzToMock);
        Assert.assertNotNull("Failed to create mock for class: " + clazzToMock.getName(), mock);
        return mock;
    }

    /**
     * Returns a string representation of the stack trace hold by the given throwable object.
     *
     * @param throwable
     *            Throwable object whose stack trace should be used for string generation.
     * @return String representation of the given throwable object.
     */
    protected static String getStackTrace(final Throwable throwable)
    {
        ParameterCheckUtils.isNotNull(throwable, "throwable");
        final StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));

        return sw.toString();
    }

    /**
     * Fails for an unexpected exception/error.
     *
     * @param t
     *            The unexpected exception/error to fail on.
     */
    protected static void failOnUnexpected(final Throwable t)
    {
        Assert.fail("Unexpected exception/error occurred. Cause: " + t.getMessage() + getStackTrace(t));
    }

    /**
     * Returns the system-dependent directory for temporary files.
     *
     * @return directory for temporary files
     */
    protected static File getTempDir()
    {

        return TEMP_DIR;
    }

    /**
     * Returns the value of the instance field for the given instance and field name.
     * <p>
     * If the value of the field is <code>null</code> or no such field exists or if access to the field has failed,
     * <code>null</code> will be returned.
     * </p>
     *
     * @param fieldName
     *            name of field
     * @param instance
     *            the instance
     * @return value of field
     */
    protected static Object getField(final String fieldName, final Object instance)
    {
        ParameterCheckUtils.isNotNullOrEmpty(fieldName, "fieldName");
        ParameterCheckUtils.isNotNull(instance, "instance");

        try
        {
            final Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);

        }
        catch (final Exception e)
        {
            failOnUnexpected(e);
        }

        return null;
    }

    /**
     * Calls the instance method identified by the given method name using the given parameters.
     * <p>
     * If the return value of the method call is <code>null</code> or if no such method exists or if the access to the
     * method has failed, <code>null</code> will be returned.
     * </p>
     * <p style="color:magenta">
     * Please notice, that the runtime classes of the passed parameters will be used for the method lookup.
     * </p>
     *
     * @param methodName
     *            name of method
     * @param instance
     *            the instance
     * @param parameters
     *            actual parameters
     * @return return value of method call
     */
    protected static Object callMethod(final String methodName, final Object instance, final Object... parameters)
    {
        ParameterCheckUtils.isNotNullOrEmpty(methodName, "methodName");
        ParameterCheckUtils.isNotNull(instance, "instance");

        final Class<?>[] classes = getClasses(parameters);
        try
        {
            final Method method = instance.getClass().getDeclaredMethod(methodName, classes);
            method.setAccessible(true);

            return method.invoke(instance, parameters);
        }
        catch (final Exception e)
        {
            failOnUnexpected(e);
        }

        return null;
    }

    /**
     * Helper method which returns an array containing the runtime classes of the given objects.
     *
     * @param objects
     *            objects for which the runtime classes should be determined
     * @return array of runtime classes of the given objects
     */
    private static Class<?>[] getClasses(final Object... objects)
    {
        if (objects != null)
        {
            final Class<?>[] classes = new Class<?>[objects.length];
            for (int i = 0; i < objects.length; i++)
            {
                classes[i] = objects[i].getClass();
            }

            return classes;
        }

        return null;
    }

    /**
     * Method to set a final static field to a value for a test Important: You have to reset it within a final block
     * later on again.
     *
     * @param clazz
     *            the class to modify
     * @param fieldName
     *            the name of the field to set
     * @param newValue
     *            the new value to set
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static void setFinalStatic(final Class<?> clazz, final String fieldName, final Object newValue)
        throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        final Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        // remove final modifier from field
        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}
