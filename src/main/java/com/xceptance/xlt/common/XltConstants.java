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
package com.xceptance.xlt.common;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Collection of global constants for directories, default values and so on. Some of the values will be overwritten with
 * custom values later on. Named XltConstants, because there are too many other Constants classes in other packages.
 *
 * @author Rene Schwietzke (Xceptance Software Technologies GmbH)
 */
public final class XltConstants
{
    /**
     * Constructor Private, because we do not want to have instances of this class
     */
    private XltConstants()
    {
    }

    /*
     * Commons configuration values
     */

    /**
     * The product's name.
     */
    public static final String PRODUCT_NAME = "Xceptance LoadTest";

    /**
     * The product's logogram.
     */
    public static final String PRODUCT_LOGOGRAM = "XLT";

    /**
     * The product's vendor.
     */
    public static final String PRODUCT_VENDOR_NAME = "Xceptance Software Technologies GmbH";

    /**
     * The product url to refer to
     */
    public static final String PRODUCT_URL = "http://www.xceptance-loadtest.com/";

    /**
     * The xlt package and domain path. Mainly for properties and class lookup
     */
    public static final String XLT_PACKAGE_PATH = "com.xceptance.xlt";

    /**
     * The extension of property files
     */
    public static final String PROPERTY_FILE_EXTENSION = ".properties";

    /**
     * The extension of config files, such as jvmargs.cfg
     */
    public static final String CFG_FILE_EXTENSION = ".cfg";

    /**
     * The extension of XML files, such as log4j2.xml.
     */
    public static final String XML_FILE_EXTENSION = ".xml";

    /**
     * The name of the system property which holds the agent configuration directory.
     */
    public static final String CONFIG_DIR_PROPERTY = XLT_PACKAGE_PATH + ".agent.config";

    /**
     * The name of the file holding secret properties
     */
    public static final String SECRET_PROPERTIES_FILENAME = "secret" + PROPERTY_FILE_EXTENSION;

    /**
     * The prefix used to mark properties as secret
     */
    public static final String SECRET_PREFIX = "secret.";

    /**
     * The text to replace secret or masked properties with
     */
    public static final String MASK_PROPERTIES_HIDETEXT = "******";

    /**
     * The name of the property which holds the test-specific configuration file.
     */
    public static final String TEST_PROPERTIES_FILE_PATH_PROPERTY = XLT_PACKAGE_PATH + ".testPropertiesFile";

    /**
     * Property name of random generator's initial value.
     */
    public static final String RANDOM_INIT_VALUE_PROPERTY = XLT_PACKAGE_PATH + ".random.initValue";

    /**
     * Empty String.
     */
    public static final String EMPTYSTRING = "";

    /**
     * The utf-8 encoding string
     */
    public static final String UTF8_ENCODING = "UTF-8";

    /**
     * Proper XML header
     */
    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"" + UTF8_ENCODING + "\"?>\n";

    /**
     * The sub directory for the timers
     */
    public static final String REPORT_TIMER_DIR = "csv";

    /**
     * The mastercontroller property file
     */
    public static final String MASTERCONTROLLER_PROPERTY_FILENAME = "mastercontroller" + PROPERTY_FILE_EXTENSION;

    /**
     * The default agent property file
     */
    public static final String DEFAULT_PROPERTY_FILENAME = "default" + PROPERTY_FILE_EXTENSION;

    /**
     * The dev agent property file
     */
    public static final String DEV_PROPERTY_FILENAME = "dev" + PROPERTY_FILE_EXTENSION;

    /**
     * The project agent property file
     */
    public static final String PROJECT_PROPERTY_FILENAME = "project" + PROPERTY_FILE_EXTENSION;

    /**
     * The JVM parameters file name
     */
    public static final String JVM_PARAMETER_FILENAME = "jvmargs.cfg";

    /**
     * The name of the timer files.
     */
    public static final String TIMER_FILENAME = "timers.csv";

