package com.xceptance.xlt.mastercontroller;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * This class is designed to convert between string representation and the int[][] representation of load function.
 * 
 * @author Sebastian Oerding
 */
public class LoadFunctionXStreamConverter implements Converter
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
        int minimum = Integer.MAX_VALUE;
        int maximum = 0;
        for (final int[] array : loadFunction)
        {
            final int users = array[1];
            if (users < minimum)
            {
                minimum = users;
            }
            if (users > maximum)
            {
                maximum = users;
            }
        }

        // format the value(s)
        String s;
        if (minimum == maximum)
        {
            s = String.format("%,d", minimum);
        }
        else
        {
            s = String.format("%,d...%,d", minimum, maximum);
        }

        writer.setValue(s);
    }

    @Override
    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context)
    {
        throw new UnsupportedOperationException("There is no way to convert back from the condensed representation of a load function!");
    }
}
