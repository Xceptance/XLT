package com.xceptance.xlt.report.util.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.Map;
import java.util.Properties;

/**
 * XStream converter for java.util.Properties that avoids security issues with the default converter.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 * @since 10.0.1
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