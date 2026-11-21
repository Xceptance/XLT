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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.report.providers.RequestTableColorization;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import util.JUnitParamsUtils;

@RunWith(JUnitParamsRunner.class)
public class ReportGeneratorConfiguration_RequestTableColorization_Test extends ReportGeneratorConfigurationTestBase
{
    @Test
    public void readRequestTableColorization_noColorizationGroups()
    {
        final List<RequestTableColorization> groups = readReportGeneratorProperties().getRequestTableColorizations();
        assertEquals(0, groups.size());
    }

    @Test
    public void readRequestTableColorization_ignorePatternsForDefaultGroup()
    {
        // for the "default" group all patterns should be ignored, so these invalid expressions should not cause errors
        appendPatternProperties(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, "([]-]", "(ABC_]", "))Fo+bar");

        final List<RequestTableColorization> groups = readReportGeneratorProperties().getRequestTableColorizations();

        assertEquals(1, groups.size());
        assertEquals(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, groups.get(0).getGroupName());
        assertEquals(".*", groups.get(0).getNamePattern());
        assertEquals(".*", groups.get(0).getLabelPattern());
        validateNoRules(groups.get(0).getColorizationRules());
    }

    @Test
    public void readRequestTableColorization_onlyNamePattern()
    {
        appendPatternProperties("homepage", "^(MyName)", null, null);

        final List<RequestTableColorization> groups = readReportGeneratorProperties().getRequestTableColorizations();

        assertEquals(1, groups.size());
        assertEquals("homepage", groups.get(0).getGroupName());
        assertEquals("^(MyName)", groups.get(0).getNamePattern());
        assertEquals(".*", groups.get(0).getLabelPattern());
        validateNoRules(groups.get(0).getColorizationRules());
    }

    @Test
    public void readRequestTableColorization_onlyLabelPattern()
    {
        appendPatternProperties("homepage", null, "^(MyLabel)", null);

        final List<RequestTableColorization> groups = readReportGeneratorProperties().getRequestTableColorizations();

        assertEquals(1, groups.size());
        assertEquals("homepage", groups.get(0).getGroupName());
        assertEquals(".*", groups.get(0).getNamePattern());
        assertEquals("^(MyLabel)", groups.get(0).getLabelPattern());
        validateNoRules(groups.get(0).getColorizationRules());
    }

    @Test
    public void readRequestTableColorization_nameAndLabelPattern()
    {
        appendPatternProperties("homepage", "^(MyName)", "Fo+bar", null);

        final List<RequestTableColorization> groups = readReportGeneratorProperties().getRequestTableColorizations();

        assertEquals(1, groups.size());
        assertEquals("homepage", groups.get(0).getGroupName());
        assertEquals("^(MyName)", groups.get(0).getNamePattern());
        assertEquals("Fo+bar", groups.get(0).getLabelPattern());
        validateNoRules(groups.get(0).getColorizationRules());
    }

    @Test
    public void readRequestTableColorization_deprecatedNamePattern()
    {
        // if the name pattern wasn't specified, try the deprecated name pattern property
        appendPatternProperties("homepage", null, null, "^(Deprecated)");

        final List<RequestTableColorization> groups = readReportGeneratorProperties().getRequestTableColorizations();

        assertEquals(1, groups.size());
        assertEquals("homepage", groups.get(0).getGroupName());
        assertEquals("^(Deprecated)", groups.get(0).getNamePattern());
        assertEquals(".*", groups.get(0).getLabelPattern());
        validateNoRules(groups.get(0).getColorizationRules());
    }

    @Test
    public void readRequestTableColorization_namePatternTakesPrecedenceOverDeprecatedNamePattern()
    {
        // if the regular name property and the deprecated name property are both defined, the regular one wins
        appendPatternProperties("homepage", "^(MyName)", null, "^(Deprecated)");

        final List<RequestTableColorization> groups = readReportGeneratorProperties().getRequestTableColorizations();

        assertEquals(1, groups.size());
        assertEquals("homepage", groups.get(0).getGroupName());
        assertEquals("^(MyName)", groups.get(0).getNamePattern());
        assertEquals(".*", groups.get(0).getLabelPattern());
        validateNoRules(groups.get(0).getColorizationRules());
    }

