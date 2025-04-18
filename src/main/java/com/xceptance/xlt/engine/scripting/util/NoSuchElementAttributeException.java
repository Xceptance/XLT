/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.scripting.util;

import com.xceptance.xlt.api.util.XltException;

/**
 * Exception indicating an element attribute lookup miss.
 */
public class NoSuchElementAttributeException extends XltException
{

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public NoSuchElementAttributeException()
    {
        super();
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public NoSuchElementAttributeException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the detail message
     */
    public NoSuchElementAttributeException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the cause
     */
    public NoSuchElementAttributeException(final Throwable cause)
    {
        super(cause);
    }

}
