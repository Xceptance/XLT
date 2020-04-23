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
package com.xceptance.xlt.report.trendreport;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.util.ShapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.xceptance.common.util.ProcessExitCodes;
import com.xceptance.common.util.ProductInformation;
import com.xceptance.common.xml.DomUtils;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.report.ReportTransformer;
import com.xceptance.xlt.report.util.CategoryItemRenderer;
import com.xceptance.xlt.report.util.ElementSpecification;
import com.xceptance.xlt.report.util.JFreeChartUtils;
import com.xceptance.xlt.report.util.NoOverlapCategoryAxis;
import com.xceptance.xlt.report.util.ReportUtils;
import com.xceptance.xlt.report.util.TaskManager;

/**
 * 
 */
public class TrendReportGeneratorMain
{
    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(TrendReportGeneratorMain.class);

    /**
     * The trend report generator's main method.
     * 
     * @param args
     *            the command-line arguments
     */
    public static void main(final String[] args)
    {
        Locale.setDefault(Locale.US);

        final TrendReportGeneratorMain main = new TrendReportGeneratorMain();

        main.run(args);
    }

    /**
     * The configuration as read from "trendreportgenerator.properties".
     */
    private TrendReportGeneratorConfiguration config;

    /**
     * Disables charts generation.
     */
    private boolean noCharts;

    /**
     * Disables sorting the reports.
     */
    private boolean noSorting;

    /**
     * The initial trend value set, which contains dummy values for each test report.
     */
    private Set<TrendValue> initialTrendValues;

    /**
     * Creates a trend chart with basic settings applied.
     * 
     * @param chartTitle
     *            the chart title
     * @param valueAxisTitle
     *            the range axis title
     * @param dataset
     *            the data to show
     * @return the chart
     */
    private JFreeChart createBasicTrendChart(final String chartTitle, final String valueAxisTitle, final CategoryDataset dataset)
    {
        // set up the renderer
        // AreaRenderer renderer = new AreaRenderer();
        final LineAndShapeRenderer renderer = new CategoryItemRenderer();
        // renderer.setLegendLine(new Rectangle2D.Double(-7.0, 0.0, 14.0, 1.0));
        renderer.setSeriesPaint(0, new Color(0xAAAAAA));
        renderer.setSeriesPaint(1, Color.BLUE);
        renderer.setSeriesPaint(2, Color.MAGENTA);
        renderer.setSeriesPaint(3, new Color(0x00AA00));
        renderer.setSeriesShape(0, ShapeUtils.createDiamond(2));
        renderer.setSeriesShape(1, ShapeUtils.createDiamond(2));
        renderer.setSeriesShape(2, ShapeUtils.createDiamond(2));
        renderer.setSeriesShape(3, ShapeUtils.createDiamond(2));

        // set up the domain axis
        final CategoryAxis categoryAxis = new NoOverlapCategoryAxis("Report");
        categoryAxis.setCategoryMargin(0.0D);
        categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        categoryAxis.setMaximumCategoryLabelLines(3);
        categoryAxis.setTickMarksVisible(true);

        // set up the range axis
        final NumberAxis valueAxis = new NumberAxis(valueAxisTitle);
        valueAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        valueAxis.setAutoRangeIncludesZero(config.getChartAutoRangeIncludesZero());

        // set up the plot
        final CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
        plot.setDomainGridlinesVisible(true);
        // plot.setAxisOffset(new RectangleInsets(4.0D, 4.0D, 4.0D, 4.0D));
        // plot.setForegroundAlpha(0.75F);

        // finally create the chart
        final JFreeChart jfreechart = JFreeChartUtils.createChart(chartTitle, plot);

        return jfreechart;
    }

    /**
     * Creates and returns the command line options.
     * 
     * @return command line options
     */
    private Options createCommandLineOptions()
    {
        final Options options = new Options();

        final Option targetDir = new Option("o", true, "the trend report target directory");
        targetDir.setArgName("dir");
        options.addOption(targetDir);

        final Option noSorting = new Option("noSorting", false, "disable sorting the input reports by their test start time");
        options.addOption(noSorting);

        final Option noCharts = new Option(XltConstants.COMMANDLINE_OPTION_NO_CHARTS, false, "disables generation of charts");
        options.addOption(noCharts);

        return options;
    }

