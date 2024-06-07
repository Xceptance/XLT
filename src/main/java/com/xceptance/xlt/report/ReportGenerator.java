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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;

import com.xceptance.common.util.Console;
import com.xceptance.common.util.ProductInformation;
import com.xceptance.xlt.api.report.ReportProvider;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.XltEngine;
import com.xceptance.xlt.engine.util.TimerUtils;
import com.xceptance.xlt.mastercontroller.TestCaseLoadProfileConfiguration;
import com.xceptance.xlt.mastercontroller.TestLoadProfileConfiguration;
import com.xceptance.xlt.report.evaluation.Evaluation;
import com.xceptance.xlt.report.evaluation.Evaluator;
import com.xceptance.xlt.report.external.ExternalReportGenerator;
import com.xceptance.xlt.report.util.ConcurrentUsersTable;
import com.xceptance.xlt.report.util.JFreeChartUtils;
import com.xceptance.xlt.report.util.ReportUtils;
import com.xceptance.xlt.report.util.TaskManager;
import com.xceptance.xlt.util.Timer;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 *
 */
public class ReportGenerator
{
    private final ReportGeneratorConfiguration config;

    private final FileObject inputDir;

    private final File outputDir;

    private final List<ReportProvider> reportProviders;

    private final ExternalReportGenerator repGen;

    private final String testCaseIncludePatternList;

    private final String testCaseExcludePatternList;

    private final String agentIncludePatternList;

    private final String agentExcludePatternList;

    /**
     * @param inputDir
     * @param outputDir
     * @param noCharts
     * @throws Exception
     */
    public ReportGenerator(final FileObject inputDir, final File outputDir, final boolean noCharts) throws Exception
    {
        this(inputDir, outputDir, noCharts, false, null, null, null, null, null, null);
    }

    /**
     * @param inputDir
     * @param outputDir
     * @param noCharts
     * @param noAgentCharts
     * @param overridePropertyFile
     * @param commandLineProperties
     * @param testCaseIncludePatternList
     *            a comma-separated list of reg-ex patterns that match the test cases to be included in the report
     * @param testCaseExcludePatternList
     *            a comma-separated list of reg-ex patterns that match the test cases to be excluded from the report
     * @throws Exception
     */
    public ReportGenerator(final FileObject inputDir, final File outputDir, final boolean noCharts, boolean noAgentCharts,
                           final File overridePropertyFile, final Properties commandLineProperties, final String testCaseIncludePatternList,
                           final String testCaseExcludePatternList, final String agentIncludePatternList,
                           final String agentExcludePatternList)
                               throws Exception
    {

        final FileObject configDir = inputDir.resolveFile(XltConstants.CONFIG_DIR_NAME);

        XltPropertiesImpl props;
        try
        {
            props = new XltPropertiesImpl(inputDir, configDir, false, false);

            // make that known to the entire process
            XltEngine.reset(props);
        }
        catch (Exception e)
        {
            XltLogger.reportLogger.warn("One or more configuration files seem to be missing or corrupt! Check log trace.");
            props = new XltPropertiesImpl(inputDir, configDir, false, true);
        }

        config = new ReportGeneratorConfiguration(props.getProperties(), overridePropertyFile, commandLineProperties);

        this.testCaseIncludePatternList = testCaseIncludePatternList;
        this.testCaseExcludePatternList = testCaseExcludePatternList;
        this.agentIncludePatternList = agentIncludePatternList;
        this.agentExcludePatternList = agentExcludePatternList;

        // charts
        if (noCharts)
        {
            config.disableChartsGeneration();
        }

        if (noAgentCharts)
        {
            config.disableAgentCharts();
        }

        // results directory
        this.inputDir = inputDir;
        config.setResultsDirectory(this.inputDir);

        // results directory name
        final String resultsDirectoryName = getResultsDirName(inputDir);
        config.setResultsDirectoryName(resultsDirectoryName);

        // report directory
        if (outputDir == null)
        {
            this.outputDir = new File(config.getTestReportsRootDirectory(), resultsDirectoryName);
        }
        else
        {
            this.outputDir = outputDir;
        }

        config.setReportDirectory(this.outputDir);

        // configure the thread pool to be cpu count for now
        TaskManager.getInstance().setMaximumThreadCount(Runtime.getRuntime().availableProcessors());

        // configure the Webp encoder
        JFreeChartUtils.setWebpCompressionFactor(config.getChartCompressionFactor());

        // setup the report providers
        reportProviders = new ArrayList<ReportProvider>();

        repGen = new com.xceptance.xlt.report.external.ExternalReportGenerator();
    }

