/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;

import com.xceptance.common.util.AbstractConfiguration;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.ReportProvider;
import com.xceptance.xlt.api.report.ReportProviderConfiguration;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.common.XltPropertyNames;
import com.xceptance.xlt.engine.XltExecutionContext;
import com.xceptance.xlt.report.ReportGeneratorConfiguration.ChartCappingInfo.ChartCappingMethod;
import com.xceptance.xlt.report.ReportGeneratorConfiguration.ChartCappingInfo.ChartCappingMode;
import com.xceptance.xlt.report.mergerules.InvalidRequestProcessingRuleException;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule;
import com.xceptance.xlt.report.providers.RequestTableColorization;
import com.xceptance.xlt.report.providers.RequestTableColorization.ColorizationRule;

/**
 * The ReportGeneratorConfiguration is the central place where all configuration information for the report generator
 * can be retrieved from.
 */
public class ReportGeneratorConfiguration extends AbstractConfiguration implements ReportProviderConfiguration
{
    /**
     * The supported scales of the y-axis in run time charts.
     */
    public enum ChartScale
    {
        /** A linear scale (default). */
        LINEAR,

        /** A logarithmic scale (log10). */
        LOGARITHMIC
    }

    /**
     * The settings describing how a chart is to be capped.
     */
    public static class ChartCappingInfo
    {
        /**
         * The supported capping methods.
         */
        public enum ChartCappingMethod
        {
         /** No capping (default). */
         NONE,

         /** Cap at an absolute value. */
         ABSOLUTE,

         /** Cap at the n-fold of the average value. */
         NFOLD_OF_AVERAGE
        };

        /**
         * The supported capping modes.
         */
        public enum ChartCappingMode
        {
         /** Cap the chart at the capping value only if necessary. */
         SMART,

         /** Always cap the chart at the capping value even if the maximum values are below the capping value. */
         ALWAYS
        };

        /**
         * The capping method to use.
         */
        public ChartCappingInfo.ChartCappingMethod method;

        /**
         * The capping mode to use.
         */
        public ChartCappingInfo.ChartCappingMode mode;

        /**
         * The capping parameter.
         */
        public double parameter;
    }

    private static final String PROP_PREFIX = XltConstants.XLT_PACKAGE_PATH + ".reportgenerator.";

    private static final String PROP_RUNTIME_PERCENTILES = PROP_PREFIX + "runtimePercentiles";

    private static final String PROP_RUNTIME_INTERVAL_BOUNDARIES = PROP_PREFIX + "runtimeIntervalBoundaries";

    private static final String PROP_REQUESTS_TABLE_COLORIZE = PROP_PREFIX + "requests.table.colorization";

    private static final String PROP_REQUESTS_TABLE_COLORIZE_DEFAULT = "default";

    private static final String PROP_SUFFIX_MATCHING = "matching";

    private static final String PROP_SUFFIX_MEAN = "mean";

    private static final String PROP_SUFFIX_MIN = "min";

    private static final String PROP_SUFFIX_MAX = "max";

    private static final String PROP_SUFFIX_PERCENTILE = "percentile";

    private static final String PROP_SUFFIX_SEGMENTATION = "segmentation";

    private static final String PROP_SUFFIX_ID = "id";

    private static final String PROP_CHARTS_PREFIX = PROP_PREFIX + "charts.";

    private static final String PROP_CHARTS_COMPRESSION_FACTOR = PROP_CHARTS_PREFIX + "compressionFactor";

    private static final String PROP_CHARTS_HEIGHT = PROP_CHARTS_PREFIX + "height";

    private static final String PROP_CHARTS_MOV_AVG_PERCENTAGE = PROP_CHARTS_PREFIX + "movingAverage.percentageOfValues";

    private static final String PROP_CHARTS_WIDTH = PROP_CHARTS_PREFIX + "width";

    private static final String PROP_DATA_RECORD_CLASSES_PREFIX = PROP_PREFIX + "dataRecords.";

    private static final String PROP_REPORT_PROVIDER_CLASSES_PREFIX = PROP_PREFIX + "providers.";

    private static final String PROP_REPORTS_ROOT_DIR = PROP_PREFIX + "reports";

    private static final String PROP_RESULTS_ROOT_DIR = PROP_PREFIX + "results";

    private static final String PROP_REQUEST_MERGE_RULES_PREFIX = PROP_PREFIX + "requestMergeRules.";

    // Special settings for profiling and debugging
    private static final String PROP_PARSER_THREAD_COUNT = PROP_PREFIX + "parser.threads";
    private static final String PROP_READER_THREAD_COUNT = PROP_PREFIX + "reader.threads";
    private static final String PROP_THREAD_QUEUE_SIZE = PROP_PREFIX + "queue.bucketsize";
    private static final String PROP_THREAD_QUEUE_LENGTH = PROP_PREFIX + "queue.length";
    private static final String PROP_DATA_SAMPLE_FACTOR = PROP_PREFIX + "data.sampleFactor";

    private static final String PROP_TRANSFORMATIONS_PREFIX = PROP_PREFIX + "transformations.";

    private static final String PROP_TRANSFORMATIONS_STYLE_SHEET_FILE_SUFFIX = ".styleSheetFileName";

    private static final String PROP_TRANSFORMATIONS_OUTPUT_FILE_SUFFIX = ".outputFileName";

    private static final String PROP_RESULTS_BASE_URI = PROP_PREFIX + "resultsBaseUri";

    private static final String PROP_GENERATE_ERROR_LINKS = PROP_PREFIX + "linkToResultBrowsers";

    private static final String PROP_CHART_SCALE = PROP_CHARTS_PREFIX + "scale";

    private static final String PROP_CHART_CAPPING_FACTOR = PROP_CHARTS_PREFIX + "cappingFactor";

    private static final String PROP_CHART_CAPPING_VALUE = PROP_CHARTS_PREFIX + "cappingValue";

    private static final String PROP_CHART_CAPPING_MODE = PROP_CHARTS_PREFIX + "cappingMode";

    private static final String PROP_REMOVE_INDEXES_FROM_REQUEST_NAMES = PROP_PREFIX + "requests.removeIndexes";

    private final float chartsCompressionFactor;

    private final int chartsHeight;

    private final int chartsWidth;

    private final File configDirectory;

    private final Map<String, Class<? extends Data>> dataRecordClasses;

