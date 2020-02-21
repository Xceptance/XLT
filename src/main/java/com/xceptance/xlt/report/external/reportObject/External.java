package com.xceptance.xlt.report.external.reportObject;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * @author matthias.ullrich
 */
@XStreamAlias("external")
public class External
{
    public String headline;

    public String description;

    @XStreamImplicit
    public List<Object> reportFragments = new ArrayList<Object>();
}
