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
package com.xceptance.xlt.engine.scripting;

/**
 * Thrown if page load was not complete within a certain time period.
 */
public class PageLoadTimeoutException extends ScriptException
{
    /**
     * Constructor.
     */
    public PageLoadTimeoutException()
    {
        super();
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public PageLoadTimeoutException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the message
     */
    public PageLoadTimeoutException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the cause
     */
    public PageLoadTimeoutException(final Throwable cause)
    {
        super(cause);
    }
}
