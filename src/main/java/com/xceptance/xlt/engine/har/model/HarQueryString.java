package com.xceptance.xlt.engine.har.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Parameter parsed from URL query string, if any (embedded in request object).
 * 
 * @see <a href="http://www.softwareishard.com/blog/har-12-spec/#queryString">specification</a>
 */
@JsonPropertyOrder(
    {
        "name", "value", "comment"
    })
public class HarQueryString
{
    private final String name;

    private final String value;

    private final String comment;

    @JsonCreator
    public HarQueryString(@JsonProperty("name") String name, @JsonProperty("value") String value, @JsonProperty("comment") String comment)
    {
        this.name = name;
        this.value = value;
        this.comment = comment;
    }

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }

    public String getComment()
    {
        return comment;
    }

    @Override
    public String toString()
    {
        return "HarQueryString [name = " + name + ", value = " + value + ", comment = " + comment + "]";
    }

    public static class Builder
    {
        private String name;

        private String value;

        private String comment;

        public Builder withName(String name)
        {
            this.name = name;
            return this;
        }

        public Builder withValue(String value)
        {
            this.value = value;
            return this;
        }

        public Builder withComment(String comment)
        {
            this.comment = comment;
            return this;
        }

        public HarQueryString build()
        {
            return new HarQueryString(name, value, comment);
        }
    }
}
