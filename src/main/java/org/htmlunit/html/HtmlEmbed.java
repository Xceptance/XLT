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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.htmlunit.SgmlPage;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;

/**
 * Wrapper for the HTML element "embed".
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Frank Danek
 */
public class HtmlEmbed extends HtmlElement {

    /** The HTML tag represented by this element. */
    public static final String TAG_NAME = "embed";

    /**
     * Creates a new instance.
     *
     * @param qualifiedName the qualified name of the element type to instantiate
     * @param page the page that contains this element
     * @param attributes the initial attributes
     */
    HtmlEmbed(final String qualifiedName, final SgmlPage page,
            final Map<String, DomAttr> attributes) {
        super(qualifiedName, page, attributes);
    }

    /**
     * Saves this content as the specified file.
     * @param file the file to save to
     * @throws IOException if an IO error occurs
     */
    public void saveAs(final File file) throws IOException {
        final HtmlPage page = (HtmlPage) getPage();
        final WebClient webclient = page.getWebClient();

        final URL url = page.getFullyQualifiedUrl(getAttributeDirect(SRC_ATTRIBUTE));
        final WebRequest request = new WebRequest(url, page.getCharset(), page.getUrl());
        final WebResponse webResponse = webclient.loadWebResponse(request);

        try (OutputStream fos = Files.newOutputStream(file.toPath());
                InputStream content =  webResponse.getContentAsStream()) {
            IOUtils.copy(content, fos);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DisplayStyle getDefaultStyleDisplay() {
        return DisplayStyle.INLINE;
    }
}
