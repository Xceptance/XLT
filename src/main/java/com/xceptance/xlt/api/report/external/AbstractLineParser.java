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
package com.xceptance.xlt.api.report.external;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

/**
 * Simple line parser that provides common functionality to parse given lines. For reading lines a file reader is
 * responsible that passes the lines to the {@link #parse} method.
 *
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractLineParser
{
    /**
     * Property name of date format pattern.
     */
    private static final String PROP_FORMAT_PATTERN = "parser.dateFormat.pattern";

    /**
     * Property name of date format time zone.
     */
    private static final String PROP_FORMAT_TIMEZONE = "parser.dateFormat.timeZone";

    /**
     * Value names represent the configured relevant value names and describe what to extract from a given line. The
     * syntax of these names depends on the concrete parser.
     */
    private Set<String> valueNames;

    /**
     * The parser's properties.
     */
    private Properties properties;

    /**
     * Date format to parse a time entry from input to a UNIX time stamp. The format is initialized with
     * <ul>
     * <li>dd.MM.yyyy HH:mm:ss</li>
     * <li>GMT+0</li>
     * </ul>
     */
    private static DateFormat FORMAT;

    /**
     * Parse the resource line.
     *
     * @param line
     *            line to parse
     * @return parsed {@link ValueSet} or <code>null</code> if there is currently nothing to return. This might be in
     *         case the line has no value of interest.
     */
    public abstract ValueSet parse(final String line);

    /**
     * Parse 'String' time stamp to 'long'.
     *
     * @param timeString
     *            human readable time stamp like <code>08.08.1977 12:34:56</code> or UNIX time stamp like
     *            <code>239891696</code>
     * @return time stamp of type 'long' or <code>-1</code> if parsing failed
     */
    protected long parseTime(final String timeString)
    {
        try
        {
            return Long.valueOf(timeString);
        }
        catch (final NumberFormatException nfe)
        {
            try
            {
                return getDateFormat().parse(timeString).getTime();
            }
            catch (final ParseException pe)
            {
                return -1;
            }
        }
    }

    /**
     * Get the date format.
     *
     * @return the date format
     */
    protected DateFormat getDateFormat()
    {
        if (FORMAT == null)
        {
            synchronized (AbstractLineParser.class)
            {
                if (FORMAT == null)
                {
                    if (getProperties() == null)
                    {
                        throw new NullPointerException("Properties have not been set for the parser. Set them first!");
                    }
                    final String formatterPattern = getProperties().getProperty(PROP_FORMAT_PATTERN, "dd.MM.yyyy HH:mm:ss");
                    final String formatterTimeZone = getProperties().getProperty(PROP_FORMAT_TIMEZONE, "GMT+0");
                    FORMAT = new SimpleDateFormat(formatterPattern);
                    FORMAT.setTimeZone(TimeZone.getTimeZone(formatterTimeZone));
                }
            }
        }
        return FORMAT;
    }

    /**
     * Store markers for relevant data (e.g. column headlines or index numbers).
     *
     * @param valueNames
     *            value names
     */
    public void setValueNames(final Set<String> valueNames)
    {
        this.valueNames = valueNames;
    }

    /**
     * Get markers for relevant data (e.g. column headlines).
     *
     * @return value names of interest
     */
    public Set<String> getValueNames()
    {
        return valueNames;
    }

    /**
     * Set the parser properties.
     *
     * @param properties
     *            the parser properties
     */
    public void setProperties(final Properties properties)
    {
        this.properties = properties;
    }

    /**
     * Get the parser properties.
     *
     * @return the parser properties
     */
    public Properties getProperties()
    {
        return properties;
    }
}
