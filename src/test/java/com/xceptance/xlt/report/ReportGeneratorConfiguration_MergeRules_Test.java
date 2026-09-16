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
package com.xceptance.xlt.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.report.mergerules.MergeRule;

public class ReportGeneratorConfiguration_MergeRules_Test extends ReportGeneratorConfigurationTestBase
{
    private static final String PROP_MERGE_RULES_PREFIX = "com.xceptance.xlt.reportgenerator.requestMergeRules.";

    @Test
    public void getMergeRules_noRules()
    {
        final List<MergeRule> mergeRules = readReportGeneratorProperties().getMergeRules();
        assertEquals(0, mergeRules.size());
    }

    @Test
    public void getMergeRules_urlPatternWithHashQuantifier()
    {
        final String rulePrefix = PROP_MERGE_RULES_PREFIX + "1.";
        appendPropertyToFile(rulePrefix + "newName", "MatchedHash");
        appendPropertyToFile(rulePrefix + "urlPattern", ".*/page#{1,3}");
        appendPropertyToFile(rulePrefix + "stopOnMatch", "true");

        final List<MergeRule> mergeRules = readReportGeneratorProperties().getMergeRules();
        assertEquals(1, mergeRules.size());

        final MergeRule rule = mergeRules.get(0);
        assertNotNull(rule);

        final RequestData data = new RequestData("MyRequest");
        data.setUrl("http://example.com/page##");
        rule.process(data);
        assertEquals("MatchedHash", data.getName());
    }

    @Test
    public void getMergeRules_urlPatternWithLiteralHashBrace()
    {
        final String rulePrefix = PROP_MERGE_RULES_PREFIX + "1.";
        appendPropertyToFile(rulePrefix + "newName", "{n} MatchedLiteral");
        appendPropertyToFile(rulePrefix + "urlPattern", ".*/item#[{].*[}]");
        appendPropertyToFile(rulePrefix + "stopOnMatch", "false");

        final List<MergeRule> mergeRules = readReportGeneratorProperties().getMergeRules();
        assertEquals(1, mergeRules.size());

        final MergeRule rule = mergeRules.get(0);
        assertNotNull(rule);

        final RequestData data = new RequestData("MyRequest");
        data.setUrl("http://example.com/item#{abc}");
        rule.process(data);
        assertEquals("MyRequest MatchedLiteral", data.getName());
    }

    @Test
    public void getMergeRules_namePatternWithHashQuantifier()
    {
        final String rulePrefix = PROP_MERGE_RULES_PREFIX + "1.";
        appendPropertyToFile(rulePrefix + "newName", "MatchedName");
        appendPropertyToFile(rulePrefix + "namePattern", "req#{1,2}");
        appendPropertyToFile(rulePrefix + "stopOnMatch", "true");

        final List<MergeRule> mergeRules = readReportGeneratorProperties().getMergeRules();
        assertEquals(1, mergeRules.size());

        final MergeRule rule = mergeRules.get(0);
        assertNotNull(rule);

        final RequestData data = new RequestData("req##");
        rule.process(data);
        assertEquals("MatchedName", data.getName());
    }

    @Test
    public void getMergeRules_newNameWithHashSyntax()
    {
        final String rulePrefix = PROP_MERGE_RULES_PREFIX + "1.";
        appendPropertyToFile(rulePrefix + "newName", "{n} #{literal}");
        appendPropertyToFile(rulePrefix + "namePattern", ".*");
        appendPropertyToFile(rulePrefix + "stopOnMatch", "true");

        final List<MergeRule> mergeRules = readReportGeneratorProperties().getMergeRules();
        assertEquals(1, mergeRules.size());

        final MergeRule rule = mergeRules.get(0);
        assertNotNull(rule);

        final RequestData data = new RequestData("MyRequest");
        rule.process(data);
        assertEquals("MyRequest #{literal}", data.getName());
    }

    @Test
    public void getMergeRules_standardVariableSubstitutionStillWorks()
    {
        appendPropertyToFile("my.base.url", "http://example.com");
        final String rulePrefix = PROP_MERGE_RULES_PREFIX + "1.";
        appendPropertyToFile(rulePrefix + "newName", "{n} MatchedBase");
        appendPropertyToFile(rulePrefix + "urlPattern", "${my.base.url}/page#{1,2}");
        appendPropertyToFile(rulePrefix + "stopOnMatch", "true");

        final List<MergeRule> mergeRules = readReportGeneratorProperties().getMergeRules();
        assertEquals(1, mergeRules.size());

        final MergeRule rule = mergeRules.get(0);
        assertNotNull(rule);

        final RequestData data = new RequestData("MyRequest");
        data.setUrl("http://example.com/page#");
        rule.process(data);
        assertEquals("MyRequest MatchedBase", data.getName());
    }
}
