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
package com.xceptance.xlt.report;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;

import com.xceptance.common.util.ParseUtils;
import com.xceptance.common.util.ProcessExitCodes;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.XltExecutionContext;

/**
 * Command line frontend of the report generator.
 */
public class ReportGeneratorMain
{
    /**
     * Class logger.
     */
    private static final Log log = LogFactory.getLog(ReportGeneratorMain.class);

    /**
     * Program entry point.
     * 
     * @param args
     *            command line arguments
     */
    public static void main(final String[] args)
    {
        Locale.setDefault(Locale.US);

        final ReportGeneratorMain main = new ReportGeneratorMain();

        try
        {
            main.init(args);
        }
        catch (final Exception ex)
        {
            System.err.println("Failed to initialize report generator: " + ex.getMessage());
            log.fatal("Failed to initialize report generator.", ex);
            main.printUsageInfo();
            System.exit(ProcessExitCodes.PARAMETER_ERROR);
        }

        try
        {
            main.run();

            log.info("Report generated successfully.");

            System.exit(ProcessExitCodes.SUCCESS);
        }
        catch (final Exception ex)
        {
            System.err.println("Failed to run report generator: " + ex.getMessage());
            log.fatal("Failed to run report generator.", ex);
            System.exit(ProcessExitCodes.GENERAL_ERROR);
        }
    }

    /**
     * Name of the 'timezone' command-line option that can be used to override the default one.
     */
    private static final String OPTION_TIMEZONE = "timezone";

    /**
     * Name of property definition option.
     */
    private static final String OPTION_PROPERTY_DEFINITION = "D";

    /**
     * Name of result browser link generation override option.
     */
    private static final String OPTION_LINK2RESULTS = "linkToResults";

    /**
     * The name of the command-line option to specify the test cases to be included in the report.
     */
    private static final String OPTION_TEST_CASE_INCLUDES = "i";

    /**
     * The name of the command-line option to specify the test cases to be excluded from the report.
     */
    private static final String OPTION_TEST_CASE_EXCLUDES = "e";

    /**
     * The name of the command-line option to specify which agents will be included in the report.
     */
    private static final String OPTION_AGENTS_INCLUDE = "ai";

    /**
     * The name of the command-line option to specify which agents will be excluded in the report.
     */
    private static final String OPTION_AGENTS_EXCLUDE = "ae";

    /**
     * The name of the command-line option to disable the generation of agent charts.
     */
    private static final String OPTION_NO_AGENT_CHARTS = "noAgentCharts";

    /**
     * Boolean indicating if links to result browsers should be generated. Acts as an override, thus if
     * <code>null</code> the configured value of the property
     * <code>com.xceptance.xlt.reportgenerator.linkToResultBrowser</code> takes effect.
     */
    private Boolean linkToResults;

    /**
     * Flag which indicates if no charts should be generated.
     */
    private boolean noCharts;

    /**
     * Flag which indicates if no agents charts should be generated.
     */
    private boolean noAgentCharts;

    /**
     * Exclude ram-up period.
     */
    private boolean noRampUp;

    /**
     * Start time of evaluation period.
     */
    private long fromTime;

    /**
     * Whether 'fromTime' is a relative time value.
     */
    private boolean fromTimeRel;

    /**
     * End time of evaluation period.
     */
    private long toTime;

    /**
     * Whether 'toTime' is a relative time value.
     */
    private boolean toTimeRel;

    /**
     * The duration of evaluation period.
     */
    private long duration;

    /**
     * Report's input directory.
     */
    private FileObject inputDir;

    /**
     * Report's output directory.
     */
    private File outputDir;

    /**
     * Command line property file.
     */
    private File overridePropertyFile;

    /**
     * The pattern list specifying the test cases to be included in the report.
     */
    private String testCaseIncludePatternList;

    /**
     * The pattern list specifying the test cases to be excluded from the report.
     */
    private String testCaseExcludePatternList;

    /**
     * The pattern list specifying the agents to be included in the report.
     */
    private String agentIncludePatternList;

    /**
     * The pattern list specifying the agents to be excluded from the report.
     */
    private String agentExcludePatternList;

