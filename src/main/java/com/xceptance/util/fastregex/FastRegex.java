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

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.PatternSyntaxException;

/**
 * Java 21 Zero-Allocation Hybrid SIMD-Prefix + Lazy-DFA + PikeVM Regex Engine
 * with automatic or strict-exception fallback to {@link java.util.regex.Pattern}.
 *
 * <p>This engine combines SIMD-accelerated literal prefix scanning (via {@link String#indexOf}),
 * an on-demand Lazy DFA cache for deterministic O(1) state transitions, and a priority-ordered
 * PikeVM with reusable flat primitive slots to guarantee linear time O(N) execution and
 * eliminate runtime GC allocations.</p>
 *
 * <p><strong>Concurrency Model (Shared-Nothing Execution, like {@link java.util.regex.Pattern}):</strong>
 * Pattern compilation ({@link #compile(String)}, {@link #compileStrict(String)}) is 100% thread-safe
 * and lock-free across concurrent threads via thread-local compiler scratch buffers. Once compiled,
 * a {@link Pattern} instance is completely immutable and safe to share across threads with zero
 * synchronization. Derived {@link Matcher} instances (obtained via {@link Pattern#matcher()} or
 * {@link Pattern#matcher(CharSequence)}) and {@link ReusableMatchResult} instances are strictly
 * single-threaded and not thread-safe; use {@link MatchSnapshot} to share match results across
 * threads.</p>
 *
 * @author Gemini
 * @author Rene Schwietzke
 */
public final class FastRegex
{

    // =========================================================================
    // CUSTOM EXCEPTION FOR UNSUPPORTED REGEX FEATURES
    // =========================================================================

    /**
     * Thrown by {@link #compileStrict(String)} when the pattern is a valid regular
     * expression (accepted by {@link java.util.regex.Pattern}), but uses advanced features
     * outside the FastRegex linear-time subset (e.g., lookarounds, backreferences, \b, \p{...}).
     *
     * @author Gemini
     * @author Rene Schwietzke
     */
    public static final class UnsupportedRegexFeatureException extends RuntimeException
    {
        private final String pattern;
        private final int position;

        /**
         * Creates a new exception indicating an unsupported regular expression feature.
         *
         * @param message descriptive explanation of the unsupported construct
         * @param pattern the regular expression pattern string
         * @param position the character index in the pattern where the construct was encountered
         */
        public UnsupportedRegexFeatureException(String message, String pattern, int position)
        {
            super(message + " at index " + position + " in pattern: \"" + pattern + "\"");
            this.pattern = pattern;
            this.position = position;
        }

        /**
         * Returns the regular expression pattern that triggered this exception.
         *
         * @return the pattern string
         */
        public String pattern()
        {
            return pattern;
        }

        /**
         * Returns the zero-based character index within the pattern where the unsupported feature occurred.
         *
         * @return character index in the pattern
         */
        public int position()
        {
            return position;
        }
    }

    // =========================================================================
    // JAVA 21 PUBLIC INTERFACE & REUSABLE MATCH RESULT
    // =========================================================================

    /**
     * Unified interface for match results produced by FastRegex, extending
     * standard {@link java.util.regex.MatchResult}.
     *
     * @author Gemini
     * @author Rene Schwietzke
     */
    public sealed interface FastMatchResult extends MatchResult
            permits ReusableMatchResult, MatchSnapshot, Matcher
    {

        /**
         * Checks if this result holds a successful match.
         *
         * @return true if a match was successfully found, false otherwise
         */
        boolean matched();

        /**
         * Returns a zero-allocation flyweight view over the entire matched input slice (group 0).
         *
         * @return CharSequence slice view of the matched text
         */
        CharSequence groupView();

        /**
         * Returns a zero-allocation flyweight view over the captured input slice for the given group.
         *
         * @param group group index (0 for entire match, 1+ for capturing subgroups)
         * @return CharSequence slice view of the matched text, or null if the group was not matched
         */
        CharSequence groupView(int group);

        /**
         * Takes an immutable record snapshot of this match result, safe for cross-thread sharing.
         *
         * @return an immutable {@link MatchSnapshot} instance
         */
        MatchSnapshot snapshot();
    }

    /**
     * Immutable Java 21 Record snapshot capturing all match details and group bounds.
     * Safe to pass across threads, retain indefinitely, and query without allocation.
     *
     * @param matched whether the match operation was successful
     * @param input the input CharSequence on which matching was performed
     * @param slots flat primitive array of group boundaries [start0, end0, start1, end1, ...]
     * @param groupCount total number of capturing groups
     *
     * @author Gemini
     * @author Rene Schwietzke
     */
    public record MatchSnapshot(
            boolean matched,
            CharSequence input,
            int[] slots,
            int groupCount
    ) implements FastMatchResult
    {

        /**
         * Checks whether this snapshot contains a valid match.
         *
         * @return true if matched, false otherwise
         */
        @Override
        public boolean hasMatch()
        {
            return matched;
        }

        /**
         * Returns the start index of the entire match (group 0).
         *
         * @return start index of the match
         * @throws IllegalStateException if no match was found
         */
        @Override
        public int start()
        {
            return start(0);
        }

        /**
         * Returns the end offset (exclusive) of the entire match (group 0).
         *
         * @return end offset of the match
         * @throws IllegalStateException if no match was found
         */
        @Override
        public int end()
        {
            return end(0);
        }

        /**
         * Returns the start index of the subsequence captured by the given group.
         *
         * @param group the index of a capturing group in this matcher's pattern
         * @return the start index of the group, or -1 if the group was not matched
         * @throws IllegalStateException if no match was found
         * @throws IndexOutOfBoundsException if group is less than 0 or greater than groupCount
         */
        @Override
        public int start(int group)
        {
            checkGroup(group);
            return slots[group << 1];
        }

        /**
         * Returns the offset after the last character of the subsequence captured by the given group.
         *
         * @param group the index of a capturing group in this matcher's pattern
         * @return the end offset of the group, or -1 if the group was not matched
         * @throws IllegalStateException if no match was found
         * @throws IndexOutOfBoundsException if group is less than 0 or greater than groupCount
         */
        @Override
        public int end(int group)
        {
            checkGroup(group);
            return slots[(group << 1) + 1];
        }

        /**
         * Returns the input subsequence matched by the entire pattern (group 0).
         *
         * @return matched string
         * @throws IllegalStateException if no match was found
         */
        @Override
        public String group()
        {
            return group(0);
        }

        /**
         * Returns the input subsequence captured by the given group as a newly allocated String.
         *
         * @param group the index of a capturing group in this matcher's pattern
         * @return the subsequence captured by the group, or null if the group failed to match
         * @throws IllegalStateException if no match was found
         * @throws IndexOutOfBoundsException if group is less than 0 or greater than groupCount
         */
        @Override
        public String group(int group)
        {
            int s = start(group), e = end(group);
            return (s >= 0 && e >= s && input != null) ? input.subSequence(s, e).toString() : null;
        }

        /**
         * Returns a CharSequence view of the entire match (group 0).
         *
         * @return CharSequence slice of the entire match, or null if input is null
         * @throws IllegalStateException if no match was found
         */
        @Override
        public CharSequence groupView()
        {
            return groupView(0);
        }

        /**
         * Returns a CharSequence view of the captured group.
         *
         * @param group the index of a capturing group in this matcher's pattern
         * @return CharSequence slice of the captured group, or null if the group failed to match
         * @throws IllegalStateException if no match was found
         * @throws IndexOutOfBoundsException if group is less than 0 or greater than groupCount
         */
        @Override
        public CharSequence groupView(int group)
        {
            int s = start(group), e = end(group);
            return (s >= 0 && e >= s && input != null) ? input.subSequence(s, e) : null;
        }

        /**
         * Returns this immutable snapshot instance directly.
         *
         * @return this snapshot
         */
        @Override
        public MatchSnapshot snapshot()
        {
            return this;
        }

        private void checkGroup(int group)
        {
            if (!matched)
            {
                throw new IllegalStateException("No match found");
            }
            if (group < 0 || group > groupCount)
            {
                throw new IndexOutOfBoundsException("Invalid group: " + group);
            }
        }
    }

    /**
     * Zero-allocation, mutable, and reusable {@link FastMatchResult} implementation.
     * Can be reused across consecutive calls, matching loops, and different regex patterns.
     *
     * @author Gemini
     * @author Rene Schwietzke
     */
    public static final class ReusableMatchResult implements FastMatchResult
    {
        private int[] slots;
        private SliceView[] views;
        private CharSequence currentInput;
        private int totalGroups;
        private boolean matched;

        /**
         * Constructs a new reusable match result with a default initial capacity of 4 groups.
         */
        public ReusableMatchResult()
        {
            this(4);
        }

        /**
         * Constructs a new reusable match result pre-allocated for the specified group capacity.
         *
         * @param initialGroupCapacity initial number of groups (including group 0) to pre-allocate
         */
        public ReusableMatchResult(int initialGroupCapacity)
        {
            int cap = Math.max(1, initialGroupCapacity);
            this.totalGroups = 1;
            this.slots = new int[cap << 1];
        }

        /**
         * Ensures the internal slot array can hold {@code neededSlots} bounds and returns it.
         *
         * @param neededSlots minimum required slot array length
         * @return internal slot array of length at least {@code neededSlots}
         */
        int[] ensureSlotCapacity(int neededSlots)
        {
            if (slots.length < neededSlots)
            {
                slots = new int[neededSlots];
            }
            return slots;
        }

        /**
         * Commits match metadata after group slot bounds have already been populated in {@link #slots}.
         *
         * @param input the matched character sequence
         * @param requiredTotalGroups total group count (capturing groups + 1 for group 0)
         */
        void commitPreparedMatch(CharSequence input, int requiredTotalGroups)
        {
            this.currentInput = input;
            this.totalGroups = requiredTotalGroups;
            this.matched = true;
        }

        /**
         * Prepares this result carrier to receive matched group bounds without redundant slot clearing.
         * Invoked only when a match succeeds; callers overwrite all {@code requiredTotalGroups * 2} slots.
         *
         * @param input the input character sequence that matched
         * @param requiredTotalGroups total group count (capturing groups + 1 for group 0)
         */
        void beginMatchWrite(CharSequence input, int requiredTotalGroups)
        {
            this.currentInput = input;
            this.totalGroups = requiredTotalGroups;
            this.matched = true;
            int neededSlots = requiredTotalGroups << 1;
            if (slots.length < neededSlots)
            {
                slots = new int[neededSlots];
            }
        }

        /**
         * Fast-path writer for patterns with no capturing subgroups ({@code totalGroups == 1}).
         *
         * @param input the matched character sequence
         * @param start match start index (inclusive)
         * @param end match end index (exclusive)
         */
        void setZeroGroupMatch(CharSequence input, int start, int end)
        {
            this.currentInput = input;
            this.totalGroups = 1;
            this.slots[0] = start;
            this.slots[1] = end;
            this.matched = true;
        }

        /**
         * Checks if the last match operation succeeded.
         *
         * @return true if matched, false otherwise
         */
        @Override
        public boolean matched()
        {
            return matched;
        }

        /**
         * Checks whether a match is present.
         *
         * @return true if matched, false otherwise
         */
        @Override
        public boolean hasMatch()
        {
            return matched;
        }

        /**
         * Returns the start index of the match (group 0).
         *
         * @return start index of the match
         * @throws IllegalStateException if no match is available
         */
        @Override
        public int start()
        {
            return start(0);
        }

        /**
         * Returns the end offset (exclusive) of the match (group 0).
         *
         * @return end offset of the match
         * @throws IllegalStateException if no match is available
         */
        @Override
        public int end()
        {
            return end(0);
        }

        /**
         * Returns the start index of the subsequence captured by the given group.
         *
         * @param group group index (0 for whole match, 1..groupCount for capturing groups)
         * @return start index of the group, or -1 if the group did not match
         * @throws IllegalStateException if no match is available
         * @throws IndexOutOfBoundsException if the group index is out of bounds
         */
        @Override
        public int start(int group)
        {
            checkMatchAndGroup(group);
            return slots[group << 1];
        }

        /**
         * Returns the end offset (exclusive) of the subsequence captured by the given group.
         *
         * @param group group index (0 for whole match, 1..groupCount for capturing groups)
         * @return end offset of the group, or -1 if the group did not match
         * @throws IllegalStateException if no match is available
         * @throws IndexOutOfBoundsException if the group index is out of bounds
         */
        @Override
        public int end(int group)
        {
            checkMatchAndGroup(group);
            return slots[(group << 1) + 1];
        }

        /**
         * Returns the matched input subsequence (group 0) as a String.
         *
         * @return matched string
         * @throws IllegalStateException if no match is available
         */
        @Override
        public String group()
        {
            return group(0);
        }

        /**
         * Returns the input subsequence captured by the given group as a String.
         *
         * @param group group index (0 for whole match, 1..groupCount for capturing groups)
         * @return captured string, or null if the optional group did not match
         * @throws IllegalStateException if no match is available
         * @throws IndexOutOfBoundsException if the group index is out of bounds
         */
        @Override
        public String group(int group)
        {
            int s = start(group);
            return (s >= 0) ? currentInput.subSequence(s, end(group)).toString() : null;
        }

        /**
         * Returns a zero-allocation flyweight {@link CharSequence} view of the entire match (group 0).
         *
         * @return zero-allocation CharSequence slice view of group 0
         * @throws IllegalStateException if no match is available
         */
        @Override
        public CharSequence groupView()
        {
            return groupView(0);
        }

        /**
         * Returns a zero-allocation flyweight {@link CharSequence} view of the captured group.
         *
         * @param group group index (0 for whole match, 1..groupCount for capturing groups)
         * @return zero-allocation CharSequence slice view, or null if the optional group did not match
         * @throws IllegalStateException if no match is available
         * @throws IndexOutOfBoundsException if the group index is out of bounds
         */
        @Override
        public CharSequence groupView(int group)
        {
            int s = start(group);
            if (s < 0)
            {
                return null;
            }
            return getOrCreateView(group).reset(currentInput, s, slots[(group << 1) + 1]);
        }

        private SliceView getOrCreateView(int group)
        {
            SliceView[] vs = this.views;
            if (vs == null)
            {
                vs = new SliceView[Math.max(slots.length >>> 1, group + 1)];
                this.views = vs;
            }
            else if (group >= vs.length)
            {
                vs = Arrays.copyOf(vs, Math.max(slots.length >>> 1, group + 1));
                this.views = vs;
            }
            SliceView v = vs[group];
            if (v == null)
            {
                v = new SliceView();
                vs[group] = v;
            }
            return v;
        }

        /**
         * Returns the number of capturing groups in the pattern that produced this result.
         *
         * @return number of capturing groups (excluding group 0)
         */
        @Override
        public int groupCount()
        {
            return totalGroups - 1;
        }

        /**
         * Creates an immutable {@link MatchSnapshot} with a defensive copy of the current group slots.
         *
         * @return an immutable {@link MatchSnapshot}
         */
        @Override
        public MatchSnapshot snapshot()
        {
            return new MatchSnapshot(
                    matched,
                    currentInput,
                    matched ? Arrays.copyOf(slots, totalGroups << 1) : EMPTY_SLOTS,
                    totalGroups - 1
            );
        }

        private void checkMatchAndGroup(int group)
        {
            if (!matched)
            {
                throw new IllegalStateException("No match available");
            }
            if (group < 0 || group >= totalGroups)
            {
                throw new IndexOutOfBoundsException("No group " + group);
            }
        }
    }

    /**
     * Reusable flyweight {@link CharSequence} view over a sub-range of an underlying input sequence.
     */
    private static final class SliceView implements CharSequence
    {
        private CharSequence base;
        private int start;
        private int end;

        SliceView reset(CharSequence base, int start, int end)
        {
            this.base = base;
            this.start = start;
            this.end = end;
            return this;
        }

        @Override
        public int length()
        {
            return end - start;
        }

        @Override
        public char charAt(int index)
        {
            if (index < 0 || index >= length())
            {
                throw new IndexOutOfBoundsException(index);
            }
            return base.charAt(start + index);
        }

