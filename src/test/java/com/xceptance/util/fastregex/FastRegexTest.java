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

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.PatternSyntaxException;

import static org.junit.Assert.*;

/**
 * Test suite for FastRegexTest.
 *
 * @author Gemini
 * @author Rene Schwietzke
 */
@RunWith(JUnitParamsRunner.class)
public class FastRegexTest
{

    // =========================================================================
    // 1. LITERALS, ANCHORS & SIMD PREFIX EXTRACTION
    // =========================================================================

    @Test
    public void testPureLiteralOptimization()
    {
        var regex = FastRegex.compileStrict("ERR_CONNECTION_REFUSED");
        assertEquals("ERR_CONNECTION_REFUSED", regex.literalPrefix());
        assertFalse(regex.isFallback());

        var matcher = regex.matcher();
        var res = regex.newMatchResult();

        assertTrue(matcher.matches("ERR_CONNECTION_REFUSED", res));
        assertEquals(0, res.start());
        assertEquals(22, res.end());

        assertFalse(matcher.matches("ERR_CONNECTION_REFUSED_EXTRA", res));

        String log = "2026-11-04 INFO ok, ERR_CONNECTION_REFUSED at socket 9";
        assertTrue(matcher.find(log, res));
        assertEquals(20, res.start());
        assertEquals(42, res.end());
        assertEquals("ERR_CONNECTION_REFUSED", res.group());
    }

    @Test
    public void testPrefixExtractionThroughGroupsAndQuantifiers()
    {
        var r1 = FastRegex.compileStrict("(https)://([a-z]+)");
        assertEquals("https://", r1.literalPrefix());

        var r2 = FastRegex.compileStrict("a{3,5}[0-9]+");
        assertEquals("aaa", r2.literalPrefix());

        var r3 = FastRegex.compileStrict("[a-z]+_suffix");
        assertEquals("", r3.literalPrefix());
    }

    @Test
    public void testAnchors()
    {
        var bol = FastRegex.compileStrict("^hello");
        var eol = FastRegex.compileStrict("world$");
        var both = FastRegex.compileStrict("^exact$");
        var res = bol.newMatchResult();

        assertTrue(bol.matcher().find("hello world", res));
        assertFalse(bol.matcher().find("say hello world", res));

        assertTrue(eol.matcher().find("hello world", res));
        assertFalse(eol.matcher().find("world peace", res));

        assertTrue(both.matcher().matches("exact", res));
        assertFalse(both.matcher().find("not exact match", res));
        assertTrue(both.matcher().find("exact", res));
    }

    @Test
    public void testCharSequenceFallback()
    {
        var regex = FastRegex.compileStrict("TOKEN:(\\d+)");
        var matcher = regex.matcher();
        var res = regex.newMatchResult();

        StringBuilder sb = new StringBuilder("noise... TOKEN:98765 ...more noise");
        assertTrue(matcher.find(sb, res));
        assertEquals("98765", res.groupView(1).toString());
    }

    // =========================================================================
    // 2. GREEDY VS. RELUCTANT QUANTIFIERS
    // =========================================================================

    @Test
    public void testGreedyVsReluctantStar()
    {
        String input = "<b>one</b> and <b>two</b>";
        var res = new FastRegex.ReusableMatchResult();

        var greedy = FastRegex.compileStrict("<b>(.*)</b>");
        assertTrue(greedy.matcher().find(input, res));
        assertEquals("<b>one</b> and <b>two</b>", res.group(0));
        assertEquals("one</b> and <b>two", res.group(1));

        var reluctant = FastRegex.compileStrict("<b>(.*?)</b>");
        var relMatcher = reluctant.matcher();
        List<String> innerMatches = new ArrayList<>();
        for (var m : relMatcher.findAll(input, res))
        {
            innerMatches.add(m.group(1));
        }
        assertEquals(List.of("one", "two"), innerMatches);
    }

    @Test
    public void testGreedyVsReluctantPlus()
    {
        String input = "aaaa";
        var res = new FastRegex.ReusableMatchResult();

        var greedy = FastRegex.compileStrict("a+");
        assertTrue(greedy.matcher().find(input, res));
        assertEquals("aaaa", res.group());

        var reluctant = FastRegex.compileStrict("a+?");
        List<String> chunks = new ArrayList<>();
        for (var m : reluctant.matcher().findAll(input, res))
        {
            chunks.add(m.group());
        }
        assertEquals(List.of("a", "a", "a", "a"), chunks);
    }

