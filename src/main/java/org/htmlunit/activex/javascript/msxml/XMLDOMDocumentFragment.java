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

import static org.htmlunit.javascript.configuration.SupportedBrowser.IE;

import org.htmlunit.html.DomDocumentFragment;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.configuration.JsxClass;

/**
 * A JavaScript object for MSXML's (ActiveX) XMLDOMDocumentFragment.<br>
 * A lightweight object that is useful for tree insert operations.
 * @see <a href="http://msdn.microsoft.com/en-us/library/ms759155.aspx">MSDN documentation</a>
 *
 * @author Ahmed Ashour
 * @author Frank Danek
 */
@JsxClass(domClass = DomDocumentFragment.class, value = IE)
public class XMLDOMDocumentFragment extends XMLDOMNode {

    /**
     * Attempting to set the value of document fragments generates an error.
     * @param newValue the new value to set
     */
    @Override
    public void setNodeValue(final String newValue) {
        if (newValue == null || "null".equals(newValue)) {
            throw JavaScriptEngine.reportRuntimeError("Type mismatch.");
        }
        throw JavaScriptEngine.reportRuntimeError("This operation cannot be performed with a node of type DOCFRAG.");
    }

    /**
     * Attempting to set the text of document fragments generates an error.
     * @param value the new value for the contents of this node
     */
    @Override
    public void setText(final Object value) {
        if (value == null || "null".equals(value)) {
            throw JavaScriptEngine.reportRuntimeError("Type mismatch.");
        }
        throw JavaScriptEngine.reportRuntimeError("This operation cannot be performed with a node of type DOCFRAG.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getXml() {
        Object xml = super.getXml();
        if (xml instanceof String) {
            String xmlString = (String) xml;
            if (xmlString.indexOf('\n') >= 0) {
                xmlString = xmlString.replaceAll("([^\r])\n", "$1\r\n");
                xmlString = xmlString.replaceAll(">\r\n\\s*", ">");
                xml = xmlString;
            }
        }
        return xml;
    }

}
