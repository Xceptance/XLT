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
package com.xceptance.xlt.report.storage.dictionary;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.roaringbitmap.RoaringBitmap;

import com.xceptance.xlt.report.storage.StorageIoUtils;

/**
 * Manages low-overhead, thread-safe dictionary interning and ID mappings for high-frequency
 * string metadata across all columnar chunks in ChunkDB.
 * <p>
 * <b>Architectural Rationale &amp; Data Model:</b>
 * <ul>
 *   <li><b>Timer Names:</b> Maps timer name strings (e.g. "Homepage", "AddToCart", "Checkout", URLs) to
 *       compact 32-bit integer IDs. Unlike 16-bit dictionaries, this removes the arbitrary 65,535 cap,
 *       gracefully supporting large-scale enterprise load tests with hundreds of thousands of dynamic URLs.</li>
 *   <li><b>Unified Agent &amp; TestCase Pairs:</b> Instead of storing separate agent and test case IDs,
 *       agents and test cases are interned together as an atomic {@link AgentTestCase} pair. This
 *       halves the storage required for these dimensions down to a single column per row,
 *       while simultaneously speeding up agent/test case filtering during report generation.</li>
 *   <li><b>General Strings:</b> Interns secondary string attributes such as HTTP methods ("GET", "POST"),
 *       MIME content types ("text/html", "application/json"), IP addresses, and error messages.</li>
 * </ul>
 * <p>
 * <b>Concurrency &amp; High-Throughput Design:</b>
 * <ul>
 *   <li><b>Lock-Free Concurrent Reads:</b> Read lookups by ID (e.g., during SIMD query scanning) read
 *       directly from append-only {@code volatile} array references ({@code timerNames}, {@code agentTestCases},
 *       {@code strings}). There are zero lock acquisitions, zero thread synchronization barriers, and zero
 *       object allocations on read lookups.</li>
 *   <li><b>Optimistic Ingestion:</b> During multi-threaded parsing, lookups check {@link ConcurrentHashMap}
 *       first. For 99.999% of records in a steady-state load test, the string is already interned and resolved
 *       without locking.</li>
 *   <li><b>Zero-Copy Array Expansion:</b> Rather than using {@code CopyOnWriteArrayList} which triggers an
 *       \(O(N)\) full array duplication on every newly discovered string (creating massive GC churn on large runs),
 *       internal storage uses dynamically resizing geometric array doubling under a narrow lock on new insertion.</li>
 * </ul>
 */
public class GlobalDictionaries
{
    /**
     * Unified pair representing an Agent name and a TestCase scenario name.
     */
    public record AgentTestCase(String agentName, String testCaseName)
    {
        /**
         * Validates non-null invariants for the agent and test case pair.
         *
         * @param agentName
         *            the non-null name of the load testing agent
         * @param testCaseName
         *            the non-null name of the executed test scenario
         */
        public AgentTestCase
        {
            Objects.requireNonNull(agentName, "agentName cannot be null");
            Objects.requireNonNull(testCaseName, "testCaseName cannot be null");
        }
    }

    /**
     * Precomputed, immutable representation of a sampled URL and its parsed host and hash code.
     * Caching these per timer eliminates multi-million URL parsing, string hashing, and
     * object allocations in the inner query scanning loop.
     */
    public record CachedSampleUrl(String url, String host, int hashCodeOfUrlWithoutFragment)
    {
        public CachedSampleUrl(final String urlString)
        {
            this(urlString,
                 retrieveHost(urlString),
                 com.xceptance.common.lang.StringHasher.hashCodeWithLimit(urlString, '#'));
        }

        private static String retrieveHost(final String url)
        {
            final String hostName = com.xceptance.xlt.report.util.UrlHostParser.retrieveHostFromUrl(url);
            return hostName.isEmpty() ? com.xceptance.xlt.api.engine.RequestData.UNKNOWN_HOST : hostName;
        }
    }

    /** Magic header identifier (0x584C5444 = 'XLTD' in ASCII hex). */
    public static final int MAGIC = 0x584C5444;

    /** Binary dictionaries format version. */
    public static final int VERSION = 1;

    /** Initial capacity for interned dictionary arrays. */
    private static final int INITIAL_CAPACITY = 256;

    // -------------------------------------------------------------------------
    // Timer Names Storage
    // -------------------------------------------------------------------------

    /** Lock guarding timer name ID allocation and reverse map reconstruction. */
    private final Object timerLock = new Object();

    /**
     * Concurrent lookup map from timer name string to interned integer ID.
     * Released ({@code null}) upon {@link #compactForReadOnly()} and lazily reconstructed on demand if
     * {@link #getOrCreateTimerNameId(CharSequence)} is called after compaction.
     */
    private volatile ConcurrentHashMap<String, Integer> timerNameToId;

    /** Volatile append-only array indexed by interned ID for lock-free \(O(1)\) reads. */
    private volatile String[] timerNames = new String[INITIAL_CAPACITY];

    /** Current number of distinct timer names interned. */
    private int timerNameCount = 0;

    // -------------------------------------------------------------------------
    // Agent + TestCase Unified Storage
    // -------------------------------------------------------------------------

    /** Lock guarding (agent, testCase) pair ID allocation and reverse map reconstruction. */
    private final Object pairLock = new Object();

    /**
     * Concurrent lookup map from (agent, testCase) pair to interned integer ID.
     * Released ({@code null}) upon {@link #compactForReadOnly()} and lazily reconstructed on demand if
     * {@link #getOrCreateAgentTestCaseId(CharSequence, CharSequence)} is called after compaction.
     */
    private volatile ConcurrentHashMap<AgentTestCase, Integer> pairToId;

