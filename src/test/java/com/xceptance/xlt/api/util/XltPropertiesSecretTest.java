package com.xceptance.xlt.api.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.xceptance.common.io.FileUtils;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.XltExecutionContext;
import com.xceptance.xlt.util.XltPropertiesImpl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases specifically concerned with the loading of secret properties
 */
public class XltPropertiesSecretTest {

    /**
     * XltProperties test instance.
     */
    protected XltProperties instance = null;
    protected Path tempDir = null;

    @Before
    public void createTestProfile() throws IOException
    {
        tempDir = Files.createTempDirectory("secret-loading-test-");
        final Path configDir = tempDir.resolve(XltConstants.CONFIG_DIR_NAME);
        Files.createDirectories(configDir);
        Files.write(configDir.resolve(XltConstants.SECRET_PROPERTIES_FILENAME), "str=SomeValue\nsecret.value=another Value\n".getBytes(StandardCharsets.ISO_8859_1));
        XltExecutionContext.getCurrent().setTestSuiteConfigDir(configDir.toFile());
        XltPropertiesImpl.reset();
        instance = XltProperties.getInstance();
    }

    @After
    public void cleanupTestProfile() throws IllegalArgumentException, IOException
    {
        if (tempDir != null)
        {
            FileUtils.deleteDirectoryRelaxed(tempDir.toFile());
        }
    }

    @Test
    public void testContainsKeyLooksForSecretKeyAsWell()
    {
        instance.setProperty("secret.prop", "Some value");

        Assert.assertTrue(instance.containsKey("secret.prop"));
        Assert.assertTrue(instance.containsKey("prop"));
    }

    @Test
    public void testLoadingSecretPropertiesFromFiles()
    {
        Assert.assertTrue(instance.containsKey("secret.str"));
        Assert.assertTrue(instance.containsKey("str"));
        Assert.assertTrue(instance.containsKey("secret.value"));
        Assert.assertTrue(instance.containsKey("value"));
    }
}
