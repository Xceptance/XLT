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

/**
 * The web vitals metrics for a certain action.
 */
@XStreamAlias("webVitals")
public class WebVitalsReport
{
    /**
     * The action/page name.
     */
    public String name;

    /**
     * The CLS metric data.
     */
    public WebVitalReport cls;

    /**
     * The FCP metric data.
     */
    public WebVitalReport fcp;

    /**
     * The FID metric data.
     */
    public WebVitalReport fid;

    /**
     * The INP metric data.
     */
    public WebVitalReport inp;

    /**
     * The LCP metric data.
     */
    public WebVitalReport lcp;

    /**
     * The TTFB metric data.
     */
    public WebVitalReport ttfb;
}
