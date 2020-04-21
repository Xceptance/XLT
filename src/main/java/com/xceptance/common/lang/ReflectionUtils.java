/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Utility class to access private/protected/package fields or methods.
 */
public final class ReflectionUtils
{
    /**
     * Private constructor to prevent instantiation.
     */
    private ReflectionUtils()
    {
    }

    /**
     * Returns whether the argument class has any only private constructors. This is a convenience method to check for
     * utility classes that should not be instantiable.
     * 
     * @param theClass
     *            which to check for having only private constructors
     * @return <code>true</code> if the argument class has only private constructors (there may be more than one),
     *         <code>false</code> otherwise
     */
    public static boolean classHasOnlyPrivateConstructors(final Class<?> theClass)
    {
        for (final Constructor<?> c : theClass.getDeclaredConstructors())
        {
            if (isPrivate(c.getModifiers()))
            {
                continue;
            }
            return false;
        }

        return true;
    }

    /**
     * Reads the value from an instance field.
     * 
     * @param object
     *            the target object (must be exactly of the type, not a sub type that defines the field)
     * @param fieldName
     *            the field's name
     * @return the field's value
     */
    @SuppressWarnings("unchecked")
    public static <T> T readInstanceField(final Object object, final String fieldName)
    {
        return (T) readField(object.getClass(), object, fieldName);
    }

    /**
     * Reads the value from a static field.
     * 
     * @param clazz
     *            the target class
     * @param fieldName
     *            the field's name
     * @return the field's value
     */
    @SuppressWarnings("unchecked")
    public static <T> T readStaticField(final Class<?> clazz, final String fieldName)
    {
        return (T) readField(clazz, null, fieldName);
    }

    /**
     * Writes a value to an instance field.
     * 
     * @param object
     *            the target object
     * @param fieldNameot
     *            the field's name
     * @param value
     *            the field's new value
     */
    public static void writeInstanceField(final Object object, final String fieldName, final Object value)
    {
        writeField(object.getClass(), object, fieldName, value);
    }

    /**
     * Writes a value to a static field.
     * 
     * @param clazz
     *            the target class
     * @param fieldName
     *            the field's name
     * @param value
     *            the field's new value
     */
    public static void writeStaticField(final Class<?> clazz, final String fieldName, final Object value)
    {
        writeField(clazz, null, fieldName, value);
    }

    /**
     * Returns a new instance of the argument class. Designed for tests of singleton classes.
     * 
     * @param classForWhichToReturnAnInstance
     *            the class from which you want to have a freshly created instance
     * @param parameters
     *            the parameters to use for the constructor
     * @throws IllegalArgumentException
     *             if the constructor is called with wrong arguments (should never happen)
     * @throws IllegalStateException
     *             if something else goes wrong
     */
    public static <T> T getNewInstance(final Class<T> classForWhichToReturnAnInstance, final Object... parameters)
    {
        Class<?>[] parameterTypes = null;
        final boolean isParameterLess = parameters == null || parameters.length == 0;
        if (!isParameterLess)
        {
            parameterTypes = new Class[parameters.length];
            for (int i = 0; i < parameters.length; i++)
            {
                final Object o = parameters[i];
                parameterTypes[i] = o.getClass();
                if (o instanceof Parameter)
                {
                    final Parameter<?> p = (Parameter<?>) o;
                    parameterTypes[i] = p.getDeclaredParameterClass();
                    parameters[i] = p.getValue();
                }
            }
        }
        return getNewInstance(classForWhichToReturnAnInstance, parameterTypes, isParameterLess, parameters);
    }

