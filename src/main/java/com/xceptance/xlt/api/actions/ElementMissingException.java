/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.actions;

/**
 * Indicates a missing condition when loading a page. For instance a missing element we can click on.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class ElementMissingException extends Exception
{
    /**
     * The serialVersionUID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * Default constructor.
     */
    public ElementMissingException()
    {
        super();
    }

    /**
     * Creates a new exception using the given message.
     * 
     * @param message
     *            exception message to use
     */
    public ElementMissingException(final String message)
    {
        super(message);
    }

    /**
     * Creates a new exception using the given throwable instance.
     * 
     * @param cause
     *            cause of this exception
     */
    public ElementMissingException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates a new exception using the given exception message and cause.
     * 
     * @param message
     *            exception message
     * @param cause
     *            cause of this exception
     */
    public ElementMissingException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

}
