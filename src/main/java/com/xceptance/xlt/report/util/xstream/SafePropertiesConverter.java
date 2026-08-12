/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.Map;
import java.util.Properties;

/**
 * XStream converter for {@link java.util.Properties} that avoids security issues with the default converter. It doesn't
 * use reflection so we don't need to open the java.util package for inspection. On the downside, this converter is not
 * able to stream any default properties, but this is often not needed.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 * @since 10.0.0
 */
public class SafePropertiesConverter implements Converter
{
    @Override
    public boolean canConvert(Class type)
    {
        return Properties.class.isAssignableFrom(type);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        Properties properties = (Properties) source;
        for (Map.Entry<Object, Object> entry : properties.entrySet())
        {
            writer.startNode("property");
            writer.addAttribute("name", entry.getKey().toString());
            writer.addAttribute("value", entry.getValue().toString());
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        Properties properties = new Properties();

        while (reader.hasMoreChildren())
        {
            reader.moveDown();
            String name = reader.getAttribute("name");
            String value = reader.getAttribute("value");
            properties.setProperty(name, value);
            reader.moveUp();
        }
        return properties;
    }
}
