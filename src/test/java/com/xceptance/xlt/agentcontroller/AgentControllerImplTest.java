package com.xceptance.xlt.agentcontroller;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import com.xceptance.common.util.zip.ZipUtils;
import com.xceptance.xlt.common.XltConstants;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for masking properties while packing result sets
 */
public class AgentControllerImplTest
{
    @Test
    public void testSecretPropertiesMustBeMasked() throws Exception
    {
        final Path testDir = Files.createTempDirectory("ziptest-");
        final Path configDir = Path.of(testDir.toString(), "mytestagent", "config");
        Files.createDirectories(configDir);
        final Path testFile = Files.createTempFile(configDir, "test-config-", ".properties");
        final Path unzipDir = Files.createTempDirectory("unziptest-");
        String outputFile = null;

        try
        {
            final Properties props = new Properties();
            props.setProperty("secret.value", "This is a secret");
            props.setProperty("public.value", "This is not");
            final BufferedWriter writer = Files.newBufferedWriter(testFile, StandardOpenOption.CREATE);
            props.store(writer, "");
            writer.close();

            final Properties commandLineProps = new Properties();
            commandLineProps.setProperty("com.xceptance.xlt.agentcontroller.agentsdir", testDir.toString());
            final AgentControllerImpl controller = new AgentControllerImpl(commandLineProps);

            outputFile = controller.archiveTestConfig();

            // unzip and check for the correct contents
            ZipUtils.unzipFile(new File(System.getProperty("java.io.tmpdir"), outputFile), unzipDir.toFile());

            final Properties restoredProps = new Properties();
            restoredProps.load(Files.newBufferedReader(Path.of(unzipDir.toString(), "config", testFile.getFileName().toString())));

            Assert.assertEquals("This is not", restoredProps.getProperty("public.value"));
            Assert.assertEquals(XltConstants.MASK_PROPERTIES_HIDETEXT, restoredProps.getProperty("secret.value"));
        }
        finally
        {
            FileUtils.deleteDirectory(testDir.toFile());
            if (outputFile != null)
            {
                FileUtils.deleteQuietly(new File(outputFile));
            }
            FileUtils.deleteDirectory(unzipDir.toFile());
        }
    }
}
