/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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

/**
 * The {@link DataManager} logs data records to a log file, from where they may be read again during test report
 * generation. The {@link DataManager} instance responsible for a certain test user may be obtained from the current
 * session object via {@link Session#getDataManager()}.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public interface DataManager
{
    /**
     * Returns the time that marks the end of the logging period.
     *
     * @return the end time
     */
    public long getEndOfLoggingPeriod();

    /**
     * Returns the time that marks the beginning of the logging period.
     *
     * @return the start time
     */
    public long getStartOfLoggingPeriod();

    /**
     * Returns whether or not logging of data records is currently enabled.
     *
     * @return the logging state
     */
    public boolean isLoggingEnabled();

    /**
     * Logs an event data record that is initialized with the given parameters, but only if logging is enabled and the
     * current time is inside the configured logging period.
     *
     * @param eventName
     *            the name of the event
     * @param message
     *            the event message
     */
    public void logEvent(String eventName, String message);

    /**
     * Logs the given data record to a log file, but only if logging is enabled and the current time is inside the
     * configured logging period.
     *
     * @param data
     *            the data record
     */
    public void logDataRecord(Data data);

    /**
     * Sets the time that marks the end of the logging period.
     *
     * @param time
     *            the end time
     */
    public void setEndOfLoggingPeriod(long time);

    /**
     * Sets whether or not logging of data records is currently enabled.
     *
     * @param state
     *            the logging state
     */
    public void setLoggingEnabled(boolean state);

    /**
     * Enables the logging of data records.
     */
    public void enableLogging();

    /**
     * Disables the logging of data records.
     */
    public void disableLogging();

    /**
     * Sets the time that marks the beginning of the logging period.
     *
     * @param time
     *            the start time
     */
    public void setStartOfLoggingPeriod(long time);
    
    /**
     * Creates or returns an already created custom data logger for the given scope.
     * @param scope the scope of the data logger
     * @return the custom data logger for the given scope
     */
    public DataLogger dataLogger(String scope);
}
