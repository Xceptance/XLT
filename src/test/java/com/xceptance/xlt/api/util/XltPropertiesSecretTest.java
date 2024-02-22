/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.ram.RamFileProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.XltEngine;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 * Test cases specifically concerned with the loading of secret properties
 */
public class XltPropertiesSecretTest
{
    protected DefaultFileSystemManager FS;
    protected FileObject home;
    protected FileObject config;

    @Before
    public void setup() throws IOException
    {
        FS = new DefaultFileSystemManager();
        FS.addProvider("ram", new RamFileProvider());
        FS.init();
        FS.createVirtualFileSystem("ram://");

        home = FS.resolveFile("ram://home");
        home.createFolder();
        config = home.resolveFile("config");
        config.createFolder();

        // remove all properties starting with secret. from system
        var keys = System.getProperties().keySet().stream()
            .filter(k -> (k instanceof String))
            .map(k -> (String) k)
            .filter(s -> s.startsWith("secret.")).collect(Collectors.toList());
        keys.forEach(System::clearProperty);
    }

    @After
    public void teardown() throws IOException
    {
        FS.close();

        // remove all properties starting with secret. from system
        var keys = System.getProperties().keySet().stream()
            .filter(k -> (k instanceof String))
            .map(k -> (String) k)
            .filter(s -> s.startsWith("secret.")).collect(Collectors.toList());
        keys.forEach(System::clearProperty);
    }

    /*
     * So we can clean up what we set
     */
    private List<String> systemPropertiesSet = new ArrayList<>();

    public void setSystemProperty(String key, String value)
    {
        System.setProperty(key, value);
        systemPropertiesSet.add(key);
    }

    /**
     * Just mop up again
     */
    @After
    public void cleanAfter()
    {
        systemPropertiesSet.forEach(System::clearProperty);
        systemPropertiesSet.clear();
    }

    private FileObject writeConfigContent(String path, String content)
    {
        return writeConfigContent(path, List.of(content));
    }

