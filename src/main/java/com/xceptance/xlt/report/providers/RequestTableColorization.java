/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
