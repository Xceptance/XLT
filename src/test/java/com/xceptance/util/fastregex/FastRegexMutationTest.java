/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
package com.xceptance.util.fastregex;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for FastRegexMutationTest.
 *
 * @author Gemini
 * @author Rene Schwietzke
 */
public class FastRegexMutationTest
{

    @Test
    public void testMatchSnapshotMutations()
    {
        // hasMatch mutation: return false
        FastRegex.MatchSnapshot matchedSnap = new FastRegex.MatchSnapshot(true, "abc", new int[]{0, 3}, 0);
        assertTrue(matchedSnap.hasMatch());
        assertTrue(matchedSnap.matched());

        FastRegex.MatchSnapshot falseSnap = new FastRegex.MatchSnapshot(false, "abc", new int[]{-1, -1}, 0);
        assertFalse(falseSnap.hasMatch());
        assertFalse(falseSnap.matched());

        // start(0) vs 0 return
        FastRegex.MatchSnapshot offsetSnap = new FastRegex.MatchSnapshot(true, "xyzabc", new int[]{3, 6}, 0);
        assertEquals(3, offsetSnap.start());
        assertEquals(6, offsetSnap.end());

        // boundary checks on group
        assertThrows(IndexOutOfBoundsException.class, () -> matchedSnap.start(1));
        assertThrows(IndexOutOfBoundsException.class, () -> matchedSnap.end(1));
        assertThrows(IndexOutOfBoundsException.class, () -> matchedSnap.group(1));
        assertThrows(IndexOutOfBoundsException.class, () -> matchedSnap.groupView(1));

        // start == end (empty match at index 2)
        FastRegex.MatchSnapshot emptySnap = new FastRegex.MatchSnapshot(true, "abc", new int[]{2, 2}, 0);
        assertEquals("", emptySnap.group());
        assertEquals("", emptySnap.groupView(0).toString());

        // start > end (invalid slice inside slots)
        FastRegex.MatchSnapshot invertedSlotsSnap = new FastRegex.MatchSnapshot(true, "abc", new int[]{3, 2}, 0);
        assertNull(invertedSlotsSnap.group(0));
        assertNull(invertedSlotsSnap.groupView(0));
    }

    @Test
    public void testReusableMatchResultSlotBoundaryAndClearing()
    {
        // Check slots resize and slot filling
        FastRegex.Pattern r = FastRegex.compileStrict("(\\w+)=(\\d+)");
        var res = new FastRegex.ReusableMatchResult(1); // smaller initial capacity
        var m = r.matcher();
        assertTrue(m.matches("a=1", res));
        assertEquals(2, res.groupCount());
        assertEquals("a", res.group(1));
        assertEquals("1", res.group(2));

        // Recheck prepare properly resets previous values
        FastRegex.Pattern rSingle = FastRegex.compileStrict("(\\w+)");
        assertTrue(rSingle.matcher().matches("hello", res));
        assertEquals(1, res.groupCount());
        assertEquals("hello", res.group(1));

        // group bounds checks
        assertThrows(IndexOutOfBoundsException.class, () -> res.start(2));
        assertThrows(IndexOutOfBoundsException.class, () -> res.end(2));
        assertThrows(IndexOutOfBoundsException.class, () -> res.group(2));
        assertThrows(IndexOutOfBoundsException.class, () -> res.groupView(2));

        // check start(0) and end(0)
        FastRegex.Pattern rFind = FastRegex.compileStrict("bar");
        assertTrue(rFind.matcher().find("foobarbaz", res));
        assertEquals(3, res.start());
        assertEquals(6, res.end());
    }

    @Test
    public void testSliceViewLengthCalculation()
    {
        FastRegex.Pattern r = FastRegex.compileStrict("(\\w+)");
        var res = r.newMatchResult();
        assertTrue(r.matcher().matches("testing", res));
        CharSequence v = res.groupView(1);
        assertEquals(7, v.length()); // ensures end - start is computed correctly
        assertNotEquals(0, v.length());
    }

    @Test
    public void testShorthandClassMutations()
    {
        FastRegex.Pattern rW = FastRegex.compileStrict("\\w+");
        var res = rW.newMatchResult();
        // check uppercase, lowercase, digit, underscore
        assertTrue(rW.matcher().matches("A", res));
        assertTrue(rW.matcher().matches("z", res));
        assertTrue(rW.matcher().matches("0", res));
        assertTrue(rW.matcher().matches("9", res));
        assertTrue(rW.matcher().matches("_", res));
        assertFalse(rW.matcher().matches("-", res));

        FastRegex.Pattern rS = FastRegex.compileStrict("\\s+");
        assertTrue(rS.matcher().matches(" ", res));
        assertTrue(rS.matcher().matches("\t", res));
        assertTrue(rS.matcher().matches("\n", res));
        assertTrue(rS.matcher().matches("\r", res));
        assertTrue(rS.matcher().matches("\u000B", res));
        assertTrue(rS.matcher().matches("\f", res));
        assertFalse(rS.matcher().matches("a", res));
    }

