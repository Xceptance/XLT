package com.xceptance.xlt.mastercontroller;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * @author Sebastian Oerding
 */
public class ComplexLoadFunctionXStreamConverter implements Converter
{
    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") final Class clazz)
    {
        return int[][].class.equals(clazz);
    }

    @Override
    public void marshal(final Object value, final HierarchicalStreamWriter writer, final MarshallingContext context)
    {
        final int[][] loadFunction = (int[][]) value;
        writer.setValue(LoadFunctionUtils.loadFunctionToString(loadFunction));
    }

    @Override
    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context)
    {
        throw new UnsupportedOperationException("Unmarshaling a load function is not supported yet.");
    }
}
