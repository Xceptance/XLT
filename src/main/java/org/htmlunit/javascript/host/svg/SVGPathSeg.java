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

import static org.htmlunit.javascript.configuration.SupportedBrowser.IE;

import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstant;

/**
 * A JavaScript object for {@code SVGPathSeg}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass(IE)
public class SVGPathSeg extends HtmlUnitScriptable {

    /** The constant {@code PATHSEG_UNKNOWN}. */
    @JsxConstant
    public static final int PATHSEG_UNKNOWN = 0;
    /** The constant {@code PATHSEG_CLOSEPATH}. */
    @JsxConstant
    public static final int PATHSEG_CLOSEPATH = 1;
    /** The constant {@code PATHSEG_MOVETO_ABS}. */
    @JsxConstant
    public static final int PATHSEG_MOVETO_ABS = 2;
    /** The constant {@code PATHSEG_MOVETO_REL}. */
    @JsxConstant
    public static final int PATHSEG_MOVETO_REL = 3;
    /** The constant {@code PATHSEG_LINETO_ABS}. */
    @JsxConstant
    public static final int PATHSEG_LINETO_ABS = 4;
    /** The constant {@code PATHSEG_LINETO_REL}. */
    @JsxConstant
    public static final int PATHSEG_LINETO_REL = 5;
    /** The constant {@code PATHSEG_CURVETO_CUBIC_ABS}. */
    @JsxConstant
    public static final int PATHSEG_CURVETO_CUBIC_ABS = 6;
    /** The constant {@code PATHSEG_CURVETO_CUBIC_REL}. */
    @JsxConstant
    public static final int PATHSEG_CURVETO_CUBIC_REL = 7;
    /** The constant {@code PATHSEG_CURVETO_QUADRATIC_ABS}. */
    @JsxConstant
    public static final int PATHSEG_CURVETO_QUADRATIC_ABS = 8;
    /** The constant {@code PATHSEG_CURVETO_QUADRATIC_REL}. */
    @JsxConstant
    public static final int PATHSEG_CURVETO_QUADRATIC_REL = 9;
    /** The constant {@code PATHSEG_ARC_ABS}. */
    @JsxConstant
    public static final int PATHSEG_ARC_ABS = 10;
    /** The constant {@code PATHSEG_ARC_REL}. */
    @JsxConstant
    public static final int PATHSEG_ARC_REL = 11;
    /** The constant {@code PATHSEG_LINETO_HORIZONTAL_ABS}. */
    @JsxConstant
    public static final int PATHSEG_LINETO_HORIZONTAL_ABS = 12;
    /** The constant {@code PATHSEG_LINETO_HORIZONTAL_REL}. */
    @JsxConstant
    public static final int PATHSEG_LINETO_HORIZONTAL_REL = 13;
    /** The constant {@code PATHSEG_LINETO_VERTICAL_ABS}. */
    @JsxConstant
    public static final int PATHSEG_LINETO_VERTICAL_ABS = 14;
    /** The constant {@code PATHSEG_LINETO_VERTICAL_REL}. */
    @JsxConstant
    public static final int PATHSEG_LINETO_VERTICAL_REL = 15;
    /** The constant {@code PATHSEG_CURVETO_CUBIC_SMOOTH_ABS}. */
    @JsxConstant
    public static final int PATHSEG_CURVETO_CUBIC_SMOOTH_ABS = 16;
    /** The constant {@code PATHSEG_CURVETO_CUBIC_SMOOTH_REL}. */
    @JsxConstant
    public static final int PATHSEG_CURVETO_CUBIC_SMOOTH_REL = 17;
    /** The constant {@code PATHSEG_CURVETO_QUADRATIC_SMOOTH_ABS}. */
    @JsxConstant
    public static final int PATHSEG_CURVETO_QUADRATIC_SMOOTH_ABS = 18;
    /** The constant {@code PATHSEG_CURVETO_QUADRATIC_SMOOTH_REL}. */
    @JsxConstant
    public static final int PATHSEG_CURVETO_QUADRATIC_SMOOTH_REL = 19;
}
