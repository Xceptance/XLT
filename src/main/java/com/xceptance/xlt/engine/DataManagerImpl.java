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
package com.xceptance.xlt.engine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import com.xceptance.common.util.CsvUtils;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.DataManager;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.metrics.Metrics;

/**
 * Implementation of interface {@link DataManager}.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class DataManagerImpl implements DataManager
{
    /**
     * System-dependent line separator.
     */
    private static final String LINE_SEPARATOR = System.lineSeparator();

    /**
     * A mutex object to guard parent directory creation. Necessary since File.mkdirs() is not thread-safe.
     */
    private static final Object mutex = new Object();

    /**
     * Whether or not logging is enabled.
     */
    private boolean loggingEnabled = true;

    /**
     * The beginning of the logging period.
     */
    private long startOfLoggingPeriod = Long.MIN_VALUE;

    /**
     * The end of the logging period.
     */
    private long endOfLoggingPeriod = Long.MAX_VALUE;

    /**
     * The number of logged events.
     */
    private int numberOfEvents;

    /**
     * Logger responsible for logging the statistics to the timer file(s).
     */
    private volatile BufferedWriter logger;

    /**
     * Returns the number of events that have occurred.
     *
     * @return the number of events
     */
    public int getNumberOfEvents()
    {
        return numberOfEvents;
    }

    /**
     * Back-reference to session using this data manager.
     * <p>
     * Necessary as this data manager might be used by foreign threads (e.g. worker-threads of Grizzly WebSocket
     * server).
     */
    private final SessionImpl session;

    /**
     * Creates a new data manager for the given session.
     *
     * @param session
     *            the session that should use this data manager
     */
    protected DataManagerImpl(final SessionImpl session)
    {
        this.session = session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logEvent(final String eventName, final String message)
    {
        final EventData e = new EventData(eventName);
        e.setTestCaseName(session.getUserName());
        e.setMessage(message);

        logDataRecord(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logDataRecord(final Data stats)
    {
        // update metrics for real-time reporting
        Metrics.getInstance().updateMetrics(stats);

        // get the statistics logger, avoid the method call
        final BufferedWriter timerWriter = logger != null ? logger : getTimerLogger();

        // no statistics logger configured -> exit here
        if (timerWriter == null)
        {
            return;
        }

        // Check whether the data record falls into the logging period.
        // Take the data record's (start) time as the criterion.
        final long time = stats.getTime();

        if (loggingEnabled && startOfLoggingPeriod <= time && time <= endOfLoggingPeriod)
        {
            // write the log line
            try
            {
                // this safes us from synchronization, the writer is already synchronized
                var s = CsvUtils.removeLineSeparator(stats.toCSV(), ' ');
                s.append(LINE_SEPARATOR);

                timerWriter.write(s.toString());
                timerWriter.flush();
            }
            catch (final IOException ex)
            {
                XltLogger.runTimeLogger.error("Failed to write statistics:", ex);
            }

            // special handling of events
            if (stats instanceof EventData)
            {
                final EventData event = (EventData) stats;

                if (XltLogger.runTimeLogger.isWarnEnabled())
                {
                    XltLogger.runTimeLogger.warn(String.format("EVENT: %2$s - %1$s - '%3$s'", event.getName(), event.getTestCaseName(),
                                                               event.getMessage()));
                }

                numberOfEvents++;
            }
        }
    }

    /**
     * Returns the output logger. The logger is created if necessary.
     *
     * @return the logger creating the timer output
     */
    private BufferedWriter getTimerLogger()
    {
        // check if logger has already been initialized
        if (logger != null)
        {
            return logger;
        }

        // only one can create the logger
        synchronized (this)
        {
            // was someone else faster?
            if (logger != null)
            {
                return logger;
            }

            // get the appropriate timer file
            final File file = getTimerFile();

            // creation of timer file has failed for any reason -> exit here
            if (file == null)
            {
                return null;
            }

            try
            {
                logger = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), XltConstants.UTF8_ENCODING));
            }
            catch (UnsupportedEncodingException | FileNotFoundException e)
            {
                XltLogger.runTimeLogger.error("Cannot create writer for file: " + file, e);
            }
        }

        return logger;
    }

    /**
     * Returns the timer file for the current session. If it does not exist yet, it will be created.
     *
     * @return timer file
     */
    File getTimerFile()
    {
        // create file handle for new file named 'timers.csv' rooted at the session's result directory
        final File file = new File(session.getResultsDirectory(), XltConstants.TIMER_FILENAME);

        try
        {
            // mkdirs is not thread-safe
            synchronized (mutex)
            {
                file.getParentFile().mkdirs();
            }

            return file;
        }
        catch (final Exception e)
        {
            XltLogger.runTimeLogger.error("Cannot create file for output of timer: " + file, e);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getStartOfLoggingPeriod()
    {
        return startOfLoggingPeriod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getEndOfLoggingPeriod()
    {
        return endOfLoggingPeriod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLoggingEnabled()
    {
        return loggingEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStartOfLoggingPeriod(final long time)
    {
        startOfLoggingPeriod = time;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEndOfLoggingPeriod(final long time)
    {
        endOfLoggingPeriod = time;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLoggingEnabled(final boolean state)
    {
        loggingEnabled = state;
    }

    /**
     * Resets the timer logger. Any subsequent call to {@link #getTimerLogger()} will cause a new timer logger to be
     * created.
     */
    public synchronized void resetLoggerFile()
    {
        logger = null;
    }
}
