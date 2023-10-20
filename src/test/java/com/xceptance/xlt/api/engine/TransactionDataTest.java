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
package com.xceptance.xlt.api.engine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.common.lang.ThrowableUtils;
import com.xceptance.common.util.CsvUtils;
import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.api.util.SimpleArrayList;
import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * Test the implementation of {@link TransactionData}.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class TransactionDataTest extends TimerDataTest
{
    /**
     * TransactionData test instance.
     */
    private TransactionData instance = null;

    /**
     * Throwable object used to manipulate the stackTrace field of TransactionData instances.
     */
    protected Throwable throwable = null;

    /**
     * Message of throwable 'throwable'.
     */
    private static final String TEST_FAILURE_MSG = "ATTENTION, Test failure!";

    private final String commonCSV = getCommonCSV();

    private final String name = "TTest";

    private final String failedActionName = "FailedAction";

    private final String testUserNumber = "123";

    private final String directoryName = "1234567890";

    private final String agentName = "ac1";

    private final String dumpDirectoryPath = StringUtils.joinWith("/", agentName, name, testUserNumber, "output", directoryName);

    private final String directoryHint = " (user: 'foo-" + testUserNumber + "', output: '" + directoryName + "')";

    private String stackTraceWithDirectoryHintEncoded;

    private String stackTraceWithoutDirectoryHint;

    private String stackTraceWithoutDirectoryHintEncoded;

    private static final int TIME = 100;
    private static final int RUNTIME = 42;
    private static final boolean FAILED = false;

    /**
     * Test fixture setup.
     *
     * @throws Exception
     *             thrown when setup failed.
     */
    @Before
    public void setupTransactionStatisticsInstance()
    {
        // create new test instance
        instance = new TransactionData();

        // create us a throwable without any fanciness aka not the real thing
        try
        {
            Assert.fail("ATTENTION, Test failure!");
        }
        catch (Throwable t)
        {
            throwable = t;
        }

        // validate throwable object (maybe some kind of paranoia)
        Assert.assertNotNull(throwable);

        // get stack traces without directory hint
        stackTraceWithoutDirectoryHint = getStackTrace(throwable).replace("\r", "");
        stackTraceWithoutDirectoryHintEncoded = stackTraceWithoutDirectoryHint.replace('\n', '\\');

        // set the directory hint
        ThrowableUtils.setMessage(throwable, throwable.getMessage() + directoryHint);

        // get stack trace with directory hint
        stackTraceWithDirectoryHintEncoded = getStackTrace(throwable).replace("\r", "").replace('\n', '\\');
    }

    /**
     * Returns a string representation of the stack trace hold by the given throwable object.
     *
     * @param throwable
     *            Throwable object whose stack trace should be used for string generation.
     * @return String representation of the given throwable object.
     */
    public static String getStackTrace(final Throwable throwable)
    {
        ParameterCheckUtils.isNotNull(throwable, "throwable");
        final StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));

        return sw.toString();
    }

    /**
     * Tests the implementation of {@link TransactionData#setFailureStackTrace(Throwable)}.
     */
    @Test
    public void testSetFailureStackTrace_Throwable()
    {
        // set stack trace null
        instance.setFailureStackTrace((Throwable) null);
        Assert.assertNull(instance.getFailureStackTrace());

        // set stack trace using the stored throwable object
        instance.setFailureStackTrace(throwable);

        // get failure message and validate it
        final String failureMsg = instance.getFailureMessage();
        Assert.assertNotNull(failureMsg);
        Assert.assertTrue(failureMsg.contains(TEST_FAILURE_MSG));

        // get stack trace and validate it
        final String stackTrace = instance.getFailureStackTrace();
        Assert.assertNotNull(stackTrace);
        Assert.assertTrue(stackTrace.contains(TEST_FAILURE_MSG));
        Assert.assertTrue(stackTrace.endsWith("..."));
    }

    /**
     * Tests the implementation of {@link TransactionData#remainingValuesFromCSV(String)} using a CSV line that was created before XLT
     * 4.13.2.
     */
    @Test
    public void testFromCSV_before_XLT_4_13_2()
    {
        final List<String> elements = new ArrayList<String>();
        elements.addAll(CsvUtils.decodeToList(commonCSV));
        elements.add(stackTraceWithDirectoryHintEncoded);
        elements.add(failedActionName);

        // construct CSV representation
        final String csvLine = CsvUtils.encode(elements).toString();

        // read in CSV representation and parse it
        var list = new SimpleArrayList<XltCharBuffer>(10);
        instance.baseValuesFromCSV(list, XltCharBuffer.valueOf(csvLine));
        instance.remainingValuesFromCSV(list);
        instance.setAgentName(agentName);

        // validate
        Assert.assertEquals(stackTraceWithoutDirectoryHint, instance.getFailureStackTrace());
        Assert.assertEquals(failedActionName, instance.getFailedActionName());
        Assert.assertEquals(testUserNumber, instance.getTestUserNumber());
        Assert.assertEquals(directoryName, instance.getDirectoryName());
        Assert.assertEquals(dumpDirectoryPath, instance.getDumpDirectoryPath());
    }

    /**
     * Tests the implementation of {@link TransactionData#remainingValuesFromCSV(String)} using a CSV line that was created with XLT
     * 4.13.2.
     */
    @Test
    public void testFromCSV_XLT_4_13_2()
    {
        final List<String> elements = new ArrayList<String>();
        elements.addAll(CsvUtils.decodeToList(commonCSV));
        elements.add(stackTraceWithoutDirectoryHintEncoded);
        elements.add(failedActionName);
        elements.add(testUserNumber);
        elements.add(directoryName);

        // construct CSV representation
        final String csvLine = CsvUtils.encode(elements).toString();

        // read in CSV representation and parse it
        var list = new SimpleArrayList<XltCharBuffer>(10);
        instance.baseValuesFromCSV(list,  XltCharBuffer.valueOf(csvLine));
        instance.remainingValuesFromCSV(list);
        instance.setAgentName(agentName);

        // validate
        Assert.assertEquals(stackTraceWithoutDirectoryHint, instance.getFailureStackTrace());
        Assert.assertEquals(failedActionName, instance.getFailedActionName());
        Assert.assertEquals(testUserNumber, instance.getTestUserNumber());
        Assert.assertEquals(directoryName, instance.getDirectoryName());
        Assert.assertEquals(dumpDirectoryPath, instance.getDumpDirectoryPath());
    }

    /**
     * Tests the implementation of {@link TransactionData#toCSV()}.
     */
    @Test
    public void testToCSV()
    {
        final List<String> elements = new ArrayList<String>();
        elements.addAll(CsvUtils.decodeToList(commonCSV));
        elements.add(stackTraceWithoutDirectoryHintEncoded);
        elements.add(failedActionName);
        elements.add(testUserNumber);
        elements.add(directoryName);

        // construct CSV representation
        final String csvLine = CsvUtils.encode(elements).toString();

        // set data record fields
        instance.setName(name);
        instance.setTime(TIME);
        instance.setRunTime(RUNTIME);
        instance.setFailed(FAILED);
        instance.setFailureStackTrace(stackTraceWithoutDirectoryHint);
        instance.setFailedActionName(failedActionName);
        instance.setTestUserNumber(testUserNumber);
        instance.setDirectoryName(directoryName);

        // compare CVS output
        Assert.assertEquals(csvLine, instance.toCSV().toString());
    }

    /**
     * Tests the implementation of {@link TransactionData#toCSV()}.
     */
    @Test
    public void testToCSVStackTraceIsNull()
    {
        // stacktrace of data record
        final String stackTrace = null;

        final List<String> elements = new ArrayList<String>();
        elements.addAll(CsvUtils.decodeToList(commonCSV));
        elements.add("");               // stack trace
        elements.add("");               // failed action
        elements.add(testUserNumber);   // user index
        elements.add(directoryName);    // directory name

        // construct CSV representation
        String csvLine = CsvUtils.encode(elements).toString();

        // set data record fields
        instance.setName(name);
        instance.setTime(TIME);
        instance.setRunTime(RUNTIME);
        instance.setFailed(FAILED);
        instance.setFailureStackTrace(stackTrace);
        instance.setTestUserNumber(testUserNumber);
        instance.setDirectoryName(directoryName);

        // compare CVS output
        Assert.assertEquals(csvLine, instance.toCSV().toString());
    }

    /**
     * Tests the implementation of {@link TransactionData#getFailureMessage()}.
     */
    @Test
    public void testGetFailureMessage()
    {
        // test, if the failure stack trace is null
        instance.setFailureStackTrace((String) null);
        try
        {
            instance.getFailureMessage();
            Assert.fail("A null pointer exception should be thrown!");
        }
        catch (final NullPointerException npe)
        {
        }
        instance.setFailureStackTrace((Throwable) null);
        try
        {
            instance.getFailureMessage();
            Assert.fail("A null pointer exception should be thrown!");
        }
        catch (final NullPointerException npe)
        {
        }
        // test, if the plain message is an empty string
        instance.setFailureStackTrace("");
        Assert.assertEquals("", instance.getFailureMessage());
        // test, if the plain message is not an empty string
        instance.setFailureStackTrace("ClassPrefix: failure text.");
        Assert.assertEquals("failure text.", instance.getFailureMessage());
        // test, if the class prefix is not empty, but the plain message
        instance.setFailureStackTrace("ClassPrefix");
        Assert.assertEquals("ClassPrefix", instance.getFailureMessage());
    }

    /**
     * Returns the common CSV string.
     *
     * @return common CSV string
     */
    private String getCommonCSV()
    {
        final TimerData stat = new TimerData(new TransactionData().getTypeCode())
        {
        };
        stat.setName(name);
        stat.setTime(TIME);
        stat.setRunTime(RUNTIME);
        stat.setFailed(FAILED);

        return stat.toCSV().toString();
    }
}
