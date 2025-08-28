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
package com.xceptance.xlt.util;

import com.xceptance.common.util.ParameterCheckUtils;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * File handler for setting key-value pairs in a property file.
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
     * <p>
     * Set the property with the given key to the given value. If the property with the given key already exists in the
     * file it will be replaced. Other properties will remain unchanged.
     * </p>
     * <p>
     * If the given property key or value are either null or blank, the property file won't be edited at all.
     * </p>
     *
     * @param propertyKey
     *            property key
     * @param propertyValue
     *            property value
     */
    public void setProperty(final String propertyKey, final String propertyValue)
    {
        if (StringUtils.isNotBlank(propertyKey) && StringUtils.isNotBlank(propertyValue))
        {
            setProperties(Map.of(propertyKey, propertyValue));
        }
    }

    /**
     * <p>
     * Set the properties as described by the given key-value pairs. If a property with any of the given keys already
     * exists in the file it will be replaced. Other properties will remain unchanged.
     * </p>
     * <p>
     * Properties with keys or values that are null or blank will be skipped.
     * </p>
     *
     * @param propertyMap
     *            map of property keys and their associated values
     */
    public void setProperties(final Map<String, String> propertyMap)
    {
        ParameterCheckUtils.isNotNull(propertyMap, "propertyMap");

        final PropertiesConfiguration config = new PropertiesConfiguration();
        config.setIOFactory(new PropertiesConfiguration.JupIOFactory()); // for better compatibility with
                                                                         // java.util.Properties (GH#144)
        final FileHandler fileHandler = new FileHandler(config);

        try
        {
            fileHandler.load(this.propertyFile);

            propertyMap.forEach((key, value) -> {
                if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value))
                {
                    config.setProperty(key, value);
                }
            });

            fileHandler.save(this.propertyFile);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to update the properties in '" + propertyFile.getName() + "'.", e);
        }
    }

    /**
     * <p>
     * Append the given properties as a block at the bottom of the property file. A new line and an (optional) comment
     * will be added above the appended properties.
     * </p>
     * <p>
     * Example of the resulting structure:
     * </p>
     * 
     * <pre>{@code
     * ...
     * test.existingProp1 = value1
     * test.existingProp2 = value2
     *
     * # Your Comment
     * test.appendedProp1 = value3
     * test.appendedProp2 = value4
     * }
     * </pre>
     * <p>
     * The appended properties will be added in alphabetical order. Existing properties aren't edited or cleared, so
     * appending properties with this method can result in duplicate keys if any of the appended keys already exist in
     * the file.
     * </p>
     * <p>
     * Properties with keys or values that are null or blank will be skipped.
     * </p>
     *
     * @param propertyMap
     *            map of property keys and values to append
     * @param comment
     *            comment to add above the appended property block; if null or blank, no comment is added
     */
    public void appendProperties(final Map<String, String> propertyMap, final String comment)
    {
        ParameterCheckUtils.isNotNull(propertyMap, "propertyMap");

        // prepare and sort property lines
        final List<String> linesToAppend = new ArrayList<>();
        propertyMap.forEach((key, value) -> {
            if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value))
            {
                linesToAppend.add(key + " = " + value);
            }
        });
        Collections.sort(linesToAppend);

        // add new line and comment (optional) above the block
        if (StringUtils.isNotBlank(comment))
        {
            linesToAppend.addFirst("# " + comment);
        }
        linesToAppend.addFirst("");

        try
        {
            Files.write(propertyFile.toPath(), linesToAppend, StandardOpenOption.APPEND);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to append properties to file '" + propertyFile.getName() + "'.", e);
        }
    }
}
