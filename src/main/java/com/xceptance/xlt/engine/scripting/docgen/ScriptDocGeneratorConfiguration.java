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
package com.xceptance.xlt.engine.scripting.docgen;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.xceptance.common.util.AbstractConfiguration;
import com.xceptance.xlt.engine.XltExecutionContext;

/**
 * Configuration used by {@link ScriptDocGenerator}.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class ScriptDocGeneratorConfiguration extends AbstractConfiguration
{
    private final Map<String, String> templates;

    private final File templateDir;

    private final File resourceDir;

    public ScriptDocGeneratorConfiguration(final Properties commandLineProperties) throws IOException
    {
        final File configDirectory = XltExecutionContext.getCurrent().getXltConfigDir();

        {
            loadProperties(new File(configDirectory, "scriptdocgenerator.properties"));
        }

        {
            if (commandLineProperties != null)
            {
                addProperties(commandLineProperties);
            }
        }

        File templateDirectory = getFileProperty("com.xceptance.xlt.scriptdocgenerator.templateDirectory");
        if (!templateDirectory.isAbsolute())
        {
            templateDirectory = new File(configDirectory, templateDirectory.getPath());
        }

        isReadableDir(templateDirectory, "Template directory");

        File resourceDirectory = getFileProperty("com.xceptance.xlt.scriptdocgenerator.resourceDirectory");
        if (!resourceDirectory.isAbsolute())
        {
            resourceDirectory = new File(configDirectory, resourceDirectory.getPath());
        }

        isReadableDir(resourceDirectory, "Resource directory");

        templateDir = templateDirectory;
        resourceDir = resourceDirectory;

        templates = readTemplateMapping();
    }

    private Map<String, String> readTemplateMapping()
    {
        final HashMap<String, String> mapping = new HashMap<String, String>();
        final String prefix = "com.xceptance.xlt.scriptdocgenerator.templates.";

        for (final String s : getPropertyKeyFragment(prefix))
        {
            final String prop = prefix + s;
            final File templateFile = getFileProperty(prop + ".templateFileName");
            final File outputFile = getFileProperty(prop + ".outputFileName");
            mapping.put(templateFile.getPath(), outputFile.getPath());

        }
        return mapping;
    }

    /**
     * @return the templateDir
     */
    public File getTemplateDir()
    {
        return templateDir;
    }

    /**
     * @return the resourceDir
     */
    public File getResourceDir()
    {
        return resourceDir;
    }

    /**
     * @return the templates
     */
    public Map<String, String> getTemplates()
    {
        return templates;
    }

    /**
     * Tests if the given directory exists, is a directory and can be read.
     * 
     * @param dir
     *            the directory to check
     * @param dirDesc
     *            the description of the given directory
     * @throws IllegalArgumentException
     *             when the given directory does not exist, is not a directory or cannot be read
     */
    private static void isReadableDir(final File dir, final String dirDesc)
    {
        if (!dir.exists())
        {
            throw new IllegalArgumentException(dirDesc + " '" + dir.getAbsolutePath() + "' does not exist.");
        }
        if (!dir.isDirectory())
        {
            throw new IllegalArgumentException(dirDesc + " '" + dir.getAbsolutePath() + "' is not a directory.");
        }
        if (!dir.canRead())
        {
            throw new IllegalArgumentException(dirDesc + " '" + dir.getAbsolutePath() + "' cannot be read.");
        }
    }
}
