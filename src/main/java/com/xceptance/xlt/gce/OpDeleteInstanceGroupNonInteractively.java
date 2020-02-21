package com.xceptance.xlt.gce;

import com.google.api.services.compute.model.InstanceGroup;

/**
 * Deletes a managed instance group non-interactively.
 */
class OpDeleteInstanceGroupNonInteractively
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
    OpDeleteInstanceGroupNonInteractively(final GceClient gceClient)
    {
        this.gceClient = gceClient;
    }

    /**
     * Deletes a managed instance group non-interactively.
     *
     * @param regionName
     *            the name of the region
     * @param instanceGroupName
     *            the name of the instance group
     */
    void execute(final String regionName, final String instanceGroupName)
    {
        try
        {
            final InstanceGroup instanceGroup = gceClient.getInstanceGroup(regionName, instanceGroupName);

            gceClient.deleteInstanceGroup(instanceGroup);
        }
        catch (final Exception e)
        {
            GceAdminUtils.dieWithMessage(String.format("Failed to delete instance group '%s' in region '%s", instanceGroupName, regionName),
                                         e);
        }
    }
}
