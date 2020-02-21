package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Represents the Hosts section in the test report XML.
 */
@XStreamAlias("hosts")
public class HostsReport
{
    @XStreamImplicit
    public List<HostReport> hosts = new ArrayList<HostReport>();
}
