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
/**
 * 
 */
package com.xceptance.xlt.engine;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

import org.apache.commons.vfs2.FileObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(
    {
        XltExecutionContext.class, System.class
    })
public class XltExecutionContextTest
{
    private static class TestObjectCreationException extends Exception
    {
        private static final long serialVersionUID = 1L;

        public TestObjectCreationException(Throwable cause)
        {
            super("Failed to create the test object", cause);
        }
    }

    /**
     * Get a new instance of {@link XltExecutionContext} by calling the private default constructor. For testing we need
     * a fresh test object for each test but {@link XltExecutionContext#getCurrent()} returns a singleton instance which
     * we can not use. This method will call the default constructor.
     * 
     * @return a new instance of {@link XltExecutionContext}
     * @throws TestObjectCreationException
     */
    private static XltExecutionContext createNewTestObject() throws TestObjectCreationException
    {
        try
        {
            return constructor(XltExecutionContext.class).newInstance();
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            throw new TestObjectCreationException(e);
        }
    }

    private static void assertSamePath(File file1, File file2)
    {
        Path normalizedPath1 = toNormalizedPath(file1);
        Path normalizedPath2 = toNormalizedPath(file2);

        assertTrue("<" + normalizedPath1 + "> is not the same file as expected <" + normalizedPath2 + ">",
                   normalizedPath1.equals(normalizedPath2));
    }

    private static Path toNormalizedPath(File file)
    {
        return file.toPath().normalize().toAbsolutePath();
    }

    /**
     * Test method for {@link XltExecutionContext#getTestSuiteHomeDir()}.
     * 
     * @throws TestObjectCreationException
     */
    @Test
    public final void testGetTestSuiteHomeDir_default() throws TestObjectCreationException
    {
        // GIVEN: a fresh default XltExecutionContext
        XltExecutionContext newContext = createNewTestObject();

        // WHEN: getting the test suite home directory
        File testSuiteHomeDir = newContext.getTestSuiteHomeDirAsFile();

        // THEN: the returned test suite home directory should be the same as the current working directory
        assertSamePath(testSuiteHomeDir, new File(System.getProperty("user.dir")));
    }

    /**
     * Test method for {@link XltExecutionContext#getTestSuiteHomeDir()}.
     * 
     * @throws TestObjectCreationException
     */
    @Test
    public final void testGetTestSuiteHomeDir_propertyDefined() throws TestObjectCreationException
    {
        // TEST_PARAMETER:
        final String systemPropertyValue = "/foo";

        // BEFORE: the system property for the test suite home directory is set
        spy(System.class);
        when(System.getProperty("com.xceptance.xlt.testSuiteHomeDir")).thenReturn(systemPropertyValue);

        // GIVEN: a fresh default XltExecutionContext
        XltExecutionContext newContext = createNewTestObject();

        // WHEN: getting the test suite home directory
        File testSuiteHomeDir = newContext.getTestSuiteHomeDirAsFile();

        // THEN: the returned test suite home directory should be the same as defined by the system property
        assertSamePath(testSuiteHomeDir, new File(systemPropertyValue));
    }

    /**
     * Test method for {@link XltExecutionContext#getTestSuiteHomeDir()}.
     * 
     * @throws TestObjectCreationException
     */
    @Test
    public final void testGetTestSuiteHomeDir_environmentDefined() throws TestObjectCreationException
    {
        // TEST_PARAMETER:
        final String environmentVariableValue = "/bar";

        // BEFORE: the environment variable for the test suite home directory is set
        spy(System.class);
        when(System.getenv("XLT_TEST_SUITE_HOME_DIR")).thenReturn(environmentVariableValue);

        // GIVEN: a fresh default XltExecutionContext
        XltExecutionContext newContext = createNewTestObject();

        // WHEN: getting the test suite home directory
        File testSuiteHomeDir = newContext.getTestSuiteHomeDirAsFile();

        // THEN: the returned test suite home directory should be the same as defined by the environment variable
        assertSamePath(testSuiteHomeDir, new File(environmentVariableValue));
    }

    /**
     * Test method for {@link XltExecutionContext#getTestSuiteHomeDir()}.
     * 
     * @throws TestObjectCreationException
     */
    @Test
    public final void testGetTestSuiteHomeDir_propertyAndEnvironmentDefined() throws TestObjectCreationException
    {
        // TEST_PARAMETER:
        final String systemPropertyValue = "/foo";
        final String environmentVariableValue = "/bar";

        // BEFORE: the system property and the environment variable for the test suite home directory is set
        spy(System.class);
        when(System.getProperty("com.xceptance.xlt.testSuiteHomeDir")).thenReturn(systemPropertyValue);
        when(System.getenv("XLT_TEST_SUITE_HOME_DIR")).thenReturn(environmentVariableValue);

        // GIVEN: a fresh default XltExecutionContext
        XltExecutionContext newContext = createNewTestObject();

        // WHEN: getting the test suite home directory
        File testSuiteHomeDir = newContext.getTestSuiteHomeDirAsFile();

        // THEN: the returned test suite home directory should be the same as defined by the system property
        assertSamePath(testSuiteHomeDir, new File(systemPropertyValue));
    }

    /**
     * Test method for {@link XltExecutionContext#getTestSuiteConfigDir()}.
     * 
     * @throws TestObjectCreationException
     */
    @Test
    public final void testGetTestSuiteConfigDir_default() throws TestObjectCreationException
    {
        // GIVEN: a fresh default XltExecutionContext
        XltExecutionContext newContext = createNewTestObject();

        // WHEN: getting the test suite config directory
        FileObject testSuiteConfigDir = newContext.getTestSuiteConfigDir();

        // THEN: the returned test suite config directory should be located in the test suite home directory
        assertSamePath(new File(testSuiteConfigDir.getName().getPath()), new File(newContext.getTestSuiteHomeDirAsFile(), "config"));
    }
}
