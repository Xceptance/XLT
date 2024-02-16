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
package com.xceptance.xlt.report.providers;

import java.awt.Color;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.xceptance.common.io.FileUtils;
import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TimerData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.api.report.ReportProviderConfiguration;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.resultbrowser.RequestHistory;
import com.xceptance.xlt.engine.resultbrowser.RequestHistory.DumpMode;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.util.JFreeChartUtils;
import com.xceptance.xlt.report.util.TaskManager;
import com.xceptance.xlt.report.util.ValueSet;

import it.unimi.dsi.util.FastRandom;

/**
 *
 */
public class ErrorsReportProvider extends AbstractReportProvider
{
    /**
     * The maximum number of directory hints remembered for a certain error (stack trace).
     */
    private int directoryLimitPerError;

    /**
     * The chance to replace directory hints remembered for a certain error (stack trace) with new hints when above the
     * maximum number. Given and used in range from 0.0 to 1.0. Converted automatically from the relating property value
     * which is given in percent.
     */
    private double directoryReplacementChance;
    
    /**
     * The maximum number of directory hints remembered for a certain error (stack trace).
     */
    private int stackTracesLimit;

    /**
     * The root directory of the result set.
     */
    private FileObject resultsDirectory;

    /**
     * The dump mode used during the load test.
     */
    private final DumpMode dumpMode;

    /**
     * A mapping from stack trace strings to {@link ErrorReport} instances.
     */
    private final Map<String, ErrorValues> errorReports = new HashMap<>();

    /**
     * The errors value set for all transactions.
     */
    private final ValueSet transactionErrorsPerSecondValueSet = new ValueSet();

    /**
     * The errors value set for all actions.
     */
    private final ValueSet actionErrorsPerSecondValueSet = new ValueSet();

    /**
     * The errors value set for all requests.
     */
    private final ValueSet requestErrorsPerSecondValueSet = new ValueSet();

    /**
     * The error message text hash code to overview chart value set mapping.
     */
    private final Map<Integer, TransactionErrorOverviewValues> transactionErrorOverviewValues = new HashMap<>();

    /**
     * The value sets for each response error code.
     */
    private final Map<String, ValueSet> requestErrorOverviewValues = new HashMap<>();

    /**
     * some fix random sequence that is fast and always the same, this might change in the future
     */
    private final FastRandom random = new FastRandom(98765111L);

