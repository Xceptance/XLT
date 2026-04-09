/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.mergerules;

import java.util.List;

import com.xceptance.xlt.report.mergerules.MergeRule.AgentNameExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.AgentNamePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.ContentTypeExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.ContentTypePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnNoMatchAtId;
import com.xceptance.xlt.report.mergerules.MergeRule.DropOnMatch;
import com.xceptance.xlt.report.mergerules.MergeRule.HttpMethodExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.HttpMethodPattern;
import com.xceptance.xlt.report.mergerules.MergeRule.NameExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.NamePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.NewName;
import com.xceptance.xlt.report.mergerules.MergeRule.RunTimeRanges;
import com.xceptance.xlt.report.mergerules.MergeRule.StatusCodeExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.StatusCodePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.StopOnMatch;
import com.xceptance.xlt.report.mergerules.MergeRule.TransactionNameExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.TransactionNamePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.UrlExcludePattern;
import com.xceptance.xlt.report.mergerules.MergeRule.UrlPattern;
import com.xceptance.xlt.report.mergerules.MergeRule.UrlText;
import com.xceptance.xlt.report.mergerules.MergeRule.UrlTextExclude;

/**
 * Basic helpers for {@link MergeRule} testing.
 *
 * @author Rene Schwietzke (Xceptance GmbH)
 */
public class MergeRuleTestBase
{
    // =================================================================================
    // Helper Methods and Classes
    // =================================================================================

    <T> T getOrDefault(final List<Object> data, final Class<T> clazz, final T defaultValue) {
        return data.stream()
            .filter(clazz::isInstance)
            .map(clazz::cast)
            .findFirst()
            .orElse(defaultValue);
    }

    MergeRule getMergeRule(final int id, Object... definitions) throws InvalidMergeRuleException {
        var data = List.of(definitions);
        return new MergeRule(id,
                             getOrDefault(data, NewName.class, new NewName("")),
                             getOrDefault(data, NamePattern.class, new NamePattern("")),
                             getOrDefault(data, UrlPattern.class, new UrlPattern("")),
                             getOrDefault(data, ContentTypePattern.class, new ContentTypePattern("")),
                             getOrDefault(data, StatusCodePattern.class, new StatusCodePattern("")),
                             getOrDefault(data, AgentNamePattern.class, new AgentNamePattern("")),
                             getOrDefault(data, TransactionNamePattern.class, new TransactionNamePattern("")),
                             getOrDefault(data, HttpMethodPattern.class, new HttpMethodPattern("")),
                             getOrDefault(data, RunTimeRanges.class, new RunTimeRanges("")),
                             getOrDefault(data, StopOnMatch.class, new StopOnMatch(true)),
                             getOrDefault(data, NameExcludePattern.class, new NameExcludePattern("")),
                             getOrDefault(data, UrlExcludePattern.class, new UrlExcludePattern("")),
                             getOrDefault(data, ContentTypeExcludePattern.class, new ContentTypeExcludePattern("")),
                             getOrDefault(data, StatusCodeExcludePattern.class, new StatusCodeExcludePattern("")),
                             getOrDefault(data, AgentNameExcludePattern.class, new AgentNameExcludePattern("")),
                             getOrDefault(data, TransactionNameExcludePattern.class, new TransactionNameExcludePattern("")),
                             getOrDefault(data, HttpMethodExcludePattern.class, new HttpMethodExcludePattern("")),
                             getOrDefault(data, ContinueOnMatchAtId.class, new ContinueOnMatchAtId(id)),
                             getOrDefault(data, ContinueOnNoMatchAtId.class, new ContinueOnNoMatchAtId(id)),
                             getOrDefault(data, DropOnMatch.class, new DropOnMatch(false)),
                             getOrDefault(data, UrlText.class, new UrlText("")),
                             getOrDefault(data, UrlTextExclude.class, new UrlTextExclude(""))
            );
    }
}
