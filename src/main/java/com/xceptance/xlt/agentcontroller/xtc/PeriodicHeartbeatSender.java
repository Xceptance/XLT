
package com.xceptance.xlt.agentcontroller.xtc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.lang.ThreadUtils;

public class PeriodicHeartbeatSender
{
    private static final Logger log = LoggerFactory.getLogger(PeriodicHeartbeatSender.class);

    private RestApiClient restApiClient;

    private long interval;

    private String id;

    public PeriodicHeartbeatSender(final RestApiClient restApiClient, final long interval, String id)
    {
        this.restApiClient = restApiClient;
        this.interval = interval;
        this.id = id;
    }

    public void start()
    {
        log.info("Starting periodic heartbeat sender");
        Thread.ofVirtual().name(PeriodicHeartbeatSender.class.getSimpleName()).start(this::run);
    }

    private void run()
    {
        while (true)
        {
            try
            {
                restApiClient.sendHeartbeat(id);

                log.debug("Heartbeat sent successfully");
            }
            catch (final Exception e)
            {
                log.error("Failed sending heartbeat", e);
            }

            ThreadUtils.sleep(interval);
        }
    }
}
