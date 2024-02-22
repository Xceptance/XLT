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
package com.xceptance.common.lang;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sebastian Oerding
 */
public class ReflectionUtilsTest
{
    /**
     * Cover private constructor
     */
    @Test
    public void testConstructor()
    {
        Assert.assertTrue(ReflectionUtils.classHasOnlyPrivateConstructors(ReflectionUtils.class));
    }

    /**
     * Check behavior for class with public and private constructor
     */
    @Test
    public void testHasOnlyPrivateConstructors_Private()
    {
        class Test
        {
            private Test()
            {
            };
        }
        Assert.assertTrue(ReflectionUtils.classHasOnlyPrivateConstructors(Test.class));
    }

    /**
     * Check behavior for class with public and private constructor
     */
    @Test
    public void testHasOnlyPrivateConstructors_PrivatePublic()
    {
        class Test
        {
            @SuppressWarnings("unused")
            private Test()
            {
            };

            @SuppressWarnings("unused")
            public Test(final int a)
            {
            };
        }
        Assert.assertFalse(ReflectionUtils.classHasOnlyPrivateConstructors(Test.class));
    }

    /**
     * readInstanceField
     */
    @Test
    public void testReadInstanceField()
    {
        class Test
        {
            @SuppressWarnings("unused")
            public String foo = "bar";

            @SuppressWarnings("unused")
            public final static String fooStatic = "bar";
        }
        Assert.assertEquals("bar", ReflectionUtils.readInstanceField(new Test(), "fooStatic"));
    }

    /**
     * readStaticField
     */
    @Test
    public void testReadStaticField()
    {
        class Test
        {
            @SuppressWarnings("unused")
            public final static String fooStatic = "bar";
        }
        Assert.assertEquals("bar", ReflectionUtils.readStaticField(Test.class, "fooStatic"));
    }

    /**
     * writeInstanceField(Object object, String fieldName, Object value)
     */
    @Test
    public void testWriteInstanceField()
    {
        class Test
        {
            public String foo = "bar";
        }

        final Test t = new Test();
        Assert.assertEquals("bar", t.foo);
        ReflectionUtils.writeInstanceField(t, "foo", "newbar");
        Assert.assertEquals("newbar", t.foo);
    }

    /**
     * writeStaticField(Class<?> clazz, String fieldName, Object value)
     */
    @Test
    public void testWriteStaticField()
    {
        Assert.assertEquals("foo", ReflectionUtilsTestClass.testWriteStaticField);
        ReflectionUtils.writeStaticField(ReflectionUtilsTestClass.class, "testWriteStaticField", "newbar");
        Assert.assertEquals("newbar", ReflectionUtilsTestClass.testWriteStaticField);
    }

    /**
     * getNewInstance(Class<T> classForWhichToReturnAnInstance, Object... parameters)
     */
    @Test
    public void getNewInstance()
    {
        final ReflectionUtilsTestClass t = ReflectionUtils.getNewInstance(ReflectionUtilsTestClass.class, "foobar");
        Assert.assertEquals("foobar", t.p1);
    }

    /**
     * getNewInstance(Class<T> classForWhichToReturnAnInstance, Object... parameters)
     */
    @Test
    public void getNewInstanceViaParameter()
    {
        final ReflectionUtilsTestClass t = ReflectionUtils.getNewInstance(ReflectionUtilsTestClass.class, Parameter.valueOf(42, int.class));
        Assert.assertTrue(42 == t.p2);
    }

    // getNewInstance(Class<T> classForWhichToReturnAnInstance, Class<?>[] parameterTypes,

    /**
     * resetFieldToNull(Class<?> classWithFieldToReset, String fieldName)
     */
    @Test
    public void resetFieldToNull()
    {
        Assert.assertEquals(ReflectionUtilsTestClass.fieldToReset, "resetme");
        ReflectionUtils.resetFieldToNull(ReflectionUtilsTestClass.class, "fieldToReset");
        Assert.assertNull(ReflectionUtilsTestClass.fieldToReset);
    }

    /**
     * resetFieldToNull(Class<?> classWithFieldToReset, String fieldName)
     */
    @Test
    public void resetFieldToNull_Primitive()
    {
        Assert.assertEquals(4200, ReflectionUtilsTestClass.fieldToResetPrimitive);
        ReflectionUtils.resetFieldToNull(ReflectionUtilsTestClass.class, "fieldToResetPrimitive");
        Assert.assertEquals(0, ReflectionUtilsTestClass.fieldToResetPrimitive);
    }

    /**
     * getNestedClass(Class<?> parent, String nestedClassName)
     */
    @Test
    public void getNestedClass()
    {
        final Class<?> c = ReflectionUtils.getNestedClass(ReflectionUtilsTestClass.class, "NestedClass");
        Assert.assertEquals(ReflectionUtilsTestClass.NestedClass.class, c);
    }

    /**
     * getNestedClass(Class<?> parent, String nestedClassName)
     */
    @Test(expected = IllegalStateException.class)
    public void getNestedClass_Fail()
    {
        ReflectionUtils.getNestedClass(ReflectionUtilsTestClass.class, "Nope");
    }

    /**
     * getMethod(Class<?> declaringClass, String methodName, Class<?>... parameterTypes)
     */
    @Test
    public void getMethod_String()
    {
        final Method m = ReflectionUtils.getMethod(ReflectionUtilsTestClass.class, "getMethod", String.class);
        Assert.assertEquals("getMethod", m.getName());
        Assert.assertEquals(String.class, m.getReturnType());
    }

