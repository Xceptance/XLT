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
package com.xceptance.xlt.report.mergerules;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import com.xceptance.util.fastregex.FastRegex;

import com.sun.management.ThreadMXBean;
import com.xceptance.common.io.XltBufferedByteLineReader;
import com.xceptance.common.io.fs.FsResource;
import com.xceptance.common.io.fs.VirtualFileSystem;
import com.xceptance.common.util.CsvByteColumns;
import com.xceptance.common.util.CsvByteLineDecoder;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.XltEngine;
import com.xceptance.xlt.engine.XltExecutionContext;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 * Isolated MergeRule & Regex Engine benchmark evaluating FastRegex vs java.util.regex.Pattern
 * on real load test datasets (ariat, aritzia, bbw).
 */
public class FastRegexEvalBenchmark
{
    private static final ThreadMXBean THREAD_MX = (ThreadMXBean) ManagementFactory.getThreadMXBean();
    private static volatile int BLACKHOLE;

    private record RawRequest(byte[] csvLine, String origName, String url) {}

    public static void main(final String[] args) throws Exception
    {
        System.out.println("================================================================================");
        System.out.println("  FASTREGEX vs. JAVA.UTIL.REGEX — REAL DATASET MERGE-RULE & REGEX BENCHMARK");
        System.out.println("================================================================================");
        System.out.println("JVM: " + System.getProperty("java.vm.name") + " " + System.getProperty("java.version"));
        System.out.println("CPUs: " + Runtime.getRuntime().availableProcessors());
        System.out.println();

        final String[][] datasets =
        {
            { "ariat", "/home/rschwietzke/projects/loadtest/test-results/xlt-result-ariat-lt-2025-315-20251119-165727" },
            { "aritzia", "/home/rschwietzke/projects/loadtest/test-results/xlt-result-aritzia-lt-2025-317-20260202-220012" },
            { "bbw", "/home/rschwietzke/projects/loadtest/bbw/results/xlt-result-bbw-bbw-2025-wah-2-281-20250717-070800" }
        };

        for (final String[] ds : datasets)
        {
            runDatasetBenchmark(ds[0], new File(ds[1]), 250_000);
        }
    }

    private static ReportGeneratorConfiguration loadConfig(final File resultsDir, final boolean fastRegexEnabled)
        throws Exception
    {
        try (final VirtualFileSystem vfs = VirtualFileSystem.open(resultsDir.getAbsolutePath()))
        {
            final FsResource inputDir = vfs.getRoot();
            XltExecutionContext.getCurrent().setTestSuiteHomeDir(inputDir);
            final FsResource configDir = inputDir.resolve(XltConstants.RESULT_CONFIG_DIR);
            if (configDir != null && configDir.isDirectory())
            {
                XltExecutionContext.getCurrent().setTestSuiteConfigDir(configDir);
            }
            else
            {
                XltExecutionContext.getCurrent().setTestSuiteConfigDir(inputDir);
            }
            XltPropertiesImpl props;
            try
            {
                props = new XltPropertiesImpl(inputDir, configDir, false, false);
                XltEngine.reset(props);
            }
            catch (final Exception e)
            {
                props = new XltPropertiesImpl(inputDir, configDir, false, true);
            }
            final Properties cli = new Properties();
            cli.setProperty(ReportGeneratorConfiguration.PROP_FAST_REGEX_ENABLED, Boolean.toString(fastRegexEnabled));
            return new ReportGeneratorConfiguration(props.getProperties(), null, cli);
        }
    }

