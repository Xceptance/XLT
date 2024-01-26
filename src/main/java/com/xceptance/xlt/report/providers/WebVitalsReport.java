/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
import com.xceptance.xlt.report.providers.WebVitalsDataProcessor.WebVital;

/**
 */
@XStreamAlias("webVitals")
public class WebVitalsReport
{
    /**
     * The page name.
     */
    public final String name;

    /**
     * The value.
     */
    public WebVital cls;

    /**
     * The value.
     */
    public WebVital fcp;

    /**
     * The value.
     */
    public WebVital fid;

    /**
     * The value.
     */
    public WebVital inp;

    /**
     * The value.
     */
    public WebVital lcp;

    /**
     * The value.
     */
    public WebVital ttfb;
    
    WebVitalsReport(String name)
    {
        this.name = name;
    }
}
