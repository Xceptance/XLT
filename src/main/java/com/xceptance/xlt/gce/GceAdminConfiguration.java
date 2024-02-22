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

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.util.AbstractConfiguration;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.engine.XltExecutionContext;

/**
 * Central place for gce_admin configuration values.
 */
class GceAdminConfiguration extends AbstractConfiguration
{
    /**
     * GCE admin property prefix.
     */
    private static final String PROP_PREFIX = "xlt.gce.";

    /**
     * GCE property name for the application name.
     */
    private static final String PROP_APPLICATION_NAME = PROP_PREFIX + "applicationName";

    /**
     * GCE property name for the instance connect timeout.
     */
    private static final String PROP_INSTANCE_CONNECT_TIMEOUT = PROP_PREFIX + "instanceConnectTimeout";

    /**
     * GCE property name for the project ID.
     */
    private static final String PROP_PROJECT_ID = PROP_PREFIX + "projectId";

    /**
     * The timeout to wait for an instance to become available.
     */
    private final long instanceConnectTimeout;

    /**
     * The ID of the Google Cloud Platform project to use.
     */
    private final String projectId;

    /**
     * The application name.
     */
    private String applicationName;

    /**
     * Creates a {@link GceAdminConfiguration} object and initializes it with the configuration from the properties
     * file.
     * 
     * @throws IOException
     *             if an error occurs while loading the configuration
     */
    GceAdminConfiguration() throws IOException
    {
        final File configDirectory = XltExecutionContext.getCurrent().getXltConfigDir();
        final File propFile = new File(configDirectory, "gce_admin.properties");

        loadProperties(propFile);

        instanceConnectTimeout = getLongProperty(PROP_INSTANCE_CONNECT_TIMEOUT, 300) * 1000;
        if (instanceConnectTimeout < 0)
        {
            throw new XltException(String.format("Property '%s' cannot be negative", PROP_INSTANCE_CONNECT_TIMEOUT));
        }

        projectId = getStringProperty(PROP_PROJECT_ID, "").trim();
        if (StringUtils.isEmpty(projectId))
        {
            throw new XltException(String.format("Property '%s' is not set", PROP_PROJECT_ID));
        }

        applicationName = getStringProperty(PROP_APPLICATION_NAME, "").trim();
        if (StringUtils.isEmpty(applicationName))
        {
            applicationName = "xlt-gce-admin";
        }
    }

    /**
     * Returns the application name.
     *
     * @return the application name
     */
    String getApplicationName()
    {
        return applicationName;
    }

    /**
     * Returns the instance connect timeout in milliseconds.
     *
     * @return the instance connect timeout
     */
    long getInstanceConnectTimeout()
    {
        return instanceConnectTimeout;
    }

    /**
     * Returns the project ID.
     *
     * @return the project ID
     */
    String getProjectId()
    {
        return projectId;
    }
}
