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
package org.htmlunit.platform.font;

import org.apache.commons.lang3.StringUtils;

/**
 * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br>
 *
 * Simple no op {@link FontUtil} implementation.
 *
 * @author Ronald Brill
 */
public class NoOpFontUtil implements FontUtil {
    @Override
    public int countLines(final String content, final int pixelWidth, final String fontSize) {
        final String[] lines = StringUtils.split(content, '\n');
        return lines.length;
    }
}
