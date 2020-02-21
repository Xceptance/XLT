package com.xceptance.xlt.gce;

import java.io.File;
import java.util.List;

import com.google.api.services.compute.model.Instance;

/**
 * Creates a new managed instance group non-interactively and prints for each instance in the group the corresponding
 * agent controller URLs to a file or stdout.
 */
class OpCreateInstanceGroupNonInteractively
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
    OpCreateInstanceGroupNonInteractively(final GceClient gceClient)
    {
        this.gceClient = gceClient;
    }

    /**
     * Creates a new managed instance group non-interactively and prints for each instance in the group the
     * corresponding agent controller URLs to a file or stdout.
     *
     * @param regionName
     *            the region in which the instance group will be created
     * @param instanceGroupName
     *            the name of the instance group
     * @param instanceTemplateName
     *            the name of the instance template to use
     * @param instanceCount
     *            the number of instances in the group
     * @param outputFile
     *            the file to write the agent controller properties to, or <code>null</code> if the properties are to be
     *            written to stdout
     */
    void execute(final String regionName, final String instanceGroupName, final String instanceTemplateName, final int instanceCount,
                        final File outputFile)
    {
        try
        {
            // create the instance group
            gceClient.createInstanceGroup(regionName, instanceGroupName, instanceTemplateName, instanceCount);

            // wait for the instances in the group to exist and achieve state RUNNING
            final List<Instance> instances = gceClient.waitForInstancesAreRunning(regionName, instanceGroupName, instanceCount);

            // output agent controller connection properties
            GceAdminUtils.outputAgentControllerConnectionProperties(instances, outputFile);
        }
        catch (final Exception e)
        {
            GceAdminUtils.dieWithMessage(String.format("Failed to create instance group '%s' with %d instances in region '%s' using template '%s'",
                                                       instanceGroupName, instanceCount, regionName, instanceTemplateName),
                                         e);
        }
    }
}
