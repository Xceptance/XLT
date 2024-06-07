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