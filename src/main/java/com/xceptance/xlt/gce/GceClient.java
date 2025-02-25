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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.api.client.http.HttpResponseException;
import com.google.api.gax.rpc.ApiException;
import com.google.api.gax.rpc.StatusCode;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstanceGroup;
import com.google.cloud.compute.v1.InstanceGroupManager;
import com.google.cloud.compute.v1.InstanceGroupManagersClient;
import com.google.cloud.compute.v1.InstanceGroupsClient;
import com.google.cloud.compute.v1.InstanceGroupsListInstancesRequest;
import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.InstanceTemplatesClient;
import com.google.cloud.compute.v1.InstanceWithNamedPorts;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.ManagedInstance;
import com.google.cloud.compute.v1.Region;
import com.google.cloud.compute.v1.RegionInstanceGroupManagersClient;
import com.google.cloud.compute.v1.RegionInstanceGroupsClient;
import com.google.cloud.compute.v1.RegionInstanceGroupsListInstancesRequest;
import com.google.cloud.compute.v1.RegionsClient;
import com.google.cloud.compute.v1.RegionsClient.ListPagedResponse;
import com.google.common.collect.Lists;
import com.xceptance.common.lang.ThreadUtils;

/**
 * Wrapper around the Google Compute API to provide higher-level functionality when dealing with GCE machine instances.
 */
class GceClient
{
    /**
     * The "running" instance state.
     */
    private static final String STATE_RUNNING = "RUNNING";

    /**
     * The polling interval [ms].
     */
    private static final long INSTANCE_STATE_POLLING_INTERVAL = 1000;

    /**
     * The client responsible for regions.
     */
    private final RegionsClient regionsClient;

    /**
     * The client responsible for instances.
     */
    private final InstancesClient instancesClient;

    /**
     * The client responsible for instance templates.
     */
    private final InstanceTemplatesClient instanceTemplatesClient;

    /**
     * The client responsible for instance group managers.
     */
    private final InstanceGroupManagersClient instanceGroupManagersClient;

    /**
     * The client responsible for region instance group managers.
     */
    private final RegionInstanceGroupManagersClient regionInstanceGroupManagersClient;

    /**
     * The client responsible for instance groups.
     */
    private final InstanceGroupsClient instanceGroupsClient;

    /**
     * The client responsible for region instance groups.
     */
    private final RegionInstanceGroupsClient regionInstanceGroupsClient;

    /**
     * The Google Cloud Platform project ID.
     */
    private final String projectId;

    /**
     * The time to wait for newly started instances to become running.
     */
    private final long instanceConnectTimeout;

    /**
     * The available regions.
     */
    private final List<Region> regions;

    /**
     * The available regions, keyed by region name.
     */
    private final Map<String, Region> regionsByName;

    /**
     * Constructor.
     *
     * @param projectId
     *            the project ID
     * @param instanceConnectTimeout
     *            the timeout
     * @throws ApiException
     *             in case of a communication error
     * @throws IOException
     *             in case of a communication error
     */
    GceClient(final String projectId, final long instanceConnectTimeout) throws ApiException, IOException
    {
        this.projectId = projectId;
        this.instanceConnectTimeout = Math.max(instanceConnectTimeout, 0);

        // pre-create the clients we need
        regionsClient = RegionsClient.create();
        instanceTemplatesClient = InstanceTemplatesClient.create();
        instancesClient = InstancesClient.create();
        instanceGroupsClient = InstanceGroupsClient.create();
        instanceGroupManagersClient = InstanceGroupManagersClient.create();
        regionInstanceGroupsClient = RegionInstanceGroupsClient.create();
        regionInstanceGroupManagersClient = RegionInstanceGroupManagersClient.create();

        // preload the available regions
        regionsByName = loadRegions();
        regions = new ArrayList<>(regionsByName.values());
    }

    /**
     * Preloads the regions that are currently available.
     *
     * @return the regions
     * @throws ApiException
     *             in case of a communication error
     */
    private Map<String, Region> loadRegions() throws ApiException
    {
        final Map<String, Region> regions = new TreeMap<>();

        final ListPagedResponse response = regionsClient.list(projectId);
        for (final Region region : response.iterateAll())
        {
            regions.put(region.getName(), region);
        }

        return regions;
    }

