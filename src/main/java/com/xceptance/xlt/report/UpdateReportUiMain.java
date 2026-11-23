/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.util.Console;
import com.xceptance.common.util.ProcessExitCodes;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.XltEngine;
import com.xceptance.xlt.engine.XltExecutionContext;
import com.xceptance.xlt.report.diffreport.DiffReportGeneratorConfiguration;
import com.xceptance.xlt.report.modern.ModernComparisonReportGenerator;
import com.xceptance.xlt.report.modern.ModernReportGenerator;
import com.xceptance.xlt.report.trendreport.TrendReportGeneratorConfiguration;
import com.xceptance.xlt.report.util.ReportUtils;
import com.xceptance.xlt.util.Timer;

/**
 * Standalone command-line utility ({@code bin/update_report_ui.sh} / {@code .cmd}) that updates modern HTML/CSS/JS
 * templates and regenerates {@code js/report-meta.js} and all per-page {@code js/*-data.js} bundles directly from
 * {@code testreport.xml} (single-run load reports), or from {@code comparison-runs.json.gz} / {@code diffreport.xml} /
 * {@code trendreport.xml} (diff and trend reports), without re-reading raw timer logs.
 */
public final class UpdateReportUiMain
{
    private static final Logger LOG = LoggerFactory.getLogger(UpdateReportUiMain.class);

    private static final String OPTION_HELP = "help";

    private final Options options;

    private File reportDir;

    private boolean helpRequested;

    private Boolean linkToReportsOverride;

    private UpdateReportUiMain()
    {
        options = createCommandLineOptions();
    }

    public static void main(final String[] args)
    {
        Locale.setDefault(Locale.US);

        XltLogger.reportLogger.info(Console.horizontalBar());
        XltLogger.reportLogger.info(Console.startSection("XLT Report UI Update"));
        XltLogger.reportLogger.info(Console.endSection());

        final UpdateReportUiMain main = new UpdateReportUiMain();
        try
        {
            main.init(args);
        }
        catch (final Exception e)
        {
            final String errorMessage = "Failed to initialize report UI updater: " + e.getMessage();
            System.err.println(errorMessage);
            XltLogger.reportLogger.error(errorMessage);
            LOG.error(errorMessage, e);

            main.printUsage();
            System.exit(ProcessExitCodes.PARAMETER_ERROR);
        }

        if (main.helpRequested)
        {
            main.printUsage();
            System.exit(ProcessExitCodes.SUCCESS);
        }

        try
        {
            main.run();
            System.exit(ProcessExitCodes.SUCCESS);
        }
        catch (final Exception e)
        {
            final String errorMessage = "Failed to update report UI: " + e.getMessage();
            System.err.println(errorMessage);
            XltLogger.reportLogger.error(errorMessage);
            LOG.error(errorMessage, e);

            System.exit(ProcessExitCodes.GENERAL_ERROR);
        }
    }

    private void init(final String[] arguments) throws ParseException, FileNotFoundException
    {
        XltLogger.reportLogger.info(Console.horizontalBar());
        XltLogger.reportLogger.info(Console.startSection("Initializing..."));

        final Timer timer = Timer.start();

        XltEngine.get();

        final CommandLine cmdLine = new DefaultParser().parse(options, arguments);
        if (cmdLine.hasOption(OPTION_HELP))
        {
            helpRequested = true;
            return;
        }

        if (cmdLine.hasOption(XltConstants.COMMANDLINE_OPTION_LINK_TO_REPORTS))
        {
            linkToReportsOverride = Boolean.valueOf(ModernComparisonReportGenerator.parseLinkToReportsOptionValue(
                cmdLine.getOptionValue(XltConstants.COMMANDLINE_OPTION_LINK_TO_REPORTS)));
        }

        final List<String> nonBlankArgs = new ArrayList<>();
        for (final String arg : cmdLine.getArgs())
        {
            if (StringUtils.isNotBlank(arg))
            {
                nonBlankArgs.add(arg.trim());
            }
        }

        if (nonBlankArgs.size() != 1)
        {
            throw new IllegalArgumentException("Please specify a single existing report directory");
        }

        reportDir = new File(nonBlankArgs.get(0));
        if (!reportDir.isDirectory())
        {
            throw new FileNotFoundException("Report directory does not exist: " + reportDir.getAbsolutePath());
        }

        final File testReportXml = new File(reportDir, XltConstants.LOAD_REPORT_XML_FILENAME);
        final File comparisonSnapshot = new File(reportDir, XltConstants.COMPARISON_SNAPSHOT_FILENAME);
        final File diffReportXml = new File(reportDir, XltConstants.DIFF_REPORT_XML_FILENAME);
        final File trendReportXml = new File(reportDir, XltConstants.TREND_REPORT_XML_FILENAME);
        if (!testReportXml.isFile() && !comparisonSnapshot.isFile() && !diffReportXml.isFile() && !trendReportXml.isFile())
        {
            throw new FileNotFoundException(
                "Could not find '" + XltConstants.LOAD_REPORT_XML_FILENAME + "', '" + XltConstants.DIFF_REPORT_XML_FILENAME + "', '"
                    + XltConstants.TREND_REPORT_XML_FILENAME + "', or '" + XltConstants.COMPARISON_SNAPSHOT_FILENAME
                    + "' in directory '" + reportDir.getAbsolutePath() + "'");
        }

        XltLogger.reportLogger.info(timer.stop().get("...finished"));
        XltLogger.reportLogger.info(Console.endSection());
    }

