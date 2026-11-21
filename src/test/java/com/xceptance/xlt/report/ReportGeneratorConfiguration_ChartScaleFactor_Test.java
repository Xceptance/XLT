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

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.report.util.JFreeChartUtils;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import util.JUnitParamsUtils;

@RunWith(JUnitParamsRunner.class)
public class ReportGeneratorConfiguration_ChartScaleFactor_Test extends ReportGeneratorConfigurationTestBase
{
    @Test
    public void readChartScaleFactor_default()
    {
        final double scaleFactor = readReportGeneratorProperties().getChartScaleFactor();
        assertEquals(JFreeChartUtils.DEFAULT_MODERN_CHART_SCALE_FACTOR, scaleFactor, 0.0);
    }

    @Test
    @Parameters(method = "provideValidScaleFactorParameters")
    public void readChartScaleFactor_validValues(final String propertyValue, final double expectedScaleFactor)
    {
        appendPropertyToFile(ReportGeneratorConfiguration.PROP_CHARTS_SCALE_FACTOR, propertyValue);

        final double scaleFactor = readReportGeneratorProperties().getChartScaleFactor();
        assertEquals(expectedScaleFactor, scaleFactor, 0.0);
    }

    @Test
    public void readChartScaleFactor_commandLineOverride() throws IOException
    {
        appendPropertyToFile(ReportGeneratorConfiguration.PROP_CHARTS_SCALE_FACTOR, "1.5");

        final Properties cmdLineProps = new Properties();
        cmdLineProps.setProperty(ReportGeneratorConfiguration.PROP_CHARTS_SCALE_FACTOR, "3.0");

        final ReportGeneratorConfiguration config = new ReportGeneratorConfiguration(homeDir, configDir, null, null, cmdLineProps);
        assertEquals(3.0, config.getChartScaleFactor(), 0.0);
    }

    @Test
    @Parameters(method = "provideInvalidScaleFactorParameters")
    public void readChartScaleFactor_invalidValues(final String invalidValue)
    {
        appendPropertyToFile(ReportGeneratorConfiguration.PROP_CHARTS_SCALE_FACTOR, invalidValue);

        final XltException ex = assertThrows(XltException.class, this::readReportGeneratorProperties);
        assertTrue(ex.getMessage().startsWith("Failed to parse chart scale factor: "));
    }

    @SuppressWarnings("unused")
    private Object[] provideValidScaleFactorParameters()
    {
        return new Object[]
            {
                JUnitParamsUtils.wrapParams("1", 1.0),
                JUnitParamsUtils.wrapParams("1.5", 1.5),
                JUnitParamsUtils.wrapParams("2", 2.0),
                JUnitParamsUtils.wrapParams("3", 3.0),
                JUnitParamsUtils.wrapParams("4.0", 4.0)
            };
    }

    @SuppressWarnings("unused")
    private Object[] provideInvalidScaleFactorParameters()
    {
        return new Object[]
            {
                JUnitParamsUtils.wrapParams("0"),
                JUnitParamsUtils.wrapParams("0.5"),
                JUnitParamsUtils.wrapParams("-1"),
                JUnitParamsUtils.wrapParams("abc"),
                JUnitParamsUtils.wrapParams("NaN"),
                JUnitParamsUtils.wrapParams("Infinity"),
                JUnitParamsUtils.wrapParams("")
            };
    }
}
