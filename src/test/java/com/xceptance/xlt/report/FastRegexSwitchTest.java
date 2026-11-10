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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Test;

import com.xceptance.common.util.RegExUtils;
import com.xceptance.common.util.StringMatcher;
import com.xceptance.util.fastregex.FastRegex;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.report.labelingrules.LabelingRule;
import com.xceptance.xlt.report.mergerules.Condition;
import com.xceptance.xlt.report.mergerules.MergeRule;
import com.xceptance.xlt.report.providers.RequestReport;
import com.xceptance.xlt.report.storage.cache.CacheFingerprint;

/**
 * Verifies the {@code com.xceptance.xlt.reportgenerator.requestMergeRules.fastRegex.enabled} configuration switch
 * (enabled by default, with legacy alias support) and confirms that FastRegex is used exclusively for
 * {@link MergeRule} condition processing and NOT for other components such as {@link LabelingRule} or {@link StringMatcher}.
 */
public class FastRegexSwitchTest extends ReportGeneratorConfigurationTestBase
{
    private static final String PROP_MERGE_RULES_PREFIX = "com.xceptance.xlt.reportgenerator.requestMergeRules.";

    private static final String PROP_LABELING_RULES_PREFIX = "com.xceptance.xlt.reportgenerator.labelingRules.";

    @After
    public void resetFastRegexDefault()
    {
        System.clearProperty(ReportGeneratorConfiguration.PROP_FAST_REGEX_ENABLED);
        System.clearProperty(ReportGeneratorConfiguration.PROP_FAST_REGEX_ENABLED_ALIAS);
        RegExUtils.setFastRegexEnabled(true);
    }

    @Test
    public void defaultEnabledInConfigurationAndRegExUtils()
    {
        final ReportGeneratorConfiguration config = readReportGeneratorProperties();
        assertTrue(config.isFastRegexEnabled());
        assertTrue(RegExUtils.isFastRegexEnabled());
    }

    @Test
    public void disableAndEnableViaPropertiesAndCommandLine() throws Exception
    {
        // 1. Disabled in reportgenerator.properties
        appendPropertyToFile(ReportGeneratorConfiguration.PROP_FAST_REGEX_ENABLED, "false");
        final ReportGeneratorConfiguration disabledConfig = readReportGeneratorProperties();
        assertFalse(disabledConfig.isFastRegexEnabled());
        assertFalse(RegExUtils.isFastRegexEnabled());

        // 2. Overridden back to true via CLI -D property
        final Properties cliProps = new Properties();
        cliProps.setProperty(ReportGeneratorConfiguration.PROP_FAST_REGEX_ENABLED, "true");
        final ReportGeneratorConfiguration cliEnabledConfig =
            new ReportGeneratorConfiguration(homeDir, configDir, null, null, cliProps);
        assertTrue(cliEnabledConfig.isFastRegexEnabled());
        assertTrue(RegExUtils.isFastRegexEnabled());

        // 3. Overridden back to false via CLI -D property using alias
        cliProps.remove(ReportGeneratorConfiguration.PROP_FAST_REGEX_ENABLED);
        cliProps.setProperty(ReportGeneratorConfiguration.PROP_FAST_REGEX_ENABLED_ALIAS, "false");
        final ReportGeneratorConfiguration cliDisabledConfig =
            new ReportGeneratorConfiguration(homeDir, configDir, null, null, cliProps);
        assertFalse(cliDisabledConfig.isFastRegexEnabled());
        assertFalse(RegExUtils.isFastRegexEnabled());
    }

    @Test
    public void mergeRulesParityEnabledVsDisabled()
    {
        // Rule 1: native FastRegex with multiple capturing groups
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.newName", "API.{u:1}.{u:2}");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.urlPattern", "/scapi/([^/]+?)/([^/]+?)/v1");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.stopOnMatch", "true");

