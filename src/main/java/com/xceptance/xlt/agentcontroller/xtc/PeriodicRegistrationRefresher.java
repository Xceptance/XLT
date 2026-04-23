/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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

package com.xceptance.xlt.agentcontroller.xtc;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.lang.ThreadUtils;
import com.xceptance.xlt.agentcontroller.AgentControllerConfiguration.PrivateMachineType;

/**
 * A helper that periodically (re)registers the current machine as a private machine with XTC.
 */
public class PeriodicRegistrationRefresher
{
    private static final Logger log = LoggerFactory.getLogger(PeriodicRegistrationRefresher.class);

    private static final long REGISTRATION_INTERVAL = 60_000L;

    private final RestApiClient xtcRestApi;

    private final String hostName;

    private final PrivateMachineType machineType;

    public PeriodicRegistrationRefresher(final RestApiClient xtcRestApi, final String hostName, final PrivateMachineType privateMachineType)
    {
        this.xtcRestApi = xtcRestApi;
        this.hostName = hostName;
        machineType = privateMachineType;
    }

    public void start()
    {
        log.debug("Start periodic registration with XTC");
        Thread.ofVirtual().name(PeriodicRegistrationRefresher.class.getSimpleName()).start(this::run);
    }

    private void run()
    {
        try
        {
            // collect remaining registration data
            final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

            final String ipAddress = InetAddress.getLocalHost().getHostAddress();
            final int cores = osBean.getAvailableProcessors();
            final long memory = (osBean instanceof final com.sun.management.OperatingSystemMXBean sunBean) ? sunBean.getTotalMemorySize()
                                                                                                           : 0;
            final long disk = new File("/").getTotalSpace();

            // periodically re-register with XTC
            while (true)
            {
                try
                {
                    xtcRestApi.registerPrivateMachine(hostName, ipAddress, machineType, cores, memory, disk);
                }
                catch (final Exception e)
                {
                    log.error("Failed to (re-)register private machine with XTC: {}", e.toString());
                }

                ThreadUtils.sleep(REGISTRATION_INTERVAL);
            }
        }
        catch (final Exception e)
        {
            log.error("Failed to collect registration data", e);
        }
    }
}
