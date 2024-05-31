package com.xceptance.xlt.report.evaluation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
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

    @XStreamAsAttribute
    private final String name;
    
    private final String description;

    @XStreamAlias("rules")
    @XStreamConverter(RuleIdConverter.class)
    private final List<String> ruleIds;

    @XStreamAsAttribute
    private final boolean enabled;

    @XStreamAsAttribute
    private final boolean failsTest;

    @XStreamAsAttribute
    @XStreamConverter(EnumToStringConverter.class)
    private final Mode mode;

    private final String successMessage;

    private final String failMessage;


    public GroupDefinition(final String id, final String name, final String description, final List<String> ruleIds, final boolean enabled,
                           final boolean failsTest, final Mode mode, final String successMessage, final String failMessage)
    {
        this.id = Objects.requireNonNull(id, "Group ID must not be null");
        this.name = name;
        this.description = description;
        this.ruleIds = ruleIds;
        this.enabled = enabled;
        this.failsTest = failsTest;
        this.mode = Objects.requireNonNull(mode, "Group mode must not be null");
        this.successMessage = successMessage;
        this.failMessage = failMessage;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public Mode getMode()
    {
        return mode;
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

    public String getSuccessMessage()
    {
        return successMessage;
    }

    public String getFailMessage()
    {
        return failMessage;
    }

    public boolean isFailsTest()
    {
        return failsTest;
    }
    
    public static enum Mode
    {
        firstPassed,
        lastPassed,
        allPassed;

    }

    public static GroupDefinition fromJSON(final JSONObject jsonObject) throws ValidationError
    {
        final String id = jsonObject.getString("id");
        final String name = StringUtils.trimToNull(jsonObject.optString("name"));
        final String description = jsonObject.optString("description", null);
        final JSONArray ruleArr = jsonObject.getJSONArray("rules");
        final boolean enabled = jsonObject.optBoolean("enabled", true);
        final boolean failsTest = jsonObject.optBoolean("failsTest", false);
        final String modeStr = StringUtils.trimToNull(jsonObject.optString("mode"));
        final Mode source = EnumUtils.getEnumIgnoreCase(Mode.class, modeStr);

        final JSONObject messageObj = jsonObject.optJSONObject("message");
        final String successMessage = messageObj != null ? StringUtils.trimToNull(messageObj.optString("success")) : null;
        final String failMessage = messageObj != null ? StringUtils.trimToNull(messageObj.optString("fail")) : null;

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

        return new GroupDefinition(id, name, description, ruleIds, enabled, failsTest, Optional.ofNullable(source).orElse(Mode.firstPassed),
                                   successMessage, failMessage);
    }

    public static class RuleIdConverter extends AbstractCollectionConverter
    {
        public RuleIdConverter(final Mapper mapper)
        {
            super(mapper);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean canConvert(final Class type)
        {
            return List.class.isAssignableFrom(type);
        }

        @Override
        public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context)
        {
            for (final Object o : (List<?>) source)
            {
                writer.startNode("rule");
                writer.addAttribute("ref-id", Objects.toString(o));
                writer.endNode();
            }
        }

        @Override
        public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context)
        {
            return null;
        }
    }
}