    /**
     * Ensure that output exists and is empty. We have that public here because we need it twice due to either
     * the dir coming in from external or is determining it when creating the report. Not really nice but
     * legacy.
     *
     * @throws IOException
     */
    public static void ensureOutputDirAndClean(final File dir) throws IOException
    {
        /*
         * Make sure that we can safely write to output directory BEFORE reading any input data.
         */
        if (dir.exists() == false)
        {
            XltLogger.reportLogger.info("Creating output directory: {}", dir);
            FileUtils.forceMkdir(dir);
        }
        else
        {
            // clean output directory first -> Improvement #3243
            XltLogger.reportLogger.info("Cleaning output directory: {}", dir);
            FileUtils.cleanDirectory(dir);
        }
    }

    /**
     * Generates the full HTML load test report from the raw load test results. This includes
     * <ol>
     * <li>reading the raw data from disk,</li>
     * <li>processing the raw data,</li>
     * <li>creating the XML report, and</li>
     * <li>transforming the XML report to HTML files.</li>
     * </ol>
     *
     * @param noRampUp
     *            whether or not to exclude ramp-up period from report
     * @throws Exception
     *             if anything goes wrong during report creation
     */
    public void generateReport(final boolean noRampUp) throws Exception
    {
        generateReport(0, Long.MAX_VALUE, -1, noRampUp, false, false);
    }

    /**
     * Generates the full HTML load test report from the raw load test results. This includes
     * <ol>
     * <li>reading the raw data from disk,</li>
     * <li>processing the raw data,</li>
     * <li>creating the XML report, and</li>
     * <li>transforming the XML report to HTML files.</li>
     * </ol>
     *
     * @param fromTime
     *            start time in seconds
     * @param toTime
     *            end time in seconds
     * @param duration
     *            duration in milliseconds
     * @param noRampUp
     *            whether or not to exclude ramp-up period from report
     * @param fromTimeRel
     *            specifies whether or not 'from time' is a relative time value
     * @param toTimeRel
     *            specifies whether or not 'to time' is a relative time value
     * @throws Exception
     *             if anything goes wrong during report creation
     */
    public void generateReport(final long fromTime, final long toTime, final long duration, final boolean noRampUp,
                               final boolean fromTimeRel, final boolean toTimeRel)
                                   throws Exception
    {
        try
        {
            // clean/create output directory first
            ensureOutputDirAndClean(this.outputDir);

            for (final Class<? extends ReportProvider> c : config.getReportProviderClasses())
            {
                try
                {
                    final ReportProvider processor = c.getDeclaredConstructor().newInstance();
                    processor.setConfiguration(config);

                    reportProviders.add(processor);
                }
                catch (final Throwable t)
                {
                    XltLogger.reportLogger.error("Failed to instantiate and initialize report provider instance of class '{}'", c.getCanonicalName(), t);
                }
            }

            // read all log files and crunch the data
            readLogs(fromTime, toTime, duration, noRampUp, fromTimeRel, toTimeRel);

            // create the xml output and the charts
            final File xmlReport = createReport(outputDir);

            // drop providers
            reportProviders.clear();

            // evaluate test if desired
            final File evaluationXml = evaluateReport(xmlReport);

            // create the html report
            transformReport(xmlReport, outputDir, evaluationXml != null);

            // create the evaluation HTML report (if evaluation took place)
            if (evaluationXml != null)
            {
                transformEvaluation(evaluationXml);
            }

            // output the path to the report either as file path (Win) or as clickable file URL
            final File reportFile = new File(outputDir, "index.html");
            final String reportPath = ReportUtils.toString(reportFile);

            XltLogger.reportLogger.info("Report: {}", reportPath);
        }
        finally
        {
            ConcurrentUsersTable.getInstance().clear();
        }
    }

