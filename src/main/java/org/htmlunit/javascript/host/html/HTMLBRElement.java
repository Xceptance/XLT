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
package org.htmlunit.javascript.host.html;

import static org.htmlunit.BrowserVersionFeatures.JS_CLEAR_RESTRICT;
import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import org.apache.commons.lang3.ArrayUtils;
import org.htmlunit.html.HtmlBreak;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.htmlunit.javascript.configuration.JsxSetter;

/**
 * The JavaScript object {@code HTMLBRElement}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass(domClass = HtmlBreak.class)
public class HTMLBRElement extends HTMLElement {

    /** Valid values for the {@link #getClear() clear} property. */
    private static final String[] VALID_CLEAR_VALUES = {"left", "right", "all", "none"};

    /**
     * Creates an instance.
     */
    public HTMLBRElement() {
    }

    /**
     * JavaScript constructor.
     */
    @Override
    @JsxConstructor({CHROME, EDGE, FF, FF_ESR})
    public void jsConstructor() {
        super.jsConstructor();
    }

    /**
     * Returns the value of the {@code clear} property.
     * @return the value of the {@code clear} property
     */
    @JsxGetter
    public String getClear() {
        final String clear = getDomNodeOrDie().getAttributeDirect("clear");
        if (getBrowserVersion().hasFeature(JS_CLEAR_RESTRICT) && !ArrayUtils.contains(VALID_CLEAR_VALUES, clear)) {
            return "";
        }
        return clear;
    }

    /**
     * Sets the value of the {@code clear} property.
     * @param clear the value of the {@code clear} property
     */
    @JsxSetter
    public void setClear(final String clear) {
        if (getBrowserVersion().hasFeature(JS_CLEAR_RESTRICT) && !ArrayUtils.contains(VALID_CLEAR_VALUES, clear)) {
            throw JavaScriptEngine.reportRuntimeError("Invalid clear property value: '" + clear + "'.");
        }
        getDomNodeOrDie().setAttribute("clear", clear);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEndTagForbidden() {
        return true;
    }
}
