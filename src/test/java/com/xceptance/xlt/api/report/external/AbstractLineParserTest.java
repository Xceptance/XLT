/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.report.external;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.common.lang.ReflectionUtils;

/**
 * Tests AbstractLineParser by using a dummy implementation of it instead of using reflection.
 * 
 * @author Sebastian Oerding
 */
public class AbstractLineParserTest
{
    /**
     * Tests the parseTime in class {@link AbstractLineParser}.
     */
    @Test
    public void testParseTime()
    {
        final AbstractLineParserDummyImpl alp = new AbstractLineParserDummyImpl();
        alp.setProperties(new Properties());
        /*
         * 1. test with valid long value, must match the intended date 2. test with non valid long value that matches a
         * date format, must match the intended date 3. test with a mess, must give -1
         */
        final long expected = 123000;
        long date = alp.parseTime(String.valueOf(expected));
        Assert.assertEquals("Date incorrectly parsed", expected, date);
        date = alp.parseTime("01.01.1970 00:02:03"); // Should also result in 123 as time zone GMT+0 should be used
        Assert.assertEquals("Date incorrectly parsed", expected, date);
        date = alp.parseTime("aargh"); // Should result in -1 as default return value
        Assert.assertEquals("Date incorrectly parsed", -1, date);
    }

    /**
     * Tests getDateFormat in class {@link AbstractLineParser}.
     */
    @Test
    public void testGetDateFormat()
    {
        /*
         * 1. check default values 3. check given values 2. Ensure setting properties afterwards does not change the
         * format
         */
        final Properties properties = new Properties();

        final AbstractLineParserDummyImpl alp = new AbstractLineParserDummyImpl();
        alp.setProperties(properties);
        Assert.assertEquals("Default time zone has been changed", "GMT+00:00", alp.getDateFormat().getTimeZone().getID());

        try
        {
            AbstractLineParser.class.getDeclaredField("PROP_FORMAT_PATTERN");
            final Field timeZoneField = AbstractLineParser.class.getDeclaredField("PROP_FORMAT_TIMEZONE");
            timeZoneField.setAccessible(true);
            final String timeZoneKey = (String) timeZoneField.get(null);

            properties.put(timeZoneKey, "GMT+5");
            // assert that the stored format does not changed due to adding the property
            Assert.assertEquals("The default / stored time zone has been changed", "GMT+00:00", alp.getDateFormat().getTimeZone().getID());

            // resetting the field via reflection
            final Field format = AbstractLineParser.class.getDeclaredField("FORMAT");
            format.setAccessible(true);
            format.set(null, null);

            final AbstractLineParserDummyImpl alp2 = new AbstractLineParserDummyImpl();
            alp2.setProperties(properties);
            // assert that the value given to the property makes to the format
            Assert.assertEquals("Time zone is wrong!", "GMT+05:00", alp2.getDateFormat().getTimeZone().getID());
        }
        catch (final SecurityException e)
        {
            // Should not happen
            e.printStackTrace();
        }
        catch (final NoSuchFieldException e)
        {
            throw new IllegalStateException("Field not found! Have the names of the constants been changed?", e);
        }
        catch (final IllegalAccessException e)
        {
            // Should not happen
            e.printStackTrace();
        }
    }

    /**
     * Verifies, that an exception is thrown, if the properties are not set.
     */
    @Test(expected = NullPointerException.class)
    public void testGetDateFormatThrowsException()
    {
        final AbstractLineParserDummyImpl alp = new AbstractLineParserDummyImpl();
        // The static field 'FORMAT' has to be null
        ReflectionUtils.writeStaticField(AbstractLineParser.class, "FORMAT", null);
        // the properties have to be null
        alp.setProperties(null);
        alp.getDateFormat();
    }

    /**
     * Dummy implementation that only makes {@link #parseTime(String)} and {@link #getDateFormat()} visible to be
     * testable.
     */
    private class AbstractLineParserDummyImpl extends AbstractLineParser
    {
        /**
         * Simple no operation implementation to have a non abstract implementation.
         * 
         * @return nothing, throws always an exception
         * @throws UnsupportedOperationException
         */
        @Override
        public ValueSet parse(final String line)
        {
            throw new UnsupportedOperationException("Not implemented!");
        }

        @Override
        protected long parseTime(final String time)
        {
            return super.parseTime(time);
        }

        @Override
        protected DateFormat getDateFormat()
        {
            return super.getDateFormat();
        }
    }
}
