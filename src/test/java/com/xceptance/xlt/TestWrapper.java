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
package com.xceptance.xlt;

/**
 * Convenience class for tests that should throw an exception.
 * 
 * @author Sebastian Oerding
 */
public abstract class TestWrapper
{
    private static final int BIG = 512;

    private final Class<? extends Throwable> errorType;

    private final String errorMessage;

    private final String noErrorMessage;

    /**
     * @param errorType
     *            the type of the expected error
     * @param errorMessage
     *            the expected error message
     * @param noErrorMessage
     *            the message to show when the expected error does not occur
     */
    public <T extends Throwable> TestWrapper(final Class<T> errorType, final String errorMessage, final String noErrorMessage)
    {
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.noErrorMessage = noErrorMessage;
    }

    /**
     * Creates a TesWrapper with a default error message for the case that no error occurs when executing it.
     * 
     * @param errorType
     *            the type of the expected error
     * @param errorMessage
     *            the expected error message
     */
    public <T extends Throwable> TestWrapper(final Class<T> errorType, final String errorMessage)
    {
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        noErrorMessage = "Expected a " + errorType.getName() + " but did not get any!";
    }

    /**
     * @throws AssertionError
     *             if no error occurs, an occur of the wrong type occurs or if an error message unequal to
     *             <code>null</code> was given to this instance and it is not equal to the actual message.
     */
    public final void execute()
    {
        boolean expectedErrorWasThrown = false;
        try
        {
            run();
        }
        catch (final Throwable e)
        {
            if (e.getClass() != errorType || (errorMessage != null && !errorMessage.equals(e.getMessage())))
            {
                final StringBuilder sb = new StringBuilder(BIG);
                sb.append("Expected error of type \"");
                sb.append(errorType.getName());
                if (errorMessage != null)
                {
                    sb.append("\" with message \"");
                    sb.append(errorMessage);
                }
                sb.append("\" but got error of type \"");
                sb.append(e.getClass().getName());
                if (errorMessage != null)
                {
                    sb.append("\" with message \"");
                    sb.append(e.getMessage());
                }
                sb.append("\"!");
                throw new AssertionError(sb.toString());
            }
            expectedErrorWasThrown = true;
        }
        if (!expectedErrorWasThrown)
        {
            throw new AssertionError(noErrorMessage);
        }
    }

    protected abstract void run() throws Throwable;
}
