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

import static org.htmlunit.BrowserVersionFeatures.HTMLBASE_HREF_DEFAULT_EMPTY;
import static org.htmlunit.html.DomElement.ATTRIBUTE_NOT_DEFINED;
import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import org.htmlunit.html.HtmlBase;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.htmlunit.javascript.configuration.JsxSetter;

/**
 * The JavaScript object {@code HTMLBaseElement}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass(domClass = HtmlBase.class)
public class HTMLBaseElement extends HTMLElement {

    /**
     * The constructor.
     */
    public HTMLBaseElement() {
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
     * Returns the value of the {@code href} property.
     * @return the value of the {@code href} property
     */
    @JsxGetter
    public String getHref() {
        final String href = getDomNodeOrDie().getAttributeDirect("href");
        if (ATTRIBUTE_NOT_DEFINED == href) {
            if (getBrowserVersion().hasFeature(HTMLBASE_HREF_DEFAULT_EMPTY)) {
                return href;
            }
            return getWindow().getLocation().getHref();
        }
        return href;
    }

    /**
     * Sets the value of the {@code href} property.
     * @param href the value of the {@code href} property
     */
    @JsxSetter
    public void setHref(final String href) {
        getDomNodeOrDie().setAttribute("href", href);
    }

    /**
     * Returns the value of the {@code target} property.
     * @return the value of the {@code target} property
     */
    @JsxGetter
    public String getTarget() {
        return getDomNodeOrDie().getAttributeDirect("target");
    }

    /**
     * Sets the value of the {@code target} property.
     * @param target the value of the {@code target} property
     */
    @JsxSetter
    public void setTarget(final String target) {
        getDomNodeOrDie().setAttribute("target", target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEndTagForbidden() {
        return true;
    }
}
