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
package com.xceptance.xlt.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;
import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.engine.metrics.Metrics;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 * This test uses an in memory file sytem for easier testing. It does not (at the moment)
 * simulate any kind of exception or issues when writing data.
 *
 * @author rschwietzke
 *
 */
public class DataManagerImplTest
{
    FileSystem fs;
    Path userDir;
    TestMetrics metrics;

    @Before
    public void setup() throws IOException
    {
        fs = MemoryFileSystemBuilder.newEmpty().build();
        userDir = fs.getPath("testuser");
        Files.createDirectory(userDir);

        metrics = new TestMetrics();
    }

    @After
    public void after() throws IOException
    {
        fs.close();
        fs = null;

        GlobalClock.reset();
    }

    /**
     * Logging period
     */
    @Test
    public void loggingPeriod() throws IOException
    {
        var session = new TestSession("TName");
        var dm = new DataManagerImpl(session, metrics);

        dm.setStartOfLoggingPeriod(8000L);
        dm.setEndOfLoggingPeriod(9000L);

        assertEquals(8000L, dm.getStartOfLoggingPeriod());
        assertEquals(9000L, dm.getEndOfLoggingPeriod());

        // before
        GlobalClock.installFixed(7999L);
        dm.logEvent("Before", "Just a message1");

        // start
        GlobalClock.installFixed(8000L);
        dm.logEvent("Start", "Just a message1");

        // in
        GlobalClock.installFixed(8500L);
        dm.logEvent("In", "Just a message1");

        // end
        GlobalClock.installFixed(9000L);
        dm.logEvent("End", "Just a message1");

        // after
        GlobalClock.installFixed(9001L);
        dm.logEvent("After", "Just a message1");

        assertEquals(3, dm.getNumberOfEvents());

        verify(dm.getTimerFile(),
               3,
               List.of(
                       "E,Start,8000,TName,Just a message1",
                       "E,In,8500,TName,Just a message1",
                       "E,End,9000,TName,Just a message1"
                   ));
    }

    /**
     * Logging enabled and disabled
     */
    @Test
    public void loggingOnAndOff() throws IOException
    {
        GlobalClock.installFixed(1666646047921L);

        var session = new TestSession("TName");
        var dm = new DataManagerImpl(session, metrics);

        dm.enableLogging();
        assertTrue(dm.isLoggingEnabled());
        dm.logEvent("EventName1", "Just a message1");
        assertTrue(dm.isLoggingEnabled());

        dm.disableLogging();
        assertFalse(dm.isLoggingEnabled());
        dm.logEvent("EventName2", "Just a message2");
        assertFalse(dm.isLoggingEnabled());

        dm.enableLogging();
        assertTrue(dm.isLoggingEnabled());
        dm.logEvent("EventName3", "Just a message3");
        assertTrue(dm.isLoggingEnabled());

        assertEquals(2, dm.getNumberOfEvents());

        verify(dm.getTimerFile(),
               2,
               List.of(
                       "E,EventName1,1666646047921,TName,Just a message1",
                       "E,EventName3,1666646047921,TName,Just a message3"
                   ));
    }

    /**
     * Logging enabled and disabled
     */
    @Test
    public void loggingOnAndOff_Legacy() throws IOException
    {
        GlobalClock.installFixed(1666646047921L);

        var session = new TestSession("TName");
        var dm = new DataManagerImpl(session, metrics);

        dm.setLoggingEnabled(true);
        assertTrue(dm.isLoggingEnabled());
        dm.logEvent("EventName1", "Just a message1");
        assertTrue(dm.isLoggingEnabled());

        dm.setLoggingEnabled(false);
        assertFalse(dm.isLoggingEnabled());
        dm.logEvent("EventName2", "Just a message2");
        assertFalse(dm.isLoggingEnabled());

        dm.setLoggingEnabled(true);
        assertTrue(dm.isLoggingEnabled());
        dm.logEvent("EventName3", "Just a message3");
        assertTrue(dm.isLoggingEnabled());

        assertEquals(2, dm.getNumberOfEvents());

        verify(dm.getTimerFile(),
               2,
               List.of(
                       "E,EventName1,1666646047921,TName,Just a message1",
                       "E,EventName3,1666646047921,TName,Just a message3"
                   ));
    }


    /**
     * Logging enabled and disabled
     */
    @Test
    public void close() throws IOException
    {
        GlobalClock.installFixed(1666646047921L);

        var session = new TestSession("TName");
        var dm = new DataManagerImpl(session, metrics);

        dm.logEvent("EventName1", "Just a message1");

        dm.close();

        dm.logEvent("EventName3", "Just a message3");

        assertEquals(2, dm.getNumberOfEvents());

        verify(dm.getTimerFile(),
               2,
               List.of(
                       "E,EventName1,1666646047921,TName,Just a message1",
                       "E,EventName3,1666646047921,TName,Just a message3"
                   ));
    }

    /**
     * We log events
     * @throws IOException
     */
    @Test
    public void logEvent() throws IOException
    {
        GlobalClock.installFixed(1666646047921L);

        var session = new TestSession("TName");
        var dm = new DataManagerImpl(session, metrics);

        dm.logEvent("EventName", "Just a message");

        assertEquals(1, dm.getNumberOfEvents());

        verify(dm.getTimerFile(),
               1,
               List.of(
                   "E,EventName,1666646047921,TName,Just a message"));
    }

    /**
     * We log events and don't need a metrics target
     *
     * @throws IOException
     */
    @Test
    public void nullMetrics() throws IOException
    {
        GlobalClock.installFixed(1666646047921L);

        var session = new TestSession("TName");
        var dm = new DataManagerImpl(session, null);

        dm.logEvent("EventName", "Just a message");

        assertEquals(1, dm.getNumberOfEvents());

        verify(dm.getTimerFile(),
               1,
               List.of(
                   "E,EventName,1666646047921,TName,Just a message"));
    }