    @Test
    public void readRequestTableColorization_error_noPattern()
    {
        // only add the ".mean" property, so a colorization group exists but no patterns are configured
        appendPropertyToFile(getMeanKey("homepage"), "125 250 500");

        final XltException ex = assertThrows(XltException.class, this::readReportGeneratorProperties);
        assertEquals("Failed to parse request table colorization: " +
                     String.format(ReportGeneratorConfiguration.ERROR_COLORIZATION_PATTERN_MISSING, "homepage",
                                   getNamePatternKey("homepage"), getLabelPatternKey("homepage"), getDeprecatedNamePatternKey("homepage")),
                     ex.getMessage());
    }

    @Test
    @Parameters(method = "provideInvalidPatternTestParameters")
    public void readRequestTableColorization_error_invalidPattern(final String propertyName, final String invalidPattern)
    {
        appendPropertyToFile(propertyName, invalidPattern);

        final XltException ex = assertThrows(XltException.class, this::readReportGeneratorProperties);
        assertTrue(ex.getMessage().startsWith("Failed to parse request table colorization: The value '" + invalidPattern +
                                              "' of property '" + propertyName + "' is not a valid regular expression:"));
    }

    @Test
    public void readRequestTableColorization_ignoreUnconfiguredPercentileRules()
    {
        appendPercentileRule(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, "p50", "50.0", "250 500 1000");
        appendPercentileRule(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, "p75", "75", "300 600 1200");
        appendPercentileRule(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, "p95", "95", "500 1000 2000");
        appendPercentileRule(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, "p99", "99", "1000 2000 4000");
        appendPercentileRule(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, "p99_9", "99.9", "2500 5000 10000");

        final List<RequestTableColorization> groups = readReportGeneratorProperties().getRequestTableColorizations();
        assertEquals(1, groups.size());
        assertEquals(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, groups.get(0).getGroupName());

        final Map<String, RequestTableColorization.ColorizationRule> rulesById = nonNullRulesById(groups.get(0).getColorizationRules());
        assertEquals(3, rulesById.size());
        assertRule(rulesById.get("p50"), "p50", ReportGeneratorConfiguration.PROP_SUFFIX_PERCENTILE, 250, 500, 1000);
        assertRule(rulesById.get("p95"), "p95", ReportGeneratorConfiguration.PROP_SUFFIX_PERCENTILE, 500, 1000, 2000);
        assertRule(rulesById.get("p99"), "p99", ReportGeneratorConfiguration.PROP_SUFFIX_PERCENTILE, 1000, 2000, 4000);
    }

    @Test
    public void readRequestTableColorization_ignoreAllPercentileRulesWhenRuntimePercentilesEmpty()
    {
        appendPropertyToFile(ReportGeneratorConfiguration.PROP_RUNTIME_PERCENTILES, "");
        appendPercentileRule(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, "p50", "50", "250 500 1000");
        appendPercentileRule(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, "p99_9", "99.9", "2500 5000 10000");

        final List<RequestTableColorization> groups = readReportGeneratorProperties().getRequestTableColorizations();
        assertEquals(1, groups.size());
        validateNoRules(groups.get(0).getColorizationRules());
    }

    @Test
    @Parameters(method = "provideInvalidPercentileIdTestParameters")
    public void readRequestTableColorization_error_invalidPercentileId(final String invalidPercentileId)
    {
        appendPercentileRule(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, "p_bad", invalidPercentileId,
                             "250 500 1000");

        final XltException ex = assertThrows(XltException.class, this::readReportGeneratorProperties);
        assertTrue(ex.getMessage().startsWith("Failed to parse request table colorization: "));
    }