    /**
     * @param classForWhichToReturnAnInstance
     * @param parameterTypes
     * @param isParameterLess
     * @param className
     * @param parameters
     * @return
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    private static <T> T getNewInstance(final Class<T> classForWhichToReturnAnInstance, final Class<?>[] parameterTypes,
                                        final boolean isParameterLess, final Object... parameters)
    {
        final String className = classForWhichToReturnAnInstance.getName();
        try
        {
            final Constructor<?> constructor = isParameterLess ? classForWhichToReturnAnInstance.getDeclaredConstructor()
                                                              : classForWhichToReturnAnInstance.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return parameterTypes != null ? (T) constructor.newInstance(parameters) : (T) constructor.newInstance();
        }
        catch (final SecurityException e)
        {
            throw new IllegalStateException("Constructor in \"" + className + "\"can not be accessed due to security restrictions!");
        }
        catch (final NoSuchMethodException e)
        {
            final String message = isParameterLess ? "Parameter less constructor from class \"" + className + "\" not in place!"
                                                  : "Constructor from class \"" + className + "\" for parameter types " +
                                                    Arrays.toString(parameterTypes) + " not in place!";
            e.printStackTrace();
            throw new IllegalStateException(message);
        }
        catch (final IllegalArgumentException e)
        {
            throw new IllegalArgumentException("Wrong type or wrong argument type for constructor of class \"" + className + "\"!");
        }
        catch (final IllegalAccessException e)
        {
            throw new IllegalStateException("Constructor of class \"" + className + "\" can not be accessed this should never happen!");
        }
        catch (final InstantiationException e)
        {
            throw new IllegalStateException("Error instantiating class \"" + className + "\"!");
        }
        catch (final InvocationTargetException e)
        {
            throw new IllegalStateException("Error instantiating class \"" + className + "\"!");
        }
    }

    /**
     * Designed to reset final static fields for testing purposes (for example instance fields). Resets the field with
     * the name of the argument field name to <code>null</code>. Works also on private, protected and package-protected
     * fields as long as access is not denied due to security restrictions. Does not work for fields of a primitive type
     * or non static fields.
     * 
     * @param classWithFieldToReset
     *            the class having the field to reset as member
     * @param fieldName
     *            the name of the field to reset to <code>null</code>
     * @throws IllegalStateException
     *             if the field can not be accessed due to a security manager or other reasons or is not found (cause it
     *             has been renamed / removed)
     * @throws IllegalArgumentException
     *             if the field can not be assigned, this should never happen
     */
    public static void resetFieldToNull(final Class<?> classWithFieldToReset, final String fieldName)
    {
        try
        {
            final Field instanceField = classWithFieldToReset.getDeclaredField(fieldName);
            instanceField.setAccessible(true);
            makeFieldNonFinal(instanceField);

            if (instanceField.getType().isPrimitive())
            {
                resetPrimitiveField(instanceField);
            }
            else
            {
                instanceField.set(null, null);
            }
        }
        catch (final SecurityException e)
        {
            throw new IllegalStateException("Field \"" + fieldName + "\"can not be accessed due to security restrictions!");
        }
        catch (final NoSuchFieldException e)
        {
            throw new IllegalStateException("Singleton instance field has been renamed! Expected it to have the name \"" + fieldName +
                                            "\"!");
        }
        catch (final IllegalArgumentException e)
        {
            throw new IllegalArgumentException("Wrong type or wrong argument type for field with name \"" + fieldName + "\"!");
        }
        catch (final IllegalAccessException e)
        {
            throw new IllegalStateException("Field \"" + fieldName + "\"can not be accessed this should never happen!");
        }
    }

    /**
     * Returns the inner class with the argument name.
     * 
     * @param parent
     *            the parent class
     * @param nestedClassName
     *            the relative (not the absolute) name of the inner class
     * @return the first class which is an inner class in parent and has its name ending with the argument name
     * @throws IllegalStateException
     *             if no such class is found
     */
    public static Class<?> getNestedClass(final Class<?> parent, final String nestedClassName)
    {
        final Class<?>[] classes = parent.getDeclaredClasses();
        for (final Class<?> c : classes)
        {
            if (c.getName().endsWith(nestedClassName))
            {
                return c;
            }
        }
        throw new IllegalStateException("Class \"" + nestedClassName + "\" has been renamed or removed. Aborting test with error!");
    }

    /**
     * The method does not accept any <code>null</code> arguments.
     * 
     * @param declaringClass
     *            the class declaring the method
     * @param methodName
     *            the name of the method to return
     * @param parameterTypes
     *            the formal parameters of the method to return
     * @return the method with the argument name declared in the argument class
     * @throws SecurityException
     *             if the method can not be accessed due to security restrictions
     * @throws IllegalStateException
     *             if there is no parameter less method with the argument name found in the argument class
     */
    public static Method getMethod(final Class<?> declaringClass, final String methodName, final Class<?>... parameterTypes)
    {
        final String className = declaringClass.getName();
        try
        {
            return getMethodWithFallback(declaringClass, methodName, parameterTypes);
        }
        catch (final SecurityException e)
        {
            throw new SecurityException("Method \"" + methodName + "\" in class \"" + className +
                                        "\" can't be accessed! Aborting test with error!", e);
        }
        catch (final NoSuchMethodException e)
        {
            throw new IllegalStateException("Method \"" + methodName + "\" not found in class \"" + className +
                                            "\"! Aborting test with error!", e);
        }
    }

