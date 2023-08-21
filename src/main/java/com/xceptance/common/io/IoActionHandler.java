package com.xceptance.common.io;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link IoActionHandler} runs an {@link IoAction}, and if that action fails with an {@link IOException} (or a
 * subclass), repeats the action unless the action eventually succeeds or the maximum number of attempts is reached.
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
     * The current number of attempts left.
     */
    private int remainingAttempts;

    /**
     * Creates a new {@link IoActionHandler} initialized with the given number of attempts.
     */
    public IoActionHandler(final int attempts)
    {
        this.remainingAttempts = attempts;
    }

    /**
     * Runs the passed {@link IoAction} performing retries if needed.
     * 
     * @param action
     *            the action to perform
     * @return the result of the action upon successful attempt
     * @throws IOException
     *             the exception thrown by the underlying action if the maximum number of attempts was reached
     */
    public <T> T run(final IoAction<T> action) throws IOException
    {
        while (true)
        {
            remainingAttempts--;

            try
            {
                return action.run();
            }
            catch (final IOException e)
            {
                if (remainingAttempts <= 0)
                {
                    throw e;
                }
                else
                {
                    log.debug("Retry action because of: {}", e.toString());
                }
            }
        }
    }
}
