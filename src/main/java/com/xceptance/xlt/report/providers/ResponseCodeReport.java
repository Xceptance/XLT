package com.xceptance.xlt.report.providers;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Represents the total number of requests that ended with a certain HTTP response code.
 */
@XStreamAlias("responseCode")
public class ResponseCodeReport
{
    /**
     * The HTTP response code.
     */
    public int code;

    /**
     * The textual representation of an HTTP status code.
     */
    public String statusText;

    /**
     * The total number of requests with that response code.
     */
    public int count;
}