    /**
     * Returns a method.
     * <p>
     * Tries at first to get the method by calling {@link Class#getDeclaredMethod(String, Class...)} and in case that
     * this fails, tries to get the method by calling {@link Class#getMethod(String, Class...)}.
     * </p>
     * 
     * @param clazz
     *            the target class
     * @param methodName
     *            the method's name
     * @param parameterTypes
     *            the types of the method's parameters
     * @return the {@link Method} object
     * @throws SecurityException
     *             if a method matching the arguments is found but may not be accessed due to security restrictions
     * @throws NoSuchMethodException
     *             if there is no method matching the arguments at all
     */
    private static Method getMethodWithFallback(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes)
        throws SecurityException, NoSuchMethodException
    {
        Method method;

        try
        {
            method = clazz.getDeclaredMethod(methodName, parameterTypes);
        }
        catch (final NoSuchMethodException e)
        {
            method = clazz.getMethod(methodName, parameterTypes);
        }

        method.setAccessible(true);

        return method;
    }

    /**
     * @param o
     *            the object on which to invoke the argument method
     * @param m
     *            the method which to invoke on the argument object
     * @param args
     *            the arguments for the method invocation
     * @return the result of the called method as a string
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(final Object o, final Method m, final Object... args)
    {
        final String className = o.getClass().getName();
        final String name = m.getName();
        try
        {
            return (T) m.invoke(o, args);
        }
        catch (final IllegalAccessException e)
        {
            throw new IllegalStateException("Method \"" + name + "\" of \"" + className +
                                            "\" was not accessible! Aborting test with error!", e);
        }
        catch (final InvocationTargetException e)
        {
            throw new IllegalStateException("Exception occurred when invoking \"" + name + "\" from \"" + className +
                                            "\"! Aborting test with error!", e);
        }
    }

    /**
     * Reads the value from a field.
     * 
     * @param clazz
     *            the target class
     * @param object
     *            the target object
     * @param fieldName
     *            the field's name
     * @return the field's value
     * @throws RuntimeException
     *             if no field with the argument name is found or the object is no instance of the argument class
     */
    @SuppressWarnings("unchecked")
    public static <T> T readField(final Class<?> clazz, final Object object, final String fieldName)
    {
        try
        {
            return (T) getFieldAndMakeAccessible(clazz, fieldName).get(object);
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Failed to lookup/read field: " + fieldName, e);
        }
    }

    /**
     * Resets the argument field to the default value for the primitive type (<code>false</code> for boolean, <code>0
     * </code> for each other type). Only work for primitive fields, not for wrapper class types!
     * 
     * @param fieldToSet
     *            the field to reset
     * @throws IllegalArgumentException
     *             if the field is not of a primitive field
     */
    public static void resetPrimitiveField(final Field fieldToSet) throws IllegalArgumentException, IllegalAccessException
    {
        final Class<?> type = fieldToSet.getType();
        if (type == boolean.class)
        {
            fieldToSet.set(null, Boolean.FALSE);
            return;
        }
        else if (type == byte.class)
        {
            fieldToSet.set(null, Byte.valueOf((byte) 0));
            return;
        }
        else if (type == char.class)
        {
            fieldToSet.set(null, Character.valueOf((char) 0));
            return;
        }
        else if (type == double.class)
        {
            fieldToSet.set(null, Double.valueOf(0));
            return;
        }
        else if (type == float.class)
        {
            fieldToSet.set(null, Float.valueOf(0));
            return;
        }
        else if (type == int.class)
        {
            fieldToSet.set(null, Integer.valueOf(0));
            return;
        }
        else if (type == long.class)
        {
            fieldToSet.set(null, Long.valueOf(0));
            return;
        }
        else if (type == short.class)
        {
            fieldToSet.set(null, Short.valueOf((short) 0));
            return;
        }
        // Is there to do anything for the void.class?
        throw new IllegalArgumentException(
                                           "Illegal attempt to assign a void field! This should never happen cause you can not define a member of type \"void\"!");
    }

