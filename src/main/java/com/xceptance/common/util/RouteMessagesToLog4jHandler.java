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
package com.xceptance.common.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * A special java.util.logging ("JUL") handler that routes any {@link LogRecord} to Log4J.
 */
public class RouteMessagesToLog4jHandler extends Handler
{
    /**
     * The name of the logger to use if the log record does not specify a logger name.
     */
    private static final String DEFAULT_LOGGER_NAME = "unknown";

    /**
     * Removes any existing handler from JUL and installs a {@link RouteMessagesToLog4jHandler} instead.
     */
    public static void install()
    {
        // get the root logger
        final java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");

        // remove all existing handlers from the root logger
        final Handler[] handlers = rootLogger.getHandlers();
        for (final Handler handler : handlers)
        {
            rootLogger.removeHandler(handler);
        }

        // finally add the one and only Log4J handler
        rootLogger.addHandler(new RouteMessagesToLog4jHandler());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(final LogRecord record)
    {
        if (record == null)
        {
            return;
        }

        final Logger log4jLogger = getLogger(record);
        final Level log4jLevel = convertToLog4jLevel(record.getLevel());

        if (log4jLogger.isEnabledFor(log4jLevel))
        {
            final LoggingEvent event = convertToLoggingEvent(record, log4jLogger, log4jLevel);

            log4jLogger.callAppenders(event);
        }
    }

    /**
     * Returns the Log4J logger with the same name as specified in the log record. If no logger name is specified, the
     * logger with the name {@value #DEFAULT_LOGGER_NAME} will be used.
     * 
     * @param record
     *            the JUL log record
     * @return the Log4J logger
     */
    private Logger getLogger(final LogRecord record)
    {
        String loggerName = record.getLoggerName();
        if (loggerName == null)
        {
            loggerName = DEFAULT_LOGGER_NAME;
        }

        return Logger.getLogger(loggerName);
    }

    /**
     * Converts a JUL log level to its corresponding Log4J log level.
     *
     * @param level
     *            the JUL log level
     * @return the Log4J log level
     */
    private Level convertToLog4jLevel(final java.util.logging.Level level)
    {
        if (java.util.logging.Level.FINEST.equals(level))
        {
            return Level.TRACE;
        }
        else if (java.util.logging.Level.FINER.equals(level))
        {
            return Level.DEBUG;
        }
        else if (java.util.logging.Level.FINE.equals(level))
        {
            return Level.DEBUG;
        }
        else if (java.util.logging.Level.INFO.equals(level))
        {
            return Level.INFO;
        }
        else if (java.util.logging.Level.WARNING.equals(level))
        {
            return Level.WARN;
        }
        else if (java.util.logging.Level.SEVERE.equals(level))
        {
            return Level.ERROR;
        }
        else if (java.util.logging.Level.ALL.equals(level))
        {
            return Level.ALL;
        }
        else if (java.util.logging.Level.OFF.equals(level))
        {
            return Level.OFF;
        }
        else
        {
            // any other log level, especially custom levels
            return Level.DEBUG;
        }
    }

    /**
     * Converts a JUL log record to its corresponding Log4J logging event.
     *
     * @param record
     *            the JUL log record
     * @param logger
     *            the Log4J logger to use for the logging event
     * @param level
     *            the Log4J level to use for the logging event
     * @return the Log4J logging event
     */
    private LoggingEvent convertToLoggingEvent(final LogRecord record, final Logger logger, final Level level)
    {
        final String message = formatMessage(record);
        final String threadName = Thread.currentThread().getName(); // String.valueOf(record.getThreadID());
        final ThrowableInformation throwableInformation = (record.getThrown()) == null ? null
                                                                                       : new ThrowableInformation(record.getThrown());
        final LocationInfo locationInfo = new LocationInfo(null, record.getSourceClassName(), record.getSourceMethodName(), null);

        return new LoggingEvent(record.getSourceClassName(), logger, record.getMillis(), level, message, threadName, throwableInformation,
                                null, locationInfo, null);
    }

    /**
     * Formats the message of a JUL log record.
     *
     * @param record
     *            the log record
     * @return the formatted message
     */
    private String formatMessage(final LogRecord record)
    {
        String message = record.getMessage();

        // look for a resource bundle and get the real message from it
        final ResourceBundle bundle = record.getResourceBundle();
        if (bundle != null)
        {
            try
            {
                message = bundle.getString(message);
            }
            catch (final MissingResourceException e)
            {
                // keep original message
            }
        }

        // inject any parameters into the message
        final Object parameters[] = record.getParameters();
        if (parameters != null && parameters.length > 0)
        {
            try
            {
                message = MessageFormat.format(message, parameters);
            }
            catch (final IllegalArgumentException e)
            {
                // keep unformatted message
            }
        }

        return message;
    }
}
