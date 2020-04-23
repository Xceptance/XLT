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
package com.xceptance.xlt.mastercontroller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.xceptance.common.util.ConsoleUiUtils;
import com.xceptance.common.util.ProductInformation;
import com.xceptance.xlt.agentcontroller.AgentController;
import com.xceptance.xlt.agentcontroller.AgentStatus;
import com.xceptance.xlt.agentcontroller.TestResultAmount;
import com.xceptance.xlt.agentcontroller.TestUserStatus;
import com.xceptance.xlt.util.AgentControllerException;
import com.xceptance.xlt.util.AgentControllerInfo;
import com.xceptance.xlt.util.FailedAgentControllerCollection;

/**
 * The BasicConsoleUI is the base class for all master controller user interfaces.
 */
public abstract class BasicConsoleUI implements MasterControllerUI
{
    private static final String COPYRIGHT = "Copyright (c) 2005-%s %s. All rights reserved.";

    private static final List<String> TEST_RESULT_AMOUNT_DISPLAY_NAMES = Arrays.asList(TestResultAmount.displayNames());

    private static final List<TestResultAmount> TEST_RESULT_AMOUNTS = Arrays.asList(TestResultAmount.values());

    private static final List<String> TEST_RESULT_SHORTCUTS = Arrays.asList(TestResultAmount.shortcuts());

    private static final List<String> REPORT_CREATION_TYPE_SHORTCUTS = Arrays.asList(ReportCreationType.shortcuts());

    private static final List<String> REPORT_CREATION_TYPE_DISPLAY_NAMES = Arrays.asList(ReportCreationType.displayNames());

    private static final List<ReportCreationType> REPORT_CREATION_TYPES = Arrays.asList(ReportCreationType.values());

    private static final String TIME_TOTALS = "/";

    private static final String SETTING_NOT_AVAILABLE = "n/a";

    private static final String OVERFLOW_ERROR = "#OUT OF RANGE#";

    /**
     * The master controller this user interface interacts with.
     */
    protected final MasterController masterController;

    /**
     * Whether the test status is shown per user or per user type.
     */
    private boolean showDetailedStatusList;

    /**
     * Interval in seconds to update the agent status list.
     */
    private int statusListUpdateInterval;

    private long summaryStartDate = Long.MAX_VALUE;

