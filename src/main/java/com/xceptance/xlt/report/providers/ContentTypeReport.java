package com.xceptance.xlt.report.providers;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Represents the total number of responses with a certain content type.
 */
@XStreamAlias("contentType")
public class ContentTypeReport
{
    /**
     * The content type.
     */
    public String contentType;

    /**
     * The total number of responses with that content type.
     */
    public int count;
}
