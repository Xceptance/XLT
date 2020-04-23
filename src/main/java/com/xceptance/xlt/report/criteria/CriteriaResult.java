/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

/**
 * Document-wide validation result.
 */
public class CriteriaResult
{
    /**
     * The individual criterion validation results.
     */
    private final List<CriterionResult> _results = new ArrayList<>();

    /**
     * Name of the validated document.
     */
    private final String documentName;

    /**
     * Creates a new validation result for the given document.
     * 
     * @param name
     *            the name of the document
     */
    public CriteriaResult(final String name)
    {
        documentName = name;
    }

    /**
     * Adds a new criterion validation result
     * 
     * @param result
     *            the criterion validation result to add
     */
    public void add(final CriterionResult result)
    {
        _results.add(result);
    }

    /**
     * @return the documentName
     */
    public String getDocumentName()
    {
        return documentName;
    }

    /**
     * Returns the individual criterion validation results.
     * 
     * @return the criterion validation results
     */
    public List<CriterionResult> getResults()
    {
        return _results;
    }

    /**
     * Returns whether or not the document meets all criteria.
     * 
     * @return <code>true</code> if all criteria are met, <code>false</code> otherwise
     */
    public boolean hasPassed()
    {
        return _results.stream().allMatch(CriterionResult::hasPassed);
    }

    public JSONObject toJSON()
    {
        final JSONObject json = new JSONObject();
        json.put("document", getDocumentName());

        final JSONObject details = new JSONObject();
        final Map<CriterionResult.Status, Integer> totalMap = new HashMap<>();
        for (final CriterionResult crit : getResults())
        {
            final CriterionResult.Status status = crit.getStatus();
            final String message = crit.getMessage();
            final Integer i = totalMap.getOrDefault(status, 0);
            totalMap.put(status, new Integer(i + 1));

            final JSONObject obj = new JSONObject();
            obj.put("status", status.name().toLowerCase());

            if (message != null)
            {
                obj.put("message", message);
            }

            details.put(crit.getId(), obj);
        }

        final JSONObject totals = new JSONObject();
        for (CriterionResult.Status s : CriterionResult.Status.values())
        {
            totals.put(s.name().toLowerCase(), totalMap.getOrDefault(s, 0));
        }

        json.put("totals", totals);
        json.put("details", details);

        return json;
    }
}
