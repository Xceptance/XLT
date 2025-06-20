package com.xceptance.xlt.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Objects;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration.PropertiesReader;
import org.apache.commons.configuration2.PropertiesConfiguration.PropertiesWriter;
import org.apache.commons.configuration2.convert.DisabledListDelimiterHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.common.XltConstants;

/**
 * Utility class used to mask secret properties.
 */
public class SecretPropertiesMask implements AutoCloseable
{
    private final Reader reader;

    private final Writer writer;

    /**
     * Creates a new instance that reads from the given reader and writes its output to the given writer.
     *
     * @param reader
     *            the reader to read properties from
     * @param writer
     *            the writer to write masked properties to
     */
    public SecretPropertiesMask(final Reader reader, final Writer writer)
    {
        this.reader = Objects.requireNonNull(reader);
        this.writer = Objects.requireNonNull(writer);
    }

    /**
     * Masks secret properties read by the instance's reader and writes the outcome to the instance's writer.
     *
     * @param maskAllProperties
     *            whether to mask all properties or only those prefixed with {@value XltConstants#SECRET_PREFIX}.
     * @throws IOException
     */
    public void maskProperties(final boolean maskAllProperties) throws IOException
    {
        final PropertiesReader propsReader = new PropertiesConfiguration.JupPropertiesReader(reader);
        @SuppressWarnings("resource")
        final PropertiesWriter propsWriter = new PropertiesConfiguration.JupPropertiesWriter(writer, new DisabledListDelimiterHandler(),
                                                                                             true);

        while (propsReader.nextProperty())
        {
            final List<String> commentLines = propsReader.getCommentLines();
            if (commentLines != null)
            {
                for (final String line : commentLines)
                {
                    propsWriter.writeln(line);
                }
            }

            final String propName = propsReader.getPropertyName();
            final String propValue = (maskAllProperties || shouldMask(propName)) ? XltConstants.MASK_PROPERTIES_HIDETEXT
                                                                                 : propsReader.getPropertyValue();
            propsWriter.setCurrentSeparator(propsReader.getPropertySeparator());
            propsWriter.writeProperty(propName, propValue);
        }

        final List<String> remainder = propsReader.getCommentLines();
        if (remainder != null)
        {
            for (final String line : remainder)
            {
                propsWriter.writeln(line);
            }
        }

    }

    /**
     * Returns whether the given key should be masked.
     *
     * @param key
     *            the property key
     * @return {@code true} if the given key should be masked, {@code false} otherwise
     */
    protected boolean shouldMask(final String key)
    {
        return StringUtils.startsWith(key, XltConstants.SECRET_PREFIX);
    }

    /**
     * Closes the underlying reader and writer instances.
     */
    @Override
    public void close()
    {
        IOUtils.closeQuietly(this.reader, this.writer);
    }
}
