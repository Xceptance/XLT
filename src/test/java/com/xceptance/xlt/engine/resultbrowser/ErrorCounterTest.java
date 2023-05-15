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
package com.xceptance.xlt.engine.resultbrowser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.XltEngine;
import com.xceptance.xlt.util.XltPropertiesImpl;

public class ErrorCounterTest
{
    @Test
    public void singleton() throws Exception
    {
        assertSame(ErrorCounter.get(), ErrorCounter.get());
    }

    /**
     * Checks default settings.
     */
    @Test
    public void defaults()
    {
        var p = new XltPropertiesImpl();
        p.removeProperty(ErrorCounter.MAX_DIFFERENT_ERRORS_PROPERTY);
        p.removeProperty(ErrorCounter.MAX_DUMP_COUNT_PROPERTY);
        p.removeProperty(ErrorCounter.COUNTER_RESET_INTERVAL_PROPERTY);
        XltEngine.reset(p);

        var ec = ErrorCounter.createInstance(p);

        assertEquals(ErrorCounter.MAX_DIFFERENT_ERRORS_DEFAULT, ec.getMaxDifferentErrors());
        assertEquals(ErrorCounter.MAX_DUMPS_PER_ERROR_DEFAULT, ec.getMaxDumpCount());
        assertEquals(ErrorCounter.COUNTER_RESET_INTERVAL_DEFAULT * 1000, ec.getResetInterval());
        assertEquals(0, ec.getDifferentErrorCount());
    }

    /**
     * Standard way of getting it and setting it up. We cannot simulate that yet, due to the non-resetable singleton
     * structure. This has to run as a fork of the VM instead. This is not doable with JUnit4, maybe later with 5.
     *
     * @throws Exception
     */
    @Test
    public void happyPath() throws Exception
    {
        var p = new XltPropertiesImpl();
        p.setProperty(ErrorCounter.MAX_DIFFERENT_ERRORS_PROPERTY, "101");
        p.setProperty(ErrorCounter.COUNTER_RESET_INTERVAL_PROPERTY, "41s");
        p.setProperty(ErrorCounter.MAX_DUMP_COUNT_PROPERTY, "42");
        XltEngine.reset(p);

        assertEquals(0, ErrorCounter.get().getDifferentErrorCount());
        assertEquals(41000, ErrorCounter.get().getResetInterval());
        assertEquals(42, ErrorCounter.get().getMaxDumpCount());
        assertEquals(101, ErrorCounter.get().getMaxDifferentErrors());

        // ask without errors
        assertFalse(ErrorCounter.get().countDumpIfOpen(new TestSession()));
        assertEquals(0, ErrorCounter.get().getDifferentErrorCount());

        // ask with errors
        var t1 = getStacktrace2("Error1");
        assertTrue(ErrorCounter.get().countDumpIfOpen(new TestSession("A", t1)));
        assertEquals(1, ErrorCounter.get().getDifferentErrorCount());

        // ask with same
        assertTrue(ErrorCounter.get().countDumpIfOpen(new TestSession("A", t1)));
        assertEquals(1, ErrorCounter.get().getDifferentErrorCount());

        // ask with different data
        assertTrue(ErrorCounter.get().countDumpIfOpen(new TestSession("A", t1.setMessage("Error2"))));
        assertEquals(2, ErrorCounter.get().getDifferentErrorCount());

        assertTrue(ErrorCounter.get().countDumpIfOpen(new TestSession("B", t1)));
        assertEquals(3, ErrorCounter.get().getDifferentErrorCount());

        var t2 = getStacktrace2("Error1");
        assertTrue(ErrorCounter.get().countDumpIfOpen(new TestSession("A", t2)));
        assertEquals(4, ErrorCounter.get().getDifferentErrorCount());

        assertTrue(ErrorCounter.get().countDumpIfOpen(new TestSession("B", t2.setMessage("Error2"))));
        assertEquals(5, ErrorCounter.get().getDifferentErrorCount());
    }

    /**
     * Different Errors Limit
     */
    @Test
    public void limitDifferentErrors()
    {
        var p = new XltPropertiesImpl();
        p.setProperty(ErrorCounter.MAX_DIFFERENT_ERRORS_PROPERTY, "2");
        p.removeProperty(ErrorCounter.COUNTER_RESET_INTERVAL_PROPERTY);
        p.removeProperty(ErrorCounter.MAX_DUMP_COUNT_PROPERTY);
        XltEngine.reset(p);

        var ec = ErrorCounter.createInstance(p);

        assertEquals(ErrorCounter.COUNTER_RESET_INTERVAL_DEFAULT * 1000, ec.getResetInterval());
        assertEquals(ErrorCounter.MAX_DUMPS_PER_ERROR_DEFAULT, ec.getMaxDumpCount());
        assertEquals(2, ec.getMaxDifferentErrors());

        assertEquals(0, ec.getDifferentErrorCount());

        // ask without errors
        assertFalse(ec.countDumpIfOpen(new TestSession()));
        assertEquals(0, ec.getDifferentErrorCount());

        // ask with errors
        var t1 = getStacktrace2("Error1");
        assertTrue(ec.countDumpIfOpen(new TestSession("A", t1)));
        assertEquals(1, ec.getDifferentErrorCount());

        assertTrue(ec.countDumpIfOpen(new TestSession("B", t1)));
        assertEquals(2, ec.getDifferentErrorCount());

        // limit reached
        assertFalse(ec.countDumpIfOpen(new TestSession("C", t1)));
        assertEquals(2, ec.getDifferentErrorCount());

        // same is ok again
        assertTrue(ec.countDumpIfOpen(new TestSession("A", t1)));
        assertEquals(2, ec.getDifferentErrorCount());
    }

