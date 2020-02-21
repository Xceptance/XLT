package com.xceptance.xlt.report.providers;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("colorization")
public class RequestTableColorization
{
    @XStreamAsAttribute
    public String groupName;

    @XStreamAlias("rules")
    public List<ColorizationRule> colorizationRules;

    @XStreamOmitField
    private String pattern;

    public RequestTableColorization(final String groupName, final String pattern, final List<ColorizationRule> colorizationRules)
    {
        this.groupName = groupName;
        this.colorizationRules = colorizationRules;
        this.pattern = pattern;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public String getPattern()
    {
        return pattern;
    }

    public List<ColorizationRule> getColorizationRules()
    {
        return colorizationRules;
    }

    @XStreamAlias("rule")
    public static class ColorizationRule
    {
        @XStreamAsAttribute
        public String id;

        @XStreamAsAttribute
        public String type;

        @XStreamAsAttribute
        public int target;

        @XStreamAsAttribute
        public int from;

        @XStreamAsAttribute
        public int to;

        public ColorizationRule(final String id, final String type)
        {
            this.id = id;
            this.type = type;
        }
    }
}