    /**
     * Reads the raw load test result data from disk and processes and stores it in memory.
     *
     * @param fromTime
     * @param toTime
     * @param duration
     * @param noRampUp
     * @param fromTimeRel
     * @param toTimeRel
     */
    public void readLogs(long fromTime, long toTime, final long duration, final boolean noRampUp, final boolean fromTimeRel,
                         final boolean toTimeRel)
    {
        XltLogger.reportLogger.info(Console.horizontalBar());
        XltLogger.reportLogger.info(Console.startSection("Reading Log Files..."));
        final long testStartTime = config.getLongProperty(XltConstants.LOAD_TEST_START_DATE, 0);
        final long elapsedTime = config.getLongProperty(XltConstants.LOAD_TEST_ELAPSED_TIME, 0);

        // convert to absolute timestamp
        final long[] timeBoundaries = getTimeBoundaries(fromTime, toTime, duration, noRampUp, fromTimeRel, toTimeRel, testStartTime,
                                                        elapsedTime);
        fromTime = timeBoundaries[0];
        toTime = timeBoundaries[1];

        printStartAndEndTime(fromTime, toTime);
        if (toTime <= fromTime)
        {
            throw new IllegalArgumentException("Specified start must not be after specified end.");
        }

        read(fromTime, toTime);
        XltLogger.reportLogger.info(Console.endSection());
    }
    
    private long[] getTimeBoundaries(long fromTime, long toTime, final long duration, final boolean noRampUp, final boolean fromTimeRel,
                                     final boolean toTimeRel, final long testStartDate, final long elapsedTime)
    {
        // recalculate 'from time' if it is set as a relative offset value
        if (fromTimeRel)
        {
            fromTime = recalculateOffsetTimeValue(fromTime, testStartDate, elapsedTime);
        }

        // recalculate 'to time' if it is set as an offset value
        if (toTimeRel)
        {
            toTime = recalculateOffsetTimeValue(toTime, testStartDate, elapsedTime);
            // set 'to time' to max value, if the offset could not be used
            if (toTime == 0)
            {
                toTime = Long.MAX_VALUE;
            }
        }

        // recalculate time values, if a duration is set
        if (duration >= 0)
        {
            // value 'to time' was not set
            if (toTime == Long.MAX_VALUE)
            {
                toTime = fromTime + duration;
            }
            // value 'from time' was not set
            else
            {
                fromTime = toTime - duration;
            }
        }

        // exclude ramp-up time if necessary
        if (noRampUp)
        {
            fromTime = excludeRampup(fromTime);
        }

        final long[] boundaries =
            {
                fromTime, toTime
            };

        return boundaries;
    }

    private long excludeRampup(long fromTime)
    {
        // earliest execution date of all users
        final long startTime = config.getLongProperty(XltConstants.LOAD_TEST_START_DATE, 0);
        if (startTime > 0)
        {
            // get load profile
            final File configDir = new File(inputDir.getName().getPath(), XltConstants.CONFIG_DIR_NAME);

            final XltPropertiesImpl properties = TestLoadProfileConfiguration.readProperties(configDir.getParentFile(), configDir);
            final TestLoadProfileConfiguration loadProfileConfig = new TestLoadProfileConfiguration(properties);

            final long endOfRampUpTime = startTime + computeRampUpOffset(loadProfileConfig.getLoadTestConfiguration()) * 1000L;

            // determine what time is more recent: end of ramp-up or given 'from'
            fromTime = Math.max(fromTime, endOfRampUpTime);
        }
        else
        {
            XltLogger.reportLogger.warn("PLEASE NOTE: Ramp-up could not be excluded since no value could be found for property '{}'.",
                                                       XltConstants.LOAD_TEST_START_DATE);
        }

        return fromTime;
    }

    /**
     * Processing of the log files within a defined time range
     *
     * @param fromTime start time of the period to report
     * @param toTime end time of the period to report
     */
    private void read(final long fromTime, final long toTime)
    {
        // setup data record factory
        final DataRecordFactory dataRecordFactory = new DataRecordFactory(config.getDataRecordClasses());

        // read the logs
        final DataProcessor logReader = new DataProcessor(config,
                                                          inputDir,
                                                          dataRecordFactory,
                                                          fromTime, toTime,
                                                          reportProviders,
                                                          testCaseIncludePatternList, testCaseExcludePatternList,
                                                          agentIncludePatternList, agentExcludePatternList);
        logReader.readDataRecords();

        XltLogger.reportLogger.info(Console.endSection());

        final long minTime = logReader.getMinimumTime();
        final long maxTime = logReader.getMaximumTime();

        config.setChartStartTime(minTime);
        config.setChartEndTime(maxTime);

        // external data
        {
            try
            {
                XltLogger.reportLogger.info(Console.horizontalBar());
                XltLogger.reportLogger.info(Console.startSection("Processing External Data Files..."));

                final Timer timer = Timer.start();

                final File externalChartsDir = new File(config.getChartDirectory(), "external");
                externalChartsDir.mkdirs();
                repGen.init(minTime, maxTime, inputDir.getName().getPath(), externalChartsDir, config.shouldChartsGenerated());
                repGen.parse();
                XltLogger.reportLogger.info(timer.stop().get("...finished"));
            }
            catch (final Exception e)
            {
                XltLogger.reportLogger.error("Failed to process external data", e);
            }
        }
    }

