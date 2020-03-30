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
package com.xceptance.xlt.gce;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.InstanceGroup;
import com.google.api.services.compute.model.Region;
import com.xceptance.common.util.ConsoleUiUtils;

/**
 * Lets the user list machine instances and prints their corresponding agent controller URLs to the console.
 */
class OpListInstances
{
    /**
     * The keys used to select the filter options.
     */
    private static final String[] FILTER_OPTION_KEYS =
        {
            "0", "l", "g"
        };

    /**
     * The descriptions of the filter options.
     */
    private static final String[] FILTER_OPTION_DESCRIPTIONS =
        {
            "(No filter)", "Name label", "Instance group"
        };

    /**
     * The filter options.
     */
    private static final String[] FILTER_OPTIONS =
        {
            "noFilter", "label", "group"
        };

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
    OpListInstances(final GceClient gceClient)
    {
        this.gceClient = gceClient;
    }

    /**
     * Lets the user list machine instances and prints their corresponding agent controller URLs to the console.
     */
    void execute() throws IOException
    {
        List<Instance> instances = new ArrayList<>();

        final List<Region> regions = gceClient.getRegions();
        final List<Region> selectedRegions = GceAdminUtils.multiSelectRegions(regions);
        System.out.println();
        final String filterOption = selectFilterOption();
        System.out.println();

        if (filterOption.equals(FILTER_OPTIONS[0]))
        {
            // no filter
            instances = getAllInstances(selectedRegions);
        }
        else if (filterOption.equals(FILTER_OPTIONS[1]))
        {
            // filter by name label
            instances = getInstancesWithNameLabel(selectedRegions);
        }
        else if (filterOption.equals(FILTER_OPTIONS[2]))
        {
            // "filter" by instance groups
            instances = getInstancesInInstanceGroups(selectedRegions);
        }

        printAgentControllerConnectionProperties(instances);
    }

    /**
     * Asks the user to select a filter option.
     *
     * @return the selected filter option
     */
    private String selectFilterOption()
    {
        return ConsoleUiUtils.selectItem("Filter instances by:", Arrays.asList(FILTER_OPTION_KEYS),
                                         Arrays.asList(FILTER_OPTION_DESCRIPTIONS), Arrays.asList(FILTER_OPTIONS));
    }

    /**
     * Retrieves all instances from the given regions.
     *
     * @param regions
     *            the regions
     * @return the list of instances found
     * @throws IOException
     *             if anything goes wrong
     */
    private List<Instance> getAllInstances(final List<Region> regions) throws IOException
    {
        final List<Instance> instances = getInstancesInRegions(regions);

        if (instances.isEmpty())
        {
            System.out.println();
            System.out.println("No instances found.");
        }

        return instances;
    }

    /**
     * Retrieves all instances from the given regions.
     *
     * @param regions
     *            the regions
     * @return the list of instances found
     * @throws IOException
     *             if anything goes wrong
     */
    private List<Instance> getInstancesInRegions(final List<Region> regions) throws IOException
    {
        final List<Instance> allInstances = new ArrayList<>();

        for (final Region region : regions)
        {
            System.out.printf("Querying all instances in region '%s' ... ", region.getName());
            final List<Instance> instances = gceClient.getInstancesInRegion(region);
            System.out.println("OK");

            allInstances.addAll(filterRunningInstance(instances));
        }

        return allInstances;
    }

    /**
     * Filters the given instances and returns only those that are running.
     * 
     * @param instances
     *            the instances
     * @return the list of running instances
     */
    private List<Instance> filterRunningInstance(final List<Instance> instances)
    {
        return instances.stream().filter(instance -> "RUNNING".equals(instance.getStatus())).collect(Collectors.toList());
    }

    /**
     * Retrieves all instances in the given regions that have a certain name label.
     *
     * @param regions
     *            the regions
     * @return the list of instances found
     * @throws IOException
     *             if anything goes wrong
     */
    private List<Instance> getInstancesWithNameLabel(final List<Region> regions) throws IOException
    {
        final List<Instance> instances = getInstancesInRegions(regions);
        final List<String> nameLabels = getNameLabels(instances);

        System.out.println();

        if (nameLabels.isEmpty())
        {
            System.out.println("No instances with name labels found.");
            return Collections.emptyList();
        }

        return filterInstancesByNameLabels(instances, multiSelectNameLabels(nameLabels));
    }