    /**
     * Creates a run time trend chart with the given title showing the specified data. The chart is stored as a PNG file
     * to the passed target directory.
     * 
     * @param timerName
     *            the chart title
     * @param dataset
     *            the data to show
     * @param outputDir
     *            the target directory
     * @param chartWidth
     *            the width of the chart (in pixels)
     * @param chartHeight
     *            the height of the chart (in pixels)
     */
    private void createErrorTrendChart(final String timerName, final CategoryDataset dataset, final File outputDir, final int chartWidth,
                                       final int chartHeight)
    {
        System.out.printf("Creating errors trend chart for timer '%s' ...\n", timerName);

        // create and modify the basic chart
        final JFreeChart jfreeChart = createBasicTrendChart(timerName, "Errors", dataset);
        jfreeChart.getCategoryPlot().getRenderer().setSeriesPaint(0, Color.RED);

        JFreeChartUtils.saveChart(jfreeChart, timerName + "_Errors", outputDir, chartWidth, chartHeight);
    }

    /**
     * Creates an initial trend value set, which contains dummy values for each test report.
     * 
     * @param directoryPathNames
     *            the test report directories
     * @throws ParseException
     * @throws IOException
     */
    private void createInitialTrendValues(final String[] directoryPathNames) throws ParseException, IOException
    {
        // remember absolute paths of all report directories to detect repetitions in user input
        final HashSet<String> reportDirs = new HashSet<String>();
        // remember test report names to compute a unique one
        final HashSet<String> reportNames = new HashSet<String>();
        for (final String directoryPathName : directoryPathNames)
        {
            final File reportDir = new File(directoryPathName);
            final String reportPath = reportDir.getAbsolutePath();
            if (reportDirs.contains(reportPath))
            {
                continue;
            }

            reportDirs.add(reportPath);
            // compute unique test report name
            String key = reportDir.getName();
            if (reportNames.contains(key))
            {
                key = computeNewKey(reportNames, key);
            }
            reportNames.add(key);
            // get test report date and comment
            Map<String, String> reportDateAndComment = getReportDateAndCommentFromFile(reportDir);
            // create a new trend value if test report data are available
            if (reportDateAndComment != null)
            {
                final Date reportDate = new SimpleDateFormat(XltConstants.REPORT_DATE_FORMAT,
                                                             Locale.ENGLISH).parse(reportDateAndComment.get("reportDate"));

                final String reportComment = reportDateAndComment.get("reportComment");
                // add a new trend value to the initial trend values
                initialTrendValues.add(new TrendValue(null, null, null, null, key, reportDate, reportComment, null, null));
            }
        }
    }

    /**
     * Returns the test report date and the test report comment.
     * 
     * @param dir
     *            the directory of the test report
     * @return the test report date and the test report comment
     * @throws IOException
     * @throws ParseException
     */
    private Map<String, String> getReportDateAndCommentFromFile(final File dir) throws IOException, ParseException
    {
        // the test report data
        final Map<String, String> testReportData = new HashMap<String, String>();
        // the test report XML file
        final File file = new File(dir, XltConstants.LOAD_REPORT_XML_FILENAME);

        if (file.isFile())
        {
            // the test report content
            String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            // get the test report date
            testReportData.put("reportDate",
                               fileContent.substring(fileContent.indexOf("<startTime>") + 11, fileContent.indexOf("</startTime>")));
            // get the test report comment
            testReportData.put("reportComment", parseCommentFromFileContent(fileContent));
            return testReportData;
        }
        else
        {
            log.warn("The specified directory '" + dir + "' does not seem to be a valid test report directory. Skipping it.");
            return null;
        }
    }

    /**
     * Returns the test report comment if it is set, otherwise {@code null}.
     * 
     * @param fileContent
     *            the string to get the test report comment from
     * @return
     */
    private String parseCommentFromFileContent(String fileContent)
    {
        // no comment was set
        if (fileContent.contains("<comments/>"))
        {
            return null;
        }
        String subStringComment = fileContent.substring(fileContent.indexOf("<comments>"), fileContent.indexOf("</comments>"));
        // get comment from sub string
        return subStringComment.substring(subStringComment.indexOf("<string>") + 8, subStringComment.indexOf("</string>"));
    }

