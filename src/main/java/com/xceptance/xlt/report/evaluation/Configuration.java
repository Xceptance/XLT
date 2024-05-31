package com.xceptance.xlt.report.evaluation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class Configuration
{
    @XStreamAsAttribute
    private final int version;

    @XStreamConverter(value = MappedValuesConverter.class)
    private final Map<String, SelectorDefinition> selectors = new LinkedHashMap<>();

    @XStreamConverter(value = MappedValuesConverter.class)
    private final Map<String, RuleDefinition> rules = new LinkedHashMap<>();

    @XStreamConverter(value = MappedValuesConverter.class)
    private final Map<String, GroupDefinition> groups = new LinkedHashMap<>();

    @XStreamConverter(value = MappedValuesConverter.class)
    private final Map<String, RatingDefinition> ratings = new LinkedHashMap<>();

    public Configuration(final int version)
    {
        this.version = Math.max(1, version);
    }

    public void addSelector(final SelectorDefinition selector) throws ValidationError
    {
        final String selectorId = selector.getId();
        validateId(selectorId);

        selectors.put(selectorId, selector);
    }

    public boolean containsSelector(final String selectorId)
    {
        return selectors.containsKey(selectorId);
    }

    public SelectorDefinition getSelector(final String selectorId)
    {
        return selectors.get(selectorId);
    }

    public Collection<SelectorDefinition> getSelectors()
    {
        return Collections.unmodifiableCollection(selectors.values());
    }

    void addRule(final RuleDefinition rule) throws ValidationError
    {
        final String ruleId = rule.getId();
        validateId(ruleId);

        final List<String> unknownSelectors = Stream.of(rule.getChecks()).map(RuleDefinition.Check::getSelectorId).filter(Objects::nonNull)
                                                    .filter(Predicate.not(this::containsSelector)).collect(Collectors.toList());
        if (!unknownSelectors.isEmpty())
        {
            throw new ValidationError(String.format("Rule '%s' references unknown selector%s: %s", ruleId,
                                                    (unknownSelectors.size() > 1 ? "s" : ""), StringUtils.join(unknownSelectors)));
        }

        rules.put(ruleId, rule);
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

    void addGroup(final GroupDefinition group) throws ValidationError
    {
        final String groupId = group.getId();
        validateId(groupId);

        final List<String> unknownRules = group.getRuleIds().stream().filter(Predicate.not(this::containsRule))
                                               .collect(Collectors.toList());
        if (!unknownRules.isEmpty())
        {
            throw new ValidationError(String.format("Group '%s' references unknown rule%s: %s", groupId,
                                                    (unknownRules.size() > 1 ? "s" : ""), StringUtils.join(unknownRules)));
        }

        groups.put(groupId, group);
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

    void addRating(final RatingDefinition rating) throws ValidationError
    {
        final String ratingId = rating.getId();
        validateId(ratingId);

        ratings.put(ratingId, rating);
    }

    public boolean containsRating(final String ratingId)
    {
        return ratings.containsKey(ratingId);
    }

    public RatingDefinition getRating(final String ratingId)
    {
        return ratings.get(ratingId);
    }

    public Collection<RatingDefinition> getRatings()
    {
        return Collections.unmodifiableCollection(ratings.values());
    }

    public int getVersion()
    {
        return version;
    }

    private void validateId(final String id) throws ValidationError
    {
        final String ambigousUDErrorMsg = "Some other %s shares the same ID: '%s'. IDs must be unique.";
        if (containsSelector(id))
        {
            throw new ValidationError(String.format(ambigousUDErrorMsg, "selector", id));
        }
        if (containsRule(id))
        {
            throw new ValidationError(String.format(ambigousUDErrorMsg, "rule", id));
        }
        if (containsGroup(id))
        {
            throw new ValidationError(String.format(ambigousUDErrorMsg, "group", id));
        }
        if (containsRating(id))
        {
            throw new ValidationError(String.format(ambigousUDErrorMsg, "rating", id));
        }
    }

    static Configuration fromJSON(final JSONObject jsonObject) throws ValidationError
    {
        final Configuration config = new Configuration(jsonObject.optInt("version", -1));
        final JSONArray selectorArr = jsonObject.optJSONArray("selectors");
        if (selectorArr != null)
        {
            for (int i = 0; i < selectorArr.length(); i++)
            {
                try
                {
                    final SelectorDefinition selDef = SelectorDefinition.fromJSON(selectorArr.getJSONObject(i));

                    config.addSelector(selDef);
                }
                catch (final Exception e)
                {
                    throw new ValidationError(String.format("Selector #%d is invalid: %s", i, e.getMessage()));
                }
            }
        }

        final JSONArray ruleArr = jsonObject.optJSONArray("rules");
        if (ruleArr != null)
        {
            for (int i = 0; i < ruleArr.length(); i++)
            {
                try
                {
                    final RuleDefinition ruleDef = RuleDefinition.fromJSON(ruleArr.getJSONObject(i));

                    config.addRule(ruleDef);
                }
                catch (final Exception e)
                {
                    throw new ValidationError(String.format("Rule #%d is invalid: %s", i, e.getMessage()));
                }
            }
        }

        final JSONArray groupArr = jsonObject.getJSONArray("groups");
        for (int i = 0; i < groupArr.length(); i++)
        {
            try
            {
                final GroupDefinition groupDef = GroupDefinition.fromJSON(groupArr.getJSONObject(i));

                config.addGroup(groupDef);
            }
            catch (final Exception e)
            {
                throw new ValidationError(String.format("Group #%d is invalid: %s", i, e.getMessage()));
            }
        }

        final JSONArray ratingArr = jsonObject.optJSONArray("ratings");
        if (ratingArr != null)
        {
            for (int i = 0; i < ratingArr.length(); i++)
            {
                try
                {
                    final RatingDefinition ratingDef = RatingDefinition.fromJSON(ratingArr.getJSONObject(i));

                    config.addRating(ratingDef);
                }
                catch (final Exception e)
                {
                    throw new ValidationError(String.format("Rating #%d is invalid: %s", i, e.getMessage()));
                }
            }
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
        public boolean canConvert(final Class type)
        {
            if (Map.class.isAssignableFrom(type))
            {
                return true;
            }

            return super.canConvert(type);
        }

        @Override
        public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context)
        {
            final Map<?, ?> m = (Map<?, ?>) source;
            for (final Object o : m.values())
            {
                writeCompleteItem(o, context, writer);
            }
        }

        @Override
        public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context)
        {
            return null;
        }
    }
}
