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
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION;
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.callCreateXMLDOMDocument;
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.callLoadXMLDOMDocumentFromURL;
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.createTestHTML;

import org.htmlunit.WebDriverTestCase;
import org.htmlunit.junit.BrowserRunner;
import org.htmlunit.junit.BrowserRunner.Alerts;
import org.htmlunit.util.MimeType;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link XMLDOMElement}.
 *
 * @author Ahmed Ashour
 * @author Marc Guillemot
 * @author Ronald Brill
 * @author Frank Danek
 */
@RunWith(BrowserRunner.class)
public class XMLDOMElementTest extends WebDriverTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "[object Object]")
    public void scriptableToString() throws Exception {
        tester("log(Object.prototype.toString.call(root));\n", "<root/>");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"true", "0", "1"})
    public void attributes() throws Exception {
        final String html = ""
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "    var element = doc.createElement('something');\n"
            + "    try {\n"
            + "      log(element.attributes != null);\n"
            + "      log(element.attributes.length);\n"
            + "      element.setAttribute('attr', 'test');\n"
            + "      log(element.attributes.length);\n"
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
            IE = {"div", "dIv", "div", "div"})
    public void baseName() throws Exception {
        xml("baseName");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"div", "dIv", "div", "dIv"})
    public void baseName_namespace() throws Exception {
        namespace("baseName");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"div", "dIv"})
    public void baseName_created() throws Exception {
        created("baseName");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"4", "#cdata-section=child-cdata", "#comment=child-comment", "child-element=null", "#text=child-text"})
    public void childNodes() throws Exception {
        final String test = ""
            + "log(root.childNodes.length);\n"
            // cdata
            + "debug(root.childNodes[0]);\n"
            // comment
            + "debug(root.childNodes[1]);\n"
            // element
            + "debug(root.childNodes[2]);\n"
            // text
            + "debug(root.childNodes[3]);\n";

        final String xml = ""
            + "<root child-attribute='test'>"
            + "<![CDATA[child-cdata]]>"
            + "<!--child-comment-->"
            + "<child-element/>"
            + "child-text"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"2", "child-element=null", "child-element=null"})
    public void childNodes_lineBreak() throws Exception {
        final String test = ""
            + "log(root.childNodes.length);\n"
            + "debug(root.childNodes[0]);\n"
            + "debug(root.childNodes[1]);\n";

        final String xml = ""
            + "<root>"
            + "<child-element/>\n"
            + "<child-element/>"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "0")
    public void childNodes_none() throws Exception {
        tester("log(root.childNodes.length);\n", "<root/>");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"null", "null", "null", "null"})
    public void dataType() throws Exception {
        xml("dataType");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"null", "null", "null", "null"})
    public void dataType_namespace() throws Exception {
        namespace("dataType");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"null", "null"})
    public void dataType_created() throws Exception {
        created("dataType");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"null", "null", "null", "null"})
    public void definition() throws Exception {
        xml("definition");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"null", "null", "null", "null"})
    public void definition_namespace() throws Exception {
        namespace("definition");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"null", "null"})
    public void definition_created() throws Exception {
        created("definition");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "#cdata-section=child-cdata")
    public void firstChild() throws Exception {
        final String xml = ""
            + "<root child-attribute='test'>"
            + "<![CDATA[child-cdata]]>"
            + "<!--child-comment-->"
            + "<child-element/>"
            + "child-text"
            + "</root>";

        tester("debug(root.firstChild);\n", xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "null")
    public void firstChild_none() throws Exception {
        tester("debug(root.firstChild);\n", "<root/>");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "#text=child-text")
    public void lastChild() throws Exception {
        final String xml = ""
            + "<root child-attribute='test'>"
            + "<![CDATA[child-cdata]]>"
            + "<!--child-comment-->"
            + "<child-element/>"
            + "child-text"
            + "</root>";

        tester("debug(root.lastChild);\n", xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "null")
    public void lastChild_none() throws Exception {
        tester("debug(root.lastChild);\n", "<root/>");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"", "", "", ""})
    public void namespaceURI() throws Exception {
        xml("namespaceURI");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"http://www.w3.org/1999/xhtml",
                  "http://www.w3.org/1999/xhtml",
                  "http://www.appcelerator.org",
                  "http://www.appcelerator.org"})
    public void namespaceURI_namespace() throws Exception {
        namespace("namespaceURI");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"", ""})
    public void namespaceURI_created() throws Exception {
        created("namespaceURI");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"div", "dIv", "div", "div"})
    public void nodeName() throws Exception {
        xml("nodeName");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"div", "dIv", "app:div", "app:dIv"})
    public void nodeName_namespace() throws Exception {
        namespace("nodeName");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"div", "dIv"})
    public void nodeName_created() throws Exception {
        created("nodeName");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"1", "1", "1", "1"})
    public void nodeType() throws Exception {
        xml("nodeType");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"1", "1", "1", "1"})
    public void nodeType_namespace() throws Exception {
        namespace("nodeType");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"1", "1"})
    public void nodeType_created() throws Exception {
        created("nodeType");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"null", "exception-setNull", "exception-setEmpty", "exception-set"})
    public void nodeValue() throws Exception {
        final String test = ""
            + "log(root.nodeValue);\n"
            + "try {\n"
            + "  root.nodeValue = null;\n"
            + "} catch(e) { log('exception-setNull'); }\n"
            + "try {\n"
            + "  root.nodeValue = '';\n"
            + "} catch(e) { log('exception-setEmpty'); }\n"
            + "try {\n"
            + "  root.nodeValue = 'test';\n"
            + "} catch(e) { log('exception-set'); }\n";

        final String xml = ""
            + "<root child-attribute='test'>"
            + "<![CDATA[child-cdata]]>"
            + "<!--child-comment-->"
            + "<child-element/>"
            + "child-text"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "true")
    public void ownerDocument() throws Exception {
        tester("log(root.ownerDocument === doc);\n", "<root/>");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "true")
    public void ownerDocument_created() throws Exception {
        final String html = ""
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "    var element = doc.createElement('something');\n"
            + "    try {\n"
            + "      log(element.ownerDocument === doc);\n"
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
            IE = {"#document=null", "true"})
    public void parentNode() throws Exception {
        final String test = ""
            + "debug(root.parentNode);\n"
            + "log(root === root.childNodes[0].parentNode);\n";

        final String xml = ""
            + "<root>"
            + "<child-element/>"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "true")
    public void parentNode_created() throws Exception {
        final String html = ""
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "    var element = doc.createElement('something');\n"
            + "    try {\n"
            + "      log(element.parentNode == null);\n"
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
            IE = {"", "", "", ""})
    public void prefix() throws Exception {
        xml("prefix");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"", "", "app", "app"})
    public void prefix_namespace() throws Exception {
        namespace("prefix");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"", ""})
    public void prefix_created() throws Exception {
        created("prefix");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"div", "dIv", "div", "div"})
    public void tagName() throws Exception {
        xml("tagName");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"div", "dIv", "app:div", "app:dIv"})
    public void tagName_namespace() throws Exception {
        namespace("tagName");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"div", "dIv"})
    public void tagName_created() throws Exception {
        created("tagName");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "child-cdatagrand-child-textchild-text")
    public void text() throws Exception {
        final String xml = ""
            + "<root child-attribute='test'>"
            + "<![CDATA[child-cdata]]>"
            + "<!--child-comment-->"
            + "<child-element/>"
            + "<child-element2>grand-child-text</child-element2>"
            + "child-text"
            + "</root>";

        tester("log(root.text);\n", xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"5", "child-cdatagrand-child-textchild-text", "1", "test", "", "exception-setNull"})
    public void text_set() throws Exception {
        final String test = ""
            + "log(root.childNodes.length);\n"
            + "log(root.text);\n"
            // normal
            + "root.text = 'test';\n"
            + "log(root.childNodes.length);\n"
            + "log(root.text);\n"
            // empty
            + "root.text = '';\n"
            + "log(root.text);\n"
            // null
            + "try {\n"
            + "  root.text = null;\n"
            + "} catch(e) { log('exception-setNull'); }\n";

        final String xml = ""
            + "<root child-attribute='test'>"
            + "<![CDATA[child-cdata]]>"
            + "<!--child-comment-->"
            + "<child-element/>"
            + "<child-element2>grand-child-text</child-element2>"
            + "child-text"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "child-cdata\\n"
                        + " grand-child-text\\n\\nchild-text")
    public void text_lineBreak() throws Exception {
        final String xml = ""
            + "<root child-attribute='test'>\n"
            + "<![CDATA[child-cdata\n]]>\n"
            + "<!--child-comment-->\n"
            + "<child-element/>\n"
            + "<child-element2>grand-child-text\n</child-element2>\n"
            + "child-text\n"
            + "</root>";

        tester("var txt = root.text; txt = txt.replace(/\\n/g, '\\\\n'); log(txt);", xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "")
    public void text_created() throws Exception {
        final String html = ""
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "    var element = doc.createElement('something');\n"
            + "    try {\n"
            + "      log(element.text);\n"
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
            IE = "<root child-attribute=\"test\"><![CDATA[child-cdata]]><!--child-comment--><child-element/>"
                + "<child-element2><grand-child-element/></child-element2>child-text</root>")
    public void xml() throws Exception {
        final String xml = ""
            + "<root child-attribute='test'>"
            + "<![CDATA[child-cdata]]>"
            + "<!--child-comment-->"
            + "<child-element/>"
            + "<child-element2><grand-child-element/></child-element2>"
            + "child-text"
            + "</root>";

        tester("log(root.xml);\n", xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "<root child-attribute=\"test\">\\r\\n"
                        + "\\t<![CDATA[child-cdata]]>\\r\\n"
                        + "\\t<!--child-comment-->\\r\\n"
                        + "\\t<child-element/>\\r\\n"
                        + "\\t<child-element2><grand-child-element/></child-element2>\\r\\n"
                        + "child-text\\r\\n"
                        + "</root>")
    public void xml_lineBreak() throws Exception {
        final String xml = ""
            + "<root child-attribute='test'>\n"
            + "<![CDATA[child-cdata]]>\n"
            + "<!--child-comment-->\n"
            + "<child-element/>\n"
            + "<child-element2><grand-child-element/></child-element2>\n"
            + "child-text\n"
            + "</root>";

        final String tester =
                        "var txt = root.xml;\n"
                        + "txt = txt.replace(/\\t/g, '\\\\t');\n"
                        + "txt = txt.replace(/\\r/g, '\\\\r');\n"
                        + "txt = txt.replace(/\\n/g, '\\\\n');\n"
                        + "log(txt);\n";
        tester(tester, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "<something/>")
    public void xml_created() throws Exception {
        final String html = ""
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "    var element = doc.createElement('something');\n"
            + "    try {\n"
            + "      log(element.xml);\n"
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
            IE = {"test", "null", "null", "exception-getNull", "exception-getEmpty"})
    public void getAttribute() throws Exception {
        final String test = ""
            + "var element = root.childNodes[0];\n"
            // normal
            + "log(element.getAttribute('attr'));\n"
            // case-sensitive
            + "log(element.getAttribute('AttR'));\n"
            // unknown
            + "log(element.getAttribute('unknown'));\n"
            // null
            + "try {\n"
            + "  element.getAttribute(null);\n"
            + "} catch(e) { log('exception-getNull'); }\n"
            // empty
            + "try {\n"
            + "  element.getAttribute('');\n"
            + "} catch(e) { log('exception-getEmpty'); }\n";

        final String xml = ""
            + "<root>\n"
            + "<element attr=\"test\"/>\n"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"test", "null", "null", "test"})
    public void getAttribute_namespace() throws Exception {
        final String test = ""
            + "var element = root.childNodes[0];\n"
            + "try {\n"
            + "  log(element.getAttribute('attr'));\n"
            + "  log(element.getAttribute('tst:attr'));\n"
            + "} catch(e) { log('exception1'); }\n"
            + "element = root.childNodes[1];\n"
            + "try {\n"
            + "  log(element.getAttribute('attr'));\n"
            + "  log(element.getAttribute('tst:attr'));\n"
            + "} catch(e) { log('exception2'); }\n";

        final String xml = ""
            + "<root xmlns:tst=\"http://test.com\">\n"
            + "<tst:element attr=\"test\"/>\n"
            + "<tst:element tst:attr=\"test\"/>\n"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"attr=test", "null", "null", "exception-getNull", "exception-getEmpty"})
    public void getAttributeNode() throws Exception {
        final String test = ""
            + "var element = root.childNodes[0];\n"
            // normal
            + "debug(element.getAttributeNode('attr'));\n"
            // case-sensitive
            + "debug(element.getAttributeNode('AttR'));\n"
            // unknown
            + "debug(element.getAttributeNode('unknown'));\n"
            // null
            + "try {\n"
            + "  element.getAttributeNode(null);\n"
            + "} catch(e) { log('exception-getNull'); }\n"
            // empty
            + "try {\n"
            + "  element.getAttributeNode('');\n"
            + "} catch(e) { log('exception-getEmpty'); }\n";

        final String xml = ""
            + "<root>\n"
            + "<element attr=\"test\"/>\n"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"attr=test", "null", "null", "tst:attr=test"})
    public void getAttributeNode_namespace() throws Exception {
        final String test = ""
            + "var element = root.childNodes[0];\n"
            + "try {\n"
            + "  debug(element.getAttributeNode('attr'));\n"
            + "  debug(element.getAttributeNode('tst:attr'));\n"
            + "} catch(e) { log('exception1'); }\n"
            + "element = doc.documentElement.childNodes[1];\n"
            + "try {\n"
            + "  debug(element.getAttributeNode('attr'));\n"
            + "  debug(element.getAttributeNode('tst:attr'));\n"
            + "} catch(e) { log('exception2'); }\n";

        final String xml = ""
            + "<root xmlns:tst=\"http://test.com\">\n"
            + "<tst:element attr=\"test\"/>\n"
            + "<tst:element tst:attr=\"test\"/>\n"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"1", "child-element=null", "0", "1", "child-element=null", "0", "1", "grand-child-element=null",
                  "2", "child-element3=null", "child-element3=null", "0", "0", "exception-getNull"})
    public void getElementsByTagName() throws Exception {
        final String test =
            // normal
              "var elements = root.getElementsByTagName('child-element');\n"
            + "log(elements.length);\n"
            + "debug(elements[0]);\n"
            // case-sensitive
            + "elements = root.getElementsByTagName('chIld-ElEmEnt');\n"
            + "log(elements.length);\n"
            // trim
            + "elements = root.getElementsByTagName(' child-element ');\n"
            + "log(elements.length);\n"
            + "debug(elements[0]);\n"
            // unknown
            + "elements = root.getElementsByTagName('unknown');\n"
            + "log(elements.length);\n"
            // recursive
            + "elements = root.getElementsByTagName('grand-child-element');\n"
            + "log(elements.length);\n"
            + "debug(elements[0]);\n"
            // multiple
            + "elements = root.getElementsByTagName('child-element3');\n"
            + "log(elements.length);\n"
            + "debug(elements[0]);\n"
            + "debug(elements[1]);\n"
            // not self
            + "elements = root.childNodes[1].getElementsByTagName('child-element2');\n"
            + "log(elements.length);\n"
            // only children
            + "elements = root.childNodes[1].getElementsByTagName('child-element');\n"
            + "log(elements.length);\n"
            // null
            + "try {\n"
            + "  root.getElementsByTagName(null);\n"
            + "} catch(e) { log('exception-getNull'); }\n";

        final String xml = ""
            + "<root child-attribute='test'>"
            + "<child-element/>"
            + "<child-element2><grand-child-element/></child-element2>"
            + "<child-element3/>"
            + "<child-element3/>"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"6", "#cdata-section=child-cdata", "#comment=child-comment", "child-element=null",
                  "child-element2=null", "grand-child-element=null", "#text=child-text", "6",
                  "1", "grand-child-element=null"})
    public void getElementsByTagName_allByEmpty() throws Exception {
        final String test =
            // normal
              "var elements = root.getElementsByTagName('');\n"
            + "log(elements.length);\n"
            //   cdata
            + "debug(elements[0]);\n"
            //   comment
            + "debug(elements[1]);\n"
            //   element
            + "debug(elements[2]);\n"
            //   no linebreak
            //   element2
            + "debug(elements[3]);\n"
            //   sub element
            + "debug(elements[4]);\n"
            //   text
            + "debug(elements[5]);\n"
            // trim
            + "elements = root.getElementsByTagName(' \t ');\n"
            + "log(elements.length);\n"
            // only children
            + "elements = root.childNodes[3].getElementsByTagName('');\n"
            + "log(elements.length);\n"
            + "debug(elements[0]);\n";

        final String xml = ""
            + "<root child-attribute='test'>"
            + "<![CDATA[child-cdata]]>"
            + "<!--child-comment-->"
            + "<child-element/>"
            + "\n"
            + "<child-element2><grand-child-element/></child-element2>"
            + "child-text"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"3", "child-element=null", "child-element2=null", "grand-child-element=null", "3",
                  "1", "grand-child-element=null"})
    public void getElementsByTagName_allByStar() throws Exception {
        final String test =
            // normal
              "var elements = root.getElementsByTagName('*');\n"
            + "log(elements.length);\n"
            //   element
            + "debug(elements[0]);\n"
            //   element2
            + "debug(elements[1]);\n"
            //   sub element
            + "debug(elements[2]);\n"
            // trim
            + "elements = root.getElementsByTagName(' * ');\n"
            + "log(elements.length);\n"
            // only children
            + "elements = root.childNodes[3].getElementsByTagName('*');\n"
            + "log(elements.length);\n"
            + "debug(elements[0]);\n";

        final String xml = ""
            + "<root child-attribute='test'>"
            + "<![CDATA[child-cdata]]>"
            + "<!--child-comment-->"
            + "<child-element/>"
            + "\n"
            + "<child-element2><grand-child-element/></child-element2>"
            + "child-text"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"0", "0", "0"})
    public void getElementsByTagName_none() throws Exception {
        final String test = ""
            + "var elements = root.getElementsByTagName('unknown');\n"
            + "log(elements.length);\n"
            + "elements = root.getElementsByTagName('');\n"
            + "log(elements.length);\n"
            + "elements = root.getElementsByTagName('*');\n"
            + "log(elements.length);\n";

        tester(test, "<root/>");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"3", "1", "#text=Hello World!"})
    public void normalize_textOnly() throws Exception {
        final String test = ""
            + "root.appendChild(doc.createTextNode('Hello '));\n"
            + "root.appendChild(doc.createTextNode('World'));\n"
            + "root.appendChild(doc.createTextNode('!'));\n"
            + "log(root.childNodes.length);\n"
            + "root.normalize();\n"
            + "log(root.childNodes.length);\n"
            + "debug(root.childNodes[0]);\n";

        tester(test, "<root/>");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"3", "3", "3", "1", "#text=Hello World!"})
    public void normalize_recursive() throws Exception {
        final String test = ""
            + "var element = doc.createElement('element');\n"
            + "root.appendChild(doc.createTextNode('outer1'));\n"
            + "root.appendChild(element);\n"
            + "root.appendChild(doc.createTextNode('outer2'));\n"
            + "element.appendChild(doc.createTextNode('Hello '));\n"
            + "element.appendChild(doc.createTextNode('World'));\n"
            + "element.appendChild(doc.createTextNode('!'));\n"
            + "log(root.childNodes.length);\n"
            + "log(root.childNodes[1].childNodes.length);\n"
            + "root.normalize();\n"
            + "log(root.childNodes.length);\n"
            + "log(root.childNodes[1].childNodes.length);\n"
            + "debug(root.childNodes[1].childNodes[0]);\n";

        tester(test, "<root/>");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"7", "7"})
    public void normalize_mixed() throws Exception {
        final String test = ""
            + "log(root.childNodes.length);\n"
            + "root.normalize();\n"
            + "log(root.childNodes.length);\n";

        final String xml = ""
            + "<root child-attribute='test'>"
            + "text1"
            + "<![CDATA[child-cdata]]>"
            + "text2"
            + "<!--child-comment-->"
            + "text3"
            + "<child-element/>"
            + "text4"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"1", "1", "0", "0", "exception-removeNull", "exception-removeEmpty"})
    public void removeAttribute() throws Exception {
        final String test = ""
            + "var element = root.childNodes[0];\n"
            + "log(element.attributes.length);\n"
            // case-sensitive
            + "element.removeAttribute('AttR');\n"
            + "log(element.attributes.length);\n"
            // normal
            + "element.removeAttribute('attr');\n"
            + "log(element.attributes.length);\n"
            // unknown
            + "element.removeAttribute('unknown');\n"
            + "log(element.attributes.length);\n"
            // null
            + "try {\n"
            + "  element.removeAttribute(null);\n"
            + "} catch(e) { log('exception-removeNull'); }\n"
            // empty
            + "try {\n"
            + "  element.removeAttribute('');\n"
            + "} catch(e) { log('exception-removeEmpty'); }\n";

        final String xml = ""
            + "<root>\n"
            + "<element attr=\"test\"/>\n"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"1", "1", "0", "1", "1", "0"})
    public void removeAttribute_namespace() throws Exception {
        final String test = ""
            + "var element = root.childNodes[0];\n"
            + "try {\n"
            + "  log(element.attributes.length);\n"
            + "  element.removeAttribute('tst:attr');\n"
            + "  log(element.attributes.length);\n"
            + "  element.removeAttribute('attr');\n"
            + "  log(element.attributes.length);\n"
            + "} catch(e) { log('exception1'); }\n"
            + "element = doc.documentElement.childNodes[1];\n"
            + "try {\n"
            + "  log(element.attributes.length);\n"
            + "  element.removeAttribute('attr');\n"
            + "  log(element.attributes.length);\n"
            + "  element.removeAttribute('tst:attr');\n"
            + "  log(element.attributes.length);\n"
            + "} catch(e) { log('exception2'); }\n";

        final String xml = ""
            + "<root xmlns:tst=\"http://test.com\">\n"
            + "<tst:element attr=\"test\"/>\n"
            + "<tst:element tst:attr=\"test\"/>\n"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"1", "0", "true", "exception-removeNull"})
    public void removeAttributeNode() throws Exception {
        final String test = ""
            + "var element = root.childNodes[0];\n"
            + "var attribute = element.getAttributeNode('attr');\n"
            + "log(element.attributes.length);\n"
            + "var removed = element.removeAttributeNode(attribute);\n"
            + "log(element.attributes.length);\n"
            + "log(attribute === removed);\n"
            // null
            + "try {\n"
            + "  element.removeAttributeNode(null);\n"
            + "} catch(e) { log('exception-removeNull'); }\n";

        final String xml = ""
            + "<root>\n"
            + "<element attr=\"test\"/>\n"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"0", "1", "test1", "1", "test2", "exception-setNameNull",
                  "exception-setNameEmpty", "exception-setValueNull", "1", ""})
    public void setAttribute() throws Exception {
        final String test = ""
            + "var element = root.childNodes[0];\n"
            + "log(element.attributes.length);\n"
            // normal
            + "element.setAttribute('attr', 'test1');\n"
            + "log(element.attributes.length);\n"
            + "log(element.getAttribute('attr'));\n"
            // overwrite
            + "element.setAttribute('attr', 'test2');\n"
            + "log(element.attributes.length);\n"
            + "log(element.getAttribute('attr'));\n"
            // null name
            + "try {\n"
            + "  element.setAttribute(null, 'test');\n"
            + "} catch(e) { log('exception-setNameNull'); }\n"
            // empty name
            + "try {\n"
            + "element.setAttribute('', 'test');\n"
            + "} catch(e) { log('exception-setNameEmpty'); }\n"
            // null value
            + "try {\n"
            + "  element.setAttribute('attr', null);\n"
            + "} catch(e) { log('exception-setValueNull'); }\n"
            // empty value
            + "element.setAttribute('attr', '');\n"
            + "log(element.attributes.length);\n"
            + "log(element.getAttribute('attr'));\n";

        final String xml = ""
            + "<root>\n"
            + "<element/>\n"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"0", "1", "test1", "2", "test2"})
    public void setAttribute_namespace() throws Exception {
        final String test = ""
            + "var element = root.childNodes[0];\n"
            + "log(element.attributes.length);\n"
            + "element.setAttribute('attr', 'test1');\n"
            + "log(element.attributes.length);\n"
            + "log(element.getAttribute('attr'));\n"
            + "element.setAttribute('tst:attr', 'test2');\n"
            + "log(element.attributes.length);\n"
            + "log(element.getAttribute('tst:attr'));\n";

        final String xml = ""
            + "<root xmlns:tst=\"http://test.com\">\n"
            + "<tst:element/>\n"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"0", "1", "attr=test1", "null", "1", "attr=test2", "attr=test1", "true",
                  "exception-setNull"})
    public void setAttributeNode() throws Exception {
        final String test = ""
            + "var element = root.childNodes[0];\n"
            + "var attribute1 = doc.createAttribute('attr');\n"
            + "attribute1.value = 'test1';\n"
            + "var attribute2 = doc.createAttribute('attr');\n"
            + "attribute2.value = 'test2';\n"

            + "log(element.attributes.length);\n"
            // normal
            + "var replaced = element.setAttributeNode(attribute1);\n"
            + "log(element.attributes.length);\n"
            + "debug(element.getAttributeNode('attr'));\n"
            + "debug(replaced);\n"
            // overwrite
            + "var replaced = element.setAttributeNode(attribute2);\n"
            + "log(element.attributes.length);\n"
            + "debug(element.getAttributeNode('attr'));\n"
            + "debug(replaced);\n"
            + "log(replaced === attribute1);\n"
            // null
            + "try {\n"
            + "  element.setAttributeNode(null);\n"
            + "} catch(e) { log('exception-setNull'); }\n";

        final String xml = ""
            + "<root>\n"
            + "<element/>\n"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"0", "1", "attr=test1", "null", "null", "1", "null", "tst:attr=test2", "attr=test1", "true"})
    public void setAttributeNode_namespace() throws Exception {
        final String test = ""
            + "var element = root.childNodes[0];\n"
            + "var attribute1 = doc.createAttribute('attr');\n"
            + "attribute1.value = 'test1';\n"
            + "var attribute2 = doc.createAttribute('tst:attr');\n"
            + "attribute2.value = 'test2';\n"

            + "log(element.attributes.length);\n"
            // normal
            + "var replaced = element.setAttributeNode(attribute1);\n"
            + "log(element.attributes.length);\n"
            + "debug(element.getAttributeNode('attr'));\n"
            + "debug(element.getAttributeNode('tst:attr'));\n"
            + "debug(replaced);\n"
            // overwrite
            + "var replaced = element.setAttributeNode(attribute2);\n"
            + "log(element.attributes.length);\n"
            + "debug(element.getAttributeNode('attr'));\n"
            + "debug(element.getAttributeNode('tst:attr'));\n"
            + "debug(replaced);\n"
            + "log(replaced === attribute1);\n";

        final String xml = ""
            + "<root xmlns:tst=\"http://test.com\">\n"
            + "<tst:element/>\n"
            + "</root>";

        tester(test, xml);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"undefined", "undefined"})
    public void not_baseURI() throws Exception {
        created("baseURI");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"undefined", "undefined"})
    public void not_innerText() throws Exception {
        created("innerText");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"undefined", "undefined"})
    public void not_localName() throws Exception {
        created("localName");
    }

    private void created(final String methodName) throws Exception {
        final String html = ""
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "    var elem1 = doc.createElement('div');\n"
            + "    var elem2 = doc.createElement('dIv');\n"
            + "    try {\n"
            + "      debug(elem1);\n"
            + "      debug(elem2);\n"
            + "    } catch(e) { log('exception'); }\n"
            + "  }\n"
            + "  function debug(e) {\n"
            + "    log(e." + methodName + ");\n"
            + "  }\n"
            + CREATE_XMLDOMDOCUMENT_FUNCTION;

        loadPageVerifyTitle2(createTestHTML(html));
    }

    private void xml(final String methodName) throws Exception {
        final String test = ""
            + "log(root.childNodes[0]." + methodName + ");\n"
            + "log(root.childNodes[1]." + methodName + ");\n"
            + "log(root.childNodes[2]." + methodName + ");\n"
            + "log(root.childNodes[3]." + methodName + ");\n";

        final String xml = ""
            + "<xml>"
            + "<div></div>"
            + "<dIv></dIv>"
            + "<div>text</div>"
            + "<div><child/></div>"
            + "</xml>";

        tester(test, xml);
    }

    private void namespace(final String methodName) throws Exception {
        final String test = ""
            + "log(root.childNodes[0]." + methodName + ");\n"
            + "log(root.childNodes[1]." + methodName + ");\n"
            + "log(root.childNodes[2]." + methodName + ");\n"
            + "log(root.childNodes[3]." + methodName + ");\n";

        final String xml = ""
            + "<html xmlns='http://www.w3.org/1999/xhtml' xmlns:app='http://www.appcelerator.org'>"
            + "<div></div>"
            + "<dIv></dIv>"
            + "<app:div>text</app:div>"
            + "<app:dIv><child/></app:dIv>"
            + "</html>";

        tester(test, xml);
    }

    private void tester(final String test, final String xml) throws Exception {
        final String html = ""
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callLoadXMLDOMDocumentFromURL("'" + URL_SECOND + "'") + ";\n"
            + "    var root = doc.documentElement;\n"
            + "    try {\n"
            + test
            + "    } catch(e) { log('exception'); }\n"
            + "  }\n"
            + "  function debug(e) {\n"
            + "    if (e != null) {\n"
            + "      log(e.nodeName + '=' + e.nodeValue);\n"
            + "    } else {\n"
            + "      log('null');\n"
            + "    }\n"
            + "  }\n"
            + LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION;

        getMockWebConnection().setResponse(URL_SECOND, xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"2", "1"})
    public void removeChild() throws Exception {
        final String html = ""
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callLoadXMLDOMDocumentFromURL("'" + URL_SECOND + "'") + ";\n"
            + "    var parent = doc.documentElement.firstChild;\n"
            + "    log(parent.childNodes.length);\n"
            + "    parent.removeChild(parent.firstChild);\n"
            + "    log(parent.childNodes.length);\n"
            + "  }\n"
            + LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION;

        final String xml = "<books><book><title>Immortality</title><author>John Smith</author></book></books>";

        getMockWebConnection().setDefaultResponse(xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"book", "0", "1"})
    public void selectNode_root() throws Exception {
        final String html = ""
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callLoadXMLDOMDocumentFromURL("'" + URL_SECOND + "'") + ";\n"
            + "    var child = doc.documentElement.firstChild;\n"
            + "    log(child.tagName);\n"
            + "    try {\n"
            + "      log(child.selectNodes('/title').length);\n"
            + "      log(child.selectNodes('title').length);\n"
            + "    } catch (e) { log('exception'); }\n"
            + "  }\n"
            + LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION;

        final String xml = "<books><book><title>Immortality</title><author>John Smith</author></book></books>";

        getMockWebConnection().setDefaultResponse(xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"1", "title"})
    public void selectNodes() throws Exception {
        final String html = ""
            + LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callLoadXMLDOMDocumentFromURL("'" + URL_SECOND + "'") + ";\n"
            + "    try {\n"
            + "      var nodes = doc.documentElement.selectNodes('//title');\n"
            + "      log(nodes.length);\n"
            + "      log(nodes[0].tagName);\n"
            + "    } catch (e) { log('exception'); }\n"
            + "  }\n"
            + LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION;

        final String xml
            = "<books>\n"
            + "  <book>\n"
            + "    <title>Immortality</title>\n"
            + "    <author>John Smith</author>\n"
            + "  </book>\n"
            + "</books>";

        getMockWebConnection().setDefaultResponse(xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }
}
