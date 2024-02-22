/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * The {@link AbstractData} class may be the super class of a special data record class.
 * <p>
 * Import change in 7.0: We are not longer automatically capturing the start time when this object is created for
 * performance reasons. You have to set the time explicitly.
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
     * Recreates the full object state at once. Mainly for testing.
     *
     * @param values
     *            the string list to recreate the object state from
     */
    public final void setAllValues(final List<XltCharBuffer> values)
    {
        setBaseValues(values);
        setRemainingValues(values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBaseValues(final List<XltCharBuffer> values)
    {
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
     * <p>
     * Override this method in sub classes by calling the super method and adding custom values to the list it returns.
     */
    public List<String> toList()
    {
        final List<String> fields = new ArrayList<String>(20);

        fields.add(String.valueOf(typeCode));
        fields.add(name);
        fields.add(Long.toString(time));

        return fields;
    }
}
