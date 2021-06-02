/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.api.services.compute.model.AccessConfig;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.InstanceGroup;
import com.google.api.services.compute.model.NetworkInterface;
import com.google.api.services.compute.model.Region;
import com.xceptance.common.util.ConsoleUiUtils;
import com.xceptance.common.util.ProcessExitCodes;

/**
 * Some utility methods that are used in more than one place.
 */
class GceAdminUtils
{
    /**
     * The log facility.
     */
    private static final Log log = LogFactory.getLog(GceAdmin.class);

    /**
     * The format that generates master controller-compatible configuration lines.
     */
    private static final String AGENT_CONTROLLER_LINE_FORMAT = "com.xceptance.xlt.mastercontroller.agentcontrollers.ac%03d_%s.url = https://%s:8500";

    /**
     * The user-friendly region names, keyed by region name.
     */
    private static final Map<String, String> FRIENDLY_REGION_NAMES = new HashMap<>();

    static
    {
        FRIENDLY_REGION_NAMES.put("asia-east1", "Asia Pacific  - Taiwan        ");
        FRIENDLY_REGION_NAMES.put("asia-east2", "Asia Pacific  - Hong Kong     ");
        FRIENDLY_REGION_NAMES.put("asia-northeast1", "Asia Pacific  - Tokyo         ");
        FRIENDLY_REGION_NAMES.put("asia-northeast2", "Asia Pacific  - Osaka         ");
        FRIENDLY_REGION_NAMES.put("asia-northeast3", "Asia Pacific  - Seoul         ");
        FRIENDLY_REGION_NAMES.put("asia-south1", "Asia Pacific  - Mumbai        ");
        FRIENDLY_REGION_NAMES.put("asia-southeast1", "Asia Pacific  - Singapore     ");
        FRIENDLY_REGION_NAMES.put("asia-southeast2", "Asia Pacific  - Jakarta       ");
        FRIENDLY_REGION_NAMES.put("australia-southeast1", "Asia Pacific  - Sydney        ");
        FRIENDLY_REGION_NAMES.put("europe-north1", "Europe        - Finland       ");
        FRIENDLY_REGION_NAMES.put("europe-west1", "Europe        - Belgium       ");
        FRIENDLY_REGION_NAMES.put("europe-west2", "Europe        - London        ");
        FRIENDLY_REGION_NAMES.put("europe-west3", "Europe        - Frankfurt     ");
        FRIENDLY_REGION_NAMES.put("europe-west4", "Europe        - Netherlands   ");
        FRIENDLY_REGION_NAMES.put("europe-west6", "Europe        - Zurich        ");
        FRIENDLY_REGION_NAMES.put("northamerica-northeast1", "Canada        - Montréal      ");
        FRIENDLY_REGION_NAMES.put("southamerica-east1", "South America - Sao Paulo     ");
        FRIENDLY_REGION_NAMES.put("us-central1", "US            - Iowa          ");
        FRIENDLY_REGION_NAMES.put("us-east1", "US            - South Carolina");
        FRIENDLY_REGION_NAMES.put("us-east4", "US            - North Virginia");
        FRIENDLY_REGION_NAMES.put("us-west1", "US            - Oregon        ");
        FRIENDLY_REGION_NAMES.put("us-west2", "US            - California    ");
        FRIENDLY_REGION_NAMES.put("us-west3", "US            - Utah          ");
        FRIENDLY_REGION_NAMES.put("us-west4", "US            - Nevada        ");

        // add a placeholder for unknown regions
        FRIENDLY_REGION_NAMES.put("", "<unknown>     - <unknown>     ");
    };

    /**
     * Asks the user to select one or more regions from the list of available regions.
     *
     * @param regions
     *            the available regions
     * @return the selected regions
     */
    static List<Region> multiSelectRegions(final List<Region> regions)
    {
        final List<String> regionNames = getFriendlyRegionNames(regions);

        return ConsoleUiUtils.multiSelectItems("Select one or more regions:", regionNames, regions, true);
    }

    /**
     * Asks the user to select one or more instance groups from the list of available instance groups.
     *
     * @param instanceGroups
     *            the available instance groups
     * @return the selected instance groups
     */
    static List<InstanceGroup> multiSelectInstanceGroups(final List<InstanceGroup> instanceGroups)
    {
        final List<InstanceGroup> sortedInstanceGroups = new ArrayList<>(instanceGroups);
        Collections.sort(sortedInstanceGroups, (ig1, ig2) -> ig1.getName().compareTo(ig2.getName()));

        final List<String> sortedInstanceGroupNames = new ArrayList<>();
        for (final InstanceGroup instanceGroup : sortedInstanceGroups)
        {
            sortedInstanceGroupNames.add(instanceGroup.getName());
        }

        return ConsoleUiUtils.multiSelectItems("Select one or more instance groups:", sortedInstanceGroupNames, sortedInstanceGroups, true);
    }

    /**
     * Creates the agent controller URL properties for use by the master controller.
     * 
     * @param instances
     *            the instances for which to get the properties
     * @return the snippet with the properties, one line per instance
     */
    static String buildAgentControllerConnectionProperties(final List<Instance> instances)
    {
        final StringBuilder sb = new StringBuilder();

        int instanceIndex = 1;
        for (final Instance instance : instances)
        {
            final String zoneResource = instance.getZone();
            final String zoneName = getZoneName(zoneResource);
            final String ipAddress = getIpAddress(instance);

            sb.append(String.format(AGENT_CONTROLLER_LINE_FORMAT, instanceIndex, zoneName, ipAddress)).append('\n');

            instanceIndex++;
        }

        return sb.toString();
    }

