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
package com.xceptance.xlt.api.util;

/**
 * The base class for all XLT exceptions. This class may also serve as a "general" exception.
 */
public class XltException extends RuntimeException
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public XltException()
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
    public XltException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the message
     */
    public XltException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the cause
     */
    public XltException(final Throwable cause)
    {
        super(cause);
    }
}
