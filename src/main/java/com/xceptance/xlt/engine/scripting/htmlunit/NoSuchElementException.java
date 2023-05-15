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
package com.xceptance.xlt.engine.scripting.htmlunit;

import com.xceptance.xlt.api.util.XltException;

/**
 * Exception indicating an element lookup miss.
 */
public class NoSuchElementException extends XltException
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public NoSuchElementException()
    {
        super();
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the exception's description
     */
    public NoSuchElementException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the exception's cause
     */
    public NoSuchElementException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the exception's description
     * @param cause
     *            the exception's cause
     */
    public NoSuchElementException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

}