        @Override
        public CharSequence subSequence(int s, int e)
        {
            if (s < 0 || e > length() || s > e)
            {
                throw new IndexOutOfBoundsException("Range [" + s + ", " + e + ") out of bounds for length " + length());
            }
            return base.subSequence(start + s, start + e);
        }

        @Override
        public String toString()
        {
            return base.subSequence(start, end).toString();
        }
    }

    // =========================================================================
    // NFA OPCODES & STATE STORAGE
    // =========================================================================

    /** Terminal accepting NFA state opcode. */
    private static final int OP_MATCH = 0;
    /** Exact single UTF-16 character match opcode. */
    private static final int OP_CHAR  = 1;
    /** Wildcard '.' match (any char except '\n') opcode. */
    private static final int OP_ANY   = 2;
    /** 256-bit ASCII character class (or negated class) match opcode. */
    private static final int OP_CLASS = 3;
    /** Epsilon branch split opcode (out has higher priority than out1). */
    private static final int OP_SPLIT = 4;
    /** Beginning-of-line/input '^' zero-width assertion opcode. */
    private static final int OP_BOL   = 5;
    /** End-of-line/input '$' zero-width assertion opcode. */
    private static final int OP_EOL   = 6;
    /** Submatch group boundary slot save epsilon opcode. */
    private static final int OP_SAVE  = 7;

    /** Sentinel value indicating an unpatched or absent state transition. */
    private static final int NONE = -1;
    /** Shared zero-length slot array for unmatched snapshots. */
    private static final int[] EMPTY_SLOTS = new int[0];

    // =========================================================================
    // LOCK-FREE THREAD-LOCAL COMPILER SCRATCH BUFFERS
    // =========================================================================

    /**
     * Per-thread reusable scratch buffers for Thompson NFA compilation, eliminating
     * concurrent compilation data races and lock contention without per-compile allocations.
     */
    private static final class CompilerScratch
    {
        int[] op = new int[32];
        int[] arg = new int[32];
        int[] out = new int[32];
        int[] out1 = new int[32];
        long[] classBits = new long[16];
        boolean[] classNegated = new boolean[4];
        int[] classExcludedChar = new int[4];
        int[] dangling = new int[8];
        int[] visited = new int[32];
        final long[] startMasks = new long[4];
        final int[] closureFlags = new int[1];
        int stateCount;
        int classCount;
        int danglingTop;
        final StringBuilder prefixSb = new StringBuilder(64);
    }

    private static final ThreadLocal<CompilerScratch> COMPILER_SCRATCH =
            ThreadLocal.withInitial(CompilerScratch::new);

    // Precomputed 256-bit ASCII masks for shorthand character classes (\d, \w, \s)
    private static final long DIGIT_MASK_W0 = 0x03FF_0000_0000_0000L;
    private static final long WORD_MASK_W1  = 0x07FF_FFFE_87FF_FFFEL;
    private static final long SPACE_MASK_W0 = 0x0000_0001_0000_3E00L;


    private FastRegex()
    {
    }

    /**
     * Compiles a pattern strictly into the high-performance linear-time engine.
     *
     * @param pattern regular expression string
     * @return compiled {@link Pattern} instance
     * @throws UnsupportedRegexFeatureException if the pattern uses constructs outside the fast linear subset
     * @throws PatternSyntaxException if the pattern syntax itself is invalid
     */
    public static Pattern compileStrict(String pattern)
            throws UnsupportedRegexFeatureException, PatternSyntaxException
    {
        return Pattern.compileStrict(pattern);
    }

    /**
     * Compiles a pattern with automatic transparent fallback to {@link java.util.regex.Pattern}.
     *
     * @param pattern regular expression string
     * @return compiled {@link Pattern} instance (either native fast or wrapped fallback)
     * @throws PatternSyntaxException if the pattern syntax is malformed
     */
    public static Pattern compile(String pattern) throws PatternSyntaxException
    {
        return Pattern.compile(pattern);
    }

    // =========================================================================
    // IMMUTABLE THREAD-SAFE COMPILED PATTERN
    // =========================================================================

    /**
     * Immutable, thread-safe compiled regular expression automaton (analogous to
     * {@link java.util.regex.Pattern}).
     *
     * <p>Instances of this class hold only read-only NFA transition tables, character-class
     * bitsets, and extracted SIMD literal prefixes. They can be shared freely across
     * concurrent threads without synchronization; each thread obtains its own single-threaded
     * {@link Matcher} via {@link #matcher()} or {@link #matcher(CharSequence)}.</p>
     *
     * @author Gemini
     * @author Rene Schwietzke
     */
    public static final class Pattern
    {
        // Parallel flat state transition arrays indexed by NFA state ID
        private int[] op;
        private int[] arg;
        private int[] out;
        private int[] out1;
        private int stateCount;
        private int startState;
        private int totalGroups;
        private int slotCount;
        private int wordsPerSet;
        private boolean hasBol;
        private boolean hasEol;
        private boolean bolAnchored;
        private boolean hasAmbiguousReluctant;
        private boolean hasAmbiguousGreedyExit;
        private boolean hasAlternation;
        private boolean canDirectDfaFind;
        private boolean isDeterministicChain;
        private int trailingDotStarState = NONE;

        // Fallback java.util.regex.Pattern & deterministic SIMD prefilters when pattern uses unsupported features
        private final java.util.regex.Pattern fallbackPattern;
        private String fallbackPrefix;
        private char fallbackFirstChar;
        private String fallbackRequiredSubstr;
        private char fallbackRequiredFirstChar;

        // Extracted Literal Prefix, Secondary Spine Substring & Start-Character Bitmask for SIMD acceleration
        private String literalPrefix;
        private int literalPrefixLen;
        private char firstPrefixChar;
        private boolean isPureLiteral;
        private int postPrefixState = NONE;
        private boolean canSkipPrefixInPikeVm;
        private String nativeRequiredSubstr;
        private char nativeReqFirstChar;
        private int nativeReqMinOffset;
        private boolean hasStartCharMask;
        private long startMask0;
        private long startMask1;
        private long startMask2;
        private long startMask3;

        // Pre-negated effective 256-bit (4 x long) ASCII bitsets, non-ASCII flags & single-excluded-char table
        private long[] classBits;
        private boolean[] classNegated;
        private int[] classExcludedChar;

        // Precomputed 64-bit / 128-bit NFA epsilon closures for (stateCount <= 128) unanchored patterns
        private boolean hasClosure64;
        private boolean hasClosure128;
        private long startClosure64;
        private long startClosure128Hi;
        private long matchStateMask64;
        private long[] nextClosure64;

        private final String pattern;
        private int pos;
        private CompilerScratch sc;

        private Pattern(String pattern, boolean allowFallback)
        {
            this.pattern = pattern;
            java.util.regex.Pattern fb = null;
            this.sc = COMPILER_SCRATCH.get();
            try
            {
                compileFast();
            }
            catch (UnsupportedRegexFeatureException e)
            {
                // Reset partial metadata recorded before encountering the unsupported construct
                this.literalPrefix = null;
                this.literalPrefixLen = 0;
                this.isPureLiteral = false;
                this.trailingDotStarState = NONE;
                this.hasStartCharMask = false;
                this.nativeRequiredSubstr = null;
                // Verify with JDK Pattern first: if syntax is broken, throw PatternSyntaxException!
                java.util.regex.Pattern jdkPattern = java.util.regex.Pattern.compile(pattern);
                if (!allowFallback)
                {
                    throw e; // Caller requested strict mode
                }
                fb = jdkPattern;
                this.totalGroups = jdkPattern.matcher("").groupCount() + 1;
                extractFallbackLiterals(this.sc.prefixSb);
            }
            finally
            {
                this.sc = null;
            }
            this.fallbackPattern = fb;
            this.slotCount = this.totalGroups << 1;
            this.wordsPerSet = (fb != null || this.isPureLiteral) ? 0 : ((this.stateCount + 63) >>> 6);
            this.canDirectDfaFind = (fb == null
                    && this.totalGroups == 1
                    && !this.hasAmbiguousReluctant
                    && !this.hasAlternation
                    && !this.hasAmbiguousGreedyExit);
        }

        /**
         * Compiles a pattern strictly into the high-performance linear-time engine.
         *
         * @param pattern regular expression string
         * @return compiled {@link Pattern} instance
         * @throws UnsupportedRegexFeatureException if the pattern uses constructs outside the fast linear subset
         * @throws PatternSyntaxException if the pattern syntax itself is invalid
         */
        public static Pattern compileStrict(String pattern)
                throws UnsupportedRegexFeatureException, PatternSyntaxException
        {
            return new Pattern(pattern, false);
        }

        /**
         * Compiles a pattern with automatic transparent fallback to {@link java.util.regex.Pattern}.
         *
         * @param pattern regular expression string
         * @return compiled {@link Pattern} instance (either native fast or wrapped fallback)
         * @throws PatternSyntaxException if the pattern syntax is malformed
         */
        public static Pattern compile(String pattern) throws PatternSyntaxException
        {
            return new Pattern(pattern, true);
        }

        /**
         * Returns the regular expression string from which this instance was compiled.
         *
         * @return source regular expression string
         */
        public String pattern()
        {
            return pattern;
        }

        /**
         * Checks whether this pattern is running via the {@link java.util.regex.Pattern} fallback engine.
         *
         * @return true if running in fallback mode, false if executing via native SIMD/DFA/PikeVM
         */
        public boolean isFallback()
        {
            return fallbackPattern != null;
        }

        /**
         * Returns the extracted deterministic literal prefix used for SIMD acceleration.
         *
         * @return literal prefix string, or empty string if pattern has no deterministic prefix
         */
        public String literalPrefix()
        {
            return literalPrefix != null ? literalPrefix : "";
        }

        /**
         * Allocates a new mutable {@link ReusableMatchResult} sized for this regex's group capacity.
         *
         * @return a new reusable match result carrier
         */
        public ReusableMatchResult newMatchResult()
        {
            return new ReusableMatchResult(totalGroups);
        }

        /**
         * Creates a new state machine matcher instance for this compiled regex.
         *
         * @return a stateful {@link Matcher} instance
         */
        public Matcher matcher()
        {
            return new Matcher(this);
        }

        /**
         * Creates a new state machine {@link Matcher} bound to the given input character sequence.
         * Mirrors {@link java.util.regex.Pattern#matcher(CharSequence)} and is 100% thread-safe
         * to call concurrently on a shared {@link Pattern} instance.
         *
         * @param input character sequence to match against
         * @return a new stateful {@link Matcher} bound to {@code input}
         */
        public Matcher matcher(CharSequence input)
        {
            return new Matcher(this, Objects.requireNonNull(input, "input"));
        }


        boolean matchesClassAt(int cid, char c)
        {
            int excl = this.classExcludedChar[cid];
            if (excl >= 0)
            {
                return c != excl;
            }
            if (c >= 256)
            {
                return this.classNegated[cid];
            }
            return (this.classBits[(cid << 2) + (c >>> 6)] & (1L << c)) != 0L;
        }

        boolean matchesAtomChar(int state, char c)
        {
            return switch (this.op[state])
            {
                case OP_CHAR  -> c == this.arg[state];
                case OP_ANY   -> c != '\n';
                case OP_CLASS -> matchesClassAt(this.arg[state], c);
                default       -> false;
            };
        }

        // =========================================================================
        // COMPILER & UNSUPPORTED FEATURE DETECTION
        // =========================================================================

        private static final long NO_FRAG = -1L;

        private static long packFrag(int start, int offset, int len)
        {
            return ((long) start << 43) | ((long) offset << 21) | len;
        }

        private static int fragStart(long frag)
        {
            return (int) (frag >>> 43);
        }

        private static int fragOffset(long frag)
        {
            return (int) ((frag >>> 21) & 0x3F_FFFF);
        }

        private static int fragLen(long frag)
        {
            return (int) (frag & 0x1F_FFFF);
        }

        private void pushDangling(int ptr)
        {
            CompilerScratch s = this.sc;
            if (s.danglingTop == s.dangling.length)
            {
                s.dangling = Arrays.copyOf(s.dangling, s.dangling.length << 1);
            }
            s.dangling[s.danglingTop++] = ptr;
        }

        private long singleFrag(int start, int danglingPtr)
        {
            int offset = this.sc.danglingTop;
            pushDangling(danglingPtr);
            return packFrag(start, offset, 1);
        }

        private void compileFast()
        {
            CompilerScratch s = this.sc;
            this.pos = 0;
            this.totalGroups = 1;
            this.hasBol = false;
            this.hasEol = false;
            this.bolAnchored = false;
            this.hasAmbiguousReluctant = false;
            this.hasAmbiguousGreedyExit = false;
            this.hasAlternation = false;
            this.isDeterministicChain = false;
            this.trailingDotStarState = NONE;
            s.stateCount = 0;
            s.classCount = 0;
            s.danglingTop = 0;

            int saveStart0 = newState(OP_SAVE, 0, NONE, NONE);
            long f = parseExpr();
            if (pos < pattern.length())
            {
                throw new PatternSyntaxException("Unexpected character '" + pattern.charAt(pos) + "'", pattern, pos);
            }

            // Detect trailing uncaptured greedy .* or .+ (single exit out1 of OP_SPLIT looping on OP_ANY)
            if (fragLen(f) == 1)
            {
                int ptr = s.dangling[fragOffset(f)];
                if ((ptr & 1) == 1)
                {
                    int sSplit = ptr >>> 1;
                    int sAny = s.out[sSplit];
                    if (s.op[sAny] == OP_ANY && s.out[sAny] == sSplit)
                    {
                        this.trailingDotStarState = sSplit;
                    }
                }
            }

            int saveEnd0 = newState(OP_SAVE, 1, NONE, NONE);
            int matchState = newState(OP_MATCH, 0, NONE, NONE);

            s.out[saveStart0] = fragStart(f);
            patch(f, saveEnd0);
            s.out[saveEnd0] = matchState;
            this.startState = saveStart0;
            this.stateCount = s.stateCount;

            extractLiteralPrefix();

            if (!this.isPureLiteral)
            {
                this.op = Arrays.copyOf(s.op, s.stateCount);
                this.arg = Arrays.copyOf(s.arg, s.stateCount);
                this.out = Arrays.copyOf(s.out, s.stateCount);
                this.out1 = Arrays.copyOf(s.out1, s.stateCount);
                if (s.classCount > 0)
                {
                    this.classBits = Arrays.copyOf(s.classBits, s.classCount << 2);
                    this.classNegated = Arrays.copyOf(s.classNegated, s.classCount);
                    this.classExcludedChar = Arrays.copyOf(s.classExcludedChar, s.classCount);
                }
                analyzeInitialClosure();
            }
        }

        private void extractLiteralPrefix()
        {
            CompilerScratch s = this.sc;
            StringBuilder sb = s.prefixSb;
            sb.setLength(0);
            int curr = startState;
            boolean crossedInnerSave = false;

            while (true)
            {
                int opcode = s.op[curr];
                if (opcode == OP_SAVE)
                {
                    if (s.arg[curr] != 0)
                    {
                        crossedInnerSave = true;
                    }
                    curr = s.out[curr];
                }
                else if (opcode == OP_CHAR)
                {
                    sb.append((char) s.arg[curr]);
                    curr = s.out[curr];
                }
                else
                {
                    break;
                }
            }

            if (!sb.isEmpty())
            {
                int prefLen = sb.length();
                this.literalPrefixLen = prefLen;
                this.firstPrefixChar = sb.charAt(0);
                this.isPureLiteral = (s.op[curr] == OP_MATCH && totalGroups == 1);
                this.literalPrefix = (this.isPureLiteral && prefLen == pattern.length())
                        ? pattern
                        : sb.toString();
                this.postPrefixState = curr;
                this.canSkipPrefixInPikeVm = !crossedInnerSave;
            }
        }

