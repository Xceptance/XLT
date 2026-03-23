
package com.xceptance.xlt.agentcontroller.xtc;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.lang.ThreadUtils;
import com.xceptance.xlt.agentcontroller.AgentControllerConfiguration.PrivateAgentType;

/**
 * A helper that periodically (re)registers the current machine as a private agent machine with XTC.
 */
public class PeriodicRegistrationRefresher
{
    private static final Logger log = LoggerFactory.getLogger(PeriodicRegistrationRefresher.class);

    private static final long REGISTRATION_INTERVAL = 60_000L;

    private RestApiClient xtcRestApi;

    private String agentId;

    private String agentName;

    private String agentDescription;

    private PrivateAgentType agentType;

    private String hostName;

    public PeriodicRegistrationRefresher(RestApiClient xtcRestApi, String agentId, String agentName, String agentDescription,
                                         PrivateAgentType privateAgentType, String hostName)
    {
        this.xtcRestApi = xtcRestApi;
        this.agentId = agentId;
        this.agentName = agentName;
        this.agentDescription = agentDescription;
        this.agentType = privateAgentType;
        this.hostName = hostName;
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
                    xtcRestApi.registerPrivateAgent(agentId, agentName, agentDescription, hostName, ipAddress, agentType, cores, memory,
                                                    disk);
                }
                catch (final Exception e)
                {
                    log.error("Failed to (re-)register private agent with XTC", e);
                }

                ThreadUtils.sleep(REGISTRATION_INTERVAL);
            }
        }
        catch (Exception e)
        {
            log.error("Failed to collect registration data", e);
        }
    }
}