    /**
     * The properties defined on the command line using the "-D" option.
     */
    private Properties commandLineProperties;

    /**
     * The command line options.
     */
    private final Options options;

    /**
     * Constructor.
     */
    public ReportGeneratorMain()
    {
        options = createCommandLineOptions();
    }

    /**
     * Creates and returns the command line options.
     * 
     * @return command line options
     */
    private Options createCommandLineOptions()
    {
        final Options options = new Options();

        final Option targetDir = new Option(XltConstants.COMMANDLINE_OPTION_OUTPUT_DIR, true, "the output directory");
        targetDir.setArgName("dir");
        options.addOption(targetDir);

        final Option startTime = new Option(XltConstants.COMMANDLINE_OPTION_FROM, true, "ignore results generated before the given time");
        startTime.setArgName("time");
        options.addOption(startTime);

        final Option endTime = new Option(XltConstants.COMMANDLINE_OPTION_TO, true, "ignore results generated after the given time");
        endTime.setArgName("time");
        options.addOption(endTime);

        final Option duration = new Option(XltConstants.COMMANDLINE_OPTION_DURATION,
                                           true,
                                           "use results generated in the specified duration, must be used with option '" +
                                                 XltConstants.COMMANDLINE_OPTION_FROM + "' or option '" +
                                                 XltConstants.COMMANDLINE_OPTION_TO + "'");
        duration.setArgName("duration");
        options.addOption(duration);

        final Option propertyFilename = new Option(XltConstants.COMMANDLINE_OPTION_PROPERTY_FILENAME, true,
                                                   "property file that overrides the basic properties");
        propertyFilename.setArgName("property file");
        options.addOption(propertyFilename);

        final Option propertyDefinition = new Option(OPTION_PROPERTY_DEFINITION, true, "override a property from file");
        propertyDefinition.setValueSeparator('=');
        propertyDefinition.setArgName("property=value");
        propertyDefinition.setArgs(2);
        options.addOption(propertyDefinition);

        final Option timeZone = new Option(OPTION_TIMEZONE, true, "override the default timezone");
        timeZone.setArgName("timezoneId");
        options.addOption(timeZone);

        final Option link2Results = new Option(OPTION_LINK2RESULTS, true, "whether or not to link to result browsers");
        link2Results.setArgName("yes|no");
        options.addOption(link2Results);

        final Option noRampUp = new Option(XltConstants.COMMANDLINE_OPTION_NO_RAMPUP, false, "whether or not to exclude ramp-up period");
        options.addOption(noRampUp);

        final Option noCharts = new Option(XltConstants.COMMANDLINE_OPTION_NO_CHARTS, "no-charts", false,
                                           "disables generation of all charts");
        options.addOption(noCharts);

        final Option noAgentCharts = new Option(OPTION_NO_AGENT_CHARTS, "no-agent-charts", false, "disables generation of agent charts");
        options.addOption(noAgentCharts);

        final Option includeScenarios = new Option(OPTION_TEST_CASE_INCLUDES, "include-testcases", true,
                                                   "comma-separated list of test cases to include");
        includeScenarios.setArgName("test cases");
        options.addOption(includeScenarios);

        final Option excludeScenarios = new Option(OPTION_TEST_CASE_EXCLUDES, "exclude-testcases", true,
                                                   "comma-separated list of test cases to exclude");
        excludeScenarios.setArgName("test cases");
        options.addOption(excludeScenarios);

        final Option includeAgents = new Option(OPTION_AGENTS_INCLUDE, "include-agents", true, "comma-separated list of agents to include");
        includeAgents.setArgName("agents");
        options.addOption(includeAgents);

        final Option excludeAgents = new Option(OPTION_AGENTS_EXCLUDE, "exclude-agents", true, "comma-separated list of agents to exclude");
        excludeAgents.setArgName("agents");
        options.addOption(excludeAgents);

        return options;
    }

