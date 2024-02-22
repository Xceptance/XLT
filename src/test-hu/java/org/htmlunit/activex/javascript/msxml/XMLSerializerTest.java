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
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.SERIALIZE_XMLDOMDOCUMENT_TO_STRING_FUNCTION;
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.callCreateXMLDOMDocument;
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.callLoadXMLDOMDocumentFromString;
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.callSerializeXMLDOMDocumentToString;
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.createTestHTML;
import static org.htmlunit.junit.BrowserRunner.TestedBrowser.IE;

import org.htmlunit.WebDriverTestCase;
import org.htmlunit.junit.BrowserRunner;
import org.htmlunit.junit.BrowserRunner.Alerts;
import org.htmlunit.junit.BrowserRunner.NotYetImplemented;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Tests for {@link XMLSerializer}.
 *
 * @author Ahmed Ashour
 * @author Darrell DeBoer
 * @author Frank Danek
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class XMLSerializerTest extends WebDriverTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {})
    public void test() throws Exception {
        final String expectedString = getExpectedAlerts().length != 0 ? ""
                : "<note>\\r\\n9<to>Tove</to>\\r\\n9<from>Jani</from>\\r\\n9<body>Do32not32forget32me32this32weekend!</body>"
                + "\\r\\n9<outer>\\r\\n99<inner>Some32Value</inner></outer>\\r\\n</note>\\r\\n";

        final String serializationText =
                "<note> "
                + "<to>Tove</to> \\n"
                + "<from>Jani</from> \\n "
                + "<body>Do not forget me this weekend!</body> "
                + "<outer>\\n "
                + "  <inner>Some Value</inner>"
                + "</outer> "
                + "</note>";

        final WebDriver driver = loadPageVerifyTitle2(constructPageContent(serializationText));
        final WebElement textArea = driver.findElement(By.id("myTextArea"));
        assertEquals(expectedString, textArea.getAttribute("value"));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {})
    public void comment() throws Exception {
        final String expectedString = getExpectedAlerts().length != 0 ? "" : "<a><!--32abc32--></a>\\r\\n";

        final String serializationText = "<a><!-- abc --></a>";
        final WebDriver driver = loadPageVerifyTitle2(constructPageContent(serializationText));
        final WebElement textArea = driver.findElement(By.id("myTextArea"));
        assertEquals(expectedString, textArea.getAttribute("value"));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {})
    public void xmlEntities() throws Exception {
        final String expectedString = getExpectedAlerts().length != 0 ? "" : "<a>&lt;&gt;&amp;</a>\\r\\n";
        final String serializationText = "<a>&lt;&gt;&amp;</a>";
        final WebDriver driver = loadPageVerifyTitle2(constructPageContent(serializationText));
        final WebElement textArea = driver.findElement(By.id("myTextArea"));
        assertEquals(expectedString, textArea.getAttribute("value"));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {})
    @NotYetImplemented(IE)
    // so far we are not able to add the XML header
    public void nameSpaces() throws Exception {
        final String expectedString = getExpectedAlerts().length != 0 ? ""
                : "<?xml32version=\"1.0\"?>\\r\\n<xsl:stylesheet32version=\"1.0\"32"
                + "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\\r\\n9<xsl:template32match=\"/\">\\r\\n99<html>"
                + "\\r\\n999<body>\\r\\n999</body>\\r\\n99</html>\\r\\n9</xsl:template>\\r\\n</xsl:stylesheet>\\r\\n";

        final String serializationText =
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\\r\\n"
                + "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\\r\\n"
                + "  <xsl:template match=\"/\">\\r\\n"
                + "  <html>\\r\\n"
                + "    <body>\\r\\n"
                + "    </body>\\r\\n"
                + "  </html>\\r\\n"
                + "  </xsl:template>\\r\\n"
                + "</xsl:stylesheet>";

        final WebDriver driver = loadPageVerifyTitle2(constructPageContent(serializationText));
        final WebElement textArea = driver.findElement(By.id("myTextArea"));
        assertEquals(expectedString, textArea.getAttribute("value"));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {})
    public void attributes() throws Exception {
        final String expectedString = getExpectedAlerts().length != 0 ? ""
                : "<document32attrib=\"attribValue\"><outer32attrib=\"attribValue\">"
                + "<inner32attrib=\"attribValue\"/><meta32attrib=\"attribValue\"/></outer></document>\\r\\n";

        final String serializationText = "<document attrib=\"attribValue\">"
                                            + "<outer attrib=\"attribValue\">"
                                            + "<inner attrib=\"attribValue\"/>"
                                            + "<meta attrib=\"attribValue\"/>"
                                            + "</outer></document>";

        final WebDriver driver = loadPageVerifyTitle2(constructPageContent(serializationText));
        final WebElement textArea = driver.findElement(By.id("myTextArea"));
        assertEquals(expectedString, textArea.getAttribute("value"));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {})
    @NotYetImplemented(IE)
    // so far we are not able to add the XML header
    public void htmlAttributes() throws Exception {
        final String expectedString = getExpectedAlerts().length != 0 ? ""
                : "<?xml32version=\"1.0\"?>\\r\\n<html32xmlns=\"http://www.w3.org/1999/xhtml\">"
                        + "<head><title>html</title></head>"
                        + "<body32id=\"bodyId\">"
                        + "<span32class=\"spanClass\">foo</span>"
                        + "</body>"
                        + "</html>\\r\\n";

        final String serializationText = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
                                          + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                                          + "<head><title>html</title></head>"
                                          + "<body id=\"bodyId\">"
                                          + "<span class=\"spanClass\">foo</span>"
                                          + "</body>"
                                          + "</html>";

        final WebDriver driver = loadPageVerifyTitle2(constructPageContent(serializationText));
        final WebElement textArea = driver.findElement(By.id("myTextArea"));
        assertEquals(expectedString, textArea.getAttribute("value"));
    }

    /**
     * Constructs an HTML page that when loaded will parse and serialize the provided text.
     * First the provided text is parsed into a Document. Then the Document is serialized (browser-specific).
     * Finally the result is placed into the text area "myTextArea".
     */
    private static String constructPageContent(final String serializationText) {
        final String escapedText = serializationText.replace("\n", "\\n");

        final StringBuilder builder = new StringBuilder();
        builder.append(
              "<html><head><script>\n"
            + LOG_TITLE_FUNCTION
            + "  function test() {\n" + ACTIVEX_CHECK);

        builder.append("    var text = '").append(escapedText).append("';\n").append(
              "    var doc = " + callLoadXMLDOMDocumentFromString("text") + ";\n"
            + "    var xml = " + callSerializeXMLDOMDocumentToString("doc") + ";\n"
            + "    var ta = document.getElementById('myTextArea');\n"
            + "    for (var i = 0; i < xml.length; i++) {\n"
            + "      if (xml.charCodeAt(i) < 33)\n"
            + "        ta.value += xml.charCodeAt(i);\n"
            + "      else\n"
            + "        ta.value += xml.charAt(i);\n"
            + "    }\n"
            + "  }\n"
            + LOAD_XMLDOMDOCUMENT_FROM_STRING_FUNCTION
            + SERIALIZE_XMLDOMDOCUMENT_TO_STRING_FUNCTION
            + "</script></head><body onload='test()'>\n"
            + "  <textarea id='myTextArea' cols='80' rows='30'></textarea>\n"
            + "</body></html>");
        return builder.toString();
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"<foo/>\\r\\n", "<foo/>"})
    public void document() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    try {\n"
            + "      var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "      doc.documentElement = doc.createElement('foo');\n"
            + "      log(" + callSerializeXMLDOMDocumentToString("doc") + ");\n"
            + "      log(" + callSerializeXMLDOMDocumentToString("doc.documentElement") + ");\n"
            + "    } catch(e) { log('exception'); }\n"
            + "  }\n"
            + CREATE_XMLDOMDOCUMENT_FUNCTION
            + SERIALIZE_XMLDOMDOCUMENT_TO_STRING_FUNCTION;
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = {"<img/>", "<?myTarget myData?>"})
    public void xml() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    try {\n"
            + "      var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "      testFragment(doc);\n"
            + "      var pi = doc.createProcessingInstruction('myTarget', 'myData');\n"
            + "      log(" + callSerializeXMLDOMDocumentToString("pi") + ");\n"
            + "    } catch(e) { log('exception'); }\n"
            + "  }\n"
            + "  function testFragment(doc) {\n"
            + "    var fragment = doc.createDocumentFragment();\n"
            + "    var img = doc.createElement('img');\n"
            + "    fragment.appendChild(img);\n"
            + "    log(" + callSerializeXMLDOMDocumentToString("fragment") + ");\n"
            + "  }\n"
            + CREATE_XMLDOMDOCUMENT_FUNCTION
            + SERIALIZE_XMLDOMDOCUMENT_TO_STRING_FUNCTION;
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "<root><my:parent xmlns:my=\"myUri\"><my:child/><another_child/></my:parent></root>\\r\\n")
    public void namespace() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    try {\n"
            + "      var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "      var root = doc.createElement('root');\n"
            + "      doc.appendChild(root);\n"
            + "      var parent = createNS(doc, 'my:parent', 'myUri');\n"
            + "      root.appendChild(parent);\n"
            + "      parent.appendChild(createNS(doc, 'my:child', 'myUri'));\n"
            + "      parent.appendChild(doc.createElement('another_child'));\n"
            + "      log(" + callSerializeXMLDOMDocumentToString("doc") + ");\n"
            + "    } catch(e) { log('exception'); }\n"
            + "  }\n"
            + "  function createNS(doc, name, uri) {\n"
            + "    return typeof doc.createNode == 'function' || typeof doc.createNode == 'unknown' ? "
            + "doc.createNode(1, name, uri) : doc.createElementNS(uri, name);\n"
            + "  }\n"
            + CREATE_XMLDOMDOCUMENT_FUNCTION
            + SERIALIZE_XMLDOMDOCUMENT_TO_STRING_FUNCTION;
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "<teXtaREa/>")
    public void mixedCase() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    try {\n"
            + "      var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "      var t = doc.createElement('teXtaREa');\n"
            + "      log(" + callSerializeXMLDOMDocumentToString("t") + ");\n"
            + "    } catch(e) { log('exception'); }\n"
            + "  }\n"
            + CREATE_XMLDOMDOCUMENT_FUNCTION
            + SERIALIZE_XMLDOMDOCUMENT_TO_STRING_FUNCTION;
        loadPageVerifyTitle2(createTestHTML(html));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "no ActiveX",
            IE = "<img href=\"mypage.htm\"/>")
    public void noClosingTagWithAttribute() throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + ACTIVEX_CHECK
            + "    try {\n"
            + "      var doc = " + callCreateXMLDOMDocument() + ";\n"
            + "      var t = doc.createElement('img');\n"
            + "      t.setAttribute('href', 'mypage.htm');\n"
            + "      log(" + callSerializeXMLDOMDocumentToString("t") + ");\n"
            + "    } catch(e) { log('exception'); }\n"
            + "  }\n"
            + CREATE_XMLDOMDOCUMENT_FUNCTION
            + SERIALIZE_XMLDOMDOCUMENT_TO_STRING_FUNCTION;
        loadPageVerifyTitle2(createTestHTML(html));
    }
}
