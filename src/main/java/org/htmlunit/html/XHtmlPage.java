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
package org.htmlunit.html;

import org.htmlunit.WebResponse;
import org.htmlunit.WebWindow;

/**
 * A representation of an XHTML page returned from a server.
 *
 * @author Daniel Gredler
 */
public class XHtmlPage extends HtmlPage {

    /**
     * Creates a new XHTML page instance. An XHTML page instance is normally retrieved
     * with {@link org.htmlunit.WebClient#getPage(String)}.
     *
     * @param webResponse the web response that was used to create this page
     * @param webWindow the window that this page is being loaded into
     */
    public XHtmlPage(final WebResponse webResponse, final WebWindow webWindow) {
        super(webResponse, webWindow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasCaseSensitiveTagNames() {
        return true;
    }

}
