package com.xceptance.xlt.report.evaluation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

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

    private String description;

    @XStreamAlias("rules")
    @XStreamConverter(RuleIdConverter.class)
    private final List<String> ruleIds;

    @XStreamAsAttribute
    private boolean enabled;

    @XStreamAsAttribute
    private boolean failsTest;

    @XStreamAsAttribute
    private TestFailTrigger failsOn;

    @XStreamAsAttribute
    @XStreamConverter(EnumToStringConverter.class)
    private Mode mode;

    private String successMessage;

    private String failMessage;

    public GroupDefinition(final String id, final String name, final List<String> ruleIds)
    {
        this.id = Objects.requireNonNull(id, "Group ID must not be null");
        this.name = name;
        this.ruleIds = ruleIds;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public Mode getMode()
    {
        return mode;
    }

    public void setMode(Mode mode)
    {
        this.mode = mode;
    }

    public String getId()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public List<String> getRuleIds()
    {
        return Collections.unmodifiableList(ruleIds);
    }

    public String getSuccessMessage()
    {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage)
    {
        this.successMessage = successMessage;
    }

    public String getFailMessage()
    {
        return failMessage;
    }

    public void setFailMessage(String failMessage)
    {
        this.failMessage = failMessage;
    }

    public boolean isFailsTest()
    {
        return failsTest;
    }

    public void setFailsTest(boolean failsTest)
    {
        this.failsTest = failsTest;
    }

    public TestFailTrigger getFailsOn()
    {
        return failsOn;
    }

    public void setFailsOn(TestFailTrigger failsOn)
    {
        this.failsOn = failsOn;
    }

    public static enum Mode
    {
        firstPassed,
        lastPassed,
        allPassed;

    }

    public static GroupDefinition fromJSON(final JSONObject jsonObject) throws ValidationException
    {
        final String id = jsonObject.getString("id");
        final String name = StringUtils.trimToNull(jsonObject.optString("name"));
        final String description = jsonObject.optString("description", null);
        final JSONArray ruleArr = jsonObject.getJSONArray("rules");
        final boolean enabled = jsonObject.optBoolean("enabled", true);
        final boolean failsTest = jsonObject.optBoolean("failsTest", false);
        final String modeStr = StringUtils.trimToNull(jsonObject.optString("mode"));
        final String failsOnStr = StringUtils.trimToNull(jsonObject.optString("failsOn"));

        final Mode source = EnumUtils.getEnumIgnoreCase(Mode.class, modeStr, Mode.firstPassed);
        final TestFailTrigger failsOn = EnumUtils.getEnumIgnoreCase(TestFailTrigger.class, failsOnStr);

        final JSONObject messageObj = jsonObject.optJSONObject("messages");
        final String successMessage = messageObj != null ? StringUtils.trimToNull(messageObj.optString("success")) : null;
        final String failMessage = messageObj != null ? StringUtils.trimToNull(messageObj.optString("fail")) : null;

        final LinkedList<String> ruleIds = new LinkedList<>();
        for (int i = 0; i < ruleArr.length(); i++)
        {
            ruleIds.add(ruleArr.getString(i));
        }

        if (ruleIds.isEmpty())
        {
            throw new ValidationException("Property 'rules' must contain at least one value");
        }
        if (ruleIds.stream().distinct().count() != ruleIds.size())
        {
            throw new ValidationException("Property 'rules' must contain distinct values");
        }

        final GroupDefinition group = new GroupDefinition(id, name, ruleIds);
        group.setEnabled(enabled);
        group.setDescription(description);
        group.setFailsTest(failsTest);
        group.setFailsOn(failsOn);
        group.setMode(source);
        group.setFailMessage(failMessage);
        group.setSuccessMessage(successMessage);

        return group;
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
