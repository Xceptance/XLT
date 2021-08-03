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
package com.xceptance.xlt.report.providers;

import java.io.File;
import java.io.IOException;

import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the {@link ConfigurationReportProvider}
 */
public class ConfigurationReportProviderTest
{
    @Test
    public void testSecretPropertiesAreMaskedInTheOutput() throws IOException
    {
        final ConfigurationReportProvider provider = new ConfigurationReportProvider();
        ReportGeneratorConfiguration config = new ReportGeneratorConfiguration();
        config.setReportDirectory(new File("samples/testsuite-posters"));
        provider.setConfiguration(config);

        final ConfigurationReport report = (ConfigurationReport) provider.createReportFragment();

        Assert.assertEquals(XltConstants.MASK_PROPERTIES_HIDETEXT, report.properties.getProperty("secret.value"));
    }
}
