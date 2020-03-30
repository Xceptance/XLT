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
package com.xceptance.xlt.report.providers;

import java.io.IOException;

import com.xceptance.xlt.report.ReportGeneratorConfiguration;

/**
 * Convenience implementation for testing purposes. Overrides {@link #getChartWidth()}.
 * 
 * @author Sebastian Oerding
 */
public class DummyReportGeneratorConfiguration extends ReportGeneratorConfiguration
{
    /**
     * @throws IOException
     */
    public DummyReportGeneratorConfiguration() throws IOException
    {
        super();
    }

    /**
     * Convenience method to get a report generator configuration without the need to catch an exception that would
     * never happen.
     */
    public static DummyReportGeneratorConfiguration getDefault()
    {
        try
        {
            return new DummyReportGeneratorConfiguration();
        }
        catch (final IOException e)
        {
            // this can't happen as long as the super class does not change but the compiler does not know.
            throw new RuntimeException(e);
        }
    }

    /**
     * @return 1 to avoid initialization errors
     */
    @Override
    public int getChartWidth()
    {
        return 1;
    }
}
