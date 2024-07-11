/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.report.util.xstream;

import java.io.Writer;

import org.apache.commons.text.StringEscapeUtils;

import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

/**
 * A custom {@link PrettyPrintWriter} that silently removes invalid XML 1.0 characters when writing text nodes.
 */
class SanitizingWriter extends PrettyPrintWriter
{
    public SanitizingWriter(final Writer writer, final NameCoder nameCoder)
    {
        super(writer, nameCoder);
    }

    @Override
    protected void writeText(final QuickWriter writer, final String text)
    {
        // escape special chars and remove invalid chars
        final String sanitizedText = StringEscapeUtils.escapeXml10(text);

        // don't call super.writeText() as this would escape the already escaped chars once more
        writer.write(sanitizedText);
    }
}