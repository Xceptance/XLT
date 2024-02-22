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
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.LOAD_XMLDOMDOCUMENT_FROM_STRING_FUNCTION;
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION;
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.callCreateXMLDOMDocument;
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.callLoadXMLDOMDocumentFromString;
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.callLoadXMLDOMDocumentFromURL;
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.createTestHTML;

import java.net.URL;

import org.htmlunit.WebDriverTestCase;
import org.htmlunit.junit.BrowserRunner;
import org.htmlunit.junit.BrowserRunner.Alerts;
import org.htmlunit.util.MimeType;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link XMLDOMDocument}.
 * @see org.htmlunit.javascript.host.xml.XMLDocumentTest
 *
 * @author Ahmed Ashour
 * @author Marc Guillemot
 * @author Chuck Dumont
 * @author Frank Danek
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class XMLDOMDocument2Test extends WebDriverTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "true")
    public void async() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "    log(doc.async);\n"
            + "  }\n"
            + CREATE_XMLDOMDOCUMENT_FUNCTION;
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"true", "books", "books", "1", "book", "0"})
    public void load() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "    doc.async = false;\n"
            + "    log(doc.load('" + URL_SECOND + "'));\n"
            + "    log(doc.documentElement.nodeName);\n"
            + "    log(doc.childNodes[0].nodeName);\n"
            + "    log(doc.childNodes[0].childNodes.length);\n"
            + "    log(doc.childNodes[0].childNodes[0].nodeName);\n"
            + "    log(doc.getElementsByTagName('books').item(0).attributes.length);\n"
            + "  }\n"
            + CREATE_XMLDOMDOCUMENT_FUNCTION;

        final String xml
            = "<books>\n"
            + "  <book>\n"
            + "    <title>Immortality</title>\n"
            + "    <author>John Smith</author>\n"
            + "  </book>\n"
            + "</books>";

        getMockWebConnection().setResponse(URL_SECOND, xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"true", "books", "books", "1", "book", "0"})
    // TODO what is the difference to load()?
    public void load_relativeURL() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "    doc.async = false;\n"
            + "    log(doc.load('" + URL_SECOND + "'));\n"
            + "    log(doc.documentElement.nodeName);\n"
            + "    log(doc.childNodes[0].nodeName);\n"
            + "    log(doc.childNodes[0].childNodes.length);\n"
            + "    log(doc.childNodes[0].childNodes[0].nodeName);\n"
            + "    log(doc.getElementsByTagName('books').item(0).attributes.length);\n"
            + "  }\n"
            + CREATE_XMLDOMDOCUMENT_FUNCTION;

        final String xml
            = "<books>\n"
            + "  <book>\n"
            + "    <title>Immortality</title>\n"
            + "    <author>John Smith</author>\n"
            + "  </book>\n"
            + "</books>";

        getMockWebConnection().setResponse(URL_SECOND, xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "false")
    public void preserveWhiteSpace() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    try {\n"
            + "      var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "      log(doc.preserveWhiteSpace);\n"
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
            IE = {})
    public void setProperty() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "    try {\n"
            + "      doc.setProperty('SelectionNamespaces', \"xmlns:xsl='http://www.w3.org/1999/XSL/Transform'\");\n"
            + "      doc.setProperty('SelectionLanguage', 'XPath');\n"
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
            IE = {"1", "books"})
    public void selectNodes() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callLoadXMLDOMDocumentFromURL("'" + URL_SECOND + "'") + ";\n"
            + "    try {\n"
            + "      var nodes = doc.selectNodes('/books');\n"
            + "      log(nodes.length);\n"
            + "      log(nodes[0].tagName);\n"
            + "    } catch(e) { log('exception'); }\n"
            + "  }\n"
            + LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION;

        final String xml
            = "<books>\n"
            + "  <book>\n"
            + "    <title>Immortality</title>\n"
            + "    <author>John Smith</author>\n"
            + "  </book>\n"
            + "</books>";

        getMockWebConnection().setResponse(URL_SECOND, xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"0", "1"})
    public void selectNodes_caseSensitive() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callLoadXMLDOMDocumentFromURL("'" + URL_SECOND + "'") + ";\n"
            + "    try {\n"
            + "      log(doc.selectNodes('/bOoKs').length);\n"
            + "      log(doc.selectNodes('/books').length);\n"
            + "    } catch(e) { log('exception'); }\n"
            + "  }\n"
            + LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION;

        final String xml
            = "<books>\n"
            + "  <book>\n"
            + "    <title>Immortality</title>\n"
            + "    <author>John Smith</author>\n"
            + "  </book>\n"
            + "</books>";

        getMockWebConnection().setResponse(URL_SECOND, xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"2", "1"})
    public void selectNodes_namespace() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callLoadXMLDOMDocumentFromURL("'" + URL_SECOND + "'") + ";\n"
            + "    try {\n"
            + "      log(doc.selectNodes('//ns1:title').length);\n"
            + "      log(doc.selectNodes('//ns2:title').length);\n"
            + "    } catch(e) { log('exception'); }\n"
            + "  }\n"
            + LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION;
        final String xml
            = "<ns1:books xmlns:ns1=\"http://one\">\n"
            + "  <ns2:book xmlns:ns2=\"http://two\">\n"
            + "    <ns2:title>Immortality</ns2:title>\n"
            + "    <ns2:author>John Smith</ns2:author>\n"
            + "  </ns2:book>\n"
            + "  <ns1:book>\n"
            + "    <ns1:title>The Hidden Secrets</ns1:title>\n"
            + "    <ns1:author>William Adams</ns1:author>\n"
            + "  </ns1:book>\n"
            + "  <ns1:book>\n"
            + "    <ns1:title>So What?</ns1:title>\n"
            + "    <ns1:author>Tony Walas</ns1:author>\n"
            + "  </ns1:book>\n"
            + "</ns1:books>";

        getMockWebConnection().setResponse(URL_SECOND, xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"book", "null", "book", "null"})
    public void selectNodes_nextNodeAndReset() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    try {\n"
            + "      var doc = " + callLoadXMLDOMDocumentFromURL("'" + URL_SECOND + "'") + ";\n"
            + "      var nodes = doc.selectNodes('//book');\n"
            + "      log(nodes.nextNode().nodeName);\n"
            + "      log(nodes.nextNode());\n"
            + "      nodes.reset();\n"
            + "      log(nodes.nextNode().nodeName);\n"
            + "      log(nodes.nextNode());\n"
            + "    } catch(e) { log('exception'); }\n"
            + "  }\n"
            + LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION;

        final String xml
            = "<books>\n"
            + "  <book>\n"
            + "    <title>Immortality</title>\n"
            + "    <author>John Smith</author>\n"
            + "  </book>\n"
            + "</books>";

        getMockWebConnection().setResponse(URL_SECOND, xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * Test that element.selectNodes("/tagName") searches from root of the tree, not from that specific element.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"book", "0", "1"})
    public void selectNodes_fromRoot() throws Exception {
        final String html = LOG_TITLE_FUNCTION
                + "  function test() {\n"
                + ACTIVEX_CHECK
                + "    var doc = " + callLoadXMLDOMDocumentFromURL("'" + URL_SECOND + "'") + ";\n"
                + "    try {\n"
                + "      var child = doc.documentElement.firstChild;\n"
                + "      log(child.tagName);\n"

                + "      try {\n"
                + "        log(child.selectNodes('/title').length);\n"
                + "      } catch(e) { log('exception /title'); }\n"

                + "      try {\n"
                + "        log(child.selectNodes('title').length);\n"
                + "      } catch(e) { log('exception title'); }\n"
                + "    } catch(e) { log('exception'); }\n"
                + "  }\n"
                + LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION;

        final String xml = "<books><book><title>Immortality</title><author>John Smith</author></book></books>";

        getMockWebConnection().setResponse(URL_SECOND, xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"book", "#document", "book", "#document"})
    public void selectSingleNode() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var text='<book/>';\n"
            + "    try {\n"
            + "      var doc = " + callLoadXMLDOMDocumentFromString("text") + ";\n"
            + "      log(doc.selectNodes('*')[0].nodeName);\n"
            + "      log(doc.selectNodes('/')[0].nodeName);\n"
            + "      log(doc.selectSingleNode('*').nodeName);\n"
            + "      log(doc.selectNodes('*')[0].selectSingleNode('/').nodeName);\n"
            + "    } catch(e) { log('exception'); }\n"
            + "  }\n"
            + LOAD_XMLDOMDOCUMENT_FROM_STRING_FUNCTION;
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "someprefix:test")
    public void loadXML_Namespace() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var text='<someprefix:test xmlns:someprefix=\"http://myNS\"/>';\n"
            + "    var doc = " + callLoadXMLDOMDocumentFromString("text") + ";\n"
            + "    log(doc.documentElement.tagName);\n"
            + "  }\n"
            + LOAD_XMLDOMDOCUMENT_FROM_STRING_FUNCTION;
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * Tests "xml:space" attribute.
     *
     * Xalan team response:<br>
     * "See the DOM Level 3 recommendation for discussion of this. XPath returns the start of the XPath text node,
     * which spans multiple DOM nodes. It is the DOM user's responsibility to gather the additional nodes,
     * either manually or by retrieving wholeText rather than value.<br>
     * This is unavoidable since DOM and XPath define the concept of "node" differently."
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "7")
    public void loadXML_XMLSpaceAttribute() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "function test() {\n"
            + ACTIVEX_CHECK
            + "  var text='<root xml:space=\\'preserve\\'>This t"
            + "<elem>ext has</elem> <![CDATA[ CDATA ]]>in<elem /> it</root>';\n"
            + "  var doc = " + callLoadXMLDOMDocumentFromString("text") + ";\n"
            + "  log(doc.documentElement.childNodes.length);\n"
            + "}\n"
            + LOAD_XMLDOMDOCUMENT_FROM_STRING_FUNCTION;
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"same doc: false", "in first: 3", "book", "ownerDocument: doc1",
                  "in 2nd: 3", "ownerDocument: doc2", "first child ownerDocument: doc2", "in first: 2", "in 2nd: 4",
                  "ownerDocument: doc1", "in first: 2", "in 2nd: 3",
                  "ownerDocument: doc2", "in first: 1", "in 2nd: 4"})
    public void moveChildBetweenDocuments() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "function test() {\n"
            + ACTIVEX_CHECK
            + "  var doc1 = " + callLoadXMLDOMDocumentFromURL("'foo.xml'") + ";\n"
            + "  var doc2 = " + callLoadXMLDOMDocumentFromURL("'foo.xml'") + ";\n"
            + "  log('same doc: ' + (doc1 == doc2));\n"
            + "  var doc1Root = doc1.firstChild;\n"
            + "  log('in first: ' + doc1Root.childNodes.length);\n"
            + "  var doc1RootOriginalFirstChild = doc1Root.firstChild;\n"
            + "  log(doc1RootOriginalFirstChild.tagName);\n"
            + "  log('ownerDocument: ' + (doc1RootOriginalFirstChild.ownerDocument == doc1 ? 'doc1' : 'doc2'));\n"
            + "\n"
            + "  var doc2Root = doc2.firstChild;\n"
            + "  log('in 2nd: ' + doc2Root.childNodes.length);\n"
            + "  doc2Root.appendChild(doc1RootOriginalFirstChild);\n"
            + "  log('ownerDocument: ' + (doc1RootOriginalFirstChild.ownerDocument == doc1 ? 'doc1' : 'doc2'));\n"
            + "  log('first child ownerDocument: ' + "
            + "(doc1RootOriginalFirstChild.firstChild.ownerDocument == doc1 ? 'doc1' : 'doc2'));\n"
            + "  log('in first: ' + doc1Root.childNodes.length);\n"
            + "  log('in 2nd: ' + doc2Root.childNodes.length);\n"
            + "\n"
            + "  doc1Root.replaceChild(doc1RootOriginalFirstChild, doc1Root.firstChild);\n"
            + "  log('ownerDocument: ' + (doc1RootOriginalFirstChild.ownerDocument == doc1 ? 'doc1' : 'doc2'));\n"
            + "  log('in first: ' + doc1Root.childNodes.length);\n"
            + "  log('in 2nd: ' + doc2Root.childNodes.length);\n"
            + "\n"
            + "  doc2Root.insertBefore(doc1RootOriginalFirstChild, doc2Root.firstChild);\n"
            + "  log('ownerDocument: ' + (doc1RootOriginalFirstChild.ownerDocument == doc1 ? 'doc1' : 'doc2'));\n"
            + "  log('in first: ' + doc1Root.childNodes.length);\n"
            + "  log('in 2nd: ' + doc2Root.childNodes.length);\n"
            + "\n"
            + "}\n"
            + LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION;

        final String xml = "<order><book><title/></book><cd/><dvd/></order>";

        getMockWebConnection().setResponse(new URL(URL_FIRST, "foo.xml"), xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"1", "0", "1", "0"})
    public void getElementsByTagName() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callLoadXMLDOMDocumentFromURL("'" + URL_SECOND + "'") + ";\n"
            + "    log(doc.getElementsByTagName('book').length);\n"
            + "    log(doc.getElementsByTagName('soap:book').length);\n"
            + "    var elem = doc.getElementsByTagName('book')[0];\n"
            + "    log(elem.getElementsByTagName('title').length);\n"
            + "    log(elem.getElementsByTagName('soap:title').length);\n"
            + "  }\n"
            + LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION;

        final String xml
            = "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>\n"
            + "  <books xmlns='http://www.example.com/ns1'>\n"
            + "    <book>\n"
            + "      <title>Immortality</title>\n"
            + "      <author>John Smith</author>\n"
            + "    </book>\n"
            + "  </books>\n"
            + "</soap:Envelope>";

        getMockWebConnection().setResponse(URL_SECOND, xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"0", "1", "0", "1"})
    public void getElementsByTagNameWithNamespace() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callLoadXMLDOMDocumentFromURL("'" + URL_SECOND + "'") + ";\n"
            + "    log(doc.getElementsByTagName('book').length);\n"
            + "    log(doc.getElementsByTagName('soap:book').length);\n"
            + "    var elem = doc.getElementsByTagName('soap:book')[0];\n"
            + "    log(elem.getElementsByTagName('title').length);\n"
            + "    log(elem.getElementsByTagName('soap:title').length);\n"
            + "  }\n"
            + LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION;

        final String xml
            = "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>\n"
            + "  <books xmlns='http://www.example.com/ns1'>\n"
            + "    <soap:book>\n"
            + "      <soap:title>Immortality</soap:title>\n"
            + "      <author>John Smith</author>\n"
            + "    </soap:book>\n"
            + "  </books>\n"
            + "</soap:Envelope>";

        getMockWebConnection().setResponse(URL_SECOND, xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "1")
    public void xpathWithNamespaces() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "function test() {\n"
            + ACTIVEX_CHECK
            + "  var doc = " + callLoadXMLDOMDocumentFromURL("'" + URL_SECOND + "'") + ";\n"
            + "  try {\n"
            + "    log(doc.selectNodes('//soap:book').length);\n"
            + "  } catch (e) {\n"
            + "    log(doc.evaluate('count(//book)', doc.documentElement, "
            + "null, XPathResult.NUMBER_TYPE, null).numberValue);\n"
            + "  }\n"
            + "}\n"
            + LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION;

        final String xml
            = "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>\n"
            + "  <books xmlns='http://www.example.com/ns1'>\n"
            + "    <soap:book>\n"
            + "      <title>Immortality</title>\n"
            + "      <author>John Smith</author>\n"
            + "    </soap:book>\n"
            + "  </books>\n"
            + "</soap:Envelope>";

        getMockWebConnection().setResponse(URL_SECOND, xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "1")
    public void selectionNamespaces() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "var selectionNamespaces = 'xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                            + "xmlns:ns1=\"http://www.example.com/ns1\"';\n"
            + "function test() {\n"
            + ACTIVEX_CHECK
            + "  var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "  doc.setProperty('SelectionNamespaces', selectionNamespaces);\n"
            + "  doc.async = false;\n"
            + "  doc.load('" + URL_SECOND + "');\n"
            + "  try {\n"
            + "    log(doc.selectNodes('/s:Envelope/ns1:books/s:book').length);\n"
            + "  } catch (e) {\n"
            + "    log(doc.evaluate('count(//book)', doc.documentElement, "
            + "null, XPathResult.NUMBER_TYPE, null).numberValue);\n"
            + "  }\n"
            + "}\n"
            + CREATE_XMLDOMDOCUMENT_FUNCTION;

        final String xml = ""
            + "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>\n"
            + "  <books xmlns='http://www.example.com/ns1'>\n"
            + "    <soap:book>\n"
            + "      <title>Immortality</title>\n"
            + "      <author>John Smith</author>\n"
            + "    </soap:book>\n"
            + "  </books>\n"
            + "</soap:Envelope>";

        getMockWebConnection().setResponse(URL_SECOND, xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "null")
    public void nodeFromID() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    var doc = " + callLoadXMLDOMDocumentFromURL("'" + URL_SECOND + "'") + ";\n"
            + "    log(doc.nodeFromID('target'));\n"
            + "  }\n"
            + LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION;

        final String xml
            = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
            + "  <body>\n"
            + "    <div id=\"target\"></div>\n"
            + "  </body>\n"
            + "</html>";

        getMockWebConnection().setResponse(URL_SECOND, xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }
}
