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

import static org.htmlunit.BrowserVersionFeatures.CSS_DISPLAY_BLOCK2;

import java.util.Map;

import org.htmlunit.SgmlPage;

/**
 * HTML 5 "meter" element.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/HTML/Element/meter">MDN documentation</a>
 * @author Marc Guillemot
 * @author Ronald Brill
 * @author Frank Danek
 */
public class HtmlMeter extends HtmlMedia implements LabelableElement {

    /** The HTML tag represented by this element. */
    public static final String TAG_NAME = "meter";

    /**
     * Creates an instance of HtmlMeter
     *
     * @param qualifiedName the qualified name of the element type to instantiate
     * @param page the HtmlPage that contains this element
     * @param attributes the initial attributes
     */
    HtmlMeter(final String qualifiedName, final SgmlPage page,
            final Map<String, DomAttr> attributes) {
        super(qualifiedName, page, attributes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DisplayStyle getDefaultStyleDisplay() {
        if (hasFeature(CSS_DISPLAY_BLOCK2)) {
            return DisplayStyle.INLINE_BLOCK;
        }
        return DisplayStyle.INLINE;
    }
}
