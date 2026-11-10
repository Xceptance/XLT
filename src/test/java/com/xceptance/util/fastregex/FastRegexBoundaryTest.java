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

import java.util.regex.PatternSyntaxException;

import static org.junit.Assert.*;

/**
 * Boundary conditions, Lazy DFA overflow edge cases, and rare branch tests for {@link FastRegex.Pattern}.
 *
 * @author Gemini
 * @author Rene Schwietzke
 */
public class FastRegexBoundaryTest
{

    @Test
    public void testCountedClosureAndGroupSyntaxErrors()
    {
        // Unclosed '{' and invalid character before '}'
        assertThrows(PatternSyntaxException.class, () -> FastRegex.compileStrict("a{2"));
        assertThrows(PatternSyntaxException.class, () -> FastRegex.compileStrict("a{2,"));
        assertThrows(PatternSyntaxException.class, () -> FastRegex.compileStrict("a{2,4"));
        assertThrows(PatternSyntaxException.class, () -> FastRegex.compileStrict("a{2x}"));

        // Unclosed '(' and '(?' at end of pattern
        assertThrows(PatternSyntaxException.class, () -> FastRegex.compileStrict("("));
        assertThrows(PatternSyntaxException.class, () -> FastRegex.compileStrict("(?"));
    }

    @Test
    public void testZeroBoundedRepeats()
    {
        // min = 0, max = -1 (a{0,})
        FastRegex.Pattern rZeroStar = FastRegex.compileStrict("a{0,}");
        var res = rZeroStar.newMatchResult();
        assertTrue(rZeroStar.matcher().matches("", res));
        assertTrue(rZeroStar.matcher().matches("aaaa", res));

        // min = 0, max > 0 (a{0,3})
        FastRegex.Pattern rZeroMax = FastRegex.compileStrict("a{0,3}");
        assertTrue(rZeroMax.matcher().matches("", res));
        assertTrue(rZeroMax.matcher().matches("a", res));
        assertTrue(rZeroMax.matcher().matches("aaa", res));
        assertFalse(rZeroMax.matcher().matches("aaaa", res));
    }

    @Test
    public void testFindDfaNonAsciiCharacters()
    {
        // Non-capturing lazy DFA find without literal prefix scanning through non-ASCII unicode characters
        FastRegex.Pattern r = FastRegex.compileStrict("[a-z]+_.+_[0-9]+");
        var m = r.matcher();
        var res = r.newMatchResult();
        assertTrue(m.find("🚀 data_🔥_999", res));
        assertEquals("data_🔥_999", res.group());
    }

    @Test
    public void testLazyDfaOverflowInFindAndStartStateRecovery()
    {
        // Pattern with > 256 DFA states and no literal prefix so findLazyDfaNoCaptures is used.
        // First candidate overflows the 256-state cache mid-scan and fails at the end;
        // second candidate must recover the start state cleanly.
        FastRegex.Pattern r = FastRegex.compileStrict("(?:[a-z][0-9]){255}");
        var m = r.matcher();
        var res = r.newMatchResult();

        String half = "a1".repeat(127); // 254 chars (creates 254 states)
        String almostFull = "a1".repeat(254) + "!"; // 509 chars: resets at char 255, fills cache back to 256 states at char 508, then fails on '!'
        String valid = "a1".repeat(255); // 510 chars

        // 1. Mid-scan cache reset followed by filling cache back to 256 states, then failing:
        // Next startIdx iteration in findLazyDfaNoCaptures calls ensureDfaStartState() when cache is full!
        assertFalse(m.find(almostFull, res));

        // 2. Subsequent find on valid input after cache overflow
        assertTrue(m.find(half + "!" + valid, res));
        assertEquals(valid, res.group());

        // 3. Same cache-full initial state recovery on findTwoPhaseDfa (with alternation disabling deterministic chain)
        FastRegex.Pattern rAlt = FastRegex.compileStrict("(?:[a-z][0-9]){254}(?:[a-z]|[A-Z])[0-9]");
        var mAlt = rAlt.matcher();
        assertFalse(mAlt.find(almostFull, res));
        assertTrue(mAlt.find(half + "!" + valid, res));
        assertEquals(valid, res.group());
    }

    @Test
    public void testPikeVmNonEmptyThreadsWithoutMatch()
    {
        // Anchored PikeVM matches() where threads remain alive till end of string but none reach OP_MATCH
        FastRegex.Pattern r = FastRegex.compileStrict("^(a){4}$");
        var m = r.matcher();
        var res = r.newMatchResult();
        assertFalse(m.matches("aaa", res));
    }

    @Test
    public void testStepIdManualCycle() throws Exception
    {
        FastRegex.Pattern r = FastRegex.compileStrict("a");
        var matcher = r.matcher();
        var stepIdField = matcher.getClass().getDeclaredField("stepId");
        stepIdField.setAccessible(true);
        stepIdField.setInt(matcher, -1);
        var nextStepMethod = matcher.getClass().getDeclaredMethod("nextStep");
        nextStepMethod.setAccessible(true);
        int val = (int) nextStepMethod.invoke(matcher);
        assertEquals(1, val);
    }
}
