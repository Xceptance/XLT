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

import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION;
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.callLoadXMLDOMDocumentFromURL;
import static org.htmlunit.activex.javascript.msxml.MSXMLTestHelper.createTestHTML;

import org.htmlunit.WebDriverTestCase;
import org.htmlunit.junit.BrowserRunner;
import org.htmlunit.junit.BrowserRunner.Alerts;
import org.htmlunit.util.MimeType;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link XMLDOMNodeList}.
 *
 * @author Ahmed Ashour
 * @author Frank Danek
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class XMLDOMNodeListTest extends WebDriverTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "exception",
            IE = "[object Object]")
    public void scriptableToString() throws Exception {
        tester("log(Object.prototype.toString.call(list));\n");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "exception",
            IE = "2")
    public void length() throws Exception {
        tester("log(list.length);\n");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "exception",
            IE = "0")
    public void length_empty() throws Exception {
        tester("log(list.length);\n", "<root/>");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "exception",
            IE = "undefined")
    public void byName_attribute() throws Exception {
        tester("log(list.child1);\n");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "exception",
            IE = "undefined")
    public void byName_map() throws Exception {
        tester("log(list['child1']);\n");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "exception",
            IE = "child1=null")
    public void byNumber() throws Exception {
        tester("debug(list[0]);\n");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "exception",
            IE = "null")
    public void byNumber_unknown() throws Exception {
        tester("debug(list[2]);\n");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "exception",
            IE = "child1=null")
    public void item() throws Exception {
        tester("debug(list.item(0));\n");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "exception",
            IE = "null")
    public void item_unknown() throws Exception {
        tester("debug(list.item(2));\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "exception",
            IE = {"2", "child1=null", "child2=null", "null"})
    public void nextNode() throws Exception {
        final String test = ""
            + "log(list.length);\n"
            + "debug(list.nextNode());\n"
            + "debug(list.nextNode());\n"
            + "debug(list.nextNode());\n";

        tester(test);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "exception",
            IE = {"2", "child1=null", "child1=null", "child2=null", "child1=null"})
    public void reset() throws Exception {
        final String test = ""
            + "log(list.length);\n"
            + "debug(list.nextNode());\n"
            + "list.reset();\n"
            + "debug(list.nextNode());\n"
            + "debug(list.nextNode());\n"
            + "list.reset();\n"
            + "debug(list.nextNode());\n";

        tester(test);
    }

    /**
     * @throws Exception on test failure
     */
    @Test
    @Alerts(DEFAULT = "exception",
            IE = "true")
    public void in() throws Exception {
        tester("log(0 in list);\n");
    }

    /**
     * @throws Exception on test failure
     */
    @Test
    @Alerts(DEFAULT = "exception",
            IE = "true")
    public void in_length() throws Exception {
        tester("log('length' in list);\n");
    }

    /**
     * @throws Exception on test failure
     */
    @Test
    @Alerts(DEFAULT = "exception",
            IE = "false")
    public void in_unknownIndex() throws Exception {
        tester("log(-1 in list);\n");
    }

    /**
     * @throws Exception on test failure
     */
    @Test
    @Alerts(DEFAULT = "exception",
            IE = "true")
    public void in_unknownIndex2() throws Exception {
        tester("log(2 in list);\n");
    }

    /**
     * @throws Exception on test failure
     */
    @Test
    @Alerts(DEFAULT = "exception",
            IE = "false")
    public void in_unknown() throws Exception {
        tester("log('child1' in list);\n");
    }

    private void tester(final String test) throws Exception {
        final String xml = ""
            + "<root>"
            + "<child1/>"
            + "<child2/>"
            + "</root>";

        tester(test, xml);
    }

    private void tester(final String test, final String xml) throws Exception {
        final String html = LOG_TITLE_FUNCTION
            + "  function test() {\n"
            + "    try {\n"
            + "      var doc = " + callLoadXMLDOMDocumentFromURL("'second.xml'") + ";\n"
            + "      var root = doc.documentElement;\n"
            + "      var list = root.childNodes;\n"
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

        getMockWebConnection().setDefaultResponse(xml, MimeType.TEXT_XML);
        loadPageVerifyTitle2(createTestHTML(html));
    }
}
