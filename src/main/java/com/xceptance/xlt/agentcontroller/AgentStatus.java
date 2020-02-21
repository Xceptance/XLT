package com.xceptance.xlt.agentcontroller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AgentStatus implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final List<TestUserStatus> allStats = new ArrayList<TestUserStatus>();

    private String agentID;

    private String hostName;

    private int errorExitCode;

    /**
     * @return the agentName
     */

    public String getAgentID()
    {
        return agentID;
    }

    /**
     * @param agentName
     *            the agentName to set
     */

    public void setAgentID(final String agentID)
    {
        this.agentID = agentID;
    }

    /**
     * @return the hostName
     */

    public String getHostName()
    {
        return hostName;
    }

    /**
     * @param hostName
     *            the hostName to set
     */

    public void setHostName(final String hostName)
    {
        this.hostName = hostName;
    }

    public void addTestUserStatus(final TestUserStatus stats)
    {
        allStats.add(stats);
    }

    public List<TestUserStatus> getTestUserStatusList()
    {
        return allStats;
    }

    public void setErrorExitCode(final int exitCode)
    {
        errorExitCode = exitCode;
    }

    public int getErrorExitCode()
    {
        return errorExitCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();

        buf.append(agentID).append('@').append(hostName).append('\n');

        for (final TestUserStatus stats : allStats)
        {
            buf.append("- ").append(stats).append('\n');
        }

        return buf.toString();
    }

}
