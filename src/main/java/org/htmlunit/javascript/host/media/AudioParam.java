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
package org.htmlunit.javascript.host.media;

import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.htmlunit.javascript.configuration.JsxSetter;

/**
 * A JavaScript object for {@code AudioParam}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass({CHROME, EDGE, FF, FF_ESR})
public class AudioParam extends HtmlUnitScriptable {

    private double value_;

    /**
     * Creates a new instance.
     */
    public AudioParam() {
    }

    /**
     * Creates a new instance.
     */
    @JsxConstructor
    public void jsConstructor() {
        value_ = getDefaultValue();
    }

    /**
     * @return the value
     */
    @JsxGetter
    public double getValue() {
        return value_;
    }

    /**
     * @param value the value
     */
    @JsxSetter
    public void setValue(final double value) {
        value_ = value;
    }

    /**
     * @return the defaultValue
     */
    @JsxGetter
    public double getDefaultValue() {
        return 1;
    }

    /**
     * @return the maxValue
     */
    @JsxGetter
    public double getMaxValue() {
        return 3.4028234663852886e+38;
    }

    /**
     * @return the minValue
     */
    @JsxGetter
    public double getMinValue() {
        return -3.4028234663852886e+38;
    }
}