    /**
     * Adds the given offset to the load test start date, if the offset is greater than 0. Otherwise, the negative
     * offset will be subtracted from the load test end time.
     *
     * @param offsetTimeValue
     * @return the recalculated time value
     */
    private long recalculateOffsetTimeValue(final long offsetTimeValue, final long testStartDate, final long elapsedTime)
    {
        long timeValue = 0;

        final long endTime = testStartDate + elapsedTime;

        // negative offset
        if (offsetTimeValue < 0)
        {
            if (testStartDate == 0 || elapsedTime == 0)
            {
                XltLogger.reportLogger.warn("PLEASE NOTE: The specified offset '{}' could not be used since no value could be found for properties '{}' and '{}'",
                                            offsetTimeValue, XltConstants.LOAD_TEST_START_DATE, XltConstants.LOAD_TEST_ELAPSED_TIME);
            }
            else
            {
                timeValue = endTime + offsetTimeValue;
            }
        }
        // positive offset
        else
        {
            if (testStartDate == 0)
            {
                XltLogger.reportLogger.warn("PLEASE NOTE: The specified offset '{}' could not be used since no value could be found for property '{}'",
                                            offsetTimeValue, XltConstants.LOAD_TEST_START_DATE);
            }
            else
            {
                timeValue = testStartDate + offsetTimeValue;
            }
        }
        return timeValue;
    }

    /**
     * Prints the given start and end time the test report will be based on.
     *
     * @param fromTime
     *            the start time
     * @param toTime
     *            the end time
     */
    private void printStartAndEndTime(final long fromTime, final long toTime)
    {
        // only 'from' parameter is set
        if (fromTime > 0 && toTime == Long.MAX_VALUE)
        {
            XltLogger.reportLogger.info("Data start: {}", new Date(fromTime));
        }
        // only 'to' parameter is set
        else if (fromTime == 0 && toTime != Long.MAX_VALUE)
        {
            XltLogger.reportLogger.info("Data end: {}", new Date(toTime));
        }
        // both parameter are set
        else if (fromTime > 0 && toTime != Long.MAX_VALUE)
        {
            XltLogger.reportLogger.info("Data start: {}", new Date(fromTime));
            XltLogger.reportLogger.info("Data end  : {}", new Date(toTime));
        }
    }

    /**
     * Creates the XML report from the internally stored data.
     *
     * @param outputDir
     *            the target directory
     * @throws Exception
     *             if anything goes wrong during report creation
     */
    public File createReport(final File outputDir) throws Exception
    {
        XltLogger.reportLogger.info(Console.horizontalBar());
        XltLogger.reportLogger.info(Console.startSection("Creating Artifacts..."));
        copyConfiguration(outputDir);

        // create the report generator
        final XmlReportGenerator xmlReportGenerator = new XmlReportGenerator();

        // setup the report providers
        xmlReportGenerator.registerStatisticsProviders(reportProviders);

        // external
        {
            xmlReportGenerator.registerStatisticsProviders(repGen.getReportCreators());
        }

        // create the report
        final long start = TimerUtils.get().getStartTime();

        try
        {
            TaskManager.getInstance().startProgress("Creating");
            final File xmlReport = new File(outputDir, XltConstants.LOAD_REPORT_XML_FILENAME);
            xmlReportGenerator.createReport(xmlReport);

            return xmlReport;
        }
        finally
        {
            // wait for any asynchronous task to complete (e.g. chart generation)
            TaskManager.getInstance().waitForAllTasksToComplete();

            TaskManager.getInstance().stopProgress();

            XltLogger.reportLogger.info("...finished - {} ms", TimerUtils.get().getElapsedTime(start));
            XltLogger.reportLogger.info(Console.endSection());
        }
    }