    /**
     * The possible name of the timer files.
     */
    public static final List<Pattern> TIMER_FILENAME_PATTERNS = Stream.of("^timers\\.csv$", "^timers\\.csv\\.gz$",
                                                                          "^timers\\.csv\\.[0-9]{4}-[0-9]{2}-[0-9]{2}$",
                                                                          "^timers\\.csv\\.[0-9]{4}-[0-9]{2}-[0-9]{2}\\.gz$")
                                                                      .map(Pattern::compile).collect(Collectors.toList());

    /**
     * The possible name of the CPT timer files.
     * <p>
     * Note: Needed for backward compatibility. Separate CPT timers files have been removed in XLT 4.8.
     */
    public static final List<Pattern> CPT_TIMER_FILENAME_PATTERNS = Stream.of("^timer-wd-.+\\.csv$", "^timer-wd-.+\\.csv\\.gz$")
                                                                          .map(Pattern::compile).collect(Collectors.toList());

    /**
     * The option name of the <em>from</em> option on the command line.
     */
    public static final String COMMANDLINE_OPTION_FROM = "from";

    /**
     * The option name of the <em>noRampUp</em> option on the command line.
     */
    public static final String COMMANDLINE_OPTION_NO_RAMPUP = "noRampUp";

    /**
     * The option name of the <em>noCharts</em> option on the command line.
     */
    public static final String COMMANDLINE_OPTION_NO_CHARTS = "noCharts";

    /**
     * The option name of the <em>o</em> option on the command line.
     */
    public static final String COMMANDLINE_OPTION_OUTPUT_DIR = "o";

    /**
     * The option name of the <em>to</em> option on the command line.
     */
    public static final String COMMANDLINE_OPTION_TO = "to";

    /**
     * The option name of the <em>duration</em> option on the command line.
     */
    public static final String COMMANDLINE_OPTION_DURATION = "l";

    /**
     * The option name of the <em>comment</em> option on the command line.
     */
    public static final String COMMANDLINE_OPTION_COMMENT = "comment";

    /**
     * The option name of the <em>pf</em> option on the command line.
     */
    public static final String COMMANDLINE_OPTION_PROPERTY_FILENAME = "pf";

    /**
     * The date format on the command line for filtering.
     */
    public static final String COMMANDLINE_DATE_FORMAT = "yyyyMMdd-HHmmss";

    /**
     * The date format of directories
     */
    public static final String DIRECTORY_DATE_FORMAT = COMMANDLINE_DATE_FORMAT;

    /**
     * The name of the configuration directory
     */
    public static final String CONFIG_DIR_NAME = "config";

    /**
     * The directory name of the common resources (CSS, JS, Images) for all test reports.
     */
    public static final String REPORT_RESOURCES_PATH = "testreport";

    /**
     * The date format used when parsing dates from test reports.
     */
    public static final String REPORT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.S z";

    /**
     * The name of the report directory. Can be overwritten.
     */
    public static final String REPORT_ROOT_DIR = "reports";

    /**
     * The name of the results directory. Can be overwritten.
     */
    public static final String RESULT_ROOT_DIR = "results";

    /**
     * The license file name.
     */
    public static final String LICENSE_FILENAME = "license.xml";

    /**
     * The name of the pages directory used for request dumping.
     */
    public static final String DUMP_PAGES_DIR = "pages";

    /**
     * The name of the responses directory used for request dumping.
     */
    public static final String DUMP_RESPONSES_DIR = "responses";

    /**
     * The name of the cache directory used for request dumping.
     */
    public static final String DUMP_CACHE_DIR = "cache";

    /**
     * The name of the output directory used for request dumping.
     */
    public static final String DUMP_OUTPUT_DIR = "output";

    /**
     * The property name for line number type.
     */
    public static final String LINE_NUMBER_TYPE_PROPERTY = XLT_PACKAGE_PATH + ".scripting.lineNumberType";

    /**
     * The property name for (test) project name.
     */
    public static final String PROJECT_NAME_PROPERTY = XLT_PACKAGE_PATH + ".projectName";

    /**
     * Name of the config directory in downloaded results directory.
     */
    public static final String RESULT_CONFIG_DIR = "config";

    /*
     * Mastercontroller configuration values
     */

    /**
     * The mastercontroller executable name
     */
    public static final String MASTERCONTROLLER_EXECUTABLE_NAME = "mastercontroller";