    /**
     * Returns the available regions.
     *
     * @return the regions
     */
    List<Region> getRegions()
    {
        return regions;
    }

    /**
     * Returns the region for the given region name.
     *
     * @param regionName
     *            the region name
     * @return the region
     */
    Region getRegion(final String regionName)
    {
        final Region region = regionsByName.get(regionName);

        if (region == null)
        {
            throw new IllegalArgumentException("Unknown region: " + regionName);
        }
        else
        {
            return region;
        }
    }

    /**
     * Creates a new managed instance group.
     *
     * @param regionName
     *            the region
     * @param instanceGroupName
     *            the name of the instance group
     * @param instanceTemplateName
     *            the name of the instance template to use
     * @param instanceCount
     *            the number of instances in the group
     * @throws ApiException
     *             in case of a communication error
     * @throws ExecutionException
     *             in case an asynchronous operation failed with an exception
     * @throws InterruptedException
     *             in case waiting for an asynchronous operation to complete was interrupted
     */
    void createInstanceGroup(final String regionName, final String instanceGroupName, final String instanceTemplateName,
                             final int instanceCount)
        throws ApiException, InterruptedException, ExecutionException
    {
        final InstanceGroupManager.Builder instanceGroupManagerBuilder = InstanceGroupManager.newBuilder();
        instanceGroupManagerBuilder.setBaseInstanceName(instanceGroupName);
        instanceGroupManagerBuilder.setName(instanceGroupName);
        instanceGroupManagerBuilder.setInstanceTemplate("global/instanceTemplates/" + instanceTemplateName);
        instanceGroupManagerBuilder.setTargetSize(instanceCount);

        regionInstanceGroupManagersClient.insertAsync(projectId, regionName, instanceGroupManagerBuilder.build()).get();
    }

    /**
     * Waits for all instances in the given region/group to be running.
     *
     * @param regionName
     *            the region
     * @param instanceGroupName
     *            the name of the instance group
     * @param instanceCount
     *            the number of instances in the group
     * @return the instances in the group
     * @throws TimeoutException
     *             if any of the instances did not reach the running state in time
     * @throws ApiException
     *             in case of a communication error
     */
    List<Instance> waitForInstancesAreRunning(final String regionName, final String instanceGroupName, final int instanceCount)
        throws ApiException, TimeoutException
    {
        final long timeout = instanceConnectTimeout;
        final long deadline = System.currentTimeMillis() + timeout;

        // wait for all the instances to exist
        List<Instance> instances = waitForInstancesToExist(regionName, instanceGroupName, instanceCount, timeout);

        // wait for all the instances to reach state RUNNING
        final long remainingTimeout = deadline - System.currentTimeMillis();
        instances = waitForInstanceState(instances, GceClient.STATE_RUNNING, remainingTimeout);

        return instances;
    }

    /**
     * Waits for all instances in the given region/group to exist.
     *
     * @param regionName
     *            the region
     * @param instanceGroupName
     *            the name of the instance group
     * @param instanceCount
     *            the number of instances in the group
     * @param timeout
     *            the time to wait
     * @return the instances in the group
     * @throws ApiException
     *             in case of a communication error
     * @throws TimeoutException
     *             if any of the instances did not exist in time
     */
    List<Instance> waitForInstancesToExist(final String regionName, final String instanceGroupName, final int instanceCount,
                                           final long timeout)
        throws ApiException, TimeoutException
    {
        final long deadline = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < deadline)
        {
            final List<ManagedInstance> managedInstances = getManagedInstances(regionName, instanceGroupName);
            if (managedInstances != null && managedInstances.size() == instanceCount)
            {
                boolean ok = true;

                // check each managed instance if its corresponding instance exists
                for (final ManagedInstance managedInstance : managedInstances)
                {
                    final String status = managedInstance.getInstanceStatus();
                    if (status == null || status.isEmpty())
                    {
                        ok = false;
                        break;
                    }
                }

                if (ok)
                {
                    return getInstances(managedInstances);
                }
            }

            // wait some time
            ThreadUtils.sleep(INSTANCE_STATE_POLLING_INTERVAL);
        }

