package com.xceptance.xlt.report;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.Predicate;

import org.junit.Test;

import com.xceptance.xlt.common.XltConstants;

public class DataRecordReaderTest
{
    /**
     * Ensure that the filter is right and catches all possible values and not the 
     */
    @Test
    public void timerFilter()
    {
        final Predicate<String> p = s -> XltConstants.TIMER_FILENAME_PATTERNS.stream().anyMatch(r -> r.asPredicate().test(s));
        
        assertTrue(p.test("timers.csv"));
        assertTrue(p.test("timers.csv.gz"));
        assertTrue(p.test("timers.csv.2012-01-01"));
        assertTrue(p.test("timers.csv.2012-01-01.gz"));

        assertFalse(p.test("timers-wd-2021.csv"));
        assertFalse(p.test("timers-wd-2020.csv.gz"));
        
        assertFalse(p.test("atimers.csv"));
        assertFalse(p.test("timers.gz"));
        assertFalse(p.test("timers.2020.csv"));
        assertFalse(p.test("Timers.csv"));
    }

    /**
     * Check timers-wd filter
     */
    @Test
    public void timerWDFilter()
    {
        final Predicate<String> p = s -> XltConstants.CPT_TIMER_FILENAME_PATTERNS.stream().anyMatch(r -> r.asPredicate().test(s));
        
        assertTrue(p.test("timers-wd-2020-11-11.csv"));
        assertTrue(p.test("timers-wd-2020.csv.gz"));
        
        assertFalse(p.test("timers.csv"));
        assertFalse(p.test("timers.csv.gz"));
        assertFalse(p.test("timers.csv.2012-01-01"));
        assertFalse(p.test("timers.csv.2012-01-01.gz"));

        assertFalse(p.test("timers-wd.csv"));
        assertFalse(p.test("timers-wd.csv.gz"));
    }
}
