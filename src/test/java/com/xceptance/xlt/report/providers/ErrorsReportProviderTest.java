package com.xceptance.xlt.report.providers;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.AbstractXLTTestCase;
import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TimerData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.common.XltPropertyNames;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.util.TaskManager;

public class ErrorsReportProviderTest extends AbstractXLTTestCase
{
    @Before
    public final void before()
    {
        Utils.tempDir = getTempDir(); // needs to be defined before using the util directory related methods for the
                                      // first time
    }

    /**
     * If no transaction errors occurred and the error details chart limiter is enabled then the report generation
     * should finish without any errors. The xml export data structure should not contain any error where the
     * detailsChartID != 0. There should be also no chart file present after the execution.
     */
    @Test
    public final void testTransactionErrorDetailsCharts_NoErrors() throws InterruptedException, IOException
    {
        // Prepare
        ErrorsReportProvider reportProvider = Utils.getDefaultErrorsReportProvider();
        List<TimerData> testData = Utils.getTestTimerData(false, true);

        // Test
        ErrorsReport reportData = Utils.runReportGeneration(reportProvider, testData);

        // Validate
        int expectedChartCount = 0;
        Utils.validateTransactionErrorDetailsCharts(reportData, expectedChartCount);
    }

    /**
     * If many transaction errors occurred and the default error details chart limiter is enabled then the report
     * generation should finish without any errors. The xml export data structure must contain as defined by the default
     * limiter the same number of the most often occurred errors where the detailsChartID != 0. There must be also a
     * chart file present for each of the errors.
     */
    @Test
    public final void testTransactionErrorDetailsCharts_Default() throws InterruptedException, IOException
    {
        // Prepare
        ErrorsReportProvider reportProvider = Utils.getDefaultErrorsReportProvider();
        List<TimerData> testData = Utils.getTestTimerData(true, true);

        // Test
        ErrorsReport reportData = Utils.runReportGeneration(reportProvider, testData);

        // Validate
        int expectedChartCount = Utils.getDefaultTransactionErrorDetailsChartLimit();
        assertEquals("The error details chart limit should be " + expectedChartCount, expectedChartCount,
                     reportProvider.getConfiguration().getErrorDetailsChartLimit());

        Utils.validateTransactionErrorDetailsCharts(reportData, expectedChartCount);
    }

    /**
     * If many transaction errors occurred and the error details chart limiter is set to 0 which means disabled then the
     * report generation should finish without any errors. The xml export data structure must not contain any errors
     * where the detailsChartID != 0. There must not be any chart file present.
     */
    @Test
    public final void testTransactionErrorDetailsCharts_LimitSetToZero() throws InterruptedException, IOException
    {
        // Prepare
        int chartLimit = 0;
        ErrorsReportProvider reportProvider = Utils.getErrorsReportProvider(Utils.getProperties(0, 0, chartLimit));
        List<TimerData> testData = Utils.getTestTimerData(true, true);

        // Test
        ErrorsReport reportData = Utils.runReportGeneration(reportProvider, testData);

        // Validate
        assertEquals("The error details chart limit should be " + chartLimit, chartLimit, reportProvider.getConfiguration()
                                                                                                        .getErrorDetailsChartLimit());

        Utils.validateTransactionErrorDetailsCharts(reportData, chartLimit);
    }

    /**
     * If many transaction errors occurred and the error details chart limiter is set to -1 which means unlimited then
     * the report generation should finish without any errors. The xml export data structure must contain all occurred
     * errors and each must have a detailsChartID != 0. There must be a chart file present for all occurred errors.
     */
    @Test
    public final void testTransactionErrorDetailsCharts_LimitSetToNegative() throws InterruptedException, IOException
    {
        // Prepare
        ErrorsReportProvider reportProvider = Utils.getErrorsReportProvider(Utils.getProperties(0, 0, -1));
        List<TimerData> testData = Utils.getTestTimerData(true, false);

        // Test
        ErrorsReport reportData = Utils.runReportGeneration(reportProvider, testData);

        // Validate
        assertEquals("The error details chart limit should be -1", -1, reportProvider.getConfiguration().getErrorDetailsChartLimit());

        int expectedChartCount = Utils.getTestTimerDataErrorDetailsCount();
        Utils.validateTransactionErrorDetailsCharts(reportData, expectedChartCount);
    }