    private static List<RawRequest> sampleRequests(final File resultsDir, final int targetRequests) throws Exception
    {
        final List<Path> timerFiles = new ArrayList<>();
        try (Stream<Path> agentDirs = Files.list(resultsDir.toPath()))
        {
            for (final Path agentDir : agentDirs.toList())
            {
                final String dirName = agentDir.getFileName().toString();
                if (!Files.isDirectory(agentDir) || "config".equals(dirName) || "telemetry-store".equals(dirName))
                {
                    continue;
                }
                try (Stream<Path> tcDirs = Files.list(agentDir))
                {
                    for (final Path tcDir : tcDirs.toList())
                    {
                        final String tcName = tcDir.getFileName().toString();
                        if (!Files.isDirectory(tcDir) || "Agent-JVM-Monitor".equals(tcName) || "CustomSampler".equals(tcName))
                        {
                            continue;
                        }
                        try (Stream<Path> userDirs = Files.list(tcDir))
                        {
                            for (final Path userDir : userDirs.toList())
                            {
                                final Path gz = userDir.resolve("timers.csv.gz");
                                if (Files.isRegularFile(gz))
                                {
                                    timerFiles.add(gz);
                                }
                            }
                        }
                    }
                }
            }
        }

        Collections.sort(timerFiles);
        Collections.shuffle(timerFiles, new Random(42L));

        final List<RawRequest> requests = new ArrayList<>(targetRequests);
        final CsvByteColumns cols = new CsvByteColumns(50);

        outer:
        for (final Path gz : timerFiles)
        {
            try (InputStream fis = new FileInputStream(gz.toFile());
                 InputStream gis = new GZIPInputStream(fis, 32768);
                 XltBufferedByteLineReader reader = new XltBufferedByteLineReader(gis, 32768))
            {
                byte[] line;
                while ((line = reader.readLine()) != null)
                {
                    if (line.length > 0 && line[0] == 'R')
                    {
                        cols.clear();
                        CsvByteLineDecoder.parse(cols, line);
                        final RequestData rd = new RequestData();
                        rd.setBaseValues(cols);
                        rd.setRemainingValues(cols);
                        requests.add(new RawRequest(line, rd.getName(), rd.getUrl()));
                        if (requests.size() >= targetRequests)
                        {
                            break outer;
                        }
                    }
                }
            }
            catch (final Exception ignored)
            {
            }
        }
        return requests;
    }

