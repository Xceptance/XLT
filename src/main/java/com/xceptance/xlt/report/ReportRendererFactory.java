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

/**
 * The {@link ReportRendererFactory} class provides a factory method to create the appropriate
 * {@link ReportRenderer} implementation based on the specified rendering engine.
 *
 * @author rschwietzke
 */
public class ReportRendererFactory
{
    /**
     * The name of the XSLT rendering engine.
     */
    public static final String ENGINE_XSLT = "xslt";

    /**
     * The name of the FreeMarker rendering engine.
     */
    public static final String ENGINE_FREEMARKER = "freemarker";

    /**
     * Creates a {@link ReportRenderer} for the specified engine.
     *
     * @param engine
     *            the name of the rendering engine (e.g., "xslt" or "freemarker")
     * @return the report renderer implementation
     * @throws IllegalArgumentException
     *             if the engine name is unknown
     */
    public static ReportRenderer createRenderer(final String engine, final RendererConfiguration config)
    {
        return switch (engine.toLowerCase())
        {
            case ENGINE_XSLT -> new XsltReportRenderer(config);
            case ENGINE_FREEMARKER -> new FreeMarkerReportRenderer(config);
            default -> throw new IllegalArgumentException("Unknown rendering engine: " + engine);
        };
    }
}
