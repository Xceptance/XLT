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
package org.htmlunit.javascript.host.dom;

import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_CREATE_HTMLDOCOMENT_REQUIRES_TITLE;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_CORE_3;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_CSS2_1;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_CSS2_3;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_CSS3_1;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_CSS3_2;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_CSS3_3;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_CSS_1;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_CSS_2;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_CSS_3;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_EVENTS_1;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_KEYBOARDEVENTS;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_LS;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_MUTATIONNAMEEVENTS;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_RANGE_1;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_RANGE_3;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_STYLESHEETS;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_SVG_BASICSTRUCTURE_1_2;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_TEXTEVENTS;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_UIEVENTS_2;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_VALIDATION;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_VIEWS_1;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_VIEWS_3;
import static org.htmlunit.BrowserVersionFeatures.JS_DOMIMPLEMENTATION_FEATURE_XPATH;
import static org.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static org.htmlunit.javascript.configuration.SupportedBrowser.EDGE;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF;
import static org.htmlunit.javascript.configuration.SupportedBrowser.FF_ESR;

import java.io.IOException;

import org.htmlunit.StringWebResponse;
import org.htmlunit.WebResponse;
import org.htmlunit.WebWindow;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.parser.HTMLParser;
import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxConstructor;
import org.htmlunit.javascript.configuration.JsxFunction;
import org.htmlunit.javascript.host.html.HTMLDocument;
import org.htmlunit.javascript.host.xml.XMLDocument;
import org.htmlunit.util.UrlUtils;
import org.htmlunit.xml.XmlPage;

/**
 * A JavaScript object for {@code DOMImplementation}.
 *
 * @author Ahmed Ashour
 * @author Frank Danek
 * @author Ronald Brill
 * @author Adam Afeltowicz
 *
 * @see <a href="http://www.w3.org/TR/2000/WD-DOM-Level-1-20000929/level-one-core.html#ID-102161490">
 * W3C Dom Level 1</a>
 */
@JsxClass
public class DOMImplementation extends HtmlUnitScriptable {

    /**
     * Creates an instance.
     */
    public DOMImplementation() {
    }

    /**
     * JavaScript constructor.
     */
    @JsxConstructor({CHROME, EDGE, FF, FF_ESR})
    public void jsConstructor() {
    }

