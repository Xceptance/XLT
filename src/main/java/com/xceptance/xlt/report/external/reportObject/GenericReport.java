package com.xceptance.xlt.report.external.reportObject;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author matthias.ullrich
 */
@XStreamAlias("genericReport")
public class GenericReport
{
    @XStreamAlias("headline")
    public String headline;

    @XStreamAlias("description")
    public String description;

    @XStreamAlias("tables")
    public final List<Table> tables = new ArrayList<Table>();

    @XStreamAlias("chartFileNames")
    public final List<String> chartFileNames = new ArrayList<String>();
}
