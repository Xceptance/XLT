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
package com.xceptance.common.io;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link IoActionHandler} runs an {@link IoAction}, and if that action fails with an {@link IOException} (or a
 * subclass), repeats the action until the action eventually succeeds or the maximum number of retries is reached.
 */
public class IoActionHandler
{
    /**
     * Functional interface describing an action that performs I/O and may fail with {@link IOException}.
     *
     * @param <T>
     *            the type of the result of the action
     */
    @FunctionalInterface
    public interface IoAction<T>
    {
        public T run() throws IOException;
    }

    private static final Logger log = LoggerFactory.getLogger(IoActionHandler.class);

    /**
     * The maximum number of retries.
     */
    private final int maxRetries;

    /**
     * Creates a new {@link IoActionHandler} initialized with the given number of retries.
     * 
     * @param maxRetries
     *            the maximum number of retries
     */
    public IoActionHandler(final int maxRetries)
    {
        this.maxRetries = maxRetries;
    }

    /**
     * Runs the passed {@link IoAction} performing retries if needed.
     *
     * @param action
     *            the action to perform
     * @return the result of the action upon successful attempt
     * @throws IOException
     *             the exception thrown by the underlying action if the maximum number of retries was reached
     */
    public <T> T run(final IoAction<T> action) throws IOException
    {
        int remainingRetries = maxRetries;

        while (true)
        {
            try
            {
                return action.run();
            }
            catch (final IOException e)
            {
                if (remainingRetries > 0)
                {
                    log.debug("Retry action because of: {}", e.toString());
                    remainingRetries--;
                }
                else
                {
                    throw e;
                }
            }
        }
    }
}
