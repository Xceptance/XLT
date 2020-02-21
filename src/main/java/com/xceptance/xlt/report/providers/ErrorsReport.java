package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * 
 */
@XStreamAlias("errors")
public class ErrorsReport
{
    @XStreamImplicit
    public List<ErrorReport> errors = new ArrayList<ErrorReport>();

    public String resultsPathPrefix;

    public List<RequestErrorChartReport> requestErrorOverviewCharts = new ArrayList<RequestErrorChartReport>();

    public List<TransactionOverviewChartReport> transactionErrorOverviewCharts = new ArrayList<TransactionOverviewChartReport>();

}
