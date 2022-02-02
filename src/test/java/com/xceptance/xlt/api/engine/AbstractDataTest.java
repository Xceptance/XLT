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
package com.xceptance.xlt.api.engine;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.common.util.CsvUtils;
import com.xceptance.xlt.AbstractXLTTestCase;
import com.xceptance.xlt.TestWrapper;

/**
 * Test the implementation of {@link AbstractData}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class AbstractDataTest extends AbstractXLTTestCase
{
    /**
     * Type code to use for creating new instances of class AbstractData.
     */
    private static final String TYPE_CODE = "TS";

    /**
     * AbstractData test instance.
     */
    private AbstractData instance = null;

    /**
     * Name of data record.
     */
    protected final String name = "John Doe";

    /**
     * Creation time of data record.
     */
    protected final long time = System.currentTimeMillis();

    /**
     * Test fixture setup.
     * 
     * @throws Exception
     *             thrown when setup failed.
     */
    @Before
    public void setupAbstractStatisticsInstance()
    {
        instance = new AbstractData(TYPE_CODE)
        {
        };
    }

    /**
     * Tests the implementation of {@link AbstractData#fromCSV(String)}.
     * <p>
     * Passed CSV representation is the empty string.
     * </p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void csvIsEmptyString()
    {
        instance.fromCSV("");
    }

    /**
     * Tests the implementation of {@link AbstractData#fromCSV(String)}.
     * <p>
     * Passed CSV representation misses the values for the <code>name</code> and <code>time</code> fields.
     * </p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void csvMissesNameAndTime()
    {
        instance.fromCSV(instance.getTypeCode());
    }

    /**
     * Tests the implementation of {@link AbstractData#fromCSV(String)}.
     * <p>
     * Passed CSV representation misses the value for the <code>time</code> field.
     * </p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void csvMissesTime()
    {
        instance.fromCSV(StringUtils.join(new Object[]
            {
                instance.getTypeCode(), name
            }, Data.DELIMITER));
    }

    /**
     * Tests the implementation of {@link AbstractData#fromCSV(String)}.
     * <p>
     * The value of the field <code>time</code> is not a valid string representation of a long value.
     * </p>
     */
    @Test(expected = NumberFormatException.class)
    public void timeInCSVIsNotLong()
    {
        instance.fromCSV(StringUtils.join(new Object[]
            {
                instance.getTypeCode(), name, "notAlong"
            }, Data.DELIMITER));
    }

    /**
     * Tests the implementation of {@link AbstractData#fromCSV(String)}.
     * <p>
     * The value of the field <code>time</code> is negative.
     * </p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void timeInCSVIsNegative()
    {
        instance.fromCSV(StringUtils.join(new Object[]
            {
                instance.getTypeCode(), name, -time
            }, Data.DELIMITER));
    }

    /**
     * Tests the set/getTransactionName.
     */
    @Test
    public void testTransactionName()
    {
        Assert.assertEquals("Wrong transaction name, ", null, instance.getTransactionName());
        instance.setTransactionName("blabla");
        Assert.assertEquals("Wrong transaction name, ", "blabla", instance.getTransactionName());
    }

    /**
     * {@link AbstractData}
     */
    @Test
    public void testParseValue()
    {
        new TestWrapper(IllegalArgumentException.class, "Invalid value for the 'time' attribute.", "Negative times should not be possible!")
        {
            @Override
            protected void run()
            {
                final TestData td = new TestData();
                final String[] array = new String[td.getMinNoCSVElements()];
                array[0] = "0";// the type code has to match the value used in the constructor of TestData
                array[1] = "aName";
                array[2] = "-123";
                for (int i = 3; i < array.length; i++)
                {
                    array[i] = "bla";
                }
                td.parseValue(array);
            }
        };
    }

    /**
     * Tests the implementation of {@link AbstractData#fromCSV(String)}.
     * <p>
     * The value of the field <code>name</code> contains the CSV delimiter.
     * </p>
     */
    @Test
    public void nameInCSVContainsCSVDelimiter()
    {
        final String name = "Doe, John";

        instance.fromCSV(CsvUtils.encode(new String[]
            {
                instance.getTypeCode(), name, Long.toString(time)
            }));
        Assert.assertEquals(name, instance.getName());
    }

    /**
     * Tests the implementation of {@link AbstractData#fromCSV(String)}.
     * <p>
     * The value of the field <code>typeCode</code> is incompatible to that of the test instance.
     * </p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void typeCodeInCSVIsInvalid()
    {
        instance.fromCSV(StringUtils.join(new Object[]
            {
                "ABC", name, time
            }, Data.DELIMITER));
    }

    /**
     * Tests the implementation of {@link AbstractData#fromCSV(String)}.
     * <p>
     * Argument string is a compatible CSV representation.
     * </p>
     */
    @Test
    public void testFromCSV_CompatibleCSV()
    {
        // read it in CSV representation and parse it

        final String typeCode = instance.getTypeCode();
        instance.fromCSV(StringUtils.join(new Object[]
            {
                typeCode, name, time
            }, Data.DELIMITER));

        // validate
        Assert.assertEquals(typeCode, instance.getTypeCode());
        Assert.assertEquals(name, instance.getName());
        Assert.assertEquals(time, instance.getTime());

    }

    /**
     * Tests the implementation of {@link AbstractData#toCSV()}.
     * <p>
     * This test case is based on the most common use case:
     * <ul>
     * <li>data record refers to a correct name and time</li>
     * <li>data record's name doesn't contain the CSV-delimiter</li>
     * <li><code>toCSV()</code> is used to construct the CSV representation of the data record which is read in shortly
     * afterwards</li>
     * </ul>
     * </p>
     */
    @Test
    public void testToCSV_StandardCase()
    {
        // set name and time
        instance.setTime(time);
        instance.setName(name);

        Assert.assertEquals(StringUtils.join(new Object[]
            {
                instance.getTypeCode(), name, time
            }, Data.DELIMITER), instance.toCSV());

    }

    /**
     * Tests the implementation of {@link AbstractData#toCSV()}.
     * <p>
     * This test case demonstrates the possibility to provoke a NullPointerException by calling <code>toCSV()</code> on
     * a AbstractData instance whose name isn't set so far.
     * </p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void testToCSV_NPE()
    {
        instance.toCSV();
    }

    /**
     * Tests the implementation of {@link AbstractData#toCSV()}.
     * <p>
     * Data record's name will be set to a string containing the CSV delimiter. It will be reinitialized using its CSV
     * representation constructed before.
     * </p>
     * <p>
     * This test case demonstrates the behavior of </code>toCSV()</code> and <code>fromCSV(String)</code> in combination
     * when the data record's name contains the CSV delimiter. Once the CSV representation has been constructed the
     * original name cannot be restored from it anymore.
     * </p>
     */
    @Test
    public void testToCSV_NameContainsDelim()
    {
        // name of data record
        final String name = "Doe, John";

        // set name
        instance.setName(name);

        // construct CSV representation
        final String csvLine = instance.toCSV();

        // restore data record from CSV representation
        instance.fromCSV(csvLine);
        // compare original and restored names
        Assert.assertEquals("Serialized and deserialized objects differ in name!", name, instance.getName());
    }

    /**
     * Check agent setter and getter
     */
    @Test
    public void testGetSetAgent()
    {
        // name of data record
        final String name = "Foobar-Agent";

        // set agent name
        instance.setAgentName(name);
        Assert.assertEquals(name, instance.getAgentName());
    }

    private static class TestData extends AbstractData
    {
        private TestData()
        {
            super("0");
        }

        protected void parseValue(final String[] values)
        {
            super.parseValues(values);
        }

        @Override
        protected int getMinNoCSVElements()
        {
            return super.getMinNoCSVElements();
        }
    }
}
