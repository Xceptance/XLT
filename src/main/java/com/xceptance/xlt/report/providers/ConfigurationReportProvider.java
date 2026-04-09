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
package com.xceptance.xlt.report.providers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.xceptance.common.util.ProductInformation;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.mastercontroller.TestCaseLoadProfileConfiguration;
import com.xceptance.xlt.mastercontroller.TestLoadProfileConfiguration;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.util.ReportUtils;
import com.xceptance.xlt.util.PropertiesConfigurationException;
import com.xceptance.xlt.util.PropertiesIOException;
import com.xceptance.xlt.util.PropertyFileNotFoundException;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 * Report provider generating a report fragment about the configuration used for the test run
 */
public class ConfigurationReportProvider extends AbstractReportProvider
{

    private static final String LOADTEST_PROP = XltConstants.XLT_PACKAGE_PATH + ".loadtests";

    private static final String MASK_PROPERTIES_PROP = XltConstants.XLT_PACKAGE_PATH + ".reportgenerator.maskPropertiesRegex";

    private static final String MASK_PROPERTIES_REGEX_DEFAULT = "(?i)password";

    private static final String MARKDOWN_PREFIX = "::markdown::";

    /** Flexmark parser with tables and autolink extensions. */
    private static final Parser MARKDOWN_PARSER;

    /** Flexmark HTML renderer. */
    private static final HtmlRenderer MARKDOWN_RENDERER;

    static
    {
        // we want to support tables and autolinks
        final MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS,
                    Arrays.asList(TablesExtension.create(), AutolinkExtension.create(), StrikethroughExtension.create()));

        MARKDOWN_PARSER = Parser.builder(options).build();
        MARKDOWN_RENDERER = HtmlRenderer.builder(options).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final ConfigurationReport report = new ConfigurationReport();

        final File reportDirectory = getConfiguration().getReportDirectory();
        final File configDir = new File(reportDirectory, XltConstants.CONFIG_DIR_NAME);

        final XltPropertiesImpl props;
        try
        {

            final FileSystemManager fsMgr = VFS.getManager();
            props = new XltPropertiesImpl(fsMgr.resolveFile(reportDirectory.getAbsolutePath()),
                                          fsMgr.resolveFile(configDir.getAbsolutePath()), false, true);

        }
        catch (PropertyFileNotFoundException | PropertiesIOException | PropertiesConfigurationException | FileSystemException e)
        {
            System.err.println();

            return report;
        }

        final File jvmArgsFile = new File(configDir, XltConstants.JVM_PARAMETER_FILENAME);

        // add the masked plain properties
        report.properties.putAll(mask(props.getProperties()));

        // add relevant report generator settings
        report.reportGeneratorConfiguration = new ReportGeneratorConfigurationReport((ReportGeneratorConfiguration) getConfiguration());

        // add product information for later output
        report.version = ProductInformation.getProductInformation();

        // get test comment specified via property
        final TreeMap<String, String> sortedLoadtestProps = new TreeMap<>();
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
            report.comments.add(processComment(entry.getValue()));
        }

        // add project name
        final String projectName = props.getProperty(XltConstants.PROJECT_NAME_PROPERTY);
        report.projectName = StringUtils.isNotBlank(projectName) ? projectName.trim() : null;

        // add the load profile
        try
        {
            for (final TestCaseLoadProfileConfiguration tclpc : new TestLoadProfileConfiguration(props).getLoadTestConfiguration())
            {
                report.loadProfile.add(new LoadProfileConfigurationReport(tclpc));
            }
            calculatePercentages(report.loadProfile);
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

        report.chartHeight = getConfiguration().getChartHeight();
        report.chartWidth = getConfiguration().getChartWidth();

        return report;
    }

    private void calculatePercentages(final List<LoadProfileConfigurationReport> loadProfile)
    {
        // iterate through report.loadProfile --> calculate totals of (max) users and (max) arrivalrate
        int arrivalRateTotal = 0;
        int userCountTotal = 0;
        for (final LoadProfileConfigurationReport loadProfileConfig : loadProfile)
        {
            arrivalRateTotal += getMaxFromLoadFunction(loadProfileConfig.arrivalRate);
            userCountTotal += getMaxFromLoadFunction(loadProfileConfig.numberOfUsers);

        }
        // iterate through report.loadProfile again and add percentages calculated from totals
        for (final LoadProfileConfigurationReport loadProfileConfig : loadProfile)
        {
            loadProfileConfig.arrivalRatePercentage = (loadProfileConfig.arrivalRate == null ? null
                                                                                             : ReportUtils.calculatePercentage(getMaxFromLoadFunction(loadProfileConfig.arrivalRate),
                                                                                                                               arrivalRateTotal));
            loadProfileConfig.numberOfUsersPercentage = (ReportUtils.calculatePercentage(getMaxFromLoadFunction(loadProfileConfig.numberOfUsers),
                                                                                         userCountTotal));
        }
        return;
    }

    private int getMaxFromLoadFunction(final int[][] loadFunction)
    {
        if (loadFunction == null)
        {
            return 0;
        }
        // see LoadFunctionXStreamConverter
        int maximum = 0;
        for (final int[] array : loadFunction)
        {
            final int users = array[1];
            if (users > maximum)
            {
                maximum = users;
            }
        }
        return maximum;
    }

    private Map<? extends Object, ? extends Object> mask(final Properties properties)
    {
        final String MASK_PROPERTIES_REGEX = getConfiguration().getProperties().getProperty(MASK_PROPERTIES_PROP,
                                                                                            MASK_PROPERTIES_REGEX_DEFAULT);

        final boolean isMaskSet = StringUtils.isNoneBlank(MASK_PROPERTIES_REGEX);
        for (final Entry<Object, Object> entry : properties.entrySet())
        {
            final String propName = (String) entry.getKey();
            if (propName.startsWith(XltConstants.SECRET_PREFIX) || isMaskSet && RegExUtils.isMatching(propName, MASK_PROPERTIES_REGEX))
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
     * Processes a comment string. If the comment starts with the marker {@value #MARKDOWN_PREFIX} (case-insensitive),
     * the remainder is treated as Markdown and converted to HTML, wrapped in a div with class "markdown". Otherwise the
     * raw string is returned unchanged.
     *
     * @param comment
     *            the comment string to process
     * @return the processed comment
     */
    static String processComment(final String comment)
    {
        if (comment == null)
        {
            return null;
        }

        final String cleanedString = comment.strip();

        if (cleanedString.length() >= MARKDOWN_PREFIX.length() &&
            cleanedString.substring(0, MARKDOWN_PREFIX.length()).equalsIgnoreCase(MARKDOWN_PREFIX))
        {
            final String markdown = cleanedString.substring(MARKDOWN_PREFIX.length());
            final String html = MARKDOWN_RENDERER.render(MARKDOWN_PARSER.parse(markdown));
            return "<div class=\"markdown\">" + html + "</div>";
        }

        return comment;
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
        final ArrayList<String> jvmArgs = new ArrayList<>();

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
