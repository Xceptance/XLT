package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Represents the statistics for a certain exception in a test report.
 */
@XStreamAlias("error")
public class ErrorReport
{
    /**
     * The number how often a certain exception has occurred.
     */
    public int count;

    /**
     * The name of the test case.
     */
    public String testCaseName;

    /**
     * The name of the action that caused the test case to fail.
     */
    public String actionName;

    /**
     * The exception's message attribute.
     */
    public String message;

    /**
     * The exception's stack trace.
     */
    public String trace;

    /**
     * The unique chartID for the error details
     */
    public int detailChartID;

    /**
     * The list of directory hints (for example: "ac1/TAuthor/1/1216803080255") where to find additional information to
     * locate the error.
     */
    public List<String> directoryHints = new ArrayList<String>();
}