    @Test
    public void testGreedyVsReluctantQuestion()
    {
        String input = "ab";
        var res = new FastRegex.ReusableMatchResult();

        var greedy = FastRegex.compileStrict("a?");
        assertTrue(greedy.matcher().find(input, res));
        assertEquals("a", res.group());

        var reluctant = FastRegex.compileStrict("a??");
        assertTrue(reluctant.matcher().find(input, res));
        assertEquals("", res.group());
        assertEquals(0, res.start());
        assertEquals(0, res.end());
    }

    @Test
    public void testBoundedQuantifiers()
    {
        var res = new FastRegex.ReusableMatchResult();

        var exact = FastRegex.compileStrict("a{3}");
        assertFalse(exact.matcher().matches("aa", res));
        assertTrue(exact.matcher().matches("aaa", res));
        assertFalse(exact.matcher().matches("aaaa", res));

        var minOnly = FastRegex.compileStrict("a{2,}");
        assertFalse(minOnly.matcher().matches("a", res));
        assertTrue(minOnly.matcher().matches("aa", res));
        assertTrue(minOnly.matcher().matches("aaaaa", res));

        var greedyRange = FastRegex.compileStrict("a{2,4}");
        assertTrue(greedyRange.matcher().find("aaaaa", res));
        assertEquals("aaaa", res.group());

        var reluctantRange = FastRegex.compileStrict("a{2,4}?");
        assertTrue(reluctantRange.matcher().find("aaaaa", res));
        assertEquals("aa", res.group());
    }

    @Test
    public void testZeroLengthMatchProgression()
    {
        var regex = FastRegex.compileStrict("a*");
        var res = regex.newMatchResult();
        List<String> matches = new ArrayList<>();

        for (var m : regex.matcher().findAll("aba", res))
        {
            matches.add(m.start() + ":" + m.end() + "=" + m.group());
        }
        assertEquals(List.of("0:1=a", "1:1=", "2:3=a", "3:3="), matches);
    }

    // =========================================================================
    // 3. CHARACTER CLASSES & SHORTHAND ESCAPES (\d, \D, \w, \W, \s, \S)
    // =========================================================================

    public Object[] parametersForTestStandaloneShorthands()
    {
        return new Object[]
        {
            new Object[] { "\\d+", "12345", true },
            new Object[] { "\\d+", "123a5", false },
            new Object[] { "\\D+", "abc_!", true },
            new Object[] { "\\D+", "abc1", false },
            new Object[] { "\\w+", "Hello_World_99", true },
            new Object[] { "\\w+", "Hello-World", false },
            new Object[] { "\\W+", "!@#$%^&*()", true },
            new Object[] { "\\W+", "!@#a", false },
            new Object[] { "\\s+", " \t\r\n", true },
            new Object[] { "\\S+", "no_spaces_here", true },
            new Object[] { "\\S+", "has space", false }
        };
    }

    @Test
    @Parameters
    public void testStandaloneShorthands(String pattern, String input, boolean expected)
    {
        var regex = FastRegex.compileStrict(pattern);
        var res = regex.newMatchResult();
        assertEquals(expected, regex.matcher().matches(input, res));
    }

    @Test
    public void testShorthandsInsideBrackets()
    {
        var hexOrSpace = FastRegex.compileStrict("[\\da-fA-F\\s]+");
        var res = hexOrSpace.newMatchResult();

        assertTrue(hexOrSpace.matcher().matches("dead BEEF 0123\t99", res));
        assertFalse(hexOrSpace.matcher().matches("dead BEEF G", res));

        var notDigitOrSpace = FastRegex.compileStrict("[^\\d\\s]+");
        assertTrue(notDigitOrSpace.matcher().matches("abc_ABC-!@#", res));
        assertFalse(notDigitOrSpace.matcher().matches("abc 123", res));
    }

