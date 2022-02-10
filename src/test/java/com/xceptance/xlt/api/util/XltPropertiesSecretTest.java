/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import com.xceptance.common.io.FileUtils;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.SessionImpl;
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


    /**
     * Ensure that the hierarchy of properties is intact, i.e. props take precedence in the following order:
     *
     * 1. user-specific
     * 2. test-class specific
     * 3. bare property key
     *
     * In each case the secret version of a property takes precedence over the public version
     */
    @Test
    public void testHierarchyOfPropertiesIsIntact()
    {
        final SessionImpl session = SessionImpl.getCurrent();

        final String originalUserName = session.getUserName();
        final String originalTestClassName = session.getTestCaseClassName();

        instance.setProperty("prop", "Public");

        Assert.assertEquals("Public", instance.getProperty("prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX+originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX+originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX, "Not Found"));

        instance.setProperty(XltConstants.SECRET_PREFIX +"prop", "Secret");

        Assert.assertEquals("Secret", instance.getProperty("prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX+originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX+originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Secret", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Not Found"));

        instance.setProperty(originalTestClassName + ".prop", "Test Class");

        Assert.assertEquals("Test Class", instance.getProperty("prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX+originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Test Class", instance.getProperty(originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX+originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Secret", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Not Found"));

        instance.setProperty(XltConstants.SECRET_PREFIX + originalTestClassName + ".prop", "Secret Test Class");

        Assert.assertEquals("Secret Test Class", instance.getProperty("prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX+originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Secret Test Class", instance.getProperty(originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Secret Test Class", instance.getProperty(XltConstants.SECRET_PREFIX+originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Secret Test Class", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Not Found"));

        instance.setProperty(originalUserName + ".prop", "User Name");

        Assert.assertEquals("User Name", instance.getProperty("prop", "Not Found"));
        Assert.assertEquals("User Name", instance.getProperty(originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Not Found", instance.getProperty(XltConstants.SECRET_PREFIX+originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Secret Test Class", instance.getProperty(originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Secret Test Class", instance.getProperty(XltConstants.SECRET_PREFIX+originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Secret Test Class", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Not Found"));

        instance.setProperty(XltConstants.SECRET_PREFIX + originalUserName + ".prop", "Secret User Name");

        Assert.assertEquals("Secret User Name", instance.getProperty("prop", "Not Found"));
        Assert.assertEquals("Secret User Name", instance.getProperty(originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Secret User Name", instance.getProperty(XltConstants.SECRET_PREFIX+originalUserName+".prop", "Not Found"));
        Assert.assertEquals("Secret Test Class", instance.getProperty(originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Secret Test Class", instance.getProperty(XltConstants.SECRET_PREFIX+originalTestClassName+".prop", "Not Found"));
        Assert.assertEquals("Secret User Name", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Not Found"));
    }

    /**
     * This tests, whether the secret properties are available with and without
     * the "secret." prefix
     */
    @Test
    public void testGetSecretPropertiesCompatible()
    {
        instance.setProperty(XltConstants.SECRET_PREFIX+"myProp", "Some very secret value");

        Assert.assertEquals("Some very secret value", instance.getProperty(XltConstants.SECRET_PREFIX+"myProp", "Secret not found"));
        Assert.assertEquals("Some very secret value", instance.getProperty("myProp", "Normal not found"));
    }

    /**
     * Check whether secret props always take precendence over normal properties
     */
    @Test
    public void testSecretPropOverwritesPublicProp()
    {
        instance.setProperty(XltConstants.SECRET_PREFIX+"prop", "Secret");
        instance.setProperty("prop", "Public");

        Assert.assertEquals("Secret", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Secret not found"));
        Assert.assertEquals("Secret", instance.getProperty("prop", "Normal not found"));
    }

    /**
     * Check whether secret properties can also be per test case
     */
    @Test
    public void testTestCaseSpecificSecretProperties()
    {
        final SessionImpl session = SessionImpl.getCurrent();

        final String originalTestClassName = session.getTestCaseClassName();

        instance.setProperty(XltConstants.SECRET_PREFIX + originalTestClassName +".prop", "Secret");

        Assert.assertEquals("Secret", instance.getProperty(XltConstants.SECRET_PREFIX + originalTestClassName +".prop", "Full secret not found"));
        Assert.assertEquals("Secret", instance.getProperty(originalTestClassName +".prop", "Full not found"));
        Assert.assertEquals("Secret", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Secret not found"));
        Assert.assertEquals("Secret", instance.getProperty("prop", "Normal not found"));
    }
    /**
     * Check whether secret properties can also be per user
     */
    @Test
    public void testUserSpecificSecretProperties()
    {
        final SessionImpl session = SessionImpl.getCurrent();

        final String originalUserName = session.getUserName();

        instance.setProperty(XltConstants.SECRET_PREFIX + originalUserName +".prop", "Secret");

        Assert.assertEquals("Secret", instance.getProperty(XltConstants.SECRET_PREFIX + originalUserName +".prop", "Full secret not found"));
        Assert.assertEquals("Secret", instance.getProperty(originalUserName +".prop", "Full not found"));
        Assert.assertEquals("Secret", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Secret not found"));
        Assert.assertEquals("Secret", instance.getProperty("prop", "Normal not found"));
    }

    /**
     * ensure that explicitly requested secret props do not return a public value
     */
    @Test
    public void testExplicitSecretProps()
    {
        instance.setProperty("prop", "This is public");

        Assert.assertEquals("Not found", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Not found"));
    }

    /**
     * ensure that explicitly requested secret props do not return a public value
     */
    @Test
    public void testExplicitSecretUserProps()
    {
        final SessionImpl session = SessionImpl.getCurrent();

        final String originalUserName = session.getUserName();

        instance.setProperty(originalUserName +".prop", "This is public");

        Assert.assertEquals("Not found", instance.getProperty("secret.prop", "Not found"));
    }

    /**
     * ensure that explicitly requested secret props do not return a public value
     */
    @Test
    public void testExplicitSecretTestCaseProps()
    {
        final SessionImpl session = SessionImpl.getCurrent();

        final String originalTestClassName = session.getTestCaseClassName();

        instance.setProperty(originalTestClassName +".prop", "This is public");

        Assert.assertEquals("Not found", instance.getProperty("secret.prop", "Not found"));
    }

    /**
     * Test whether {@link XltProperties#getProperties()} returns only the original secret property
     */
    @Test
    public void testConvertingToNormalPropertiesReturnsOriginalKey()
    {
        instance.setProperty("secret.prop", "Secret");

        final Properties properties = instance.getProperties();

        Assert.assertEquals("Secret", properties.getProperty("secret.prop"));
        Assert.assertNull(properties.getProperty("prop"));
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
