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
