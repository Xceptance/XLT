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
package util.lang;

/**
 * This is a helper class for {@link SimpleCompiler}. It encapsulates classes given together with their corresponding
 * name as strings.
 * 
 * @author Sebastian Oerding
 */
public final class ClassAsSourceCode
{
    private final String className;

    private final String classAsString;

    /**
     * Returns a new ClassAsByteArray with the argument data. Neither for the class name nor for the byte array
     * <code>null</code> or an empty / zero sized value is accepted.
     * 
     * @param className
     *            the name of the class
     * @param classAsString
     *            the class file as source code
     * @throws IllegalArgumentException
     *             if at least one the arguments is <code>null</code> or empty
     */
    public ClassAsSourceCode(final String className, final String classAsString)
    {
        if (className == null || className.isEmpty())
        {
            throw new IllegalArgumentException("The class name neither may be null nor empty!");
        }
        if (classAsString == null || classAsString.isEmpty())
        {
            throw new IllegalArgumentException("The string containing the source code neither may be null nor empty!");
        }

        this.className = className;
        this.classAsString = classAsString;
    }

    /**
     * The name of the class represented by the source code.
     * 
     * @return the className
     * @see #getClassAsString()
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * Returns the string representing the source code of a class.
     * 
     * @return the source code
     */
    public String getClassAsString()
    {
        return classAsString;
    }
}
