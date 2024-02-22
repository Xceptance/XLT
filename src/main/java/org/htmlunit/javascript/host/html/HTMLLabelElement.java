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

import static org.htmlunit.BrowserVersionFeatures.JS_LABEL_FORM_OF_SELF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlLabel;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxGetter;
import org.htmlunit.javascript.configuration.JsxSetter;

/**
 * A JavaScript object for {@code HTMLLabelElement}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Frank Danek
 */
@JsxClass(domClass = HtmlLabel.class)
public class HTMLLabelElement extends HTMLElement {

    /**
     * Creates an instance.
     */
    public HTMLLabelElement() {
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
     * Retrieves the object to which the given label object is assigned.
     * @return the identifier of the element to which the label element is assigned
     */
    @JsxGetter
    public String getHtmlFor() {
        return ((HtmlLabel) getDomNodeOrDie()).getForAttribute();
    }

    /**
     * Sets or retrieves the object to which the given label object is assigned.
     * @param id Specifies the identifier of the element to which the label element is assigned
     * @see <a href="http://msdn2.microsoft.com/en-us/library/ms533872.aspx">MSDN Documentation</a>
     */
    @JsxSetter
    public void setHtmlFor(final String id) {
        getDomNodeOrDie().setAttribute("for", id);
    }

    /**
     * @return the HTMLElement labeled by the given label object
     */
    @JsxGetter({CHROME, EDGE, FF, FF_ESR})
    public HTMLElement getControl() {
        final HtmlLabel label = (HtmlLabel) getDomNodeOrDie();
        final HtmlElement labeledElement = label.getLabeledElement();

        if (labeledElement == null) {
            return null;
        }

        return (HTMLElement) getScriptableFor(labeledElement);
    }

    /**
     * Returns the value of the JavaScript {@code form} attribute.
     *
     * @return the value of the JavaScript {@code form} attribute
     */
    @JsxGetter
    @Override
    public HTMLFormElement getForm() {
        if (getBrowserVersion().hasFeature(JS_LABEL_FORM_OF_SELF)) {
            final HtmlForm form = getDomNodeOrDie().getEnclosingForm();
            if (form == null) {
                return null;
            }
            return (HTMLFormElement) getScriptableFor(form);
        }

        final HtmlLabel label = (HtmlLabel) getDomNodeOrDie();
        final HtmlElement labeledElement = label.getLabeledElement();

        if (labeledElement == null) {
            return null;
        }

        final HtmlForm form = labeledElement.getEnclosingForm();
        if (form == null) {
            return null;
        }

        return (HTMLFormElement) getScriptableFor(form);
    }
}