    /**
     * Asks the user to select one name label from a list of name labels.
     *
     * @param instances
     *            List of selected Regions
     */
    private List<String> getNameLabels(final List<Instance> instances)
    {
        // collect the value of name labels
        final List<String> nameLabels = new ArrayList<>();

        for (final Instance instance : instances)
        {
            final Map<String, String> labels = instance.getLabels();
            if (labels != null)
            {
                final String nameLabel = labels.get("name");
                if (nameLabel != null && !nameLabels.contains(nameLabel))
                {
                    nameLabels.add(nameLabel);
                }
            }
        }

        Collections.sort(nameLabels);

        return nameLabels;
    }

    /**
     * Asks the user to select one or more name labels from the list of name labels.
     *
     * @param nameLabels
     *            the list of name labels
     * @return the selected name labels
     */
    private List<String> multiSelectNameLabels(final List<String> nameLabels)
    {
        return ConsoleUiUtils.multiSelectItems("Select one or more name labels:", nameLabels, nameLabels, true);
    }

    /**
     * Filters a list of instances and returns only those instances that are labeled with at least one of the given name
     * labels.
     *
     * @param instances
     *            the instances to filter
     * @param labels
     *            the name labels to match
     * @return the matching instances
     */
    private List<Instance> filterInstancesByNameLabels(final List<Instance> instances, final List<String> labels)
    {
        final List<Instance> filteredInstances = new ArrayList<>();

        for (final Instance instance : instances)
        {
            final Map<String, String> instanceLabels = instance.getLabels();

            if (instanceLabels != null)
            {
                final String nameLabel = instanceLabels.get("name");

                if (labels.contains(nameLabel))
                {
                    filteredInstances.add(instance);
                }
            }
        }

        return filteredInstances;
    }

    /**
     * Retrieves all instances that belong to an instance group in the given regions.
     *
     * @param regions
     *            the regions
     * @return the instances found
     * @throws IOException
     *             if anything goes wrong
     */
    private List<Instance> getInstancesInInstanceGroups(final List<Region> regions) throws IOException
    {
        final List<InstanceGroup> instanceGroups = getInstanceGroupsInRegions(regions);

        System.out.println();

        if (instanceGroups.isEmpty())
        {
            System.out.println("No instance groups found.");
            return Collections.emptyList();
        }

        return getInstancesOfInstanceGroups(GceAdminUtils.multiSelectInstanceGroups(instanceGroups));
    }

    /**
     * Retrieves all instance groups in the given regions.
     *
     * @param regions
     *            the regions
     * @return the instance groups found
     * @throws IOException
     *             if anything goes wrong
     */
    private List<InstanceGroup> getInstanceGroupsInRegions(final List<Region> regions) throws IOException
    {
        final List<InstanceGroup> allInstanceGroups = new ArrayList<>();

        for (final Region region : regions)
        {
            System.out.printf("Querying all instance groups in region '%s' ... ", region.getName());
            final List<InstanceGroup> instanceGroups = gceClient.getAllInstanceGroups(region);
            System.out.println("OK");

            allInstanceGroups.addAll(instanceGroups);
        }

        return allInstanceGroups;
    }

    /**
     * Retrieves all instances that belong to the given instance groups.
     *
     * @param instanceGroups
     *            the instance groups
     * @return the instances found
     * @throws IOException
     *             if anything goes wrong
     */
    private List<Instance> getInstancesOfInstanceGroups(final List<InstanceGroup> instanceGroups) throws IOException
    {
        final List<Instance> allInstances = new ArrayList<>();

        System.out.println();

        for (final InstanceGroup instanceGroup : instanceGroups)
        {
            System.out.printf("Querying all instances in instance group '%s' ... ", instanceGroup.getName());
            final List<Instance> instancesOfGroup = gceClient.getInstancesInGroup(instanceGroup);
            System.out.println("OK");

            allInstances.addAll(filterRunningInstance(instancesOfGroup));
        }

        return allInstances;
    }

    /**
     * Prints the master controller configuration for the given instances.
     *
     * @param instances
     *            the instances to list
     */
    private void printAgentControllerConnectionProperties(final List<Instance> instances)
    {
        if (!instances.isEmpty())
        {
            System.out.println();
            System.out.println("--- Master controller configuration ---");
            System.out.print(GceAdminUtils.buildAgentControllerConnectionProperties(instances));
        }
    }
}
