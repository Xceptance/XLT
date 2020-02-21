/**
 * 
 */
package com.xceptance.xlt.mastercontroller;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
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

    private File addtionalProperties;

    @Before
    public void init() throws IOException
    {
        addtionalProperties = tempFolder.newFile();
    }

    @Test
    public void parseCommandLine() throws Exception
    {
        final String filePath = addtionalProperties.getAbsolutePath();
        final String[] args =
            {
                "-pf", filePath
            };
        
        final Method parseCommandLine = Main.class.getDeclaredMethod("parseCommandLine", String[].class);
        parseCommandLine.setAccessible(true);
        final CommandLine commandLine = (CommandLine) parseCommandLine.invoke(null, new Object[]
            {
                args
            });

        Assert.assertEquals("Parsed file path does not match expected one.", filePath, commandLine.getOptionValue("pf"));
    }

    @Test
    public void getOverridePropertieFile() throws Exception
    {
        final String filePath = addtionalProperties.getAbsolutePath();
         
        final CommandLine commandLine = PowerMockito.mock(CommandLine.class);
        PowerMockito.when(commandLine, "getOptionValue", XltConstants.COMMANDLINE_OPTION_PROPERTY_FILENAME).thenReturn(filePath);
         
        final Method getOverridePropertieFile = Main.class.getDeclaredMethod("getOverridePropertieFile", CommandLine.class);
        getOverridePropertieFile.setAccessible(true);
        final File file = (File) getOverridePropertieFile.invoke(null, commandLine);
        Assert.assertEquals("Parsed file path does not match expected one.", new File(filePath), file);
    }

    @Test
    public void getOverridePropertieFile_null() throws Exception
    {
        final String filePath = null;

        final CommandLine commandLine = PowerMockito.mock(CommandLine.class);
        PowerMockito.when(commandLine, "getOptionValue", XltConstants.COMMANDLINE_OPTION_PROPERTY_FILENAME).thenReturn(filePath);

        final Method getOverridePropertieFile = Main.class.getDeclaredMethod("getOverridePropertieFile", CommandLine.class);
        getOverridePropertieFile.setAccessible(true);
        final File file = (File) getOverridePropertieFile.invoke(null, commandLine);
        Assert.assertEquals("Parsed file path does not match expected one.", filePath, file);
    }

    @Test
    public void getOverridePropertieFile_empty() throws Exception
    {
        final String filePath = "";

        final CommandLine commandLine = PowerMockito.mock(CommandLine.class);
        PowerMockito.when(commandLine, "getOptionValue", XltConstants.COMMANDLINE_OPTION_PROPERTY_FILENAME).thenReturn(filePath);

        final Method getOverridePropertieFile = Main.class.getDeclaredMethod("getOverridePropertieFile", CommandLine.class);
        getOverridePropertieFile.setAccessible(true);
        final File file = (File) getOverridePropertieFile.invoke(null, commandLine);
        Assert.assertEquals("Parsed file path does not match expected one.", null, file);
    }

    @Test
    public void getOverridePropertieFile_blank() throws Exception
    {
        final String filePath = "   ";

        final CommandLine commandLine = PowerMockito.mock(CommandLine.class);
        PowerMockito.when(commandLine, "getOptionValue", XltConstants.COMMANDLINE_OPTION_PROPERTY_FILENAME).thenReturn(filePath);

        final Method getOverridePropertieFile = Main.class.getDeclaredMethod("getOverridePropertieFile", CommandLine.class);
        getOverridePropertieFile.setAccessible(true);
        final File file = (File) getOverridePropertieFile.invoke(null, commandLine);
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
        final FileWriter writer = new FileWriter(addtionalProperties);
        writer.write(testProperty + " = " + newValue);
        writer.close();

        // read config
        final MasterControllerConfiguration config = new MasterControllerConfiguration(addtionalProperties, new Properties(), true);

        // get property
        final String actual = config.getAgentControllerConnectionInfos().get(0).getUrl().toExternalForm();

        // check we read the custom value
        Assert.assertEquals("Actual property value does not match expected one.", newValue, actual);
    }
}
