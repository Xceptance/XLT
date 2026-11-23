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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xceptance.common.util.ProductInformation;
import com.xceptance.xlt.common.XltConstants;

/**
 * Generates modern client-rendered report data bundles ({@code js/report-meta.js} and per-page {@code js/*-data.js})
 * from {@code testreport.xml}, optional {@code scorecard.xml}, and optional {@code charts/*&#47;*.json} time-series
 * files, and manages copying static template assets from {@code config/testreport-modern/}.
 */
public class ModernReportGenerator
{
    /** Default number of time-slice buckets used for duration-agnostic sparklines. */
    public static final int SPARKLINE_BUCKETS = 24;

    private static final Logger LOG = LoggerFactory.getLogger(ModernReportGenerator.class);

    private static final DateTimeFormatter XLT_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z", Locale.US);

    private static final Pattern COMMENT_NAME_PATTERN = Pattern.compile(
        "<[^>]*class\\s*=\\s*['\"][^'\"]*\\bname\\b[^'\"]*['\"][^>]*>(.*?)</div\\s*>",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern COMMENT_XTC_HEADING_PATTERN = Pattern.compile(
        "<div[^>]*class\\s*=\\s*['\"][^'\"]*\\bxtc[_-]comment\\b[^'\"]*['\"][^>]*>\\s*<(?:strong|b)[^>]*>(.*?)</(?:strong|b)>",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    private static final Set<String> NUMERIC_CELL_TAGS = Set.of("int", "long", "double", "float", "big-decimal", "short", "byte");

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final JsonFactory jsonFactory = objectMapper.getFactory();

    /**
     * Copies static HTML/CSS/JS/font/image template files from {@code templateDir} ({@code config/testreport-modern})
     * into {@code reportDir}, omitting {@code scorecard.html} when {@code scorecardPresent} is {@code false}.
     *
     * @param templateDir
     *            the source modern template directory
     * @param reportDir
     *            the target report directory
     * @param scorecardPresent
     *            whether scorecard output is present for this report
     * @throws IOException
     *             if copying template files fails
     */
    public void copyTemplateResources(final File templateDir, final File reportDir, final boolean scorecardPresent) throws IOException
    {
        if (templateDir == null || !templateDir.isDirectory())
        {
            throw new FileNotFoundException("Modern report template directory not found: " + templateDir);
        }

        FileUtils.forceMkdir(reportDir);

        IOFileFilter filter = FileFilterUtils.makeSVNAware(null);
        if (!scorecardPresent)
        {
            final IOFileFilter skipScorecardHtml = new IOFileFilter()
            {
                @Override
                public boolean accept(final File file)
                {
                    return !(file.isFile() && file.getParentFile() != null && file.getParentFile().equals(templateDir)
                             && XltConstants.SCORECARD_REPORT_HTML_FILENAME.equals(file.getName()));
                }

                @Override
                public boolean accept(final File dir, final String name)
                {
                    return !(dir != null && dir.equals(templateDir) && XltConstants.SCORECARD_REPORT_HTML_FILENAME.equals(name));
                }
            };
            filter = FileFilterUtils.and(filter, skipScorecardHtml);
        }

        FileUtils.copyDirectory(templateDir, reportDir, filter, false);

        if (!scorecardPresent)
        {
            FileUtils.deleteQuietly(new File(reportDir, XltConstants.SCORECARD_REPORT_HTML_FILENAME));
            FileUtils.deleteQuietly(new File(new File(reportDir, "js"), "scorecard-data.js"));
        }
    }

    /**
     * Generates {@code js/report-meta.js} and all per-page {@code js/*-data.js} bundles for {@code reportDir} by
     * reading {@code <reportDir>/testreport.xml}, optional {@code <reportDir>/scorecard.xml}, and optional
     * {@code <reportDir>/charts/*&#47;*.json}.
     *
     * @param reportDir
     *            the target report directory containing {@code testreport.xml}
     * @throws Exception
     *             if {@code testreport.xml} is missing or bundle generation fails
     */
    public void generateReportBundles(final File reportDir) throws Exception
    {
        final File xmlFile = new File(reportDir, XltConstants.LOAD_REPORT_XML_FILENAME);
        final File scFile = new File(reportDir, XltConstants.SCORECARD_REPORT_XML_FILENAME);
        generateReportBundles(xmlFile, reportDir, scFile.isFile() ? scFile : null);
    }

    /**
     * Generates {@code js/report-meta.js} and all per-page {@code js/*-data.js} bundles from {@code testReportXmlFile}
     * and optional {@code scorecardXmlFile} into {@code <reportDir>/js/}.
     *
     * @param testReportXmlFile
     *            the {@code testreport.xml} file to read
     * @param reportDir
     *            the target report output directory
     * @param scorecardXmlFile
     *            optional {@code scorecard.xml} file (may be {@code null} or non-existent)
     * @throws Exception
     *             if XML parsing or JS bundle serialization fails
     */
    public void generateReportBundles(final File testReportXmlFile, final File reportDir, final File scorecardXmlFile) throws Exception
    {
        if (testReportXmlFile == null || !testReportXmlFile.isFile())
        {
            throw new FileNotFoundException("Report XML file does not exist: " + testReportXmlFile);
        }

        final File jsDir = new File(reportDir, "js");
        FileUtils.forceMkdir(jsDir);

        final Document doc = parseXmlDocument(testReportXmlFile);
        final Element root = doc.getDocumentElement();

        final File effectiveScorecardXml = (scorecardXmlFile != null && scorecardXmlFile.isFile())
            ? scorecardXmlFile
            : new File(reportDir, XltConstants.SCORECARD_REPORT_XML_FILENAME);
        final boolean hasScorecard = effectiveScorecardXml.isFile();

        Map<String, Object> fullScorecard = null;
        Map<String, Object> scorecardSummary = createAbsentScorecardSummary();
        if (hasScorecard)
        {
            final ScorecardParsed parsedSc = parseScorecardXmlSafe(effectiveScorecardXml);
            fullScorecard = parsedSc.fullBundle;
            scorecardSummary = parsedSc.summary;
        }

        // 1. General & Configuration metadata
        final Element generalEl = firstChildElement(root, "general");
        final Element summaryEl = firstChildElement(root, "summary");
        final Element configEl = firstChildElement(root, "configuration");
        final Element testReportCfgEl = firstChildElement(root, "testReportConfig");
        final Element errorsRootEl = firstChildElement(root, "errors");

        final String startTimeStr = childText(generalEl, "startTime", "");
        final String endTimeStr = childText(generalEl, "endTime", "");
        final long durationSec = childLong(generalEl, "duration", 0L);
        final long bytesSent = childLong(generalEl, "bytesSent", 0L);
        final long bytesReceived = childLong(generalEl, "bytesReceived", 0L);
        final long hits = childLong(generalEl, "hits", 0L);

        final long startTimeMs = parseTimestampMs(startTimeStr);
        final long endTimeMs = parseTimestampMs(endTimeStr);
        final long durationMs = durationSec > 0 ? durationSec * 1000L : Math.max(0L, endTimeMs - startTimeMs);
        final String durationDisplay = formatDurationHms(durationSec > 0 ? durationSec : durationMs / 1000L);

        // Version info
        final Element versionEl = firstChildElement(configEl, "version");
        final ProductInformation prodInfo = ProductInformation.getProductInformation();
        final String productName = nonBlankOrDefault(childText(versionEl, "productName", null), prodInfo.getProductName());
        final String productVersion = nonBlankOrDefault(childText(versionEl, "version", null), prodInfo.getVersion());
        final String productUrl = nonBlankOrDefault(
            childText(versionEl, "productURL", null),
            prodInfo.getProductURL() != null ? prodInfo.getProductURL().toString() : "");
        final String revisionNumber = childText(versionEl, "revisionNumber", "");
        final String buildDate = childText(versionEl, "buildDate", "");
        final String vendorName = childText(versionEl, "vendorName", "");

        final String projectName = childText(configEl, "projectName", "").trim();
        final List<String> comments = readStringList(firstChildElement(configEl, "comments"), "string");

        // Properties
        final List<Map<String, String>> propertiesList = new ArrayList<>();
        String xtcLoadTestName = null;
        final Element propsEl = firstChildElement(configEl, "properties");
        if (propsEl != null)
        {
            for (final Element propEl : childElements(propsEl, "property"))
            {
                final String name = propEl.getAttribute("name");
                final String value = propEl.getAttribute("value");
                final Map<String, String> entry = new LinkedHashMap<>(2);
                entry.put("name", name);
                entry.put("value", value);
                propertiesList.add(entry);
                if ("com.xceptance.xtc.loadtest.name".equals(name) && StringUtils.isNotBlank(value))
                {
                    xtcLoadTestName = value.trim();
                }
            }
            propertiesList.sort(Comparator.comparing(m -> m.getOrDefault("name", "")));
        }

        final String loadTestName = nonBlankOrDefault(xtcLoadTestName, extractCommentHeadline(comments));

        // testReportConfig: percentiles, runtimeIntervals, requestTableColorization
        final Map<String, Object> testReportConfig = parseTestReportConfig(testReportCfgEl);
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> colorizationGroups = (List<Map<String, Object>>) testReportConfig.get("requestTableColorization");
        final boolean hasColorizationConfig = colorizationGroups != null && !colorizationGroups.isEmpty();

        // Charts & AI data flags
        final boolean chartsGenerated = hasGeneratedCharts(reportDir);
        final boolean aiDataGenerated = new File(reportDir, "ai-data.md").isFile();
        final String resultsPathPrefix = childText(errorsRootEl, "resultsPathPrefix", "").trim();
        final boolean linkToResultBrowser = StringUtils.isNotBlank(resultsPathPrefix);

        final File chartsDir = new File(reportDir, "charts");

        // 2. Timer sections: Transactions, Actions, Requests, Custom Timers, Page Load Timings
        final Map<String, Object> txBundle = buildTimerSectionBundle(
            firstChildElement(root, "transactions"), "transaction",
            firstChildElement(summaryEl, "transactions"),
            chartsGenerated ? new File(chartsDir, "transactions") : null,
            false, false, false, startTimeMs, endTimeMs);

        final Map<String, Object> actBundle = buildTimerSectionBundle(
            firstChildElement(root, "actions"), "action",
            firstChildElement(summaryEl, "actions"),
            chartsGenerated ? new File(chartsDir, "actions") : null,
            true, false, false, startTimeMs, endTimeMs);

        final Map<String, Object> reqBundle = buildTimerSectionBundle(
            firstChildElement(root, "requests"), "request",
            firstChildElement(summaryEl, "requests"),
            chartsGenerated ? new File(chartsDir, "requests") : null,
            false, true, hasColorizationConfig, startTimeMs, endTimeMs);

        final Map<String, Object> ctBundle = buildTimerSectionBundle(
            firstChildElement(root, "customTimers"), "customTimer",
            firstChildElement(summaryEl, "customTimers"),
            chartsGenerated ? new File(chartsDir, "custom") : null,
            false, false, false, startTimeMs, endTimeMs);

        final Map<String, Object> pltBundle = buildTimerSectionBundle(
            firstChildElement(root, "pageLoadTimings"), "pageLoadTiming",
            firstChildElement(summaryEl, "pageLoadTimings"),
            chartsGenerated ? new File(chartsDir, "pageLoadTimings") : null,
            false, false, false, startTimeMs, endTimeMs);

        // 3. Slowest Requests (cross-referencing baseline mean & p95 from requests)
        final Map<String, Map<String, Object>> reqLookupByName = buildRequestLookupByName(reqBundle);
        final Map<String, Object> slowestReqBundle = buildSlowestRequestsBundle(firstChildElement(root, "slowestRequests"), reqLookupByName);

        // 4. Custom Data Logs & Custom Values
        final List<Map<String, Object>> customLogsList = parseCustomLogs(firstChildElement(root, "customLogs"), reportDir);
        final Map<String, Object> customValuesBundle = buildCustomValuesBundle(
            firstChildElement(root, "customValues"),
            chartsGenerated ? new File(chartsDir, "customvalues") : null,
            customLogsList, startTimeMs, endTimeMs);

        // 5. Agents
        final Map<String, Object> agentsBundle = buildAgentsBundle(
            firstChildElement(root, "agents"),
            firstChildElement(summaryEl, "agents"));

        // 6. Web Vitals
        final Map<String, Object> webVitalsBundle = buildWebVitalsBundle(firstChildElement(root, "webVitalsList"));

        // 7. Events
        final Map<String, Object> eventsBundle = buildEventsBundle(firstChildElement(root, "events"));

        // 8. Network (Hosts, IPs, Methods, Response Codes, Content Types)
        final Map<String, Object> networkBundle = buildNetworkBundle(root, bytesSent, bytesReceived, hits, durationSec);

        // 9. Errors
        final Map<String, Object> errorsBundle = buildErrorsBundle(errorsRootEl, summaryEl, customLogsList);

        // 10. External Data
        final Map<String, Object> externalBundle = buildExternalBundle(firstChildElement(root, "external"));

        // 11. Load Profile & Configuration Bundle
        final LoadProfileParsed loadProfileParsed = parseLoadProfile(firstChildElement(configEl, "loadProfile"));
        final Map<String, Object> configBundle = buildConfigBundle(
            propertiesList, loadProfileParsed, configEl,
            productName, productVersion, revisionNumber, buildDate, vendorName);

        // 12. Counts map for window.XLT_META
        final int txItemCount = countNonAggregate(txBundle);
        final int actItemCount = countNonAggregate(actBundle);
        final int reqItemCount = countNonAggregate(reqBundle);
        final int slowestCount = listSize(slowestReqBundle.get("items"));
        final int ctItemCount = countNonAggregate(ctBundle);
        final int cvItemCount = listSize(customValuesBundle.get("items"));
        final int customLogsCount = customLogsList.size();
        final int pltItemCount = countNonAggregate(pltBundle);
        final int wvItemCount = listSize(webVitalsBundle.get("items"));
        final int agentItemCount = countNonAggregate(agentsBundle);
        final int hostsCount = listSize(networkBundle.get("hosts"));
        final int ipsCount = listSize(networkBundle.get("ips"));
        final int externalSectionsCount = countExternalArtifacts(externalBundle);

        @SuppressWarnings("unchecked")
        final Map<String, Object> errSummary = (Map<String, Object>) errorsBundle.get("summary");
        final int txErrCount = ((Number) errSummary.getOrDefault("transactionErrors", Integer.valueOf(0))).intValue();
        final int actErrCount = ((Number) errSummary.getOrDefault("actionErrors", Integer.valueOf(0))).intValue();
        final int reqErrCount = ((Number) errSummary.getOrDefault("requestErrors", Integer.valueOf(0))).intValue();
        final int totalErrorsBadge = txErrCount > 0 ? txErrCount : (actErrCount > 0 ? actErrCount : reqErrCount);

        final int totalEventsCount = resolveTotalEventsCount(summaryEl, eventsBundle);

        final Map<String, Object> counts = new LinkedHashMap<>();
        counts.put("transactions", Integer.valueOf(txItemCount));
        counts.put("actions", Integer.valueOf(actItemCount));
        counts.put("requests", Integer.valueOf(reqItemCount));
        counts.put("slowestRequests", Integer.valueOf(slowestCount));
        counts.put("customTimers", Integer.valueOf(ctItemCount));
        counts.put("customValues", Integer.valueOf(cvItemCount));
        counts.put("customLogs", Integer.valueOf(customLogsCount));
        counts.put("pageLoadTimings", Integer.valueOf(pltItemCount));
        counts.put("webVitals", Integer.valueOf(wvItemCount));
        counts.put("agents", Integer.valueOf(agentItemCount));
        counts.put("errors", Integer.valueOf(totalErrorsBadge));
        counts.put("requestErrors", Integer.valueOf(reqErrCount));
        counts.put("actionErrors", Integer.valueOf(actErrCount));
        counts.put("transactionErrors", Integer.valueOf(txErrCount));
        counts.put("events", Integer.valueOf(totalEventsCount));
        counts.put("properties", Integer.valueOf(propertiesList.size()));
        counts.put("hosts", Integer.valueOf(hostsCount));
        counts.put("ips", Integer.valueOf(ipsCount));
        counts.put("externalCharts", Integer.valueOf(externalSectionsCount));

        // 13. Overview Bundle
        final Map<String, Object> overviewBundle = buildOverviewBundle(
            startTimeStr, endTimeStr, durationSec, bytesSent, bytesReceived, hits,
            comments, scorecardSummary, summaryEl, agentItemCount, txBundle, errorsBundle, networkBundle, loadProfileParsed);

        // 13b. Executive Summary Bundle (window.XLT_EXECUTIVE)
        final Map<String, Object> executiveBundle = buildExecutiveBundle(
            summaryEl, chartsGenerated ? chartsDir : null, startTimeMs, endTimeMs,
            txBundle, actBundle, reqBundle, errorsBundle, eventsBundle, networkBundle,
            agentsBundle, webVitalsBundle, loadProfileParsed, fullScorecard, testReportConfig);

        // 14. Shared Metadata Bundle (window.XLT_META)
        final Map<String, Object> metaBundle = new LinkedHashMap<>();
        metaBundle.put("productName", productName);
        metaBundle.put("productVersion", productVersion);
        metaBundle.put("productUrl", productUrl);
        metaBundle.put("xltVersion", productVersion);
        metaBundle.put("projectName", projectName);
        metaBundle.put("loadTestName", loadTestName);
        metaBundle.put("comments", comments);
        metaBundle.put("startTime", startTimeStr);
        metaBundle.put("endTime", endTimeStr);
        metaBundle.put("startTimeMs", Long.valueOf(startTimeMs));
        metaBundle.put("endTimeMs", Long.valueOf(endTimeMs));
        metaBundle.put("durationMs", Long.valueOf(durationMs));
        metaBundle.put("durationDisplay", durationDisplay);
        metaBundle.put("chartsGenerated", Boolean.valueOf(chartsGenerated));
        metaBundle.put("aiDataGenerated", Boolean.valueOf(aiDataGenerated));
        metaBundle.put("linkToResultBrowser", Boolean.valueOf(linkToResultBrowser));
        metaBundle.put("counts", counts);
        metaBundle.put("scorecard", scorecardSummary);
        metaBundle.put("testReportConfig", testReportConfig);

        // 15. Write all JS bundles deterministically
        writeJsGlobal(new File(jsDir, "report-meta.js"), "XLT_META", metaBundle);
        writeJsGlobal(new File(jsDir, "overview-data.js"), "XLT_OVERVIEW", overviewBundle);
        writeJsGlobal(new File(jsDir, "executive-data.js"), "XLT_EXECUTIVE", executiveBundle);
        writeJsGlobal(new File(jsDir, "transactions-data.js"), "XLT_TRANSACTIONS", txBundle);
        writeJsGlobal(new File(jsDir, "actions-data.js"), "XLT_ACTIONS", actBundle);
        writeJsGlobal(new File(jsDir, "requests-data.js"), "XLT_REQUESTS", reqBundle);
        writeJsGlobal(new File(jsDir, "slowest-requests-data.js"), "XLT_SLOWEST_REQUESTS", slowestReqBundle);
        writeJsGlobal(new File(jsDir, "network-data.js"), "XLT_NETWORK", networkBundle);
        writeJsGlobal(new File(jsDir, "errors-data.js"), "XLT_ERRORS", errorsBundle);
        writeJsGlobal(new File(jsDir, "agents-data.js"), "XLT_AGENTS", agentsBundle);
        writeJsGlobal(new File(jsDir, "events-data.js"), "XLT_EVENTS", eventsBundle);
        writeJsGlobal(new File(jsDir, "config-data.js"), "XLT_CONFIG", configBundle);
        writeJsGlobal(new File(jsDir, "custom-timers-data.js"), "XLT_CUSTOM_TIMERS", ctBundle);
        writeJsGlobal(new File(jsDir, "custom-values-data.js"), "XLT_CUSTOM_VALUES", customValuesBundle);
        writeJsGlobal(new File(jsDir, "page-load-timings-data.js"), "XLT_PAGE_LOAD_TIMINGS", pltBundle);
        writeJsGlobal(new File(jsDir, "web-vitals-data.js"), "XLT_WEB_VITALS", webVitalsBundle);
        writeJsGlobal(new File(jsDir, "external-data.js"), "XLT_EXTERNAL", externalBundle);

        final File scorecardDataJs = new File(jsDir, "scorecard-data.js");
        if (hasScorecard && fullScorecard != null)
        {
            writeJsGlobal(scorecardDataJs, "XLT_SCORECARD", fullScorecard);
        }
        else
        {
            FileUtils.deleteQuietly(scorecardDataJs);
        }
    }

    /**
     * Updates the modern scorecard bundle ({@code js/scorecard-data.js}), ensures {@code scorecard.html} is present in
     * {@code reportDir}, and refreshes {@code scorecard} in {@code js/report-meta.js} and {@code js/overview-data.js}.
     * If {@code js/report-meta.js} does not exist yet (for instance when {@code update_scorecard} is run on a classic
     * report directory configured for modern UI), copies templates and generates all modern bundles.
     *
     * @param scorecardXmlFile
     *            the evaluated {@code scorecard.xml} file
     * @param reportDir
     *            the report directory
     * @param templateDir
     *            optional {@code config/testreport-modern} template directory
     * @throws Exception
     *             if parsing or writing fails
     */
    public void updateScorecardBundles(final File scorecardXmlFile, final File reportDir, final File templateDir) throws Exception
    {
        if (scorecardXmlFile == null || !scorecardXmlFile.isFile())
        {
            throw new FileNotFoundException("Scorecard XML file does not exist: " + scorecardXmlFile);
        }

        final File jsDir = new File(reportDir, "js");
        final File metaJsFile = new File(jsDir, "report-meta.js");
        final File overviewJsFile = new File(jsDir, "overview-data.js");

        if (!metaJsFile.isFile())
        {
            if (templateDir != null && templateDir.isDirectory())
            {
                copyTemplateResources(templateDir, reportDir, true);
            }
            generateReportBundles(new File(reportDir, XltConstants.LOAD_REPORT_XML_FILENAME), reportDir, scorecardXmlFile);
            return;
        }

        if (templateDir != null && templateDir.isDirectory())
        {
            final File srcScorecardHtml = new File(templateDir, XltConstants.SCORECARD_REPORT_HTML_FILENAME);
            final File dstScorecardHtml = new File(reportDir, XltConstants.SCORECARD_REPORT_HTML_FILENAME);
            if (srcScorecardHtml.isFile())
            {
                FileUtils.copyFile(srcScorecardHtml, dstScorecardHtml, false);
            }
        }

        final ScorecardParsed parsed = parseScorecardXml(scorecardXmlFile);
        FileUtils.forceMkdir(jsDir);
        writeJsGlobal(new File(jsDir, "scorecard-data.js"), "XLT_SCORECARD", parsed.fullBundle);

        patchScorecardFieldInJsBundle(metaJsFile, "XLT_META", parsed.summary);
        if (overviewJsFile.isFile())
        {
            patchScorecardFieldInJsBundle(overviewJsFile, "XLT_OVERVIEW", parsed.summary);
        }
        final File executiveJsFile = new File(jsDir, "executive-data.js");
        if (executiveJsFile.isFile())
        {
            patchFieldInJsBundle(executiveJsFile, "XLT_EXECUTIVE", "scorecardGroups", extractScorecardGroups(parsed.fullBundle));
        }
    }

    /**
     * Downsamples a chart time-series JSON file ({@code [[timestampMs, mean, min, max, countPerSec], ...]}) into at
     * most {@link #SPARKLINE_BUCKETS} bucket averages across the test duration. Returns an empty list if the file is
     * {@code null}, missing, empty, or malformed.
     *
     * @param jsonFile
     *            the {@code charts/<section>/<chartBase>.json} file
     * @return downsampled bucket averages (empty list if unavailable)
     */
    public List<Double> downsampleChartJson(final File jsonFile)
    {
        return downsampleChartJson(jsonFile, SPARKLINE_BUCKETS, 0L, 0L);
    }

    /**
     * Downsamples a chart time-series JSON file into up to {@code maxBuckets} equal-width time-slice averages.
     *
     * @param jsonFile
     *            the chart JSON file
     * @param maxBuckets
     *            maximum number of buckets (typically 20)
     * @param reportStartMs
     *            optional overall report start timestamp in ms (0 to auto-detect from series)
     * @param reportEndMs
     *            optional overall report end timestamp in ms (0 to auto-detect from series)
     * @return list of rounded bucket averages, or {@code Collections.emptyList()} when absent
     */
    public List<Double> downsampleChartJson(final File jsonFile, final int maxBuckets, final long reportStartMs, final long reportEndMs)
    {
        if (jsonFile == null || !jsonFile.isFile() || maxBuckets <= 0)
        {
            return Collections.emptyList();
        }

        long[] timestamps = new long[256];
        double[] means = new double[256];
        int count = 0;

        try (JsonParser parser = jsonFactory.createParser(jsonFile))
        {
            if (parser.nextToken() != JsonToken.START_ARRAY)
            {
                return Collections.emptyList();
            }
            while (parser.nextToken() != JsonToken.END_ARRAY && parser.currentToken() != null)
            {
                if (parser.currentToken() == JsonToken.START_ARRAY)
                {
                    final JsonToken tokTs = parser.nextToken();
                    if (tokTs == null || tokTs == JsonToken.END_ARRAY)
                    {
                        continue;
                    }
                    final boolean tsValid = tokTs.isNumeric();
                    final long tsVal = tsValid ? parser.getLongValue() : 0L;
                    if (!tsValid)
                    {
                        parser.skipChildren();
                    }

                    final JsonToken tokMean = parser.nextToken();
                    if (tokMean == null || tokMean == JsonToken.END_ARRAY)
                    {
                        continue;
                    }
                    final boolean meanValid = tokMean.isNumeric();
                    final double meanVal = meanValid ? parser.getDoubleValue() : Double.NaN;
                    if (!meanValid)
                    {
                        parser.skipChildren();
                    }

                    // Consume remaining elements of the inner tuple until END_ARRAY
                    while (parser.nextToken() != JsonToken.END_ARRAY && parser.currentToken() != null)
                    {
                        if (parser.currentToken().isStructStart())
                        {
                            parser.skipChildren();
                        }
                    }

                    if (tsValid && meanValid && Double.isFinite(meanVal))
                    {
                        if (count == timestamps.length)
                        {
                            timestamps = Arrays.copyOf(timestamps, timestamps.length * 2);
                            means = Arrays.copyOf(means, means.length * 2);
                        }
                        timestamps[count] = tsVal;
                        means[count] = meanVal;
                        count++;
                    }
                }
                else if (parser.currentToken().isStructStart())
                {
                    parser.skipChildren();
                }
            }
        }
        catch (final Exception ex)
        {
            LOG.debug("Ignoring unreadable chart JSON file '{}': {}", jsonFile, ex.getMessage());
            return Collections.emptyList();
        }

        if (count == 0)
        {
            return Collections.emptyList();
        }
        if (count == 1)
        {
            return List.of(Double.valueOf(round1(means[0])));
        }

        long minTs = Long.MAX_VALUE;
        long maxTs = Long.MIN_VALUE;
        for (int i = 0; i < count; i++)
        {
            if (timestamps[i] < minTs)
            {
                minTs = timestamps[i];
            }
            if (timestamps[i] > maxTs)
            {
                maxTs = timestamps[i];
            }
        }

        // Compute the 5% moving average matching JFreeChartUtils.createMovingAverageTimeSeriesPercentage
        // so the sparkline reflects the smooth average curve without collapsing warm-ups or inflating end spikes
        final int samples = Math.max(1, (count * 5) / 100);
        final double[] maValues = new double[count];
        double maSum = 0.0;
        for (int i = 0; i < count; i++)
        {
            maSum += means[i];
            if (i < samples)
            {
                maValues[i] = maSum / (i + 1);
            }
            else
            {
                maSum -= means[i - samples];
                maValues[i] = maSum / samples;
            }
        }

        final int buckets = Math.min(maxBuckets, count);
        final double[] bucketSums = new double[buckets];
        final int[] bucketCounts = new int[buckets];

        // Anchor to [minTs, maxTs] so delayed-start items do not get flat-line padding before their first sample
        final long span = maxTs - minTs;
        for (int i = 0; i < count; i++)
        {
            final int b;
            if (span <= 0L)
            {
                b = Math.min(buckets - 1, (int) (((long) i * buckets) / count));
            }
            else
            {
                final long offset = Math.max(0L, timestamps[i] - minTs);
                b = (int) Math.min(buckets - 1L, (offset * buckets) / (span + 1L));
            }
            bucketSums[b] += maValues[i];
            bucketCounts[b]++;
        }

        final double[] bucketAvgs = new double[buckets];
        for (int b = 0; b < buckets; b++)
        {
            if (bucketCounts[b] > 0)
            {
                bucketAvgs[b] = bucketSums[b] / bucketCounts[b];
            }
        }

        // Linearly interpolate any empty interior buckets across quiet intervals (fall back to nearest edge)
        for (int b = 0; b < buckets; b++)
        {
            if (bucketCounts[b] == 0)
            {
                int leftIdx = b - 1;
                while (leftIdx >= 0 && bucketCounts[leftIdx] == 0)
                {
                    leftIdx--;
                }
                int rightIdx = b + 1;
                while (rightIdx < buckets && bucketCounts[rightIdx] == 0)
                {
                    rightIdx++;
                }
                if (leftIdx >= 0 && rightIdx < buckets)
                {
                    final double t = (double) (b - leftIdx) / (rightIdx - leftIdx);
                    bucketAvgs[b] = bucketAvgs[leftIdx] + t * (bucketAvgs[rightIdx] - bucketAvgs[leftIdx]);
                }
                else if (leftIdx >= 0)
                {
                    bucketAvgs[b] = bucketAvgs[leftIdx];
                }
                else if (rightIdx < buckets)
                {
                    bucketAvgs[b] = bucketAvgs[rightIdx];
                }
            }
        }

        final List<Double> result = new ArrayList<>(buckets);
        for (int b = 0; b < buckets; b++)
        {
            result.add(Double.valueOf(round1(bucketAvgs[b])));
        }
        return result;
    }

    // =========================================================================
    // Section Builders
    // =========================================================================

    private Map<String, Object> parseTestReportConfig(final Element cfgEl)
    {
        final Map<String, Object> out = new LinkedHashMap<>();

        // 1. runtimePercentiles
        final List<String> percentiles = readStringList(firstChildElement(cfgEl, "runtimePercentiles"), "string");
        out.put("runtimePercentiles", percentiles);

        // 2. runtimeIntervals
        final List<Map<String, Object>> intervals = new ArrayList<>();
        final Element intervalsEl = firstChildElement(cfgEl, "runtimeIntervals");
        if (intervalsEl != null)
        {
            for (final Element ivEl : childElements(intervalsEl, "interval"))
            {
                final String fromAttr = ivEl.getAttribute("from").trim();
                final String toAttr = ivEl.getAttribute("to").trim();
                final Map<String, Object> ivMap = new LinkedHashMap<>(2);
                ivMap.put("from", Long.valueOf(parseLongSafe(fromAttr, 0L)));
                ivMap.put("to", toAttr.isEmpty() ? null : Long.valueOf(parseLongSafe(toAttr, 0L)));
                intervals.add(ivMap);
            }
        }
        out.put("runtimeIntervals", intervals);

        // 3. requestTableColorization
        final List<Map<String, Object>> colorizationList = new ArrayList<>();
        final Element rtcEl = firstChildElement(cfgEl, "requestTableColorization");
        if (rtcEl != null)
        {
            for (final Element colEl : childElements(rtcEl, "colorization"))
            {
                final String groupName = colEl.getAttribute("groupName").trim();
                if (groupName.isEmpty())
                {
                    continue;
                }
                final List<Map<String, Object>> rules = new ArrayList<>();
                final Element rulesEl = firstChildElement(colEl, "rules");
                if (rulesEl != null)
                {
                    // Skip <null/> entries intentionally serialized by XStream for omitted rules
                    for (final Element ruleEl : childElements(rulesEl, "rule"))
                    {
                        final String type = ruleEl.getAttribute("type").trim();
                        if (type.isEmpty())
                        {
                            continue;
                        }
                        final Map<String, Object> rMap = new LinkedHashMap<>(5);
                        rMap.put("id", ruleEl.getAttribute("id"));
                        rMap.put("type", type);
                        rMap.put("from", Double.valueOf(parseDoubleSafe(ruleEl.getAttribute("from"), 0.0)));
                        rMap.put("target", Double.valueOf(parseDoubleSafe(ruleEl.getAttribute("target"), 0.0)));
                        rMap.put("to", Double.valueOf(parseDoubleSafe(ruleEl.getAttribute("to"), 0.0)));
                        rules.add(rMap);
                    }
                }
                final Map<String, Object> groupEntry = new LinkedHashMap<>(2);
                groupEntry.put("groupName", groupName);
                groupEntry.put("rules", rules);
                colorizationList.add(groupEntry);
            }
        }
        out.put("requestTableColorization", colorizationList);

        return out;
    }

    private Map<String, Object> buildTimerSectionBundle(
        final Element sectionEl, final String itemTagName, final Element summaryTimerEl, final File sectionChartsDir,
        final boolean includeApdex, final boolean isRequests, final boolean hasColorizationConfig,
        final long startTimeMs, final long endTimeMs)
    {
        final List<Element> itemEls = sectionEl != null ? childElements(sectionEl, itemTagName) : Collections.emptyList();
        final Map<String, Object> bundle = new LinkedHashMap<>();

        if (itemEls.isEmpty())
        {
            bundle.put("totals", summaryTimerEl != null
                ? parseTimerElement(summaryTimerEl, true, null, includeApdex, isRequests, false, startTimeMs, endTimeMs)
                : Collections.emptyMap());
            bundle.put("items", Collections.emptyList());
            return bundle;
        }

        final List<Map<String, Object>> items = new ArrayList<>(itemEls.size() + 1);
        Map<String, Object> totals = null;
        if (summaryTimerEl != null)
        {
            totals = parseTimerElement(
                summaryTimerEl, true, sectionChartsDir, includeApdex, isRequests, false, startTimeMs, endTimeMs);
            items.add(totals);
        }

        for (final Element itemEl : itemEls)
        {
            items.add(parseTimerElement(
                itemEl, false, sectionChartsDir, includeApdex, isRequests, hasColorizationConfig, startTimeMs, endTimeMs));
        }

        bundle.put("totals", totals != null ? totals : items.get(0));
        bundle.put("items", items);
        return bundle;
    }

    private Map<String, Object> parseTimerElement(
        final Element el, final boolean isAggregate, final File sectionChartsDir,
        final boolean includeApdex, final boolean isRequests, final boolean hasColorizationConfig,
        final long startTimeMs, final long endTimeMs)
    {
        final Map<String, Object> item = new LinkedHashMap<>();
        final String name = childText(el, "name", "");
        final String chartBase = com.xceptance.common.io.FileUtils.convertIllegalCharsInFileName(name);

        item.put("key", name);
        item.put("name", name);
        item.put("isAggregate", Boolean.valueOf(isAggregate));
        item.put("chartBase", chartBase);

        final List<String> labels = splitLabels(childText(el, "labels", null));
        item.put("labels", labels);

        item.put("count", Long.valueOf(childLong(el, "count", 0L)));
        item.put("rate", Double.valueOf(childDouble(el, "countPerSecond", 0.0)));
        item.put("cpm", Double.valueOf(childDouble(el, "countPerMinute", 0.0)));
        item.put("cph", Double.valueOf(childDouble(el, "countPerHour", 0.0)));
        item.put("cpd", Double.valueOf(childDouble(el, "countPerDay", 0.0)));
        item.put("errors", Long.valueOf(childLong(el, "errors", 0L)));
        item.put("errPct", Double.valueOf(childDouble(el, "errorPercentage", 0.0)));
        item.put("events", Long.valueOf(childLong(el, "events", 0L)));
        item.put("mean", Double.valueOf(childDouble(el, "mean", 0.0)));
        item.put("min", Long.valueOf(childLong(el, "min", 0L)));
        item.put("max", Long.valueOf(childLong(el, "max", 0L)));
        item.put("dev", Double.valueOf(childDouble(el, "deviation", 0.0)));
        item.put("median", Double.valueOf(childDouble(el, "median", 0.0)));

        final Map<String, Double> percentiles = parsePercentilesMap(firstChildElement(el, "percentiles"));
        item.put("percentiles", percentiles);

        // Runtime segmentation (present on requests and any timer that carries countPerInterval)
        final Element cpiEl = firstChildElement(el, "countPerInterval");
        final Element ppiEl = firstChildElement(el, "percentagePerInterval");
        if (cpiEl != null)
        {
            final List<Long> countsPerInterval = new ArrayList<>();
            for (final Element intEl : childElements(cpiEl))
            {
                countsPerInterval.add(Long.valueOf(parseLongSafe(intEl.getTextContent(), 0L)));
            }
            final List<Double> pctsPerInterval = new ArrayList<>();
            if (ppiEl != null)
            {
                for (final Element bdEl : childElements(ppiEl))
                {
                    pctsPerInterval.add(Double.valueOf(parseDoubleSafe(bdEl.getTextContent(), 0.0)));
                }
            }
            final List<Map<String, Object>> segmentation = new ArrayList<>(countsPerInterval.size());
            for (int i = 0; i < countsPerInterval.size(); i++)
            {
                final Map<String, Object> seg = new LinkedHashMap<>(2);
                seg.put("count", countsPerInterval.get(i));
                seg.put("pct", i < pctsPerInterval.size() ? pctsPerInterval.get(i) : Double.valueOf(0.0));
                segmentation.add(seg);
            }
            item.put("countPerInterval", countsPerInterval);
            item.put("percentagePerInterval", pctsPerInterval);
            item.put("segmentation", segmentation);
        }

        if (includeApdex)
        {
            final Element apdexEl = firstChildElement(el, "apdex");
            if (apdexEl != null)
            {
                final String valStr = childText(apdexEl, "value", "").trim();
                final String longValStr = childText(apdexEl, "longValue", "").trim();
                if (!valStr.isEmpty())
                {
                    final Map<String, Object> apdexMap = new LinkedHashMap<>(2);
                    apdexMap.put("value", Double.valueOf(parseDoubleSafe(valStr, 0.0)));
                    apdexMap.put("longValue", longValStr);
                    item.put("apdex", apdexMap);
                }
            }
        }

        if (isRequests)
        {
            if (!isAggregate)
            {
                final Element cgEl = firstChildElement(el, "colorizationGroupName");
                if (cgEl != null)
                {
                    final String cgName = cgEl.getTextContent().trim();
                    item.put("colorizationGroupName", cgName.isEmpty() ? null : cgName);
                    item.put("colorizationGroup", cgName.isEmpty() ? null : cgName);
                }
                else if (hasColorizationConfig)
                {
                    // Request matched multiple colorization groups in RequestsReportProvider -> intentionally uncolorized
                    item.put("colorizationGroupName", null);
                    item.put("colorizationGroup", null);
                }
            }

            // Bytes sent / received
            final Element bsEl = firstChildElement(el, "bytesSent");
            if (bsEl != null)
            {
                item.put("sentMean", Double.valueOf(childDouble(bsEl, "mean", 0.0)));
                item.put("sentMin", Long.valueOf(childLong(bsEl, "min", 0L)));
                item.put("sentMax", Long.valueOf(childLong(bsEl, "max", 0L)));
                item.put("sentRate", Double.valueOf(childDouble(bsEl, "countPerSecond", 0.0)));
                item.put("sentCpm", Double.valueOf(childDouble(bsEl, "countPerMinute", 0.0)));
                item.put("sentCph", Double.valueOf(childDouble(bsEl, "countPerHour", 0.0)));
                item.put("sentCpd", Double.valueOf(childDouble(bsEl, "countPerDay", 0.0)));
                item.put("sentTotal", Long.valueOf(childLong(bsEl, "totalCount", 0L)));
            }
            final Element brEl = firstChildElement(el, "bytesReceived");
            if (brEl != null)
            {
                item.put("recvMean", Double.valueOf(childDouble(brEl, "mean", 0.0)));
                item.put("recvMin", Long.valueOf(childLong(brEl, "min", 0L)));
                item.put("recvMax", Long.valueOf(childLong(brEl, "max", 0L)));
                item.put("recvRate", Double.valueOf(childDouble(brEl, "countPerSecond", 0.0)));
                item.put("recvCpm", Double.valueOf(childDouble(brEl, "countPerMinute", 0.0)));
                item.put("recvCph", Double.valueOf(childDouble(brEl, "countPerHour", 0.0)));
                item.put("recvCpd", Double.valueOf(childDouble(brEl, "countPerDay", 0.0)));
                item.put("recvTotal", Long.valueOf(childLong(brEl, "totalCount", 0L)));
            }

            // Network socket timing breakdown
            putSubStat(item, firstChildElement(el, "dnsTime"), "dns", "dnsMin", "dnsMax");
            putSubStat(item, firstChildElement(el, "connectTime"), "conn", "connMin", "connMax");
            putSubStat(item, firstChildElement(el, "sendTime"), "send", "sendMin", "sendMax");
            putSubStat(item, firstChildElement(el, "serverBusyTime"), "busy", "busyMin", "busyMax");
            putSubStat(item, firstChildElement(el, "receiveTime"), "recv", "recvTimeMin", "recvTimeMax");
            putSubStat(item, firstChildElement(el, "timeToFirstBytes"), "ttfb", "ttfbMin", "ttfbMax");
            putSubStat(item, firstChildElement(el, "timeToLastBytes"), "ttlb", "ttlbMin", "ttlbMax");

            // Distinct & exemplary URLs
            final Element urlsEl = firstChildElement(el, "urls");
            if (urlsEl != null)
            {
                item.put("distinctUrls", Long.valueOf(childLong(urlsEl, "total", 0L)));
                item.put("sampleUrls", readStringList(firstChildElement(urlsEl, "list"), "string"));
            }
            else
            {
                item.put("distinctUrls", null);
                item.put("sampleUrls", Collections.emptyList());
            }
        }

        // Sparkline downsampling from charts/<dir>/<chartBase>.json
        final File chartJson = sectionChartsDir != null ? new File(sectionChartsDir, chartBase + ".json") : null;
        item.put("spark", downsampleChartJson(chartJson, SPARKLINE_BUCKETS, startTimeMs, endTimeMs));

        return item;
    }

    private void putSubStat(
        final Map<String, Object> target, final Element statEl,
        final String meanKey, final String minKey, final String maxKey)
    {
        if (statEl == null)
        {
            return;
        }
        target.put(meanKey, Double.valueOf(childDouble(statEl, "mean", 0.0)));
        if (minKey != null)
        {
            target.put(minKey, Long.valueOf(childLong(statEl, "min", 0L)));
        }
        if (maxKey != null)
        {
            target.put(maxKey, Long.valueOf(childLong(statEl, "max", 0L)));
        }
    }

    private Map<String, Map<String, Object>> buildRequestLookupByName(final Map<String, Object> reqBundle)
    {
        final Map<String, Map<String, Object>> map = new HashMap<>();
        final Object rawItems = reqBundle.get("items");
        if (rawItems instanceof List<?> list)
        {
            for (final Object o : list)
            {
                if (o instanceof Map<?, ?> m)
                {
                    @SuppressWarnings("unchecked")
                    final Map<String, Object> item = (Map<String, Object>) m;
                    if (!Boolean.TRUE.equals(item.get("isAggregate")))
                    {
                        final Object name = item.get("name");
                        if (name != null)
                        {
                            map.put(name.toString(), item);
                        }
                    }
                }
            }
        }
        return map;
    }

    private Map<String, Object> buildSlowestRequestsBundle(
        final Element slowestEl, final Map<String, Map<String, Object>> reqLookupByName)
    {
        final List<Map<String, Object>> items = new ArrayList<>();
        if (slowestEl != null)
        {
            int rank = 1;
            for (final Element reqEl : childElements(slowestEl, "request"))
            {
                final Map<String, Object> row = new LinkedHashMap<>();
                final String name = childText(reqEl, "name", "");
                row.put("rank", Integer.valueOf(rank++));
                row.put("name", name);
                row.put("runtime", Long.valueOf(childLong(reqEl, "runtime", 0L)));

                final Map<String, Object> baseReq = reqLookupByName.get(name);
                if (baseReq != null)
                {
                    row.put("baseMean", baseReq.get("mean"));
                    @SuppressWarnings("unchecked")
                    final Map<String, Double> pcts = (Map<String, Double>) baseReq.get("percentiles");
                    if (pcts != null && !pcts.isEmpty())
                    {
                        row.put("basePercentiles", pcts);
                        if (pcts.get("p95") != null)
                        {
                            row.put("baseP95", pcts.get("p95"));
                        }
                    }
                }

                row.put("time", childText(reqEl, "time", ""));
                row.put("requestId", childText(reqEl, "requestId", ""));
                row.put("method", childText(reqEl, "httpMethod", ""));
                row.put("url", childText(reqEl, "url", ""));
                row.put("status", Integer.valueOf((int) childLong(reqEl, "responseCode", 0L)));
                row.put("contentType", childText(reqEl, "contentType", ""));
                row.put("bytesSent", Long.valueOf(childLong(reqEl, "bytesSent", 0L)));
                row.put("bytesReceived", Long.valueOf(childLong(reqEl, "bytesReceived", 0L)));
                row.put("dns", Long.valueOf(childLong(reqEl, "dnsTime", 0L)));
                row.put("conn", Long.valueOf(childLong(reqEl, "connectTime", 0L)));
                row.put("send", Long.valueOf(childLong(reqEl, "sendTime", 0L)));
                row.put("busy", Long.valueOf(childLong(reqEl, "serverBusyTime", 0L)));
                row.put("recv", Long.valueOf(childLong(reqEl, "receiveTime", 0L)));
                row.put("ttfb", Long.valueOf(childLong(reqEl, "timeToFirstBytes", 0L)));
                row.put("ttlb", Long.valueOf(childLong(reqEl, "timeToLastBytes", 0L)));
                row.put("usedIp", childText(reqEl, "usedIpAddress", ""));
                row.put("ips", readStringList(firstChildElement(reqEl, "ipAddresses"), "string"));
                items.add(row);
            }
        }
        final Map<String, Object> out = new LinkedHashMap<>(1);
        out.put("items", items);
        return out;
    }

    private List<Map<String, Object>> parseCustomLogs(final Element customLogsEl, final File reportDir)
    {
        final List<Map<String, Object>> logs = new ArrayList<>();
        final Set<String> seenPaths = new LinkedHashSet<>();

        if (customLogsEl != null)
        {
            for (final Element lgEl : childElements(customLogsEl, "customLog"))
            {
                final String scope = childText(lgEl, "scope", "");
                final String path = childText(lgEl, "path", "").trim();
                final long size = childLong(lgEl, "size", 0L);
                final String fileName = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
                final Map<String, Object> entry = new LinkedHashMap<>(4);
                entry.put("name", fileName.isEmpty() ? scope : fileName);
                entry.put("scope", scope);
                entry.put("path", path);
                entry.put("size", Long.valueOf(size));
                entry.put("sizeBytes", Long.valueOf(size));
                logs.add(entry);
                if (!path.isEmpty())
                {
                    seenPaths.add(path);
                }
            }
        }

        final File logsDir = new File(reportDir, "custom_data_logs");
        if (logsDir.isDirectory())
        {
            final File[] files = logsDir.listFiles(File::isFile);
            if (files != null)
            {
                Arrays.sort(files, Comparator.comparing(File::getName));
                for (final File f : files)
                {
                    final String relPath = "custom_data_logs/" + f.getName();
                    if (!seenPaths.contains(relPath))
                    {
                        final Map<String, Object> entry = new LinkedHashMap<>(4);
                        entry.put("name", f.getName());
                        entry.put("scope", StringUtils.substringBeforeLast(f.getName(), "."));
                        entry.put("path", relPath);
                        entry.put("size", Long.valueOf(f.length()));
                        entry.put("sizeBytes", Long.valueOf(f.length()));
                        logs.add(entry);
                    }
                }
            }
        }

        return logs;
    }

    private Map<String, Object> buildCustomValuesBundle(
        final Element cvRootEl, final File cvChartsDir, final List<Map<String, Object>> customLogs,
        final long startTimeMs, final long endTimeMs)
    {
        final List<Map<String, Object>> items = new ArrayList<>();
        if (cvRootEl != null)
        {
            for (final Element cvEl : childElements(cvRootEl, "customValue"))
            {
                final Map<String, Object> item = new LinkedHashMap<>();
                final String name = childText(cvEl, "name", "");
                final String chartFilename = childText(cvEl, "chartFilename", "").trim();
                final String chartBase = !chartFilename.isEmpty()
                    ? chartFilename
                    : com.xceptance.common.io.FileUtils.convertIllegalCharsInFileName(name);

                item.put("key", name);
                item.put("name", name);
                item.put("description", childText(cvEl, "description", ""));
                item.put("isAggregate", Boolean.FALSE);
                item.put("chartBase", chartBase);
                item.put("labels", splitLabels(childText(cvEl, "labels", null)));
                item.put("count", Long.valueOf(childLong(cvEl, "count", 0L)));
                item.put("rate", Double.valueOf(childDouble(cvEl, "countPerSecond", 0.0)));
                item.put("cpm", Double.valueOf(childDouble(cvEl, "countPerMinute", 0.0)));
                item.put("cph", Double.valueOf(childDouble(cvEl, "countPerHour", 0.0)));
                item.put("cpd", Double.valueOf(childDouble(cvEl, "countPerDay", 0.0)));
                item.put("mean", Double.valueOf(childDouble(cvEl, "mean", 0.0)));
                item.put("min", Double.valueOf(childDouble(cvEl, "min", 0.0)));
                item.put("max", Double.valueOf(childDouble(cvEl, "max", 0.0)));
                final double stdDev = childDouble(cvEl, "standardDeviation", Double.NaN);
                item.put("dev", Double.valueOf(Double.isNaN(stdDev) ? childDouble(cvEl, "deviation", 0.0) : stdDev));
                item.put("percentiles", parsePercentilesMap(firstChildElement(cvEl, "percentiles")));

                final File chartJson = cvChartsDir != null ? new File(cvChartsDir, chartBase + ".json") : null;
                item.put("spark", downsampleChartJson(chartJson, SPARKLINE_BUCKETS, startTimeMs, endTimeMs));
                items.add(item);
            }
        }
        final Map<String, Object> out = new LinkedHashMap<>(2);
        out.put("items", items);
        out.put("logs", customLogs);
        return out;
    }

    private Map<String, Object> buildAgentsBundle(final Element agentsEl, final Element summaryAgentsEl)
    {
        final List<Element> agentNodes = agentsEl != null ? childElements(agentsEl, "agent") : Collections.emptyList();
        final Map<String, Object> out = new LinkedHashMap<>(2);

        if (agentNodes.isEmpty())
        {
            out.put("totals", Collections.emptyMap());
            out.put("items", Collections.emptyList());
            return out;
        }

        final List<Map<String, Object>> items = new ArrayList<>(agentNodes.size() + 1);
        Map<String, Object> totals = null;
        if (summaryAgentsEl != null)
        {
            totals = parseAgentElement(summaryAgentsEl, true);
            items.add(totals);
        }
        for (final Element agEl : agentNodes)
        {
            items.add(parseAgentElement(agEl, false));
        }

        out.put("totals", totals != null ? totals : items.get(0));
        out.put("items", items);
        return out;
    }

    private Map<String, Object> parseAgentElement(final Element agEl, final boolean isAggregate)
    {
        final Map<String, Object> item = new LinkedHashMap<>();
        final String name = childText(agEl, "name", isAggregate ? "All Agents" : "");
        item.put("key", name);
        item.put("name", name);
        item.put("isAggregate", Boolean.valueOf(isAggregate));
        item.put("chartBase", com.xceptance.common.io.FileUtils.convertIllegalCharsInFileName(name));
        item.put("totalTx", Long.valueOf(childLong(agEl, "transactions", 0L)));
        item.put("errors", Long.valueOf(childLong(agEl, "transactionErrors", 0L)));
        item.put("errPct", Double.valueOf(childDouble(agEl, "transactionErrorPercentage", 0.0)));

        final Element totalCpuEl = firstChildElement(agEl, "totalCpuUsage");
        item.put("totalCpuMean", Double.valueOf(childDouble(totalCpuEl, "mean", 0.0)));
        item.put("totalCpuMax", Double.valueOf(childDouble(totalCpuEl, "max", 0.0)));

        final Element cpuEl = firstChildElement(agEl, "cpuUsage");
        item.put("agentCpuMean", Double.valueOf(childDouble(cpuEl, "mean", 0.0)));
        item.put("agentCpuMax", Double.valueOf(childDouble(cpuEl, "max", 0.0)));

        item.put("minorGcCount", Long.valueOf(childLong(agEl, "minorGcCount", 0L)));
        item.put("minorGcTime", Long.valueOf(childLong(agEl, "minorGcTime", 0L)));
        item.put("minorGcCpu", Double.valueOf(childDouble(agEl, "minorGcCpuUsage", 0.0)));

        item.put("fullGcCount", Long.valueOf(childLong(agEl, "fullGcCount", 0L)));
        item.put("fullGcTime", Long.valueOf(childLong(agEl, "fullGcTime", 0L)));
        item.put("fullGcCpu", Double.valueOf(childDouble(agEl, "fullGcCpuUsage", 0.0)));
        return item;
    }

    private Map<String, Object> buildWebVitalsBundle(final Element wvListEl)
    {
        final List<Map<String, Object>> items = new ArrayList<>();
        if (wvListEl != null)
        {
            for (final Element wvEl : childElements(wvListEl, "webVitals"))
            {
                final Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("name", childText(wvEl, "name", ""));
                for (final String metric : List.of("lcp", "cls", "inp", "fcp", "fid", "ttfb"))
                {
                    final Element mEl = firstChildElement(wvEl, metric);
                    if (mEl != null)
                    {
                        final String scoreTxt = childText(mEl, "score", "").trim();
                        if (!scoreTxt.isEmpty())
                        {
                            final Map<String, Object> mMap = new LinkedHashMap<>(5);
                            mMap.put("score", Double.valueOf(parseDoubleSafe(scoreTxt, 0.0)));
                            mMap.put("rating", childText(mEl, "rating", ""));
                            mMap.put("goodCount", Long.valueOf(childLong(mEl, "goodCount", 0L)));
                            mMap.put("improveCount", Long.valueOf(childLong(mEl, "improveCount", 0L)));
                            mMap.put("poorCount", Long.valueOf(childLong(mEl, "poorCount", 0L)));
                            entry.put(metric, mMap);
                        }
                    }
                }
                items.add(entry);
            }
        }
        final Map<String, Object> out = new LinkedHashMap<>(1);
        out.put("items", items);
        return out;
    }

    private Map<String, Object> buildEventsBundle(final Element eventsEl)
    {
        final List<Map<String, Object>> items = new ArrayList<>();
        if (eventsEl != null)
        {
            for (final Element evEl : childElements(eventsEl, "event"))
            {
                final Map<String, Object> ev = new LinkedHashMap<>(5);
                ev.put("name", childText(evEl, "name", ""));
                ev.put("testCase", childText(evEl, "testCaseName", ""));
                ev.put("totalCount", Long.valueOf(childLong(evEl, "totalCount", 0L)));
                ev.put("droppedCount", Long.valueOf(childLong(evEl, "droppedCount", 0L)));

                final List<Map<String, Object>> messages = new ArrayList<>();
                final Element msgsEl = firstChildElement(evEl, "messages");
                if (msgsEl != null)
                {
                    for (final Element mEl : childElements(msgsEl, "message"))
                    {
                        final Map<String, Object> msg = new LinkedHashMap<>(2);
                        msg.put("count", Long.valueOf(childLong(mEl, "count", 0L)));
                        msg.put("info", childText(mEl, "info", ""));
                        messages.add(msg);
                    }
                }
                ev.put("messages", messages);
                items.add(ev);
            }
        }
        final Map<String, Object> out = new LinkedHashMap<>(1);
        out.put("items", items);
        return out;
    }

    private Map<String, Object> buildNetworkBundle(
        final Element root, final long bytesSent, final long bytesReceived, final long hits, final long durationSec)
    {
        final Map<String, Object> out = new LinkedHashMap<>();
        final double dur = durationSec > 0 ? durationSec : 1.0;

        out.put("bytesSent", Long.valueOf(bytesSent));
        out.put("bytesReceived", Long.valueOf(bytesReceived));
        out.put("sentPerSec", Double.valueOf(durationSec > 0 ? bytesSent / dur : 0.0));
        out.put("receivedPerSec", Double.valueOf(durationSec > 0 ? bytesReceived / dur : 0.0));
        out.put("hits", Long.valueOf(hits));
        out.put("reqPerSec", Double.valueOf(durationSec > 0 ? hits / dur : 0.0));
        out.put("durationSec", Long.valueOf(durationSec));

        // Hosts
        final List<Map<String, Object>> hosts = new ArrayList<>();
        final Element hostsEl = firstChildElement(root, "hosts");
        if (hostsEl != null)
        {
            for (final Element hEl : childElements(hostsEl, "host"))
            {
                final Map<String, Object> h = new LinkedHashMap<>(2);
                h.put("name", childText(hEl, "name", ""));
                h.put("count", Long.valueOf(childLong(hEl, "count", 0L)));
                hosts.add(h);
            }
            hosts.sort((a, b) -> Long.compare(((Number) b.get("count")).longValue(), ((Number) a.get("count")).longValue()));
        }
        out.put("hosts", hosts);

        // IPs
        final List<Map<String, Object>> ips = new ArrayList<>();
        final Element ipsEl = firstChildElement(root, "ips");
        if (ipsEl != null)
        {
            for (final Element ipEl : childElements(ipsEl, "ip"))
            {
                final Map<String, Object> ip = new LinkedHashMap<>(3);
                ip.put("ip", childText(ipEl, "ip", ""));
                ip.put("host", childText(ipEl, "host", ""));
                ip.put("count", Long.valueOf(childLong(ipEl, "count", 0L)));
                ips.add(ip);
            }
            ips.sort((a, b) -> Long.compare(((Number) b.get("count")).longValue(), ((Number) a.get("count")).longValue()));
        }
        out.put("ips", ips);

        // Request Methods
        final List<Map<String, Object>> methods = new ArrayList<>();
        final Element methodsEl = firstChildElement(root, "requestMethods");
        if (methodsEl != null)
        {
            for (final Element mEl : childElements(methodsEl, "requestMethod"))
            {
                final Map<String, Object> m = new LinkedHashMap<>(2);
                m.put("method", childText(mEl, "method", ""));
                m.put("count", Long.valueOf(childLong(mEl, "count", 0L)));
                methods.add(m);
            }
            methods.sort((a, b) -> Long.compare(((Number) b.get("count")).longValue(), ((Number) a.get("count")).longValue()));
        }
        out.put("methods", methods);

        // Response Codes
        final List<Map<String, Object>> responseCodes = new ArrayList<>();
        final Element codesEl = firstChildElement(root, "responseCodes");
        if (codesEl != null)
        {
            for (final Element cEl : childElements(codesEl, "responseCode"))
            {
                final Map<String, Object> c = new LinkedHashMap<>(3);
                c.put("code", Integer.valueOf((int) childLong(cEl, "code", 0L)));
                c.put("statusText", childText(cEl, "statusText", ""));
                c.put("count", Long.valueOf(childLong(cEl, "count", 0L)));
                responseCodes.add(c);
            }
            responseCodes.sort(Comparator.comparingInt(m -> ((Number) m.get("code")).intValue()));
        }
        out.put("responseCodes", responseCodes);

        // Content Types
        final List<Map<String, Object>> contentTypes = new ArrayList<>();
        final Element ctEl = firstChildElement(root, "contentTypes");
        if (ctEl != null)
        {
            for (final Element cEl : childElements(ctEl, "contentType"))
            {
                final Map<String, Object> c = new LinkedHashMap<>(2);
                final String typeStr = childText(cEl, "contentType", "").trim();
                c.put("contentType", typeStr.isEmpty() ? "(empty / unspecified)" : typeStr);
                c.put("count", Long.valueOf(childLong(cEl, "count", 0L)));
                contentTypes.add(c);
            }
            contentTypes.sort((a, b) -> Long.compare(((Number) b.get("count")).longValue(), ((Number) a.get("count")).longValue()));
        }
        out.put("contentTypes", contentTypes);

        return out;
    }

    private Map<String, Object> buildErrorsBundle(
        final Element errorsEl, final Element summaryEl, final List<Map<String, Object>> customLogs)
    {
        final Element sumTx = firstChildElement(summaryEl, "transactions");
        final Element sumAct = firstChildElement(summaryEl, "actions");
        final Element sumReq = firstChildElement(summaryEl, "requests");

        final Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("transactionErrors", Long.valueOf(childLong(sumTx, "errors", 0L)));
        summary.put("transactionCount", Long.valueOf(childLong(sumTx, "count", 0L)));
        summary.put("transactionErrPct", Double.valueOf(childDouble(sumTx, "errorPercentage", 0.0)));
        summary.put("actionErrors", Long.valueOf(childLong(sumAct, "errors", 0L)));
        summary.put("actionCount", Long.valueOf(childLong(sumAct, "count", 0L)));
        summary.put("requestErrors", Long.valueOf(childLong(sumReq, "errors", 0L)));
        summary.put("requestCount", Long.valueOf(childLong(sumReq, "count", 0L)));

        final String resultsPathPrefix = childText(errorsEl, "resultsPathPrefix", "").trim();

        // Request Error Overview Charts
        final List<Map<String, Object>> reqErrCharts = new ArrayList<>();
        final Element reqChartsEl = firstChildElement(errorsEl, "requestErrorOverviewCharts");
        if (reqChartsEl != null)
        {
            for (final Element cEl : childElements(reqChartsEl, "chart"))
            {
                final String id = childText(cEl, "id", "").trim();
                if (!id.isEmpty())
                {
                    final Map<String, Object> c = new LinkedHashMap<>(2);
                    c.put("id", id);
                    c.put("src", "charts/errors/r" + id + ".webp");
                    reqErrCharts.add(c);
                }
            }
        }

        // Transaction Error Overview Charts & lookup by title
        final List<Map<String, Object>> txErrCharts = new ArrayList<>();
        final Map<String, String> txChartIdByTitle = new HashMap<>();
        final Element txChartsEl = firstChildElement(errorsEl, "transactionErrorOverviewCharts");
        if (txChartsEl != null)
        {
            for (final Element cEl : childElements(txChartsEl, "chart"))
            {
                final String id = childText(cEl, "id", "").trim();
                final String title = childText(cEl, "title", "");
                if (!id.isEmpty())
                {
                    final Map<String, Object> c = new LinkedHashMap<>(3);
                    c.put("id", id);
                    c.put("title", title);
                    c.put("src", "charts/errors/t" + id + ".webp");
                    txErrCharts.add(c);
                    if (!title.isEmpty())
                    {
                        txChartIdByTitle.put(title, id);
                    }
                }
            }
        }

        // Error incidents & overviewByMessage
        final List<Map<String, Object>> incidents = new ArrayList<>();
        final Map<String, Map<String, Object>> byMessage = new LinkedHashMap<>();
        if (errorsEl != null)
        {
            for (final Element errEl : childElements(errorsEl, "error"))
            {
                final long cnt = childLong(errEl, "count", 0L);
                final String testCaseName = childText(errEl, "testCaseName", "");
                final String actionName = childText(errEl, "actionName", "");
                final String requestName = childText(errEl, "requestName", "");
                final String message = childText(errEl, "message", "");
                final String trace = childText(errEl, "trace", "");
                final String exClass = extractExceptionClass(trace, message);
                final String detailChartId = childText(errEl, "detailChartID", "0").trim();
                final String overviewChartId = txChartIdByTitle.get(message);
                final List<String> dirs = readStringList(firstChildElement(errEl, "directoryHints"), "string");

                final Map<String, Object> inc = new LinkedHashMap<>();
                inc.put("count", Long.valueOf(cnt));
                inc.put("testCaseName", testCaseName);
                inc.put("actionName", actionName);
                if (!requestName.isEmpty())
                {
                    inc.put("requestName", requestName);
                }
                inc.put("message", message);
                inc.put("exceptionClass", exClass);
                inc.put("trace", trace);
                if (!detailChartId.isEmpty() && !"0".equals(detailChartId))
                {
                    inc.put("detailChartId", detailChartId);
                }
                if (overviewChartId != null)
                {
                    inc.put("overviewChartId", overviewChartId);
                }
                inc.put("directories", dirs);
                incidents.add(inc);

                final String msgKey = message.isEmpty() ? "(No error message)" : message;
                Map<String, Object> agg = byMessage.get(msgKey);
                if (agg == null)
                {
                    agg = new LinkedHashMap<>(4);
                    agg.put("message", msgKey);
                    agg.put("count", Long.valueOf(0L));
                    agg.put("exceptionClass", exClass);
                    agg.put("chartId", overviewChartId);
                    byMessage.put(msgKey, agg);
                }
                agg.put("count", Long.valueOf(((Number) agg.get("count")).longValue() + cnt));
            }
        }

        incidents.sort((a, b) -> Long.compare(((Number) b.get("count")).longValue(), ((Number) a.get("count")).longValue()));
        final List<Map<String, Object>> overviewByMessage = new ArrayList<>(byMessage.values());
        overviewByMessage.sort((a, b) -> Long.compare(((Number) b.get("count")).longValue(), ((Number) a.get("count")).longValue()));

        final Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", summary);
        out.put("resultsPathPrefix", resultsPathPrefix);
        out.put("requestErrorCharts", reqErrCharts);
        out.put("transactionErrorCharts", txErrCharts);
        out.put("overviewByMessage", overviewByMessage);
        out.put("customLogs", customLogs);
        out.put("incidents", incidents);
        return out;
    }

    private Map<String, Object> buildExternalBundle(final Element externalEl)
    {
        final List<Map<String, Object>> sections = new ArrayList<>();
        if (externalEl != null)
        {
            for (final Element grEl : childElements(externalEl, "genericReport"))
            {
                final Map<String, Object> sec = new LinkedHashMap<>(4);
                sec.put("headline", childText(grEl, "headline", ""));
                sec.put("description", childText(grEl, "description", ""));

                final List<Map<String, Object>> tables = new ArrayList<>();
                final Element tablesEl = firstChildElement(grEl, "tables");
                if (tablesEl != null)
                {
                    for (final Element tblEl : childElements(tablesEl, "table"))
                    {
                        final Map<String, Object> tbl = new LinkedHashMap<>(3);
                        tbl.put("title", childText(tblEl, "title", ""));

                        final List<String> headers = new ArrayList<>();
                        final Element headRowEl = firstChildElement(tblEl, "headRow");
                        final Element headCellsEl = firstChildElement(headRowEl, "cells");
                        if (headCellsEl != null)
                        {
                            for (final Element cellEl : childElements(headCellsEl))
                            {
                                headers.add(cellEl.getTextContent());
                            }
                        }
                        tbl.put("headers", headers);

                        final List<List<Map<String, Object>>> rows = new ArrayList<>();
                        final Element bodyRowsEl = firstChildElement(tblEl, "bodyRows");
                        if (bodyRowsEl != null)
                        {
                            for (final Element rowEl : childElements(bodyRowsEl, "row"))
                            {
                                final List<Map<String, Object>> rowCells = new ArrayList<>();
                                final Element cellsEl = firstChildElement(rowEl, "cells");
                                if (cellsEl != null)
                                {
                                    for (final Element cellEl : childElements(cellsEl))
                                    {
                                        final String tag = cellEl.getTagName();
                                        final String valTxt = cellEl.getTextContent();
                                        final boolean isNumeric = NUMERIC_CELL_TAGS.contains(tag);
                                        final Map<String, Object> cellMap = new LinkedHashMap<>(2);
                                        if (isNumeric)
                                        {
                                            cellMap.put("value", Double.valueOf(parseDoubleSafe(valTxt, 0.0)));
                                            cellMap.put("numeric", Boolean.TRUE);
                                        }
                                        else
                                        {
                                            cellMap.put("value", valTxt != null ? valTxt : "");
                                            cellMap.put("numeric", Boolean.FALSE);
                                        }
                                        rowCells.add(cellMap);
                                    }
                                }
                                rows.add(rowCells);
                            }
                        }
                        tbl.put("rows", rows);
                        tables.add(tbl);
                    }
                }
                sec.put("tables", tables);

                final List<Map<String, String>> charts = new ArrayList<>();
                final Element chartNamesEl = firstChildElement(grEl, "chartFileNames");
                if (chartNamesEl != null)
                {
                    for (final String rawName : readStringList(chartNamesEl, "string"))
                    {
                        final String encoded = com.xceptance.common.io.FileUtils.convertIllegalCharsInFileName(rawName);
                        final Map<String, String> ch = new LinkedHashMap<>(2);
                        ch.put("title", rawName);
                        ch.put("src", "charts/external/" + encoded + ".webp");
                        charts.add(ch);
                    }
                }
                sec.put("charts", charts);
                sections.add(sec);
            }
        }
        final Map<String, Object> out = new LinkedHashMap<>(1);
        out.put("sections", sections);
        return out;
    }

    List<Map<String, Object>> parseLoadProfileItems(final Element lpEl)
    {
        return parseLoadProfile(lpEl).items;
    }

    private LoadProfileParsed parseLoadProfile(final Element lpEl)
    {
        final List<Map<String, Object>> items = new ArrayList<>();
        long sumUsers = 0L;
        boolean complexUsers = false;
        long sumIters = 0L;
        boolean anyIters = false;
        boolean complexIters = false;
        long sumArrival = 0L;
        boolean anyArrival = false;
        boolean complexArrival = false;
        long maxRampUp = 0L;
        long maxMeasurement = 0L;
        long maxShutdown = 0L;
        String commonThinkDisplay = null;
        boolean sameThink = true;

        if (lpEl != null)
        {
            for (final Element tcEl : childElements(lpEl, "testCase"))
            {
                final Map<String, Object> tc = new LinkedHashMap<>();
                final String userName = childText(tcEl, "userName", "");
                final String className = childText(tcEl, "testCaseClassName", "");
                final String usersStr = childText(tcEl, "numberOfUsers", "0").trim();
                final String itersStr = childText(tcEl, "numberOfIterations", "0").trim();
                final String arrivalStr = childText(tcEl, "arrivalRate", "0").trim();
                final long rampUp = childLong(tcEl, "rampUpPeriod", 0L);
                final long measurement = childLong(tcEl, "measurementPeriod", 0L);
                final long shutdown = childLong(tcEl, "shutdownPeriod", 0L);
                final long thinkTime = childLong(tcEl, "actionThinkTime", 0L);
                final long thinkTimeDev = childLong(tcEl, "actionThinkTimeDeviation", 0L);

                tc.put("name", !userName.isEmpty() ? userName : className);
                tc.put("userName", userName);
                tc.put("className", className);
                tc.put("users", usersStr);
                tc.put("usersPct", Double.valueOf(childDouble(tcEl, "numberOfUsersPercentage", 0.0)));
                tc.put("iterations", itersStr);
                tc.put("arrivalRate", arrivalStr);
                tc.put("arrivalPct", Double.valueOf(childDouble(tcEl, "arrivalRatePercentage", 0.0)));
                tc.put("rampUp", Long.valueOf(rampUp));
                tc.put("measurement", Long.valueOf(measurement));
                tc.put("shutdown", Long.valueOf(shutdown));
                tc.put("thinkTime", Long.valueOf(thinkTime));
                tc.put("thinkTimeDev", Long.valueOf(thinkTimeDev));
                items.add(tc);

                final Long parsedUsers = tryParsePlainCount(usersStr);
                if (parsedUsers != null)
                {
                    sumUsers += parsedUsers.longValue();
                }
                else
                {
                    complexUsers = true;
                    sumUsers += extractPeakNumber(usersStr);
                }

                if (!itersStr.isEmpty() && !"0".equals(itersStr))
                {
                    anyIters = true;
                    final Long parsedIters = tryParsePlainCount(itersStr);
                    if (parsedIters != null)
                    {
                        sumIters += parsedIters.longValue();
                    }
                    else
                    {
                        complexIters = true;
                    }
                }

                if (!arrivalStr.isEmpty() && !"0".equals(arrivalStr))
                {
                    anyArrival = true;
                    final Long parsedArr = tryParsePlainCount(arrivalStr);
                    if (parsedArr != null)
                    {
                        sumArrival += parsedArr.longValue();
                    }
                    else
                    {
                        complexArrival = true;
                        sumArrival += extractPeakNumber(arrivalStr);
                    }
                }

                if (rampUp > maxRampUp)
                {
                    maxRampUp = rampUp;
                }
                if (measurement > maxMeasurement)
                {
                    maxMeasurement = measurement;
                }
                if (shutdown > maxShutdown)
                {
                    maxShutdown = shutdown;
                }

                final String thisThink = formatThinkTime(thinkTime, thinkTimeDev);
                if (commonThinkDisplay == null)
                {
                    commonThinkDisplay = thisThink;
                }
                else if (!commonThinkDisplay.equals(thisThink))
                {
                    sameThink = false;
                }
            }
        }

        final Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("usersDisplay", (complexUsers ? "up to " : "") + String.format(Locale.US, "%,d", Long.valueOf(sumUsers)));
        totals.put("iterationsDisplay", !anyIters ? "–" : (complexIters ? "variable" : String.format(Locale.US, "%,d", Long.valueOf(sumIters))));
        totals.put("arrivalDisplay", !anyArrival ? "0" : ((complexArrival ? "up to " : "") + String.format(Locale.US, "%,d", Long.valueOf(sumArrival))));
        totals.put("rampUpDisplay", formatDurationHms(maxRampUp));
        totals.put("measurementDisplay", formatDurationHms(maxMeasurement));
        totals.put("shutdownDisplay", formatDurationHms(maxShutdown));
        totals.put("thinkTimeDisplay", items.isEmpty() ? "—" : (sameThink ? commonThinkDisplay : "mixed"));

        return new LoadProfileParsed(items, totals);
    }

    private Map<String, Object> buildConfigBundle(
        final List<Map<String, String>> propertiesList, final LoadProfileParsed loadProfile, final Element configEl,
        final String productName, final String productVersion, final String revisionNumber, final String buildDate, final String vendorName)
    {
        final Map<String, Object> out = new LinkedHashMap<>();
        out.put("properties", propertiesList);
        out.put("loadProfile", loadProfile.items);
        out.put("loadProfileTotals", loadProfile.totals);
        out.put("customJvmArgs", readStringList(firstChildElement(configEl, "customJvmArgs"), "string"));

        final Map<String, Object> version = new LinkedHashMap<>(5);
        version.put("productName", productName);
        version.put("version", productVersion);
        version.put("revisionNumber", revisionNumber);
        version.put("buildDate", buildDate);
        version.put("vendorName", vendorName);
        out.put("version", version);

        final Element rgCfgEl = firstChildElement(configEl, "reportGeneratorConfiguration");
        final Map<String, Object> rgCfg = new LinkedHashMap<>();
        if (rgCfgEl != null)
        {
            rgCfg.put("timeZoneLabel", childText(rgCfgEl, "timeZoneLabel", "UTC"));
            rgCfg.put("timeZoneOffset", Long.valueOf(childLong(rgCfgEl, "timeZoneOffset", 0L)));
            rgCfg.put("slowestRequestsTotal", Long.valueOf(childLong(rgCfgEl, "slowestRequestsTotal", 0L)));
            rgCfg.put("slowestRequestsPerBucket", Long.valueOf(childLong(rgCfgEl, "slowestRequestsPerBucket", 0L)));
            rgCfg.put("slowestRequestsMinRuntime", Long.valueOf(childLong(rgCfgEl, "slowestRequestsMinRuntime", 0L)));
            rgCfg.put("slowestRequestsMaxRuntime", Long.valueOf(childLong(rgCfgEl, "slowestRequestsMaxRuntime", 0L)));
            rgCfg.put("dynamicChartsEnabled", Boolean.valueOf("true".equalsIgnoreCase(childText(rgCfgEl, "dynamicChartsEnabled", "true"))));
        }
        out.put("reportGeneratorConfig", rgCfg);

        return out;
    }

    private Map<String, Object> buildOverviewBundle(
        final String startTimeStr, final String endTimeStr, final long durationSec,
        final long bytesSent, final long bytesReceived, final long hits,
        final List<String> comments, final Map<String, Object> scorecardSummary,
        final Element summaryEl, final int agentCount,
        final Map<String, Object> txBundle, final Map<String, Object> errorsBundle,
        final Map<String, Object> networkBundle, final LoadProfileParsed loadProfile)
    {
        final Map<String, Object> out = new LinkedHashMap<>();

        final Map<String, Object> general = new LinkedHashMap<>(6);
        general.put("startTime", startTimeStr);
        general.put("endTime", endTimeStr);
        general.put("durationSec", Long.valueOf(durationSec));
        general.put("bytesSent", Long.valueOf(bytesSent));
        general.put("bytesReceived", Long.valueOf(bytesReceived));
        general.put("hits", Long.valueOf(hits));
        out.put("general", general);
        out.put("comments", comments);
        out.put("scorecard", scorecardSummary);

        // Summary matrix tiers + agents summary
        final Map<String, Object> summary = new LinkedHashMap<>();
        for (final String tier : List.of("transactions", "actions", "requests", "pageLoadTimings", "customTimers"))
        {
            final Element tierEl = firstChildElement(summaryEl, tier);
            summary.put(tier, tierEl != null
                ? parseTimerElement(tierEl, true, null, "actions".equals(tier), false, false, 0L, 0L)
                : Collections.emptyMap());
        }
        final Element sumAgentsEl = firstChildElement(summaryEl, "agents");
        final Map<String, Object> agSummary = sumAgentsEl != null ? parseAgentElement(sumAgentsEl, true) : new LinkedHashMap<>();
        agSummary.put("count", Integer.valueOf(agentCount));
        summary.put("agents", agSummary);
        out.put("summary", summary);

        // Top transactions sorted by cph descending
        final List<Map<String, Object>> topTx = new ArrayList<>();
        final Object rawTxItems = txBundle.get("items");
        if (rawTxItems instanceof List<?> txList)
        {
            for (final Object o : txList)
            {
                if (o instanceof Map<?, ?> m)
                {
                    @SuppressWarnings("unchecked")
                    final Map<String, Object> item = (Map<String, Object>) m;
                    if (!Boolean.TRUE.equals(item.get("isAggregate")))
                    {
                        final Map<String, Object> entry = new LinkedHashMap<>(5);
                        entry.put("name", item.get("name"));
                        entry.put("cph", item.get("cph"));
                        entry.put("count", item.get("count"));
                        entry.put("mean", item.get("mean"));
                        entry.put("errors", item.get("errors"));
                        topTx.add(entry);
                    }
                }
            }
            topTx.sort((a, b) -> Double.compare(((Number) b.get("cph")).doubleValue(), ((Number) a.get("cph")).doubleValue()));
        }
        out.put("topTransactions", topTx.size() > 10 ? new ArrayList<>(topTx.subList(0, 10)) : topTx);

        // Root-cause diagnostics (top error messages + non-2xx/3xx HTTP response codes)
        final List<Map<String, Object>> diagnostics = new ArrayList<>();
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> errByMsg = (List<Map<String, Object>>) errorsBundle.get("overviewByMessage");
        if (errByMsg != null)
        {
            for (final Map<String, Object> e : errByMsg)
            {
                if (diagnostics.size() >= 4)
                {
                    break;
                }
                final long c = ((Number) e.getOrDefault("count", Long.valueOf(0L))).longValue();
                final Map<String, Object> d = new LinkedHashMap<>(4);
                d.put("label", String.valueOf(e.get("message")));
                d.put("count", Long.valueOf(c));
                d.put("detail", String.format(Locale.US, "%,d tx error%s", Long.valueOf(c), c == 1 ? "" : "s"));
                d.put("severity", "error");
                diagnostics.add(d);
            }
        }
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> respCodes = (List<Map<String, Object>>) networkBundle.get("responseCodes");
        if (respCodes != null)
        {
            final List<Map<String, Object>> badCodes = new ArrayList<>();
            for (final Map<String, Object> rc : respCodes)
            {
                final int code = ((Number) rc.getOrDefault("code", Integer.valueOf(200))).intValue();
                if (code == 0 || code >= 400)
                {
                    badCodes.add(rc);
                }
            }
            badCodes.sort((a, b) -> Long.compare(((Number) b.get("count")).longValue(), ((Number) a.get("count")).longValue()));
            for (final Map<String, Object> rc : badCodes)
            {
                if (diagnostics.size() >= 6)
                {
                    break;
                }
                final int code = ((Number) rc.get("code")).intValue();
                final long cnt = ((Number) rc.get("count")).longValue();
                final String statusText = String.valueOf(rc.getOrDefault("statusText", "")).trim();
                final Map<String, Object> d = new LinkedHashMap<>(4);
                d.put("label", code == 0 ? "HTTP 0 (Connection / Timeout)" : ("HTTP " + code + (statusText.isEmpty() ? "" : " " + statusText)));
                d.put("count", Long.valueOf(cnt));
                d.put("detail", String.format(Locale.US, "%,d resp", Long.valueOf(cnt)));
                d.put("severity", code >= 500 || code == 0 ? "error" : "warn");
                diagnostics.add(d);
            }
        }
        out.put("diagnostics", diagnostics);
        out.put("loadProfile", loadProfile.items);
        out.put("loadProfileTotals", loadProfile.totals);

        return out;
    }

    // =========================================================================
    // Executive Summary Bundle
    // =========================================================================

    private static final int EXECUTIVE_CHART_BUCKETS = 60;

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildExecutiveBundle(
        final Element summaryEl, final File chartsDir, final long startTimeMs, final long endTimeMs,
        final Map<String, Object> txBundle, final Map<String, Object> actBundle, final Map<String, Object> reqBundle,
        final Map<String, Object> errorsBundle, final Map<String, Object> eventsBundle,
        final Map<String, Object> networkBundle, final Map<String, Object> agentsBundle,
        final Map<String, Object> webVitalsBundle, final LoadProfileParsed loadProfile,
        final Map<String, Object> fullScorecard, final Map<String, Object> testReportConfig)
    {
        final Map<String, Object> out = new LinkedHashMap<>();

        // 1. Time-series progression (XML <summary><timeSeries>, with fallback to aggregate chart JSONs)
        Map<String, Object> timeSeries = parseExecutiveTimeSeries(firstChildElement(summaryEl, "timeSeries"));
        if (timeSeries == null && chartsDir != null && chartsDir.isDirectory())
        {
            timeSeries = synthesizeTimeSeriesFromCharts(chartsDir, startTimeMs, endTimeMs);
        }
        out.put("timeSeries", timeSeries);

        // 2. Executed Flows (all non-aggregate transactions enriched with configured load profile targets)
        final Map<String, Map<String, Object>> lpByName = new HashMap<>();
        if (loadProfile != null && loadProfile.items != null)
        {
            for (final Map<String, Object> lp : loadProfile.items)
            {
                final String name = String.valueOf(lp.getOrDefault("name", "")).trim();
                if (!name.isEmpty())
                {
                    lpByName.put(name, lp);
                }
            }
        }
        final List<Map<String, Object>> flows = new ArrayList<>();
        final long totalTxCount = Math.max(0L, ((Number) ((Map<String, Object>) txBundle.getOrDefault("totals", Collections.emptyMap()))
            .getOrDefault("count", Long.valueOf(0L))).longValue());
        final Object rawTxItems = txBundle.get("items");
        if (rawTxItems instanceof List<?> txList)
        {
            for (final Object o : txList)
            {
                if (o instanceof Map<?, ?> m)
                {
                    final Map<String, Object> item = (Map<String, Object>) m;
                    if (Boolean.TRUE.equals(item.get("isAggregate")))
                    {
                        continue;
                    }
                    final String name = String.valueOf(item.getOrDefault("name", ""));
                    final long cnt = ((Number) item.getOrDefault("count", Long.valueOf(0L))).longValue();
                    final double sharePct = totalTxCount > 0 ? round3((cnt * 100.0) / totalTxCount) : 0.0;

                    final Map<String, Object> flow = new LinkedHashMap<>();
                    flow.put("name", name);
                    flow.put("count", Long.valueOf(cnt));
                    flow.put("sharePct", Double.valueOf(sharePct));
                    flow.put("cph", item.get("cph"));
                    flow.put("rate", item.get("rate"));
                    flow.put("errors", item.get("errors"));
                    flow.put("errPct", item.get("errPct"));
                    flow.put("events", item.get("events"));
                    flow.put("mean", item.get("mean"));
                    flow.put("median", item.get("median"));
                    flow.put("percentiles", item.get("percentiles"));

                    final Map<String, Object> lp = lpByName.get(name);
                    if (lp != null)
                    {
                        flow.put("targetUsers", lp.get("users"));
                        flow.put("targetArrivalRate", lp.get("arrivalRate"));
                        flow.put("rampUp", lp.get("rampUp"));
                        flow.put("measurement", lp.get("measurement"));
                        flow.put("thinkTime", lp.get("thinkTime"));
                        flow.put("thinkTimeDev", lp.get("thinkTimeDev"));
                    }
                    flows.add(flow);
                }
            }
            flows.sort((a, b) -> Long.compare(((Number) b.get("count")).longValue(), ((Number) a.get("count")).longValue()));
        }
        out.put("flows", flows);

        // 3. Slowest Measured Steps (Actions) & Server Requests (sorted by representative percentile or mean)
        final List<String> cfgPercentiles = (List<String>) testReportConfig.getOrDefault("runtimePercentiles", Collections.emptyList());
        final String repPctKey = pickRepresentativePercentileKey(cfgPercentiles);
        out.put("repPercentile", repPctKey);
        out.put("topSlowActions", pickTopSlowTimers(actBundle, repPctKey, 8, true));
        out.put("topSlowRequests", pickTopSlowTimers(reqBundle, repPctKey, 8, false));

        // 4. Top Error Groups (enriched with affected flows and share of total transaction errors)
        final List<Map<String, Object>> topErrors = new ArrayList<>();
        final Map<String, LinkedHashSet<String>> flowsByErrMsg = new HashMap<>();
        final List<Map<String, Object>> incidents = (List<Map<String, Object>>) errorsBundle.get("incidents");
        if (incidents != null)
        {
            for (final Map<String, Object> inc : incidents)
            {
                final String rawMsg = String.valueOf(inc.getOrDefault("message", ""));
                final String msgKey = rawMsg.isEmpty() ? "(No error message)" : rawMsg;
                final String tc = String.valueOf(inc.getOrDefault("testCaseName", "")).trim();
                if (!tc.isEmpty())
                {
                    flowsByErrMsg.computeIfAbsent(msgKey, k -> new LinkedHashSet<>()).add(tc);
                }
            }
        }
        final Map<String, Object> errSummary = (Map<String, Object>) errorsBundle.getOrDefault("summary", Collections.emptyMap());
        final long totalTxErrors = ((Number) errSummary.getOrDefault("transactionErrors", Long.valueOf(0L))).longValue();
        final List<Map<String, Object>> errByMsg = (List<Map<String, Object>>) errorsBundle.get("overviewByMessage");
        if (errByMsg != null)
        {
            for (final Map<String, Object> e : errByMsg)
            {
                if (topErrors.size() >= 8)
                {
                    break;
                }
                final String msg = String.valueOf(e.getOrDefault("message", ""));
                final long cnt = ((Number) e.getOrDefault("count", Long.valueOf(0L))).longValue();
                final Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("message", msg);
                entry.put("count", Long.valueOf(cnt));
                entry.put("sharePct", Double.valueOf(totalTxErrors > 0 ? round3((cnt * 100.0) / totalTxErrors) : 0.0));
                entry.put("exceptionClass", e.getOrDefault("exceptionClass", ""));
                final LinkedHashSet<String> affected = flowsByErrMsg.get(msg);
                entry.put("affectedFlows", affected != null ? new ArrayList<>(affected).subList(0, Math.min(3, affected.size())) : Collections.emptyList());
                topErrors.add(entry);
            }
        }
        out.put("topErrors", topErrors);

        // 5. Top Event Categories
        final List<Map<String, Object>> topEvents = new ArrayList<>();
        final List<Map<String, Object>> evItems = (List<Map<String, Object>>) eventsBundle.get("items");
        if (evItems != null)
        {
            final List<Map<String, Object>> sortedEv = new ArrayList<>(evItems);
            sortedEv.sort((a, b) -> Long.compare(
                ((Number) b.getOrDefault("totalCount", Long.valueOf(0L))).longValue(),
                ((Number) a.getOrDefault("totalCount", Long.valueOf(0L))).longValue()));
            for (final Map<String, Object> ev : sortedEv)
            {
                if (topEvents.size() >= 8)
                {
                    break;
                }
                final Map<String, Object> entry = new LinkedHashMap<>(3);
                entry.put("name", ev.getOrDefault("name", ""));
                entry.put("testCase", ev.getOrDefault("testCase", ""));
                entry.put("totalCount", ev.getOrDefault("totalCount", Long.valueOf(0L)));
                topEvents.add(entry);
            }
        }
        out.put("topEvents", topEvents);

        // 6. HTTP Status Class Breakdown
        long c2xx = 0L;
        long c3xx = 0L;
        long c4xx = 0L;
        long c5xx = 0L;
        long c0 = 0L;
        long cOther = 0L;
        final List<Map<String, Object>> nonOkCodes = new ArrayList<>();
        final List<Map<String, Object>> respCodes = (List<Map<String, Object>>) networkBundle.get("responseCodes");
        if (respCodes != null)
        {
            for (final Map<String, Object> rc : respCodes)
            {
                final int code = ((Number) rc.getOrDefault("code", Integer.valueOf(0))).intValue();
                final long cnt = ((Number) rc.getOrDefault("count", Long.valueOf(0L))).longValue();
                if (code >= 200 && code < 300)
                {
                    c2xx += cnt;
                }
                else if (code >= 300 && code < 400)
                {
                    c3xx += cnt;
                }
                else if (code >= 400 && code < 500)
                {
                    c4xx += cnt;
                    nonOkCodes.add(rc);
                }
                else if (code >= 500 && code < 600)
                {
                    c5xx += cnt;
                    nonOkCodes.add(rc);
                }
                else if (code == 0)
                {
                    c0 += cnt;
                    nonOkCodes.add(rc);
                }
                else
                {
                    cOther += cnt;
                }
            }
            nonOkCodes.sort((a, b) -> Long.compare(((Number) b.get("count")).longValue(), ((Number) a.get("count")).longValue()));
        }
        final Map<String, Object> httpStatus = new LinkedHashMap<>();
        httpStatus.put("total", Long.valueOf(c2xx + c3xx + c4xx + c5xx + c0 + cOther));
        httpStatus.put("c2xx", Long.valueOf(c2xx));
        httpStatus.put("c3xx", Long.valueOf(c3xx));
        httpStatus.put("c4xx", Long.valueOf(c4xx));
        httpStatus.put("c5xx", Long.valueOf(c5xx));
        httpStatus.put("c0", Long.valueOf(c0));
        httpStatus.put("cOther", Long.valueOf(cOther));
        httpStatus.put("topNonOk", nonOkCodes.size() > 6 ? new ArrayList<>(nonOkCodes.subList(0, 6)) : nonOkCodes);
        out.put("httpStatus", httpStatus);

        // 7. Agent Health Summary (cluster + single-agent peak saturation)
        final Map<String, Object> agTotals = (Map<String, Object>) agentsBundle.getOrDefault("totals", Collections.emptyMap());
        final List<Map<String, Object>> agItems = (List<Map<String, Object>>) agentsBundle.get("items");
        int nonAggAgents = 0;
        double peakSingleAgentCpu = 0.0;
        String peakSingleAgentName = "";
        double peakSingleHostCpu = 0.0;
        String peakSingleHostName = "";
        long maxSingleAgentFullGc = 0L;
        if (agItems != null)
        {
            for (final Map<String, Object> ag : agItems)
            {
                if (Boolean.TRUE.equals(ag.get("isAggregate")))
                {
                    continue;
                }
                nonAggAgents++;
                final double aMax = ((Number) ag.getOrDefault("agentCpuMax", Double.valueOf(0.0))).doubleValue();
                final double tMax = ((Number) ag.getOrDefault("totalCpuMax", Double.valueOf(0.0))).doubleValue();
                final long fgc = ((Number) ag.getOrDefault("fullGcCount", Long.valueOf(0L))).longValue();
                final String nm = String.valueOf(ag.getOrDefault("name", ""));
                if (aMax > peakSingleAgentCpu)
                {
                    peakSingleAgentCpu = aMax;
                    peakSingleAgentName = nm;
                }
                if (tMax > peakSingleHostCpu)
                {
                    peakSingleHostCpu = tMax;
                    peakSingleHostName = nm;
                }
                if (fgc > maxSingleAgentFullGc)
                {
                    maxSingleAgentFullGc = fgc;
                }
            }
        }
        final Map<String, Object> agentHealth = new LinkedHashMap<>();
        agentHealth.put("agentCount", Integer.valueOf(nonAggAgents));
        agentHealth.put("clusterCpuMean", agTotals.getOrDefault("totalCpuMean", Double.valueOf(0.0)));
        agentHealth.put("clusterCpuMax", agTotals.getOrDefault("totalCpuMax", Double.valueOf(0.0)));
        agentHealth.put("clusterAgentCpuMean", agTotals.getOrDefault("agentCpuMean", Double.valueOf(0.0)));
        agentHealth.put("clusterAgentCpuMax", agTotals.getOrDefault("agentCpuMax", Double.valueOf(0.0)));
        agentHealth.put("peakSingleAgentCpu", Double.valueOf(round2(peakSingleAgentCpu)));
        agentHealth.put("peakSingleAgentName", peakSingleAgentName);
        agentHealth.put("peakSingleHostCpu", Double.valueOf(round2(peakSingleHostCpu)));
        agentHealth.put("peakSingleHostName", peakSingleHostName);
        agentHealth.put("fullGcCount", agTotals.getOrDefault("fullGcCount", Long.valueOf(maxSingleAgentFullGc)));
        agentHealth.put("fullGcTime", agTotals.getOrDefault("fullGcTime", Long.valueOf(0L)));
        agentHealth.put("minorGcCpu", agTotals.getOrDefault("minorGcCpu", Double.valueOf(0.0)));
        out.put("agentHealth", agentHealth);

        // 8. Browser Core Web Vitals Summary (present only when browser vitals were collected)
        out.put("webVitals", summarizeWebVitals(webVitalsBundle));

        // 9. Scorecard Group Summaries (present only when scorecard.xml was evaluated)
        out.put("scorecardGroups", extractScorecardGroups(fullScorecard));

        return out;
    }

    private Map<String, Object> parseExecutiveTimeSeries(final Element tsEl)
    {
        if (tsEl == null)
        {
            return null;
        }
        final Element rowsEl = firstChildElement(tsEl, "rows");
        if (rowsEl == null)
        {
            return null;
        }
        final List<Element> rowEls = childElements(rowsEl, "row");
        if (rowEls.isEmpty())
        {
            return null;
        }

        final long intervalSec = childLong(tsEl, "interval", 60L);
        final List<Map<String, Object>> rows = new ArrayList<>(rowEls.size());
        for (final Element r : rowEls)
        {
            final Map<String, Object> row = new LinkedHashMap<>(11);
            row.put("elapsed", Long.valueOf(childLong(r, "elapsed", 0L)));
            row.put("time", childText(r, "time", ""));
            row.put("txMean", Double.valueOf(round1(childDouble(r, "transactionMean", 0.0))));
            row.put("txRate", Double.valueOf(round3(childDouble(r, "transactionCountPerSecond", 0.0))));
            row.put("txErr", Double.valueOf(round3(childDouble(r, "transactionErrorsPerSecond", 0.0))));
            row.put("actMean", Double.valueOf(round1(childDouble(r, "actionMean", 0.0))));
            row.put("actRate", Double.valueOf(round3(childDouble(r, "actionCountPerSecond", 0.0))));
            row.put("actErr", Double.valueOf(round3(childDouble(r, "actionErrorsPerSecond", 0.0))));
            row.put("reqMean", Double.valueOf(round1(childDouble(r, "requestMean", 0.0))));
            row.put("reqRate", Double.valueOf(round3(childDouble(r, "requestCountPerSecond", 0.0))));
            row.put("reqErr", Double.valueOf(round3(childDouble(r, "requestErrorsPerSecond", 0.0))));
            rows.add(row);
        }

        final Map<String, Object> out = new LinkedHashMap<>(4);
        out.put("source", "xml");
        out.put("intervalSec", Long.valueOf(intervalSec));
        out.put("hasErrorSeries", Boolean.TRUE);
        out.put("rows", rows);
        return out;
    }

    private Map<String, Object> synthesizeTimeSeriesFromCharts(final File chartsDir, final long reportStartMs, final long reportEndMs)
    {
        final File txJson = new File(new File(chartsDir, "transactions"), "All Transactions.json");
        final File actJson = new File(new File(chartsDir, "actions"), "All Actions.json");
        final File reqJson = new File(new File(chartsDir, "requests"), "All Requests.json");

        final double[][] txBuckets = downsampleChartJsonFull(txJson, EXECUTIVE_CHART_BUCKETS, reportStartMs, reportEndMs);
        final double[][] actBuckets = downsampleChartJsonFull(actJson, EXECUTIVE_CHART_BUCKETS, reportStartMs, reportEndMs);
        final double[][] reqBuckets = downsampleChartJsonFull(reqJson, EXECUTIVE_CHART_BUCKETS, reportStartMs, reportEndMs);

        final int n = txBuckets != null ? txBuckets.length : (reqBuckets != null ? reqBuckets.length : (actBuckets != null ? actBuckets.length : 0));
        if (n == 0)
        {
            return null;
        }

        final long spanMs = Math.max(1000L, reportEndMs > reportStartMs ? (reportEndMs - reportStartMs) : (n * 60_000L));
        final long intervalSec = Math.max(1L, Math.round((spanMs / 1000.0) / n));

        final List<Map<String, Object>> rows = new ArrayList<>(n);
        for (int i = 0; i < n; i++)
        {
            final long elapsedSec = Math.round(i * (spanMs / 1000.0) / n);
            final Map<String, Object> row = new LinkedHashMap<>(11);
            row.put("elapsed", Long.valueOf(elapsedSec));
            row.put("time", formatDurationHms(elapsedSec));
            row.put("txMean", Double.valueOf(txBuckets != null && i < txBuckets.length ? round1(txBuckets[i][0]) : 0.0));
            row.put("txRate", Double.valueOf(txBuckets != null && i < txBuckets.length ? round3(txBuckets[i][1]) : 0.0));
            row.put("txErr", Double.valueOf(0.0));
            row.put("actMean", Double.valueOf(actBuckets != null && i < actBuckets.length ? round1(actBuckets[i][0]) : 0.0));
            row.put("actRate", Double.valueOf(actBuckets != null && i < actBuckets.length ? round3(actBuckets[i][1]) : 0.0));
            row.put("actErr", Double.valueOf(0.0));
            row.put("reqMean", Double.valueOf(reqBuckets != null && i < reqBuckets.length ? round1(reqBuckets[i][0]) : 0.0));
            row.put("reqRate", Double.valueOf(reqBuckets != null && i < reqBuckets.length ? round3(reqBuckets[i][1]) : 0.0));
            row.put("reqErr", Double.valueOf(0.0));
            rows.add(row);
        }

        final Map<String, Object> out = new LinkedHashMap<>(4);
        out.put("source", "charts");
        out.put("intervalSec", Long.valueOf(intervalSec));
        out.put("hasErrorSeries", Boolean.FALSE);
        out.put("rows", rows);
        return out;
    }

    /**
     * Downsamples a chart JSON file of {@code [timestampMs, meanMs, minMs, maxMs, countPerSec]} tuples into
     * {@code buckets} equal-width slices returning {@code [meanMs, countPerSec]} per bucket.
     */
    private double[][] downsampleChartJsonFull(final File jsonFile, final int buckets, final long reportStartMs, final long reportEndMs)
    {
        if (jsonFile == null || !jsonFile.isFile() || buckets <= 0)
        {
            return null;
        }
        long[] timestamps = new long[256];
        double[] means = new double[256];
        double[] rates = new double[256];
        int count = 0;

        try (JsonParser parser = jsonFactory.createParser(jsonFile))
        {
            if (parser.nextToken() != JsonToken.START_ARRAY)
            {
                return null;
            }
            while (parser.nextToken() != JsonToken.END_ARRAY && parser.currentToken() != null)
            {
                if (parser.currentToken() == JsonToken.START_ARRAY)
                {
                    long tsVal = 0L;
                    double meanVal = Double.NaN;
                    double rateVal = 0.0;
                    boolean tsValid = false;
                    boolean meanValid = false;
                    int idx = 0;
                    while (parser.nextToken() != JsonToken.END_ARRAY && parser.currentToken() != null)
                    {
                        final JsonToken tok = parser.currentToken();
                        if (tok.isStructStart())
                        {
                            parser.skipChildren();
                        }
                        else if (idx == 0 && tok.isNumeric())
                        {
                            tsVal = parser.getLongValue();
                            tsValid = true;
                        }
                        else if (idx == 1 && tok.isNumeric())
                        {
                            meanVal = parser.getDoubleValue();
                            meanValid = true;
                        }
                        else if (idx == 4 && tok.isNumeric())
                        {
                            rateVal = parser.getDoubleValue();
                        }
                        idx++;
                    }
                    if (tsValid && meanValid && Double.isFinite(meanVal))
                    {
                        if (count == timestamps.length)
                        {
                            timestamps = Arrays.copyOf(timestamps, timestamps.length * 2);
                            means = Arrays.copyOf(means, means.length * 2);
                            rates = Arrays.copyOf(rates, rates.length * 2);
                        }
                        timestamps[count] = tsVal;
                        means[count] = meanVal;
                        rates[count] = Double.isFinite(rateVal) ? rateVal : 0.0;
                        count++;
                    }
                }
                else if (parser.currentToken().isStructStart())
                {
                    parser.skipChildren();
                }
            }
        }
        catch (final IOException e)
        {
            LOG.debug("Failed to synthesize executive time series from '{}': {}", jsonFile, e.getMessage());
            return null;
        }

        if (count == 0)
        {
            return null;
        }

        long minTs = Long.MAX_VALUE;
        long maxTs = Long.MIN_VALUE;
        for (int i = 0; i < count; i++)
        {
            if (timestamps[i] < minTs)
            {
                minTs = timestamps[i];
            }
            if (timestamps[i] > maxTs)
            {
                maxTs = timestamps[i];
            }
        }
        final long start = reportStartMs > 0 && reportEndMs > reportStartMs ? reportStartMs : minTs;
        final long end = reportStartMs > 0 && reportEndMs > reportStartMs ? reportEndMs : maxTs;
        final double span = Math.max(1.0, (double) (end - start));

        final int outCount = Math.min(buckets, count);
        final double[] meanSum = new double[outCount];
        final double[] rateSum = new double[outCount];
        final int[] bucketCnt = new int[outCount];

        for (int i = 0; i < count; i++)
        {
            int b = (int) Math.floor(((timestamps[i] - start) / span) * outCount);
            if (b < 0)
            {
                b = 0;
            }
            else if (b >= outCount)
            {
                b = outCount - 1;
            }
            meanSum[b] += means[i];
            rateSum[b] += rates[i];
            bucketCnt[b]++;
        }

        final double[][] result = new double[outCount][2];
        for (int b = 0; b < outCount; b++)
        {
            if (bucketCnt[b] > 0)
            {
                result[b][0] = meanSum[b] / bucketCnt[b];
                result[b][1] = rateSum[b] / bucketCnt[b];
            }
        }
        return result;
    }

    private static String pickRepresentativePercentileKey(final List<String> pctList)
    {
        if (pctList == null || pctList.isEmpty())
        {
            return "95";
        }
        if (pctList.contains("95"))
        {
            return "95";
        }
        if (pctList.contains("90"))
        {
            return "90";
        }
        return pctList.get(pctList.size() - 1);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> pickTopSlowTimers(
        final Map<String, Object> sectionBundle, final String repPctKey, final int limit, final boolean includeApdex)
    {
        final List<Map<String, Object>> items = new ArrayList<>();
        final Object raw = sectionBundle.get("items");
        if (!(raw instanceof List<?> list))
        {
            return items;
        }
        final String pKey = "p" + repPctKey;
        for (final Object o : list)
        {
            if (o instanceof Map<?, ?> m)
            {
                final Map<String, Object> item = (Map<String, Object>) m;
                if (Boolean.TRUE.equals(item.get("isAggregate")))
                {
                    continue;
                }
                final Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("name", item.get("name"));
                entry.put("count", item.get("count"));
                entry.put("mean", item.get("mean"));
                entry.put("median", item.get("median"));
                entry.put("percentiles", item.get("percentiles"));
                entry.put("errors", item.get("errors"));
                entry.put("errPct", item.get("errPct"));
                if (includeApdex && item.containsKey("apdex"))
                {
                    entry.put("apdex", item.get("apdex"));
                }
                items.add(entry);
            }
        }
        items.sort((a, b) -> Double.compare(extractSortMetric(b, pKey), extractSortMetric(a, pKey)));
        return items.size() > limit ? new ArrayList<>(items.subList(0, limit)) : items;
    }

    @SuppressWarnings("unchecked")
    private static double extractSortMetric(final Map<String, Object> item, final String pKey)
    {
        final Object pcts = item.get("percentiles");
        if (pcts instanceof Map<?, ?> pm && pm.get(pKey) instanceof Number n)
        {
            return n.doubleValue();
        }
        final Object mean = item.get("mean");
        return mean instanceof Number n ? n.doubleValue() : 0.0;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> summarizeWebVitals(final Map<String, Object> webVitalsBundle)
    {
        final Map<String, Object> out = new LinkedHashMap<>();
        final List<Map<String, Object>> items = (List<Map<String, Object>>) webVitalsBundle.get("items");
        if (items == null || items.isEmpty())
        {
            out.put("present", Boolean.FALSE);
            return out;
        }
        final List<String> metricKeys = List.of("lcp", "inp", "cls", "fcp", "ttfb");
        int goodActions = 0;
        int improveActions = 0;
        int poorActions = 0;
        final Map<String, long[]> agg = new LinkedHashMap<>();
        for (final String k : metricKeys)
        {
            agg.put(k, new long[3]);
        }

        for (final Map<String, Object> it : items)
        {
            int worst = 0; // 1=good, 2=improve, 3=poor
            for (final String k : metricKeys)
            {
                final Object mv = it.get(k);
                if (mv instanceof Map<?, ?> m)
                {
                    final String rating = String.valueOf(m.get("rating"));
                    final int r = "poor".equals(rating) ? 3 : ("improve".equals(rating) ? 2 : ("good".equals(rating) ? 1 : 0));
                    if (r > worst)
                    {
                        worst = r;
                    }
                    final long[] counts = agg.get(k);
                    counts[0] += ((Number) ((Map<String, Object>) m).getOrDefault("goodCount", Long.valueOf(0L))).longValue();
                    counts[1] += ((Number) ((Map<String, Object>) m).getOrDefault("improveCount", Long.valueOf(0L))).longValue();
                    counts[2] += ((Number) ((Map<String, Object>) m).getOrDefault("poorCount", Long.valueOf(0L))).longValue();
                }
            }
            if (worst == 3)
            {
                poorActions++;
            }
            else if (worst == 2)
            {
                improveActions++;
            }
            else if (worst == 1)
            {
                goodActions++;
            }
        }

        final Map<String, Object> metrics = new LinkedHashMap<>();
        for (final Map.Entry<String, long[]> e : agg.entrySet())
        {
            final long[] c = e.getValue();
            if (c[0] + c[1] + c[2] > 0)
            {
                final Map<String, Object> m = new LinkedHashMap<>(3);
                m.put("good", Long.valueOf(c[0]));
                m.put("improve", Long.valueOf(c[1]));
                m.put("poor", Long.valueOf(c[2]));
                metrics.put(e.getKey(), m);
            }
        }

        out.put("present", Boolean.TRUE);
        out.put("actionCount", Integer.valueOf(items.size()));
        out.put("goodActions", Integer.valueOf(goodActions));
        out.put("improveActions", Integer.valueOf(improveActions));
        out.put("poorActions", Integer.valueOf(poorActions));
        out.put("metrics", metrics);
        return out;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> extractScorecardGroups(final Map<String, Object> fullScorecard)
    {
        if (fullScorecard == null)
        {
            return Collections.emptyList();
        }
        final Object raw = fullScorecard.get("groups");
        if (!(raw instanceof List<?> list))
        {
            return Collections.emptyList();
        }
        final List<Map<String, Object>> groups = new ArrayList<>(list.size());
        for (final Object o : list)
        {
            if (o instanceof Map<?, ?> m)
            {
                final Map<String, Object> g = (Map<String, Object>) m;
                final Map<String, Object> compact = new LinkedHashMap<>(6);
                compact.put("id", g.get("id"));
                compact.put("name", g.get("name"));
                compact.put("result", g.get("result"));
                compact.put("points", g.get("points"));
                compact.put("maxPoints", g.get("maxPoints"));
                compact.put("ruleCount", g.get("ruleCount"));
                groups.add(compact);
            }
        }
        return groups;
    }

    private static double round1(final double v)
    {
        return Math.round(v * 10.0) / 10.0;
    }

    private static double round2(final double v)
    {
        return Math.round(v * 100.0) / 100.0;
    }

    private static double round3(final double v)
    {
        return Math.round(v * 1000.0) / 1000.0;
    }

    // =========================================================================
    // Scorecard XML Parser
    // =========================================================================

    static Map<String, Object> createAbsentScorecardSummary()
    {
        final Map<String, Object> sc = new LinkedHashMap<>(2);
        sc.put("present", Boolean.FALSE);
        sc.put("failedRules", Collections.emptyList());
        return sc;
    }

    Map<String, Object> parseScorecardSummary(final File scorecardXmlFile)
    {
        if (scorecardXmlFile == null || !scorecardXmlFile.isFile())
        {
            return createAbsentScorecardSummary();
        }
        return parseScorecardXmlSafe(scorecardXmlFile).summary;
    }

    private ScorecardParsed parseScorecardXmlSafe(final File scorecardXmlFile)
    {
        try
        {
            return parseScorecardXml(scorecardXmlFile);
        }
        catch (final Exception ex)
        {
            LOG.warn("Failed to parse scorecard XML '{}'", scorecardXmlFile, ex);
            final Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("present", Boolean.TRUE);
            summary.put("result", "ERROR");
            summary.put("points", Integer.valueOf(0));
            summary.put("maxPoints", Integer.valueOf(0));
            summary.put("pointsPercentage", Double.valueOf(0.0));
            summary.put("errorMessage", String.valueOf(ex.getMessage()));
            summary.put("failedRules", Collections.emptyList());

            final Map<String, Object> fullBundle = new LinkedHashMap<>(summary);
            fullBundle.put("groups", Collections.emptyList());
            fullBundle.put("rules", Collections.emptyList());
            fullBundle.put("ratings", Collections.emptyList());
            return new ScorecardParsed(summary, fullBundle);
        }
    }

    private ScorecardParsed parseScorecardXml(final File scorecardXmlFile) throws Exception
    {
        final Document doc = parseXmlDocument(scorecardXmlFile);
        final Element root = doc.getDocumentElement();
        final Element cfgEl = firstChildElement(root, "configuration");
        final Element outcomeEl = firstChildElement(root, "outcome");

        // 1. Named selectors lookup
        final Map<String, String> namedSelectors = new HashMap<>();
        final Element selectorsEl = firstChildElement(cfgEl, "selectors");
        if (selectorsEl != null)
        {
            for (final Element selEl : childElements(selectorsEl, "selector"))
            {
                final String id = selEl.getAttribute("id").trim();
                final String expr = childText(selEl, "expression", selEl.getTextContent()).trim();
                if (!id.isEmpty())
                {
                    namedSelectors.put(id, expr);
                }
            }
        }

        // 2. Rule definitions lookup
        final Map<String, RuleDefInfo> ruleDefs = new LinkedHashMap<>();
        final Element rulesCfgEl = firstChildElement(cfgEl, "rules");
        if (rulesCfgEl != null)
        {
            for (final Element rEl : childElements(rulesCfgEl, "rule"))
            {
                final String id = rEl.getAttribute("id").trim();
                final RuleDefInfo def = new RuleDefInfo();
                def.id = id;
                def.name = nonBlankOrDefault(rEl.getAttribute("name"), id);
                def.description = childText(rEl, "description", "").trim();
                def.enabled = !"false".equalsIgnoreCase(rEl.getAttribute("enabled"));
                def.failsTest = "true".equalsIgnoreCase(rEl.getAttribute("failsTest"));
                def.maxPoints = (int) parseLongSafe(rEl.getAttribute("points"), 0L);

                final Element checksEl = firstChildElement(rEl, "checks");
                if (checksEl != null)
                {
                    for (final Element chEl : childElements(checksEl, "check"))
                    {
                        final String cond = childText(chEl, "condition", "").trim();
                        if (!cond.isEmpty())
                        {
                            def.conditions.add(cond);
                        }
                        final Element selNode = firstChildElement(chEl, "selector");
                        if (selNode != null)
                        {
                            final String selRef = selNode.getAttribute("selector-id").trim();
                            final String selText = selNode.getTextContent().trim();
                            if (!selText.isEmpty())
                            {
                                def.selectors.add(selText);
                            }
                            else if (!selRef.isEmpty())
                            {
                                def.selectors.add(namedSelectors.getOrDefault(selRef, "@" + selRef));
                            }
                        }
                        else
                        {
                            final String selIdAttr = chEl.getAttribute("selector-id").trim();
                            if (!selIdAttr.isEmpty())
                            {
                                def.selectors.add(namedSelectors.getOrDefault(selIdAttr, "@" + selIdAttr));
                            }
                        }
                    }
                }
                if (!id.isEmpty())
                {
                    ruleDefs.put(id, def);
                }
            }
        }

        // 3. Group definitions lookup
        final Map<String, Element> groupDefs = new LinkedHashMap<>();
        final Element groupsCfgEl = firstChildElement(cfgEl, "groups");
        if (groupsCfgEl != null)
        {
            for (final Element gEl : childElements(groupsCfgEl, "group"))
            {
                final String gid = gEl.getAttribute("id").trim();
                if (!gid.isEmpty())
                {
                    groupDefs.put(gid, gEl);
                }
            }
        }

        // 4. Ratings definitions
        final List<Map<String, Object>> ratingsList = new ArrayList<>();
        final Map<String, Map<String, Object>> ratingById = new HashMap<>();
        final Element ratingsCfgEl = firstChildElement(cfgEl, "ratings");
        if (ratingsCfgEl != null)
        {
            for (final Element rtEl : childElements(ratingsCfgEl, "rating"))
            {
                final String rid = rtEl.getAttribute("id").trim();
                final Map<String, Object> rt = new LinkedHashMap<>(6);
                rt.put("id", rid);
                rt.put("name", nonBlankOrDefault(rtEl.getAttribute("name"), rid));
                rt.put("value", Double.valueOf(parseDoubleSafe(rtEl.getAttribute("value"), 0.0)));
                rt.put("enabled", Boolean.valueOf(!"false".equalsIgnoreCase(rtEl.getAttribute("enabled"))));
                rt.put("failsTest", Boolean.valueOf("true".equalsIgnoreCase(rtEl.getAttribute("failsTest"))));
                rt.put("description", childText(rtEl, "description", "").trim());
                ratingsList.add(rt);
                if (!rid.isEmpty())
                {
                    ratingById.put(rid, rt);
                }
            }
        }

        // 5. Outcome parsing
        final Element errEl = firstChildElement(outcomeEl, "error");
        final String errorMessage = errEl != null ? childText(errEl, "message", "").trim() : "";
        final boolean testFailed = outcomeEl != null && "true".equalsIgnoreCase(outcomeEl.getAttribute("testFailed"));
        final String verdict = !errorMessage.isEmpty() ? "ERROR" : (testFailed ? "FAILED" : "PASSED");
        final int points = outcomeEl != null ? (int) parseLongSafe(outcomeEl.getAttribute("points"), 0L) : 0;
        final int maxPoints = outcomeEl != null ? (int) parseLongSafe(outcomeEl.getAttribute("totalPoints"), 0L) : 0;
        final double pointsPct = outcomeEl != null ? parseDoubleSafe(outcomeEl.getAttribute("pointsPercentage"), 0.0) : 0.0;
        final String ratingId = childText(outcomeEl, "rating", "").trim();
        final Map<String, Object> matchedRating = ratingById.get(ratingId);
        final String ratingName = matchedRating != null ? String.valueOf(matchedRating.get("name")) : ratingId;
        final String ratingDesc = matchedRating != null ? String.valueOf(matchedRating.get("description")) : "";

        final List<Map<String, Object>> groupsOut = new ArrayList<>();
        final List<Map<String, Object>> rulesOut = new ArrayList<>();
        final Map<String, Map<String, Object>> failedRulesSummaryById = new LinkedHashMap<>();

        final Element outcomeGroupsEl = firstChildElement(outcomeEl, "groups");
        if (outcomeGroupsEl != null)
        {
            for (final Element gOutEl : childElements(outcomeGroupsEl, "group"))
            {
                final String gId = nonBlankOrDefault(gOutEl.getAttribute("ref-id"), gOutEl.getAttribute("id"));
                final Element gDefEl = groupDefs.get(gId);
                final String gName = gDefEl != null ? nonBlankOrDefault(gDefEl.getAttribute("name"), gId) : nonBlankOrDefault(gId, "Default Group");
                final String gDesc = gDefEl != null ? childText(gDefEl, "description", "").trim() : "";
                final String gResult = childText(gOutEl, "result", "PASSED").trim();
                final int gPoints = (int) parseLongSafe(gOutEl.getAttribute("points"), 0L);
                final int gMaxPoints = (int) parseLongSafe(gOutEl.getAttribute("totalPoints"), 0L);
                final String gMessage = childText(gOutEl, "message", "").trim();

                final Element gRulesEl = firstChildElement(gOutEl, "rules");
                final List<Element> gRuleNodes = gRulesEl != null ? childElements(gRulesEl, "rule") : Collections.emptyList();

                final Map<String, Object> gMap = new LinkedHashMap<>(8);
                gMap.put("id", gId);
                gMap.put("name", gName);
                gMap.put("description", gDesc);
                gMap.put("result", gResult);
                gMap.put("ruleCount", Integer.valueOf(gRuleNodes.size()));
                gMap.put("points", Integer.valueOf(gPoints));
                gMap.put("maxPoints", Integer.valueOf(gMaxPoints));
                gMap.put("message", gMessage);
                groupsOut.add(gMap);

                for (final Element rOutEl : gRuleNodes)
                {
                    final String rId = nonBlankOrDefault(rOutEl.getAttribute("ref-id"), rOutEl.getAttribute("id"));
                    final RuleDefInfo rDef = ruleDefs.get(rId);
                    final String rName = rDef != null ? rDef.name : rId;
                    final String rResult = childText(rOutEl, "result", "PASSED").trim();
                    final boolean rTestFailed = "true".equalsIgnoreCase(rOutEl.getAttribute("testFailed"));
                    final boolean rFailsTest = rDef != null && rDef.failsTest;
                    final int rPoints = (int) parseLongSafe(rOutEl.getAttribute("points"), 0L);
                    final int rMaxPoints = rDef != null ? rDef.maxPoints : rPoints;
                    final String rMessage = childText(rOutEl, "message", "").trim();
                    final String conditionStr = rDef != null ? String.join(" && ", rDef.conditions) : "";

                    final List<String> actualValues = new ArrayList<>();
                    final Element checksOutEl = firstChildElement(rOutEl, "checks");
                    if (checksOutEl != null)
                    {
                        for (final Element cOutEl : childElements(checksOutEl, "check"))
                        {
                            final String v = childText(cOutEl, "value", "").trim();
                            if (!v.isEmpty())
                            {
                                actualValues.add(v);
                            }
                        }
                    }
                    final String actualValueStr = String.join(", ", actualValues);

                    final Map<String, Object> rMap = new LinkedHashMap<>();
                    rMap.put("id", rId);
                    rMap.put("name", rName);
                    rMap.put("groupName", gName);
                    rMap.put("description", rDef != null ? rDef.description : "");
                    rMap.put("enabled", Boolean.valueOf(rDef == null || rDef.enabled));
                    rMap.put("failsTest", Boolean.valueOf(rFailsTest));
                    rMap.put("testFailed", Boolean.valueOf(rTestFailed));
                    rMap.put("result", rResult);
                    rMap.put("condition", conditionStr);
                    rMap.put("actualValue", actualValueStr);
                    rMap.put("points", Integer.valueOf(rPoints));
                    rMap.put("maxPoints", Integer.valueOf(rMaxPoints));
                    rMap.put("message", rMessage);
                    rMap.put("selectors", rDef != null ? rDef.selectors : Collections.emptyList());
                    rulesOut.add(rMap);

                    final String upperRes = rResult.toUpperCase(Locale.ROOT);
                    if (!"PASSED".equals(upperRes) && !"SKIPPED".equals(upperRes) && !"DISABLED".equals(upperRes)
                        && !failedRulesSummaryById.containsKey(rId))
                    {
                        final Map<String, Object> miss = new LinkedHashMap<>(5);
                        miss.put("id", rId);
                        miss.put("name", rName);
                        miss.put("value", actualValueStr);
                        miss.put("condition", conditionStr);
                        miss.put("failsTest", Boolean.valueOf(rTestFailed || rFailsTest));
                        failedRulesSummaryById.put(rId, miss);
                    }
                }
            }
        }

        final Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("present", Boolean.TRUE);
        summary.put("result", verdict);
        summary.put("testFailed", Boolean.valueOf(testFailed));
        summary.put("points", Integer.valueOf(points));
        summary.put("maxPoints", Integer.valueOf(maxPoints));
        summary.put("pointsPercentage", Double.valueOf(pointsPct));
        summary.put("ratingId", ratingId);
        summary.put("ratingName", ratingName);
        summary.put("ratingDescription", ratingDesc);
        if (!errorMessage.isEmpty())
        {
            summary.put("errorMessage", errorMessage);
        }
        summary.put("failedRules", new ArrayList<>(failedRulesSummaryById.values()));

        final Map<String, Object> fullBundle = new LinkedHashMap<>(summary);
        fullBundle.put("groups", groupsOut);
        fullBundle.put("rules", rulesOut);
        fullBundle.put("ratings", ratingsList);

        return new ScorecardParsed(summary, fullBundle);
    }

    // =========================================================================
    // Serialization & XML / Formatting Helpers
    // =========================================================================

    void writeJsGlobal(final File targetFile, final String globalVarName, final Object payload) throws IOException
    {
        final String json = objectMapper.writeValueAsString(payload);
        final String content = "window." + globalVarName + " = " + json + ";\n";
        Files.writeString(targetFile.toPath(), content, StandardCharsets.UTF_8);
    }

    ObjectMapper getObjectMapper()
    {
        return objectMapper;
    }

    private void patchScorecardFieldInJsBundle(
        final File jsFile, final String globalVarName, final Map<String, Object> scorecardSummary) throws IOException
    {
        patchFieldInJsBundle(jsFile, globalVarName, "scorecard", scorecardSummary);
    }

    private void patchFieldInJsBundle(
        final File jsFile, final String globalVarName, final String fieldName, final Object fieldValue) throws IOException
    {
        final String raw = Files.readString(jsFile.toPath(), StandardCharsets.UTF_8);
        final int eqIdx = raw.indexOf('=');
        final int endIdx = raw.lastIndexOf(';');
        if (eqIdx < 0 || endIdx <= eqIdx)
        {
            throw new IOException("Unexpected JS bundle format in " + jsFile);
        }
        final String jsonPart = raw.substring(eqIdx + 1, endIdx).trim();
        final Map<String, Object> obj = objectMapper.readValue(jsonPart, new TypeReference<LinkedHashMap<String, Object>>()
        {
        });
        obj.put(fieldName, fieldValue);
        writeJsGlobal(jsFile, globalVarName, obj);
    }

    static Document parseXmlDocument(final File xmlFile) throws ParserConfigurationException, Exception
    {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        final DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(xmlFile);
    }

    private static boolean hasGeneratedCharts(final File reportDir)
    {
        final Path chartsPath = reportDir.toPath().resolve("charts");
        if (!Files.isDirectory(chartsPath))
        {
            return false;
        }
        try (Stream<Path> walk = Files.walk(chartsPath, 3, FileVisitOption.FOLLOW_LINKS))
        {
            return walk.filter(Files::isRegularFile).anyMatch(p -> {
                final String fileName = p.getFileName().toString().toLowerCase(Locale.ROOT);
                return !fileName.equals("placeholder.webp") && (fileName.endsWith(".webp") || fileName.endsWith(".json"));
            });
        }
        catch (final IOException e)
        {
            return false;
        }
    }

    static Map<String, Double> parsePercentilesMap(final Element pctsEl)
    {
        final Map<String, Double> map = new LinkedHashMap<>();
        if (pctsEl != null)
        {
            for (final Element child : childElements(pctsEl))
            {
                final String key = child.getTagName();
                final String val = child.getTextContent();
                if (key != null && !key.isEmpty() && val != null && !val.trim().isEmpty())
                {
                    map.put(key, Double.valueOf(parseDoubleSafe(val, 0.0)));
                }
            }
        }
        return map;
    }

    static List<String> splitLabels(final String rawLabels)
    {
        if (StringUtils.isBlank(rawLabels))
        {
            return Collections.emptyList();
        }
        return Arrays.asList(StringUtils.split(rawLabels.trim()));
    }

    static String extractCommentHeadline(final List<String> comments)
    {
        if (comments == null || comments.isEmpty())
        {
            return "";
        }
        for (final String raw : comments)
        {
            if (raw == null)
            {
                continue;
            }
            final Matcher m = COMMENT_NAME_PATTERN.matcher(raw);
            if (m.find())
            {
                final String stripped = HTML_TAG_PATTERN.matcher(m.group(1)).replaceAll("").trim();
                if (!stripped.isEmpty())
                {
                    return stripped;
                }
            }
            final Matcher xtcMatcher = COMMENT_XTC_HEADING_PATTERN.matcher(raw);
            if (xtcMatcher.find())
            {
                final String stripped = HTML_TAG_PATTERN.matcher(xtcMatcher.group(1)).replaceAll("").trim();
                if (!stripped.isEmpty())
                {
                    return stripped;
                }
            }
        }
        return "";
    }

    static String extractExceptionClass(final String trace, final String message)
    {
        if (StringUtils.isNotBlank(trace))
        {
            final String firstLine = trace.trim().split("\\R", 2)[0].trim();
            final int colon = firstLine.indexOf(':');
            if (colon > 0)
            {
                return firstLine.substring(0, colon).trim();
            }
            if (!firstLine.isEmpty())
            {
                return firstLine;
            }
        }
        return StringUtils.isNotBlank(message) ? "Error" : "—";
    }

    static long parseTimestampMs(final String tsStr)
    {
        if (StringUtils.isBlank(tsStr))
        {
            return 0L;
        }
        try
        {
            return ZonedDateTime.parse(tsStr.trim(), XLT_DATE_FMT).toInstant().toEpochMilli();
        }
        catch (final Exception e)
        {
            return 0L;
        }
    }

    static String formatDurationHms(final long durationSec)
    {
        final long s = Math.max(0L, durationSec);
        final long h = s / 3600L;
        final long m = (s % 3600L) / 60L;
        final long rem = s % 60L;
        return String.format(Locale.US, "%02d:%02d:%02d", Long.valueOf(h), Long.valueOf(m), Long.valueOf(rem));
    }

    private static String formatThinkTime(final long think, final long dev)
    {
        if (think == 0L && dev == 0L)
        {
            return "0 ms";
        }
        if (dev > 0L)
        {
            return String.format(Locale.US, "%,d...%,d ms", Long.valueOf(Math.max(0L, think - dev)), Long.valueOf(think + dev));
        }
        return String.format(Locale.US, "%,d ms", Long.valueOf(think));
    }

    private static Long tryParsePlainCount(final String s)
    {
        if (StringUtils.isBlank(s))
        {
            return Long.valueOf(0L);
        }
        final String cleaned = s.replace(",", "").trim();
        try
        {
            return Long.valueOf(Long.parseLong(cleaned));
        }
        catch (final NumberFormatException e)
        {
            return null;
        }
    }

    private static long extractPeakNumber(final String expr)
    {
        if (StringUtils.isBlank(expr))
        {
            return 0L;
        }
        long maxVal = 0L;
        // Strip commas between digits (e.g. "1...5,250" -> "1...5250") before scanning integers
        final String normalized = expr.replaceAll("(?<=\\d),(?=\\d{3}\\b)", "");
        final Matcher m = Pattern.compile("\\d+").matcher(normalized);
        while (m.find())
        {
            final long v = parseLongSafe(m.group(), 0L);
            if (v > maxVal)
            {
                maxVal = v;
            }
        }
        return maxVal;
    }

    private static int countNonAggregate(final Map<String, Object> bundle)
    {
        final Object raw = bundle.get("items");
        if (!(raw instanceof List<?> list))
        {
            return 0;
        }
        int cnt = 0;
        for (final Object o : list)
        {
            if (o instanceof Map<?, ?> m && !Boolean.TRUE.equals(m.get("isAggregate")))
            {
                cnt++;
            }
        }
        return cnt;
    }

    private static int listSize(final Object raw)
    {
        return raw instanceof List<?> list ? list.size() : 0;
    }

    private static int countExternalArtifacts(final Map<String, Object> externalBundle)
    {
        final Object raw = externalBundle.get("sections");
        if (!(raw instanceof List<?> secs))
        {
            return 0;
        }
        int total = 0;
        for (final Object s : secs)
        {
            if (s instanceof Map<?, ?> m)
            {
                total += listSize(m.get("charts")) + listSize(m.get("tables"));
            }
        }
        return total > 0 ? total : secs.size();
    }

    private static int resolveTotalEventsCount(final Element summaryEl, final Map<String, Object> eventsBundle)
    {
        final Element sumTx = firstChildElement(summaryEl, "transactions");
        final long txEvents = childLong(sumTx, "events", -1L);
        if (txEvents >= 0L)
        {
            return (int) txEvents;
        }
        long sum = 0L;
        final Object raw = eventsBundle.get("items");
        if (raw instanceof List<?> list)
        {
            for (final Object o : list)
            {
                if (o instanceof Map<?, ?> m && m.get("totalCount") instanceof Number n)
                {
                    sum += n.longValue();
                }
            }
        }
        return (int) sum;
    }

    static Element firstChildElement(final Element parent, final String tagName)
    {
        if (parent == null)
        {
            return null;
        }
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling())
        {
            if (n.getNodeType() == Node.ELEMENT_NODE && tagName.equals(n.getNodeName()))
            {
                return (Element) n;
            }
        }
        return null;
    }

    static List<Element> childElements(final Element parent)
    {
        if (parent == null)
        {
            return Collections.emptyList();
        }
        final List<Element> list = new ArrayList<>();
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling())
        {
            if (n.getNodeType() == Node.ELEMENT_NODE)
            {
                list.add((Element) n);
            }
        }
        return list;
    }

    static List<Element> childElements(final Element parent, final String tagName)
    {
        if (parent == null)
        {
            return Collections.emptyList();
        }
        final List<Element> list = new ArrayList<>();
        final NodeList nl = parent.getChildNodes();
        for (int i = 0, len = nl.getLength(); i < len; i++)
        {
            final Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && tagName.equals(n.getNodeName()))
            {
                list.add((Element) n);
            }
        }
        return list;
    }

    static List<String> readStringList(final Element parent, final String itemTag)
    {
        if (parent == null)
        {
            return Collections.emptyList();
        }
        final List<String> out = new ArrayList<>();
        for (final Element el : childElements(parent, itemTag))
        {
            final String txt = el.getTextContent();
            if (txt != null)
            {
                out.add(txt);
            }
        }
        return out;
    }

    static String childText(final Element parent, final String tagName, final String defaultVal)
    {
        final Element el = firstChildElement(parent, tagName);
        if (el == null)
        {
            return defaultVal;
        }
        final String txt = el.getTextContent();
        return txt != null ? txt : defaultVal;
    }

    static long childLong(final Element parent, final String tagName, final long defaultVal)
    {
        final String s = childText(parent, tagName, null);
        return parseLongSafe(s, defaultVal);
    }

    static double childDouble(final Element parent, final String tagName, final double defaultVal)
    {
        final String s = childText(parent, tagName, null);
        return parseDoubleSafe(s, defaultVal);
    }

    static long parseLongSafe(final String s, final long defaultVal)
    {
        if (StringUtils.isBlank(s))
        {
            return defaultVal;
        }
        try
        {
            return Long.parseLong(s.trim());
        }
        catch (final NumberFormatException e)
        {
            try
            {
                return Math.round(Double.parseDouble(s.trim()));
            }
            catch (final NumberFormatException ex)
            {
                return defaultVal;
            }
        }
    }

    static double parseDoubleSafe(final String s, final double defaultVal)
    {
        if (StringUtils.isBlank(s))
        {
            return defaultVal;
        }
        try
        {
            final double d = Double.parseDouble(s.trim());
            return Double.isFinite(d) ? d : defaultVal;
        }
        catch (final NumberFormatException e)
        {
            return defaultVal;
        }
    }

    static String nonBlankOrDefault(final String candidate, final String fallback)
    {
        return StringUtils.isNotBlank(candidate) ? candidate : (fallback != null ? fallback : "");
    }

    private static double round1(final double val)
    {
        return Math.round(val * 10.0) / 10.0;
    }

    private static final class LoadProfileParsed
    {
        final List<Map<String, Object>> items;

        final Map<String, Object> totals;

        LoadProfileParsed(final List<Map<String, Object>> items, final Map<String, Object> totals)
        {
            this.items = items;
            this.totals = totals;
        }
    }

    private static final class RuleDefInfo
    {
        String id;

        String name;

        String description;

        boolean enabled = true;

        boolean failsTest;

        int maxPoints;

        final List<String> conditions = new ArrayList<>();

        final List<String> selectors = new ArrayList<>();
    }

    private static final class ScorecardParsed
    {
        final Map<String, Object> summary;

        final Map<String, Object> fullBundle;

        ScorecardParsed(final Map<String, Object> summary, final Map<String, Object> fullBundle)
        {
            this.summary = summary;
            this.fullBundle = fullBundle;
        }
    }
}