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
package com.xceptance.xlt.report.util;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * A custom XStream converter that converts any class implementing {@link Iterable} in such a way that its values are
 * streamed as <code>&lt;tag>value&lt;/tag></code> where the name of the tag can be customized.
 * <p>
 * Note that this converter supports streaming only, but not parsing.
 */
public class NamedIterableConverter implements Converter
{
    private final String tagName;

    protected NamedIterableConverter(final String tagName)
    {
        this.tagName = tagName;
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") final Class type)
    {
        return Iterable.class.isAssignableFrom(type);
    }

    @Override
    public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context)
    {
        final Iterable<?> iterable = (Iterable<?>) source;
        for (final Object value : iterable)
        {
            writer.startNode(tagName);
            writer.setValue(value.toString());
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context)
    {
        throw new UnsupportedOperationException();
    }
}
