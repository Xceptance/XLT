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

public class ReflectionUtilsTestClass
{
    public static String testWriteStaticField = "foo";

    public static String fieldToReset = "resetme";

    public static int fieldToResetPrimitive = 4200;

    public String p1;

    public int p2;

    public final static String finalField = "I am final.";

    public static boolean primitiveFieldBoolean = true;

    public static byte primitiveFieldByte = 8;

    public static char primitiveFieldChar = 'c';

    public static double primitiveFieldDouble = 42.42d;

    public static float primitiveFieldFloat = 42.42f;

    public static int primitiveFieldInt = 42;

    public static long primitiveFieldLong = 42L;

    public static short primitiveFieldShort = 42;

    public ReflectionUtilsTestClass(final String p1)
    {
        this.p1 = p1;
    }

    public ReflectionUtilsTestClass(final int p2)
    {
        this.p2 = p2;
    }

    public String getMethod(final String s)
    {
        return s;
    }

    public int getMethod(final int i)
    {
        return i;
    }

    public static String callAStaticMethod(final String a)
    {
        return a;
    }

    class NestedClass
    {
        public String getFoo()
        {
            return "foo";
        }
    }

}