        // Rule 2: backreference + lookahead rule (triggers transparent fallback in FastRegex)
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "2.newName", "Suggest.{u:2}.{u:3}");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "2.urlPattern",
                             "(shopper-([^/]+))/[\\\\w/]+/(\\\\2-suggestions)(?=\\\\?|([/A-z-0-9]+)\\\\?)");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "2.stopOnMatch", "true");

        final String[] urls =
        {
            "https://example.com/scapi/shopper/products/v1?id=1",
            "https://example.com/scapi/shopper/products/v1?id=1", // cache hit
            "https://example.com/api/search/shopper-search/v1/org/search-suggestions?q=shirt",
            "https://example.com/static/logo.png",
            "https://example.com/static/logo.png" // negative cache hit
        };

        // Enabled (default)
        RegExUtils.setFastRegexEnabled(true);
        final List<MergeRule> fastRules = readReportGeneratorProperties().getMergeRules();
        assertEquals(2, fastRules.size());
        assertTrue(fastRules.get(0).getIncludeConditions()[0].isUsingFastRegex());
        assertTrue(fastRules.get(1).getIncludeConditions()[0].isUsingFastRegex());

        final List<String> fastResults = new ArrayList<>();
        for (final String url : urls)
        {
            final RequestData req = new RequestData("Orig");
            req.setUrl(url);
            for (final MergeRule rule : fastRules)
            {
                if (rule.process(req) == MergeRule.STOP)
                {
                    break;
                }
            }
            fastResults.add(req.getName());
        }

        // Disabled
        appendPropertyToFile(ReportGeneratorConfiguration.PROP_FAST_REGEX_ENABLED, "false");
        final List<MergeRule> jdkRules = readReportGeneratorProperties().getMergeRules();
        assertEquals(2, jdkRules.size());
        assertFalse(jdkRules.get(0).getIncludeConditions()[0].isUsingFastRegex());
        assertFalse(jdkRules.get(1).getIncludeConditions()[0].isUsingFastRegex());

        final List<String> jdkResults = new ArrayList<>();
        for (final String url : urls)
        {
            final RequestData req = new RequestData("Orig");
            req.setUrl(url);
            for (final MergeRule rule : jdkRules)
            {
                if (rule.process(req) == MergeRule.STOP)
                {
                    break;
                }
            }
            jdkResults.add(req.getName());
        }

        assertEquals(List.of("API.shopper.products",
                             "API.shopper.products",
                             "Suggest.search.search-suggestions",
                             "Orig",
                             "Orig"),
                     fastResults);
        assertEquals(jdkResults, fastResults);
    }

    @Test
    public void nonMergeRuleComponentsDoNotUseFastRegex()
    {
        // 1. Labeling rules must always use standard JDK Pattern, never FastRegex
        appendPropertyToFile(PROP_LABELING_RULES_PREFIX + "1.newLabels", "svc:{n:1}");
        appendPropertyToFile(PROP_LABELING_RULES_PREFIX + "1.types", "R");
        appendPropertyToFile(PROP_LABELING_RULES_PREFIX + "1.namePattern", "^API\\.([^.]+)\\..*");

        RegExUtils.setFastRegexEnabled(true);
        final List<LabelingRule> rules = readReportGeneratorProperties().getLabelingRules();
        assertEquals(1, rules.size());

        final RequestReport report = new RequestReport();
        report.name = "API.shopper.products";
        rules.get(0).process(report);
        assertEquals("svc:shopper", report.labels);

        // 2. StringMatcher must always use standard JDK Pattern, never FastRegex
        final StringMatcher matcher = new StringMatcher("T(Browse|Order).*", ".*_Internal", true);
        assertTrue(matcher.isAccepted("TBrowse"));
        assertTrue(matcher.isAccepted("TOrder_US"));
        assertFalse(matcher.isAccepted("TOrder_Internal"));
        assertFalse(matcher.isAccepted("TSearch"));
    }

    @Test
    public void perThreadMergeRuleIsolationAndSharedNothingConcurrency() throws Exception
    {
        // Configure merge rule
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.newName", "Item.{u:1}");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.urlPattern", "/items/([0-9]+)");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.stopOnMatch", "true");

        final ReportGeneratorConfiguration config = readReportGeneratorProperties();

        // Main thread rule set
        final List<MergeRule> mainRules = config.getMergeRules();

        final ExecutorService pool = Executors.newFixedThreadPool(8);
        try
        {
            final List<Callable<Void>> tasks = new ArrayList<>();
            for (int t = 0; t < 16; t++)
            {
                final int idx = t;
                tasks.add(() ->
                {
                    // Each parser thread builds its own rule set with separate Condition instances
                    final List<MergeRule> threadRules = config.getMergeRules();
                    assertNotSame("Thread must get its own MergeRule instance", mainRules.get(0), threadRules.get(0));
                    assertNotSame("Thread must get its own Condition instance",
                                  mainRules.get(0).getIncludeConditions()[0],
                                  threadRules.get(0).getIncludeConditions()[0]);

                    // Test repeated lookups against matching and non-matching URLs to verify per-thread Condition result caches
                    for (int rep = 0; rep < 5; rep++)
                    {
                        final RequestData reqMatch = new RequestData("Orig");
                        reqMatch.setUrl("https://host/catalog/items/" + (1000 + idx));
                        for (final MergeRule rule : threadRules)
                        {
                            if (rule.process(reqMatch) == MergeRule.STOP)
                            {
                                break;
                            }
                        }
                        assertEquals("Item." + (1000 + idx), reqMatch.getName());

                        final RequestData reqMiss = new RequestData("Orig");
                        reqMiss.setUrl("https://host/static/css/style.css");
                        for (final MergeRule rule : threadRules)
                        {
                            if (rule.process(reqMiss) == MergeRule.STOP)
                            {
                                break;
                            }
                        }
                        assertEquals("Orig", reqMiss.getName());
                    }
                    return null;
                });
            }
            for (final Future<Void> f : pool.invokeAll(tasks))
            {
                f.get();
            }
        }
        finally
        {
            pool.shutdownNow();
        }
    }

    @Test
    public void testRuleOverrideToStandardRegexWhenGloballyEnabled()
    {
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.newName", "Rule1");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.urlPattern", "/test/(.+)");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.fastRegex", "false");

        final ReportGeneratorConfiguration config = readReportGeneratorProperties();
        assertTrue(config.isFastRegexEnabled());
        final List<MergeRule> rules = config.getMergeRules();
        assertEquals(1, rules.size());
        assertFalse(rules.get(0).isUsingFastRegex());
        assertFalse(rules.get(0).getIncludeConditions()[0].isUsingFastRegex());
    }

    @Test
    public void testRuleExplicitFastRegexWhenGloballyEnabled()
    {
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.newName", "Rule1");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.urlPattern", "/test/(.+)");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.fastRegex", "true");

        final ReportGeneratorConfiguration config = readReportGeneratorProperties();
        assertTrue(config.isFastRegexEnabled());
        final List<MergeRule> rules = config.getMergeRules();
        assertEquals(1, rules.size());
        assertTrue(rules.get(0).isUsingFastRegex());
        assertTrue(rules.get(0).getIncludeConditions()[0].isUsingFastRegex());
    }

    @Test
    public void testRuleDefaultFastRegexWhenGloballyEnabled()
    {
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.newName", "Rule1");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.urlPattern", "/test/(.+)");

        final ReportGeneratorConfiguration config = readReportGeneratorProperties();
        assertTrue(config.isFastRegexEnabled());
        final List<MergeRule> rules = config.getMergeRules();
        assertEquals(1, rules.size());
        assertTrue(rules.get(0).isUsingFastRegex());
        assertTrue(rules.get(0).getIncludeConditions()[0].isUsingFastRegex());
    }

    @Test
    public void testGlobalDisabledWithRuleExplicitTrue()
    {
        appendPropertyToFile(ReportGeneratorConfiguration.PROP_FAST_REGEX_ENABLED, "false");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.newName", "Rule1");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.urlPattern", "/test/(.+)");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.fastRegex", "true");

        final ReportGeneratorConfiguration config = readReportGeneratorProperties();
        assertFalse(config.isFastRegexEnabled());
        final List<MergeRule> rules = config.getMergeRules();
        assertEquals(1, rules.size());
        assertFalse(rules.get(0).isUsingFastRegex());
        assertFalse(rules.get(0).getIncludeConditions()[0].isUsingFastRegex());
    }

    @Test
    public void testGlobalDisabledWithRuleExplicitFalse()
    {
        appendPropertyToFile(ReportGeneratorConfiguration.PROP_FAST_REGEX_ENABLED, "false");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.newName", "Rule1");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.urlPattern", "/test/(.+)");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.fastRegex", "false");

        final ReportGeneratorConfiguration config = readReportGeneratorProperties();
        assertFalse(config.isFastRegexEnabled());
        final List<MergeRule> rules = config.getMergeRules();
        assertEquals(1, rules.size());
        assertFalse(rules.get(0).isUsingFastRegex());
        assertFalse(rules.get(0).getIncludeConditions()[0].isUsingFastRegex());
    }

    @Test
    public void testGlobalDisabledWithRuleUnspecified()
    {
        appendPropertyToFile(ReportGeneratorConfiguration.PROP_FAST_REGEX_ENABLED, "false");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.newName", "Rule1");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.urlPattern", "/test/(.+)");

        final ReportGeneratorConfiguration config = readReportGeneratorProperties();
        assertFalse(config.isFastRegexEnabled());
        final List<MergeRule> rules = config.getMergeRules();
        assertEquals(1, rules.size());
        assertFalse(rules.get(0).isUsingFastRegex());
        assertFalse(rules.get(0).getIncludeConditions()[0].isUsingFastRegex());
    }

    @Test
    public void testAllConditionTypesInheritRuleFastRegexSetting()
    {
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.newName", "AllConditions");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.urlPattern", "/url/.*");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.namePattern", "name_.*");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.contentTypePattern", "text/.*");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.statusCodePattern", "20[0-9]");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.agentPattern", "agent_.*");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.transactionPattern", "trans_.*");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.methodPattern", "GET|POST");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.urlPattern.exclude", "/ex_url/.*");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.namePattern.exclude", "ex_name_.*");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.contentTypePattern.exclude", "image/.*");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.statusCodePattern.exclude", "40[0-9]");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.agentPattern.exclude", "ex_agent_.*");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.transactionPattern.exclude", "ex_trans_.*");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.methodPattern.exclude", "HEAD");
        appendPropertyToFile(PROP_MERGE_RULES_PREFIX + "1.fastRegex", "false");

        final ReportGeneratorConfiguration config = readReportGeneratorProperties();
        final MergeRule rule = config.getMergeRules().get(0);
        assertFalse(rule.isUsingFastRegex());

        for (final Condition c : rule.getIncludeConditions())
        {
            assertFalse(c.isUsingFastRegex());
        }
        for (final Condition c : rule.getExcludeConditions())
        {
            assertFalse(c.isUsingFastRegex());
        }
    }

    @Test
    public void testCacheFingerprintDivergesOnFastRegexToggle() throws IOException
    {
        final File resultsDir = tempFolder.newFolder("results");
        final Properties propsFast = new Properties();
        propsFast.setProperty(PROP_MERGE_RULES_PREFIX + "1.newName", "Rule1");
        propsFast.setProperty(PROP_MERGE_RULES_PREFIX + "1.urlPattern", "/catalog/([^/]+)");
        propsFast.setProperty(PROP_MERGE_RULES_PREFIX + "1.fastRegex", "true");
        final ReportGeneratorConfiguration configFast =
            new ReportGeneratorConfiguration(homeDir, configDir, null, null, propsFast);

        final Properties propsStandard = new Properties();
        propsStandard.setProperty(PROP_MERGE_RULES_PREFIX + "1.newName", "Rule1");
        propsStandard.setProperty(PROP_MERGE_RULES_PREFIX + "1.urlPattern", "/catalog/([^/]+)");
        propsStandard.setProperty(PROP_MERGE_RULES_PREFIX + "1.fastRegex", "false");
        final ReportGeneratorConfiguration configStandard =
            new ReportGeneratorConfiguration(homeDir, configDir, null, null, propsStandard);

        final CacheFingerprint fp1 = CacheFingerprint.calculate(resultsDir, configFast);
        final CacheFingerprint fp2 = CacheFingerprint.calculate(resultsDir, configStandard);

        assertNotEquals(fp1.mergeRulesHash(), fp2.mergeRulesHash());
        assertFalse(fp1.matches(fp2));
    }
}
