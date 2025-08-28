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
package com.xceptance.xlt.engine;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.xceptance.common.io.FileUtils;
import com.xceptance.xlt.api.engine.DataLogger;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.report.providers.CustomLogsReportProvider;

public class DataLoggerImpl implements DataLogger
{
    /**
     * Back-reference to session using this data logger.
     */
    private final Session session;
    
    private volatile BufferedWriter logger;
    
    private String filename;
    
    private String extension;
    
    protected DataLoggerImpl(final Session session, String scope)
    {
        this.session = session;
        this.filename = XltConstants.CUSTOM_LOG_PREFIX + scope; 
        this.extension = "log";
    }
    
    protected DataLoggerImpl(final Session session, String scope, String extension)
    {
        this.session = session;
        this.filename = XltConstants.CUSTOM_LOG_PREFIX + scope; 
        this.extension = extension;
    }    

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHeader(String header)
    {
        Path file = getLoggerFile();
        try
        {
            if (!Files.exists(file) || Files.size(file) == 0)
            {
                // if there is nothing logged yet we can add a header, otherwise it would be a line of content?
          
                final BufferedWriter writer = logger != null ? logger : getTimerLogger();
   
                // no logger configured -> exit here
                if (writer == null)
                {
                    return;
                }
                
                // write a marker line that tells the CustomLogsReportProvider that this file is using a header
                writer.write(CustomLogsReportProvider.CUSTOM_DATA_HEADER_MARKER);
                writer.write(System.lineSeparator());
   
                // write the header line
                StringBuilder s = new StringBuilder(header);
                s = removeLineSeparators(s, ' ');
                s.append(System.lineSeparator());
                
                writer.write(s.toString());
                writer.flush();
            }
            else
            {
                XltLogger.runTimeLogger.warn("Did not write custom data header because logfile already contains data: " + file);
            }
        }
        catch (final IOException ex)
        {
            XltLogger.runTimeLogger.error("Failed to write custom data header:", ex);
        }      
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExtension(String extension)
    {
        this.extension = extension;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(String lineOfData)
    {
        final BufferedWriter writer = logger != null ? logger : getTimerLogger();

        // no logger configured -> exit here
        if (writer == null)
        {
            return;
        }

        // write the log line
        try
        {
            StringBuilder s = new StringBuilder(lineOfData);
            s = removeLineSeparators(s, ' ');
            s.append(System.lineSeparator());
            
            writer.write(s.toString());
            writer.flush();
        }
        catch (final IOException ex)
        {
            XltLogger.runTimeLogger.error("Failed to write data:", ex);
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
            final Path file = getLoggerFile();

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
     * Returns the logger file for the current session. If it does not exist yet, it will be created.
     *
     * @return logger file
     */
    private Path getLoggerFile()
    {
        // create file handle for new logger file rooted at the session's result directory
        // will create the directory as well!
        final Path dir = session.getResultsDirectory();

        if (dir == null)
        {
            throw new RuntimeException("Missing result dir, see previous exceptions.");
        }

        final Path file = dir.resolve(FileUtils.convertIllegalCharsInFileName(extension == null ? filename : filename + '.' + extension));

        return file;
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
