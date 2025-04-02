package com.xceptance.xlt.util;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.io.FileHandler;

import java.io.File;
import java.util.Map;

/**
 * File handler for setting name-value pairs in a property file.
 */
public class PropertyFileHandler
{
    private final File propertyFile;

    /**
     * Creates a new property file handler for editing the given property file.
     *
     * @param propertyFile
     *            the property file to edit
     */
    public PropertyFileHandler(File propertyFile)
    {
        this.propertyFile = propertyFile;
    }

    /**
     * Set the property with the given name to the given value. If the property with the given name already exists in
     * the file it will be replaced. Other properties will remain unchanged.
     *
     * @param propertyName
     *            property name
     * @param propertyValue
     *            property value
     */
    public void setProperty(final String propertyName, final String propertyValue)
    {
        setProperties(Map.of(propertyName, propertyValue));
    }

    /**
     * Set the properties as described by the given name-value pairs. If a property with any of the given names already
     * exists in the file it will be replaced. Other properties will remain unchanged.
     *
     * @param propertyMap
     *            map of property names and their associated values
     */
    public void setProperties(final Map<String, String> propertyMap)
    {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        config.setIOFactory(new PropertiesConfiguration.JupIOFactory()); // for better compatibility with
                                                                         // java.util.Properties (GH#144)
        final FileHandler fileHandler = new FileHandler(config);

        try
        {
            fileHandler.load(this.propertyFile);

            for (final String propertyName : propertyMap.keySet())
            {
                config.setProperty(propertyName, propertyMap.get(propertyName));
            }

            fileHandler.save(this.propertyFile);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to update the properties in '" + propertyFile.getName() + "'.", e);
        }

    }
}
