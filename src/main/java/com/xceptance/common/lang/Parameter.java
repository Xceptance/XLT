package com.xceptance.common.lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Reflection fails when the parameter type does not EXACTLY matches the type from the method declaration. For example
 * giving an {@link ArrayList} as parameter type whereas the method declaration contains a {@link List} results in a
 * {@link NoSuchMethodException}. This is a small helper class to overcome these shortcomings of
 * {@link ReflectionUtils#getNewInstance(Class, Object...)}. So it has an a value field and an explicit field for the
 * value class. Due to using generics with the value of being type T and the class being of type ? super T simple errors
 * when setting them will cause compile time errors.
 * 
 * @param T
 *            the type of the value, the class of the value must be of type ? super T
 * @see #valueOf(Object, Class) to get more information
 * @author Sebastian Oerding
 */
public class Parameter<T>
{
    private final Class<? super T> parameterClass;

    private final T value;

    private Parameter(final T value, final Class<? super T> declaredParamClass)
    {
        parameterClass = declaredParamClass;
        this.value = value;
    }

    /**
     * Returns a new parameter instance with the arguments as values.
     * <p>
     * Note that the generic type of the returned instance will not be any primitive but the type of the wrapper class
     * instead. This is due to boxing / unboxing. But it is fine as long as you give the correct class as the second
     * argument. Take care that you exactly match the classes declared in the method signature or you will not be able
     * to find the method by using reflection, this also holds true for interfaces used in method declarations!
     * </p>
     * 
     * @param value
     *            the value for the parameter
     * @param declaredParamClass
     *            the class for the parameter
     */
    public static <T> Parameter<T> valueOf(final T value, final Class<? super T> declaredParamClass)
    {
        return new Parameter<T>(value, declaredParamClass);
    }

    Class<? super T> getDeclaredParameterClass()
    {
        return parameterClass;
    }

    T getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("Value : ");
        sb.append(value);
        sb.append(", declared parameter class : ");
        sb.append(parameterClass.getName());
        return sb.toString();
    }
}