    private final File homeDirectory;

    private final int movingAveragePoints;

    private final List<String> outputFileNames;

    private final List<Class<? extends ReportProvider>> reportProviderClasses;

    private final int[] runtimeIntervalBoundaries;

    private final double[] runtimePercentiles;

    private final List<RequestTableColorization> requestTableColorization;

    private final List<String> styleSheetFileNames;

    private final File testReportsRootDirectory;

    private final File testResultsRootDirectory;

    private File reportDirectory;

    /**
     * The results directory, used by providers for example to link to results.
     */
    private FileObject resultsDirectory;

    /**
     * The name of the results directory.
     */
    private String resultsDirectoryName;

    private File chartsDirectory;

    private File csvDirectory;

    private long maximumChartTime;

    private long minimumChartTime;

    private boolean noCharts;

    private boolean noAgentCharts;

    public final int readerThreadCount;
    public final int parserThreadCount;
    public final int threadQueueBucketSize;
    public final int threadQueueLength;

    public final int dataSampleFactor;

    private final ChartScale chartScaleMode;

    private final ChartCappingInfo transactionChartCappingInfo;

    private final ChartCappingInfo actionChartCappingInfo;

    private final ChartCappingInfo requestChartCappingInfo;

    private final ChartCappingInfo customChartCappingInfo;

    /**
     * The URI to the linked results base directory. It may be an ordinary valid http:// URI and it is <code>null</code>
     * if the property &quot;com.xceptance.xlt.reportgenerator.linked.results.base.dir&quot; is not set in
     * reportGenerator.properties.
     */
    private final URI linkedResultsBaseUri;

    /**
     * Whether links from the errors in the report to the corresponding results should be generated.
     */
    private final boolean generateErrorLinks;

    private final int requestErrorOverviewChartLimit;

    private final int transactionErrorOverviewChartLimit;

    private final int errorDetailsChartLimit;
    
    private final int directoryLimitPerError;
    
    private final double directoryReplacementChance;
    
    private final int stackTracesLimit;

    private final Map<Pattern, Double> apdexThresholdsByActionNamePattern = new HashMap<>();

    private double defaultApdexThreshold;

    /**
     * Whether or not to group events by test case.
     */
    private final boolean groupEventsByTestCase;

    /**
     * How many different events per test case?
     */
    private final int eventLimit;

    /**
     * How many different messages per event?
     */
    private final int eventMessageLimit;

    /**
     * Whether to automatically remove any indexes from the request name (i.e. "HomePage.1.27" -> "HomePage").
     */
    private final boolean removeIndexesFromRequestNames;

    /**
     * Creates a new ReportGeneratorConfiguration object.
     *
     * @throws IOException
     *             if an I/O error occurs
     */
    public ReportGeneratorConfiguration() throws IOException
    {
        this(null, null, null);
    }

