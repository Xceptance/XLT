/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.criteria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Criteria definitions.
 */
public final class CriteriaDefinition
{
    /**
     * Parsed criteria.
     */
    private final List<Criterion> criteria;

    /**
     * Creates a new criteria definition for the given criteria.
     * 
     * @param criteria
     *            the criteria
     */
    private CriteriaDefinition(final List<Criterion> criteria)
    {
        this.criteria = Collections.unmodifiableList(criteria);
    }

    /**
     * Creates a new criteria definition from the parsed JSON.
     * 
     * @param jsonObj
     *            the parsed JSON
     * @return criteria definition
     * @throws ValidationError
     *             thrown if criteria definition file is malformed
     */
    public static CriteriaDefinition fromJSON(final JSONObject jsonObj) throws ValidationError
    {
        validateJSON(jsonObj);

        final JSONArray _criteria = jsonObj.optJSONArray("criteria");
        final List<Criterion> list = new ArrayList<>();
        for (int i = 0, l = _criteria.length(); i < l; i++)
        {
            final JSONObject c = _criteria.optJSONObject(i);
            final String id = c.optString("id");
            final String cond = c.optString("condition");
            final String message = StringUtils.defaultString(c.optString("message"));
            final boolean enabled = !c.has("enabled") || c.optBoolean("enabled");

            list.add(new Criterion(id, enabled, cond, message));
        }

        return new CriteriaDefinition(list);
    }

    /**
     * Validates the given JSON object.
     * 
     * @param json
     *            the parsed JSON object to validate
     * @throws ValidationError
     *             thrown on validation error
     */
    private static void validateJSON(final JSONObject json) throws ValidationError
    {
        if (!json.has("criteria"))
        {
            die("Property 'criteria' does not exist");
        }
        final JSONArray critArr = json.optJSONArray("criteria");
        if (critArr == null)
        {
            die("Property 'criteria' is no array");
        }

        final LinkedList<String> list = new LinkedList<>();

        for (int i = 0, l = critArr.length(); i < l; i++)
        {
            final JSONObject c = critArr.getJSONObject(i);
            if (c == null)
            {
                die("Value at index '" + i + "' of array 'criteria' is not an object");
            }

            if (!c.has("id"))
            {
                die("Criterion at index '" + i + "' does not specify a property named 'id'");
            }

            final String id = c.optString("id");
            if (StringUtils.isBlank(id))
            {
                die("Criterion at index '" + i + "' must define a non-blank string as value for property 'id'");
            }
            final int idx = list.indexOf(id);
            if (idx > -1)
            {
                die("Criteria at indices '" + idx + "' and '" + i + "' define the same value '" + id + "' for property 'id'");
            }

            if (!c.has("condition"))
            {
                die("Criterion at index '" + i + "' does not specify a property named 'condition'");
            }
            if (StringUtils.isBlank(c.optString("condition")))
            {
                die("Criterion at index '" + i + "' must define a non-blank string as value for property 'condition'");
            }

            list.add(id);
        }
    }

    private static void die(final String message) throws ValidationError
    {
        throw new ValidationError(message);
    }

    /**
     * @return the criteria
     */
    public List<Criterion> getCriteria()
    {
        return criteria;
    }

    /**
     * Criterion definition.
     */
    public static class Criterion
    {
        /**
         * Criterion ID.
         */
        private final String id;

        /**
         * XPath expression used to determine if this criterion is met.
         */
        private final String condition;

        /**
         * Criterion's enabled flag.
         */
        private final boolean enabled;

        /**
         * Criterion's failure message.
         */
        private final String message;

        private Criterion(final String id, final boolean enabled, final String condition, final String message)
        {
            this.id = id;
            this.enabled = enabled;
            this.condition = condition;
            this.message = message;
        }

        /**
         * @return the id
         */
        public String getId()
        {
            return id;
        }

        /**
         * @return the condition
         */
        public String getCondition()
        {
            return condition;
        }

        /**
         * Returns whether this criterion is enabled.
         * 
         * @return criterion's enabled flag
         */
        public boolean isEnabled()
        {
            return enabled;
        }

        /**
         * Returns the criterion's failure message (if defined).
         * 
         * @return failure message
         */
        public String getMessage()
        {
            return message;
        }

        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append(getClass().getSimpleName()).append("[id: '").append(getId()).append("', condition: '").append(getCondition())
              .append(", enabled: ").append(isEnabled()).append(", message: '").append(getMessage()).append("']");
            return sb.toString();
        }
    }

    /**
     * Exception to signal validation errors.
     */
    public static class ValidationError extends Exception
    {
        private static final long serialVersionUID = 1L;

        private ValidationError(final String message)
        {
            super(message);
        }

        private ValidationError(final String message, final Throwable cause)
        {
            super(message, cause);
        }

        private ValidationError(final Throwable cause)
        {
            super(cause);
        }

        private ValidationError()
        {
            super();
        }
    }
}
