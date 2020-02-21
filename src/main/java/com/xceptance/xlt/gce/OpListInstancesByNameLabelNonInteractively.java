package com.xceptance.xlt.gce;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.Region;

/**
 * Retrieves all machine instances with a certain name label and prints their corresponding agent controller URLs to a
 * file or stdout.
 */
class OpListInstancesByNameLabelNonInteractively
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
    OpListInstancesByNameLabelNonInteractively(final GceClient gceClient)
    {
        this.gceClient = gceClient;
    }

    /**
     * Retrieves all machine instances with a certain name label and prints their corresponding agent controller URLs to
     * a file or stdout.
     *
     * @param regionName
     *            the name of the region
     * @param nameLabel
     *            the value of the name label
     * @param outputFile
     *            the file to write the agent controller properties to, or <code>null</code> if the properties are to be
     *            written to stdout
     */
    void execute(final String regionName, final String nameLabel, final File outputFile) throws IOException
    {
        try
        {
            final Region region = gceClient.getRegion(regionName);
            List<Instance> instances = gceClient.getInstancesInRegion(region);

            // TODO: check for state RUNNING?
            instances = filterInstancesByNameLabel(instances, nameLabel);

            GceAdminUtils.outputAgentControllerConnectionProperties(instances, outputFile);
        }
        catch (final Exception e)
        {
            GceAdminUtils.dieWithMessage(String.format("Failed to list the instances with name label '%s' in region '%s'", nameLabel,
                                                       regionName),
                                         e);
        }
    }

    /**
     * Filters instances by the given name label.
     *
     * @param instances
     *            the list of instances
     * @param nameLabel
     *            the name label
     * @return the instances having such a name label
     */
    private List<Instance> filterInstancesByNameLabel(final List<Instance> instances, final String nameLabel)
    {
        final List<Instance> filteredInstances = new ArrayList<>();

        for (final Instance instance : instances)
        {
            final Map<String, String> instanceLabels = instance.getLabels();

            if (instanceLabels != null)
            {
                final String instanceNameLabel = instanceLabels.get("name");

                if (nameLabel.equals(instanceNameLabel))
                {
                    filteredInstances.add(instance);
                }
            }
        }

        return filteredInstances;
    }
}
