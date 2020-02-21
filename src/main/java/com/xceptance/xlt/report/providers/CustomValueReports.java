package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
@XStreamAlias("customValues")
public class CustomValueReports
{
    @XStreamImplicit
    public List<CustomValueReport> customValueReports = new ArrayList<CustomValueReport>();
}
