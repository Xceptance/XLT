/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.junit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.xceptance.xlt.api.data.DataSetProvider;
import com.xceptance.xlt.api.data.DataSetProviderException;
import com.xceptance.xlt.api.engine.DataSetIndex;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.engine.data.DataSetProviderFactory;
import com.xceptance.xlt.engine.util.XltTestRunner;

/**
 * A JUnit {@link Runner} implementation for arbitrary test cases. This runner runs a certain test case possibly
 * multiple times if there is a {@link DataSetProvider} with more than one data set (data-driven test). To use this
 * runner, annotate your JUnit test class with this class using {@link RunWith}.
 */
public abstract class AbstractTestCaseRunner extends XltTestRunner
{
    /**
     * A specialization of {@link FrameworkMethod}, which replaces the default method name with the provided name and
     * the test data set used.
     */
    private static class ParameterizedFrameworkMethod extends FrameworkMethod
    {
        /**
         * The test data set to use.
         */
        private final Map<String, String> dataSet;

        /**
         * The new method name.
         */
        private final String name;

        /**
         * Constructor.
         *
         * @param method
         *            the test method
         * @param testMethodName
         *            the name to show for the test method
         * @param index
         *            the index of the test run
         * @param dataSet
         *            the test data set
         */
        public ParameterizedFrameworkMethod(final Method method, final String testMethodName, final int index,
                                            final Map<String, String> dataSet)
        {
            super(method);

            this.dataSet = dataSet;

            if (index == -1)
            {
                name = testMethodName;
            }
            else
            {
                name = String.format("%s[%d] - %s", testMethodName, index, dataSet);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName()
        {
            return name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object obj)
        {
            return this == obj;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode()
        {
            return System.identityHashCode(this);
        }
    }

    /**
     * An empty data set.
     */
    private static final Map<String, String> EMPTY_DATA_SET = Collections.emptyMap();

    /**
     * The current directory.
     */
    protected static final File CURRENT_DIR = new File(".");

    /**
     * The data sets directory as specified in the XLT configuration. Maybe <code>null</code> if not configured.
     */
    protected static final File DATA_SETS_DIR;

    static
    {
        final String dataSetFileDirectoryName = XltProperties.getInstance().getProperty("com.xceptance.xlt.data.dataSets.dir", "");
        if (dataSetFileDirectoryName.length() > 0)
        {
            final File dir = new File(dataSetFileDirectoryName);

            DATA_SETS_DIR = dir.isDirectory() ? dir : null;
        }
        else
        {
            DATA_SETS_DIR = null;
        }
    }

    /**
     * The JUnit children of this runner.
     */
    private final List<FrameworkMethod> methods = new ArrayList<FrameworkMethod>();

    /**
     * Constructor.
     *
     * @param testCaseClass
     *            the test case class
     * @param testCaseName
     *            the name of the test
     * @param defaultTestMethodName
     *            the name of the test method (maybe <code>null</code>)
     * @param dataSetFileDirs
     *            the list of directories to search for data set files
     */
    protected AbstractTestCaseRunner(final Class<?> testCaseClass, final String testCaseName, final String defaultTestMethodName,
                                     final List<File> dataSetFileDirs)
        throws Throwable
    {
        super(testCaseClass);

        // get the short (package-less) test case name
        final String shortTestCaseName = StringUtils.contains(testCaseName, '.') ? StringUtils.substringAfterLast(testCaseName, ".")
                                                                                 : testCaseName;

        // get the data sets
        final List<Map<String, String>> dataSets = getDataSets(testCaseClass, testCaseName, shortTestCaseName, dataSetFileDirs);

        // create the set of framework methods to run
        for (final FrameworkMethod frameworkMethod : getTestClass().getAnnotatedMethods(Test.class))
        {
            // get the test method to run
            final Method testMethod = frameworkMethod.getMethod();

            // check whether to override the test method name
            final String testMethodName = (defaultTestMethodName == null) ? testMethod.getName() : defaultTestMethodName;

            // create the JUnit children
            addParameterizedFrameworkMethods(testCaseClass, testMethod, testMethodName, dataSets);
        }
    }

    /**
     * @param testCaseClass
     * @param testMethod
     * @param testMethodName
     * @param dataSets
     */
    private void addParameterizedFrameworkMethods(final Class<?> testCaseClass, final Method testMethod, final String testMethodName,
                                                  final List<Map<String, String>> dataSets)
    {
        if (dataSets == null || dataSets.isEmpty())
        {
            // run the method once with an empty data set
            methods.add(new ParameterizedFrameworkMethod(testMethod, testMethodName, -1, EMPTY_DATA_SET));
        }
        else
        {
            int dSIndex = -1;
            // Get annotations of test class.
            final Annotation[] annotations = testCaseClass.getAnnotations();
            for (final Annotation annotation : annotations)
            {
                // look for '@DataSetIndex'
                if (annotation instanceof DataSetIndex)
                {
                    dSIndex = ((DataSetIndex) annotation).value();
                }
            }

            // check whether we are in load test mode
            if (Session.getCurrent().isLoadTest() && dSIndex <= -1)
            {
                if (XltProperties.getInstance().getProperty("com.xceptance.xlt.data.dataSets.loadtest.pickRandomDataSet", false))
                {
                    dSIndex = XltRandom.nextInt(dataSets.size());
                }
                else
                {
                    dSIndex = 0;
                }
            }

            if (dSIndex >= dataSets.size())
            {
                throw new IllegalArgumentException("Selected data set can not be found. Please check your test case setup.");
            }

            // run the method once for each data set
            int i = 0;
            for (final Map<String, String> dataSet : dataSets)
            {
                if (dSIndex <= -1 || dSIndex == i)
                {
                    methods.add(new ParameterizedFrameworkMethod(testMethod, testMethodName, i, dataSet));
                }
                i++;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Description getDescription()
    {
        final Description description = Description.createSuiteDescription(getTestClass().getJavaClass());

        for (final FrameworkMethod frameworkMethod : getChildren())
        {
            description.addChild(Description.createTestDescription(getTestClass().getJavaClass(), frameworkMethod.getName()));
        }

        return description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<FrameworkMethod> getChildren()
    {
        return methods;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Statement methodInvoker(final FrameworkMethod method, final Object test)
    {
        // prepare the test instance before executing it
        // TODO: check whether there is a better place to do this
        setUpTest(method, test);

        // the real job is done here
        return super.methodInvoker(method, test);
    }

    /**
     * Sets the test instance up.
     *
     * @param method
     *            the method
     * @param test
     *            the test instance
     */
    protected void setUpTest(final FrameworkMethod method, final Object test)
    {
        if (test instanceof AbstractTestCase)
        {
            final AbstractTestCase abstractTestCase = (AbstractTestCase) test;
            final ParameterizedFrameworkMethod frameworkMethod = (ParameterizedFrameworkMethod) method;

            // only set the data set at the test instance if none has been set yet (in test case constructor, #2605)
            if (abstractTestCase.getTestDataSet() == null)
            {
                abstractTestCase.setTestDataSet(frameworkMethod.dataSet);
            }
        }
    }

    /**
     * Returns the test data sets associated with the given test case class.
     *
     * @param testClass
     *            the test case class
     * @param fullTestCaseName
     *            the full test case name
     * @param shortTestCaseName
     *            the short test case name
     * @param dataSetFileDirs
     *            the list of directories to search for data set files
     * @return the data sets, or <code>null</code> if there are no associated test data sets
     * @throws DataSetProviderException
     *             if the responsible data set provider cannot be created
     * @throws FileNotFoundException
     *             if an explicitly configured data set file cannot be found
     * @throws IOException
     */
    private List<Map<String, String>> getDataSets(final Class<?> testClass, final String fullTestCaseName, final String shortTestCaseName,
                                                  final List<File> dataSetFileDirs)
        throws DataSetProviderException, FileNotFoundException, IOException
    {
        // check whether data-driven tests are enabled
        final boolean enabled = XltProperties.getInstance().getProperty("com.xceptance.xlt.data.dataDrivenTests.enabled", true);
        if (!enabled)
        {
            return null;
        }

        // check whether a specific file has been configured
        final String specificFileNameKey1 = testClass.getName() + ".dataSetsFile";
        String specificFileName = XltProperties.getInstance().getProperty(specificFileNameKey1, "");
        if (specificFileName.length() == 0)
        {
            final String specificFileNameKey2 = fullTestCaseName + ".dataSetsFile";
            specificFileName = XltProperties.getInstance().getProperty(specificFileNameKey2, "");
        }

        if (specificFileName.length() != 0)
        {
            // there is a specific file
            File batchDataFile = new File(specificFileName);
            if (batchDataFile.isAbsolute())
            {
                // absolute -> try it as is
                return readDataSets(batchDataFile);
            }
            else
            {
                // relative -> search for it in the usual directories
                for (final File directory : dataSetFileDirs)
                {
                    batchDataFile = new File(directory, specificFileName);
                    if (batchDataFile.isFile())
                    {
                        return readDataSets(batchDataFile);
                    }
                }

                throw new FileNotFoundException("Specific test data set file name configured, but file could not be found: " +
                                                specificFileName);
            }
        }
        else
        {
            // no specific file -> try the usual suspects
            final Set<String> fileNames = new LinkedHashSet<String>();

            final String dottedName = fullTestCaseName;
            final String slashedName = dottedName.replace('.', '/');

            final DataSetProviderFactory dataSetProviderFactory = DataSetProviderFactory.getInstance();
            for (final String fileExtension : dataSetProviderFactory.getRegisteredFileExtensions())
            {
                final String suffix = "_datasets." + fileExtension;

                fileNames.add(slashedName + suffix);
                fileNames.add(dottedName + suffix);
            }

            // look for such a file in the usual directories
            return getDataSets(dataSetFileDirs, fileNames, testClass);
        }
    }

    /**
     * Looks for a data set file and, if found, returns its the data sets. Tries all the specified file names in all the
     * passed directories and finally in the class path.
     *
     * @param dataSetFileDirs
     *            the directories to search
     * @param fileNames
     *            the file names to try
     * @param testClass
     *            the test case class as the class path context
     * @return the data sets, or <code>null</code> if no data sets file was found
     * @throws IOException
     *             if an I/O error occurred
     * @throws DataSetProviderException
     *             if there is no responsible data set provider
     */
    private List<Map<String, String>> getDataSets(final List<File> dataSetFileDirs, final Set<String> fileNames, final Class<?> testClass)
        throws IOException
    {
        // look for a data set file in the passed directories
        for (final File directory : dataSetFileDirs)
        {
            for (final String fileName : fileNames)
            {
                final File batchDataFile = new File(directory, fileName);
                if (batchDataFile.isFile())
                {
                    return readDataSets(batchDataFile);
                }
            }
        }

        // look for a data set file in the class path
        for (final String fileName : fileNames)
        {
            final InputStream input = testClass.getResourceAsStream("/" + fileName);
            if (input != null)
            {
                OutputStream output = null;
                File batchDataFile = null;

                try
                {
                    // copy the stream to a temporary file
                    final String extension = "." + FilenameUtils.getExtension(fileName);
                    batchDataFile = File.createTempFile("dataSets_", extension);
                    output = new FileOutputStream(batchDataFile);

                    IOUtils.copy(input, output);
                    output.flush();

                    // read the data sets from the temporary file
                    return readDataSets(batchDataFile);
                }
                finally
                {
                    // clean up
                    IOUtils.closeQuietly(input);
                    IOUtils.closeQuietly(output);
                    FileUtils.deleteQuietly(batchDataFile);
                }
            }
        }

        return null;
    }

    /**
     * Returns the test data sets contained in the given test data file. The data set provider used to read the file is
     * determined from the data file's extension.
     *
     * @param dataSetsFile
     *            the test data set file
     * @return the data sets
     * @throws DataSetProviderException
     *             if there is no responsible data set provider
     */
    private List<Map<String, String>> readDataSets(final File dataSetsFile) throws DataSetProviderException
    {
        if (XltLogger.runTimeLogger.isDebugEnabled())
        {
            XltLogger.runTimeLogger.debug("Test data set file used: " + dataSetsFile.getAbsolutePath());
        }

        final DataSetProviderFactory dataSetProviderFactory = DataSetProviderFactory.getInstance();
        final String fileExtension = FilenameUtils.getExtension(dataSetsFile.getName());
        final DataSetProvider dataSetProvider = dataSetProviderFactory.createDataSetProvider(fileExtension);

        return dataSetProvider.getAllDataSets(dataSetsFile);
    }
}