    /**
     * Copies the configuration files.
     * <p>
     * Due to ticket #1650 it was necessary to store configuration files in an own config folder in the results. However
     * results generated with previous versions of XLT do not have such a folder (or only by coincidence but with
     * different content).
     * </p>
     * Thus this method copies all files as it had been previously when there is no config folder in the results
     * otherwise it copies recursively that folder.
     *
     * @param outputDir
     *            the report directory
     * @throws FileSystemException
     */
    private void copyConfiguration(final File outputDir) throws FileSystemException
    {
        final FileObject reportConfigDir = VFS.getManager().resolveFile(outputDir, XltConstants.CONFIG_DIR_NAME);
        FileObject resultsConfigDir = inputDir.getChild("config");

        // results generated by XLT 4.3.x and later are only accepted
        // accept all files/directories in the results configuration directory

        // copy the configuration files to the report's configuration directory
        if (resultsConfigDir != null)
        {
            try
            {
                reportConfigDir.copyFrom(resultsConfigDir, Selectors.SELECT_ALL);
            }
            catch (FileSystemException e)
            {
                XltLogger.reportLogger.error("Issue while copying original properties from {}", reportConfigDir.getPublicURIString(), e);
            }
        }
        else
        {
            XltLogger.reportLogger.warn("There is not config directory with property files avaialble. Skipping.");
        }

        // we not longer coping data from outside of the home dir, because it might override another similar file
        // you can still use these propery files in the home of the test suite, but we are not preserving them
    }

    /**
     * Transforms the given input XML file to HTML files according to the configured transformation rules.
     *
     * @param inputXmlFile
     *            the input XML file
     * @param outputDir
     *            the target directory
     * @throws Exception
     *             if anything goes wrong during transformation
     */
    public void transformReport(final File inputXmlFile, final File outputDir, final boolean evaluationPresent) throws Exception
    {
        XltLogger.reportLogger.info(Console.horizontalBar());
        XltLogger.reportLogger.info(Console.startSection("Creating HTML Report..."));

        // we did this before already... mmn....
        FileUtils.forceMkdir(outputDir);

        // copy the report's static resources
        final File resourcesDir = new File(config.getConfigDirectory(), XltConstants.REPORT_RESOURCES_PATH);
        FileUtils.copyDirectory(resourcesDir, outputDir, FileFilterUtils.makeSVNAware(null), false);

        // get the configured output and style sheet file names
        final List<File> outputFiles = new ArrayList<File>();
        final List<File> styleSheetFiles = new ArrayList<File>();

        // create the files from the file names
        final List<String> styleSheetFileNames = config.getStyleSheetFileNames();
        final List<String> outputFileNames = config.getOutputFileNames();

        for (int i = 0; i < styleSheetFileNames.size(); i++)
        {
            final File outputFile = new File(outputDir, outputFileNames.get(i));
            outputFiles.add(outputFile);

            final File styleSheetFile = new File(new File(config.getConfigDirectory(), XltConstants.LOAD_REPORT_XSL_PATH),
                                                 styleSheetFileNames.get(i));
            styleSheetFiles.add(styleSheetFile);
        }

        // create some dynamic parameters
        final HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("productName", ProductInformation.getProductInformation().getProductName());
        parameters.put("productVersion", ProductInformation.getProductInformation().getVersion());
        parameters.put("productUrl", ProductInformation.getProductInformation().getProductURL());
        parameters.put("evaluationPresent", Boolean.valueOf(evaluationPresent));

        // transform the report
        final ReportTransformer reportTransformer = new ReportTransformer(outputFiles, styleSheetFiles, parameters);

        final long start = TimerUtils.get().getStartTime();

        try
        {
            XltLogger.reportLogger.info("XML data file: {}", inputXmlFile);

            // ok, we want to avoid high memory usage
            TaskManager.getInstance().setMaximumThreadCount(1);

            TaskManager.getInstance().startProgress("Creating");
            reportTransformer.run(inputXmlFile, outputDir);

        }
        finally
        {
            // wait for any asynchronous task to complete
            TaskManager.getInstance().waitForAllTasksToComplete();
            TaskManager.getInstance().stopProgress();

            XltLogger.reportLogger.info("...finished - {} ms", TimerUtils.get().getElapsedTime(start));
            XltLogger.reportLogger.info(Console.endSection());
        }
    }


