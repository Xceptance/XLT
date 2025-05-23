/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.report.scorecard;

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

    static SelectorDefinition fromJSON(final JSONObject jsonObject) throws ValidationException
    {
        final String id = jsonObject.getString("id");
        final String expression = jsonObject.getString("expression");

        return new SelectorDefinition(id, expression);
    }
}