    /**
     * The default user name (for Basic Authentication) used to secure communication between Master Controller and Agent
     * Controller.
     */
    public static final String USER_NAME = "xlt";

    /*
     * General chart configuration values
     */
    /**
     * The name of the chart directory within a report
     */
    public static final String REPORT_CHART_DIR = "charts";

    /**
     * Placeholder file name for reports
     */
    public static final String REPORT_CHART_PLACEHOLDER_FILENAME = "placeholder.png";

    /**
     * The text on the placeholder chart image
     */
    public static final String REPORT_CHART_PLACEHOLDER_MESSAGE = "Loading chart...";

    /*
     * Load report configuration values
     */

    /**
     * The name of the property file for load reports
     */
    public static final String LOAD_REPORT_PROPERTY_FILENAME = "reportgenerator" + PROPERTY_FILE_EXTENSION;

    /**
     * The name of the xml data file for load test reports
     */
    public static final String LOAD_REPORT_XML_FILENAME = "testreport.xml";

    /**
     * The path of the directory with all xsl files for load report rendering
     */
    public static final String LOAD_REPORT_XSL_PATH = "xsl/loadreport";

    /**
     * The name of the chart directory within a load test report
     */
    public static final String LOAD_REPORT_CHART_DIR = REPORT_CHART_DIR;

    /**
     * The name of the executable to create a report
     */
    public static final String REPORT_EXECUTABLE_NAME = "create_report";

    /*
     * Diff report configuration values
     */

    /**
     * The name of the property file for diff reports
     */
    public static final String DIFF_REPORT_PROPERTY_FILENAME = "diffreportgenerator" + PROPERTY_FILE_EXTENSION;

    /**
     * The xml data file name for the diff report
     */
    public static final String DIFF_REPORT_XML_FILENAME = "diffreport.xml";

    /**
     * The html name of the final diff report
     */
    public static final String DIFF_REPORT_HTML_FILENAME = "index.html";

    /**
     * The path of the directory with all xsl files for diff report rendering
     */
    public static final String DIFF_REPORT_XSL_PATH = "xsl/diffreport";

    /**
     * The name of the xsl file for rendering the diff report
     */
    public static final String DIFF_REPORT_XSL_FILENAME = "index.xsl";

    /**
     * The name of the executable to create a diff report
     */
    public static final String DIFF_REPORT_EXECUTABLE_NAME = "create_diff_report";

    /*
     * Trend report configuration values
     */

    /**
     * The name of the property file for trend reports
     */
    public static final String TREND_REPORT_PROPERTY_FILENAME = "trendreportgenerator" + PROPERTY_FILE_EXTENSION;

    /**
     * The name of the executable to create trend report.
     */
    public static final String TREND_REPORT_EXECUTABLE_NAME = "create_trend_report";

    /**
     * The prefix of the trendreport directory
     */
    public static final String TREND_REPORT_DIR_PREFIX = "trendreport";

    /**
     * The date format used when generating trend report names.
     */
    public static final String TREND_REPORT_OUTPUT_DATE_FORMAT = "yyyyMMdd-HHmmss";

    /**
     * The path of the directory with all xsl files for report rendering
     */
    public static final String TREND_REPORT_XSL_PATH = "xsl/trendreport";

    /**
     * The xml data file name for trend reports
     */
    public static final String TREND_REPORT_XML_FILENAME = "trendreport.xml";

    /**
     * The name of the chart directory within a trend report
     */
    public static final String TREND_REPORT_CHART_DIR = REPORT_CHART_DIR;

    /**
     * The name of the property that denotes when the loadtest has started.
     */
    public static final String LOAD_TEST_START_DATE = XLT_PACKAGE_PATH + ".loadtest.start";

    /**
     * The name of the property that denotes how many milliseconds have elapsed since 'start'.
     */
    public static final String LOAD_TEST_ELAPSED_TIME = XLT_PACKAGE_PATH + ".loadtest.elapsed";

    /**
     * The name of the property that denotes how many milliseconds it took for all active test scenarios to finish their
     * ramp-up.
     */
    public static final String LOAD_TEST_RAMP_UP_PERIOD = XLT_PACKAGE_PATH + ".loadtest.rampUp";
}
