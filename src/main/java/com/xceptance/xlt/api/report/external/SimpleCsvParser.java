package com.xceptance.xlt.api.report.external;

import com.xceptance.common.util.CsvUtils;

/**
 * Parses lines of a CSV file. Addresses columns by their column index number. Requires unique column names.
 *
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class SimpleCsvParser extends AbstractLineParser
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
        final String[] csv = CsvUtils.decode(line, getFieldSeparator());

        // parse time
        final long time = parseTime(csv[0]);
        final ValueSet dp = new ValueSet(time);

        // parse values
        for (int i = 1; i < csv.length; i++)
        {
            final String name = getName(i);
            if (getValueNames() != null && getValueNames().contains(name))
            {
                dp.addValue(name, Double.valueOf(csv[i]));
            }
        }

        return dp;
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
