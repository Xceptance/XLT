package com.xceptance.xlt.engine.har.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Request/response header.
 *
 * @see <a href="http://www.softwareishard.com/blog/har-12-spec/#headers">specification</a>
 */
@JsonPropertyOrder(
    {
        "name", "value", "comment"
    })
public class HarHeader
{
    private final String name;

    private final String value;

    private final String comment;

    @JsonCreator
    public HarHeader(@JsonProperty("name") String name, @JsonProperty("value") String value, @JsonProperty("comment") String comment)
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
        return "HarHeader [name = " + name + ", value = " + value + ", comment = " + comment + "]";
    }

    public static class Builder
    {
        private String name;

        private List<String> values = new ArrayList<>();

        private String comment;

        public Builder withName(String name)
        {
            this.name = name;
            return this;
        }

        public Builder withValue(String value)
        {
            this.values.add(value);
            return this;
        }

        public Builder withValues(List<String> values)
        {
            if (values != null)
            {
                this.values.addAll(values);
            }
            return this;
        }

        public Builder withComment(String comment)
        {
            this.comment = comment;
            return this;
        }

        public HarHeader build()
        {
            return new HarHeader(name, StringUtils.join(values, ","), comment);
        }
    }
}
