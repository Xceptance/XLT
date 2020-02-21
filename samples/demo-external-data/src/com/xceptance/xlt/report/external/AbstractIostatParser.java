package com.xceptance.xlt.report.external;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xceptance.xlt.api.report.external.AbstractLineParser;
import com.xceptance.xlt.api.report.external.ValueSet;
import com.xceptance.xlt.api.util.XltLogger;

/**
 * Abstract reader for <em>iostat</em> output log files. Parses line blocks belonging to the same time stamp and section
 * type (CPU, Device etc.).
 */
public abstract class AbstractIostatParser extends AbstractLineParser
{
    /**
     * Headline (key) and corresponding column index (value).
     */
    protected final Map<String, Integer> headlines = new HashMap<String, Integer>();

    private long lastReadTime = -1L;

    private final List<String> rows = new ArrayList<String>();

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueSet parse(final String line)
    {
        final String ln = line.trim();
        ValueSet valueSet = null;
        if (lastReadTime >= 0 || isDatetime(ln))
        {
            if (isDatetime(ln))
            {
                if (!rows.isEmpty())
                {
                    valueSet = new ValueSet(lastReadTime);
                    parseSection(rows, valueSet);
                    rows.clear();
                }
                lastReadTime = parseTime(ln);
            }
            else
            {
                rows.add(ln);
            }
        }
        return valueSet;
    }

    /**
     * Parse CPU section of 'iostat' output.
     * 
     * @param lines
     *            section lines
     * @param valueSet
     *            parsed results
     */
    protected void parseSection(final List<String> lines, final ValueSet valueSet)
    {
        if (lines.size() >= 2)
        {
            boolean isSection = false;
            for (final String line : lines)
            {
                if (!isSection)
                {
                    if (line.trim().startsWith(getSectionIdentifier()))
                    {
                        isSection = true;
                    }
                }
                else
                {
                    if (line.trim().isEmpty())
                    {
                        isSection = false;
                    }
                }

                if (isSection)
                {
                    if (!isHeaderInitialized())
                    {
                        // initialize series names.
                        //
                        // the names are taken from the first section line.
                        //
                        // if you want to use your own names, it's recommended to
                        // adapt the parsed line (if necessary) and
                        // overwrite the parseHeader() method.
                        parseHeader(line);
                    }
                    else
                    {
                        // the first line of section gets evaluated by default.
                        //
                        // if you want to evaluate an other line (e.g. a certain device in device section)
                        // you have to parse this line of interest.
                        //
                        // a good criteria might be the line start (e.g. device name 'sda1').
                        if (!line.startsWith(getSectionIdentifier()))
                        {
                            final String[] dataRow = line.trim().split("\\s+");

                            // now parse the values of the extracted line
                            parseValues(dataRow, valueSet);

                            break;
                        }
                    }
                }
            }
        }
        else
        {
            XltLogger.runTimeLogger.warn("unexpected number of section lines");
        }
    }

    /**
     * Parse the line values.
     * 
     * @param dataRow
     *            values in line
     * @param valueSet
     *            parsing results
     */
    protected void parseValues(final String[] dataRow, final ValueSet valueSet)
    {
        for (final String headline : getHeadlines().keySet())
        {
            final int columnIndex = headlines.get(headline);
            final String rawData = dataRow[columnIndex].replaceAll(",", ".");
            final double data = Double.parseDouble(rawData);

            valueSet.addValue(headline, data);
        }
    }

    /**
     * Parse the column headlines.
     * 
     * @param line
     *            line to parse
     */
    protected void parseHeader(final String line)
    {
        final String[] heads = line.substring(getSectionIdentifier().length(), line.length()).trim().split("\\s+");
        for (int index = 0; index < heads.length; index++)
        {
            final String headline = heads[index];
            if (getValueNames().contains(headline))
            {
                getHeadlines().put(headline, index + 1);
            }
        }
    }

    /**
     * Are the headlines initialized already?
     * 
     * @return <code>true</code> if headlines mapping is initialized, <code>false</code> otherwise.
     */
    protected boolean isHeaderInitialized()
    {
        return !getHeadlines().isEmpty();
    }

    /**
     * Get the headlines mapping.
     * 
     * @return headlines (key) and their column index (value)
     */
    protected Map<String, Integer> getHeadlines()
    {
        return headlines;
    }

    /**
     * Get the section identifier (e.g. 'Device:' or 'avg-cpu:')
     * 
     * @return the section identifier
     */
    protected abstract String getSectionIdentifier();

    /**
     * Does current line consists of a date string?
     * 
     * @param line
     *            line to parse
     * @return <code>true</code> if the line only consits of a date and time like <code>08.08.1977 12:34:56</code>
     */
    protected boolean isDatetime(final String line)
    {
        return parseTime(line) < 0 ? false : true;
    }
}
