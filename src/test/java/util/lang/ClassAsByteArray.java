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
package util.lang;

/**
 * This is a helper class for {@link ClassFromByteArrayLoader}. It encapsulates classes given together with their
 * corresponding name as byte array.
 * 
 * @author Sebastian Oerding
 */
public final class ClassAsByteArray
{
    private final String className;

    private final byte[] classAsBytes;

    /**
     * Returns a new ClassAsByteArray with the argument data. Neither for the class name nor for the byte array
     * <code>null</code> or an empty / zero sized value is accepted.
     * 
     * @param className
     *            the name of the class
     * @param classAsBytes
     *            the class file as byte array
     * @throws IllegalArgumentException
     *             if at least one the arguments is <code>null</code> or empty
     */
    public ClassAsByteArray(final String className, final byte[] classAsBytes)
    {
        if (className == null || className.isEmpty())
        {
            throw new IllegalArgumentException("The class name neither may be null nor empty!");
        }
        if (classAsBytes == null || classAsBytes.length == 0)
        {
            throw new IllegalArgumentException("The byte array containing the class neither may be null nor empty!");
        }

        this.className = className;
        this.classAsBytes = classAsBytes;
    }

    /**
     * The name of the class represented by the byte array containing the class.
     * 
     * @return the className
     * @see #getClassAsBytes()
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * Returns the byte array representing a class. Due to performance issues the returned array is not a clone. Thus
     * YOU SHOULD NOT modify this array unless you really know what you are doing.
     * 
     * @return the classAsBytes
     */
    public byte[] getClassAsBytes()
    {
        return classAsBytes;
    }
}
