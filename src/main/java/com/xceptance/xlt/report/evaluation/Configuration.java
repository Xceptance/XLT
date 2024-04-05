package com.xceptance.xlt.report.evaluation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class Configuration
{
    @XStreamConverter(value = MappedValuesConverter.class)
    private final Map<String, RuleDefinition> rules = new LinkedHashMap<>();

    @XStreamConverter(value = MappedValuesConverter.class)
    private final Map<String, GroupDefinition> groups = new LinkedHashMap<>();

    @XStreamConverter(value = MappedValuesConverter.class)
    private final Map<String, RatingDefinition> ratings = new LinkedHashMap<>();

    public void addRule(final RuleDefinition rule)
    {
        rules.put(rule.getId(), rule);
    }

    public boolean containsRule(final String ruleId)
    {
        return rules.containsKey(ruleId);
    }

    public Collection<RuleDefinition> getRules()
    {
        return Collections.unmodifiableCollection(rules.values());
    }

    public RuleDefinition getRule(final String ruleId)
    {
        return rules.get(ruleId);
    }

    public void addGroup(final GroupDefinition group)
    {
        groups.put(group.getId(), group);
    }

    public boolean containsGroup(final String groupId)
    {
        return groups.containsKey(groupId);
    }

    public GroupDefinition getGroup(final String groupId)
    {
        return groups.get(groupId);
    }

    public Collection<GroupDefinition> getGroups()
    {
        return Collections.unmodifiableCollection(groups.values());
    }

    public void addRating(final RatingDefinition rating)
    {
        ratings.put(rating.getName(), rating);
    }

    public boolean containsRating(final String ratingName)
    {
        return ratings.containsKey(ratingName);
    }

    public RatingDefinition getRating(final String ratingName)
    {
        return ratings.get(ratingName);
    }

    public Collection<RatingDefinition> getRatings()
    {
        return Collections.unmodifiableCollection(ratings.values());
    }

    static Configuration fromJSON(final JSONObject jsonObject) throws ValidationError
    {
        final Configuration config = new Configuration();
        final String ambigousUDErrorMsg = "Encountered %s sharing the same ID: '%s'. IDs must be unique.";

        final JSONArray ruleArr = jsonObject.getJSONArray("rules");
        for (int i = 0; i < ruleArr.length(); i++)
        {
            final RuleDefinition ruleDef = RuleDefinition.fromJSON(ruleArr.getJSONObject(i));
            if (config.containsRule(ruleDef.getId()))
            {
                throw new ValidationError(String.format(ambigousUDErrorMsg, "multiple rules", ruleDef.getId()));
            }

            config.addRule(ruleDef);
        }

        final JSONArray groupArr = jsonObject.getJSONArray("groups");
        for (int i = 0; i < groupArr.length(); i++)
        {
            final GroupDefinition groupDef = GroupDefinition.fromJSON(groupArr.getJSONObject(i));
            final String groupId = groupDef.getId();
            if (config.containsGroup(groupId))
            {
                throw new ValidationError(String.format(ambigousUDErrorMsg, "multiple groups", groupId));
            }

            if (config.containsRule(groupId))
            {
                throw new ValidationError(String.format(ambigousUDErrorMsg, "group and rule", groupId));
            }

            final List<String> unknownRules = groupDef.getRuleIds().stream().filter(Predicate.not(config::containsRule))
                                                      .collect(Collectors.toList());
            if (!unknownRules.isEmpty())
            {
                throw new ValidationError(String.format("Group '%s' references unknown rule%s: %s", groupId,
                                                        (unknownRules.size() > 1 ? "s" : ""), StringUtils.join(unknownRules)));
            }

            config.addGroup(groupDef);
        }

        final JSONArray ratingArr = jsonObject.getJSONArray("ratings");
        for (int i = 0; i < ratingArr.length(); i++)
        {
            final RatingDefinition ratingDef = RatingDefinition.fromJSON(ratingArr.getJSONObject(i));
            if (config.containsRating(ratingDef.getName()))
            {
                throw new ValidationError(String.format("Encountered multipe ratings sharing the same name: '%s'", ratingDef.getName()));
            }

            config.addRating(ratingDef);
        }

        if (!config.rules.values().stream().anyMatch(RuleDefinition::isEnabled))
        {
            throw new ValidationError("Configuration must contain at least one enabled rule");
        }

        if (!config.groups.values().stream().anyMatch((groupDef) -> groupDef.isEnabled() && !groupDef.getRuleIds().isEmpty()))
        {
            throw new ValidationError("Configuration must contain at least one enabled and non-empty group");
        }

        return config;
    }

    public static class MappedValuesConverter extends MapConverter
    {

        public MappedValuesConverter(final Mapper mapper, final Class<?> type)
        {
            super(mapper, type);
        }

        public MappedValuesConverter(final Mapper mapper)
        {
            super(mapper);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean canConvert(Class type)
        {
            if (Map.class.isAssignableFrom(type))
            {
                return true;
            }

            return super.canConvert(type);
        }

        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
        {
            final Map<?, ?> m = (Map<?, ?>) source;
            for (final Object o : m.values())
            {
                writeCompleteItem(o, context, writer);
            }

        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
        {
            return null;
        }
    }
}
