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

import org.htmlunit.html.HtmlPageTest;

/**
 * Test utility stuff for MSXML tests.
 *
 * @author Frank Danek
 * @author Ronald Brill
 */
public final class MSXMLTestHelper {

    /** Helper. */
    public static final String ACTIVEX_CHECK =
            "    if (!('ActiveXObject' in window)) { log('no ActiveX'); return; }\n";

    /** Helper. */
    public static final String CREATE_XMLDOMDOCUMENT_FUNCTION_NAME = "createXMLDOMDocument";

    /** Helper. */
    public static final String CREATE_XMLDOMDOCUMENT_FUNCTION = ""
            + "  function " + CREATE_XMLDOMDOCUMENT_FUNCTION_NAME + "() {\n"
            + "    return new ActiveXObject('Microsoft.XMLDOM');\n"
            + "  }\n";

    /**
     * Helper.
     * @return XML helper
     */
    public static String callCreateXMLDOMDocument() {
        return CREATE_XMLDOMDOCUMENT_FUNCTION_NAME + "()";
    }

    /** Helper. */
    public static final String LOAD_XMLDOMDOCUMENT_FROM_STRING_FUNCTION_NAME = "loadXMLDOMDocumentFromString";

    /** Helper. */
    public static final String LOAD_XMLDOMDOCUMENT_FROM_STRING_FUNCTION = ""
            + "  function " + LOAD_XMLDOMDOCUMENT_FROM_STRING_FUNCTION_NAME + "(xml) {\n"
            + "    xmlDoc = new ActiveXObject(\"Microsoft.XMLDOM\");\n"
            + "    xmlDoc.async = false;\n"
            + "    xmlDoc.loadXML(xml);\n"
            + "    return xmlDoc;\n"
            + "  }\n";

    /**
     * Helper.
     * @param string the parameter
     * @return XML helper
     */
    public static String callLoadXMLDOMDocumentFromString(final String string) {
        return LOAD_XMLDOMDOCUMENT_FROM_STRING_FUNCTION_NAME + "(" + string + ")";
    }

    /** Helper. */
    public static final String LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION_NAME = "loadXMLDOMDocumentFromURL";

    /** Helper. */
    public static final String LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION = ""
            + "  function " + LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION_NAME + "(url) {\n"
            + "    xmlDoc = new ActiveXObject(\"Microsoft.XMLDOM\");\n"
            + "    xmlDoc.async = false;\n"
            + "    xmlDoc.load(url);\n"
            + "    return xmlDoc;\n"
            + "  }\n";

    /**
     * Helper.
     * @param url the parameter
     * @return XML helper
     */
    public static String callLoadXMLDOMDocumentFromURL(final String url) {
        return LOAD_XMLDOMDOCUMENT_FROM_URL_FUNCTION_NAME + "(" + url + ")";
    }

    /** Helper. */
    public static final String CREATE_XMLHTTPREQUEST_FUNCTION_NAME = "createXMLHTTPRequest";

    /** Helper. */
    public static final String CREATE_XMLHTTPREQUEST_FUNCTION = ""
            + "  function " + CREATE_XMLHTTPREQUEST_FUNCTION_NAME + "() {\n"
            + "    return new ActiveXObject(\"Microsoft.XMLHTTP\");\n"
            + "  }\n";

    /**
     * Helper.
     * @return XML helper
     */
    public static String callCreateXMLHTTPRequest() {
        return CREATE_XMLHTTPREQUEST_FUNCTION_NAME + "()";
    }

    /** Helper. */
    public static final String LOAD_XMLHTTPREQUEST_FROM_URL_FUNCTION_NAME = "loadXMLHTTPRequestFromURL";

    /** Helper. */
    public static final String LOAD_XMLHTTPREQUEST_FROM_URL_FUNCTION = ""
            + "  function " + LOAD_XMLHTTPREQUEST_FROM_URL_FUNCTION_NAME + "(url) {\n"
            + "    xhr = new ActiveXObject(\"Microsoft.XMLHTTP\");\n"
            + "    xhr.open(\"GET\", url, false);\n"
            + "    xhr.send();\n"
            + "    return xhr;\n"
            + "  }\n";

    /**
     * Helper.
     * @param url the parameter
     * @return XML helper
     */
    public static String callLoadXMLHTTPRequestFromURL(final String url) {
        return LOAD_XMLHTTPREQUEST_FROM_URL_FUNCTION_NAME + "(" + url + ")";
    }

    /** Helper. */
    public static final String SERIALIZE_XMLDOMDOCUMENT_TO_STRING_FUNCTION_NAME = "serializeXMLDOMDocumentToString";

    /** Helper. */
    public static final String SERIALIZE_XMLDOMDOCUMENT_TO_STRING_FUNCTION = ""
            + "  function " + SERIALIZE_XMLDOMDOCUMENT_TO_STRING_FUNCTION_NAME + "(doc) {\n"
            + "    var txt = doc.xml;\n"
            + "    txt = txt.replace(/\\r/g, '\\\\r');\n"
            + "    txt = txt.replace(/\\n/g, '\\\\n');\n"
            + "    return txt;\n"
            + "  }\n";

    /**
     * Helper.
     * @param doc the doc parameter
     * @return XML helper
     */
    public static String callSerializeXMLDOMDocumentToString(final String doc) {
        return SERIALIZE_XMLDOMDOCUMENT_TO_STRING_FUNCTION_NAME + "(" + doc + ")";
    }

    /**
     * @param scriptContent the header script content to test
     * @return the HTML page
     */
    public static String createTestHTML(final String scriptContent) {
        return HtmlPageTest.STANDARDS_MODE_PREFIX_
            + "<html><head><script>\n"
            + scriptContent
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";
    }

    private MSXMLTestHelper() {
        super();
    }
}
