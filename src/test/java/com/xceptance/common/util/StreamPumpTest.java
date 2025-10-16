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
package com.xceptance.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the implementation of {@link StreamPump}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class StreamPumpTest
{
    /**
     * Tests the implementation of {@link StreamPump#StreamPump(InputStream, String)} by passing an invalid file name.
     */
    @Test(expected = FileNotFoundException.class)
    public void testInit_InvalidOutputFileName() throws Throwable
    {
        new StreamPump(null, System.getProperty("java.io.tmpdir"));
    }

    /**
     * Tests the implementation of {@link StreamPump#StreamPump(InputStream, File)} by passing an invalid file object.
     */
    @Test(expected = FileNotFoundException.class)
    public void testInit_InvalidOutputFile() throws Throwable
    {
        new StreamPump(null, new File(System.getProperty("java.io.tmpdir")));
    }

    /**
     * Tests the implementation of {@link StreamPump#run()} by passing null a reference to
     * {@link StreamPump#StreamPump(InputStream, OutputStream)}.
     */
    public void testRun_inIsNull() throws Throwable
    {
        new StreamPump(null, new ByteArrayOutputStream()).run();
    }

    /**
     * Tests the implementation of {@link StreamPump#run()} by passing a null reference to
     * {@link StreamPump#StreamPump(InputStream, OutputStream)}.
     */
    @Test(expected = NullPointerException.class)
    public void testRun_outIsNull() throws Throwable
    {
        new StreamPump(new ByteArrayInputStream("foo".getBytes()), (OutputStream) null).run();
    }

    /**
     * Tests the implementation of {@link StreamPump#run()}.
     */
    @Test
    public void testRun() throws Throwable
    {
        final String testString = "Test STriNg";
        final InputStream in = new ByteArrayInputStream(testString.getBytes());
        final OutputStream out = new ByteArrayOutputStream();

        new StreamPump(in, out).run();

        Assert.assertEquals(testString, ((ByteArrayOutputStream) out).toString());
    }
}
