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
package com.xceptance.xlt.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.lang.ThreadUtils;
import com.xceptance.xlt.agentcontroller.AgentController;
import com.xceptance.xlt.agentcontroller.AgentStatus;
import com.xceptance.xlt.agentcontroller.TestUserStatus;
import com.xceptance.xlt.api.engine.GlobalClock;

/**
 * Class description.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class AgentStatusPusher extends Thread
{
    private static final Logger log = LoggerFactory.getLogger(AgentStatusPusher.class);

    private static final int PUSH_INTERVAL = 1000;

    private final AgentController agentController;

    private final AgentStatus agentStatus;

    private final int maxErrors;

    // #3363
    private int stackOverflowErrorLogAttempts;

    /**
     * Creates a status pusher object, that pushes the status information to the given agent controller.
     * 
     * @param agentController
     *            agent controller
     * @param agentStatus
     *            status of agent
     * @param maxErrors
     *            maximum of errors
     */
    public AgentStatusPusher(final AgentController agentController, final AgentStatus agentStatus, final int maxErrors)
    {
        super("AgentStatusPusher");

        this.agentStatus = agentStatus;
        this.agentController = agentController;
        this.maxErrors = maxErrors;
    }

    public void pushStatus()
    {
        try
        {
            agentController.setAgentStatus(agentStatus);
        }
        catch (final Throwable t)
        {
            // #3363
            // log only the first N stack overflow errors
            if (t instanceof StackOverflowError && ++stackOverflowErrorLogAttempts > 5)
            {
                return;
            }

            log.error("Failed to push agent status to agent controller:", t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run()
    {
        log.debug("Agent status pusher thread started.");

        while (true)
        {
            updateElapsedTimeAndProgress();
            pushStatus();
            checkMaxErrors();

            ThreadUtils.sleep(PUSH_INTERVAL);
        }
    }

    private void checkMaxErrors()
    {
        int errors = 0;

        for (final TestUserStatus status : agentStatus.getTestUserStatusList())
        {
            errors += status.getErrors();
        }

        if (errors >= maxErrors)
        {
            log.error("Agent will terminate because the maximum error limit (" + maxErrors + ") is reached.");

            System.exit(AgentExitCodes.TOO_MANY_TRANSACTION_ERRORS);
        }
    }

    private void updateElapsedTimeAndProgress()
    {
        final long now = GlobalClock.getInstance().getTime();

        for (final TestUserStatus status : agentStatus.getTestUserStatusList())
        {
            final long elapsedTime = now - status.getStartDate();
            final long duration = status.getEndDate() - status.getStartDate();

            // update elapsed time, but only if the user is still running
            final TestUserStatus.State state = status.getState();
            if (state == TestUserStatus.State.Running || state == TestUserStatus.State.Waiting)
            {
                status.setElapsedTime(elapsedTime);
            }

            // update progress
            if (duration > 0)
            {
                final int progress = Math.min((int) (elapsedTime * 100 / duration), 99);
                if (progress > status.getPercentageComplete())
                {
                    status.setPercentageComplete(progress);
                }
            }
        }
    }
}
