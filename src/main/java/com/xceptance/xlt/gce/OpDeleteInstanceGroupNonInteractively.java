/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
