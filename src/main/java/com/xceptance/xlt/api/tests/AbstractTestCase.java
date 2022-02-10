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
package com.xceptance.xlt.api.tests;

import java.net.MalformedURLException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import com.xceptance.xlt.api.data.DataSetProvider;
import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.junit.JavaTestCaseRunner;
import com.xceptance.xlt.engine.util.TimerUtils;
import com.xceptance.xlt.engine.util.URLInfo;
import com.xceptance.xlt.engine.util.UrlUtils;

/**
 * AbstractTestCase is the base class for all load test cases. The purpose of this class is to perform some internal
 * housekeeping tasks once the test case has finished. This includes:
 * <ul>
 * <li>logging of collected results to disk</li>
 * <li>flushing any caches</li>
 * </ul>
 * If your test cases do not inherit from this class, the system does not break, but you will loose some of the
 * features, e.g. logging the results collected during the test case.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
@RunWith(JavaTestCaseRunner.class)
public abstract class AbstractTestCase
{
    /**
     * The start time of the test.
     */
    private long startTime;

    /**
     * The test data set to use when running the test.
     */
    private Map<String, String> testDataSet;

    /**
     * Name of the test.
     */
    private String testName;

    /**
     * Executes base setup of the test case.
     */
    @Before
    public final void __setup()
    {
        // set unique identifier for this session
        final String id = String.valueOf(GlobalClock.getInstance().getTime());
        Session.getCurrent().setID(id);

        // override the default user name with a more specific one
        final SessionImpl session = (SessionImpl) Session.getCurrent();
        if (!session.isLoadTest())
        {
            session.setUserName(getSimpleName());
            Thread.currentThread().setName(session.getUserID());
        }

        // set test case class name
        session.setTestCaseClassName(getTestName());

        // set test instance
        session.setTestInstance(this);

        // remember the seed of XltRandom for the current iteration
        session.getValueLog().put(XltConstants.RANDOM_INIT_VALUE_PROPERTY, XltRandom.getSeed());
    }

    /**
     * Executes base tear-down of the test case.
     */
    @After
    public final void __tearDown()
    {
        // reinitialize XltRandom for the upcoming iteration
        XltRandom.reseed();
    }

    /**
     * Returns the value for the given key as configured in the test suite configuration. The process of looking up a
     * property uses multiple fall-backs. When resolving the value for the key "password", for example, the following
     * effective keys are tried, in this order:
     * <ol>
     * <li>the test user name plus simple key, e.g. "TAuthor.password"</li>
     * <li>the test class name plus simple key, e.g. "com.xceptance.xlt.samples.tests.TAuthor.password"</li>
     * <li>the simple key, e.g. "password"</li>
     * </ol>
     * This multi-step hierarchy allows for test-user-specific or test-case-specific overrides of certain settings,
     * while falling back to the globally defined values if such specific settings are absent.
     *
     * @param key
     *            the simple property key
     * @return the property value, or <code>null</code> if not found
     */

    public String getProperty(final String key)
    {
        final String effectiveKey = getEffectiveKey(key);

        return XltProperties.getInstance().getProperty(effectiveKey);
    }

    /**
     * Returns the value for the given key as configured in the test suite configuration. See
     * {@link #getProperty(String)} for a description of the look-up logic. This method returns the passed default value
     * if the property value could not be found.
     *
     * @param key
     *            the property key
     * @param defaultValue
     *            the default value
     * @return the property value as a boolean
     */
    public boolean getProperty(final String key, final boolean defaultValue)
    {
        final String effectiveKey = getEffectiveKey(key);

        return XltProperties.getInstance().getProperty(effectiveKey, defaultValue);
    }

    /**
     * Returns the value for the given key as configured in the test suite configuration. See
     * {@link #getProperty(String)} for a description of the look-up logic. This method returns the passed default value
     * if the property value could not be found.
     *
     * @param key
     *            the property key
     * @param defaultValue
     *            the default value
     * @return the property value as an int
     */
    public int getProperty(final String key, final int defaultValue)
    {
        final String effectiveKey = getEffectiveKey(key);

        return XltProperties.getInstance().getProperty(effectiveKey, defaultValue);
    }

    /**
     * Returns the value for the given key as configured in the test suite configuration. See
     * {@link #getProperty(String)} for a description of the look-up logic. This method returns the passed default value
     * if the property value could not be found.
     *
     * @param key
     *            the property key
     * @param defaultValue
     *            the default value
     * @return the property value
     */
    public String getProperty(final String key, final String defaultValue)
    {
        final String effectiveKey = getEffectiveKey(key);

        return XltProperties.getInstance().getProperty(effectiveKey, defaultValue);
    }

    /**
     * Returns the test data set to use when running the test script. Call this method from your test code to get access
     * to the test data set for this test run.
     *
     * @return the test data
     */
    public Map<String, String> getTestDataSet()
    {
        return testDataSet;
    }

    /**
     * Returns the actual start URL to be used for the test based on the given URL and the local configuration. This
     * method reconfigures the given URL to contain "user-local" settings instead. This is especially useful if a test
     * has been created/recorded on one system, but should later be run on another system without changing the code.
     * <p>
     * Typically, a URL looks like this:
     * 
     * <pre>
     * &lt;protocol&gt;://[&lt;userInfo&gt;@]&lt;host&gt;[:&lt;port&gt;][&lt;path&gt;][?&lt;query&gt;][#&lt;ref&gt;]
     * </pre>
     * 
     * Each part of the URL can be overwritten individually by local configuration. Typically, this makes sense for
     * protocol, userInfo, host, and port only. In order to override the values from the given URL, one has to provide
     * appropriate settings in the XLT configuration. Using the following example configuration, host and port of the
     * original URL will be replaced:
     *
     * <pre>
     * #startUrl.protocol = http
     * #startUrl.userInfo =
     * startUrl.host = myhost
     * startUrl.port = 81
     * #startUrl.path =
     * #startUrl.query =
     * #startUrl.ref =
     * </pre>
     *
     * All other parts of the URL are left as they are.
     *
     * @param urlString
     *            the original URL string
     * @return the reconfigured URL
     * @throws Exception
     *             this is just declared to enable classes overwriting this method to throw an appropriate exception,
     *             the basic implementation only throws a {@link MalformedURLException} if the argument URL string is
     *             invalid, or if the resulting URL is invalid due to illegal values in the configuration
     */
    public String reconfigureStartUrl(final String urlString) throws Exception
    {

        // get the actual values from the configuration if available
        final String protocol = getProperty("startUrl.protocol", "");
        final String userInfo = getProperty("startUrl.userInfo", "");
        final String host = getProperty("startUrl.host", "");
        final int port = getProperty("startUrl.port", -1);

        final String path = getProperty("startUrl.path", "");
        final String query = getProperty("startUrl.query", "");
        final String fragment = getProperty("startUrl.fragment", "");

        final URLInfo urlInfo = URLInfo.builder().proto(protocol).userInfo(userInfo).host(host).port(port).path(path).query(query)
                                       .fragment(fragment).build();
        return UrlUtils.rewriteUrl(urlString, urlInfo).toExternalForm();
    }

    /**
     * Sets the test data set to use when running the test case. This method is called by the XLT test case runner to
     * provide the test data set for this test run, retrieved from a {@link DataSetProvider} instance.
     *
     * @param testDataSet
     *            the test data
     */
    public void setTestDataSet(final Map<String, String> testDataSet)
    {
        this.testDataSet = testDataSet;
    }

    /**
     * Executes basic setup of the test case. Mainly reports the beginning of the execution to the log. Can be
     * overwritten to get rid of the message.
     */
    @Before
    public void setUp()
    {
        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            XltLogger.runTimeLogger.info(String.format("####### Test '%s' started", getTestName()));
        }

        startTime = TimerUtils.getTime();
    }

    /**
     * Executes the basic tear down for the test case. Mainly reports the end of the execution to the log. Can be
     * overwritten to get rid of the message.
     */
    @After
    public void tearDown()
    {
        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            XltLogger.runTimeLogger.info(String.format("####### Test '%s' finished after %d ms", getTestName(),
                                                       TimerUtils.getTime() - startTime));
        }
    }

    /**
     * Returns the effective key to be used for property lookup via one of the getProperty(...) methods.
     * <p>
     * When looking up a key, "password" for example, the following effective keys are tried, in this order:
     * <ol>
     * <li>the test user name plus simple key, e.g. "TAuthor.password"</li>
     * <li>the test class name plus simple key, e.g. "com.xceptance.xlt.samples.tests.TAuthor.password"</li>
     * <li>the simple key, e.g. "password"</li>
     * </ol>
     *
     * @param bareKey
     *            the bare property key, i.e. without any prefixes
     * @return the first key that produces a result
     */
    protected String getEffectiveKey(final String bareKey)
    {
        final String effectiveKey;
        final XltProperties xltProperties = XltProperties.getInstance();

        // 1. use the current user name as prefix
        final String userNameQualifiedKey = Session.getCurrent().getUserName() + "." + bareKey;
        if (xltProperties.containsKey(userNameQualifiedKey))
        {
            effectiveKey = userNameQualifiedKey;
        }
        else
        {
            // 2. use the current class name as prefix
            final String classNameQualifiedKey = getTestName() + "." + bareKey;
            if (xltProperties.containsKey(classNameQualifiedKey))
            {
                effectiveKey = classNameQualifiedKey;
            }
            else
            {
                // 3. use the bare key
                effectiveKey = bareKey;
            }
        }

        return effectiveKey;
    }

    /**
     * Returns the test name.
     *
     * @return test name
     */
    protected String getSimpleName()
    {
        final String name = getTestName();

        final int idx = Math.max(name.lastIndexOf('.'), name.lastIndexOf('$'));
        if (idx != -1)
        {
            return name.substring(idx + 1);
        }

        return name;
    }

    /**
     * Returns the test name. If no custom name was set this is the class name.
     *
     * @return test name
     */
    protected String getTestName()
    {
        if (testName == null)
        {
            testName = getClass().getName();
        }

        return testName;
    }

    /**
     * Sets the test name.
     *
     * @deprecated will be removed in XLT 4.6. Test case name will be set via constructor.
     */
    @Deprecated
    protected void setTestName()
    {
        // test name is already set
    }

    /**
     * <p>
     * Sets the test name.
     * </p>
     * <p>
     * It's highly recommended to call this method in the constructor body only.
     * </p>
     * 
     * @param testName
     *            test name
     */
    protected void setTestName(final String testName)
    {
        if (StringUtils.isNotBlank(testName))
        {
            this.testName = testName;
        }
        else
        {
            this.testName = getClass().getName();
            XltLogger.runTimeLogger.warn("A test name should not be blank, empty, or null. Used \"" + testName + "\" instead!");
        }
    }
}
