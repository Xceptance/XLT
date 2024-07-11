package com.xceptance.xlt.report.scorecard;

import java.io.File;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.util.Console;
import com.xceptance.common.util.ProcessExitCodes;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.XltEngine;
import com.xceptance.xlt.engine.XltExecutionContext;
import com.xceptance.xlt.report.ReportGenerator;
import com.xceptance.xlt.util.Timer;

public final class UpdateMain
{
    /** Class logger */
    private static final Logger LOG = LoggerFactory.getLogger(UpdateMain.class);

    public static void main(final String[] args) throws Throwable
    {
        Locale.setDefault(Locale.US);

        XltLogger.reportLogger.info(Console.horizontalBar());
        XltLogger.reportLogger.info(Console.startSection("XLT Scorecard Update"));
        XltLogger.reportLogger.info(Console.endSection());

        final UpdateMain main = new UpdateMain();
        try
        {
            main.init(args);
        }
        catch (final Exception e)
        {
            final String errorMessage = "Failed to initialize report generator";
            System.err.println(errorMessage + ": " + e.getMessage());
            LOG.error(errorMessage, e);

            main.printUsage();
            System.exit(ProcessExitCodes.PARAMETER_ERROR);
        }

        try
        {

            main.run();

            System.exit(ProcessExitCodes.SUCCESS);
        }
        catch (final Exception e)
        {
            final String errorMessage = "Failed to update scorecard";
            System.err.println(errorMessage + ": " + e.getMessage());
            LOG.error(errorMessage, e);

            System.exit(ProcessExitCodes.GENERAL_ERROR);
        }

    }

    private final Options options;

    private File inputDir;

    private File outputDir;

    private UpdateMain()
    {
        options = createCommandLineOptions();
    }

    private void init(final String[] arguments) throws ParseException
    {
        XltLogger.reportLogger.info(Console.horizontalBar());
        XltLogger.reportLogger.info(Console.startSection("Initializing..."));

        final Timer timer = Timer.start();

        XltEngine.get();

        final CommandLine cmdLine = new DefaultParser().parse(options, arguments);

        final String outputDirName = cmdLine.getOptionValue(XltConstants.COMMANDLINE_OPTION_OUTPUT_DIR);
        if (outputDirName != null)
        {
            outputDir = new File(outputDirName);
        }

        final String[] remainingArguments = cmdLine.getArgs();
        if (remainingArguments.length != 1)
        {
            throw new IllegalArgumentException("Please specify a single report directory");
        }

        inputDir = new File(remainingArguments[0]);

        XltExecutionContext.getCurrent().setTestSuiteHomeDir(inputDir);
        XltExecutionContext.getCurrent().setTestSuiteConfigDir(new File(inputDir, XltConstants.CONFIG_DIR_NAME));

        XltLogger.reportLogger.info(timer.stop().get("...finished"));
        XltLogger.reportLogger.info(Console.endSection());
    }

    private void run() throws Exception
    {
        XltLogger.reportLogger.info(Console.horizontalBar());
        XltLogger.reportLogger.info(Console.startSection("Setup..."));

        final Timer timer = Timer.start();

        final ReportGenerator reportGenerator = new ReportGenerator(VFS.getManager().resolveFile(inputDir.toURI()), outputDir, true);

        XltLogger.reportLogger.info(timer.stop().get("...finished"));
        XltLogger.reportLogger.info(Console.endSection());

        reportGenerator.updateScorecard();
    }

    private void printUsage()
    {
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setSyntaxPrefix("Usage: ");
        helpFormatter.setWidth(79);

        System.out.println();
        helpFormatter.printHelp("update_scorecard (<options>) reportdir", "Updates the scorecard of the given report\n\nOptions:\n",
                                options, null);
        System.out.println();
    }

    private static Options createCommandLineOptions()
    {
        final Options opts = new Options();
        final Option targetDir = new Option(XltConstants.COMMANDLINE_OPTION_OUTPUT_DIR, true, "the output directory");
        targetDir.setArgName("dir");
        opts.addOption(targetDir);

        return opts;
    }
}
