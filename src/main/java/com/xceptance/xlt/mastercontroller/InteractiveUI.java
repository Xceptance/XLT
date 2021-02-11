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
package com.xceptance.xlt.mastercontroller;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.lang.ThreadUtils;
import com.xceptance.common.util.ConsoleUiUtils;
import com.xceptance.common.util.ProcessExitCodes;
import com.xceptance.common.util.ProductInformation;
import com.xceptance.xlt.util.AgentControllerInfo;

/**
 * The {@link InteractiveUI} is a user interface for the master controller which allows the user to execute any test run
 * step manually. The available options are:
 * <ul>
 * <li>uploading the agent files to all agent controllers</li>
 * <li>starting the agent at all agent controllers</li>
 * <li>aborting the agent at all agent controllers</li>
 * <li>downloading the test results from all agent controllers</li>
 * <li>printing the current agent status</li>
 * </ul>
 * The user is responsible to combine the available steps to a meaningful sequence.
 * <p>
 * Please note that with this kind of user interface it is possible to close the master controller after starting the
 * test run and to re-gain control when the master controller is started again.
 */
public class InteractiveUI extends BasicConsoleUI
{
    private static final List<String> OPERATION_NAMES = Arrays.asList(new String[]
        {
            "Upload test suite", "Start test", "Report test status", "Download test results", "Create test report", "Abort test",
            "Ping agent controllers", "Show agent controller information", "Quit"
        });

    private static final List<String> OPERATION_KEYS = Arrays.asList(new String[]
        {
            "u", "s", "r", "d", "c", "a", "p", "i", "q"
        });

    private static final String STOP_STATUS_PRINTING_MSG;

