/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package util.lang;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

/**
 * When testing sometimes it is getting difficult due to static initializations blocks or other code patterns that
 * are difficult or even impossible to test without reloading a class. The purpose of this class is to provide a class
 * loader that can be used to load specific classes from byte arrays and behave as used for all other classes. By using
 * different instances the same class can be loaded several times which enables checks of static blocks (for examples if
 * a static block uses a property you won't notice any change as long as the class is not reloaded but by using
 * different instances of this class you can check for it).
 * 
 * @author Sebastian Oerding
 */
public final class ClassFromByteArrayLoader extends ClassLoader
{
    /**
     * The classes to deal with especially mapped by their names.
     */
    private final Map<String, Class<?>> classesByNames = new HashMap<String, Class<?>>();

    /**
     * Convenience constructor for the most common case of having a single specific class. Returns a new class loader
     * with the argument class already resolved. So to get the class just call {@link #loadClass(String)} with the class
     * name on this instance or use for convenience {@link #getFreshlyLoadedClass(ClassAsByteArray)} if you do not need
     * to further call methods on this class loader.
     * 
     * @param classAsByteArray
     *            the class wrapped into a {@link ClassAsByteArray}
     * @throws IllegalArgumentException
     *             if the argument is <code>null</code>
     */
    private ClassFromByteArrayLoader(final ClassAsByteArray classAsByteArray)
    {
        if (classAsByteArray == null)
        {
            throw new IllegalArgumentException("You may not give \"null\" as value!");
        }

        prepareClass(classAsByteArray);
    }

    /**
     * Returns a new class loader with the argument classes already resolved. So to get the classes just call
     * {@link #loadClass(String)} with the class name for each or use for convenience
     * {@link #getFreshlyLoadedClasses(ClassAsByteArray...)} if you do not need to further call methods on this class
     * loader.
     * 
     * @param classesAsByteArrays
     *            the classes wrapped into a {@link ClassAsByteArray}
     * @throws IllegalArgumentException
     *             if the argument is <code>null</code> or empty
     */
    private ClassFromByteArrayLoader(final ClassAsByteArray... classesAsByteArrays)
    {
        if (classesAsByteArrays == null || classesAsByteArrays.length == 0)
        {
            throw new IllegalArgumentException("You may neither give \"null\" nor an empty array as value!");
        }

        for (final ClassAsByteArray caba : classesAsByteArrays)
        {
            prepareClass(caba);
        }
    }

    /**
     * @param classAsByteArray
     * @throws ClassFormatError
     */
    private void prepareClass(final ClassAsByteArray classAsByteArray) throws ClassFormatError
    {
        final String className = classAsByteArray.getClassName();
        final byte[] classBytes = classAsByteArray.getClassAsBytes();
        final Class<?> theClass = super.defineClass(null, classBytes, 0, classBytes.length);
        resolveClass(theClass);
        classesByNames.put(className, theClass);
    }

    /**
     * Returns the argument class freshly loaded into a new class loader.
     * 
     * @param classAsByteArray
     *            the class which to load
     * @return the argument class freshly loaded into a new class loader
     * @throws IllegalStateException
     *             if a class is not found
     */
    public static Class<?> getFreshlyLoadedClass(final ClassAsByteArray classAsByteArray)
    {
        try
        {
            return new ClassFromByteArrayLoader(classAsByteArray).loadClass(classAsByteArray.getClassName());
        }
        catch (final ClassNotFoundException e)
        {
            throw new IllegalStateException("Class not found!", e);
        }
    }

    /**
     * Returns the argument classes freshly loaded into a new class loader.
     * 
     * @param classAsByteArrayes
     *            the classes which to load
     * @return the argument classes freshly loaded into a new class loader being in the same order as the corresponding
     *         argument classes
     */
    public static Class<?>[] getFreshlyLoadedClasses(final ClassAsByteArray... classesAsByteArrays)
    {
        final Map<String, Class<?>> theClassesByNames = new ClassFromByteArrayLoader(classesAsByteArrays).classesByNames;
        final Class<?>[] returnValue = new Class<?>[theClassesByNames.size()];
        int index = 0;
        for (final Class<?> c : theClassesByNames.values())
        {
            // intentional side effect with index++
            returnValue[index++] = c;
        }
        return returnValue;
    }

    public static Class<?> getFreshlyLoadedClass(final Class<?> theClass)
    {
        final String className = theClass.getName().replace('.', '/') + ".class";
        final InputStream iStream = theClass.getClassLoader().getResourceAsStream(className);
        byte[] classAsBytes = null;
        try
        {
            classAsBytes = IOUtils.toByteArray(iStream);
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }

        final ClassAsByteArray caba = new ClassAsByteArray(className, classAsBytes);
        try
        {
            return new ClassFromByteArrayLoader(caba).loadClass(caba.getClassName());
        }
        catch (final ClassNotFoundException e)
        {
            throw new IllegalStateException("Class not found!", e);
        }
    }

    /**
     * Catches all calls for classes to deal especially. Returns such classes from the internals and forwards calls for
     * all other classes to the parent class loader.
     * 
     * @param name
     *            the name of the class to load
     * @return the identified class
     * @throws ClassNotFoundException
     *             if no class for the argument name is found
     */
    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException
    {
        if (classesByNames.containsKey(name))
        {
            return classesByNames.get(name);
        }
        return super.loadClass(name);
    }
}
