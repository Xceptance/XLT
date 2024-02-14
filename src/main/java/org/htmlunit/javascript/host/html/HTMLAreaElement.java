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

import static org.htmlunit.BrowserVersionFeatures.JS_AREA_WITHOUT_HREF_FOCUSABLE;
import static org.htmlunit.html.DomElement.ATTRIBUTE_NOT_DEFINED;
import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import org.htmlunit.html.HtmlArea;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.htmlunit.javascript.configuration.JsxSetter;
import org.htmlunit.javascript.host.dom.DOMTokenList;

/**
 * The JavaScript object {@code HTMLAreaElement}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Frank Danek
 */
@JsxClass(domClass = HtmlArea.class)
public class HTMLAreaElement extends HTMLElement {

    /**
     * The constructor.
     */
    public HTMLAreaElement() {
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
     * Calls for instance for implicit conversion to string.
     * @see org.htmlunit.javascript.HtmlUnitScriptable#getDefaultValue(java.lang.Class)
     * @param hint the type hint
     * @return the default value
     */
    @Override
    public Object getDefaultValue(final Class<?> hint) {
        final HtmlElement element = getDomNodeOrNull();
        if (element == null) {
            return super.getDefaultValue(null);
        }
        return HTMLAnchorElement.getDefaultValue(element);
    }

    /**
     * Returns the value of the {@code alt} property.
     * @return the value of the {@code alt} property
     */
    @JsxGetter
    public String getAlt() {
        return getDomNodeOrDie().getAttributeDirect("alt");
    }

    /**
     * Returns the value of the {@code alt} property.
     * @param alt the value
     */
    @JsxSetter
    public void setAlt(final String alt) {
        getDomNodeOrDie().setAttribute("alt", alt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEndTagForbidden() {
        return true;
    }

    /**
     * Returns the value of the {@code rel} property.
     * @return the value of the {@code rel} property
     */
    @JsxGetter
    public String getRel() {
        return getDomNodeOrDie().getAttributeDirect("rel");
    }

    /**
     * Returns the value of the {@code rel} property.
     * @param rel the value
     */
    @JsxSetter
    public void setRel(final String rel) {
        getDomNodeOrDie().setAttribute("rel", rel);
    }

    /**
     * Returns the {@code relList} attribute.
     * @return the {@code relList} attribute
     */
    @JsxGetter({CHROME, EDGE, FF, FF_ESR})
    public DOMTokenList getRelList() {
        return new DOMTokenList(this, "rel");
    }

    /**
     * Sets the focus to this element.
     */
    @Override
    public void focus() {
        // in reality this depends also on the visibility of the area itself
        final HtmlArea area = (HtmlArea) getDomNodeOrDie();
        final String hrefAttr = area.getHrefAttribute();

        if (ATTRIBUTE_NOT_DEFINED != hrefAttr
                || getBrowserVersion().hasFeature(JS_AREA_WITHOUT_HREF_FOCUSABLE)) {
            area.focus();
        }
    }

    /**
     * Returns the {@code coords} attribute.
     * @return the {@code coords} attribute
     */
    @JsxGetter
    public String getCoords() {
        return getDomNodeOrDie().getAttributeDirect("coords");
    }

    /**
     * Sets the {@code coords} attribute.
     * @param coords {@code coords} attribute
     */
    @JsxSetter
    public void setCoords(final String coords) {
        getDomNodeOrDie().setAttribute("coords", coords);
    }

}
