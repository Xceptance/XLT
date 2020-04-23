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
package com.xceptance.xlt.engine;

import com.xceptance.xlt.api.util.XltException;

/**
 * Thrown in a virtual user's thread to indicate that the current transaction was interrupted. In such a case, the test
 * code should abort the current transaction as quickly as possible.
 */
public class TransactionInterruptedException extends XltException
{
    /**
     * Constructor.
     */
    public TransactionInterruptedException()
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
    public TransactionInterruptedException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the message
     */
    public TransactionInterruptedException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the cause
     */
    public TransactionInterruptedException(final Throwable cause)
    {
        super(cause);
    }
}
