package com.xceptance.xlt.api.report.external;

import com.xceptance.common.util.CsvUtils;

/**
 * Parses lines of a CSV file. Addresses columns by their column headline. It is assumed that the first line in file is
 * the head-line. For any values to be parsed the {@link #setValueNames(java.util.Set)} has to be called with the names
 * of the columns which should be parsed (the time is stored in an extra field, so you do not have to give it as value
 * name. The time column has to be the first one in the csv file).
 *
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class HeadedCsvParser extends SimpleCsvParser
{
    /**
     * Column headlines.
     */
    private String[] heads = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueSet parse(final String line)
    {
        if (heads == null)
        {
            // first line is header line
            heads = CsvUtils.decode(line, getFieldSeparator());
            return null;
        }

        return super.parse(line);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getName(final int i)
    {
        return (heads != null && i < heads.length) ? heads[i] : Integer.toString(i);
    }
}
