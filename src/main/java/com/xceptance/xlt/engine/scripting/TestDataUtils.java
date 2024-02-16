/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.scripting;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.xceptance.common.util.CsvUtils;
import com.xceptance.xlt.engine.XltExecutionContext;

/**
 * Utility class for test data handling.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public final class TestDataUtils
{
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestDataUtils.class);

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private TestDataUtils()
    {
        // Empty
    }

    /**
     * Loads and returns the test data for the given script file.
     *
     * @param scriptFile
     *            the script file
     * @return test data for given script
     */
    public static Map<String, String> getTestData(final File scriptFile)
    {
        final String baseName = scriptFile.getName().replace(".xml", "");
        try
        {
            File dataFile = new File(scriptFile.getParentFile(), baseName + "_data.xml");
            if (dataFile.isFile())
            {
                try (final FileInputStream inputStream = new FileInputStream(dataFile))
                {
                    return parseXMLData(inputStream);
                }
            }

            dataFile = new File(scriptFile.getParentFile(), baseName + "_data.csv");
            if (dataFile.isFile())
            {
                try (final FileInputStream inputStream = new FileInputStream(dataFile))
                {
                    return parseCSVData(inputStream);
                }
            }

            dataFile = new File(scriptFile.getParentFile(), baseName + "_data.properties");
            if (dataFile.isFile())
            {
                try (final FileInputStream inputStream = new FileInputStream(dataFile))
                {
                    return parsePropertiesData(inputStream);
                }
            }
        }
        catch (final Exception e)
        {
            LOGGER.error("Failed to load test data for script '" + baseName + "'.", e);
        }

        return Collections.emptyMap();
    }

    /**
     * Loads and returns the test data for the given script.
     *
     * @param script
     *            the script
     * @return test data for the given script
     */
    public static Map<String, String> getTestData(final Script script)
    {
        return getTestData(script.getScriptFile());
    }

    /**
     * Loads and returns the test data for the given class.
     *
     * @param clazz
     *            the script class
     * @return test data for the given script class
     */
    public static Map<String, String> getTestData(final Class<?> clazz)
    {
        final String clazzName = clazz.getSimpleName();

        try
        {
            // (1) try to load it from XML
            InputStream is = clazz.getResourceAsStream(clazzName + "_data.xml");
            if (is != null)
            {
                return parseXMLData(is);
            }

            // (2) no XML file found, try CSV now
            is = clazz.getResourceAsStream(clazzName + "_data.csv");
            if (is != null)
            {
                return parseCSVData(is);
            }

            // (3) last but not least try to load it from a properties file
            is = clazz.getResourceAsStream(clazzName + "_data.properties");
            if (is != null)
            {
                return parsePropertiesData(is);
            }

        }
        catch (final Exception e)
        {
            LOGGER.error("Failed to parse test data for class '" + clazz.getCanonicalName() + "'", e);
        }

        return Collections.emptyMap();
    }

    /**
     * Parses and returns the XML data using the given input stream.
     *
     * @param is
     *            the input stream
     * @return parsed XML data
     * @throws Exception
     */
    private static Map<String, String> parseXMLData(final InputStream is) throws Exception
    {
        final DocumentBuilderFactory parserFactory = DocumentBuilderFactory.newInstance();
        parserFactory.setNamespaceAware(true);

        final XmlErrorHandler errorHandler = new XmlErrorHandler();

        final DocumentBuilder parser = parserFactory.newDocumentBuilder();
        parser.setErrorHandler(errorHandler);

        final Document document = parser.parse(is);

        if (errorHandler.errors > 0)
        {
            throw new SAXException(String.format("Parsing the XML data file produced %d error(s) and %d warning(s)", errorHandler.errors,
                                                 errorHandler.warnings));
        }

        if (errorHandler.warnings > 0)
        {
            if (LOGGER.isWarnEnabled())
            {
                LOGGER.warn(String.format("Parsing the XML data file produced %d warning(s)", errorHandler.warnings));
            }
        }

        // get the test data
        final Map<String, String> data = new HashMap<String, String>();

        final Element rootElement = document.getDocumentElement();

        final NodeList childNodes = rootElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++)
        {
            final Node childNode = childNodes.item(i);

            if (childNode instanceof Element)
            {
                final Element element = (Element) childNode;

                data.put(element.getTagName(), element.getTextContent());
            }
        }

        return data;
    }

    /**
     * Parses and returns the CSV data using the given input stream.
     *
     * @param is
     *            the input stream
     * @return parsed CSV data
     * @throws Exception
     */
    private static Map<String, String> parseCSVData(final InputStream is) throws Exception
    {
        final HashMap<String, String> testData = new HashMap<String, String>();
        try
        {
            final List<?> lines = IOUtils.readLines(is, "UTF-8");
            for (final Object o : lines)
            {
                var parts = CsvUtils.decodeToList((String) o);
                if (parts.size() == 2)
                {
                    testData.put(parts.get(0), parts.get(1));
                }
            }
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }

        return testData;
    }

    /**
     * Parses and returns the properties data using the given input stream.
     *
     * @param is
     *            the input stream
     * @return parsed CSV data
     * @throws Exception
     */
    private static Map<String, String> parsePropertiesData(final InputStream is) throws Exception
    {
        final HashMap<String, String> testData = new HashMap<String, String>();
        try
        {
            final Properties props = new Properties();
            props.load(new InputStreamReader(is, "UTF-8"));

            for (final Map.Entry<Object, Object> entry : props.entrySet())
            {
                testData.put((String) entry.getKey(), (String) entry.getValue());
            }
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }

        return testData;
    }

    /**
     * Returns the global test data as configured in <em>&lt;TESTSUITE_DIR&gt;/global_testdata.properties</em>.
     *
     * @return global test data
     */
    public static Map<String, String> getGlobalTestData()
    {
        final File testDataFile = new File(XltExecutionContext.getCurrent().getTestSuiteHomeDirAsFile(), "global_testdata.properties");
        try
        {
            if (testDataFile.isFile())
            {
                try (final FileInputStream inputStream = new FileInputStream(testDataFile))
                {
                    return parsePropertiesData(inputStream);
                }
            }
        }
        catch (final Exception e)
        {
            LOGGER.error("Failed to parse global test data properties file '" + testDataFile.getAbsolutePath() + "'");
        }
        return Collections.emptyMap();
    }

    /**
     * Loads and returns the package test data for the given script package.
     *
     * @param clazz
     *            the class object to use for resource lookup
     * @param baseDir
     *            the base directory to use for data file lookup
     * @param packageName
     *            the name of the script package
     * @return test data of given script package
     */
    public static Map<String, String> getPackageTestData(final Class<?> clazz, final String baseDir, final String packageName)
    {
        final String baseName = packageName.replace('.', '/') + "/package_testdata.";

        try
        {
            InputStream is = null;
            // use 'clazz' for resource lookup
            if (clazz != null)
            {
                final String base = baseDir + "/" + baseName;
                is = clazz.getResourceAsStream(base + "xml");
                if (is != null)
                {
                    return parseXMLData(is);
                }

                is = clazz.getResourceAsStream(base + "csv");
                if (is != null)
                {
                    return parseCSVData(is);
                }

                is = clazz.getResourceAsStream(base + "properties");
                if (is != null)
                {
                    return parsePropertiesData(is);
                }

            }
            // file lookup ('baseDir' is expected to be an absolute path)
            else
            {

                final File bd = new File(baseDir);
                File dataFile = new File(bd, baseName + "xml");
                if (dataFile.isFile())
                {
                    is = new FileInputStream(dataFile);
                    if (is != null)
                    {
                        return parseXMLData(is);
                    }
                }

                dataFile = new File(bd, baseName + "csv");
                if (dataFile.isFile())
                {
                    is = new FileInputStream(dataFile);
                    if (is != null)
                    {
                        return parseCSVData(is);
                    }
                }

                dataFile = new File(bd, baseName + "properties");
                if (dataFile.isFile())
                {
                    is = new FileInputStream(dataFile);
                    if (is != null)
                    {
                        return parsePropertiesData(is);
                    }
                }
            }
        }
        catch (final Exception e)
        {
            LOGGER.error("Failed to parse package test data for package '" + packageName + "'", e);
        }

        return Collections.emptyMap();

    }

    /**
     * An error handler for errors in XML files.
     */
    private static class XmlErrorHandler implements ErrorHandler
    {
        /**
         * The number of errors encountered.
         */
        int errors;

        /**
         * The number of warnings encountered.
         */
        int warnings;

        /**
         * {@inheritDoc}
         */
        @Override
        public void error(final SAXParseException exception) throws SAXException
        {
            LOGGER.error(report(exception));
            errors++;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void fatalError(final SAXParseException exception) throws SAXException
        {
            LOGGER.error(report(exception));
            errors++;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void warning(final SAXParseException exception) throws SAXException
        {
            if (LOGGER.isWarnEnabled())
            {
                LOGGER.warn(report(exception));
            }

            warnings++;
        }

        /**
         * Logs the given exception.
         *
         * @param exception
         *            the exception
         */
        private String report(final SAXParseException exception)
        {
            return String.format("### %d:%d - %s\n", exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
        }
    }

}