    @Test
    public void readRequestTableColorization_ignoreUnconfiguredSegmentationRules()
    {
        appendPropertyToFile(ReportGeneratorConfiguration.PROP_RUNTIME_INTERVAL_BOUNDARIES, "1000, 5000");
        appendSegmentationRule(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, "<1000", "1000", "100 95 80");
        appendSegmentationRule(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, "<3000", "3000", "100 97 90");
        appendSegmentationRule(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, "<5000", "5000", "100 99 95");
        appendSegmentationRule(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, ">5000", ">", "0 1 5");

        final List<RequestTableColorization> groups = readReportGeneratorProperties().getRequestTableColorizations();
        assertEquals(1, groups.size());

        final Map<String, RequestTableColorization.ColorizationRule> rulesById = nonNullRulesById(groups.get(0).getColorizationRules());
        assertEquals(3, rulesById.size());
        assertRule(rulesById.get("1000"), "1000", ReportGeneratorConfiguration.PROP_SUFFIX_SEGMENTATION, 80, 95, 100);
        assertRule(rulesById.get("5000"), "5000", ReportGeneratorConfiguration.PROP_SUFFIX_SEGMENTATION, 95, 99, 100);
        assertRule(rulesById.get(""), "", ReportGeneratorConfiguration.PROP_SUFFIX_SEGMENTATION, 0, 1, 5);
    }

    @Test
    public void readRequestTableColorization_ignoreSegmentationRulesWhenBoundariesEmpty()
    {
        appendSegmentationRule(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, "<1000", "1000", "100 95 80");
        appendSegmentationRule(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, ">5000", ">", "0 1 5");

        final List<RequestTableColorization> groups = readReportGeneratorProperties().getRequestTableColorizations();
        assertEquals(1, groups.size());
        validateNoRules(groups.get(0).getColorizationRules());
    }

    @Test
    @Parameters(method = "provideInvalidSegmentationIdTestParameters")
    public void readRequestTableColorization_error_invalidSegmentationId(final String invalidSegmentationId)
    {
        appendSegmentationRule(ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE_DEFAULT, "<bad", invalidSegmentationId,
                               "100 95 80");

        final XltException ex = assertThrows(XltException.class, this::readReportGeneratorProperties);
        assertTrue(ex.getMessage().startsWith("Failed to parse request table colorization: "));
    }

    /**
     * Helper method for appending a percentile colorization rule and its ID property.
     */
    private void appendPercentileRule(final String groupName, final String ruleName, final String id, final String range)
    {
        final String baseKey = ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE + "." + groupName + "." +
                               ReportGeneratorConfiguration.PROP_SUFFIX_PERCENTILE + "." + ruleName;
        appendPropertyLinesToFile(List.of(baseKey + "." + ReportGeneratorConfiguration.PROP_SUFFIX_ID + " = " + id,
                                          baseKey + " = " + range));
    }

    /**
     * Helper method for appending a segmentation colorization rule and its ID property.
     */
    private void appendSegmentationRule(final String groupName, final String ruleName, final String id, final String range)
    {
        final String baseKey = ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE + "." + groupName + "." +
                               ReportGeneratorConfiguration.PROP_SUFFIX_SEGMENTATION + "." + ruleName;
        appendPropertyLinesToFile(List.of(baseKey + "." + ReportGeneratorConfiguration.PROP_SUFFIX_ID + " = " + id,
                                          baseKey + " = " + range));
    }

    /**
     * Collects all non-null colorization rules keyed by their rule ID.
     */
    private Map<String, RequestTableColorization.ColorizationRule> nonNullRulesById(final List<RequestTableColorization.ColorizationRule> rules)
    {
        return rules.stream().filter(Objects::nonNull).collect(Collectors.toMap(rule -> rule.id, Function.identity()));
    }

