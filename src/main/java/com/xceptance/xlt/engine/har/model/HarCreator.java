package com.xceptance.xlt.engine.har.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Information about the creator of HAR.
 *
 * @see <a href="http://www.softwareishard.com/blog/har-12-spec/#creator">specification</a>
 */
@JsonPropertyOrder(
    {
        "name", "version", "comment"
    })
public class HarCreator
{
    private final String name;

    private final String version;

    private final String comment;

    @JsonCreator
    public HarCreator(@JsonProperty("name") String name, @JsonProperty("comment") String comment, @JsonProperty("version") String version)
    {
        this.name = name;
        this.comment = comment;
        this.version = version;
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
        return "HarCreator [name = " + name + ", comment = " + comment + ", version = " + version + "]";
    }

    public static class Builder
    {
        private String name;

        private String comment;

        private String version;

        public Builder withName(String name)
        {
            this.name = name;
            return this;
        }

        public Builder withComment(String comment)
        {
            this.comment = comment;
            return this;
        }

        public Builder withVersion(String version)
        {
            this.version = version;
            return this;
        }

        public HarCreator build()
        {
            return new HarCreator(name, comment, version);
        }

    }
}