    /**
     * If many transaction errors occurred and the error details chart limiter is set to a positive value then the
     * report generation should finish without any errors. The xml export data structure must contain as defined by the
     * limiter the same number of the most often occurred errors where the detailsChartID != 0. There must be also a
     * chart file present for each of the errors.
     */
    @Test
    public final void testTransactionErrorDetailsCharts_LimitSetToPositive() throws InterruptedException, IOException
    {
        // Prepare
        int chartLimit = 10;
        ErrorsReportProvider reportProvider = Utils.getErrorsReportProvider(Utils.getProperties(0, 0, chartLimit));
        List<TimerData> testData = Utils.getTestTimerData(true, true);

        // Test
        ErrorsReport reportData = Utils.runReportGeneration(reportProvider, testData);

        // Validate
        assertEquals("The error details chart limit should be " + chartLimit, chartLimit, reportProvider.getConfiguration()
                                                                                                        .getErrorDetailsChartLimit());

        Utils.validateTransactionErrorDetailsCharts(reportData, chartLimit);
    }

    /**
     * If no transaction errors occurred and the transaction error overview chart limiter is enabled then the report
     * generation should finish without any errors. The xml export data structure should not contain any transaction
     * error overview chart entry. There should be also no chart file present after the execution.
     */
    @Test
    public final void testTransactionErrorCharts_NoErrors() throws InterruptedException, IOException
    {
        // Prepare
        ErrorsReportProvider reportProvider = Utils.getDefaultErrorsReportProvider();
        List<TimerData> testData = Utils.getTestTimerData(false, true);

        // Test
        ErrorsReport reportData = Utils.runReportGeneration(reportProvider, testData);

        // Validate
        int expectedChartCount = 0;
        Utils.validateTransactionErrorCharts(reportData, expectedChartCount);
    }

    /**
     * If many transaction errors occurred and the default transaction error overview chart limiter is enabled then the
     * report generation should finish without any errors. The xml export data structure must contain as defined by the
     * default limiter the same number of the most often occurred error types. There must be also a chart file present
     * for each of the error types.
     */
    @Test
    public final void testTransactionErrorCharts_Default() throws InterruptedException, IOException
    {
        // Prepare
        ErrorsReportProvider reportProvider = Utils.getDefaultErrorsReportProvider();
        List<TimerData> testData = Utils.getTestTimerData(true, true);

        // Test
        ErrorsReport reportData = Utils.runReportGeneration(reportProvider, testData);

        // Validate
        int expectedChartCount = Utils.getDefaultTransactionErrorOverviewChartLimit();
        assertEquals("The transaction error chart limit should be " + expectedChartCount, expectedChartCount,
                     reportProvider.getConfiguration().getTransactionErrorOverviewChartLimit());

        Utils.validateTransactionErrorCharts(reportData, expectedChartCount);
    }

    /**
     * If many transaction errors occurred and the transaction error overview chart limiter is set to 0 which means
     * disabled then the report generation should finish without any errors. The xml export data structure should not
     * contain any transaction error overview chart entry. There should be also no chart file present after the
     * execution.
     */
    @Test
    public final void testTransactionErrorCharts_LimitSetToZero() throws InterruptedException, IOException
    {
        // Prepare
        int chartLimit = 0;
        ErrorsReportProvider reportProvider = Utils.getErrorsReportProvider(Utils.getProperties(0, chartLimit, 0));
        List<TimerData> testData = Utils.getTestTimerData(true, true);

        // Test
        ErrorsReport reportData = Utils.runReportGeneration(reportProvider, testData);

        // Validate
        assertEquals("The transaction error chart limit should be " + chartLimit, chartLimit,
                     reportProvider.getConfiguration().getTransactionErrorOverviewChartLimit());

        Utils.validateTransactionErrorCharts(reportData, chartLimit);
    }

    /**
     * If many transaction errors occurred and the transaction error overview chart limiter is set to -1 which means
     * unlimited then the report generation should finish without any errors. The xml export data structure must contain
     * chart entries for all occurred error types. There must be a chart file present for all occurred error types.
     */
    @Test
    public final void testTransactionErrorCharts_LimitSetToNegative() throws InterruptedException, IOException
    {
        // Prepare
        ErrorsReportProvider reportProvider = Utils.getErrorsReportProvider(Utils.getProperties(0, -1, 0));
        List<TimerData> testData = Utils.getTestTimerData(true, false);

        // Test
        ErrorsReport reportData = Utils.runReportGeneration(reportProvider, testData);

        // Validate
        assertEquals("The transaction error chart limit should be -1", -1, reportProvider.getConfiguration()
                                                                                         .getTransactionErrorOverviewChartLimit());

        int expectedChartCount = Utils.getTestTimerDataTransactionErrorTypeCount();
        Utils.validateTransactionErrorCharts(reportData, expectedChartCount);
    }

