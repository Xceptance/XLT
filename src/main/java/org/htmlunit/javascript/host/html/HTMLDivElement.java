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

import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;
import static org.htmlunit.javascript.configuration.SupportedBrowser.IE;

import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlMarquee;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.htmlunit.javascript.configuration.JsxSetter;

/**
 * The JavaScript object {@code HTMLDivElement}.
 *
 * @author Ahmed Ashour
 * @author Daniel Gredler
 * @author Ronald Brill
 */
@JsxClass(domClass = HtmlDivision.class)
@JsxClass(domClass = HtmlMarquee.class, value = {FF, FF_ESR})
public class HTMLDivElement extends HTMLElement {

    /**
     * Creates an instance.
     */
    public HTMLDivElement() {
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
     * Returns the value of the {@code align} property.
     * @return the value of the {@code align} property
     */
    @JsxGetter
    public String getAlign() {
        return getAlign(true);
    }

    /**
     * Sets the value of the {@code align} property.
     * @param align the value of the {@code align} property
     */
    @JsxSetter
    public void setAlign(final String align) {
        setAlign(align, false);
    }

    /**
     * Returns the value of the {@code noWrap} attribute.
     * @return the value of the {@code noWrap} attribute
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms534196.aspx">MSDN Documentation</a>
     */
    @JsxGetter(IE)
    public boolean isNoWrap() {
        return getDomNodeOrDie().hasAttribute("noWrap");
    }

    /**
     * Sets the value of the {@code noWrap} attribute.
     * @param noWrap the value of the {@code noWrap} attribute
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms534196.aspx">MSDN Documentation</a>
     */
    @JsxSetter(IE)
    public void setNoWrap(final boolean noWrap) {
        if (noWrap) {
            getDomNodeOrDie().setAttribute("noWrap", "");
        }
        else {
            getDomNodeOrDie().removeAttribute("noWrap");
        }
    }
}
