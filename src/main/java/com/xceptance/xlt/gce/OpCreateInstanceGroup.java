/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.api.services.compute.model.InstanceTemplate;
import com.google.api.services.compute.model.Region;
import com.xceptance.common.util.ConsoleUiUtils;

/**
 * Lets the user create a managed instance group interactively.
 */
class OpCreateInstanceGroup
{
    /**
     * The underlying GCE client.
     */
    private final GceClient gceClient;

    /**
     * Constructor.
     * 
     * @param gceClient
     *            the GCE client to use
     */
    OpCreateInstanceGroup(final GceClient gceClient)
    {
        this.gceClient = gceClient;
    }

    /**
     * Lets the user create an instance group interactively.
     *
     * @throws IOException
     *             if anything goes wrong
     */
    void execute() throws IOException
    {
        final List<InstanceTemplate> instanceTemplates = gceClient.getInstanceTemplates();
        if (instanceTemplates.isEmpty())
        {
            System.out.println("To create a managed instance group, an instance template is needed but none was found.");
            return;
        }

        final List<Region> regions = gceClient.getRegions();
        final List<Region> selectedRegions = GceAdminUtils.multiSelectRegions(regions);
        System.out.println();

        final InstanceTemplate instanceTemplate = selectInstanceTemplate(instanceTemplates);
        System.out.println();

        final String instanceGroupName = readInstanceGroupName();
        System.out.println();

        final int instanceCount = readInstanceCount();
        System.out.println();

        final StringBuilder sb = new StringBuilder();
        sb.append("  Regions             : ").append(getRegionNames(selectedRegions)).append("\n");
        sb.append("  Instance group name : ").append(instanceGroupName).append("\n");
        sb.append("  Instance count      : ").append(instanceCount).append("\n");
        sb.append("  Instance template     ").append("\n");
        sb.append("     - Name           : ").append(instanceTemplate.getName()).append("\n");
        sb.append("     - Image          : ")
          .append(instanceTemplate.getProperties().getDisks().get(0).getInitializeParams().getSourceImage()).append("\n");
        sb.append("     - Machine type  : ").append(instanceTemplate.getProperties().getMachineType()).append("\n");
        sb.append("\n");
        sb.append("Do you want to create a managed instance group with the above configuration?");

        if (ConsoleUiUtils.confirm(sb.toString()))
        {
            System.out.println();

            // whether to append the region name to the instance group name
            final boolean appendRegionName = selectedRegions.size() > 1 && instanceCount > 1;

            int remainingRegions = selectedRegions.size();
            int remainingInstances = instanceCount;

            for (Region region : selectedRegions)
            {
                final String regionName = region.getName();

                // calculate the number of instances to start in this region
                final int instancesInRegion = (int) Math.ceil((double) remainingInstances / (double) remainingRegions);

                remainingInstances -= instancesInRegion;
                remainingRegions--;

                // something to do here in this region?
                if (instancesInRegion > 0)
                {
                    try
                    {
                        final String regionizedInstanceGroupName = appendRegionName ? instanceGroupName + "-" + regionName
                                                                                    : instanceGroupName;

                        System.out.printf("Creating instance group '%s' with %d instance(s) in region '%s' ... ",
                                          regionizedInstanceGroupName, instancesInRegion, regionName);

                        gceClient.createInstanceGroup(regionName, regionizedInstanceGroupName, instanceTemplate.getName(),
                                                      instancesInRegion);

                        System.out.println("OK");
                    }
                    catch (final Exception e)
                    {
                        GceAdminUtils.logError("Failed to create instance group", e);
                    }
                }
            }
        }
    }

    /**
     * Returns the names of the given regions.
     *
     * @return the region names
     */
    private static String getRegionNames(final List<Region> regions)
    {
        List<String> regionNames = regions.stream().map(Region::getName).collect(Collectors.toList());

        return StringUtils.join(regionNames, ", ");
    }

    /**
     * Asks the user to select an instance template from the list of available instance templates.
     *
     * @param instanceTemplates
     *            the available instance templates
     * @return the selected instance template
     */
    private InstanceTemplate selectInstanceTemplate(final List<InstanceTemplate> instanceTemplates)
    {
        final List<String> instanceTemplateNames = new ArrayList<>();
        for (final InstanceTemplate instanceTemplate : instanceTemplates)
        {
            instanceTemplateNames.add(instanceTemplate.getName());
        }

        return ConsoleUiUtils.selectItem("Select an instance template:", instanceTemplateNames, instanceTemplates);
    }

    /**
     * Asks the user to enter the name for the instance group.
     *
     * @return the instance group name
     */
    private String readInstanceGroupName()
    {
        return ConsoleUiUtils.readLine("Enter the name of the instance group");
    }

    /**
     * Asks the user to enter the number of GCE instances to start.
     *
     * @return the instance count
     */
    private int readInstanceCount()
    {
        return ConsoleUiUtils.readInt("Enter the number of instances to start");
    }
}
