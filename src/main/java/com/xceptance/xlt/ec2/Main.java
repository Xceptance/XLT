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
package com.xceptance.xlt.ec2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.ec2.model.AvailabilityZone;
import software.amazon.awssdk.services.ec2.model.DescribeTagsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeTagsResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Image;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;
import software.amazon.awssdk.services.ec2.model.Region;
import software.amazon.awssdk.services.ec2.model.ResourceType;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import software.amazon.awssdk.services.ec2.model.Subnet;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.TagDescription;
import software.amazon.awssdk.services.ec2.model.Vpc;
import com.google.common.collect.Sets;
import com.xceptance.common.util.ConsoleUiUtils;
import com.xceptance.common.util.ProcessExitCodes;

/**
 * The {@link Main} class is the entry point to the simple front-end for managing AWS EC2 instances.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class Main extends AbstractEC2Client
{
    /**
     * The format that generates master controller-compatible configuration lines.
     */
    private static final String AGENT_CONTROLLER_LINE_FORMAT = "com.xceptance.xlt.mastercontroller.agentcontrollers.ac%03d_%s.url = https://%s:8500";

    /**
     * The user-friendly region names, keyed by region name.
     */
    private static final Map<String, String> FRIENDLY_REGION_NAMES = new HashMap<String, String>();

    static
    {
        FRIENDLY_REGION_NAMES.put("af-south-1", "Africa        - Cape Town       ");
        FRIENDLY_REGION_NAMES.put("ap-east-1", "Asia Pacific  - Hong Kong       ");
        FRIENDLY_REGION_NAMES.put("ap-northeast-1", "Asia Pacific  - Tokyo           ");
        FRIENDLY_REGION_NAMES.put("ap-northeast-2", "Asia Pacific  - Seoul           ");
        FRIENDLY_REGION_NAMES.put("ap-northeast-3", "Asia Pacific  - Osaka           ");
        FRIENDLY_REGION_NAMES.put("ap-south-1", "Asia Pacific  - Mumbai          ");
        FRIENDLY_REGION_NAMES.put("ap-south-2", "Asia Pacific  - Hyderabad       ");
        FRIENDLY_REGION_NAMES.put("ap-southeast-1", "Asia Pacific  - Singapore       ");
        FRIENDLY_REGION_NAMES.put("ap-southeast-2", "Asia Pacific  - Sydney          ");
        FRIENDLY_REGION_NAMES.put("ap-southeast-3", "Asia Pacific  - Jakarta         ");
        FRIENDLY_REGION_NAMES.put("ap-southeast-4", "Asia Pacific  - Melbourne       ");
        FRIENDLY_REGION_NAMES.put("ca-central-1", "Canada        - Central         ");
        FRIENDLY_REGION_NAMES.put("eu-central-1", "Europe        - Frankfurt       ");
        FRIENDLY_REGION_NAMES.put("eu-central-2", "Europe        - Zurich          ");
        FRIENDLY_REGION_NAMES.put("eu-north-1", "Europe        - Stockholm       ");
        FRIENDLY_REGION_NAMES.put("eu-south-1", "Europe        - Milan           ");
        FRIENDLY_REGION_NAMES.put("eu-south-2", "Europe        - Spain           ");
        FRIENDLY_REGION_NAMES.put("eu-west-1", "Europe        - Ireland         ");
        FRIENDLY_REGION_NAMES.put("eu-west-2", "Europe        - London          ");
        FRIENDLY_REGION_NAMES.put("eu-west-3", "Europe        - Paris           ");
        FRIENDLY_REGION_NAMES.put("il-central-1", "Israel        - Tel Aviv        ");
        FRIENDLY_REGION_NAMES.put("me-central-1", "Middle East   - UAE             ");
        FRIENDLY_REGION_NAMES.put("me-south-1", "Middle East   - Bahrain         ");
        FRIENDLY_REGION_NAMES.put("sa-east-1", "South America - Sao Paulo       ");
        FRIENDLY_REGION_NAMES.put("us-east-1", "US East       - North Virginia  ");
        FRIENDLY_REGION_NAMES.put("us-east-2", "US East       - Ohio            ");
        FRIENDLY_REGION_NAMES.put("us-west-1", "US West       - North California");
        FRIENDLY_REGION_NAMES.put("us-west-2", "US West       - Oregon          ");

        // add a placeholder for unknown regions
        FRIENDLY_REGION_NAMES.put("", "<unknown>     - <unknown>       ");
    };

    /**
     * The descriptions of the instance types suitable for load tests.
     */
    private static final String[] INSTANCE_TYPE_DESCRIPTIONS =
        {
            " 2 cores,   8.0 compute units,   3.75 GB RAM, EBS-only", // c4.large
            " 4 cores,  16.0 compute units,   7.50 GB RAM, EBS-only", // c4.xlarge
            " 8 cores,  31.0 compute units,  15.00 GB RAM, EBS-only", // c4.2xlarge
            "16 cores,  62.0 compute units,  30.00 GB RAM, EBS-only", // c4.4xlarge
            "36 cores, 132.0 compute units,  60.00 GB RAM, EBS-only", // c4.8xlarge
            " 2 cores,   9.0 compute units,   4.00 GB RAM, EBS-only", // c5.large
            " 4 cores,  17.0 compute units,   8.00 GB RAM, EBS-only", // c5.xlarge
            " 8 cores,  34.0 compute units,  16.00 GB RAM, EBS-only", // c5.2xlarge
            "16 cores,  68.0 compute units,  32.00 GB RAM, EBS-only", // c5.4xlarge
            "36 cores, 141.0 compute units,  72.00 GB RAM, EBS-only", // c5.9xlarge
            "72 cores, 281.0 compute units, 144.00 GB RAM, EBS-only", // c5.18xlarge
            " 2 cores,   6.5 compute units,   8.00 GB RAM, EBS-only", // m4.large
            " 4 cores,  13.0 compute units,  16.00 GB RAM, EBS-only", // m4.xlarge
            " 8 cores,  26.0 compute units,  32.00 GB RAM, EBS-only", // m4.2xlarge
            "16 cores,  53.5 compute units,  64.00 GB RAM, EBS-only", // m4.4xlarge
            " 2 cores,   8.0 compute units,   8.00 GB RAM, EBS-only", // m5.large
            " 4 cores,  16.0 compute units,  16.00 GB RAM, EBS-only", // m5.xlarge
            " 8 cores,  31.0 compute units,  32.00 GB RAM, EBS-only", // m5.2xlarge
            "16 cores,  60.0 compute units,  64.00 GB RAM, EBS-only", // m5.4xlarge
            " 2 cores,   7.0 compute units,  15.25 GB RAM, EBS-only", // r4.large
            " 4 cores,  13.5 compute units,  30.50 GB RAM, EBS-only", // r4.xlarge
            " 8 cores,  27.0 compute units,  61.00 GB RAM, EBS-only", // r4.2xlarge
            "16 cores,  53.0 compute units, 122.00 GB RAM, EBS-only", // r4.4xlarge
            " 2 cores,   8.0 compute units,  16.00 GB RAM, EBS-only", // r5.large
            " 4 cores,  16.0 compute units,  32.00 GB RAM, EBS-only", // r5.xlarge
            " 8 cores,  31.0 compute units,  64.00 GB RAM, EBS-only", // r5.2xlarge
            "16 cores,  60.0 compute units, 128.00 GB RAM, EBS-only", // r5.4xlarge
        };

    /**
     * The instance types suitable for load tests.
     */
    private static final String[] INSTANCE_TYPES =
        {
            InstanceType.C4_LARGE.toString(), InstanceType.C4_XLARGE.toString(), InstanceType.C4_2_XLARGE.toString(),
            InstanceType.C4_4_XLARGE.toString(), InstanceType.C4_8_XLARGE.toString(), InstanceType.C5_LARGE.toString(),
            InstanceType.C5_XLARGE.toString(), InstanceType.C5_2_XLARGE.toString(), InstanceType.C5_4_XLARGE.toString(),
            InstanceType.C5_9_XLARGE.toString(), InstanceType.C5_18_XLARGE.toString(), InstanceType.M4_LARGE.toString(),
            InstanceType.M4_XLARGE.toString(), InstanceType.M4_2_XLARGE.toString(), InstanceType.M4_4_XLARGE.toString(),
            InstanceType.M5_LARGE.toString(), InstanceType.M5_XLARGE.toString(), InstanceType.M5_2_XLARGE.toString(),
            InstanceType.M5_4_XLARGE.toString(), InstanceType.R4_LARGE.toString(), InstanceType.R4_XLARGE.toString(),
            InstanceType.R4_2_XLARGE.toString(), InstanceType.R4_4_XLARGE.toString(), InstanceType.R5_LARGE.toString(),
            InstanceType.R5_XLARGE.toString(), InstanceType.R5_2_XLARGE.toString(), InstanceType.R5_4_XLARGE.toString(),
        };

    /**
     * The descriptions of the main functions.
     */
    private static final String[] OPERATION_DESCRIPTIONS =
        {
            "Run new instances", "Terminate instances", "List running instances", "Show instance details", "Quit"
        };

    /**
     * The keys used to select the main functions.
     */
    private static final String[] OPERATION_KEYS =
        {
            "r", "t", "l", "d", "q"
        };

    /**
     * The main functions.
     */
    private static final String[] OPERATIONS =
        {
            "run", "terminate", "list", "show details", "quit"
        };

    /**
     * The time to wait for instances to eventually exist after they have been created.
     */
    private static final long EVENTUAL_CONSISTENCY_TIMEOUT = 10_000;

    /**
     * Main method.
     *
     * @param args
     *            the command-line arguments
     */
    public static void main(final String[] args)
    {
        final Options options = createCommandLineOptions();
        final CommandLine commandLine = AbstractEC2Client.parseCommandLine(options, args);

        if (commandLine.hasOption("help"))
        {
            printUsageInfo(options);
        }
        else
        {
            Main ec2Admin = null;
            try
            {
                ec2Admin = new Main();
            }
            catch (final Exception e)
            {
                // error is already logged in super class constructor
                System.exit(ProcessExitCodes.GENERAL_ERROR);
            }

            try
            {
                if (commandLine.getArgs().length > 0)
                {
                    final String firstArg = (String) commandLine.getArgList().get(0);
                    if (OPERATIONS[0].equals(firstArg) || OPERATIONS[1].equals(firstArg))
                    {
                        // start in non-interactive mode
                        ec2Admin.startNonInteractiveMode(commandLine);
                    }
                    else
                    {
                        printUsageInfo(options);
                        System.exit(ProcessExitCodes.PARAMETER_ERROR);
                    }
                }
                else
                {
                    // enter the command loop
                    ec2Admin.administrate(commandLine);
                }
            }
            catch (final Exception e)
            {
                // in case any exception slips through
                System.err.println("An unexpected error occurred: " + e.getMessage());
                log.error("Unexpected error", e);
                System.exit(ProcessExitCodes.GENERAL_ERROR);
            }
            finally
            {
                ec2Admin.shutdown();
            }
        }
    }

    public Main() throws Exception
    {
        super();
    }

    private void administrate(final CommandLine commandLine)
    {
        while (true)
        {
            try
            {
                final String operation = selectOperation();

                if (operation.equals("list"))
                {
                    listInstances();
                }
                else if (operation.equals("show details"))
                {
                    listMoreDetails();
                }
                else if (operation.equals("run"))
                {
                    runInstances(commandLine);
                }
                else if (operation.equals("terminate"))
                {
                    terminateInstances();
                }
                else if (operation.equals("quit"))
                {
                    return;
                }
            }
            catch (final Exception e)
            {
                System.err.println("Failed to execute operation: " + e.getMessage());
                log.error("Failed to execute operation", e);
            }
        }
    }

    private void listMoreDetails()
    {
        final List<Region> regions = multiSelectRegions();
        final List<TagDescription> tags = multiSelectTags(regions);

        System.out.print("\nQuerying ");

        listInstanceDetails(regions, tags, false);
    }

    private void listInstanceDetails(final List<Region> regions, final List<TagDescription> tags, final boolean runningOrPendingOnly)
    {
        final StringBuilder sb = new StringBuilder();
        final String indentStr = StringUtils.repeat(' ', 2);
        final String indent2Str = StringUtils.repeat(' ', 4);

        if (tags.isEmpty())
        {
            sb.append("*all* ");
        }
        sb.append("instances ");
        if (!tags.isEmpty())
        {
            sb.append("tagged with:");

            if (tags.size() == 1)
            {
                // keep a single line
                final TagDescription tag = tags.get(0);
                sb.append(indentStr).append(tag.key()).append("=").append(tag.value()).append("\n");
            }
            else
            {
                sb.append('\n');
                // list all tags, 1 per line
                for (final TagDescription tag : tags)
                {
                    sb.append(indent2Str).append(tag.key()).append("=").append(tag.value()).append('\n');
                }
            }
            sb.append('\n').append(indentStr);
        }

        sb.append("in region");
        if (regions.size() > 1)
        {
            sb.append("s:\n");
        }
        else
        {
            sb.append(": ");
        }

        System.out.print(sb.toString());

        if (regions.size() == 1)
        {
            final Region singleRegion = regions.get(0);
            System.out.printf("%s ... ", singleRegion.regionName());

            try
            {
                final String s = getInstances(singleRegion, tags, indent2Str, runningOrPendingOnly);
                System.out.println("OK\n");
                System.out.println(s);
            }
            catch (Exception e)
            {
                log.error("Failed to retrieve instances", e);
                System.out.println("Failed: " + e.getMessage());
            }

        }
        else
        {

            for (final Region region : regions)
            {
                System.out.printf("\n%s%s ... ", indentStr, region.regionName());

                try
                {
                    final String s = getInstances(region, tags, indent2Str, runningOrPendingOnly);
                    System.out.println("OK\n");
                    System.out.println(s);
                }
                catch (Exception e)
                {
                    System.out.println("Failed: " + e.getMessage());
                }

            }
        }
    }

    /**
     * Creates the command-line options.
     *
     * @return the options
     */
    private static Options createCommandLineOptions()
    {
        final Options options = new Options();

        options.addOption(null, "help", false, "Show help.");

        options.addOption("o", "outputFile", true,
                          "The file that will contain the agent controller connection properties corresponding to the started instances. If no such file is specified, the properties will be printed to the console.");

        // TODO #2339
        // options.addOption("s",
        // "securityGroupIDs",
        // true,
        // "The ID(s) of the security group(s) to start with. Separate the groups by comma if needed. If this option is
        // not set the default security group will be used.");

        options.addOption("p", "password", true, "The password that is later used when starting the agent controller on the machines.");

        options.addOption("pf", "passwordFile", true, "File that contains the password to pass to the instance.");

        options.addOption("h", "hostData", true, "The host data to pass to the instance. This option will override option 'hf'.");

        options.addOption("hf", "hostDataFile", true, "File that contains the host data to pass to the instance.");

        options.addOption("u", "userData", true,
                          "The user data to pass to the instance. This option will override option 'uf'. Can only be used if no host data or password is set.");

        options.addOption("uf", "userDataFile", true,
                          "File that contains the user data to pass to the instance. Can only be used if no host data or password is set.");

        final Option key = new Option("k", "key", true, "Key-pair name for SSH login");
        key.setArgName("key-pair name");
        options.addOption(key);

        options.addOption("nk", "noKey", false, "Start instance without any SSH key.");

        return options;
    }

    /**
     * Lists any running/pending instances in all regions.
     */
    private void listInstances()
    {
        final List<Region> regions = multiSelectRegions();
        final List<TagDescription> tags = multiSelectTags(regions);

        listInstances(regions, tags);
    }

    /**
     * Lists any running/pending instances in the specified regions.
     *
     * @param regions
     *            the regions in question
     * @param tags
     *            the tags to match
     */
    private void listInstances(final List<Region> regions, final List<TagDescription> tags)
    {
        System.out.println();

        final List<String> agentControllerLines = new ArrayList<String>();
        int pendingInstanceCount = 0;
        int runningInstanceCount = 0;

        for (final Region region : regions)
        {
            final String regionName = region.regionName();

            try
            {
                System.out.printf("Querying all instances in region '%s' ... ", regionName);

                for (final Instance instance : getInstances(region, tags))
                {
                    final InstanceStateName state = instance.state().name();
                    if (state == InstanceStateName.RUNNING)
                    {
                        runningInstanceCount++;

                        final String address = getAddress(instance);

                        final String agentControllerLine = String.format(AGENT_CONTROLLER_LINE_FORMAT, runningInstanceCount, regionName,
                                                                         address);

                        agentControllerLines.add(agentControllerLine);
                    }
                    else if (state == InstanceStateName.PENDING)
                    {
                        pendingInstanceCount++;
                    }
                }

                System.out.println("OK.");
            }
            catch (final SdkException e)
            {
                System.out.println("Failed: " + e.getMessage());
            }
        }

        System.out.printf("\n%d running and %d pending instance(s) found.\n\n", runningInstanceCount, pendingInstanceCount);

        // print the according master controller configuration for the instances found
        if (runningInstanceCount > 0)
        {
            System.out.println("--- Master controller configuration ---");
            for (final String agentControllerLine : agentControllerLines)
            {
                System.out.println(agentControllerLine);
            }
        }
    }

    /**
     * Returns the public DNS name from the passed instance. If the public DNS name is not available fall back to the
     * public IP address or even the private IP address.
     *
     * @param instance
     *            EC2 instance to get the address from
     * @return DNS or IP of given instance
     */
    private String getAddress(final Instance instance)
    {
        // 1st: public DNS
        String address = instance.publicDnsName();
        if (StringUtils.isBlank(address))
        {
            // 2nd: public IP
            address = instance.publicIpAddress();
            if (StringUtils.isBlank(address))
            {
                // 3rd: private IP
                address = instance.privateIpAddress();
                if (StringUtils.isBlank(address))
                {
                    // DNS name/IP address might not be available yet (#2795)
                    address = "<not available yet>";
                }
            }
        }

        return address;
    }

    /**
     * Presents the user a list of regions from which the user might select one or more. The selected regions will be
     * returned.
     *
     * @return the selected regions
     */
    private List<Region> multiSelectRegions()
    {
        final List<Region> regions = getRegions();

        final List<String> regionNames = new ArrayList<String>();
        for (final Region region : regions)
        {
            regionNames.add(getFriendlyRegionName(region));
        }

        return ConsoleUiUtils.multiSelectItems("\nSelect one or more regions:", regionNames, regions, true);
    }

    /**
     * Presents the user a list of instance tags from which the user might select one or more. The selected tags will be
     * returned.
     *
     * @param regions
     *            the regions to get the tags for
     * @return the selected tags
     */
    private List<TagDescription> multiSelectTags(final List<Region> regions)
    {
        final Map<String, TagDescription> tags = new TreeMap<String, TagDescription>();

        for (final Region region : regions)
        {
            final DescribeTagsRequest describeTagsRequest = DescribeTagsRequest.builder()
                                                                               .filters(Filter.builder().name("resource-type")
                                                                                              .values(ResourceType.INSTANCE.toString())
                                                                                              .build())
                                                                               .build();
            final DescribeTagsResponse describeTagsResponse = getClient(region).describeTags(describeTagsRequest);

            for (final TagDescription tagDescription : describeTagsResponse.tags())
            {

                final String tagDisplayName = tagDescription.key() + "=" + tagDescription.value();
                tags.put(tagDisplayName, tagDescription);
            }
        }

        // let the user choose only if there were tags at all
        if (!tags.isEmpty())
        {
            return ConsoleUiUtils.multiSelectItems("\nFilter instances by one or more tags:", new ArrayList<String>(tags.keySet()),
                                                   new ArrayList<TagDescription>(tags.values()), false, "Do not filter (select all)");
        }
        else
        {
            return Collections.emptyList();
        }
    }

    /**
     * Asks the user to enter the number of EC2 instances to start.
     *
     * @return the count
     */
    private int readInstanceCount()
    {
        return ConsoleUiUtils.readInt("\nEnter the number of instances to start:");
    }

    private String getPassword(final CommandLine commandLine)
    {
        String password = getPasswordFromCommandLine(commandLine);
        if (StringUtils.isBlank(password))
        {
            password = queryPassword();
        }
        return password;
    }

    /**
     * Query user for password data
     *
     * @return user data or empty String if no user data specified
     */
    private String queryPassword()
    {
        return ConsoleUiUtils.readLine("\nEnter agent controller password");
    }

    private String getPasswordFromCommandLine(final CommandLine commandLine)
    {
        // get password from the command line first
        String password = commandLine.getOptionValue("p");

        // if no password was given, check for password file
        if (StringUtils.isBlank(password))
        {
            final String passwordFilePath = commandLine.getOptionValue("pf");
            if (StringUtils.isNotBlank(passwordFilePath))
            {

                try (BufferedReader br = new BufferedReader(new FileReader(passwordFilePath)))
                {
                    // read in first line and treat it as password
                    password = br.readLine().trim();

                    // try to read in another line
                    if (br.readLine() != null)
                    {
                        password = null;
                        // ... and throw an error if file contains more than 1 line
                        throw new IOException("Password file must not contain more than one line.");
                    }
                }
                catch (final IOException e)
                {
                    logError("\tCould not read password from file: " + passwordFilePath + ".\n\t>> " + e.getMessage());
                }
            }
        }

        return StringUtils.defaultIfBlank(password, null);
    }

    private String getHostData(final CommandLine commandLine)
    {
        String hostData = getHostDataFromCommandLine(commandLine);
        if (StringUtils.isBlank(hostData))
        {
            hostData = queryHostData();
        }
        return hostData;
    }

    private String getHostDataFromCommandLine(final CommandLine commandLine)
    {
        // get host data from the command line first
        String hostData = commandLine.getOptionValue("h");

        // if no host data was given, check for host data file
        if (StringUtils.isBlank(hostData))
        {
            final String hostDatafilePath = commandLine.getOptionValue("hf");
            if (StringUtils.isNotBlank(hostDatafilePath))
            {
                try (BufferedReader br = new BufferedReader(new FileReader(hostDatafilePath)))
                {
                    // read the file's lines
                    final StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null)
                    {
                        sb.append(line).append("\n");
                    }

                    // convert to string
                    hostData = sb.toString();

                    // remove trailing line break if necessary
                    if (!hostData.isEmpty())
                    {
                        hostData = hostData.substring(0, hostData.lastIndexOf("\n"));
                    }
                }
                catch (final IOException e)
                {
                    logError("\tCould not read host data from file: " + hostDatafilePath + ".\n\t>> " + e.getMessage());
                }
            }
        }

        return StringUtils.defaultIfBlank(hostData, null);
    }

    /**
     * Query user for host data
     *
     * @return host data or empty String if no host data specified
     */
    private String queryHostData()
    {
        final String hostDataRaw = ConsoleUiUtils.readLine("\nEnter host data (mark line break with '\\n')");
        return hostDataRaw.replaceAll("\\\\n", "\n");
    }

    private String getUserData(final CommandLine commandLine)
    {
        String userData = getUserDataFromCommandLine(commandLine);
        if (StringUtils.isBlank(userData))
        {
            userData = queryUserData();
        }
        return userData;
    }

    private String getUserDataFromCommandLine(final CommandLine commandLine)
    {
        // get the command line user data first
        String userData = commandLine.getOptionValue("u");

        // if no such value evaluate the user data file if given
        if (StringUtils.isBlank(userData))
        {
            final String userDatafilePath = commandLine.getOptionValue("uf");
            if (StringUtils.isNotBlank(userDatafilePath))
            {
                try (BufferedReader br = new BufferedReader(new FileReader(userDatafilePath)))
                {
                    // read the file's lines
                    final StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null)
                    {
                        sb.append(line).append("\n");
                    }

                    // convert to string
                    userData = sb.toString();

                    // remove trailing line break if necessary
                    if (!userData.isEmpty())
                    {
                        userData = userData.substring(0, userData.lastIndexOf("\n"));
                    }
                }
                catch (final IOException e)
                {
                    logError("\tCould not read user data from file: " + userDatafilePath + ".\n\t>> " + e.getMessage());
                }
            }
        }

        return StringUtils.defaultIfBlank(userData, null);
    }

    /**
     * Query user for user data
     *
     * @return user data or empty String if no host data specified
     */
    private String queryUserData()
    {
        final String userDataRaw = ConsoleUiUtils.readLine("\nEnter user data (mark line break with '\\n')");
        return userDataRaw.replaceAll("\\\\n", "\n");
    }

    /**
     * Asks the user to enter the name for the started EC2 instances.
     *
     * @return the name
     */
    private String readInstanceName()
    {
        return ConsoleUiUtils.readLine("\nEnter the instance name:");
    }

    /**
     * Starts new EC2 instances.
     */
    private void runInstances(final CommandLine commandLine)
    {
        final Region region = selectRegion();
        final AvailabilityZone availabilityZone = selectAvailabilityZone(region);
        final VpcSubnetPair vsp = selectVpcAndSubnet(region, availabilityZone);
        if (vsp == null)
        {
            logError("No subnet available for desired region and availability zone.");
            return;
        }

        final Image image = selectImage(region);
        final String instanceType = selectInstanceType(region.regionName());
        final int instanceCount = readInstanceCount();
        final String name = readInstanceName();
        final Collection<String> securityGroupIds = getSecurityGroupIDs(commandLine, region);
        final String password = getPassword(commandLine);
        final String hostData = getHostData(commandLine);
        final String userData;
        final Boolean showUserData;
        if (StringUtils.isBlank(password) && StringUtils.isBlank(hostData))
        {
            showUserData = true;
            userData = getUserData(commandLine);
        }
        else
        {
            showUserData = false;
            userData = null;
        }
        final String keyPairName = getKeyPairName(commandLine, region);

        final StringBuilder sb = new StringBuilder();
        sb.append("\nConfiguration:\n");
        sb.append("  AMI               : ").append(image.imageId()).append(" - ").append(describeImage(image)).append("\n");
        sb.append("  Region            : ").append(region.regionName()).append("\n");
        sb.append("  Availability zone : ").append(availabilityZone != null ? availabilityZone.zoneName() : "<unspecified>").append("\n");
        sb.append("  VPC               : ").append(getVpcDisplayName(vsp.vpc)).append("\n");
        sb.append("  Subnet            : ").append(getSubnetDisplayName(vsp.subnet)).append("\n");
        sb.append("  Type              : ").append(instanceType).append("\n");
        sb.append("  Count             : ").append(instanceCount).append("\n");
        sb.append("  Name              : ").append(name).append("\n");
        sb.append("  Key-pair          : ").append(StringUtils.isBlank(keyPairName) ? "<none>" : keyPairName).append("\n");
        sb.append("  Password          : ").append(password).append("\n");
        sb.append("  Host data         : ");
        if (StringUtils.isBlank(hostData))
        {
            sb.append("<none>\n");
        }
        else
        {
            boolean firstHostDataLine = true;
            for (final String hostDataLine : hostData.split("\\n"))
            {
                if (firstHostDataLine)
                {
                    firstHostDataLine = false;
                }
                else
                {
                    sb.append("                      ");
                }
                sb.append(hostDataLine).append("\n");
            }
        }
        if (showUserData)
        {
            sb.append("  User data         : ");
            if (StringUtils.isBlank(userData))
            {
                sb.append("<none>\n");
            }
            else
            {
                boolean firstUserDataLine = true;
                for (final String userDataLine : userData.split("\\n"))
                {
                    if (firstUserDataLine)
                    {
                        firstUserDataLine = false;
                    }
                    else
                    {
                        sb.append("                      ");
                    }
                    sb.append(userDataLine).append("\n");
                }
            }
        }
        sb.append("\n");
        sb.append("Do you want to run the instance(s) with the above configuration?");

        if (ConsoleUiUtils.confirm(sb.toString()))
        {

            boolean instancesStarted = false;
            final String warnMsg = "\n\n  WARNING: Despite of the previous error, some instances might have been started nevertheless." +
                                   "\n           Please check their status. Also note that some of them might not be tagged with any name.";
            final String errMsg = "Failed to start " + instanceCount + " instances in region '" + region.regionName() + "'";

            try
            {
                System.out.println("\nStarting instances in region '" + region.regionName() + "'");
                System.out.printf(" - creating instances     ... ");

                final List<Instance> instances = runInstances(region, vsp.subnet, image, instanceType, instanceCount, securityGroupIds,
                                                              keyPairName, buildUserData(password, hostData, userData));

                System.out.println("OK");
                instancesStarted = true;

                // wait for instances to be alive
                boolean checkAvailability = true;
                do
                {
                    try
                    {
                        System.out.printf(" - checking availability  ... ");
                        waitForInstancesToEventuallyExist(region, instances, EVENTUAL_CONSISTENCY_TIMEOUT);

                        System.out.println("OK");
                        checkAvailability = false;

                    }
                    catch (final Exception e)
                    {
                        System.out.println("Failed: " + e.getMessage());
                        if (!ConsoleUiUtils.confirm("\nWait a bit longer for all instances to become available? (recommended)"))
                        {
                            log.error(errMsg, e);

                            System.out.println(warnMsg);
                            return;
                        }

                    }
                }
                while (checkAvailability);

                System.out.printf(" - applying 'Name' tag    ... ");
                // set name
                setInstanceName(region, instances, name);

                System.out.println("OK");
            }
            catch (final Exception e)
            {
                log.error(errMsg, e);

                System.out.println("Failed: " + e.getMessage());
                if (instancesStarted)
                {
                    System.out.println(warnMsg);
                }
            }
        }
    }

    /**
     * Selects the VPC and subnet for the given region and availability zone.
     * 
     * @param region
     *            the region
     * @param availabilityZone
     *            the availability zone (may be {@code null})
     * @return the selected VPC/subnet pair or {@code null} if the given availability zone does not define any subnet
     */
    private VpcSubnetPair selectVpcAndSubnet(final Region region, final AvailabilityZone availabilityZone)
    {
        // retrieve all subnets of selected region and AZ
        final List<Subnet> allSubnets = getSubnets(region, availabilityZone, null);
        if (allSubnets.isEmpty())
        {
            return null;
        }

        // collect all distinct VPC-IDs
        final List<String> vpcIds = allSubnets.stream().map(net -> net.vpcId()).distinct().collect(Collectors.toList());

        // retrieve and select VPC for given region and VPC-IDs
        final Vpc vpc = selectVpc(region, vpcIds);
        final String vpcId = vpc.vpcId();
        // filter out all those subnets that do not belong to the selected VPC
        final List<Subnet> vpcSubnets = allSubnets.stream().filter(net -> vpcId.equals(net.vpcId())).collect(Collectors.toList());
        final Subnet subnet = selectSubnet(vpcSubnets);

        return new VpcSubnetPair(vpc, subnet);
    }

    /**
     * Prompts the user to select the desired subnet in case the given list of subnets contains more than one element.
     * In case the list contains only one element, it will be returned without any prompting. Finally, in case the given
     * list empty, {@code null} is returned.
     *
     * @param subnets
     *            the list of subnets to choose from
     * @return the selected subnet or {@code null} if given list is empty
     */
    private Subnet selectSubnet(final List<Subnet> subnets)
    {
        if (subnets.size() > 1)
        {
            final List<String> displayNames = subnets.stream().map(this::getSubnetDisplayName).collect(Collectors.toList());

            return ConsoleUiUtils.selectItem("\nWhich subnet should the instance(s) be launched into?", displayNames, subnets);
        }

        return subnets.isEmpty() ? null : subnets.get(0);
    }

    /**
     * Get key-pair name from command line. If key-pair option is not set query user.
     *
     * @param commandLine
     * @param region
     * @return key pair name or <code>null</code> if no such key is available
     */
    private String getKeyPairName(final CommandLine commandLine, final Region region)
    {
        String keyPairName = null;

        // is key desired at all?
        if (!commandLine.hasOption("nk"))
        {
            // key specified by command line?
            keyPairName = getKeyPairNameFromCommandLine(commandLine);
            if (StringUtils.isBlank(keyPairName))
            {
                // key configured?
                keyPairName = awsConfiguration.getSshKey(region.regionName());
                if (StringUtils.isBlank(keyPairName))
                {
                    // query user
                    keyPairName = getKeypairNameFromUser(region);
                }
                else if (!doesKeyPairExist(keyPairName, region))
                {
                    // inform user
                    System.out.printf("\nThe configured key-pair '%s' does not exist for region '%s'.", keyPairName, region.regionName());

                    // query correct key pair name from user
                    keyPairName = getKeypairNameFromUser(region);
                }
            }
            else if (!doesKeyPairExist(keyPairName, region))
            {
                // inform user
                System.out.printf("\nThe key-pair '%s' does not exist for region '%s'.", keyPairName, region.regionName());

                // query correct key pair name from user
                keyPairName = getKeypairNameFromUser(region);
            }
        }

        return keyPairName;
    }

    /**
     * Get key-pair name from command line
     *
     * @return key-pair name or <code>null</code> if key-pair option is not set
     */
    private String getKeyPairNameFromCommandLine(final CommandLine commandLine)
    {
        return commandLine.hasOption("nk") ? null : commandLine.getOptionValue("k");
    }

    /**
     * Get a key-pair name via a user query.
     *
     * @param region
     *            the region of interest
     * @return a key-pair name, or <code>null</code> if no key pair should be used
     */
    private String getKeypairNameFromUser(final Region region)
    {
        // query user
        final KeyPairInfo keyPairInfo = selectKeyPair(region);
        return keyPairInfo != null ? keyPairInfo.keyName() : null;
    }

    /**
     * Check if the given key pair name exists in the given region.
     *
     * @param keyPairName
     *            key pair name
     * @param region
     *            the region
     * @return <code>true</code> if the given key pair name matches a key pair name in the given region,
     *         <code>false</code> otherwise
     */
    private boolean doesKeyPairExist(final String keyPairName, final Region region)
    {
        for (final KeyPairInfo keyPairInfo : getKeyPairs(region))
        {
            if (keyPairInfo.keyName().equals(keyPairName))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Get security group IDs) from command line. If security group key option is not set query user.
     *
     * @param commandLine
     * @param region
     * @return
     */
    private Collection<String> getSecurityGroupIDs(final CommandLine commandLine, final Region region)
    {
        final Collection<String> securityGroups = getSecurityGroupIDsFromCommandLine(commandLine);
        if (securityGroups == null)
        {
            // TODO #2339
            // securityGroups = selectSecurityGroupIDs(region);
        }

        return securityGroups;
    }

    private Collection<String> getSecurityGroupIDsFromCommandLine(final CommandLine commandLine)
    {
        final String securityGroupsString = commandLine.getOptionValue("s");
        if (StringUtils.isNotBlank(securityGroupsString))
        {
            return Sets.newHashSet(securityGroupsString.split(","));
        }
        return null;
    }

    /**
     * Query user to select security group(s).
     *
     * @param region
     * @return the selected security group IDs
     */
    @SuppressWarnings("unused")
    private Collection<String> selectSecurityGroupIDs(final Region region)
    {
        // prepare display
        final List<SecurityGroup> securityGroups = getSecurityGroupIDs(region);
        final List<String> displayNames = new ArrayList<String>();
        for (final SecurityGroup securityGroup : securityGroups)
        {
            if (StringUtils.isBlank(securityGroup.vpcId()))
            {
                displayNames.add(securityGroup.groupName() + " - " + securityGroup.description() + " (" + securityGroup.groupId() + ")");
            }
        }

        // select IDs
        final List<SecurityGroup> selectedGroups = ConsoleUiUtils.multiSelectItems("\nSelect the security group to use for the new EC2 instances:",
                                                                                   displayNames, securityGroups, true);

        // extract result
        final Collection<String> groupIDs = new HashSet<String>();
        for (final SecurityGroup selectedGroup : selectedGroups)
        {
            groupIDs.add(selectedGroup.groupId());
        }

        return groupIDs;
    }

    /**
     * Set the given name to the given instances if a name is given.
     *
     * @param instances
     *            instances to set the same on
     * @param name
     *            name to set (must not be blank)
     */
    private void setInstanceName(final Region region, final List<Instance> instances, final String name)
    {
        if (StringUtils.isNotBlank(name))
        {
            // collect instance IDs
            final List<String> instanceIDs = getInstanceIds(instances);

            // set the name
            setNameTag(region, instanceIDs, name);
        }
    }

    /**
     * Asks the user to select one of the availability zones in the given region.
     *
     * @param region
     *            the target region
     * @return the selected availability zone, or <code>null</code> if the target zone does not matter
     */
    private AvailabilityZone selectAvailabilityZone(final Region region)
    {
        final List<String> displayNames = new ArrayList<String>();
        final List<AvailabilityZone> availabilityZones = getAvailabilityZones(region);

        for (final AvailabilityZone availabilityZone : availabilityZones)
        {
            displayNames.add(availabilityZone.zoneName());
        }

        // add "unspecified" (to indicate that the target zone does not matter) to the top of the list
        displayNames.add(0, "<unspecified>");
        availabilityZones.add(0, null);

        return ConsoleUiUtils.selectItem("\nSelect the availability zone to use for the new EC2 instances:", displayNames,
                                         availabilityZones);
    }

    /**
     * Asks the user to select one of the images available in the given region.
     *
     * @param region
     *            the target region
     * @return the selected image
     */
    private Image selectImage(final Region region)
    {
        final List<String> displayNames = new ArrayList<String>();
        final List<Image> images = getImages(region);

        final Optional<Image> imageWithLongestId = images.stream()
                                                         .max((i1, i2) -> Integer.compare(i1.imageId().length(), i2.imageId().length()));
        final int maxIdLength = imageWithLongestId.isPresent() ? imageWithLongestId.get().imageId().length() : 21; // ami-<17-digit
                                                                                                                   // hex>

        // sort images first
        Collections.sort(images, (i1, i2) -> describeImage(i1).compareTo(describeImage(i2)));

        for (final Image image : images)
        {
            displayNames.add(StringUtils.rightPad(image.imageId(), maxIdLength) + " - " + describeImage(image));
        }

        return ConsoleUiUtils.selectItem("\nSelect the machine image to use for the new EC2 instances:", displayNames, images);
    }

    /**
     * Query user for key pair for given region.
     *
     * @param region
     * @return a key-pair, or <code>null</code> if no key pair should be used
     */
    private KeyPairInfo selectKeyPair(final Region region)
    {
        final List<String> displayNames = new ArrayList<String>();
        final List<KeyPairInfo> keyPairInfos = getKeyPairs(region);

        for (final KeyPairInfo keyPairInfo : keyPairInfos)
        {
            displayNames.add(keyPairInfo.keyName());
        }

        // add "none" (to indicate that the key pair does not matter) to the top of the list
        displayNames.add(0, "<none>");
        keyPairInfos.add(0, null);

        return ConsoleUiUtils.selectItem("\nSelect the key-pair to use for the new EC2 instances:", displayNames, keyPairInfos);
    }

    /**
     * Asks the user to select an instance type.
     *
     * @return the selected instance type
     */
    private String selectInstanceType(final String regionName)
    {
        final List<String> displayNames = new ArrayList<String>();

        for (int i = 0; i < INSTANCE_TYPES.length; i++)
        {
            displayNames.add(String.format("%-11s - %s", INSTANCE_TYPES[i], INSTANCE_TYPE_DESCRIPTIONS[i]));
        }

        return ConsoleUiUtils.selectItem("\nSelect the instance type to use for the new EC2 instances:", displayNames,
                                         Arrays.asList(INSTANCE_TYPES));
    }

    /**
     * Asks the user to select one of the available operations.
     *
     * @return the selected operation
     */
    private String selectOperation()
    {
        return ConsoleUiUtils.selectItem("\nWhat do you want to do?", Arrays.asList(OPERATION_KEYS), Arrays.asList(OPERATION_DESCRIPTIONS),
                                         Arrays.asList(OPERATIONS));
    }

    /**
     * Asks the user to select a region from the list of available regions.
     *
     * @return the selected region
     */
    private Region selectRegion()
    {
        final List<Region> regions = getRegions();

        final List<String> regionNames = new ArrayList<String>();
        for (final Region region : regions)
        {
            regionNames.add(getFriendlyRegionName(region));
        }

        return ConsoleUiUtils.selectItem("\nSelect a region:", regionNames, regions);
    }

    /**
     * Returns a user-friendly region name for the given region.
     *
     * @return the friendly region name
     */
    private String getFriendlyRegionName(final Region region)
    {
        final String regionName = region.regionName();

        String friendlyRegionName = FRIENDLY_REGION_NAMES.get(regionName);
        if (friendlyRegionName == null)
        {
            // get a placeholder for unknown regions
            friendlyRegionName = FRIENDLY_REGION_NAMES.get("");
        }

        return friendlyRegionName + " (" + regionName + ")";
    }

    /**
     * Terminates all running/stopped instances in the selected region.
     */
    private void terminateInstances()
    {
        final List<Region> regions = multiSelectRegions();
        final List<TagDescription> tags = multiSelectTags(regions);

        System.out.print("\nYou selected to terminate ");

        listInstanceDetails(regions, tags, true);

        if (ConsoleUiUtils.confirm("\nAre you sure?"))
        {
            terminateInstances(regions, tags);
        }
    }

    /**
     * Returns a string representation of all instances in a region that matches at least one given tag.
     * 
     * @param region
     *            Only tagged instances will be returned.
     * @param tags
     *            Instances must be tagged with at least one of the given tags
     * @param lineOffset
     *            All strings will start with the content of lineOffset
     * @param runningPendingOnly
     *            whether to include running/pending instances only
     * @return a textual representation of instances with given tags in the selected region.
     */
    private String getInstances(final Region region, final List<TagDescription> tags, final String lineOffset,
                                final boolean runningPendingOnly)
        throws SdkException
    {
        final StringBuilder output = new StringBuilder();

        int pendingInstanceCount = 0;
        int runningInstanceCount = 0;
        int stoppedInstanceCount = 0;

        final List<MachineInfo> runningMachines = new LinkedList<MachineInfo>();
        final Map<String, Image> imagesById = new HashMap<>();
        for (final Instance instance : getInstances(region, tags))
        {
            final InstanceStateName state = instance.state().name();
            if (state == InstanceStateName.TERMINATED || state == InstanceStateName.SHUTTING_DOWN)
            {
                continue;
            }
            if (state == InstanceStateName.RUNNING)
            {
                runningInstanceCount++;
            }
            else if (state == InstanceStateName.PENDING)
            {
                pendingInstanceCount++;
            }
            else if (state == InstanceStateName.STOPPED || state == InstanceStateName.STOPPING)
            {
                if (runningPendingOnly)
                {
                    continue;
                }

                stoppedInstanceCount++;
            }

            final String imageId = instance.imageId();
            Image image = imagesById.get(imageId);
            if (image == null)
            {
                image = getImage(region, imageId);
                if (image != null)
                {
                    imagesById.put(imageId, image);
                }
            }

            MachineInfo currentMachineInfo = MachineInfo.createMachineInfo(instance, image);
            runningMachines.add(currentMachineInfo);

        }

        output.append(lineOffset);
        if ((runningInstanceCount + pendingInstanceCount + stoppedInstanceCount) > 0)
        {

            if (runningPendingOnly)
            {
                output.append(String.format("%d running and %d pending instance(s) found.\n", runningInstanceCount, pendingInstanceCount));
            }
            else
            {
                output.append(String.format("%d running, %d pending and %d stopped instance(s) found.\n", runningInstanceCount,
                                            pendingInstanceCount, stoppedInstanceCount));
            }
            output.append(MachineInfoPrinter.prettyPrint(runningMachines, lineOffset));
        }
        else
        {
            if (runningPendingOnly)
            {
                output.append("No running or pending instance(s) found.\n");
            }
            else
            {
                output.append("No running, pending or stopped instance(s) found.\n");
            }
        }

        return output.toString();
    }

    /**
     * Parses the given arguments and starts the ec2_admin in non-interactive mode.
     *
     * @param commandLine
     */
    private void startNonInteractiveMode(final CommandLine commandLine)
    {
        final String[] args = commandLine.getArgs();
        final String executeCommand = args[0];
        if (executeCommand.equals(OPERATIONS[0]))
        {
            // run instances
            if (args.length == 6)
            {
                final String outputFileString = commandLine.getOptionValue("o");
                // TODO #2339
                // final Collection<String> securityGroupIDs = getSecurityGroupIDsFromCommandLine(commandLine);
                final Collection<String> securityGroupIDs = null;
                final String password = getPasswordFromCommandLine(commandLine);
                final String hostData = getHostDataFromCommandLine(commandLine);

                final String userData;
                if (StringUtils.isBlank(password) && StringUtils.isBlank(hostData))
                {
                    userData = getUserDataFromCommandLine(commandLine);
                }
                else
                {
                    userData = null;
                }

                final String keyPairName = getKeyPairNameFromCommandLine(commandLine);

                final File outputFile = outputFileString != null ? new File(outputFileString) : null;

                runInstancesNonInteractiveMode(args[1], args[2], args[3], args[4], args[5], outputFile, securityGroupIDs, keyPairName,
                                               password, hostData, userData);
            }
            else
            {
                dieWithMessage("Use \"run <region> <ami> <type> <count> <nameTag>\"");
            }
        }
        else if (executeCommand.equals(OPERATIONS[1]))
        {
            // terminate instances
            if (args.length == 3)
            {
                terminateInstancesNonInteractiveMode(args[1], args[2]);
            }
            else
            {
                dieWithMessage("Use \"terminate <region> <nameTag>\"");
            }
        }
        else
        {
            // wrong command
            dieWithMessage("Unknown command: " + executeCommand);
        }
    }

    /**
     * Start instances in non-interactive mode.
     *
     * @param regionName
     *            region to start the instances in
     * @param imageName
     *            ami ID
     * @param type
     *            instance type such as 'c3-2xlarge' or 'm3.large'
     * @param instanceCountAsString
     *            how many instances to start
     * @param nameTag
     *            the name the instance should be tagged with
     * @param outputFile
     *            File to write the agent controller address properties to. If <code>null</code> properties will be
     *            printed to console
     */
    private void runInstancesNonInteractiveMode(final String regionName, final String imageName, final String type,
                                                final String instanceCountAsString, final String nameTag, final File outputFile,
                                                final Collection<String> securityGroupIDs, final String keyPairName, final String password,
                                                final String hostData, final String userData)
    {
        /*
         * check parameters
         */

        // check region name
        if (!FRIENDLY_REGION_NAMES.containsKey(regionName))
        {
            dieWithMessage("Region '" + regionName + "' is unknown.");
        }

        // check image name
        if (StringUtils.isBlank(imageName))
        {
            dieWithMessage("Image must not be blank.");
        }

        // check instance type
        if (!isInstanceTypeSupported(type))
        {
            dieWithMessage("Instance type '" + type + "' is not supported.");
        }

        // check instance count
        int instanceCount = 0;
        try
        {
            instanceCount = Integer.parseInt(instanceCountAsString);
            if (instanceCount < 1)
            {
                dieWithMessage("The instance count '" + instanceCountAsString + "' must be a positive integer value greater than zero.");
            }
        }
        catch (final NumberFormatException e)
        {
            dieWithMessage("The instance count '" + instanceCountAsString + "' must be a positive integer value greater than zero.");
        }

        // check tag name
        if (StringUtils.isBlank(nameTag))
        {
            dieWithMessage("The tag name must not be blank.");
        }

        /*
         * check AWS entities
         */

        // lookup region
        final Region region = getRegion(regionName);

        // lookup image
        final Image image = getImage(region, imageName);
        if (image == null)
        {
            dieWithMessage("Image '" + imageName + "' not available in this region.");
        }

        /*
         * start instances
         */
        final String nameOfRegion = region.regionName();
        System.out.printf("\nStarting instances in region '%s' ... ", nameOfRegion);
        final List<String> agentControllerConnectionProperties = new ArrayList<String>();
        try
        {
            final List<Instance> instances = runInstances(region, null, image, type, instanceCount, securityGroupIDs, keyPairName,
                                                          buildUserData(password, hostData, userData));

            // get the over-all deadline (allow at least the consistency timeout)
            final long timeout = Math.max(awsConfiguration.getInstanceConnectTimeout(), EVENTUAL_CONSISTENCY_TIMEOUT);
            final long deadline = System.currentTimeMillis() + timeout;

            // wait for instances to be alive
            waitForInstancesToEventuallyExist(region, instances, timeout);

            // now the name can safely be set
            setInstanceName(region, instances, nameTag);

            // wait for instance state 'running'
            for (int i = 0; i < instances.size(); i++)
            {
                final Instance instance = instances.get(i);
                final long remainingTimeout = deadline - System.currentTimeMillis();

                final Instance startedInstance = waitForInstanceState(region, instance, InstanceStateName.RUNNING, remainingTimeout);

                // OK, it's running. Now it's worth to remember it.
                agentControllerConnectionProperties.add(String.format(AGENT_CONTROLLER_LINE_FORMAT, i + 1, nameOfRegion,
                                                                      getAddress(startedInstance)));
            }

            System.out.println("OK.");
        }
        catch (final Exception e)
        {
            dieWithMessage("Failed: " + e.getMessage());
        }

        // output
        outputAgentControllerConnectionProperties(agentControllerConnectionProperties, outputFile);
    }

    /**
     * Output DNS names in agent controller property style.<br>
     * <code>com.xceptance.xlt.mastercontroller.agentcontrollers.ac01.url = https://1.2.3.4:8500</code>
     *
     * @param agentControllerConnectionProperties
     *            agent controller connection properties
     * @param outputFile
     *            Where to write the agent controller connection properties to? set to <code>null</code> if output to
     *            console is desired.
     */
    private void outputAgentControllerConnectionProperties(final List<String> agentControllerConnectionProperties, final File outputFile)
    {
        if (outputFile != null)
        {
            outputToFile(agentControllerConnectionProperties, outputFile);
        }
        else
        {
            outputToConsole(agentControllerConnectionProperties);
        }
    }

    /**
     * Terminate instances in non-interactive mode by region and name tag.
     *
     * @param regionName
     *            region name
     * @param nameTag
     *            name tag
     */
    private void terminateInstancesNonInteractiveMode(final String regionName, final String nameTag)
    {
        // check region
        if (StringUtils.isBlank(regionName) || !FRIENDLY_REGION_NAMES.containsKey(regionName))
        {
            dieWithMessage("Region '" + regionName + "' is unknown.");
        }

        // check tag name
        if (StringUtils.isBlank(nameTag))
        {
            dieWithMessage("Tag name must not be '" + regionName + "' blank.");
        }

        final Region region = getRegion(regionName);

        // get tag description
        final List<TagDescription> tagDescriptions = new ArrayList<TagDescription>();
        final DescribeTagsResponse describeTagsResponse = getClient(region).describeTags(DescribeTagsRequest.builder().build());

        // find specified tag description
        for (final TagDescription tagDescription : describeTagsResponse.tags())
        {
            // it's an instance description
            if (tagDescription.resourceType() == ResourceType.INSTANCE)
            {
                // and value of tag description is equal to specified tag name
                if (tagDescription.value().equals(nameTag))
                {
                    // remember it
                    tagDescriptions.add(tagDescription);

                    // only one tag name can be specified
                    break;
                }
            }
        }

        if (tagDescriptions.isEmpty())
        {
            dieWithMessage("There are no instances with the specified tag name '" + nameTag + "' in region '" + regionName + "'.");
        }

        /*
         * terminate
         */
        final ArrayList<Region> regions = new ArrayList<Region>();
        regions.add(region);
        terminateInstances(regions, tagDescriptions);
    }

    /**
     * Print URLs to agent controllers to predefined file.
     *
     * @param publicDnsNames
     *            list of agent controller URLs
     * @param outputFile
     *            The target output file
     */
    private void outputToFile(final List<String> publicDnsNames, final File outputFile)
    {
        try
        {
            FileUtils.writeLines(outputFile, publicDnsNames);
        }
        catch (final IOException e)
        {
            logError(e.getMessage());
        }
    }

    /**
     * Print list to console.
     *
     * @param agentControllerConnectionProperties
     *            agent controller connection properties
     */
    private void outputToConsole(final List<String> agentControllerConnectionProperties)
    {
        // output to console
        final StringBuilder sb = new StringBuilder();
        for (final String property : agentControllerConnectionProperties)
        {
            sb.append(property).append("\n");
        }
        System.out.println(sb.toString().trim());
    }

    /**
     * Prints the error message to the console and the log.
     *
     * @param errorMessage
     */
    private void logError(final String errorMessage)
    {
        System.err.println("\nError:");
        System.err.println(errorMessage);
        log.error(errorMessage);
    }

    /**
     * Print message and exit.
     *
     * @param msg
     *            message to print
     */
    private void dieWithMessage(final String msg)
    {
        logError(msg);
        System.exit(ProcessExitCodes.GENERAL_ERROR);
    }

    /**
     * Returns whether or not the given instance type is supported by ec2_admin.
     * 
     * @param instanceType
     *            the instance type code
     * @return <code>true</code> if the given instance type is supported, <code>false</code> otherwise
     */
    private boolean isInstanceTypeSupported(final String instanceType)
    {
        return ArrayUtils.contains(INSTANCE_TYPES, instanceType);
    }

    /**
     * Describes the given image.
     *
     * @param image
     *            the image to describe
     * @return value of 'Name' tag if present and not blank, and image description otherwise
     */
    private String describeImage(final Image image)
    {
        return StringUtils.defaultIfBlank(getNameTagValue(image.tags()).orElse(null),
                                          StringUtils.defaultIfBlank(image.description(), "(no description)"));
    }

    private String buildUserData(final String password, final String hostData, String userData)
    {
        if (StringUtils.isNotBlank(password) || StringUtils.isNotBlank(hostData))
        {
            JSONObject userDataObj = new JSONObject();

            userDataObj.put("acPassword", password);
            userDataObj.put("hostData", hostData);

            return userDataObj.toString();
        }
        else
        {
            return userData;
        }
    }

    /**
     * Retrieves all VPCs for the given region and prompts the user to select the desired VPC in case more than one VPC
     * was found. In case only one such VPC was found, it is returned. Finally, in case no VPC was found for the given
     * region, {@code null} is returned.
     * <p>
     * The retrieved list of VPCs can be additionally restricted to contain only those VPCs whose ID is in the list of
     * VPC-IDs given as additional argument.
     * 
     * @param region
     *            the region
     * @param vpcIds
     *            list of VPC-IDs (may be {@code null}) to use as filter
     * @return the chosen VPC or {@code null} if given region does not define any VPC (whose ID is contained in the
     *         given list)
     */
    private Vpc selectVpc(final Region region, final List<String> vpcIds)
    {
        final List<Vpc> vpcs = getVpcs(region, vpcIds);
        if (vpcs.size() > 1)
        {
            final List<String> vpcNames = vpcs.stream().map(this::getVpcDisplayName).collect(Collectors.toList());
            return ConsoleUiUtils.selectItem("\nMultiple VPCs found for selected region.\nPlease select the desired one:", vpcNames, vpcs);
        }
        return vpcs.isEmpty() ? null : vpcs.get(0);
    }

    /**
     * Computes the display name for the given VPC.
     * 
     * @param vpc
     *            the VPC
     * @return display name of given VPC
     */
    private String getVpcDisplayName(final Vpc vpc)
    {
        final StringBuilder sb = new StringBuilder(vpc.vpcId());
        final Optional<String> nameTag = getNameTagValue(vpc.tags());
        if (nameTag.isPresent())
        {
            sb.append(" | ").append(nameTag.get());
        }
        if (vpc.isDefault())
        {
            sb.append(" *default*");
        }

        return sb.toString();
    }

    /**
     * Computes the display name for the given subnet.
     * 
     * @param subnet
     *            the subnet
     * @return display name of given subnet
     */
    private String getSubnetDisplayName(final Subnet subnet)
    {
        final StringBuilder sb = new StringBuilder(subnet.subnetId());
        sb.append(" (").append(subnet.availabilityZone()).append(")");
        final Optional<String> nameTag = getNameTagValue(subnet.tags());
        if (nameTag.isPresent())
        {
            sb.append(" | ").append(nameTag.get());
        }
        sb.append(" [").append(subnet.cidrBlock()).append("]");
        if (subnet.defaultForAz())
        {
            sb.append(" *default*");
        }

        return sb.toString();
    }

    /**
     * Lookups the 1st tag with key 'Name' from the given list of tags and returns its value as optional.
     * 
     * @param tags
     *            the list of tags
     * @return value of 1st tag whose key is 'Name'
     */
    protected Optional<String> getNameTagValue(final List<Tag> tags)
    {
        if (tags != null)
        {
            return tags.stream().filter(t -> "Name".equals(t.key())).findFirst().map(t -> t.value());
        }
        return Optional.empty();
    }

    private static class VpcSubnetPair
    {
        private final Vpc vpc;

        private final Subnet subnet;

        private VpcSubnetPair(final Vpc aVpc, final Subnet aSubnet)
        {
            vpc = aVpc;
            subnet = aSubnet;
        }
    }

}
