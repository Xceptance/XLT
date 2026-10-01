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
            final String host = (hostName == null || hostName.length() == 0) ? com.xceptance.xlt.api.engine.RequestData.UNKNOWN_HOST : hostName;
            return host;
        }
    }

    /** Initial capacity for interned dictionary arrays. */
    private static final int INITIAL_CAPACITY = 256;

    // -------------------------------------------------------------------------
    // Timer Names Storage
    // -------------------------------------------------------------------------

    /** Concurrent lookup map from timer name string to interned integer ID. */
    private final ConcurrentHashMap<String, Integer> timerNameToId = new ConcurrentHashMap<>();

    /** Volatile append-only array indexed by interned ID for lock-free \(O(1)\) reads. */
    private volatile String[] timerNames = new String[INITIAL_CAPACITY];

    /** Current number of distinct timer names interned. */
    private int timerNameCount = 0;

    // -------------------------------------------------------------------------
    // Agent + TestCase Unified Storage
    // -------------------------------------------------------------------------

    /** Concurrent lookup map from (agent, testCase) pair to interned integer ID. */
    private final ConcurrentHashMap<AgentTestCase, Integer> pairToId = new ConcurrentHashMap<>();

    /** Volatile append-only array indexed by interned ID for lock-free \(O(1)\) reads. */
    private volatile AgentTestCase[] agentTestCases = new AgentTestCase[INITIAL_CAPACITY];

    /** Current number of distinct agent-testCase pairs interned. */
    private int agentTestCaseCount = 0;

    // -------------------------------------------------------------------------
    // General Strings Storage
    // -------------------------------------------------------------------------

    /** Concurrent lookup map from general string to interned integer ID. */
    private final ConcurrentHashMap<String, Integer> stringToId = new ConcurrentHashMap<>();

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
    private static final int MAX_SAMPLE_URLS_PER_TIMER = 5;

    /** Concurrent lookup map from timer name ID to list of representative sample URL strings. */
    private final ConcurrentHashMap<Integer, java.util.List<String>> sampleUrlsByTimer = new ConcurrentHashMap<>();

    /** Volatile append-only array of precomputed sample URLs indexed by timer ID for lock-free reads. */
    private volatile CachedSampleUrl[][] cachedSampleUrls = new CachedSampleUrl[INITIAL_CAPACITY][];

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

    private final ThreadLocal<ThreadLocalTimerCache> localTimerCache =
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

    private final ThreadLocal<ThreadLocalAgentTestCaseCache> localAgentTestCaseCache =
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

    private final ThreadLocal<ThreadLocalStringCache> localStringCache =
        ThreadLocal.withInitial(ThreadLocalStringCache::new);

    /**
     * Compares a {@link CharSequence} with a {@link String} character-by-character without
     * allocating a new {@link String} on the heap.
     *
     * @param cs
     *            the candidate char sequence (may be null)
     * @param s
     *            the known string (may be null)
     * @return {@code true} if both sequences are non-null and contain identical characters, or both null
     */
    public static boolean charSequenceEquals(final CharSequence cs, final String s)
    {
        if (cs == s)
        {
            return true;
        }
        if (cs == null || s == null)
        {
            return false;
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

    // -------------------------------------------------------------------------
    // Timer Names Operations
    // -------------------------------------------------------------------------

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
        if (name == cache.lastRef && cache.lastId != -1)
        {
            return cache.lastId;
        }
        if (cache.lastStr != null && charSequenceEquals(name, cache.lastStr))
        {
            cache.lastRef = name;
            return cache.lastId;
        }

        final String s = name.toString();

        // 2. Optimistic lock-free read for steady-state hits
        final Integer id = timerNameToId.get(s);
        if (id != null)
        {
            cache.lastRef = name;
            cache.lastStr = s;
            cache.lastId = id;
            return id;
        }

        // 3. Synchronize on the lookup map only when allocating a newly discovered string
        synchronized (timerNameToId)
        {
            final int assignedId = timerNameToId.computeIfAbsent(s, key -> {
                final int nextId = timerNameCount++;
                if (nextId >= timerNames.length)
                {
                    // Geometric array doubling: eliminates O(N) array clone churn
                    timerNames = Arrays.copyOf(timerNames, Math.max(timerNames.length * 2, nextId + 1));
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
        if (agent == cache.lastAgentRef && testCase == cache.lastTestCaseRef && cache.lastId != -1)
        {
            return cache.lastId;
        }

        // Fast string/char-sequence match without allocating new Strings or AgentTestCase objects
        if (cache.lastAgentStr != null && cache.lastTestCaseStr != null &&
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

        // 2. Optimistic lock-free read
        final Integer id = pairToId.get(pair);
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
        synchronized (pairToId)
        {
            final int assignedId = pairToId.computeIfAbsent(pair, key -> {
                final int nextId = agentTestCaseCount++;
                if (nextId >= agentTestCases.length)
                {
                    agentTestCases = Arrays.copyOf(agentTestCases, Math.max(agentTestCases.length * 2, nextId + 1));
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
            if (pair == null)
            {
                continue;
            }
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
        if (text == cache.lastRef && cache.lastId != -1)
        {
            return cache.lastId;
        }
        if (cache.lastStr != null && charSequenceEquals(text, cache.lastStr))
        {
            cache.lastRef = text;
            return cache.lastId;
        }

        final String s = text.toString();

        // 2. Optimistic lock-free read
        final Integer id = stringToId.get(s);
        if (id != null)
        {
            cache.lastRef = text;
            cache.lastStr = s;
            cache.lastId = id;
            return id;
        }

        // 3. Synchronize for allocating next sequential ID
        synchronized (stringToId)
        {
            final int assignedId = stringToId.computeIfAbsent(s, key -> {
                final int nextId = stringCount++;
                if (nextId >= strings.length)
                {
                    final int newCapacity = Math.max(strings.length * 2, nextId + 1);
                    strings = Arrays.copyOf(strings, newCapacity);
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
     * Registers a representative sample URL for the specified timer ID if the maximum quota
     * ({@value #MAX_SAMPLE_URLS_PER_TIMER} URLs per timer) has not yet been reached across the dataset.
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

        final java.util.List<String> existing = sampleUrlsByTimer.get(timerId);
        if (existing != null && existing.size() >= MAX_SAMPLE_URLS_PER_TIMER)
        {
            return;
        }

        final java.util.List<String> list = sampleUrlsByTimer.computeIfAbsent(timerId, k -> new java.util.concurrent.CopyOnWriteArrayList<>());
        if (list.size() < MAX_SAMPLE_URLS_PER_TIMER)
        {
            final String urlStr = url.toString();
            if (!list.contains(urlStr))
            {
                list.add(urlStr);
            }
        }
    }

    /**
     * Returns the array of precomputed sample URLs for the specified timer ID.
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
        return computeCachedSampleUrls(timerId);
    }

    /**
     * Synchronously computes precomputed sample URLs for the given timer ID and caches them.
     *
     * @param timerId
     *            the interned timer name ID
     * @return array of cached sample URLs, or {@code null} if none exist
     */
    private synchronized CachedSampleUrl[] computeCachedSampleUrls(final int timerId)
    {
        if (timerId < cachedSampleUrls.length && cachedSampleUrls[timerId] != null)
        {
            return cachedSampleUrls[timerId];
        }
        final java.util.List<String> list = sampleUrlsByTimer.get(timerId);
        if (list == null || list.isEmpty())
        {
            return null;
        }
        final CachedSampleUrl[] arr = new CachedSampleUrl[list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            arr[i] = new CachedSampleUrl(list.get(i));
        }
        if (timerId >= cachedSampleUrls.length)
        {
            cachedSampleUrls = Arrays.copyOf(cachedSampleUrls, Math.max(cachedSampleUrls.length * 2, timerId + 1));
        }
        cachedSampleUrls[timerId] = arr;
        return arr;
    }

    // -------------------------------------------------------------------------
    // Serialization / Deserialization
    // -------------------------------------------------------------------------

    /**
     * Serializes all interned dictionaries and sample URLs to the specified data output stream.
     *
     * @param out
     *            the target {@link DataOutput}
     * @throws IOException
     *             if an I/O error occurs
     */
    public void writeTo(final DataOutput out) throws IOException
    {
        // 1. Timer names
        final int tCount = timerNameCount;
        out.writeInt(tCount);
        final String[] tArr = timerNames;
        for (int i = 0; i < tCount; i++)
        {
            out.writeUTF(tArr[i]);
        }

        // 2. Agent + TestCase pairs
        final int atCount = agentTestCaseCount;
        out.writeInt(atCount);
        final AgentTestCase[] atArr = agentTestCases;
        for (int i = 0; i < atCount; i++)
        {
            final AgentTestCase pair = atArr[i];
            out.writeUTF(pair.agentName());
            out.writeUTF(pair.testCaseName());
        }

        // 3. General strings
        final int sCount = stringCount;
        out.writeInt(sCount);
        final String[] sArr = strings;
        for (int i = 0; i < sCount; i++)
        {
            out.writeUTF(sArr[i]);
        }

        // 4. Sample URLs by timer ID
        final int uCount = sampleUrlsByTimer.size();
        out.writeInt(uCount);
        for (final java.util.Map.Entry<Integer, java.util.List<String>> entry : sampleUrlsByTimer.entrySet())
        {
            out.writeInt(entry.getKey());
            final java.util.List<String> list = entry.getValue();
            out.writeInt(list.size());
            for (final String u : list)
            {
                out.writeUTF(u);
            }
        }
    }

    /**
     * Deserializes global dictionaries and sample URLs from the specified data input stream.
     *
     * @param in
     *            the source {@link DataInput}
     * @return reconstructed {@link GlobalDictionaries} instance
     * @throws IOException
     *             if an I/O error occurs
     */
    public static GlobalDictionaries readFrom(final DataInput in) throws IOException
    {
        final GlobalDictionaries dict = new GlobalDictionaries();

        // 1. Timer names
        final int timerCount = in.readInt();
        dict.timerNames = new String[Math.max(INITIAL_CAPACITY, timerCount)];
        dict.timerNameCount = timerCount;
        for (int i = 0; i < timerCount; i++)
        {
            final String s = in.readUTF();
            dict.timerNames[i] = s;
            dict.timerNameToId.put(s, i);
        }

        // 2. Agent + TestCase pairs
        final int pairCount = in.readInt();
        dict.agentTestCases = new AgentTestCase[Math.max(INITIAL_CAPACITY, pairCount)];
        dict.agentTestCaseCount = pairCount;
        for (int i = 0; i < pairCount; i++)
        {
            final String agent = in.readUTF();
            final String tc = in.readUTF();
            final AgentTestCase pair = new AgentTestCase(agent, tc);
            dict.agentTestCases[i] = pair;
            dict.pairToId.put(pair, i);
        }

        // 3. General strings
        final int strCount = in.readInt();
        dict.strings = new String[Math.max(INITIAL_CAPACITY, strCount)];
        dict.splitIpAddresses = new String[Math.max(INITIAL_CAPACITY, strCount)][];
        dict.stringCount = strCount;
        for (int i = 0; i < strCount; i++)
        {
            final String s = in.readUTF();
            dict.strings[i] = s;
            dict.stringToId.put(s, i);
        }

        // 4. Sample URLs by timer ID (with backwards compatibility)
        try
        {
            final int uCount = in.readInt();
            for (int i = 0; i < uCount; i++)
            {
                final int timerId = in.readInt();
                final int count = in.readInt();
                final java.util.List<String> list = new java.util.concurrent.CopyOnWriteArrayList<>();
                for (int j = 0; j < count; j++)
                {
                    list.add(in.readUTF());
                }
                dict.sampleUrlsByTimer.put(timerId, list);
            }
        }
        catch (final java.io.EOFException e)
        {
            // Backwards compatibility with older dictionary files lacking sample URLs
        }

        return dict;
    }
}
