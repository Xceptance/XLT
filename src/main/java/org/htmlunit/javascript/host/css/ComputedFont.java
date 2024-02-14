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
package org.htmlunit.javascript.host.css;

import static org.htmlunit.BrowserVersionFeatures.CSS_SET_NULL_THROWS;
import static org.htmlunit.javascript.host.css.CSSStyleDeclaration.isLength;

import org.htmlunit.BrowserVersion;
import org.htmlunit.util.StringUtils;

/**
 * A helper class for handling font attributes of {@link ComputedCSSStyleDeclaration}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
final class ComputedFont {

    // static final int FONT_STYLE_INDEX = 0;
    // static final int FONT_WEIGHT_INDEX = 1;
    // static final int FONT_STRETCH_INDEX = 2;

    static final int FONT_SIZE_INDEX = 3;
    static final int LINE_HEIGHT_INDEX = 4;
    static final int FONT_FAMILY_INDEX = 5;

    static String[] getDetails(final String font, final BrowserVersion browserVersion) {
        String fontName = font;
        while (fontName.contains("  ")) {
            fontName = fontName.replace("  ", " ");
        }
        if (browserVersion.hasFeature(CSS_SET_NULL_THROWS)
                && fontName.contains("/ ")) {
            return null;
        }
        final String[] tokens = StringUtils.splitAtBlank(fontName);
        if (tokens.length > 1) {
            final String[] fontSizeDetails = getFontSizeDetails(tokens[tokens.length - 2]);
            if (fontSizeDetails == null) {
                return null;
            }
            final String[] details = new String[6];
            details[FONT_SIZE_INDEX] = fontSizeDetails[0];
            details[LINE_HEIGHT_INDEX] = fontSizeDetails[1];
            details[FONT_FAMILY_INDEX] = tokens[tokens.length - 1];
            return details;
        }
        return null;
    }

    /**
     * @return an array of {@code fontSize} and {@code lineHeight}, or {@code null} if invalid
     */
    private static String[] getFontSizeDetails(final String fontSize) {
        final int slash = fontSize.indexOf('/');
        final String actualFontSize = slash == -1 ? fontSize : fontSize.substring(0, slash);
        if (!isLength(actualFontSize)) {
            return null;
        }

        String actualLineHeight = slash == -1 ? "" : fontSize.substring(slash + 1);
        if (actualLineHeight.isEmpty()) {
            actualLineHeight = null;
        }
        else if (!isValidLineHeight(actualLineHeight)) {
            return null;
        }
        return new String[] {actualFontSize, actualLineHeight};
    }

    private static boolean isValidLineHeight(final String lineHeight) {
        return isLength(lineHeight) || "normal".equals(lineHeight);
    }

    private ComputedFont() {
    }

}
