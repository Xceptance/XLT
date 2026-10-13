/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.xceptance.common.io.fs.FsResource;
import com.xceptance.common.io.fs.VirtualFileSystem;
import com.xceptance.xlt.common.XltConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private static FsResource origHome;
    private static FsResource origConfig;

    private FsResource homeDir;
    private FsResource configDir;

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
    public void setup(final String home, final String config) throws IOException
    {
        homeDir = getResourceFile(home);
        configDir = homeDir.resolve("config");

        XltExecutionContext.getCurrent().setTestSuiteConfigDir(configDir);
        XltExecutionContext.getCurrent().setTestSuiteHomeDir(homeDir);

        // just make something known, so we can check that we loaded it
        setSystemProperty("systemkey", "systemkeyvalue");
    }

    /**
     * Get a FsResource for the resource with the given name
     */
    private FsResource getResourceFile(final String resourceName) throws IOException
    {
        var path = getClass().getResource(resourceName).getFile();
        return VirtualFileSystem.open(new File(path).toPath()).getRoot();
    }

    private static String getRelativeName(final FsResource base, final FsResource target)
    {
        if (base.toLocalPath().isPresent() && target.toLocalPath().isPresent())
        {
            return base.toLocalPath().get().relativize(target.toLocalPath().get()).toString();
        }
        final String basePath = base.getPath();
        final String targetPath = target.getPath();
        if (basePath.isEmpty())
        {
            return targetPath;
        }
        if (targetPath.startsWith(basePath + "/"))
        {
            return targetPath.substring(basePath.length() + 1);
        }
        return targetPath;
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

        System.clearProperty("test");

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
     * @throws Exception
     */
    @Test
    public void happyPath() throws Exception
    {
        setup("propertytest_hp", "propertytest_hp/config");

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);


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

        assertEquals("config/default.properties", getRelativeName(homeDir, files.get(0)));
        assertEquals("config/project.properties", getRelativeName(homeDir, files.get(1)));
        assertEquals("config/test.properties",    getRelativeName(homeDir, files.get(2)));
        assertEquals("config/dev.properties",     getRelativeName(homeDir, files.get(3)));
        assertEquals("config/secret.properties",  getRelativeName(homeDir, files.get(4)));

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
    public void noDevPropsLoaded() throws Exception
    {
        setup("propertytest_hp", "propertytest_hp/config");

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = XltEngine.reset(new XltPropertiesImpl((FsResource) null, (FsResource) null, false, false)).xltProperties;

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

        assertEquals("config/default.properties", getRelativeName(homeDir, files.get(0)));
        assertEquals("config/project.properties", getRelativeName(homeDir, files.get(1)));
        assertEquals("config/test.properties",    getRelativeName(homeDir, files.get(2)));
        assertEquals("config/secret.properties",  getRelativeName(homeDir, files.get(3)));

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
    public void noSecret() throws Exception
    {
        setup("propertytest_nosecret", "propertytest_nosecret/config");

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, false, false);

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
        assertEquals("config/default.properties", getRelativeName(homeDir, files.get(0)));
        assertEquals("config/project.properties", getRelativeName(homeDir, files.get(1)));
        assertEquals("config/test.properties",    getRelativeName(homeDir, files.get(2)));

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
    public void testViaSystem() throws Exception
    {
        setup("propertytest_testfromsystem", "propertytest_testfromsystem/config");
        setSystemProperty("com.xceptance.xlt.testPropertiesFile", "test2.properties");

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, false, false);

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
        assertEquals("config/default.properties", getRelativeName(homeDir, files.get(0)));
        assertEquals("config/project.properties", getRelativeName(homeDir, files.get(1)));
        assertEquals("config/test2.properties",   getRelativeName(homeDir, files.get(2)));

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
    public void testNotSet() throws Exception
    {
        setup("propertytest_notestfallback", "propertytest_notestfallback/config");

        // we don't complain about not test props
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, false, false);

        var files = p.getUsedPropertyFiles();
        assertEquals(2, files.size());
        assertEquals("config/default.properties", getRelativeName(homeDir, files.get(0)));
        assertEquals("config/project.properties", getRelativeName(homeDir, files.get(1)));

        var rFilename = p.getUsedPropertyFilesByRelativeName();
        assertEquals(2, rFilename.size());
        assertEquals("default.properties", rFilename.get(0));
        assertEquals("project.properties", rFilename.get(1));
    }

    /**
     * Test does not exists
     */
    @Test
    public void testDoesNotExist() throws Exception
    {
        setup("propertytest_notestfallback", "propertytest_notestfallback/config");
        setSystemProperty("com.xceptance.xlt.testPropertiesFile", "noidea.properties");

        try
        {
            new XltPropertiesImpl((FsResource) null, (FsResource) null, false, false);

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
    public void defaultDoesNotExist() throws Exception
    {
        setup("propertytest_nodefaults", "propertytest_nodefaults/config");
        setSystemProperty("com.xceptance.xlt.testPropertiesFile", "test2.properties");

        new XltPropertiesImpl((FsResource) null, (FsResource) null, false, false);
    }

    /**
     * nothing at all
     */
    @Test
    public void nothingExists() throws Exception
    {
        setup("propertytest_nothing", "propertytest_nothing/config");
        new XltPropertiesImpl((FsResource) null, (FsResource) null, false, false);
    }

    /**
     * Clear
     * @throws Exception
     */
    @Test
    public void clear() throws Exception
    {
        setup("propertytest_hp", "propertytest_hp/config");

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

        // we are not in control of most system props, so we have to exclude that count dynamically and we have to add our
        // only system prop with +1
        assertEquals(8, p.getProperties().size() - System.getProperties().size() + 1);

        // ok, more about the files
        var files = p.getUsedPropertyFiles();
        assertEquals(5, files.size());

        assertEquals("config/default.properties", getRelativeName(homeDir, files.get(0)));
        assertEquals("config/project.properties", getRelativeName(homeDir, files.get(1)));
        assertEquals("config/test.properties",    getRelativeName(homeDir, files.get(2)));
        assertEquals("config/dev.properties",     getRelativeName(homeDir, files.get(3)));
        assertEquals("config/secret.properties",  getRelativeName(homeDir, files.get(4)));

        p.clear();
        assertEquals(0, p.getProperties().size());
    }

    /**
     * Reset
     * @throws Exception
     */
    @Test
    public void reset() throws Exception
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

            assertEquals("config/default.properties", getRelativeName(homeDir, files.get(0)));
            assertEquals("config/project.properties", getRelativeName(homeDir, files.get(1)));
            assertEquals("config/test.properties",    getRelativeName(homeDir, files.get(2)));
            assertEquals("config/dev.properties",     getRelativeName(homeDir, files.get(3)));
            assertEquals("config/secret.properties",  getRelativeName(homeDir, files.get(4)));
        };

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = XltEngine.reset(new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false)).xltProperties;

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
    public void simpleFileInclude() throws Exception
    {
        setup("propertytest_simpleinclude", "propertytest_simpleinclude/config");

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

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

        assertEquals("config/default.properties", getRelativeName(homeDir, files.get(0)));
        assertEquals("config/include.properties", getRelativeName(homeDir, files.get(1)));
        assertEquals("config/project.properties", getRelativeName(homeDir, files.get(2)));
        assertEquals("config/test.properties",    getRelativeName(homeDir, files.get(3)));
        assertEquals("config/dev.properties",     getRelativeName(homeDir, files.get(4)));
        assertEquals("config/secret.properties",  getRelativeName(homeDir, files.get(5)));

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
    public void simpleDirInclude() throws Exception
    {
        setup("propertytest_dirinclude", "propertytest_dirinclude/config");

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

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

        assertEquals("config/default.properties", getRelativeName(homeDir, files.get(0)));
        assertEquals("config/dir/a.properties", getRelativeName(homeDir, files.get(1)));
        assertEquals("config/dir/b.properties", getRelativeName(homeDir, files.get(2)));
        assertEquals("config/dir/c.properties", getRelativeName(homeDir, files.get(3)));
        assertEquals("config/project.properties", getRelativeName(homeDir, files.get(4)));
        assertEquals("config/test.properties",    getRelativeName(homeDir, files.get(5)));
        assertEquals("config/dev.properties",     getRelativeName(homeDir, files.get(6)));
        assertEquals("config/secret.properties",  getRelativeName(homeDir, files.get(7)));

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
    public void simpleFileIncludeMissing() throws Exception
    {
        setup("propertytest_simpleincludemissing", "propertytest_simpleincludemissing/config");

        // load the happy path defaults, default, project, test, dev, secret, system
        var p = XltEngine.reset(new XltPropertiesImpl((FsResource) null, (FsResource) null, false, true)).xltProperties;

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

        assertEquals("config/default.properties", getRelativeName(homeDir, files.pollFirst()));
        assertEquals("config/project.properties", getRelativeName(homeDir, files.pollFirst()));
        assertEquals("config/test.properties",    getRelativeName(homeDir, files.pollFirst()));
        assertEquals("config/secret.properties",  getRelativeName(homeDir, files.pollFirst()));

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
    public void simpleFileIncludeMissingDontIgnore() throws Exception
    {
        setup("propertytest_simpleincludemissing", "propertytest_simpleincludemissing/config");

        try
        {
            new XltPropertiesImpl((FsResource) null, (FsResource) null, false, false);
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
    public void includeConfigProblem() throws Exception
    {
        setup("propertytest_incorrectinclude", "propertytest_incorrectinclude/config");

        try
        {
            new XltPropertiesImpl((FsResource) null, (FsResource) null, false, false);
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
    public void getConfigAndDataDirs_SetDirsInConstructor() throws Exception
    {
        // Set home and config dir in execution context
        setup("propertytest_hp", "propertytest_hp/config");

        // Provide different home and config dirs to XltPropertiesImpl constructor
        final FsResource homeOverride = getResourceFile("propertytest_notestfallback");
        final FsResource configOverride = getResourceFile("propertytest_notestfallback/config");
        var p = new XltPropertiesImpl(homeOverride, configOverride, true, false);

        // Getter return values are based on the directories set in the constructor
        assertEquals(configOverride.toLocalPath().get(), p.getConfigDirectory());
        assertEquals(homeOverride.toLocalPath().get().resolve(XltConstants.DEFAULT_DATA_DIR_PATH), p.getDataDirectory());
    }

    /**
     * Test getConfigDirectory and getDataDirectory when backing files reside in a zip archive (virtual file system).
     */
    @Test
    public void getConfigAndDataDirs_ArchiveFile() throws Exception
    {
        final File zipFile = tempFolder.newFile("testsuite.zip");
        try (final java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(zipFile)))
        {
            zos.putNextEntry(new java.util.zip.ZipEntry("config/default.properties"));
            final byte[] content = "prop1 = val1\ncom.xceptance.xlt.data.directory = data\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            zos.write(content);
            zos.closeEntry();
        }

        try (final VirtualFileSystem vfs = VirtualFileSystem.open(zipFile.toPath()))
        {
            final FsResource archive = vfs.getRoot();
            final FsResource configDir = archive.resolve("config");

            var p = new XltPropertiesImpl(archive, configDir, true, false);

            assertEquals("val1", p.getProperty("prop1"));
            assertNull(p.getConfigDirectory());
            assertNull(p.getDataDirectory());

            // Test with absolute data directory property
            p.setProperty(XltConstants.PROP_DATA_DIRECTORY, "/tmp/custom-data");
            assertEquals(java.nio.file.Paths.get("/tmp/custom-data"), p.getDataDirectory());
        }
    }

    /**
     * Get config and data directories from XltPropertiesImpl object that uses the directories from the execution
     * context
     */
    @Test
    public void getConfigAndDataDirs_UseDirsFromExecutionContext() throws Exception
    {
        // Set home and config dir in execution context
        setup("propertytest_hp", "propertytest_hp/config");

        // Don't provide home and config dirs to XltPropertiesImpl constructor
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

        // Getter return values are based on the directories from the execution context
        assertEquals(configDir.toLocalPath().get(), p.getConfigDirectory());
        assertEquals(homeDir.toLocalPath().get().resolve(XltConstants.DEFAULT_DATA_DIR_PATH), p.getDataDirectory());
    }

    /**
     * Get config and data directories from XltPropertiesImpl objects created before and after updating the execution
     * context directories
     */
    @Test
    public void getConfigAndDataDirs_UpdateDirsInExecutionContext() throws Exception
    {
        // Create instance of XltProperties with directories from execution context and remember the directories
        setup("propertytest_hp", "propertytest_hp/config");
        final FsResource homeDir1 = XltExecutionContext.getCurrent().getTestSuiteHomeDir();
        final FsResource configDir1 = XltExecutionContext.getCurrent().getTestSuiteConfigDir();
        var p1 = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

        // Update execution context and create a second instance
        setup("propertytest_notestfallback", "propertytest_notestfallback/config");
        var p2 = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

        // Getter return values are based on the home and config directories set during object creation
        assertEquals(configDir1.toLocalPath().get(), p1.getConfigDirectory());
        assertEquals(homeDir1.toLocalPath().get().resolve(XltConstants.DEFAULT_DATA_DIR_PATH), p1.getDataDirectory());
        assertEquals(configDir.toLocalPath().get(), p2.getConfigDirectory());
        assertEquals(homeDir.toLocalPath().get().resolve(XltConstants.DEFAULT_DATA_DIR_PATH), p2.getDataDirectory());
    }

    /**
     * Get config and data directories from XltPropertiesImpl object with empty properties
     */
    @Test
    public void getConfigAndDataDirs_InstanceWithoutProperties() throws Exception
    {
        setup("propertytest_hp", "propertytest_hp/config");

        // Create instance of XltPropertiesImpl using the constructor with empty properties
        var p = new XltPropertiesImpl();

        // Getter return values are based on the directories from the execution context
        assertEquals(configDir.toLocalPath().get(), p.getConfigDirectory());
        assertEquals(homeDir.toLocalPath().get().resolve(XltConstants.DEFAULT_DATA_DIR_PATH), p.getDataDirectory());
    }

    /**
     * Get config and data directories when relative data directory path is set in the properties
     */
    @Test
    public void getConfigAndDataDirs_RelativeDataDirFromProperty() throws Exception
    {
        setup("propertytest_hp", "propertytest_hp/config");
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

        // Data directory is overridden with relative path in properties
        final String relativePath = "datadir";
        p.setProperty(XltConstants.PROP_DATA_DIRECTORY, relativePath);

        // Getter for data directory returns the relative path inside the provided home directory
        assertEquals(configDir.toLocalPath().get(), p.getConfigDirectory());
        assertEquals(homeDir.toLocalPath().get().resolve(relativePath), p.getDataDirectory());
    }

    /**
     * Get config and data directories when absolute data directory path is set in the properties
     */
    @Test
    public void getConfigAndDataDirs_AbsoluteDataDirFromProperty() throws Exception
    {
        setup("propertytest_hp", "propertytest_hp/config");
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

        // Data directory is overridden with absolute directory in properties
        final Path absolutePath = getResourceFile("propertytest_dirinclude").toLocalPath().get().toAbsolutePath();
        p.setProperty(XltConstants.PROP_DATA_DIRECTORY, absolutePath.toString());

        // Getter for data directory returns the absolute directory
        assertEquals(configDir.toLocalPath().get(), p.getConfigDirectory());
        assertEquals(absolutePath, p.getDataDirectory());
    }

    /**
     * Get config and data directories when empty data directory path is set in the properties
     */
    @Test
    public void getConfigAndDataDirs_EmptyDataDirFromProperty() throws Exception
    {
        setup("propertytest_hp", "propertytest_hp/config");
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

        // Data directory is overridden with empty value
        p.setProperty(XltConstants.PROP_DATA_DIRECTORY, "");

        // Getter for data directory returns the default data directory
        assertEquals(configDir.toLocalPath().get(), p.getConfigDirectory());
        assertEquals(homeDir.toLocalPath().get().resolve(XltConstants.DEFAULT_DATA_DIR_PATH), p.getDataDirectory());
    }

    /**
     * Get config and data directories after updating data directory path in properties
     */
    @Test
    public void getConfigAndDataDirs_UpdateDataDirInProperty() throws Exception
    {
        setup("propertytest_hp", "propertytest_hp/config");
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

        // Property isn't set initially; data dir is default data dir path in home dir
        assertEquals(configDir.toLocalPath().get(), p.getConfigDirectory());
        assertEquals(homeDir.toLocalPath().get().resolve(XltConstants.DEFAULT_DATA_DIR_PATH), p.getDataDirectory());

        // Set absolute path in property; data dir is set to the absolute path
        final Path absolutePath = getResourceFile("propertytest_dirinclude/config").toLocalPath().get().toAbsolutePath();
        p.setProperty(XltConstants.PROP_DATA_DIRECTORY, absolutePath.toString());
        assertEquals(configDir.toLocalPath().get(), p.getConfigDirectory());
        assertEquals(absolutePath, p.getDataDirectory());

        // Set relative path in property; data dir is set to relative path in home dir
        final String relativePath = "datadir/subdir";
        p.setProperty(XltConstants.PROP_DATA_DIRECTORY, relativePath);
        assertEquals(configDir.toLocalPath().get(), p.getConfigDirectory());
        assertEquals(homeDir.toLocalPath().get().resolve(relativePath), p.getDataDirectory());

        // Remove property; data dir is default data dir path in home dir again
        p.removeProperty(XltConstants.PROP_DATA_DIRECTORY);
        assertEquals(configDir.toLocalPath().get(), p.getConfigDirectory());
        assertEquals(homeDir.toLocalPath().get().resolve(XltConstants.DEFAULT_DATA_DIR_PATH), p.getDataDirectory());
    }

    // =============================================
    // Groovy Property Expansion Tests
    // =============================================
    /**
     * Test that Groovy expressions using ${...} variable substitution work correctly
     * within the XltProperties integration layer.
     * @throws Exception 
     */
    @Test
    public void oneFileCalculation() throws Exception
    {
        setup("propertytest_groovy_1", "propertytest_groovy_1/config");

        // Create instance of XltPropertiesImpl 
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

        // ask for simple properties first
        assertEquals(100, p.getProperty("totalUsers", 0));

        // simple evaluation without required larger context
        assertEquals(400, p.getProperty("TBrowse.users", 0));
        assertEquals(300, p.getProperty("TSearch.users", 0));
        assertEquals(200, p.getProperty("TOrder.users", 0));
        assertEquals(100, p.getProperty("TCheckout.users", 0));
        
        // stacked with references to other dynamic props
        assertEquals(10, p.getProperty("TBrowse2.users", 0));
        assertEquals(10 * 2, p.getProperty("TSearch2.users", 0));
        assertEquals(10 * 2 * 3, p.getProperty("TCheckout2.users", 0));
        assertEquals(10 * 2 * 3 * 4, p.getProperty("TOrder2.users", 0));
    }

    @Test
    public void groovyFloatEvaluationFallback() throws Exception
    {
        setup("propertytest_groovy_1", "propertytest_groovy_1/config");
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

        // Inject a property that explicitly evaluates to a floating-point string
        p.setProperty("float.test", "#{ 100 * 0.405 }");
        
        // Assert that the raw string is a float with preserved scale from the multiplier
        assertEquals("40.500", p.getProperty("float.test"));

        // Assert that getProperty(key, defaultValue) correctly falls back to defaultValue (99)
        // rather than crashing with NumberFormatException
        assertEquals(99, p.getProperty("float.test", 99));
    }

    // =============================================
    // Doc: "Load Mix Calculation" — .intValue() for clean integer results
    // =============================================
    /**
     * Validates the documented load mix calculation pattern:
     * {@code #{ (${totalUsers} * 0.40).intValue() }}
     * Ensures .intValue() produces clean integers without trailing decimals.
     *
     * @see PROPERTY_EXPANSION.md "Practical Examples / Load Mix Calculation"
     */
    @Test
    public void loadMixCalculationWithIntValue() throws Exception
    {
        setup("propertytest_groovy_1", "propertytest_groovy_1/config");
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

        // totalUsers = 100, .intValue() produces clean integers
        assertEquals(40, p.getProperty("mix.TBrowse.users", 0));
        assertEquals(30, p.getProperty("mix.TSearch.users", 0));
        assertEquals(10, p.getProperty("mix.TOrder.users", 0));
        assertEquals(20, p.getProperty("mix.TCheckout.users", 0));
    }

    // =============================================
    // Doc: "Environment-Based Configuration" — multiplier scaling
    // =============================================
    /**
     * Validates the documented environment-based scaling pattern:
     * {@code #{ (${base.browse.users} * ${load.multiplier}).intValue() }}
     *
     * @see PROPERTY_EXPANSION.md "Practical Examples / Environment-Based Configuration"
     */
    @Test
    public void environmentBasedMultiplierScaling() throws Exception
    {
        setup("propertytest_groovy_1", "propertytest_groovy_1/config");
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

        // base.browse.users = 50, load.multiplier = 1.5 → 75
        assertEquals(75, p.getProperty("scaled.TBrowse.users", 0));
    }

    // =============================================
    // Doc: "Supported Operations — Conditionals"
    // =============================================
    /**
     * Validates that ternary conditional expressions work with ${} substitution.
     * String values must be quoted when pasted via ${} into Groovy.
     *
     * @see PROPERTY_EXPANSION.md "Supported Operations"
     */
    @Test
    public void conditionalTernaryOperator() throws Exception
    {
        setup("propertytest_groovy_1", "propertytest_groovy_1/config");
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

        // mode=production → quoted as '${mode}' in Groovy → 100
        assertEquals(100, p.getProperty("conditional.users", 0));
    }

    // =============================================
    // Doc: "Supported Operations — Arithmetic"
    // =============================================
    /**
     * Validates that modulo arithmetic works in Groovy expressions.
     *
     * @see PROPERTY_EXPANSION.md "Supported Operations"
     */
    @Test
    public void arithmeticModulo() throws Exception
    {
        setup("propertytest_groovy_1", "propertytest_groovy_1/config");
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

        // 17 % 5 = 2
        assertEquals(2, p.getProperty("modulo.result", 0));
    }

    // =============================================
    // Doc: "Supported Operations — Type conversion"
    // =============================================
    /**
     * Validates the documented type conversion operations: {@code as int}, {@code as double}, {@code as String}.
     *
     * @see PROPERTY_EXPANSION.md "Supported Operations"
     */
    @Test
    public void typeConversion() throws Exception
    {
        setup("propertytest_groovy_1", "propertytest_groovy_1/config");
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

        assertEquals(42, p.getProperty("typed.int", 0));
        assertEquals("3.14", p.getProperty("typed.double"));
        assertEquals("42", p.getProperty("typed.string"));
    }

    // =============================================
    // Doc: "Error Handling"
    // =============================================
    /**
     * Validates the documented error handling behavior: invalid Groovy syntax
     * throws {@link IllegalArgumentException} with the script content in the message.
     *
     * @see PROPERTY_EXPANSION.md "Error Handling"
     */
    @Test
    public void invalidGroovySyntaxThrowsWithMessage() throws Exception
    {
        setup("propertytest_groovy_1", "propertytest_groovy_1/config");
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

        // Inject invalid Groovy syntax at runtime
        p.setProperty("bad.syntax", "#{ this is not valid ++ }");

        try
        {
            p.getProperty("bad.syntax");
            fail("Expected IllegalArgumentException for invalid Groovy syntax");
        }
        catch (final IllegalArgumentException e)
        {
            assertTrue("Exception message should contain the script",
                       e.getMessage().contains("this is not valid ++"));
        }
    }

    // =============================================
    // Doc: "Security" — blocked operations
    // =============================================
    /**
     * Validates the documented security sandbox: file system access, network operations,
     * system access, thread creation, and reflection are all blocked with an
     * {@link IllegalArgumentException}.
     *
     * @see PROPERTY_EXPANSION.md "Security"
     */
    @Test
    public void securitySandboxBlocksDangerousOperations() throws Exception
    {
        setup("propertytest_groovy_1", "propertytest_groovy_1/config");
        var p = new XltPropertiesImpl((FsResource) null, (FsResource) null, true, false);

        // Doc: "File system access (File, FileReader, etc.)"
        assertSecurityBlocks(p, "#{ new java.io.File('/etc/passwd') }", "File access");

        // Doc: "Network operations (URL, Socket, etc.)"
        assertSecurityBlocks(p, "#{ new java.net.URL('http://evil.com') }", "URL access");

        // Doc: "System access (System, Runtime)"
        assertSecurityBlocks(p, "#{ System.exit(0) }", "System.exit");
        assertSecurityBlocks(p, "#{ Runtime.getRuntime().exec('ls') }", "Runtime.exec");

        // Doc: "Thread creation"
        assertSecurityBlocks(p, "#{ new Thread({ }).start() }", "Thread creation");

        // Doc: "Reflection"
        assertSecurityBlocks(p, "#{ Class.forName('java.lang.Runtime') }", "Reflection");
    }

    /**
     * Helper to assert that a Groovy expression is blocked by the security sandbox.
     */
    private void assertSecurityBlocks(final XltPropertiesImpl p, final String expression, final String description)
    {
        p.setProperty("security.test", expression);
        try
        {
            p.getProperty("security.test");
            fail("Expected IllegalArgumentException for blocked operation: " + description);
        }
        catch (final IllegalArgumentException e)
        {
            // Expected — security sandbox blocked it
        }
    }
}
