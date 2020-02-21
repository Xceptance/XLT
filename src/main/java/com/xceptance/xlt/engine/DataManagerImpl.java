package com.xceptance.xlt.engine;

import java.io.File;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

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
    private Logger logger;

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
    public synchronized void logDataRecord(final Data stats)
    {
        // update metrics for real-time reporting
        Metrics.getInstance().updateMetrics(stats);

        // get the statistics logger
        logger = getTimerLogger();

        // no statistics logger configured -> exit here
        if (logger == null)
        {
            return;
        }

        // Check whether the data record falls into the logging period.
        // Take the data record's (start) time as the criterion.
        final long time = stats.getTime();

        if (loggingEnabled && startOfLoggingPeriod <= time && time <= endOfLoggingPeriod)
        {
            // write the log line
            logger.info(stats.toCSV().replaceAll("[\n\r]+", " "));

            // special handling of events
            if (stats instanceof EventData)
            {
                final EventData event = (EventData) stats;

                if (XltLogger.runTimeLogger.isEnabledFor(Level.WARN))
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
    private Logger getTimerLogger()
    {
        // check if logger has already been initialized
        synchronized (this)
        {
            if (logger != null)
            {
                return logger;
            }
        }

        // get the appropriate timer file
        final File file = getTimerFile();
        // creation of timer file has failed for any reason -> exit here
        if (file == null)
        {
            return null;
        }

        // create the roll-over file appender
        final DailyRollingFileAppender appender = new DailyRollingFileAppender();
        // set the file encoding
        appender.setEncoding("UTF-8");
        // set our logging layout
        appender.setLayout(new Layout()
        {

            @Override
            public String format(final LoggingEvent paramLoggingEvent)
            {
                return paramLoggingEvent.getMessage() + LINE_SEP;
            }

            @Override
            public boolean ignoresThrowable()
            {
                return true;
            }

            @Override
            public void activateOptions()
            {
            }
        });

        // set the pattern to be used
        appender.setDatePattern("'.'yyyy-MM-dd");
        // set a name for the appender (so it can be identified later on if necessary)
        appender.setName("DailyAppender");
        // set the output file
        appender.setFile(file.getAbsolutePath());
        // appender configuration finished -> activate it
        appender.activateOptions();

        // get the logger for the current session
        final Logger logger = Logger.getLogger(session.getUserID());
        // set its additivity
        logger.setAdditivity(false);
        // ... its level
        logger.setLevel(Level.ALL);
        // ... and finally its appender
        logger.addAppender(appender);

        // returned configured logger
        return logger;
    }

    /**
     * Returns the timer file for the current session. It it does not exist yet, it will be created.
     * 
     * @return timer file
     */
    private File getTimerFile()
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
            XltLogger.runTimeLogger.fatal("Cannot create file for output of timer: " + file, e);
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
