/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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

/**
 * <p>
 * The {@link EventData} class is used to record information about arbitrary "events" that may occur during a test run.
 * These events can be used to indicate that the test has encountered a special situation, which is not an error (which
 * would abort the test run), but is too important to ignore or to write to the log only. Events recorded this way are
 * evaluated during report generation, and a summary of the events occurred during a test run appears in the test
 * report.
 * </p>
 * <p>
 * Typically, {@link EventData} objects are created by custom code only, since the framework has no notion about
 * "special situations". The statistics name inherited from the parent class can be used to indicate the type of event.
 * Additionally, an {@link EventData} object carries a message string describing the event in some greater detail. As
 * with all statistics, {@link EventData} objects can be recorded via the {@link DataManager}.
 * </p>
 * <p style="color:green">
 * Note that {@link EventData} objects have an "E" as their type code.
 * </p>
 * 
 * @see ActionData
 * @see CustomData
 * @see RequestData
 * @see TransactionData
 * @see DataManager
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class EventData extends AbstractData
{
    /**
     * The type code ("E").
     */
    private static final String TYPE_CODE = "E";

    /**
     * The message describing the details of this event.
     */
    private String message;

    /**
     * The name of the test case that generated this event.
     */
    private String testCaseName;

    /**
     * Creates a new EventData object.
     */
    public EventData()
    {
        super(TYPE_CODE);
    }

    /**
     * Creates a new EventData object and gives it the specified name.
     * 
     * @param name
     *            the event name
     */
    public EventData(final String name)
    {
        super(name, TYPE_CODE);
    }

    /**
     * Returns the message associated with this event.
     * 
     * @return the message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Returns the name of the test case that generated this event.
     * 
     * @return the test case name
     */
    public String getTestCaseName()
    {
        return testCaseName;
    }

    /**
     * Sets the message associated with this event.
     * 
     * @param message
     *            the message to set
     */
    public void setMessage(final String message)
    {
        this.message = message;
    }

    /**
     * Sets the name of the test case that generated this event.
     * 
     * @param testCaseName
     *            the test case name
     */
    public void setTestCaseName(final String testCaseName)
    {
        this.testCaseName = testCaseName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> addValues()
    {
        final List<String> fields = super.addValues();

        fields.add(testCaseName);
        fields.add(message);

        return fields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void parseValues(final String[] values)
    {
        super.parseValues(values);

        // read and check the values
        testCaseName = values[3];
        message = values[4];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getMinNoCSVElements()
    {
        return 5;
    }
}
