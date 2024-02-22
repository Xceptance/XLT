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
package com.xceptance.xlt.report.external;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.xceptance.xlt.api.report.ReportCreator;
import com.xceptance.xlt.api.report.external.AbstractLineParser;
import com.xceptance.xlt.report.external.config.ChartConfig;
import com.xceptance.xlt.report.external.config.Config;
import com.xceptance.xlt.report.external.config.DataFileConfig;
import com.xceptance.xlt.report.external.config.ExternalDataConfigProvider;
import com.xceptance.xlt.report.external.config.Property;
import com.xceptance.xlt.report.external.config.SeriesConfig;
import com.xceptance.xlt.report.external.config.TableConfig;
import com.xceptance.xlt.report.external.config.ValueConfig;
import com.xceptance.xlt.report.external.converter.ConverterConfiguration;
import com.xceptance.xlt.report.external.converter.CustomReportProvider;

/**
 * Generator for external data report.
 * 
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class ExternalReportGenerator
{
    private static final Logger LOG = LoggerFactory.getLogger(ExternalReportGenerator.class);

    private static final String ERROR_MSG = "Failed to process external data file '%s'\n-> Configured parser class '%s' cannot be instantiated.";

    protected final List<ReportCreator> reportProviders = new ArrayList<ReportCreator>();

    protected long minTime;

    protected long maxTime;

    protected String inputDir;

    protected File chartDir;

    /** <KEY> is file name */
    protected List<ConverterConfiguration> resources = new ArrayList<ConverterConfiguration>();

    private boolean generateCharts;

    /**
     * Initializes the report generator.
     * 
     * @param minTime
     *            chart start time
     * @param maxTime
     *            chart end time
     * @param chartDir
     *            chart directory
     */
    public void init(final long minTime, final long maxTime, final String inputDir, final File chartDir, final boolean generateCharts)
    {
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.inputDir = inputDir;
        this.chartDir = chartDir;
        this.generateCharts = generateCharts;
    }

    /**
     * Reads and parses the configured resource(s).
     * 
     * @throws IllegalArgumentException
     *             - deprecated?
     * @throws IOException
     *             if reading from file fails
     * @throws JAXBException
     * @throws SAXException 
     */
    public void parse() throws IOException, JAXBException, SAXException
    {
        final Config config = ExternalDataConfigProvider.getConfig(inputDir);

        // parse every source
        for (final DataFileConfig dataFile : config.getFiles())
        {
            String fileName = dataFile.getFileName();
            if (StringUtils.isNotEmpty(fileName))
            {
                fileName = normalizeFileName(fileName);

                final ConverterConfiguration converterConfig = configureConverterConfig(dataFile);

                final CustomReportProvider reportProvider = new CustomReportProvider();
                reportProvider.setConfiguration(converterConfig);

                final String parserClassName = dataFile.getParserClassName();
                final AbstractLineParser abstractParser = initializeParserImplementation(fileName, parserClassName);

                if (abstractParser == null)
                {
                    // previously an error occurred and was logged. So there is no more to do and we switch to the next
                    // iteration.
                    continue;
                }
                reportProviders.add(reportProvider);

                abstractParser.setValueNames(getValueNames(dataFile));
                abstractParser.setProperties(getProperties(dataFile));

                final Reader reader = new Reader(fileName, dataFile.getEncoding(), abstractParser);
                reader.setConverter(reportProvider);
                reader.readData();
            }
            else
            {
                final String msg = "External data file not specified in configuration.";
                LOG.error(msg);
                System.out.println(msg);
            }
        }
    }

    /**
     * @param fileName
     * @return
     * @throws IOException
     */
    private String normalizeFileName(String fileName) throws IOException
    {
        // normalize file name
        if (!new File(fileName).isAbsolute() && !fileName.startsWith("/"))
        {
            // resolve the file relative to the result directory.
            fileName = new File(inputDir, fileName).getCanonicalFile().toString();
        }
        // otherwise file has already absolute path, thus nothing is to do.
        return fileName;
    }

    /**
     * @param dataFile
     * @return
     * @throws IOException
     */
    private ConverterConfiguration configureConverterConfig(final DataFileConfig dataFile) throws IOException
    {
        final ConverterConfiguration converterConfig = new ConverterConfiguration();
        converterConfig.setChartStartTime(minTime);
        converterConfig.setChartEndTime(maxTime);
        converterConfig.setChartsDir(chartDir);
        converterConfig.setDataFile(dataFile);
        if (!generateCharts)
        {
            converterConfig.disableChartGeneration();
        }
        return converterConfig;
    }

    private Properties getProperties(final DataFileConfig dataFile)
    {
        // pass properties to parser
        final Properties properties = new Properties();
        final List<Property> fileProperties = dataFile.getProperties();
        if (fileProperties != null)
        {
            for (final Property property : fileProperties)
            {
                final String key = property.getKey();
                final String value = property.getValue();
                if (key != null && value != null)
                {
                    properties.put(key, value);
                }
            }
        }
        return properties;
    }

    /**
     * @param dataFile
     * @return
     */
    private Set<String> getValueNames(final DataFileConfig dataFile)
    {
        // collect headlines of interest
        final Set<String> valueNames = new HashSet<String>();

        for (final TableConfig table : dataFile.getTables())
        {
            List<ValueConfig> valueConfigs = table.getRows();
            if (valueConfigs.isEmpty())
            {
                valueConfigs = table.getColumns();
            }

            for (final ValueConfig row : valueConfigs)
            {
                valueNames.add(row.getValueName());
            }
        }

        for (final ChartConfig chart : dataFile.getCharts())
        {
            for (final SeriesConfig series : chart.getSeriesCollection())
            {
                valueNames.add(series.getValueName());
            }
        }

        return valueNames;
    }

    /**
     * @param fileName
     * @param parserClassName
     * @return
     */
    private AbstractLineParser initializeParserImplementation(final String fileName, final String parserClassName)
    {
        // instantiate reader
        ClassLoader classLoader = null;
        Class<?> parserClass = null;
        AbstractLineParser abstractParser = null;

        try
        {
            classLoader = ClassLoader.getSystemClassLoader();
            // get constructor
            parserClass = classLoader.loadClass(parserClassName);
            abstractParser = (AbstractLineParser) parserClass.getConstructor().newInstance();
        }
        catch (final ClassNotFoundException cnfe)
        {
            final String msg = String.format(ERROR_MSG, fileName, parserClassName);
            LOG.error(msg, cnfe);
            System.out.println(msg);
        }
        catch (final NoSuchMethodException nsme)
        {
            final String msg = String.format(ERROR_MSG + " Did you provide a public no arg constructor?", fileName, parserClassName);
            LOG.error(msg, nsme);
            System.out.println(msg);
        }
        catch (final SecurityException se)
        {
            final String msg = String.format(ERROR_MSG, fileName, parserClassName);
            LOG.error(msg, se);
            System.out.println(msg);
        }
        catch (final Throwable t) // The constructor called above by reflection may cause an arbitrary error / exception
        {
            final String msg = String.format(ERROR_MSG, fileName, parserClassName);
            LOG.error(msg, t);
            System.out.println(msg);
        }
        return abstractParser;
    }

    /**
     * get all converters
     * 
     * @return all converters
     */
    public List<ReportCreator> getReportCreators()
    {
        return reportProviders;
    }
}
