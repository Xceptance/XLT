package com.xceptance.xlt.report.providers;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Represents a list of most called URLs for detailed reporting. Keeps also total data.
 */
@XStreamAlias("urls")
public class UrlData
{
    /**
     * The total count of different URLs.
     */
    public int total;

    /**
     * A list of URLs.
     */
    public List<String> list;
}
