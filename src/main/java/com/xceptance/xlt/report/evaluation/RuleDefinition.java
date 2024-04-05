package com.xceptance.xlt.report.evaluation;

import java.util.LinkedList;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("rule")
public class RuleDefinition
{
    @XStreamAsAttribute
    private final String id;

    @XStreamAsAttribute
    private boolean enabled = true;

    private String description;

    @XStreamAsAttribute
    private boolean failsTest;

    private final Check[] checks;

    @XStreamAsAttribute
    private int points;

    private String successMessage;

    private String failMessage;

    public RuleDefinition(final String id, final Check[] checks)
    {
        this.id = Objects.requireNonNull(id, "Rule ID must not be null");
        this.checks = Objects.requireNonNull(checks, "Rule checks must not be null");
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public boolean isFailsTest()
    {
        return failsTest;
    }

    public void setFailsTest(boolean failsTest)
    {
        this.failsTest = failsTest;
    }

    public int getPoints()
    {
        return points;
    }

    public void setPoints(int points)
    {
        this.points = points;
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

    public String getId()
    {
        return id;
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

        private final String condition;

        @XStreamAsAttribute
        private final boolean enabled;

        @XStreamAsAttribute
        private final boolean displayValue;

        public Check(final int index, final String selector, final String condition, final boolean enabled, final boolean displayValue)
        {
            this.index = Math.max(0, index);
            this.selector = Objects.requireNonNull(selector, "Rule check selector must not be null");
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

        public String getCondition()
        {
            return condition;
        }

    }

    static RuleDefinition fromJSON(JSONObject jsonObject) throws ValidationError
    {
        final String ruleId = jsonObject.getString("id");
        final String ruleDesc = jsonObject.optString("description");
        final boolean enabled = jsonObject.optBoolean("enabled", true);
        final boolean failsTest = jsonObject.optBoolean("failsTest", false);
        final JSONObject messages = jsonObject.optJSONObject("messages");
        final JSONArray checks = jsonObject.getJSONArray("checks");
        final int rulePoints = jsonObject.optInt("points");

        final LinkedList<Check> checkList = new LinkedList<>();
        for (int i = 0; i < checks.length(); i++)
        {
            final JSONObject checkObj = checks.getJSONObject(i);
            final String selector = checkObj.getString("selector");
            final String condition = checkObj.getString("condition");

            final boolean displayValue = checkObj.optBoolean("displayValue", true);
            final boolean checkEnabled = checkObj.optBoolean("enabled", true);

            checkList.add(new Check(i, selector, condition, checkEnabled, displayValue));
        }

        if (rulePoints < 0)
        {
            throw new ValidationError("Property 'points' must be a non-negative integer");
        }
        if (!checkList.stream().anyMatch(Check::isEnabled))
        {
            throw new ValidationError("Property 'checks' must contain at least one enabled check definition");
        }

        final RuleDefinition ruleDef = new RuleDefinition(ruleId, checkList.toArray(Check[]::new));
        ruleDef.setDescription(ruleDesc);
        ruleDef.setEnabled(enabled);
        ruleDef.setFailsTest(failsTest);
        ruleDef.setPoints(rulePoints);
        ruleDef.setSuccessMessage(messages != null ? messages.optString("success") : null);
        ruleDef.setFailMessage(messages != null ? messages.optString("fail") : null);

        return ruleDef;
    }
}
