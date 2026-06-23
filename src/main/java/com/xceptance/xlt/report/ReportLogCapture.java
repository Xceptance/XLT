/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report;

import java.io.File;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.LevelRangeFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Manages a programmatic Log4j2 {@link FileAppender} that tees all {@code reportLogger} output to a
 * {@code report.log} file in the report output directory. The existing console logging remains unchanged.
 *
 * <p>Usage:
 * <pre>
 * ReportLogCapture capture = ReportLogCapture.start(outputDir, "INFO");
 * try
 * {
 *     // ... report generation ...
 * }
 * finally
 * {
 *     capture.stop();
 * }
 * </pre>
 */
public class ReportLogCapture
{
    /** Name of the file appender added to the report logger. */
    private static final String APPENDER_NAME = "report-log-file";

    /** Name of the SLF4J/Log4j2 logger to attach to. */
    private static final String LOGGER_NAME = "report";

    /** Output filename inside the report directory. */
    static final String REPORT_LOG_FILENAME = "report.log";

    /** Pattern for each log line: timestamp [level] message */
    private static final String LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%-5level] %msg%n";

    private final FileAppender appender;

    private final LoggerConfig loggerConfig;

    private final LoggerContext context;

    private ReportLogCapture(final FileAppender appender, final LoggerConfig loggerConfig, final LoggerContext context)
    {
        this.appender = appender;
        this.loggerConfig = loggerConfig;
        this.context = context;
    }

    /**
     * Starts capturing report logger output to {@code report.log} in the given directory.
     *
     * @param outputDir
     *            the report output directory
     * @param levelName
     *            minimum log level to capture (e.g. "INFO", "DEBUG")
     * @return a {@link ReportLogCapture} handle — call {@link #stop()} when done
     */
    public static ReportLogCapture start(final File outputDir, final String levelName)
    {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration logConfig = ctx.getConfiguration();

        // Resolve the configured level, defaulting to INFO
        final Level level = Level.toLevel(levelName, Level.INFO);

        // Build the pattern layout with timestamps and level tags
        final PatternLayout layout = PatternLayout.newBuilder()
                                                  .withPattern(LOG_PATTERN)
                                                  .build();

        // Build a level filter so the file appender has its own independent level
        final LevelRangeFilter filter = LevelRangeFilter.createFilter(
            Level.FATAL, level, org.apache.logging.log4j.core.Filter.Result.ACCEPT,
            org.apache.logging.log4j.core.Filter.Result.DENY);

        // Write a header so the log file is never truly empty and explains itself.
        // Written directly to the file (not via the logger) so it stays out of the console.
        final File logFile = new File(outputDir, REPORT_LOG_FILENAME);
        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.FileWriter(logFile), true))
        {
            pw.println("Report generator output to enhance debugging, can be empty.");
            pw.println("===================================================");
        }
        catch (final java.io.IOException ignore)
        {
            // best effort — if the file can't be written, the appender will fail too
        }

        // Build the file appender targeting report.log in the output directory (append to header)
        final FileAppender appender = FileAppender.newBuilder()
                                                  .setName(APPENDER_NAME)
                                                  .withFileName(logFile.getAbsolutePath())
                                                  .setLayout(layout)
                                                  .withAppend(true)
                                                  .setFilter(filter)
                                                  .build();
        appender.start();

        // Attach to the "report" logger
        final LoggerConfig reportLoggerConfig = logConfig.getLoggerConfig(LOGGER_NAME);
        reportLoggerConfig.addAppender(appender, level, filter);
        ctx.updateLoggers();

        return new ReportLogCapture(appender, reportLoggerConfig, ctx);
    }

    /**
     * Stops capturing and removes the file appender from the report logger.
     */
    public void stop()
    {
        loggerConfig.removeAppender(APPENDER_NAME);
        appender.stop();
        context.updateLoggers();
    }
}