    /**
     * Writes a value to a field. In difference to {@link #writeInstanceField(Object, String, Object)} this can be used
     * to write on inherited fields.
     * 
     * @param clazz
     *            the target class
     * @param object
     *            the target object
     * @param fieldName
     *            the field's name
     * @param value
     *            the field's new value
     */
    public static void writeField(final Class<?> clazz, final Object object, final String fieldName, final Object value)
    {
        try
        {
            final Field field = getFieldAndMakeAccessible(clazz, fieldName);
            makeFieldNonFinal(field);
            field.set(object, value);
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Failed to lookup/write field: " + fieldName, e);
        }
    }

    /**
     * Returns a field.
     * 
     * @param clazz
     *            the target class
     * @param fieldName
     *            the field's name
     * @return the {@link Field} object
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    private static Field getFieldAndMakeAccessible(Class<?> clazz, final String fieldName) throws SecurityException, NoSuchFieldException
    {
        Field field = null;

        while (clazz != null && clazz != Object.class)
        {
            try
            {
                field = clazz.getDeclaredField(fieldName);
                break;
            }
            catch (NoSuchFieldException e)
            {
                clazz = clazz.getSuperclass();
            }
        }

        if (field == null)
        {
            throw new NoSuchFieldException(fieldName);
        }
        
//        try
//        {
//             field = clazz.getDeclaredField(fieldName);
//        }
//        catch (final NoSuchFieldException e)
//        {
//            field = clazz.getField(fieldName);
//        }

        field.setAccessible(true);
        return field;
    }

    /**
     * Returns a field.
     * 
     * @param clazz
     *            the target class
     * @param fieldName
     *            the field's name
     * @return the {@link Field} object
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    public static Field getField(final Class<?> clazz, final String fieldName) throws SecurityException, NoSuchFieldException
    {
        Field field;

        try
        {
            field = clazz.getDeclaredField(fieldName);
        }
        catch (final NoSuchFieldException e)
        {
            field = clazz.getField(fieldName);
        }

        return field;
    }

    /**
     * Attempts to remove the final modifier from the argument field to make it writable. May fail silently when
     * attempting to make the returned writable. That is still to be able to read the field even if it can not made
     * writable.
     * 
     * @param field
     *            the field to make non final
     * @throws SecurityException
     *             if the field may not be accessed due to a security manager
     * @throws IllegalStateException
     *             if the field can not be made non final
     */
    public static void makeFieldNonFinal(final Field field)
    {
        try
        {
            if (Modifier.isFinal(field.getModifiers()))
            {
                final Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            }
        }
        catch (final IllegalAccessException e)
        {
            throw new IllegalStateException("Failed to make field \"" + field.getName() + "\" writable!");
        }
        catch (final NoSuchFieldException e)
        {
            // Can not happen until the field is not renamed in the JDK
        }
    }

    /**
     * Calls a static method.
     * 
     * @param clazz
     *            the target class
     * @param methodName
     *            the method's name
     * @param args
     *            the method's arguments
     * @return the result
     */
    @SuppressWarnings("unchecked")
    public static <T> T callStaticMethod(final Class<?> clazz, final String methodName, final Object... args)
    {
        return (T) callMethod(clazz, null, methodName, args);
    }

    /**
     * Calls a method.
     * 
     * @param clazz
     *            the target class
     * @param object
     *            the target object
     * @param methodName
     *            the method's name
     * @param args
     *            the method's arguments
     * @return the result
     */
    public static Object callMethod(final Class<?> clazz, final Object object, final String methodName, final Object... args)
    {
        final Class<?>[] parameterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++)
        {
            parameterTypes[i] = args[i].getClass();
        }

        try
        {
            return getMethodWithFallback(clazz, methodName, parameterTypes).invoke(object, args);
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Failed to lookup/call method: " + methodName, e);
        }
    }

    /**
     * Calls a method.
     * 
     * @param object
     *            the target object
     * @param methodName
     *            the method's name
     * @param args
     *            the method's arguments
     * @return the result
     */
    public static Object callMethod(final Object object, final String methodName, final Object... args)
    {
        return callMethod(object.getClass(), object, methodName, args);
    }

    /**
     * Checks whether the argument modifier is the <code>private</code> modifier.
     */
    private static boolean isPrivate(final int modifiers)
    {
        return Modifier.isPrivate(modifiers);
    }
}
