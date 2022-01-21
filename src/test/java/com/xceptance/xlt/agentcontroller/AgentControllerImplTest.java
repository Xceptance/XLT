/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
        final Path configDir = testDir.resolve("mytestagent").resolve("config");
        Files.createDirectories(configDir);
        final Path testFile = Files.createTempFile(configDir, "test-config-", ".properties");
        final File secretsFile = new File(configDir.toFile(), XltConstants.SECRET_PROPERTIES_FILENAME);
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

            final Properties secretProps = new Properties();
            secretProps.setProperty("secret.prop", "This is a secret value");
            secretProps.setProperty("public.prop", "This is also secret because it's in the secrets file");
            final BufferedWriter secretWriter = Files.newBufferedWriter(secretsFile.toPath(), StandardOpenOption.CREATE);
            secretProps.store(secretWriter, "");
            secretWriter.close();


            final Properties commandLineProps = new Properties();
            commandLineProps.setProperty("com.xceptance.xlt.agentcontroller.agentsdir", testDir.toString());
            final AgentControllerImpl controller = new AgentControllerImpl(commandLineProps);

            outputFile = controller.archiveTestConfig();

            // unzip and check for the correct contents
            ZipUtils.unzipFile(new File(System.getProperty("java.io.tmpdir"), outputFile), unzipDir.toFile());

            final Properties restoredProps = new Properties();
            restoredProps.load(Files.newBufferedReader(unzipDir.resolve("config").resolve(testFile.getFileName().toString())));

            Assert.assertEquals("This is not", restoredProps.getProperty("public.value"));
            Assert.assertEquals(XltConstants.MASK_PROPERTIES_HIDETEXT, restoredProps.getProperty("secret.value"));

            final Properties restoredSecretProps = new Properties();
            restoredSecretProps.load(Files.newBufferedReader(unzipDir.resolve("config").resolve(XltConstants.SECRET_PROPERTIES_FILENAME)));

            Assert.assertEquals(XltConstants.MASK_PROPERTIES_HIDETEXT, restoredSecretProps.getProperty("public.prop"));
            Assert.assertEquals(XltConstants.MASK_PROPERTIES_HIDETEXT, restoredSecretProps.getProperty("secret.prop"));
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
