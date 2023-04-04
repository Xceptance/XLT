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
package com.xceptance.xlt.api.report.external;

import java.util.List;

import com.xceptance.common.util.CsvUtils;

/**
 * Parses a line of a CSV file, extracts the values of interest, and returns them as a {@link ValueSet}. The value set
 * will <em>not</em> carry a timestamp. Use this parser if you simply want to present the contents of a CSV file as a
 * data table in the load test report.
 * <p>
 * This parser can be configured in file <code>externaldataconfig.xml</code> using the following properties:
 * <ul>
 * <li>{@value #PROP_FIELD_SEPARATOR} - the field separator to use (',' by default)</li>
 * </ul>
 *
 * @see SimpleCsvParser
 * @see HeadedCsvParser
 */
public class PlainDataTableCsvParser extends AbstractLineParser
{
    /**
     * Property name for CVS separator char.
     */
    private static final String PROP_FIELD_SEPARATOR = "parser.csv.separator";

    /**
     * Used field separator.
     */
    private char fieldSeparator = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueSet parse(final String line)
    {
        final List<String> csv = CsvUtils.decode(line, getFieldSeparator());

        final ValueSet dp = new ValueSet(-1);

        // parse values
        for (int i = 0; i < csv.size(); i++)
        {
            final String name = getName(i);
            if (getValueNames() != null && getValueNames().contains(name))
            {
                dp.addValue(name, parseValue(csv.get(i)));
            }
        }

        return dp;
    }

    /**
     * Parses the given string value to an object and returns either an {@link Integer} or a {@link Double} or the
     * string as is.
     *
     * @return the value as an object
     */
    private Object parseValue(final String valueString)
    {
        // TODO: get the wanted type from the configuration?

        Object value;

        try
        {
            value = Integer.valueOf(valueString);
        }
        catch (final NumberFormatException e)
        {
            try
            {
                value = Double.valueOf(valueString);
            }
            catch (final NumberFormatException e2)
            {
                value = valueString;
            }
        }

        return value;
    }

    /**
     * Returns the name of the i-th column.
     *
     * @param i
     *            the column index
     * @return the column's name
     */
    protected String getName(final int i)
    {
        return String.valueOf(i);
    }

    /**
     * Returns the configured CSV field separator character. By default it is a comma.
     *
     * @return field separator
     */
    protected char getFieldSeparator()
    {
        if (fieldSeparator == 0)
        {
            // the field separator has not been set yet
            if (getProperties() == null)
            {
                throw new IllegalStateException("Properties have not been set! Set them first.");
            }
            final String separatorProp = getProperties().getProperty(PROP_FIELD_SEPARATOR, ",");
            fieldSeparator = (separatorProp.length() == 0) ? ',' : separatorProp.charAt(0);
        }

        return fieldSeparator;
    }
}