        throw new TimeoutException(String.format("One or more of the %d instances did not exist within %d seconds", instanceCount,
                                                 timeout));
    }

    /**
     * Returns the managed instances in the given instance group.
     *
     * @param regionName
     *            the region
     * @param instanceGroupName
     *            the name of the instance group
     * @return the managed instances
     * @throws ApiException
     *             in case of a communication error
     */
    private List<ManagedInstance> getManagedInstances(final String regionName, final String instanceGroupName) throws ApiException
    {
        return Lists.newArrayList(regionInstanceGroupManagersClient.listManagedInstances(projectId, regionName, instanceGroupName)
                                                                   .iterateAll());
    }

    /**
     * Returns the real instances behind the given managed instances.
     *
     * @param managedInstances
     *            the managed instances
     * @return the real instances
     * @throws ApiException
     *             in case of a communication error
     */
    private List<Instance> getInstances(final List<ManagedInstance> managedInstances) throws ApiException
    {
        final List<Instance> instances = new ArrayList<>();

        for (final ManagedInstance managedInstance : managedInstances)
        {
            instances.add(getInstance(managedInstance));
        }

        return instances;
    }

    /**
     * Returns the real instance behind the given managed instance.
     *
     * @param managedInstance
     *            the managed instance
     * @return the real instance
     * @throws ApiException
     *             in case of a communication error
     */
    private Instance getInstance(final ManagedInstance managedInstance) throws ApiException
    {
        final String zoneName = GceAdminUtils.getZoneName(managedInstance.getInstance());
        final String instanceName = GceAdminUtils.getInstanceName(managedInstance.getInstance());

        return instancesClient.get(projectId, zoneName, instanceName);
    }

    /**
     * Waits for all the instances to reach the given state.
     *
     * @param instances
     *            the instances to check
     * @param state
     *            the wanted instance state
     * @param timeout
     *            the maximum waiting time
     * @return the updated instances
     * @throws TimeoutException
     *             if any of the instances did not reach the wanted state in time
     * @throws ApiException
     *             in case of a communication error
     */
    List<Instance> waitForInstanceState(final List<Instance> instances, final String state, final long timeout)
        throws ApiException, TimeoutException
    {
        final List<Instance> updatedInstances = new ArrayList<>();

        final long deadline = System.currentTimeMillis() + timeout;

        // wait for the instances to achieve the wanted state
        for (final Instance instance : instances)
        {
            final long remainingTimeout = deadline - System.currentTimeMillis();

            final Instance updatedInstance = waitForInstanceState(instance, state, remainingTimeout);

            updatedInstances.add(updatedInstance);
        }

        return updatedInstances;
    }

    /**
     * Waits for the instance to reach the given state.
     *
     * @param instance
     *            the instance to check
     * @param state
     *            the wanted instance state
     * @param timeout
     *            the maximum waiting time
     * @return the updated instance
     * @throws ApiException
     *             in case of a communication error
     * @throws TimeoutException
     *             if the instance did not reach the wanted state in time
     */
    Instance waitForInstanceState(Instance instance, final String state, final long timeout) throws ApiException, TimeoutException
    {
        final long deadline = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < deadline)
        {
            instance = getInstance(instance.getSelfLink());
            if (instance != null && instance.getStatus().equals(state))
            {
                return instance;
            }

            // wait some time
            ThreadUtils.sleep(INSTANCE_STATE_POLLING_INTERVAL);
        }

        throw new TimeoutException(String.format("Instance '%s' did not reach state '%s' within %d seconds. Current state is '%s'.",
                                                 instance.getName(), state, timeout / 1000, instance.getStatus()));
    }

    /**
     * Returns the instance for the given instance URL.
     *
     * @param instanceUrl
     *            the instance URL
     * @return the instance
     * @throws ApiException
     *             in case of a communication error
     */
    Instance getInstance(final String instanceUrl) throws ApiException
    {
        final String zoneName = GceAdminUtils.getZoneName(instanceUrl);
        final String instanceName = GceAdminUtils.getInstanceName(instanceUrl);

        return instancesClient.get(projectId, zoneName, instanceName);
    }

    /**
     * Returns the list of available instance templates.
     *
     * @return the available instance templates
     * @throws ApiException
     *             in case of a communication error
     */
    List<InstanceTemplate> getInstanceTemplates() throws ApiException
    {
        final List<InstanceTemplate> instanceTemplates = Lists.newArrayList(instanceTemplatesClient.list(projectId).iterateAll());

        // sort the instance templates by name
        Collections.sort(instanceTemplates, new Comparator<InstanceTemplate>()
        {
            @Override
            public int compare(final InstanceTemplate t1, final InstanceTemplate t2)
            {
                final String s1 = StringUtils.defaultString(t1.getName());
                final String s2 = StringUtils.defaultString(t2.getName());
                return s1.compareTo(s2);
            }
        });

        return instanceTemplates;
    }

    /**
     * Returns the instance group with the given name in the given region.
     *
     * @param regionOrZoneName
     *            the name of the region or zone
     * @param instanceGroupName
     *            the name of the instance group
     * @return the instance group
     * @throws ApiException
     *             in case of a communication error
     */
    InstanceGroup getInstanceGroup(final String regionOrZoneName, final String instanceGroupName) throws ApiException
    {
        /*
         * N.B.: Instance groups can be defined as zonal or regional (multi-zone)
         */

        // look for a multi-zone instance group
        if (regionsByName.containsKey(regionOrZoneName))
        {
            try
            {
                return regionInstanceGroupsClient.get(projectId, regionOrZoneName, instanceGroupName);
            }
            catch (final ApiException ex)
            {
                // requesting a zonal instance group from a region causes a 404 Not Found
                // -> loop through the zones of that region as fallback
                if (isNotFound(ex))
                {
                    for (final String zone : getZoneNamesFromRegion(getRegion(regionOrZoneName)))
                    {
                        final InstanceGroup group = instanceGroupsClient.get(projectId, zone, instanceGroupName);
                        if (group != null)
                        {
                            return group;
                        }
                    }
                }

                throw ex;

            }
        }
        // look for a single-zone instance group
        else
        {
            return instanceGroupsClient.get(projectId, regionOrZoneName, instanceGroupName);
        }
    }

    /**
     * Returns all managed instance groups in the given region. This includes both multi-zone and single-zone instance
     * groups.
     *
     * @param region
     *            the region
     * @return the instance groups
     * @throws ApiException
     *             in case of a communication error
     */
    List<InstanceGroup> getAllInstanceGroups(final Region region) throws ApiException
    {
        final List<InstanceGroup> groupList = new ArrayList<>();

        final String regionName = region.getName();

        // add regionally managed instance groups
        groupList.addAll(getMultiZoneInstanceGroups(regionName));

        // loop through all zones in the region
        final List<String> zoneNames = getZoneNamesFromRegion(region);
        for (final String zoneName : zoneNames)
        {
            // add zonally managed instance groups
            groupList.addAll(getSingleZoneInstanceGroups(zoneName));
        }

        return groupList;
    }

    /**
     * Returns all multi-zone managed instance groups in the given region.
     *
     * @param regionName
     *            the region
     * @return the instance groups
     * @throws ApiException
     *             in case of a communication error
     */
    List<InstanceGroup> getMultiZoneInstanceGroups(final String regionName) throws ApiException
    {
        return Lists.newArrayList(regionInstanceGroupsClient.list(projectId, regionName).iterateAll());
    }

    /**
     * Returns all single-zone managed instance groups in the given region.
     *
     * @param zoneName
     *            the zone
     * @return the instance groups
     * @throws ApiException
     *             in case of a communication error
     */
    List<InstanceGroup> getSingleZoneInstanceGroups(final String zoneName) throws ApiException
    {
        return Lists.newArrayList(instanceGroupsClient.list(projectId, zoneName).iterateAll());
    }

    /**
     * Returns all instances in the given region.
     *
     * @param region
     *            the region
     * @return the instances
     * @throws ApiException
     *             in case of a communication error
     */
    List<Instance> getInstancesInRegion(final Region region) throws ApiException
    {
        final List<Instance> instances = new ArrayList<>();

        final List<String> zoneNames = getZoneNamesFromRegion(region);
        for (final String zoneName : zoneNames)
        {
            instances.addAll(getInstancesInZone(zoneName));
        }

        return instances;
    }

    /**
     * Returns the list of zone names for the given region.
     *
     * @param region
     *            the region
     * @return list of zone names or {@code null} if the region's zone list is {@code null}
     */
    private List<String> getZoneNamesFromRegion(final Region region)
    {
        List<String> zones = region.getZonesList();
        if (zones != null)
        {
            zones = zones.stream().map(GceAdminUtils::getZoneName).collect(Collectors.toList());
        }
        return zones;
    }

    /**
     * Returns all instances in the given zone.
     *
     * @param zoneName
     *            the zone
     * @return the instances
     * @throws ApiException
     *             in case of a communication error
     */
    List<Instance> getInstancesInZone(final String zoneName) throws ApiException
    {
        return Lists.newArrayList(instancesClient.list(projectId, zoneName).iterateAll());
    }

    /**
     * Returns all instances in the given instance group.
     *
     * @param instanceGroup
     *            the instance group
     * @return the instances
     * @throws ApiException
     *             in case of a communication error
     */
    List<Instance> getInstancesInGroup(final InstanceGroup instanceGroup) throws ApiException
    {
        final List<Instance> instances = new ArrayList<>();

        final String groupName = instanceGroup.getName();

        // Check for regional managed group
        if (instanceGroup.getSelfLink().contains("regions"))
        {
            final String regionName = GceAdminUtils.getRegionName(instanceGroup.getRegion());

            final RegionInstanceGroupsListInstancesRequest request = RegionInstanceGroupsListInstancesRequest.newBuilder().build();

            final Iterable<InstanceWithNamedPorts> instancesWithNamedPorts = regionInstanceGroupsClient.listInstances(projectId, regionName,
                                                                                                                      groupName, request)
                                                                                                       .iterateAll();
            for (final InstanceWithNamedPorts instanceWithNamedPorts : instancesWithNamedPorts)
            {
                final String instanceUrl = instanceWithNamedPorts.getInstance();
                final String zoneName = GceAdminUtils.getZoneName(instanceUrl);
                final String instanceName = GceAdminUtils.getInstanceName(instanceUrl);

                final Instance instance = instancesClient.get(projectId, zoneName, instanceName);

                instances.add(instance);
            }
        }

        // Check for zonal managed group
        else if (instanceGroup.getSelfLink().contains("zones"))
        {
            final String zoneName = GceAdminUtils.getZoneName(instanceGroup.getZone());

            final InstanceGroupsListInstancesRequest request = InstanceGroupsListInstancesRequest.newBuilder().build();

            final Iterable<InstanceWithNamedPorts> instancesWithNamedPorts = instanceGroupsClient.listInstances(projectId, zoneName,
                                                                                                                groupName, request)
                                                                                                 .iterateAll();
            for (final InstanceWithNamedPorts instanceWithNamedPorts : instancesWithNamedPorts)
            {
                final String instanceUrl = instanceWithNamedPorts.getInstance();
                final String instanceName = GceAdminUtils.getInstanceName(instanceUrl);

                final Instance instance = instancesClient.get(projectId, zoneName, instanceName);

                instances.add(instance);
            }
        }

        return instances;
    }

    /**
     * Deletes the given managed instance group.
     *
     * @param instanceGroup
     *            the instance group to delete
     * @throws ApiException
     *             in case of a communication error
     * @throws ExecutionException
     *             in case an asynchronous operation failed with an exception
     * @throws InterruptedException
     *             in case waiting for an asynchronous operation to complete was interrupted
     */
    void deleteInstanceGroup(final InstanceGroup instanceGroup) throws ApiException, InterruptedException, ExecutionException
    {
        // check whether instance group is regional
        final String regionUrl = instanceGroup.getRegion();
        if (regionUrl != null)
        {
            final String regionName = GceAdminUtils.getRegionName(regionUrl);
            regionInstanceGroupManagersClient.deleteAsync(projectId, regionName, instanceGroup.getName()).get();
        }
        else // instance group must be zonal
        {
            final String zoneName = GceAdminUtils.getZoneName(instanceGroup.getZone());
            instanceGroupManagersClient.deleteAsync(projectId, zoneName, instanceGroup.getName()).get();
        }
    }

    /**
     * Determines if the given exception denotes a NotFound status.
     *
     * @param ex
     *            the exception to check
     * @return {@code true} iff the given exception is an instance of {@link HttpResponseException} and its status code
     *         is 404, {@code false} otherwise
     */
    private static boolean isNotFound(final ApiException ex)
    {
        return ex.getStatusCode().getCode() == StatusCode.Code.NOT_FOUND;
    }
}