    private void run() throws Exception
    {
        XltLogger.reportLogger.info(Console.horizontalBar());
        XltLogger.reportLogger.info(Console.startSection("Updating Modern Report UI..."));

        final Timer timer = Timer.start();

        File configDir = XltExecutionContext.getCurrent().getXltConfigDir();
        if (configDir == null || !configDir.isDirectory())
        {
            configDir = new File(XltExecutionContext.getCurrent().getXltHomeDir(), XltConstants.CONFIG_DIR_NAME);
        }

        final File testReportXml = new File(reportDir, XltConstants.LOAD_REPORT_XML_FILENAME);
        if (testReportXml.isFile())
        {
            final File templateDir = new File(configDir, XltConstants.MODERN_REPORT_RESOURCES_PATH);
            final boolean scorecardPresent = new File(reportDir, XltConstants.SCORECARD_REPORT_XML_FILENAME).isFile();

            final ModernReportGenerator generator = new ModernReportGenerator();
            generator.copyTemplateResources(templateDir, reportDir, scorecardPresent);
            generator.generateReportBundles(reportDir);
        }
        else
        {
            final boolean linkToReports = resolveLinkToReports(configDir, reportDir);
            new ModernComparisonReportGenerator().updateExistingReportDir(configDir, reportDir, linkToReports);
        }

        XltLogger.reportLogger.info(timer.stop().get("...finished"));
        XltLogger.reportLogger.info(Console.endSection());

        final File indexFile = new File(reportDir, "index.html");
        XltLogger.reportLogger.info("Report: {}", ReportUtils.toString(indexFile));
    }

    private boolean resolveLinkToReports(final File configDir, final File targetReportDir)
    {
        if (linkToReportsOverride != null)
        {
            return linkToReportsOverride.booleanValue();
        }
        final File homeDir = XltExecutionContext.getCurrent().getXltHomeDir();
        try
        {
            if (new File(targetReportDir, XltConstants.DIFF_REPORT_XML_FILENAME).isFile())
            {
                return new DiffReportGeneratorConfiguration(homeDir, configDir, null).isLinkToReports();
            }
            if (new File(targetReportDir, XltConstants.TREND_REPORT_XML_FILENAME).isFile())
            {
                return new TrendReportGeneratorConfiguration(homeDir, configDir, null).isLinkToReports();
            }
        }
        catch (final Exception e)
        {
            LOG.debug("Could not read linkToReports setting from config directory '{}'; defaulting to false", configDir, e);
        }
        return false;
    }

    private void printUsage()
    {
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setSyntaxPrefix("Usage: ");
        helpFormatter.setWidth(79);

        System.out.println();
        helpFormatter.printHelp("update_report_ui (<options>) <reportdir>",
                                "Regenerates the modern HTML/JS UI of an existing load, diff, or trend report directory\n\nOptions:\n",
                                options, null);
        System.out.println();
    }

    private static Options createCommandLineOptions()
    {
        final Options opts = new Options();
        opts.addOption(Option.builder("h").longOpt(OPTION_HELP).desc("print this usage help").build());
        final Option linkToReports = new Option(XltConstants.COMMANDLINE_OPTION_LINK_TO_REPORTS, true,
                                                "whether or not to link diff/trend reports to single-run reports (default: no)");
        linkToReports.setArgName("yes|no");
        opts.addOption(linkToReports);
        return opts;
    }
}

