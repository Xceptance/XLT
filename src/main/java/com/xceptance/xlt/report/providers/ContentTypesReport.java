package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * 
 */
@XStreamAlias("contentTypes")
public class ContentTypesReport
{
    @XStreamImplicit
    public List<ContentTypeReport> contentTypes = new ArrayList<ContentTypeReport>();
}