    /**
     * Test if the DOM implementation implements a specific feature.
     * @param feature the name of the feature to test (case-insensitive)
     * @param version the version number of the feature to test
     * @return true if the feature is implemented in the specified version, false otherwise
     */
    @JsxFunction
    public boolean hasFeature(final String feature, final String version) {
        switch (feature) {
            case "Core":
            case "HTML":
            case "XHTML":
            case "XML":
                switch (version) {
                    case "1.0":
                    case "2.0":
                        return true;
                    case "3.0":
                        return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_CORE_3);
                    default:
                }
                break;

            case "Views":
                switch (version) {
                    case "1.0":
                        return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_VIEWS_1);
                    case "2.0":
                        return true;
                    case "3.0":
                        return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_VIEWS_3);
                    default:
                }
                break;

            case "StyleSheets":
                return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_STYLESHEETS);

            case "CSS":
                switch (version) {
                    case "1.0":
                        return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_CSS_1);
                    case "2.0":
                        return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_CSS_2);
                    case "3.0":
                        return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_CSS_3);
                    default:
                }
                break;

            case "CSS2":
                switch (version) {
                    case "1.0":
                        return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_CSS2_1);
                    case "2.0":
                        return true;
                    case "3.0":
                        return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_CSS2_3);
                    default:
                }
                break;

            case "CSS3":
                switch (version) {
                    case "1.0":
                        return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_CSS3_1);
                    case "2.0":
                        return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_CSS3_2);
                    case "3.0":
                        return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_CSS3_3);
                    default:
                }
                break;

            case "Events":
            case "HTMLEvents":
            case "MouseEvents":
            case "MutationEvents":
                switch (version) {
                    case "1.0":
                        return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_EVENTS_1);
                    case "2.0":
                    case "3.0":
                        return true;
                    default:
                }
                break;

            case "UIEvents":
                switch (version) {
                    case "1.0":
                    case "2.0":
                        return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_UIEVENTS_2);
                    case "3.0":
                        return true;
                    default:
                }
                break;

            case "KeyboardEvents":
                return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_KEYBOARDEVENTS);

            case "MutationNameEvents":
                return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_MUTATIONNAMEEVENTS);

            case "TextEvents":
                return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_TEXTEVENTS);

            case "LS":
            case "LS-Async":
                return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_LS);

            case "Range":
            case "Traversal":
                switch (version) {
                    case "1.0":
                        return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_RANGE_1);
                    case "2.0":
                        return true;
                    case "3.0":
                        return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_RANGE_3);
                    default:
                }
                break;

            case "Validation":
                return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_VALIDATION);

            case "XPath":
                return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_XPATH);

            case "http://www.w3.org/TR/SVG11/feature#BasicStructure":
            case "http://www.w3.org/TR/SVG11/feature#Shape":
                switch (version) {
                    case "1.0":
                    case "1.1":
                        return true;
                    case "1.2":
                        return getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_FEATURE_SVG_BASICSTRUCTURE_1_2);
                    default:
                }
                break;

            default:
        }
        //TODO: other features.
        return false;
    }

    /**
     * Creates an {@link XMLDocument}.
     *
     * @param namespaceURI the URI that identifies an XML namespace
     * @param qualifiedName the qualified name of the document to instantiate
     * @param doctype the document types of the document
     * @return the newly created {@link XMLDocument}
     */
    @JsxFunction
    public XMLDocument createDocument(final String namespaceURI, final String qualifiedName,
            final DocumentType doctype) {
        final XMLDocument document = new XMLDocument(getWindow().getWebWindow());
        document.setParentScope(getParentScope());
        document.setPrototype(getPrototype(document.getClass()));
        if (qualifiedName != null && !qualifiedName.isEmpty()) {
            final XmlPage page = (XmlPage) document.getDomNodeOrDie();
            page.appendChild(page.createElementNS("".equals(namespaceURI) ? null : namespaceURI, qualifiedName));
        }
        return document;
    }

    /**
     * Creates an {@link HTMLDocument}.
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/DOMImplementation/createHTMLDocument">
     *   createHTMLDocument (MDN)</a>
     *
     * @param titleObj the document title
     * @return the newly created {@link HTMLDocument}
     */
    @JsxFunction
    public HTMLDocument createHTMLDocument(final Object titleObj) {
        if (JavaScriptEngine.isUndefined(titleObj)
                && getBrowserVersion().hasFeature(JS_DOMIMPLEMENTATION_CREATE_HTMLDOCOMENT_REQUIRES_TITLE)) {
            throw JavaScriptEngine.reportRuntimeError("Title is required");
        }

        // a similar impl is in
        // org.htmlunit.javascript.host.dom.DOMParser.parseFromString(String, Object)
        try {
            final WebWindow webWindow = getWindow().getWebWindow();
            final String html;
            if (JavaScriptEngine.isUndefined(titleObj)) {
                html = "<html><head></head><body></body></html>";
            }
            else {
                html = "<html><head><title>"
                        + JavaScriptEngine.toString(titleObj)
                        + "</title></head><body></body></html>";
            }
            final WebResponse webResponse = new StringWebResponse(html, UrlUtils.URL_ABOUT_BLANK);
            final HtmlPage page = new HtmlPage(webResponse, webWindow);
            // According to spec and behavior of function in browsers new document
            // has no location object and is not connected with any window
            page.setEnclosingWindow(null);

            // document knows the window but is not the windows document
            final HTMLDocument document = new HTMLDocument();
            document.setParentScope(getWindow());
            document.setPrototype(getPrototype(document.getClass()));
            // document.setWindow(getWindow());
            document.setDomNode(page);

            final HTMLParser htmlParser = webWindow.getWebClient().getPageCreator().getHtmlParser();
            htmlParser.parse(webResponse, page, false, false);
            return page.getScriptableObject();
        }
        catch (final IOException e) {
            throw JavaScriptEngine.reportRuntimeError("Parsing failed" + e.getMessage());
        }
    }
}
