package com.xceptance.xlt.engine.har.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(
    {
        "name", "version", "comment"
    })
public class HarBrowser
{
    private final String name;

    private final String version;

    private final String comment;

    @JsonCreator
    public HarBrowser(@JsonProperty("name") String name, @JsonProperty("version") String version, @JsonProperty("comment") String comment)
    {
        this.name = name;
        this.version = version;
        this.comment = comment;
    }

    public String getName()
    {
        return name;
    }

    public String getComment()
    {
        return comment;
    }

    public String getVersion()
    {
        return version;
    }

    @Override
    public String toString()
    {
        return "HarBrowser [name = " + name + ", comment = " + comment + ", version = " + version + "]";
    }

    public static class Builder
    {
        private String name;

        private String version;

        private String comment;

        public Builder withName(String name)
        {
            this.name = name;
            return this;
        }

        public Builder withVersion(String version)
        {
            this.version = version;
            return this;
        }

        public Builder withComment(String comment)
        {
            this.comment = comment;
            return this;
        }

        public HarBrowser build()
        {
            return new HarBrowser(name, version, comment);
        }

    }
}