    /**
     * Asserts that the given colorization rule exists and has the expected attributes.
     */
    private void assertRule(final RequestTableColorization.ColorizationRule rule, final String expectedId, final String expectedType,
                            final int expectedFrom, final int expectedTarget, final int expectedTo)
    {
        assertTrue("Expected colorization rule for id '" + expectedId + "'", rule != null);
        assertEquals(expectedId, rule.id);
        assertEquals(expectedType, rule.type);
        assertEquals(expectedFrom, rule.from);
        assertEquals(expectedTarget, rule.target);
        assertEquals(expectedTo, rule.to);
    }

    @SuppressWarnings("unused")
    private Object[] provideInvalidPercentileIdTestParameters()
    {
        return new Object[]
            {
                JUnitParamsUtils.wrapParams("abc"),
                JUnitParamsUtils.wrapParams("0"),
                JUnitParamsUtils.wrapParams("-5"),
                JUnitParamsUtils.wrapParams("100.1"),
                JUnitParamsUtils.wrapParams("NaN")
            };
    }

    @SuppressWarnings("unused")
    private Object[] provideInvalidSegmentationIdTestParameters()
    {
        return new Object[]
            {
                JUnitParamsUtils.wrapParams("abc"),
                JUnitParamsUtils.wrapParams("0"),
                JUnitParamsUtils.wrapParams("-100")
            };
    }

    /**
     * Helper method for appending colorization group pattern properties with the given groupName and the given patterns
     * to the "reportgenerator.properties" test file.
     */
    private void appendPatternProperties(final String groupName, final String namePattern, final String labelPattern,
                                         final String deprecatedNamePattern)
    {
        final List<String> lines = new ArrayList<>();

        if (namePattern != null)
        {
            lines.add(getNamePatternKey(groupName) + " = " + namePattern);
        }

        if (labelPattern != null)
        {
            lines.add(getLabelPatternKey(groupName) + " = " + labelPattern);
        }

        if (deprecatedNamePattern != null)
        {
            lines.add(getDeprecatedNamePatternKey(groupName) + " = " + deprecatedNamePattern);
        }

        appendPropertyLinesToFile(lines);
    }

    /**
     * Get key for name pattern property with the given groupName.
     */
    private String getNamePatternKey(final String groupName)
    {
        return ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE + "." + groupName + "." +
               ReportGeneratorConfiguration.PROP_SUFFIX_MATCHING_NAME;
    }

    /**
     * Get key for label pattern property with the given groupName.
     */
    private String getLabelPatternKey(final String groupName)
    {
        return ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE + "." + groupName + "." +
               ReportGeneratorConfiguration.PROP_SUFFIX_MATCHING_LABEL;
    }

    /**
     * Get key for deprecated name pattern property with the given groupName.
     */
    private String getDeprecatedNamePatternKey(final String groupName)
    {
        return ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE + "." + groupName + "." +
               ReportGeneratorConfiguration.PROP_SUFFIX_MATCHING;
    }

    /**
     * Get key for mean colorization config property with the given groupName.
     */
    private String getMeanKey(final String groupName)
    {
        return ReportGeneratorConfiguration.PROP_REQUESTS_TABLE_COLORIZE + "." + groupName + "." +
               ReportGeneratorConfiguration.PROP_SUFFIX_MEAN;
    }

    /**
     * Helper method to validate that the given colorization rules only contain the elements that are expected if no
     * rules were defined.
     */
    private void validateNoRules(final List<RequestTableColorization.ColorizationRule> rules)
    {
        // if no colorization rules were specified, the resulting list will still contain three "null" elements
        assertEquals(3, rules.size());
        assertEquals(null, rules.get(0));
        assertEquals(null, rules.get(1));
        assertEquals(null, rules.get(2));
    }

    @SuppressWarnings("unused")
    private Object[] provideInvalidPatternTestParameters()
    {
        // propertyName | invalidPattern
        return new Object[]
            {
                JUnitParamsUtils.wrapParams(getNamePatternKey("homepage"), "([]-]"),
                JUnitParamsUtils.wrapParams(getLabelPatternKey("homepage"), "([]-]"),
                JUnitParamsUtils.wrapParams(getDeprecatedNamePatternKey("homepage"), "([]-]")
            };
    }
}
