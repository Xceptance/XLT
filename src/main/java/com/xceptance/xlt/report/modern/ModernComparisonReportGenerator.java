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
package com.xceptance.xlt.report.modern;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xceptance.common.util.ProductInformation;
import com.xceptance.xlt.common.XltConstants;

/**
 * Unified generator for modern client-rendered comparison (2-run diff) and trend (N-run progression) HTML/JS reports.
 * <p>
 * Supports building bundles directly from N source {@code testreport.xml} documents (preserving full details such as
 * labels, configured percentiles, errors, events, network breakdowns, and configurations via a lossless
 * {@code comparison-runs.json.gz} snapshot), as well as reconstructing modern UI bundles from legacy
 * {@code diffreport.xml} or {@code trendreport.xml} files via {@code bin/update_report_ui.sh}.
 */
public class ModernComparisonReportGenerator
{
    private static final Logger LOG = LoggerFactory.getLogger(ModernComparisonReportGenerator.class);

    private static final DateTimeFormatter DATE_TO_STRING_FMT = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

    private static final Pattern HTTP_METHOD_TOKEN_PATTERN = Pattern.compile(
        "(?:^|\\s)\\[(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS|TRACE|CONNECT)\\](?=\\s|\\[\\d+\\]|$)");

    private static final Pattern COMMENT_BLOCK_BREAK_PATTERN = Pattern.compile("</?(?:p|div|li|br|h[1-6])[^>]*>",
                                                                       Pattern.CASE_INSENSITIVE);

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    private static final List<String> SUMMARY_TIERS = List.of("transactions", "actions", "requests", "customTimers", "pageLoadTimings");

    private static final List<String> WEB_VITAL_METRICS = List.of("lcp", "cls", "inp", "fcp", "fid", "ttfb");

    private static final List<String> SHARED_JS_ASSETS = List.of("theme.js", "report-utils.js", "table-copy-markdown.js",
                                                                 "echarts-6.0.0.min.js");

    private final ModernReportGenerator reportHelper = new ModernReportGenerator();

    private final ObjectMapper compactMapper = createCompactMapper();

    /**
     * Input descriptor for a single test run when generating a comparison or trend report directly from source reports.
     */
    public static final class RunInput
    {
        private final String reportName;

        private final File reportDir;

        private final Document testReportDoc;

        private final File scorecardFile;

        public RunInput(final String reportName, final File reportDir, final Document testReportDoc)
        {
            this(reportName, reportDir, testReportDoc,
                 reportDir != null ? new File(reportDir, XltConstants.SCORECARD_REPORT_XML_FILENAME) : null);
        }

        public RunInput(final String reportName, final File reportDir, final Document testReportDoc, final File scorecardFile)
        {
            this.reportName = reportName;
            this.reportDir = reportDir;
            this.testReportDoc = testReportDoc;
            this.scorecardFile = scorecardFile;
        }

        public String getReportName()
        {
            return reportName;
        }

        public File getReportDir()
        {
            return reportDir;
        }

        public Document getTestReportDoc()
        {
            return testReportDoc;
        }

        public File getScorecardFile()
        {
            return scorecardFile;
        }
    }

