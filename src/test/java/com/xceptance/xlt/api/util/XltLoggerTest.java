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
package com.xceptance.xlt.api.util;

import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Testcases to prove the correct functionality of {@link XltLogger}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class XltLoggerTest
{
    /**
     * temporary log4j configuration file
     */
    private File tmpFile;

    /**
     * Test initialization.
     * 
     * @throws Throwable
     */
    @Before
    public void intro() throws Throwable
    {
        tmpFile = File.createTempFile("myLog", ".properties", new File(System.getProperty("java.io.tmpdir")));
        FileUtils.writeLines(tmpFile, Arrays.asList(new String[]
            {
                "log4j.logger.runtime = error", "log4j.additivity.runtime = true"
            }));
    }

    /**
     * Test finalization.
     * 
     * @throws Throwable
     */
    @After
    public void outro()
    {
        FileUtils.deleteQuietly(tmpFile);
    }

    /**
     * Tests the implementation of {@link XltLogger#setupLogging(String)}.
     * 
     * @throws Throwable
     */
    @Test
    public void testSetupLogging()
    {
        // test with non-existent file
        Assert.assertFalse(XltLogger.setupLogging("doesnotexist.txt"));

        // test with prepared file
        Assert.assertTrue(XltLogger.setupLogging(tmpFile.getAbsolutePath()));
        Assert.assertTrue(XltLogger.runTimeLogger.getAdditivity());
        Assert.assertEquals(Level.ERROR, XltLogger.runTimeLogger.getLevel());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInitLogging()
    {
        final Enumeration<Appender> appenders = Logger.getRootLogger().getAllAppenders();
        Logger.getRootLogger().removeAllAppenders();
        XltLogger.initLogging();
        while (appenders.hasMoreElements())
        {
            Logger.getRootLogger().addAppender(appenders.nextElement());
        }
    }
}
