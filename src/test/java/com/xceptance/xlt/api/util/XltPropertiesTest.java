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
package com.xceptance.xlt.api.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.xceptance.xlt.common.XltConstants;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.xceptance.xlt.engine.XltEngine;
import com.xceptance.xlt.engine.XltExecutionContext;
import com.xceptance.xlt.util.PropertiesConfigurationException;
import com.xceptance.xlt.util.PropertyFileNotFoundException;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 * Test the implementation of {@link XltProperties}.
 *
 * @author Rene Schwietzke (Xceptance)
 */
public class XltPropertiesTest
{
    private static FileObject origHome;
    private static FileObject origConfig;

    private FileObject homeDir;
    private FileObject configDir;

    @BeforeClass
    public static void beforeClass()
    {
        // Remember original directories from execution context to restore them after the tests
        origHome = XltExecutionContext.getCurrent().getTestSuiteHomeDir();
        origConfig = XltExecutionContext.getCurrent().getTestSuiteConfigDir();
    }

    @After
    public void after()
    {
        // Restore directories in execution context to their original values
        XltExecutionContext.getCurrent().setTestSuiteHomeDir(origHome);
        XltExecutionContext.getCurrent().setTestSuiteConfigDir(origConfig);
    }

    /**
     * Setup the base source
     */
    public void setup(final String home, final String config) throws FileSystemException
    {
        homeDir = getResourceFile(home);
        configDir = getResourceFile(config);

        XltExecutionContext.getCurrent().setTestSuiteConfigDir(configDir);
        XltExecutionContext.getCurrent().setTestSuiteHomeDir(homeDir);

        // just make something known, so we can check that we loaded it
        setSystemProperty("systemkey", "systemkeyvalue");
    }