    /**
     * Creates a new BasicConsoleUI object.
     * 
     * @param masterController
     *            the master controller to use
     */
    public BasicConsoleUI(final MasterController masterController)
    {
        this.masterController = masterController;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void agentFilesUploaded()
    {
        print();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void agentsStarted()
    {
        print();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void agentsStopped()
    {
        print();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void agentStatusReceived(final FailedAgentControllerCollection results)
    {
        if (!results.isEmpty())
        {
            for (final Map.Entry<AgentController, Exception> result : results.getMap().entrySet())
            {
                final Exception ex = result.getValue();
                if (ex != null)
                {
                    final String msg = getUserFriendlyExceptionMessage(ex);
                    System.out.println("Failed to get agent status from " + result.getKey() + " -> " + msg);
                }
            }
            System.out.println();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void downloadingTestResults()
    {
        System.out.print(" -> Downloading test results");
    }

    /**
     * Triggers the master controller to download the test results from all known agent controllers.
     * 
     * @param testResultAmount
     *            the amount of test result data to download
     * @return <code>true</code> if the operation was successful for all agent controllers; <code>false</code> otherwise
     */
    public boolean downloadTestResults(TestResultAmount testResultAmount)
    {
        if (testResultAmount == null)
        {
            testResultAmount = ConsoleUiUtils.selectItem("Select the data to be downloaded:", TEST_RESULT_SHORTCUTS,
                                                         TEST_RESULT_AMOUNT_DISPLAY_NAMES, TEST_RESULT_AMOUNTS);
            System.out.println();
        }

        boolean ok = !TestResultAmount.CANCEL.equals(testResultAmount);

        if (ok)
        {
            System.out.println("Downloading test results... Please be patient, it might take some time...");
            ok = masterController.downloadTestResults(testResultAmount);
            System.out.println();

            if (ok)
            {
                handleTestComment();
                System.out.println("\nResults have been downloaded to: " + masterController.getCurrentTestResultsDirectory());
            }
        }

        return ok;
    }

    /**
     * Triggers the master controller to generate the test report from the downloaded results and queries the user for
     * time range option.
     * 
     * @return <code>true</code> if the operation was successful; <code>false</code> otherwise
     */
    public boolean generateReport()
    {
        return generateReport(null);
    }

    /**
     * Triggers the master controller to generate the test report from the downloaded results.
     * 
     * @param reportCreationType
     *            time range option to generate the report from or <code>null</code> to query the user
     * @return <code>true</code> if the operation was successful; <code>false</code> otherwise
     */
    public boolean generateReport(ReportCreationType reportCreationType)
    {
        boolean result = false;

        if (reportCreationType == null)
        {
            reportCreationType = ConsoleUiUtils.selectItem("Would you like to include the ramp-up period in the test report?",
                                                           REPORT_CREATION_TYPE_SHORTCUTS, REPORT_CREATION_TYPE_DISPLAY_NAMES,
                                                           REPORT_CREATION_TYPES);
            System.out.println();
        }

        if (!reportCreationType.equals(ReportCreationType.ABORT))
        {
            System.out.println("Generating load test report based on latest download...");
            result = masterController.generateReport(reportCreationType);
            if (!result)
            {
                System.out.println(" -> Failed");
            }
            System.out.println();
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatusListUpdateInterval()
    {
        return statusListUpdateInterval;
    }

    /**
     * Checks whether there is a running load test.
     * 
     * @return <code>true</code> if there is a running load test; <code>false</code> otherwise
     */
    public boolean isLoadTestRunning()
    {
        // check whether there are still running agents
        try
        {
            if (masterController.isAnyAgentRunning())
            {
                System.out.println("-> WARNING: Unable to execute command because a test is currently running.");
                return true;
            }
        }
        catch (final AgentControllerException e)
        {
            // potentially there are running agents even if we can not communicate with the agent controller.
            System.out.println("-> WARNING: Unable to execute command because at least one agent controller cannot be reached.");
            return true;
        }

        return false;
    }

    /**
     * Returns whether to display detailed status information for each simulated test user, or whether status
     * information will be aggregated into one line per user type.
     * 
     * @return whether to show detailed information
     */
    public boolean isShowDetailedStatusList()
    {
        return showDetailedStatusList;
    }

    /**
     * Prints the status of all agents to the console in a tabular format.
     */
    public void printAgentStatusList()
    {
        if (showDetailedStatusList)
        {
            printDetailedAgentStatusList();
        }
        else
        {
            printCondensedAgentStatusList();
        }
    }

    /**
     * Prints the status of all agents to the console in a tabular format. The output contains one line per simulated
     * user type.
     */
    public void printCondensedAgentStatusList()
    {
        final Map<String, TestUserTypeStatus> userTypeStatusList = new TreeMap<String, TestUserTypeStatus>();

        int maxNameLength = 9;

        final Set<AgentStatus> agentStatusList = masterController.getAgentStatusList();
        /** host name : agent status list */
        final Map<String, Set<AgentStatus>> badHostAgents = new HashMap<String, Set<AgentStatus>>();

        long startDate = Long.MAX_VALUE;

        for (final AgentStatus agentStatus : agentStatusList)
        {
            for (final TestUserStatus userStatus : agentStatus.getTestUserStatusList())
            {
                // get the base name from the user name
                final String testCaseName = StringUtils.substringBeforeLast(userStatus.getUserName(), "-");

                // maintain one global status for all users of the same type
                TestUserTypeStatus userTypeStatus = userTypeStatusList.get(testCaseName);
                if (userTypeStatus == null)
                {
                    userTypeStatus = new TestUserTypeStatus();
                    userTypeStatus.setUserName(testCaseName);
                    userTypeStatus.setStartDate(Long.MAX_VALUE);
                    userTypeStatus.setElapsedTime(Long.MIN_VALUE);

                    userTypeStatusList.put(testCaseName, userTypeStatus);
                }

                // maintain count of total and running users
                userTypeStatus.setTotalUsers(userTypeStatus.getTotalUsers() + 1);

                if (userStatus.getState() == TestUserStatus.State.Running)
                {
                    userTypeStatus.setRunningUsers(userTypeStatus.getRunningUsers() + 1);
                }

                // determine the over-all running state
                TestUserStatus.State globalState = userTypeStatus.getState();
                final TestUserStatus.State state = userStatus.getState();

                if (globalState == TestUserStatus.State.Running)
                {
                    // globalState = TestUserStatus.State.Running;
                }
                else if (state != TestUserStatus.State.Waiting)
                {
                    globalState = state;
                }

                userTypeStatus.setState(globalState);

                // take the last run time from the most recently updated status
                if (userTypeStatus.getLastModifiedDate() < userStatus.getLastModifiedDate())
                {
                    userTypeStatus.setLastModifiedDate(userStatus.getLastModifiedDate());
                    userTypeStatus.setLastRuntime(userStatus.getLastRuntime());
                }

                // calculate the overall percentage
                if (userStatus.getMode() == TestUserStatus.Mode.TIME_PERIOD)
                {
                    // for duration-based tests take the maximum
                    userTypeStatus.setPercentageComplete(Math.max(userTypeStatus.getPercentageComplete(),
                                                                  userStatus.getPercentageComplete()));
                }
                else
                {
                    // for iteration-based tests take the mean
                    final int totalUsers = userTypeStatus.getTotalUsers();
                    if (totalUsers == 1)
                    {
                        // this is the initial value
                        userTypeStatus.setPercentageComplete(userStatus.getPercentageComplete());
                    }
                    else
                    {
                        // incrementally update the mean value
                        double mean = userTypeStatus.getPercentageComplete();

                        mean = mean + (userStatus.getPercentageComplete() - mean) / totalUsers;

                        userTypeStatus.setPercentageComplete((int) mean);
                    }
                }

                // update the remaining values
                userTypeStatus.setIterations(userTypeStatus.getIterations() + userStatus.getIterations());
                userTypeStatus.setTotalRuntime(userTypeStatus.getTotalRuntime() + userStatus.getTotalRuntime());
                userTypeStatus.setEvents(userTypeStatus.getEvents() + userStatus.getEvents());
                userTypeStatus.setErrors(userTypeStatus.getErrors() + userStatus.getErrors());
                userTypeStatus.setException(userStatus.getException());
                userTypeStatus.setStartDate(Math.min(userTypeStatus.getStartDate(), userStatus.getStartDate()));
                userTypeStatus.setElapsedTime(Math.max(userTypeStatus.getElapsedTime(), userStatus.getElapsedTime()));

                // get the max length of the name (for formatting purposes)
                maxNameLength = Math.max(maxNameLength, testCaseName.length());

                startDate = Math.min(startDate, userStatus.getStartDate());
            }

            if (agentStatus.getErrorExitCode() != 0)
            {
                Set<AgentStatus> badAgents = badHostAgents.get(agentStatus.getHostName());
                if (badAgents == null)
                {
                    badAgents = new HashSet<AgentStatus>();
                    badHostAgents.put(agentStatus.getHostName(), badAgents);
                }
                badAgents.add(agentStatus);
            }
        }

        if (!userTypeStatusList.isEmpty())
        {
            if (summaryStartDate == Long.MAX_VALUE)
            {
                summaryStartDate = startDate;
            }

            final List<TestUserTypeStatus> userTypeStates = new ArrayList<TestUserTypeStatus>(userTypeStatusList.values());

            // format and append the header line
            final StringBuilder buf = new StringBuilder();
            final Formatter formatter = new Formatter(buf);

            final String headerFormat = "%-" + maxNameLength +
                                        "s   State         Running Users   Iterations   Last Time   Avg. Time   Elapsed Time      Events          Errors" +
                                        "   Progress\n";
            formatter.format(headerFormat, "Test Case");

            final String separatorLineFormat = "%s   --------   ----------------   ----------   ---------   ---------   ------------   ---------   -------------" +
                                               "   --------\n";
            formatter.format(separatorLineFormat, StringUtils.repeat("-", maxNameLength));

            // format and append the user type lines
            final String format = "%-" + maxNameLength + "s   %-8s   %,6d of %,6d   %,10d   %9s   %9s     %10s   %,9d   %,6d %6s" +
                                  "   %7d%%\n";
            final String timeFormat = "%,7.2f s";

            final String failedFormat = "%-" + maxNameLength + "s   %-8s   %s\n";

            int errors = 0, events = 0, iterations = 0, runningUsers = 0, totalUsers = 0, percentage = 0;
            TestUserStatus.State state = TestUserStatus.State.Waiting;
            long elapsed = Long.MIN_VALUE;
            for (final TestUserTypeStatus userTypeStatus : userTypeStates)
            {

                if (state != TestUserStatus.State.Running && userTypeStatus.getState() != TestUserStatus.State.Waiting)
                {
                    state = userTypeStatus.getState();
                }

                errors += userTypeStatus.getErrors();
                events += userTypeStatus.getEvents();
                iterations += userTypeStatus.getIterations();
                runningUsers += userTypeStatus.getRunningUsers();
                totalUsers += userTypeStatus.getTotalUsers();
                percentage += userTypeStatus.getPercentageComplete();
                elapsed = Math.max(elapsed, userTypeStatus.getElapsedTime());

                if (userTypeStatus.getState() == TestUserStatus.State.Failed)
                {
                    formatter.format(failedFormat, userTypeStatus.getUserName(), userTypeStatus.getState(), userTypeStatus.getException());
                }
                else
                {
                    final String elapsedTime = formatTime(userTypeStatus.getElapsedTime());
                    int errorRate = 0;
                    if (userTypeStatus.getIterations() > 0)
                    {
                        errorRate = Math.round(userTypeStatus.getErrors() * 100.0f / userTypeStatus.getIterations());
                    }

                    final double lastRuntime = userTypeStatus.getLastRuntime() / 1000.0;
                    final double avgRuntime = userTypeStatus.getAverageRuntime() / 1000.0;

                    formatter.format(format, userTypeStatus.getUserName(), userTypeStatus.getState(), userTypeStatus.getRunningUsers(),
                                     userTypeStatus.getTotalUsers(), userTypeStatus.getIterations(), String.format(timeFormat, lastRuntime),
                                     String.format(timeFormat, avgRuntime), elapsedTime, userTypeStatus.getEvents(),
                                     userTypeStatus.getErrors(), "(" + errorRate + "%)", userTypeStatus.getPercentageComplete());
                }
            }

            // print summary line
            if (userTypeStates.size() > 1)
            {
                formatter.format(separatorLineFormat, StringUtils.repeat("-", maxNameLength));

                final String elapsedTime = formatTime(elapsed);
                int errorRate = 0;
                if (iterations > 0)
                {
                    errorRate = Math.round(errors * 100.0f / iterations);
                }

                formatter.format(format, "Totals", state, runningUsers, totalUsers, iterations, TIME_TOTALS, TIME_TOTALS, elapsedTime,
                                 events, errors, "(" + errorRate + "%)", percentage / userTypeStates.size());

            }

            // print the status
            formatter.close();
            System.out.println(buf);
        }

        if (!badHostAgents.isEmpty())
        {
            final StringBuilder sb = new StringBuilder();
            int count = 0;
            sb.append("->  Agent(s) exited unexpectedly:\n");
            for (final Entry<String, Set<AgentStatus>> badAgents : badHostAgents.entrySet())
            {
                sb.append("  - ").append(badAgents.getKey()).append("\n");
                final Set<AgentStatus> badAgentsOfHost = badAgents.getValue();
                for (final AgentStatus badAgentStatus : badAgentsOfHost)
                {
                    sb.append("        Agent '").append(badAgentStatus.getAgentID()).append("' returned with exit code '")
                      .append(badAgentStatus.getErrorExitCode()).append("'\n");
                }

                count += badAgentsOfHost.size();
            }
            sb.insert(3, Integer.toString(count));
            System.out.println(sb.toString());
        }
    }

    /**
     * Prints the status of all agents to the console in a tabular format. The output contains one line per simulated
     * test user.
     */
    public void printDetailedAgentStatusList()
    {
        final Set<AgentStatus> agentStatusList = masterController.getAgentStatusList();

        // determine the maximum length of user names
        int maxNameLength = 4;
        int maxHostNameLength = 15;
        boolean nonEmpty = false;
        for (final AgentStatus agentStatus : agentStatusList)
        {
            final List<TestUserStatus> testUserStatusList = agentStatus.getTestUserStatusList();
            nonEmpty |= (!testUserStatusList.isEmpty());
            for (final TestUserStatus userStatus : testUserStatusList)
            {
                maxNameLength = Math.max(maxNameLength, userStatus.getUserName().length());
            }
            maxHostNameLength = Math.max(maxHostNameLength, agentStatus.getHostName().length());
        }

        if (!nonEmpty)
        {
            return;
        }

        // format and append the header line
        final StringBuilder buf = new StringBuilder();
        final Formatter formatter = new Formatter(buf);

        final String headerFormat = "%-" + maxHostNameLength + "s   %-" + maxNameLength +
                                    "s   State      Iterations   Last Time   Avg. Time   Elapsed Time      Events          Errors   Progress\n";
        formatter.format(headerFormat, "Agent Host", "User");

        final String separatorLineFormat = "%s   %s   --------   ----------   ---------   ---------   ------------   ---------   -------------   --------\n";
        formatter.format(separatorLineFormat, StringUtils.repeat("-", maxHostNameLength), StringUtils.repeat("-", maxNameLength));

        // format and append the user lines
        final String format = "%-" + maxHostNameLength + "s   %-" + maxNameLength +
                              "s   %-8s   %,10d   %,7.2f s   %,7.2f s     %10s   %,9d   %,6d %6s   %7d%%\n";
        final String failedFormat = "%-" + maxHostNameLength + "s   %-" + maxNameLength + "s   %-8s   *** %s ***\n";

        for (final AgentStatus agentStatus : agentStatusList)
        {
            for (final TestUserStatus userStatus : agentStatus.getTestUserStatusList())
            {
                if (userStatus.getState() == TestUserStatus.State.Failed)
                {
                    formatter.format(failedFormat, agentStatus.getHostName(), userStatus.getUserName(), userStatus.getState(),
                                     userStatus.getException());
                }
                else
                {
                    final String elapsedTime = formatTime(userStatus.getElapsedTime());
                    int errorRate = 0;
                    if (userStatus.getIterations() > 0)
                    {
                        errorRate = Math.round(userStatus.getErrors() * 100.0f / userStatus.getIterations());
                    }

                    formatter.format(format, agentStatus.getHostName(), userStatus.getUserName(), userStatus.getState(),
                                     userStatus.getIterations(), userStatus.getLastRuntime() / 1000.0,
                                     userStatus.getAverageRuntime() / 1000.0, elapsedTime, userStatus.getEvents(), userStatus.getErrors(),
                                     "(" + errorRate + "%)", userStatus.getPercentageComplete());
                }
            }
        }

        // print the status
        formatter.close();
        System.out.println(buf);
    }

    public void printLoadTestSettings()
    {
        // Get the current load profile.
        final String output = getLoadTestSettings(masterController.getCurrentLoadProfile());
        if (!StringUtils.isBlank(output))
        {
            System.out.println();
            System.out.println("Load Profile");
            System.out.println(output);
        }
    }

    /**
     * This method returns a summary of the current load test settings.
     * 
     * @param loadTestSettings
     *            The load test settings that should be analyzed.
     * @return A String containing the settings for all configured load profiles. In case no load profile is configured
     *         an empty String ("") will be returned.
     */
    public String getLoadTestSettings(final TestLoadProfileConfiguration loadTestSettings)
    {
        // If the mastercontroller has not parsed the configuration yet, we quit quietly.
        final StringBuilder configLine = new StringBuilder();
        if (loadTestSettings != null)
        {
            final Formatter formatter = new Formatter(configLine);

            // Prepare the table header
            final String testCaseColumnHeading = "Test Case";
            final String arrivalRateColumnHeading = "Arrival Rate [eff]";
            final String usersColumnHeading = "Users [eff]";
            final String loadFactorColumnHeading = "Load Factor";
            final String measurementPeriodColumnHeading = "Measurement Period";
            final String colSep = " | ";
            final Map<String, Integer> maxLenghtMap = getColumnBoundaries(loadTestSettings);

            // Check what's longer: column heading or cell-content.
            final int maxNameLength = Math.max(maxLenghtMap.get("testCases"), testCaseColumnHeading.length());
            final int arrivalLength = Math.max(maxLenghtMap.get("arrivalRate"), arrivalRateColumnHeading.length());
            final int maxUserLength = Math.max(maxLenghtMap.get("users"), usersColumnHeading.length());
            final int maxLoadFactor = Math.max(maxLenghtMap.get("loadFactor"), loadFactorColumnHeading.length());
            final int measureLength = measurementPeriodColumnHeading.length();

            final int totalLength = maxNameLength + arrivalLength + maxUserLength + maxLoadFactor + measureLength + 4 * colSep.length();

            final String lineFormat = StringUtils.joinWith(colSep, "%s", "%s", "%s", "%s", "%s\n");
            final String dashLine = StringUtils.repeat("-", totalLength);
            formatter.format("%s\n", dashLine);

            formatter.format(lineFormat, StringUtils.center(testCaseColumnHeading, maxNameLength),
                             StringUtils.center(arrivalRateColumnHeading, arrivalLength),
                             StringUtils.center(usersColumnHeading, maxUserLength),
                             StringUtils.center(loadFactorColumnHeading, maxLoadFactor),
                             StringUtils.center(measurementPeriodColumnHeading, measureLength));

            formatter.format("%s\n", dashLine);

            int arrivalRateSum = 0, numberOfUsersSum = 0, measurementPeriodMaximum = 0;
            String previousLoadFactor = "";
            boolean sameLoadFactor = true, arrivalRateOverflow = false, userOverflow = false;

            for (TestCaseLoadProfileConfiguration settings : loadTestSettings.getLoadTestConfiguration())
            {

                // Arrival Rate
                Pair<Integer, Integer> boundaries = getMinMaxValue(settings.getArrivalRate());
                if (!arrivalRateOverflow && boundaries != null)
                {
                    try
                    {
                        arrivalRateSum = Math.addExact(arrivalRateSum, boundaries.getRight());
                    }
                    catch (ArithmeticException e)
                    {
                        // We exceeded the maximum integer boundaries
                        arrivalRateOverflow = true;
                    }
                }

                final String arrivalRate = getIntRangeAsString(boundaries);

                // Number of Users
                boundaries = getMinMaxValue(settings.getNumberOfUsers());
                if (!userOverflow && boundaries != null)
                {
                    try
                    {
                        numberOfUsersSum = Math.addExact(numberOfUsersSum, boundaries.getRight());
                    }
                    catch (ArithmeticException e)
                    {
                        userOverflow = true;
                    }
                }
                final String users = getIntRangeAsString(boundaries);

                // Load Factor
                boundaries = getMinMaxValue(settings.getLoadFactor());
                final String loadFactor;
                if (boundaries != null)
                {
                    final double minLoadFactorFraction = boundaries.getLeft() / 1000.0;
                    final double maxLoadFactorFraction = boundaries.getRight() / 1000.0;

                    loadFactor = getDoubleRangeAsString(new MutablePair<Double, Double>(minLoadFactorFraction, maxLoadFactorFraction));
                }
                else
                {
                    loadFactor = getDoubleRangeAsString(null);
                }

                if (sameLoadFactor)
                {
                    // At the beginning we need to initialize previousLoadFactor
                    if (StringUtils.isEmpty(previousLoadFactor))
                    {
                        previousLoadFactor = loadFactor;
                    }
                    else
                    {
                        sameLoadFactor = previousLoadFactor.equals(loadFactor);
                    }
                }

                int measurementPeriod = settings.getMeasurementPeriod();
                measurementPeriodMaximum = Math.max(measurementPeriod, measurementPeriodMaximum);

                formatter.format(lineFormat, StringUtils.rightPad(settings.getUserName(), maxNameLength),
                                 StringUtils.leftPad(arrivalRate, arrivalLength), StringUtils.leftPad(users, maxUserLength),
                                 StringUtils.leftPad(loadFactor, maxLoadFactor),
                                 StringUtils.leftPad(convertSecondsToTime(measurementPeriod), measureLength));
            }

            // print summary line
            formatter.format("%s\n", StringUtils.repeat("-", totalLength));
            final int firstColWidth = arrivalLength + maxNameLength + colSep.length();

            if (!arrivalRateOverflow)
            {
                formatter.format("%," + firstColWidth + "d" + colSep, arrivalRateSum);
            }
            else
            {
                formatter.format("%" + Math.max(firstColWidth, OVERFLOW_ERROR.length()) + "s" + colSep, OVERFLOW_ERROR);
            }
            if (!userOverflow)
            {
                formatter.format("%," + (maxUserLength) + "d" + colSep, numberOfUsersSum);
            }
            else
            {
                formatter.format("%" + Math.max((maxUserLength), OVERFLOW_ERROR.length()) + "s" + colSep, OVERFLOW_ERROR);
            }
            formatter.format("%" + (maxLoadFactor) + "s" + colSep, (sameLoadFactor) ? previousLoadFactor : " ");
            formatter.format("%" + (measureLength) + "s\n", convertSecondsToTime(measurementPeriodMaximum));

            formatter.close();
        }

        return configLine.toString();
    }

    /**
     * Converts seconds to a more human readable time format. If the amount of seconds is negative, it will be first
     * negated.
     * 
     * @param seconds
     *            The number of seconds which should be converted.
     * @return The returned time format follows the pattern <i>h:mm:ss</i>.
     */
    public String convertSecondsToTime(long seconds)
    {
        // Alternative format: "%d h %02d min %02d sec"
        seconds = Math.abs(seconds);
        if (seconds > 0)
        {
            final long rem = seconds % 3600;
            return String.format("%d:%02d:%02d", seconds / 3600, rem / 60, rem % 60);
        }
        return "0:00:00";
    }

    /**
     * This method searches the lowest and highest value in a 2D-array and returns them as a pair.
     * 
     * @param pairs
     *            2D-array which follows the pattern <code>{[timestamp_1, value_1], [timestamp_2, value_2] ...}</code>.
     * @return Returns a pair, containing the lowest value in <code>Left</code> and the highest value in
     *         <code>Right</code>. <br>
     *         If <i>pairs</i> is <code>null</code>, <code>null</code> will be returned as well.
     */
    public Pair<Integer, Integer> getMinMaxValue(int[][] pairs)
    {
        // Handle null objects. For example, arrivalRate and loadFactor might be null.
        // Also make sure that the array contains at least one value (pairs[0][1]).
        if (pairs == null || pairs.length == 0 || pairs[0].length == 1)
        {
            return null;
        }

        int min = pairs[0][1];
        int max = pairs[0][1];

        for (int i = 1; i < pairs.length; i++)
        {
            final int val = pairs[i][1];
            min = Math.min(min, val);
            max = Math.max(max, val);
        }

        return new ImmutablePair<Integer, Integer>(min, max);
    }

    /**
     * Calculates and stores the maximum String size for test case names, arrival rate, number of users and load factor.
     * Numerical values will be first converted to a String representation before the maximum String length is
     * determined. Numerical ranges will be also converted into String representations following the pattern
     * <i>minValue..maxValue</i>.<br>
     * The maximum lengths will be stored in a map and can be accessed by referencing to the keys <i>testCases</i>,
     * <i>arrivalRate</i>, <i>users</i> and <i>loadFactor</i>.
     * 
     * @param loadTestSettings
     *            The configured load test settings that should be analyzed.
     * @return A map which contains the String size of the longest test case name, arrival rate, number of users and
     *         load factor.
     */
    private Map<String, Integer> getColumnBoundaries(final TestLoadProfileConfiguration loadTestSettings)
    {
        int maxTestCaseLength = 0;
        int maxArrivalRateLength = 0;
        int maxUsersLength = 0;
        int maxLoadFactor = 0;
        final Map<String, Integer> map = new HashMap<>();

        for (final TestCaseLoadProfileConfiguration profile : loadTestSettings.getLoadTestConfiguration())
        {
            maxTestCaseLength = Math.max(maxTestCaseLength, profile.getUserName().length());
            maxArrivalRateLength = Math.max(maxArrivalRateLength, getIntRangeAsString(getMinMaxValue(profile.getArrivalRate())).length());
            maxUsersLength = Math.max(maxUsersLength, getIntRangeAsString(getMinMaxValue(profile.getNumberOfUsers())).length());

            final Pair<Integer, Integer> loadFactorPair = getMinMaxValue(profile.getLoadFactor());
            if (loadFactorPair != null)
            {
                double min = loadFactorPair.getLeft() / 1000.0;
                double max = loadFactorPair.getRight() / 1000.0;
                maxLoadFactor = Math.max(maxLoadFactor, getDoubleRangeAsString(new ImmutablePair<Double, Double>(min, max)).length());
            }
            else
            {
                maxLoadFactor = Math.max(maxLoadFactor, getDoubleRangeAsString(null).length());
            }
        }

        map.put("testCases", maxTestCaseLength);
        map.put("arrivalRate", maxArrivalRateLength);
        map.put("users", maxUsersLength);
        map.put("loadFactor", maxLoadFactor);

        return map;
    }

    /**
     * Converts the passed pair of Integers to a String.
     * 
     * @param pair
     *            The pair that should be converted to a String.
     * @return Depending on the content, one of following Strings will be returned: <br>
     *         pair(<code>null</code>) --> {@value #SETTING_NOT_AVAILABLE} <br>
     *         pair(n, n) ---> "n" <br>
     *         pair(m, n) ---> "m..n"
     */
    private String getIntRangeAsString(final Pair<Integer, Integer> pair)
    {
        if (pair == null)
        {
            return SETTING_NOT_AVAILABLE;
        }

        final int min = pair.getLeft();
        final int max = pair.getRight();

        return (min == max) ? String.format("%,d", max) : String.format("%,d..%,d", min, max);
    }

    /**
     * Converts the passed pair of Floats to a String. Floats will be displayed with a precision of 3.
     * 
     * @param pair
     *            The pair that should be converted to a String.
     * @return Depending on the content, one of following Strings will be returned: <br>
     *         pair(<code>null</code>) --> {@value #SETTING_NOT_AVAILABLE} <br>
     *         pair(n, n) ---> "n" <br>
     *         pair(m, n) ---> "m..n"
     */
    private String getDoubleRangeAsString(final Pair<Double, Double> pair)
    {
        if (pair == null)
        {
            return SETTING_NOT_AVAILABLE;
        }

        final double min = pair.getLeft();
        final double max = pair.getRight();

        return (min == max) ? String.format("%,.3f", max) : String.format("%,.3f..%,.3f", min, max);
    }

    /**
     * Prints the status of all agents to the console in a tabular format.
     */
    public void printXltInfo()
    {
        final ProductInformation info = ProductInformation.getProductInformation();

        // get the "to" year for the copyright
        final GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(info.getBuildDate());
        final int year = cal.get(Calendar.YEAR);

        // format the info
        final String productInfo = info.getProductIdentifier();
        final String copyright = String.format(COPYRIGHT, year, info.getVendorName());
        final String licenseInfo = "XLT is Open Source and available under the Apache License 2.0.";

        // put it all together
        System.out.printf("\n%s\n%s\n%s\n\n", productInfo, copyright, licenseInfo);
    }

    /**
     * Prints some basic agent controller precheck information like accessibility or XLT conflict.
     */
    public void printAgentControllerPreCheckInformation()
    {
        System.out.print("\nChecking for agent controller reachability and XLT version conflicts ... ");
        final AgentControllersInformation agentControllerInfos = masterController.getAgentControllerInformation();

        final StringBuilder sb = new StringBuilder();

        // check for errors
        if (agentControllerInfos.hasErrors())
        {
            sb.append("\n\n");
            sb.append("WARNING: At least one agent controller is unreachable.\n\n");

            for (final AgentControllerInfo agentControllerInfo : agentControllerInfos.getAgentControllerInformation())
            {
                final Exception ex = agentControllerInfo.getException();
                if (ex != null)
                {
                    sb.append(" -> ").append(agentControllerInfo.getName()).append(": ")
                      .append(getUserFriendlyExceptionMessage(agentControllerInfo.getException())).append('\n');
                }
            }
        }
        // check for different XLT version
        else if (agentControllerInfos.hasXltVersionConflict())
        {
            sb.append("\n\n");
            sb.append("WARNING: Master controller and agent controllers run different XLT versions.\n");
            sb.append("         Master controller version: ")
              .append(ProductInformation.getProductInformation().getCondensedProductIdentifier()).append('\n');
        }
        else
        {
            sb.append("OK\n");
        }

        System.out.println(sb.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void receivingAgentStatus()
    {
    }

    /**
     * Runs the user interface.
     */
    public abstract void run();

    /**
     * Sets whether to display detailed status information for each simulated test user, or whether status information
     * will be aggregated into one line per user type.
     * 
     * @param showDetailedStatusList
     *            whether to show detailed information
     */
    public void setShowDetailedStatusList(final boolean showDetailedStatusList)
    {
        this.showDetailedStatusList = showDetailedStatusList;
    }

    /**
     * Sets the number of seconds to wait before the status list is updated again.
     * 
     * @param statusListUpdateInterval
     *            the update interval
     */
    public void setStatusListUpdateInterval(final int statusListUpdateInterval)
    {
        this.statusListUpdateInterval = statusListUpdateInterval;
    }

    /**
     * Triggers the master controller to start the agent on all known agent controllers.
     * 
     * @param testCaseName
     *            the name of the test case to start the agents for, or <code>null</code> if all active test cases
     *            should be started
     * @return <code>true</code> if the operation was successful for all agent controllers; <code>false</code> otherwise
     */
    public boolean startAgents(final String testCaseName)
    {
        if (masterController.areAgentsInSync())
        {
            System.out.println("Starting agents... ");
            boolean result = false;
            try
            {
                result = masterController.startAgents(testCaseName);
            }
            catch (final AgentControllerException e)
            {
                print(e.getFailed());
            }

            System.out.println();

            if (result)
            {
                summaryStartDate = Long.MAX_VALUE;
            }

            return result;
        }
        else
        {
            System.out.println("The test suite has to be uploaded before a test can be started.\n");
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startingAgents()
    {
        System.out.print("Starting agents... ");
    }

    /**
     * Triggers the master controller to stop the agent on all known agent controllers.
     * 
     * @return <code>true</code> if the operation was successful for all agent controllers; <code>false</code> otherwise
     */
    public boolean stopAgents()
    {
        System.out.println("Aborting agents... ");
        boolean result = false;
        try
        {
            result = masterController.stopAgents();
        }
        catch (final AgentControllerException e)
        {
            print(e.getFailed());
        }
        System.out.println();

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stoppingAgents()
    {
        System.out.print("Stopping agents... ");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testResultsDownloaded(final FailedAgentControllerCollection results)
    {
        print(results);
    }

    /**
     * Triggers the master controller to upload the agent files to all known agent controllers.
     * 
     * @return <code>true</code> if uploading was successful, <code>false</code> otherwise
     */
    public boolean uploadAgentFiles()
    {
        boolean result = false;
        System.out.println("Uploading test suite... ");
        try
        {
            masterController.updateAgentFiles();
            result = true;
        }
        catch (final AgentControllerException e)
        {
            print(e.getFailed());
        }
        catch (final Exception t)
        {
            print(t);
        }
        System.out.println();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uploadingAgentFiles()
    {
        System.out.print(" -> Uploading agent files... ");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void skipAgentControllerConnections(final FailedAgentControllerCollection unconnectedAgentControllers)
    {
        if (!unconnectedAgentControllers.isEmpty())
        {
            System.out.println("\n-> WARNING: Skipped unreachable agent controllers:");
            for (final AgentController agentcontroller : unconnectedAgentControllers.getAgentControllers())
            {
                System.out.println("   SKIPPED " + agentcontroller);
            }
        }
    }

    /**
     * Handles the test comment.
     */
    protected void handleTestComment()
    {
        masterController.setTestComment4DownloadedResults();
    }

    /**
     * Returns the given time (difference) value in the format "hours:mins:secs". Negative values are prefixed with a
     * negative sign. The number of hours is not limited to 24.
     * 
     * @param time
     *            the time to convert
     * @return the formatted time
     */
    private String formatTime(long time)
    {
        String sign = "";

        if (time < 0)
        {
            sign = "-";
            time = -time;
        }

        final long timeInSecs = time / 1000;
        final long timeInMins = timeInSecs / 60;
        final long timeInHours = timeInMins / 60;

        final long secs = timeInSecs % 60;
        final long mins = timeInMins % 60;
        final long hours = timeInHours;

        return String.format("%s%d:%02d:%02d", sign, hours, mins, secs);
    }

    /**
     * Builds the list of failures ready for printing.
     * 
     * @param results
     *            the list of errors
     * @return the formatted failures
     */
    private String buildFailureList(final FailedAgentControllerCollection results)
    {
        final StringBuilder failed = new StringBuilder();
        for (final Map.Entry<AgentController, Exception> result : results.getMap().entrySet())
        {
            failed.append(" -> " + result.getKey());

            final Exception ex = result.getValue();
            if (ex != null)
            {
                final String msg = getUserFriendlyExceptionMessage(ex);
                failed.append(" - " + msg);
            }

            failed.append("\n");
        }

        return failed.toString();
    }

    /**
     * Returns a more user-friendly message for the given exception.
     * 
     * @param ex
     *            the exception
     * @return the user-friendly message
     */
    protected String getUserFriendlyExceptionMessage(final Exception ex)
    {
        String msg = StringUtils.defaultString(ex.getMessage());

        if (msg.startsWith("401:"))
        {
            msg = "Authentication failed. The agent controller rejected the master controller's password.";
        }

        return msg;
    }

    /**
     * Prints the result of an operation at the given agent controller. If the operation was successful, the passed
     * exception is null, otherwise it may hold more information.
     * 
     * @param ex
     *            the exception
     */
    private void print(final FailedAgentControllerCollection results)
    {
        final StringBuilder failed = new StringBuilder();
        if (results != null && results.getMap().size() > 0)
        {
            failed.append("- FAILED!\n\n");
            failed.append(buildFailureList(results));
            System.out.println(failed);
        }
        else
        {
            System.out.println("- OK");
        }
    }

    private void print(final Exception e)
    {
        System.out.println("- FAILED: " + e.getMessage());
    }

    private void print()
    {
        print((FailedAgentControllerCollection) null);
    }

    protected boolean checkAlive()
    {
        // check if agent controllers are alive.
        // if we can not connect to an agent controller it has a running test potentially
        try
        {
            masterController.checkAlive();
            return true;
        }
        catch (final AgentControllerException e)
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("WARNING: Unable to execute this command because at least one agent controller is unreachable:\n\n");
            sb.append(buildFailureList(e.getFailed()));

            System.out.println(sb);
        }
        catch (final IllegalStateException e)
        {
            System.out.println("WARNING: Unable to execute this command: " + e.getMessage());
        }

        return false;
    }
}