    private static void runDatasetBenchmark(final String name, final File resultsDir, final int maxSample) throws Exception
    {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("DATASET: " + name + " (" + resultsDir.getAbsolutePath() + ")");
        System.out.println("--------------------------------------------------------------------------------");

        final ReportGeneratorConfiguration fastCfg = loadConfig(resultsDir, true);
        final List<MergeRule> fastRulesInit = fastCfg.getMergeRules();

        int regexConditions = 0;
        int nativeFastConditions = 0;
        int fallbackFastConditions = 0;
        final List<String> urlRegexes = new ArrayList<>();
        final List<String> allRegexes = new ArrayList<>();

        for (final MergeRule rule : fastRulesInit)
        {
            for (final Condition c : rule.getIncludeConditions())
            {
                if (!c.isEmpty() && c.isUsingFastRegex())
                {
                    regexConditions++;
                    final String pat = c.getPattern();
                    allRegexes.add(pat);
                    if ("u".equals(c.getTypeCode()))
                    {
                        urlRegexes.add(pat);
                    }
                    final FastRegex.Pattern fr = RegExUtils.getFastRegex(pat);
                    if (fr.isFallback())
                    {
                        fallbackFastConditions++;
                    }
                    else
                    {
                        nativeFastConditions++;
                    }
                }
            }
            for (final Condition c : rule.getExcludeConditions())
            {
                if (!c.isEmpty() && c.isUsingFastRegex())
                {
                    regexConditions++;
                    final String pat = c.getPattern();
                    allRegexes.add(pat);
                    if ("u".equals(c.getTypeCode()))
                    {
                        urlRegexes.add(pat);
                    }
                    final FastRegex.Pattern fr = RegExUtils.getFastRegex(pat);
                    if (fr.isFallback())
                    {
                        fallbackFastConditions++;
                    }
                    else
                    {
                        nativeFastConditions++;
                    }
                }
            }
        }

        final List<RawRequest> sample = sampleRequests(resultsDir, maxSample);
        final Set<String> uniqueUrls = new HashSet<>();
        final Set<String> uniqueNames = new HashSet<>();
        for (final RawRequest rr : sample)
        {
            uniqueUrls.add(rr.url());
            uniqueNames.add(rr.origName());
        }
        final List<String> uniqueUrlList = new ArrayList<>(uniqueUrls);

        System.out.printf("  MergeRules total:      %d%n", fastRulesInit.size());
        System.out.printf("  Regex conditions:      %d (Native FastRegex: %d, JDK Fallback: %d)%n",
                          regexConditions, nativeFastConditions, fallbackFastConditions);
        System.out.printf("  URL regex conditions:  %d%n", urlRegexes.size());
        System.out.printf("  Sampled R records:     %,d (Unique URLs: %,d [%.1f%%], Unique Names: %,d)%n",
                          sample.size(), uniqueUrls.size(),
                          (100.0 * uniqueUrls.size()) / Math.max(1, sample.size()),
                          uniqueNames.size());

        // 1. Exact Parity Check across all sampled RequestData records
        final ReportGeneratorConfiguration fastParityCfg = loadConfig(resultsDir, true);
        final MergeRuleProcessor fastParityProc = new MergeRuleProcessor(
            fastParityCfg.getMergeRules(), fastParityCfg.getRemoveIndexesFromRequestNames());

        final ReportGeneratorConfiguration jdkParityCfg = loadConfig(resultsDir, false);
        final MergeRuleProcessor jdkParityProc = new MergeRuleProcessor(
            jdkParityCfg.getMergeRules(), jdkParityCfg.getRemoveIndexesFromRequestNames());

        final CsvByteColumns cols1 = new CsvByteColumns(50);
        final CsvByteColumns cols2 = new CsvByteColumns(50);
        int mismatches = 0;
        for (int i = 0; i < sample.size(); i++)
        {
            final byte[] line = sample.get(i).csvLine();

            cols1.clear();
            CsvByteLineDecoder.parse(cols1, line);
            final RequestData rFast = new RequestData();
            rFast.setBaseValues(cols1);
            rFast.setRemainingValues(cols1);
            final RequestData outFast = fastParityProc.postprocess(rFast);

            cols2.clear();
            CsvByteLineDecoder.parse(cols2, line);
            final RequestData rJdk = new RequestData();
            rJdk.setBaseValues(cols2);
            rJdk.setRemainingValues(cols2);
            final RequestData outJdk = jdkParityProc.postprocess(rJdk);

            final String nameFast = (outFast == null) ? "<DROP>" : outFast.getName();
            final String nameJdk = (outJdk == null) ? "<DROP>" : outJdk.getName();
            if (!Objects.equals(nameFast, nameJdk))
            {
                mismatches++;
                if (mismatches <= 5)
                {
                    System.out.printf("  MISMATCH at %d: url=%s fast=%s jdk=%s%n",
                                      i, sample.get(i).url(), nameFast, nameJdk);
                }
            }
        }
        System.out.printf("  Parity check:          %,d / %,d identical (%d mismatches)%n",
                          sample.size() - mismatches, sample.size(), mismatches);
        if (mismatches > 0)
        {
            throw new IllegalStateException("Parity mismatch on dataset " + name);
        }

        // 2. Benchmark A: Uncached Regex Engine Throughput on Dataset's URL Regex Rules x Stream URLs
        final List<String> rulesToBench = urlRegexes.isEmpty() ? allRegexes : urlRegexes;
        if (!rulesToBench.isEmpty())
        {
            benchmarkRawRegexEngine(rulesToBench, uniqueUrlList, sample);
            benchmarkFreshMatcherPerUrl(rulesToBench, sample);
        }

        // 3. Benchmark B: Realistic MergeRuleProcessor (Cold-to-Warm per pass)
        benchmarkMergeRuleProcessor(resultsDir, sample);
        System.out.println();
    }