        /**
         * Analyzes the initial epsilon closure from {@code startState} to detect strict
         * beginning-of-input anchoring ({@code ^...}) and compute a 256-bit ASCII start-character
         * bitmask when {@code literalPrefixLen == 0}.
         */
        private void analyzeInitialClosure()
        {
            if (this.literalPrefixLen == 0)
            {
                CompilerScratch s = this.sc;
                if (s.visited.length < this.stateCount)
                {
                    s.visited = new int[this.stateCount];
                }
                Arrays.fill(s.visited, 0, this.stateCount, 0);

                long[] masks = s.startMasks;
                masks[0] = 0L;
                masks[1] = 0L;
                masks[2] = 0L;
                masks[3] = 0L;
                // flags[0]: 1 = saw non-BOL leaf, 2 = cannot build ASCII start-char bitmask
                int[] flags = s.closureFlags;
                flags[0] = 0;
                walkInitialClosure(this.startState, s.visited, masks, flags);

                this.bolAnchored = this.hasBol && ((flags[0] & 1) == 0);
                if ((flags[0] & 2) == 0)
                {
                    this.hasStartCharMask = true;
                    this.startMask0 = masks[0];
                    this.startMask1 = masks[1];
                    this.startMask2 = masks[2];
                    this.startMask3 = masks[3];
                }
            }
            analyzeSplitDeterminism();
        }

        /**
         * Inspects quantifier {@code OP_SPLIT} states to distinguish genuinely ambiguous reluctant
         * or optional exits from deterministic disjoint loops (such as {@code [^/]+?/} or {@code [^/]+/}).
         */
        private void analyzeSplitDeterminism()
        {
            for (int s = 0; s < this.stateCount; s++)
            {
                if (this.op[s] != OP_SPLIT)
                {
                    continue;
                }
                int kind = this.arg[s];
                if (kind == 1)
                {
                    int exitTarget = skipSaves(this.out[s]);
                    int bodyTarget = skipSaves(this.out1[s]);
                    if (!isSingleAtomDisjointLoop(s, bodyTarget, exitTarget))
                    {
                        this.hasAmbiguousReluctant = true;
                    }
                }
                else if (kind == 0 && s != this.trailingDotStarState)
                {
                    int exitTarget = skipSaves(this.out1[s]);
                    int exitOp = this.op[exitTarget];
                    int bodyTarget = skipSaves(this.out[s]);
                    boolean terminalExit = (exitOp == OP_MATCH)
                            || (exitTarget == this.trailingDotStarState)
                            || (exitOp == OP_EOL && this.op[skipSaves(this.out[exitTarget])] == OP_MATCH);
                    if (!terminalExit && !isSingleAtomDisjointLoop(s, bodyTarget, exitTarget))
                    {
                        if (isOverlapTerminalCharLoop(s, this.out[s], exitTarget))
                        {
                            this.arg[s] = 4;
                        }
                        else
                        {
                            this.hasAmbiguousGreedyExit = true;
                        }
                    }
                }
            }
            this.isDeterministicChain = checkDeterministicChain();
            if (this.isDeterministicChain && this.literalPrefixLen > 0)
            {
                extractNativeRequiredSubstr();
            }
            if (!this.isDeterministicChain
                    && this.stateCount <= 128
                    && !this.hasBol
                    && !this.hasEol
                    && this.trailingDotStarState == NONE)
            {
                precomputeClosure();
            }
        }

        private void precomputeClosure()
        {
            int n = this.stateCount;
            long[] scratch = new long[4];
            computeClosure128(this.startState, scratch);
            this.startClosure64 = scratch[0];
            this.matchStateMask64 = 1L << (n - 1);
            if (n <= 64)
            {
                long[] nextClosures = new long[n];
                for (int s = 0; s < n; s++)
                {
                    int opcode = this.op[s];
                    if (opcode >= OP_CHAR && opcode <= OP_CLASS)
                    {
                        computeClosure128(this.out[s], scratch);
                        nextClosures[s] = scratch[0];
                    }
                }
                this.nextClosure64 = nextClosures;
                this.hasClosure64 = true;
            }
            else
            {
                this.startClosure128Hi = scratch[1];
                long[] nextClosures = new long[n << 1];
                for (int s = 0; s < n; s++)
                {
                    int opcode = this.op[s];
                    if (opcode >= OP_CHAR && opcode <= OP_CLASS)
                    {
                        computeClosure128(this.out[s], scratch);
                        int base = s << 1;
                        nextClosures[base] = scratch[0];
                        nextClosures[base + 1] = scratch[1];
                    }
                }
                this.nextClosure64 = nextClosures;
                this.hasClosure128 = true;
            }
        }

        private void computeClosure128(int entryState, long[] scratch)
        {
            scratch[0] = 0L;
            scratch[1] = 0L;
            scratch[2] = 0L;
            scratch[3] = 0L;
            walkClosure128(entryState, scratch);
        }

        private void walkClosure128(int s, long[] scratch)
        {
            if (s == NONE)
            {
                return;
            }
            int wordIdx = s >>> 6;
            long bit = 1L << s;
            if ((scratch[2 + wordIdx] & bit) != 0L)
            {
                return;
            }
            scratch[2 + wordIdx] |= bit;
            switch (this.op[s])
            {
                case OP_SPLIT ->
                {
                    walkClosure128(this.out[s], scratch);
                    walkClosure128(this.out1[s], scratch);
                }
                case OP_SAVE -> walkClosure128(this.out[s], scratch);
                default -> scratch[wordIdx] |= bit;
            }
        }

        private void extractNativeRequiredSubstr()
        {
            int curr = this.postPrefixState;
            int minOffset = this.literalPrefixLen;
            int runStartState = NONE;
            int runLen = 0;
            int runStartOffset = minOffset;
            int bestStartState = NONE;
            int bestLen = 1;
            int bestMinOffset = 0;

            for (;;)
            {
                int opcode = this.op[curr];
                if (opcode == OP_CHAR)
                {
                    if (runLen == 0)
                    {
                        runStartState = curr;
                        runStartOffset = minOffset;
                    }
                    runLen++;
                    minOffset++;
                    curr = this.out[curr];
                }
                else if (opcode == OP_SAVE)
                {
                    curr = this.out[curr];
                }
                else
                {
                    if (runLen > bestLen)
                    {
                        bestStartState = runStartState;
                        bestLen = runLen;
                        bestMinOffset = runStartOffset;
                    }
                    if (opcode == OP_MATCH)
                    {
                        break;
                    }
                    runLen = 0;
                    if (opcode == OP_CLASS || opcode == OP_ANY)
                    {
                        minOffset++;
                        curr = this.out[curr];
                    }
                    else if (opcode == OP_SPLIT)
                    {
                        curr = (curr == this.trailingDotStarState)
                                ? this.out1[curr]
                                : (this.arg[curr] == 3
                                        ? this.out[curr]
                                        : (this.arg[curr] == 1 ? this.out[curr] : this.out1[curr]));
                    }
                    else
                    {
                        curr = this.out[curr];
                    }
                }
            }
            if (bestStartState != NONE)
            {
                StringBuilder sb = this.sc.prefixSb;
                sb.setLength(0);
                int s = bestStartState;
                while (sb.length() < bestLen)
                {
                    if (this.op[s] == OP_CHAR)
                    {
                        sb.append((char) this.arg[s]);
                    }
                    s = this.out[s];
                }
                String req = sb.toString();
                this.nativeRequiredSubstr = req;
                this.nativeReqFirstChar = req.charAt(0);
                this.nativeReqMinOffset = bestMinOffset;
            }
        }

        private boolean isOverlapTerminalCharLoop(int splitState, int rawBody, int exitTarget)
        {
            int opBody = this.op[rawBody];
            if ((opBody != OP_CHAR && opBody != OP_CLASS && opBody != OP_ANY)
                    || this.out[rawBody] != splitState
                    || this.op[exitTarget] != OP_CHAR)
            {
                return false;
            }
            int afterTerm = skipSaves(this.out[exitTarget]);
            return this.op[afterTerm] == OP_MATCH || afterTerm == this.trailingDotStarState;
        }

        private boolean checkDeterministicChain()
        {
            if (this.hasBol || this.hasAlternation || this.hasAmbiguousReluctant || this.hasAmbiguousGreedyExit)
            {
                return false;
            }
            int curr = this.startState;
            while (this.op[curr] != OP_MATCH)
            {
                if (this.op[curr] == OP_SPLIT)
                {
                    if (curr == this.trailingDotStarState)
                    {
                        curr = this.out1[curr];
                    }
                    else if (this.arg[curr] == 3)
                    {
                        curr = this.out[curr];
                    }
                    else
                    {
                        boolean rel = (this.arg[curr] == 1);
                        int body = rel ? this.out1[curr] : this.out[curr];
                        int exit = rel ? this.out[curr] : this.out1[curr];
                        int opBody = this.op[body];
                        if ((opBody != OP_CHAR && opBody != OP_CLASS && opBody != OP_ANY)
                                || (this.out[body] != curr && this.out[body] != exit))
                        {
                            return false;
                        }
                        curr = exit;
                    }
                }
                else
                {
                    curr = this.out[curr];
                }
            }
            return true;
        }

        private int skipSaves(int s)
        {
            while (this.op[s] == OP_SAVE)
            {
                s = this.out[s];
            }
            return s;
        }

