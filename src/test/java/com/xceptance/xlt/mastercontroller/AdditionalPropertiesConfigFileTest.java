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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.powermock.api.mockito.PowerMockito;

import com.xceptance.xlt.common.XltConstants;

/**
 * Tests the commandline parameter to add a new propertiefile
 */
public class AdditionalPropertiesConfigFileTest
{
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private File additionalProperties;

    @Before
    public void init() throws IOException
    {
        additionalProperties = tempFolder.newFile();
    }

    @Test
    public void parseCommandLine() throws Exception
    {
        final String filePath = additionalProperties.getAbsolutePath();
        final String[] args =
            {
                "-pf", filePath
            };

        final MasterControllerMain main = new MasterControllerMain();
        final CommandLine commandLine = main.parseCommandLine(args, main.createCommandLineOptions());

        Assert.assertEquals("Parsed file path does not match expected one.", filePath, commandLine.getOptionValue("pf"));
    }

    @Test
    public void getOverridePropertieFile() throws Exception
    {
        final String filePath = additionalProperties.getAbsolutePath();

        final CommandLine commandLine = PowerMockito.mock(CommandLine.class);
        PowerMockito.when(commandLine, "getOptionValue", XltConstants.COMMANDLINE_OPTION_PROPERTY_FILENAME).thenReturn(filePath);

        final File file = new MasterControllerMain().getOverridePropertiesFile(commandLine);
        Assert.assertEquals("Parsed file path does not match expected one.", new File(filePath), file);
    }

    @Test
    public void getOverridePropertieFile_null() throws Exception
    {
        final String filePath = null;

        final CommandLine commandLine = PowerMockito.mock(CommandLine.class);
        PowerMockito.when(commandLine, "getOptionValue", XltConstants.COMMANDLINE_OPTION_PROPERTY_FILENAME).thenReturn(filePath);

        final File file = new MasterControllerMain().getOverridePropertiesFile(commandLine);
        Assert.assertEquals("Parsed file path does not match expected one.", filePath, file);
    }

    @Test
    public void getOverridePropertieFile_empty() throws Exception
    {
        final String filePath = "";

        final CommandLine commandLine = PowerMockito.mock(CommandLine.class);
        PowerMockito.when(commandLine, "getOptionValue", XltConstants.COMMANDLINE_OPTION_PROPERTY_FILENAME).thenReturn(filePath);

        final File file = new MasterControllerMain().getOverridePropertiesFile(commandLine);
        Assert.assertEquals("Parsed file path does not match expected one.", null, file);
    }

    @Test
    public void getOverridePropertieFile_blank() throws Exception
    {
        final String filePath = "   ";

        final CommandLine commandLine = PowerMockito.mock(CommandLine.class);
        PowerMockito.when(commandLine, "getOptionValue", XltConstants.COMMANDLINE_OPTION_PROPERTY_FILENAME).thenReturn(filePath);

        final File file = new MasterControllerMain().getOverridePropertiesFile(commandLine);
        Assert.assertEquals("Parsed file path does not match expected one.", null, file);
    }

    @Test
    public void newProperty() throws Exception
    {
        // property to override
        final String testProperty = "com.xceptance.xlt.mastercontroller.agentcontrollers.ac001.url";

        // look into the real master controller properties to get existing value
        final Properties realProperties = new Properties();
        realProperties.load(new FileReader("config/mastercontroller.properties"));
        final String origValue = realProperties.getProperty(testProperty);

        // check there is a base property and also check that it's different to our custom value
        Assert.assertNotNull("No such value to override.", origValue);
        Assert.assertNotEquals("Property values must differ.", origValue, testProperty);

        // set our custom property value
        final String newValue = "http://" + RandomStringUtils.randomAlphabetic(20) + ".org";
        final FileWriter writer = new FileWriter(additionalProperties);
        writer.write(testProperty + " = " + newValue);
        writer.close();

        // read config
        final MasterControllerConfiguration config = new MasterControllerConfiguration(additionalProperties, new Properties(), true);

        // get property
        final String actual = config.getAgentControllerConnectionInfos().get(0).getUrl().toExternalForm();

        // check we read the custom value
        Assert.assertEquals("Actual property value does not match expected one.", newValue, actual);
    }
}