    /**
     * getMethod(Class<?> declaringClass, String methodName, Class<?>... parameterTypes)
     */
    @Test
    public void getMethod_Int()
    {
        final Method m = ReflectionUtils.getMethod(ReflectionUtilsTestClass.class, "getMethod", int.class);
        Assert.assertEquals("getMethod", m.getName());
        Assert.assertEquals(int.class, m.getReturnType());
    }

    /**
     * getMethod(Class<?> declaringClass, String methodName, Class<?>... parameterTypes)
     */
    @Test(expected = IllegalStateException.class)
    public void getMethod_Exception()
    {
        ReflectionUtils.getMethod(ReflectionUtilsTestClass.class, "getMethod", long.class);
    }

    // getMethodWithFallback(Class<?> clazz, String methodName, Class<?>... parameterTypes)
    /**
     * invokeMethod(Object o, Method m, Object... args)
     */
    @Test
    public void testInvokeMethod()
    {
        final Method m = ReflectionUtils.getMethod(ReflectionUtilsTestClass.class, "getMethod", String.class);
        final Object result = ReflectionUtils.invokeMethod(new ReflectionUtilsTestClass(1), m, "foofoo");
        Assert.assertEquals("foofoo", result);
    }

    /**
     * readField(Class<?> clazz, Object object, String fieldName)
     */
    @Test
    public void testGetField()
    {
        final Object h = ReflectionUtils.readField(ReflectionUtilsTestClass.class, new ReflectionUtilsTestClass(123), "p2");
        Assert.assertEquals(123, h);
    }

    /**
     * readField(Class<?> clazz, Object object, String fieldName)
     */
    @Test(expected = Exception.class)
    public void testGetField_Exception()
    {
        ReflectionUtils.readField(ReflectionUtilsTestClass.class, new ReflectionUtilsTestClass(123), "none");
    }

    /**
     * resetPrimitiveField(Field fieldToSet) throws IllegalArgumentException, IllegalAccessException
     * 
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void resetPrimitiveField() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        final Field booleanField = ReflectionUtils.getField(ReflectionUtilsTestClass.class, "primitiveFieldBoolean");
        ReflectionUtils.resetPrimitiveField(booleanField);
        Assert.assertEquals(false, ReflectionUtilsTestClass.primitiveFieldBoolean);

        final Field byteField = ReflectionUtils.getField(ReflectionUtilsTestClass.class, "primitiveFieldByte");
        ReflectionUtils.resetPrimitiveField(byteField);
        Assert.assertEquals(0, ReflectionUtilsTestClass.primitiveFieldByte);

        final Field charField = ReflectionUtils.getField(ReflectionUtilsTestClass.class, "primitiveFieldChar");
        ReflectionUtils.resetPrimitiveField(charField);
        Assert.assertEquals(0, ReflectionUtilsTestClass.primitiveFieldChar);

        final Field doubleField = ReflectionUtils.getField(ReflectionUtilsTestClass.class, "primitiveFieldDouble");
        ReflectionUtils.resetPrimitiveField(doubleField);
        Assert.assertEquals(0.0, ReflectionUtilsTestClass.primitiveFieldDouble, 0.0);

        final Field floatField = ReflectionUtils.getField(ReflectionUtilsTestClass.class, "primitiveFieldFloat");
        ReflectionUtils.resetPrimitiveField(floatField);
        Assert.assertEquals(0.0f, ReflectionUtilsTestClass.primitiveFieldFloat, 0.0);

        final Field intField = ReflectionUtils.getField(ReflectionUtilsTestClass.class, "primitiveFieldInt");
        ReflectionUtils.resetPrimitiveField(intField);
        Assert.assertEquals(0, ReflectionUtilsTestClass.primitiveFieldInt);

        final Field longField = ReflectionUtils.getField(ReflectionUtilsTestClass.class, "primitiveFieldLong");
        ReflectionUtils.resetPrimitiveField(longField);
        Assert.assertEquals(0L, ReflectionUtilsTestClass.primitiveFieldLong);

        final Field shortField = ReflectionUtils.getField(ReflectionUtilsTestClass.class, "primitiveFieldShort");
        ReflectionUtils.resetPrimitiveField(shortField);
        Assert.assertEquals(0, ReflectionUtilsTestClass.primitiveFieldShort);
    }

    /**
     * resetPrimitiveField(Field fieldToSet) throws IllegalArgumentException, IllegalAccessException
     * 
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void resetPrimitiveField_Exception()
        throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        final Field field = ReflectionUtils.getField(ReflectionUtilsTestClass.class, "testWriteStaticField");
        ReflectionUtils.resetPrimitiveField(field);
    }

    /**
     * makeFieldNonFinal(Field field)
     * 
     * @throws NoSuchFieldException
     * @throws SecurityException
     */
    @Test
    public void makeFieldNonFinal() throws SecurityException, NoSuchFieldException
    {
        final Field field = ReflectionUtils.getField(ReflectionUtilsTestClass.class, "finalField");
        ReflectionUtils.makeFieldNonFinal(field);
        Assert.assertFalse(Modifier.isFinal(field.getModifiers()));
    }

    /**
     * callStaticMethod(Class<?> clazz, String methodName, Object... args)
     */
    @Test
    public void callStaticMethod()
    {
        final Object result = ReflectionUtils.callStaticMethod(ReflectionUtilsTestClass.class, "callAStaticMethod", "test");
        Assert.assertEquals("test", result);
    }

    /**
     * callMethod(Class<?> clazz, Object object, String methodName, Object... args)
     */
    @Test
    public void callMethod()
    {
        final Object result = ReflectionUtils.callMethod(ReflectionUtilsTestClass.class, new ReflectionUtilsTestClass(1), "getMethod",
                                                         "data");
        Assert.assertEquals("data", result);
    }

}