    private static void benchmarkRawRegexEngine(final List<String> patterns,
                                                final List<String> uniqueUrls,
                                                final List<RawRequest> stream)
    {
        final FastRegex.Matcher[] fastMatchers = new FastRegex.Matcher[patterns.size()];
        final Matcher[] jdkMatchers = new Matcher[patterns.size()];
        for (int i = 0; i < patterns.size(); i++)
        {
            fastMatchers[i] = FastRegex.compile(patterns.get(i)).matcher();
            jdkMatchers[i] = Pattern.compile(patterns.get(i)).matcher("");
        }

        final int n = Math.min(100_000, stream.size());
        final String[] urls = new String[n];
        for (int i = 0; i < n; i++)
        {
            urls[i] = stream.get(i).url();
        }

        for (int w = 0; w < 3; w++)
        {
            BLACKHOLE ^= evalFastAll(fastMatchers, urls);
            BLACKHOLE ^= evalJdkAll(jdkMatchers, urls);
        }

        final int rounds = 5;
        long fastNsTotal = 0;
        long fastBytesTotal = 0;
        long jdkNsTotal = 0;
        long jdkBytesTotal = 0;

        for (int r = 0; r < rounds; r++)
        {
            long b0 = THREAD_MX.getCurrentThreadAllocatedBytes();
            long t0 = System.nanoTime();
            int fHits = evalFastAll(fastMatchers, urls);
            fastNsTotal += (System.nanoTime() - t0);
            fastBytesTotal += (THREAD_MX.getCurrentThreadAllocatedBytes() - b0);

            b0 = THREAD_MX.getCurrentThreadAllocatedBytes();
            t0 = System.nanoTime();
            int jHits = evalJdkAll(jdkMatchers, urls);
            jdkNsTotal += (System.nanoTime() - t0);
            jdkBytesTotal += (THREAD_MX.getCurrentThreadAllocatedBytes() - b0);

            if (fHits != jHits)
            {
                throw new IllegalStateException("Hit count mismatch: fast=" + fHits + " jdk=" + jHits);
            }
            BLACKHOLE ^= (fHits + jHits);
        }

        final long totalUrls = (long) rounds * n;
        final double fastNsPerUrl = (double) fastNsTotal / totalUrls;
        final double jdkNsPerUrl = (double) jdkNsTotal / totalUrls;
        final double fastMUrlsSec = (totalUrls * 1e3) / fastNsTotal;
        final double jdkMUrlsSec = (totalUrls * 1e3) / jdkNsTotal;
        final double speedup = (double) jdkNsTotal / fastNsTotal;

        System.out.printf("  [A] Uncached URL Regex Evaluation (reused Matcher, %d URL rules x %,d URLs x %d rounds):%n",
                          patterns.size(), n, rounds);
        System.out.printf("      FastRegex (enabled):  %8.1f ns/URL | %6.2f M URLs/s | %5.1f B/URL allocated%n",
                          fastNsPerUrl, fastMUrlsSec, (double) fastBytesTotal / totalUrls);
        System.out.printf("      JDK Regex (disabled): %8.1f ns/URL | %6.2f M URLs/s | %5.1f B/URL allocated%n",
                          jdkNsPerUrl, jdkMUrlsSec, (double) jdkBytesTotal / totalUrls);
        System.out.printf("      --> Uncached Regex Speedup: %.2fx faster (%.1f%% latency reduction)%n",
                          speedup, (1.0 - (double) fastNsTotal / jdkNsTotal) * 100.0);
    }

    private static void benchmarkFreshMatcherPerUrl(final List<String> patterns, final List<RawRequest> stream)
    {
        final FastRegex.Pattern[] fastPatterns = new FastRegex.Pattern[patterns.size()];
        final Pattern[] jdkPatterns = new Pattern[patterns.size()];
        for (int i = 0; i < patterns.size(); i++)
        {
            fastPatterns[i] = FastRegex.compile(patterns.get(i));
            jdkPatterns[i] = Pattern.compile(patterns.get(i));
        }

        final int n = Math.min(50_000, stream.size());
        final String[] urls = new String[n];
        for (int i = 0; i < n; i++)
        {
            urls[i] = stream.get(i).url();
        }

        for (int w = 0; w < 2; w++)
        {
            BLACKHOLE ^= evalFreshFastAll(fastPatterns, urls);
            BLACKHOLE ^= evalFreshJdkAll(jdkPatterns, urls);
        }

        final int rounds = 3;
        long fastNsTotal = 0;
        long fastBytesTotal = 0;
        long jdkNsTotal = 0;
        long jdkBytesTotal = 0;

        for (int r = 0; r < rounds; r++)
        {
            long b0 = THREAD_MX.getCurrentThreadAllocatedBytes();
            long t0 = System.nanoTime();
            int fHits = evalFreshFastAll(fastPatterns, urls);
            fastNsTotal += (System.nanoTime() - t0);
            fastBytesTotal += (THREAD_MX.getCurrentThreadAllocatedBytes() - b0);

            b0 = THREAD_MX.getCurrentThreadAllocatedBytes();
            t0 = System.nanoTime();
            int jHits = evalFreshJdkAll(jdkPatterns, urls);
            jdkNsTotal += (System.nanoTime() - t0);
            jdkBytesTotal += (THREAD_MX.getCurrentThreadAllocatedBytes() - b0);

            if (fHits != jHits)
            {
                throw new IllegalStateException("Fresh Matcher hit mismatch: fast=" + fHits + " jdk=" + jHits);
            }
            BLACKHOLE ^= (fHits + jHits);
        }

        final long totalUrls = (long) rounds * n;
        System.out.printf("  [A2] Fresh pattern.matcher().reset(url).find() per URL (%d rules x %,d URLs x %d rounds):%n",
                          patterns.size(), n, rounds);
        System.out.printf("      FastRegex (enabled):  %8.1f ns/URL | %6.2f M URLs/s | %5.1f B/URL allocated%n",
                          (double) fastNsTotal / totalUrls, (totalUrls * 1e3) / fastNsTotal, (double) fastBytesTotal / totalUrls);
        System.out.printf("      JDK Regex (disabled): %8.1f ns/URL | %6.2f M URLs/s | %5.1f B/URL allocated%n",
                          (double) jdkNsTotal / totalUrls, (totalUrls * 1e3) / jdkNsTotal, (double) jdkBytesTotal / totalUrls);
        System.out.printf("      --> Fresh-Matcher Speedup: %.2fx faster (%.1f%% latency reduction)%n",
                          (double) jdkNsTotal / fastNsTotal, (1.0 - (double) fastNsTotal / jdkNsTotal) * 100.0);
    }

