/*
 * Copyright (c) 2002-2025 Gargoyle Software Inc.
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

import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstant;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.svg.SvgFeMorphology;

/**
 * A JavaScript object for {@code SVGFEMorphologyElement}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass(domClass = SvgFeMorphology.class)
public class SVGFEMorphologyElement extends SVGElement {

    /** The constant {@code SVG_MORPHOLOGY_OPERATOR_UNKNOWN}. */
    @JsxConstant
    public static final int SVG_MORPHOLOGY_OPERATOR_UNKNOWN = 0;
    /** The constant {@code SVG_MORPHOLOGY_OPERATOR_ERODE}. */
    @JsxConstant
    public static final int SVG_MORPHOLOGY_OPERATOR_ERODE = 1;
    /** The constant {@code SVG_MORPHOLOGY_OPERATOR_DILATE}. */
    @JsxConstant
    public static final int SVG_MORPHOLOGY_OPERATOR_DILATE = 2;

    /**
     * Creates an instance.
     */
    @Override
    @JsxConstructor
    public void jsConstructor() {
        super.jsConstructor();
    }
}
