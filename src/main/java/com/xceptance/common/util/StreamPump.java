/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The StreamPump is responsible for asynchronously copying the content of a stream to another stream. This class is
 * most useful for transferring the output of a sub process so that the output's buffer does not fill completely and
 * block the process (because otherwise it would block sooner or later).
 * <p>
 * Note that both streams are closed once the source stream is exhausted.
 * </p>
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class StreamPump extends Thread
{
    /**
     * Class logger.
     */
    private static final Logger log = LoggerFactory.getLogger(StreamPump.class);

    /**
     * The source stream.
     */
    private final InputStream in;

    /**
     * The target stream.
     */
    private final OutputStream out;

    /**
     * Creates a new StreamPump object and initializes it with the given source and target streams.
     * 
     * @param in
     *            the source stream
     * @param out
     *            the target stream
     */
    public StreamPump(final InputStream in, final OutputStream out)
    {
        this.in = in;
        this.out = out;
    }

    /**
     * Creates a new StreamPump object and initializes it with the given source stream. The target stream is created
     * from the specified file.
     * 
     * @param in
     *            the source stream
     * @param file
     *            the target file
     * @throws FileNotFoundException
     *             if the target file is not valid
     */
    public StreamPump(final InputStream in, final File file) throws FileNotFoundException
    {
        this(in, new FileOutputStream(file));
    }

    /**
     * Creates a new StreamPump object and initializes it with the given source stream. The target stream is created
     * from the specified file name.
     * 
     * @param in
     *            the source stream
     * @param fileName
     *            the name of the target file
     * @throws FileNotFoundException
     *             if the target file name does not specify a valid file
     */
    public StreamPump(final InputStream in, final String fileName) throws FileNotFoundException
    {
        this(in, new FileOutputStream(fileName));
    }

    /**
     * Reads the source stream block-wise and writes the data read to the target stream.
     */
    @Override
    public void run()
    {
        try
        {
            IOUtils.copy(in, out);
        }
        catch (final IOException ex)
        {
            log.error("Error while copying stream:", ex);
        }
        finally
        {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }
}
