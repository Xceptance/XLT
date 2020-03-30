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
package com.xceptance.common.util;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

/**
 * The {@link ParameterCheckUtils} class makes verifying method parameters easy.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public final class ParameterCheckUtils
{
    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private ParameterCheckUtils()
    {
    }

    /**
     * Checks that the passed parameter is not <code>null</code>.
     * 
     * @param parameter
     *            the parameter to check
     * @param parameterName
     *            the parameter name
     * @throws IllegalArgumentException
     *             if the parameter is <code>null</code>
     */
    public static void isNotNull(final Object parameter, final String parameterName)
    {
        if (parameter == null)
        {
            doThrow(parameterName, Reason.NULL);
        }
    }

    /**
     * Checks that the passed string parameter is not <code>null</code> or empty.
     * 
     * @param parameter
     *            the parameter to check
     * @param parameterName
     *            the parameter name
     * @throws IllegalArgumentException
     *             if the parameter is <code>null</code> or empty
     */
    public static void isNotNullOrEmpty(final String parameter, final String parameterName)
    {
        isNotNull(parameter, parameterName);
        if (parameter.trim().length() == 0)
        {
            doThrow(parameterName, Reason.EMPTY);
        }
    }

    /**
     * Checks that the passed string array parameter is not <code>null</code> or empty.
     * 
     * @param parameter
     *            the parameter to check
     * @param parameterName
     *            the parameter name
     * @throws IllegalArgumentException
     *             if the parameter is <code>null</code>
     */
    public static void isNotNullOrEmpty(final String[] parameter, final String parameterName)
    {
        isNotNull(parameter, parameterName);
        isGreaterThan(parameter.length, 0, parameterName);
    }

    /**
     * Checks if the passed file fulfills the following criteria:
     * <ul>
     * <li>it is not <code>null</code></li>
     * <li>it is a regular file</li>
     * <li>it exists</li>
     * <li>it can be read</li>
     * </ul>
     * If any of the given criteria checks fails then an <code>IllegalArgumentException</code> is thrown.
     * 
     * @param file
     *            the file to be checked
     * @param parameterName
     *            the name of the parameter
     * @throws IllegalArgumentException
     *             thrown when the given file is not a regular file, an existent file or an unreadable file.
     */
    public static void isReadableFile(final File file, final String parameterName)
    {
        isNotNull(file, parameterName);

        Reason reason = null;

        if (!file.exists())
        {
            reason = Reason.NON_EXISTENT;
        }
        else if (!file.isFile())
        {
            reason = Reason.NOT_FILE;
        }
        else if (!file.canRead())
        {
            reason = Reason.UNREADABLE;
        }

        if (reason != null)
        {
            doThrow(parameterName, reason, file.getAbsolutePath());
        }
    }

    /**
     * Checks if the passed file object fulfills the following criteria:
     * <ul>
     * <li>it is not <code>null</code></li>
     * <li>it is a regular file</li>
     * <li>it exists</li>
     * <li>it can be read</li>
     * </ul>
     * If any of the given criteria checks fails then an <code>IllegalArgumentException</code> is thrown.
     * 
     * @param file
     *            the file object to be checked
     * @param parameterName
     *            the name of the parameter
     * @throws IllegalArgumentException
     *             thrown when the given file is not a regular file, an existent file or an unreadable file.
     */
    public static void isReadableFile(final FileObject file, final String parameterName)
    {
        isNotNull(file, parameterName);

        Reason reason = null;

        try
        {
            if (!file.exists())
            {
                reason = Reason.NON_EXISTENT;
            }
            else if (file.getType() != FileType.FILE)
            {
                reason = Reason.NOT_FILE;
            }
            else if (!file.isReadable())
            {
                reason = Reason.UNREADABLE;
            }

        }
        catch (final FileSystemException e)
        {
            reason = Reason.UNACCESSIBLE;
        }

        if (reason != null)
        {
            doThrow(parameterName, reason, file.getName().getURI());
        }
    }

    /**
     * Checks if the passed file denotes a writable directory by testing the following criteria:
     * <ul>
     * <li>it is not the null reference</li>
     * <li>it is a directory</li>
     * <li>it is writable</li>
     * </ul>
     * If any of the above criteria is not fulfilled an IllegalArgumentException will be thrown.
     * 
     * @param file
     *            the directory to check
     * @param parameterName
     *            the name of the parameter
     */
    public static void isWritableDirectory(final File file, final String parameterName)
    {
        isNotNull(file, parameterName);
        Reason reason = null;
        if (!file.isDirectory())
        {
            reason = Reason.NOT_DIRECTORY;
        }
        else if (!file.exists())
        {
            reason = Reason.NON_EXISTENT;
        }
        else if (!file.canWrite())
        {
            reason = Reason.UNWRITABLE;
        }

        if (reason != null)
        {
            doThrow(parameterName, reason, file.getAbsolutePath());
        }
    }

    /**
     * Checks if the passed file denotes a writable file by testing the following criteria:
     * <ul>
     * <li>it is not the null reference</li>
     * <li>it doesn't exist <b>OR</b>
     * <ul>
     * <li>it is a regular file <b>AND</b></li>
     * <li>it is writable</li>
     * </ul>
     * </li>
     * </ul>
     * If any of the above criteria is not fulfilled an IllegalArgumentException will be thrown.
     * 
     * @param file
     *            the directory to check
     * @param parameterName
     *            the name of the parameter
     */
    public static void isWritableFile(final File file, final String parameterName)
    {
        isNotNull(file, parameterName);
        Reason reason = null;
        if (file.exists())
        {
            if (!file.isFile())
            {
                reason = Reason.NOT_FILE;
            }
            else if (!file.canWrite())
            {
                reason = Reason.UNWRITABLE;
            }
        }

        if (reason != null)
        {
            doThrow(parameterName, reason, file.getAbsolutePath());
        }
    }

    /**
     * Checks the given object if it references to a valid array instance. This is done by checking the following
     * conditions:
     * <ul>
     * <li>it must not be null</li>
     * <li>its class must be assignable to <code>Object[].class</code></li>
     * </ul>
     * If any of the above condition is not met, an <code>IllegalArgumentException</code> is thrown to indicate the
     * appropriate error.
     * 
     * @param arr
     *            the object to check
     * @param parameterName
     *            the name of the parameter
     */
    public static void isValidArray(final Object arr, final String parameterName)
    {
        isNotNull(arr, parameterName);
        if (!(Object[].class.isAssignableFrom(arr.getClass())))
        {
            doThrow(parameterName, Reason.NOT_ARRAY);
        }
    }

    /**
     * Checks the given object if it references to a valid array instance of the given minimum size.
     * 
     * @param arr
     *            the object to check
     * @param minSize
     *            the minimum size of the array
     * @param parameterName
     *            the name of the parameter
     * @see #isValidArray(Object, String)
     */
    public static void isValidArrayOfMinSize(final Object arr, final int minSize, final String parameterName)
    {
        isValidArray(arr, parameterName);
        if (((Object[]) arr).length < minSize)
        {
            doThrow(parameterName, Reason.LESS, Integer.toString(minSize));
        }
    }

    /**
     * Checks whether the passed parameter is a non-empty string.
     * 
     * @param str
     *            the string to check
     * @param parameterName
     *            the parameter name
     * @throws IllegalArgumentException
     *             if the parameter is <code>null</code> or an empty string
     */
    public static void isNonEmptyString(final String str, final String parameterName)
    {
        isNotNull(str, parameterName);
        if (!(str.trim().length() > 0))
        {
            doThrow(parameterName, Reason.EMPTY);
        }
    }

    /**
     * Checks whether the passed value is greater than the specified limit.
     * 
     * @param value
     *            the value to check
     * @param limit
     *            the limit
     * @param parameterName
     *            the parameter name
     * @throws IllegalArgumentException
     *             if the parameter value is not greater than the limit
     */
    public static void isGreaterThan(final int value, final int limit, final String parameterName)
    {
        if (!(value > limit))
        {
            doThrow(parameterName, Reason.LESS, Integer.toString((limit + 1)));
        }
    }

    /**
     * Checks whether the passed value is less than the specified limit.
     * 
     * @param value
     *            the value to check
     * @param limit
     *            the limit
     * @param parameterName
     *            the parameter name
     * @throws IllegalArgumentException
     *             if the parameter value is not greater than the limit
     */
    public static void isLessThan(final int value, final int limit, final String parameterName)
    {
        if (!(value < limit))
        {
            doThrow(parameterName, Reason.GREATER, Integer.toString(limit));
        }
    }

    /**
     * Checks whether the passed value is less than the specified limit.
     * 
     * @param value
     *            the value to check
     * @param parameterName
     *            the parameter name
     * @throws IllegalArgumentException
     *             if the parameter value is negative
     */
    public static void isNotNegative(final int value, final String parameterName)
    {
        if (value < 0)
        {
            doThrow(parameterName, Reason.NEGATIVE);
        }
    }

    /**
     * Checks whether the passed parameter is a relative path.
     * 
     * @param path
     *            the path to check
     * @param parameterName
     *            the parameter name
     * @throws IllegalArgumentException
     *             if the parameter path is not a relative path.
     */
    public static void isRelativePath(final String path, final String parameterName)
    {
        isNonEmptyString(path, parameterName);

        if (path.charAt(0) == '/')
        {
            doThrow(parameterName, Reason.ABSOLUTE);
        }
    }

    /**
     * Throws an IllegalArgumentException. The exception message is constructed using the given parameter name, reason
     * and additional arguments.
     * 
     * @param parameterName
     *            parameter name
     * @param reason
     *            reason
     * @param arguments
     *            additional arguments
     * @throws IllegalArgumentException
     */
    private static void doThrow(final String parameterName, final Reason reason, final String... arguments)
    {
        String s = "Parameter '" + parameterName + "' is invalid, because " + reason.toString();
        if (arguments != null && arguments.length > 0)
        {
            s += " -> " + Arrays.toString(arguments);
        }
        throw new IllegalArgumentException(s);
    }

    /**
     * Error reasons.
     */
    private static enum Reason
    {
        NULL,
        EMPTY,
        NEGATIVE,
        UNREADABLE("an unreadable file"),
        UNWRITABLE("an unwritable file"),
        ABSOLUTE("an absolute path"),
        LESS("less than"),
        GREATER("greater than"),
        NOT_ARRAY("not an array"),
        NOT_DIRECTORY("not a directory"),
        NON_EXISTENT("non-existent"),
        NOT_FILE("not a file"),
        UNACCESSIBLE("an unaccessible file");

        /**
         * Description of reason.
         */
        private final String message;

        /**
         * Creates a new reason using the given description.
         * 
         * @param reasonDesc
         *            description of reason
         */
        private Reason(final String reasonDesc)
        {
            message = reasonDesc;
        }

        /**
         * Default constructor.
         */
        private Reason()
        {
            this(null);
        }

        /**
         * Returns the reason message.
         * 
         * @return message of this reason
         */
        public String getMessage()
        {
            return (message == null) ? name().toLowerCase() : message;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "its value is " + getMessage();
        }
    }
}
