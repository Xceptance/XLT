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
package com.xceptance.xlt.report.providers;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.xceptance.xlt.report.ReportGeneratorConfiguration;

public class RequestsReportProviderTest
{
    @Test
    public void processTableColorizations_patternCombinationsWithoutDefault()
    {
        // only name matches
        final TimerReport report1 = new TimerReport();
        report1.name = "MyName123";
        report1.labels = "Any Label";

        // only label matches
        final TimerReport report2 = new TimerReport();
        report2.name = "Any Name";
        report2.labels = "Foobar";

        // name and label match
        final TimerReport report3 = new TimerReport();
        report3.name = "MyName123";
        report3.labels = "Foobar";

        // nothing matches
        final TimerReport report4 = new TimerReport();
        report4.name = "Any Name";
        report4.labels = "Any Label";

        // no "default" colorization group exists
        final RequestTableColorization col = new RequestTableColorization("test", "MyName.*", "Fo+bar", null);

        RequestsReportProvider.processTableColorizations(List.of(report1, report2, report3, report4), mockConfig(List.of(col)));

        // colorization group is only applied if name AND label match
        assertEquals(null, report1.colorizationGroupName);
        assertEquals(null, report2.colorizationGroupName);
        assertEquals("test", report3.colorizationGroupName);
        assertEquals(null, report4.colorizationGroupName);
    }

    @Test
    public void processTableColorizations_patternCombinationsWithDefault()
    {
        // only name matches
        final TimerReport report1 = new TimerReport();
        report1.name = "MyName123";
        report1.labels = "Any Label";

        // only label matches
        final TimerReport report2 = new TimerReport();
        report2.name = "Any Name";
        report2.labels = "Foobar";

        // name and label match
        final TimerReport report3 = new TimerReport();
        report3.name = "MyName123";
        report3.labels = "Foobar";

        // nothing matches
        final TimerReport report4 = new TimerReport();
        report4.name = "Any Name";
        report4.labels = "Any Label";

        // there is a "default" and one other colorization group
        final RequestTableColorization defaultCol = new RequestTableColorization("default", null, null, null);
        final RequestTableColorization col1 = new RequestTableColorization("test", "MyName.*", "Fo+bar", null);

        RequestsReportProvider.processTableColorizations(List.of(report1, report2, report3, report4),
                                                         mockConfig(List.of(defaultCol, col1)));

        // group "test" is only applied if name AND label match; otherwise the "default" is applied
        assertEquals("default", report1.colorizationGroupName);
        assertEquals("default", report2.colorizationGroupName);
        assertEquals("test", report3.colorizationGroupName);
        assertEquals("default", report4.colorizationGroupName);
    }

    @Test
    public void processTableColorizations_multipleMatches()
    {
        final TimerReport report = new TimerReport();
        report.name = "MyName123";
        report.labels = "MyLabel123";

        // multiple colorization groups match the report
        final RequestTableColorization col1 = new RequestTableColorization("test1", "MyName.*", ".*", null);
        final RequestTableColorization col2 = new RequestTableColorization("test2", ".*", "MyLabel.*", null);

        RequestsReportProvider.processTableColorizations(List.of(report), mockConfig(List.of(col1, col2)));

        // no group is applied
        assertEquals(null, report.colorizationGroupName);
    }

    @Test
    public void processTableColorizations_multipleMatchesAndDefault()
    {
        final TimerReport report = new TimerReport();
        report.name = "MyName123";
        report.labels = "MyLabel123";

        // multiple colorization groups match the report and a "default" group exists as well
        final RequestTableColorization defaultCol = new RequestTableColorization("default", null, null, null);
        final RequestTableColorization col1 = new RequestTableColorization("test1", "MyName.*", ".*", null);
        final RequestTableColorization col2 = new RequestTableColorization("test2", ".*", "MyLabel.*", null);

        RequestsReportProvider.processTableColorizations(List.of(report), mockConfig(List.of(defaultCol, col1, col2)));

        // no group is applied
        assertEquals(null, report.colorizationGroupName);
    }

    /**
     * Create a {@link ReportGeneratorConfiguration} mock that returns the given colorizations when asked.
     */
    private ReportGeneratorConfiguration mockConfig(final List<RequestTableColorization> colorizations)
    {
        final ReportGeneratorConfiguration configMock = Mockito.mock(ReportGeneratorConfiguration.class);

        Mockito.doReturn("default").when(configMock).getRequestTableColorizationDefaultGroupName();
        Mockito.doReturn(colorizations).when(configMock).getRequestTableColorizations();

        return configMock;
    }
}
