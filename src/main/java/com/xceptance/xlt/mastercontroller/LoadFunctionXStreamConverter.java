/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
