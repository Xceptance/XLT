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

import org.htmlunit.html.DomNode;
import org.htmlunit.javascript.configuration.JsxClass;

/**
 * A JavaScript object for MSXML's (ActiveX) XMLDOMSelection.<br>
 * Represents the list of nodes that match a given XML Path Language (XPath) expression.
 * @see <a href="http://msdn.microsoft.com/en-us/library/ms759171.aspx">MSDN documentation</a>
 *
 * @author Frank Danek
 */
@JsxClass(IE)
public class XMLDOMSelection extends XMLDOMNodeList {

    /**
     * Creates an instance.
     */
    public XMLDOMSelection() {
    }

    /**
     * Creates an instance.
     * @param parentScope parent scope
     * @param attributeChangeSensitive indicates if the content of the collection may change when an attribute
     * of a descendant node of parentScope changes (attribute added, modified or removed)
     * @param description a text useful for debugging
     */
    public XMLDOMSelection(final DomNode parentScope, final boolean attributeChangeSensitive,
            final String description) {
        super(parentScope, attributeChangeSensitive, description);
    }
}
