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

import java.util.NoSuchElementException;
import java.util.regex.PatternSyntaxException;


/**
 * Full instruction and branch coverage test suite for {@link FastRegex}.
 *
 * @author Gemini
 * @author Rene Schwietzke
 */
public class FastRegexCoverageTest
{

    @Test
    public void testUnsupportedRegexFeatureExceptionAccessors()
    {
        FastRegex.UnsupportedRegexFeatureException ex =
                new FastRegex.UnsupportedRegexFeatureException("feature", "abc(?=def)", 3);
        assertEquals("abc(?=def)", ex.pattern());
        assertEquals(3, ex.position());
    }

    @Test
    public void testMatchSnapshotFullCoverage()
    {
        FastRegex.Pattern r = FastRegex.compileStrict("(\\w+)=(\\d+)");
        var matcher = r.matcher();
        var res = r.newMatchResult();
        assertTrue(matcher.matches("foo=123", res));

        FastRegex.MatchSnapshot snap = res.snapshot();
        assertTrue(snap.hasMatch());
        assertTrue(snap.matched());
        assertEquals(0, snap.start());
        assertEquals(7, snap.end());
        assertEquals("foo=123", snap.group());
        assertEquals("foo", snap.group(1));
        assertEquals("123", snap.group(2));
        assertNotNull(snap.groupView(1));
        assertEquals("foo", snap.groupView(1).toString());
        assertSame(snap, snap.snapshot());

        // Test exception on unmatched snapshot
        FastRegex.MatchSnapshot unmatchedSnap = new FastRegex.MatchSnapshot(false, "abc", new int[] { -1, -1 }, 0);
        assertThrows(IllegalStateException.class, unmatchedSnap::start);
        assertThrows(IllegalStateException.class, unmatchedSnap::end);
        assertThrows(IllegalStateException.class, unmatchedSnap::group);
        try { unmatchedSnap.group(0); fail("Expected IllegalStateException"); } catch (IllegalStateException expected) {}
        try { unmatchedSnap.start(0); fail("Expected IllegalStateException"); } catch (IllegalStateException expected) {}
        try { unmatchedSnap.end(0); fail("Expected IllegalStateException"); } catch (IllegalStateException expected) {}
        try { unmatchedSnap.groupView(0); fail("Expected IllegalStateException"); } catch (IllegalStateException expected) {}

        // Test invalid group bounds on matched snapshot
        try { snap.start(-1); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { snap.start(99); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { snap.end(-1); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { snap.end(99); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { snap.group(-1); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { snap.group(99); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { snap.groupView(-1); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { snap.groupView(99); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}

        // Snapshot with null input
        FastRegex.MatchSnapshot nullInputSnap = new FastRegex.MatchSnapshot(true, null, new int[] { 0, 2 }, 0);
        assertNull(nullInputSnap.group(0));
        assertNull(nullInputSnap.groupView(0));

        // Snapshot with start == -1 (unmatched group)
        FastRegex.MatchSnapshot unhitGroupSnap = new FastRegex.MatchSnapshot(true, "hello", new int[] { 0, 5, -1, -1 }, 1);
        assertNull(unhitGroupSnap.group(1));
        assertNull(unhitGroupSnap.groupView(1));
    }

    @Test
    public void testReusableMatchResultCoverage()
    {
        var fresh = new FastRegex.ReusableMatchResult();
        assertEquals(0, fresh.groupCount());
        assertFalse(fresh.snapshot().matched());

        FastRegex.Pattern r = FastRegex.compileStrict("(\\w+)");
        var matcher = r.matcher();
        var res = r.newMatchResult();

        assertFalse(res.hasMatch());
        assertFalse(res.matched());
        assertThrows(IllegalStateException.class, res::start);
        assertThrows(IllegalStateException.class, res::end);
        assertThrows(IllegalStateException.class, res::group);
        try { res.group(0); fail("Expected IllegalStateException"); } catch (IllegalStateException expected) {}
        try { res.start(0); fail("Expected IllegalStateException"); } catch (IllegalStateException expected) {}
        try { res.end(0); fail("Expected IllegalStateException"); } catch (IllegalStateException expected) {}

        assertTrue(matcher.matches("test", res));
        assertTrue(res.hasMatch());
        assertTrue(res.matched());
        try { res.start(-1); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { res.start(99); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { res.end(-1); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { res.end(99); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { res.group(-1); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { res.group(99); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { res.groupView(-1); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { res.groupView(99); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}

        // Lazy SliceView array growth when ReusableMatchResult is reused across larger group counts
        var smallRes = new FastRegex.ReusableMatchResult(1);
        assertTrue(matcher.matches("one", smallRes));
        assertEquals("one", smallRes.groupView(0).toString());
        assertEquals("one", smallRes.groupView(0).toString()); // cached SliceView reuse
        FastRegex.Pattern r4 = FastRegex.compileStrict("(a)(b)(c)(d)");
        assertTrue(r4.matcher().matches("abcd", smallRes));
        assertEquals("d", smallRes.groupView(4).toString());
        assertEquals("d", smallRes.groupView(4).toString());
    }

    @Test
    public void testSliceViewCoverageAndBoundsValidation()
    {
        FastRegex.Pattern r = FastRegex.compileStrict("prefix_(\\w+)_suffix");
        var matcher = r.matcher();
        var res = r.newMatchResult();
        assertTrue(matcher.matches("prefix_hello_suffix", res));

        CharSequence view = res.groupView(1);
        assertNotNull(view);
        assertEquals(5, view.length());
        assertEquals('h', view.charAt(0));
        assertEquals('o', view.charAt(4));
        assertEquals("ell", view.subSequence(1, 4).toString());
        assertEquals("", view.subSequence(2, 2).toString());

        // Bounds checks on SliceView.charAt and subSequence
        try { view.charAt(-1); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { view.charAt(5); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { view.subSequence(-1, 2); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { view.subSequence(0, 6); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { view.subSequence(3, 2); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
    }

    @Test
    public void testCompileDefaultFactory()
    {
        FastRegex.Pattern r = FastRegex.compile("[a-z]+");
        assertNotNull(r);
        assertTrue(r.matcher().matches("abc", r.newMatchResult()));
    }

    @Test
    public void testNextCandidateIndexAndPrefixBranches()
    {
        // Pure literal matches and find
        FastRegex.Pattern pure = FastRegex.compileStrict("needle");
        var m = pure.matcher();
        var res = pure.newMatchResult();
        assertTrue(m.matches("needle", res));
        assertFalse(m.matches("nee", res));
        assertFalse(m.matches("xeedle", res));
        // Same length, matching first char, mismatching subsequent char
        assertFalse(m.matches("needlx", res));
        assertTrue(m.find("haystack needle haystack", res));
        assertFalse(m.find("haystack nedle haystack", res));
        // Find fromIndex beyond candidate limit
        assertFalse(m.find("needle", 10, res));

        // Pure literal inside capturing group (totalGroups > 1, so not isPureLiteral fast-path)
        FastRegex.Pattern groupedLiteral = FastRegex.compileStrict("(needle)");
        assertTrue(groupedLiteral.matcher().matches("needle", res));
        assertEquals("needle", res.group(1));
        assertTrue(groupedLiteral.matcher().find("xx needle yy", res));
        assertEquals("needle", res.group(1));

        // Single character prefix on String and StringBuilder
        FastRegex.Pattern singleCharPref = FastRegex.compileStrict("x[0-9]+");
        var m2 = singleCharPref.matcher();
        assertTrue(m2.find("abc x42", res));
        assertFalse(m2.find("abc y42", res));
        assertTrue(m2.find(new StringBuilder("abc x42"), res));
        assertFalse(m2.find(new StringBuilder("abc y42"), res));

        // Non-String CharSequence multi-char prefix
        StringBuilder sb = new StringBuilder("abc needle123");
        FastRegex.Pattern prefRegex = FastRegex.compileStrict("needle([0-9]+)");
        assertTrue(prefRegex.matcher().find(sb, res));
        assertEquals("123", res.groupView(1).toString());

        // Non-String CharSequence where prefix first char matches but subsequent chars differ
        StringBuilder sbFail = new StringBuilder("abc needlx123");
        assertFalse(prefRegex.matcher().find(sbFail, res));

        // fromIndex < 0 or > len
        assertFalse(m.find("needle", -1, res));
        assertFalse(m.find("needle", 99, res));
    }

    @Test
    public void testFallbackMatcherFind()
    {
        FastRegex.Pattern fallback = FastRegex.compile("(?<=start_)(\\w+)");
        assertTrue(fallback.isFallback());
        var m = fallback.matcher();
        var res = fallback.newMatchResult();

        assertTrue(m.find("start_target end", 0, res));
        assertEquals("target", res.group(1));

        assertFalse(m.find("nomatch", 0, res));
    }

    @Test
    public void testPikeVmExactMatchFailureBranches()
    {
        // Anchored pattern forces matches() to execute runPikeVmMatches directly
        FastRegex.Pattern r = FastRegex.compileStrict("^(a)+$");
        var m = r.matcher();
        var res = r.newMatchResult();
        assertFalse(m.matches("b", res));
        assertFalse(m.matches("ba", res));
        assertFalse(m.matches("ab", res));
        assertTrue(m.matches("aa", res));
    }

    @Test
    public void testMatchCursorNoSuchElementException()
    {
        FastRegex.Pattern r = FastRegex.compileStrict("\\d+");
        var m = r.matcher();
        var res = r.newMatchResult();
        var iter = m.findAll("abc", res).iterator();
        assertFalse(iter.hasNext());
        assertThrows(NoSuchElementException.class, iter::next);
    }

    @Test
    public void testStepIdWrapAround()
    {
        for (String pat : new String[] { "a", "a+", "a|b", "^(a|b)$" })
        {
            FastRegex.Pattern r = FastRegex.compileStrict(pat);
            var matcher = r.matcher();
            try
            {
                var res = r.newMatchResult();
                assertTrue(matcher.matches("a", res)); // Warm up visited array
                var field = matcher.getClass().getDeclaredField("stepId");
                field.setAccessible(true);
                field.setInt(matcher, -1); // Next increment gives 0, triggering visited array reset
                assertTrue(matcher.matches("a", res));
            }
            catch (ReflectiveOperationException e)
            {
                fail(e.toString());
            }
        }
    }

    @Test
    public void testLazyDfaNoCapturesFindMisses()
    {
        // Pattern with prefix that finds candidate but DFA fails afterwards
        FastRegex.Pattern r = FastRegex.compileStrict("pref_[0-9]+");
        var m = r.matcher();
        var res = r.newMatchResult();
        assertFalse(m.find("pref_abc other", res));

        // Pattern without prefix where DFA fails
        FastRegex.Pattern r2 = FastRegex.compileStrict("[0-9]{5}");
        var m2 = r2.matcher();
        assertFalse(m2.find("no digits here", res));
    }

    @Test
    public void testEscapeCharactersCoverage()
    {
        // Test \a, \e, \n, \r, \t, \f and punctuation escapes (> 'z' such as \|, \{, \~)
        FastRegex.Pattern r = FastRegex.compileStrict("\\a\\e\\n\\r\\t\\f\\|\\{\\~");
        var res = r.newMatchResult();
        assertTrue(r.matcher().matches("\u0007\u001B\n\r\t\f|{~", res));

        // Test escaped in brackets
        FastRegex.Pattern rClass = FastRegex.compileStrict("[\\a\\e\\n\\r\\t\\f]");
        assertTrue(rClass.matcher().matches("\u0007", res));
        assertTrue(rClass.matcher().matches("\u001B", res));
        assertTrue(rClass.matcher().matches("\n", res));
        assertTrue(rClass.matcher().matches("\r", res));
        assertTrue(rClass.matcher().matches("\t", res));
        assertTrue(rClass.matcher().matches("\f", res));

        // Test escaped range [\\t-\\r]
        FastRegex.Pattern rRange = FastRegex.compileStrict("[\\t-\\r]");
        assertTrue(rRange.matcher().matches("\n", res));

        // Invalid alphabetic escape syntax
        try { FastRegex.compileStrict("\\y"); fail("Expected PatternSyntaxException"); } catch (PatternSyntaxException expected) {}
        try { FastRegex.compileStrict("\\C"); fail("Expected PatternSyntaxException"); } catch (PatternSyntaxException expected) {}
    }

    @Test
    public void testCharClassErrorsAndIntersections()
    {
        // Non-ASCII (> 255) in class
        try { FastRegex.compileStrict("[€]"); fail("Expected FastRegex.UnsupportedRegexFeatureException"); } catch (FastRegex.UnsupportedRegexFeatureException expected) {}
        try { FastRegex.compileStrict("[a-€]"); fail("Expected FastRegex.UnsupportedRegexFeatureException"); } catch (FastRegex.UnsupportedRegexFeatureException expected) {}
        // Trailing backslash in class and after range hyphen
        try { FastRegex.compileStrict("["); fail("Expected PatternSyntaxException"); } catch (PatternSyntaxException expected) {}
        try { FastRegex.compileStrict("[abc\\"); fail("Expected PatternSyntaxException"); } catch (PatternSyntaxException expected) {}
        try { FastRegex.compileStrict("[a-\\"); fail("Expected PatternSyntaxException"); } catch (PatternSyntaxException expected) {}
        try { FastRegex.compileStrict("[a&"); fail("Expected PatternSyntaxException"); } catch (PatternSyntaxException expected) {}

        // Single '&' in character class is valid literal '&'
        FastRegex.Pattern singleAmp = FastRegex.compileStrict("[a&b]+");
        var res = singleAmp.newMatchResult();
        assertTrue(singleAmp.matcher().matches("a&b", res));

        // '&&' intersection and nested '[' trigger UnsupportedRegexFeatureException in strict mode
        try { FastRegex.compileStrict("[a-z&&0-9]"); fail("Expected FastRegex.UnsupportedRegexFeatureException"); } catch (FastRegex.UnsupportedRegexFeatureException expected) {}
        try { FastRegex.compileStrict("[a[b]]"); fail("Expected FastRegex.UnsupportedRegexFeatureException"); } catch (FastRegex.UnsupportedRegexFeatureException expected) {}
        FastRegex.Pattern fbIntersect = FastRegex.compile("[a-z&&0-9]");
        assertTrue(fbIntersect.isFallback());
        assertFalse(fbIntersect.matcher().matches("a", res));
    }

    @Test
    public void testQuantifierBoundedUnrollLimitAndZeroBounds()
    {
        // min = 0, max = 0
        FastRegex.Pattern zeroBound = FastRegex.compileStrict("a{0,0}");
        var res = zeroBound.newMatchResult();
        assertTrue(zeroBound.matcher().matches("", res));

        // bound > 256 (exact, max, open-ended min, and huge number that would overflow 32-bit int)
        try { FastRegex.compileStrict("a{257}"); fail("Expected FastRegex.UnsupportedRegexFeatureException"); } catch (FastRegex.UnsupportedRegexFeatureException expected) {}
        try { FastRegex.compileStrict("a{1,257}"); fail("Expected FastRegex.UnsupportedRegexFeatureException"); } catch (FastRegex.UnsupportedRegexFeatureException expected) {}
        try { FastRegex.compileStrict("a{257,}"); fail("Expected FastRegex.UnsupportedRegexFeatureException"); } catch (FastRegex.UnsupportedRegexFeatureException expected) {}
        try { FastRegex.compileStrict("a{2147483648}"); fail("Expected PatternSyntaxException"); } catch (PatternSyntaxException expected) {}

        // Possessive in bounded and unbounded quantifiers
        try { FastRegex.compileStrict("a{1,3}+"); fail("Expected FastRegex.UnsupportedRegexFeatureException"); } catch (FastRegex.UnsupportedRegexFeatureException expected) {}
        try { FastRegex.compileStrict("a*+"); fail("Expected FastRegex.UnsupportedRegexFeatureException"); } catch (FastRegex.UnsupportedRegexFeatureException expected) {}
        try { FastRegex.compileStrict("a++"); fail("Expected FastRegex.UnsupportedRegexFeatureException"); } catch (FastRegex.UnsupportedRegexFeatureException expected) {}
        try { FastRegex.compileStrict("a?+"); fail("Expected FastRegex.UnsupportedRegexFeatureException"); } catch (FastRegex.UnsupportedRegexFeatureException expected) {}

        // Quantifiers on anchors are rejected as dangling meta-characters
        try { FastRegex.compileStrict("^*a"); fail("Expected PatternSyntaxException"); } catch (PatternSyntaxException expected) {}
        try { FastRegex.compileStrict("a$+"); fail("Expected PatternSyntaxException"); } catch (PatternSyntaxException expected) {}
        try { FastRegex.compileStrict("^?a"); fail("Expected PatternSyntaxException"); } catch (PatternSyntaxException expected) {}
        try { FastRegex.compileStrict("a${2}"); fail("Expected PatternSyntaxException"); } catch (PatternSyntaxException expected) {}
    }

    @Test
    public void testSingleThreadedJavaCompatibleMatcherApi()
    {
        FastRegex.Pattern rx = FastRegex.compileStrict("(\\w+)=(\\d+)");
        assertEquals("(\\w+)=(\\d+)", rx.pattern());

        // First call initializes defaultMatcher; second call reuses the exact same instance
        FastRegex.Matcher m1 = rx.matcher("k1=10 k2=20");
        assertSame(rx, m1.pattern());
        assertFalse(m1.matched());
        assertFalse(m1.hasMatch());
        assertEquals(2, m1.groupCount());
        assertThrows(IllegalStateException.class, m1::start);
        assertThrows(IllegalStateException.class, m1::group);
        assertThrows(IllegalStateException.class, m1::groupView);

        assertTrue(m1.find());
        assertTrue(m1.matched());
        assertTrue(m1.hasMatch());
        assertEquals(0, m1.start());
        assertEquals(5, m1.end());
        assertEquals("k1=10", m1.group());
        assertEquals("k1=10", m1.groupView().toString());
        assertEquals("k1", m1.group(1));
        assertEquals("10", m1.groupView(2).toString());
        assertEquals(3, m1.start(2));
        assertEquals(5, m1.end(2));

        FastRegex.MatchSnapshot snap = m1.snapshot();
        assertEquals("k1=10", snap.groupView().toString());
        assertEquals("k1", snap.groupView(1).toString());

        assertTrue(m1.find());
        assertEquals("k2=20", m1.group());
        assertFalse(m1.find());
        assertFalse(m1.matched());

        // Calling rx.matcher(...) returns an independent thread-safe Matcher bound to input
        FastRegex.Matcher m2 = rx.matcher("solo=99");
        assertNotSame(m1, m2);
        assertTrue(m2.matches());
        assertEquals("solo=99", m2.group());
        assertFalse(m2.find());

        // Non-matching matches() resets cursor so find() searches from index 0
        m2.reset("prefix solo=99");
        assertFalse(m2.matches());
        assertTrue(m2.find());
        assertEquals("solo=99", m2.group());

        // reset() rewinds cursor on existing input
        assertSame(m2, m2.reset());
        assertFalse(m2.matched());
        assertTrue(m2.find());
        assertEquals("solo=99", m2.group());

        // find(int) bounds validation and zero-width cursor progression
        try { m2.find(-1); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { m2.find(999); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        assertFalse(m2.find(12));

        FastRegex.Pattern zeroWidth = FastRegex.compileStrict("a*");
        FastRegex.Matcher zw = zeroWidth.matcher("");
        assertTrue(zw.matches());
        assertFalse(zw.find()); // nextSearchIndex == 1 > len (0)
        zw.reset("b");
        assertTrue(zw.find(0)); // zero-width at 0
        assertEquals(0, zw.start());
        assertEquals(0, zw.end());
        assertTrue(zw.find());  // zero-width at 1
        assertFalse(zw.find()); // nextSearchIndex == 2 > len (1)

        // Fallback stateful matches() hit branch
        FastRegex.Pattern fbExact = FastRegex.compile("(?=a)ab");
        FastRegex.Matcher fbm = fbExact.matcher("ab");
        assertTrue(fbm.matches());
        assertEquals("ab", fbm.group());
    }

    @Test
    public void testLazyBufferAllocationFootprintInvariants() throws Exception
    {
        var dfaField = FastRegex.Matcher.class.getDeclaredField("dfa");
        var pikeField = FastRegex.Matcher.class.getDeclaredField("pike");
        var auxField = FastRegex.Matcher.class.getDeclaredField("aux");
        var visitedField = FastRegex.Matcher.class.getDeclaredField("visited");
        dfaField.setAccessible(true);
        pikeField.setAccessible(true);
        auxField.setAccessible(true);
        visitedField.setAccessible(true);

        // 1. Fresh Matcher allocates ZERO child buffers (dfa, pike, aux, visited all null)
        FastRegex.Pattern ariat = FastRegex.compileStrict("-Site/[^/]+/([^/\\?]+).*");
        FastRegex.Matcher mChain = ariat.matcher();
        assertNull(dfaField.get(mChain));
        assertNull(pikeField.get(mChain));
        assertNull(auxField.get(mChain));
        assertNull(visitedField.get(mChain));

        // Deterministic chain find() + groupView(1) keeps dfa, pike, aux, AND visited all null!
        assertTrue(mChain.reset("https://x.com/-Site/en_US/Product-Show?pid=1").find());
        assertEquals("Product-Show", mChain.groupView(1).toString());
        assertNull(dfaField.get(mChain));
        assertNull(pikeField.get(mChain));
        assertNull(auxField.get(mChain));
        assertNull(visitedField.get(mChain));

        // Fallback Matcher rejected by prefix/requiredSubstr keeps aux (and jdkFallbackMatcher) null!
        FastRegex.Pattern fbLookahead = FastRegex.compile("shopper/([a-z]+)/categories(?=\\?)");
        FastRegex.Matcher mFb = fbLookahead.matcher().reset("https://x.com/cart");
        assertFalse(mFb.find());
        assertNull(auxField.get(mFb));

        // 2. Direct DFA find (grouped terminal repeat without alternation or captures) allocates dfa only, leaving pike null
        FastRegex.Pattern directDfaRx = FastRegex.compileStrict("(?:ab)+.*");
        FastRegex.Matcher mDfa = directDfaRx.matcher();
        assertTrue(mDfa.reset("xx abab?q=1\nyy").find());
        assertEquals("abab?q=1", mDfa.group());
        assertNotNull(dfaField.get(mDfa));
        assertNull(pikeField.get(mDfa));
        // Second Matcher on same pattern owns an independent shared-nothing DfaBuffers instance
        FastRegex.Matcher mDfa2 = directDfaRx.matcher();
        assertTrue(mDfa2.reset("xx abab?q=1\nyy").find());
        assertNotSame(dfaField.get(mDfa), dfaField.get(mDfa2));

        // 3. ^-anchored pattern allocates PikeVM pike only, leaving 256 KB dfa null
        FastRegex.Pattern bolRx = FastRegex.compileStrict("^hello(\\d+).*");
        FastRegex.Matcher mBol = bolRx.matcher();
        assertTrue(mBol.reset("hello99?q=1\nnext").find());
        assertEquals("99", mBol.group(1));
        assertEquals("hello99?q=1", mBol.group());
        assertNull(dfaField.get(mBol));
        assertNotNull(pikeField.get(mBol));

        // Cross-tier visited sharing: PikeVM first (matches on $-anchored), then DFA (find)
        FastRegex.Pattern eolAlt = FastRegex.compileStrict("(foo|bar)$");
        FastRegex.Matcher mBoth = eolAlt.matcher();
        assertTrue(mBoth.reset("bar").matches());
        assertTrue(mBoth.reset("xx foo").find());
        assertEquals("foo", mBoth.group(1));

        // 4. 1-shot matches() on <=64-state unanchored pattern uses 64-bit register NFA (dfa & pike both null!),
        // then allocates growable DfaBuffers on 2nd call when the Matcher is reused
        FastRegex.Pattern altNoGrp = FastRegex.compileStrict("(?:foo|bar)[0-9]+");
        FastRegex.Matcher mReuse0 = altNoGrp.matcher();
        assertTrue(mReuse0.reset("foo42").matches());
        assertNull(dfaField.get(mReuse0));
        assertNull(pikeField.get(mReuse0));
        assertFalse(altNoGrp.matcher("baz99").matches());
        assertFalse(altNoGrp.matcher("foo").matches());
        assertTrue(mReuse0.reset("bar99").matches());
        assertNotNull(dfaField.get(mReuse0));
        assertNull(pikeField.get(mReuse0));
        assertFalse(mReuse0.reset("baz99").matches());
        assertFalse(mReuse0.reset("foo").matches());
        assertFalse(mReuse0.reset("foo€").matches());

        FastRegex.Pattern altWithGrp = FastRegex.compileStrict("(foo|bar)[0-9]+");
        FastRegex.Matcher mReuse1 = altWithGrp.matcher();
        assertTrue(mReuse1.reset("foo42").matches());
        assertNull(dfaField.get(mReuse1));
        assertNotNull(pikeField.get(mReuse1));
        assertFalse(altWithGrp.matcher("baz99").matches());
        assertTrue(mReuse1.reset("bar99").matches());
        assertEquals("bar", mReuse1.group(1));
        assertNotNull(dfaField.get(mReuse1));
        assertFalse(mReuse1.reset("baz99").matches());

        // 65..128-state unanchored pattern uses 128-bit register NFA on 1st call, allocates DfaBuffers on 2nd call
        FastRegex.Pattern medAlt = FastRegex.compileStrict("(?:a|b){25}");
        FastRegex.Matcher mMed = medAlt.matcher();
        assertTrue(mMed.reset("a".repeat(25)).matches());
        assertNull(dfaField.get(mMed));
        assertNull(pikeField.get(mMed));
        assertFalse(medAlt.matcher("c".repeat(25)).matches());
        assertFalse(medAlt.matcher("a".repeat(24)).matches());
        assertFalse(medAlt.matcher("a".repeat(24) + "€").matches());
        assertTrue(mMed.reset("b".repeat(25)).matches());
        assertNotNull(dfaField.get(mMed));

        // >128-state unanchored pattern defers DfaBuffers on 1st call, allocates DfaBuffers on 2nd call
        FastRegex.Pattern largeAlt = FastRegex.compileStrict("(?:a|b){129}");
        FastRegex.Matcher mLarge = largeAlt.matcher();
        assertTrue(mLarge.reset("a".repeat(129)).matches());
        assertNull(dfaField.get(mLarge));
        assertTrue(mLarge.reset("b".repeat(129)).matches());
        assertNotNull(dfaField.get(mLarge));
    }

    @Test
    public void testBolAnchoredMixedBolAndTrailingDotStarBranches()
    {
        // bolAnchored with fromIndex > 0, String \n, and StringBuilder with and without \n
        FastRegex.Pattern bolDotStar = FastRegex.compileStrict("^item:(\\w+).*");
        var m = bolDotStar.matcher();
        var res = bolDotStar.newMatchResult();
        assertFalse(m.find("item:abc", 1, res));
        assertFalse(m.find("other", 0, res));
        assertTrue(m.find(new StringBuilder("item:abc?x=1\nsecond"), 0, res));
        assertEquals("item:abc?x=1", res.group());
        assertTrue(m.find(new StringBuilder("item:abc?x=1"), 0, res));
        assertEquals("item:abc?x=1", res.group());
        // extendToLineEnd when match already reaches len
        assertTrue(m.find("item:z", 0, res));
        assertEquals("item:z", res.group());

        // Mixed hasBol (not strictly bolAnchored: ^a|b.*) routes through runPikeVmFind + trailingDotStarState
        FastRegex.Pattern mixedBol = FastRegex.compileStrict("^head|tail(\\d+).*");
        var mm = mixedBol.matcher();
        assertTrue(mm.find("head rest", 0, res));
        assertEquals("head", res.group());
        assertTrue(mm.find("xx tail42?a=1\nyy", 0, res));
        assertEquals("tail42?a=1", res.group());
        assertEquals("42", res.group(1));
        assertFalse(mm.find("nothing", 0, res));
    }

    @Test
    public void testDeterministicChainAllOpcodesAndLinearFallback()
    {
        // Zero-group deterministic chain + trailing .*
        FastRegex.Pattern zeroGrp = FastRegex.compileStrict("key=[a-z]+.*");
        var res = zeroGrp.newMatchResult();
        assertTrue(zeroGrp.matcher().find("xx key=val?1\nnext", res));
        assertEquals("key=val?1", res.group());
        assertFalse(zeroGrp.matcher().find("", res));

        // Prefix crossing inner group save (canSkipPrefixInPikeVm == false): (https)://([a-z]+)
        FastRegex.Pattern crossedPref = FastRegex.compileStrict("(https)://([a-z]+)");
        assertTrue(crossedPref.matcher().find("go to https://ariat now", res));
        assertEquals("https", res.group(1));
        assertEquals("ariat", res.group(2));

        // OP_ANY ('.'), OP_EOL ('$'), OP_SPLIT arg==3 (a{0}), single-pass '?' (0 and 1 hit), and failed OP_CLASS/OP_ANY/OP_EOL
        FastRegex.Pattern opsChain = FastRegex.compileStrict("x.a{0}b?[0-9]$");
        var mc = opsChain.matcher();
        assertTrue(mc.find("zz x_b7", res));
        assertEquals("x_b7", res.group());
        assertTrue(mc.find("zz x_7", res));
        assertEquals("x_7", res.group());
        assertFalse(mc.find("x\n7", res));
        assertFalse(mc.find("x", res));
        assertFalse(mc.find("x_z", res));
        assertFalse(mc.find("x_7 trailing", res));

        // Disjoint class-to-class loops ([a-c]+[0-9]+, [^a]+[a], [a]+[^a]) and overlapping classes ([a-c]+[b-d], [^a]+[^b]+)
        assertTrue(FastRegex.compileStrict("[a-c]+[0-9]+").matcher().find("zz abba99", res));
        assertEquals("abba99", res.group());
        assertTrue(FastRegex.compileStrict("[^a]+a").matcher().find("xyza", res));
        assertTrue(FastRegex.compileStrict("[a]+[^a]").matcher().find("aaab", res));
        assertTrue(FastRegex.compileStrict("[a-c]+[b-d]").matcher().find("abcd", res));
        assertTrue(FastRegex.compileStrict("[^a]+[^b]+").matcher().find("cd", res));

        // Repeated failed candidate prefixes triggering O(N) fallback from findDeterministicChain -> findTwoPhaseDfa
        FastRegex.Pattern longFailChain = FastRegex.compileStrict("a+b");
        String quadraticAttempt = "a".repeat(80) + "c" + "aaab";
        assertTrue(longFailChain.matcher().find(quadraticAttempt, res));
        assertEquals("aaab", res.group());
    }

    @Test
    public void testTwoPhaseDfaReluctantEolStartMasksAndPikeVmFallback()
    {
        var res = new FastRegex.ReusableMatchResult(4);

        // Start-char bitmask across all 4 64-bit ASCII mask words:
        // word 0 (<64: '#' = 35), word 1 (64..127: 'z' = 122), word 2 (128..191: '\u00A3' = 163), word 3 (192..255: '\u00E9' = 233)
        FastRegex.Pattern maskAllWords = FastRegex.compileStrict("(#|z|\u00A3|\u00E9)[0-9]");
        var mm = maskAllWords.matcher();
        assertTrue(mm.find("🚀   #1", res));
        assertEquals("#1", res.group());
        assertTrue(mm.find("🚀   z2", res));
        assertEquals("z2", res.group());
        assertTrue(mm.find("🚀   \u00A33", res));
        assertEquals("\u00A33", res.group());
        assertTrue(mm.find("🚀   \u00E94", res));
        assertEquals("\u00E94", res.group());
        assertFalse(mm.find("🚀 \u0090 \u00D0 no hit", res));

        // Non-ASCII OP_CHAR (>255) in walkInitialClosure disables ASCII bitmask cleanly
        FastRegex.Pattern nonAsciiAlt = FastRegex.compileStrict("(€|£)[0-9]");
        assertTrue(nonAsciiAlt.matcher().find("xx €5", res));
        assertEquals("€5", res.group());

        // Direct DFA find with trailingDotStarState: (?:foo|bar).*
        FastRegex.Pattern directDotStar = FastRegex.compileStrict("(?:foo|bar).*");
        assertTrue(directDotStar.matcher().find("zz bar?1=2\nnext", res));
        assertEquals("bar?1=2", res.group());

        // Ambiguous reluctant in scanDfaWindow: immediate zero-length match and non-zero reluctant stop
        FastRegex.Pattern relZero = FastRegex.compileStrict("(?:a|b)*?");
        assertTrue(relZero.matcher().find("ab", res));
        assertEquals("", res.group());
        FastRegex.Pattern relSpan = FastRegex.compileStrict("<(?:a|b)+?>");
        assertTrue(relSpan.matcher().find("xx <ab> <ba>", res));
        assertEquals("<ab>", res.group());

        // canReachMatchAtEol in DFA subset construction: OP_SAVE, chained OP_EOL, OP_SPLIT, non-matching post-EOL
        FastRegex.Pattern eolComplex = FastRegex.compileStrict("(?:(a)$$|(b.*)|c$d)$");
        assertTrue(eolComplex.matcher().find("xx a", res));
        assertEquals("a", res.group(1));
        assertFalse(eolComplex.matcher().find("xx c", res));

        // Two-phase DFA with capturing group + trailing .*
        FastRegex.Pattern capAltDotStar = FastRegex.compileStrict("(foo|bar)=(\\d+).*");
        assertTrue(capAltDotStar.matcher().find("xx bar=77&k=v\nend", res));
        assertEquals("bar=77&k=v", res.group());
        assertEquals("77", res.group(2));

        // Repeated failed DFA candidates triggering O(N) fallback to runPikeVmFind (with multi-char prefix + trailing .*)
        FastRegex.Pattern dfaFallback = FastRegex.compileStrict("px_(a|a)+b.*");
        StringBuilder manyFails = new StringBuilder();
        for (int i = 0; i < 30; i++)
        {
            manyFails.append("px_aaaa!");
        }
        manyFails.append("px_x!px_aab?ok=1\nrest");
        assertTrue(dfaFallback.matcher().find(manyFails.toString(), res));
        assertEquals("px_aab?ok=1", res.group());

        // runPikeVmFind where prefix matches at very end or fails mid-scan without match
        FastRegex.Pattern dfaFallbackMiss = FastRegex.compileStrict("px_(a|a)+b");
        assertFalse(dfaFallbackMiss.matcher().find("px_aaaa!".repeat(30) + "px_", res));

        // unwindSaveStack in iterative addThread: ((a)|b) hits OP_MATCH while outer OP_SAVE undo marker is on epsStack
        FastRegex.Pattern nestedSaveAlt = FastRegex.compileStrict("((a)|b)");
        assertTrue(nestedSaveAlt.matcher().matches("a", res));
        assertEquals("a", res.group(1));
        assertEquals("a", res.group(2));
        assertTrue(nestedSaveAlt.matcher().matches("b", res));
        assertEquals("b", res.group(1));
        assertNull(res.group(2));
    }

    @Test
    public void testFallbackLiteralPrefilterAndStatefulFindBranches()
    {
        // Fallback pattern with BOTH fallbackPrefix ("shop/") and fallbackRequiredSubstr ("/categories")
        FastRegex.Pattern fbBoth = FastRegex.compile("shop/[\\w]+/categories(?=\\?|/).*");
        assertTrue(fbBoth.isFallback());
        var m = fbBoth.matcher();
        var res = fbBoth.newMatchResult();

        // matches(): fail on len < prefLen, first char mismatch, prefix mismatch, requiredSubstr mismatch, JDK fail, and hit
        assertFalse(m.matches("sho", res));
        assertFalse(m.matches("xhop/v1/categories", res));
        assertFalse(m.matches("shox/v1/categories", res));
        assertFalse(m.matches("shop/v1/products", res));
        assertFalse(m.matches("shop/v1/categories", res)); // lookahead (?=\?|/) fails at end
        assertFalse(m.matches(new StringBuilder("shox/v1/categories"), res));
        assertFalse(m.matches(new StringBuilder("shop/v1/products"), res));
        assertTrue(m.matches(new StringBuilder("shop/v1/categories?a=1"), res));

        // find() on String and StringBuilder: fail prefix, fail requiredSubstr, and hit
        assertFalse(m.find("https://example.com/cart", res));
        assertFalse(m.find("https://example.com/shop/v1/products", res));
        assertTrue(m.find("https://example.com/shop/v1/categories?id=1", res));
        assertEquals("shop/v1/categories?id=1", res.group());
        assertTrue(m.find(new StringBuilder("https://example.com/shop/v1/categories/men"), res));
        assertEquals("shop/v1/categories/men", res.group());
        assertFalse(m.find(new StringBuilder("https://example.com/shox/v1/categories/men"), res));

        // Fallback with fallbackRequiredSubstr ONLY (fallbackPrefix == null)
        FastRegex.Pattern fbReqOnly = FastRegex.compile("(?<=/)categories");
        assertFalse(fbReqOnly.matcher().matches("products", res));
        assertFalse(fbReqOnly.matcher().find("https://example.com/products", res));
        assertTrue(fbReqOnly.matcher().find("https://example.com/categories", res));

        // Stateful Matcher.find() across multiple fallback matches
        FastRegex.Pattern fbMulti = FastRegex.compile("item_(\\d+)(?=!)");
        FastRegex.Matcher fm = fbMulti.matcher("item_1! item_2! item_3?");
        assertTrue(fm.find());
        assertEquals("1", fm.group(1));
        assertTrue(fm.find());
        assertEquals("2", fm.group(1));
        assertFalse(fm.find());

        // Diverse fallback syntax constructs in extractFallbackLiterals:
        // \Q..\E, \G, nested [a[b]], (?<=..), (?<!..), (?!..), (?i), depth>=16, top-level |,
        // quantified groups (foo)? / (foo){1,2}?, bracket [^\]\a]+, escaped punctuation
        String[] diverseFallbacks = {
                "\\Qlit\\E(?=x)",
                "\\Gabc",
                "[a[b]]+(?=x)",
                "(?<=pre_)mid_[0-9]+_longer_end",
                "(?<!bad_)ok(?=\\?)",
                "lead(?!bad)_tail",
                "(?i)case_insensitive",
                "((((((((((((((((deep))))))))))))))))(?=x)",
                "left(?=1)|right(?=2)",
                "(opt)?req_literal(?=\\?)",
                "(rep){1,2}?after_rep(?=\\?)",
                "[^]\\a\\-]+\\[lit\\](?=\\?)"
        };
        for (String pat : diverseFallbacks)
        {
            FastRegex.Pattern fb = FastRegex.compile(pat);
            assertTrue("Expected fallback for: " + pat, fb.isFallback());
            fb.matcher("pre_mid_123_longer_end?").find();
        }

        // Additional fallback & native edge-case branch coverage:
        String[] moreFallbacks = {
                "(?:foo)(?=bar)",
                "(((((((((((((((((seventeen)))))))))))))))))(?=x)",
                "[a-z](?=x)",
                "a\\S(?=x)",
                "([a-z])\\1",
                "^a.b$(?=x)",
                "(foo){2}(?=x)",
                "(foo)*(?=x)",
                "(a|b)(?=x)",
                "a\\t(?=x)",
                "\\07(?=x)",
                "(a)(b)(c)(d)(e)(f)(g)(h)(i)\\9"
        };
        for (String pat : moreFallbacks)
        {
            FastRegex.Pattern fb = FastRegex.compile(pat);
            assertTrue(fb.isFallback());
            fb.matcher("foobar").find();
        }

        // 1. Mixed hasBol with outer trailing .* (hits L1427-1429)
        var res2 = new FastRegex.ReusableMatchResult(4);
        FastRegex.Pattern mixedBolDotStar = FastRegex.compileStrict("(?:^head|tail(\\d+)).*");
        assertTrue(mixedBolDotStar.matcher().find("xx tail99?a=1\nnext", res2));
        assertEquals("tail99?a=1", res2.group());

        // 2. Direct DFA find WITHOUT trailing .* (hits false branch of L1856)
        FastRegex.Pattern directNoDotStar = FastRegex.compileStrict("(?:ab)+");
        assertTrue(directNoDotStar.matcher().find("xx abab yy", res2));
        assertEquals("abab", res2.group());

        // 3. findTwoPhaseDfa failedSteps > maxFailedSteps fallback to runPikeVmFind with trailing .* (hits L1879-1881)
        FastRegex.Pattern twoPhaseOverflow = FastRegex.compileStrict("(a|a)+b.*");
        String longOverlap = "a".repeat(90) + "!aab?q=1\nend";
        assertTrue(twoPhaseOverflow.matcher().find(longOverlap, res2));
        assertEquals("aab?q=1", res2.group());
        assertFalse(twoPhaseOverflow.matcher().find("a".repeat(90) + "!", res2));

        // 4. Optional '?' at end of input in deterministic chain (hits i == len on L1809) and trailing '.?' / '.*'
        assertTrue(FastRegex.compileStrict("xa?").matcher().find("x", res2));
        assertTrue(FastRegex.compileStrict(".?").matcher().find("", res2));
        assertTrue(FastRegex.compileStrict(".*").matcher().find("abc", res2));

        // 5. scanDfaWindow with scanStart == len && (initFlag & 2) != 0 (hits L1900)
        assertTrue(FastRegex.compileStrict("foo(?:$|bar)").matcher().find("foo", res2));

        // 6. canReachMatchAtEol with OP_SPLIT, trailingDotStarState, s == NONE (a{0}), and visited cycle (hits L2086-2114)
        assertTrue(FastRegex.compileStrict("(?:$$|$(?:a?|b).*)").matcher().find("hello", res2));
        assertTrue(FastRegex.compileStrict("$(?:a{0}|b)").matcher().find("hello", res2));
        assertFalse(FastRegex.compileStrict("$(?:c|d)").matcher().find("hello", res2));

        // 7. runPikeVmFind with multi-char prefix + mixed ^ (hits L2237, L2249, L2272-2277) and advancePikeVm nsize==0 (L2306)
        FastRegex.Pattern prefMixedBol = FastRegex.compileStrict("px_(?:^x|ab)");
        assertFalse(prefMixedBol.matcher().find("zzz", res2));
        assertTrue(prefMixedBol.matcher().find("px_pxx_px_ab", res2));
        assertEquals("px_ab", res2.group());

        FastRegex.Pattern csizeZeroMid = FastRegex.compileStrict("(?:^$)|(?:^a)");
        assertFalse(csizeZeroMid.matcher().find("xy", res2));
        assertTrue(csizeZeroMid.matcher().find("", res2));
        assertTrue(csizeZeroMid.matcher().find("ab", res2));

        assertFalse(FastRegex.compileStrict("^(a$)$").matcher().matches("ab", res2));

        // 8. Disjoint/overlapping atom combinations in isSingleAtomDisjointLoop (hits L2866, L2868, L2882)
        assertTrue(FastRegex.compileStrict("a+[a-z]").matcher().find("aaab", res2));
        assertTrue(FastRegex.compileStrict("[a-z]+.").matcher().find("ab!", res2));
        assertTrue(FastRegex.compileStrict(".+[0-9]").matcher().find("ab9", res2));
        assertTrue(FastRegex.compileStrict("[^a]+[a]").matcher().find("xyza", res2));
        assertFalse(FastRegex.compileStrict("a+$b").matcher().find("aaa", res2));
        assertTrue(FastRegex.compileStrict("a{0}").matcher().find("x", res2));

        // 9. Remaining EOL & BOL/EOL unanchored PikeVM branches
        assertFalse(FastRegex.compileStrict("z|$(?:a{0}c)").matcher().find("hello", res2));
        assertFalse(FastRegex.compileStrict("z|$(?:c?|d)e").matcher().find("hello", res2));
        assertTrue(FastRegex.compileStrict("(?:^a|$)").matcher().find("xyz", res2));
        assertFalse(FastRegex.compileStrict("(?:^a|$b)").matcher().find("xyz", res2));
        assertTrue(FastRegex.compileStrict("(?:^|$)").matcher().find("x", res2));
    }

    @Test
    public void testCompactMatcherInlineOverflowAndSimdOptimizations()
    {
        // 1. Stateful Matcher with 5 capturing groups (totalGroups = 6 > 4):
        // covers s0..s7 + overflowSlots, view0..view3 + overflowViews, snapshot(), bounds & unmatched checks
        FastRegex.Pattern fiveGrp = FastRegex.compileStrict("px_([a-z])/([0-9])/([A-Z])/(\\w)/([^/]+)");
        FastRegex.Matcher m5 = fiveGrp.matcher();
        assertFalse(m5.matched());
        assertFalse(m5.snapshot().matched());
        try { m5.start(0); fail("Expected IllegalStateException"); } catch (IllegalStateException expected) {}
        try { m5.end(0); fail("Expected IllegalStateException"); } catch (IllegalStateException expected) {}

        assertTrue(m5.reset("xx px_a/1/B/z/tail1 yy px_c/2/D/w/tail2").find());
        assertEquals(5, m5.groupCount());
        assertEquals("px_a/1/B/z/tail1 yy px_c", m5.group());
        assertEquals("a", m5.group(1));
        assertEquals("1", m5.group(2));
        assertEquals("B", m5.group(3));
        assertEquals("z", m5.group(4));
        assertEquals("tail1 yy px_c", m5.group(5));
        for (int g = 0; g <= 5; g++)
        {
            assertEquals(m5.group(g), m5.groupView(g).toString());
            assertEquals(m5.group(g), m5.groupView(g).toString()); // cached view reuse for view0..3 & overflowViews
        }
        assertEquals(m5.group(), m5.groupView().toString());
        try { m5.start(-1); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        try { m5.start(99); fail("Expected IndexOutOfBoundsException"); } catch (IndexOutOfBoundsException expected) {}
        FastRegex.MatchSnapshot snap5 = m5.snapshot();
        assertTrue(snap5.matched());
        assertEquals("z", snap5.group(4));
        assertTrue(m5.reset("px_a/1/B/z/tail1").matches());
        assertEquals("tail1", m5.group(5));
        assertFalse(m5.reset("px_a/1/B/z/tail1/extra").matches());

        FastRegex.Pattern chainDotStar = FastRegex.compileStrict("px_([a-z]+).*");
        FastRegex.Matcher mDot = chainDotStar.matcher();
        assertTrue(mDot.reset("px_hello?a=1").matches());
        assertEquals("hello", mDot.group(1));
        assertFalse(mDot.reset("px_hello\nworld").matches());
        assertFalse(mDot.reset("px_123").matches());

        // 5-group PikeVM & Fallback stateful Matcher (exercises writeMatchedSlots & populateFromJdkMatcher overflow)
        FastRegex.Pattern fivePike = FastRegex.compileStrict("^(a)(b)(c)(d)(e)?$");
        FastRegex.Matcher mp = fivePike.matcher().reset("abcd");
        assertTrue(mp.matches());
        assertEquals("d", mp.group(4));
        assertNull(mp.group(5));
        assertNull(mp.groupView(5));

        FastRegex.Pattern fiveFb = FastRegex.compile("(a)(b)(c)(d)(e)(?=!)" );
        FastRegex.Matcher mfb = fiveFb.matcher().reset("abcde!abcde!");
        assertTrue(mfb.find());
        assertEquals("e", mfb.group(5));
        assertTrue(mfb.find());
        assertEquals("e", mfb.group(5));

        // 2. Single-excluded-char SIMD & CharSequence loops: [^/]+, (.+)\?, ([^/]+)\? on String & StringBuilder
        FastRegex.Pattern overlapSlash = FastRegex.compileStrict("/v1/([^/]+)\\?");
        assertTrue(overlapSlash.matcher().find("https://x.com/v1/products?id=1", new FastRegex.ReusableMatchResult()));
        assertTrue(overlapSlash.matcher().reset(new StringBuilder("https://x.com/v1/products?id=1")).find());
        assertFalse(overlapSlash.matcher().reset(new StringBuilder("https://x.com/v1/products/sub")).find());
        assertFalse(overlapSlash.matcher().reset("https://x.com/v1//?").find());

        FastRegex.Pattern anyLoopMid = FastRegex.compileStrict("k=(.+)\\?");
        FastRegex.Matcher mAny = anyLoopMid.matcher();
        assertTrue(mAny.reset("k=val1?val2?end").find());
        assertEquals("val1?val2", mAny.group(1));
        assertTrue(mAny.reset(new StringBuilder("k=val1?end\nnext")).find());
        assertEquals("val1", mAny.group(1));

        // 3. scanClassMaskRun across all 4 ASCII 64-bit words + Unicode (>= 256) positive & negated + shorthand non-ASCII
        FastRegex.Pattern multiExcl = FastRegex.compileStrict("id=([^/\\?]+)");
        FastRegex.Matcher mMask = multiExcl.matcher().reset("id=09_az_\u00A3_\u00E9_\u20AC/end");
        assertTrue(mMask.find());
        assertEquals("09_az_\u00A3_\u00E9_\u20AC", mMask.group(1));

        FastRegex.Pattern posMask = FastRegex.compileStrict("id=([0-9a-z\u00A3\u00E9]+)");
        FastRegex.Matcher mPos = posMask.matcher().reset("id=9z\u00A3\u00E9\u20AC");
        assertTrue(mPos.find());
        assertEquals("9z\u00A3\u00E9", mPos.group(1));
        assertTrue(mPos.reset("id=9z\u00A4").find());
        assertEquals("9z", mPos.group(1));
        assertTrue(mPos.reset("id=9z\u00EA").find());
        assertEquals("9z", mPos.group(1));
        assertTrue(mPos.reset("id=9z!").find());
        assertEquals("9z", mPos.group(1));
        assertTrue(mPos.reset("id=9z~").find());
        assertEquals("9z", mPos.group(1));

        // Shorthand \D, \W, \S and [^\D] on non-ASCII Unicode ('€') and multi-word excluded [a\D] / [^/a]
        assertTrue(FastRegex.compileStrict("\\D+").matcher().reset("abc€🚀").matches());
        assertTrue(FastRegex.compileStrict("\\W+").matcher().reset("!@#€").matches());
        assertTrue(FastRegex.compileStrict("\\S+").matcher().reset("x€y").matches());
        assertFalse(FastRegex.compileStrict("[^\\D]+").matcher().reset("12€").matches());
        assertTrue(FastRegex.compileStrict("[^\\D]+").matcher().reset("129").matches());
        assertTrue(FastRegex.compileStrict("[0-9\\D]+").matcher().reset("a1€").matches());
        assertTrue(FastRegex.compileStrict("[^/a]+").matcher().reset("xyz").matches());

        // 4. Native spine secondary required literal (nativeRequiredSubstr):
        // multiple candidate prefixes reusing cachedReqPos, various spine opcodes (., a{0}, +?, $, trailing .*)
        FastRegex.Pattern spineAuth = FastRegex.compileStrict("shop/v\\d\\d_\\d/[^/\\?]+/(auth)");
        FastRegex.Matcher mSpine = spineAuth.matcher();
        assertFalse(mSpine.reset("https://x.com/shop/v20_9/products/123").find());
        assertTrue(mSpine.reset("shop/vXX_X/bad shop/v20_9/customers/auth").find());
        assertEquals("auth", mSpine.group(1));

        FastRegex.Pattern spineMultiRuns = FastRegex.compileStrict("px_.a{0}[^/]+?/ab/[0-9]+/longer_seg/[0-9]+/xy$.*");
        assertFalse(spineMultiRuns.matcher().reset("px_1x/ab/1/short/2/xy").find());
        FastRegex.Pattern spineEol = FastRegex.compileStrict("px_.a{0}[^/]+?/ab/[0-9]+/longer_seg/[0-9]+/xy$");
        assertTrue(spineEol.matcher().reset("zz px_!z/ab/7/longer_seg/8/xy").find());
        FastRegex.Pattern spineTrail = FastRegex.compileStrict("px_[0-9]+/items/.*");
        assertTrue(spineTrail.matcher().reset("zz px_9/items/view?a=1\nnext").find());
    }
}