    /**
     * Parses the given arguments.
     * 
     * @param args
     *            command line arguments
     */
    public void init(final String[] args) throws Exception
    {
        final CommandLine commandLine = new DefaultParser().parse(options, args);

        // get command line options
        noCharts = commandLine.hasOption(XltConstants.COMMANDLINE_OPTION_NO_CHARTS);
        noAgentCharts = commandLine.hasOption(OPTION_NO_AGENT_CHARTS);
        noRampUp = commandLine.hasOption(XltConstants.COMMANDLINE_OPTION_NO_RAMPUP);

        if (commandLine.hasOption(OPTION_LINK2RESULTS))
        {
            linkToResults = Boolean.valueOf("yes".equalsIgnoreCase(commandLine.getOptionValue(OPTION_LINK2RESULTS)));
        }
        else
        {
            linkToResults = null;
        }

        final String overridePropertyFileName = commandLine.getOptionValue(XltConstants.COMMANDLINE_OPTION_PROPERTY_FILENAME);
        final String outputDirName = commandLine.getOptionValue(XltConstants.COMMANDLINE_OPTION_OUTPUT_DIR);

        final String timezone = commandLine.getOptionValue(OPTION_TIMEZONE);
        if (StringUtils.isNotBlank(timezone))
        {
            final TimeZone tz = TimeZone.getTimeZone(timezone);
            if (!tz.equals(TimeZone.getDefault()))
            {
                TimeZone.setDefault(tz);
            }
        }

        final String fromOption = commandLine.getOptionValue(XltConstants.COMMANDLINE_OPTION_FROM);
        final String toOption = commandLine.getOptionValue(XltConstants.COMMANDLINE_OPTION_TO);
        final String durationOption = commandLine.getOptionValue(XltConstants.COMMANDLINE_OPTION_DURATION);

        fromTime = parseTimeOption(fromOption, XltConstants.COMMANDLINE_OPTION_FROM, 0);
        toTime = parseTimeOption(toOption, XltConstants.COMMANDLINE_OPTION_TO, Long.MAX_VALUE);

        if (durationOption != null)
        {
            if (!(fromOption != null ^ toOption != null))
            {
                // both -to/-from given or none of them
                throw new IllegalArgumentException(String.format("The option '-%s' must be used with either '-%s' or '-%s'.",
                                                                 XltConstants.COMMANDLINE_OPTION_DURATION,
                                                                 XltConstants.COMMANDLINE_OPTION_FROM, XltConstants.COMMANDLINE_OPTION_TO));
            }

            duration = parseDurationOption(durationOption, 0);
            if (duration < 0)
            {
                throw new IllegalArgumentException("The specified duration '" + duration + "' must be a positive value.");
            }
        }
        else
        {
            duration = -1;
        }

        // get input and output directory
        final String[] remainingArgs = commandLine.getArgs();
        if (remainingArgs.length != 1)
        {
            throw new IllegalArgumentException("Please specify a single input directory.");
        }

        final String inputDirURI = getInputDirURI(remainingArgs[0]);
        inputDir = VFS.getManager().resolveFile(inputDirURI);

        // non-archive URIs use simple 'file' protocol
        final boolean isArchiveFile = !inputDirURI.startsWith("file:");

        // special handling for archives: use archived root directory as input if present
        if (isArchiveFile)
        {
            // get 1st level content of inputDir
            final FileObject[] childs = inputDir.getChildren();

            // check if 1st level content of inputDir is a single directory
            // if so, take the sub directory as inputDir
            if (childs.length == 1 && childs[0].getType() == FileType.FOLDER)
            {
                inputDir = childs[0];
            }
        }

        XltExecutionContext.getCurrent().setTestSuiteHomeDir(new File(inputDir.getName().getPath()));

        // set output directory
        if (outputDirName != null)
        {
            outputDir = new File(outputDirName);
        }

        /*
         * Starting with XLT 4.3.0 the load test configuration resides in a separate sub-directory named "config". We
         * have to fall back to given input directory in case such sub-directory does not exist, is not a directory or
         * is not readable.
         */
        final FileObject configDir = inputDir.getChild(XltConstants.RESULT_CONFIG_DIR);
        if (configDir != null && configDir.exists() && configDir.isReadable() && configDir.getType().equals(FileType.FOLDER))
        {
            // set configuration context
            XltExecutionContext.getCurrent().setTestSuiteConfigDir(configDir);
        }
        else
        {
            XltExecutionContext.getCurrent().setTestSuiteConfigDir(inputDir);
        }

        if (StringUtils.isNotBlank(overridePropertyFileName))
        {
            overridePropertyFile = new File(overridePropertyFileName);
        }

        commandLineProperties = commandLine.getOptionProperties(OPTION_PROPERTY_DEFINITION);

        // get result browser link generation override and set property appropriately
        if (linkToResults != null)
        {
            commandLineProperties.setProperty("com.xceptance.xlt.reportgenerator.linkToResultBrowsers",
                                              Boolean.toString(linkToResults.booleanValue()));
        }

        // get test case include/exclude patterns
        testCaseIncludePatternList = commandLine.getOptionValue(OPTION_TEST_CASE_INCLUDES);
        testCaseExcludePatternList = commandLine.getOptionValue(OPTION_TEST_CASE_EXCLUDES);

        // get agent include/exclude patterns
        agentIncludePatternList = commandLine.getOptionValue(OPTION_AGENTS_INCLUDE);
        agentExcludePatternList = commandLine.getOptionValue(OPTION_AGENTS_EXCLUDE);
    }