    /**
     * If many transaction errors occurred and the transaction error overview chart limiter is set to a positive value
     * then the report generation should finish without any errors. The xml export data structure must contain chart
     * entries, as defined by the limiter the same number of the most often occurred error types. There must be also a
     * chart file present for each of the error types.
     */
    @Test
    public final void testTransactionErrorCharts_LimitSetToPositive() throws InterruptedException, IOException
    {
        // Prepare
        int chartLimit = 10;
        ErrorsReportProvider reportProvider = Utils.getErrorsReportProvider(Utils.getProperties(0, chartLimit, 0));
        List<TimerData> testData = Utils.getTestTimerData(true, true);

        // Test
        ErrorsReport reportData = Utils.runReportGeneration(reportProvider, testData);

        // Validate
        assertEquals("The transaction error chart limit should be " + chartLimit, chartLimit,
                     reportProvider.getConfiguration().getTransactionErrorOverviewChartLimit());

        Utils.validateTransactionErrorCharts(reportData, chartLimit);
    }

    /**
     * If no request error occurred and the request error overview chart limiter is enabled then the report generation
     * should finish without any errors. The xml export data structure should not contain any request error chart
     * entries. There should be also no chart file present after the execution.
     */
    @Test
    public final void testResponseErrorCharts_NoErrors() throws IOException, InterruptedException
    {
        // Prepare
        ErrorsReportProvider reportProvider = Utils.getDefaultErrorsReportProvider();
        List<TimerData> testData = Utils.getTestTimerData(true, false);

        // Test
        ErrorsReport reportData = Utils.runReportGeneration(reportProvider, testData);

        // Validate
        int expectedChartCount = 0;
        Utils.validateRequestErrorCharts(reportData, expectedChartCount);
    }

    /**
     * If many request errors occurred and the default request error overview chart limiter is enabled then the report
     * generation should finish without any errors. The xml export data structure must contain as defined by the default
     * limiter the same number of the most often occurred request error types. There must be also a chart file present
     * for each of the request error types.
     */
    @Test
    public final void testResponseErrorCharts_Default() throws IOException, InterruptedException
    {
        // Prepare
        ErrorsReportProvider reportProvider = Utils.getDefaultErrorsReportProvider();
        List<TimerData> testData = Utils.getTestTimerData(true, true);

        // Test
        ErrorsReport reportData = Utils.runReportGeneration(reportProvider, testData);

        // Validate
        int expectedChartCount = Utils.getDefaultRequestErrorOverviewChartLimit();
        assertEquals("The request error chart limit should be " + expectedChartCount, expectedChartCount,
                     reportProvider.getConfiguration().getRequestErrorOverviewChartLimit());

        Utils.validateRequestErrorCharts(reportData, expectedChartCount);
    }

    /**
     * If many request errors occurred and the request error overview chart limiter is set to 0 which means disabled
     * then the report generation should finish without any errors. The xml export data structure should not contain any
     * request error overview chart entry. There should be also no chart file present after the execution.
     */
    @Test
    public final void testResponseErrorCharts_LimitSetToZero() throws IOException, InterruptedException
    {
        // Prepare
        int chartLimit = 0;
        ErrorsReportProvider reportProvider = Utils.getErrorsReportProvider(Utils.getProperties(chartLimit, 0, 0));
        List<TimerData> testData = Utils.getTestTimerData(true, true);

        // Test
        ErrorsReport reportData = Utils.runReportGeneration(reportProvider, testData);

        // Validate
        assertEquals("The request error chart limit should be " + chartLimit, chartLimit,
                     reportProvider.getConfiguration().getRequestErrorOverviewChartLimit());

        Utils.validateRequestErrorCharts(reportData, chartLimit);
    }

