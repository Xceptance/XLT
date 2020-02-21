package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * 
 */
@XStreamAlias("transactions")
public class TransactionsReport
{
    @XStreamImplicit
    public List<TimerReport> transactions = new ArrayList<TimerReport>();
}
