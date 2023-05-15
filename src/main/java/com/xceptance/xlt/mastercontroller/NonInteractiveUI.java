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
package com.xceptance.xlt.mastercontroller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.agentcontroller.TestResultAmount;
import com.xceptance.xlt.api.util.XltException;

/**
 * The {@link NonInteractiveUI} is a user interface for the master controller which does not allow any user interaction,
 * but simply runs the commands passed on the command line. The supported commands are:
 * <ul>
 * <li>uploading the agent files to all agent controllers</li>
 * <li>starting the agents on all agent controller machines</li>
 * <li>aborting the agents on all agent controller machines</li>
 * <li>downloading the test results from all agent controllers</li>
 * <li>creating the test report</li>
 * </ul>
 */
public class NonInteractiveUI extends BasicConsoleUI
{
    /**
     * The supported commands.
     */
    public enum MasterControllerCommands
    {
     upload,
     start,
     abort,
     download,
     report;

        /**
         * Converts the command list to an array of corresponding enum values.
         *
         * @param commandList
         *            the comma-separated list of commands, such as "upload,start"
         * @return the corresponding enum values
         */
        public static MasterControllerCommands[] convert(final String commandList)
        {
            final String[] commands = parse(commandList);
            final MasterControllerCommands[] mcCommands = new MasterControllerCommands[commands.length];

            for (int i = 0; i < commands.length; i++)
            {
                mcCommands[i] = MasterControllerCommands.valueOf(commands[i]);
            }

            return mcCommands;
        }

        /**
         * Validates the command list and returns an array with those commands in the list that are unknown.
         *
         * @param commandList
         *            the comma-separated list of commands, such as "upload,start"
         * @return an array of unknown commands
         */
        public static String[] validate(final String commandList)
        {
            final String[] commands = parse(commandList);

            final List<String> unknownCommands = new ArrayList<>();
            for (final String command : commands)
            {
                try
                {
                    MasterControllerCommands.valueOf(command);
                }
                catch (final IllegalArgumentException e)
                {
                    unknownCommands.add(command);
                }
            }

            return unknownCommands.toArray(new String[unknownCommands.size()]);
        }

        /**
         * Parses the command list into separate commands.
         *
         * @param commandList
         *            the comma-separated list of commands, such as "upload,start"
         * @return an array of commands
         */
        private static String[] parse(final String commandList)
        {
            return StringUtils.split(commandList, ", ");
        }
    }

    /**
     * The maximum time [ms] to wait for all agent controllers to become alive.
     */
    private final long agentControllerTimeout;

    /**
     * The commands that are to be executed.
     */
    private final MasterControllerCommands[] commands;

    /**
     * The amount of test results to be downloaded.
     */
    private final TestResultAmount testResultAmount;

    /**
     * Creates a new {@link NonInteractiveUI} object.
     *
     * @param masterController
     *            the master controller to use
     * @param commandList
     *            a comma-separated list of commands
     * @param agentControllerTimeout
     *            the maximum time [ms] to wait for all agent controllers to become alive
     * @param resultAmount
     *            the amount of test results to download
     */
    public NonInteractiveUI(final MasterController masterController, final String commandList, final long agentControllerTimeout,
                            final TestResultAmount resultAmount)
    {
        super(masterController);

        commands = MasterControllerCommands.convert(commandList);
        this.agentControllerTimeout = agentControllerTimeout;
        this.testResultAmount = resultAmount;
    }

    /**
     * Runs the user interface.
     */
    @Override
    public void run()
    {
        try
        {
            // check if all agent controller instances became alive within the specified timeout
            checkAlive(agentControllerTimeout);

            // run the commands one after the other
            for (final MasterControllerCommands command : commands)
            {
                if (command == MasterControllerCommands.upload)
                {
                    upload();
                }
                else if (command == MasterControllerCommands.start)
                {
                    start();
                }
                else if (command == MasterControllerCommands.abort)
                {
                    abort();
                }
                else if (command == MasterControllerCommands.download)
                {
                    download();
                }
                else if (command == MasterControllerCommands.report)
                {
                    report();
                }
            }
        }
        finally
        {
            masterController.shutdown();
        }
    }

    private void checkAlive(final long timeout)
    {
        if (timeout > 0)
        {
            System.out.println("Waiting for agent controllers up to " + timeout / 1000 + "s.");

            final long deadline = System.currentTimeMillis() + timeout;

            while (System.currentTimeMillis() < deadline)
            {
                // check alive
                try
                {
                    masterController.checkAlive();
                    return;
                }
                catch (final Exception e)
                {
                    // ignore
                }

                // wait some time
                try
                {
                    Thread.sleep(1000);
                }
                catch (final Exception e)
                {
                    // ignore
                }
            }
        }

        // check one last time
        if (!checkAlive())
        {
            throw new XltException("There are unreachable agent controllers.");
        }
    }

    private void upload()
    {
        if (isLoadTestRunning())
        {
            throw new XltException("There is another load test running.");
        }

        if (!uploadAgentFiles())
        {
            throw new XltException("Uploading test suite failed.");
        }
    }

    private void start()
    {
        if (isLoadTestRunning())
        {
            throw new XltException("There is another load test running.");
        }

        if (!startAgents(null, false))
        {
            throw new XltException("Starting agents failed.");
        }
    }

    private void abort()
    {
        if (isLoadTestRunning())
        {
            if (!stopAgents())
            {
                throw new XltException("Stopping agents failed.");
            }
        }
    }

    private void download()
    {
        if (!downloadTestResults(testResultAmount))
        {
            throw new XltException("Downloading test results failed.");
        }
    }

    private void report()
    {
        if (!generateReport(ReportCreationType.ALL))
        {
            throw new XltException("Creating test report failed.");
        }
    }
}