    /**
     * If many request errors occurred and the request error overview chart limiter is set to -1 which means unlimited
     * then the report generation should finish without any errors. The xml export data structure must contain chart
     * entries for all occurred request error types. There must be a chart file present for all occurred request error
     * types.
     */
    @Test
    public final void testResponseErrorCharts_LimitSetToNegative() throws IOException, InterruptedException
    {
        // Prepare
        ErrorsReportProvider reportProvider = Utils.getErrorsReportProvider(Utils.getProperties(-1, 0, 0));
        List<TimerData> testData = Utils.getTestTimerData(false, true);

        // Test
        ErrorsReport reportData = Utils.runReportGeneration(reportProvider, testData);

        // Validate
        assertEquals("The request error chart limit should be -1", -1, reportProvider.getConfiguration()
                                                                                     .getRequestErrorOverviewChartLimit());

        int expectedChartCount = Utils.getTestTimerDataRequestErrorTypeCount();
        Utils.validateRequestErrorCharts(reportData, expectedChartCount);
    }

    /**
     * If many request errors occurred and the request error overview chart limiter is set to a positive value then the
     * report generation should finish without any errors. The xml export data structure must contain chart entries, as
     * defined by the limiter the same number of the most often occurred request error types. There must be also a chart
     * file present for each of the request error types.
     */
    @Test
    public final void testResponseErrorCharts_LimitSetToPositive() throws IOException, InterruptedException
    {
        // Prepare
        int chartLimit = 10;
        ErrorsReportProvider reportProvider = Utils.getErrorsReportProvider(Utils.getProperties(chartLimit, 0, 0));
        List<TimerData> testData = Utils.getTestTimerData(true, true);

        // Test
        ErrorsReport reportData = Utils.runReportGeneration(reportProvider, testData);

        // Validate
        assertEquals("The request error chart limit should be " + chartLimit, chartLimit,
                     reportProvider.getConfiguration().getRequestErrorOverviewChartLimit());

        Utils.validateRequestErrorCharts(reportData, chartLimit);
    }

    private static class Utils
    {
        private static File tempDir;

        /**
         * Perform the report generation as the report generator would do for a given error report provider while using
         * a given set of test data. The report provider is executed for each of the data in the given set which will
         * perform all the data collection things and creating the charts. The generated error report data structure
         * which will later be exported as xml is returned.
         * 
         * @param reportProvider
         *            under test that should be executed with the given data
         * @param timerData
         *            used as test data for the given report provider
         * @return the generated report data structure
         * @throws InterruptedException
         *             should never happen during the tests
         */
        public static ErrorsReport runReportGeneration(ErrorsReportProvider reportProvider, List<TimerData> timerData)
            throws InterruptedException
        {
            for (TimerData eachData : timerData)
            {
                reportProvider.processDataRecord(eachData);
            }
            ErrorsReport reportData = (ErrorsReport) reportProvider.createReportFragment();

            TaskManager.getInstance().waitForAllTasksToComplete();

            return reportData;
        }

        /**
         * Get a fresh new error report provider to be tested. This provider will have the default properties set.
         * 
         * @return a new and configured error report provider for testing
         * @throws IOException
         *             should never happen during the tests
         */
        public static ErrorsReportProvider getDefaultErrorsReportProvider() throws IOException
        {
            return getErrorsReportProvider(null);
        }

        /**
         * Get a fresh new error report provider to be tested. This provider will have the given properties set as if
         * the user would provide them via command line properties. See also {@link #getProperties(int, int, int)}.
         * 
         * @param commandLineProperties
         *            the properties that should be set
         * @return a new and configured error report provider for testing
         * @throws IOException
         *             should never happen during the tests
         */
        public static ErrorsReportProvider getErrorsReportProvider(Properties commandLineProperties) throws IOException
        {
            ReportGeneratorConfiguration reportGeneratorConfig = new ReportGeneratorConfiguration(null, null, commandLineProperties);
            FileUtils.deleteDirectory(getErrorChartsDir()); // must be cleared before every test otherwise we have some
                                                            // old charts present
            reportGeneratorConfig.setReportDirectory(getReportsDir());

            ErrorsReportProvider reportProvider = new ErrorsReportProvider();
            reportProvider.setConfiguration(reportGeneratorConfig);

            return reportProvider;
        }

        private static File getReportsDir()
        {
            return new File(tempDir, "reports");
        }

        private static File getChartsDir()
        {
            return new File(Utils.getReportsDir(), "charts");
        }

        /**
         * Get the directory where all the error charts should be.
         * 
         * @return the error charts directory
         */
        private static File getErrorChartsDir()
        {
            return new File(getChartsDir(), "errors");
        }

