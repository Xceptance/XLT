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
import org.htmlunit.svg.SvgFeDisplacementMap;

/**
 * A JavaScript object for {@code SVGFEDisplacementMapElement}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass(domClass = SvgFeDisplacementMap.class)
public class SVGFEDisplacementMapElement extends SVGElement {

    /** The constant {@code SVG_CHANNEL_UNKNOWN}. */
    @JsxConstant
    public static final int SVG_CHANNEL_UNKNOWN = 0;
    /** The constant {@code SVG_CHANNEL_R}. */
    @JsxConstant
    public static final int SVG_CHANNEL_R = 1;
    /** The constant {@code SVG_CHANNEL_G}. */
    @JsxConstant
    public static final int SVG_CHANNEL_G = 2;
    /** The constant {@code SVG_CHANNEL_B}. */
    @JsxConstant
    public static final int SVG_CHANNEL_B = 3;
    /** The constant {@code SVG_CHANNEL_A}. */
    @JsxConstant
    public static final int SVG_CHANNEL_A = 4;

    /**
     * Creates an instance.
     */
    public SVGFEDisplacementMapElement() {
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