    /**
     * Outputs the agent controller URL properties to a file or stdout.
     *
     * @param instances
     *            the instances for which to write the properties
     * @param outputFile
     *            the file to write the agent controller properties to, or <code>null</code> if the properties are to be
     *            written to stdout
     * @throws IOException
     *             if the file could not be written to
     */
    static void outputAgentControllerConnectionProperties(final List<Instance> instances, final File outputFile) throws IOException
    {
        final String agentControllerConnectionProperties = buildAgentControllerConnectionProperties(instances);

        if (outputFile != null)
        {
            FileUtils.writeStringToFile(outputFile, agentControllerConnectionProperties, StandardCharsets.UTF_8);
        }
        else
        {
            System.out.print(agentControllerConnectionProperties);
        }
    }

    /**
     * Prints the given error info to both the console/log.
     *
     * @param errorMessage
     *            the error message
     * @param t
     *            the corresponding throwable (maybe <code>null</code>)
     */
    static void logError(final String errorMessage, final Throwable t)
    {
        System.err.println("Error: " + errorMessage);
        log.error(errorMessage, t);
    }

    /**
     * Prints the given error info to both the console/log and exits the program.
     *
     * @param errorMessage
     *            the error message
     */
    static void dieWithMessage(final String errorMessage)
    {
        dieWithMessage(errorMessage, null);
    }

    /**
     * Prints the given error info to both the console/log and exits the program.
     *
     * @param errorMessage
     *            the error message
     * @param t
     *            the corresponding throwable (maybe <code>null</code>)
     */
    static void dieWithMessage(final String errorMessage, final Throwable t)
    {
        logError(errorMessage, t);
        System.exit(ProcessExitCodes.GENERAL_ERROR);
    }

    /**
     * Returns user-friendly region names for the given regions.
     *
     * @return the friendly region names
     */
    static List<String> getFriendlyRegionNames(final List<Region> regions)
    {
        return regions.stream().map(GceAdminUtils::getFriendlyRegionName).collect(Collectors.toList());
    }

    /**
     * Returns a user-friendly region name for the given region.
     *
     * @return the friendly region name
     */
    static String getFriendlyRegionName(final Region region)
    {
        final String regionName = region.getName();

        String friendlyRegionName = FRIENDLY_REGION_NAMES.get(regionName);
        if (friendlyRegionName == null)
        {
            // get a placeholder for unknown regions
            friendlyRegionName = FRIENDLY_REGION_NAMES.get("");
        }

        return friendlyRegionName + " (" + regionName + ")";
    }

    /**
     * Extracts the instance name from an instance URL.
     * 
     * @param instanceUrl
     *            the instance URL
     * @return the instance name
     */
    static String getInstanceName(final String instanceUrl)
    {
        return StringUtils.substringAfter(instanceUrl, "instances/");
    }

    /**
     * Extracts the region name from a region URL.
     * 
     * @param regionUrl
     *            the region URL
     * @return the region name
     */
    static String getRegionName(final String regionUrl)
    {
        return StringUtils.substringAfter(regionUrl, "regions/");
    }

    /**
     * Extracts the zone name from an instance or zone URL.
     * 
     * @param instanceOrZoneUrl
     *            the instance or zone URL
     * @return the zone name
     */
    static String getZoneName(final String resourceUrl)
    {
        String zoneName = StringUtils.substringBetween(resourceUrl, "zones/", "/instances/");

        if (zoneName == null)
        {
            zoneName = StringUtils.substringAfter(resourceUrl, "zones/");
        }

        return zoneName;
    }

    /**
     * Extracts the machine type of the given machine-type URL.
     * 
     * @param machineTypeUrl
     *            the machine-type URl
     * @return the machine type
     */
    static String getMachineType(final String machineTypeUrl)
    {
        return StringUtils.substringAfter(machineTypeUrl, "machineTypes/");
    }

    /**
     * Returns the first public IP address attached to the given instance or, if there is no public IP address, the
     * first private IP address.
     * 
     * @param instance
     *            the instance
     * @return the IP address
     */
    static String getIpAddress(final Instance instance)
    {
        // check if the instance has network interface at all
        final List<NetworkInterface> networkInterfaces = instance.getNetworkInterfaces();
        if (networkInterfaces != null && networkInterfaces.size() > 0)
        {
            // first try to find a public IP
            for (final NetworkInterface networkInterface : networkInterfaces)
            {
                // check if the network interface has access configs at all
                final List<AccessConfig> accessConfigs = networkInterface.getAccessConfigs();
                if (accessConfigs != null && accessConfigs.size() > 0)
                {
                    for (final AccessConfig accessConfig : accessConfigs)
                    {
                        // check if the access config provides a public IP
                        final String ipAddress = accessConfig.getNatIP();
                        if (StringUtils.isNotBlank(ipAddress))
                        {
                            return ipAddress;
                        }
                    }
                }
            }

            // now try to find a private IP at least
            for (final NetworkInterface networkInterface : networkInterfaces)
            {
                // check if the network interface provides a private IP
                final String ipAddress = networkInterface.getNetworkIP();
                if (StringUtils.isNotBlank(ipAddress))
                {
                    return ipAddress;
                }
            }
        }

        // return something to indicate that there is no public/private IP address
        return "<none>";
    }
}
