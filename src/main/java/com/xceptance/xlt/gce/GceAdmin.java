/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.gce;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.util.ConsoleUiUtils;

/**
 * Runs gce_admin in either interactive or non-interactive mode.
 */
class GceAdmin
{
    /**
     * The log facility.
     */
    private static final Logger log = LoggerFactory.getLogger(GceAdmin.class);

    /**
     * The keys used to select the main functions.
     */
    private static final String[] OPERATION_KEYS =
        {
            "l", "c", "d", "q"
        };

    /**
     * The descriptions of the main functions.
     */
    private static final String[] OPERATION_DESCRIPTIONS =
        {
            "List running instances", "Create managed instance groups", "Delete managed instance groups", "Quit"
        };

    /**
     * The main functions.
     */
    static final String[] OPERATIONS =
        {
            "list", "create", "delete", "quit"
        };

    /**
     * The underlying GCE client.
     */
    private final GceClient gceClient;

    /**
     * Constructor.
     *
     * @throws IOException
     *             if the GCE client cannot be created
     * @throws GeneralSecurityException
     *             if the GCE client cannot be created
     */
    GceAdmin() throws IOException, GeneralSecurityException
    {
        final GceAdminConfiguration gceConfiguration = new GceAdminConfiguration();

        gceClient = new GceClient(gceConfiguration.getProjectId(), gceConfiguration.getInstanceConnectTimeout());
    }

    /**
     * Runs the application in interactive mode.
     */
    void startInteractiveMode()
    {
        while (true)
        {
            try
            {
                System.out.println();

                final String operation = selectOperation();

                System.out.println();

                if (operation.equals(OPERATIONS[0]))
                {
                    new OpListInstances(gceClient).execute();
                }
                else if (operation.equals(OPERATIONS[1]))
                {
                    new OpCreateInstanceGroup(gceClient).execute();
                }
                else if (operation.equals(OPERATIONS[2]))
                {
                    new OpDeleteInstanceGroup(gceClient).execute();
                }
                else if (operation.equals(OPERATIONS[3]))
                {
                    return;
                }

                System.out.println();
            }
            catch (final Exception e)
            {
                System.err.println("Failed to execute operation: " + e.getMessage());
                log.error("Failed to execute operation", e);
            }
        }
    }

    /**
     * Asks the user select an operation.
     *
     * @return the operation name
     */
    private String selectOperation()
    {
        return ConsoleUiUtils.selectItem("What do you want to do?", Arrays.asList(OPERATION_KEYS), Arrays.asList(OPERATION_DESCRIPTIONS),
                                         Arrays.asList(OPERATIONS));
    }

    /**
     * Runs the application in non-interactive mode.
     *
     * @param commandLine
     *            the command-line object with the parameters what to do
     * @throws IOException
     *             if anything goes wrong
     */
    void startNonInteractiveMode(final CommandLine commandLine) throws IOException
    {
        final List<String> args = commandLine.getArgList();

        final String command = args.get(0);

        final String outputFilePath = commandLine.getOptionValue("o");
        final File outputFile = StringUtils.isNotBlank(outputFilePath) ? new File(outputFilePath) : null;

        // do the right thing
        if (command.equals("list-by-label"))
        {
            if (args.size() != 3)
            {
                GceAdminUtils.dieWithMessage("Use 'gce_admin list-by-label <region-name> <name-label>'");
            }
            else
            {
                final String regionName = args.get(1);
                final String nameLabel = args.get(2);

                new OpListInstancesByNameLabelNonInteractively(gceClient).execute(regionName, nameLabel, outputFile);
            }
        }
        else if (command.equals("list-by-group"))
        {
            if (args.size() != 3)
            {
                GceAdminUtils.dieWithMessage("Use 'gce_admin list-by-group <region-name> <instance-group-name>'");
            }
            else
            {
                final String regionName = args.get(1);
                final String instanceGroupName = args.get(2);

                new OpListInstancesByGroupNonInteractively(gceClient).execute(regionName, instanceGroupName, outputFile);
            }
        }
        else if (command.equals("create-group"))
        {
            if (args.size() != 5)
            {
                GceAdminUtils.dieWithMessage("Use 'gce_admin create-group <region-name> <instance-group-name> <instance-template-name> <instance-count>'");
            }
            else
            {
                final String regionName = args.get(1);
                final String instanceGroupName = args.get(2);
                final String instanceTemplateName = args.get(3);
                final int instanceCount = Integer.parseInt(args.get(4));

                new OpCreateInstanceGroupNonInteractively(gceClient).execute(regionName, instanceGroupName, instanceTemplateName,
                                                                             instanceCount, outputFile);
            }
        }
        else if (command.equals("delete-group"))
        {
            if (args.size() != 3)
            {
                GceAdminUtils.dieWithMessage("Use 'gce_admin delete-group <region-name> <instance-group-name>'");
            }
            else
            {
                final String regionName = args.get(1);
                final String instanceGroupName = args.get(2);

                new OpDeleteInstanceGroupNonInteractively(gceClient).execute(regionName, instanceGroupName);
            }
        }
        else // unknown command
        {
            GceAdminUtils.dieWithMessage("Unknown command: " + command);
        }
    }
}