    /**
     * Generates the report.
     * 
     * @throws Exception
     */
    public void run() throws Exception
    {
        final ReportGenerator reportGenerator = new ReportGenerator(inputDir, outputDir, noCharts, noAgentCharts, overridePropertyFile,
                                                                    commandLineProperties, testCaseIncludePatternList,
                                                                    testCaseExcludePatternList, agentIncludePatternList,
                                                                    agentExcludePatternList);

        reportGenerator.generateReport(fromTime, toTime, duration, noRampUp, fromTimeRel, toTimeRel);
    }

    /**
     * Parses the given value as a date/time value.
     * 
     * @param optionValue
     *            the value to parse
     * @param optionName
     *            the option name
     * @param defaultValue
     *            the default value, if the option is not given on the command line
     * @return the date/time value as a long
     * @throws java.text.ParseException
     *             if the option value cannot be parsed as a date/time
     */
    private long parseTimeOption(String optionValue, final String optionName, final long defaultValue) throws ParseException
    {
        // Short-cut: No value given -> return passed default value
        if (optionValue == null)
        {
            return defaultValue;
        }

        // (1) Try to parse given option value as if it was given in full date format.
        {
            optionValue = optionValue.trim();
            // check, if the time option is set as a date value
            try
            {
                final SimpleDateFormat dateParser = new SimpleDateFormat(XltConstants.COMMANDLINE_DATE_FORMAT);
                dateParser.setLenient(false);

                return dateParser.parse(optionValue).getTime();
            }
            catch (final ParseException e)
            {
                // do nothing
            }
        }

        // (2) Now check if it was given in offset format and parse its value.
        // IMPORTANT: Remember that the appropriate option describes a relative time.
        {
            if (RegExUtils.isMatching(optionValue, "^[+-]") && !RegExUtils.isMatching(optionValue, "^[+-]\\d+$"))
            {
                try
                {
                    final long time = ParseUtils.parseRelativeTimePeriod(optionValue) * 1000L;

                    // the 'from time' value is relative
                    if (optionName.equals(XltConstants.COMMANDLINE_OPTION_FROM))
                    {
                        fromTimeRel = true;
                    }
                    // the 'to time' value is absolute when not given (internal Long.MAX) and relative otherwise
                    else if (optionName.equals(XltConstants.COMMANDLINE_OPTION_TO))
                    {
                        toTimeRel = true;
                    }

                    return time;
                }
                catch (final ParseException e)
                {
                    // do nothing
                }
            }
        }

        // finally throw an exception if the option value could not be parsed
        throw new ParseException(String.format("Unknown format of time period '%s'.\n\nPlease pass time in one of the following formats:\nAbsolute\n\t" +
                                               XltConstants.COMMANDLINE_DATE_FORMAT +
                                               "\nRelative:\n\t+1s, -2m, -3h, +1h2m3s, ...\n\t+1:00:00, -0:30:00\nPositive (+) marked time is added to test start time, negative (-) marked time is offset to test end time. ",
                                               optionValue),
                                 0);
    }

