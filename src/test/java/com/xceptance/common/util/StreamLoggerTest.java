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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.AbstractXLTTestCase;
import com.xceptance.xlt.api.util.XltRandom;

/**
 * Test the implementation of {@link StreamLogger}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class StreamLoggerTest extends AbstractXLTTestCase
{
    /**
     * Constant representing an ASCII encoded line feed (LF) character as decimal value.
     */
    protected final static byte LF = (byte) 0xa;

    /**
     * Constant representing an ASCII encoded carriage return (CR) character as decimal value.
     */
    protected final static byte CR = (byte) 0xd;

    /**
     * Output stream used by writer appender.
     */
    protected OutputStream os = null;

    /**
     * Input stream.
     */
    protected InputStream is = null;

    /**
     * Writer appender used to grab log output.
     */
    protected WriterAppender appender = null;

    /**
     * Stream logger test instance.
     */
    protected StreamLogger streamLogger = null;

    /**
     * Test data (random content).
     */
    protected final byte[] testData = getTestData();

    /**
     * Test fixture setup.
     * 
     * @throws Exception
     *             thrown when setup failed.
     */
    @Before
    public void intro() throws Exception
    {
        // create input and output streams
        is = new ByteArrayInputStream(testData);
        os = new ByteArrayOutputStream();

        // create new writer appender using output stream
        appender = new WriterAppender(new PatternLayout(), os);

        // reset logging configuration
        Logger.getRootLogger().getLoggerRepository().resetConfiguration();

        // configure writer appender
        BasicConfigurator.configure(appender);
        appender.activateOptions();

        // create stream logger test instance
        streamLogger = new StreamLogger(is, LogFactory.getLog(StreamLogger.class));
    }

    /**
     * Test fixture cleanup.
     * 
     * @throws Exception
     *             thrown when cleanup failed.
     */
    @After
    public void outro()
    {
        // remove writer appender
        Logger.getRootLogger().removeAppender(appender);
        // close appender (underlying output stream gets closed too)
        appender.close();

        // close input stream quietly
        try
        {
            is.close();
        }
        catch (final IOException ie)
        {
            // ignore
        }
    }

    /**
     * Tests the implementation of {@link StreamLogger#run()} by redirecting log output to an output stream.
     */
    @Test
    public void testRun()
    {
        streamLogger.run();
        final byte[] output = filterLF(((ByteArrayOutputStream) os).toByteArray());
        Assert.assertTrue("Output array doesn't contain any element!", output.length > 0);
        Assert.assertEquals(dump(output), testData.length, output.length);

        for (int i = 0; i < testData.length; i++)
        {
            Assert.assertEquals(dump(output), testData[i], output[i]);
        }
    }

    /**
     * Creates and returns the test data.
     * 
     * @return test data
     */
    private byte[] getTestData()
    {
        final byte[] testData = new byte[1 + XltRandom.nextInt(99)];
        for (int i = 0; i < testData.length; i++)
        {
            byte randByte = (byte) -1;
            do
            {
                randByte = (byte) XltRandom.nextInt(Byte.MAX_VALUE);
            }
            while (randByte == LF || randByte == CR);

            testData[i] = randByte;
        }

        return testData;
    }

    /**
     * Filters LFs from given byte array.
     * 
     * @param in
     *            array of bytes that potentially contains one or more LFs
     * @return passed input array excluding any LFs
     */
    private byte[] filterLF(final byte[] in)
    {
        // parameter validation
        if (in == null)
        {
            return null;
        }
        // use vector of Byte to store non-LF elements
        final Vector<Byte> elements = new Vector<Byte>();
        for (int i = 0; i < in.length; i++)
        {
            if (in[i] != LF && in[i] != CR)
            {
                elements.add(in[i]);
            }
        }

        // copy Bytes to byte array
        final byte[] out = new byte[elements.size()];
        for (int i = 0; i < out.length; i++)
        {
            out[i] = elements.get(i).byteValue();
        }

        // return filtered array
        return out;
    }

    private String dump(final byte[] actual)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append("Expected [ ").append(testData[0]);
        for (int i = 1; i < testData.length; i++)
        {
            sb.append(", ").append(testData[i]);
        }
        sb.append(" ], actual [ ").append(actual[0]);
        for (int i = 1; i < actual.length; i++)
        {
            sb.append(", ").append(actual[i]);
        }

        return sb.toString();
    }
}