    /** Volatile append-only array indexed by interned ID for lock-free \(O(1)\) reads. */
    private volatile AgentTestCase[] agentTestCases = new AgentTestCase[INITIAL_CAPACITY];

    /** Current number of distinct agent-testCase pairs interned. */
    private int agentTestCaseCount = 0;

    // -------------------------------------------------------------------------
    // General Strings Storage
    // -------------------------------------------------------------------------

    /** Lock guarding general string ID allocation and reverse map reconstruction. */
    private final Object stringLock = new Object();

    /**
     * Concurrent lookup map from general string to interned integer ID.
     * Released ({@code null}) upon {@link #compactForReadOnly()} and lazily reconstructed on demand if
     * {@link #getOrCreateStringId(CharSequence)} is called after compaction.
     */
    private volatile ConcurrentHashMap<String, Integer> stringToId;

    /** Volatile append-only array indexed by interned ID for lock-free \(O(1)\) reads. */
    private volatile String[] strings = new String[INITIAL_CAPACITY];

    /** Current number of distinct general strings interned. */
    private int stringCount = 0;

    /** Volatile cached array of pre-split IP address strings indexed by string ID. */
    private volatile String[][] splitIpAddresses = new String[INITIAL_CAPACITY][];

    // -------------------------------------------------------------------------
    // Representative Sample URLs Storage
    // -------------------------------------------------------------------------

    /** Maximum number of sample URLs stored per timer name across the entire dataset. */
    private static final int MAX_SAMPLE_URLS_PER_TIMER = 10;

    /** Lock guarding sample URL registration and precomputation. */
    private final Object sampleUrlLock = new Object();

    /**
     * Number of sample URLs collected per timer ID (`0 .. MAX_SAMPLE_URLS_PER_TIMER`).
     * Stored in a flat primitive `byte[]` indexed by `timerId` so {@link #hasSufficientSamples(int)}
     * executes as a single L1-cache array load without `Integer` autoboxing or map lookups.
     */
    private volatile byte[] sampleUrlCounts = new byte[INITIAL_CAPACITY];

    /**
     * Ingestion-time staging buckets indexed by `timerId`. Each non-null bucket has fixed capacity
     * {@link #MAX_SAMPLE_URLS_PER_TIMER}, avoiding `CopyOnWriteArrayList`/`CopyOnWriteArraySet` array
     * cloning on every newly discovered sample URL. Released (`null`) upon {@link #compactForReadOnly()}.
     */
    private volatile String[][] rawSampleUrls;

    /** Volatile array of precomputed sample URLs indexed by timer ID for lock-free reads. */
    private volatile CachedSampleUrl[][] cachedSampleUrls = new CachedSampleUrl[INITIAL_CAPACITY][];

    /** Tracked distinct string IDs for HTTP methods, content types, and used IPs for instant metadata facet discovery. */
    private final RoaringBitmap httpMethodIds = new RoaringBitmap();
    private final RoaringBitmap contentTypeIds = new RoaringBitmap();
    private final RoaringBitmap usedIpIds = new RoaringBitmap();

    public void recordHttpMethodId(final int id)
    {
        if (id >= 0)
        {
            synchronized (httpMethodIds)
            {
                httpMethodIds.add(id);
            }
        }
    }

    public void recordContentTypeId(final int id)
    {
        if (id >= 0)
        {
            synchronized (contentTypeIds)
            {
                contentTypeIds.add(id);
            }
        }
    }

    public void recordUsedIpId(final int id)
    {
        if (id >= 0)
        {
            synchronized (usedIpIds)
            {
                usedIpIds.add(id);
            }
        }
    }

    public RoaringBitmap getHttpMethodIds()
    {
        return httpMethodIds;
    }

    public RoaringBitmap getContentTypeIds()
    {
        return contentTypeIds;
    }

    public RoaringBitmap getUsedIpIds()
    {
        return usedIpIds;
    }

    // -------------------------------------------------------------------------
    // High-Throughput Thread-Local Ingestion Caches
    // -------------------------------------------------------------------------

    /**
     * Thread-local single-entry memoization cache for timer names.
     * In load tests, consecutive requests often share identical timer names. Caching the last
     * resolved timer name reference avoids converting CharSequence to String and querying ConcurrentHashMap.
     */
    private static final class ThreadLocalTimerCache
    {
        CharSequence lastRef;
        String lastStr;
        int lastId = -1;
    }

    private volatile ThreadLocal<ThreadLocalTimerCache> localTimerCache =
        ThreadLocal.withInitial(ThreadLocalTimerCache::new);

    /**
     * Thread-local single-entry memoization cache for (agent, testCase) pairs.
     * In multi-threaded log parsing, parser threads process log files partitioned by agent and test case.
     * Within each file, every single record has the exact same agent name and transaction name.
     * Caching the last resolved pair in thread-local storage avoids allocating millions of new
     * {@link AgentTestCase} heap objects and intermediate String instances on 99.99%+ of lookups.
     */
    private static final class ThreadLocalAgentTestCaseCache
    {
        CharSequence lastAgentRef;
        CharSequence lastTestCaseRef;
        String lastAgentStr;
        String lastTestCaseStr;
        int lastId = -1;
    }

    private volatile ThreadLocal<ThreadLocalAgentTestCaseCache> localAgentTestCaseCache =
        ThreadLocal.withInitial(ThreadLocalAgentTestCaseCache::new);

    /**
     * Thread-local single-entry memoization cache for general strings (e.g. HTTP method, content type).
     */
    private static final class ThreadLocalStringCache
    {
        CharSequence lastRef;
        String lastStr;
        int lastId = -1;
    }

