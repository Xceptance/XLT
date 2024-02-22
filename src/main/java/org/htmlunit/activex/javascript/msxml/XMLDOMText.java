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

import org.htmlunit.html.DomText;
import org.htmlunit.javascript.JavaScriptEngine;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxFunction;

/**
 * A JavaScript object for MSXML's (ActiveX) XMLDOMText.<br>
 * Represents the text content of an element or attribute.
 * @see <a href="http://msdn.microsoft.com/en-us/library/ms757862.aspx">MSDN documentation</a>
 *
 * @author David K. Taylor
 * @author Chris Erskine
 * @author Ahmed Ashour
 * @author Chuck Dumont
 * @author Ronald Brill
 * @author Frank Danek
 */
@JsxClass(domClass = DomText.class, value = IE)
public class XMLDOMText extends XMLDOMCharacterData {

    /**
     * Returns the text contained in the node.
     * @return the text contained in the node
     */
    @Override
    public Object getText() {
        final DomText domText = getDomNodeOrDie();
        return domText.getWholeText();
    }

    /**
     * Splits this text node into two text nodes at the specified offset and inserts the new text node into the tree
     * as a sibling that immediately follows this node.
     * @param offset the number of characters at which to split this text node into two nodes, starting from zero
     * @return the new text node
     */
    @JsxFunction
    public Object splitText(final int offset) {
        if (offset < 0) {
            throw JavaScriptEngine.reportRuntimeError(
                    "The offset must be 0 or a positive number that is not greater than the "
                    + "number of characters in the data.");
        }

        final DomText domText = getDomNodeOrDie();
        if (offset > domText.getLength()) {
            throw JavaScriptEngine.reportRuntimeError(
                    "The offset must be 0 or a positive number that is not greater than the "
                    + "number of characters in the data.");
        }

        return getScriptableFor(domText.splitText(offset));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DomText getDomNodeOrDie() {
        return (DomText) super.getDomNodeOrDie();
    }
}
