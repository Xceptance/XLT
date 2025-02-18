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
package com.xceptance.xlt.mastercontroller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import com.xceptance.common.lang.ParseNumbers;
import com.xceptance.common.lang.ThrowableUtils;
import com.xceptance.common.util.AbstractConfiguration;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 * Load test profile configuration.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class TestLoadProfileConfiguration extends AbstractConfiguration
{
    /**
     * property suffix to set the arrival rate of a test
     */
    private static final String PROP_SUFFIX_ARRIVAL_RATE = ".arrivalRate";

    /**
     * property suffix to set the class of a test
     */
    public static final String PROP_SUFFIX_CLASS = ".class";

    /**
     * property suffix to set the initial delay of a test
     */
    public static final String PROP_SUFFIX_INITIAL_DELAY = ".initialDelay";

    /**
     * property suffix to set the number of iterations of a test
     */
    private static final String PROP_SUFFIX_ITERATIONS = ".iterations";

    /**
     * property suffix to set the load factor of a test
     */
    private static final String PROP_SUFFIX_LOAD_FACTOR = ".loadFactor";

    /**
     * property suffix to set the ramp-up period of a test
     */
    public static final String PROP_SUFFIX_RAMP_UP_PERIOD = ".rampUpPeriod";

    /**
     * property suffix to set the ramp-up steady period of a test
     */
    private static final String PROP_SUFFIX_RAMP_UP_STEADY_PERIOD = ".rampUpSteadyPeriod";

    /**
     * property suffix to set the shut-down period of a test
     */
    private static final String PROP_SUFFIX_SHUT_DOWN_PERIOD = ".shutdownPeriod";

    /**
     * property suffix to set the warm-up period of a test
     */
    public static final String PROP_SUFFIX_WARM_UP_PERIOD = ".warmUpPeriod";

    /**
     * property suffix to set the measurement period of a test
     */
    private static final String PROP_SUFFIX_MEASUREMENT_PERIOD = ".measurementPeriod";

    /**
     * property suffix to set the number users running the test
     */
    private static final String PROP_SUFFIX_USERS = ".users";

    /**
     * property suffix to set the number of users to use for load increment during ramp-up
     */
    private static final String PROP_SUFFIX_RAMP_UP_STEP_SIZE = ".rampUpStepSize";

    /**
     * Property suffix for the number of users to start with when ramping up the load.
     */
    @Deprecated
    private static final String PROP_SUFFIX_RAMP_UP_INITIAL_USERS = ".rampUpInitialUsers";

    /**
     * Property suffix for the load parameter value (either user count or arrival rate) to start with when ramping up
     * the load.
     */
    private static final String PROP_SUFFIX_RAMP_UP_INITIAL_VALUE = ".rampUpInitialValue";

    /**
     * property suffix to set the duration of the load test
     */
    @Deprecated
    private static final String PROP_SUFFIX_DURATION = ".duration";

    private static final String PROP_SUFFIX_ISCLIENTPERFTEST = ".clientPerformanceTest";

    /**
     * property name to get the base value for the action think time
     */
    private static final String PROP_ACTION_THINK_TIME = XltConstants.XLT_PACKAGE_PATH + ".thinktime.action";

    /**
     * property name to get the deviation value for the action think time
     */
    private static final String PROP_ACTION_THINK_TIME_DEVIATION = XltConstants.XLT_PACKAGE_PATH + ".thinktime.action.deviation";

    /**
     * property name to get the list of active test cases
     */
    private static final String PROP_ACTIVE_LOAD_TESTS = XltConstants.XLT_PACKAGE_PATH + ".loadtests";

    /**
     * property prefix to set the test cases of the load test
     */
    public static final String PROP_PREFIX_LOAD_TESTS = PROP_ACTIVE_LOAD_TESTS + ".";

    /**
     * property suffix to set the default value of all tests
     */
    public static final String PROP_LOAD_TEST_DEFAULTS = PROP_PREFIX_LOAD_TESTS + "default";

    /**
     * test configurations
     */
    private final Map<String, TestCaseLoadProfileConfiguration> loadTestConfigs;

    /**
     * set of active test cases
     */
    private Set<String> activeTestCases;

    /**
     * The XLT properties reads initially. We need that to be able to look up by test case name and user and avoid the
     * dependencies to Session/SessionImpl.
     */
    private final XltPropertiesImpl xltProperties;

    /**
     * Helper method used to retrieve all the properties that are read in by XltProperties using the given testsuite's
     * home and configuration directory.
     *
     * @param homeDir
     *            the testsuite's home directory
     * @param configDir
     *            the testsuite's configuration directory
     * @return properties as read in by XltProperties
     */
    public static XltPropertiesImpl readProperties(final File homeDir, final File configDir)
    {
        try
        {
            final FileSystemManager fsMgr = VFS.getManager();

            final FileObject homeDirFO = fsMgr.resolveFile(homeDir.getAbsolutePath());
            final FileObject configDirFO = fsMgr.resolveFile(configDir.getAbsolutePath());

            return new XltPropertiesImpl(homeDirFO, configDirFO, false, false);
        }
        catch (final FileSystemException fse)
        {
            throw new IllegalArgumentException("Failed to resolve configuration: " + configDir);
        }
    }

    /**
     * Creates an empty load test profile configuration. For testing purposes only.
     */
    public TestLoadProfileConfiguration()
    {
        this.loadTestConfigs = new TreeMap<>();
        this.activeTestCases = new LinkedHashSet<>();
        this.xltProperties = new XltPropertiesImpl();
    }

    /**
     * Creates a new load test profile configuration.
     *
     * @param properties
     *            from external to avoid loading conflicts
     */
    public TestLoadProfileConfiguration(final XltPropertiesImpl properties)
    {
        this.xltProperties = properties;
        addProperties(xltProperties.getProperties());

        this.loadTestConfigs = readLoadTestCaseConfiguration();
    }

    /**
     * Creates a new load test profile configuration.
     *
     * @param source
     * @param testCaseName
     */
    protected TestLoadProfileConfiguration(final TestLoadProfileConfiguration source, final String testCaseName)
    {
        this.xltProperties = new XltPropertiesImpl();
        this.loadTestConfigs = new HashMap<>();

        final TestCaseLoadProfileConfiguration config = source.loadTestConfigs.get(testCaseName);
        if (config != null)
        {
            loadTestConfigs.put(testCaseName, config);
        }
    }

    /**
     * Adds a test case configuration to this load profile. For testing purposes only.
     *
     * @param testCaseLoadProfileConfiguration
     *            the test case configuration
     */
    public void addTestCaseLoadProfileConfiguration(final TestCaseLoadProfileConfiguration testCaseLoadProfileConfiguration)
    {
        loadTestConfigs.put(testCaseLoadProfileConfiguration.getUserName(), testCaseLoadProfileConfiguration);
    }

    /**
     * Returns a new load test profile configuration pre-configured for the given test case.
     *
     * @param testCaseName
     *            name of the test case
     * @return load test profile configuration pre-configured for the given test case
     */
    public TestLoadProfileConfiguration getTestLoadProfileConfiguration(final String testCaseName)
    {
        return new TestLoadProfileConfiguration(this, testCaseName);
    }

    /**
     * Returns the list of active test cases.
     *
     * @return active test cases
     */
    public Set<String> getActiveTestCaseNames()
    {
        return activeTestCases;
    }

    /**
     * Returns all test configurations.
     *
     * @return test configurations
     */
    public List<TestCaseLoadProfileConfiguration> getLoadTestConfiguration()
    {
        return new ArrayList<TestCaseLoadProfileConfiguration>(loadTestConfigs.values());
    }

    /**
     * Returns the total time (in seconds) it takes for all active test scenarios to finish their ramp-up. This value is
     * relative to the moment when the first scenario would begin to run. Initial delays are taken into consideration.
     *
     * @return the total ramp-up time [s]
     */
    public long getTotalRampUpPeriod()
    {
        long maxRampUpOffset = 0L;
        long smallestInitialDelay = Long.MAX_VALUE;

        for (final TestCaseLoadProfileConfiguration loadProfile : getLoadTestConfiguration())
        {
            // initial delay + ramp-up is offset
            final int initialDelay = loadProfile.getInitialDelay();
            final int rampUpPeriod = loadProfile.getRampUpPeriod();
            if (rampUpPeriod > 0)
            {
                maxRampUpOffset = Math.max(maxRampUpOffset, initialDelay + rampUpPeriod);
            }
            smallestInitialDelay = Math.min(smallestInitialDelay, initialDelay);
        }

        return Math.max(0, maxRampUpOffset - smallestInitialDelay);
    }

    /**
     * Reads, parses and returns the load test configurations from the configured location.
     *
     * @return test configurations
     * @throws ClassNotFoundException
     *             thrown when the configured test case class could not be found
     */
    private Map<String, TestCaseLoadProfileConfiguration> readLoadTestCaseConfiguration()
    {
        final Map<String, TestCaseLoadProfileConfiguration> configs = new TreeMap<String, TestCaseLoadProfileConfiguration>();

        final DefaultTestCaseLoadProfileConfiguration defaultTestCaseConfig = getDefaultTestCaseLoadProfileConfiguration();

        // determine the active test cases

        final String activeTestCasesPropertyValue = getStringProperty(PROP_ACTIVE_LOAD_TESTS, "");
        final String[] activeTestCaseNames = StringUtils.split(activeTestCasesPropertyValue, " ,;");

        // store list of active test case names in the same order as configured
        activeTestCases = new LinkedHashSet<String>();

        // configure active test cases
        configure(activeTestCaseNames, configs, defaultTestCaseConfig);

        return configs;
    }

    private DefaultTestCaseLoadProfileConfiguration getDefaultTestCaseLoadProfileConfiguration()
    {
        checkForOutdatedProperties(PROP_LOAD_TEST_DEFAULTS);

        /*
         * read the default test case settings
         */
        final String defaultClassName = getStringProperty(PROP_LOAD_TEST_DEFAULTS + PROP_SUFFIX_CLASS, null);
        final int defaultWarmUpPeriod = getTimePeriodProperty(PROP_LOAD_TEST_DEFAULTS + PROP_SUFFIX_WARM_UP_PERIOD, 0);
        final int defaultMeasurementPeriod = getTimePeriodProperty(PROP_LOAD_TEST_DEFAULTS + PROP_SUFFIX_MEASUREMENT_PERIOD, 0);
        final int defaultRampUpPeriod = getTimePeriodProperty(PROP_LOAD_TEST_DEFAULTS + PROP_SUFFIX_RAMP_UP_PERIOD, -1);
        final int defaultRampUpSteadyPeriod = getTimePeriodProperty(PROP_LOAD_TEST_DEFAULTS + PROP_SUFFIX_RAMP_UP_STEADY_PERIOD, -1);
        final int defaultShutdownPeriod = getTimePeriodProperty(PROP_LOAD_TEST_DEFAULTS + PROP_SUFFIX_SHUT_DOWN_PERIOD, 0);
        final int defaultIterations = getIntProperty(PROP_LOAD_TEST_DEFAULTS + PROP_SUFFIX_ITERATIONS, 0);
        final int defaultInitialDelay = getTimePeriodProperty(PROP_LOAD_TEST_DEFAULTS + PROP_SUFFIX_INITIAL_DELAY, 0);
        final int defaultRampUpStepSize = getIntProperty(PROP_LOAD_TEST_DEFAULTS + PROP_SUFFIX_RAMP_UP_STEP_SIZE, -1);
        final int defaultRampUpInitialValue = getIntProperty(PROP_LOAD_TEST_DEFAULTS + PROP_SUFFIX_RAMP_UP_INITIAL_VALUE, -1);
        final int[][] defaultLoadFactor = getDoubleLoadFunction(PROP_LOAD_TEST_DEFAULTS + PROP_SUFFIX_LOAD_FACTOR, null);
        final int[][] defaultUsers = getLoadFunction(PROP_LOAD_TEST_DEFAULTS + PROP_SUFFIX_USERS, null);
        final int[][] defaultArrivalRate = getLoadFunction(PROP_LOAD_TEST_DEFAULTS + PROP_SUFFIX_ARRIVAL_RATE, null);
        final int defaultActionThinkTime = getIntProperty(PROP_ACTION_THINK_TIME, 0);
        final int defaultActionThinkTimeDeviation = getIntProperty(PROP_ACTION_THINK_TIME_DEVIATION, 0);

        final DefaultTestCaseLoadProfileConfiguration defaultConfig = new DefaultTestCaseLoadProfileConfiguration();
        defaultConfig.setTestCaseClassName(defaultClassName);
        defaultConfig.setWarmUpPeriod(defaultWarmUpPeriod);
        defaultConfig.setMeasurementPeriod(defaultMeasurementPeriod);
        defaultConfig.setRampUpPeriod(defaultRampUpPeriod);
        defaultConfig.setRampUpSteadyPeriod(defaultRampUpSteadyPeriod);
        defaultConfig.setShutdownPeriod(defaultShutdownPeriod);
        defaultConfig.setNumberOfIterations(defaultIterations);
        defaultConfig.setInitialDelay(defaultInitialDelay);
        defaultConfig.setRampUpStepSize(defaultRampUpStepSize);
        defaultConfig.setRampUpInitialValue(defaultRampUpInitialValue);
        defaultConfig.setLoadFactor(defaultLoadFactor);
        defaultConfig.setNumberOfUsers(defaultUsers);
        defaultConfig.setArrivalRate(defaultArrivalRate);
        defaultConfig.setActionThinkTime(defaultActionThinkTime);
        defaultConfig.setActionThinkTimeDeviation(defaultActionThinkTimeDeviation);

        return defaultConfig;
    }

    private void configure(final String[] testCaseNames, final Map<String, TestCaseLoadProfileConfiguration> configurations,
                           final DefaultTestCaseLoadProfileConfiguration defaultConfiguration)
    {
        /*
         * read the test case specific values
         */
        for (final String testCaseName : testCaseNames)
        {
            activeTestCases.add(testCaseName);
            final String propertyName = PROP_PREFIX_LOAD_TESTS + testCaseName;

            checkForOutdatedProperties(propertyName);

            final String className = getStringProperty(propertyName + PROP_SUFFIX_CLASS, defaultConfiguration.getTestCaseClassName());
            final int iterations = getIntProperty(propertyName + PROP_SUFFIX_ITERATIONS, defaultConfiguration.getNumberOfIterations());
            final int warmUpPeriod = getTimePeriodProperty(propertyName + PROP_SUFFIX_WARM_UP_PERIOD,
                                                           defaultConfiguration.getWarmUpPeriod());
            final int shutdownPeriod = getTimePeriodProperty(propertyName + PROP_SUFFIX_SHUT_DOWN_PERIOD,
                                                             defaultConfiguration.getShutdownPeriod());
            final int measurementPeriod = getTimePeriodProperty(propertyName + PROP_SUFFIX_MEASUREMENT_PERIOD,
                                                                defaultConfiguration.getMeasurementPeriod());
            int rampUpPeriod = getTimePeriodProperty(propertyName + PROP_SUFFIX_RAMP_UP_PERIOD, defaultConfiguration.getRampUpPeriod());
            final int rampUpSteadyPeriod = getTimePeriodProperty(propertyName + PROP_SUFFIX_RAMP_UP_STEADY_PERIOD,
                                                                 defaultConfiguration.getRampUpSteadyPeriod());
            final int initialDelay = getTimePeriodProperty(propertyName + PROP_SUFFIX_INITIAL_DELAY,
                                                           defaultConfiguration.getInitialDelay());
            final int rampUpStepSize = getIntProperty(propertyName + PROP_SUFFIX_RAMP_UP_STEP_SIZE,
                                                      defaultConfiguration.getRampUpStepSize());
            final int rampUpInitialValue = getIntProperty(propertyName + PROP_SUFFIX_RAMP_UP_INITIAL_VALUE,
                                                          defaultConfiguration.getRampUpInitialValue());
            final int[][] loadFactor = getDoubleLoadFunction(propertyName + PROP_SUFFIX_LOAD_FACTOR, defaultConfiguration.getLoadFactor());
            int[][] users = getLoadFunction(propertyName + PROP_SUFFIX_USERS, defaultConfiguration.getNumberOfUsers());
            int[][] arrivalRate = getLoadFunction(propertyName + PROP_SUFFIX_ARRIVAL_RATE, defaultConfiguration.getArrivalRate());
            final boolean isCPTest = getBooleanProperty(propertyName + PROP_SUFFIX_ISCLIENTPERFTEST, false);

            final int actionThinkTime = xltProperties.getProperty(className, testCaseName, PROP_ACTION_THINK_TIME)
                                                     .flatMap(ParseNumbers::parseOptionalInt)
                                                     .orElse(defaultConfiguration.getActionThinkTime());
            final int actionThinkTimeDeviation = xltProperties.getProperty(className, testCaseName, PROP_ACTION_THINK_TIME_DEVIATION)
                                                              .flatMap(ParseNumbers::parseOptionalInt)
                                                              .orElse(defaultConfiguration.getActionThinkTimeDeviation());

            // check mandatory parameters
            if (className != null && className.isBlank())
            {
                throw new XltException("Test class specified for test case '" + testCaseName + "', but the value is empty.");
            }

            if (measurementPeriod == 0)
            {
                throw new XltException("No measurement period specified for test case '" + testCaseName + "'.");
            }

            if (users == null)
            {
                throw new XltException("Number of users not specified for test case '" + testCaseName + "'.");
            }

            // check for mutually exclusive parameters
            if (arrivalRate != null)
            {
                if (LoadFunctionUtils.isComplexLoadFunction(users))
                {
                    throw new XltException("Both a complex user function and an arrival rate are specified for test case '" + testCaseName +
                                           "', but they cannot be used together.");
                }

                if (iterations != 0)
                {
                    throw new XltException("Both number of iterations and arrival rate are specified for test case '" + testCaseName +
                                           "', but they cannot be used together.");
                }
            }

            if (loadFactor != null && LoadFunctionUtils.isComplexLoadFunction(loadFactor))
            {
                if (LoadFunctionUtils.isComplexLoadFunction(users))
                {
                    throw new XltException("Both a complex user function and a complex load factor function are specified for test case '" +
                                           testCaseName + "', but only one of them can be complex.");
                }

                if (arrivalRate != null && LoadFunctionUtils.isComplexLoadFunction(arrivalRate))
                {
                    throw new XltException("Both a complex arrival rate function and a complex load factor function are specified for test case '" +
                                           testCaseName + "', but only one of them can be complex.");
                }
            }

            // apply load factor function
            arrivalRate = LoadFunctionUtils.scaleLoadFunction(arrivalRate, loadFactor);
            users = LoadFunctionUtils.scaleLoadFunction(users, loadFactor);

            // set the load function for the test report
            int[][] complexLoadFunction = null;
            if (arrivalRate != null && LoadFunctionUtils.isComplexLoadFunction(arrivalRate))
            {
                complexLoadFunction = arrivalRate;
            }
            else if (LoadFunctionUtils.isComplexLoadFunction(users))
            {
                complexLoadFunction = users;
            }

            // handle ramp-up parameters
            if (complexLoadFunction != null)
            {
                // GH#457: Clear ramp-up period in the presence of already complex user/arrival rate load functions
                rampUpPeriod = -1;
            }
            else
            {
                // apply ramp-up parameters to either users or arrival rates
                if (arrivalRate == null)
                {
                    final int rampUpTargetValue = users[0][1];

                    users = LoadFunctionUtils.computeLoadFunction(rampUpInitialValue, rampUpTargetValue, rampUpPeriod, rampUpStepSize,
                                                                  rampUpSteadyPeriod);
                }
                else
                {
                    final int rampUpTargetValue = arrivalRate[0][1];

                    arrivalRate = LoadFunctionUtils.computeLoadFunction(rampUpInitialValue, rampUpTargetValue, rampUpPeriod, rampUpStepSize,
                                                                        rampUpSteadyPeriod);
                }
            }

            // now build the test case configuration
            final TestCaseLoadProfileConfiguration config = new TestCaseLoadProfileConfiguration();

            config.setTestCaseClassName(className);
            config.setArrivalRate(arrivalRate);
            config.setNumberOfUsers(users);
            config.setNumberOfIterations(iterations);
            config.setShutdownPeriod(shutdownPeriod);
            config.setWarmUpPeriod(warmUpPeriod);
            config.setMeasurementPeriod(measurementPeriod);
            config.setInitialDelay(initialDelay);
            config.setUserName(testCaseName);
            config.setComplexLoadFunction(complexLoadFunction);
            config.setRampUpPeriod(rampUpPeriod);
            config.setLoadFactor(loadFactor);
            config.setCPTest(isCPTest);
            config.setActionThinkTime(actionThinkTime);
            config.setActionThinkTimeDeviation(actionThinkTimeDeviation);

            configurations.put(testCaseName, config);

            // System.out.println(config);
        }
    }

    /**
     * Checks if any outdated parameters are used in the load profile configuration.
     *
     * @throws XltException
     *             if such a property was found
     */
    private void checkForOutdatedProperties(final String propertyPrefix) throws XltException
    {
        final String message = "Property '%s' is not supported any longer. Please remove it and use '%s' instead.";

        // "duration"
        String propertyName = propertyPrefix + PROP_SUFFIX_DURATION;
        if (getStringProperty(propertyName, null) != null)
        {
            throw new XltException(String.format(message, propertyName, propertyPrefix + PROP_SUFFIX_MEASUREMENT_PERIOD));
        }

        // "rampUpInitialUsers"
        propertyName = propertyPrefix + PROP_SUFFIX_RAMP_UP_INITIAL_USERS;
        if (getStringProperty(propertyName, null) != null)
        {
            throw new XltException(String.format(message, propertyName, propertyPrefix + PROP_SUFFIX_RAMP_UP_INITIAL_VALUE));
        }
    }

    /**
     * Parses a load function from the configuration. If the passed property does not exist, the default function is
     * returned. If the property does not specify a complex function, but a single value, a trivial load function is
     * returned for it.
     *
     * @param propertyName
     *            the name of the property to use
     * @param defaultValues
     *            the default user count function, may be <code>null</code>
     * @return the load function
     */
    private int[][] getLoadFunction(final String propertyName, final int[][] defaultValues)
    {
        // get the property value
        String propertyValue = null;

        try
        {
            propertyValue = getStringProperty(propertyName);
        }
        catch (final RuntimeException e)
        {
            // the property is not specified, use defaults
            return defaultValues;
        }

        // the property is defined, now parse it
        int[][] loadFunction = null;

        final AbstractLoadFunctionParser parser = new IntValueLoadFunctionParser();
        try
        {
            // first try to parse the property as a load function
            loadFunction = parser.parse(propertyValue);
        }
        catch (final Exception e)
        {
            try
            {
                // now try to parse the property as an integer value
                final int value = parser.parseValue(propertyValue);

                // build a pseudo function
                loadFunction = new int[][]
                    {
                        {
                            0, value
                        }
                    };
            }
            catch (final Exception e2)
            {
                throw new IllegalArgumentException(String.format("The value '%s' of property '%s' can neither be parsed as simple integer, nor as load function.",
                                                                 propertyValue, propertyName));
            }
        }

        // finally we have a function, now check it
        try
        {
            LoadFunctionUtils.checkLoadFunction(loadFunction);
        }
        catch (final IllegalArgumentException e)
        {
            ThrowableUtils.setMessage(e, String.format("The property '%s' does not specify a valid load function: %s", propertyName,
                                                       e.getMessage()));

            throw e;
        }

        // complete the function
        loadFunction = LoadFunctionUtils.completeLoadFunctionIfNecessary(loadFunction);

        return loadFunction;
    }

    /**
     * Parses a load function from the configuration. If the passed property does not exist, the default function is
     * returned. If the property does not specify a complex function, but a single value, a trivial load function is
     * returned for it.
     *
     * @param propertyName
     *            the name of the property to use
     * @param defaultValues
     *            the default user count function, may be <code>null</code>
     * @return the load function
     */
    private int[][] getDoubleLoadFunction(final String propertyName, final int[][] defaultValues)
    {
        // get the property value
        String propertyValue = null;

        try
        {
            propertyValue = getStringProperty(propertyName);
        }
        catch (final RuntimeException e)
        {
            // the property is not specified, use defaults
            return defaultValues;
        }

        // the property is defined, now parse it
        int[][] loadFunction = null;

        final AbstractLoadFunctionParser parser = new DoubleValueLoadFunctionParser();
        try
        {
            // first try to parse the property as a load function
            loadFunction = parser.parse(propertyValue);
        }
        catch (final Exception e)
        {
            try
            {
                final int value = parser.parseValue(propertyValue);

                // build a pseudo function
                loadFunction = new int[][]
                    {
                        {
                            0, value
                        }
                    };
            }
            catch (final Exception e2)
            {
                throw new IllegalArgumentException(String.format("The value '%s' of property '%s' can neither be parsed as simple double, nor as load function.",
                                                                 propertyValue, propertyName));
            }
        }

        // finally we have a function, now check it
        try
        {
            LoadFunctionUtils.checkLoadFunction(loadFunction);
        }
        catch (final IllegalArgumentException e)
        {
            ThrowableUtils.setMessage(e, String.format("The property '%s' does not specify a valid load function: %s", propertyName,
                                                       e.getMessage()));

            throw e;
        }

        // complete the function
        loadFunction = LoadFunctionUtils.completeLoadFunctionIfNecessary(loadFunction);

        return loadFunction;
    }

    /**
     * Set the test classes in the test case specific configurations.
     *
     * @param testCaseClassMappings
     *            the map of test case names and their associated test class names to set
     */
    public void setTestCaseClassMappings(final Map<String, String> testCaseClassMappings)
    {
        for (final String testCaseName : testCaseClassMappings.keySet())
        {
            this.loadTestConfigs.get(testCaseName).setTestCaseClassName(testCaseClassMappings.get(testCaseName));
        }
    }

    private static class DefaultTestCaseLoadProfileConfiguration extends TestCaseLoadProfileConfiguration
    {
        private int rampUpSteadyPeriod;

        private int rampUpInitialValue;

        private int rampUpStepSize;

        public void setRampUpSteadyPeriod(final int defaultRampUpSteadyPeriod)
        {
            rampUpSteadyPeriod = defaultRampUpSteadyPeriod;
        }

        public void setRampUpInitialValue(final int defaultRampUpInitialValue)
        {
            rampUpInitialValue = defaultRampUpInitialValue;
        }

        public void setRampUpStepSize(final int defaultRampUpStepSize)
        {
            rampUpStepSize = defaultRampUpStepSize;
        }

        public final int getRampUpSteadyPeriod()
        {
            return rampUpSteadyPeriod;
        }

        public final int getRampUpInitialValue()
        {
            return rampUpInitialValue;
        }

        public final int getRampUpStepSize()
        {
            return rampUpStepSize;
        }
    }
}
