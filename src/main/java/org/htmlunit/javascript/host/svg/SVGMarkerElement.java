/*
 * Copyright (c) 2002-2024 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.htmlunit.javascript.host.svg;

import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstant;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.svg.SvgMarker;

/**
 * A JavaScript object for {@code SVGMarkerElement}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass(domClass = SvgMarker.class)
public class SVGMarkerElement extends SVGElement {

    /** The constant {@code SVG_MARKER_ORIENT_UNKNOWN}. */
    @JsxConstant
    public static final int SVG_MARKER_ORIENT_UNKNOWN = 0;

    /** The constant {@code SVG_MARKERUNITS_UNKNOWN}. */
    @JsxConstant
    public static final int SVG_MARKERUNITS_UNKNOWN = 0;

    /** The constant {@code SVG_MARKER_ORIENT_AUTO}. */
    @JsxConstant
    public static final int SVG_MARKER_ORIENT_AUTO = 1;

    /** The constant {@code SVG_MARKERUNITS_USERSPACEONUSE}. */
    @JsxConstant
    public static final int SVG_MARKERUNITS_USERSPACEONUSE = 1;

    /** The constant {@code SVG_MARKER_ORIENT_ANGLE}. */
    @JsxConstant
    public static final int SVG_MARKER_ORIENT_ANGLE = 2;

    /** The constant {@code SVG_MARKERUNITS_STROKEWIDTH}. */
    @JsxConstant
    public static final int SVG_MARKERUNITS_STROKEWIDTH = 2;

    /** The constant {@code SVG_MARKER_ORIENT_AUTO_START_REVERSE}. */
    @JsxConstant({FF, FF_ESR})
    public static final int SVG_MARKER_ORIENT_AUTO_START_REVERSE = 3;

    /**
     * Creates an instance.
     */
    public SVGMarkerElement() {
    }

    /**
     * Creates an instance.
     */
    @Override
    @JsxConstructor({CHROME, EDGE, FF, FF_ESR})
    public void jsConstructor() {
        super.jsConstructor();
    }
}