    @Test
    public void testBracketEdgeCases()
    {
        var regex = FastRegex.compileStrict("[-a-c\\]]+");
        var res = regex.newMatchResult();
        assertTrue(regex.matcher().matches("a-b-c]", res));
        assertFalse(regex.matcher().matches("d", res));
    }

    @Test
    public void testNegatedClassWithUnicode()
    {
        var notDigit = FastRegex.compileStrict("[^0-9]+");
        var res = notDigit.newMatchResult();
        assertTrue(notDigit.matcher().matches("abc_üöä_🚀", res));
        assertFalse(notDigit.matcher().matches("abc1", res));
    }

    // =========================================================================
    // 4. CAPTURE GROUPS, REUSABLE MATCH RESULT & JAVA 21 SNAPSHOTS
    // =========================================================================

    @Test
    public void testNestedAndNonCapturingGroups()
    {
        var regex = FastRegex.compileStrict("(?:ID:)(\\w+)-((\\d{3})\\.(\\d{2}))");
        var res = regex.newMatchResult();

        assertTrue(regex.matcher().matches("ID:item_4-123.45", res));
        assertEquals(4, res.groupCount());
        assertEquals("ID:item_4-123.45", res.group(0));
        assertEquals("item_4", res.groupView(1).toString());
        assertEquals("123.45", res.groupView(2).toString());
        assertEquals("123", res.groupView(3).toString());
        assertEquals("45", res.groupView(4).toString());
    }

    @Test
    public void testUnmatchedOptionalCaptureGroup()
    {
        var regex = FastRegex.compileStrict("(\\w+)(?:-(\\d+))?");
        var res = regex.newMatchResult();

        assertTrue(regex.matcher().matches("alpha", res));
        assertEquals("alpha", res.group(1));
        assertEquals(-1, res.start(2));
        assertEquals(-1, res.end(2));
        assertNull(res.group(2));
        assertNull(res.groupView(2));
    }

    @Test
    public void testCrossPatternResultReuse()
    {
        var r1 = FastRegex.compileStrict("\\d+");
        var r2 = FastRegex.compileStrict("(\\w+)=(\\d+):(\\w+)");

        var sharedResult = new FastRegex.ReusableMatchResult(1);

        assertTrue(r1.matcher().matches("42", sharedResult));
        assertEquals(0, sharedResult.groupCount());
        assertEquals("42", sharedResult.group());

        assertTrue(r2.matcher().matches("port=8080:tcp", sharedResult));
        assertEquals(3, sharedResult.groupCount());
        assertEquals("port", sharedResult.groupView(1).toString());
        assertEquals("8080", sharedResult.groupView(2).toString());
        assertEquals("tcp", sharedResult.groupView(3).toString());
    }

    @Test
    public void testJava21SnapshotImmutability()
    {
        var regex = FastRegex.compileStrict("(\\w+)=(\\d+)");
        var matcher = regex.matcher();
        var mutableRes = regex.newMatchResult();

        List<FastRegex.MatchSnapshot> snapshots = new ArrayList<>();
        for (var m : matcher.findAll("a=1 b=2 c=3", mutableRes))
        {
            snapshots.add(m.snapshot());
        }

        assertEquals(3, snapshots.size());
        assertEquals("a", snapshots.get(0).group(1));
        assertEquals("2", snapshots.get(1).group(2));
        assertEquals("c=3", snapshots.get(2).group(0));
    }

    // =========================================================================
    // 5. LAZY DFA TRANSITIONS, CACHE OVERFLOW & REDOS IMMUNITY
    // =========================================================================

    @Test
    public void testLazyDfaWarmupConsistency()
    {
        var regex = FastRegex.compileStrict("(?:GET|POST|PUT|DELETE) /(?:api/v[12]/)?[a-z0-9_/-]+");
        var matcher = regex.matcher();
        var res = regex.newMatchResult();

        for (int iter = 0; iter < 50; iter++)
        {
            assertTrue(matcher.matches("GET /api/v2/users/profile_1", res));
            assertTrue(matcher.matches("DELETE /items/42", res));
            assertFalse(matcher.matches("PATCH /api/v1/users", res));
        }
    }

