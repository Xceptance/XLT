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

import static org.htmlunit.BrowserVersionFeatures.JS_TABLE_COLUMN_WIDTH_NO_NEGATIVE_VALUES;
import static org.htmlunit.BrowserVersionFeatures.JS_TABLE_COLUMN_WIDTH_NULL_STRING;
import static org.htmlunit.BrowserVersionFeatures.JS_TABLE_SPAN_THROWS_EXCEPTION_IF_INVALID;
import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import org.htmlunit.html.HtmlTableColumn;
import org.htmlunit.html.HtmlTableColumnGroup;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.htmlunit.javascript.configuration.JsxSetter;

/**
 * The JavaScript object {@code HTMLTableColElement}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass(domClass = HtmlTableColumn.class)
@JsxClass(domClass = HtmlTableColumnGroup.class)
public class HTMLTableColElement extends HTMLTableComponent {

    /**
     * Creates an instance.
     */
    public HTMLTableColElement() {
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
     * Returns the value of the {@code span} property.
     * @return the value of the {@code span} property
     */
    @JsxGetter
    public int getSpan() {
        final String span = getDomNodeOrDie().getAttributeDirect("span");
        int i;
        try {
            i = Integer.parseInt(span);
            if (i < 1) {
                i = 1;
            }
        }
        catch (final NumberFormatException e) {
            i = 1;
        }
        return i;
    }

    /**
     * Sets the value of the {@code span} property.
     * @param span the value of the {@code span} property
     */
    @JsxSetter
    public void setSpan(final Object span) {
        final double d = JavaScriptEngine.toNumber(span);
        int i = (int) d;
        if (i < 1) {
            if (getBrowserVersion().hasFeature(JS_TABLE_SPAN_THROWS_EXCEPTION_IF_INVALID)) {
                final Exception e = new Exception("Cannot set the span property to invalid value: " + span);
                throw JavaScriptEngine.throwAsScriptRuntimeEx(e);
            }
            i = 1;
        }
        getDomNodeOrDie().setAttribute("span", Integer.toString(i));
    }

    /**
     * Returns the value of the {@code width} property.
     * @return the value of the {@code width} property
     */
    @JsxGetter(propertyName = "width")
    public String getWidth_js() {
        final boolean ie = getBrowserVersion().hasFeature(JS_TABLE_COLUMN_WIDTH_NO_NEGATIVE_VALUES);
        final Boolean returnNegativeValues = ie ? Boolean.FALSE : null;
        return getWidthOrHeight("width", returnNegativeValues);
    }

    /**
     * Sets the value of the {@code width} property.
     * @param width the value of the {@code width} property
     */
    @JsxSetter(propertyName = "width")
    public void setWidth_js(final Object width) {
        final String value;
        if (width == null && !getBrowserVersion().hasFeature(JS_TABLE_COLUMN_WIDTH_NULL_STRING)) {
            value = "";
        }
        else {
            value = JavaScriptEngine.toString(width);
        }
        setWidthOrHeight("width", value, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEndTagForbidden() {
        return getDomNodeOrDie() instanceof HtmlTableColumn;
    }

    /**
     * Overwritten to throw an exception.
     * @param value the new value for replacing this node
     */
    @Override
    public void setOuterHTML(final Object value) {
        throw JavaScriptEngine.reportRuntimeError("outerHTML is read-only for tag '"
                            + getDomNodeOrDie().getNodeName() + "'");
    }
}