    /**
     * Creates a new ReportGeneratorConfiguration object.
     *
     * @param overridePropertyFileName
     *            Property file that overrides the basic one. This parameter might be <code>null</code> or empty
     * @param commandLineProperties
     *            Properties set on command line. This parameter might be <code>null</code>.
     * @throws IOException
     *             if an I/O error occurs
     */
    public ReportGeneratorConfiguration(Properties xltProperties, final File overridePropertyFile, final Properties commandLineProperties)
        throws IOException
    {
        homeDirectory = XltExecutionContext.getCurrent().getXltHomeDir();
        configDirectory = XltExecutionContext.getCurrent().getXltConfigDir();

        if (xltProperties == null)
        {
            xltProperties = XltProperties.getInstance().getProperties();
        }

        // report generator's configuration
        {
            loadProperties(new File(configDirectory, XltConstants.LOAD_REPORT_PROPERTY_FILENAME));
        }

        // master controller's configuration
        // replace merge rules if necessary
        {
            final Map<String, String> currentMergeRules = snapshotMergeRules();
            loadProperties(new File(configDirectory, XltConstants.MASTERCONTROLLER_PROPERTY_FILENAME));
            mergeMergeRules(currentMergeRules);
        }

        // load test suite properties
        // replace merge rules if necessary
        {
            final Map<String, String> currentMergeRules = snapshotMergeRules();
            addProperties(xltProperties);
            mergeMergeRules(currentMergeRules);
        }

        // load command line property file
        // replace merge rules if necessary
        if (overridePropertyFile != null)
        {
            if (!overridePropertyFile.isFile() || !overridePropertyFile.canRead())
            {
                throw new FileNotFoundException(overridePropertyFile.getAbsolutePath());
            }

            final Map<String, String> currentMergeRules = snapshotMergeRules();
            loadProperties(overridePropertyFile);
            mergeMergeRules(currentMergeRules);
        }

        // load command line properties
        // replace merge rules if necessary
        if (commandLineProperties != null)
        {
            final Map<String, String> currentMergeRules = snapshotMergeRules();
            addProperties(commandLineProperties);
            mergeMergeRules(currentMergeRules);
        }

        File testReportsRootDir = getFileProperty(PROP_REPORTS_ROOT_DIR, new File(homeDirectory, XltConstants.REPORT_ROOT_DIR));
        if (!testReportsRootDir.isAbsolute())
        {
            testReportsRootDir = new File(homeDirectory, testReportsRootDir.getPath());
        }
        testReportsRootDirectory = testReportsRootDir;

        File testResultsRootDir = getFileProperty(PROP_RESULTS_ROOT_DIR, new File(homeDirectory, XltConstants.RESULT_ROOT_DIR));
        if (!testResultsRootDir.isAbsolute())
        {
            testResultsRootDir = new File(homeDirectory, testResultsRootDir.getPath());
        }
        testResultsRootDirectory = testResultsRootDir;

        // error settings
        generateErrorLinks = getBooleanProperty(PROP_GENERATE_ERROR_LINKS, false);
        linkedResultsBaseUri = getUriProperty(PROP_RESULTS_BASE_URI, null);

        requestErrorOverviewChartLimit = getIntProperty(XltPropertyNames.ReportGenerator.Errors.REQUEST_ERROR_OVERVIEW_CHARTS_LIMIT, 50);
        transactionErrorOverviewChartLimit = getIntProperty(XltPropertyNames.ReportGenerator.Errors.TRANSACTION_ERROR_OVERVIEW_CHARTS_LIMIT,
                                                            50);
        errorDetailsChartLimit = getIntProperty(XltPropertyNames.ReportGenerator.Errors.TRANSACTION_ERROR_DETAIL_CHARTS_LIMIT, 50);

        directoryLimitPerError = getIntProperty(XltPropertyNames.ReportGenerator.Errors.DIRECTORY_LIMIT_PER_ERROR, 10);
        directoryReplacementChance = getDoubleProperty(XltPropertyNames.ReportGenerator.Errors.DIRECTORY_REPLACEMENT_CHANCE, 0.1);
        
        stackTracesLimit = getIntProperty(XltPropertyNames.ReportGenerator.Errors.STACKTRACES_LIMIT, 500);
        
        // event settings
        groupEventsByTestCase = getBooleanProperty(PROP_PREFIX + "events.groupByTestCase", true);
        eventLimit = getIntProperty(PROP_PREFIX + "events.eventLimit", 100);
        eventMessageLimit = getIntProperty(PROP_PREFIX + "events.messageLimit", 100);

        // chart settings
        chartScaleMode = getEnumProperty(ChartScale.class, PROP_CHART_SCALE, ChartScale.LINEAR);

        final double defaultChartCappingFactor = getDoubleProperty(PROP_CHART_CAPPING_FACTOR, -1);
        final double defaultChartCappingValue = getDoubleProperty(PROP_CHART_CAPPING_VALUE, -1);
        final ChartCappingMode defaultChartCappingMode = getEnumProperty(ChartCappingMode.class, PROP_CHART_CAPPING_MODE,
                                                                         ChartCappingMode.SMART);

        transactionChartCappingInfo = readChartCappingInfo("transactions", defaultChartCappingValue, defaultChartCappingFactor,
                                                           defaultChartCappingMode);
        actionChartCappingInfo = readChartCappingInfo("actions", defaultChartCappingValue, defaultChartCappingFactor,
                                                      defaultChartCappingMode);
        requestChartCappingInfo = readChartCappingInfo("requests", defaultChartCappingValue, defaultChartCappingFactor,
                                                       defaultChartCappingMode);
        customChartCappingInfo = readChartCappingInfo("custom", defaultChartCappingValue, defaultChartCappingFactor,
                                                      defaultChartCappingMode);

        chartsCompressionFactor = (float) getDoubleProperty(PROP_CHARTS_COMPRESSION_FACTOR, 0.0f);
        chartsWidth = getIntProperty(PROP_CHARTS_WIDTH, 900);
        chartsHeight = getIntProperty(PROP_CHARTS_HEIGHT, 300);
        movingAveragePoints = getIntProperty(PROP_CHARTS_MOV_AVG_PERCENTAGE, 5);

        readerThreadCount = Math.max(1, getIntProperty(PROP_READER_THREAD_COUNT, Runtime.getRuntime().availableProcessors()));
        parserThreadCount = Math.max(1, getIntProperty(PROP_PARSER_THREAD_COUNT, Runtime.getRuntime().availableProcessors()));

        dataSampleFactor = Math.max(1, getIntProperty(PROP_DATA_SAMPLE_FACTOR, 1));

        threadQueueBucketSize = Math.max(1, getIntProperty(PROP_THREAD_QUEUE_SIZE, Dispatcher.DEFAULT_QUEUE_CHUNK_SIZE));
        threadQueueLength = Math.max(1, getIntProperty(PROP_THREAD_QUEUE_LENGTH, Dispatcher.DEFAULT_QUEUE_LENGTH));

        removeIndexesFromRequestNames = getBooleanProperty(PROP_REMOVE_INDEXES_FROM_REQUEST_NAMES, true);

        dataRecordClasses = readDataRecordClasses();
        reportProviderClasses = readReportProviderClasses();

        runtimeIntervalBoundaries = readRuntimeIntervalBoundaries();
        runtimePercentiles = readRuntimePercentiles();

        requestTableColorization = readRequestTableColorization(runtimeIntervalBoundaries, runtimePercentiles);

        // load the transformation configuration
        outputFileNames = new ArrayList<>();
        styleSheetFileNames = new ArrayList<>();

        readTransformations(outputFileNames, styleSheetFileNames);

        // Apdex settings
        readApdexThresholds();
    }

    /**
     * Checks the given string for leading zero digits.
     *
     * @param s
     *            the string to be checked
     */
    private void checkForLeadingZeros(final String s)
    {
        if (s.length() > 1 && s.startsWith("0"))
        {
            final StringBuilder sb = new StringBuilder("Leading zeros are not allowed in request merge rule indices.\nPlease check your configuration and fix the following properties:");
            for (final String prop : getPropertyKeysWithPrefix(PROP_REQUEST_MERGE_RULES_PREFIX + s + "."))
            {
                sb.append("\n\t").append(prop);
            }
            sb.append("\n");

            throw new RuntimeException(sb.toString());
        }
    }

    /**
     * Reads the chart capping settings for the given chart type from the configuration. Uses the passed defaults in
     * case there is no specific value configured.
     *
     * @param chartType
     *            the chart type
     * @param defaultCappingValue
     *            the default capping value
     * @param defaultCappingFactor
     *            the default capping factor
     * @param defaultCappingMode
     *            the default capping mode
     * @return the capping info
     */
    private ChartCappingInfo readChartCappingInfo(final String chartType, final double defaultCappingValue,
                                                  final double defaultCappingFactor, final ChartCappingMode defaultCappingMode)
    {
        final ChartCappingInfo info = new ChartCappingInfo();

        final double factor = getDoubleProperty(PROP_CHART_CAPPING_FACTOR + "." + chartType, defaultCappingFactor);
        final double value = getDoubleProperty(PROP_CHART_CAPPING_VALUE + "." + chartType, defaultCappingValue);

        if (value > 0)
        {
            info.method = ChartCappingMethod.ABSOLUTE;
            info.parameter = value;
        }
        else if (factor > 1)
        {
            info.method = ChartCappingMethod.NFOLD_OF_AVERAGE;
            info.parameter = factor;
        }
        else
        {
            info.method = ChartCappingMethod.NONE;
            info.parameter = 0;
        }

        info.mode = getEnumProperty(ChartCappingMode.class, PROP_CHART_CAPPING_MODE + "." + chartType, defaultCappingMode);

        return info;
    }

