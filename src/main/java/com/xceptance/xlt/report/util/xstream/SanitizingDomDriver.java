package com.xceptance.xlt.report.util.xstream;

import java.io.Writer;

import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * A custom {@link DomDriver} that uses a {@link SanitizingWriter} to write an XML file.
 */
public class SanitizingDomDriver extends DomDriver
{
    @Override
    public HierarchicalStreamWriter createWriter(final Writer out)
    {
        return new SanitizingWriter(out, getNameCoder());
    }
}