    /**
     * Constructor.
     */
    public ErrorsReportProvider()
    {
        final String dumpModeValue = XltProperties.getInstance().getProperty(RequestHistory.OUTPUT2DISK_PROPERTY, "onError");
        dumpMode = DumpMode.valueFrom(dumpModeValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfiguration(final ReportProviderConfiguration config)
    {
        super.setConfiguration(config);

        // cache some configuration values
        directoryLimitPerError = getConfiguration().getDirectoryLimitPerError();
        directoryReplacementChance = getConfiguration().getDirectoryReplacementChance();
        stackTracesLimit = getConfiguration().getStackTracesLimit();
        resultsDirectory = getConfiguration().getResultsDirectory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final List<TransactionOverviewChartReport> availableTransactionErrorOverviewCharts = new ArrayList<>();
        final List<RequestErrorChartReport> availableRequestErrorCharts = new ArrayList<>();

        if (getConfiguration().shouldChartsGenerated())
        {
            // create charts asynchronously
            final TaskManager taskManager = TaskManager.getInstance();

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    final int minMaxValueSetSize = getConfiguration().getChartWidth();

                    // post-process the time series
                    final TimeSeries transactionErrorsPerSecondTimeSeries = JFreeChartUtils.toStandardTimeSeries(transactionErrorsPerSecondValueSet.toMinMaxValueSet(minMaxValueSetSize),
                                                                                                                 "Transaction Errors/s");
                    final TimeSeries actionErrorsPerSecondTimeSeries = JFreeChartUtils.toStandardTimeSeries(actionErrorsPerSecondValueSet.toMinMaxValueSet(minMaxValueSetSize),
                                                                                                            "Action Errors/s");
                    final TimeSeries requestErrorsPerSecondTimeSeries = JFreeChartUtils.toStandardTimeSeries(requestErrorsPerSecondValueSet.toMinMaxValueSet(minMaxValueSetSize),
                                                                                                             "Request Errors/s");

                    createErrorsChart(transactionErrorsPerSecondTimeSeries, actionErrorsPerSecondTimeSeries,
                                      requestErrorsPerSecondTimeSeries);
                }
            });

            // create request error charts
            if (getConfiguration().createRequestErrorOverviewCharts())
            {
                // get top N transaction errors
                final int chartCount = getChartCount(getConfiguration().getRequestErrorOverviewChartLimit(),
                                                     requestErrorOverviewValues.size());
                final List<Entry<String, ValueSet>> selectedErrors = getRequestOverviewErrorsSortedByOccurrence().subList(0, chartCount);
                for (final Entry<String, ValueSet> eachEntry : selectedErrors)
                {
                    final RequestErrorChartReport requestErrorChartReport = new RequestErrorChartReport();
                    requestErrorChartReport.id = Integer.valueOf(eachEntry.getKey());

                    availableRequestErrorCharts.add(requestErrorChartReport);
                }

                taskManager.addTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (final Entry<String, ValueSet> eachEntry : selectedErrors)
                        {
                            final String responseCode = eachEntry.getKey();
                            final ValueSet data = eachEntry.getValue();

                            createRequestErrorOverviewChart(responseCode, data);
                        }
                    }
                });
            }

            // create transaction errors overview charts
            if (getConfiguration().createTransactionErrorOverviewCharts())
            {
                // get top N transaction errors
                final int chartCount = getChartCount(getConfiguration().getTransactionErrorOverviewChartLimit(),
                                                     transactionErrorOverviewValues.size());
                final List<TransactionErrorOverviewValues> selectedErrors = getTransactionErrorsSortedByOccurrence().subList(0, chartCount);
                for (final TransactionErrorOverviewValues eachError : selectedErrors)
                {
                    availableTransactionErrorOverviewCharts.add(eachError.getChartReport());
                }

                // create charts for selected errors
                taskManager.addTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (final TransactionErrorOverviewValues eachEntry : selectedErrors)
                        {
                            createTransactionErrorOverviewChart(eachEntry);
                        }
                    }
                });
            }

            // create the error details charts
            if (getConfiguration().createErrorDetailsCharts())
            {
                // get top N errors
                final int chartCount = getChartCount(getConfiguration().getErrorDetailsChartLimit(), errorReports.size());
                final List<ErrorValues> selectedErrors = getErrorsSortedByOccurrence().subList(0, chartCount);

                // the selected errors will have a chart and maybe a transaction overview chart, set the ID's
                for (final ErrorValues eachError : selectedErrors)
                {
                    eachError.errorReport.detailChartID = System.identityHashCode(eachError.errorReport);
                }

                // create the charts
                taskManager.addTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (final ErrorValues eachErrorValues : selectedErrors)
                        {
                            createErrorDetailsChart(eachErrorValues);
                        }
                    }
                });
            }
        }

        final ErrorsReport errorReport = new ErrorsReport();

        errorReport.errors = getErrorReports();
        errorReport.requestErrorOverviewCharts = availableRequestErrorCharts;
        errorReport.transactionErrorOverviewCharts = availableTransactionErrorOverviewCharts;
        sortDirectoryHints(errorReport);

        final String temp = computePathPrefix();
        if (temp != null)
        {
            errorReport.resultsPathPrefix = temp + "/";
        }

        return errorReport;
    }

    private int getChartCount(final int configuredCount, final int collectionSize)
    {
        if (configuredCount < 0 || configuredCount > collectionSize)
        {
            return collectionSize;
        }
        return configuredCount;
    }

    private List<Entry<String, ValueSet>> getRequestOverviewErrorsSortedByOccurrence()
    {
        final ArrayList<Entry<String, ValueSet>> errors = new ArrayList<>(requestErrorOverviewValues.entrySet());
        Collections.sort(errors, Collections.reverseOrder(new Comparator<Entry<String, ValueSet>>()
        {
            @Override
            public int compare(final Entry<String, ValueSet> o1, final Entry<String, ValueSet> o2)
            {
                return Long.compare(o1.getValue().getValueCount(), o2.getValue().getValueCount());
            }
        }));
        return errors;
    }

    private List<ErrorValues> getErrorsSortedByOccurrence()
    {
        final List<ErrorValues> errors = new ArrayList<>(errorReports.values());
        Collections.sort(errors, Collections.reverseOrder(new Comparator<ErrorValues>()
        {
            @Override
            public int compare(final ErrorValues o1, final ErrorValues o2)
            {
                return Integer.compare(o1.getErrorReport().count, o2.getErrorReport().count);
            }
        }));
        return errors;
    }

    private List<TransactionErrorOverviewValues> getTransactionErrorsSortedByOccurrence()
    {
        final List<TransactionErrorOverviewValues> errors = new ArrayList<>(transactionErrorOverviewValues.values());
        Collections.sort(errors, Collections.reverseOrder(new Comparator<TransactionErrorOverviewValues>()
        {
            @Override
            public int compare(final TransactionErrorOverviewValues o1, final TransactionErrorOverviewValues o2)
            {
                return Long.compare(o1.getValues().getValueCount(), o2.getValues().getValueCount());
            }
        }));
        return errors;
    }

    private void createRequestErrorOverviewChart(final String responseCode, final ValueSet data)
    {
        final String title = "(" + responseCode + ")";
        final String fileName = "r" + responseCode;

        final int width = getConfiguration().getChartWidth();
        final int height = (int) (getConfiguration().getChartHeight() / 2.8);

        createErrorChart(data, fileName, title, "Errors", "Errors", width, height, false, false);
    }

    private void createErrorDetailsChart(final ErrorValues errorValues)
    {
        final ErrorReport errorReport = errorValues.getErrorReport();

        final String title = null;
        final String fileName = "d" + errorReport.detailChartID;
        final ValueSet data = errorValues.getValues();

        final int detailsWidth = (int) (getConfiguration().getChartWidth() / 1.4);
        final int detailsHeight = (int) (getConfiguration().getChartHeight() / 3.5);

        createErrorChart(data, fileName, title, null, "Errors", detailsWidth, detailsHeight, false, false);
    }

    private void createTransactionErrorOverviewChart(final TransactionErrorOverviewValues errorOverview)
    {
        final String title = StringUtils.defaultIfBlank(errorOverview.getErrorMessage(), "");
        final String fileName = "t" + errorOverview.getOverviewChartID();
        final ValueSet data = errorOverview.getValues();

        final int overviewWidth = getConfiguration().getChartWidth();
        final int overviewHeight = (int) (getConfiguration().getChartHeight() / 2.8);

        createErrorChart(data, fileName, title, "Errors", "Errors", overviewWidth, overviewHeight, false, false);
    }

    private void createErrorChart(final ValueSet data, final String chartName, final String chartTitle, final String yAxisTitle,
                                  final String dataName, final int width, final int height, final boolean createLegend,
                                  final boolean createTimeAxis)
    {
        final int minMaxValueSetSize = width;
        final TimeSeries eachErrorTimeSeries = JFreeChartUtils.toStandardTimeSeries(data.toMinMaxValueSet(minMaxValueSetSize), dataName);
        final TimeSeriesCollection eachErrorTimeSeriesCollection = new TimeSeriesCollection(eachErrorTimeSeries);

        final JFreeChart chart = JFreeChartUtils.createBarChart(chartTitle, eachErrorTimeSeriesCollection, yAxisTitle, Color.RED,
                                                                getConfiguration().getChartStartTime(),
                                                                getConfiguration().getChartEndTime(), createLegend, createTimeAxis);

        JFreeChartUtils.saveChart(chart, chartName, new File(getConfiguration().getChartDirectory(), "errors"), width, height);
    }

    private List<ErrorReport> getErrorReports()
    {
        final List<ErrorReport> reports = new ArrayList<>(errorReports.size());
        for (final ErrorValues eachErrorValues : errorReports.values())
        {
            reports.add(eachErrorValues.getErrorReport());
        }
        return reports;
    }

    /**
     * Sorts the list of directory hints for any contained error report.
     *
     * @param errorsReport
     *            the errors report
     */
    private void sortDirectoryHints(final ErrorsReport errorsReport)
    {
        for (final ErrorReport errorReport : errorsReport.errors)
        {
            Collections.sort(errorReport.directoryHints);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data stat)
    {
        // process error messages/stack traces
        if (stat instanceof TransactionData)
        {
            final TransactionData txnStats = (TransactionData) stat;
            final String trace = txnStats.getFailureStackTrace();
            if (trace != null)
            {
                // qualify the trace with the test case/action name in case of equal stack traces (#1092)
                final String testCaseName = txnStats.getName();
                final String failedActionName = txnStats.getFailedActionName();
                final String key = testCaseName + "|" + failedActionName + "|" + trace;

                // lookup/create the error entry for this trace
                ErrorValues errorValues = errorReports.get(key);
                if (errorValues == null)
                {
                    final ErrorReport errorReport = new ErrorReport();
                    errorReport.message = txnStats.getFailureMessage();
                    errorReport.testCaseName = testCaseName;
                    errorReport.actionName = failedActionName;
                    errorReport.detailChartID = 0;
                    
                    //if (stackTracesLimit not reached) //TODO
                    errorReport.trace = trace;

                    errorValues = new ErrorValues(errorReport);
                    errorReports.put(key, errorValues);
                }

                final ErrorReport errorReport = errorValues.getErrorReport();

                // update errors per second for the error details
                if (getConfiguration().createErrorDetailsCharts())
                {
                    errorValues.getValues().addOrUpdateValue(txnStats.getEndTime(), 1);
                }

                // update errors per second for the transaction error overview
                if (getConfiguration().createTransactionErrorOverviewCharts())
                {
                    final int id = getTransactionErrorOverviewChartID(errorReport.message);
                    TransactionErrorOverviewValues overviewErrorValues = transactionErrorOverviewValues.get(id);
                    if (overviewErrorValues == null)
                    {
                        overviewErrorValues = new TransactionErrorOverviewValues(errorReport.message, id);
                        transactionErrorOverviewValues.put(id, overviewErrorValues);
                    }
                    overviewErrorValues.getValues().addOrUpdateValue(txnStats.getEndTime(), 1);
                }

                // update the error entry
                errorReport.count++;

                if (dumpMode != DumpMode.NEVER)
                {
                    // add directory hints (up to the limit)
                    final String directoryHint = txnStats.getDumpDirectoryPath();
                    if (directoryHint != null)
                    {
                        // check if we should add/replace a result browser directory hint
                        final boolean safeToAdd = errorReport.directoryHints.size() < directoryLimitPerError;
                        if (safeToAdd || random.nextDoubleFast() <= directoryReplacementChance)
                        {
                            // either limit not reached yet or replacement chance
                            final String indexFilePath = directoryHint + "/index.html";

                            try
                            {
                                // check if such a directory exists and contains an index.html file
                                if (VFS.getManager().resolveFile(resultsDirectory, indexFilePath).exists())
                                {
                                    // now decide what to do with it
                                    if (safeToAdd)
                                    {
                                        // add the directory
                                        errorReport.directoryHints.add(directoryHint);
                                    }
                                    else
                                    {
                                        // randomly replace one of the existing hints with the new hint
                                        errorReport.directoryHints.set(random.nextInt(directoryLimitPerError), directoryHint);
                                    }
                                }
                            }
                            catch (final FileSystemException e)
                            {
                                XltLogger.reportLogger.warn("Unable to check if '{}' exists in '{}'", indexFilePath,
                                                            resultsDirectory.getName().getPath());
                            }
                        }
                    }
                }
            }
        }

        // count errors/events
        if (stat instanceof TimerData)
        {
            final TimerData timerData = (TimerData) stat;

            if (timerData.hasFailed())
            {
                final ValueSet errorsPerSecondValueSet;

                if (timerData instanceof TransactionData)
                {
                    errorsPerSecondValueSet = transactionErrorsPerSecondValueSet;
                }
                else if (timerData instanceof ActionData)
                {
                    errorsPerSecondValueSet = actionErrorsPerSecondValueSet;
                }
                else if (timerData instanceof RequestData)
                {
                    errorsPerSecondValueSet = requestErrorsPerSecondValueSet;
                }
                else
                {
                    errorsPerSecondValueSet = null;
                }

                if (errorsPerSecondValueSet != null)
                {
                    // we expect the timer to be failed around the same time as it has finished
                    errorsPerSecondValueSet.addOrUpdateValue(timerData.getEndTime(), 1);
                }
            }

            // collect the request errors
            if (getConfiguration().createRequestErrorOverviewCharts() && timerData instanceof RequestData)
            {
                final RequestData requestData = (RequestData) timerData;
                final int code = requestData.getResponseCode();
                if (code == 0 || code >= 500)
                {
                    final String responseCode = String.valueOf(code);
                    ValueSet valueSet = requestErrorOverviewValues.get(responseCode);
                    if (valueSet == null)
                    {
                        valueSet = new ValueSet();
                        requestErrorOverviewValues.put(responseCode, valueSet);
                    }
                    valueSet.addOrUpdateValue(requestData.getEndTime(), 1);
                }
            }
        }
    }

    /**
     * Determines the path prefix to use when generating links from errors to result browsers.
     *
     * @return the path prefix, maybe <code>null</code>
     */
    private String computePathPrefix()
    {
        String pathPrefix = null;

        final ReportGeneratorConfiguration config = getConfiguration();
        if (config.isGenerateErrorLinks())
        {
            final URI linkedResultsBaseUri = config.getLinkedResultsBaseUri();

            if (linkedResultsBaseUri != null)
            {
                // there is an URI configured -> simply append the results directory name
                pathPrefix = appendPathToUri(linkedResultsBaseUri, config.getResultsDirectoryName());
            }
            else
            {
                // there is NO URI configured

                final FileObject resultDir = config.getResultsDirectory();
                if (resultDir instanceof LocalFile)
                {
                    // the results are in the regular file system -> create a path from the report to the results
                    final File reportDir = config.getReportDirectory();
                    final File resultsDirAsFile = FileUtils.convertLocalFileToFile((LocalFile) resultDir);
                    pathPrefix = FileUtils.computeRelativeUri(reportDir, resultsDirAsFile, true);
                }
                else
                {
                    // the results are in an archive file -> cannot generate links into an archive file
                }
            }
        }

        return pathPrefix;
    }

    /**
     * Appends the given path to the argument URI. Adds a single slash between the URI and the path if the argument URI
     * does already not end with a slash.
     *
     * @param uri
     *            the URI to extend
     * @param path
     *            the path to append
     * @return the extended URI as a string
     */
    private String appendPathToUri(final URI uri, final String path)
    {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(uri.toString());
        if (sb.charAt(sb.length() - 1) != '/')
        {
            sb.append('/');
        }
        sb.append(path);
        return sb.toString();
    }

    /**
     * Creates a combined chart where the passed errors time series are drawn as separate bar plots. The chart is
     * generated to the charts directory.
     *
     * @param transactionErrorsPerSecondTimeSeries
     *            the transaction errors
     * @param actionErrorsPerSecondTimeSeries
     *            the action errors
     * @param requestErrorsPerSecondTimeSeries
     *            the request errors
     */
    private void createErrorsChart(final TimeSeries transactionErrorsPerSecondTimeSeries, final TimeSeries actionErrorsPerSecondTimeSeries,
                                   final TimeSeries requestErrorsPerSecondTimeSeries)
    {
        // System.out.println("Creating errors chart ... ");

        // final long start = TimerUtils.getTime();

        // convert the time series
        final TimeSeriesCollection transactionErrors = new TimeSeriesCollection(transactionErrorsPerSecondTimeSeries);
        final TimeSeriesCollection actionErrors = new TimeSeriesCollection(actionErrorsPerSecondTimeSeries);
        final TimeSeriesCollection requestErrors = new TimeSeriesCollection(requestErrorsPerSecondTimeSeries);

        // create a combined plot area and add the separate bar plots to it
        final CombinedDomainXYPlot combinedPlot = JFreeChartUtils.createCombinedPlot(getConfiguration().getChartStartTime(),
                                                                                     getConfiguration().getChartEndTime());

        combinedPlot.add(JFreeChartUtils.createBarPlot(transactionErrors, null, "Transaction Errors", Color.RED));
        combinedPlot.add(JFreeChartUtils.createBarPlot(actionErrors, null, "Action Errors", Color.RED));
        combinedPlot.add(JFreeChartUtils.createBarPlot(requestErrors, null, "Request Errors", Color.RED));

        // finally create and save the chart
        final JFreeChart chart = JFreeChartUtils.createChart("Errors", combinedPlot);

        JFreeChartUtils.saveChart(chart, "Errors", getConfiguration().getChartDirectory(), getConfiguration().getChartWidth(),
                                  2 * getConfiguration().getChartHeight());

        // System.out.printf("OK (%,d values, %,d ms)\n", runTimeTimeSeries.getItemCount(), TimerUtils.getTime() -
        // start);
    }

    @Override
    public ReportGeneratorConfiguration getConfiguration()
    {
        return (ReportGeneratorConfiguration) super.getConfiguration();
    }

    private int getTransactionErrorOverviewChartID(final String errorMessage)
    {
        return StringUtils.defaultString(errorMessage).hashCode();
    }

    private static class ErrorValues
    {
        private final ErrorReport errorReport;

        private final ValueSet values = new ValueSet();

        public ErrorValues(final ErrorReport errorReport)
        {
            this.errorReport = errorReport;
        }

        public ErrorReport getErrorReport()
        {
            return errorReport;
        }

        public ValueSet getValues()
        {
            return values;
        }
    }

    private static class TransactionErrorOverviewValues
    {
        private final ValueSet values = new ValueSet();

        private final TransactionOverviewChartReport chartReport = new TransactionOverviewChartReport();

        public TransactionErrorOverviewValues(final String errorMessage, final int overviewChartID)
        {
            chartReport.id = overviewChartID;
            chartReport.title = errorMessage;
        }

        public int getOverviewChartID()
        {
            return chartReport.id;
        }

        public ValueSet getValues()
        {
            return values;
        }

        public String getErrorMessage()
        {
            return chartReport.title;
        }

        public TransactionOverviewChartReport getChartReport()
        {
            return chartReport;
        }
    }
}