    private static int evalFastAll(final FastRegex.Matcher[] matchers, final String[] urls)
    {
        int hits = 0;
        for (int i = 0; i < urls.length; i++)
        {
            final String u = urls[i];
            for (int m = 0; m < matchers.length; m++)
            {
                final FastRegex.Matcher fm = matchers[m].reset(u);
                if (fm.find())
                {
                    hits += fm.end();
                    if (fm.groupCount() >= 1)
                    {
                        final CharSequence gv = fm.groupView(1);
                        if (gv != null)
                        {
                            hits += gv.length();
                        }
                    }
                    break;
                }
            }
        }
        return hits;
    }

    private static int evalJdkAll(final Matcher[] matchers, final String[] urls)
    {
        int hits = 0;
        for (int i = 0; i < urls.length; i++)
        {
            final String u = urls[i];
            for (int m = 0; m < matchers.length; m++)
            {
                final Matcher jm = matchers[m].reset(u);
                if (jm.find())
                {
                    hits += jm.end();
                    if (jm.groupCount() >= 1)
                    {
                        final String g = jm.group(1);
                        if (g != null)
                        {
                            hits += g.length();
                        }
                    }
                    break;
                }
            }
        }
        return hits;
    }

    private static int evalFreshFastAll(final FastRegex.Pattern[] patterns, final String[] urls)
    {
        int hits = 0;
        for (int i = 0; i < urls.length; i++)
        {
            final String u = urls[i];
            for (int m = 0; m < patterns.length; m++)
            {
                final FastRegex.Matcher fm = patterns[m].matcher(u);
                if (fm.find())
                {
                    hits += fm.end();
                    if (fm.groupCount() >= 1)
                    {
                        final CharSequence gv = fm.groupView(1);
                        if (gv != null)
                        {
                            hits += gv.length();
                        }
                    }
                    break;
                }
            }
        }
        return hits;
    }

    private static int evalFreshJdkAll(final Pattern[] patterns, final String[] urls)
    {
        int hits = 0;
        for (int i = 0; i < urls.length; i++)
        {
            final String u = urls[i];
            for (int m = 0; m < patterns.length; m++)
            {
                final Matcher jm = patterns[m].matcher(u);
                if (jm.find())
                {
                    hits += jm.end();
                    if (jm.groupCount() >= 1)
                    {
                        final String g = jm.group(1);
                        if (g != null)
                        {
                            hits += g.length();
                        }
                    }
                    break;
                }
            }
        }
        return hits;
    }