        /**
         * Get a list of all transaction error details charts in the errors directory. See also
         * {@link #getErrorChartsDir()}
         * 
         * @return the transaction error details charts in the errors directory
         */
        private static List<String> getTransactionErrorDetailsChartFileNames()
        {
            String[] listedFiles = Utils.getErrorChartsDir().list(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return name.startsWith("d");
                }
            });
            if (listedFiles != null)
            {
                return Arrays.asList(listedFiles);
            }
            return new ArrayList<>();
        }

        /**
         * Get a list of all transaction error overview charts in the errors directory. See also
         * {@link #getErrorChartsDir()}
         * 
         * @return the transaction error overview charts in the errors directory
         */
        private static List<String> getTransactionErrorChartFileNames()
        {
            String[] listedFiles = Utils.getErrorChartsDir().list(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return name.startsWith("t");
                }
            });
            if (listedFiles != null)
            {
                return Arrays.asList(listedFiles);
            }
            return new ArrayList<>();
        }

        /**
         * Get a list of all request error overview charts in the errors directory. See also
         * {@link #getErrorChartsDir()}
         * 
         * @return the request error overview charts in the errors directory
         */
        private static List<String> getRequestErrorChartFileNames()
        {
            String[] listedFiles = Utils.getErrorChartsDir().list(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return name.startsWith("r");
                }
            });
            if (listedFiles != null)
            {
                return Arrays.asList(listedFiles);
            }
            return new ArrayList<>();
        }

        /**
         * Create properties to configure a error report provider. See also {@link #getErrorsReportProvider(Properties)}
         * 
         * @param requestErrorOverviewChartsLimit
         *            sets the request error overview charts limit property
         * @param transactionErrorOverviewChartsLimit
         *            sets the transaction error overview charts limit property
         * @param transactionErrorDetailChartsLimit
         *            sets the transaction error details charts limit property
         * @return the properties
         */
        public static Properties getProperties(int requestErrorOverviewChartsLimit, int transactionErrorOverviewChartsLimit,
                                               int transactionErrorDetailChartsLimit)
        {
            Properties properties = new Properties();
            properties.setProperty(XltPropertyNames.ReportGenerator.Errors.REQUEST_ERROR_OVERVIEW_CHARTS_LIMIT,
                                   String.valueOf(requestErrorOverviewChartsLimit));
            properties.setProperty(XltPropertyNames.ReportGenerator.Errors.TRANSACTION_ERROR_OVERVIEW_CHARTS_LIMIT,
                                   String.valueOf(transactionErrorOverviewChartsLimit));
            properties.setProperty(XltPropertyNames.ReportGenerator.Errors.TRANSACTION_ERROR_DETAIL_CHARTS_LIMIT,
                                   String.valueOf(transactionErrorDetailChartsLimit));

            return properties;
        }

        /**
         * The expected default value for the transaction error detail charts limit property.
         * 
         * @return the default transaction error detail charts limit property value
         */
        public static int getDefaultTransactionErrorDetailsChartLimit()
        {
            return 50;
        }

        /**
         * The expected default value for the transaction error overview charts limit property.
         * 
         * @return the default transaction error overview charts limit property value
         */
        public static int getDefaultTransactionErrorOverviewChartLimit()
        {
            return 50;
        }

        /**
         * The expected default value for the request error overview charts limit property.
         * 
         * @return the default request error overview charts limit property value
         */
        public static int getDefaultRequestErrorOverviewChartLimit()
        {
            return 50;
        }

        /**
         * The number of request error types for the test data created by {@link #getTestTimerData(boolean, boolean)}.
         * 
         * @return the number of request error types
         */
        public static int getTestTimerDataRequestErrorTypeCount()
        {
            return 101; // response code 0 and 500 to 599
        }

        /**
         * The number of transaction error types for the test data created by
         * {@link #getTestTimerData(boolean, boolean)}.
         * 
         * @return the number of transaction error types
         */
        public static int getTestTimerDataTransactionErrorTypeCount()
        {
            return 3 + (2 * getDefaultTransactionErrorOverviewChartLimit()); // 3 always present error types and the
                                                                             // number of the default limit multiplied
                                                                             // with a factor to have some more errors
        }

        /**
         * The number of transaction error details for the test data created by
         * {@link #getTestTimerData(boolean, boolean)}.
         * 
         * @return the number of transaction error details
         */
        public static int getTestTimerDataErrorDetailsCount()
        {
            return 5 + (2 * getDefaultTransactionErrorDetailsChartLimit()); // 5 always present error type to
                                                                            // transaction combinations and the number
                                                                            // of the default limit multiplied with a
                                                                            // factor to have some more errors
        }

        /**
         * Common validator to validate the transaction error details output after an error report provider was
         * executed. See also @ #runReportGeneration(ErrorsReportProvider, List)} . Check if the report data structure
         * contains the expected number of only valid data entries and if the top N errors are present. Also verify that
         * the expected number of charts are created for the top N errors.
         * 
         * @param reportData
         *            the created error report data structure to be validated
         * @param expectedChartCount
         *            the expected number of transaction error details
         */
        public static void validateTransactionErrorDetailsCharts(ErrorsReport reportData, int expectedChartCount)
        {
            // check the expected number of error detail charts
            assertTrue("There should be " + expectedChartCount + " error details ", reportData.errors.size() >= expectedChartCount);

            Set<String> foundErrors = new HashSet<>();
            Set<Integer> foundIDs = new HashSet<>();
            Map<String, List<Integer>> errorsToIDs = new HashMap<>();
            for (ErrorReport eachError : reportData.errors)
            {
                if (eachError.detailChartID != 0)
                {
                    List<Integer> idList = errorsToIDs.get(eachError.message);
                    if (idList == null)
                    {
                        idList = new ArrayList<>();
                        errorsToIDs.put(eachError.message, idList);
                    }
                    idList.add(eachError.detailChartID);
                    foundErrors.add(eachError.message);
                    foundIDs.add(eachError.detailChartID);
                }
            }

            // check if we have the expected number of unique chart id's
            assertEquals("There should be " + expectedChartCount + " unique error details chart id's", expectedChartCount, foundIDs.size());

            int sameErrorMessageCout = foundIDs.size() - foundErrors.size();
            int expectedErrorMessagesCount = expectedChartCount - sameErrorMessageCout;
            // check if we have the expected number of unique error types
            assertEquals("There should be " + expectedErrorMessagesCount + " unique error details types", expectedErrorMessagesCount,
                         foundErrors.size());

            // there should be the top N error details chart id's present
            for (int i = 1; i <= expectedErrorMessagesCount; i++)
            {
                assertTrue("Expected chart for error " + i, foundErrors.contains("error " + i));
            }

            // check if the expected number of error details charts are created
            List<String> fileNames = Utils.getTransactionErrorDetailsChartFileNames();
            assertEquals("Expected " + expectedChartCount + " error details charts", expectedChartCount, fileNames.size());

            // there should be the top N error details charts be created
            for (int i = 1; i <= expectedErrorMessagesCount; i++)
            {
                String errorType = "error " + i;
                List<Integer> ids = errorsToIDs.get(errorType);
                for (Integer eachId : ids)
                {
                    assertTrue("Expected error details chart for error " + errorType, fileNames.contains("d" + eachId + ".png"));
                }
            }
        }

        /**
         * Common validator to validate the transaction error overview charts output after an error report provider was
         * executed. See also @ #runReportGeneration(ErrorsReportProvider, List)} . Check if the report data structure
         * contains the expected number of only valid data entries and if the top N error types are present. Also verify
         * that the expected number of charts are created for the top N error types.
         * 
         * @param reportData
         *            the created error report data structure to be validated
         * @param expectedChartCount
         *            the expected number of transaction error overview charts
         */
        public static void validateTransactionErrorCharts(ErrorsReport reportData, int expectedChartCount)
        {
            // check the expected number of transaction error chart
            assertEquals("There should be " + expectedChartCount + " transaction error chart id's.", expectedChartCount,
                         reportData.transactionErrorOverviewCharts.size());

            Set<String> foundErrors = new HashSet<>();
            Set<Integer> foundIDs = new HashSet<>();
            Map<String, Integer> errorsToIDs = new HashMap<>();
            for (TransactionOverviewChartReport eachTransactionErrorChartReport : reportData.transactionErrorOverviewCharts)
            {
                errorsToIDs.put(eachTransactionErrorChartReport.title, eachTransactionErrorChartReport.id);
                foundErrors.add(eachTransactionErrorChartReport.title);
                foundIDs.add(eachTransactionErrorChartReport.id);
            }

            // check if we have the expected number of unique chart id's
            assertEquals("There should be " + expectedChartCount + " unique transaction chart id's", expectedChartCount, foundIDs.size());

            // check if we have the expected number of unique error types
            assertEquals("There should be " + expectedChartCount + " unique transaction error types", expectedChartCount,
                         foundErrors.size());

            // there should be the top N transaction error types present
            for (int i = 1; i <= expectedChartCount; i++)
            {
                assertTrue("Expected chart for error " + i, foundErrors.contains("error " + i));
            }

            // check if the expected number of transaction error charts are created
            List<String> fileNames = Utils.getTransactionErrorChartFileNames();
            assertEquals("Expected " + expectedChartCount + " transaction error charts", expectedChartCount, fileNames.size());

            // there should be the top N transaction error charts be created
            for (int i = 1; i <= expectedChartCount; i++)
            {
                String errorType = "error " + i;
                int id = errorsToIDs.get(errorType);
                assertTrue("Expected transaction error chart for error type " + errorType, fileNames.contains("t" + id + ".png"));
            }
        }

        /**
         * Common validator to validate the request error overview charts output after an error report provider was
         * executed. See also @ #runReportGeneration(ErrorsReportProvider, List)} . Check if the report data structure
         * contains the expected number of only valid data entries and if the top N request error types are present.
         * Also verify that the expected number of charts are created for the top N request error types.
         * 
         * @param reportData
         *            the created error report data structure to be validated
         * @param expectedChartCount
         *            the expected number of transaction error overview charts
         */
        public static void validateRequestErrorCharts(ErrorsReport reportData, int expectedChartCount)
        {
            // check the expected number of request error chart id's
            assertEquals("There should be " + expectedChartCount + " request error chart id's.", expectedChartCount,
                         reportData.requestErrorOverviewCharts.size());

            Set<Integer> foundIDs = new HashSet<>();
            for (RequestErrorChartReport eachRequestErrorChartReport : reportData.requestErrorOverviewCharts)
            {
                foundIDs.add(eachRequestErrorChartReport.id);
            }

            // check if we have the expected number of unique chart id's
            assertEquals("There should be " + expectedChartCount + " unique request chart id's", expectedChartCount, foundIDs.size());

            if (expectedChartCount != 0)
            {
                // there should be the top N request error chart id's present
                assertTrue("Expected chart id for status code 0.", foundIDs.contains(0));
                for (int i = 500; i < 500 + expectedChartCount - 1; i++)
                {
                    assertTrue("Expected chart id for status code " + i, foundIDs.contains(i));
                }
            }

            // check if the expected number of request error charts are created
            List<String> fileNames = Utils.getRequestErrorChartFileNames();
            assertEquals("Expected " + expectedChartCount + " request error charts", expectedChartCount, fileNames.size());

            if (expectedChartCount > 0)
            {
                // there should be the top N request error charts be created
                assertTrue("Expected request error chart for response code 0", fileNames.contains("r0.png"));
                for (int i = 500; i < 500 + expectedChartCount - 1; i++)
                {
                    assertTrue("Expected request error chart for response code " + i, fileNames.contains("r" + i + ".png"));
                }
            }
        }

        /**
         * Get a set of test data containing request and transactions with and without errors. The number of transaction
         * errors and request errors in the data set is defined. See {@link #getTestTimerDataErrorDetailsCount()},
         * {@link #getTestTimerDataRequestErrorTypeCount()}, {@link #getTestTimerDataTransactionErrorTypeCount()}.
         * 
         * @param includeTransactionErrors
         *            if true then the data set will contain a specific number of transaction errors otherwise it will
         *            contain only successful transaction entries
         * @param includeRequestErrors
         *            if true then the data set will contain a specific number of request errors otherwise it will
         *            contain only successful request entries
         * @return a test data set containing request and transaction entries
         */
        public static List<TimerData> getTestTimerData(boolean includeTransactionErrors, boolean includeRequestErrors)
        {
            List<TimerData> data = new ArrayList<>();

            long startTime = GlobalClock.getInstance().getTime();

            int defaultErrorLimit = getDefaultTransactionErrorOverviewChartLimit();
            for (int i = 0; i < defaultErrorLimit; i++)
            {
                data.addAll(getTestTimerDataSet("Action1", startTime, 150, includeRequestErrors, includeTransactionErrors ? "error 1"
                                                                                                                         : null));
                data.addAll(getTestTimerDataSet("Action1", startTime, 150, includeRequestErrors, includeTransactionErrors ? "error 1"
                                                                                                                         : null));
                data.addAll(getTestTimerDataSet("Action2", startTime, 150, includeRequestErrors, includeTransactionErrors ? "error 1"
                                                                                                                         : null));
                data.addAll(getTestTimerDataSet("Action2", startTime, 150, false, includeTransactionErrors ? "error 2" : null));
                data.addAll(getTestTimerDataSet("Action3", startTime, 150, false, includeTransactionErrors ? "error 2" : null));
                data.addAll(getTestTimerDataSet("Action3", startTime, 150, false, includeTransactionErrors ? "error 3" : null));
                data.addAll(getTestTimerDataSet("Action3", startTime, 150, false, null));
            }

            int errorStartIndex = 4;
            int occurrenceCount = defaultErrorLimit - 1;
            int count = errorStartIndex + (defaultErrorLimit * 2);
            for (int i = errorStartIndex; i < count; i++)
            {
                if (occurrenceCount == 0)
                {
                    data.addAll(getTestTimerDataSet("Action" + i, startTime, 150, false, includeTransactionErrors ? "error " + i : null));
                    data.addAll(getTestTimerDataSet("Action" + i, startTime, 150, false, null));
                }
                else
                {
                    for (int j = 0; j < occurrenceCount; j++)
                    {
                        data.addAll(getTestTimerDataSet("Action" + i, startTime, 150, false, includeTransactionErrors ? "error " + i : null));
                        data.addAll(getTestTimerDataSet("Action" + i, startTime, 150, false, null));
                    }
                    occurrenceCount--;
                }
            }

            return data;
        }

        private static List<TimerData> getTestTimerDataSet(String actionName, long startTime, long runTime, boolean includeRequestErrors,
                                                           String stackTrace)
        {
            List<TimerData> timerData = new ArrayList<>();

            timerData.add(getTestTransactionData(actionName, startTime, runTime, stackTrace));

            // request entries
            // add status code 0 as most often occurred request error
            if (includeRequestErrors)
            {
                for (int i = 0; i < 102; i++)
                {
                    timerData.add(getTestRequestData(actionName, 0, startTime, runTime));
                }
            }
            timerData.add(getTestRequestData(actionName, 1, startTime, runTime));
            timerData.add(getTestRequestData(actionName, 199, startTime, runTime));
            timerData.add(getTestRequestData(actionName, 200, startTime, runTime));
            timerData.add(getTestRequestData(actionName, 201, startTime, runTime));
            timerData.add(getTestRequestData(actionName, 299, startTime, runTime));
            timerData.add(getTestRequestData(actionName, 300, startTime, runTime));
            timerData.add(getTestRequestData(actionName, 301, startTime, runTime));
            timerData.add(getTestRequestData(actionName, 399, startTime, runTime));
            timerData.add(getTestRequestData(actionName, 400, startTime, runTime));
            timerData.add(getTestRequestData(actionName, 401, startTime, runTime));
            timerData.add(getTestRequestData(actionName, 499, startTime, runTime));

            if (includeRequestErrors)
            {
                // add error code 500 to 599 while 500 is the second most often occurred response error code and every
                // following is the next most often occurred one while 599 is the less often occurred of the 5xx codes
                int occurrenceCount = 100;
                for (int i = 500; i < 600; i++)
                {
                    for (int j = 0; j < occurrenceCount; j++)
                    {
                        timerData.add(getTestRequestData(actionName, i, startTime, runTime));
                    }
                    occurrenceCount--;
                }
            }
            return timerData;
        }

        private static TransactionData getTestTransactionData(String actionName, long startTime, long runTime, String stackTrace)
        {
            TransactionData data = new TransactionData();
            data.setName(actionName);
            data.setTransactionName("Transaction-" + actionName);
            data.setTime(startTime);
            data.setRunTime(runTime);

            if (StringUtils.isNotBlank(stackTrace))
            {
                data.setFailedActionName(actionName);
                data.setFailed(true);
                data.setFailureStackTrace("foo.bar.Exception : " + stackTrace + "\n a stack trace " + stackTrace);
            }

            return data;
        }

        private static RequestData getTestRequestData(String actionName, int statusCode, long startTime, long runTime)
        {
            RequestData data = new RequestData();
            data.setName(actionName);
            data.setFailed(statusCode == 0 || statusCode >= 500);
            data.setResponseCode(statusCode);
            data.setTime(startTime);
            data.setRunTime(runTime);

            return data;
        }
    }
}
