package com.xceptance.xlt.engine;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.xceptance.xlt.api.engine.DataLogger;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.common.XltConstants;

public class DataLoggerImpl implements DataLogger
{
    /**
     * Back-reference to session using this data logger.
     */
    private Session session;
    
    private volatile BufferedWriter logger;
    
    private String filename;
    
    private String extension;
    
    protected DataLoggerImpl(Session session, String scope)
    {
        new DataLoggerImpl(session, scope, "csv");
    }
    
    protected DataLoggerImpl(Session session, String scope, String extension)
    {
        this.session = session;
        this.filename = XltConstants.CUSTOM_LOG_PREFIX + scope; 
    }    

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHeader(String header)
    {
        if ("csv".equals(this.extension))
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
       
                    // write the header line - TODO csv validation might be useful here? in case we have csv... whatever
                    // this safes us from synchronization, the writer is already synchronized
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
        else
        {
            XltLogger.runTimeLogger.warn("Did not write custom data header because logfile is not csv format but " + this.extension);
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

        // write the log line - TODO csv validation might be useful here? in case we have csv... whatever
        try
        {
            // this safes us from synchronization, the writer is already synchronized
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

        final Path file = dir.resolve(filename + '.' + extension);

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
