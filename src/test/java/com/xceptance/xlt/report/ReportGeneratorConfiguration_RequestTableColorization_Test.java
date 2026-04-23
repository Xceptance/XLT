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