    /**
     * Creates a run time trend chart with the given title showing the specified data. The chart is stored as a PNG file
     * to the passed target directory.
     * 
     * @param timerName
     *            the chart title
     * @param dataset
     *            the data to show
     * @param outputDir
     *            the target directory
     * @param chartWidth
     *            the width of the chart (in pixels)
     * @param chartHeight
     *            the height of the chart (in pixels)
     */
    private void createRunTimeTrendChart(final String timerName, final CategoryDataset dataset, final File outputDir, final int chartWidth,
                                         final int chartHeight)
    {
        System.out.printf("Creating run time trend chart for timer '%s' ...\n", timerName);

        // create the basic chart, it is sufficient for us
        final JFreeChart jfreeChart = createBasicTrendChart(timerName, "Run Time [ms]", dataset);

        JFreeChartUtils.saveChart(jfreeChart, timerName + "_RunTime", outputDir, chartWidth, chartHeight);
    }

    /**
     * Creates a throughput trend chart with the given title showing the specified data. The chart is stored as a PNG
     * file to the passed target directory.
     * 
     * @param timerName
     *            the chart title
     * @param dataset
     *            the data to show
     * @param outputDir
     *            the target directory
     * @param chartWidth
     *            the width of the chart (in pixels)
     * @param chartHeight
     *            the height of the chart (in pixels)
     */
    private void createThroughputTrendChart(final String timerName, final CategoryDataset dataset, final File outputDir,
                                            final int chartWidth, final int chartHeight)
    {
        System.out.printf("Creating throughput trend chart for timer '%s' ...\n", timerName);

        // create and modify the basic chart
        final JFreeChart jfreeChart = createBasicTrendChart(timerName, "Throughput", dataset);
        jfreeChart.getCategoryPlot().getRenderer().setSeriesPaint(0, Color.BLUE);
        jfreeChart.getCategoryPlot().getRangeAxis().setStandardTickUnits(NumberAxis.createStandardTickUnits());

        JFreeChartUtils.saveChart(jfreeChart, timerName + "_Throughput", outputDir, chartWidth, chartHeight);
    }

