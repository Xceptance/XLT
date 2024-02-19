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
package com.xceptance.xlt.api.engine;

import java.util.List;

import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * <p>
 * The {@link Data} interface defines the minimum functionality any data record must implement to be recordable by the
 * XLT engine. Typically, the XLT engine already collects sufficient data (e.g. error and run time values). However, it
 * may be necessary to log other important values as well. For this, a new data record type needs to be created, either
 * by implementing this interface or - preferably - by extending from one of the already existing data record types.
 * </p>
 * <p>
 * Such a data record class must obey the following rules:
 * <ul>
 * <li>it must have a name</li>
 * <li>it must have a time stamp</li>
 * <li>it must be able to serialize its state as a list of strings</li>
 * <li>it must be able to reconstruct its state from a list of strings</li>
 * <li>in order to tell one type of data record from the other, the first entry in the value list must be a type code
 * (e.g. "MyRecordType" or simple "M")</li>
 * </ul>
 * <p>
 * Note that the type codes 'A', 'C', 'E', 'J', 'P', 'R', 'T', 'V', and 'W' are already used by the XLT engine.
 * </p>
 * <p>
 * The data record can be logged using the {@link DataManager}.
 * </p>
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public interface Data
{
    /**
     * Returns the full state of the object as a list of strings. The first three entries have to be: the type code, the
     * name, and the timestamp. All remaining entries are specific to the concrete implementation class.
     * <p>
     * Note that {@link #getAllValues()} and the two methods {@link #initBaseValues(List)} and
     * {@link #initRemainingValues(List)} work together. It is the programmer's responsibility that the number of values
     * and their order in the list
     *
     * @return the list of values that form the state of this object
     * @see #initBaseValues(List)
     * @see #initRemainingValues(List)
     */
    public List<String> getAllValues();

    /**
     * Initializes only the base values of this object (type code, name, and timestamp) from a list of string values. If
     * needed at all, the remaining values will later be initialized by calling {@link #initRemainingValues(List)} with
     * the same list of values. Splitting the process of recreating the full object state into two methods is purely for
     * performance reasons.
     *
     * @param values
     *            the string list to recreate the object state from
     * @see #initRemainingValues(List)
     * @see #getAllValues()
     */
    public void initBaseValues(final List<XltCharBuffer> values);

    /**
     * Initializes the remaining values of this object from a list of string values. Splitting the process of recreating
     * the full object state into two methods is purely for performance reasons.
     *
     * @param values
     *            the string list to recreate the object state from
     * @param result
     *            the previously parsed data as list
     * @see #initBaseValues(List)
     * @see #getAllValues()
     */
    public void initRemainingValues(final List<XltCharBuffer> result);

    /**
     * Returns the name of the agent that produced this data record. Only used during report generation or analysis.
     *
     * @return the agent's name
     */
    public String getAgentName();

    /**
     * Returns the name of this data record.
     *
     * @return the name
     */
    public String getName();

    /**
     * Returns the time when the event occurred that this data record was created for.
     *
     * @return the time
     */
    public long getTime();

    /**
     * Returns the name of the transaction that produced this data record. Only used during report generation or
     * analysis.
     *
     * @return the transaction's name
     */
    public String getTransactionName();

    /**
     * Returns the type code of this data record.
     *
     * @return the type code
     */
    public char getTypeCode();

    /**
     * Sets the name of the agent that produced this data record. Only used during report generation or analysis.
     *
     * @param agentName
     *            the agent's name
     */
    public void setAgentName(final String agentName);

    /**
     * Sets the name of this data record.
     *
     * @param name
     *            the name
     */
    public void setName(String name);

    /**
     * Sets the time when this record's event occurred. To obtain the timestamp, please use
     * {@link GlobalClock#millis()}.
     *
     * @param time
     *            the timestamp
     */
    public void setTime(long time);

    /**
     * Sets the name of the transaction that produced this data record. Only used during report generation or analysis.
     *
     * @param transactionName
     *            the transaction's name
     */
    public void setTransactionName(String transactionName);
}
