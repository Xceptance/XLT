package com.xceptance.xlt.engine.har.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The HAR root.
 */
public class HarLogRoot
{
    private final HarLog log;

    @JsonCreator
    public HarLogRoot(@JsonProperty("log") HarLog log)
    {
        this.log = log;
    }

    public HarLog getLog()
    {
        return log;
    }
}