    private volatile ThreadLocal<ThreadLocalStringCache> localStringCache =
        ThreadLocal.withInitial(ThreadLocalStringCache::new);

    /**
     * Compares a {@link CharSequence} with a {@link String} character-by-character without
     * allocating a new {@link String} on the heap.
     *
     * @param cs
     *            the candidate char sequence (may be null)
     * @param s
     *            the known string (may be null)
     * @return {@code true} if both sequences are non-null and contain identical characters
     */
    public static boolean charSequenceEquals(final CharSequence cs, final String s)
    {
        if (cs == null || s == null)
        {
            return false;
        }
        if (cs instanceof String)
        {
            return ((String) cs).equals(s);
        }
        final int len = cs.length();
        if (len != s.length())
        {
            return false;
        }
        for (int i = 0; i < len; i++)
        {
            if (cs.charAt(i) != s.charAt(i))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Constructs a new instance of {@link GlobalDictionaries} and pre-interns common HTTP constants.
     */
    public GlobalDictionaries()
    {
        this(true);
    }

    /**
     * Internal constructor with option to suppress pre-interning constants, used during deserialization
     * to prevent constructor state pollution and avoid allocating reverse lookup maps on warm runs.
     */
    private GlobalDictionaries(final boolean preIntern)
    {
        if (preIntern)
        {
            this.timerNameToId = new ConcurrentHashMap<>();
            this.pairToId = new ConcurrentHashMap<>();
            this.stringToId = new ConcurrentHashMap<>();
            this.rawSampleUrls = new String[INITIAL_CAPACITY][];

            // Pre-intern standard HTTP methods and content types to guarantee immediate ID assignment
            getOrCreateStringId("GET");
            getOrCreateStringId("POST");
            getOrCreateStringId("PUT");
            getOrCreateStringId("DELETE");
            getOrCreateStringId("HEAD");
            getOrCreateStringId("OPTIONS");
            getOrCreateStringId("PATCH");
            getOrCreateStringId("text/html");
            getOrCreateStringId("application/json");
            getOrCreateStringId("application/javascript");
            getOrCreateStringId("text/css");
            getOrCreateStringId("image/png");
            getOrCreateStringId("image/jpeg");
            getOrCreateStringId("image/gif");
            getOrCreateStringId("image/svg+xml");
        }
    }

    // -------------------------------------------------------------------------
    // Timer Names Operations
    // -------------------------------------------------------------------------

    /**
     * Returns the reverse lookup map for timer names, lazily rebuilding it from {@link #timerNames}
     * if this dictionary was previously compacted or deserialized from disk.
     */
    private ConcurrentHashMap<String, Integer> getOrInitTimerNameMap()
    {
        ConcurrentHashMap<String, Integer> map = this.timerNameToId;
        if (map != null)
        {
            return map;
        }
        synchronized (timerLock)
        {
            map = new ConcurrentHashMap<>();
            final String[] names = this.timerNames;
            final int count = this.timerNameCount;
            for (int i = 0; i < count; i++)
            {
                map.put(names[i], i);
            }
            this.timerNameToId = map;
            return map;
        }
    }

    /**
     * Interns the specified timer name, returning its existing or newly assigned integer identifier.
     * <p>
     * Thread-safe: employs thread-local single-entry memoization first, falling back to optimistic
     * lock-free lookup via {@link ConcurrentHashMap}, and finally synchronized insertion on new discovery.
     *
     * @param name
     *            the timer name char sequence (may be {@code null})
     * @return non-negative integer ID, or {@code -1} if {@code name} is null
     */
    public int getOrCreateTimerNameId(final CharSequence name)
    {
        if (name == null)
        {
            return -1;
        }

        // 1. Thread-local fast-path: check if matches last seen timer name on this thread
        final ThreadLocalTimerCache cache = localTimerCache.get();
        if (name == cache.lastRef)
        {
            return cache.lastId;
        }
        if (cache.lastStr != null && charSequenceEquals(name, cache.lastStr))
        {
            cache.lastRef = name;
            return cache.lastId;
        }

        final String s = name.toString();
        final ConcurrentHashMap<String, Integer> map = getOrInitTimerNameMap();

        // 2. Optimistic lock-free read for steady-state hits
        final Integer id = map.get(s);
        if (id != null)
        {
            cache.lastRef = name;
            cache.lastStr = s;
            cache.lastId = id;
            return id;
        }

        // 3. Synchronize on timerLock only when allocating a newly discovered string
        synchronized (timerLock)
        {
            final int assignedId = map.computeIfAbsent(s, key -> {
                final int nextId = timerNameCount++;
                if (nextId >= timerNames.length)
                {
                    // Geometric array doubling: eliminates O(N) array clone churn
                    timerNames = Arrays.copyOf(timerNames, Math.max(INITIAL_CAPACITY, timerNames.length * 2));
                }
                timerNames[nextId] = key;
                return nextId;
            });
            cache.lastRef = name;
            cache.lastStr = s;
            cache.lastId = assignedId;
            return assignedId;
        }
    }

    /**
     * Resolves an interned ID back to its original timer name.
     * <p>
     * Lock-free \(O(1)\) array lookup.
     *
     * @param id
     *            the interned integer ID
     * @return the timer name, or {@code null} if ID is negative or out of bounds
     */
    public String getTimerName(final int id)
    {
        final String[] array = timerNames;
        return (id >= 0 && id < timerNameCount) ? array[id] : null;
    }

    /**
     * Returns the total number of distinct timer names interned in this dictionary.
     *
     * @return number of distinct timer names
     */
    public int getTimerNameCount()
    {
        return timerNameCount;
    }

    /**
     * Returns the raw backing array of interned timer names for high-throughput batch scanning.
     * <p>
     * Callers must treat the returned array as read-only. Accessing elements by direct array index
     * avoids repeated volatile field dereferencing and bounds check method call overhead in hot loops.
     *
     * @return the raw array of timer names
     */
    public String[] getTimerNamesArray()
    {
        return timerNames;
    }


    // -------------------------------------------------------------------------
    // Agent + TestCase Unified Pair Operations
    // -------------------------------------------------------------------------

    /**
     * Returns the reverse lookup map for (agent, testCase) pairs, lazily rebuilding it from {@link #agentTestCases}
     * if this dictionary was previously compacted or deserialized from disk.
     */
    private ConcurrentHashMap<AgentTestCase, Integer> getOrInitPairMap()
    {
        ConcurrentHashMap<AgentTestCase, Integer> map = this.pairToId;
        if (map != null)
        {
            return map;
        }
        synchronized (pairLock)
        {
            map = new ConcurrentHashMap<>();
            final AgentTestCase[] pairs = this.agentTestCases;
            final int count = this.agentTestCaseCount;
            for (int i = 0; i < count; i++)
            {
                map.put(pairs[i], i);
            }
            this.pairToId = map;
            return map;
        }
    }

    /**
     * Interns the specified agent and test case combination, returning a unified integer identifier.
     * <p>
     * Thread-safe: checks thread-local memoization cache first to eliminate object allocations,
     * falling back to optimistic lock-free lookup via {@link ConcurrentHashMap}, and synchronized
     * insertion when encountering a new (agent, testCase) combination.
     *
     * @param agent
     *            the agent name char sequence (e.g. "Agent-01")
     * @param testCase
     *            the test case scenario name (e.g. "TBrowse")
     * @return non-negative integer unified pair ID
     */
    public int getOrCreateAgentTestCaseId(final CharSequence agent, final CharSequence testCase)
    {
        // 1. Thread-local cache fast-path: eliminates 99.99%+ of allocations
        final ThreadLocalAgentTestCaseCache cache = localAgentTestCaseCache.get();
        if (cache.lastId != -1 && agent == cache.lastAgentRef && testCase == cache.lastTestCaseRef)
        {
            return cache.lastId;
        }

        // Fast string/char-sequence match without allocating new Strings or AgentTestCase objects
        if (cache.lastAgentStr != null &&
            charSequenceEquals(agent, cache.lastAgentStr) &&
            charSequenceEquals(testCase, cache.lastTestCaseStr))
        {
            cache.lastAgentRef = agent;
            cache.lastTestCaseRef = testCase;
            return cache.lastId;
        }

        final String a = agent != null ? agent.toString() : "";
        final String tc = testCase != null ? testCase.toString() : "";
        final AgentTestCase pair = new AgentTestCase(a, tc);
        final ConcurrentHashMap<AgentTestCase, Integer> map = getOrInitPairMap();

        // 2. Optimistic lock-free read
        final Integer id = map.get(pair);
        if (id != null)
        {
            cache.lastAgentRef = agent;
            cache.lastTestCaseRef = testCase;
            cache.lastAgentStr = a;
            cache.lastTestCaseStr = tc;
            cache.lastId = id;
            return id;
        }

        // 3. Synchronize only for allocating the next sequential ID
        synchronized (pairLock)
        {
            final int assignedId = map.computeIfAbsent(pair, key -> {
                final int nextId = agentTestCaseCount++;
                if (nextId >= agentTestCases.length)
                {
                    agentTestCases = Arrays.copyOf(agentTestCases, Math.max(INITIAL_CAPACITY, agentTestCases.length * 2));
                }
                agentTestCases[nextId] = key;
                return nextId;
            });
            cache.lastAgentRef = agent;
            cache.lastTestCaseRef = testCase;
            cache.lastAgentStr = a;
            cache.lastTestCaseStr = tc;
            cache.lastId = assignedId;
            return assignedId;
        }
    }

    /**
     * Resolves a unified integer ID back to its original {@link AgentTestCase} pair.
     * <p>
     * Lock-free \(O(1)\) array lookup.
     *
     * @param id
     *            the interned unified ID
     * @return the {@link AgentTestCase} pair, or {@code null} if ID is out of bounds
     */
    public AgentTestCase getAgentTestCase(final int id)
    {
        final AgentTestCase[] array = agentTestCases;
        return (id >= 0 && id < agentTestCaseCount) ? array[id] : null;
    }

    /**
     * Convenience method to resolve only the agent name for a unified pair ID.
     *
     * @param id
     *            the interned unified ID
     * @return the agent name, or {@code null} if out of bounds
     */
    public String getAgentName(final int id)
    {
        final AgentTestCase pair = getAgentTestCase(id);
        return pair != null ? pair.agentName() : null;
    }

    /**
     * Convenience method to resolve only the test case scenario name for a unified pair ID.
     *
     * @param id
     *            the interned unified ID
     * @return the test case name, or {@code null} if out of bounds
     */
    public String getTestCaseName(final int id)
    {
        final AgentTestCase pair = getAgentTestCase(id);
        return pair != null ? pair.testCaseName() : null;
    }

    /**
     * Returns the total count of distinct (agent, testCase) pairs interned in this dictionary.
     *
     * @return total pair count
     */
    public int getAgentTestCaseCount()
    {
        return agentTestCaseCount;
    }

    /**
     * Returns the raw backing array of interned agent and test case pairs for high-throughput batch scanning.
     * <p>
     * Callers must treat the returned array as read-only. Accessing elements by direct array index
     * avoids repeated volatile field dereferencing and bounds check method call overhead in hot loops.
     *
     * @return the raw array of {@link AgentTestCase} instances
     */
    public AgentTestCase[] getAgentTestCasesArray()
    {
        return agentTestCases;
    }


    /**
     * Evaluates agent and test case filters across all interned pairs, compiling a {@link RoaringBitmap}
     * of all matching integer IDs.
     * <p>
     * This precomputed bitmap enables \(O(1)\) bitwise chunk pruning and row-level filtering during report
     * generation queries without evaluating regular expressions against raw strings repeatedly.
     *
     * @param agentFilter
     *            pattern matcher for agent names (may be {@code null} to accept all)
     * @param testCaseFilter
     *            pattern matcher for test case names (may be {@code null} to accept all)
     * @return bitmap of matching pair IDs, or {@code null} if no filters are active or all pairs matched
     */
    public RoaringBitmap filterAgentTestCaseIds(final com.xceptance.common.util.StringMatcher agentFilter,
                                                final com.xceptance.common.util.StringMatcher testCaseFilter)
    {
        // If neither filter is defined, all pairs match unconditionally
        if (agentFilter == null && testCaseFilter == null)
        {
            return null;
        }

        final RoaringBitmap bitmap = new RoaringBitmap();
        boolean anyExcluded = false;

        final int count = agentTestCaseCount;
        final AgentTestCase[] array = agentTestCases;

        // Iterate through all interned pairs and evaluate regex matchers
        for (int i = 0; i < count; i++)
        {
            final AgentTestCase pair = array[i];
            final boolean agentOk = agentFilter == null || agentFilter.isAccepted(pair.agentName());
            final boolean testCaseOk = testCaseFilter == null || testCaseFilter.isAccepted(pair.testCaseName());
            if (agentOk && testCaseOk)
            {
                bitmap.add(i);
            }
            else
            {
                anyExcluded = true;
            }
        }

        // If all pairs passed, return null so queries skip bitmap testing entirely
        return anyExcluded ? bitmap : null;
    }

    // -------------------------------------------------------------------------
    // General Strings Operations
    // -------------------------------------------------------------------------

    /**
     * Returns the reverse lookup map for general strings, lazily rebuilding it from {@link #strings}
     * if this dictionary was previously compacted or deserialized from disk.
     */
    private ConcurrentHashMap<String, Integer> getOrInitStringMap()
    {
        ConcurrentHashMap<String, Integer> map = this.stringToId;
        if (map != null)
        {
            return map;
        }
        synchronized (stringLock)
        {
            map = new ConcurrentHashMap<>();
            final String[] arr = this.strings;
            final int count = this.stringCount;
            for (int i = 0; i < count; i++)
            {
                map.put(arr[i], i);
            }
            this.stringToId = map;
            return map;
        }
    }

    /**
     * Interns an arbitrary string (e.g. HTTP method, content type, IP address, error message),
     * returning its integer interned ID.
     * <p>
     * Thread-safe: checks thread-local memoization cache first to eliminate String conversions and map lookups,
     * falling back to optimistic lock-free read via {@link ConcurrentHashMap}, and synchronized allocation
     * when encountering a new string.
     *
     * @param text
     *            the text char sequence (may be {@code null})
     * @return non-negative integer ID, or {@code -1} if {@code text} is null
     */
    public int getOrCreateStringId(final CharSequence text)
    {
        if (text == null)
        {
            return -1;
        }

        // 1. Thread-local cache fast-path: avoids String conversions and map lookups for repeated strings
        final ThreadLocalStringCache cache = localStringCache.get();
        if (text == cache.lastRef)
        {
            return cache.lastId;
        }
        if (cache.lastStr != null && charSequenceEquals(text, cache.lastStr))
        {
            cache.lastRef = text;
            return cache.lastId;
        }

        final String s = text.toString();
        final ConcurrentHashMap<String, Integer> map = getOrInitStringMap();

        // 2. Optimistic lock-free read
        final Integer id = map.get(s);
        if (id != null)
        {
            cache.lastRef = text;
            cache.lastStr = s;
            cache.lastId = id;
            return id;
        }

        // 3. Synchronize for allocating next sequential ID
        synchronized (stringLock)
        {
            final int assignedId = map.computeIfAbsent(s, key -> {
                final int nextId = stringCount++;
                if (nextId >= strings.length)
                {
                    strings = Arrays.copyOf(strings, Math.max(INITIAL_CAPACITY, strings.length * 2));
                }
                strings[nextId] = key;
                return nextId;
            });
            cache.lastRef = text;
            cache.lastStr = s;
            cache.lastId = assignedId;
            return assignedId;
        }
    }

    /**
     * Resolves an interned ID back to its original general string.
     * <p>
     * Lock-free \(O(1)\) array lookup.
     *
     * @param id
     *            the interned integer ID
     * @return the string, or {@code null} if out of bounds or negative
     */
    public String getString(final int id)
    {
        final String[] array = strings;
        return (id >= 0 && id < stringCount) ? array[id] : null;
    }

    /**
     * Returns the total number of distinct general strings interned in this dictionary.
     *
     * @return total general string count
     */
    public int getStringCount()
    {
        return stringCount;
    }

    /**
     * Returns the raw backing array of interned general strings for high-throughput batch scanning.
     * <p>
     * Callers must treat the returned array as read-only. Accessing elements by direct array index
     * avoids repeated volatile field dereferencing and bounds check method call overhead in hot loops.
     *
     * @return the raw array of general strings
     */
    public String[] getStringsArray()
    {
        return strings;
    }

    /**
     * Resolves an interned string ID to its pre-split array of IP address strings (delimited by '|').
     * <p>
     * Performs lock-free array lookup. If the string has not yet been split, splits it once,
     * caches the resulting {@code String[]} array, and returns it. This avoids repeated
     * string splitting overhead across millions of scanned request rows.
     *
     * @param id
     *            the interned string ID representing pipe-delimited IP addresses
     * @return array of IP address strings, or {@code null} if id is out of bounds or string is null
     */
    public String[] getSplitIpAddresses(final int id)
    {
        final String[][] cache = splitIpAddresses;
        if (id >= 0 && id < cache.length)
        {
            final String[] cached = cache[id];
            if (cached != null)
            {
                return cached;
            }
        }
        return computeSplitIpAddresses(id);
    }

    /**
     * Synchronously computes the split array for an IP address string ID and stores it in the cache.
     *
     * @param id
     *            the interned string ID
     * @return array of IP address strings, or {@code null} if id is invalid or string is null
     */
    private synchronized String[] computeSplitIpAddresses(final int id)
    {
        final String str = getString(id);
        if (str == null)
        {
            return null;
        }
        final String[] split = org.apache.commons.lang3.StringUtils.split(str, '|');
        if (id >= splitIpAddresses.length)
        {
            splitIpAddresses = Arrays.copyOf(splitIpAddresses, Math.max(splitIpAddresses.length * 2, id + 1));
        }
        splitIpAddresses[id] = split;
        return split;
    }

    // -------------------------------------------------------------------------
    // Representative Sample URLs Operations
    // -------------------------------------------------------------------------

    /**
     * Checks whether the representative sample URL quota has already been met for the specified timer ID.
     * Lock-free, zero-allocation \(O(1)\) primitive array check allowing caller threads to bypass URL string
     * construction entirely without `Integer` autoboxing or hash table traversal.
     *
     * @param timerId
     *            the interned timer name ID
     * @return {@code true} if quota reached (or {@code timerId < 0}); {@code false} otherwise
     */
    public boolean hasSufficientSamples(final int timerId)
    {
        if (timerId < 0)
        {
            return true;
        }
        final byte[] counts = this.sampleUrlCounts;
        return timerId < counts.length && counts[timerId] >= MAX_SAMPLE_URLS_PER_TIMER;
    }

    /**
     * Registers a representative sample URL for the specified timer ID if the maximum quota
     * ({@value #MAX_SAMPLE_URLS_PER_TIMER} URLs per timer) has not yet been reached across the dataset.
     * <p>
     * Uses fixed-size 10-element `String[]` staging buckets indexed by `timerId` to avoid array cloning
     * on each newly discovered sample URL.
     *
     * @param timerId
     *            the interned timer name ID
     * @param url
     *            the request URL char sequence (may be {@code null} or empty)
     */
    public void addSampleUrl(final int timerId, final CharSequence url)
    {
        if (url == null || url.length() == 0 || timerId < 0)
        {
            return;
        }

        synchronized (sampleUrlLock)
        {
            if (timerId >= sampleUrlCounts.length)
            {
                sampleUrlCounts = Arrays.copyOf(sampleUrlCounts, Math.max(sampleUrlCounts.length * 2, timerId + 1));
            }
            final int count = sampleUrlCounts[timerId];
            if (count >= MAX_SAMPLE_URLS_PER_TIMER)
            {
                return;
            }

            if (rawSampleUrls == null)
            {
                rawSampleUrls = new String[sampleUrlCounts.length][];
                for (int id = 0; id < cachedSampleUrls.length; id++)
                {
                    final CachedSampleUrl[] existingCached = cachedSampleUrls[id];
                    if (existingCached != null)
                    {
                        final String[] bucket = new String[MAX_SAMPLE_URLS_PER_TIMER];
                        for (int j = 0; j < existingCached.length; j++)
                        {
                            bucket[j] = existingCached[j].url();
                        }
                        rawSampleUrls[id] = bucket;
                    }
                }
            }
            else if (timerId >= rawSampleUrls.length)
            {
                rawSampleUrls = Arrays.copyOf(rawSampleUrls, Math.max(rawSampleUrls.length * 2, timerId + 1));
            }

            String[] bucket = rawSampleUrls[timerId];
            if (bucket == null)
            {
                bucket = new String[MAX_SAMPLE_URLS_PER_TIMER];
                rawSampleUrls[timerId] = bucket;
            }

            final String urlStr = url.toString();
            for (int i = 0; i < count; i++)
            {
                if (bucket[i].equals(urlStr))
                {
                    return;
                }
            }

            bucket[count] = urlStr;
            if (timerId < cachedSampleUrls.length)
            {
                cachedSampleUrls[timerId] = null;
            }
            sampleUrlCounts[timerId] = (byte) (count + 1);
        }
    }

    /**
     * Returns the array of precomputed sample URLs for the specified timer ID.
     * <p>
     * Once {@link #compactForReadOnly()} or {@link #readFrom(DataInput)} has completed, all non-empty
     * sample URL arrays are precomputed in {@link #cachedSampleUrls} and {@link #rawSampleUrls} is {@code null},
     * making both hit and miss paths 100% lock-free.
     *
     * @param timerId
     *            the interned timer name ID
     * @return array of cached sample URLs, or {@code null} if none exist
     */
    public CachedSampleUrl[] getCachedSampleUrls(final int timerId)
    {
        if (timerId < 0)
        {
            return null;
        }
        final CachedSampleUrl[][] cache = this.cachedSampleUrls;
        if (timerId < cache.length)
        {
            final CachedSampleUrl[] existing = cache[timerId];
            if (existing != null)
            {
                return existing;
            }
        }
        final byte[] counts = this.sampleUrlCounts;
        if (this.rawSampleUrls == null || timerId >= counts.length || counts[timerId] == 0)
        {
            return null;
        }
        return computeCachedSampleUrls(timerId);
    }

    /**
     * Synchronously computes precomputed sample URLs for the given timer ID and caches them.
     *
     * @param timerId
     *            the interned timer name ID
     * @return array of cached sample URLs
     */
    private CachedSampleUrl[] computeCachedSampleUrls(final int timerId)
    {
        synchronized (sampleUrlLock)
        {
            final int count = sampleUrlCounts[timerId];
            final String[] bucket = rawSampleUrls[timerId];
            final CachedSampleUrl[] arr = new CachedSampleUrl[count];
            for (int i = 0; i < count; i++)
            {
                arr[i] = new CachedSampleUrl(bucket[i]);
            }
            if (timerId >= cachedSampleUrls.length)
            {
                cachedSampleUrls = Arrays.copyOf(cachedSampleUrls, Math.max(cachedSampleUrls.length * 2, timerId + 1));
            }
            cachedSampleUrls[timerId] = arr;
            return arr;
        }
    }

    // -------------------------------------------------------------------------
    // Post-Ingestion Read-Only Compaction
    // -------------------------------------------------------------------------

    /**
     * Releases all ingestion-only reverse lookup maps, thread-local memoization caches, and raw sample URL
     * staging buffers, while trimming forward lookup arrays to their exact element counts and precomputing
     * all {@link CachedSampleUrl} arrays for lock-free Phase 2 query execution.
     * <p>
     * <b>Memory &amp; GC Lifecycle Rationale:</b>
     * During Phase 1 cold-run CSV parsing, {@link GlobalDictionaries} maintains {@link ConcurrentHashMap}
     * reverse indexes ({@code String -> ID} and {@code AgentTestCase -> ID}) so parser threads can intern
     * strings in \(O(1)\) time. Once {@link com.xceptance.xlt.report.storage.ChunkIngestionCollector#finish()}
     * seals all chunks, Phase 2 ({@link com.xceptance.xlt.report.ChunkQueryEngine}) only resolves integer IDs
     * forward to strings ({@code ID -> String}) via direct array indexing. Releasing the reverse maps and
     * staging buffers before Phase 2 starts frees Old Gen heap occupied by hash table nodes, table spines,
     * thread-local caches, and geometric array slack before {@link com.xceptance.xlt.report.StatisticsProcessor}
     * allocates per-thread report processors.
     */
    public synchronized void compactForReadOnly()
    {
        synchronized (timerLock)
        {
            timerNameToId = null;
            localTimerCache = ThreadLocal.withInitial(ThreadLocalTimerCache::new);
            if (timerNames.length > timerNameCount)
            {
                timerNames = Arrays.copyOf(timerNames, timerNameCount);
            }
        }

        synchronized (pairLock)
        {
            pairToId = null;
            localAgentTestCaseCache = ThreadLocal.withInitial(ThreadLocalAgentTestCaseCache::new);
            if (agentTestCases.length > agentTestCaseCount)
            {
                agentTestCases = Arrays.copyOf(agentTestCases, agentTestCaseCount);
            }
        }

        synchronized (stringLock)
        {
            stringToId = null;
            localStringCache = ThreadLocal.withInitial(ThreadLocalStringCache::new);
            if (strings.length > stringCount)
            {
                strings = Arrays.copyOf(strings, stringCount);
            }
        }

        if (splitIpAddresses.length > stringCount)
        {
            splitIpAddresses = Arrays.copyOf(splitIpAddresses, stringCount);
        }

        synchronized (sampleUrlLock)
        {
            int maxSampleTimerId = -1;
            for (int i = 0; i < sampleUrlCounts.length; i++)
            {
                if (sampleUrlCounts[i] > 0)
                {
                    maxSampleTimerId = i;
                }
            }
            final int targetSampleLen = Math.max(timerNameCount, maxSampleTimerId + 1);
            if (cachedSampleUrls.length != targetSampleLen)
            {
                cachedSampleUrls = Arrays.copyOf(cachedSampleUrls, targetSampleLen);
            }
            if (rawSampleUrls != null)
            {
                for (int i = 0; i <= maxSampleTimerId; i++)
                {
                    final String[] bucket = rawSampleUrls[i];
                    final int count = sampleUrlCounts[i];
                    if (count > 0 && cachedSampleUrls[i] == null)
                    {
                        final CachedSampleUrl[] arr = new CachedSampleUrl[count];
                        for (int j = 0; j < count; j++)
                        {
                            arr[j] = new CachedSampleUrl(bucket[j]);
                        }
                        cachedSampleUrls[i] = arr;
                    }
                }
                rawSampleUrls = null;
            }
            if (sampleUrlCounts.length != targetSampleLen)
            {
                sampleUrlCounts = Arrays.copyOf(sampleUrlCounts, targetSampleLen);
            }
        }
    }

    /**
     * Returns whether this dictionary instance has been compacted for read-only query execution
     * (all ingestion-only reverse lookup maps and raw sample URL staging buffers released).
     *
     * @return {@code true} if compacted; {@code false} if any ingestion reverse map or staging buffer is active
     */
    public boolean isCompacted()
    {
        return timerNameToId == null && pairToId == null && stringToId == null && rawSampleUrls == null;
    }

    // -------------------------------------------------------------------------
    // Serialization / Deserialization
    // -------------------------------------------------------------------------

    /**
     * Serializes all interned dictionaries and sample URLs to the specified data output stream.
     * Supports serialization both before and after {@link #compactForReadOnly()}.
     *
     * @param out
     *            the target {@link DataOutput}
     * @throws IOException
     *             if an I/O error occurs
     */
    public void writeTo(final DataOutput out) throws IOException
    {
        // Header
        out.writeInt(MAGIC);
        out.writeInt(VERSION);

        // 1. Timer names
        final int tCount = timerNameCount;
        out.writeInt(tCount);
        final String[] tArr = timerNames;
        for (int i = 0; i < tCount; i++)
        {
            StorageIoUtils.writeUtfString(out, tArr[i]);
        }

        // 2. Agent + TestCase pairs
        final int atCount = agentTestCaseCount;
        out.writeInt(atCount);
        final AgentTestCase[] atArr = agentTestCases;
        for (int i = 0; i < atCount; i++)
        {
            final AgentTestCase pair = atArr[i];
            StorageIoUtils.writeUtfString(out, pair.agentName());
            StorageIoUtils.writeUtfString(out, pair.testCaseName());
        }

        // 3. General strings
        final int sCount = stringCount;
        out.writeInt(sCount);
        final String[] sArr = strings;
        for (int i = 0; i < sCount; i++)
        {
            StorageIoUtils.writeUtfString(out, sArr[i]);
        }

        // 4. Sample URLs by timer ID
        synchronized (sampleUrlLock)
        {
            int uCount = 0;
            for (int i = 0; i < sampleUrlCounts.length; i++)
            {
                if (sampleUrlCounts[i] > 0)
                {
                    uCount++;
                }
            }
            out.writeInt(uCount);
            for (int timerId = 0; timerId < sampleUrlCounts.length; timerId++)
            {
                final int count = sampleUrlCounts[timerId];
                if (count > 0)
                {
                    out.writeInt(timerId);
                    out.writeInt(count);
                    final CachedSampleUrl[] cached = timerId < cachedSampleUrls.length ? cachedSampleUrls[timerId] : null;
                    if (cached != null)
                    {
                        for (int j = 0; j < count; j++)
                        {
                            StorageIoUtils.writeUtfString(out, cached[j].url());
                        }
                    }
                    else
                    {
                        final String[] raw = rawSampleUrls[timerId];
                        for (int j = 0; j < count; j++)
                        {
                            StorageIoUtils.writeUtfString(out, raw[j]);
                        }
                    }
                }
            }
        }
    }

    /**
     * Deserializes global dictionaries and sample URLs from the specified data input stream.
     * <p>
     * Populates only the compact forward lookup arrays and precomputed {@link CachedSampleUrl} arrays,
     * omitting reverse {@link ConcurrentHashMap} instances and `CopyOnWriteArrayList` wrappers so warm
     * cache loads start directly in compacted read-only state.
     *
     * @param in
     *            the source {@link DataInput}
     * @return reconstructed {@link GlobalDictionaries} instance in compacted read-only state
     * @throws IOException
     *             if an I/O error occurs
     */
    public static GlobalDictionaries readFrom(final DataInput in) throws IOException
    {
        final int magic = in.readInt();
        if (magic != MAGIC)
        {
            throw new IOException(String.format("Invalid dictionaries file format: magic 0x%08X != 0x%08X", magic, MAGIC));
        }
        final int version = in.readInt();
        if (version != VERSION)
        {
            throw new IOException(String.format("Unsupported dictionaries file version: version %d != %d", version, VERSION));
        }

        final GlobalDictionaries dict = new GlobalDictionaries(false);

        // 1. Timer names
        final int timerCount = in.readInt();
        dict.timerNames = new String[timerCount];
        dict.timerNameCount = timerCount;
        for (int i = 0; i < timerCount; i++)
        {
            dict.timerNames[i] = StorageIoUtils.readUtfString(in);
        }

        // 2. Agent + TestCase pairs
        final int pairCount = in.readInt();
        dict.agentTestCases = new AgentTestCase[pairCount];
        dict.agentTestCaseCount = pairCount;
        for (int i = 0; i < pairCount; i++)
        {
            final String agent = StorageIoUtils.readUtfString(in);
            final String tc = StorageIoUtils.readUtfString(in);
            dict.agentTestCases[i] = new AgentTestCase(agent, tc);
        }

        // 3. General strings
        final int strCount = in.readInt();
        dict.strings = new String[strCount];
        dict.splitIpAddresses = new String[strCount][];
        dict.stringCount = strCount;
        for (int i = 0; i < strCount; i++)
        {
            dict.strings[i] = StorageIoUtils.readUtfString(in);
        }

        // 4. Sample URLs by timer ID (with backwards compatibility for streams ending right after general strings)
        dict.sampleUrlCounts = new byte[timerCount];
        dict.cachedSampleUrls = new CachedSampleUrl[timerCount][];
        final int uCount;
        try
        {
            uCount = in.readInt();
        }
        catch (final java.io.EOFException e)
        {
            return dict;
        }
        for (int i = 0; i < uCount; i++)
        {
            final int timerId = in.readInt();
            final int count = in.readInt();
            final CachedSampleUrl[] arr = new CachedSampleUrl[count];
            for (int j = 0; j < count; j++)
            {
                arr[j] = new CachedSampleUrl(StorageIoUtils.readUtfString(in));
            }
            if (timerId >= dict.cachedSampleUrls.length)
            {
                final int newCap = Math.max(dict.cachedSampleUrls.length * 2, timerId + 1);
                dict.cachedSampleUrls = Arrays.copyOf(dict.cachedSampleUrls, newCap);
                dict.sampleUrlCounts = Arrays.copyOf(dict.sampleUrlCounts, newCap);
            }
            dict.cachedSampleUrls[timerId] = arr;
            dict.sampleUrlCounts[timerId] = (byte) count;
        }

        return dict;
    }
}