        private boolean isSingleAtomDisjointLoop(int splitState, int bodyTarget, int exitTarget)
        {
            int opBody = this.op[bodyTarget];
            int opExit = this.op[exitTarget];
            if ((opBody != OP_CHAR && opBody != OP_CLASS && opBody != OP_ANY)
                    || (opExit != OP_CHAR && opExit != OP_CLASS && opExit != OP_ANY))
            {
                return false;
            }
            int afterBody = skipSaves(this.out[bodyTarget]);
            if (afterBody != splitState && afterBody != exitTarget)
            {
                return false;
            }
            if (opExit == OP_CHAR)
            {
                return !matchesAtomChar(bodyTarget, (char) this.arg[exitTarget]);
            }
            if (opBody == OP_CHAR)
            {
                return !matchesAtomChar(exitTarget, (char) this.arg[bodyTarget]);
            }
            if (opBody == OP_CLASS && opExit == OP_CLASS)
            {
                int cb = this.arg[bodyTarget];
                int ce = this.arg[exitTarget];
                if (this.classNegated[cb] && this.classNegated[ce])
                {
                    return false;
                }
                int baseB = cb << 2;
                int baseE = ce << 2;
                for (int w = 0; w < 4; w++)
                {
                    if ((this.classBits[baseB + w] & this.classBits[baseE + w]) != 0L)
                    {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        private void walkInitialClosure(int state, int[] vis, long[] masks, int[] flags)
        {
            if (state == NONE || vis[state] != 0)
            {
                return;
            }
            vis[state] = 1;

            int opcode = this.op[state];
            switch (opcode)
            {
                case OP_SAVE -> walkInitialClosure(this.out[state], vis, masks, flags);
                case OP_SPLIT ->
                {
                    if (state != this.trailingDotStarState)
                    {
                        walkInitialClosure(this.out[state], vis, masks, flags);
                    }
                    walkInitialClosure(this.out1[state], vis, masks, flags);
                }
                case OP_BOL -> flags[0] |= 2;
                case OP_CHAR ->
                {
                    flags[0] |= 1;
                    int ch = this.arg[state];
                    if (ch < 256)
                    {
                        masks[ch >>> 6] |= (1L << (ch & 63));
                    }
                    else
                    {
                        flags[0] |= 2;
                    }
                }
                case OP_CLASS ->
                {
                    flags[0] |= 1;
                    int cid = this.arg[state];
                    if (this.classNegated[cid])
                    {
                        flags[0] |= 2;
                    }
                    else
                    {
                        int base = cid << 2;
                        masks[0] |= this.classBits[base];
                        masks[1] |= this.classBits[base + 1];
                        masks[2] |= this.classBits[base + 2];
                        masks[3] |= this.classBits[base + 3];
                    }
                }
                default -> flags[0] |= 3; // OP_ANY, OP_EOL, OP_MATCH
            }
        }

        /**
         * Extracts an unconditional leading literal prefix and/or secondary literal substring
         * from a validated fallback pattern to enable SIMD {@link String#indexOf} pre-filtering
         * before invoking {@link java.util.regex.Matcher}.
         */
        private void extractFallbackLiterals(StringBuilder sb)
        {
            String pat = this.pattern;
            int len = pat.length();
            int[] openStack = new int[16];
            boolean[] unconditionalGroup = new boolean[len];
            boolean[] hasPipeAtDepth = new boolean[17];
            int depth = 0;

            for (int i = 0; i < len; i++)
            {
                char c = pat.charAt(i);
                if (c == '\\')
                {
                    char esc = pat.charAt(++i);
                    if (esc == 'Q' || esc == 'G')
                    {
                        return;
                    }
                    continue;
                }
                if (c == '[')
                {
                    int close = scanFallbackBracket(pat, i);
                    if (close < 0)
                    {
                        return;
                    }
                    i = close;
                    continue;
                }
                if (c == '(')
                {
                    boolean transparent = true;
                    if (pat.charAt(i + 1) == '?')
                    {
                        char kind = pat.charAt(i + 2);
                        if (kind == ':')
                        {
                            i += 2;
                        }
                        else if (kind == '=' || kind == '!')
                        {
                            transparent = false;
                            i += 2;
                        }
                        else if (kind == '<' && (pat.charAt(i + 3) == '=' || pat.charAt(i + 3) == '!'))
                        {
                            transparent = false;
                            i += 3;
                        }
                        else
                        {
                            return;
                        }
                    }
                    if (depth >= 16)
                    {
                        return;
                    }
                    openStack[depth++] = transparent ? i : -1;
                    hasPipeAtDepth[depth] = false;
                }
                else if (c == ')')
                {
                    int openIdx = openStack[--depth];
                    boolean optionalAfter = (i + 1 < len) && isOptionalQuantifierStart(pat.charAt(i + 1));
                    if (openIdx >= 0 && !hasPipeAtDepth[depth + 1] && !optionalAfter)
                    {
                        unconditionalGroup[openIdx] = true;
                    }
                }
                else if (c == '|')
                {
                    hasPipeAtDepth[depth] = true;
                }
            }

            if (hasPipeAtDepth[0])
            {
                return;
            }

            sb.setLength(0);
            boolean atStart = true;
            int nonUncondDepth = 0;
            String[] runs = { null, null }; // runs[0] = leadPrefix, runs[1] = bestRequired

            for (int i = 0; i < len; i++)
            {
                char c = pat.charAt(i);
                if (c == '(')
                {
                    if (!unconditionalGroup[i] || nonUncondDepth > 0)
                    {
                        flushFallbackRun(sb, atStart, runs);
                        atStart = false;
                        nonUncondDepth++;
                    }
                    if (pat.charAt(i + 1) == '?')
                    {
                        i += (pat.charAt(i + 2) == '<') ? 3 : 2;
                    }
                    continue;
                }
                if (c == ')')
                {
                    if (nonUncondDepth > 0)
                    {
                        nonUncondDepth--;
                    }
                    if (i + 1 < len && isAnyQuantifierStart(pat.charAt(i + 1)))
                    {
                        flushFallbackRun(sb, atStart, runs);
                        atStart = false;
                        i = skipFallbackQuantifier(pat, i + 1, len) - 1;
                    }
                    continue;
                }
                if (nonUncondDepth > 0)
                {
                    if (c == '\\')
                    {
                        i++;
                    }
                    else if (c == '[')
                    {
                        i = scanFallbackBracket(pat, i);
                    }
                    continue;
                }
                if (c == '[')
                {
                    flushFallbackRun(sb, atStart, runs);
                    atStart = false;
                    i = scanFallbackBracket(pat, i);
                    if (i + 1 < len && isAnyQuantifierStart(pat.charAt(i + 1)))
                    {
                        i = skipFallbackQuantifier(pat, i + 1, len) - 1;
                    }
                    continue;
                }

                int litChar = -1;
                int nextIdx = i + 1;
                if (c == '\\')
                {
                    char esc = pat.charAt(i + 1);
                    nextIdx = i + 2;
                    if (!((esc >= 'a' && esc <= 'z') || (esc >= 'A' && esc <= 'Z') || (esc >= '0' && esc <= '9')))
                    {
                        litChar = esc;
                    }
                }
                else if (c != '.' && c != '^' && c != '$')
                {
                    litChar = c;
                }

                boolean hasQuant = (nextIdx < len) && isAnyQuantifierStart(pat.charAt(nextIdx));
                if (litChar >= 0 && !hasQuant)
                {
                    sb.append((char) litChar);
                    i = nextIdx - 1;
                }
                else
                {
                    flushFallbackRun(sb, atStart, runs);
                    atStart = false;
                    i = hasQuant ? (skipFallbackQuantifier(pat, nextIdx, len) - 1) : (nextIdx - 1);
                }
            }

            flushFallbackRun(sb, atStart, runs);

            if (runs[0] != null)
            {
                this.fallbackPrefix = runs[0];
                this.fallbackFirstChar = runs[0].charAt(0);
            }
            if (runs[1] != null)
            {
                this.fallbackRequiredSubstr = runs[1];
                this.fallbackRequiredFirstChar = runs[1].charAt(0);
            }
        }

        private static void flushFallbackRun(StringBuilder sb, boolean atStart, String[] runs)
        {
            if (!sb.isEmpty())
            {
                if (atStart)
                {
                    runs[0] = sb.toString();
                }
                else if (runs[1] == null || sb.length() > runs[1].length())
                {
                    runs[1] = sb.toString();
                }
                sb.setLength(0);
            }
        }

        private static int scanFallbackBracket(String pat, int openBracket)
        {
            int i = openBracket + 1;
            if (pat.charAt(i) == '^')
            {
                i++;
            }
            if (pat.charAt(i) == ']')
            {
                i++;
            }
            while (pat.charAt(i) != ']')
            {
                char bc = pat.charAt(i);
                if (bc == '[')
                {
                    return -1;
                }
                if (bc == '\\')
                {
                    i++;
                }
                i++;
            }
            return i;
        }

        private static boolean isOptionalQuantifierStart(char c)
        {
            return c == '*' || c == '?' || c == '{';
        }

        private static boolean isAnyQuantifierStart(char c)
        {
            return c == '*' || c == '+' || c == '?' || c == '{';
        }

        private static int skipFallbackQuantifier(String pat, int qStart, int len)
        {
            int i = qStart;
            if (pat.charAt(i) == '{')
            {
                i = pat.indexOf('}', i + 1) + 1;
            }
            else
            {
                i++;
            }
            if (i < len && (pat.charAt(i) == '?' || pat.charAt(i) == '+'))
            {
                i++;
            }
            return i;
        }

        private int newState(int opcode, int argument, int outState, int out1State)
        {
            CompilerScratch s = this.sc;
            if (s.stateCount == s.op.length)
            {
                int newCap = s.op.length << 1;
                s.op = Arrays.copyOf(s.op, newCap);
                s.arg = Arrays.copyOf(s.arg, newCap);
                s.out = Arrays.copyOf(s.out, newCap);
                s.out1 = Arrays.copyOf(s.out1, newCap);
            }
            int id = s.stateCount++;
            s.op[id] = opcode;
            s.arg[id] = argument;
            s.out[id] = outState;
            s.out1[id] = out1State;
            return id;
        }

        private void patch(long frag, int targetState)
        {
            CompilerScratch s = this.sc;
            int offset = fragOffset(frag);
            int end = offset + fragLen(frag);
            for (int i = offset; i < end; i++)
            {
                int ptr = s.dangling[i];
                int state = ptr >>> 1;
                if ((ptr & 1) == 0)
                {
                    s.out[state] = targetState;
                }
                else
                {
                    s.out1[state] = targetState;
                }
            }
        }

        /**
         * Parses an alternation expression (expr1 | expr2 | ...).
         *
         * @return packed NFA fragment representing the alternation
         */
        private long parseExpr()
        {
            long left = parseConcat();
            while (pos < pattern.length() && pattern.charAt(pos) == '|')
            {
                hasAlternation = true;
                pos++;
                long right = parseConcat();
                int s = newState(OP_SPLIT, 2, fragStart(left), fragStart(right));
                left = packFrag(s, fragOffset(left), fragLen(left) + fragLen(right));
            }
            return left;
        }

        /**
         * Parses a concatenated sequence of regex repeat atoms.
         *
         * @return packed NFA fragment representing the concatenated sequence
         */
        private long parseConcat()
        {
            long first = NO_FRAG;
            while (pos < pattern.length())
            {
                char c = pattern.charAt(pos);
                if (c == ')' || c == '|')
                {
                    break;
                }
                long next = parseRepeat();
                first = (first == NO_FRAG) ? next : concatFrags(first, next);
            }
            if (first == NO_FRAG)
            {
                int s = newState(OP_SPLIT, 3, NONE, NONE);
                return singleFrag(s, s << 1);
            }
            return first;
        }

        /**
         * Parses an atom followed by an optional quantifier (*, +, ?, {m,n}, with greedy or reluctant modifiers).
         *
         * @return packed NFA fragment representing the repeated atom
         */
        private long parseRepeat()
        {
            int atomStartPos = pos;
            int savedGroupCount = totalGroups;
            long atom = parseAtom();
            if (pos >= pattern.length())
            {
                return atom;
            }

            char c = pattern.charAt(pos);
            return switch (c)
            {
                case '*' ->
                {
                    rejectAnchorQuantifier(atom, c);
                    pos++;
                    boolean reluctant = consumeQuantifierModifier();
                    yield applyStar(atom, reluctant);
                }
                case '+' ->
                {
                    rejectAnchorQuantifier(atom, c);
                    pos++;
                    boolean reluctant = consumeQuantifierModifier();
                    yield applyPlus(atom, reluctant);
                }
                case '?' ->
                {
                    rejectAnchorQuantifier(atom, c);
                    pos++;
                    boolean reluctant = consumeQuantifierModifier();
                    yield applyQuestion(atom, reluctant);
                }
                case '{' ->
                {
                    rejectAnchorQuantifier(atom, c);
                    pos++;
                    int min = parseNumber();
                    int max = min;
                    if (pos < pattern.length() && pattern.charAt(pos) == ',')
                    {
                        pos++;
                        max = (pos < pattern.length() && pattern.charAt(pos) == '}') ? -1 : parseNumber();
                    }
                    if (pos >= pattern.length() || pattern.charAt(pos) != '}')
                    {
                        throw new PatternSyntaxException("Unclosed counted closure '{'", pattern, pos);
                    }
                    pos++;
                    if (max > 256 || min > 256)
                    {
                        throw new UnsupportedRegexFeatureException(
                                "Quantifier bound > 256 exceeds fast unroll limit", pattern, atomStartPos);
                    }
                    boolean reluctant = consumeQuantifierModifier();
                    yield buildBoundedRepeat(atom, atomStartPos, savedGroupCount, pos, min, max, reluctant);
                }
                default -> atom;
            };
        }

        private void rejectAnchorQuantifier(long atom, char quantifier)
        {
            int opcode = this.sc.op[fragStart(atom)];
            if (opcode == OP_BOL || opcode == OP_EOL)
            {
                throw new PatternSyntaxException("Dangling meta-character '" + quantifier + "'", pattern, pos);
            }
        }

        private boolean consumeQuantifierModifier()
        {
            if (pos < pattern.length())
            {
                char mod = pattern.charAt(pos);
                if (mod == '?')
                {
                    pos++;
                    return true;
                }
                if (mod == '+')
                {
                    throw new UnsupportedRegexFeatureException(
                            "Possessive quantifiers (*+, ++, ?+, {m,n}+) are not supported in fast engine",
                            pattern, pos);
                }
            }
            return false;
        }

        private long applyStar(long f, boolean reluctant)
        {
            int fStart = fragStart(f);
            int s = reluctant
                    ? newState(OP_SPLIT, 1, NONE, fStart)
                    : newState(OP_SPLIT, 0, fStart, NONE);
            patch(f, s);
            this.sc.danglingTop = fragOffset(f);
            int exitPtr = reluctant ? (s << 1) : ((s << 1) | 1);
            return singleFrag(s, exitPtr);
        }

        private long applyPlus(long f, boolean reluctant)
        {
            int fStart = fragStart(f);
            int s = reluctant
                    ? newState(OP_SPLIT, 1, NONE, fStart)
                    : newState(OP_SPLIT, 0, fStart, NONE);
            patch(f, s);
            this.sc.danglingTop = fragOffset(f);
            int exitPtr = reluctant ? (s << 1) : ((s << 1) | 1);
            return singleFrag(fStart, exitPtr);
        }

        private long applyQuestion(long f, boolean reluctant)
        {
            int fStart = fragStart(f);
            int s = reluctant
                    ? newState(OP_SPLIT, 1, NONE, fStart)
                    : newState(OP_SPLIT, 0, fStart, NONE);
            int exitPtr = reluctant ? (s << 1) : ((s << 1) | 1);
            pushDangling(exitPtr);
            return packFrag(s, fragOffset(f), fragLen(f) + 1);
        }

        private long buildBoundedRepeat(
                long firstAtom,
                int atomStartPos,
                int baseGroupCount,
                int endPos,
                int min,
                int max,
                boolean reluctant)
        {
            if (max != -1 && max < min)
            {
                throw new PatternSyntaxException("Illegal repetition range {" + min + "," + max + "}", pattern, atomStartPos);
            }
            int finalGroupCount = this.totalGroups;
            int origStart = fragStart(firstAtom);
            int origOp = this.sc.op[origStart];
            int singleOp = (this.sc.out[origStart] == NONE && origOp >= OP_CHAR && origOp <= OP_CLASS) ? origOp : NONE;
            int singleArg = this.sc.arg[origStart];
            long result = NO_FRAG;

            for (int i = 0; i < min; i++)
            {
                long copy = (i == 0) ? firstAtom : cloneAtom(singleOp, singleArg, atomStartPos, baseGroupCount);
                result = (result == NO_FRAG) ? copy : concatFrags(result, copy);
            }

            if (max == -1)
            {
                long starCopy = (min == 0)
                        ? applyStar(firstAtom, reluctant)
                        : applyStar(cloneAtom(singleOp, singleArg, atomStartPos, baseGroupCount), reluctant);
                result = (result == NO_FRAG) ? starCopy : concatFrags(result, starCopy);
            }
            else if (max > min)
            {
                for (int i = min; i < max; i++)
                {
                    long copy = (i == 0) ? firstAtom : cloneAtom(singleOp, singleArg, atomStartPos, baseGroupCount);
                    long opt = applyQuestion(copy, reluctant);
                    result = (result == NO_FRAG) ? opt : concatFrags(result, opt);
                }
            }
            else if (min == 0)
            {
                this.sc.danglingTop = fragOffset(firstAtom);
                int s = newState(OP_SPLIT, 3, NONE, NONE);
                result = singleFrag(s, s << 1);
            }

            this.totalGroups = finalGroupCount;
            this.pos = endPos;
            return result;
        }

        private long cloneAtom(int singleOp, int singleArg, int atomStartPos, int baseGroupCount)
        {
            if (singleOp != NONE)
            {
                int s = newState(singleOp, singleArg, NONE, NONE);
                return singleFrag(s, s << 1);
            }
            int savedPos = this.pos;
            this.pos = atomStartPos;
            this.totalGroups = baseGroupCount;
            long copy = parseAtom();
            this.pos = savedPos;
            return copy;
        }

        private long concatFrags(long first, long next)
        {
            CompilerScratch s = this.sc;
            patch(first, fragStart(next));
            int dstOffset = fragOffset(first);
            int srcOffset = fragOffset(next);
            int nextLen = fragLen(next);
            System.arraycopy(s.dangling, srcOffset, s.dangling, dstOffset, nextLen);
            s.danglingTop = dstOffset + nextLen;
            return packFrag(fragStart(first), dstOffset, nextLen);
        }

        private int parseNumber()
        {
            int start = pos;
            int val = 0;
            while (pos < pattern.length() && Character.isDigit(pattern.charAt(pos)))
            {
                val = Math.min(1000, val * 10 + (pattern.charAt(pos) - '0'));
                pos++;
            }
            if (pos == start)
            {
                throw new PatternSyntaxException("Expected repetition number", pattern, pos);
            }
            return val;
        }

        private long parseAtom()
        {
            char c = pattern.charAt(pos++);
            return switch (c)
            {
                case '(' ->
                {
                    boolean capture = true;
                    if (pos < pattern.length() && pattern.charAt(pos) == '?')
                    {
                        if (pos + 1 < pattern.length() && pattern.charAt(pos + 1) == ':')
                        {
                            pos += 2;
                            capture = false;
                        }
                        else
                        {
                            throw new UnsupportedRegexFeatureException(
                                    "Unsupported group construct '(?" + (pos + 1 < pattern.length() ? pattern.charAt(pos + 1) : "") + "...)'",
                                    pattern, pos - 1);
                        }
                    }
                    int g = capture ? totalGroups++ : -1;
                    long f = parseExpr();
                    if (pos >= pattern.length())
                    {
                        throw new PatternSyntaxException("Unclosed group '('", pattern, pos);
                    }
                    pos++;
                    if (!capture)
                    {
                        yield f;
                    }

                    int saveStart = newState(OP_SAVE, g << 1, fragStart(f), NONE);
                    int saveEnd = newState(OP_SAVE, (g << 1) + 1, NONE, NONE);
                    patch(f, saveEnd);
                    this.sc.danglingTop = fragOffset(f);
                    yield singleFrag(saveStart, saveEnd << 1);
                }
                case '[' -> parseCharClass();
                case '.' ->
                {
                    int s = newState(OP_ANY, 0, NONE, NONE);
                    yield singleFrag(s, s << 1);
                }
                case '^' ->
                {
                    hasBol = true;
                    int s = newState(OP_BOL, 0, NONE, NONE);
                    yield singleFrag(s, s << 1);
                }
                case '$' ->
                {
                    hasEol = true;
                    int s = newState(OP_EOL, 0, NONE, NONE);
                    yield singleFrag(s, s << 1);
                }
                case '\\' ->
                {
                    if (pos >= pattern.length())
                    {
                        throw new PatternSyntaxException("Trailing '\\'", pattern, pos - 1);
                    }
                    char esc = pattern.charAt(pos);
                    if (isShorthandClass(esc))
                    {
                        pos++;
                        int classId = allocClass();
                        boolean sawNeg = applyShorthandBits(classId << 2, esc);
                        finalizeCharClass(classId, false, sawNeg);
                        int s = newState(OP_CLASS, classId, NONE, NONE);
                        yield singleFrag(s, s << 1);
                    }
                    int s = newState(OP_CHAR, parseEscapeChar(), NONE, NONE);
                    yield singleFrag(s, s << 1);
                }
                case '*', '+', '?', '{', ')' ->
                    throw new PatternSyntaxException("Dangling meta-character '" + c + "'", pattern, pos - 1);
                default ->
                {
                    int s = newState(OP_CHAR, c, NONE, NONE);
                    yield singleFrag(s, s << 1);
                }
            };
        }

        // =========================================================================
        // CHARACTER CLASSES & ESCAPE VALIDATION
        // =========================================================================

        private int allocClass()
        {
            CompilerScratch s = this.sc;
            if (s.classCount == s.classNegated.length)
            {
                s.classBits = Arrays.copyOf(s.classBits, s.classBits.length << 1);
                s.classNegated = Arrays.copyOf(s.classNegated, s.classNegated.length << 1);
                s.classExcludedChar = Arrays.copyOf(s.classExcludedChar, s.classExcludedChar.length << 1);
            }
            int classId = s.classCount++;
            int base = classId << 2;
            Arrays.fill(s.classBits, base, base + 4, 0L);
            s.classNegated[classId] = false;
            s.classExcludedChar[classId] = -1;
            return classId;
        }

        private void finalizeCharClass(int classId, boolean negate, boolean sawNegShorthand)
        {
            CompilerScratch s = this.sc;
            int base = classId << 2;
            if (negate)
            {
                s.classBits[base]     = ~s.classBits[base];
                s.classBits[base + 1] = ~s.classBits[base + 1];
                s.classBits[base + 2] = ~s.classBits[base + 2];
                s.classBits[base + 3] = ~s.classBits[base + 3];
            }
            boolean matchesHi = negate ? !sawNegShorthand : sawNegShorthand;
            s.classNegated[classId] = matchesHi;
            s.classExcludedChar[classId] = computeExcludedAsciiChar(s.classBits, base, matchesHi);
        }

        private static int computeExcludedAsciiChar(long[] bits, int base, boolean matchesHi)
        {
            if (!matchesHi)
            {
                return -1;
            }
            int excl = -1;
            for (int w = 0; w < 4; w++)
            {
                long inv = ~bits[base + w];
                if (inv != 0L)
                {
                    if (excl >= 0 || (inv & (inv - 1L)) != 0L)
                    {
                        return -1;
                    }
                    excl = (w << 6) + Long.numberOfTrailingZeros(inv);
                }
            }
            return excl;
        }

        private static boolean isShorthandClass(char c)
        {
            return switch (c)
            {
                case 'd', 'D', 'w', 'W', 's', 'S' -> true;
                default -> false;
            };
        }

        private boolean applyShorthandBits(int base, char shorthand)
        {
            long[] bits = this.sc.classBits;
            return switch (shorthand)
            {
                case 'd' ->
                {
                    bits[base] |= DIGIT_MASK_W0;
                    yield false;
                }
                case 'D' ->
                {
                    bits[base]     |= ~DIGIT_MASK_W0;
                    bits[base + 1] = -1L;
                    bits[base + 2] = -1L;
                    bits[base + 3] = -1L;
                    yield true;
                }
                case 'w' ->
                {
                    bits[base]     |= DIGIT_MASK_W0;
                    bits[base + 1] |= WORD_MASK_W1;
                    yield false;
                }
                case 'W' ->
                {
                    bits[base]     |= ~DIGIT_MASK_W0;
                    bits[base + 1] |= ~WORD_MASK_W1;
                    bits[base + 2] = -1L;
                    bits[base + 3] = -1L;
                    yield true;
                }
                case 's' ->
                {
                    bits[base] |= SPACE_MASK_W0;
                    yield false;
                }
                default ->
                {
                    // 'S' (non-whitespace)
                    bits[base]     |= ~SPACE_MASK_W0;
                    bits[base + 1] = -1L;
                    bits[base + 2] = -1L;
                    bits[base + 3] = -1L;
                    yield true;
                }
            };
        }

        private void setSingleBit(int base, char c)
        {
            if (c >= 256)
            {
                throw new UnsupportedRegexFeatureException(
                        "Non-ASCII character '" + c + "' inside character class requires Unicode engine",
                        pattern, pos - 1);
            }
            this.sc.classBits[base + (c >>> 6)] |= (1L << (c & 63));
        }

        private void setRangeBits(int base, char start, char end)
        {
            if (start > end)
            {
                throw new PatternSyntaxException("Illegal character range " + start + "-" + end, pattern, pos - 1);
            }
            if (end >= 256)
            {
                throw new UnsupportedRegexFeatureException(
                        "Non-ASCII character range requires Unicode engine", pattern, pos - 1);
            }
            long[] bits = this.sc.classBits;
            for (int w = start >>> 6, endWord = end >>> 6; w <= endWord; w++)
            {
                int lo = Math.max(start, w << 6) & 63;
                int hi = Math.min(end, (w << 6) + 63) & 63;
                bits[base + w] |= (-1L >>> (63 - (hi - lo))) << lo;
            }
        }

        /**
         * Parses a character class bracket expression [...] or negated [^...].
         *
         * @return packed NFA fragment representing the character class state
         */
        private long parseCharClass()
        {
            boolean negate = false;
            if (pos < pattern.length() && pattern.charAt(pos) == '^')
            {
                negate = true;
                pos++;
            }

            int classId = allocClass();
            int base = classId << 2;
            boolean sawNegShorthand = false;

            boolean first = true;
            while (pos < pattern.length() && (first || pattern.charAt(pos) != ']'))
            {
                first = false;
                char ch = pattern.charAt(pos);
                if (ch == '[' || (ch == '&' && pos + 1 < pattern.length() && pattern.charAt(pos + 1) == '&'))
                {
                    throw new UnsupportedRegexFeatureException(
                            "Nested character classes or intersections (e.g. [a-z&&[^m]]) are not supported in fast engine",
                            pattern, pos);
                }
                if (ch == '\\')
                {
                    pos++;
                    if (pos >= pattern.length())
                    {
                        throw new PatternSyntaxException("Trailing '\\' inside [...]'", pattern, pos - 1);
                    }
                    char esc = pattern.charAt(pos);
                    if (isShorthandClass(esc))
                    {
                        pos++;
                        sawNegShorthand |= applyShorthandBits(base, esc);
                        continue;
                    }
                    char startChar = parseEscapeChar();
                    parseClassRangeOrSingle(base, startChar);
                }
                else
                {
                    char startChar = pattern.charAt(pos++);
                    parseClassRangeOrSingle(base, startChar);
                }
            }

            if (pos >= pattern.length())
            {
                throw new PatternSyntaxException("Unclosed character class '['", pattern, pos);
            }
            pos++;
            finalizeCharClass(classId, negate, sawNegShorthand);

            int s = newState(OP_CLASS, classId, NONE, NONE);
            return singleFrag(s, s << 1);
        }

        private void parseClassRangeOrSingle(int base, char startChar)
        {
            if (pos + 1 < pattern.length() && pattern.charAt(pos) == '-' && pattern.charAt(pos + 1) != ']')
            {
                pos++; // consume '-'
                char endChar;
                if (pattern.charAt(pos) == '\\')
                {
                    pos++;
                    endChar = parseEscapeChar();
                }
                else
                {
                    endChar = pattern.charAt(pos++);
                }
                setRangeBits(base, startChar, endChar);
            }
            else
            {
                setSingleBit(base, startChar);
            }
        }

        /**
         * Parses and validates an escaped character following a backslash (\).
         *
         * @return unescaped literal character
         */
        private char parseEscapeChar()
        {
            int escPos = pos - 1;
            if (pos >= pattern.length())
            {
                throw new PatternSyntaxException("Trailing '\\'", pattern, escPos);
            }
            char c = pattern.charAt(pos++);
            return switch (c)
            {
                case 'a' -> '\u0007';
                case 'e' -> '\u001B';
                case 'f' -> '\f';
                case 'n' -> '\n';
                case 'r' -> '\r';
                case 't' -> '\t';
                case '1', '2', '3', '4', '5', '6', '7', '8', '9' ->
                    throw new UnsupportedRegexFeatureException(
                            "Backreference '\\" + c + "' requires backtracking engine", pattern, escPos);
                case 'b', 'B', 'A', 'G', 'Z', 'z' ->
                    throw new UnsupportedRegexFeatureException(
                            "Boundary assertion '\\" + c + "' is not supported in fast engine", pattern, escPos);
                case 'p', 'P', 'x', 'u', '0', 'Q', 'E', 'k', 'h', 'H', 'v', 'V', 'R', 'X' ->
                    throw new UnsupportedRegexFeatureException(
                            "Advanced escape sequence '\\" + c + "' is not supported in fast engine", pattern, escPos);
                default ->
                {
                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))
                    {
                        throw new UnsupportedRegexFeatureException(
                                "Unrecognized alphabetic escape '\\" + c + "'", pattern, escPos);
                    }
                    yield c;
                }
            };
        }
    }

    // =========================================================================
    // HYBRID SIMD-PREFIX + LAZY-DFA + PIKE VM + JDK FALLBACK MATCHER
    // =========================================================================

    /**
     * Single-threaded state machine execution engine combining SIMD prefix acceleration,
     * Lazy DFA cache, and priority-ordered PikeVM.
     *
     * <p>Implements {@link FastMatchResult} (and {@link java.util.regex.MatchResult}) directly
     * alongside {@link #reset(CharSequence)}, {@link #matches()}, {@link #find()}, and
     * {@link #find(int)} to provide a zero-allocation drop-in counterpart to
     * {@link java.util.regex.Matcher}.</p>
     *
     * @author Gemini
     * @author Rene Schwietzke
     */
    public static final class Matcher implements FastMatchResult
    {
        private static final int UNKNOWN_DFA        = 0;
        private static final int DEAD_DFA           = 1;
        private static final int FIRST_DFA_ID       = 2;
        private static final int INITIAL_DFA_STATES = 16;
        private static final int MAX_DFA_STATES     = 257;
        private static final int HASH_SIZE          = 512;
        private static final int HASH_MASK          = HASH_SIZE - 1;

        /**
         * Lazily allocated, dynamically growable (16 up to 255 active states) append-only
         * Lazy DFA subset-construction tables.
         * Using {@code UNKNOWN_DFA == 0} allows newly allocated {@code trans} and {@code hashTable}
         * arrays to use JVM zero-initialization directly without {@code Arrays.fill} overhead.
         */
        private static final class DfaBuffers
        {
            int capacity = INITIAL_DFA_STATES;
            int[] trans = new int[INITIAL_DFA_STATES << 8];
            long[] stateSets;
            byte[] flags = new byte[INITIAL_DFA_STATES];
            final int[] hashTable = new int[HASH_SIZE];
            final long[] scratchSet;
            final long[] transFromSet;
            int stateCount = FIRST_DFA_ID;
            int startExact;
            int afterPrefix;

            DfaBuffers(int wordsPerSet)
            {
                this.stateSets = new long[INITIAL_DFA_STATES * wordsPerSet];
                this.scratchSet = new long[wordsPerSet];
                this.transFromSet = new long[wordsPerSet];
            }
        }

        /**
         * Lazily allocated double-buffered active thread queues and flat epsilon workstack for PikeVM.
         */
        private static final class PikeBuffers
        {
            int[] clist;
            int[] nlist;
            int[] cslots;
            int[] nslots;
            final int[] emptySlots;
            final int[] epsStack;

            PikeBuffers(int stateCount, int slotCount)
            {
                this.clist = new int[stateCount];
                this.nlist = new int[stateCount];
                this.cslots = new int[stateCount * slotCount];
                this.nslots = new int[stateCount * slotCount];
                this.emptySlots = new int[slotCount];
                this.epsStack = new int[(stateCount * 3) + 4];
                Arrays.fill(this.emptySlots, -1);
            }
        }

        /**
         * Lazily allocated auxiliary state for >3 capturing groups, fallback JDK Matcher, and findAll cursor.
         */
        private final class AuxState
        {
            int[] overflowSlots;
            SliceView[] overflowViews;
            java.util.regex.Matcher jdkFallbackMatcher;
            MatchCursor reusableCursor;
        }

        private final Pattern regex;
        private CharSequence boundInput = "";
        private int nextSearchIndex;
        private int stepId = 1; // Start from 1 so 0 is unvisited
        private boolean ownMatched;
        private boolean jdkMatcherBound;
        private boolean dfaMatchesWarmed;

        // Inline capture slots for groups 0..3 (totalGroups <= 4), avoiding heap array allocations
        private int s0;
        private int s1;
        private int s2;
        private int s3;
        private int s4;
        private int s5;
        private int s6;
        private int s7;

        // Inline flyweight SliceViews for groups 0..3, allocated lazily per requested group
        private SliceView view0;
        private SliceView view1;
        private SliceView view2;
        private SliceView view3;

        // Lazily allocated execution tiers and cold auxiliary state
        private DfaBuffers dfa;
        private PikeBuffers pike;
        private int[] visited;
        private AuxState aux;

        private Matcher(Pattern regex)
        {
            this.regex = regex;
        }

        private Matcher(Pattern regex, CharSequence input)
        {
            this.regex = regex;
            this.boundInput = input;
        }

        private DfaBuffers ensureDfaBuffers()
        {
            DfaBuffers db = this.dfa;
            if (db == null)
            {
                db = new DfaBuffers(this.regex.wordsPerSet);
                this.dfa = db;
            }
            return db;
        }

        private static void resetDfaCache(DfaBuffers db)
        {
            Arrays.fill(db.trans, UNKNOWN_DFA);
            Arrays.fill(db.hashTable, UNKNOWN_DFA);
            db.stateCount = FIRST_DFA_ID;
            db.startExact = UNKNOWN_DFA;
            db.afterPrefix = UNKNOWN_DFA;
        }

        private PikeBuffers ensurePikeVmBuffers()
        {
            PikeBuffers pb = this.pike;
            if (pb == null)
            {
                Pattern rx = this.regex;
                ensureVisited(rx.stateCount);
                pb = new PikeBuffers(rx.stateCount, rx.slotCount);
                this.pike = pb;
            }
            return pb;
        }

        private void ensureVisited(int stateCount)
        {
            if (this.visited == null)
            {
                this.visited = new int[stateCount];
            }
        }

        private AuxState ensureAux()
        {
            AuxState a = this.aux;
            if (a == null)
            {
                a = new AuxState();
                this.aux = a;
            }
            return a;
        }

        private void ensureOverflowSlots(int neededOverflow)
        {
            AuxState a = ensureAux();
            if (a.overflowSlots == null)
            {
                a.overflowSlots = new int[neededOverflow];
            }
        }

        private java.util.regex.Matcher ensureJdkFallbackMatcher(Pattern rx)
        {
            AuxState a = ensureAux();
            java.util.regex.Matcher jm = a.jdkFallbackMatcher;
            if (jm == null)
            {
                jm = rx.fallbackPattern.matcher("");
                a.jdkFallbackMatcher = jm;
            }
            return jm;
        }

        private void setOwnSlot(int idx, int val)
        {
            switch (idx)
            {
                case 0 -> s0 = val;
                case 1 -> s1 = val;
                case 2 -> s2 = val;
                case 3 -> s3 = val;
                case 4 -> s4 = val;
                case 5 -> s5 = val;
                case 6 -> s6 = val;
                case 7 -> s7 = val;
                default -> aux.overflowSlots[idx - 8] = val;
            }
        }

        private int getOwnSlot(int idx)
        {
            return switch (idx)
            {
                case 0 -> s0;
                case 1 -> s1;
                case 2 -> s2;
                case 3 -> s3;
                case 4 -> s4;
                case 5 -> s5;
                case 6 -> s6;
                case 7 -> s7;
                default -> aux.overflowSlots[idx - 8];
            };
        }

        private SliceView getOwnView(int group)
        {
            return switch (group)
            {
                case 0 -> (view0 != null) ? view0 : (view0 = new SliceView());
                case 1 -> (view1 != null) ? view1 : (view1 = new SliceView());
                case 2 -> (view2 != null) ? view2 : (view2 = new SliceView());
                case 3 -> (view3 != null) ? view3 : (view3 = new SliceView());
                default -> getOverflowView(group - 4);
            };
        }

        private SliceView getOverflowView(int ovIdx)
        {
            AuxState a = ensureAux();
            SliceView[] vs = a.overflowViews;
            if (vs == null)
            {
                vs = new SliceView[regex.totalGroups - 4];
                a.overflowViews = vs;
            }
            SliceView v = vs[ovIdx];
            if (v == null)
            {
                v = new SliceView();
                vs[ovIdx] = v;
            }
            return v;
        }

        private void checkOwnMatchAndGroup(int group)
        {
            if (!ownMatched)
            {
                throw new IllegalStateException("No match available");
            }
            if (group < 0 || group >= regex.totalGroups)
            {
                throw new IndexOutOfBoundsException("No group " + group);
            }
        }

        private boolean failMatch(ReusableMatchResult res)
        {
            if (res != null)
            {
                res.matched = false;
            }
            else
            {
                this.ownMatched = false;
            }
            return false;
        }

        private boolean writeZeroGroupMatch(CharSequence input, int start, int end, ReusableMatchResult res)
        {
            if (res != null)
            {
                res.setZeroGroupMatch(input, start, end);
            }
            else
            {
                this.s0 = start;
                this.s1 = end;
                this.ownMatched = true;
            }
            return true;
        }

        private void writeMatchedSlots(
                CharSequence input,
                int totalGroups,
                int[] src,
                int srcOffset,
                ReusableMatchResult res)
        {
            int count = totalGroups << 1;
            if (res != null)
            {
                res.beginMatchWrite(input, totalGroups);
                copyIntSlots(src, srcOffset, res.slots, 0, count);
            }
            else
            {
                this.ownMatched = true;
                if (count > 8)
                {
                    ensureOverflowSlots(count - 8);
                }
                for (int k = 0; k < count; k++)
                {
                    setOwnSlot(k, src[srcOffset + k]);
                }
            }
        }

        private void extendMatchEndToLineEnd(CharSequence input, int len, ReusableMatchResult res)
        {
            if (res != null)
            {
                res.slots[1] = scanUntilChar(input, '\n', res.slots[1], len);
            }
            else
            {
                this.s1 = scanUntilChar(input, '\n', this.s1, len);
            }
        }

        /**
         * Returns the {@link Pattern} interpreted by this matcher.
         *
         * @return parent {@link Pattern}
         */
        public Pattern pattern()
        {
            return regex;
        }

        /**
         * Resets this matcher with a new input character sequence.
         *
         * @param input the new input character sequence
         * @return this matcher
         */
        public Matcher reset(CharSequence input)
        {
            this.boundInput = Objects.requireNonNull(input, "input");
            this.nextSearchIndex = 0;
            this.ownMatched = false;
            this.jdkMatcherBound = false;
            return this;
        }

        /**
         * Resets this matcher's search position and match state on the currently bound input.
         *
         * @return this matcher
         */
        public Matcher reset()
        {
            this.nextSearchIndex = 0;
            this.ownMatched = false;
            this.jdkMatcherBound = false;
            return this;
        }

        /**
         * Attempts to match the entire currently bound input sequence against the pattern.
         *
         * @return true if the entire bound input matches, false otherwise
         */
        public boolean matches()
        {
            Pattern rx = this.regex;
            CharSequence input = this.boundInput;
            if (rx.fallbackPattern != null)
            {
                boolean hit = matchesFallback(rx, input, input.length(), null);
                this.jdkMatcherBound = hit;
                return hit;
            }
            if (matchesNative(rx, input, input.length(), null))
            {
                int e = this.s1;
                this.nextSearchIndex = (e == 0) ? 1 : e;
                return true;
            }
            this.nextSearchIndex = 0;
            return false;
        }

        /**
         * Attempts to find the next subsequence of the bound input sequence that matches the pattern.
         *
         * @return true if a matching subsequence is found, false otherwise
         */
        public boolean find()
        {
            Pattern rx = this.regex;
            CharSequence input = this.boundInput;
            if (rx.fallbackPattern != null)
            {
                if (this.jdkMatcherBound)
                {
                    java.util.regex.Matcher jm = this.aux.jdkFallbackMatcher;
                    if (jm.find())
                    {
                        populateFromJdkMatcher(jm, input, rx.totalGroups, null);
                        return true;
                    }
                    this.ownMatched = false;
                    return false;
                }
                return findFallbackFrom(rx, input, 0, input.length(), null, true);
            }
            int len = input.length();
            int from = this.nextSearchIndex;
            if (from <= len && findNative(rx, input, from, len, null))
            {
                int s = this.s0;
                int e = this.s1;
                this.nextSearchIndex = (s == e) ? e + 1 : e;
                return true;
            }
            this.ownMatched = false;
            return false;
        }

        /**
         * Resets this matcher and attempts to find the next matching subsequence starting at the specified index.
         *
         * @param start the index in the bound input at which to start searching
         * @return true if a matching subsequence is found, false otherwise
         * @throws IndexOutOfBoundsException if {@code start} is negative or greater than the bound input length
         */
        public boolean find(int start)
        {
            Pattern rx = this.regex;
            CharSequence input = this.boundInput;
            int len = input.length();
            if (start < 0 || start > len)
            {
                throw new IndexOutOfBoundsException("Illegal start index: " + start);
            }
            this.jdkMatcherBound = false;
            if (rx.fallbackPattern != null)
            {
                return findFallbackFrom(rx, input, start, len, null, true);
            }
            if (findNative(rx, input, start, len, null))
            {
                int s = this.s0;
                int e = this.s1;
                this.nextSearchIndex = (s == e) ? e + 1 : e;
                return true;
            }
            this.ownMatched = false;
            return false;
        }

        @Override
        public boolean matched()
        {
            return ownMatched;
        }

        @Override
        public boolean hasMatch()
        {
            return ownMatched;
        }

        @Override
        public int start()
        {
            return start(0);
        }

        @Override
        public int end()
        {
            return end(0);
        }

        @Override
        public int start(int group)
        {
            checkOwnMatchAndGroup(group);
            return getOwnSlot(group << 1);
        }

        @Override
        public int end(int group)
        {
            checkOwnMatchAndGroup(group);
            return getOwnSlot((group << 1) + 1);
        }

        @Override
        public String group()
        {
            return group(0);
        }

        @Override
        public String group(int group)
        {
            int s = start(group);
            return (s >= 0) ? boundInput.subSequence(s, getOwnSlot((group << 1) + 1)).toString() : null;
        }

        @Override
        public CharSequence groupView()
        {
            return groupView(0);
        }

        @Override
        public CharSequence groupView(int group)
        {
            int s = start(group);
            if (s < 0)
            {
                return null;
            }
            return getOwnView(group).reset(boundInput, s, getOwnSlot((group << 1) + 1));
        }

        @Override
        public int groupCount()
        {
            return regex.totalGroups - 1;
        }

        @Override
        public MatchSnapshot snapshot()
        {
            int groups = regex.totalGroups - 1;
            if (!ownMatched)
            {
                return new MatchSnapshot(false, null, EMPTY_SLOTS, groups);
            }
            int count = regex.slotCount;
            int[] copy = new int[count];
            for (int k = 0; k < count; k++)
            {
                copy[k] = getOwnSlot(k);
            }
            return new MatchSnapshot(true, boundInput, copy, groups);
        }

        private int nextStep()
        {
            if (++stepId == 0)
            {
                // Wrapped around or hit zero: clear visited array and restart from 1
                if (visited != null)
                {
                    Arrays.fill(visited, 0);
                }
                stepId = 1;
            }
            return stepId;
        }

        /**
         * Attempts to match the entire input sequence against the pattern.
         *
         * @param input the character sequence to match
         * @param result the reusable result container to populate with match bounds
         * @return true if the entire input sequence matches, false otherwise
         */
        public boolean matches(CharSequence input, ReusableMatchResult result)
        {
            Objects.requireNonNull(result, "result");
            Pattern rx = this.regex;
            int len = input.length();
            if (rx.fallbackPattern != null)
            {
                return matchesFallback(rx, input, len, result);
            }
            return matchesNative(rx, input, len, result);
        }

        private boolean matchesNative(Pattern rx, CharSequence input, int len, ReusableMatchResult res)
        {
            int prefLen = rx.literalPrefixLen;
            if (prefLen > 0)
            {
                if (len < prefLen
                        || input.charAt(0) != rx.firstPrefixChar
                        || (prefLen > 1 && !startsWithLiteral(input, 0, rx.literalPrefix)))
                {
                    return failMatch(res);
                }
                if (rx.isPureLiteral)
                {
                    return (len == prefLen)
                            ? writeZeroGroupMatch(input, 0, len, res)
                            : failMatch(res);
                }
            }

            if (rx.isDeterministicChain)
            {
                return matchesDeterministicChain(rx, input, len, res);
            }

            if (!rx.hasBol && !rx.hasEol && rx.trailingDotStarState == NONE)
            {
                if (this.dfa == null && !this.dfaMatchesWarmed)
                {
                    this.dfaMatchesWarmed = true;
                    if (rx.hasClosure64 || rx.hasClosure128)
                    {
                        boolean hit = rx.hasClosure64
                                ? runBitsetNfaExact64(rx, input, len)
                                : runBitsetNfaExact128(rx, input, len);
                        if (!hit)
                        {
                            return failMatch(res);
                        }
                        if (rx.totalGroups == 1)
                        {
                            return writeZeroGroupMatch(input, 0, len, res);
                        }
                    }
                }
                else
                {
                    DfaBuffers db = ensureDfaBuffers();
                    if (!runLazyDfaExact(rx, db, input, len))
                    {
                        return failMatch(res);
                    }
                    if (rx.totalGroups == 1)
                    {
                        return writeZeroGroupMatch(input, 0, len, res);
                    }
                }
            }
            PikeBuffers pb = ensurePikeVmBuffers();
            return runPikeVmMatches(rx, pb, input, len, res);
        }

        /**
         * Scans the input sequence looking for the next matching subsequence from the start.
         *
         * @param input the character sequence to search
         * @param result the reusable result container to populate with match bounds
         * @return true if a match is found, false otherwise
         */
        public boolean find(CharSequence input, ReusableMatchResult result)
        {
            return find(input, 0, result);
        }

        /**
         * Scans the input sequence looking for the next matching subsequence starting at fromIndex.
         *
         * @param input the character sequence to search
         * @param fromIndex index at which to start searching
         * @param result the reusable result container to populate with match bounds
         * @return true if a match is found, false otherwise
         */
        public boolean find(CharSequence input, int fromIndex, ReusableMatchResult result)
        {
            Objects.requireNonNull(result, "result");
            int len = input.length();
            if (fromIndex < 0 || fromIndex > len)
            {
                result.matched = false;
                return false;
            }
            Pattern rx = this.regex;
            if (rx.fallbackPattern != null)
            {
                return findFallbackFrom(rx, input, fromIndex, len, result, false);
            }
            return findNative(rx, input, fromIndex, len, result);
        }

        private boolean findNative(
                Pattern rx,
                CharSequence input,
                int fromIndex,
                int len,
                ReusableMatchResult res)
        {
            if (rx.isPureLiteral)
            {
                int idx = indexOfLiteral(input, rx.literalPrefix, rx.firstPrefixChar, fromIndex, len);
                if (idx < 0)
                {
                    return failMatch(res);
                }
                return writeZeroGroupMatch(input, idx, idx + rx.literalPrefixLen, res);
            }

            if (rx.isDeterministicChain)
            {
                return findDeterministicChain(rx, input, fromIndex, len, res);
            }

            return findNonChain(rx, input, fromIndex, len, res);
        }

        private boolean findNonChain(
                Pattern rx,
                CharSequence input,
                int fromIndex,
                int len,
                ReusableMatchResult res)
        {
            if (rx.bolAnchored)
            {
                if (fromIndex > 0)
                {
                    return failMatch(res);
                }
                PikeBuffers pb = ensurePikeVmBuffers();
                boolean hit = runPikeVmAnchored(rx, pb, input, 0, len, len, res);
                if (hit && rx.trailingDotStarState != NONE)
                {
                    extendMatchEndToLineEnd(input, len, res);
                }
                return hit;
            }

            if (rx.hasBol)
            {
                PikeBuffers pb = ensurePikeVmBuffers();
                boolean hit = runPikeVmFind(rx, pb, input, fromIndex, len, res);
                if (hit && rx.trailingDotStarState != NONE)
                {
                    extendMatchEndToLineEnd(input, len, res);
                }
                return hit;
            }

            return findTwoPhaseDfa(rx, input, fromIndex, len, res);
        }

        private boolean matchesFallback(Pattern rx, CharSequence input, int len, ReusableMatchResult res)
        {
            String pref = rx.fallbackPrefix;
            if (pref != null)
            {
                int prefLen = pref.length();
                if (len < prefLen || input.charAt(0) != rx.fallbackFirstChar
                        || !startsWithLiteral(input, 0, pref))
                {
                    return failMatch(res);
                }
            }
            String req = rx.fallbackRequiredSubstr;
            if (req != null)
            {
                int reqFrom = (pref != null) ? pref.length() : 0;
                if (indexOfLiteral(input, req, rx.fallbackRequiredFirstChar, reqFrom, len) < 0)
                {
                    return failMatch(res);
                }
            }
            java.util.regex.Matcher jm = ensureJdkFallbackMatcher(rx);
            jm.reset(input);
            if (jm.matches())
            {
                populateFromJdkMatcher(jm, input, rx.totalGroups, res);
                return true;
            }
            return failMatch(res);
        }

        private boolean findFallbackFrom(
                Pattern rx,
                CharSequence input,
                int fromIndex,
                int len,
                ReusableMatchResult res,
                boolean bindStateful)
        {
            int searchFrom = fromIndex;
            String pref = rx.fallbackPrefix;
            if (pref != null)
            {
                searchFrom = indexOfLiteral(input, pref, rx.fallbackFirstChar, fromIndex, len);
                if (searchFrom < 0)
                {
                    return failMatch(res);
                }
            }
            String req = rx.fallbackRequiredSubstr;
            if (req != null)
            {
                int reqFrom = (pref != null) ? searchFrom + pref.length() : searchFrom;
                if (indexOfLiteral(input, req, rx.fallbackRequiredFirstChar, reqFrom, len) < 0)
                {
                    return failMatch(res);
                }
            }
            java.util.regex.Matcher jm = ensureJdkFallbackMatcher(rx);
            jm.reset(input);
            if (bindStateful)
            {
                this.jdkMatcherBound = true;
            }
            boolean hit = (searchFrom == 0) ? jm.find() : jm.find(searchFrom);
            if (hit)
            {
                populateFromJdkMatcher(jm, input, rx.totalGroups, res);
                return true;
            }
            return failMatch(res);
        }

        private void populateFromJdkMatcher(
                java.util.regex.Matcher jm,
                CharSequence input,
                int groups,
                ReusableMatchResult res)
        {
            if (res != null)
            {
                res.beginMatchWrite(input, groups);
                int[] dstSlots = res.slots;
                for (int g = 0; g < groups; g++)
                {
                    dstSlots[g << 1] = jm.start(g);
                    dstSlots[(g << 1) + 1] = jm.end(g);
                }
            }
            else
            {
                this.ownMatched = true;
                int count = groups << 1;
                if (count > 8)
                {
                    ensureOverflowSlots(count - 8);
                }
                for (int g = 0; g < groups; g++)
                {
                    setOwnSlot(g << 1, jm.start(g));
                    setOwnSlot((g << 1) + 1, jm.end(g));
                }
            }
        }

        // =====================================================================
        // SIMD LITERAL PREFIX & START-BITMASK ACCELERATORS
        // =====================================================================

        private static int indexOfLiteral(
                CharSequence input,
                String literal,
                char firstChar,
                int fromIndex,
                int len)
        {
            int litLen = literal.length();
            if (fromIndex + litLen > len)
            {
                return -1;
            }

            if (input instanceof String str)
            {
                return (litLen == 1)
                        ? str.indexOf(firstChar, fromIndex)
                        : str.indexOf(literal, fromIndex);
            }

            int limit = len - litLen;
            for (int i = fromIndex; i <= limit; i++)
            {
                if (input.charAt(i) == firstChar && (litLen == 1 || startsWithLiteral(input, i, literal)))
                {
                    return i;
                }
            }
            return -1;
        }

        private static boolean startsWithLiteral(CharSequence input, int offset, String literal)
        {
            if (input instanceof String str)
            {
                return str.startsWith(literal, offset);
            }
            int litLen = literal.length();
            for (int i = 1; i < litLen; i++)
            {
                if (input.charAt(offset + i) != literal.charAt(i))
                {
                    return false;
                }
            }
            return true;
        }

        private static int nextStartMaskIndex(Pattern rx, CharSequence input, int fromIndex, int len)
        {
            long m0 = rx.startMask0;
            long m1 = rx.startMask1;
            long m2 = rx.startMask2;
            long m3 = rx.startMask3;
            for (int i = fromIndex; i < len; i++)
            {
                char c = input.charAt(i);
                if (c < 128)
                {
                    long mask = (c < 64) ? m0 : m1;
                    if ((mask & (1L << c)) != 0L)
                    {
                        return i;
                    }
                }
                else if (c < 256)
                {
                    long mask = (c < 192) ? m2 : m3;
                    if ((mask & (1L << c)) != 0L)
                    {
                        return i;
                    }
                }
            }
            return -1;
        }

        private static int scanUntilChar(CharSequence input, char stopChar, int from, int len)
        {
            if (from >= len)
            {
                return len;
            }
            if (input instanceof String str)
            {
                int hit = str.indexOf(stopChar, from);
                return (hit < 0) ? len : hit;
            }
            for (int i = from; i < len; i++)
            {
                if (input.charAt(i) == stopChar)
                {
                    return i;
                }
            }
            return len;
        }

        private static int findNextCandidate(Pattern rx, CharSequence input, int from, int len)
        {
            if (rx.literalPrefixLen > 0)
            {
                return indexOfLiteral(input, rx.literalPrefix, rx.firstPrefixChar, from, len);
            }
            if (rx.hasStartCharMask)
            {
                return nextStartMaskIndex(rx, input, from, len);
            }
            return from;
        }

        // =====================================================================
        // SINGLE-PASS DETERMINISTIC CHAIN EXECUTION
        // =====================================================================

        /**
         * Single-pass deterministic chain full-input match for flat linear NFAs with disjoint
         * or terminal quantifiers, avoiding both Lazy DFA table allocation and PikeVM thread/slot copying.
         */
        private boolean matchesDeterministicChain(
                Pattern rx,
                CharSequence input,
                int len,
                ReusableMatchResult res)
        {
            int[] extSlots = (res != null) ? res.ensureSlotCapacity(rx.slotCount) : null;
            if (res == null && rx.slotCount > 8)
            {
                ensureOverflowSlots(rx.slotCount - 8);
            }
            int endOrFail = stepDeterministicChain(rx, input, 0, len, extSlots);
            if (endOrFail >= 0 && rx.trailingDotStarState != NONE)
            {
                endOrFail = scanUntilChar(input, '\n', endOrFail, len);
            }
            if (endOrFail != len)
            {
                return failMatch(res);
            }
            if (res != null)
            {
                extSlots[1] = len;
                res.commitPreparedMatch(input, rx.totalGroups);
            }
            else
            {
                this.s1 = len;
                this.ownMatched = true;
            }
            return true;
        }

        /**
         * Single-pass deterministic chain search for flat linear NFAs with disjoint or terminal
         * quantifiers, avoiding both Lazy DFA table allocation and PikeVM thread/slot copying.
         */
        private boolean findDeterministicChain(
                Pattern rx,
                CharSequence input,
                int fromIndex,
                int len,
                ReusableMatchResult res)
        {
            int[] extSlots = (res != null) ? res.ensureSlotCapacity(rx.slotCount) : null;
            String reqSub = rx.nativeRequiredSubstr;
            int cachedReqPos = -1;
            int candStart = fromIndex;
            int failedSteps = 0;
            int maxFailedSteps = Math.max(64, len);

            while (candStart <= len)
            {
                candStart = findNextCandidate(rx, input, candStart, len);
                if (candStart < 0)
                {
                    return failMatch(res);
                }
                if (reqSub != null)
                {
                    int minReqIdx = candStart + rx.nativeReqMinOffset;
                    if (cachedReqPos < minReqIdx)
                    {
                        cachedReqPos = indexOfLiteral(input, reqSub, rx.nativeReqFirstChar, minReqIdx, len);
                        if (cachedReqPos < 0)
                        {
                            return failMatch(res);
                        }
                    }
                }
                if (res == null && rx.slotCount > 8)
                {
                    ensureOverflowSlots(rx.slotCount - 8);
                }
                int endOrFail = stepDeterministicChain(rx, input, candStart, len, extSlots);
                if (endOrFail >= 0)
                {
                    if (rx.trailingDotStarState != NONE)
                    {
                        endOrFail = scanUntilChar(input, '\n', endOrFail, len);
                    }
                    if (res != null)
                    {
                        extSlots[1] = endOrFail;
                        res.commitPreparedMatch(input, rx.totalGroups);
                    }
                    else
                    {
                        this.s1 = endOrFail;
                        this.ownMatched = true;
                    }
                    return true;
                }
                candStart++;
                failedSteps += ~endOrFail;
                if (failedSteps > maxFailedSteps)
                {
                    return findTwoPhaseDfa(rx, input, candStart, len, res);
                }
            }
            return failMatch(res);
        }

        private void writeChainSlot(int[] extSlots, int idx, int val)
        {
            if (extSlots != null)
            {
                extSlots[idx] = val;
            }
            else
            {
                setOwnSlot(idx, val);
            }
        }

        private int stepDeterministicChain(
                Pattern rx,
                CharSequence input,
                int candStart,
                int len,
                int[] extSlots)
        {
            int[] op = rx.op;
            int[] arg = rx.arg;
            int[] out = rx.out;
            int[] out1 = rx.out1;
            int curr;
            int i;
            if (rx.canSkipPrefixInPikeVm)
            {
                if (extSlots != null)
                {
                    extSlots[0] = candStart;
                }
                else
                {
                    this.s0 = candStart;
                }
                curr = rx.postPrefixState;
                i = candStart + rx.literalPrefixLen;
            }
            else
            {
                curr = rx.startState;
                i = candStart;
            }
            int scanStart = i;

            while (true)
            {
                int opcode = op[curr];
                if (opcode <= OP_CLASS)
                {
                    if (opcode == OP_MATCH)
                    {
                        return i;
                    }
                    if (i >= len || !rx.matchesAtomChar(curr, input.charAt(i)))
                    {
                        return ~((i - scanStart) + 1);
                    }
                    i++;
                    curr = out[curr];
                }
                else if (opcode == OP_SAVE)
                {
                    writeChainSlot(extSlots, arg[curr], i);
                    curr = out[curr];
                }
                else if (opcode == OP_SPLIT)
                {
                    i = stepDeterministicSplit(rx, input, curr, i, len);
                    if (i < 0)
                    {
                        return ~((~i - scanStart) + 1);
                    }
                    curr = (arg[curr] == 1 || arg[curr] == 3) ? out[curr] : out1[curr];
                }
                else
                {
                    if (i != len)
                    {
                        return ~((i - scanStart) + 1);
                    }
                    curr = out[curr];
                }
            }
        }

        private static int stepDeterministicSplit(Pattern rx, CharSequence input, int curr, int i, int len)
        {
            int kind = rx.arg[curr];
            if (kind == 3 || curr == rx.trailingDotStarState)
            {
                return i;
            }
            int body = (kind == 1) ? rx.out1[curr] : rx.out[curr];
            if (kind == 4)
            {
                char termChar = (char) rx.arg[rx.skipSaves(rx.out1[curr])];
                return scanOverlapTerminalLoop(rx, input, body, termChar, i, len);
            }
            if (rx.out[body] == curr)
            {
                return scanGreedyOrDisjointLoop(rx, input, body, i, len);
            }
            if (i < len && rx.matchesAtomChar(body, input.charAt(i)))
            {
                return i + 1;
            }
            return i;
        }

        private static int scanGreedyOrDisjointLoop(Pattern rx, CharSequence input, int body, int i, int len)
        {
            int opBody = rx.op[body];
            int argBody = rx.arg[body];
            if (opBody == OP_CLASS)
            {
                int excl = rx.classExcludedChar[argBody];
                if (excl >= 0)
                {
                    return scanUntilChar(input, (char) excl, i, len);
                }
                return scanClassMaskRun(rx.classBits, rx.classNegated[argBody], argBody << 2, input, i, len);
            }
            if (opBody == OP_ANY)
            {
                return scanUntilChar(input, '\n', i, len);
            }
            char target = (char) argBody;
            while (i < len && input.charAt(i) == target)
            {
                i++;
            }
            return i;
        }

        private static int scanClassMaskRun(
                long[] bits,
                boolean matchesHi,
                int base,
                CharSequence input,
                int i,
                int len)
        {
            long w0 = bits[base];
            long w1 = bits[base + 1];
            long w2 = bits[base + 2];
            long w3 = bits[base + 3];
            while (i < len)
            {
                char c = input.charAt(i);
                boolean ok;
                if (c < 128)
                {
                    ok = (((c < 64) ? w0 : w1) & (1L << c)) != 0L;
                }
                else if (c < 256)
                {
                    ok = (((c < 192) ? w2 : w3) & (1L << c)) != 0L;
                }
                else
                {
                    ok = matchesHi;
                }
                if (!ok)
                {
                    break;
                }
                i++;
            }
            return i;
        }

        private static int scanOverlapTerminalLoop(
                Pattern rx,
                CharSequence input,
                int body,
                char termChar,
                int i,
                int len)
        {
            int endRun = scanGreedyOrDisjointLoop(rx, input, body, i, len);
            if (input instanceof String str)
            {
                int last = str.lastIndexOf(termChar, endRun - 1);
                return (last >= i) ? last : ~endRun;
            }
            for (int p = endRun - 1; p >= i; p--)
            {
                if (input.charAt(p) == termChar)
                {
                    return p;
                }
            }
            return ~endRun;
        }

        // =====================================================================
        // TWO-PHASE LAZY DFA EXECUTION & SUBSET CONSTRUCTION
        // =====================================================================

        private static boolean runBitsetNfaExact64(Pattern rx, CharSequence input, int len)
        {
            long active = rx.startClosure64;
            long[] nextClosure = rx.nextClosure64;
            for (int i = 0; i < len; i++)
            {
                char c = input.charAt(i);
                long word = active;
                long next = 0L;
                while (word != 0L)
                {
                    int s = Long.numberOfTrailingZeros(word);
                    if (rx.matchesAtomChar(s, c))
                    {
                        next |= nextClosure[s];
                    }
                    word &= (word - 1);
                }
                if (next == 0L)
                {
                    return false;
                }
                active = next;
            }
            return (active & rx.matchStateMask64) != 0L;
        }

        private static boolean runBitsetNfaExact128(Pattern rx, CharSequence input, int len)
        {
            long active0 = rx.startClosure64;
            long active1 = rx.startClosure128Hi;
            long[] nextClosure = rx.nextClosure64;
            for (int i = 0; i < len; i++)
            {
                char c = input.charAt(i);
                long w0 = active0;
                long w1 = active1;
                long next0 = 0L;
                long next1 = 0L;
                while (w0 != 0L)
                {
                    int s = Long.numberOfTrailingZeros(w0);
                    if (rx.matchesAtomChar(s, c))
                    {
                        int base = s << 1;
                        next0 |= nextClosure[base];
                        next1 |= nextClosure[base + 1];
                    }
                    w0 &= (w0 - 1);
                }
                while (w1 != 0L)
                {
                    int s = 64 + Long.numberOfTrailingZeros(w1);
                    if (rx.matchesAtomChar(s, c))
                    {
                        int base = s << 1;
                        next0 |= nextClosure[base];
                        next1 |= nextClosure[base + 1];
                    }
                    w1 &= (w1 - 1);
                }
                if ((next0 | next1) == 0L)
                {
                    return false;
                }
                active0 = next0;
                active1 = next1;
            }
            return (active1 & rx.matchStateMask64) != 0L;
        }

        private boolean runLazyDfaExact(Pattern rx, DfaBuffers db, CharSequence input, int len)
        {
            int state = ensureDfaStartState(rx, db);
            int[] trans = db.trans;
            for (int i = 0; i < len; i++)
            {
                char c = input.charAt(i);
                int next = (c < 256) ? trans[(state << 8) | c] : UNKNOWN_DFA;
                if (next == UNKNOWN_DFA)
                {
                    next = stepDfaState(rx, db, state, c);
                    trans = db.trans;
                }
                if (next == DEAD_DFA)
                {
                    return false;
                }
                state = next;
            }
            return (db.flags[state] & 1) != 0;
        }

        /**
         * Two-phase search: uses SIMD prefix / start-bitmask + Lazy DFA to reject non-matching
         * candidates in O(1)/char without slot copying, and invokes narrow-window anchored PikeVM
         * over {@code [candStart, matchEnd]} only when a match is confirmed and subgroup/priority
         * disambiguation is required.
         */
        private boolean findTwoPhaseDfa(
                Pattern rx,
                CharSequence input,
                int fromIndex,
                int len,
                ReusableMatchResult res)
        {
            DfaBuffers db = ensureDfaBuffers();
            int candStart = fromIndex;
            int failedSteps = 0;
            int maxFailedSteps = Math.max(64, len);
            boolean stopAtFirstMatch = rx.hasAmbiguousReluctant;
            int prefLen = rx.literalPrefixLen;

            while (candStart <= len)
            {
                candStart = findNextCandidate(rx, input, candStart, len);
                if (candStart < 0)
                {
                    return failMatch(res);
                }

                int state = (prefLen > 0) ? ensureDfaAfterPrefixState(rx, db) : ensureDfaStartState(rx, db);
                int scanStart = candStart + prefLen;
                int matchEnd = scanDfaWindow(rx, db, input, state, scanStart, len, stopAtFirstMatch);

                if (matchEnd >= 0)
                {
                    if (rx.canDirectDfaFind)
                    {
                        if (rx.trailingDotStarState != NONE)
                        {
                            matchEnd = scanUntilChar(input, '\n', matchEnd, len);
                        }
                        return writeZeroGroupMatch(input, candStart, matchEnd, res);
                    }
                    int pikeEnd = stopAtFirstMatch ? len : matchEnd;
                    PikeBuffers pb = ensurePikeVmBuffers();
                    runPikeVmAnchored(rx, pb, input, candStart, pikeEnd, len, res);
                    if (rx.trailingDotStarState != NONE)
                    {
                        extendMatchEndToLineEnd(input, len, res);
                    }
                    return true;
                }

                candStart++;
                failedSteps += ~matchEnd;
                if (failedSteps > maxFailedSteps)
                {
                    PikeBuffers pb = ensurePikeVmBuffers();
                    boolean hit = runPikeVmFind(rx, pb, input, candStart, len, res);
                    if (hit && rx.trailingDotStarState != NONE)
                    {
                        extendMatchEndToLineEnd(input, len, res);
                    }
                    return hit;
                }
            }
            return failMatch(res);
        }

        private int scanDfaWindow(
                Pattern rx,
                DfaBuffers db,
                CharSequence input,
                int state,
                int scanStart,
                int len,
                boolean stopAtFirstMatch)
        {
            int[] trans = db.trans;
            byte[] flags = db.flags;
            int initFlag = flags[state];
            int matchEnd = ((initFlag & 1) != 0 || (scanStart == len && (initFlag & 2) != 0))
                    ? scanStart
                    : -1;

            if (stopAtFirstMatch && matchEnd >= 0)
            {
                return matchEnd;
            }

            int i = scanStart;
            for (; i < len; i++)
            {
                char c = input.charAt(i);
                int next = (c < 256) ? trans[(state << 8) | c] : UNKNOWN_DFA;
                if (next == UNKNOWN_DFA)
                {
                    next = stepDfaState(rx, db, state, c);
                    trans = db.trans;
                    flags = db.flags;
                }
                if (next == DEAD_DFA)
                {
                    break;
                }
                state = next;
                int fl = flags[state];
                if ((fl & 1) != 0 || (i + 1 == len && (fl & 2) != 0))
                {
                    matchEnd = i + 1;
                    if (stopAtFirstMatch)
                    {
                        break;
                    }
                }
            }
            return (matchEnd >= 0) ? matchEnd : ~((i - scanStart) + 1);
        }

        private int ensureDfaStartState(Pattern rx, DfaBuffers db)
        {
            int state = db.startExact;
            if (state != UNKNOWN_DFA)
            {
                return state;
            }
            state = buildInitialDfaState(rx, db, rx.startState);
            db.startExact = state;
            return state;
        }

        private int ensureDfaAfterPrefixState(Pattern rx, DfaBuffers db)
        {
            int state = db.afterPrefix;
            if (state != UNKNOWN_DFA)
            {
                return state;
            }
            state = buildInitialDfaState(rx, db, rx.postPrefixState);
            db.afterPrefix = state;
            return state;
        }

        private int buildInitialDfaState(Pattern rx, DfaBuffers db, int nfaEntry)
        {
            if (rx.hasClosure64 && nfaEntry == rx.startState)
            {
                db.scratchSet[0] = rx.startClosure64;
            }
            else
            {
                ensureVisited(rx.stateCount);
                Arrays.fill(db.scratchSet, 0L);
                nextStep();
                addDfaEpsilonClosure(rx, db.scratchSet, nfaEntry);
            }
            int state = getOrCreateDfaState(rx, db, db.scratchSet);
            if (state == UNKNOWN_DFA)
            {
                resetDfaCache(db);
                state = getOrCreateDfaState(rx, db, db.scratchSet);
            }
            return state;
        }

        private int stepDfaState(Pattern rx, DfaBuffers db, int fromDfaState, char c)
        {
            if (rx.hasClosure64)
            {
                long word = db.stateSets[fromDfaState];
                db.transFromSet[0] = word;
                long nextSet = 0L;
                long[] nextClosure = rx.nextClosure64;
                while (word != 0L)
                {
                    int nfaState = Long.numberOfTrailingZeros(word);
                    if (rx.matchesAtomChar(nfaState, c))
                    {
                        nextSet |= nextClosure[nfaState];
                    }
                    word &= (word - 1);
                }
                db.scratchSet[0] = nextSet;
            }
            else
            {
                ensureVisited(rx.stateCount);
                int words = rx.wordsPerSet;
                // Copy from-state bitset first, in case cache reset occurs
                System.arraycopy(db.stateSets, fromDfaState * words, db.transFromSet, 0, words);

                Arrays.fill(db.scratchSet, 0L);
                nextStep();

                int[] out = rx.out;
                for (int w = 0; w < words; w++)
                {
                    long word = db.transFromSet[w];
                    while (word != 0L)
                    {
                        int bit = Long.numberOfTrailingZeros(word);
                        int nfaState = (w << 6) + bit;
                        if (rx.matchesAtomChar(nfaState, c))
                        {
                            addDfaEpsilonClosure(rx, db.scratchSet, out[nfaState]);
                        }
                        word &= (word - 1);
                    }
                }
            }

            int nextState = getOrCreateDfaState(rx, db, db.scratchSet);
            if (nextState == UNKNOWN_DFA)
            {
                // Cache full: reset private DFA cache in-place and re-register source and target states
                resetDfaCache(db);
                fromDfaState = getOrCreateDfaState(rx, db, db.transFromSet);
                nextState = getOrCreateDfaState(rx, db, db.scratchSet);
            }
            if (c < 256)
            {
                db.trans[(fromDfaState << 8) | c] = nextState;
            }
            return nextState;
        }

        private void addDfaEpsilonClosure(Pattern rx, long[] bitset, int s)
        {
            if (s == NONE || visited[s] == stepId)
            {
                return;
            }
            visited[s] = stepId;

            switch (rx.op[s])
            {
                case OP_SPLIT ->
                {
                    if (s != rx.trailingDotStarState)
                    {
                        addDfaEpsilonClosure(rx, bitset, rx.out[s]);
                    }
                    addDfaEpsilonClosure(rx, bitset, rx.out1[s]);
                }
                case OP_SAVE -> addDfaEpsilonClosure(rx, bitset, rx.out[s]);
                default -> bitset[s >>> 6] |= (1L << s);
            }
        }

        private int getOrCreateDfaState(Pattern rx, DfaBuffers db, long[] bitset)
        {
            int words = rx.wordsPerSet;
            boolean isEmpty = true;
            long hash = 0x9E3779B97F4A7C15L;
            for (int w = 0; w < words; w++)
            {
                long word = bitset[w];
                if (word != 0L)
                {
                    isEmpty = false;
                }
                hash ^= word + 0x9E3779B97F4A7C15L + (hash << 6) + (hash >>> 2);
            }
            if (isEmpty)
            {
                return DEAD_DFA;
            }

            int[] hashTable = db.hashTable;
            int bucket = ((int) (hash ^ (hash >>> 32))) & HASH_MASK;
            while (true)
            {
                int candidate = hashTable[bucket];
                if (candidate == UNKNOWN_DFA)
                {
                    break;
                }
                if (equalsSet(db.stateSets, candidate * words, bitset, words))
                {
                    return candidate;
                }
                bucket = (bucket + 1) & HASH_MASK;
            }

            if (db.stateCount >= db.capacity)
            {
                if (db.capacity >= MAX_DFA_STATES)
                {
                    return UNKNOWN_DFA;
                }
                int newCap = Math.min(MAX_DFA_STATES, db.capacity << 1);
                db.capacity = newCap;
                db.trans = Arrays.copyOf(db.trans, newCap << 8);
                db.stateSets = Arrays.copyOf(db.stateSets, newCap * words);
                db.flags = Arrays.copyOf(db.flags, newCap);
            }
            int newId = db.stateCount++;
            System.arraycopy(bitset, 0, db.stateSets, newId * words, words);
            db.flags[newId] = computeDfaStateFlags(rx, bitset, words);
            hashTable[bucket] = newId;
            return newId;
        }

        private byte computeDfaStateFlags(Pattern rx, long[] bitset, int words)
        {
            if (rx.hasClosure64)
            {
                return (byte) (((bitset[0] & rx.matchStateMask64) != 0L) ? 1 : 0);
            }
            if (rx.hasClosure128)
            {
                return (byte) (((bitset[1] & rx.matchStateMask64) != 0L) ? 1 : 0);
            }
            int flags = 0;
            int[] op = rx.op;
            int[] out = rx.out;
            for (int w = 0; w < words; w++)
            {
                long word = bitset[w];
                while (word != 0L)
                {
                    int nfaState = (w << 6) + Long.numberOfTrailingZeros(word);
                    int stateOp = op[nfaState];
                    if (stateOp == OP_MATCH)
                    {
                        flags |= 1;
                    }
                    else if (stateOp == OP_EOL && (flags & 2) == 0)
                    {
                        nextStep();
                        if (canReachMatchAtEol(rx, out[nfaState]))
                        {
                            flags |= 2;
                        }
                    }
                    word &= (word - 1);
                }
            }
            return (byte) flags;
        }

        private boolean canReachMatchAtEol(Pattern rx, int s)
        {
            if (s == NONE || visited[s] == stepId)
            {
                return false;
            }
            visited[s] = stepId;
            return switch (rx.op[s])
            {
                case OP_MATCH -> true;
                case OP_SAVE, OP_EOL -> canReachMatchAtEol(rx, rx.out[s]);
                case OP_SPLIT -> (s != rx.trailingDotStarState && canReachMatchAtEol(rx, rx.out[s]))
                        || canReachMatchAtEol(rx, rx.out1[s]);
                default -> false;
            };
        }

        private static boolean equalsSet(long[] stateSets, int base, long[] bitset, int words)
        {
            for (int w = 0; w < words; w++)
            {
                if (stateSets[base + w] != bitset[w])
                {
                    return false;
                }
            }
            return true;
        }

        // =====================================================================
        // PIKE VM EXECUTION
        // =====================================================================

        private boolean runPikeVmMatches(
                Pattern rx,
                PikeBuffers pb,
                CharSequence input,
                int len,
                ReusableMatchResult res)
        {
            nextStep();
            int csize = addThread(rx, pb, pb.clist, pb.cslots, 0, rx.startState, pb.emptySlots, 0, 0, len, true);

            for (int i = 0; i < len; i++)
            {
                if (csize == 0)
                {
                    return failMatch(res);
                }
                csize = advancePikeVm(rx, pb, input.charAt(i), csize, i + 1, len, true);
            }

            int[] op = rx.op;
            for (int j = 0; j < csize; j++)
            {
                if (op[pb.clist[j]] == OP_MATCH)
                {
                    writeMatchedSlots(input, rx.totalGroups, pb.cslots, j * rx.slotCount, res);
                    return true;
                }
            }
            return failMatch(res);
        }

        /**
         * Executes priority-ordered PikeVM anchored at a confirmed match start index {@code candStart}
         * over the bounded slice {@code [candStart, windowEnd]}, avoiding unanchored thread spawns
         * and scanning only until subgroup boundaries are resolved.
         */
        private boolean runPikeVmAnchored(
                Pattern rx,
                PikeBuffers pb,
                CharSequence input,
                int candStart,
                int windowEnd,
                int fullLen,
                ReusableMatchResult res)
        {
            int csize;
            int i;
            nextStep();
            if (rx.canSkipPrefixInPikeVm)
            {
                i = candStart + rx.literalPrefixLen;
                pb.emptySlots[0] = candStart;
                csize = addThread(rx, pb, pb.clist, pb.cslots, 0, rx.postPrefixState, pb.emptySlots, 0, i, fullLen, false);
                pb.emptySlots[0] = -1;
            }
            else
            {
                i = candStart;
                csize = addThread(rx, pb, pb.clist, pb.cslots, 0, rx.startState, pb.emptySlots, 0, i, fullLen, false);
            }

            int[] op = rx.op;
            int slotCount = rx.slotCount;
            int groups = rx.totalGroups;
            boolean found = false;
            while (true)
            {
                for (int j = 0; j < csize; j++)
                {
                    if (op[pb.clist[j]] == OP_MATCH)
                    {
                        writeMatchedSlots(input, groups, pb.cslots, j * slotCount, res);
                        found = true;
                        csize = j;
                        break;
                    }
                }

                if (csize == 0 || i == windowEnd)
                {
                    break;
                }

                csize = advancePikeVm(rx, pb, input.charAt(i), csize, i + 1, fullLen, false);
                i++;
            }

            return found ? true : failMatch(res);
        }

        private boolean runPikeVmFind(
                Pattern rx,
                PikeBuffers pb,
                CharSequence input,
                int startIdx,
                int len,
                ReusableMatchResult res)
        {
            int csize = 0;
            int i = startIdx;
            boolean found = false;
            int prefLen = rx.literalPrefixLen;
            char firstChar = rx.firstPrefixChar;
            String pref = rx.literalPrefix;
            int[] op = rx.op;
            int slotCount = rx.slotCount;
            int groups = rx.totalGroups;

            while (true)
            {
                if (csize == 0 && !found && prefLen > 0)
                {
                    i = indexOfLiteral(input, pref, firstChar, i, len);
                    if (i < 0)
                    {
                        break;
                    }
                }

                nextStep();
                if (!found)
                {
                    if (prefLen == 0
                            || (i + prefLen <= len
                                && input.charAt(i) == firstChar
                                && startsWithLiteral(input, i, pref)))
                    {
                        csize = addThread(rx, pb, pb.clist, pb.cslots, csize, rx.startState, pb.emptySlots, 0, i, len, false);
                    }
                }

                for (int j = 0; j < csize; j++)
                {
                    if (op[pb.clist[j]] == OP_MATCH)
                    {
                        writeMatchedSlots(input, groups, pb.cslots, j * slotCount, res);
                        found = true;
                        csize = j;
                        break;
                    }
                }

                if (csize == 0)
                {
                    if (found || i == len)
                    {
                        break;
                    }
                    i++;
                    continue;
                }
                if (i == len)
                {
                    break;
                }

                csize = advancePikeVm(rx, pb, input.charAt(i), csize, i + 1, len, false);
                i++;
            }

            return found ? true : failMatch(res);
        }

        private int advancePikeVm(
                Pattern rx,
                PikeBuffers pb,
                char c,
                int csize,
                int nextIdx,
                int fullLen,
                boolean exact)
        {
            int nsize = 0;
            nextStep();
            int[] clist = pb.clist;
            int[] nlist = pb.nlist;
            int[] cslots = pb.cslots;
            int[] nslots = pb.nslots;
            int[] op = rx.op;
            int[] out = rx.out;
            int slotCount = rx.slotCount;

            for (int j = 0; j < csize; j++)
            {
                int s = clist[j];
                if (rx.matchesAtomChar(s, c))
                {
                    nsize = addThread(rx, pb, nlist, nslots, nsize, out[s], cslots, j * slotCount, nextIdx, fullLen, exact);
                    if (nsize > 0 && op[nlist[nsize - 1]] == OP_MATCH)
                    {
                        break;
                    }
                }
            }

            pb.clist = nlist;
            pb.nlist = clist;
            pb.cslots = nslots;
            pb.nslots = cslots;
            return nsize;
        }

        /**
         * Returns an iterable cursor yielding matches across the entire input sequence.
         *
         * @param input the character sequence to search
         * @param result the single reusable result container reused on each iteration step
         * @return zero-allocation Iterable yielding the match result
         */
        public Iterable<ReusableMatchResult> findAll(CharSequence input, ReusableMatchResult result)
        {
            AuxState a = ensureAux();
            MatchCursor cursor = a.reusableCursor;
            if (cursor == null)
            {
                cursor = new MatchCursor();
                a.reusableCursor = cursor;
            }
            return cursor.reset(input, result);
        }

        private final class MatchCursor implements Iterable<ReusableMatchResult>, Iterator<ReusableMatchResult>
        {
            private CharSequence input;
            private ReusableMatchResult result;
            private int nextCursor;
            private boolean hasNextComputed;
            private boolean hasNextVal;

            MatchCursor reset(CharSequence input, ReusableMatchResult result)
            {
                this.input = input;
                this.result = result;
                this.nextCursor = 0;
                this.hasNextComputed = false;
                this.hasNextVal = false;
                return this;
            }

            @Override
            public Iterator<ReusableMatchResult> iterator()
            {
                return this;
            }

            @Override
            public boolean hasNext()
            {
                if (!hasNextComputed)
                {
                    hasNextVal = (nextCursor <= input.length()) && find(input, nextCursor, result);
                    if (hasNextVal)
                    {
                        // Advance past zero-width matches (where start == end, even if found beyond nextCursor)
                        // identically to java.util.regex.Matcher.find().
                        nextCursor = (result.start() == result.end()) ? result.end() + 1 : result.end();
                    }
                    hasNextComputed = true;
                }
                return hasNextVal;
            }

            @Override
            public ReusableMatchResult next()
            {
                if (!hasNext())
                {
                    throw new NoSuchElementException();
                }
                hasNextComputed = false;
                return result;
            }
        }

        /**
         * Iteratively computes the priority-ordered epsilon closure from {@code startS}
         * using a flat primitive workstack ({@link PikeBuffers#epsStack}) without method recursion,
         * enabling full HotSpot C2 inlining on hot PikeVM paths.
         *
         * <p>Stack encoding: non-negative values are NFA state IDs to visit in LIFO order;
         * negative values {@code ~slotIdx} are undo markers restoring {@code currentSlots[slotIdx]}
         * from the preceding stack entry once an {@code OP_SAVE} subtree completes.</p>
         */
        private int addThread(
                Pattern rx,
                PikeBuffers pb,
                int[] list,
                int[] slotsBuf,
                int size,
                int startS,
                int[] currentSlots,
                int slotsOffset,
                int idx,
                int len,
                boolean exact)
        {
            int[] vis = this.visited;
            int step = this.stepId;
            if (vis[startS] == step)
            {
                return size;
            }
            int[] stack = pb.epsStack;
            int top = 0;
            stack[top++] = startS;

            while (top > 0)
            {
                int s = stack[--top];
                if (s < 0)
                {
                    currentSlots[~s] = stack[--top];
                    continue;
                }
                if (vis[s] == step)
                {
                    continue;
                }
                vis[s] = step;

                int opcode = rx.op[s];
                if (opcode == OP_SPLIT)
                {
                    int o1 = rx.out1[s];
                    if (o1 != NONE)
                    {
                        stack[top++] = o1;
                    }
                    if (exact || s != rx.trailingDotStarState)
                    {
                        stack[top++] = rx.out[s];
                    }
                }
                else if (opcode == OP_SAVE)
                {
                    int slotIdx = slotsOffset + rx.arg[s];
                    stack[top++] = currentSlots[slotIdx];
                    stack[top++] = ~slotIdx;
                    currentSlots[slotIdx] = idx;
                    stack[top++] = rx.out[s];
                }
                else if (opcode == OP_BOL || opcode == OP_EOL)
                {
                    if (idx == (opcode == OP_BOL ? 0 : len))
                    {
                        stack[top++] = rx.out[s];
                    }
                }
                else
                {
                    if (opcode == OP_MATCH)
                    {
                        if (exact && idx != len)
                        {
                            continue;
                        }
                        emitThread(list, slotsBuf, size, s, currentSlots, slotsOffset, rx.slotCount);
                        unwindSaveStack(stack, top, currentSlots);
                        return size + 1;
                    }
                    emitThread(list, slotsBuf, size++, s, currentSlots, slotsOffset, rx.slotCount);
                }
            }
            return size;
        }

        private static void emitThread(
                int[] list, int[] slotsBuf, int idx, int state, int[] currentSlots, int slotsOffset, int slotCount)
        {
            list[idx] = state;
            copyIntSlots(currentSlots, slotsOffset, slotsBuf, idx * slotCount, slotCount);
        }

        private static void unwindSaveStack(int[] stack, int top, int[] currentSlots)
        {
            while (top > 0)
            {
                int rem = stack[--top];
                if (rem < 0)
                {
                    currentSlots[~rem] = stack[--top];
                }
            }
        }

        private static void copyIntSlots(int[] src, int srcPos, int[] dst, int dstPos, int count)
        {
            if (count == 4)
            {
                dst[dstPos]     = src[srcPos];
                dst[dstPos + 1] = src[srcPos + 1];
                dst[dstPos + 2] = src[srcPos + 2];
                dst[dstPos + 3] = src[srcPos + 3];
            }
            else
            {
                System.arraycopy(src, srcPos, dst, dstPos, count);
            }
        }
    }

}
