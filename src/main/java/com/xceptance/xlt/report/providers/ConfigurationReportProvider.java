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
package com.xceptance.xlt.report.providers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import com.xceptance.common.util.ProductInformation;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.mastercontroller.TestLoadProfileConfiguration;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 * Report provider generating a report fragment about the configuration used for the test run
 */
public class ConfigurationReportProvider extends AbstractReportProvider
{

    private static final String LOADTEST_PROP = XltConstants.XLT_PACKAGE_PATH + ".loadtests";

    private static final String MASK_PROPERTIES_PROP = XltConstants.XLT_PACKAGE_PATH + ".reportgenerator.maskPropertiesRegex";

    private static final String MASK_PROPERTIES_REGEX_DEFAULT = "(?i)password";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final ConfigurationReport report = new ConfigurationReport();

        final File reportDirectory = getConfiguration().getReportDirectory();
        final File configDir = new File(reportDirectory, XltConstants.CONFIG_DIR_NAME);

        final XltProperties props;
        try
        {

            final FileSystemManager fsMgr = VFS.getManager();
            props = new XltPropertiesImpl(fsMgr.resolveFile(reportDirectory.getAbsolutePath()),
                                          fsMgr.resolveFile(configDir.getAbsolutePath()), true);

        }
        catch (FileSystemException fse)
        {
            System.err.println();
            return report;
        }

        final File jvmArgsFile = new File(configDir, XltConstants.JVM_PARAMETER_FILENAME);

        // add the masked plain properties
        report.properties.putAll(mask(props.getProperties()));

        // add product information for later output
        report.version = ProductInformation.getProductInformation();

        // get test comment specified via property
        final TreeMap<String, String> sortedLoadtestProps = new TreeMap<String, String>();
        final Map<String, String> loadtestProps = props.getPropertiesForKey(LOADTEST_PROP);
        for (final Entry<String, String> entry : loadtestProps.entrySet())
        {
            final String propName = entry.getKey();

            if (RegExUtils.isMatching(propName, "(comment\\.?\\d*$)|(comment\\.commandLine$)"))
            {
                sortedLoadtestProps.put(propName, entry.getValue());
            }
        }

        for (final Map.Entry<String, String> entry : sortedLoadtestProps.entrySet())
        {
            report.comments.add(entry.getValue());
        }

        // add project name
        final String projectName = props.getProperty(XltConstants.PROJECT_NAME_PROPERTY);
        report.projectName = StringUtils.isNotBlank(projectName) ? projectName.trim() : null;

        // add the load profile
        try
        {
            report.loadProfile = new TestLoadProfileConfiguration(props.getProperties()).getLoadTestConfiguration();
        }
        catch (final Exception e)
        {
            System.err.println("Failed to get load test profile configuration. Cause: " + e.getMessage());
        }

        // add custom JVM settings
        try
        {
            report.customJvmArgs = getCustomJvmArgs(jvmArgsFile);
        }
        catch (final IOException ioe)
        {
            System.err.println("Failed to get custom JVM arguments. Cause: " + ioe.getMessage());
        }

        return report;
    }

    private Map<? extends Object, ? extends Object> mask(Properties properties)
    {
        final String MASK_PROPERTIES_REGEX = getConfiguration().getProperties().getProperty(MASK_PROPERTIES_PROP,
                                                                                            MASK_PROPERTIES_REGEX_DEFAULT);

        final boolean isMaskSet = StringUtils.isNoneBlank(MASK_PROPERTIES_REGEX);
        for (final Entry<Object, Object> entry : properties.entrySet())
        {
            final String propName = (String)entry.getKey();
            if (propName.startsWith(XltConstants.SECRET_PREFIX) ||
                isMaskSet && RegExUtils.isMatching((String) propName, MASK_PROPERTIES_REGEX))
            {
                properties.replace(propName, XltConstants.MASK_PROPERTIES_HIDETEXT);
            }
        }

        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        // nothing to do here
    }
    
    /**
     * Tell the system that there is no need to call processDataRecord
     */
    @Override
    public boolean wantsDataRecords()
    {
        return false;
    }
    
    /**
     * Returns the custom JVM arguments stored in the file "jvmargs.cfg". If no such file can be found, the returned
     * list is empty.
     *
     * @param file
     *            the configuration file
     * @return the list of JVM options
     * @throws IOException
     *             if the configuration file cannot be read
     */
    private List<String> getCustomJvmArgs(final File file) throws IOException
    {
        final ArrayList<String> jvmArgs = new ArrayList<String>();

        if (file.isFile())
        {
            try (final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), XltConstants.UTF8_ENCODING)))
            {
                String line = null;
                while ((line = in.readLine()) != null)
                {
                    // cut off any comment
                    final int i = line.indexOf('#');
                    if (i >= 0)
                    {
                        line = line.substring(0, i);
                    }

                    // add non-empty lines only
                    line = line.trim();
                    if (line.length() > 0)
                    {
                        jvmArgs.add(line);
                    }
                }
            }
        }

        return jvmArgs;
    }
}
