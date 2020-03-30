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
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.InstanceGroup;
import com.google.api.services.compute.model.InstanceGroupList;
import com.google.api.services.compute.model.InstanceGroupManager;
import com.google.api.services.compute.model.InstanceGroupsListInstances;
import com.google.api.services.compute.model.InstanceGroupsListInstancesRequest;
import com.google.api.services.compute.model.InstanceList;
import com.google.api.services.compute.model.InstanceTemplate;
import com.google.api.services.compute.model.InstanceTemplateList;
import com.google.api.services.compute.model.InstanceWithNamedPorts;
import com.google.api.services.compute.model.ManagedInstance;
import com.google.api.services.compute.model.Region;
import com.google.api.services.compute.model.RegionInstanceGroupList;
import com.google.api.services.compute.model.RegionInstanceGroupsListInstances;
import com.google.api.services.compute.model.RegionInstanceGroupsListInstancesRequest;
import com.google.api.services.compute.model.RegionList;
import com.xceptance.common.lang.ThreadUtils;

/**
 * Wrapper around {@link Compute} to provide higher-level functionality when dealing with GCE machine instances.
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
     * The underlying {@link Compute} object.
     */
    private final Compute compute;

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
     * @param applicationName
     *            the application name
     * @param projectId
     *            the project ID
     * @param instanceConnectTimeout
     *            the timeout
     * @throws GeneralSecurityException
     * @throws IOException
     *             in case of a communication error
     */
    GceClient(final String applicationName, final String projectId, final long instanceConnectTimeout)
        throws GeneralSecurityException, IOException
    {
        this.projectId = projectId;
        this.instanceConnectTimeout = Math.max(instanceConnectTimeout, 0);

        // Authenticate using Google Application Default Credentials.
        GoogleCredential credential = GoogleCredential.getApplicationDefault();

        if (credential.createScopedRequired())
        {
            final List<String> scopes = new ArrayList<>();

            // Set Google Cloud Storage scope to Full Control.
            scopes.add(ComputeScopes.DEVSTORAGE_FULL_CONTROL);

            // Set Google Compute Engine scope to Read-write.
            scopes.add(ComputeScopes.COMPUTE);
            credential = credential.createScoped(scopes);
        }

        // Create Compute Engine object
        compute = new Compute.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
                                      credential).setApplicationName(applicationName).build();

        // preload the available regions
        regionsByName = loadRegions();
        regions = new ArrayList<>(regionsByName.values());
    }

    /**
     * Preloads the regions that are currently available.
     * 
     * @return the regions
     * @throws IOException
     *             in case of a communication error
     */
    private Map<String, Region> loadRegions() throws IOException
    {
        final Map<String, Region> regions = new TreeMap<>();

        final Compute.Regions.List request = compute.regions().list(projectId);
        RegionList response;

        do
        {
            response = request.execute();

            final List<Region> items = response.getItems();
            if (items != null)
            {
                for (Region region : items)
                {
                    regions.put(region.getName(), region);
                }
            }

            request.setPageToken(response.getNextPageToken());
        }
        while (response.getNextPageToken() != null);

        return regions;
    }

    /**
     * Returns the available regions.
     * 
     * @return the regions
     */
    List<Region> getRegions() throws IOException
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
     * @throws IOException
     *             in case of a communication error
     */
    void createInstanceGroup(final String regionName, final String instanceGroupName, final String instanceTemplateName,
                             final int instanceCount)
        throws IOException
    {
        final InstanceGroupManager instanceGroupManager = new InstanceGroupManager();
        instanceGroupManager.setBaseInstanceName(instanceGroupName);
        instanceGroupManager.setName(instanceGroupName);
        instanceGroupManager.setInstanceTemplate("global/instanceTemplates/" + instanceTemplateName);
        instanceGroupManager.setTargetSize(instanceCount);

        compute.regionInstanceGroupManagers().insert(projectId, regionName, instanceGroupManager).execute();
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
     * @throws IOException
     *             in case of a communication error
     */
    List<Instance> waitForInstancesAreRunning(final String regionName, final String instanceGroupName, final int instanceCount)
        throws IOException, TimeoutException
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
     * @throws IOException
     *             in case of a communication error
     * @throws TimeoutException
     *             if any of the instances did not exist in time
     */
    List<Instance> waitForInstancesToExist(final String regionName, final String instanceGroupName, final int instanceCount,
                                           final long timeout)
        throws IOException, TimeoutException
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
                    ok = ok && managedInstance.getInstanceStatus() != null;
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
     * @throws IOException
     *             in case of a communication error
     */
    private List<ManagedInstance> getManagedInstances(final String regionName, final String instanceGroupName) throws IOException
    {
        return compute.regionInstanceGroupManagers().listManagedInstances(projectId, regionName, instanceGroupName).execute()
                      .getManagedInstances();
    }

    /**
     * Returns the real instances behind the given managed instances.
     * 
     * @param managedInstances
     *            the managed instances
     * @return the real instances
     * @throws IOException
     *             in case of a communication error
     */
    private List<Instance> getInstances(final List<ManagedInstance> managedInstances) throws IOException
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
     * @throws IOException
     *             in case of a communication error
     */
    private Instance getInstance(final ManagedInstance managedInstance) throws IOException
    {
        final String zoneName = GceAdminUtils.getZoneName(managedInstance.getInstance());
        final String instanceName = GceAdminUtils.getInstanceName(managedInstance.getInstance());

        return compute.instances().get(projectId, zoneName, instanceName).execute();
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
     * @throws IOException
     *             in case of a communication error
     */
    List<Instance> waitForInstanceState(final List<Instance> instances, final String state, final long timeout)
        throws IOException, TimeoutException
    {
        final List<Instance> updatedInstances = new ArrayList<>();

        final long deadline = System.currentTimeMillis() + timeout;

        // wait for the instances to achieve state RUNNING
        for (final Instance instance : instances)
        {
            final long remainingTimeout = deadline - System.currentTimeMillis();

            // Waiting for the instance state 'RUNNING'
            final Instance updatedInstance = waitForInstanceState(instance, STATE_RUNNING, remainingTimeout);

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
     * @throws IOException
     *             in case of a communication error
     * @throws TimeoutException
     *             if the instance did not reach the wanted state in time
     */
    Instance waitForInstanceState(Instance instance, final String state, final long timeout) throws IOException, TimeoutException
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
     * @throws IOException
     *             in case of a communication error
     */
    Instance getInstance(final String instanceUrl) throws IOException
    {
        final String zoneName = GceAdminUtils.getZoneName(instanceUrl);
        final String instanceName = GceAdminUtils.getInstanceName(instanceUrl);

        return compute.instances().get(projectId, zoneName, instanceName).execute();
    }

    /**
     * Returns the list of available instance templates.
     * 
     * @return the available instance templates
     * @throws IOException
     *             in case of a communication error
     */
    List<InstanceTemplate> getInstanceTemplates() throws IOException
    {
        final List<InstanceTemplate> instanceTemplates = new ArrayList<>();

        final Compute.InstanceTemplates.List request = compute.instanceTemplates().list(projectId);

        InstanceTemplateList response;
        do
        {
            response = request.execute();

            if (response.getItems() != null)
            {
                instanceTemplates.addAll(response.getItems());
            }

            request.setPageToken(response.getNextPageToken());
        }
        while (response.getNextPageToken() != null);

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
     * @throws IOException
     *             in case of a communication error
     */
    InstanceGroup getInstanceGroup(final String regionOrZoneName, final String instanceGroupName) throws IOException
    {
        /*
         * N.B.: Instance groups can be defined as zonal or regional (multi-zone)
         */

        // look for a multi-zone instance group
        if (regionsByName.containsKey(regionOrZoneName))
        {
            try
            {
                return compute.regionInstanceGroups().get(projectId, regionOrZoneName, instanceGroupName).execute();
            }
            catch (final IOException ex)
            {
                // requesting a zonal instance group from a region causes a 404 Not Found
                // -> loop through the zones of that region as fallback
                if (isNotFound(ex))
                {
                    for (final String zone : getZoneNamesFromRegion(getRegion(regionOrZoneName)))
                    {
                        final InstanceGroup group = getOrNull(compute.instanceGroups().get(projectId, zone, instanceGroupName));
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
            return compute.instanceGroups().get(projectId, regionOrZoneName, instanceGroupName).execute();
        }
    }

    /**
     * Returns all managed instance groups in the given region. This includes both multi-zone and single-zone instance
     * groups.
     *
     * @param region
     *            the region
     * @return the instance groups
     * @throws IOException
     *             in case of a communication error
     */
    List<InstanceGroup> getAllInstanceGroups(final Region region) throws IOException
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
     * @throws IOException
     *             in case of a communication error
     */
    List<InstanceGroup> getMultiZoneInstanceGroups(final String regionName) throws IOException
    {
        final List<InstanceGroup> instanceGroups = new ArrayList<>();

        final Compute.RegionInstanceGroups.List request = compute.regionInstanceGroups().list(projectId, regionName);
        RegionInstanceGroupList response;

        do
        {
            response = request.execute();

            if (response.getItems() != null)
            {
                instanceGroups.addAll(response.getItems());
            }

            request.setPageToken(response.getNextPageToken());
        }
        while (response.getNextPageToken() != null);

        return instanceGroups;
    }

    /**
     * Returns all single-zone managed instance groups in the given region.
     *
     * @param zoneName
     *            the zone
     * @return the instance groups
     * @throws IOException
     *             in case of a communication error
     */
    List<InstanceGroup> getSingleZoneInstanceGroups(final String zoneName) throws IOException
    {
        final List<InstanceGroup> instanceGroups = new ArrayList<>();

        final Compute.InstanceGroups.List request = compute.instanceGroups().list(projectId, zoneName);
        InstanceGroupList response;

        do
        {
            response = request.execute();

            if (response.getItems() != null)
            {
                instanceGroups.addAll(response.getItems());
            }

            request.setPageToken(response.getNextPageToken());
        }
        while (response.getNextPageToken() != null);

        return instanceGroups;
    }

    /**
     * Returns all instances in the given region.
     *
     * @param region
     *            the region
     * @return the instances
     * @throws IOException
     *             in case of a communication error
     */
    List<Instance> getInstancesInRegion(final Region region) throws IOException
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
        List<String> zones = region.getZones();
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
     * @throws IOException
     *             in case of a communication error
     */
    List<Instance> getInstancesInZone(final String zoneName) throws IOException
    {
        final List<Instance> instanceList = new ArrayList<>();

        // Request a list of instances in the specified zone
        final InstanceList instances = compute.instances().list(projectId, zoneName).execute();
        if (instances.getItems() != null)
        {
            instanceList.addAll(instances.getItems());
        }

        return instanceList;
    }

    /**
     * Returns all instances in the given instance group.
     *
     * @param instanceGroup
     *            the instance group
     * @return the instances
     * @throws IOException
     *             in case of a communication error
     */
    List<Instance> getInstancesInGroup(final InstanceGroup instanceGroup) throws IOException
    {
        final List<Instance> instances = new ArrayList<>();

        final String groupName = instanceGroup.getName();

        // Check for regional managed group
        if (instanceGroup.getSelfLink().contains("regions"))
        {
            final String regionName = GceAdminUtils.getRegionName(instanceGroup.getRegion());

            final RegionInstanceGroupsListInstancesRequest requestBody = new RegionInstanceGroupsListInstancesRequest();

            final Compute.RegionInstanceGroups.ListInstances request = compute.regionInstanceGroups().listInstances(projectId, regionName,
                                                                                                                    groupName, requestBody);
            RegionInstanceGroupsListInstances response;
            do
            {
                response = request.execute();

                if (response.getItems() == null)
                {
                    continue;
                }

                for (final InstanceWithNamedPorts instanceWithNamedPorts : response.getItems())
                {
                    final String instanceUrl = instanceWithNamedPorts.getInstance();
                    final String zoneName = GceAdminUtils.getZoneName(instanceUrl);
                    final String instanceName = GceAdminUtils.getInstanceName(instanceUrl);

                    final Instance instance = compute.instances().get(projectId, zoneName, instanceName).execute();

                    instances.add(instance);
                }

                request.setPageToken(response.getNextPageToken());
            }
            while (response.getNextPageToken() != null);
        }

        // Check for zonal managed group
        else if (instanceGroup.getSelfLink().contains("zones"))
        {
            final String zoneName = GceAdminUtils.getZoneName(instanceGroup.getZone());

            final InstanceGroupsListInstancesRequest requestBody = new InstanceGroupsListInstancesRequest();

            final Compute.InstanceGroups.ListInstances request = compute.instanceGroups().listInstances(projectId, zoneName, groupName,
                                                                                                        requestBody);
            InstanceGroupsListInstances response;
            do
            {
                response = request.execute();
                if (response.getItems() == null)
                {
                    continue;
                }

                for (final InstanceWithNamedPorts instanceWithNamedPorts : response.getItems())
                {
                    final String instanceUrl = instanceWithNamedPorts.getInstance();
                    final String instanceName = GceAdminUtils.getInstanceName(instanceUrl);

                    final Instance instance = compute.instances().get(projectId, zoneName, instanceName).execute();

                    instances.add(instance);
                }

                request.setPageToken(response.getNextPageToken());
            }
            while (response.getNextPageToken() != null);
        }

        return instances;
    }

    /**
     * Deletes the given managed instance group.
     * 
     * @param instanceGroup
     *            the instance group to delete
     * @throws IOException
     *             in case of a communication error
     */
    void deleteInstanceGroup(final InstanceGroup instanceGroup) throws IOException
    {
        // check whether instance group is regional
        final String regionUrl = instanceGroup.getRegion();
        if (regionUrl != null)
        {
            final String regionName = GceAdminUtils.getRegionName(regionUrl);
            compute.regionInstanceGroupManagers().delete(projectId, regionName, instanceGroup.getName()).execute();
        }
        else // instance group must be zonal
        {
            final String zoneName = GceAdminUtils.getZoneName(instanceGroup.getZone());
            compute.instanceGroupManagers().delete(projectId, zoneName, instanceGroup.getName()).execute();
        }
    }

    /**
     * Executes the given request and return its result if there is any, and {@code null} if the requested resource does
     * not exist.
     * 
     * @param request
     *            the request to execute
     * @return result (response) of the given request or {@code null} if the requested resource does not exist
     * @throws IOException
     *             any exception thrown while executing the request and that does not denote a non-existing resource
     */
    static <T> T getOrNull(final AbstractGoogleClientRequest<T> request) throws IOException
    {
        try
        {
            return request.execute();
        }
        catch (final IOException ioe)
        {
            if (isNotFound(ioe))
            {
                return null;
            }

            throw ioe;
        }
    }

    /**
     * Determines if the given exception denotes a NotFound status.
     * 
     * @param ioe
     *            the exception to check
     * @return {@code true} iff the given exception is an instance of {@link HttpResponseException} and its status code
     *         is 404, {@code false} otherwise
     */
    private static boolean isNotFound(final IOException ioe)
    {
        return ioe instanceof HttpResponseException && ((HttpResponseException) ioe).getStatusCode() == 404;
    }
}
