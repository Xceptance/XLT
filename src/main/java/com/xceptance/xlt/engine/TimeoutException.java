/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine;

/**
 * Thrown in case waiting for a condition to become true has timed out.
 */
public class TimeoutException extends RuntimeException
{
    /**
     * Default constructor.
     */
    public TimeoutException()
    {
        super();
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the detail message
     */
    public TimeoutException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the cause of this exception
     */
    public TimeoutException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause of this exception
     */
    public TimeoutException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