    private FileObject writeConfigContent(String path, List<String> content)
    {
        FileObject file;
        try
        {
            file = config.resolveFile(path);
            if (!file.exists())
            {
                file.createFile();
            }

            var os = file.getContent().getOutputStream();
            for (String s : content)
            {
                os.write((s + "\n").getBytes());
            }
            file.getContent().close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return file;
    }

    private XltPropertiesImpl createDefaults()
    {
        writeConfigContent("default.properties", "default = dvalue");
        writeConfigContent("project.properties", List.of("com.xceptance.xlt.testPropertiesFile = test.properties", "project = pvalue"));
        writeConfigContent("test.properties", "test = tvalue");

        return XltEngine.reset(new XltPropertiesImpl(home, config, true, false)).xltProperties;
    }

    /**
     * Ensure that the hierarchy of properties is intact, i.e. props take precedence in the following order:
     *
     * 1. user-specific
     * 2. test-class specific
     * 3. bare property key
     *
     * In each case the secret version of a property takes precedence over the public version
     * @throws IOException
     */
    @Test
    public void testHierarchyOfPropertiesIsIntact()
    {
        final XltPropertiesImpl instance = createDefaults();
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
        final XltPropertiesImpl instance = createDefaults();

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
        final XltPropertiesImpl instance = createDefaults();

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
        final XltPropertiesImpl instance = createDefaults();
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
        final XltPropertiesImpl instance = createDefaults();
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
        final XltPropertiesImpl instance = createDefaults();
        instance.setProperty("prop", "This is public");

        Assert.assertEquals("Not found", instance.getProperty(XltConstants.SECRET_PREFIX+"prop", "Not found"));
    }

    /**
     * ensure that explicitly requested secret props do not return a public value
     */
    @Test
    public void testExplicitSecretUserProps()
    {
        final XltPropertiesImpl instance = createDefaults();
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
        final XltPropertiesImpl instance = createDefaults();
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
        final XltPropertiesImpl instance = createDefaults();
        instance.setProperty("secret.prop", "Secret");

        final Properties properties = instance.getProperties();

        Assert.assertEquals("Secret", properties.getProperty("secret.prop"));
        Assert.assertNull(properties.getProperty("prop"));
    }

    @Test
    public void testContainsKeyLooksForSecretKeyAsWell()
    {
        final XltPropertiesImpl instance = createDefaults();
        instance.setProperty("secret.prop", "Some value");

        Assert.assertTrue(instance.containsKey("secret.prop"));
        Assert.assertTrue(instance.containsKey("prop"));
    }

    @Test
    public void testLoadingSecretPropertiesFromFiles()
    {
        writeConfigContent("default.properties", "default = dvalue");
        writeConfigContent("project.properties", List.of("com.xceptance.xlt.testPropertiesFile = test.properties", "project = pvalue"));
        writeConfigContent("secret.properties", List.of("a=va", "secret.b=vb"));
        writeConfigContent("test.properties", "test = tvalue");

        final var instance = XltEngine.reset(new XltPropertiesImpl(home, config, false, false)).xltProperties;

        Assert.assertEquals("va", instance.getProperty("a"));
        Assert.assertEquals("va", instance.getProperty("secret.a"));
        Assert.assertEquals("vb", instance.getProperty("b"));
        Assert.assertEquals("vb", instance.getProperty("secret.b"));
    }

    /**
     * Secret overwrites a non-secret. Defined as secret in secret.properties
     */
    @Test
    public void secretOverridesANonSecret()
    {
        writeConfigContent("default.properties", "default = dvalue");
        writeConfigContent("project.properties", List.of("com.xceptance.xlt.testPropertiesFile = test.properties", "project = pvalue"));
        writeConfigContent("test.properties", "test = tvalue");
        writeConfigContent("secret.properties", "default = newValue");

        final var instance = XltEngine.reset(new XltPropertiesImpl(home, config, false, false)).xltProperties;

        Assert.assertEquals("newValue", instance.getProperty("default"));
        Assert.assertEquals("newValue", instance.getProperty("secret.default"));
    }

    /**
     * Secret overwrites a non-secret. Defined without prefix in secret.properties
     */
    @Ignore
    @Test
    public void secretRemovesNonSecret()
    {
        writeConfigContent("default.properties", "default = dvalue");
        writeConfigContent("project.properties", List.of("com.xceptance.xlt.testPropertiesFile = test.properties", "project = pvalue"));
        writeConfigContent("test.properties", "test = tvalue");
        writeConfigContent("secret.properties", "default = newValue");

        final var instance = XltEngine.reset(new XltPropertiesImpl(home, config, false, false)).xltProperties;

        Assert.assertEquals("newValue", instance.getProperty("secret.default"));
        Assert.assertNull(instance.getProperties().getProperty("default"));
    }

    /**
     * Secret with prefix does not override non-prefix prop
     */
    @Test
    public void nonPrefixSecretDoesNotRemovesNonSecret()
    {
        writeConfigContent("default.properties", "default = dvalue");
        writeConfigContent("project.properties", List.of("com.xceptance.xlt.testPropertiesFile = test.properties", "project = pvalue"));
        writeConfigContent("test.properties", "test = tvalue");
        writeConfigContent("secret.properties", "secret.default = newValue");

        final var instance = XltEngine.reset(new XltPropertiesImpl(home, config, false, false)).xltProperties;

        Assert.assertEquals("newValue", instance.getProperty("secret.default"));
        Assert.assertEquals("dvalue", instance.getProperties().getProperty("default"));
    }

    /**
     * Secret in system.properties overwrites secret from secret.properties
     */
    /**
     * Secret overwrites a non-secret. Defined without prefix in secret.properties
     */
    @Ignore
    @Test
    public void secretInSystemOverridesAll()
    {
        writeConfigContent("default.properties", "default = dvalue");
        writeConfigContent("project.properties", List.of("com.xceptance.xlt.testPropertiesFile = test.properties", "project = pvalue"));
        writeConfigContent("dev.properties", "dev = devvalue");
        writeConfigContent("test.properties", "test = tvalue");
        writeConfigContent("secret.properties", List.of("mySecret1 = newValue1", "secret.mySecret2 = newValue2"));

        setSystemProperty("secret.default", "0");
        setSystemProperty("secret.mySecret1", "1");
        setSystemProperty("secret.mySecret2", "2");
        setSystemProperty("secret.dev", "3");
        setSystemProperty("secret.project", "4");
        setSystemProperty("secret.test", "5");

        final var instance = XltEngine.reset(new XltPropertiesImpl(home, config, true, false)).xltProperties;

        Assert.assertEquals("0", instance.getProperty("secret.default"));
        Assert.assertEquals("1", instance.getProperty("secret.mySecret1"));
        Assert.assertEquals("2", instance.getProperty("secret.mySecret2"));
        Assert.assertEquals("3", instance.getProperty("secret.dev"));
        Assert.assertEquals("4", instance.getProperty("secret.project"));
        Assert.assertEquals("5", instance.getProperty("secret.test"));

        // bypass get logic and verify real properties, important when people customize things on top if XLT
        Assert.assertNull(instance.getProperties().getProperty("default"));
        Assert.assertNull(instance.getProperties().getProperty("project"));
        Assert.assertNull(instance.getProperties().getProperty("dev"));
        Assert.assertNull(instance.getProperties().getProperty("test"));

        Assert.assertEquals("1", instance.getProperties().getProperty("secret.mySecret1"));
        Assert.assertEquals("2", instance.getProperties().getProperty("secret.mySecret2"));
        Assert.assertNull(instance.getProperties().getProperty("mySecret1"));
        Assert.assertNull(instance.getProperties().getProperty("mySecret2"));
    }

    /**
     * CLI non-secret does not override anything from secret.properties
     */
    @Test
    public void systemNonSecrectDopesNotOverrideSecret()
    {
        writeConfigContent("default.properties", "default = dvalue");
        writeConfigContent("project.properties", List.of("com.xceptance.xlt.testPropertiesFile = test.properties", "project = pvalue"));
        writeConfigContent("dev.properties", "dev = devvalue");
        writeConfigContent("test.properties", "test = tvalue");
        writeConfigContent("secret.properties", List.of("mySecret1 = newValue1", "secret.mySecret2 = newValue2"));

        // you can set this, but when you ask via the XltProperties, you are not seeing it only when you go for the plain
        // properties
        setSystemProperty("mySecret1", "0");
        setSystemProperty("mySecret2", "1");

        final var instance = XltEngine.reset(new XltPropertiesImpl(home, config, true, false)).xltProperties;

        Assert.assertEquals("newValue1", instance.getProperty("secret.mySecret1"));
        Assert.assertEquals("newValue2", instance.getProperty("secret.mySecret2"));

        // you cannot see the override!
        Assert.assertEquals("newValue1", instance.getProperty("mySecret1"));
        Assert.assertEquals("newValue2", instance.getProperty("mySecret2"));

        Assert.assertEquals("dvalue", instance.getProperty("default"));
        Assert.assertEquals("devvalue", instance.getProperty("dev"));
        Assert.assertEquals("pvalue", instance.getProperty("project"));
        Assert.assertEquals("tvalue", instance.getProperty("test"));

        // bypass get logic and verify real properties, important when people customize things on top if XLT
        Assert.assertEquals("newValue1", instance.getProperties().getProperty("secret.mySecret1"));
        Assert.assertEquals("newValue2", instance.getProperties().getProperty("secret.mySecret2"));

        // we set that manually and after secret props loading, hence this is available
        Assert.assertEquals("0", instance.getProperties().getProperty("mySecret1"));
        Assert.assertEquals("1", instance.getProperties().getProperty("mySecret2"));
    }

    /**
     * Secret file is optional
     */
    @Test
    public void secretFileIsOptional()
    {
        writeConfigContent("default.properties", "default = dvalue");
        writeConfigContent("project.properties", List.of("com.xceptance.xlt.testPropertiesFile = test.properties", "project = pvalue"));
        writeConfigContent("dev.properties", "dev = devvalue");
        writeConfigContent("test.properties", "test = tvalue");

        // you can set this, but when you ask via the XltProperties, you are not seeing it only when you go for the plain
        // properties
        setSystemProperty("system", "svalue");

        final var instance = XltEngine.reset(new XltPropertiesImpl(home, config, true, false)).xltProperties;

        Assert.assertEquals("dvalue", instance.getProperty("default"));
        Assert.assertEquals("devvalue", instance.getProperty("dev"));
        Assert.assertEquals("pvalue", instance.getProperty("project"));
        Assert.assertEquals("tvalue", instance.getProperty("test"));
        Assert.assertEquals("svalue", instance.getProperty("system"));

        // but we still can add secrets
        instance.setProperty("secret.s1", "v1");
        instance.setProperty("secret.dev", "dsv");

        Assert.assertEquals("dvalue", instance.getProperty("default"));
        Assert.assertEquals("dsv", instance.getProperty("dev")); // old is gone

        Assert.assertEquals("pvalue", instance.getProperty("project"));
        Assert.assertEquals("tvalue", instance.getProperty("test"));
        Assert.assertEquals("svalue", instance.getProperty("system"));

        Assert.assertEquals("v1", instance.getProperty("secret.s1"));
        Assert.assertEquals("v1", instance.getProperty("s1"));

        Assert.assertEquals("dsv", instance.getProperty("secret.dev"));
        Assert.assertEquals("dsv", instance.getProperty("dev"));

        // not gone
        Assert.assertEquals("dvalue", instance.getProperties().getProperty("default"));
    }

    /**
     * Secret in any other file is handled like a secret except no auto-expanding with prefix
     */
    @Test
    public void secretInAnotherFile()
    {
        writeConfigContent("default.properties", List.of("secret.default = dvalue2", "default = dvalue"));
        writeConfigContent("project.properties", List.of("com.xceptance.xlt.testPropertiesFile = test.properties", "secret.project = pvalue2", "project = pvalue"));
        writeConfigContent("dev.properties", List.of("dev = devvalue", "secret.dev = devvalue2"));
        writeConfigContent("test.properties", List.of("secret.test = tvalue2", "test = tvalue"));

        final var instance = XltEngine.reset(new XltPropertiesImpl(home, config, true, false)).xltProperties;

        Assert.assertEquals("dvalue2", instance.getProperty("default"));
        Assert.assertEquals("devvalue2", instance.getProperty("dev"));
        Assert.assertEquals("pvalue2", instance.getProperty("project"));
        Assert.assertEquals("tvalue2", instance.getProperty("test"));
    }

    /**
     * Secret value expansion works aka ${}
     */
    @Test
    public void secretValueExpansion()
    {
        writeConfigContent("default.properties", List.of("default = dvalue", "key = AAA", "testMe = ${key}-testtest"));
        writeConfigContent("project.properties", List.of("com.xceptance.xlt.testPropertiesFile = test.properties", "secret.project = ${key}-pvalue2"));
        writeConfigContent("dev.properties", List.of("dev = ${key}-devvalue", "secret.dev = ${key}-devvalue2"));
        writeConfigContent("secret.properties", List.of("mySecret11 = ${key}-mySecretV1", "secret.mySecret21 = ${key}-mySecretV2"));
        writeConfigContent("test.properties", List.of("secret.test = ${key}-tvalue2", "extTest1 = ${mySecret11}-tvalue", "extTest2 = ${secret.mySecret11}-tvalue"));

        final var instance = XltEngine.reset(new XltPropertiesImpl(home, config, true, false)).xltProperties;

        Assert.assertEquals("AAA-testtest", instance.getProperty("testMe"));
        Assert.assertEquals("AAA-devvalue2", instance.getProperty("dev"));
        Assert.assertEquals("AAA-pvalue2", instance.getProperty("project"));
        Assert.assertEquals("AAA-tvalue2", instance.getProperty("test"));
        Assert.assertEquals("AAA-mySecretV1", instance.getProperty("mySecret11"));
        Assert.assertEquals("AAA-mySecretV2", instance.getProperty("mySecret21"));
        Assert.assertEquals("AAA-mySecretV1", instance.getProperty("secret.mySecret11"));
        Assert.assertEquals("AAA-mySecretV2", instance.getProperty("secret.mySecret21"));
        Assert.assertEquals("AAA-mySecretV1-tvalue", instance.getProperty("extTest2"));

        // cannot extend, because I don't see ${mySecret1} only ${secret.mySecret1}
        Assert.assertEquals("${mySecret11}-tvalue", instance.getProperty("extTest1"));
    }

    /**
     * We don't log it. This is not a nice test because we opened the API for that but better safe then sorry. XLT
     * is not a set-in-stone API.
     */
    @Ignore
    @Test
    public void maskedLogging()
    {
        writeConfigContent("default.properties", "default = dvalue");
        writeConfigContent("project.properties", List.of("com.xceptance.xlt.testPropertiesFile = test.properties", "secret.project = pvalue"));
        writeConfigContent("test.properties", "test = tvalue");
        writeConfigContent("secret.properties", "default = newValue");

        final var instance = new XltPropertiesImpl(home, config, false, false);
        final Set<String> set = new HashSet<>(instance.dumpAllProperties());

        assertTrue(set.contains("secret.default = " + XltConstants.MASK_PROPERTIES_HIDETEXT));
        assertTrue(set.contains("secret.project = " + XltConstants.MASK_PROPERTIES_HIDETEXT));
        assertFalse(set.contains("default = dvalue"));
        assertFalse(set.contains("secret.default = newValue"));

    }
}
