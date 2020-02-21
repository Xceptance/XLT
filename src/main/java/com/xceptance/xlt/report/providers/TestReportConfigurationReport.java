package com.xceptance.xlt.report.providers;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 */
@XStreamAlias("testReportConfig")
public class TestReportConfigurationReport
{
    public List<String> runtimePercentiles;

    public List<RuntimeInterval> runtimeIntervals;
    
    public List<RequestTableColorization> requestTableColorization;
}