    /**
     * We log events and don't need a metrics target
     *
     * @throws IOException
     */
    @Test
    public void simpleConstructor() throws IOException
    {
        GlobalClock.installFixed(1666646047921L);

        var session = new TestSession("TName");
        var dm = new DataManagerImpl(session);

        dm.logEvent("EventName", "Just a message");

        assertEquals(1, dm.getNumberOfEvents());

        verify(dm.getTimerFile(),
               1,
               List.of(
                   "E,EventName,1666646047921,TName,Just a message"));
    }

    /**
     * We log data but only count events
     *
     * @throws IOException
     */
    @Test
    public void dontCountOtherThanEvent() throws IOException
    {
        var session = new TestSession("TName");
        var dm = new DataManagerImpl(session, metrics);

        var c = new CustomData("Custom");
        c.setTime(1000L);

        dm.logDataRecord(c);

        assertEquals(0, dm.getNumberOfEvents());

        verify(dm.getTimerFile(),
               1,
               List.of("C,Custom,1000,0,false"));
    }

    /**
     * We append events
     * @throws IOException
     */
    @Test
    public void logEventAppend() throws IOException
    {
        var session = new TestSession("TName");
        var dm = new DataManagerImpl(session, metrics);

        GlobalClock.installFixed(1666646047921L);
        dm.logEvent("EventName1", "M1");

        GlobalClock.installFixed(1666646047922L);
        dm.logEvent("EventName2", "M2");

        assertEquals(2, dm.getNumberOfEvents());

        session.clear();

        session = new TestSession("TName");
        dm = new DataManagerImpl(session, metrics);

        GlobalClock.installFixed(1666646047923L);
        dm.logEvent("EventName3", "M3");

        GlobalClock.installFixed(1666646047924L);
        dm.logEvent("EventName4", "M4");

        assertEquals(2, dm.getNumberOfEvents());

        session.clear();

        verify(dm.getTimerFile(),
               4, List.of(
                          "E,EventName1,1666646047921,TName,M1",
                          "E,EventName2,1666646047922,TName,M2",
                          "E,EventName3,1666646047923,TName,M3",
                          "E,EventName4,1666646047924,TName,M4"
                   ));
    }


    /**
     * We count events
     */
    @Test
    public void numberOfEvents()
    {
        var session = new TestSession("TName");
        var dm = new DataManagerImpl(session, metrics);

        GlobalClock.installFixed(1666646047921L);
        dm.logEvent("EventName1", "M1");

        GlobalClock.installFixed(1666646047922L);
        dm.logEvent("EventName2", "M2");

        assertEquals(2, dm.getNumberOfEvents());
    }

    /**
     * Direct logging of a data record
     * @throws IOException
     */
    @Test
    public void directLogging() throws IOException
    {
        var session = new TestSession("TName");
        var dm = new DataManagerImpl(session, metrics);

        var e = new EventData();
        e.setTime(1000L);
        e.setName("EName");
        e.setTestCaseName("TName");
        e.setMessage("Message Test");

        dm.logDataRecord(e);

        session.clear();

        verify(dm.getTimerFile(),
               1, List.of(
                          "E,EName,1000,TName,Message Test"
                   ));
    }

    /**
     * Test replace of line seperators
     */
    @Test
    public void replaceLFAndCR()
    {
        // inplace!
        final var sb1 = new StringBuilder("abc");
        assertSame(sb1, DataManagerImpl.removeLineSeparators(sb1, '#'));

        final var sb2 = new StringBuilder("a\nc");
        assertSame(sb2, DataManagerImpl.removeLineSeparators(sb2, '#'));

        // check result
        assertEquals("abc", DataManagerImpl.removeLineSeparators(new StringBuilder("abc"), '#').toString());
        assertEquals("", DataManagerImpl.removeLineSeparators(new StringBuilder(""), '#').toString());
        assertEquals("#", DataManagerImpl.removeLineSeparators(new StringBuilder("\n"), '#').toString());
        assertEquals("#", DataManagerImpl.removeLineSeparators(new StringBuilder("\r"), '#').toString());
        assertEquals("##", DataManagerImpl.removeLineSeparators(new StringBuilder("\r\n"), '#').toString());
        assertEquals("a##c", DataManagerImpl.removeLineSeparators(new StringBuilder("a\r\nc"), '#').toString());
    }
    
    /**
     * Mock for the metrics
     * @author rschwietzke
     *
     */
    class TestMetrics extends Metrics
    {
        public int callCount = 0;

        public void updateMetrics(final Data data)
        {
            callCount++;
        }
    }

    /**
     * Mock for the session and our verifier
     * @author rschwietzke
     *
     */
    class TestSession extends SessionImpl
    {
        private final String userName;
        private Path outputPath;

        public TestSession(final String userName)
        {
            super(new XltPropertiesImpl());
            this.userName = userName;
        }

        @Override
        public Path getResultsDirectory()
        {
            outputPath = userDir.resolve(userName);
            try
            {
                Files.createDirectories(outputPath);
            }
            catch (IOException e)
            {
                System.err.println(e);
            }

            return outputPath;
        }

        public String getUserName()
        {
            return userName;
        }
    }

    public static void verify(Path file, int lineCount, List<String> content) throws IOException
    {
        // created timers.csv
        assertTrue(Files.exists(file));

        // expected content
        var lines = Files.readAllLines(file);

        assertEquals(lineCount, lines.size());

        for (int i = 0; i < lines.size(); i++)
        {
            assertEquals(content.get(i), lines.get(i));
        }
    }
}
