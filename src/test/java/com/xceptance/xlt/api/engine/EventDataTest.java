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
package com.xceptance.xlt.api.engine;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.api.util.XltRandom;

/**
 * Test the implementation of {@link EventData}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class EventDataTest extends AbstractDataTest
{
    private EventData instance = null;

    /** Name of data record. */
    protected final static String NAME = "SoMeNA.Me";

    /** Creation time of data record. */
    protected final static long TIME = System.currentTimeMillis();

    /** Name of test case of data record. */
    protected final static String TESTCASENAME = "a.b.c-Test";

    /** Message of data record. */
    protected final static String MESSAGE = "the Message!";

    /** Type code of data records. */
    protected final static String TYPE_CODE = new EventData().getTypeCode();

    /** Common CSV representation (equal to {@link AbstractData#toCSV()}). */
    protected final static String COMMON_CSV = getCommonCSV();

    /**
     * Test fixture setup.
     * 
     * @throws Exception
     *             thrown when setup failed.
     */
    @Before
    public void intro()
    {
        instance = new EventData();
    }

    /**
     * Tests the implementation of {@link EventData#fromCSV(String)}.
     * <p>
     * Passed CSV string misses the values for the <code>testCaseName</code> and <code>message</code> fields.
     * </p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void csvMissesTestCaseNameAndMessage()
    {
        instance.fromCSV(COMMON_CSV);
    }

    /**
     * Tests the implementation of {@link EventData#fromCSV(String)}.
     * <p>
     * Passed CSV string misses the value for the <code>message</code> field.
     * </p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void cvsMissesMessage()
    {
        instance.fromCSV(StringUtils.join(new Object[]
            {
                COMMON_CSV, TESTCASENAME
            }, Data.DELIMITER));
    }

    /**
     * Tests the implementation of {@link EventData#fromCSV(String)}.
     * <p>
     * Passed CSV string misses the value for the <code>testCaseName</code> field.
     * </p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void csvMissesTestCaseName()
    {
        instance.fromCSV(StringUtils.join(new Object[]
            {
                COMMON_CSV, MESSAGE
            }, Data.DELIMITER));
    }

    /**
     * Tests the implementation of {@link EventData#fromCSV(String)}.
     * <p>
     * The value of the field <code>testCaseName</code> contains the CSV delimiter.
     * </p>
     */
    @Test
    public void testCaseNameInCSVContainsCSVDelimiter()
    {
        // modify test case name so that it contains the delimiter
        final String testCaseName = TESTCASENAME.replace(TESTCASENAME.charAt(XltRandom.nextInt(TESTCASENAME.length())), Data.DELIMITER);

        instance.fromCSV(StringUtils.join(new Object[]
            {
                COMMON_CSV, testCaseName, MESSAGE
            }, Data.DELIMITER));
        Assert.assertNotSame(testCaseName, (instance).getTestCaseName());
    }

    /**
     * Tests the implementation of {@link EventData#fromCSV(String)}.
     * <p>
     * The value of the field <code>message</code> contains the CSV delimiter.
     * </p>
     */
    @Test
    public void messageInCSVContainsCSVDelimiter()
    {
        // modify message so that it contains the delimiter
        final String message = MESSAGE.replace(MESSAGE.charAt(XltRandom.nextInt(MESSAGE.length())), Data.DELIMITER);

        instance.fromCSV(StringUtils.join(new Object[]
            {
                COMMON_CSV, TESTCASENAME, message
            }, Data.DELIMITER));

        Assert.assertNotSame(message, (instance).getMessage());
    }

    @Test
    public void testConstructorWithName()
    {
        final EventData ev = new EventData("Huhu");
        Assert.assertEquals("Wrong name, ", "Huhu", ev.getName());
    }

    /**
     * Tests the implementation of {@link EventData#fromCSV(String)}.
     */
    @Test
    public void testFromCSV()
    {
        instance.fromCSV(StringUtils.join(new Object[]
            {
                COMMON_CSV, TESTCASENAME, MESSAGE
            }, Data.DELIMITER));

        Assert.assertEquals(NAME, instance.getName());
        Assert.assertEquals(TIME, instance.getTime());
        Assert.assertEquals(TYPE_CODE, instance.getTypeCode());
        Assert.assertEquals(TESTCASENAME, (instance).getTestCaseName());
    }

    /**
     * Tests the implementation of {@link EventData#toCSV()}.
     */
    @Test
    public void testToCSV()
    {
        final String csvLine = StringUtils.join(new Object[]
            {
                COMMON_CSV, TESTCASENAME, MESSAGE
            }, Data.DELIMITER);

        instance.setName(NAME);
        instance.setTime(TIME);

        (instance).setTestCaseName(TESTCASENAME);
        (instance).setMessage(MESSAGE);

        Assert.assertEquals(csvLine, instance.toCSV());
    }

    /**
     * Tests the implementation of {@link EventData#toCSV()} by setting the field <code>testCaseName</code> to a string
     * value that contains the CSV delimiter.
     */
    @Test
    public void testToCSV_TestCaseNameContainsCSVDelimiter()
    {
        instance.setName(NAME);
        instance.setTime(TIME);

        final String testCaseName = TESTCASENAME.replace(TESTCASENAME.charAt(XltRandom.nextInt(TESTCASENAME.length())), Data.DELIMITER);

        (instance).setTestCaseName(testCaseName);
        (instance).setMessage(MESSAGE);

        instance.fromCSV(instance.toCSV());

        Assert.assertEquals(testCaseName, instance.getTestCaseName());
    }

    /**
     * Tests the implementation of {@link EventData#toCSV()} by setting the field <code>message</code> to a string value
     * that contains the CSV delimiter.
     */
    @Test
    public void testToCSV_MessageContainsCSVDelimiter()
    {
        instance.setName(NAME);
        instance.setTime(TIME);
        (instance).setTestCaseName(TESTCASENAME);

        final String message = MESSAGE.replace(MESSAGE.charAt(XltRandom.nextInt(MESSAGE.length())), Data.DELIMITER);
        (instance).setMessage(message);

        final String csvOut = instance.toCSV();
        instance.fromCSV(csvOut);

        Assert.assertEquals(message, instance.getMessage());
    }

    /**
     * Tests the implementation of {@link EventData#fromCSV(String)} by passing a CSV string whose value for the field
     * <code>testCaseName</code> contains the CSV delimiter.
     */
    @Test
    public void testFromCSV_TestCaseNameContainsCSVDelimiter()
    {
        instance.setName(NAME);
        instance.setTime(TIME);

        final String testCaseName = TESTCASENAME.replace(TESTCASENAME.charAt(XltRandom.nextInt(TESTCASENAME.length())), Data.DELIMITER);

        (instance).setTestCaseName(testCaseName);
        (instance).setMessage(MESSAGE);

        instance.fromCSV(instance.toCSV());

        Assert.assertEquals(testCaseName, (instance).getTestCaseName());
        Assert.assertEquals(MESSAGE, (instance).getMessage());

    }

    /**
     * Tests the implementation of {@link EventData#fromCSV(String)} by passing a CSV string whose value for the field
     * <code>message</code> contains the CSV delimiter.
     */
    @Test
    public void testFromCSV_MessageContainsCSVDelimiter()
    {
        instance.setName(NAME);
        instance.setTime(TIME);
        (instance).setTestCaseName(TESTCASENAME);

        final String message = MESSAGE.replace(MESSAGE.charAt(XltRandom.nextInt(MESSAGE.length())), Data.DELIMITER);
        (instance).setMessage(message);

        instance.fromCSV(instance.toCSV());

        Assert.assertEquals(TESTCASENAME, (instance).getTestCaseName());
        Assert.assertEquals(message, (instance).getMessage());

    }

    /**
     * Returns the common CSV representation.
     * 
     * @return Common CSV representation.
     */
    private static String getCommonCSV()
    {
        final AbstractData stat = new AbstractData(TYPE_CODE)
        {
        };

        stat.setName(NAME);
        stat.setTime(TIME);

        return stat.toCSV();
    }
}
