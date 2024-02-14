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
package org.htmlunit.javascript.host.canvas.ext;

import static org.htmlunit.javascript.configuration.SupportedBrowser.IE;

import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstant;

/**
 * A JavaScript object for {@code ANGLE_instanced_arrays}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass(IE)
public class ANGLE_instanced_arrays extends HtmlUnitScriptable {

    /** The constant {@code VERTEX_ATTRIB_ARRAY_DIVISOR_ANGLE}. */
    @JsxConstant
    public static final int VERTEX_ATTRIB_ARRAY_DIVISOR_ANGLE = 35_070;

    /**
     * Default constructor.
     */
    public ANGLE_instanced_arrays() {
    }
}