    public void updateEvaluation() throws Exception
    {
        try
        {
            inputDir.resolveFile(XltConstants.LOAD_REPORT_XML_FILENAME, NameScope.CHILD);
        }
        catch(FileSystemException fse)
        {
            XltLogger.reportLogger.error("Could not find '{}' in directory '{}'.", XltConstants.LOAD_REPORT_XML_FILENAME, inputDir.getName().getPathDecoded());
            return;
        }
        
        
        final FileObject targetDir = VFS.getManager().toFileObject(outputDir);
        if(inputDir.getName() != targetDir.getName())
        {
            ensureOutputDirAndClean(outputDir);

            targetDir.copyFrom(inputDir, Selectors.SELECT_ALL);
        }
        
        final File xmlReport = new File(outputDir, XltConstants.LOAD_REPORT_XML_FILENAME);
        
        // evaluate test if desired
        final File evaluationXml = evaluateReport(xmlReport);

        // create the evaluation HTML report (if evaluation took place)
        if (evaluationXml != null)
        {
            transformEvaluation(evaluationXml);
        }

        // output the path to the report either as file path (Win) or as clickable file URL
        final File reportFile = new File(outputDir, "index.html");
        final String reportPath = ReportUtils.toString(reportFile);

        XltLogger.reportLogger.info("Report: {}", reportPath);
    }

    
    /**
     * Derives a directory name from the given input directory/archive file.
     *
     * @param input
     *            the input directory or archive file
     * @return the directory name
     */
    private String getResultsDirName(final FileObject input)
    {
        final String inputDirName;

        final FileName inputName = input.getName();
        final boolean isPlainDirectory = inputName.getScheme().equals("file");

        if (isPlainDirectory)
        {
            // example: file:///d:/workspace/xlt-4.2.x/results/20121106-111751 -> 20121106-111751

            // use the name of the results directory
            inputDirName = inputName.getBaseName();
        }
        else
        {
            // must be a results archive

            // check if there is a results directory in the archive
            if (inputName.getPath().equals("/"))
            {
                // no, use the name of the archive, but exclude the file extension
                // example: zip:file:///d:/workspace/xlt-4.2.x/results/res.zip!/ -> res

                final String archivePathName = StringUtils.substringBefore(inputName.getRoot().toString(), "!");
                inputDirName = FilenameUtils.getBaseName(archivePathName);
            }
            else
            {
                // yes, use the name of the results directory inside the archive
                // example: zip:file:///d:/workspace/xlt-4.2.x/results/res.zip!/20121106-111751 -> 20121106-111751

                inputDirName = inputName.getBaseName();
            }
        }

        return inputDirName;
    }

    /**
     * Evaluates the given test report XML file and writes its outcome to file named
     * {@value XltConstants#EVALUATION_REPORT_XML_FILENAME}. N.B. No evaluation is done if there is no evaluation
     * configuration file configured.
     *
     * @param reportXMLFile
     *            the test report XML to evaluate
     * @return the evaluation report XML file if evaluation took place, or {@code null} otherwise.
     */
    private File evaluateReport(final File reportXMLFile)
    {
        final String evaluationConfig = config.getStringProperty(XltConstants.EVALUATION_CONFIG_FILE_PROPERTY, null);
        final File evaluationConfigFile = evaluationConfig != null ? new File(new File(outputDir, XltConstants.CONFIG_DIR_NAME),
                                                                              evaluationConfig)
                                                                   : null;
        if (evaluationConfigFile != null)
        {
            XltLogger.reportLogger.debug("Evaluating test report using configuration file '{}'", evaluationConfigFile.getAbsolutePath());

            final String errMessage = "Failed to evaluate test report";
            try
            {
                final File evaluationXMLFile = new File(outputDir, XltConstants.EVALUATION_REPORT_XML_FILENAME);
                final Evaluator evaluator = new Evaluator(evaluationConfigFile);
                final Evaluation outcome = evaluator.evaluate(reportXMLFile);

                final String error = outcome.result.getError();
                if (StringUtils.isNotBlank(error))
                {
                    XltLogger.reportLogger.error("{}: {}" , errMessage, error);
                }

                evaluator.writeEvaluationToFile(outcome, evaluationXMLFile);

                return evaluationXMLFile;
            }
            catch (final Throwable t)
            {
                XltLogger.reportLogger.error(errMessage, t);
            }
        }

        return null;
    }