    /**
     * Limit per error
     */
    @Test
    public void limitMaxPerError()
    {
        var p = new XltPropertiesImpl();
        p.setProperty(ErrorCounter.MAX_DIFFERENT_ERRORS_PROPERTY, "2");
        p.removeProperty(ErrorCounter.COUNTER_RESET_INTERVAL_PROPERTY);
        p.setProperty(ErrorCounter.MAX_DUMP_COUNT_PROPERTY, "2");
        XltEngine.reset(p);

        var ec = ErrorCounter.createInstance(p);

        assertEquals(ErrorCounter.COUNTER_RESET_INTERVAL_DEFAULT * 1000, ec.getResetInterval());
        assertEquals(2, ec.getMaxDumpCount());
        assertEquals(2, ec.getMaxDifferentErrors());

        assertEquals(0, ec.getDifferentErrorCount());

        // ask without errors
        assertFalse(ec.countDumpIfOpen(new TestSession()));
        assertEquals(0, ec.getDifferentErrorCount());

        // ask with errors
        var t1 = getStacktrace2("Error1");
        assertTrue(ec.countDumpIfOpen(new TestSession("A", t1)));
        assertEquals(1, ec.getDifferentErrorCount());

        assertTrue(ec.countDumpIfOpen(new TestSession("B", t1)));
        assertEquals(2, ec.getDifferentErrorCount());

        // limit reached
        assertFalse(ec.countDumpIfOpen(new TestSession("C", t1)));
        assertEquals(2, ec.getDifferentErrorCount());

        // same is ok again
        assertTrue(ec.countDumpIfOpen(new TestSession("A", t1)));
        assertEquals(2, ec.getDifferentErrorCount());

        // now same is filled up and we stop
        assertFalse(ec.countDumpIfOpen(new TestSession("A", t1)));
        assertEquals(2, ec.getDifferentErrorCount());
    }

    /**
     * reset error limiter
     * 
     * @throws InterruptedException
     */
    @Test
    public void limit() throws InterruptedException
    {
        var p = new XltPropertiesImpl();
        p.setProperty(ErrorCounter.MAX_DIFFERENT_ERRORS_PROPERTY, "2");
        p.setProperty(ErrorCounter.COUNTER_RESET_INTERVAL_PROPERTY, "1s");
        p.setProperty(ErrorCounter.MAX_DUMP_COUNT_PROPERTY, "2");
        XltEngine.reset(p);

        var ec = ErrorCounter.createInstance(p);

        assertEquals(1_000, ec.getResetInterval());
        assertEquals(2, ec.getMaxDumpCount());
        assertEquals(2, ec.getMaxDifferentErrors());

        assertEquals(0, ec.getDifferentErrorCount());

        // ask without errors
        assertFalse(ec.countDumpIfOpen(new TestSession()));
        assertEquals(0, ec.getDifferentErrorCount());

        // ask with errors
        var t1 = getStacktrace2("Error1");
        assertTrue(ec.countDumpIfOpen(new TestSession("A", t1)));
        assertEquals(1, ec.getDifferentErrorCount());

        assertTrue(ec.countDumpIfOpen(new TestSession("B", t1)));
        assertEquals(2, ec.getDifferentErrorCount());

        // limit of different reached
        assertFalse(ec.countDumpIfOpen(new TestSession("C", t1)));
        assertEquals(2, ec.getDifferentErrorCount());

        // same is ok again
        assertTrue(ec.countDumpIfOpen(new TestSession("A", t1)));
        assertEquals(2, ec.getDifferentErrorCount());

        // now same is filled up and we stop
        assertFalse(ec.countDumpIfOpen(new TestSession("A", t1)));
        assertEquals(2, ec.getDifferentErrorCount());

        // wait till we expire
        Thread.sleep(1500);

        // Ok, the elements are gone now, we can add the same or new errors
        assertEquals(0, ec.getDifferentErrorCount());

        // do the same again to see if the logic still works

        // ask with errors
        assertTrue(ec.countDumpIfOpen(new TestSession("A", t1)));
        assertEquals(1, ec.getDifferentErrorCount());

        assertTrue(ec.countDumpIfOpen(new TestSession("B", t1)));
        assertEquals(2, ec.getDifferentErrorCount());

        // limit of different reached
        assertFalse(ec.countDumpIfOpen(new TestSession("C", t1)));
        assertEquals(2, ec.getDifferentErrorCount());

        // same is ok again
        assertTrue(ec.countDumpIfOpen(new TestSession("A", t1)));
        assertEquals(2, ec.getDifferentErrorCount());

        // now same is filled up and we stop
        assertFalse(ec.countDumpIfOpen(new TestSession("A", t1)));
        assertEquals(2, ec.getDifferentErrorCount());

        // wait till we expire
        Thread.sleep(1500);

        // empty
        assertEquals(0, ec.getDifferentErrorCount());

        // I can also add something totally new now
        assertTrue(ec.countDumpIfOpen(new TestSession("D", t1)));
        assertEquals(1, ec.getDifferentErrorCount());
    }

