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
package org.htmlunit.activex.javascript.msxml;

import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.ACTIVEX_CHECK;
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.CREATE_XMLDOMDOCUMENT_FUNCTION;
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.callCreateXMLDOMDocument;
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.createTestHTML;

import org.htmlunit.WebDriverTestCase;
import org.htmlunit.junit.BrowserRunner;
import org.htmlunit.junit.BrowserRunner.Alerts;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link XMLDOMImplementation}.
 *
 * @author Ahmed Ashour
 * @author Marc Guillemot
 * @author Frank Danek
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class XMLDOMImplementationTest extends WebDriverTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "[object Object]")
    public void scriptableToString() throws Exception {
        final String html =  LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "    log(Object.prototype.toString.call(doc.implementation));\n"
            + "  }\n"
            + CREATE_XMLDOMDOCUMENT_FUNCTION;

        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "exception")
    public void hasFeature_featureNull() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "    try {\n"
            + "      doc.implementation.hasFeature(null, '1.0');\n"
            + "    } catch(e) { log('exception'); }\n"
            + "  }\n"
            + CREATE_XMLDOMDOCUMENT_FUNCTION;

        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "1.0: false")
    public void hasFeature_featureEmpty() throws Exception {
        hasFeature("", "['1.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "exception")
    public void hasFeature_versionNull() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "    try {\n"
            + "      doc.implementation.hasFeature('MS-DOM', null);\n"
            + "    } catch(e) { log('exception'); }\n"
            + "  }\n"
            + CREATE_XMLDOMDOCUMENT_FUNCTION;

        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "MS-DOM : false")
    public void hasFeature_versionEmpty() throws Exception {
        hasFeature("MS-DOM", "['']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"Core 1.0: false", "Core 2.0: false", "Core 3.0: false"})
    public void hasFeature_Core() throws Exception {
        hasFeature("Core", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"HTML 1.0: false", "HTML 2.0: false", "HTML 3.0: false"})
    public void hasFeature_HTML() throws Exception {
        hasFeature("HTML", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"XML 1.0: true", "XML 2.0: false", "XML 3.0: false"})
    public void hasFeature_XML() throws Exception {
        hasFeature("XML", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"XHTML 1.0: false", "XHTML 2.0: false", "XHTML 3.0: false"})
    public void hasFeature_XHTML() throws Exception {
        hasFeature("XHTML", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"Views 1.0: false", "Views 2.0: false", "Views 3.0: false"})
    public void hasFeature_Views() throws Exception {
        hasFeature("Views", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"StyleSheets 1.0: false", "StyleSheets 2.0: false", "StyleSheets 3.0: false"})
    public void hasFeature_StyleSheets() throws Exception {
        hasFeature("StyleSheets", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"CSS 1.0: false", "CSS 2.0: false", "CSS 3.0: false"})
    public void hasFeature_CSS() throws Exception {
        hasFeature("CSS", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"CSS2 1.0: false", "CSS2 2.0: false", "CSS2 3.0: false"})
    public void hasFeature_CSS2() throws Exception {
        hasFeature("CSS2", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"CSS3 1.0: false", "CSS3 2.0: false", "CSS3 3.0: false"})
    public void hasFeature_CSS3() throws Exception {
        hasFeature("CSS3", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"Events 1.0: false", "Events 2.0: false", "Events 3.0: false"})
    public void hasFeature_Events() throws Exception {
        hasFeature("Events", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"UIEvents 1.0: false", "UIEvents 2.0: false", "UIEvents 3.0: false"})
    public void hasFeature_UIEvents() throws Exception {
        hasFeature("UIEvents", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"MouseEvents 1.0: false", "MouseEvents 2.0: false", "MouseEvents 3.0: false"})
    public void hasFeature_MouseEvents() throws Exception {
        hasFeature("MouseEvents", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"TextEvents 1.0: false", "TextEvents 2.0: false", "TextEvents 3.0: false"})
    public void hasFeature_TextEvents() throws Exception {
        hasFeature("TextEvents", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"KeyboardEvents 1.0: false", "KeyboardEvents 2.0: false", "KeyboardEvents 3.0: false"})
    public void hasFeature_KeyboardEvents() throws Exception {
        hasFeature("KeyboardEvents", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"MutationEvents 1.0: false", "MutationEvents 2.0: false", "MutationEvents 3.0: false"})
    public void hasFeature_MutationEvents() throws Exception {
        hasFeature("MutationEvents", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"MutationNameEvents 1.0: false",
                  "MutationNameEvents 2.0: false",
                  "MutationNameEvents 3.0: false"})
    public void hasFeature_MutationNameEvents() throws Exception {
        hasFeature("MutationNameEvents", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"HTMLEvents 1.0: false", "HTMLEvents 2.0: false", "HTMLEvents 3.0: false"})
    public void hasFeature_HTMLEvents() throws Exception {
        hasFeature("HTMLEvents", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"Range 1.0: false", "Range 2.0: false", "Range 3.0: false"})
    public void hasFeature_Range() throws Exception {
        hasFeature("Range", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"Traversal 1.0: false", "Traversal 2.0: false", "Traversal 3.0: false"})
    public void hasFeature_Traversal() throws Exception {
        hasFeature("Traversal", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"LS 1.0: false", "LS 2.0: false", "LS 3.0: false"})
    public void hasFeature_LS() throws Exception {
        hasFeature("LS", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"LS-Async 1.0: false", "LS-Async 2.0: false", "LS-Async 3.0: false"})
    public void hasFeature_LSAsync() throws Exception {
        hasFeature("LS-Async", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"Validation 1.0: false", "Validation 2.0: false", "Validation 3.0: false"})
    public void hasFeature_Validation() throws Exception {
        hasFeature("Validation", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"XPath 1.0: false", "XPath 2.0: false", "XPath 3.0: false"})
    public void hasFeature_XPath() throws Exception {
        hasFeature("XPath", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"http://www.w3.org/TR/SVG11/feature#BasicStructure 1.0: false",
                  "http://www.w3.org/TR/SVG11/feature#BasicStructure 1.1: false",
                  "http://www.w3.org/TR/SVG11/feature#BasicStructure 1.2: false"})
    public void hasFeature_SVG_BasicStructure() throws Exception {
        hasFeature("http://www.w3.org/TR/SVG11/feature#BasicStructure", "['1.0', '1.1', '1.2']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"http://www.w3.org/TR/SVG11/feature#Shape 1.0: false",
                  "http://www.w3.org/TR/SVG11/feature#Shape 1.1: false",
                  "http://www.w3.org/TR/SVG11/feature#Shape 1.2: false"})
    public void hasFeature_SVG_Shape() throws Exception {
        hasFeature("http://www.w3.org/TR/SVG11/feature#Shape", "['1.0', '1.1', '1.2']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"DOM 1.0: true", "DOM 2.0: false", "DOM 3.0: false"})
    public void hasFeature_DOM() throws Exception {
        hasFeature("DOM", "['1.0', '2.0', '3.0']");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"MS-DOM 1.0: true", "MS-DOM 2.0: true", "MS-DOM 3.0: false"})
    public void hasFeature_MSDOM() throws Exception {
        hasFeature("MS-DOM", "['1.0', '2.0', '3.0']");
    }

    private void hasFeature(final String feature, final String versions) throws Exception {
        final String html =
            LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "    var feature = '" + feature + "';\n"
            + "    var versions = " + versions + ";\n"
            + "    for (var j = 0; j < versions.length; j++) {\n"
            + "      var version = versions[j];\n"
            + "      log(feature + ' ' + version + ': ' + doc.implementation.hasFeature(feature, version));\n"
            + "    }\n"
            + "  }\n"
            + CREATE_XMLDOMDOCUMENT_FUNCTION;

        loadPageVerifyTitle2(createTestHTML(html));
    }
}