    @Test
    public void testMultipleCharacterClassesAllocClass()
    {
        // Ensures allocClass initializes classBits cleanly across multiple classes
        FastRegex.Pattern r = FastRegex.compileStrict("[0-9][a-z][A-Z][0-9]");
        var res = r.newMatchResult();
        assertTrue(r.matcher().matches("1aA2", res));
        assertFalse(r.matcher().matches("11A2", res));
        assertFalse(r.matcher().matches("1a12", res));
    }

    @Test
    public void testClassRangeBoundaries()
    {
        FastRegex.Pattern r = FastRegex.compileStrict("[b-d]");
        var res = r.newMatchResult();
        assertFalse(r.matcher().matches("a", res));
        assertTrue(r.matcher().matches("b", res));
        assertTrue(r.matcher().matches("c", res));
        assertTrue(r.matcher().matches("d", res));
        assertFalse(r.matcher().matches("e", res));
    }

    @Test
    public void testDfaStartExactAndTransitions()
    {
        FastRegex.Pattern r = FastRegex.compileStrict("[a-c]+");
        var m = r.matcher();
        var res = r.newMatchResult();
        // Matches non-empty
        assertTrue(m.matches("abcba", res));
        // Fails with non-matching char
        assertFalse(m.matches("abcd", res));
        // Empty match
        assertFalse(m.matches("", res));
    }

    @Test
    public void testPikeVmExactLengthConstraint()
    {
        // Matches exact requires idx == len
        FastRegex.Pattern r = FastRegex.compileStrict("foo");
        var m = r.matcher();
        var res = r.newMatchResult();
        assertTrue(m.matches("foo", res));
        assertFalse(m.matches("foobar", res));
    }

    @Test
    public void testPrefixLengthOneOptimization()
    {
        FastRegex.Pattern r = FastRegex.compileStrict("x[0-9]+");
        var m = r.matcher();
        var res = r.newMatchResult();
        assertTrue(m.find("aaa x123", res));
        assertEquals(4, res.start());
        assertEquals(8, res.end());
    }

    @Test
    public void testPrefixMultiCharMatchAtEnd()
    {
        FastRegex.Pattern r = FastRegex.compileStrict("cat[0-9]+");
        var m = r.matcher();
        var res = r.newMatchResult();
        // prefix starts close to end
        assertFalse(m.find("the ca", res));
    }

    @Test
    public void testNumberParserZeroAndDigits()
    {
        FastRegex.Pattern r = FastRegex.compileStrict("a{10}");
        var res = r.newMatchResult();
        assertTrue(r.matcher().matches("a".repeat(10), res));
        assertFalse(r.matcher().matches("a".repeat(9), res));
        assertFalse(r.matcher().matches("a".repeat(11), res));
    }

    @Test
    public void testFragDanglingPointersPatch()
    {
        FastRegex.Pattern r = FastRegex.compileStrict("(a|b|c)d");
        var res = r.newMatchResult();
        assertTrue(r.matcher().matches("ad", res));
        assertTrue(r.matcher().matches("bd", res));
        assertTrue(r.matcher().matches("cd", res));
        assertFalse(r.matcher().matches("dd", res));
    }

    @Test
    public void testEolAnchorOnNonEmpty()
    {
        FastRegex.Pattern r = FastRegex.compileStrict("end$");
        var res = r.newMatchResult();
        assertTrue(r.matcher().find("at the end", res));
        assertFalse(r.matcher().find("end of story", res));
    }

    @Test
    public void testBolAnchorOnNonEmpty()
    {
        FastRegex.Pattern r = FastRegex.compileStrict("^start");
        var res = r.newMatchResult();
        assertTrue(r.matcher().find("start here", res));
        assertFalse(r.matcher().find("the start here", res));
    }

    @Test
    public void testZeroLengthBranchAlternation()
    {
        FastRegex.Pattern r = FastRegex.compileStrict("a|b|");
        var res = r.newMatchResult();
        assertTrue(r.matcher().matches("a", res));
        assertTrue(r.matcher().matches("b", res));
        assertTrue(r.matcher().matches("", res));
        assertFalse(r.matcher().matches("c", res));
    }
}
