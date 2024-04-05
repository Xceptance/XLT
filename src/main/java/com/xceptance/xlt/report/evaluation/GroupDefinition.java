package com.xceptance.xlt.report.evaluation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.EnumUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.converters.enums.EnumToStringConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

@XStreamAlias("group")
public class GroupDefinition
{
    @XStreamAsAttribute
    private final String id;

    private final String description;

    @XStreamAlias("rules")
    @XStreamConverter(RuleIdConverter.class)
    private final List<String> ruleIds;

    @XStreamAsAttribute
    private final boolean enabled;

    @XStreamAsAttribute
    @XStreamConverter(EnumToStringConverter.class)
    private final PointsSource pointsSource;

    public GroupDefinition(final String id, final String description, final List<String> ruleIds, final boolean enabled,
                           final PointsSource pointsSource)
    {
        this.id = id;
        this.description = description;
        this.ruleIds = ruleIds;
        this.enabled = enabled;
        this.pointsSource = pointsSource;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public PointsSource getPointSource()
    {
        return pointsSource;
    }

    public String getId()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }

    public List<String> getRuleIds()
    {
        return Collections.unmodifiableList(ruleIds);
    }

    public static enum PointsSource
    {
        FIRST,
        LAST,
        ALL;

        @Override
        public String toString()
        {
            return name().toLowerCase();
        }
    }

    public static GroupDefinition fromJSON(JSONObject jsonObject) throws ValidationError
    {
        final String id = jsonObject.getString("id");
        final String description = jsonObject.optString("description", null);
        final JSONArray ruleArr = jsonObject.getJSONArray("rules");
        final boolean enabled = jsonObject.optBoolean("enabled", true);
        final String pointsSourceStr = jsonObject.optString("pointsSource", null);
        final PointsSource source = pointsSourceStr != null ? EnumUtils.getEnumIgnoreCase(PointsSource.class, pointsSourceStr) : null;

        final LinkedList<String> ruleIds = new LinkedList<>();
        for (int i = 0; i < ruleArr.length(); i++)
        {
            ruleIds.add(ruleArr.getString(i));
        }

        if (ruleIds.isEmpty())
        {
            throw new ValidationError("Property 'rules' must contain at least one value");
        }
        if (ruleIds.stream().distinct().count() != ruleIds.size())
        {
            throw new ValidationError("Property 'rules' must contain distinct values");
        }

        return new GroupDefinition(id, description, ruleIds, enabled, Optional.ofNullable(source).orElse(PointsSource.FIRST));
    }
    
    public static class RuleIdConverter extends AbstractCollectionConverter
    {
        public RuleIdConverter(Mapper mapper)
        {
            super(mapper);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean canConvert(Class type)
        {
            return List.class.isAssignableFrom(type);
        }
        
        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
        {
            for(final Object o : (List<?>) source)
            {
                writer.startNode("rule");
                writer.addAttribute("ref-id", Objects.toString(o));
                writer.endNode();
            }
        }
        
        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
        {
            return null;
        }
    }
}
