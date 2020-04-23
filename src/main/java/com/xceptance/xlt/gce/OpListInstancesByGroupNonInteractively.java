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

import java.io.File;
import java.util.List;

import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.InstanceGroup;

/**
 * Retrieves all machine instances in a certain instance group and prints their corresponding agent controller URLs to a
 * file or stdout.
 */
class OpListInstancesByGroupNonInteractively
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
    OpListInstancesByGroupNonInteractively(final GceClient gceClient)
    {
        this.gceClient = gceClient;
    }

    /**
     * Retrieves all machine instances in a certain instance group and prints their corresponding agent controller URLs
     * to a file or stdout.
     *
     * @param regionName
     *            the name of the region
     * @param instanceGroupName
     *            the name of the instance group
     * @param outputFile
     *            the file to write the agent controller properties to, or <code>null</code> if the properties are to be
     *            written to stdout
     */
    void execute(final String regionName, final String instanceGroupName, final File outputFile)
    {
        try
        {
            final InstanceGroup instanceGroup = gceClient.getInstanceGroup(regionName, instanceGroupName);
            final List<Instance> instances = gceClient.getInstancesInGroup(instanceGroup);

            GceAdminUtils.outputAgentControllerConnectionProperties(instances, outputFile);
        }
        catch (final Exception e)
        {
            GceAdminUtils.dieWithMessage(String.format("Failed to list the instances of instance group '%s' in region '%s'",
                                                       instanceGroupName, regionName),
                                         e);
        }
    }
}
