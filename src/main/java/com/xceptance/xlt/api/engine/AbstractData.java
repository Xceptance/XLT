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

import java.util.ArrayList;
import java.util.List;

import com.xceptance.common.lang.ParseNumbers;
import com.xceptance.common.lang.XltCharBuffer;
import com.xceptance.common.util.CsvUtils;
import com.xceptance.common.util.CsvUtilsDecode;
import com.xceptance.common.util.SimpleArrayList;

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
    private long time;

    /**
     * The type code.
     */
    private char typeCode;

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
     * Creates a new AbstractData object and gives it the specified name and type code.
     *
     * @param name
     *            the request name
     * @param typeCode
     *            the type code
     */
    public AbstractData(final String name, final char typeCode)
    {
        this.name = name;
        this.typeCode = typeCode;
    }

    /**
     * Creates a new AbstractData object and gives it the specified type code.
     *
     * @param typeCode
     *            the type code
     */
    public AbstractData(final char typeCode)
    {
        this(null, typeCode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void baseValuesFromCSV(final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer s)
    {
        CsvUtilsDecode.parse(result, s, DELIMITER);
        parseBaseValues(result);
    }

    /**
     * Mainly for testing, we can recreate the state from a list at once
     */
    public final void parseAll(final SimpleArrayList<XltCharBuffer> result)
    {
        parseBaseValues(result);
        parseValues(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void remainingFromCSV(final SimpleArrayList<XltCharBuffer> result)
    {
        parseValues(result);
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
    public char getTypeCode()
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
        final List<String> fields = addValues();

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

        fields.add(String.valueOf(typeCode));
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
        // typeCode, name, time
        return 3;
    }

    /**
     * Recreates the base states, such as time and typecode
     *
     * @param values
     *            the list of values, must have at least the length {@link #getMinNoCSVElements()}
     */
    protected void parseBaseValues(final List<XltCharBuffer> values)
    {
        if (values.size() < getMinNoCSVElements())
        {
            throw new IllegalArgumentException(String.format("Expected at least %d fields, but got only %d -> %s", getMinNoCSVElements(),
                                                             values.size(), values));
        }

        // check the type code
        if (values.get(0).charAt(0) == typeCode)
        {
            // read and check the values
            name = values.get(1).toString();
            name.hashCode(); // create it when it is still hot in the cache

            time = ParseNumbers.parseLong(values.get(2));

            if (time <= 0)
            {
                throw new IllegalArgumentException(String.format("Invalid time value: %d", time));
            }
        }
        else
        {
            throw new IllegalArgumentException("Cannot recreate the object state. The read type code '" + values.get(0) +
                                               "' does not match the expected type code '" + typeCode + "'.");
        }
    }

    /**
     * Recreates the state of this object from an array of values. Override this method in sub classes to restore
     * custom values.
     *
     * @param values
     *            the list of values, must have at least the length {@link #getMinNoCSVElements()}
     */
    protected abstract void parseValues(final List<XltCharBuffer> values);

    @Override
    public int compareTo(final Data o)
    {
        if (this.typeCode == o.getTypeCode())
        {
            return this.name.compareTo(o.getName());
        }

        return this.typeCode < o.getTypeCode() ? -1 : 1;
    }


}