    /**
     * @param trendValuesByTimerName
     * @param outputDir
     * @throws IOException
     * @throws InterruptedException
     */
    private void createTrendCharts(final Map<String, Set<TrendValue>> trendValuesByTimerName, final File outputDir)
        throws IOException, InterruptedException
    {
        if (noCharts)
        {
            return;
        }

        final int chartWidth = config.getChartWidth();
        final int chartHeight = config.getChartHeight();

        FileUtils.forceMkdir(outputDir);

        final TaskManager taskManager = TaskManager.getInstance();
        for (final Entry<String, Set<TrendValue>> entry : trendValuesByTimerName.entrySet())
        {
            final String name = entry.getKey();
            final Set<TrendValue> trendValues = entry.getValue();

            // set up the data sets
            final DefaultCategoryDataset runTimeDataSet = new DefaultCategoryDataset();
            final DefaultCategoryDataset errorDataSet = new DefaultCategoryDataset();
            final DefaultCategoryDataset throughputDataSet = new DefaultCategoryDataset();

            for (final TrendValue value : trendValues)
            {
                runTimeDataSet.setValue(value.maximum, "Maximum", value.reportName);
                runTimeDataSet.setValue(value.mean, "Mean", value.reportName);
                runTimeDataSet.setValue(value.median, "Median", value.reportName);
                runTimeDataSet.setValue(value.minimum, "Minimum", value.reportName);

                errorDataSet.setValue(value.errors, "Errors", value.reportName);

                throughputDataSet.setValue(value.countPerSecond, "Count/s", value.reportName);
            }

            // create the trend charts asynchronously
            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    createRunTimeTrendChart(name, runTimeDataSet, outputDir, chartWidth, chartHeight);
                }
            });

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    createErrorTrendChart(name, errorDataSet, outputDir, chartWidth, chartHeight);
                }
            });

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    createThroughputTrendChart(name, throughputDataSet, outputDir, chartWidth, chartHeight);
                }
            });
        }

        taskManager.waitForAllTasksToComplete();
    }

    /**
     */
    private void createTrendElements(final Map<String, Set<TrendValue>> trendValuesByTimerName, final String tagName,
                                     final Element parentElement)
    {
        for (final Entry<String, Set<TrendValue>> entry : trendValuesByTimerName.entrySet())
        {
            final String timerName = entry.getKey();
            final Set<TrendValue> trendValues = entry.getValue();

            final Element element = ReportUtils.addTextElement(tagName, null, parentElement);
            ReportUtils.addTextElement("name", timerName, element);
            final Element trendValuesElement = ReportUtils.addTextElement("trendValues", null, element);

            for (final TrendValue trendValue : trendValues)
            {
                final Element trendValueElement = ReportUtils.addTextElement("trendValue", null, trendValuesElement);

                ReportUtils.addTextElement("median", ReportUtils.formatValue(trendValue.median), trendValueElement);
                ReportUtils.addTextElement("mean", ReportUtils.formatValue(trendValue.mean), trendValueElement);
                ReportUtils.addTextElement("min", String.valueOf(trendValue.minimum), trendValueElement);
                ReportUtils.addTextElement("max", String.valueOf(trendValue.maximum), trendValueElement);
                ReportUtils.addTextElement("errors", String.valueOf(trendValue.errors), trendValueElement);
                ReportUtils.addTextElement("throughput", ReportUtils.formatValue(trendValue.countPerSecond), trendValueElement);
                ReportUtils.addTextElement("reportName", String.valueOf(trendValue.reportName), trendValueElement);
                ReportUtils.addTextElement("reportDate", String.valueOf(trendValue.reportDate), trendValueElement);
                ReportUtils.addTextElement("reportComments", trendValue.reportComment, trendValueElement);
            }
        }
    }

    private void readDataFromTestReport(Map<String, Set<TrendValue>> trendValuesByTimerName, TestReportByName testReportByName,
                                        String currentTagName, ElementSpecification elementSpec)
        throws IOException, InterruptedException, ParseException
    {
        final String reportName = testReportByName.getReportName();
        final Document testReport = testReportByName.getTestReport();

        // get the interesting elements
        final Map<String, Element> elementsByTimerName = ReportUtils.filterElements(testReport, elementSpec);
        for (final Entry<String, Element> elementEntry : elementsByTimerName.entrySet())
        {
            final String timerName = elementEntry.getKey();
            final Element element = elementEntry.getValue();

            // get/create the trend value set for this timer name
            Set<TrendValue> trendValues = trendValuesByTimerName.get(timerName);
            if (trendValues == null)
            {
                trendValues = noSorting ? new LinkedHashSet<TrendValue>() : new TreeSet<TrendValue>();
                for (TrendValue value : initialTrendValues)
                {
                    trendValues.add(new TrendValue(null, null, null, null, value.reportName, value.reportDate, value.reportComment, null,
                                                   null));
                }
                trendValuesByTimerName.put(timerName, trendValues);
            }

            // get the run time, error, and throughput statistics
            final int min = ReportUtils.getChildElementTextAsInt(element, "min");
            final int max = ReportUtils.getChildElementTextAsInt(element, "max");
            final double median = ReportUtils.getChildElementTextAsDouble(element, "median");
            final double mean = ReportUtils.getChildElementTextAsDouble(element, "mean");
            final int errors = ReportUtils.getChildElementTextAsInt(element, "errors");
            final double countPerSecond = ReportUtils.getChildElementTextAsDouble(element, "countPerSecond");

            // lookup the empty/initial trend value for the current report and populate it
            for (final TrendValue trendValue : trendValues)
            {
                if (trendValue.reportName.equals(reportName))
                {
                    trendValue.minimum = min;
                    trendValue.maximum = max;
                    trendValue.median = median;
                    trendValue.mean = mean;
                    trendValue.errors = errors;
                    trendValue.countPerSecond = countPerSecond;

                    break;
                }
            }
        }
    }

    /**
     * Creates the trend report XML file and the trend charts.
     * 
     * @param outputDir
     *            the output directory
     * @param trendValuesByTimerNameByTagName
     *            the test report data
     * @return the trend report XML file
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws InterruptedException
     */
    private Document createTrendReport(final File outputDir,
                                       final Map<String, Map<String, Set<TrendValue>>> trendValuesByTimerNameByTagName)
        throws IOException, ParserConfigurationException, InterruptedException
    {
        // create a placeholder image
        final File chartsDir = new File(outputDir, XltConstants.TREND_REPORT_CHART_DIR);
        if (!noCharts)
        {
            FileUtils.forceMkdir(chartsDir);
            JFreeChartUtils.createPlaceholderChart(chartsDir, config.getChartWidth(), config.getChartHeight());
        }

        // create the trend report XML file
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document trendReport = builder.getDOMImplementation().createDocument(null, "trendreport", null);

        final Element rootElement = trendReport.getDocumentElement();
        final Element transactionsElement = ReportUtils.addTextElement("transactions", null, rootElement);
        final Element actionsElement = ReportUtils.addTextElement("actions", null, rootElement);
        final Element requestsElement = ReportUtils.addTextElement("requests", null, rootElement);
        final Element pageLoadTimingsElement = ReportUtils.addTextElement("pageLoadTimings", null, rootElement);
        final Element customTimersElement = ReportUtils.addTextElement("customTimers", null, rootElement);
        final Element summaryElement = ReportUtils.addTextElement("summary", null, rootElement);

        // transaction
        Map<String, Set<TrendValue>> trendValuesByTimerName = trendValuesByTimerNameByTagName.get("transaction");
        createTrendElements(trendValuesByTimerName, "transaction", transactionsElement);
        createTrendCharts(trendValuesByTimerName, new File(chartsDir, "transactions"));
        // action
        trendValuesByTimerName = trendValuesByTimerNameByTagName.get("action");
        createTrendElements(trendValuesByTimerName, "action", actionsElement);
        createTrendCharts(trendValuesByTimerName, new File(chartsDir, "actions"));
        // request
        trendValuesByTimerName = trendValuesByTimerNameByTagName.get("request");
        createTrendElements(trendValuesByTimerName, "request", requestsElement);
        createTrendCharts(trendValuesByTimerName, new File(chartsDir, "requests"));
        // page load timing
        trendValuesByTimerName = trendValuesByTimerNameByTagName.get("pageLoadTiming");
        createTrendElements(trendValuesByTimerName, "pageLoadTiming", pageLoadTimingsElement);
        createTrendCharts(trendValuesByTimerName, new File(chartsDir, "pageLoadTimings"));
        // custom timer
        trendValuesByTimerName = trendValuesByTimerNameByTagName.get("customTimer");
        createTrendElements(trendValuesByTimerName, "customTimer", customTimersElement);
        createTrendCharts(trendValuesByTimerName, new File(chartsDir, "custom"));

        // summary
        // transactions
        trendValuesByTimerName = trendValuesByTimerNameByTagName.get("transactions");
        createTrendElements(trendValuesByTimerName, "transactions", summaryElement);
        createTrendCharts(trendValuesByTimerName, new File(chartsDir, "summary"));
        // actions
        trendValuesByTimerName = trendValuesByTimerNameByTagName.get("actions");
        createTrendElements(trendValuesByTimerName, "actions", summaryElement);
        createTrendCharts(trendValuesByTimerName, new File(chartsDir, "summary"));
        // requests
        trendValuesByTimerName = trendValuesByTimerNameByTagName.get("requests");
        createTrendElements(trendValuesByTimerName, "requests", summaryElement);
        createTrendCharts(trendValuesByTimerName, new File(chartsDir, "summary"));
        // page load timing
        trendValuesByTimerName = trendValuesByTimerNameByTagName.get("pageLoadTimings");
        createTrendElements(trendValuesByTimerName, "pageLoadTimings", summaryElement);
        createTrendCharts(trendValuesByTimerName, new File(chartsDir, "summary"));
        // custom timers
        trendValuesByTimerName = trendValuesByTimerNameByTagName.get("customTimers");
        createTrendElements(trendValuesByTimerName, "customTimers", summaryElement);
        createTrendCharts(trendValuesByTimerName, new File(chartsDir, "summary"));

        return trendReport;
    }

    /**
     * Prints the usage information to stdout and quits.
     */
    private void printUsageInfoAndExit(final Options options)
    {
        System.out.println("\nCreates a trend report from multiple test reports.");

        final HelpFormatter formatter = new HelpFormatter();
        formatter.setSyntaxPrefix("Usage: ");
        formatter.setWidth(79);

        System.out.println();
        formatter.printHelp(XltConstants.TREND_REPORT_EXECUTABLE_NAME + " [options] <testReportDir_1> ... <testReportDir_n>", "\nOptions:",
                            options, null);
        System.out.println();

        System.exit(ProcessExitCodes.PARAMETER_ERROR);
    }

    /**
     * Reads the test report XML file contained in the given directory.
     * 
     * @param dir
     *            the report directory
     * @return the XML document just read, or <code>null</code> if the specified directory is not a valid test report
     *         directory
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private Document readTestReport(final File dir) throws ParserConfigurationException, SAXException, IOException
    {
        System.out.println("Reading report from directory: " + dir);

        final File file = new File(dir, XltConstants.LOAD_REPORT_XML_FILENAME);

        if (file.isFile())
        {
            final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            return documentBuilder.parse(file);
        }
        else
        {
            log.warn("The specified directory '" + dir + "' does not seem to be a valid test report directory. Skipping it.");
            return null;
        }
    }

    /**
     * Reads the test report XML file contained in the given directory.
     * 
     * @param directoryPathName
     *            the test report directory
     * @param reportDirs
     *            already read test report directories
     * @param reportNames
     *            already used test report names
     * @return A test report document keyed by a unique test report name.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private TestReportByName readTestReport(final String directoryPathName, final HashSet<String> reportDirs,
                                            final HashSet<String> reportNames)
        throws ParserConfigurationException, SAXException, IOException
    {
        final File reportDir = new File(directoryPathName);
        final String reportPath = reportDir.getAbsolutePath();
        // report directory was specified more than once
        if (reportDirs.contains(reportPath))
        {
            return null;
        }
        // add current report directory to the list
        reportDirs.add(reportPath);
        // compute the report name as unique identifier
        String key = reportDir.getName();
        if (reportNames.contains(key))
        {
            key = computeNewKey(reportNames, key);
            reportNames.add(key);
        }
        final Document testReport = readTestReport(reportDir);
        if (testReport == null)
        {
            return null;
        }
        return new TestReportByName(key, testReport);
    }

    /**
     * Read the test report data from the given test report.
     * 
     * @param trendValuesByTimerNameByTagName
     *            the map to save the test report data
     * @param testReportByName
     *            the test report document keyed by the test report name
     * @throws ParseException
     * @throws InterruptedException
     * @throws IOException
     */
    private void readDataFromTestReport(final Map<String, Map<String, Set<TrendValue>>> trendValuesByTimerNameByTagName,
                                        final TestReportByName testReportByName)
        throws IOException, InterruptedException, ParseException
    {
        // process the transaction nodes
        String currentTagName = "transaction";
        readDataFromTestReport(getTrendValuesByTimerNameByTagName(trendValuesByTimerNameByTagName, currentTagName), testReportByName,
                               currentTagName, new ElementSpecification("/testreport/transactions/transaction", "name"));

        // process the action nodes
        currentTagName = "action";
        readDataFromTestReport(getTrendValuesByTimerNameByTagName(trendValuesByTimerNameByTagName, currentTagName), testReportByName,
                               currentTagName, new ElementSpecification("/testreport/actions/action", "name"));

        // process the request nodes
        currentTagName = "request";
        readDataFromTestReport(getTrendValuesByTimerNameByTagName(trendValuesByTimerNameByTagName, currentTagName), testReportByName,
                               currentTagName, new ElementSpecification("/testreport/requests/request", "name"));

        // process the page load timing nodes
        currentTagName = "pageLoadTiming";
        readDataFromTestReport(getTrendValuesByTimerNameByTagName(trendValuesByTimerNameByTagName, currentTagName), testReportByName,
                               currentTagName, new ElementSpecification("/testreport/pageLoadTimings/pageLoadTiming", "name"));

        // process the customTimer nodes
        currentTagName = "customTimer";
        readDataFromTestReport(getTrendValuesByTimerNameByTagName(trendValuesByTimerNameByTagName, currentTagName), testReportByName,
                               currentTagName, new ElementSpecification("/testreport/customTimers/customTimer", "name"));

        // process the summary nodes

        // transactions summary
        currentTagName = "transactions";
        readDataFromTestReport(getTrendValuesByTimerNameByTagName(trendValuesByTimerNameByTagName, currentTagName), testReportByName,
                               currentTagName, new ElementSpecification("/testreport/summary/transactions", "name"));
        
        // actions summary
        currentTagName = "actions";
        readDataFromTestReport(getTrendValuesByTimerNameByTagName(trendValuesByTimerNameByTagName, currentTagName), testReportByName,
                               currentTagName, new ElementSpecification("/testreport/summary/actions", "name"));
        
        // requests summary
        currentTagName = "requests";
        readDataFromTestReport(getTrendValuesByTimerNameByTagName(trendValuesByTimerNameByTagName, currentTagName), testReportByName,
                               currentTagName, new ElementSpecification("/testreport/summary/requests", "name"));
        
        // page load timings summary
        currentTagName = "pageLoadTimings";
        readDataFromTestReport(getTrendValuesByTimerNameByTagName(trendValuesByTimerNameByTagName, currentTagName), testReportByName,
                               currentTagName, new ElementSpecification("/testreport/summary/pageLoadTimings", "name"));
        
        // custom timers summary
        currentTagName = "customTimers";
        readDataFromTestReport(getTrendValuesByTimerNameByTagName(trendValuesByTimerNameByTagName, currentTagName), testReportByName,
                               currentTagName, new ElementSpecification("/testreport/summary/customTimers", "name"));
    }

    /**
     * Returns the trend values keyed by the timer name of the given tag name.
     * 
     * @param trendValuesByTimerNameByTagName
     *            the map with the test report data
     * @param tagName
     * @return a trend value set keyed by the timer names
     */
    private Map<String, Set<TrendValue>> getTrendValuesByTimerNameByTagName(final Map<String, Map<String, Set<TrendValue>>> trendValuesByTimerNameByTagName,
                                                                            final String tagName)
    {
        // timer name : trend values
        Map<String, Set<TrendValue>> trendValuesByTimerName = trendValuesByTimerNameByTagName.get(tagName);
        if (trendValuesByTimerName == null)
        {
            trendValuesByTimerName = new TreeMap<String, Set<TrendValue>>();
            trendValuesByTimerNameByTagName.put(tagName, trendValuesByTimerName);
        }
        return trendValuesByTimerName;
    }

    /**
     * Runs the trend report generator.
     * 
     * @param args
     *            the name of the directories to scan for test reports
     */
    private void run(final String[] args)
    {
        final Options options = createCommandLineOptions();

        try
        {
            config = new TrendReportGeneratorConfiguration();

            final CommandLine commandLine = new DefaultParser().parse(options, args);

            noCharts = commandLine.hasOption(XltConstants.COMMANDLINE_OPTION_NO_CHARTS);
            noSorting = commandLine.hasOption("noSorting");

            // create the output directory
            final File outputDir;
            final String outputDirName = commandLine.getOptionValue(XltConstants.COMMANDLINE_OPTION_OUTPUT_DIR);
            if (outputDirName == null)
            {
                // create an artificial name
                final String defaultOutputDirName = XltConstants.TREND_REPORT_DIR_PREFIX + "-" +
                                                    new SimpleDateFormat(XltConstants.TREND_REPORT_OUTPUT_DATE_FORMAT).format(new Date());
                final File reportsDir = config.getReportsRootDirectory();
                outputDir = new File(reportsDir, defaultOutputDirName);
            }
            else
            {
                // take it as specified
                outputDir = new File(outputDirName);
            }

            // check the remaining arguments
            final String[] remainingArgs = commandLine.getArgs();
            if (remainingArgs.length < 2)
            {
                printUsageInfoAndExit(options);
            }

            // configure the thread pool
            TaskManager.getInstance().setMaximumThreadCount(config.getThreadCount());

            FileUtils.forceMkdir(outputDir);

            // create initial trend values
            // either maintain the order of the reports as given on the command line or sort them by date
            initialTrendValues = noSorting ? new LinkedHashSet<TrendValue>() : new TreeSet<TrendValue>();
            createInitialTrendValues(remainingArgs);

            // remember all necessary test report data to create a trend report
            // tag name : timer name : trend values
            // tag names are e.g. 'transaction', 'action', 'request', ...
            // timer names are e.g. 'TBrowse', 'OpenHomepage', 'OpenHomepage [200]', ...
            final Map<String, Map<String, Set<TrendValue>>> trendValuesByTimerNameByTagName = new HashMap<String, Map<String, Set<TrendValue>>>();

            // remember absolute paths of all report directories to detect repetitions in user input
            final HashSet<String> reportDirs = new HashSet<String>();
            // remember test report names
            final HashSet<String> reportNames = new HashSet<String>();
            // remember the parsed documents
            final LinkedList<Document> documents = new LinkedList<>();
            // get each report one by one and add the data to the trendValuesByTimerNameByTagName map
            for (String remainingArg : remainingArgs)
            {
                // read the test report
                final TestReportByName testReportByName = readTestReport(remainingArg, reportDirs, reportNames);
                if (testReportByName != null)
                {
                    // get the test report data
                    readDataFromTestReport(trendValuesByTimerNameByTagName, testReportByName);
                    documents.add(testReportByName.getTestReport());
                }
            }

            // create the trend report and the trend charts
            System.out.println("Creating the XML trend report ...");
            final Document trendReport = createTrendReport(outputDir, trendValuesByTimerNameByTagName);

            // create the trend report XML file
            final File xmlFile = new File(outputDir, XltConstants.TREND_REPORT_XML_FILENAME);
            writeTrendReport(trendReport, xmlFile);

            // create the trend report HTML file
            System.out.println("Rendering the HTML trend report ...");

            final HashMap<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("productName", ProductInformation.getProductInformation().getProductName());
            parameters.put("productVersion", ProductInformation.getProductInformation().getVersion());
            parameters.put("productUrl", ProductInformation.getProductInformation().getProductURL());
            parameters.put("projectName", ReportUtils.obtainProjectName(documents));

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

                final File styleSheetFile = new File(new File(config.getConfigDirectory(), XltConstants.TREND_REPORT_XSL_PATH),
                                                     styleSheetFileNames.get(i));
                styleSheetFiles.add(styleSheetFile);
            }

            // transform the report
            final ReportTransformer reportTransformer = new ReportTransformer(outputFiles, styleSheetFiles, parameters);
            reportTransformer.run(xmlFile, outputDir);

            // copy the report's static resources
            final File resourcesDir = new File(config.getConfigDirectory(), XltConstants.REPORT_RESOURCES_PATH);
            FileUtils.copyDirectory(resourcesDir, outputDir, FileFilterUtils.makeSVNAware(null), true);

            // output the path to the report either as file path (Win) or as clickable file URL
            final File reportFile = new File(outputDir, "index.html");
            final String reportPath = ReportUtils.toString(reportFile);

            // wait for any asynchronous task to complete
            TaskManager.getInstance().waitForAllTasksToComplete();

            System.out.println("\nReport: " + reportPath);

            System.exit(ProcessExitCodes.SUCCESS);
        }
        catch (final org.apache.commons.cli.ParseException ex)
        {
            printUsageInfoAndExit(options);
        }
        catch (final Exception ex)
        {
            log.fatal("Failed to run trend report generator.", ex);
            System.exit(ProcessExitCodes.GENERAL_ERROR);
        }
    }

    /**
     * Writes the trend report as an XML file to disk.
     * 
     * @param trendReport
     *            the trend report
     * @param file
     *            the target file
     * @throws IOException
     *             if anything goes wrong
     */
    private void writeTrendReport(final Document trendReport, final File file) throws IOException
    {
        try (final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), XltConstants.UTF8_ENCODING))
        {
            osw.write(XltConstants.XML_HEADER);
            DomUtils.prettyPrintNode(trendReport, osw);
        }
    }

    /**
     * Computes a new key for the given original key.
     * 
     * @param knownKeys
     *            the set of known keys
     * @param key
     *            the original key
     * @return new key based on the original key that don't collide with any key in the given set of known keys
     */
    private static String computeNewKey(final Set<String> knownKeys, final String key)
    {
        final String format = key.concat("(%d)");
        int i = 2;
        String newKey = String.format(format, i);
        while (knownKeys.contains(newKey))
        {
            ++i;
            newKey = String.format(format, i);
        }

        return newKey;
    }
}
