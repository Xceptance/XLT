package com.xceptance.xlt.util;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.xceptance.xlt.agentcontroller.AgentController;

/**
 * Threadsafe collection of failed agent controllers and corresponding Exceptions (if any).
 *
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class FailedAgentControllerCollection
{
    private final Map<AgentController, Exception> failed = new ConcurrentHashMap<AgentController, Exception>();

    /**
     * Add a failed agent controller and its corresponding exception.
     *
     * @param agentcontroller
     *            agent controller
     * @param e
     *            exception
     */
    public void add(final AgentController agentcontroller, final Exception e)
    {
        if (!failed.containsKey(agentcontroller))
        {
            failed.put(agentcontroller, e);
        }
    }

    /**
     * Returns true if this map contains no key-value mappings.
     *
     * @return <code>true</code> if this map contains no key-value mappings, <code>false</code> otherwise
     */
    public boolean isEmpty()
    {
        return failed.isEmpty();
    }

    /**
     * Returns a Set view of the contained agent controllers.
     *
     * @return Set view of the contained agent controllers
     */
    public Set<AgentController> getAgentControllers()
    {
        return failed.keySet();
    }

    /**
     * Returns a Map view of the contained agent controllers and their corresponding exceptions.
     *
     * @return Map view of the contained agent controllers and their corresponding exceptions
     */
    public Map<AgentController, Exception> getMap()
    {
        return failed;
    }
}
