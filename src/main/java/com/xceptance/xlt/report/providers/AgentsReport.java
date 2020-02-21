package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * 
 */
@XStreamAlias("agents")
public class AgentsReport
{
    @XStreamImplicit
    public List<AgentReport> agents = new ArrayList<AgentReport>();
}
