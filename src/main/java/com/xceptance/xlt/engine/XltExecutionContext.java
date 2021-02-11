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
package com.xceptance.xlt.engine;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;

import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.common.XltConstants;

/**
 * Helper class to get access to home and configuration directory of XLT and the current test suite, respectively.
 */
public final class XltExecutionContext
{
    /**
     * The execution context singleton.
     */
    private static final XltExecutionContext context = new XltExecutionContext();

    /**
     * Returns the current execution context singleton.
     * 
     * @return the execution context
     */
    public static XltExecutionContext getCurrent()
    {
        return context;
    }

    /**
     * The test suite's configuration directory (defaults to "./config"). This is a {@link FileObject}, so the
     * configuration directory can also be located in an archive, etc.
     */
    private FileObject testSuiteConfigDir;

    /**
     * The test suite's home directory (defaults to ".").
     */
    private FileObject testSuiteHomeDir;

    /**
     * The XLT configuration directory (defaults to "xltHomeDir/config").
     */
    private File xltConfigDir;

    /**
     * The XLT home directory.
     */
    private final File xltHomeDir;

    private XltExecutionContext()
    {
        // determine XLT home directory
        final String xltHomeDirPath = getXltPropertyValue("home", "XLT_HOME", ".");
        xltHomeDir = new File(xltHomeDirPath);

        // determine XLT configuration directory
        final String configDirPath = getXltPropertyValue("configDir", "XLT_CONFIG_DIR", "config");
        xltConfigDir = new File(configDirPath);
        if (!xltConfigDir.isAbsolute())
        {
            xltConfigDir = new File(xltHomeDir, configDirPath);
        }

        // determine test suite home directory
        final String testSuiteHomePath = getXltPropertyValue("testSuiteHomeDir", "XLT_TEST_SUITE_HOME_DIR", ".");
        final File testSuiteHome = new File(testSuiteHomePath);
        setTestSuiteHomeDir(testSuiteHome);

        // determine test suite configuration directory
        setTestSuiteConfigDir(new File(testSuiteHome, "config"));
    }

    public FileObject getTestSuiteConfigDir()
    {
        return testSuiteConfigDir;
    }

    public FileObject getTestSuiteHomeDir()
    {
        return testSuiteHomeDir;
    }

    public File getTestSuiteHomeDirAsFile()
    {
        return testSuiteHomeDir == null ? null : new File(testSuiteHomeDir.getName().getPath());
    }

    public File getXltConfigDir()
    {
        return xltConfigDir;
    }

    public File getXltHomeDir()
    {
        return xltHomeDir;
    }

    public void setTestSuiteConfigDir(final File dir)
    {
        try
        {
            testSuiteConfigDir = VFS.getManager().resolveFile(dir.getAbsolutePath());
        }
        catch (final FileSystemException e)
        {
            throw new XltException("Failed to resolve directory: " + dir, e);
        }
    }

    public void setTestSuiteConfigDir(final FileObject dir)
    {
        testSuiteConfigDir = dir;
    }

    public void setTestSuiteHomeDir(final File dir)
    {
        try
        {
            testSuiteHomeDir = VFS.getManager().resolveFile(dir.getAbsolutePath());
        }
        catch (final FileSystemException e)
        {
            throw new XltException("Failed to resolve directory: " + dir, e);
        }
    }

    public void setTestSuiteHomeDir(final FileObject dir)
    {
        testSuiteHomeDir = dir;
    }

    /**
     * Get a XLT property value by first trying to resolve the system variable. If nothing was found then try to find
     * the environment variable. If nothing was found then return a given default value.
     * 
     * @param systemPropertyName
     *            - The system property to look for (prefixed with {@link XltConstants#XLT_PACKAGE_PATH} )
     * @param environmentPropertyName
     *            - The environment variable to look for
     * @param defaultValue
     *            - The value that will be returned if nothing else was found
     * @return the resolved value of either the system property if available, the system property if available or the
     *         given default value
     */
    private String getXltPropertyValue(String systemPropertyName, String environmentPropertyName, String defaultValue)
    {
        String propertyValue = System.getProperty(XltConstants.XLT_PACKAGE_PATH + "." + systemPropertyName);
        if (StringUtils.isBlank(propertyValue))
        {
            propertyValue = System.getenv(environmentPropertyName);
            if (StringUtils.isBlank(propertyValue))
            {
                propertyValue = defaultValue;
            }
        }
        return propertyValue;
    }
}