    /**
     * Returns the Apdex threshold value configured for the given action.
     *
     * @param actionName
     *            the name of the action
     * @return the threshold
     */
    public double getApdexThresholdForAction(final String actionName)
    {
        for (final Entry<Pattern, Double> entry : apdexThresholdsByActionNamePattern.entrySet())
        {
            if (RegExUtils.isMatching(actionName, entry.getKey()))
            {
                return entry.getValue();
            }
        }

        return defaultApdexThreshold;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getChartDirectory()
    {
        if (chartsDirectory == null)
        {
            chartsDirectory = new File(reportDirectory, XltConstants.REPORT_CHART_DIR);
        }

        if (!chartsDirectory.exists())
        {
            chartsDirectory.mkdirs();
        }

        return chartsDirectory;
    }

    /**
     * Returns the compression factor to use when creating Webp images.
     *
     * @return the compression quality (0 -> fastest compression, 1 -> best compression)
     */
    public float getChartCompressionFactor()
    {
        return chartsCompressionFactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getChartEndTime()
    {
        return maximumChartTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getChartHeight()
    {
        return chartsHeight;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getChartStartTime()
    {
        return minimumChartTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getChartWidth()
    {
        return chartsWidth;
    }

    /**
     * Returns the scale to be used for the y-axis of run time charts.
     *
     * @return {@link ChartScale#LINEAR} or {@link ChartScale#LOGARITHMIC}
     */
    public ChartScale getChartScale()
    {
        return chartScaleMode;
    }

    /**
     * Returns the capping information configured for transaction charts.
     *
     * @return the capping info
     */
    public ChartCappingInfo getTransactionChartCappingInfo()
    {
        return transactionChartCappingInfo;
    }

    /**
     * Returns the capping information configured for action charts.
     *
     * @return the capping info
     */
    public ChartCappingInfo getActionChartCappingInfo()
    {
        return actionChartCappingInfo;
    }

    /**
     * Returns the capping information configured for request charts.
     *
     * @return the capping info
     */
    public ChartCappingInfo getRequestChartCappingInfo()
    {
        return requestChartCappingInfo;
    }

    /**
     * Returns the capping information configured for custom charts.
     *
     * @return the capping info
     */
    public ChartCappingInfo getCustomChartCappingInfo()
    {
        return customChartCappingInfo;
    }

    /**
     * Returns the directory where the master controller's configuration is located.
     *
     * @return the config directory
     */
    public File getConfigDirectory()
    {
        return configDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getCsvDirectory()
    {
        return csvDirectory;
    }

    public Map<String, Class<? extends Data>> getDataRecordClasses()
    {
        return dataRecordClasses;
    }

    /**
     * Returns the master controller's home directory.
     *
     * @return the home directory
     */
    public File getHomeDirectory()
    {
        return homeDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMovingAveragePercentage()
    {
        return movingAveragePoints;
    }

    public List<String> getOutputFileNames()
    {
        return outputFileNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getReportDirectory()
    {
        return reportDirectory;
    }

    public List<Class<? extends ReportProvider>> getReportProviderClasses()
    {
        return reportProviderClasses;
    }

    public FileObject getResultsDirectory()
    {
        return resultsDirectory;
    }

    public String getResultsDirectoryName()
    {
        return resultsDirectoryName;
    }

    public int[] getRuntimeIntervalBoundaries()
    {
        return runtimeIntervalBoundaries;
    }

    public double[] getRuntimePercentiles()
    {
        return runtimePercentiles;
    }

    public List<RequestTableColorization> getRequestTableColorizations()
    {
        return requestTableColorization;
    }

    public String getRequestTableColorizationDefaultGroupName()
    {
        return PROP_REQUESTS_TABLE_COLORIZE_DEFAULT;
    }

    public List<String> getStyleSheetFileNames()
    {
        return styleSheetFileNames;
    }

    /**
     * Returns the root directory of all test reports.
     *
     * @return the test reports directory
     */
    public File getTestReportsRootDirectory()
    {
        return testReportsRootDirectory;
    }

    /**
     * Returns the root directory of all test result files.
     *
     * @return the test results directory
     */
    public File getTestResultsRootDirectory()
    {
        return testResultsRootDirectory;
    }

    /**
     * The number of the top N occurring errors for which to create the error details chart.
     *
     * @return the maximum number of charts to create
     */
    public int getErrorDetailsChartLimit()
    {
        return errorDetailsChartLimit;
    }
    
    /**
     * The maximum number of directory hints remembered for a certain error (stack trace).
     *
     * @return the maximum number of directories to list
     */
    public int getDirectoryLimitPerError()
    {
        return directoryLimitPerError;
    }
    
    /**
     * The chance to replace directory hints remembered for a certain error (stack trace) when the maximum number is reached.
     *
     * @return the chance to replace listed directory hints
     */
    public double getDirectoryReplacementChance()
    {
        return directoryReplacementChance;
    }
    
    /**
     * The maximum number of errors that will be saved complete with their stack trace.
     *
     * @return the maximum number of error stack traces displayed in the report
     */
    public int getStackTracesLimit()
    {
        return stackTracesLimit;
    }

    /**
     * If true then the error details charts should be created.
     *
     * @return true if the error detail charts should be generated otherwise false
     */
    public boolean createErrorDetailsCharts()
    {
        return getErrorDetailsChartLimit() != 0;
    }

    /**
     * If true then the request error charts should be created.
     *
     * @return true if the request error charts should be generated otherwise false
     */
    public boolean createRequestErrorOverviewCharts()
    {
        return getRequestErrorOverviewChartLimit() != 0;
    }

    /**
     * The number of the top N request errors for which to create the overview chart.
     *
     * @return the maximum number of charts to create
     */
    public int getRequestErrorOverviewChartLimit()
    {
        return requestErrorOverviewChartLimit;
    }

    /**
     * If true then the transaction error charts should be created.
     *
     * @return true if the transaction error charts should be generated otherwise false
     */
    public boolean createTransactionErrorOverviewCharts()
    {
        return getTransactionErrorOverviewChartLimit() != 0;
    }

    /**
     * The number of the top N transaction errors for which to create the overview chart.
     *
     * @return the maximum number of charts to create
     */
    public int getTransactionErrorOverviewChartLimit()
    {
        return transactionErrorOverviewChartLimit;
    }

    /**
     * Sets the new value of the 'maximumChartTime' attribute.
     *
     * @param maximumChartTime
     *            the new maximumChartTime value
     */
    public void setChartEndTime(final long maximumChartTime)
    {
        this.maximumChartTime = maximumChartTime;
    }

    /**
     * Sets the new value of the 'minimumChartTime' attribute.
     *
     * @param minimumChartTime
     *            the new minimumChartTime value
     */
    public void setChartStartTime(final long minimumChartTime)
    {
        this.minimumChartTime = minimumChartTime;
    }

    /**
     * Sets the new value of the 'reportDirectory' attribute.
     *
     * @param reportDirectory
     *            the new reportDirectory value
     */
    public void setReportDirectory(final File reportDirectory)
    {
        this.reportDirectory = reportDirectory;
        chartsDirectory = new File(reportDirectory, XltConstants.REPORT_CHART_DIR);
        csvDirectory = new File(reportDirectory, XltConstants.REPORT_TIMER_DIR);
    }

    /**
     * Sets the new value of the 'resultDirectory' attribute.
     *
     * @param resultsDirectory
     *            the new resultDirectory value
     */
    public void setResultsDirectory(final FileObject resultsDirectory)
    {
        this.resultsDirectory = resultsDirectory;
    }

    /**
     * Sets the new value of the 'setResultDirectoryName' attribute.
     *
     * @param resultsDirectoryName
     *            the new resultDirectoryName value
     */
    public void setResultsDirectoryName(final String resultsDirectoryName)
    {
        this.resultsDirectoryName = resultsDirectoryName;
    }

    /**
     * Disables chart generation.
     */
    public void disableChartsGeneration()
    {
        noCharts = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldChartsGenerated()
    {
        return !noCharts;
    }

    /**
     * Disables the generation of agent charts.
     */
    public void disableAgentCharts()
    {
        noAgentCharts = true;
    }

    /**
     * Returns whether the generation of agent charts is enabled.
     */
    public boolean agentChartsEnabled()
    {
        return !noCharts && !noAgentCharts;
    }

    /**
     * Returns whether links from the errors in the report to the corresponding results should be generated.
     *
     * @return <code>true</code> if to generate links for the results of the erroneous test executions in the report,
     *         <code>false</code> otherwise
     */
    public boolean isGenerateErrorLinks()
    {
        return generateErrorLinks;
    }

    /**
     * @return the linkedResultsBaseDir
     */
    public URI getLinkedResultsBaseUri()
    {
        return linkedResultsBaseUri;
    }

    /**
     * Indicates whether or not to group events by test case.
     *
     * @return <code>true</code> if events should be grouped by test case, <code>false</code> otherwise
     */
    public boolean getGroupEventsByTestCase()
    {
        return groupEventsByTestCase;
    }

    /**
     * Indicates whether or not to group events by test case.
     *
     * @return
     */
    public int getEventLimitPerTestCase()
    {
        return eventLimit;
    }


    /**
     * Indicates whether or not to group events by test case.
     *
     * @return <code>true</code> if events should be grouped by test case, <code>false</code> otherwise
     */
    public int getEventMessageLimitPerEvent()
    {
        return eventMessageLimit;
    }


    /**
     * Returns whether to automatically remove any indexes from the request name (i.e. "HomePage.1.27" -> "HomePage").
     *
     * @return <code>true</code> if indexes are to be removed, <code>false</code> otherwise
     */
    public boolean getRemoveIndexesFromRequestNames()
    {
        return removeIndexesFromRequestNames;
    }

    /**
     * Reads and returns all configured runtime interval boundaries.
     *
     * @return the boundary values
     */
    private int[] readRuntimeIntervalBoundaries()
    {
        try
        {
            final int[] values = readIntValueList(PROP_RUNTIME_INTERVAL_BOUNDARIES, "");

            // check values
            for (final int value : values)
            {
                if (value < 1)
                {
                    throw new IllegalArgumentException(String.format("Value '%d' is smaller than 1", value));
                }
            }

            // sort the values
            Arrays.sort(values);

            return values;
        }
        catch (final Exception e)
        {
            throw new XltException("Failed to parse runtime interval boundaries: " + e.getMessage(), e);
        }
    }

    /**
     * Reads and returns all configured runtime percentiles.
     *
     * @return the percentiles
     */
    private double[] readRuntimePercentiles()
    {
        try
        {
            final double[] values = readDoubleValueList(PROP_RUNTIME_PERCENTILES, "50, 95, 99, 99.9");

            // check values
            for (final double value : values)
            {
                if (value <= 0.0 || value > 100.0)
                {
                    throw new IllegalArgumentException(String.format("Value '%f' is not in range (0,100]", value));
                }
            }

            // sort the values
            Arrays.sort(values);

            return values;
        }
        catch (final Exception e)
        {
            throw new XltException("Failed to parse runtime percentiles: " + e.getMessage(), e);
        }
    }

    /**
     * Reads and returns all colorization properties for the request tables.
     *
     * @return
     */
    private List<RequestTableColorization> readRequestTableColorization(final int[] segmentationIntervals,
                                                                        final double[] percentileIntervals)
    {
        try
        {
            final List<RequestTableColorization> colorizationConfigs = new ArrayList<>();

            final Set<String> groupNames = getPropertyKeyFragment(PROP_REQUESTS_TABLE_COLORIZE + ".");
            for (final String eachGroupName : groupNames)
            {
                final String propertyGroup = PROP_REQUESTS_TABLE_COLORIZE + "." + eachGroupName;

                final String matchingPattern;
                final String propertyMatching = propertyGroup + "." + PROP_SUFFIX_MATCHING;
                if (PROP_REQUESTS_TABLE_COLORIZE_DEFAULT.equals(eachGroupName))
                {
                    matchingPattern = ".*";
                }
                else
                {
                    matchingPattern = getStringProperty(propertyMatching);

                    if (StringUtils.isBlank(matchingPattern))
                    {
                        throw new XltException(propertyMatching + " has no value. The value must be a regular expression");
                    }
                }

                // just to break early in case the pattern is invalid
                try
                {
                    compileRegEx(matchingPattern, propertyMatching);
                }
                catch (XltException e)
                {
                    throw new XltException(e.getMessage());
                }

                final List<RequestTableColorization.ColorizationRule> colorizationRules = new ArrayList<>();
                colorizationRules.add(readColorizationRule(propertyGroup, PROP_SUFFIX_MEAN, PROP_SUFFIX_MEAN, false));
                colorizationRules.add(readColorizationRule(propertyGroup, PROP_SUFFIX_MIN, PROP_SUFFIX_MIN, false));
                colorizationRules.add(readColorizationRule(propertyGroup, PROP_SUFFIX_MAX, PROP_SUFFIX_MAX, false));
                colorizationRules.addAll(readPercentileColorizationRules(propertyGroup, percentileIntervals));
                colorizationRules.addAll(readSegmentationColorizationRules(propertyGroup, segmentationIntervals));

                colorizationConfigs.add(new RequestTableColorization(eachGroupName, matchingPattern, colorizationRules));
            }

            return colorizationConfigs;
        }
        catch (final Exception e)
        {
            throw new XltException("Failed to parse request table colorization: " + e.getMessage(), e);
        }
    }

    private List<RequestTableColorization.ColorizationRule> readSegmentationColorizationRules(final String propertyGroup,
                                                                                              final int[] segmentationIntervals)
    {
        List<RequestTableColorization.ColorizationRule> segmentationRules = new ArrayList<>();

        final String segmentationProperty = propertyGroup + "." + PROP_SUFFIX_SEGMENTATION;

        final Set<String> segmentationGroups = getPropertyKeyFragment(segmentationProperty + ".");
        for (final String eachSegmentationGroup : segmentationGroups)
        {
            final String propertyID = segmentationProperty + "." + eachSegmentationGroup + "." + PROP_SUFFIX_ID;

            final String segmentationValue = getStringProperty(propertyID);
            final List<String> segmentationStrings = getSegmentationsAsStringList(segmentationIntervals);
            if (!segmentationStrings.contains(segmentationValue) && !">".equals(segmentationValue))
            {
                throw new XltException(propertyID + " = " + segmentationValue + " value should be one of " + segmentationStrings +
                                       " as specified at the \"runtimeIntervalBoundaries\" property or \">\" for the last column.");
            }

            ColorizationRule rule = readColorizationRule(segmentationProperty, eachSegmentationGroup, PROP_SUFFIX_SEGMENTATION,
                                                         !">".equals(segmentationValue));
            rule.id = segmentationValue;

            // check for last column rule and replace id according to the testreport.xml segmentation "to" value
            if (">".equals(rule.id))
            {
                rule.id = "";
            }

            segmentationRules.add(rule);
        }
        return segmentationRules;
    }

    private List<String> getSegmentationsAsStringList(final int[] segmentationIntervals)
    {
        List<String> list = new ArrayList<>(segmentationIntervals.length);
        for (int eachEntry : segmentationIntervals)
        {
            list.add(Integer.toString(eachEntry));
        }
        return list;
    }

    private List<RequestTableColorization.ColorizationRule> readPercentileColorizationRules(final String propertyGroup,
                                                                                            final double[] percentileIntervals)
    {
        List<RequestTableColorization.ColorizationRule> percentileRules = new ArrayList<>();

        final String percentileProperty = propertyGroup + "." + PROP_SUFFIX_PERCENTILE;

        final Set<String> percentileGroups = getPropertyKeyFragment(percentileProperty + ".");
        for (final String eachPercentileGroup : percentileGroups)
        {
            final String propertyID = percentileProperty + "." + eachPercentileGroup + "." + PROP_SUFFIX_ID;

            final String percentileValue = getStringProperty(propertyID);
            final List<String> percentileStrings = getPercentilesAsStringList(percentileIntervals);
            if (!percentileStrings.contains(percentileValue))
            {
                throw new XltException(propertyID + " = " + percentileValue + " value should be one of " + percentileStrings +
                                       " as specified at the \"runtimePercentiles\" property.");
            }
            final String percentileID = "p" + percentileValue;

            ColorizationRule rule = readColorizationRule(percentileProperty, eachPercentileGroup, PROP_SUFFIX_PERCENTILE, false);
            rule.id = percentileID;

            percentileRules.add(rule);
        }
        return percentileRules;
    }

    private List<String> getPercentilesAsStringList(final double[] percentileIntervals)
    {
        List<String> list = new ArrayList<>(percentileIntervals.length);
        for (double eachEntry : percentileIntervals)
        {
            if (eachEntry == (long) eachEntry)
            {
                list.add(Long.toString((long) eachEntry));
            }
            else
            {
                list.add(Double.toString(eachEntry));
            }
        }
        return list;
    }

    private RequestTableColorization.ColorizationRule readColorizationRule(final String propertyGroup, final String propertyName,
                                                                           final String type, final boolean scaleInverted)
        throws XltException
    {
        final String ruleProperty = propertyGroup + "." + propertyName;

        if (getPropertyKeyFragments(ruleProperty).isEmpty())
        {
            return null;
        }

        final String rangeSpec = getStringProperty(ruleProperty);
        final String[] ranges = rangeSpec.split(" ");
        if (ranges.length != 3)
        {
            throw new XltException(ruleProperty + " = " + rangeSpec +
                                   " must be a whitespace separated list of numbers in the pattern of \"<FROM> <TARGET> <TO>\"");
        }

        final int from = parseInt(ruleProperty + " (FROM parameter, first value)", ranges[0]);
        final int target = parseInt(ruleProperty + " (TARGET parameter, second value)", ranges[1]);
        final int to = parseInt(ruleProperty + " (TO parameter, third value)", ranges[2]);

        if (from < 0)
            throw new XltException(ruleProperty + " = " + rangeSpec + " FROM parameter (first value) must be greater or equal 0");

        if (!scaleInverted)
        {
            if (to <= from)
                throw new XltException(ruleProperty + " = " + rangeSpec +
                                       " TO parameter (third value) must be greater than the FROM parameter (first value)");

            if (target < from || target > to)
                throw new XltException(ruleProperty + " = " + rangeSpec +
                                       " TARGET parameter (second value) must be greater or equal the FROM parameter (first value) and lower or equal the TO parameter (third value)");
        }
        else
        {
            if (to >= from)
                throw new XltException(ruleProperty + " = " + rangeSpec +
                                       " TO parameter (third value) must be lower than the FROM parameter (first value)");

            if (target > from || target < to)
                throw new XltException(ruleProperty + " = " + rangeSpec +
                                       " TARGET parameter (second value) must be less or equal the FROM parameter (first value) and greater or equal the TO parameter (third value)");
        }

        final RequestTableColorization.ColorizationRule rule = new RequestTableColorization.ColorizationRule(propertyName, type);
        rule.from = scaleInverted ? to : from;
        rule.target = target;
        rule.to = scaleInverted ? from : to;

        return rule;
    }

    /**
     * Parses the value of a property as a list of comma-separated integers.
     *
     * @param propertyName
     *            the name of the property
     * @param defaultValue
     *            the default value
     * @return the int values
     */
    private int[] readIntValueList(final String propertyName, final String defaultValue)
    {
        final String propertyValue = getStringProperty(propertyName, defaultValue);
        final String[] valueStrings = StringUtils.split(propertyValue, " ,;");

        final int[] values = new int[valueStrings.length];

        for (int i = 0; i < valueStrings.length; i++)
        {
            values[i] = Integer.parseInt(valueStrings[i]);
        }

        return values;
    }

    /**
     * Parses the value of a property as a list of comma-separated integers.
     *
     * @param propertyName
     *            the name of the property
     * @param defaultValue
     *            the default value
     * @return the int values
     */
    private double[] readDoubleValueList(final String propertyName, final String defaultValue)
    {
        final String propertyValue = getStringProperty(propertyName, defaultValue);
        final String[] valueStrings = StringUtils.split(propertyValue, " ,;");

        final double[] values = new double[valueStrings.length];

        for (int i = 0; i < valueStrings.length; i++)
        {
            values[i] = Double.parseDouble(valueStrings[i]);
        }

        return values;
    }

    /**
     * Reads and returns all configured data record classes as a mapping from type code to class.
     *
     * @return the data record classes
     */
    private Map<String, Class<? extends Data>> readDataRecordClasses()
    {
        final Map<String, Class<? extends Data>> dataRecordClasses = new HashMap<>();

        final Set<String> typeCodes = getPropertyKeyFragment(PROP_DATA_RECORD_CLASSES_PREFIX);
        for (final String typeCode : typeCodes)
        {
            final Class<?> cl = getClassProperty(PROP_DATA_RECORD_CLASSES_PREFIX + typeCode);
            if (!Data.class.isAssignableFrom(cl))
            {
                continue;
            }

            final Class<? extends Data> c = cl.asSubclass(Data.class);

            dataRecordClasses.put(typeCode, c);
        }

        return dataRecordClasses;
    }

    /**
     * Reads and returns the list of all configured report provider classes.
     *
     * @return the list of report provider classes
     */
    private List<Class<? extends ReportProvider>> readReportProviderClasses()
    {
        final List<Class<? extends ReportProvider>> providerClasses = new ArrayList<>();

        final Set<String> keys = getPropertyKeysWithPrefix(PROP_REPORT_PROVIDER_CLASSES_PREFIX);
        for (final String key : keys)
        {
            final Class<?> cl = getClassProperty(key);
            if (!ReportProvider.class.isAssignableFrom(cl))
            {
                continue;
            }

            final Class<? extends ReportProvider> c = cl.asSubclass(ReportProvider.class);

            providerClasses.add(c);
        }

        return providerClasses;
    }

    /**
     * Reads the transformation configurations. The style sheet file names and output file names for each transformation
     * are added to the respective lists passed as parameters.
     *
     * @param outputFileNames
     *            the list of output file names
     * @param styleSheetFileNames
     *            the list of style sheet file names
     */
    private void readTransformations(final List<String> outputFileNames, final List<String> styleSheetFileNames)
    {
        final Set<String> keys = getPropertyKeyFragment(PROP_TRANSFORMATIONS_PREFIX);
        for (final String key : keys)
        {
            final String propertyPrefix = PROP_TRANSFORMATIONS_PREFIX + key;

            final File outputFile = getFileProperty(propertyPrefix + PROP_TRANSFORMATIONS_OUTPUT_FILE_SUFFIX);
            final File styleSheetFile = getFileProperty(propertyPrefix + PROP_TRANSFORMATIONS_STYLE_SHEET_FILE_SUFFIX);

            outputFileNames.add(outputFile.getPath());
            styleSheetFileNames.add(styleSheetFile.getPath());
        }
    }

    private Map<String, String> snapshotMergeRules()
    {
        // save old merge rules and remove them from configuration
        final Set<String> oldMergeRuleKeys = getPropertyKeysWithPrefix(PROP_REQUEST_MERGE_RULES_PREFIX);
        final Map<String, String> oldMergeRulesComplete = new HashMap<>();
        for (final String key : oldMergeRuleKeys)
        {
            oldMergeRulesComplete.put(key, getStringProperty(key));
            getProperties().remove(key);
        }

        return oldMergeRulesComplete;
    }

    private void mergeMergeRules(final Map<String, String> oldMergeRulesComplete)
    {
        // if no new merge rule was loaded, apply the old rules
        if (getPropertyKeysWithPrefix(PROP_REQUEST_MERGE_RULES_PREFIX).isEmpty())
        {
            for (final Entry<String, String> rule : oldMergeRulesComplete.entrySet())
            {
                getProperties().setProperty(rule.getKey(), rule.getValue());
            }
        }
    }

    /**
     * Reads the configured request processing rules and returns new instances
     *
     * @return the list of request processing rules
     */
    public List<RequestProcessingRule> getRequestProcessingRules()
    {
        final List<RequestProcessingRule> requestProcessingRules = new ArrayList<>();

        final Set<Integer> requestMergerNumbers = new TreeSet<>();
        final Set<String> requestMergerNumberStrings = getPropertyKeyFragment(PROP_REQUEST_MERGE_RULES_PREFIX);
        for (final String s : requestMergerNumberStrings)
        {
            checkForLeadingZeros(s);
            requestMergerNumbers.add(Integer.parseInt(s));
        }

        boolean invalidRulePresent = false;
        for (final int i : requestMergerNumbers)
        {
            final String basePropertyName = PROP_REQUEST_MERGE_RULES_PREFIX + i;

            // general stuff
            final String newName = getStringProperty(basePropertyName + ".newName", "");
            final boolean stopOnMatch = getBooleanProperty(basePropertyName + ".stopOnMatch", true);
            final boolean dropOnMatch = getBooleanProperty(basePropertyName + ".dropOnMatch", false);

            // include patterns
            final String urlPattern = getStringProperty(basePropertyName + ".urlPattern", "");
            final String contentTypePattern = getStringProperty(basePropertyName + ".contentTypePattern", "");
            final String statusCodePattern = getStringProperty(basePropertyName + ".statusCodePattern", "");
            final String requestNamePattern = getStringProperty(basePropertyName + ".namePattern", "");
            final String agentNamePattern = getStringProperty(basePropertyName + ".agentPattern", "");
            final String transactionNamePattern = getStringProperty(basePropertyName + ".transactionPattern", "");
            final String methodPattern = getStringProperty(basePropertyName + ".methodPattern", "");
            final String responseTimes = getStringProperty(basePropertyName + ".runTimeRanges", "");

            // exclude patterns
            final String urlExcludePattern = getStringProperty(basePropertyName + ".urlPattern.exclude", "");
            final String contentTypeExcludePattern = getStringProperty(basePropertyName + ".contentTypePattern.exclude", "");
            final String statusCodeExcludePattern = getStringProperty(basePropertyName + ".statusCodePattern.exclude", "");
            final String requestNameExcludePattern = getStringProperty(basePropertyName + ".namePattern.exclude", "");
            final String agentNameExcludePattern = getStringProperty(basePropertyName + ".agentPattern.exclude", "");
            final String transactionNameExcludePattern = getStringProperty(basePropertyName + ".transactionPattern.exclude", "");
            final String methodExcludePattern = getStringProperty(basePropertyName + ".methodPattern.exclude", "");

            // ensure that either newName or dropOnMatch is set
            if (StringUtils.isNotBlank(newName) == dropOnMatch)
            {
                throw new RuntimeException(String.format("Either specify property '%s' or set property '%s' to true",
                                                         basePropertyName + ".newName", basePropertyName + ".dropOnMatch"));
            }

            // ensure that dropOnMatch and stopOnMatch are not contradicting
            if (dropOnMatch && !stopOnMatch)
            {
                throw new RuntimeException(String.format("If property '%s' is true, property '%s' cannot be false",
                                                         basePropertyName + ".dropOnMatch", basePropertyName + ".stopOnMatch"));
            }

            // create and validate the rules
            try
            {
                final RequestProcessingRule mergeRule = new RequestProcessingRule(newName, requestNamePattern, urlPattern,
                                                                                  contentTypePattern, statusCodePattern, agentNamePattern,
                                                                                  transactionNamePattern, methodPattern, responseTimes,
                                                                                  stopOnMatch, requestNameExcludePattern, urlExcludePattern,
                                                                                  contentTypeExcludePattern, statusCodeExcludePattern,
                                                                                  agentNameExcludePattern, transactionNameExcludePattern,
                                                                                  methodExcludePattern, dropOnMatch);
                requestProcessingRules.add(mergeRule);
            }
            catch (final InvalidRequestProcessingRuleException imre)
            {
                // Log it and continue with next rule.
                final String errMsg = "Request processing rule '" + basePropertyName + "' is invalid. " + imre.getMessage();
                XltLogger.reportLogger.error(errMsg, imre);
                System.err.println(errMsg);
                // remember that we encountered an invalid merge rule
                invalidRulePresent = true;
            }
        }

        if (invalidRulePresent)
        {
            throw new RuntimeException("Please check your configuration. At least one request processing rule is invalid and needs to be fixed.");
        }

        return requestProcessingRules;
    }

    /**
     * Reads the Apdex thresholds (per action group) from the configurations.
     */
    private void readApdexThresholds()
    {
        final String PROP_APDEX_PREFIX = PROP_PREFIX + "apdex.";
        final String ACTION_GROUP_DEFAULT = "default";

        // get all action group names, except the default group that is handled separately
        final Set<String> actionGroups = getPropertyKeyFragment(PROP_APDEX_PREFIX);
        actionGroups.remove(ACTION_GROUP_DEFAULT);

        // get the threshold value of the default action group
        final String defaultThresholdPropertyName = PROP_APDEX_PREFIX + ACTION_GROUP_DEFAULT + ".threshold";
        defaultApdexThreshold = getDoubleProperty(defaultThresholdPropertyName, 4.0);
        validateApdexThreshold(defaultApdexThreshold, defaultThresholdPropertyName);

        // get the threshold value per action group
        for (final String actionGroup : actionGroups)
        {
            final String thresholdPropertyName = PROP_APDEX_PREFIX + actionGroup + ".threshold";
            final double threshold = getDoubleProperty(thresholdPropertyName, Double.NaN);
            final String actionsPropertyName = PROP_APDEX_PREFIX + actionGroup + ".actions";
            final String actionNamePattern = getStringProperty(actionsPropertyName, "");

            // check if both threshold and actions were defined
            if (!Double.isNaN(threshold) && StringUtils.isNotBlank(actionNamePattern))
            {
                // check if the threshold is valid
                validateApdexThreshold(threshold, thresholdPropertyName);

                // create/validate the pattern
                final Pattern pattern = compileRegEx(actionNamePattern, actionsPropertyName);

                // store the pattern together with the threshold
                apdexThresholdsByActionNamePattern.put(pattern, threshold);
            }
        }
    }

    /**
     * Validates that the given Apdex threshold value is greater than 0.
     *
     * @param threshold
     *            the threshold value to check
     * @param propertyName
     *            the name of the corresponding property
     */
    private void validateApdexThreshold(final double threshold, final String propertyName)
    {
        if (threshold <= 0.0)
        {
            throw new XltException(String.format("The value '%f' of property '%s' must be greater than 0.0", threshold, propertyName));
        }
    }

    /**
     * Creates a pattern from the given regular expression, implicitly validating that the pattern is valid.
     *
     * @param regEx
     *            the regular expression
     * @param propertyName
     *            the name of the corresponding property
     */
    private Pattern compileRegEx(final String regEx, final String propertyName)
    {
        try
        {
            return RegExUtils.getPattern(regEx);
        }
        catch (Exception ex)
        {
            throw new XltException(String.format("The value '%s' of property '%s' is not a valid regular expression:\n%s", regEx,
                                                 propertyName, ex.getMessage()));
        }
    }
}
