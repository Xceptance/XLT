package com.xceptance.xlt.report.evaluation;

import java.util.LinkedList;
import java.util.Objects;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

@XStreamAlias("rule")
public class RuleDefinition
{
    @XStreamAsAttribute
    private final String id;

    @XStreamAsAttribute
    private final String name;

    @XStreamAsAttribute
    private boolean enabled = true;

    private String description;

    @XStreamAsAttribute
    private boolean failsTest;

    @XStreamAsAttribute
    private TestFailTrigger failsOn;

    @XStreamAsAttribute
    private boolean negateResult;

    private final Check[] checks;

    @XStreamAsAttribute
    private int points;

    private String successMessage;

    private String failMessage;

    public RuleDefinition(final String id, final String name, final Check[] checks)
    {
        this.id = Objects.requireNonNull(id, "Rule ID must not be null");
        this.name = name;
        this.checks = Objects.requireNonNull(checks, "Rule checks must not be null");
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(final boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public boolean isFailsTest()
    {
        return failsTest;
    }

    public void setFailsTest(final boolean failsTest)
    {
        this.failsTest = failsTest;
    }

    public TestFailTrigger getFailsOn()
    {
        return failsOn;
    }

    public void setFailsOn(final TestFailTrigger failsOn)
    {
        this.failsOn = failsOn;
    }

    public boolean isNegateResult()
    {
        return negateResult;
    }

    public void setNegateResult(boolean negateResult)
    {
        this.negateResult = negateResult;
    }

    public int getPoints()
    {
        return points;
    }

    public void setPoints(final int points)
    {
        this.points = points;
    }

    public String getSuccessMessage()
    {
        return successMessage;
    }

    public void setSuccessMessage(final String successMessage)
    {
        this.successMessage = successMessage;
    }

    public String getFailMessage()
    {
        return failMessage;
    }

    public void setFailMessage(final String failMessage)
    {
        this.failMessage = failMessage;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public Check[] getChecks()
    {
        return checks;
    }

    @XStreamAlias("check")
    public static class Check
    {
        @XStreamAsAttribute
        private final int index;

        private final String selector;

        @XStreamConverter(SelectorIdConverter.class)
        @XStreamAlias("selector")
        private final String selectorId;

        private final String condition;

        @XStreamAsAttribute
        private final boolean enabled;

        @XStreamAsAttribute
        private final boolean displayValue;

        public Check(final int index, final String selector, final String selectorId, final String condition, final boolean enabled,
                     final boolean displayValue)
        {
            this.index = Math.max(0, index);
            this.selectorId = selectorId;
            this.selector = selector;
            this.condition = Objects.requireNonNull(condition, "Rule check condition must not be null");
            this.enabled = enabled;
            this.displayValue = displayValue;
        }

        public int getIndex()
        {
            return index;
        }

        public boolean isEnabled()
        {
            return enabled;
        }

        public boolean isDisplayValue()
        {
            return displayValue;
        }

        public String getSelector()
        {
            return selector;
        }

        public String getSelectorId()
        {
            return selectorId;
        }

        public String getCondition()
        {
            return condition;
        }

    }

    static RuleDefinition fromJSON(final JSONObject jsonObject) throws ValidationException
    {
        final String ruleId = jsonObject.getString("id");
        final String ruleName = StringUtils.trimToNull(jsonObject.optString("name"));
        final String ruleDesc = jsonObject.optString("description", null);
        final boolean enabled = jsonObject.optBoolean("enabled", true);
        final boolean failsTest = jsonObject.optBoolean("failsTest", false);
        final boolean negateResult = jsonObject.optBoolean("negateResult", false);
        final JSONObject messages = jsonObject.optJSONObject("messages");
        final JSONArray checks = jsonObject.optJSONArray("checks");
        final int rulePoints = jsonObject.optInt("points");

        final String failsOnStr = StringUtils.trimToNull(jsonObject.optString("failsOn"));
        final TestFailTrigger failsOn = EnumUtils.getEnumIgnoreCase(TestFailTrigger.class, failsOnStr);

        final LinkedList<Check> checkList = new LinkedList<>();
        if (checks != null)
        {
            for (int i = 0; i < checks.length(); i++)
            {
                final JSONObject checkObj = checks.getJSONObject(i);
                final String selector = checkObj.optString("selector", null);
                final String selectorId = checkObj.optString("selectorId", null);
                final String condition = checkObj.getString("condition");

                final boolean displayValue = checkObj.optBoolean("displayValue", true);
                final boolean checkEnabled = checkObj.optBoolean("enabled", true);

                if (!(selector == null ^ selectorId == null))
                {
                    throw new ValidationException("Check #" + i +
                                                  " is ambiguous: either 'selector' or 'selectorId' property must be given");
                }

                checkList.add(new Check(i, selector, selectorId, condition, checkEnabled, displayValue));
            }
        }
        if (rulePoints < 0)
        {
            throw new ValidationException("Property 'points' must be a non-negative integer");
        }
        if (!checkList.isEmpty() && !checkList.stream().anyMatch(Check::isEnabled))
        {
            throw new ValidationException("Property 'checks' must contain at least one enabled check definition");
        }

        final RuleDefinition ruleDef = new RuleDefinition(ruleId, ruleName, checkList.toArray(Check[]::new));
        ruleDef.setDescription(ruleDesc);
        ruleDef.setEnabled(enabled);
        ruleDef.setFailsTest(failsTest);
        ruleDef.setFailsOn(failsOn);
        ruleDef.setNegateResult(negateResult);
        ruleDef.setPoints(rulePoints);
        ruleDef.setSuccessMessage(messages != null ? StringUtils.trimToNull(messages.optString("success")) : null);
        ruleDef.setFailMessage(messages != null ? StringUtils.trimToNull(messages.optString("fail")) : null);

        return ruleDef;
    }

    public static class SelectorIdConverter implements Converter
    {
        @SuppressWarnings("rawtypes")
        @Override
        public boolean canConvert(Class type)
        {
            return type == String.class;
        }

        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
        {
            writer.addAttribute("ref-id", Objects.toString(source));
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
        {
            return null;
        }

    }
}
