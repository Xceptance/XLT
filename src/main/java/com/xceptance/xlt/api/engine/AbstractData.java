package com.xceptance.xlt.api.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.xceptance.common.lang.ParseNumbers;
import com.xceptance.common.util.CsvUtils;

/**
 * The {@link AbstractData} class may be the super class of a special data record class.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractData implements Data
{
    /**
     * The time when the event occurred that this data record was created for.
     */
    private long time = GlobalClock.getInstance().getTime();

    /**
     * The type code.
     */
    private final String typeCode;

    /**
     * The name of the data record. Typically, data records for the same piece of work share a common name.
     */
    private String name;

    /**
     * The name of the transaction that produced this data record. Only used during report generation or analysis.
     */
    private String transactionName;

    /**
     * The name of the agent that produced this data record. Only used during report generation or analysis.
     */
    private String agentName;

    /**
     * Creates a new AbstractData object and gives it the specified type code.
     * 
     * @param typeCode
     *            the type code
     */
    public AbstractData(final String typeCode)
    {
        this(null, typeCode);
    }

    /**
     * Creates a new AbstractData object and gives it the specified name and type code.
     * 
     * @param name
     *            the request name
     * @param typeCode
     *            the type code
     */
    public AbstractData(final String name, final String typeCode)
    {
        this.name = name;
        this.typeCode = typeCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void fromCSV(final String s)
    {
        final String[] fields = CsvUtils.decode(s, DELIMITER);
        parseValues(fields);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAgentName()
    {
        return agentName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTime()
    {
        return time;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTransactionName()
    {
        return transactionName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeCode()
    {
        return typeCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAgentName(final String agentName)
    {
        this.agentName = agentName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTime()
    {
        time = GlobalClock.getInstance().getTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTime(final long time)
    {
        this.time = time;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTransactionName(final String transactionName)
    {
        this.transactionName = transactionName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toCSV()
    {
        final List<String> fieldList = addValues();
        final String[] fields = fieldList.toArray(new String[fieldList.size()]);

        return CsvUtils.encode(fields, DELIMITER);
    }

    /**
     * Builds a list of string values that represents the state of this object. Override this method in sub classes to
     * add custom values and use the list created by the super class.
     * 
     * @return the list of values
     */
    protected List<String> addValues()
    {
        final List<String> fields = new ArrayList<String>(20);

        fields.add(typeCode);
        fields.add(name);
        fields.add(Long.toString(time));

        return fields;
    }

    /**
     * Returns the minimum number of elements in the CSV string.
     * 
     * @return minimum number of elements in the CSV string
     */
    protected int getMinNoCSVElements()
    {
        return 3;
    }

    /**
     * Recreates the state of this object from an array of string values. Override this method in sub classes to restore
     * custom values, but do not forget to call the super class first.
     * 
     * @param values
     *            the list of values, must have at least the length {@link #getMinNoCSVElements()}
     */
    protected void parseValues(final String[] values)
    {
        if (values.length < getMinNoCSVElements())
        {
            throw new IllegalArgumentException(String.format("Expected at least %d fields, but got only %d -> %s", getMinNoCSVElements(),
                                                             values.length, Arrays.toString(values)));
        }

        // check the type code
        if (values[0].equals(typeCode))
        {
            // read and check the values
            name = values[1];
            time = ParseNumbers.parseLong(values[2]);

            if (time <= 0)
            {
                throw new IllegalArgumentException("Invalid value for the 'time' attribute.");
            }
        }
        else
        {
            throw new IllegalArgumentException("Cannot recreate the object state. The read type code '" + values[0] +
                                               "' does not match the expected type code '" + typeCode + "'.");
        }
    }
}
