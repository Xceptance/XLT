/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.xlt.common.XltConstants;

/**
 * The StreamLogger is responsible for asynchronously logging the content of a stream to a certain log category. The
 * content is logged line-by-line using the DEBUG log level. This class is most useful for transferring the output of a
 * sub process so that the output's buffer does not fill completely and block the process (because otherwise it would
 * block sooner or later).
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class StreamLogger
{
    /**
     * Class logger.
     */
    private static final Log log = LogFactory.getLog(StreamLogger.class);

    /**
     * The stream to read from.
     */
    private BufferedReader stream;

    /**
     * The logger to write the stream content to.
     */
    private final Log logger;

    /**
     * Creates a new StreamLogger object and initializes it with the given stream and logger.
     * 
     * @param in
     *            the stream
     * @param logger
     *            the logger
     */
    public StreamLogger(final InputStream in, final Log logger)
    {
        try
        {
            stream = new BufferedReader(new InputStreamReader(in, XltConstants.UTF8_ENCODING));
        }
        catch (final UnsupportedEncodingException uee)
        {
            log.warn("Failed to create input stream reader using '" + XltConstants.UTF8_ENCODING + "' encoding!", uee);

            stream = new BufferedReader(new InputStreamReader(in));
        }

        this.logger = logger;
    }

    /**
     * Creates a new StreamLogger object and initializes it with the given stream and the name of the logger to use.
     * 
     * @param in
     *            the stream
     * @param category
     *            the category name
     */

    public StreamLogger(final InputStream in, final String category)
    {
        this(in, LogFactory.getLog(category));
    }

    /**
     * Reads the stream line-by-line and logs the line to the configured logger.
     */
    public void run()
    {
        try
        {
            String line = null;

            while ((line = stream.readLine()) != null)
            {
                logger.debug(line);
            }
        }
        catch (final IOException ex)
        {
            log.error("Error while logging stream:", ex);
        }
    }
}
