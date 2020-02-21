package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * 
 */
@XStreamAlias("responseCodes")
public class ResponseCodesReport
{
    @XStreamImplicit
    public List<ResponseCodeReport> responseCodes = new ArrayList<ResponseCodeReport>();
}
