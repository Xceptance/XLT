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
package com.xceptance.xlt.agentcontroller;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.util.ProcessExitCodes;
import com.xceptance.xlt.engine.XltEngine;

/**
 * The Main class is the entry point to the XLT agent controller.
 */
public class AgentControllerMain
{
    private static final Logger log = LoggerFactory.getLogger(AgentControllerMain.class);

    private static final String OPTION_PROPERTY_DEFINITION = "D";

    public static void main(final String[] args)
    {
        // start engine to avoid later issues, stay silent about problems
        XltEngine.get();

        final CommandLine commandLine = parseCommandLine(args);
        final Properties commandLineProps = commandLine.getOptionProperties(OPTION_PROPERTY_DEFINITION);

        try
        {
            new AgentControllerImpl(commandLineProps);
        }
        catch (final Exception ex)
        {
            log.error("Error while starting agent controller", ex);
            // agent controller failed to start -> terminate VM with proper exit code
            System.exit(ProcessExitCodes.GENERAL_ERROR);
        }
    }

    private static CommandLine parseCommandLine(final String[] args)
    {
        final Options options = createCommandLineOptions();
        final CommandLineParser parser = new DefaultParser();

        try
        {
            return parser.parse(options, args);
        }
        catch (final ParseException ex)
        {
            printUsageInfoAndExit(options);

            // will never get here
            return null;
        }
    }

    private static Options createCommandLineOptions()
    {
        final Options options = new Options();

        final Option propertyDefinition = new Option(OPTION_PROPERTY_DEFINITION, true,
                                                     "override a property in file 'agentcontroller.properties'");
        propertyDefinition.setValueSeparator('=');
        propertyDefinition.setArgName("property=value");
        propertyDefinition.setArgs(2);
        options.addOption(propertyDefinition);

        return options;
    }

    private static void printUsageInfoAndExit(final Options options)
    {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.setSyntaxPrefix("Usage: ");
        formatter.setWidth(79);

        System.out.println();
        formatter.printHelp("agentcontroller [<options>]", "\nOptions:", options, "");
        System.out.println();

        System.exit(ProcessExitCodes.PARAMETER_ERROR);
    }
}
