/*
 * Copyright (c) 2002-2023 Gargoyle Software Inc.
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

import org.htmlunit.html.HtmlDetails;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.htmlunit.javascript.configuration.JsxSetter;

import org.htmlunit.corejs.javascript.ScriptRuntime;

/**
 * The JavaScript object {@code HTMLDetailsElement}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass(domClass = HtmlDetails.class, value = {CHROME, EDGE, FF, FF_ESR})
public class HTMLDetailsElement extends HTMLElement {

    /**
     * Creates a new instance.
     */
    @JsxConstructor
    public HTMLDetailsElement() {
    }

    /**
     * Returns the {@code open} property.
     * @return the {@code open} property
     */
    @JsxGetter
    public boolean isOpen() {
        return ((HtmlDetails) getDomNodeOrDie()).isOpen();
    }

    /**
     * Sets the open attribute.
     * @param newValue the new value to set
     */
    @JsxSetter
    public void setOpen(final Object newValue) {
        final boolean bool = ScriptRuntime.toBoolean(newValue);
        if (bool) {
            getDomNodeOrDie().setAttribute("open", "");
        }
        else {
            getDomNodeOrDie().removeAttribute("open");
        }
    }
}
