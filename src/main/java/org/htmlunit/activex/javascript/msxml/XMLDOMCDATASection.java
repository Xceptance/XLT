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

import org.htmlunit.html.DomCDataSection;
import org.htmlunit.javascript.configuration.JsxClass;

/**
 * A JavaScript object for MSXML's (ActiveX) XMLDOMCDATASection.<br>
 * Used to quote or escape blocks of text to keep that text from being interpreted as markup language.
 * @see <a href="http://msdn.microsoft.com/en-us/library/ms762780.aspx">MSDN documentation</a>
 *
 * @author Ahmed Ashour
 * @author Frank Danek
 */
@JsxClass(domClass = DomCDataSection.class, value = IE)
public final class XMLDOMCDATASection extends XMLDOMText {
}