    /**
     * Parses the given option value as total number of milliseconds.
     * 
     * @param optionValue
     *            the value to parse
     * @param defaultValue
     *            the default value
     * @return the parsed value as total numbers of milliseconds
     * @throws ParseException
     */
    private long parseDurationOption(final String optionValue, final long defaultValue) throws ParseException
    {
        if (optionValue == null)
        {
            return defaultValue;
        }
        return ParseUtils.parseRelativeTimePeriod(optionValue) * 1000L;
    }

    /**
     * Prints the usage information text plus the list of options to stdout.
     */
    private void printUsageInfo()
    {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.setSyntaxPrefix("Usage: ");
        formatter.setWidth(79);

        final String usageInfo = XltConstants.REPORT_EXECUTABLE_NAME + " [<options>] <inputdir>";

        System.out.println();
        formatter.printHelp(usageInfo, "\nOptions:",
                            options,
                            "\nNotes:\n" + "<time> is to be specified in the format '" + XltConstants.COMMANDLINE_DATE_FORMAT +
                                     "'. Alternatively, a relative time value can be given, such as '+1h15m', '+1:15:00', or '-30m'. The +/- sign is mandatory here.\n" +
                                     "<duration> is to be specified as a time period value, such as '1h15m' or '1:15:00'.\n" +
                                     "<inputdir> can also be an archive of one of the following types: ZIP, JAR, TAR and compressed TAR (BZIP or GZIP).\n" +
                                     "<test cases> is a comma-separated list of test case names, but regular expressions are supported as well.");
        System.out.println();
    }

    /**
     * Returns the URI of the given input directory whereas the archive's type is determined by the appropriate filename
     * suffix of the input directory.
     * 
     * @param input
     *            input directory
     * @return URI of given input directory
     */
    private String getInputDirURI(final String input)
    {
        final String inputDirectory = StringUtils.isEmpty(input) ? "." : input;
        final String inputDirURI = new File(inputDirectory).toURI().toString();

        for (final ArchiveType type : ArchiveType.values())
        {
            if (type.accept(inputDirURI))
            {
                return type.getProtocol() + inputDirURI;
            }
        }

        return inputDirURI;
    }

    /**
     * Enumeration of archive types.
     */
    private enum ArchiveType
    {
        tbz2("tar.bz|tar.bz2|tbz2|tbz"), tgz("tar.gz|tgz"), tar, jar, zip;

        /**
         * File suffixes separated by '|'.
         */
        private final String fileSuffix;

        /**
         * Default constructor.
         */
        private ArchiveType()
        {
            this(null);
        }

        /**
         * Creates a new archive type.
         * 
         * @param suffix
         *            file suffixes of new archive type
         */
        private ArchiveType(final String suffix)
        {
            fileSuffix = suffix;
        }

        /**
         * Returns the protocol of this archive type.
         * 
         * @return protocol of this archive type
         */
        public String getProtocol()
        {
            return name() + ":";
        }

        /**
         * Returns the file suffixes as '|' separated list of strings.
         * 
         * @return file suffixes
         */
        public String getFileSuffixes()
        {
            return (fileSuffix == null) ? name() : fileSuffix;
        }

        /**
         * Determines whether or not the given file name is accepted by this archive type.
         * 
         * @param fileName
         *            name of file to be checked
         * @return <code>true</code> if this archive accepts the given file name, <code>false</code> otherwise
         */
        public boolean accept(final String fileName)
        {
            if (fileName != null && fileName.length() > 0)
            {
                final String s = fileName.toLowerCase();
                for (final String suffix : getFileSuffixes().split("\\|"))
                {
                    if (s.endsWith("." + suffix))
                    {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    long getFromTime()
    {
        return fromTime;
    }

    long getToTime()
    {
        return toTime;
    }

    long getDuration()
    {
        return duration;
    }

    boolean isFromTimeRel()
    {
        return fromTimeRel;
    }

    boolean isToTimeRel()
    {
        return toTimeRel;
    }
}
