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