    /**
     * Transform the given evaluation report XML file using the XSL stylesheet
     * {@value XltConstants#EVALUATION_REPORT_XSL_FILENAME} that is expected to reside in report generator's
     * configuration sub-directory {@value XltConstants#EVALUATION_REPORT_XSL_PATH}
     *
     * @param inputXmlFile
     *            the evaluation report XML to transform
     * @throws Exception
     *             thrown when transformation failed for any reason
     */
    private void transformEvaluation(final File inputXmlFile) throws Exception
    {
        XltLogger.reportLogger.info(Console.horizontalBar());
        XltLogger.reportLogger.info(Console.startSection("Creating Evaluation Report..."));

        final File styleSheetFile = new File(new File(config.getConfigDirectory(), XltConstants.EVALUATION_REPORT_XSL_PATH),
                                             XltConstants.EVALUATION_REPORT_XSL_FILENAME);
        final File outputFile = new File(outputDir, XltConstants.EVALUATION_REPORT_HTML_FILENAME);

        // determine the name of the project from configuration
        final String projectName;
        {
            final String projectNamePropValue = config.getStringProperty(XltConstants.PROJECT_NAME_PROPERTY, null);
            projectName = StringUtils.trimToEmpty(projectNamePropValue);
        }

        // XTC specific parameters
        final String organization, project, loadTestId, resultId, reportId;
        {
            final String organizationPropValue = config.getStringProperty("com.xceptance.xtc.organization", null);
            organization = StringUtils.trimToEmpty(organizationPropValue);

            final String projectPropValue = config.getStringProperty("com.xceptance.xtc.project", null);
            project = StringUtils.trimToEmpty(projectPropValue);

            final String loadTestIdPropValue = config.getStringProperty("com.xceptance.xtc.loadtest.run.id", null);
            loadTestId = StringUtils.trimToEmpty(loadTestIdPropValue);

            final String resultIdPropValue = config.getStringProperty("com.xceptance.xtc.loadtest.result.id", null);
            resultId = StringUtils.trimToEmpty(resultIdPropValue);

            final String reportIdPropValue = config.getStringProperty("com.xceptance.xtc.loadtest.report.id", null);
            reportId = StringUtils.trimToEmpty(reportIdPropValue);
        }

        // create some dynamic parameters
        final Map<String, Object> parameters = Map.of("productName", ProductInformation.getProductInformation().getProductName(),
                                                      "productVersion", ProductInformation.getProductInformation().getVersion(),
                                                      "productUrl", ProductInformation.getProductInformation().getProductURL(),
                                                      "projectName", projectName, "evaluationPresent", Boolean.TRUE, "xtcOrganization",
                                                      organization, "xtcProject", project, "xtcLoadTestId", loadTestId, "xtcResultId",
                                                      resultId, "xtcReportId", reportId);

        // transform the report
        final ReportTransformer reportTransformer = new ReportTransformer(List.of(outputFile), List.of(styleSheetFile), parameters);

        final long start = TimerUtils.get().getStartTime();

        try
        {

            // ok, we want to avoid high memory usage
            TaskManager.getInstance().setMaximumThreadCount(1);

            TaskManager.getInstance().startProgress("Creating");
            reportTransformer.run(inputXmlFile, outputDir);

        }
        finally
        {
            // wait for any asynchronous task to complete
            TaskManager.getInstance().waitForAllTasksToComplete();
            TaskManager.getInstance().stopProgress();

            XltLogger.reportLogger.info("...finished - {} ms", TimerUtils.get().getElapsedTime(start));
            XltLogger.reportLogger.info(Console.endSection());
        }
    }

    // TODO: Check if the similar method {@link TestLoadProfileConfiguration#getTotalRampUpPeriod()} is still needed.
    static long computeRampUpOffset(final List<TestCaseLoadProfileConfiguration> profiles)
    {
        // determine highest offset from the start time when all tests have completed their ramp-up
        long maxRampUpOffset = 0L;
        long smallestInitialDelay = Long.MAX_VALUE;
        for (final TestCaseLoadProfileConfiguration profile : profiles)
        {
            // initial delay + ramp-up is offset
            final int initialDelay = profile.getInitialDelay();
            final int rampUpPeriod = profile.getRampUpPeriod();
            if (rampUpPeriod > 0)
            {
                maxRampUpOffset = Math.max(maxRampUpOffset, initialDelay + rampUpPeriod);
            }
            smallestInitialDelay = Math.min(smallestInitialDelay, initialDelay);
        }

        return Math.max(0, maxRampUpOffset - smallestInitialDelay);
    }
}