    private static void benchmarkMergeRuleProcessor(final File resultsDir, final List<RawRequest> sample)
        throws Exception
    {
        final int n = sample.size();
        final RequestData[] reqs = new RequestData[n];
        final String[] origNames = new String[n];
        final CsvByteColumns cols = new CsvByteColumns(50);
        for (int i = 0; i < n; i++)
        {
            cols.clear();
            CsvByteLineDecoder.parse(cols, sample.get(i).csvLine());
            final RequestData rd = new RequestData();
            rd.setBaseValues(cols);
            rd.setRemainingValues(cols);
            rd.getUrl();
            origNames[i] = rd.getName();
            reqs[i] = rd;
        }

        for (int w = 0; w < 2; w++)
        {
            final ReportGeneratorConfiguration fc = loadConfig(resultsDir, true);
            final MergeRuleProcessor fp = new MergeRuleProcessor(fc.getMergeRules(), fc.getRemoveIndexesFromRequestNames());
            BLACKHOLE ^= runProcessorPass(fp, reqs, origNames);

            final ReportGeneratorConfiguration jc = loadConfig(resultsDir, false);
            final MergeRuleProcessor jp = new MergeRuleProcessor(jc.getMergeRules(), jc.getRemoveIndexesFromRequestNames());
            BLACKHOLE ^= runProcessorPass(jp, reqs, origNames);
        }

        final int rounds = 5;
        long fastColdNs = 0;
        long fastColdBytes = 0;
        long jdkColdNs = 0;
        long jdkColdBytes = 0;

        for (int r = 0; r < rounds; r++)
        {
            final ReportGeneratorConfiguration fc = loadConfig(resultsDir, true);
            final MergeRuleProcessor fp = new MergeRuleProcessor(fc.getMergeRules(), fc.getRemoveIndexesFromRequestNames());
            resetNames(reqs, origNames);
            long b0 = THREAD_MX.getCurrentThreadAllocatedBytes();
            long t0 = System.nanoTime();
            BLACKHOLE ^= runProcessorPass(fp, reqs, origNames);
            fastColdNs += (System.nanoTime() - t0);
            fastColdBytes += (THREAD_MX.getCurrentThreadAllocatedBytes() - b0);

            final ReportGeneratorConfiguration jc = loadConfig(resultsDir, false);
            final MergeRuleProcessor jp = new MergeRuleProcessor(jc.getMergeRules(), jc.getRemoveIndexesFromRequestNames());
            resetNames(reqs, origNames);
            b0 = THREAD_MX.getCurrentThreadAllocatedBytes();
            t0 = System.nanoTime();
            BLACKHOLE ^= runProcessorPass(jp, reqs, origNames);
            jdkColdNs += (System.nanoTime() - t0);
            jdkColdBytes += (THREAD_MX.getCurrentThreadAllocatedBytes() - b0);
        }

        final long totalReqs = (long) rounds * n;
        final double fastNsReq = (double) fastColdNs / totalReqs;
        final double jdkNsReq = (double) jdkColdNs / totalReqs;
        final double fastMReqSec = (totalReqs * 1e3) / fastColdNs;
        final double jdkMReqSec = (totalReqs * 1e3) / jdkColdNs;
        final double speedup = (double) jdkColdNs / fastColdNs;

        System.out.printf("  [B] Full MergeRuleProcessor + LRUClockMap (%,d real requests x %d passes):%n", n, rounds);
        System.out.printf("      FastRegex (enabled):  %8.1f ns/req | %6.2f M req/s | %5.1f B/req allocated%n",
                          fastNsReq, fastMReqSec, (double) fastColdBytes / totalReqs);
        System.out.printf("      JDK Regex (disabled): %8.1f ns/req | %6.2f M req/s | %5.1f B/req allocated%n",
                          jdkNsReq, jdkMReqSec, (double) jdkColdBytes / totalReqs);
        System.out.printf("      --> MergeRuleProcessor Speedup: %.2fx faster (%.1f%% latency reduction)%n",
                          speedup, (1.0 - (double) fastColdNs / jdkColdNs) * 100.0);
    }

    private static void resetNames(final RequestData[] reqs, final String[] origNames)
    {
        for (int i = 0; i < reqs.length; i++)
        {
            reqs[i].setName(origNames[i]);
        }
    }

    private static int runProcessorPass(final MergeRuleProcessor proc, final RequestData[] reqs, final String[] origNames)
    {
        int acc = 0;
        for (int i = 0; i < reqs.length; i++)
        {
            final RequestData rd = reqs[i];
            rd.setName(origNames[i]);
            final RequestData out = proc.postprocess(rd);
            if (out != null)
            {
                acc += out.getName().length();
            }
        }
        return acc;
    }
}