    @Test
    public void off()
    {
        var p = new XltPropertiesImpl();
        p.setProperty(ErrorCounter.MAX_DIFFERENT_ERRORS_PROPERTY, "-1");
        p.removeProperty(ErrorCounter.COUNTER_RESET_INTERVAL_PROPERTY);
        p.removeProperty(ErrorCounter.MAX_DUMP_COUNT_PROPERTY);
        XltEngine.reset(p);

        var ec = ErrorCounter.createInstance(p);

        assertEquals(0, ec.getDifferentErrorCount());
        assertEquals(ErrorCounter.COUNTER_RESET_INTERVAL_DEFAULT * 1000, ec.getResetInterval());
        assertEquals(ErrorCounter.MAX_DUMPS_PER_ERROR_DEFAULT, ec.getMaxDumpCount());
        assertEquals(-1, ec.getMaxDifferentErrors());

        // ask without errors
        assertTrue(ec.countDumpIfOpen(new TestSession()));
        assertEquals(0, ec.getDifferentErrorCount());

        // ask with errors
        var t1 = getStacktrace2("Error1");
        assertTrue(ec.countDumpIfOpen(new TestSession("A", t1)));
        assertEquals(0, ec.getDifferentErrorCount());
    }

    /**
     * Test the error key generation: No Throwable avaialble
     */
    @Test
    public void errorKeyNoThrowable()
    {
        var key1 = ErrorCounter.getErrorKey("Test1", null);
        var key2 = ErrorCounter.getErrorKey("Test2", null);
        assertNotEquals(key1, key2);
    }

    /**
     * Test the error key generation: Same exception and text
     */
    @Test
    public void errorKeyThrowable1()
    {
        var t = getStacktrace1("");
        var key1 = ErrorCounter.getErrorKey("Test1", t);
        var key2 = ErrorCounter.getErrorKey("Test1", t);
        assertEquals(key1, key2);
    }

    /**
     * Test the error key generation: Same exception and text
     */
    @Test
    public void errorKeyThrowableDifferentMessage()
    {
        var t = getStacktrace2("");
        var key1 = ErrorCounter.getErrorKey("Test1", t.setMessage("Msg1"));
        var key2 = ErrorCounter.getErrorKey("Test1", t.setMessage("Msg1"));
        assertEquals(key1, key2);

        var key3 = ErrorCounter.getErrorKey("Test1", t.setMessage("Msg2"));
        assertNotEquals(key1, key3);
    }

    /**
     * Get us a stracktrace
     */
    private Throwable getStacktrace1(String reason)
    {
        return new AssertionError(reason);
    }

    /**
     * Get us a stracktrace
     */
    private TestException getStacktrace2(String reason)
    {
        return new TestException(reason);
    }

    class TestException extends Throwable
    {
        String detailMessage;

        public TestException(String detailMessage)
        {
            this.detailMessage = detailMessage;
        }

        @Override
        public String getMessage()
        {
            return detailMessage;
        }

        public TestException setMessage(String detailMessage)
        {
            this.detailMessage = detailMessage;
            return this;
        }
    }

    /**
     * Mock for the session and our verifier
     * 
     * @author rschwietzke
     */
    class TestSession extends SessionImpl
    {
        private final String userName;

        private final Throwable t;

        private final boolean failed;

        public TestSession(final String userName, Throwable t)
        {
            super();
            this.userName = userName;
            this.t = t;
            this.failed = true;
        }

        public TestSession()
        {
            super();
            this.userName = "UNKNOWN";
            this.t = null;
            this.failed = false;
        }

        @Override
        public String getUserName()
        {
            return userName;
        }

        @Override
        public boolean hasFailed()
        {
            return failed;
        }

        @Override
        public Throwable getFailReason()
        {
            return t;
        }
    }
}
