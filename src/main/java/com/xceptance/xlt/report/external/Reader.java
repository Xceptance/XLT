/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.external;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.api.report.external.AbstractLineParser;
import com.xceptance.xlt.api.report.external.ValueSet;
import com.xceptance.xlt.report.AbstractReader;
import com.xceptance.xlt.report.external.converter.AbstractDataConverter;

/**
 * Simple file reader that defines basic functionality for parsing a given file.
 *
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class Reader extends AbstractReader<ValueSet>
{
    private static final Logger LOG = LoggerFactory.getLogger(Reader.class);

    /**
     * File name
     */
    private final String fileName;

    /**
     * Encoding type (see chapter '<code>Standard charsets</code>' in {@link java.nio.charset.Charset})
     */
    private final String encoding;

    private final AbstractLineParser parser;

    private AbstractDataConverter converter;

    /**
     * Create a reader instance.
     *
     * @param fileName
     *            file name
     * @param encoding
     *            encoding type (see chapter '<code>Standard charsets</code>' in {@link java.nio.charset.Charset})
     * @param parser
     *            the external data parser to be used for the given file
     */
    public Reader(final String fileName, final String encoding, final AbstractLineParser parser)
    {
        super("ExternalDataReader");

        ParameterCheckUtils.isNonEmptyString(fileName, "fileName");
        ParameterCheckUtils.isNonEmptyString(encoding, "encoding");

        this.fileName = fileName;
        this.encoding = encoding;
        this.parser = parser;
    }

    /**
     * @param converter
     */
    public void setConverter(final AbstractDataConverter converter)
    {
        if (this.converter == null)
        {
            this.converter = converter;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ValueSet processLine(final String line)
    {
        try
        {
            return parser.parse(line);
        }
        catch (final Exception ex)
        {
            if (LOG.isErrorEnabled())
            {
                LOG.error(String.format("Failed to read file '%s': %s\n", getFileName(), ex.getMessage()));
            }

            System.out.println("FAILED: " + ex.getMessage());
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processLineResult(final ValueSet point)
    {
        final long time = point.getTime();
        try
        {
            converter.parse(time, point.getValues());
        }
        catch (final Exception e)
        {
            LOG.warn("Failed to parse external data", e);
            System.err.println("\nFailed to parse external data: " + e);
        }
    }

    /**
     * Reads the external data from the configured file.
     */
    public void readData()
    {
        setOverallStartTime(GlobalClock.millis());

        try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(getFileName()),
                                                                                            getEncoding())))
        {
            // System.out.printf("Reading file '%s' ...\n", VFS.getManager().resolveFile(getFileName()));

            read(bufferedReader);

            // wait for the data processor thread to empty the queue
            waitForDataRecordProcessingToComplete();
        }
        catch (final Exception e)
        {
            System.out.println("Failed to read data records: " + e.getMessage());
            LOG.error("Failed to read data records", e);
        }
        finally
        {
            cleanUp();
        }
    }

    /**
     * Get the file name.
     *
     * @return file name
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * Get the file encoding.
     *
     * @return file encoding
     */
    public String getEncoding()
    {
        return encoding;
    }
}
