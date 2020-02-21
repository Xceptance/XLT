package com.xceptance.xlt.report.util;

import java.util.Map;
import java.util.Map.Entry;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * A custom XStream converter that converts any class implementing {@link Map} in such a way that map entries are
 * streamed as <code>&lt;key>value&lt;/key></code>. Note that this converter supports streaming only, but not parsing.
 */
public class CustomMapConverter implements Converter
{
    public boolean canConvert(@SuppressWarnings("rawtypes") Class type)
    {
        return Map.class.isAssignableFrom(type);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        Map<?, ?> map = (Map<?, ?>) source;
        for (Entry<?, ?> entry : map.entrySet())
        {
            writer.startNode(String.valueOf(entry.getKey()));
            writer.setValue(String.valueOf(entry.getValue()));
            writer.endNode();
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        throw new UnsupportedOperationException();
    }
}
