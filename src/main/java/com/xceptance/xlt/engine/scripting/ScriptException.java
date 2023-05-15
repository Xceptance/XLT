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
package com.xceptance.xlt.engine.scripting;

import com.xceptance.xlt.api.util.XltException;

/**
 * The base class for all exceptions thrown by the script interpreter framework. This class may also serve as a
 * "general" script interpreter exception.
 */
public class ScriptException extends XltException
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -3888385537400709411L;

    /**
     * Constructor.
     */
    public ScriptException()
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
    public ScriptException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the message
     */
    public ScriptException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the cause
     */
    public ScriptException(final Throwable cause)
    {
        super(cause);
    }
}