    /**
     * Get a FileObject for the resource with the given name
     */
    private FileObject getResourceFile(final String resourceName) throws FileSystemException
    {
        var path = getClass().getResource(resourceName).getFile();
        return VFS.getManager().toFileObject(new File(path));
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
     * Just start clean
     */
    @Before
    public void cleanBefore()
    {
        // remove all properties starting with secret. from system
        var keys = System.getProperties().keySet().stream()
            .filter(k -> (k instanceof String))
            .map(k -> (String) k)
            .filter(s -> s.startsWith("secret.")).collect(Collectors.toList());
        keys.forEach(System::clearProperty);

        XltPropertiesImpl.getInstance().clear();
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

    /**
     * Assume the path is in the context of all dirs, we later will also set it
     * @throws FileSystemException
     */
    @Test
    public void happyPath() throws FileSystemException
    {
        setup("propertytest_hp", "propertytest_hp/config");

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = new XltPropertiesImpl(null, null, true, false);

        // we are not in control of most system props, so we have to exclude that count dynamically and we have to add our
        // only system prop with +1
        assertEquals(8, p.getProperties().size() - System.getProperties().size() + 1);

        // see that we have our buckets and in order

        final BiConsumer<String, String> test = (k, b) ->
        {
            // secret has an extended key
            var key = b.equals(XltProperties.SECRET_PROPERTIES) ? "secret." + k : k;

            assertEquals(k + "value", p.getProperty(key));
            assertEquals(k + "value", p.getPropertyBuckets().get(b).getProperty(key));
        };

        test.accept("default", XltProperties.DEFAULT_PROPERTIES);
        test.accept("project", XltProperties.PROJECT_PROPERTIES);
        test.accept("test", XltProperties.TEST_PROPERTIES);
        test.accept("dev", XltProperties.DEVELOPMENT_PROPERTIES);
        test.accept("key1", XltProperties.SECRET_PROPERTIES);
        test.accept("key2", XltProperties.SECRET_PROPERTIES);
        test.accept("systemkey", XltProperties.SYSTEM_PROPERTIES);

        // ok, more about the files
        var files = p.getUsedPropertyFiles();
        assertEquals(5, files.size());

        assertEquals("config/default.properties", homeDir.getName().getRelativeName(files.get(0).getName()));
        assertEquals("config/project.properties", homeDir.getName().getRelativeName(files.get(1).getName()));
        assertEquals("config/test.properties",    homeDir.getName().getRelativeName(files.get(2).getName()));
        assertEquals("config/dev.properties",     homeDir.getName().getRelativeName(files.get(3).getName()));
        assertEquals("config/secret.properties",  homeDir.getName().getRelativeName(files.get(4).getName()));

        var rFilename = p.getUsedPropertyFilesByRelativeName();
        assertEquals(5, rFilename.size());
        assertEquals("default.properties", rFilename.get(0));
        assertEquals("project.properties", rFilename.get(1));
        assertEquals("test.properties", rFilename.get(2));
        assertEquals("dev.properties", rFilename.get(3));
        assertEquals("secret.properties", rFilename.get(4));
    }

    /**
     * Load no dev because we are in load test, file exists
     */
    @Ignore
    @Test
    public void noDevPropsLoaded() throws FileSystemException
    {
        setup("propertytest_hp", "propertytest_hp/config");

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = XltEngine.reset(new XltPropertiesImpl(null, null, false, false)).xltProperties;

        // we are not in control of most system props, so we have to exclude that count dynamically and we have to add our
        // only system prop with +1
        assertEquals(7, p.getProperties().size() - System.getProperties().size() + 1);

        // see that we have our buckets and in order

        final BiConsumer<String, String> test = (k, b) ->
        {
            // secret has an extended key
            var key = b.equals(XltProperties.SECRET_PROPERTIES) ? "secret." + k : k;

            assertEquals(k + "value", p.getProperty(key));
            assertEquals(k + "value", p.getPropertyBuckets().get(b).getProperty(key));
        };

        test.accept("default", XltProperties.DEFAULT_PROPERTIES);
        test.accept("project", XltProperties.PROJECT_PROPERTIES);
        test.accept("test", XltProperties.TEST_PROPERTIES);
        test.accept("key1", XltProperties.SECRET_PROPERTIES);
        test.accept("key2", XltProperties.SECRET_PROPERTIES);
        test.accept("systemkey", XltProperties.SYSTEM_PROPERTIES);

        // ok, more about the files
        var files = p.getUsedPropertyFiles();
        assertEquals(4, files.size());

        assertEquals("config/default.properties", homeDir.getName().getRelativeName(files.get(0).getName()));
        assertEquals("config/project.properties", homeDir.getName().getRelativeName(files.get(1).getName()));
        assertEquals("config/test.properties",    homeDir.getName().getRelativeName(files.get(2).getName()));
        assertEquals("config/secret.properties",  homeDir.getName().getRelativeName(files.get(3).getName()));

        var rFilename = p.getUsedPropertyFilesByRelativeName();
        assertEquals(4, rFilename.size());
        assertEquals("default.properties", rFilename.get(0));
        assertEquals("project.properties", rFilename.get(1));
        assertEquals("test.properties", rFilename.get(2));
        assertEquals("secret.properties", rFilename.get(3));
    }

    /**
     * Secret is missing
     */
    @Test
    public void noSecret() throws FileSystemException
    {
        setup("propertytest_nosecret", "propertytest_nosecret/config");

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = new XltPropertiesImpl(null, null, false, false);

        // we are not in control of most system props, so we have to exclude that count dynamically and we have to add our
        // only system prop with +1
        assertEquals(5, p.getProperties().size() - System.getProperties().size() + 1);

        // see that we have our buckets and in order
        final BiConsumer<String, String> test = (k, b) ->
        {
            // secret has an extended key
            var key = b.equals(XltProperties.SECRET_PROPERTIES) ? "secret." + k : k;

            assertEquals(k + "value", p.getProperty(key));
            assertEquals(k + "value", p.getPropertyBuckets().get(b).getProperty(key));
        };

        test.accept("default", XltProperties.DEFAULT_PROPERTIES);
        test.accept("project", XltProperties.PROJECT_PROPERTIES);
        test.accept("test", XltProperties.TEST_PROPERTIES);
        test.accept("systemkey", XltProperties.SYSTEM_PROPERTIES);

        // ok, more about the files
        var files = p.getUsedPropertyFiles();
        assertEquals(3, files.size());
        assertEquals("config/default.properties", homeDir.getName().getRelativeName(files.get(0).getName()));
        assertEquals("config/project.properties", homeDir.getName().getRelativeName(files.get(1).getName()));
        assertEquals("config/test.properties",    homeDir.getName().getRelativeName(files.get(2).getName()));

        var rFilename = p.getUsedPropertyFilesByRelativeName();
        assertEquals(3, rFilename.size());
        assertEquals("default.properties", rFilename.get(0));
        assertEquals("project.properties", rFilename.get(1));
        assertEquals("test.properties", rFilename.get(2));
    }

    /**
     * test props location defined by system property
     */
    @Test
    public void testViaSystem() throws FileSystemException
    {
        setup("propertytest_testfromsystem", "propertytest_testfromsystem/config");
        setSystemProperty("com.xceptance.xlt.testPropertiesFile", "test2.properties");

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = new XltPropertiesImpl(null, null, false, false);

        // we are not in control of most system props, so we have to exclude that count dynamically and we have to add our
        // systems prop with +2
        assertEquals(5, p.getProperties().size() - System.getProperties().size() + 2);

        // see that we have our buckets and in order
        final BiConsumer<String, String> test = (k, b) ->
        {
            // secret has an extended key
            var key = b.equals(XltProperties.SECRET_PROPERTIES) ? "secret." + k : k;

            assertEquals(k + "value", p.getProperty(key));
            assertEquals(k + "value", p.getPropertyBuckets().get(b).getProperty(key));
        };

        test.accept("default", XltProperties.DEFAULT_PROPERTIES);
        test.accept("project", XltProperties.PROJECT_PROPERTIES);
        test.accept("test", XltProperties.TEST_PROPERTIES);
        test.accept("systemkey", XltProperties.SYSTEM_PROPERTIES);

        // ok, more about the files
        var files = p.getUsedPropertyFiles();
        assertEquals(3, files.size());
        assertEquals("config/default.properties", homeDir.getName().getRelativeName(files.get(0).getName()));
        assertEquals("config/project.properties", homeDir.getName().getRelativeName(files.get(1).getName()));
        assertEquals("config/test2.properties",   homeDir.getName().getRelativeName(files.get(2).getName()));

        var rFilename = p.getUsedPropertyFilesByRelativeName();
        assertEquals(3, rFilename.size());
        assertEquals("default.properties", rFilename.get(0));
        assertEquals("project.properties", rFilename.get(1));
        assertEquals("test2.properties", rFilename.get(2));
    }

    /**
     * Load test not set
     */
    @Test
    public void testNotSet() throws FileSystemException
    {
        setup("propertytest_notestfallback", "propertytest_notestfallback/config");

        // we don't complain about not test props
        var p = new XltPropertiesImpl(null, null, false, false);

        var files = p.getUsedPropertyFiles();
        assertEquals(2, files.size());
        assertEquals("config/default.properties", homeDir.getName().getRelativeName(files.get(0).getName()));
        assertEquals("config/project.properties", homeDir.getName().getRelativeName(files.get(1).getName()));

        var rFilename = p.getUsedPropertyFilesByRelativeName();
        assertEquals(2, rFilename.size());
        assertEquals("default.properties", rFilename.get(0));
        assertEquals("project.properties", rFilename.get(1));
    }

    /**
     * Test does not exists
     */
    @Test
    public void testDoesNotExist() throws FileSystemException
    {
        setup("propertytest_notestfallback", "propertytest_notestfallback/config");
        setSystemProperty("com.xceptance.xlt.testPropertiesFile", "noidea.properties");

        try
        {
            new XltPropertiesImpl(null, null, false, false);

            // don't want to get here
            fail("Exception not raised");
        }
        catch(PropertyFileNotFoundException e)
        {
            assertEquals("Property file config/noidea.properties does not exist", e.getMessage());
        }
    }

    /**
     * no standard configs, just test
     */
    @Test
    public void defaultDoesNotExist() throws FileSystemException
    {
        setup("propertytest_nodefaults", "propertytest_nodefaults/config");
        setSystemProperty("com.xceptance.xlt.testPropertiesFile", "test2.properties");

        new XltPropertiesImpl(null, null, false, false);
    }

    /**
     * nothing at all
     */
    @Test
    public void nothingExists() throws FileSystemException
    {
        setup("propertytest_nothing", "propertytest_nothing/config");
        new XltPropertiesImpl(null, null, false, false);
    }

    /**
     * Clear
     * @throws FileSystemException
     */
    @Test
    public void clear() throws FileSystemException
    {
        setup("propertytest_hp", "propertytest_hp/config");

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = new XltPropertiesImpl(null, null, true, false);

        // we are not in control of most system props, so we have to exclude that count dynamically and we have to add our
        // only system prop with +1
        assertEquals(8, p.getProperties().size() - System.getProperties().size() + 1);

        // ok, more about the files
        var files = p.getUsedPropertyFiles();
        assertEquals(5, files.size());

        assertEquals("config/default.properties", homeDir.getName().getRelativeName(files.get(0).getName()));
        assertEquals("config/project.properties", homeDir.getName().getRelativeName(files.get(1).getName()));
        assertEquals("config/test.properties",    homeDir.getName().getRelativeName(files.get(2).getName()));
        assertEquals("config/dev.properties",     homeDir.getName().getRelativeName(files.get(3).getName()));
        assertEquals("config/secret.properties",  homeDir.getName().getRelativeName(files.get(4).getName()));

        p.clear();
        assertEquals(0, p.getProperties().size());
    }

    /**
     * Reset
     * @throws FileSystemException
     */
    @Test
    public void reset() throws FileSystemException
    {
        setup("propertytest_hp", "propertytest_hp/config");

        final Consumer<XltPropertiesImpl> tester = p ->
        {
            // we are not in control of most system props, so we have to exclude that count dynamically and we have to add our
            // only system prop with +1
            assertEquals(8, p.getProperties().size() - System.getProperties().size() + 1);

            // see that we have our buckets and in order

            final BiConsumer<String, String> test = (k, b) ->
            {
                // secret has an extended key
                var key = b.equals(XltProperties.SECRET_PROPERTIES) ? "secret." + k : k;

                assertEquals(k + "value", p.getProperty(key));
                assertEquals(k + "value", p.getPropertyBuckets().get(b).getProperty(key));
            };

            test.accept("default", XltProperties.DEFAULT_PROPERTIES);
            test.accept("project", XltProperties.PROJECT_PROPERTIES);
            test.accept("test", XltProperties.TEST_PROPERTIES);
            test.accept("dev", XltProperties.DEVELOPMENT_PROPERTIES);
            test.accept("key1", XltProperties.SECRET_PROPERTIES);
            test.accept("key2", XltProperties.SECRET_PROPERTIES);
            test.accept("systemkey", XltProperties.SYSTEM_PROPERTIES);

            // ok, more about the files
            var files = p.getUsedPropertyFiles();
            assertEquals(5, files.size());

            try
            {
                assertEquals("config/default.properties", homeDir.getName().getRelativeName(files.get(0).getName()));
                assertEquals("config/project.properties", homeDir.getName().getRelativeName(files.get(1).getName()));
                assertEquals("config/test.properties",    homeDir.getName().getRelativeName(files.get(2).getName()));
                assertEquals("config/dev.properties",     homeDir.getName().getRelativeName(files.get(3).getName()));
                assertEquals("config/secret.properties",  homeDir.getName().getRelativeName(files.get(4).getName()));
            }
            catch (FileSystemException e)
            {
                throw new RuntimeException(e);
            }
        };

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = XltEngine.reset(new XltPropertiesImpl(null, null, true, false)).xltProperties;

        // check init state
        tester.accept(p);

        // add something custom
        p.setProperty("custom", "any");
        assertEquals("any", p.getProperty("custom"));

        // clear all
        p.clear();
        assertEquals(0, p.getProperties().size());
        assertNull(p.getProperty("custom"));

        // custom goes away
        p.setProperty("custom", "any");
        assertEquals("any", p.getProperty("custom"));

        p = XltEngine.reset().xltProperties;

        assertNull(p.getProperty("custom"));
        // read data is back
        tester.accept(p);

        p.clear();
        assertEquals(0, p.getProperties().size());

        // set property goes away when reset
        p.setProperty("custom", "any");
        assertEquals("any", p.getProperty("custom"));

        p = XltEngine.reset().xltProperties;

        tester.accept(p);
        assertNull(p.getProperty("custom"));
    }

    /**
     * We use a simple include
     */
    @Test
    public void simpleFileInclude() throws FileSystemException
    {
        setup("propertytest_simpleinclude", "propertytest_simpleinclude/config");

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = new XltPropertiesImpl(null, null, true, false);

        // we are not in control of most system props, so we have to exclude that count dynamically and we have to add our
        // only system prop with +1
        // +2: Include and value of include
        assertEquals(8 + 2, p.getProperties().size() - System.getProperties().size() + 1);

        // see that we have our buckets and in order

        final BiConsumer<String, String> test = (k, b) ->
        {
            // secret has an extended key
            var key = b.equals(XltProperties.SECRET_PROPERTIES) ? "secret." + k : k;

            assertEquals(k + "value", p.getProperty(key));
            assertEquals(k + "value", p.getPropertyBuckets().get(b).getProperty(key));
        };

        test.accept("default", XltProperties.DEFAULT_PROPERTIES);
        test.accept("include", XltProperties.DEFAULT_PROPERTIES);

        test.accept("project", XltProperties.PROJECT_PROPERTIES);
        test.accept("test", XltProperties.TEST_PROPERTIES);
        test.accept("dev", XltProperties.DEVELOPMENT_PROPERTIES);
        test.accept("key1", XltProperties.SECRET_PROPERTIES);
        test.accept("key2", XltProperties.SECRET_PROPERTIES);
        test.accept("systemkey", XltProperties.SYSTEM_PROPERTIES);

        // ok, more about the files
        var files = p.getUsedPropertyFiles();
        assertEquals(6, files.size());

        assertEquals("config/default.properties", homeDir.getName().getRelativeName(files.get(0).getName()));
        assertEquals("config/include.properties", homeDir.getName().getRelativeName(files.get(1).getName()));
        assertEquals("config/project.properties", homeDir.getName().getRelativeName(files.get(2).getName()));
        assertEquals("config/test.properties",    homeDir.getName().getRelativeName(files.get(3).getName()));
        assertEquals("config/dev.properties",     homeDir.getName().getRelativeName(files.get(4).getName()));
        assertEquals("config/secret.properties",  homeDir.getName().getRelativeName(files.get(5).getName()));

        var rFilename = p.getUsedPropertyFilesByRelativeName();
        assertEquals(6, rFilename.size());
        assertEquals("default.properties", rFilename.get(0));
        assertEquals("include.properties", rFilename.get(1));
        assertEquals("project.properties", rFilename.get(2));
        assertEquals("test.properties", rFilename.get(3));
        assertEquals("dev.properties", rFilename.get(4));
        assertEquals("secret.properties", rFilename.get(5));
    }

    /**
     * We use a dir include
     */
    @Test
    public void simpleDirInclude() throws FileSystemException
    {
        setup("propertytest_dirinclude", "propertytest_dirinclude/config");

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = new XltPropertiesImpl(null, null, true, false);

        // we are not in control of most system props, so we have to exclude that count dynamically and we have to add our
        // only system prop with +1
        // +4: Include and value of includes (3 files)
        assertEquals(8 + 4, p.getProperties().size() - System.getProperties().size() + 1);

        // see that we have our buckets and in order

        final BiConsumer<String, String> test = (k, b) ->
        {
            // secret has an extended key
            var key = b.equals(XltProperties.SECRET_PROPERTIES) ? "secret." + k : k;

            assertEquals(k + "value", p.getProperty(key));
            assertEquals(k + "value", p.getPropertyBuckets().get(b).getProperty(key));
        };

        test.accept("default", XltProperties.DEFAULT_PROPERTIES);
        test.accept("a", XltProperties.DEFAULT_PROPERTIES);

        test.accept("project", XltProperties.PROJECT_PROPERTIES);
        test.accept("test", XltProperties.TEST_PROPERTIES);
        test.accept("dev", XltProperties.DEVELOPMENT_PROPERTIES);
        test.accept("key1", XltProperties.SECRET_PROPERTIES);
        test.accept("key2", XltProperties.SECRET_PROPERTIES);
        test.accept("systemkey", XltProperties.SYSTEM_PROPERTIES);

        // ok, more about the files
        var files = p.getUsedPropertyFiles();
        assertEquals(8, files.size());

        assertEquals("config/default.properties", homeDir.getName().getRelativeName(files.get(0).getName()));
        assertEquals("config/dir/a.properties", homeDir.getName().getRelativeName(files.get(1).getName()));
        assertEquals("config/dir/b.properties", homeDir.getName().getRelativeName(files.get(2).getName()));
        assertEquals("config/dir/c.properties", homeDir.getName().getRelativeName(files.get(3).getName()));
        assertEquals("config/project.properties", homeDir.getName().getRelativeName(files.get(4).getName()));
        assertEquals("config/test.properties",    homeDir.getName().getRelativeName(files.get(5).getName()));
        assertEquals("config/dev.properties",     homeDir.getName().getRelativeName(files.get(6).getName()));
        assertEquals("config/secret.properties",  homeDir.getName().getRelativeName(files.get(7).getName()));

        var rFilename = p.getUsedPropertyFilesByRelativeName();
        assertEquals(8, rFilename.size());
        assertEquals("default.properties", rFilename.get(0));
        assertEquals("dir/a.properties", rFilename.get(1));
        assertEquals("dir/b.properties", rFilename.get(2));
        assertEquals("dir/c.properties", rFilename.get(3));
        assertEquals("project.properties", rFilename.get(4));
        assertEquals("test.properties", rFilename.get(5));
        assertEquals("dev.properties", rFilename.get(6));
        assertEquals("secret.properties", rFilename.get(7));
    }

    /**
     * Include does not exist and we ignore
     */
    @Ignore
    @Test
    public void simpleFileIncludeMissing() throws FileSystemException
    {
        setup("propertytest_simpleincludemissing", "propertytest_simpleincludemissing/config");

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = XltEngine.reset(new XltPropertiesImpl(null, null, false, true)).xltProperties;

        // we are not in control of most system props, so we have to exclude that count dynamically and we have to add our
        // only system prop with +1, no dev is loaded -1
        // +1: Include
        assertEquals(7 + 1, p.getProperties().size() - System.getProperties().size() + 1);

        // see that we have our buckets and in order

        final BiConsumer<String, String> test = (k, b) ->
        {
            // secret has an extended key
            var key = b.equals(XltProperties.SECRET_PROPERTIES) ? "secret." + k : k;

            assertEquals(k + "value", p.getProperty(key));
            assertEquals(k + "value", p.getPropertyBuckets().get(b).getProperty(key));
        };

        test.accept("default", XltProperties.DEFAULT_PROPERTIES);
        test.accept("project", XltProperties.PROJECT_PROPERTIES);
        test.accept("test", XltProperties.TEST_PROPERTIES);
        test.accept("key1", XltProperties.SECRET_PROPERTIES);
        test.accept("key2", XltProperties.SECRET_PROPERTIES);
        test.accept("systemkey", XltProperties.SYSTEM_PROPERTIES);

        // ok, more about the files
        var files = new ArrayDeque<>(p.getUsedPropertyFiles());
        assertEquals(4, files.size());

        assertEquals("config/default.properties", homeDir.getName().getRelativeName(files.pollFirst().getName()));
        assertEquals("config/project.properties", homeDir.getName().getRelativeName(files.pollFirst().getName()));
        assertEquals("config/test.properties",    homeDir.getName().getRelativeName(files.pollFirst().getName()));
        assertEquals("config/secret.properties",  homeDir.getName().getRelativeName(files.pollFirst().getName()));

        var rFilename = p.getUsedPropertyFilesByRelativeName();
        assertEquals(4, rFilename.size());
        assertEquals("default.properties", rFilename.get(0));
        assertEquals("project.properties", rFilename.get(1));
        assertEquals("test.properties", rFilename.get(2));
        assertEquals("secret.properties", rFilename.get(3));
    }

    /**
     * Include does not exist and we don't ignore
     */
    @Test
    public void simpleFileIncludeMissingDontIgnore() throws FileSystemException
    {
        setup("propertytest_simpleincludemissing", "propertytest_simpleincludemissing/config");

        try
        {
            new XltPropertiesImpl(null, null, false, false);
            fail("No exeption raised");
        }
        catch (PropertyFileNotFoundException e)
        {
            assertEquals("File missinginclude.properties does not exist", e.getMessage());
        }
    }


    /**
     * Same include misconfiguration aka circular
     */
    @Test
    public void includeConfigProblem() throws FileSystemException
    {
        setup("propertytest_incorrectinclude", "propertytest_incorrectinclude/config");

        try
        {
            new XltPropertiesImpl(null, null, false, false);
            fail("No exeption raised");
        }
        catch (PropertiesConfigurationException e)
        {
            assertEquals("File include.properties has been seen multiple times when resolving properties, this can indicate a cyclic include pattern but also just be a repeated reference.", e.getMessage());
        }
    }

    /**
     * Get config and data directory from XltPropertiesImpl object where the directories are set in the constructor
     */
    @Test
    public void getConfigAndDataDirs_SetDirsInConstructor() throws FileSystemException
    {
        // Set home and config dir in execution context
        setup("propertytest_hp", "propertytest_hp/config");

        // Provide different home and config dirs to XltPropertiesImpl constructor
        final FileObject homeOverride = getResourceFile("propertytest_notestfallback");
        final FileObject configOverride = getResourceFile("propertytest_notestfallback/config");
        var p = new XltPropertiesImpl(homeOverride, configOverride, true, false);

        // Getter return values are based on the directories set in the constructor
        assertEquals(configOverride.getPath(), p.getConfigDirectory());
        assertEquals(homeOverride.getPath().resolve(XltConstants.DEFAULT_DATA_DIR_PATH), p.getDataDirectory());
    }

    /**
     * Get config and data directories from XltPropertiesImpl object that uses the directories from the execution
     * context
     */
    @Test
    public void getConfigAndDataDirs_UseDirsFromExecutionContext() throws FileSystemException
    {
        // Set home and config dir in execution context
        setup("propertytest_hp", "propertytest_hp/config");

        // Don't provide home and config dirs to XltPropertiesImpl constructor
        var p = new XltPropertiesImpl(null, null, true, false);

        // Getter return values are based on the directories from the execution context
        assertEquals(configDir.getPath(), p.getConfigDirectory());
        assertEquals(homeDir.getPath().resolve(XltConstants.DEFAULT_DATA_DIR_PATH), p.getDataDirectory());
    }

    /**
     * Get config and data directories from XltPropertiesImpl objects created before and after updating the execution
     * context directories
     */
    @Test
    public void getConfigAndDataDirs_UpdateDirsInExecutionContext() throws FileSystemException
    {
        // Create instance of XltProperties with directories from execution context and remember the directories
        setup("propertytest_hp", "propertytest_hp/config");
        final FileObject homeDir1 = XltExecutionContext.getCurrent().getTestSuiteHomeDir();
        final FileObject configDir1 = XltExecutionContext.getCurrent().getTestSuiteConfigDir();
        var p1 = new XltPropertiesImpl(null, null, true, false);

        // Update execution context and create a second instance
        setup("propertytest_notestfallback", "propertytest_notestfallback/config");
        var p2 = new XltPropertiesImpl(null, null, true, false);

        // Getter return values are based on the home and config directories set during object creation
        assertEquals(configDir1.getPath(), p1.getConfigDirectory());
        assertEquals(homeDir1.getPath().resolve(XltConstants.DEFAULT_DATA_DIR_PATH), p1.getDataDirectory());
        assertEquals(configDir.getPath(), p2.getConfigDirectory());
        assertEquals(homeDir.getPath().resolve(XltConstants.DEFAULT_DATA_DIR_PATH), p2.getDataDirectory());
    }

    /**
     * Get config and data directories from XltPropertiesImpl object with empty properties
     */
    @Test
    public void getConfigAndDataDirs_InstanceWithoutProperties() throws FileSystemException
    {
        setup("propertytest_hp", "propertytest_hp/config");

        // Create instance of XltPropertiesImpl using the constructor with empty properties
        var p = new XltPropertiesImpl();

        // Getter return values are based on the directories from the execution context
        assertEquals(configDir.getPath(), p.getConfigDirectory());
        assertEquals(homeDir.getPath().resolve(XltConstants.DEFAULT_DATA_DIR_PATH), p.getDataDirectory());
    }

    /**
     * Get config and data directories when relative data directory path is set in the properties
     */
    @Test
    public void getConfigAndDataDirs_RelativeDataDirFromProperty() throws FileSystemException
    {
        setup("propertytest_hp", "propertytest_hp/config");
        var p = new XltPropertiesImpl(null, null, true, false);

        // Data directory is overridden with relative path in properties
        final String relativePath = "datadir";
        p.setProperty(XltConstants.PROP_DATA_DIRECTORY, relativePath);

        // Getter for data directory returns the relative path inside the provided home directory
        assertEquals(configDir.getPath(), p.getConfigDirectory());
        assertEquals(homeDir.getPath().resolve(relativePath), p.getDataDirectory());
    }

    /**
     * Get config and data directories when absolute data directory path is set in the properties
     */
    @Test
    public void getConfigAndDataDirs_AbsoluteDataDirFromProperty() throws FileSystemException
    {
        setup("propertytest_hp", "propertytest_hp/config");
        var p = new XltPropertiesImpl(null, null, true, false);

        // Data directory is overridden with absolute directory in properties
        final Path absolutePath = getResourceFile("propertytest_dirinclude").getPath().toAbsolutePath();
        p.setProperty(XltConstants.PROP_DATA_DIRECTORY, absolutePath.toString());

        // Getter for data directory returns the absolute directory
        assertEquals(configDir.getPath(), p.getConfigDirectory());
        assertEquals(absolutePath, p.getDataDirectory());
    }

    /**
     * Get config and data directories when empty data directory path is set in the properties
     */
    @Test
    public void getConfigAndDataDirs_EmptyDataDirFromProperty() throws FileSystemException
    {
        setup("propertytest_hp", "propertytest_hp/config");
        var p = new XltPropertiesImpl(null, null, true, false);

        // Data directory is overridden with empty value
        p.setProperty(XltConstants.PROP_DATA_DIRECTORY, "");

        // Getter for data directory returns the default data directory
        assertEquals(configDir.getPath(), p.getConfigDirectory());
        assertEquals(homeDir.getPath().resolve(XltConstants.DEFAULT_DATA_DIR_PATH), p.getDataDirectory());
    }

    /**
     * Get config and data directories after updating data directory path in properties
     */
    @Test
    public void getConfigAndDataDirs_UpdateDataDirInProperty() throws FileSystemException
    {
        setup("propertytest_hp", "propertytest_hp/config");
        var p = new XltPropertiesImpl(null, null, true, false);

        // Property isn't set initially; data dir is default data dir path in home dir
        assertEquals(configDir.getPath(), p.getConfigDirectory());
        assertEquals(homeDir.getPath().resolve(XltConstants.DEFAULT_DATA_DIR_PATH), p.getDataDirectory());

        // Set absolute path in property; data dir is set to the absolute path
        final Path absolutePath = getResourceFile("propertytest_dirinclude/config").getPath().toAbsolutePath();
        p.setProperty(XltConstants.PROP_DATA_DIRECTORY, absolutePath.toString());
        assertEquals(configDir.getPath(), p.getConfigDirectory());
        assertEquals(absolutePath, p.getDataDirectory());

        // Set relative path in property; data dir is set to relative path in home dir
        final String relativePath = "datadir/subdir";
        p.setProperty(XltConstants.PROP_DATA_DIRECTORY, relativePath);
        assertEquals(configDir.getPath(), p.getConfigDirectory());
        assertEquals(homeDir.getPath().resolve(relativePath), p.getDataDirectory());

        // Remove property; data dir is default data dir path in home dir again
        p.removeProperty(XltConstants.PROP_DATA_DIRECTORY);
        assertEquals(configDir.getPath(), p.getConfigDirectory());
        assertEquals(homeDir.getPath().resolve(XltConstants.DEFAULT_DATA_DIR_PATH), p.getDataDirectory());
    }
}