    @Test
    public void testNonAsciiHandlingInLazyDfa()
    {
        var regex = FastRegex.compileStrict("user_.+_ok");
        var matcher = regex.matcher();
        var res = regex.newMatchResult();

        assertTrue(matcher.matches("user_🚀🔥_ok", res));
        assertFalse(matcher.matches("user_🚀\n_ok", res));
    }

    @Test
    public void testLazyDfaCacheOverflowRecovery()
    {
        var regex = FastRegex.compileStrict("(?:[a-z][0-9]){140}");
        var matcher = regex.matcher();
        var res = regex.newMatchResult();

        String valid = "a1b2c3d4e5".repeat(28);
        assertTrue(matcher.matches(valid, res));
        assertFalse(matcher.matches(valid + "x", res));
        assertTrue(matcher.matches(valid, res));
    }

    @Test
    public void testReDoSImmunity() throws Exception
    {
        CompletableFuture.runAsync(() ->
        {
            var p1 = FastRegex.compileStrict("(a?){30}a{30}");
            var res = p1.newMatchResult();
            assertTrue(p1.matcher().matches("a".repeat(30), res));

            var p2 = FastRegex.compileStrict("(a+)+b");
            assertFalse(p2.matcher().matches("a".repeat(60) + "c", res));
        }).get(50, TimeUnit.MILLISECONDS);
    }

    // =========================================================================
    // 6. STRICT EXCEPTION VS. AUTOMATIC JDK FALLBACK & SYNTAX ERRORS
    // =========================================================================

    public Object[][] parametersForTestUnsupportedConstructsThrowInStrictAndSucceedInFallback()
    {
        return new Object[][]
        {
            { "(\\w+)=\\1" },           // Backreference \1
            { "foo(?=bar)" },           // Positive lookahead
            { "\\bword\\b" },           // Word boundary \b
            { "(?<id>\\d+)" },          // Named group
            { "a++" },                  // Possessive quantifier
            { "\\p{Lower}+" },          // POSIX/Unicode property
            { "[a-z&&[^m]]" },          // Character class intersection
            { "(shopper-([^/]+))/[\\w/]+/(\\2-suggestions)(?=\\?|([/A-z-0-9]+)\\?)" }, // Backref \2 + lookahead
            { "(shopper-([^/]+))/[\\w/]+/(categories)(?=\\?|([/A-z-0-9]+)\\?)" }       // Positive lookahead with capture
        };
    }

    @Test
    @Parameters
    public void testUnsupportedConstructsThrowInStrictAndSucceedInFallback(String advancedPattern)
    {
        var ex = assertThrows(FastRegex.UnsupportedRegexFeatureException.class,
                () -> FastRegex.compileStrict(advancedPattern));
        assertEquals(advancedPattern, ex.pattern());
        assertTrue(ex.position() >= 0 && ex.position() < advancedPattern.length());

        var fallbackRegex = FastRegex.compile(advancedPattern);
        assertTrue(fallbackRegex.isFallback());
    }

    @Test
    public void testFallbackMatcherExecution()
    {
        var fallbackRegex = FastRegex.compile("(\\w+)=\\1");
        assertTrue(fallbackRegex.isFallback());

        var matcher = fallbackRegex.matcher();
        var res = fallbackRegex.newMatchResult();

        assertTrue(matcher.matches("secret=secret", res));
        assertEquals("secret", res.groupView(1).toString());
        assertFalse(matcher.matches("secret=other", res));
    }

    public Object[][] parametersForTestInvalidPatternsThrowPatternSyntaxException()
    {
        return new Object[][]
        {
            { "(abc" },          // Unclosed '('
            { "abc)" },          // Unexpected ')'
            { "[a-z" },          // Unclosed '['
            { "[z-a]" },         // Reversed range
            { "*abc" },          // Leading quantifier
            { "a{5,2}" },        // min > max
            { "a{3,x}" },        // Non-numeric bound
            { "abc\\" }          // Trailing backslash
        };
    }

    @Test
    @Parameters
    public void testInvalidPatternsThrowPatternSyntaxException(String badPattern)
    {
        assertThrows(PatternSyntaxException.class, () -> FastRegex.compileStrict(badPattern));
        assertThrows(PatternSyntaxException.class, () -> FastRegex.compile(badPattern));
    }
}
