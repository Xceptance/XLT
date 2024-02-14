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

import static org.htmlunit.BrowserVersionFeatures.JS_HTML_HYPHEN_ELEMENT_CLASS_NAME;
import static org.htmlunit.BrowserVersionFeatures.JS_HTML_RUBY_ELEMENT_CLASS_NAME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import org.htmlunit.Page;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlRb;
import org.htmlunit.html.HtmlRp;
import org.htmlunit.html.HtmlRt;
import org.htmlunit.html.HtmlRtc;
import org.htmlunit.html.HtmlRuby;
import org.htmlunit.html.HtmlUnknownElement;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.xml.XmlPage;

/**
 * The JavaScript object {@code HTMLUnknownElement}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@JsxClass(domClass = HtmlUnknownElement.class)
public class HTMLUnknownElement extends HTMLElement {

    /**
     * Creates an instance.
     */
    public HTMLUnknownElement() {
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
     * Gets the JavaScript property {@code nodeName} for the current node.
     * @return the node name
     */
    @Override
    public String getNodeName() {
        final HtmlElement elem = getDomNodeOrDie();
        final Page page = elem.getPage();
        if (page instanceof XmlPage) {
            return elem.getLocalName();
        }
        return super.getNodeName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClassName() {
        if (getWindow().getWebWindow() != null) {
            final HtmlElement element = getDomNodeOrNull();
            if (element != null) {
                final String name = element.getNodeName();
                if (getBrowserVersion().hasFeature(JS_HTML_RUBY_ELEMENT_CLASS_NAME)
                        && (HtmlRb.TAG_NAME.equals(name)
                                || HtmlRp.TAG_NAME.equals(name)
                                || HtmlRt.TAG_NAME.equals(name)
                                || HtmlRtc.TAG_NAME.equals(name)
                                || HtmlRuby.TAG_NAME.equals(name))) {
                    return "HTMLElement";
                }

                if (name.indexOf('-') != -1
                    && getBrowserVersion().hasFeature(JS_HTML_HYPHEN_ELEMENT_CLASS_NAME)) {
                    return "HTMLElement";
                }
            }
        }
        return super.getClassName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isLowerCaseInOuterHtml() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEndTagForbidden() {
        if ("BGSOUND".equals(getNodeName())) {
            return true;
        }
        return super.isEndTagForbidden();
    }
}
