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
package com.xceptance.xlt.report;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.Predicate;

import org.junit.Test;

import com.xceptance.xlt.common.XltConstants;

public class DataRecordReaderTest
{
    /**
     * Ensures that the filter catches the valid timer file names only.
     */
    @Test
    public void timerFilter()
    {
        final Predicate<String> p = s -> XltConstants.TIMER_FILENAME_PATTERNS.stream().anyMatch(r -> r.asPredicate().test(s));

        assertTrue(p.test("timers.csv"));
        assertTrue(p.test("timers.csv.gz"));
        assertTrue(p.test("timers.csv.2012-01-01"));
        assertTrue(p.test("timers.csv.2012-01-01.gz"));

        assertFalse(p.test("timer-wd-57362576329865634278.csv"));
        assertFalse(p.test("timer-wd-57362576329865634278.csv.gz"));

        assertFalse(p.test("atimers.csv"));
        assertFalse(p.test("timers.gz"));
        assertFalse(p.test("timers.2020.csv"));
        assertFalse(p.test("Timers.csv"));
    }

    /**
     * Ensures that the filter catches the valid CPT timer file names only.
     */
    @Test
    public void timerWDFilter()
    {
        final Predicate<String> p = s -> XltConstants.CPT_TIMER_FILENAME_PATTERNS.stream().anyMatch(r -> r.asPredicate().test(s));

        assertTrue(p.test("timer-wd-57362576329865634278.csv"));
        assertTrue(p.test("timer-wd-57362576329865634278.csv.gz"));

        assertFalse(p.test("timers.csv"));
        assertFalse(p.test("timers.csv.gz"));
        assertFalse(p.test("timers.csv.2012-01-01"));
        assertFalse(p.test("timers.csv.2012-01-01.gz"));

        assertFalse(p.test("timer-wd.csv"));
        assertFalse(p.test("timer-wd.csv.gz"));
    }
}
