
package com.xceptance.xlt.agentcontroller.xtc;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.lang.ThreadUtils;

/**
 * A helper that periodically (re)registers the current machine as a private agent machine with XTC.
 */
public class PeriodicRegistrationRefresher
{
    private static final Logger log = LoggerFactory.getLogger(PeriodicRegistrationRefresher.class);

    private RestApiClient xtcRestApi;

    private String agentId;

    private String agentName;

    private String hostName;

    private String type;

    private long interval;

    public PeriodicRegistrationRefresher(RestApiClient xtcRestApi, String agentId, String agentName, String hostName, String type)
    {
        this.xtcRestApi = xtcRestApi;
        this.agentId = agentId;
        this.agentName = agentName;
        this.hostName = hostName;
        this.type = type;

        // TODO
        interval = 60_000L;
    }

    public void start()
    {
        log.info("Starting periodic registration refresher");
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
                    // TODO: description needed? if so, make it configurable.
                    String description = "";

                    xtcRestApi.registerPrivateAgent(agentId, agentName, description, hostName, ipAddress, type, cores, memory, disk);
                }
                catch (final Exception e)
                {
                    log.error("Failed to (re-)register private agent with XTC", e);
                }

                ThreadUtils.sleep(interval);
            }
        }
        catch (Exception e)
        {
            log.error("Failed to collect registration data", e);
        }
    }
}
