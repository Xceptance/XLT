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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.DataManager;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.api.engine.Session;
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
     * Our reference to metrics
     */
    private final Metrics metrics;

    /**
     * Back-reference to session using this data manager.
     * <p>
     * Necessary as this data manager might be used by foreign threads (e.g. worker-threads of Grizzly WebSocket
     * server).
     */
    private final Session session;

    /**
     * Creates a new data manager for the given session.
     *
     * @param session
     *            the session that should use this data manager
     * @param metrics
     *            a metrics target for real time loggiing
     */
    protected DataManagerImpl(final Session session, final Metrics metrics)
    {
        this.session = session;
        this.metrics = metrics;
    }

    /**
     * Creates a new data manager for the given session.
     *
     * @param session
     *            the session that should use this data manager
     */
    protected DataManagerImpl(final Session session)
    {
        this.session = session;
        this.metrics = null;
    }

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
     * {@inheritDoc}
     */
    @Override
    public void logEvent(final String eventName, final String message)
    {
        final EventData e = new EventData(eventName);
        e.setTime(GlobalClock.millis());
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
        if (metrics != null)
        {
            metrics.updateMetrics(stats);
        }

        // Check whether the data record falls into the logging period.
        // Take the data record's (start) time as the criterion.
        final long time = stats.getTime();

        if (loggingEnabled && startOfLoggingPeriod <= time && time <= endOfLoggingPeriod)
        {
            // get the statistics logger, avoid the method call
            final BufferedWriter timerWriter = logger != null ? logger : getTimerLogger();

            // no statistics logger configured -> exit here
            if (timerWriter == null)
            {
                return;
            }

            // write the log line
            try
            {
                var s = removeLineSeparators(stats.toCSV(), ' ');
                s.append(LINE_SEPARATOR);

                // this safes us from synchronization, the writer is already synchronized
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
                numberOfEvents++;

                if (XltLogger.runTimeLogger.isWarnEnabled())
                {
                    final EventData event = (EventData) stats;
                    XltLogger.runTimeLogger.warn(String.format("EVENT: %2$s - %1$s - '%3$s'", event.getName(), event.getTestCaseName(),
                                                               event.getMessage()));
                }
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
            final Path file = getTimerFile();

            // creation of timer file has failed for any reason -> exit here
            if (file == null)
            {
                return null;
            }

            try
            {
                // we append to an existing file
                logger = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
            catch (IOException e)
            {
                XltLogger.runTimeLogger.error("Cannot create writer for file: " + file.toString(), e);
            }
        }

        return logger;
    }

    /**
     * Returns the timer file for the current session. If it does not exist yet, it will be created.
     *
     * @return timer file
     */
    Path getTimerFile()
    {
        // create file handle for new file named 'timers.csv' rooted at the session's result directory
        // will create the directory as well!
        final Path dir = session.getResultsDirectory();

        if (dir == null)
        {
            throw new RuntimeException("Missing result dir, see previous exceptions.");
        }

        final Path file = dir.resolve(XltConstants.TIMER_FILENAME);

        return file;
    }

    /**
     * Closes the timer logger and voids it. Any subsequent call to {@link #getTimerLogger()} will cause a new timer
     * logger to be created.
     *
     * @return true if logger closes, false otherwise
     */
    public boolean close()
    {
        if (logger != null)
        {
            try
            {
                var l = logger;
                logger = null;

                // it might be shared
                if (l != null)
                {
                    l.close();
                }

                return true;
            }
            catch (IOException e)
            {
                return false;
            }
        }

        // no logger, counts as closed
        return true;
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
    public boolean isLoggingEnabled()
    {
        return loggingEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enableLogging()
    {
        loggingEnabled = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disableLogging()
    {
        loggingEnabled = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLoggingEnabled(boolean state)
    {
        if (state)
        {
            enableLogging();
        }
        else
        {
            disableLogging();
        }
    }

    /**
     * Removes LF and CR and replaces it with something else. This is an in-place operation on the passed buffer.
     *
     * @param the
     *            buffer to check
     * @param the
     *            replacement character
     * @return a cleaned buffer
     */
    static StringBuilder removeLineSeparators(final StringBuilder src, final char replacementChar)
    {
        for (int i = 0; i < src.length(); i++)
        {
            var c = src.charAt(i);

            if (c == '\n' || c == '\r')
            {
                src.setCharAt(i, replacementChar);
            }
        }

        return src;
    }
}
