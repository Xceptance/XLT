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
package com.xceptance.xlt.gce;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstanceGroup;
import com.google.cloud.compute.v1.Region;
import com.xceptance.common.util.ConsoleUiUtils;

/**
 * Lets the user delete a managed instance group interactively.
 */
class OpDeleteInstanceGroup
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
    OpDeleteInstanceGroup(final GceClient gceClient)
    {
        this.gceClient = gceClient;
    }

    /**
     * Lets the user delete a managed instance group interactively.
     */
    void execute() throws IOException
    {
        final List<Region> regions = gceClient.getRegions();
        final List<Region> selectedRegions = GceAdminUtils.multiSelectRegions(regions);

        System.out.println();

        final List<InstanceGroup> instanceGroups = getInstanceGroupsInRegions(selectedRegions);

        System.out.println();

        if (instanceGroups.isEmpty())
        {
            System.out.println("No managed instance groups found.");
        }
        else
        {

            // request the instance groups to delete
            final List<InstanceGroup> selectedInstanceGroups = GceAdminUtils.multiSelectInstanceGroups(instanceGroups);

            System.out.print("\nYou selected to terminate ");
            if (selectedInstanceGroups.size() == instanceGroups.size())
            {
                System.out.print("*all* ");
            }

            // print the chosen instances to screen
            listInstanceDetails(selectedInstanceGroups);

            if (ConsoleUiUtils.confirm("\nAre you sure?"))
            {
                System.out.println();
                selectedInstanceGroups.forEach(this::deleteInstanceGroup);
            }
        }
    }

    private void listInstanceDetails(final List<InstanceGroup> groups)
    {
        final StringBuilder sb = new StringBuilder();
        final String indentStr = StringUtils.repeat(' ', 2);
        final String indent2Str = StringUtils.repeat(' ', 4);
        final int groupSize = groups.size();

        sb.append("managed instances of group");
        if (groupSize == 1)
        {
            // keep a single line
            sb.append(" '").append(groups.get(0).getName()).append("'\n");
        }
        else
        {
            sb.append("s\n");
            // list all groups, 1 per line
            for (final InstanceGroup group : groups)
            {
                sb.append(indent2Str).append(group.getName()).append('\n');
            }
        }
        sb.append('\n').append(indentStr);

        final Map<String, List<InstanceGroup>> groupsPerRegion = new HashMap<>();
        for (final InstanceGroup group : groups)
        {
            final String regionOfGroup = getRegionName(group);
            List<InstanceGroup> groupsOfRegion = groupsPerRegion.get(regionOfGroup);
            if (groupsOfRegion == null)
            {
                groupsOfRegion = new LinkedList<InstanceGroup>();
                groupsPerRegion.put(regionOfGroup, groupsOfRegion);
            }
            groupsOfRegion.add(group);
        }

        sb.append("in region");
        if (groupsPerRegion.size() > 1)
        {
            sb.append("s:\n");
        }
        else
        {
            sb.append(": ");
        }

        System.out.print(sb.toString());

        if (groupsPerRegion.size() == 1)
        {

            final String singleRegion = getRegionName(groups.get(0));
            System.out.printf("%s ... ", singleRegion);

            try
            {
                final String s = prettyPrintInstances(groups, indent2Str, true);
                System.out.println("OK\n");
                System.out.println(s);
            }
            catch (Exception e)
            {
                System.out.println("Failed: " + e.getMessage());
            }

        }
        else
        {

            for (final Map.Entry<String, List<InstanceGroup>> entry : groupsPerRegion.entrySet())
            {
                System.out.printf("\n%s%s ... ", indentStr, entry.getKey());

                try
                {
                    final String s = prettyPrintInstances(entry.getValue(), indent2Str, true);
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
     * Returns a string representation of all instances that are part of the given managed instance groups.
     * 
     * @param groups
     *            Instances must be part of at least one of the given groups
     * @param lineOffset
     *            All strings will start with the content of lineOffset
     * @return a textual representation of instances
     */
    private String prettyPrintInstances(final List<InstanceGroup> groups, final String lineOffset, final boolean excludeStopped)
        throws Exception
    {
        final StringBuilder output = new StringBuilder();

        int pendingInstanceCount = 0;
        int runningInstanceCount = 0;
        int stoppedInstanceCount = 0;

        final List<MachineInfo> runningMachines = new LinkedList<MachineInfo>();
        for (final InstanceGroup group : groups)
        {
            for (final Instance instance : gceClient.getInstancesInGroup(group))
            {
                final String state = StringUtils.defaultString(instance.getStatus());
                /*
                 * PROVISIONING, STAGING, RUNNING, STOPPING, STOPPED, SUSPENDING, SUSPENDED, and TERMINATED
                 */
                if (state.equals("RUNNING"))
                {
                    runningInstanceCount++;
                }
                else if (state.equals("PROVISIONING"))
                {
                    pendingInstanceCount++;
                }
                else if (state.equals("STOPPED") || state.equals("STOPPING"))
                {
                    if (excludeStopped)
                    {
                        continue;
                    }

                    stoppedInstanceCount++;
                }
                else
                {
                    continue;
                }

                final MachineInfo currentMachineInfo = MachineInfo.createMachineInfo(instance);
                runningMachines.add(currentMachineInfo);
            }
        }

        output.append(lineOffset);
        if ((runningInstanceCount + pendingInstanceCount + stoppedInstanceCount) > 0)
        {

            if (excludeStopped)
            {
                output.append(String.format("%d running and %d pending instance(s) found.\n", runningInstanceCount, pendingInstanceCount));
            }
            else
            {
                output.append(String.format("%d running, %d pending and %d stopped instance(s) found.\n", runningInstanceCount,
                                            pendingInstanceCount, stoppedInstanceCount));
            }

            output.append(MachineInfo.Printer.prettyPrint(runningMachines, lineOffset));
        }
        else
        {
            if (excludeStopped)
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
     * Retrieves all instance groups in the given regions.
     *
     * @param regions
     *            the regions
     * @return the list of instance groups found
     * @throws IOException
     *             if anything goes wrong
     */
    private List<InstanceGroup> getInstanceGroupsInRegions(final List<Region> regions) throws IOException
    {
        final List<InstanceGroup> allInstanceGroups = new ArrayList<>();

        for (final Region region : regions)
        {
            System.out.printf("Retrieving all managed instance groups in region '%s' ... ", region.getName());
            final List<InstanceGroup> instanceGroups = gceClient.getAllInstanceGroups(region);
            System.out.println("OK");

            allInstanceGroups.addAll(instanceGroups);
        }

        return allInstanceGroups;
    }

    /**
     * Deletes the given instance group.
     *
     * @param instanceGroup
     *            the instance group to delete
     */
    private void deleteInstanceGroup(final InstanceGroup instanceGroup)
    {
        try
        {
            System.out.printf("Deleting instance group '%s' ... ", instanceGroup.getName());
            gceClient.deleteInstanceGroup(instanceGroup);
            System.out.println("OK");
        }
        catch (final Exception e)
        {
            GceAdminUtils.logError(String.format("Failed to delete instance group '%s'", instanceGroup.getName()), e);
        }
    }

    /**
     * Returns the user-friendly region name of the given instance group.
     * 
     * @param group
     *            the instance group
     * @return user-friendly region name of the given instance group
     */
    private static String getRegionName(final InstanceGroup group)
    {
        String s = GceAdminUtils.getRegionName(group.getRegion());
        if (s == null)
        {
            s = StringUtils.substringBeforeLast(GceAdminUtils.getZoneName(group.getZone()), "-");
        }

        return StringUtils.defaultString(s, "<unknown region>");
    }
}
