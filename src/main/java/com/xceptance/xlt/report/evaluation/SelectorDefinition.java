package com.xceptance.xlt.report.evaluation;

import java.util.Objects;

import org.json.JSONObject;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("selector")
public class SelectorDefinition
{
    @XStreamAsAttribute
    private final String id;

    private final String expression;

    SelectorDefinition(final String id, final String expression)
    {
        this.id = Objects.requireNonNull(id, "Selector ID must not be null");
        this.expression = Objects.requireNonNull(expression, "Selector expression must not be null");
    }

    public String getId()
    {
        return id;
    }

    public String getExpression()
    {
        return expression;
    }

    static SelectorDefinition fromJSON(final JSONObject jsonObject) throws ValidationError
    {
        final String id = jsonObject.getString("id");
        final String expression = jsonObject.getString("expression");

        return new SelectorDefinition(id, expression);
    }
}
