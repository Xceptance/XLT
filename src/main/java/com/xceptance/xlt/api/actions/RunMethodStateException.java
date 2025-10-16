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
package com.xceptance.xlt.api.actions;

/**
 * This exception indicates, that the run() method of an {@link AbstractAction} has already been called.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class RunMethodStateException extends IllegalStateException
{
    /**
     * Serial version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public RunMethodStateException()
    {
        super("run() method was already called. Can only be executed once.");
    }

    /**
     * Constructor.
     * 
     * @param msg
     *            message to report
     */
    public RunMethodStateException(final String msg)
    {
        super(msg);
    }
}