    private static final DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS z");
    static
    {
        iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    static
    {
        final StringBuilder sb = new StringBuilder(130);
        sb.append(" +").append(StringUtils.repeat("-", 40)).append("+\n");
        sb.append(" |").append(StringUtils.center("Press ENTER to return to menu", 40)).append("|\n");
        sb.append(" +").append(StringUtils.repeat("-", 40)).append("+\n");

        STOP_STATUS_PRINTING_MSG = sb.toString();
    }

    /**
     * Whether a test report will be automatically created right after downloading the test results.
     */
    private final boolean generateReport;

    /**
     * Creates a new {@link InteractiveUI} object.
     * 
     * @param masterController
     *            the master controller to use
     * @param generateReport
     *            whether a test report will be automatically created right after downloading the test results
     */
    public InteractiveUI(final MasterController masterController, final boolean generateReport)
    {
        super(masterController);

        this.generateReport = generateReport;
    }

    /**
     * Runs the user interface.
     */
    @Override
    public void run()
    {
        while (true)
        {
            final String option = ConsoleUiUtils.selectItem("\n" + "-----------------------------------------------\n" +
                                                            " What do you want to do?\n" +
                                                            "-----------------------------------------------", OPERATION_KEYS,
                                                            OPERATION_NAMES, OPERATION_KEYS);

            System.out.println();

            // option dispatcher
            if (option.equals("u"))
            {
                if (checkAlive() && !isLoadTestRunning())
                {
                    uploadAgentFiles();
                }
            }
            else if (option.equals("s"))
            {
                if (checkAlive() && !isLoadTestRunning())
                {
                    startAgents(null);
                    printLoadTestSettings();
                }
            }
            else if (option.equals("a"))
            {
                if (ConsoleUiUtils.confirm("Do you really want to abort the running test?"))
                {
                    System.out.println();
                    stopAgents();
                }
            }
            else if (option.equals("d"))
            {
                final boolean ok = downloadTestResults(null);

                if (ok && generateReport)
                {
                    generateReport();
                }
            }
            else if (option.equals("r"))
            {
                System.out.println(STOP_STATUS_PRINTING_MSG);
                printLoadTestSettings();
                System.out.println();
                System.out.println();
                printInfoUntilCanceled();
            }
            else if (option.equals("c"))
            {
                generateReport();
            }
            else if (option.equals("p"))
            {
                pingAgentControllers();
            }
            else if (option.equals("i"))
            {
                showAgentControllerInfo();
                printLoadTestSettings();
            }
            else if (option.equals("q"))
            {
                masterController.shutdown();
                System.exit(ProcessExitCodes.SUCCESS);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleTestComment()
    {
        final String testCommentProp = masterController.getTestCommentPropertyValue();
        final String prompt;
        if (testCommentProp != null && testCommentProp.trim().length() > 0)
        {
            System.out.println("Preconfigured test comment: \"" + testCommentProp + "\"");
            System.out.println();
            prompt = "Add another comment about the test";
        }
        else
        {
            prompt = "Add a comment about the test";
        }

        final String comment = ConsoleUiUtils.readLine(prompt);
        if (comment.trim().length() > 0)
        {
            masterController.setTestComment(comment);
        }
        else
        {
            // clear test comment
            masterController.setTestComment(null);
        }

        super.handleTestComment();
    }

    /**
     * Prints the agent status list until the user has canceled it.
     */
    private void printInfoUntilCanceled()
    {
        masterController.startAgentStatusList();

        printAgentStatusList();

        do
        {
            if (sleepUntilCanceled())
            {
                break;
            }
            printAgentStatusList();
        }
        while (masterController.isAnyAgentRunning_SAFE());

        masterController.stopAgentStatusList();
    }

    /**
     * Sleeps the configured update interval time and checks every 100ms if the user has canceled the agent status
     * report.
     * 
     * @return <code>true</code> if the user has canceled the agent status report, <code>false</code> otherwise
     */
    private boolean sleepUntilCanceled()
    {
        final long sleepTime = 100L;
        long remainingTime = Math.max(0L, getStatusListUpdateInterval() * 1000L);

        while (remainingTime > 0)
        {
            ThreadUtils.sleep(sleepTime);
            if (ConsoleUiUtils.wasEnterKeyPressed())
            {
                return true;
            }
            remainingTime -= sleepTime;
        }

        return false;
    }

    /**
     * Triggers the master controller to ping its agent controllers and retrieve some agent controller information.
     */
    public void pingAgentControllers()
    {
        System.out.println("Pinging agent controllers...\n");

        final Map<String, PingResult> pingResults = masterController.pingAgentControllers();

        if (pingResults.isEmpty())
        {
            System.out.println(" -> No agent controller has been pinged.");
        }

        // print results
        final StringBuilder sb = new StringBuilder();
        boolean isTimeout = false;
        for (final Entry<String, PingResult> entry : pingResults.entrySet())
        {
            sb.append(" -> ").append(entry.getKey()).append(" - ");

            final PingResult pingResult = entry.getValue();
            final Exception ex = pingResult.getException();

            if (ex == null)
            {
                sb.append(pingResult.getPingTime()).append(" ms");
            }
            else
            {
                sb.append(getUserFriendlyExceptionMessage(ex));
                isTimeout = true;
            }

            sb.append("\n");
        }

        System.out.println(sb.toString());

        // check for timeouts
        if (isTimeout)
        {
            System.out.println("WARNING: At least one agent controller could not be pinged.\n");
        }
    }

    /**
     * Triggers the master controller to ping its agent controllers and retrieve some agent controller information.
     */
    public void showAgentControllerInfo()
    {
        System.out.println("Requesting agent controller information...\n");

        final AgentControllersInformation agentControllersInfos = masterController.getAgentControllerInformation();

        if (agentControllersInfos.getAgentControllerInformation().isEmpty())
        {
            System.out.println(" -> No agent controller information available.");
        }
        else
        {
            final StringBuilder sb = new StringBuilder();
            for (final AgentControllerInfo agentControllerInfo : agentControllersInfos.getAgentControllerInformation())
            {
                // Agent controller name
                sb.append(" -> ").append(agentControllerInfo.getName()).append(": ");
                if (agentControllerInfo.getAgentControllerSystemInfo() != null)
                {
                    // XLT Version
                    sb.append(agentControllerInfo.getAgentControllerSystemInfo().getXltVersion());
                    // Java version
                    sb.append(", Java ").append(agentControllerInfo.getAgentControllerSystemInfo().getJavaVersion());
                    // OS
                    sb.append(", ").append(agentControllerInfo.getAgentControllerSystemInfo().getOsInfo());
                    // time
                    final long acTime = agentControllerInfo.getAgentControllerSystemInfo().getTime();
                    sb.append(", ").append(iso8601Format.format(new Date(acTime)));
                    sb.append(" (diff: ").append(NumberFormat.getInstance().format(agentControllerInfo.getTimeDifference())).append("ms)");
                    // status
                    sb.append(", status: ").append(agentControllerInfo.getAgentControllerSystemInfo().getStatus());
                }
                else
                {
                    // exception
                    sb.append(getUserFriendlyExceptionMessage(agentControllerInfo.getException()));
                }
                sb.append("\n");
            }

            System.out.print(sb.toString());

            // check timeouts
            if (agentControllersInfos.hasErrors())
            {
                System.out.println("\nWARNING: At least one agent controller could not be queried.");
            }
            else
            {
                // check XLT versions
                if (agentControllersInfos.hasXltVersionConflict())
                {
                    System.out.printf("\nWARNING: Master controller and agent controllers run different XLT versions.\n" +
                                      "         Master controller version: %s\n",
                                      ProductInformation.getProductInformation().getCondensedProductIdentifier());
                }

                // check Java versions
                if (agentControllersInfos.hasJavaConflict())
                {
                    System.out.println("\nWARNING: Your agent controllers probably run different Java versions.");
                }

                // check time diff
                if (agentControllersInfos.hasHighTimeDifference())
                {
                    System.out.printf("\nWARNING: At least one agent controller has a time difference to the master controller of %ss or higher.\n",
                                      agentControllersInfos.getTimeDiffThreshold());
                }
            }
        }

        System.out.println();
    }
}
