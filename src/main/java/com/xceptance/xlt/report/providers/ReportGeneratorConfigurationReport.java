/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;

/**
 * Represents report generator configuration values that were used during report creation and should be shown in the
 * report.
 */
@XStreamAlias("reportGeneratorConfiguration")
public class ReportGeneratorConfigurationReport
{
    public int slowestRequestsPerBucket;

    public int slowestRequestsTotal;

    public int slowestRequestsMinRuntime;

    public int slowestRequestsMaxRuntime;

    ReportGeneratorConfigurationReport(ReportGeneratorConfiguration config)
    {
        this.slowestRequestsPerBucket = config.getSlowestRequestsPerBucket();
        this.slowestRequestsTotal = config.getSlowestRequestsTotal();
        this.slowestRequestsMinRuntime = config.getSlowestRequestsMinRuntime();
        this.slowestRequestsMaxRuntime = config.getSlowestRequestsMaxRuntime();
    }
}
