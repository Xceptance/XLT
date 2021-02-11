/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
 * An exception to indicate that the wrong page type was loaded and the conversion to an XML or HTML page would fail.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class UnexpectedPageTypeException extends Exception
{
    /**
     * To satisfy Java
     */
    private static final long serialVersionUID = -8147092739069610752L;

    /**
     * Constructor.
     * 
     * @param msg
     *            the message to use
     */
    public UnexpectedPageTypeException(final String msg)
    {
        super(msg);
    }

    /**
     * Constructor.
     * 
     * @param msg
     *            the message to use
     * @param e
     *            the previous exception
     */
    public UnexpectedPageTypeException(final String msg, final Throwable e)
    {
        super(msg, e);
    }

    /**
     * Constructor.
     * 
     * @param e
     *            The previous exception
     */
    public UnexpectedPageTypeException(final Throwable e)
    {
        super(e);
    }
}
