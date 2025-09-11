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
package com.xceptance.xlt.engine;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to decouple the transaction time from the session to avoid tight coupling
 *
 * @author Rene Schwietzke (Xceptance)
 * @since 7.0.0
 */
class TransactionExpirationTimer
{
    /**
     * The log facility.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TransactionExpirationTimer.class);

    /**
     * The global transaction expiration timer responsible for all sessions.
     */
    private static final Timer transactionExpirationTimer = new Timer("TransactionExpirationTimer", true);

    public static TransactionTimerTask addTimerTask(final String userId, final long transactionTimeout)
    {
        // needs to create a new timer task each time as timer tasks cannot be reused
        final var transactionExpirationTimerTask = new TransactionTimerTask(userId, transactionTimeout);
        transactionExpirationTimer.schedule(transactionExpirationTimerTask, transactionTimeout);

        return transactionExpirationTimerTask;
    }

    public static class TransactionTimerTask extends TimerTask
    {
        public volatile boolean isExpired = false;
        private final String userId;
        private final long transactionTimeout;

        public TransactionTimerTask(final String userId, long transactionTimeout)
        {
            this.userId = userId;
            this.transactionTimeout = transactionTimeout;
        }

        @Override
        public void run()
        {
            // mark the transaction as expired -> the user will hopefully end its transaction voluntarily
            isExpired = true;

            if (LOG.isWarnEnabled())
            {
                LOG.warn(String.format("User '%s' exceeds maximum permitted run time of %,d ms. Will mark it as expired.",
                                       userId, transactionTimeout));
            }
        }
    }
}
