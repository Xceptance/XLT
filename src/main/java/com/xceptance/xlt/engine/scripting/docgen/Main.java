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
package com.xceptance.xlt.engine.scripting.docgen;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import com.xceptance.common.util.ProcessExitCodes;
import com.xceptance.xlt.engine.scripting.ScriptException;
import com.xceptance.xlt.report.util.ReportUtils;

/**
 * Main class. This is the entry point for the Doc generator.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public final class Main
{

    /** (Short)Name of the output-directory option. */
    private static final String OUTPUT_OPTION = "o";

    /** Name of this program. */
    private static final String PROG_NAME = "create_scriptdoc";

    /** Name of the property-definition option. */
    private static final String PROPERTY_DEFINITION_OPTION = "D";

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception
    {
        final Options opts = makeOptions();
        CommandLine cli = null;
        try
        {
            cli = new DefaultParser().parse(opts, args);
        }
        catch (final Throwable t)
        {
            System.err.println(t.getMessage());
            printUsageAndExit(opts);
        }
        final String outputDirName = cli.getOptionValue(OUTPUT_OPTION);

        final String[] remainingArgs = cli.getArgs();
        if (remainingArgs.length == 0)
        {
            System.err.println("Please specify the path to your test suite.");
            printUsageAndExit(opts);
        }
        else if (remainingArgs.length > 1)
        {
            System.err.println("Too many arguments.");
            printUsageAndExit(opts);
        }

        final File suiteDir = new File(remainingArgs[0]);
        final File outputDir;

        if (cli.hasOption(OUTPUT_OPTION))
        {
            if (StringUtils.isBlank(outputDirName))
            {
                System.err.println("Please specify an output directory or omit option '-" + OUTPUT_OPTION + "'.");
                printUsageAndExit(opts);
            }

            outputDir = new File(outputDirName);
        }
        else
        {
            outputDir = new File(suiteDir, "scriptdoc");
        }

        ScriptDocGenerator docGen = null;
        try
        {
            docGen = new ScriptDocGenerator(suiteDir, outputDir, cli.getOptionProperties(PROPERTY_DEFINITION_OPTION));
        }
        catch (final Throwable t)
        {
            handleFailure(t, opts);
        }

        System.out.print("Generating Script-Doc ... ");
        final long startTime = System.currentTimeMillis();
        try
        {
            docGen.run();
        }
        catch (final Throwable t)
        {
            System.out.println("FAILED!\n");

            handleFailure(t, opts);
        }

        final long endTime = System.currentTimeMillis();
        final File indexFile = new File(outputDir, "index.html");
        System.out.printf("OK [took %d ms]\nURL: %s\n\n", (endTime - startTime), ReportUtils.toString(indexFile));

        System.exit(ProcessExitCodes.SUCCESS);
    }

    private static void handleFailure(final Throwable t, final Options opts)
    {
        System.err.println(t.getMessage());

        if (t instanceof ScriptException)
        {
            System.exit(ProcessExitCodes.GENERAL_ERROR);
        }

        printUsageAndExit(opts);

    }

    /**
     * Prints the usage help message to stdout.
     */
    private static void printUsageAndExit(final Options opts)
    {
        System.out.println();

        final String progName = PROG_NAME + getExtension();
        new HelpFormatter().printHelp(progName + " [-o <dir>] <test_suite>", opts);

        System.out.println();
        System.exit(ProcessExitCodes.PARAMETER_ERROR);
    }

    /**
     * @return
     */
    private static String getExtension()
    {
        return SystemUtils.IS_OS_WINDOWS ? ".cmd" : ".sh";
    }

    /**
     * @return
     */
    private static Options makeOptions()
    {
        final Options opts = new Options();

        final Option propertyDefinition = new Option(PROPERTY_DEFINITION_OPTION, true, "override a property with the given value");
        propertyDefinition.setValueSeparator('=');
        propertyDefinition.setArgName("property=value");
        propertyDefinition.setArgs(2);
        opts.addOption(propertyDefinition);

        final Option o = new Option(OUTPUT_OPTION, "output-directory", true, "the output directory");
        o.setArgName("dir");
        opts.addOption(o);

        return opts;
    }

}