    /**
     * Parses a CLI flag value for {@code -linkToReports} (accepting {@code yes}/{@code true} or {@code no}/{@code false},
     * case-insensitively).
     *
     * @param value
     *            raw command-line option value
     * @return parsed boolean flag
     * @throws IllegalArgumentException
     *             if {@code value} is blank or not a recognized boolean token
     */
    public static boolean parseLinkToReportsOptionValue(final String value)
    {
        final String trimmed = StringUtils.trimToEmpty(value);
        if ("yes".equalsIgnoreCase(trimmed) || "true".equalsIgnoreCase(trimmed))
        {
            return true;
        }
        if ("no".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed))
        {
            return false;
        }
        throw new IllegalArgumentException("Invalid value for '-" + XltConstants.COMMANDLINE_OPTION_LINK_TO_REPORTS
                                           + "': '" + value + "' (expected 'yes' or 'no')");
    }

    /**
     * Generates a complete modern comparison or trend report from N parsed single-run {@code testreport.xml} documents,
     * writes a lossless {@code comparison-runs.json.gz} snapshot for future {@code update_report_ui} runs, copies static
     * templates, and emits all {@code js/*.js} bundles. Relative links to source single-run reports are disabled by
     * default.
     *
     * @param reportKind
     *            {@code "diff"} or {@code "trend"}
     * @param projectName
     *            resolved project name (may be blank)
     * @param runInputs
     *            ordered run inputs (index {@code 0..N-1})
     * @param configDir
     *            XLT {@code config/} directory containing {@code testreport-modern/} and {@code comparison-modern/}
     * @param outputDir
     *            target comparison/trend report output directory
     * @throws IOException
     *             if writing files or copying templates fails
     */
    public void generateFromTestReports(
        final String reportKind, final String projectName, final List<RunInput> runInputs,
        final File configDir, final File outputDir) throws IOException
    {
        generateFromTestReports(reportKind, projectName, runInputs, configDir, outputDir, false);
    }

    /**
     * Generates a complete modern comparison or trend report from N parsed single-run {@code testreport.xml} documents,
     * writes a lossless {@code comparison-runs.json.gz} snapshot for future {@code update_report_ui} runs, copies static
     * templates, and emits all {@code js/*.js} bundles.
     *
     * @param reportKind
     *            {@code "diff"} or {@code "trend"}
     * @param projectName
     *            resolved project name (may be blank)
     * @param runInputs
     *            ordered run inputs (index {@code 0..N-1})
     * @param configDir
     *            XLT {@code config/} directory containing {@code testreport-modern/} and {@code comparison-modern/}
     * @param outputDir
     *            target comparison/trend report output directory
     * @param linkToReports
     *            whether to emit relative {@code reportHref} links to each source single-run report on disk
     * @throws IOException
     *             if writing files or copying templates fails
     */
    public void generateFromTestReports(
        final String reportKind, final String projectName, final List<RunInput> runInputs,
        final File configDir, final File outputDir, final boolean linkToReports) throws IOException
    {
        final ComparisonModel model = buildModelFromTestReports(reportKind, projectName, runInputs, outputDir, linkToReports);
        FileUtils.forceMkdir(outputDir);
        writeSnapshot(model, new File(outputDir, XltConstants.COMPARISON_SNAPSHOT_FILENAME));
        copyTemplateResources(configDir, outputDir);
        writeBundlesFromModel(model, outputDir, linkToReports);
    }

    /**
     * Reconstructs and writes a modern diff report directly from a legacy {@code diffreport.xml} document.
     *
     * @param diffDoc
     *            parsed {@code diffreport.xml} document
     * @param configDir
     *            XLT {@code config/} directory
     * @param outputDir
     *            target report directory
     * @throws IOException
     *             if template copy or bundle writing fails
     */
    public void generateFromDiffReportXml(final Document diffDoc, final File configDir, final File outputDir) throws IOException
    {
        final ComparisonModel model = buildModelFromDiffXml(diffDoc);
        FileUtils.forceMkdir(outputDir);
        copyTemplateResources(configDir, outputDir);
        writeBundlesFromModel(model, outputDir, false);
    }

    /**
     * Reconstructs and writes a modern trend report directly from a legacy {@code trendreport.xml} document.
     *
     * @param trendDoc
     *            parsed {@code trendreport.xml} document
     * @param configDir
     *            XLT {@code config/} directory
     * @param outputDir
     *            target report directory
     * @throws IOException
     *             if template copy or bundle writing fails
     */
    public void generateFromTrendReportXml(final Document trendDoc, final File configDir, final File outputDir) throws IOException
    {
        final ComparisonModel model = buildModelFromTrendXml(trendDoc);
        FileUtils.forceMkdir(outputDir);
        copyTemplateResources(configDir, outputDir);
        writeBundlesFromModel(model, outputDir, false);
    }

    /**
     * Updates templates and regenerates {@code js/*.js} bundles in an existing comparison or trend report directory
     * using {@code comparison-runs.json.gz} (preferred when present), {@code diffreport.xml}, or {@code trendreport.xml}.
     * Relative links to source single-run reports are omitted by default.
     *
     * @param configDir
     *            XLT {@code config/} directory
     * @param reportDir
     *            existing comparison or trend report directory
     * @throws Exception
     *             if no supported comparison/trend data file exists or parsing/writing fails
     */
    public void updateExistingReportDir(final File configDir, final File reportDir) throws Exception
    {
        updateExistingReportDir(configDir, reportDir, false);
    }

    /**
     * Updates templates and regenerates {@code js/*.js} bundles in an existing comparison or trend report directory
     * using {@code comparison-runs.json.gz} (preferred when present), {@code diffreport.xml}, or {@code trendreport.xml}.
     *
     * @param configDir
     *            XLT {@code config/} directory
     * @param reportDir
     *            existing comparison or trend report directory
     * @param linkToReports
     *            whether to preserve any relative {@code reportHref} links stored in the snapshot when emitting
     *            {@code js/report-meta.js}; when {@code false}, source report links are stripped from the bundle
     * @throws Exception
     *             if no supported comparison/trend data file exists or parsing/writing fails
     */
    public void updateExistingReportDir(final File configDir, final File reportDir, final boolean linkToReports) throws Exception
    {
        final File snapshotFile = new File(reportDir, XltConstants.COMPARISON_SNAPSHOT_FILENAME);
        final File diffXml = new File(reportDir, XltConstants.DIFF_REPORT_XML_FILENAME);
        final File trendXml = new File(reportDir, XltConstants.TREND_REPORT_XML_FILENAME);

        final ComparisonModel model;
        if (snapshotFile.isFile())
        {
            model = readSnapshot(snapshotFile);
        }
        else if (diffXml.isFile())
        {
            model = buildModelFromDiffXml(ModernReportGenerator.parseXmlDocument(diffXml));
        }
        else if (trendXml.isFile())
        {
            model = buildModelFromTrendXml(ModernReportGenerator.parseXmlDocument(trendXml));
        }
        else
        {
            throw new FileNotFoundException(
                "Could not find '" + XltConstants.COMPARISON_SNAPSHOT_FILENAME + "', '" + XltConstants.DIFF_REPORT_XML_FILENAME
                    + "', or '" + XltConstants.TREND_REPORT_XML_FILENAME + "' in '" + reportDir.getAbsolutePath() + "'");
        }

        copyTemplateResources(configDir, reportDir);
        writeBundlesFromModel(model, reportDir, linkToReports);
    }

    /**
     * Copies shared base static assets from {@code config/testreport-modern/} and comparison templates/scripts from
     * {@code config/comparison-modern/} into {@code outputDir}, avoiding duplicated static libraries in the repository.
     *
     * @param configDir
     *            XLT {@code config/} directory
     * @param outputDir
     *            target report directory
     * @throws IOException
     *             if any required template directory or asset is missing or copy fails
     */
    public void copyTemplateResources(final File configDir, final File outputDir) throws IOException
    {
        if (configDir == null || !configDir.isDirectory())
        {
            throw new FileNotFoundException("Config directory not found: " + configDir);
        }

        final File sharedTemplateDir = new File(configDir, XltConstants.MODERN_REPORT_RESOURCES_PATH);
        if (!sharedTemplateDir.isDirectory())
        {
            throw new FileNotFoundException("Modern report template directory not found: " + sharedTemplateDir);
        }

        final File comparisonTemplateDir = new File(configDir, XltConstants.MODERN_COMPARISON_RESOURCES_PATH);
        if (!comparisonTemplateDir.isDirectory())
        {
            throw new FileNotFoundException("Modern comparison template directory not found: " + comparisonTemplateDir);
        }

        FileUtils.forceMkdir(outputDir);
        final File dstCssDir = new File(outputDir, "css");
        final File dstJsDir = new File(outputDir, "js");
        FileUtils.forceMkdir(dstCssDir);
        FileUtils.forceMkdir(dstJsDir);

        final var svnFilter = FileFilterUtils.makeSVNAware(null);

        final File srcFonts = new File(sharedTemplateDir, "fonts");
        if (srcFonts.isDirectory())
        {
            FileUtils.copyDirectory(srcFonts, new File(outputDir, "fonts"), svnFilter, false);
        }

        final File srcImages = new File(sharedTemplateDir, "images");
        if (srcImages.isDirectory())
        {
            FileUtils.copyDirectory(srcImages, new File(outputDir, "images"), svnFilter, false);
        }

        final File srcBaseCss = new File(new File(sharedTemplateDir, "css"), "style.css");
        if (srcBaseCss.isFile())
        {
            FileUtils.copyFile(srcBaseCss, new File(dstCssDir, "style.css"), false);
        }

        final File srcSharedJsDir = new File(sharedTemplateDir, "js");
        for (final String jsName : SHARED_JS_ASSETS)
        {
            final File srcJs = new File(srcSharedJsDir, jsName);
            if (srcJs.isFile())
            {
                FileUtils.copyFile(srcJs, new File(dstJsDir, jsName), false);
            }
        }

        FileUtils.copyDirectory(comparisonTemplateDir, outputDir, svnFilter, false);
    }

    // =========================================================================
    // Model Builders (TestReports / Diff XML / Trend XML)
    // =========================================================================

    private ComparisonModel buildModelFromTestReports(
        final String reportKind, final String projectName, final List<RunInput> runInputs,
        final File outputDir, final boolean linkToReports)
    {
        final int runCount = runInputs.size();
        final ProductInformation prodInfo = ProductInformation.getProductInformation();

        String resolvedProdName = prodInfo.getProductName();
        String resolvedProdVersion = prodInfo.getVersion();
        String resolvedProdUrl = prodInfo.getProductURL() != null ? prodInfo.getProductURL().toString() : "";
        String resolvedProjectName = StringUtils.trimToEmpty(projectName);

        final Set<String> percentileSet = new LinkedHashSet<>();

        // Collectors keyed by item name / signature
        final Map<String, ItemAccumulator> txMap = new LinkedHashMap<>();
        final Map<String, ItemAccumulator> actMap = new LinkedHashMap<>();
        final Map<String, ItemAccumulator> reqMap = new LinkedHashMap<>();
        final Map<String, ItemAccumulator> ctMap = new LinkedHashMap<>();
        final Map<String, ItemAccumulator> cvMap = new LinkedHashMap<>();
        final Map<String, ItemAccumulator> pltMap = new LinkedHashMap<>();
        final Map<String, ItemAccumulator> wvMap = new LinkedHashMap<>();

        final Map<String, ErrorAccumulator> errorsMap = new LinkedHashMap<>();
        final Map<String, EventAccumulator> eventsMap = new LinkedHashMap<>();
        final Map<String, NetworkAccumulator> codesMap = new LinkedHashMap<>();
        final Map<String, NetworkAccumulator> methodsMap = new LinkedHashMap<>();
        final Map<String, NetworkAccumulator> typesMap = new LinkedHashMap<>();
        final Map<String, NetworkAccumulator> hostsMap = new LinkedHashMap<>();
        final Map<String, LoadProfileAccumulator> loadProfileMap = new LinkedHashMap<>();
        final Map<String, PropertyAccumulator> propertiesMap = new LinkedHashMap<>();

        final List<Map<String, Object>> runsMeta = new ArrayList<>(runCount);

        for (int runIdx = 0; runIdx < runCount; runIdx++)
        {
            final RunInput input = runInputs.get(runIdx);
            final Document doc = input.getTestReportDoc();
            final Element root = doc.getDocumentElement();
            final Element generalEl = ModernReportGenerator.firstChildElement(root, "general");
            final Element summaryEl = ModernReportGenerator.firstChildElement(root, "summary");
            final Element configEl = ModernReportGenerator.firstChildElement(root, "configuration");
            final Element trCfgEl = ModernReportGenerator.firstChildElement(root, "testReportConfig");

            if (runIdx == runCount - 1)
            {
                final Element verEl = ModernReportGenerator.firstChildElement(configEl, "version");
                resolvedProdName = ModernReportGenerator.nonBlankOrDefault(
                    ModernReportGenerator.childText(verEl, "productName", null), resolvedProdName);
                resolvedProdVersion = ModernReportGenerator.nonBlankOrDefault(
                    ModernReportGenerator.childText(verEl, "version", null), resolvedProdVersion);
                resolvedProdUrl = ModernReportGenerator.nonBlankOrDefault(
                    ModernReportGenerator.childText(verEl, "productURL", null), resolvedProdUrl);
            }

            final String runProject = ModernReportGenerator.childText(configEl, "projectName", "").trim();
            if (resolvedProjectName.isEmpty() && !runProject.isEmpty())
            {
                resolvedProjectName = runProject;
            }

            final String startTimeStr = ModernReportGenerator.childText(generalEl, "startTime", "").trim();
            final String endTimeStr = ModernReportGenerator.childText(generalEl, "endTime", "").trim();
            final long durationSec = ModernReportGenerator.childLong(generalEl, "duration", 0L);
            final long startTimeMs = parseFlexibleTimestampMs(startTimeStr);
            final long endTimeMs = parseFlexibleTimestampMs(endTimeStr);
            final long effDurationSec = durationSec > 0 ? durationSec : Math.max(0L, (endTimeMs - startTimeMs) / 1000L);

            final List<String> comments = ModernReportGenerator.readStringList(
                ModernReportGenerator.firstChildElement(configEl, "comments"), "string");

            // Configured percentiles
            final Element pctCfgEl = ModernReportGenerator.firstChildElement(trCfgEl, "runtimePercentiles");
            for (final String p : ModernReportGenerator.readStringList(pctCfgEl, "string"))
            {
                final String norm = normalizePercentileKey(p);
                if (!norm.isEmpty())
                {
                    percentileSet.add(norm);
                }
            }

            // Summary tiers
            final Map<String, Map<String, Object>> summaryMap = new LinkedHashMap<>();
            collectSummaryTierFromTestReport(summaryEl, "transactions", "All Transactions", false, false, txMap, summaryMap, runIdx, runCount, percentileSet);
            collectSummaryTierFromTestReport(summaryEl, "actions", "All Actions", true, false, actMap, summaryMap, runIdx, runCount, percentileSet);
            collectSummaryTierFromTestReport(summaryEl, "requests", "All Requests", false, true, reqMap, summaryMap, runIdx, runCount, percentileSet);
            collectSummaryTierFromTestReport(summaryEl, "customTimers", "All Custom Timers", false, false, ctMap, summaryMap, runIdx, runCount, percentileSet);
            collectSummaryTierFromTestReport(summaryEl, "pageLoadTimings", "All Page Load Timings", false, false, pltMap, summaryMap, runIdx, runCount, percentileSet);

            // Timer item sections
            collectTimerSectionFromTestReport(
                ModernReportGenerator.firstChildElement(root, "transactions"), "transaction",
                false, false, txMap, runIdx, runCount, percentileSet);
            collectTimerSectionFromTestReport(
                ModernReportGenerator.firstChildElement(root, "actions"), "action",
                true, false, actMap, runIdx, runCount, percentileSet);
            collectTimerSectionFromTestReport(
                ModernReportGenerator.firstChildElement(root, "requests"), "request",
                false, true, reqMap, runIdx, runCount, percentileSet);
            collectTimerSectionFromTestReport(
                ModernReportGenerator.firstChildElement(root, "customTimers"), "customTimer",
                false, false, ctMap, runIdx, runCount, percentileSet);
            collectTimerSectionFromTestReport(
                ModernReportGenerator.firstChildElement(root, "pageLoadTimings"), "pageLoadTiming",
                false, false, pltMap, runIdx, runCount, percentileSet);
            collectCustomValuesFromTestReport(
                ModernReportGenerator.firstChildElement(root, "customValues"),
                cvMap, runIdx, runCount, percentileSet);
            collectWebVitalsFromTestReport(
                ModernReportGenerator.firstChildElement(root, "webVitalsList"),
                wvMap, runIdx, runCount);

            // Errors
            final Element errorsEl = ModernReportGenerator.firstChildElement(root, "errors");
            if (errorsEl != null)
            {
                for (final Element errEl : ModernReportGenerator.childElements(errorsEl, "error"))
                {
                    final long cnt = ModernReportGenerator.childLong(errEl, "count", 0L);
                    final String tc = ModernReportGenerator.childText(errEl, "testCaseName", "");
                    final String act = ModernReportGenerator.childText(errEl, "actionName", "");
                    final String req = ModernReportGenerator.childText(errEl, "requestName", "");
                    final String msg = ModernReportGenerator.childText(errEl, "message", "");
                    final String trace = ModernReportGenerator.childText(errEl, "trace", "");
                    final String sig = tc + "\0" + act + "\0" + req + "\0" + msg;
                    final ErrorAccumulator acc = errorsMap.computeIfAbsent(
                        sig, k -> new ErrorAccumulator(sig, tc, act, req, msg, trace, runCount));
                    acc.counts[runIdx] += cnt;
                    if (StringUtils.isBlank(acc.traceSample) && StringUtils.isNotBlank(trace))
                    {
                        acc.traceSample = trace;
                    }
                }
            }

            // Events
            final Element eventsEl = ModernReportGenerator.firstChildElement(root, "events");
            if (eventsEl != null)
            {
                for (final Element evEl : ModernReportGenerator.childElements(eventsEl, "event"))
                {
                    final String name = ModernReportGenerator.childText(evEl, "name", "");
                    final String tc = ModernReportGenerator.childText(evEl, "testCaseName", "");
                    final long cnt = ModernReportGenerator.childLong(evEl, "totalCount", 0L);
                    final List<String> msgs = new ArrayList<>();
                    final Element msgsEl = ModernReportGenerator.firstChildElement(evEl, "messages");
                    if (msgsEl != null)
                    {
                        for (final Element mEl : ModernReportGenerator.childElements(msgsEl, "message"))
                        {
                            final String info = ModernReportGenerator.childText(mEl, "info", "").trim();
                            if (!info.isEmpty() && msgs.size() < 5)
                            {
                                msgs.add(info);
                            }
                        }
                    }
                    final String sig = tc + "\0" + name;
                    final EventAccumulator acc = eventsMap.computeIfAbsent(
                        sig, k -> new EventAccumulator(sig, tc, name, runCount));
                    acc.counts[runIdx] += cnt;
                    for (final String m : msgs)
                    {
                        if (acc.sampleMessages.size() < 5)
                        {
                            acc.sampleMessages.add(m);
                        }
                    }
                }
            }

            // Network
            collectNetworkList(ModernReportGenerator.firstChildElement(root, "responseCodes"), "responseCode", "code", "statusText",
                               codesMap, runIdx, runCount);
            collectNetworkList(ModernReportGenerator.firstChildElement(root, "requestMethods"), "requestMethod", "method", null,
                               methodsMap, runIdx, runCount);
            collectNetworkList(ModernReportGenerator.firstChildElement(root, "contentTypes"), "contentType", "contentType", null,
                               typesMap, runIdx, runCount);
            collectNetworkList(ModernReportGenerator.firstChildElement(root, "hosts"), "host", "name", null,
                               hostsMap, runIdx, runCount);

            // Load Profile & Properties
            final Element lpEl = ModernReportGenerator.firstChildElement(configEl, "loadProfile");
            final Long configuredUsers = collectLoadProfile(lpEl, loadProfileMap, runIdx, runCount);

            String xtcLoadTestName = "";
            final Element propsEl = ModernReportGenerator.firstChildElement(configEl, "properties");
            if (propsEl != null)
            {
                for (final Element propEl : ModernReportGenerator.childElements(propsEl, "property"))
                {
                    final String pName = propEl.getAttribute("name");
                    final String pVal = propEl.getAttribute("value");
                    if (StringUtils.isNotEmpty(pName))
                    {
                        propertiesMap.computeIfAbsent(pName, k -> new PropertyAccumulator(pName, runCount)).values.set(runIdx, pVal);
                        if ("com.xceptance.xtc.loadtest.name".equals(pName) && StringUtils.isNotBlank(pVal))
                        {
                            xtcLoadTestName = pVal.trim();
                        }
                    }
                }
            }

            final String runDisplayName = StringUtils.isNotBlank(input.getReportName())
                ? input.getReportName().trim()
                : (!xtcLoadTestName.isEmpty() ? xtcLoadTestName : ("Run " + (runIdx + 1)));

            final Map<String, Object> runMeta = buildRunMeta(
                runIdx, runDisplayName, xtcLoadTestName,
                startTimeStr, endTimeStr, startTimeMs, effDurationSec,
                Long.valueOf(ModernReportGenerator.childLong(generalEl, "bytesSent", 0L)),
                Long.valueOf(ModernReportGenerator.childLong(generalEl, "bytesReceived", 0L)),
                Long.valueOf(ModernReportGenerator.childLong(generalEl, "hits", 0L)),
                comments,
                linkToReports ? computeRelativeReportHref(outputDir, input.getReportDir()) : null,
                reportHelper.parseScorecardSummary(input.getScorecardFile()),
                configuredUsers,
                summaryMap);
            runsMeta.add(runMeta);
        }

        final List<String> orderedPercentiles = sortPercentilesNumerically(percentileSet);

        final Map<String, Boolean> capabilities = new LinkedHashMap<>();
        capabilities.put("hasErrors", Boolean.TRUE);
        capabilities.put("hasEvents", Boolean.TRUE);
        capabilities.put("hasNetwork", Boolean.TRUE);
        capabilities.put("hasConfiguration", Boolean.TRUE);
        capabilities.put("hasCounts", Boolean.TRUE);
        capabilities.put("hasPercentiles", Boolean.valueOf(!orderedPercentiles.isEmpty()));
        capabilities.put("hasNetworkDetails", Boolean.TRUE);

        return assembleComparisonModel(
            reportKind, resolvedProdName, resolvedProdVersion, resolvedProdUrl, resolvedProjectName,
            runsMeta, orderedPercentiles, capabilities,
            txMap, actMap, reqMap, ctMap, cvMap, pltMap, wvMap,
            errorsMap, eventsMap, codesMap, methodsMap, typesMap, hostsMap,
            loadProfileMap, propertiesMap);
    }

    private ComparisonModel buildModelFromDiffXml(final Document diffDoc)
    {
        final Element root = diffDoc.getDocumentElement();
        final ProductInformation prodInfo = ProductInformation.getProductInformation();

        final Element tr1El = ModernReportGenerator.firstChildElement(root, "testReport1");
        final Element tr2El = ModernReportGenerator.firstChildElement(root, "testReport2");
        final Element summaryEl = ModernReportGenerator.firstChildElement(root, "summary");

        final String p1 = ModernReportGenerator.childText(tr1El, "projectName", "").trim();
        final String p2 = ModernReportGenerator.childText(tr2El, "projectName", "").trim();
        final String projectName = ModernReportGenerator.nonBlankOrDefault(p2, p1);

        final Set<String> percentileSet = new LinkedHashSet<>();

        final Map<String, ItemAccumulator> txMap = new LinkedHashMap<>();
        final Map<String, ItemAccumulator> actMap = new LinkedHashMap<>();
        final Map<String, ItemAccumulator> reqMap = new LinkedHashMap<>();
        final Map<String, ItemAccumulator> ctMap = new LinkedHashMap<>();
        final Map<String, ItemAccumulator> cvMap = new LinkedHashMap<>();
        final Map<String, ItemAccumulator> pltMap = new LinkedHashMap<>();
        final Map<String, ItemAccumulator> wvMap = new LinkedHashMap<>();

        final Map<String, LoadProfileAccumulator> loadProfileMap = new LinkedHashMap<>();
        final Map<String, PropertyAccumulator> propertiesMap = new LinkedHashMap<>();

        final List<Map<String, Object>> runsMeta = new ArrayList<>(2);
        final Element[] trEls = { tr1El, tr2El };

        for (int runIdx = 0; runIdx < 2; runIdx++)
        {
            final Element trEl = trEls[runIdx];
            final Element generalEl = ModernReportGenerator.firstChildElement(trEl, "general");
            final String rawName = ModernReportGenerator.childText(trEl, "name", "Run " + (runIdx + 1)).trim();
            final String startTimeStr = ModernReportGenerator.childText(generalEl, "startTime", "").trim();
            final String endTimeStr = ModernReportGenerator.childText(generalEl, "endTime", "").trim();
            final long durationSec = ModernReportGenerator.childLong(generalEl, "duration", 0L);
            final long startTimeMs = parseFlexibleTimestampMs(startTimeStr);
            final long endTimeMs = parseFlexibleTimestampMs(endTimeStr);
            final long effDurationSec = durationSec > 0 ? durationSec : Math.max(0L, (endTimeMs - startTimeMs) / 1000L);
            final List<String> comments = ModernReportGenerator.readStringList(
                ModernReportGenerator.firstChildElement(trEl, "comments"), "string");

            final Element lpEl = ModernReportGenerator.firstChildElement(trEl, "loadProfile");
            final Long configuredUsers = collectLoadProfile(lpEl, loadProfileMap, runIdx, 2);

            String xtcLoadTestName = "";
            final Element propsEl = ModernReportGenerator.firstChildElement(trEl, "properties");
            if (propsEl != null)
            {
                for (final Element propEl : ModernReportGenerator.childElements(propsEl, "property"))
                {
                    final String pName = propEl.getAttribute("name");
                    final String pVal = propEl.getAttribute("value");
                    if (StringUtils.isNotEmpty(pName))
                    {
                        propertiesMap.computeIfAbsent(pName, k -> new PropertyAccumulator(pName, 2)).values.set(runIdx, pVal);
                        if ("com.xceptance.xtc.loadtest.name".equals(pName) && StringUtils.isNotBlank(pVal))
                        {
                            xtcLoadTestName = pVal.trim();
                        }
                    }
                }
            }

            final Map<String, Map<String, Object>> summaryMap = new LinkedHashMap<>();
            collectDiffSummaryTier(summaryEl, "transactions", "All Transactions", false, false, txMap, summaryMap, runIdx, percentileSet);
            collectDiffSummaryTier(summaryEl, "actions", "All Actions", true, false, actMap, summaryMap, runIdx, percentileSet);
            collectDiffSummaryTier(summaryEl, "requests", "All Requests", false, true, reqMap, summaryMap, runIdx, percentileSet);
            collectDiffSummaryTier(summaryEl, "customTimers", "All Custom Timers", false, false, ctMap, summaryMap, runIdx, percentileSet);
            collectDiffSummaryTier(summaryEl, "pageLoadTimings", "All Page Load Timings", false, false, pltMap, summaryMap, runIdx, percentileSet);

            final Map<String, Object> runMeta = buildRunMeta(
                runIdx, rawName, xtcLoadTestName,
                startTimeStr, endTimeStr, startTimeMs, effDurationSec,
                generalEl != null ? Long.valueOf(ModernReportGenerator.childLong(generalEl, "bytesSent", 0L)) : null,
                generalEl != null ? Long.valueOf(ModernReportGenerator.childLong(generalEl, "bytesReceived", 0L)) : null,
                generalEl != null ? Long.valueOf(ModernReportGenerator.childLong(generalEl, "hits", 0L)) : null,
                comments,
                null,
                ModernReportGenerator.createAbsentScorecardSummary(),
                configuredUsers,
                summaryMap);
            runsMeta.add(runMeta);
        }

        collectDiffTimerSection(ModernReportGenerator.firstChildElement(root, "transactions"), "transaction", false, false, txMap, percentileSet);
        collectDiffTimerSection(ModernReportGenerator.firstChildElement(root, "actions"), "action", true, false, actMap, percentileSet);
        collectDiffTimerSection(ModernReportGenerator.firstChildElement(root, "requests"), "request", false, true, reqMap, percentileSet);
        collectDiffTimerSection(ModernReportGenerator.firstChildElement(root, "customTimers"), "customTimer", false, false, ctMap, percentileSet);
        collectDiffTimerSection(ModernReportGenerator.firstChildElement(root, "customValues"), "customValue", false, false, cvMap, percentileSet);
        collectDiffTimerSection(ModernReportGenerator.firstChildElement(root, "pageLoadTimings"), "pageLoadTiming", false, false, pltMap, percentileSet);
        collectDiffWebVitals(ModernReportGenerator.firstChildElement(root, "webVitalsList"), wvMap);

        final List<String> orderedPercentiles = sortPercentilesNumerically(percentileSet);

        final Map<String, Boolean> capabilities = new LinkedHashMap<>();
        capabilities.put("hasErrors", Boolean.FALSE);
        capabilities.put("hasEvents", Boolean.FALSE);
        capabilities.put("hasNetwork", Boolean.FALSE);
        capabilities.put("hasConfiguration", Boolean.valueOf(!loadProfileMap.isEmpty() || !propertiesMap.isEmpty()));
        capabilities.put("hasCounts", Boolean.TRUE);
        capabilities.put("hasPercentiles", Boolean.valueOf(!orderedPercentiles.isEmpty()));
        capabilities.put("hasNetworkDetails", Boolean.TRUE);

        return assembleComparisonModel(
            "diff", prodInfo.getProductName(), prodInfo.getVersion(),
            prodInfo.getProductURL() != null ? prodInfo.getProductURL().toString() : "",
            projectName,
            runsMeta, orderedPercentiles, capabilities,
            txMap, actMap, reqMap, ctMap, cvMap, pltMap, wvMap,
            Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            loadProfileMap, propertiesMap);
    }

    private ComparisonModel buildModelFromTrendXml(final Document trendDoc)
    {
        final Element root = trendDoc.getDocumentElement();
        final Element summaryEl = ModernReportGenerator.firstChildElement(root, "summary");
        final ProductInformation prodInfo = ProductInformation.getProductInformation();

        // Discover run count and run metadata from the first populated <trendValues> list
        final List<Element> templateTrendValues = findFirstTrendValues(root, summaryEl);
        final int runCount = templateTrendValues.size();

        final List<Map<String, Object>> runsMeta = new ArrayList<>(runCount);
        final Map<String, Integer> runIndexByName = new LinkedHashMap<>();
        final List<Map<String, Map<String, Object>>> runSummaries = new ArrayList<>(runCount);

        for (int i = 0; i < runCount; i++)
        {
            final Element tv = templateTrendValues.get(i);
            final String rName = ModernReportGenerator.childText(tv, "reportName", "Run " + (i + 1)).trim();
            final String rDate = ModernReportGenerator.childText(tv, "reportDate", "").trim();
            final String rawComment = ModernReportGenerator.childText(tv, "reportComments", "").trim();
            final List<String> comments = (rawComment.isEmpty() || "null".equalsIgnoreCase(rawComment))
                ? Collections.emptyList()
                : List.of(rawComment);
            final long startMs = parseFlexibleTimestampMs(rDate);
            final Map<String, Map<String, Object>> sMap = new LinkedHashMap<>();
            runSummaries.add(sMap);
            runIndexByName.put(rName, Integer.valueOf(i));

            runsMeta.add(buildRunMeta(
                i, rName, "",
                rDate, "", startMs, 0L,
                null, null, null,
                comments,
                null,
                ModernReportGenerator.createAbsentScorecardSummary(),
                null,
                sMap));
        }

        final Map<String, ItemAccumulator> txMap = new LinkedHashMap<>();
        final Map<String, ItemAccumulator> actMap = new LinkedHashMap<>();
        final Map<String, ItemAccumulator> reqMap = new LinkedHashMap<>();
        final Map<String, ItemAccumulator> ctMap = new LinkedHashMap<>();
        final Map<String, ItemAccumulator> pltMap = new LinkedHashMap<>();

        collectTrendSummary(summaryEl, "transactions", txMap, runSummaries, runIndexByName, runCount);
        collectTrendSummary(summaryEl, "actions", actMap, runSummaries, runIndexByName, runCount);
        collectTrendSummary(summaryEl, "requests", reqMap, runSummaries, runIndexByName, runCount);
        collectTrendSummary(summaryEl, "customTimers", ctMap, runSummaries, runIndexByName, runCount);
        collectTrendSummary(summaryEl, "pageLoadTimings", pltMap, runSummaries, runIndexByName, runCount);

        collectTrendSection(ModernReportGenerator.firstChildElement(root, "transactions"), "transaction", false, txMap, runIndexByName, runCount);
        collectTrendSection(ModernReportGenerator.firstChildElement(root, "actions"), "action", false, actMap, runIndexByName, runCount);
        collectTrendSection(ModernReportGenerator.firstChildElement(root, "requests"), "request", true, reqMap, runIndexByName, runCount);
        collectTrendSection(ModernReportGenerator.firstChildElement(root, "customTimers"), "customTimer", false, ctMap, runIndexByName, runCount);
        collectTrendSection(ModernReportGenerator.firstChildElement(root, "pageLoadTimings"), "pageLoadTiming", false, pltMap, runIndexByName, runCount);

        final Map<String, Boolean> capabilities = new LinkedHashMap<>();
        capabilities.put("hasErrors", Boolean.FALSE);
        capabilities.put("hasEvents", Boolean.FALSE);
        capabilities.put("hasNetwork", Boolean.FALSE);
        capabilities.put("hasConfiguration", Boolean.FALSE);
        capabilities.put("hasCounts", Boolean.FALSE);
        capabilities.put("hasPercentiles", Boolean.FALSE);
        capabilities.put("hasNetworkDetails", Boolean.FALSE);

        return assembleComparisonModel(
            "trend", prodInfo.getProductName(), prodInfo.getVersion(),
            prodInfo.getProductURL() != null ? prodInfo.getProductURL().toString() : "",
            "",
            runsMeta, Collections.emptyList(), capabilities,
            txMap, actMap, reqMap, ctMap, Collections.emptyMap(), pltMap, Collections.emptyMap(),
            Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), Collections.emptyMap());
    }

    // =========================================================================
    // TestReport Section Collectors
    // =========================================================================

    private void collectSummaryTierFromTestReport(
        final Element summaryEl, final String tierTag, final String fallbackAggName,
        final boolean includeApdex, final boolean isRequests,
        final Map<String, ItemAccumulator> itemMap, final Map<String, Map<String, Object>> summaryMap,
        final int runIdx, final int runCount, final Set<String> percentileSet)
    {
        final Element tierEl = ModernReportGenerator.firstChildElement(summaryEl, tierTag);
        if (tierEl == null)
        {
            summaryMap.put(tierTag, Collections.emptyMap());
            return;
        }
        final Map<String, Object> runMetrics = parseTestReportTimerMetrics(tierEl, includeApdex, isRequests, null, null);
        collectPercentileKeys(runMetrics.get("percentiles"), percentileSet);
        summaryMap.put(tierTag, runMetrics);

        final String name = ModernReportGenerator.nonBlankOrDefault(
            ModernReportGenerator.childText(tierEl, "name", fallbackAggName), fallbackAggName);
        final ItemAccumulator acc = itemMap.computeIfAbsent(name, k -> new ItemAccumulator(name, true, runCount));
        acc.runs.set(runIdx, runMetrics);
    }

    private void collectTimerSectionFromTestReport(
        final Element sectionEl, final String itemTag,
        final boolean includeApdex, final boolean isRequests,
        final Map<String, ItemAccumulator> itemMap, final int runIdx, final int runCount,
        final Set<String> percentileSet)
    {
        if (sectionEl == null)
        {
            return;
        }
        for (final Element el : ModernReportGenerator.childElements(sectionEl, itemTag))
        {
            final String name = ModernReportGenerator.childText(el, "name", "").trim();
            if (name.isEmpty())
            {
                continue;
            }
            final ItemAccumulator acc = itemMap.computeIfAbsent(name, k -> new ItemAccumulator(name, false, runCount));
            acc.labels.addAll(ModernReportGenerator.splitLabels(ModernReportGenerator.childText(el, "labels", null)));
            if (isRequests && acc.method.isEmpty())
            {
                acc.method = extractHttpMethodFromName(name);
            }
            final Map<String, Object> runMetrics = parseTestReportTimerMetrics(el, includeApdex, isRequests, acc.sampleUrls, null);
            collectPercentileKeys(runMetrics.get("percentiles"), percentileSet);
            acc.runs.set(runIdx, runMetrics);
        }
    }

    private void collectCustomValuesFromTestReport(
        final Element cvRootEl, final Map<String, ItemAccumulator> cvMap,
        final int runIdx, final int runCount, final Set<String> percentileSet)
    {
        if (cvRootEl == null)
        {
            return;
        }
        for (final Element el : ModernReportGenerator.childElements(cvRootEl, "customValue"))
        {
            final String name = ModernReportGenerator.childText(el, "name", "").trim();
            if (name.isEmpty())
            {
                continue;
            }
            final ItemAccumulator acc = cvMap.computeIfAbsent(name, k -> new ItemAccumulator(name, false, runCount));
            acc.labels.addAll(ModernReportGenerator.splitLabels(ModernReportGenerator.childText(el, "labels", null)));
            final String desc = ModernReportGenerator.childText(el, "description", "").trim();
            if (acc.description.isEmpty() && !desc.isEmpty())
            {
                acc.description = desc;
            }
            final Map<String, Object> m = parseTestReportTimerMetrics(el, false, false, null, null);
            collectPercentileKeys(m.get("percentiles"), percentileSet);
            acc.runs.set(runIdx, m);
        }
    }

    private void collectWebVitalsFromTestReport(
        final Element wvRootEl, final Map<String, ItemAccumulator> wvMap, final int runIdx, final int runCount)
    {
        if (wvRootEl == null)
        {
            return;
        }
        for (final Element wvEl : ModernReportGenerator.childElements(wvRootEl, "webVitals"))
        {
            final String name = ModernReportGenerator.childText(wvEl, "name", "").trim();
            if (name.isEmpty())
            {
                continue;
            }
            final Map<String, Object> entry = new LinkedHashMap<>();
            for (final String metric : WEB_VITAL_METRICS)
            {
                final Element mEl = ModernReportGenerator.firstChildElement(wvEl, metric);
                if (mEl != null)
                {
                    final Double score = parseNullableDouble(ModernReportGenerator.childText(mEl, "score", null));
                    if (score != null)
                    {
                        entry.put(metric, score);
                    }
                }
            }
            if (!entry.isEmpty())
            {
                wvMap.computeIfAbsent(name, k -> new ItemAccumulator(name, false, runCount)).runs.set(runIdx, entry);
            }
        }
    }

    private Map<String, Object> parseTestReportTimerMetrics(
        final Element el, final boolean includeApdex, final boolean isRequests,
        final Set<String> sampleUrlsOut, final Void unused)
    {
        final Map<String, Object> m = new LinkedHashMap<>();
        m.put("count", Long.valueOf(ModernReportGenerator.childLong(el, "count", 0L)));
        m.put("rate", Double.valueOf(ModernReportGenerator.childDouble(el, "countPerSecond", 0.0)));
        m.put("cpm", Double.valueOf(ModernReportGenerator.childDouble(el, "countPerMinute", 0.0)));
        m.put("cph", Double.valueOf(ModernReportGenerator.childDouble(el, "countPerHour", 0.0)));
        m.put("cpd", Double.valueOf(ModernReportGenerator.childDouble(el, "countPerDay", 0.0)));
        m.put("errors", Long.valueOf(ModernReportGenerator.childLong(el, "errors", 0L)));
        m.put("errPct", Double.valueOf(ModernReportGenerator.childDouble(el, "errorPercentage", 0.0)));

        final Element evEl = ModernReportGenerator.firstChildElement(el, "events");
        if (evEl != null)
        {
            m.put("events", Long.valueOf(ModernReportGenerator.parseLongSafe(evEl.getTextContent(), 0L)));
        }

        m.put("mean", Double.valueOf(ModernReportGenerator.childDouble(el, "mean", 0.0)));
        m.put("min", Double.valueOf(ModernReportGenerator.childDouble(el, "min", 0.0)));
        m.put("max", Double.valueOf(ModernReportGenerator.childDouble(el, "max", 0.0)));
        m.put("median", Double.valueOf(ModernReportGenerator.childDouble(el, "median", 0.0)));

        final Double stdDev = parseNullableDouble(ModernReportGenerator.childText(el, "standardDeviation", null));
        m.put("dev", stdDev != null ? stdDev : Double.valueOf(ModernReportGenerator.childDouble(el, "deviation", 0.0)));

        final Map<String, Double> pcts = ModernReportGenerator.parsePercentilesMap(
            ModernReportGenerator.firstChildElement(el, "percentiles"));
        if (!pcts.isEmpty())
        {
            m.put("percentiles", pcts);
        }

        if (includeApdex)
        {
            final Element apdexEl = ModernReportGenerator.firstChildElement(el, "apdex");
            if (apdexEl != null)
            {
                final Double val = parseNullableDouble(ModernReportGenerator.childText(apdexEl, "value", null));
                if (val != null)
                {
                    m.put("apdex", val);
                }
            }
        }

        if (isRequests)
        {
            final Element urlsEl = ModernReportGenerator.firstChildElement(el, "urls");
            if (urlsEl != null)
            {
                final Double distUrls = parseNullableDouble(ModernReportGenerator.childText(urlsEl, "total", null));
                if (distUrls != null)
                {
                    m.put("distinctUrls", Long.valueOf(distUrls.longValue()));
                }
                if (sampleUrlsOut != null)
                {
                    for (final String url : ModernReportGenerator.readStringList(
                        ModernReportGenerator.firstChildElement(urlsEl, "list"), "string"))
                    {
                        if (StringUtils.isNotBlank(url) && sampleUrlsOut.size() < 3)
                        {
                            sampleUrlsOut.add(url.trim());
                        }
                    }
                }
            }
            putByteStatsFromElement(m, ModernReportGenerator.firstChildElement(el, "bytesSent"), "sent");
            putByteStatsFromElement(m, ModernReportGenerator.firstChildElement(el, "bytesReceived"), "recv");
            putSubMeanFromElement(m, ModernReportGenerator.firstChildElement(el, "dnsTime"), "dns");
            putSubMeanFromElement(m, ModernReportGenerator.firstChildElement(el, "connectTime"), "conn");
            putSubMeanFromElement(m, ModernReportGenerator.firstChildElement(el, "sendTime"), "send");
            putSubMeanFromElement(m, ModernReportGenerator.firstChildElement(el, "serverBusyTime"), "busy");
            putSubMeanFromElement(m, ModernReportGenerator.firstChildElement(el, "receiveTime"), "recv");
            putSubMeanFromElement(m, ModernReportGenerator.firstChildElement(el, "timeToFirstBytes"), "ttfb");
            putSubMeanFromElement(m, ModernReportGenerator.firstChildElement(el, "timeToLastBytes"), "ttlb");
        }
        return m;
    }

    private static void putByteStatsFromElement(final Map<String, Object> target, final Element el, final String prefix)
    {
        if (el == null)
        {
            return;
        }
        target.put(prefix + "Mean", Double.valueOf(ModernReportGenerator.childDouble(el, "mean", 0.0)));
        target.put(prefix + "Rate", Double.valueOf(ModernReportGenerator.childDouble(el, "countPerSecond", 0.0)));
        target.put(prefix + "Total", Long.valueOf(ModernReportGenerator.childLong(el, "totalCount", 0L)));
    }

    private static void putSubMeanFromElement(final Map<String, Object> target, final Element el, final String key)
    {
        if (el != null)
        {
            target.put(key, Double.valueOf(ModernReportGenerator.childDouble(el, "mean", 0.0)));
        }
    }

    // =========================================================================
    // Diff XML Collectors
    // =========================================================================

    private void collectDiffSummaryTier(
        final Element summaryEl, final String tierTag, final String fallbackAggName,
        final boolean includeApdex, final boolean isRequests,
        final Map<String, ItemAccumulator> itemMap, final Map<String, Map<String, Object>> summaryMap,
        final int runIdx, final Set<String> percentileSet)
    {
        final Element tierEl = ModernReportGenerator.firstChildElement(summaryEl, tierTag);
        if (tierEl == null)
        {
            summaryMap.put(tierTag, Collections.emptyMap());
            return;
        }
        final Map<String, Object> runMetrics = parseDiffTimerMetrics(tierEl, runIdx, includeApdex, isRequests, null);
        if (runMetrics == null)
        {
            summaryMap.put(tierTag, Collections.emptyMap());
            return;
        }
        collectPercentileKeys(runMetrics.get("percentiles"), percentileSet);
        summaryMap.put(tierTag, runMetrics);

        final String name = ModernReportGenerator.nonBlankOrDefault(
            ModernReportGenerator.childText(tierEl, "name", fallbackAggName), fallbackAggName);
        final ItemAccumulator acc = itemMap.computeIfAbsent(name, k -> new ItemAccumulator(name, true, 2));
        acc.runs.set(runIdx, runMetrics);
    }

    private void collectDiffTimerSection(
        final Element sectionEl, final String itemTag,
        final boolean includeApdex, final boolean isRequests,
        final Map<String, ItemAccumulator> itemMap, final Set<String> percentileSet)
    {
        if (sectionEl == null)
        {
            return;
        }
        for (final Element el : ModernReportGenerator.childElements(sectionEl, itemTag))
        {
            final String name = ModernReportGenerator.childText(el, "name", "").trim();
            if (name.isEmpty())
            {
                continue;
            }
            final ItemAccumulator acc = itemMap.computeIfAbsent(name, k -> new ItemAccumulator(name, false, 2));
            acc.labels.addAll(ModernReportGenerator.splitLabels(ModernReportGenerator.childText(el, "labels", null)));
            if ("customValue".equals(itemTag))
            {
                final String desc = ModernReportGenerator.childText(el, "description", "").trim();
                if (acc.description.isEmpty() && !desc.isEmpty())
                {
                    acc.description = desc;
                }
            }
            if (isRequests && acc.method.isEmpty())
            {
                acc.method = extractHttpMethodFromName(name);
            }

            for (int runIdx = 0; runIdx < 2; runIdx++)
            {
                final Map<String, Object> m = parseDiffTimerMetrics(el, runIdx, includeApdex, isRequests, acc.sampleUrls);
                if (m != null)
                {
                    collectPercentileKeys(m.get("percentiles"), percentileSet);
                    acc.runs.set(runIdx, m);
                }
            }
        }
    }

    private void collectDiffWebVitals(final Element wvRootEl, final Map<String, ItemAccumulator> wvMap)
    {
        if (wvRootEl == null)
        {
            return;
        }
        for (final Element wvEl : ModernReportGenerator.childElements(wvRootEl, "webVitals"))
        {
            final String name = ModernReportGenerator.childText(wvEl, "name", "").trim();
            if (name.isEmpty())
            {
                continue;
            }
            final ItemAccumulator acc = wvMap.computeIfAbsent(name, k -> new ItemAccumulator(name, false, 2));
            for (int runIdx = 0; runIdx < 2; runIdx++)
            {
                final Map<String, Object> entry = new LinkedHashMap<>();
                for (final String metric : WEB_VITAL_METRICS)
                {
                    final Element mEl = ModernReportGenerator.firstChildElement(wvEl, metric);
                    final Double val = readDiffLeaf(mEl, "score", runIdx);
                    if (val != null)
                    {
                        entry.put(metric, val);
                    }
                }
                if (!entry.isEmpty())
                {
                    acc.runs.set(runIdx, entry);
                }
            }
        }
    }

    private Map<String, Object> parseDiffTimerMetrics(
        final Element el, final int runIdx, final boolean includeApdex, final boolean isRequests, final Set<String> sampleUrlsOut)
    {
        final Double countVal = readDiffLeaf(el, "count", runIdx);
        if (countVal == null)
        {
            return null;
        }

        final Map<String, Object> m = new LinkedHashMap<>();
        m.put("count", Long.valueOf(Math.round(countVal.doubleValue())));
        putIfNonNull(m, "rate", readDiffLeaf(el, "countPerSecond", runIdx));
        putIfNonNull(m, "cpm", readDiffLeaf(el, "countPerMinute", runIdx));
        putIfNonNull(m, "cph", readDiffLeaf(el, "countPerHour", runIdx));
        putIfNonNull(m, "cpd", readDiffLeaf(el, "countPerDay", runIdx));

        final Double errorsVal = readDiffLeaf(el, "errors", runIdx);
        if (errorsVal != null)
        {
            m.put("errors", Long.valueOf(Math.round(errorsVal.doubleValue())));
        }
        putIfNonNull(m, "errPct", readDiffLeaf(el, "errorPercentage", runIdx));

        final Double eventsVal = readDiffLeaf(el, "events", runIdx);
        if (eventsVal != null)
        {
            m.put("events", Long.valueOf(Math.round(eventsVal.doubleValue())));
        }

        putIfNonNull(m, "mean", readDiffLeaf(el, "mean", runIdx));
        putIfNonNull(m, "min", readDiffLeaf(el, "min", runIdx));
        putIfNonNull(m, "max", readDiffLeaf(el, "max", runIdx));
        putIfNonNull(m, "median", readDiffLeaf(el, "median", runIdx));

        final Double devVal = readDiffLeaf(el, "standardDeviation", runIdx);
        putIfNonNull(m, "dev", devVal != null ? devVal : readDiffLeaf(el, "deviation", runIdx));

        // Percentiles
        final Element pctsEl = ModernReportGenerator.firstChildElement(el, "percentiles");
        if (pctsEl != null)
        {
            final Map<String, Double> pcts = new LinkedHashMap<>();
            for (final Element pEl : ModernReportGenerator.childElements(pctsEl))
            {
                final Double v = readDiffLeafValue(pEl, runIdx);
                if (v != null && pEl.getTagName() != null)
                {
                    pcts.put(pEl.getTagName(), v);
                }
            }
            if (!pcts.isEmpty())
            {
                m.put("percentiles", pcts);
            }
        }

        if (includeApdex)
        {
            final Double apdex = readDiffLeaf(ModernReportGenerator.firstChildElement(el, "apdex"), "value", runIdx);
            if (apdex != null)
            {
                m.put("apdex", apdex);
            }
        }

        if (isRequests)
        {
            final Element urlsEl = ModernReportGenerator.firstChildElement(el, "urls");
            if (urlsEl != null)
            {
                final Double distUrls = readDiffLeaf(urlsEl, "total", runIdx);
                if (distUrls != null)
                {
                    m.put("distinctUrls", Long.valueOf(Math.round(distUrls.doubleValue())));
                }
                if (sampleUrlsOut != null)
                {
                    for (final String url : ModernReportGenerator.readStringList(
                        ModernReportGenerator.firstChildElement(urlsEl, "list"), "string"))
                    {
                        if (StringUtils.isNotBlank(url) && sampleUrlsOut.size() < 3)
                        {
                            sampleUrlsOut.add(url.trim());
                        }
                    }
                }
            }
            putDiffByteStats(m, ModernReportGenerator.firstChildElement(el, "bytesSent"), "sent", runIdx);
            putDiffByteStats(m, ModernReportGenerator.firstChildElement(el, "bytesReceived"), "recv", runIdx);
            putDiffSubMean(m, ModernReportGenerator.firstChildElement(el, "dnsTime"), "dns", runIdx);
            putDiffSubMean(m, ModernReportGenerator.firstChildElement(el, "connectTime"), "conn", runIdx);
            putDiffSubMean(m, ModernReportGenerator.firstChildElement(el, "sendTime"), "send", runIdx);
            putDiffSubMean(m, ModernReportGenerator.firstChildElement(el, "serverBusyTime"), "busy", runIdx);
            putDiffSubMean(m, ModernReportGenerator.firstChildElement(el, "receiveTime"), "recv", runIdx);
            putDiffSubMean(m, ModernReportGenerator.firstChildElement(el, "timeToFirstBytes"), "ttfb", runIdx);
            putDiffSubMean(m, ModernReportGenerator.firstChildElement(el, "timeToLastBytes"), "ttlb", runIdx);
        }

        return m;
    }

    private static void putDiffByteStats(final Map<String, Object> target, final Element el, final String prefix, final int runIdx)
    {
        if (el == null)
        {
            return;
        }
        putIfNonNull(target, prefix + "Mean", readDiffLeaf(el, "mean", runIdx));
        putIfNonNull(target, prefix + "Rate", readDiffLeaf(el, "countPerSecond", runIdx));
        final Double tot = readDiffLeaf(el, "totalCount", runIdx);
        if (tot != null)
        {
            target.put(prefix + "Total", Long.valueOf(Math.round(tot.doubleValue())));
        }
    }

    private static void putDiffSubMean(final Map<String, Object> target, final Element el, final String key, final int runIdx)
    {
        if (el != null)
        {
            putIfNonNull(target, key, readDiffLeaf(el, "mean", runIdx));
        }
    }

    private static Double readDiffLeaf(final Element parent, final String tagName, final int runIdx)
    {
        if (parent == null)
        {
            return null;
        }
        return readDiffLeafValue(ModernReportGenerator.firstChildElement(parent, tagName), runIdx);
    }

    private static Double readDiffLeafValue(final Element leafEl, final int runIdx)
    {
        if (leafEl == null)
        {
            return null;
        }
        final String sideTag = runIdx == 0 ? "oldValue" : "newValue";
        return parseNullableDouble(ModernReportGenerator.childText(leafEl, sideTag, null));
    }

    // =========================================================================
    // Trend XML Collectors
    // =========================================================================

    private static List<Element> findFirstTrendValues(final Element root, final Element summaryEl)
    {
        if (summaryEl != null)
        {
            for (final Element child : ModernReportGenerator.childElements(summaryEl))
            {
                final Element tvs = ModernReportGenerator.firstChildElement(child, "trendValues");
                if (tvs != null)
                {
                    final List<Element> list = ModernReportGenerator.childElements(tvs, "trendValue");
                    if (!list.isEmpty())
                    {
                        return list;
                    }
                }
            }
        }
        for (final String secTag : List.of("transactions", "actions", "requests", "pageLoadTimings", "customTimers"))
        {
            final Element secEl = ModernReportGenerator.firstChildElement(root, secTag);
            if (secEl != null)
            {
                for (final Element itemEl : ModernReportGenerator.childElements(secEl))
                {
                    final Element tvs = ModernReportGenerator.firstChildElement(itemEl, "trendValues");
                    if (tvs != null)
                    {
                        final List<Element> list = ModernReportGenerator.childElements(tvs, "trendValue");
                        if (!list.isEmpty())
                        {
                            return list;
                        }
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private void collectTrendSummary(
        final Element summaryEl, final String tierTag,
        final Map<String, ItemAccumulator> itemMap,
        final List<Map<String, Map<String, Object>>> runSummaries,
        final Map<String, Integer> runIndexByName, final int runCount)
    {
        final Element tierEl = ModernReportGenerator.firstChildElement(summaryEl, tierTag);
        if (tierEl == null)
        {
            for (int i = 0; i < runCount; i++)
            {
                runSummaries.get(i).put(tierTag, Collections.emptyMap());
            }
            return;
        }
        final String aggName = ModernReportGenerator.nonBlankOrDefault(
            ModernReportGenerator.childText(tierEl, "name", null), "All " + StringUtils.capitalize(tierTag));
        final ItemAccumulator acc = itemMap.computeIfAbsent(aggName, k -> new ItemAccumulator(aggName, true, runCount));
        populateTrendAccumulator(tierEl, acc, runIndexByName, runCount);
        for (int i = 0; i < runCount; i++)
        {
            final Map<String, Object> m = acc.runs.get(i);
            runSummaries.get(i).put(tierTag, m != null ? m : Collections.emptyMap());
        }
    }

    private void collectTrendSection(
        final Element sectionEl, final String itemTag, final boolean isRequests,
        final Map<String, ItemAccumulator> itemMap, final Map<String, Integer> runIndexByName, final int runCount)
    {
        if (sectionEl == null)
        {
            return;
        }
        for (final Element itemEl : ModernReportGenerator.childElements(sectionEl, itemTag))
        {
            final String name = ModernReportGenerator.childText(itemEl, "name", "").trim();
            if (name.isEmpty())
            {
                continue;
            }
            final ItemAccumulator acc = itemMap.computeIfAbsent(name, k -> new ItemAccumulator(name, false, runCount));
            if (isRequests && acc.method.isEmpty())
            {
                acc.method = extractHttpMethodFromName(name);
            }
            populateTrendAccumulator(itemEl, acc, runIndexByName, runCount);
        }
    }

    private static void populateTrendAccumulator(
        final Element itemEl, final ItemAccumulator acc,
        final Map<String, Integer> runIndexByName, final int runCount)
    {
        final Element tvsEl = ModernReportGenerator.firstChildElement(itemEl, "trendValues");
        if (tvsEl == null)
        {
            return;
        }
        final List<Element> tvs = ModernReportGenerator.childElements(tvsEl, "trendValue");
        for (int idxFallback = 0; idxFallback < tvs.size(); idxFallback++)
        {
            final Element tv = tvs.get(idxFallback);
            final String rName = ModernReportGenerator.childText(tv, "reportName", "").trim();
            final Integer mappedIdx = runIndexByName.get(rName);
            final int runIdx = mappedIdx != null ? mappedIdx.intValue() : idxFallback;
            if (runIdx < 0 || runIdx >= runCount)
            {
                continue;
            }

            final Double mean = parseNullableDouble(ModernReportGenerator.childText(tv, "mean", null));
            final Double min = parseNullableDouble(ModernReportGenerator.childText(tv, "min", null));
            final Double max = parseNullableDouble(ModernReportGenerator.childText(tv, "max", null));
            final Double median = parseNullableDouble(ModernReportGenerator.childText(tv, "median", null));
            final Double errors = parseNullableDouble(ModernReportGenerator.childText(tv, "errors", null));
            final Double throughput = parseNullableDouble(ModernReportGenerator.childText(tv, "throughput", null));

            if (mean == null && min == null && max == null && median == null && errors == null && throughput == null)
            {
                acc.runs.set(runIdx, null);
                continue;
            }

            final Map<String, Object> m = new LinkedHashMap<>();
            if (throughput != null)
            {
                m.put("rate", throughput);
                m.put("cpm", Double.valueOf(throughput.doubleValue() * 60.0));
                m.put("cph", Double.valueOf(throughput.doubleValue() * 3600.0));
                m.put("cpd", Double.valueOf(throughput.doubleValue() * 86400.0));
            }
            if (errors != null)
            {
                m.put("errors", Long.valueOf(Math.round(errors.doubleValue())));
            }
            putIfNonNull(m, "mean", mean);
            putIfNonNull(m, "min", min);
            putIfNonNull(m, "max", max);
            putIfNonNull(m, "median", median);
            acc.runs.set(runIdx, m);
        }
    }

    // =========================================================================
    // Shared Accumulator Helpers (Network / Config / Metadata)
    // =========================================================================

    private static void collectNetworkList(
        final Element rootListEl, final String itemTag, final String keyTag, final String labelTag,
        final Map<String, NetworkAccumulator> accMap, final int runIdx, final int runCount)
    {
        if (rootListEl == null)
        {
            return;
        }
        for (final Element el : ModernReportGenerator.childElements(rootListEl, itemTag))
        {
            String key = ModernReportGenerator.childText(el, keyTag, "").trim();
            if ("contentType".equals(itemTag) && key.isEmpty())
            {
                key = "(empty / unspecified)";
            }
            if (key.isEmpty())
            {
                continue;
            }
            final String label = labelTag != null ? ModernReportGenerator.childText(el, labelTag, "").trim() : "";
            final long cnt = ModernReportGenerator.childLong(el, "count", 0L);
            final String accKey = key;
            final NetworkAccumulator acc = accMap.computeIfAbsent(accKey, k -> new NetworkAccumulator(accKey, label, runCount));
            if (acc.label.isEmpty() && !label.isEmpty())
            {
                acc.label = label;
            }
            acc.counts[runIdx] += cnt;
        }
    }

    private static Long collectLoadProfile(
        final Element lpEl, final Map<String, LoadProfileAccumulator> lpMap, final int runIdx, final int runCount)
    {
        if (lpEl == null)
        {
            return null;
        }
        final List<Element> testCases = ModernReportGenerator.childElements(lpEl, "testCase");
        if (testCases.isEmpty())
        {
            return null;
        }

        long totalUsers = 0L;
        for (final Element tcEl : testCases)
        {
            final String userName = ModernReportGenerator.childText(tcEl, "userName", "").trim();
            final String className = ModernReportGenerator.childText(tcEl, "testCaseClassName", "").trim();
            final String name = !userName.isEmpty() ? userName : className;
            if (name.isEmpty())
            {
                continue;
            }
            final String usersStr = ModernReportGenerator.childText(tcEl, "numberOfUsers", "0").trim();
            final String itersStr = ModernReportGenerator.childText(tcEl, "numberOfIterations", "0").trim();
            final String arrivalStr = ModernReportGenerator.childText(tcEl, "arrivalRate", "0").trim();
            final long rampUp = ModernReportGenerator.childLong(tcEl, "rampUpPeriod", 0L);
            final long measurement = ModernReportGenerator.childLong(tcEl, "measurementPeriod", 0L);
            final long thinkTime = ModernReportGenerator.childLong(tcEl, "actionThinkTime", 0L);
            final long thinkDev = ModernReportGenerator.childLong(tcEl, "actionThinkTimeDeviation", 0L);

            totalUsers += extractUsersCount(usersStr);

            final Map<String, Object> tcMap = new LinkedHashMap<>();
            tcMap.put("users", usersStr);
            tcMap.put("iterations", itersStr);
            tcMap.put("arrivalRate", arrivalStr);
            tcMap.put("rampUp", Long.valueOf(rampUp));
            tcMap.put("measurement", Long.valueOf(measurement));
            tcMap.put("thinkTime", Long.valueOf(thinkTime));
            tcMap.put("thinkTimeDev", Long.valueOf(thinkDev));

            final String key = name + "|" + className;
            lpMap.computeIfAbsent(key, k -> new LoadProfileAccumulator(name, className, runCount)).runs.set(runIdx, tcMap);
        }
        return Long.valueOf(totalUsers);
    }

    private static Map<String, Object> buildRunMeta(
        final int idx, final String name, final String testName,
        final String startTime, final String endTime, final long startTimeMs, final long durationSec,
        final Long bytesSent, final Long bytesReceived, final Long hits,
        final List<String> comments, final String reportHref, final Map<String, Object> scorecardSummary,
        final Long configuredUsers, final Map<String, Map<String, Object>> summaryMap)
    {
        final String headline = ModernReportGenerator.extractCommentHeadline(comments);
        final String plainComment = extractPlainCommentBody(comments, headline);

        final Map<String, Object> run = new LinkedHashMap<>();
        run.put("idx", Integer.valueOf(idx));
        run.put("id", name);
        run.put("name", name);
        run.put("testName", testName != null ? testName : "");
        run.put("startTime", startTime);
        run.put("endTime", endTime);
        run.put("startTimeMs", Long.valueOf(startTimeMs));
        run.put("durationSec", Long.valueOf(durationSec));
        run.put("durationDisplay", durationSec > 0 ? ModernReportGenerator.formatDurationHms(durationSec) : "");
        run.put("bytesSent", bytesSent);
        run.put("bytesReceived", bytesReceived);
        run.put("hits", hits);
        run.put("comments", comments);
        run.put("commentHeadline", headline);
        run.put("commentPlain", plainComment);
        run.put("reportHref", reportHref);
        run.put("scorecard", scorecardSummary);
        run.put("configuredUsers", configuredUsers);
        run.put("summary", summaryMap);
        return run;
    }

    // =========================================================================
    // Assemble ComparisonModel & Serialize Bundles
    // =========================================================================

    private ComparisonModel assembleComparisonModel(
        final String reportKind, final String productName, final String productVersion,
        final String productUrl, final String projectName,
        final List<Map<String, Object>> runsMeta, final List<String> runtimePercentiles,
        final Map<String, Boolean> capabilities,
        final Map<String, ItemAccumulator> txMap, final Map<String, ItemAccumulator> actMap,
        final Map<String, ItemAccumulator> reqMap, final Map<String, ItemAccumulator> ctMap,
        final Map<String, ItemAccumulator> cvMap, final Map<String, ItemAccumulator> pltMap,
        final Map<String, ItemAccumulator> wvMap,
        final Map<String, ErrorAccumulator> errorsMap, final Map<String, EventAccumulator> eventsMap,
        final Map<String, NetworkAccumulator> codesMap, final Map<String, NetworkAccumulator> methodsMap,
        final Map<String, NetworkAccumulator> typesMap, final Map<String, NetworkAccumulator> hostsMap,
        final Map<String, LoadProfileAccumulator> loadProfileMap, final Map<String, PropertyAccumulator> propertiesMap)
    {
        final ComparisonModel model = new ComparisonModel();
        model.reportKind = "trend".equalsIgnoreCase(reportKind) ? "trend" : "diff";
        model.productName = productName;
        model.productVersion = productVersion;
        model.productUrl = productUrl;
        model.projectName = projectName;
        model.runs = runsMeta;
        model.runtimePercentiles = runtimePercentiles;
        model.capabilities = capabilities;

        model.transactions = finalizeItems(txMap.values(), false);
        model.actions = finalizeItems(actMap.values(), false);
        model.requests = finalizeItems(reqMap.values(), true);
        model.customTimers = finalizeItems(ctMap.values(), false);
        model.customValues = finalizeItems(cvMap.values(), false);
        model.pageLoadTimings = finalizeItems(pltMap.values(), false);
        model.webVitals = finalizeItems(wvMap.values(), false);

        model.errors = finalizeErrors(errorsMap.values());
        model.events = finalizeEvents(eventsMap.values());

        model.network = new LinkedHashMap<>();
        model.network.put("responseCodes", finalizeNetwork(codesMap.values(), "code", "statusText", true));
        model.network.put("methods", finalizeNetwork(methodsMap.values(), "method", null, false));
        model.network.put("contentTypes", finalizeNetwork(typesMap.values(), "contentType", null, false));
        model.network.put("hosts", finalizeNetwork(hostsMap.values(), "name", null, false));

        model.configuration = new LinkedHashMap<>();
        model.configuration.put("loadProfiles", finalizeLoadProfiles(loadProfileMap.values()));
        model.configuration.put("properties", finalizeProperties(propertiesMap.values()));

        model.counts = new LinkedHashMap<>();
        model.counts.put("transactions", Integer.valueOf(countNonAggregate(model.transactions)));
        model.counts.put("actions", Integer.valueOf(countNonAggregate(model.actions)));
        model.counts.put("requests", Integer.valueOf(countNonAggregate(model.requests)));
        model.counts.put("customTimers", Integer.valueOf(countNonAggregate(model.customTimers)));
        model.counts.put("customValues", Integer.valueOf(model.customValues.size()));
        model.counts.put("pageLoadTimings", Integer.valueOf(countNonAggregate(model.pageLoadTimings)));
        model.counts.put("webVitals", Integer.valueOf(model.webVitals.size()));
        model.counts.put("errors", Integer.valueOf(model.errors.size()));
        model.counts.put("events", Integer.valueOf(model.events.size()));
        model.counts.put("properties", Integer.valueOf(propertiesMap.size()));
        model.counts.put("hosts", Integer.valueOf(hostsMap.size()));
        model.counts.put("responseCodes", Integer.valueOf(codesMap.size()));

        return model;
    }

    private void writeBundlesFromModel(final ComparisonModel model, final File outputDir, final boolean linkToReports) throws IOException
    {
        final File jsDir = new File(outputDir, "js");
        FileUtils.forceMkdir(jsDir);

        final int runCount = model.runs != null ? model.runs.size() : 0;
        final Map<String, Object> metaBundle = new LinkedHashMap<>();
        metaBundle.put("reportType", "comparison");
        metaBundle.put("reportKind", model.reportKind);
        metaBundle.put("productName", model.productName);
        metaBundle.put("productVersion", model.productVersion);
        metaBundle.put("productUrl", model.productUrl);
        metaBundle.put("projectName", model.projectName);
        metaBundle.put("defaultBaselineIdx", Integer.valueOf(0));
        metaBundle.put("defaultTargetIdx", Integer.valueOf(Math.max(0, runCount - 1)));
        metaBundle.put("runs", linkToReports ? model.runs : stripReportHrefs(model.runs));
        metaBundle.put("counts", model.counts);
        metaBundle.put("capabilities", model.capabilities);

        final Map<String, Object> testReportCfg = new LinkedHashMap<>(1);
        testReportCfg.put("runtimePercentiles", model.runtimePercentiles != null ? model.runtimePercentiles : Collections.emptyList());
        metaBundle.put("testReportConfig", testReportCfg);

        final Map<String, Object> overviewBundle = new LinkedHashMap<>();
        overviewBundle.put("transactions", toCompactOverviewList(model.transactions));
        overviewBundle.put("actions", toCompactOverviewList(model.actions));
        overviewBundle.put("requests", toCompactOverviewList(model.requests));

        writeCompactJsGlobal(new File(jsDir, "report-meta.js"), "XLT_META", metaBundle);
        writeCompactJsGlobal(new File(jsDir, "overview-data.js"), "XLT_OVERVIEW", overviewBundle);
        writeCompactJsGlobal(new File(jsDir, "transactions-data.js"), "XLT_TRANSACTIONS", Map.of("items", model.transactions));
        writeCompactJsGlobal(new File(jsDir, "actions-data.js"), "XLT_ACTIONS", Map.of("items", model.actions));
        writeCompactJsGlobal(new File(jsDir, "requests-data.js"), "XLT_REQUESTS", Map.of("items", model.requests));
        writeCompactJsGlobal(new File(jsDir, "custom-timers-data.js"), "XLT_CUSTOM_TIMERS", Map.of("items", model.customTimers));
        writeCompactJsGlobal(new File(jsDir, "custom-values-data.js"), "XLT_CUSTOM_VALUES", Map.of("items", model.customValues));
        writeCompactJsGlobal(new File(jsDir, "page-load-timings-data.js"), "XLT_PAGE_LOAD_TIMINGS", Map.of("items", model.pageLoadTimings));
        writeCompactJsGlobal(new File(jsDir, "web-vitals-data.js"), "XLT_WEB_VITALS", Map.of("items", model.webVitals));

        final boolean hasErrors = Boolean.TRUE.equals(model.capabilities.get("hasErrors"));
        final Map<String, Object> errPayload = new LinkedHashMap<>(2);
        errPayload.put("available", Boolean.valueOf(hasErrors));
        errPayload.put("items", model.errors);
        writeCompactJsGlobal(new File(jsDir, "errors-data.js"), "XLT_ERRORS", errPayload);

        final boolean hasEvents = Boolean.TRUE.equals(model.capabilities.get("hasEvents"));
        final Map<String, Object> evPayload = new LinkedHashMap<>(2);
        evPayload.put("available", Boolean.valueOf(hasEvents));
        evPayload.put("items", model.events);
        writeCompactJsGlobal(new File(jsDir, "events-data.js"), "XLT_EVENTS", evPayload);

        final boolean hasNetwork = Boolean.TRUE.equals(model.capabilities.get("hasNetwork"));
        final Map<String, Object> netPayload = new LinkedHashMap<>(model.network);
        netPayload.put("available", Boolean.valueOf(hasNetwork));
        writeCompactJsGlobal(new File(jsDir, "network-data.js"), "XLT_NETWORK", netPayload);

        final boolean hasCfg = Boolean.TRUE.equals(model.capabilities.get("hasConfiguration"));
        final Map<String, Object> cfgPayload = new LinkedHashMap<>(model.configuration);
        cfgPayload.put("available", Boolean.valueOf(hasCfg));
        writeCompactJsGlobal(new File(jsDir, "config-data.js"), "XLT_CONFIG", cfgPayload);
    }

    private void writeCompactJsGlobal(final File targetFile, final String globalVarName, final Object payload) throws IOException
    {
        final String json = compactMapper.writeValueAsString(payload);
        Files.writeString(targetFile.toPath(), "window." + globalVarName + " = " + json + ";\n");
    }

    private void writeSnapshot(final ComparisonModel model, final File snapshotFile) throws IOException
    {
        try (GZIPOutputStream gzos = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(snapshotFile))))
        {
            compactMapper.writeValue(gzos, model);
        }
    }

    private ComparisonModel readSnapshot(final File snapshotFile) throws IOException
    {
        try (GZIPInputStream gzis = new GZIPInputStream(new BufferedInputStream(new FileInputStream(snapshotFile))))
        {
            return compactMapper.readValue(gzis, new TypeReference<ComparisonModel>()
            {
            });
        }
    }

    // =========================================================================
    // Transformation & Formatting Utilities
    // =========================================================================

    private static ObjectMapper createCompactMapper()
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    private static List<Map<String, Object>> toCompactOverviewList(final List<Map<String, Object>> items)
    {
        final List<Map<String, Object>> out = new ArrayList<>();
        if (items == null)
        {
            return out;
        }
        for (final Map<String, Object> item : items)
        {
            if (Boolean.TRUE.equals(item.get("isAggregate")))
            {
                continue;
            }
            final Map<String, Object> slim = new LinkedHashMap<>(5);
            slim.put("key", item.get("key"));
            slim.put("name", item.get("name"));
            final Object labels = item.get("labels");
            if (labels instanceof List<?> l && !l.isEmpty())
            {
                slim.put("labels", l);
            }
            final Object method = item.get("method");
            if (method instanceof String s && !s.isEmpty())
            {
                slim.put("method", s);
            }
            @SuppressWarnings("unchecked")
            final List<Map<String, Object>> rawRuns = (List<Map<String, Object>>) item.get("runs");
            final List<Map<String, Object>> slimRuns = new ArrayList<>(rawRuns != null ? rawRuns.size() : 0);
            if (rawRuns != null)
            {
                for (final Map<String, Object> rm : rawRuns)
                {
                    if (rm == null)
                    {
                        slimRuns.add(null);
                        continue;
                    }
                    final Map<String, Object> sr = new LinkedHashMap<>();
                    putIfPresent(sr, "count", rm.get("count"));
                    putIfPresent(sr, "rate", rm.get("rate"));
                    putIfPresent(sr, "cph", rm.get("cph"));
                    putIfPresent(sr, "errors", rm.get("errors"));
                    putIfPresent(sr, "events", rm.get("events"));
                    putIfPresent(sr, "mean", rm.get("mean"));
                    putIfPresent(sr, "median", rm.get("median"));
                    putIfPresent(sr, "percentiles", rm.get("percentiles"));
                    slimRuns.add(sr);
                }
            }
            slim.put("runs", slimRuns);
            out.add(slim);
        }
        return out;
    }

    private static List<Map<String, Object>> finalizeItems(final Collection<ItemAccumulator> values, final boolean isRequests)
    {
        final List<ItemAccumulator> sorted = new ArrayList<>(values);
        sorted.sort((a, b) -> {
            if (a.isAggregate != b.isAggregate)
            {
                return a.isAggregate ? -1 : 1;
            }
            return a.name.compareToIgnoreCase(b.name);
        });

        final List<Map<String, Object>> out = new ArrayList<>(sorted.size());
        for (final ItemAccumulator acc : sorted)
        {
            final Map<String, Object> m = new LinkedHashMap<>();
            m.put("key", acc.name);
            m.put("name", acc.name);
            m.put("isAggregate", Boolean.valueOf(acc.isAggregate));
            m.put("labels", acc.labels.isEmpty() ? Collections.emptyList() : new ArrayList<>(acc.labels));
            if (isRequests && !acc.method.isEmpty())
            {
                m.put("method", acc.method);
            }
            if (isRequests && !acc.sampleUrls.isEmpty())
            {
                m.put("sampleUrls", new ArrayList<>(acc.sampleUrls));
            }
            if (!acc.description.isEmpty())
            {
                m.put("description", acc.description);
            }
            m.put("runs", acc.runs);
            out.add(m);
        }
        return out;
    }

    private static List<Map<String, Object>> finalizeErrors(final Collection<ErrorAccumulator> values)
    {
        final List<Map<String, Object>> out = new ArrayList<>(values.size());
        for (final ErrorAccumulator acc : values)
        {
            final Map<String, Object> m = new LinkedHashMap<>(7);
            m.put("key", acc.key);
            m.put("testCaseName", acc.testCaseName);
            m.put("actionName", acc.actionName);
            m.put("requestName", acc.requestName);
            m.put("message", acc.message);
            m.put("exceptionClass", ModernReportGenerator.extractExceptionClass(acc.traceSample, acc.message));
            if (StringUtils.isNotBlank(acc.traceSample))
            {
                m.put("traceSample", acc.traceSample);
            }
            m.put("runs", toLongList(acc.counts));
            out.add(m);
        }
        out.sort((a, b) -> Long.compare(maxInList(b.get("runs")), maxInList(a.get("runs"))));
        return out;
    }

    private static List<Map<String, Object>> finalizeEvents(final Collection<EventAccumulator> values)
    {
        final List<Map<String, Object>> out = new ArrayList<>(values.size());
        for (final EventAccumulator acc : values)
        {
            final Map<String, Object> m = new LinkedHashMap<>(5);
            m.put("key", acc.key);
            m.put("testCaseName", acc.testCaseName);
            m.put("name", acc.name);
            m.put("sampleMessages", new ArrayList<>(acc.sampleMessages));
            m.put("runs", toLongList(acc.counts));
            out.add(m);
        }
        out.sort((a, b) -> Long.compare(maxInList(b.get("runs")), maxInList(a.get("runs"))));
        return out;
    }

    private static List<Map<String, Object>> finalizeNetwork(
        final Collection<NetworkAccumulator> values, final String keyField, final String labelField, final boolean numericKey)
    {
        final List<NetworkAccumulator> sorted = new ArrayList<>(values);
        if (numericKey)
        {
            sorted.sort(Comparator.comparingInt(a -> (int) ModernReportGenerator.parseLongSafe(a.key, 0L)));
        }
        else
        {
            sorted.sort((a, b) -> Long.compare(maxArr(b.counts), maxArr(a.counts)));
        }

        final List<Map<String, Object>> out = new ArrayList<>(sorted.size());
        for (final NetworkAccumulator acc : sorted)
        {
            final Map<String, Object> m = new LinkedHashMap<>(3);
            m.put(keyField, numericKey ? Integer.valueOf((int) ModernReportGenerator.parseLongSafe(acc.key, 0L)) : acc.key);
            if (labelField != null)
            {
                m.put(labelField, acc.label);
            }
            m.put("runs", toLongList(acc.counts));
            out.add(m);
        }
        return out;
    }

    private static List<Map<String, Object>> finalizeLoadProfiles(final Collection<LoadProfileAccumulator> values)
    {
        final List<Map<String, Object>> out = new ArrayList<>(values.size());
        for (final LoadProfileAccumulator acc : values)
        {
            final Map<String, Object> m = new LinkedHashMap<>(3);
            m.put("name", acc.name);
            m.put("className", acc.className);
            m.put("runs", acc.runs);
            out.add(m);
        }
        return out;
    }

    private static List<Map<String, Object>> finalizeProperties(final Collection<PropertyAccumulator> values)
    {
        final List<PropertyAccumulator> sorted = new ArrayList<>(values);
        sorted.sort(Comparator.comparing(a -> a.key));
        final List<Map<String, Object>> out = new ArrayList<>(sorted.size());
        for (final PropertyAccumulator acc : sorted)
        {
            final Map<String, Object> m = new LinkedHashMap<>(2);
            m.put("key", acc.key);
            m.put("runs", acc.values);
            out.add(m);
        }
        return out;
    }

    private static List<Long> toLongList(final long[] arr)
    {
        final List<Long> list = new ArrayList<>(arr.length);
        for (final long v : arr)
        {
            list.add(Long.valueOf(v));
        }
        return list;
    }

    private static long maxArr(final long[] arr)
    {
        long max = 0L;
        for (final long v : arr)
        {
            if (v > max)
            {
                max = v;
            }
        }
        return max;
    }

    private static long maxInList(final Object raw)
    {
        if (!(raw instanceof List<?> list))
        {
            return 0L;
        }
        long max = 0L;
        for (final Object o : list)
        {
            if (o instanceof Number n && n.longValue() > max)
            {
                max = n.longValue();
            }
        }
        return max;
    }

    private static int countNonAggregate(final List<Map<String, Object>> list)
    {
        if (list == null)
        {
            return 0;
        }
        int count = 0;
        for (final Map<String, Object> m : list)
        {
            if (!Boolean.TRUE.equals(m.get("isAggregate")))
            {
                count++;
            }
        }
        return count;
    }

    private static void collectPercentileKeys(final Object pctMapObj, final Set<String> percentileSet)
    {
        if (!(pctMapObj instanceof Map<?, ?> map))
        {
            return;
        }
        for (final Object k : map.keySet())
        {
            final String norm = normalizePercentileKey(String.valueOf(k));
            if (!norm.isEmpty())
            {
                percentileSet.add(norm);
            }
        }
    }

    private static String normalizePercentileKey(final String raw)
    {
        if (StringUtils.isBlank(raw))
        {
            return "";
        }
        final String stripped = raw.trim().replaceFirst("^[pP]", "");
        return stripped;
    }

    private static List<String> sortPercentilesNumerically(final Set<String> rawSet)
    {
        final List<String> list = new ArrayList<>(rawSet);
        list.sort(Comparator.comparingDouble(s -> ModernReportGenerator.parseDoubleSafe(s, Double.MAX_VALUE)));
        return list;
    }

    private static Double parseNullableDouble(final String s)
    {
        if (StringUtils.isBlank(s))
        {
            return null;
        }
        final String trimmed = s.trim();
        if ("null".equalsIgnoreCase(trimmed) || "NaN".equalsIgnoreCase(trimmed))
        {
            return null;
        }
        try
        {
            final double d = Double.parseDouble(trimmed);
            return Double.isFinite(d) ? Double.valueOf(d) : null;
        }
        catch (final NumberFormatException e)
        {
            return null;
        }
    }

    private static void putIfNonNull(final Map<String, Object> map, final String key, final Object val)
    {
        if (val != null)
        {
            map.put(key, val);
        }
    }

    private static void putIfPresent(final Map<String, Object> map, final String key, final Object val)
    {
        if (val != null)
        {
            map.put(key, val);
        }
    }

    private static String extractHttpMethodFromName(final String name)
    {
        if (StringUtils.isBlank(name))
        {
            return "";
        }
        final Matcher m = HTTP_METHOD_TOKEN_PATTERN.matcher(name);
        String last = "";
        while (m.find())
        {
            last = m.group(1);
        }
        return last;
    }

    private static long parseFlexibleTimestampMs(final String tsStr)
    {
        if (StringUtils.isBlank(tsStr))
        {
            return 0L;
        }
        final long xltMs = ModernReportGenerator.parseTimestampMs(tsStr);
        if (xltMs > 0L)
        {
            return xltMs;
        }
        try
        {
            return ZonedDateTime.parse(tsStr.trim(), DATE_TO_STRING_FMT).toInstant().toEpochMilli();
        }
        catch (final Exception e)
        {
            return 0L;
        }
    }

    private static long extractUsersCount(final String usersStr)
    {
        if (StringUtils.isBlank(usersStr))
        {
            return 0L;
        }
        final String normalized = usersStr.replaceAll("(?<=\\d),(?=\\d{3}\\b)", "");
        final Matcher m = Pattern.compile("\\d+").matcher(normalized);
        long maxVal = 0L;
        while (m.find())
        {
            final long v = ModernReportGenerator.parseLongSafe(m.group(), 0L);
            if (v > maxVal)
            {
                maxVal = v;
            }
        }
        return maxVal;
    }

    private static String extractPlainCommentBody(final List<String> comments, final String headline)
    {
        if (comments == null || comments.isEmpty())
        {
            return "";
        }
        final List<String> parts = new ArrayList<>();
        for (final String raw : comments)
        {
            if (StringUtils.isBlank(raw))
            {
                continue;
            }
            String text = COMMENT_BLOCK_BREAK_PATTERN.matcher(raw).replaceAll("\n");
            text = HTML_TAG_PATTERN.matcher(text).replaceAll("");
            text = text.replaceAll("[ \\t\\r]+", " ").replaceAll("\\n\\s*\\n+", "\n").trim();
            if (StringUtils.isNotBlank(headline) && text.startsWith(headline))
            {
                text = text.substring(headline.length()).trim();
            }
            if (!text.isEmpty())
            {
                parts.add(text);
            }
        }
        return String.join("\n", parts);
    }

    private static List<Map<String, Object>> stripReportHrefs(final List<Map<String, Object>> runs)
    {
        if (runs == null)
        {
            return null;
        }
        final List<Map<String, Object>> sanitized = new ArrayList<>(runs.size());
        for (final Map<String, Object> run : runs)
        {
            if (run == null)
            {
                sanitized.add(null);
            }
            else if (run.containsKey("reportHref"))
            {
                final Map<String, Object> copy = new LinkedHashMap<>(run);
                copy.remove("reportHref");
                sanitized.add(copy);
            }
            else
            {
                sanitized.add(run);
            }
        }
        return sanitized;
    }

    private static String computeRelativeReportHref(final File outputDir, final File sourceReportDir)
    {
        if (outputDir == null || sourceReportDir == null)
        {
            return null;
        }
        final File indexFile = new File(sourceReportDir, "index.html");
        if (!indexFile.isFile())
        {
            return null;
        }
        try
        {
            final Path outPath = outputDir.getCanonicalFile().toPath();
            final Path targetPath = indexFile.getCanonicalFile().toPath();
            return outPath.relativize(targetPath).toString().replace(File.separatorChar, '/');
        }
        catch (final IOException | IllegalArgumentException e)
        {
            LOG.debug("Could not compute relative link from '{}' to '{}'", outputDir, indexFile, e);
            return null;
        }
    }

    // =========================================================================
    // Internal Mutable Accumulators & Snapshot POJO
    // =========================================================================

    static final class ComparisonModel
    {
        public String reportKind;

        public String productName;

        public String productVersion;

        public String productUrl;

        public String projectName;

        public List<Map<String, Object>> runs = new ArrayList<>();

        public List<String> runtimePercentiles = new ArrayList<>();

        public Map<String, Boolean> capabilities = new LinkedHashMap<>();

        public Map<String, Integer> counts = new LinkedHashMap<>();

        public List<Map<String, Object>> transactions = new ArrayList<>();

        public List<Map<String, Object>> actions = new ArrayList<>();

        public List<Map<String, Object>> requests = new ArrayList<>();

        public List<Map<String, Object>> customTimers = new ArrayList<>();

        public List<Map<String, Object>> customValues = new ArrayList<>();

        public List<Map<String, Object>> pageLoadTimings = new ArrayList<>();

        public List<Map<String, Object>> webVitals = new ArrayList<>();

        public List<Map<String, Object>> errors = new ArrayList<>();

        public List<Map<String, Object>> events = new ArrayList<>();

        public Map<String, Object> network = new LinkedHashMap<>();

        public Map<String, Object> configuration = new LinkedHashMap<>();
    }

    private static final class ItemAccumulator
    {
        final String name;

        final boolean isAggregate;

        final Set<String> labels = new LinkedHashSet<>();

        String method = "";

        String description = "";

        final Set<String> sampleUrls = new LinkedHashSet<>();

        final List<Map<String, Object>> runs;

        ItemAccumulator(final String name, final boolean isAggregate, final int runCount)
        {
            this.name = name;
            this.isAggregate = isAggregate;
            this.runs = new ArrayList<>(Collections.nCopies(runCount, null));
        }
    }

    private static final class ErrorAccumulator
    {
        final String key;

        final String testCaseName;

        final String actionName;

        final String requestName;

        final String message;

        String traceSample;

        final long[] counts;

        ErrorAccumulator(
            final String key, final String testCaseName, final String actionName,
            final String requestName, final String message, final String traceSample, final int runCount)
        {
            this.key = key;
            this.testCaseName = testCaseName;
            this.actionName = actionName;
            this.requestName = requestName;
            this.message = message;
            this.traceSample = traceSample;
            this.counts = new long[runCount];
        }
    }

    private static final class EventAccumulator
    {
        final String key;

        final String testCaseName;

        final String name;

        final Set<String> sampleMessages = new LinkedHashSet<>();

        final long[] counts;

        EventAccumulator(final String key, final String testCaseName, final String name, final int runCount)
        {
            this.key = key;
            this.testCaseName = testCaseName;
            this.name = name;
            this.counts = new long[runCount];
        }
    }

    private static final class NetworkAccumulator
    {
        final String key;

        String label;

        final long[] counts;

        NetworkAccumulator(final String key, final String label, final int runCount)
        {
            this.key = key;
            this.label = label != null ? label : "";
            this.counts = new long[runCount];
        }
    }

    private static final class LoadProfileAccumulator
    {
        final String name;

        final String className;

        final List<Map<String, Object>> runs;

        LoadProfileAccumulator(final String name, final String className, final int runCount)
        {
            this.name = name;
            this.className = className;
            this.runs = new ArrayList<>(Collections.nCopies(runCount, null));
        }
    }

    private static final class PropertyAccumulator
    {
        final String key;

        final List<String> values;

        PropertyAccumulator(final String key, final int runCount)
        {
            this.key = key;
            this.values = new ArrayList<>(Collections.nCopies(runCount, null));
        }
    }
}
