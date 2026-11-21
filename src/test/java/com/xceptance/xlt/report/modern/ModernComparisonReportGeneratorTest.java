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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.report.ReportGeneratorConfiguration.UiMode;
import com.xceptance.xlt.report.diffreport.DiffReportGeneratorConfiguration;
import com.xceptance.xlt.report.trendreport.TrendReportGeneratorConfiguration;

/**
 * Unit tests for {@link ModernComparisonReportGenerator} and comparison/trend UI configuration modes.
 */
public class ModernComparisonReportGeneratorTest
{
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> EXPECTED_HTML_PAGES = List.of(
        "index.html",
        "transactions.html",
        "actions.html",
        "requests.html",
        "network.html",
        "errors.html",
        "events.html",
        "configuration.html",
        "custom-timers.html",
        "custom-values.html",
        "page-load-timings.html",
        "web-vitals.html"
    );

    @Test
    public void testConfigurationUiModeDefaultAndOverride() throws Exception
    {
        final File configDir = new File("config");
        final File homeDir = new File(".");

        final DiffReportGeneratorConfiguration diffDefault = new DiffReportGeneratorConfiguration(homeDir, configDir, null);
        assertEquals(UiMode.MODERN, diffDefault.getUiMode());
        assertTrue(diffDefault.isModernUi());

        final TrendReportGeneratorConfiguration trendDefault = new TrendReportGeneratorConfiguration(homeDir, configDir, null);
        assertEquals(UiMode.MODERN, trendDefault.getUiMode());
        assertTrue(trendDefault.isModernUi());

        final Properties overrideProps = new Properties();
        overrideProps.setProperty(DiffReportGeneratorConfiguration.PROP_UI_MODE, "classic");
        overrideProps.setProperty(TrendReportGeneratorConfiguration.PROP_UI_MODE, "classic");

        final DiffReportGeneratorConfiguration diffClassicProp = new DiffReportGeneratorConfiguration(homeDir, configDir, overrideProps);
        assertEquals(UiMode.CLASSIC, diffClassicProp.getUiMode());
        assertFalse(diffClassicProp.isModernUi());

        final TrendReportGeneratorConfiguration trendClassicProp = new TrendReportGeneratorConfiguration(homeDir, configDir, overrideProps);
        assertEquals(UiMode.CLASSIC, trendClassicProp.getUiMode());
        assertFalse(trendClassicProp.isModernUi());

        final String prevDiffSys = System.getProperty(DiffReportGeneratorConfiguration.PROP_UI_MODE);
        final String prevTrendSys = System.getProperty(TrendReportGeneratorConfiguration.PROP_UI_MODE);
        try
        {
            System.setProperty(DiffReportGeneratorConfiguration.PROP_UI_MODE, "classic");
            System.setProperty(TrendReportGeneratorConfiguration.PROP_UI_MODE, "classic");

            assertFalse(new DiffReportGeneratorConfiguration(homeDir, configDir, null).isModernUi());
            assertFalse(new TrendReportGeneratorConfiguration(homeDir, configDir, null).isModernUi());
        }
        finally
        {
            restoreSystemProperty(DiffReportGeneratorConfiguration.PROP_UI_MODE, prevDiffSys);
            restoreSystemProperty(TrendReportGeneratorConfiguration.PROP_UI_MODE, prevTrendSys);
        }
    }

    @Test
    public void testGenerateDiffFromTestReportsAndSnapshotUpdateRoundTrip() throws Exception
    {
        final File run1Dir = tempFolder.newFolder("run-1");
        final File run2Dir = tempFolder.newFolder("run-2");
        final File outDir = tempFolder.newFolder("diff-out");

        final String xmlRun1 = buildSampleTestReportXml(
            "ShopProject",
            "Run One Baseline",
            List.of("50", "90"),
            100.0,
            0,
            false,
            true,
            0);

        final String xmlRun2 = buildSampleTestReportXml(
            "ShopProject",
            "Run Two Candidate",
            List.of("50", "90"),
            140.0,
            3,
            true,
            false,
            4);

        final File xml1File = new File(run1Dir, XltConstants.LOAD_REPORT_XML_FILENAME);
        final File xml2File = new File(run2Dir, XltConstants.LOAD_REPORT_XML_FILENAME);
        Files.writeString(xml1File.toPath(), xmlRun1, StandardCharsets.UTF_8);
        Files.writeString(xml2File.toPath(), xmlRun2, StandardCharsets.UTF_8);

        final ModernReportGenerator xmlHelper = new ModernReportGenerator();
        final Document doc1 = xmlHelper.parseXmlDocument(xml1File);
        final Document doc2 = xmlHelper.parseXmlDocument(xml2File);

        final ModernComparisonReportGenerator generator = new ModernComparisonReportGenerator();
        generator.generateFromTestReports(
            "diff",
            "ShopProject",
            List.of(
                new ModernComparisonReportGenerator.RunInput("run-1", run1Dir, doc1),
                new ModernComparisonReportGenerator.RunInput("run-2", run2Dir, doc2)),
            new File("config"),
            outDir);

        for (final String page : EXPECTED_HTML_PAGES)
        {
            assertTrue("Expected HTML page " + page, new File(outDir, page).isFile());
        }
        assertTrue(new File(outDir, "css/style.css").isFile());
        assertTrue(new File(outDir, "css/comparison.css").isFile());
        assertTrue(new File(outDir, "js/echarts-6.0.0.min.js").isFile());
        assertTrue(new File(outDir, "js/report-utils.js").isFile());
        assertTrue(new File(outDir, "js/table-copy-markdown.js").isFile());
        assertTrue(new File(outDir, XltConstants.COMPARISON_SNAPSHOT_FILENAME).isFile());

        final Map<String, Object> meta = readJsBundle(new File(outDir, "js/report-meta.js"), "XLT_META");
        assertEquals("diff", meta.get("reportKind"));
        assertEquals("ShopProject", meta.get("projectName"));
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> runs = (List<Map<String, Object>>) meta.get("runs");
        assertEquals(2, runs.size());
        assertEquals("run-1", runs.get(0).get("name"));
        assertEquals("run-2", runs.get(1).get("name"));

        @SuppressWarnings("unchecked")
        final Map<String, Object> trCfg = (Map<String, Object>) meta.get("testReportConfig");
        assertEquals(List.of("50", "90"), trCfg.get("runtimePercentiles"));

        @SuppressWarnings("unchecked")
        final Map<String, Object> caps = (Map<String, Object>) meta.get("capabilities");
        assertEquals(Boolean.TRUE, caps.get("hasErrors"));
        assertEquals(Boolean.TRUE, caps.get("hasEvents"));
        assertEquals(Boolean.TRUE, caps.get("hasNetwork"));
        assertEquals(Boolean.TRUE, caps.get("hasConfiguration"));
        assertEquals(Boolean.TRUE, caps.get("hasCounts"));
        assertEquals(Boolean.TRUE, caps.get("hasPercentiles"));

        final Map<String, Object> txBundle = readJsBundle(new File(outDir, "js/transactions-data.js"), "XLT_TRANSACTIONS");
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> txItems = (List<Map<String, Object>>) txBundle.get("items");
        // Aggregate + TOrder + added TAdded
        assertEquals(3, txItems.size());

        final Map<String, Object> orderItem = findByName(txItems, "TOrder");
        assertNotNull(orderItem);
        assertEquals(List.of("guest", "checkout"), orderItem.get("labels"));
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> orderRuns = (List<Map<String, Object>>) orderItem.get("runs");
        @SuppressWarnings("unchecked")
        final Map<String, Object> pMap = (Map<String, Object>) orderRuns.get(1).get("percentiles");
        assertTrue(pMap.containsKey("p50"));
        assertTrue(pMap.containsKey("p90"));
        assertFalse(pMap.containsKey("p99.9"));

        final Map<String, Object> addedItem = findByName(txItems, "TAdded");
        assertNotNull(addedItem);
        @SuppressWarnings("unchecked")
        final List<Object> addedRuns = (List<Object>) addedItem.get("runs");
        assertNull(addedRuns.get(0));
        assertNotNull(addedRuns.get(1));

        // Verify removed request in requests-data.js
        final Map<String, Object> reqBundle = readJsBundle(new File(outDir, "js/requests-data.js"), "XLT_REQUESTS");
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> reqItems = (List<Map<String, Object>>) reqBundle.get("items");
        final Map<String, Object> removedReq = findByName(reqItems, "LegacyPing [GET]");
        assertNotNull(removedReq);
        assertEquals("GET", removedReq.get("method"));
        @SuppressWarnings("unchecked")
        final List<Object> removedRuns = (List<Object>) removedReq.get("runs");
        assertNotNull(removedRuns.get(0));
        assertNull(removedRuns.get(1));

        // Verify events & errors bundles
        final Map<String, Object> errBundle = readJsBundle(new File(outDir, "js/errors-data.js"), "XLT_ERRORS");
        assertEquals(Boolean.TRUE, errBundle.get("available"));
        assertFalse(((List<?>) errBundle.get("items")).isEmpty());

        final Map<String, Object> evBundle = readJsBundle(new File(outDir, "js/events-data.js"), "XLT_EVENTS");
        assertEquals(Boolean.TRUE, evBundle.get("available"));
        assertFalse(((List<?>) evBundle.get("items")).isEmpty());

        // Round-trip via comparison-runs.json.gz through UpdateReportUi
        assertTrue(new File(outDir, "js/report-meta.js").delete());
        assertTrue(new File(outDir, "index.html").delete());
        generator.updateExistingReportDir(new File("config"), outDir);
        assertTrue(new File(outDir, "index.html").isFile());
        final Map<String, Object> rebuiltMeta = readJsBundle(new File(outDir, "js/report-meta.js"), "XLT_META");
        assertEquals("ShopProject", rebuiltMeta.get("projectName"));
        assertEquals(meta.get("counts"), rebuiltMeta.get("counts"));
    }

    @Test
    public void testEmptyRuntimePercentilesInComparison() throws Exception
    {
        final File run1Dir = tempFolder.newFolder("empty-1");
        final File run2Dir = tempFolder.newFolder("empty-2");
        final File outDir = tempFolder.newFolder("empty-out");

        final String xml1 = buildSampleTestReportXml("EmptyPct", "Run 1", List.of(), 80.0, 0, false, false, 0);
        final String xml2 = buildSampleTestReportXml("EmptyPct", "Run 2", List.of(), 90.0, 0, false, false, 0);
        final File f1 = new File(run1Dir, XltConstants.LOAD_REPORT_XML_FILENAME);
        final File f2 = new File(run2Dir, XltConstants.LOAD_REPORT_XML_FILENAME);
        Files.writeString(f1.toPath(), xml1, StandardCharsets.UTF_8);
        Files.writeString(f2.toPath(), xml2, StandardCharsets.UTF_8);

        final ModernReportGenerator xmlHelper = new ModernReportGenerator();
        new ModernComparisonReportGenerator().generateFromTestReports(
            "diff",
            "EmptyPct",
            List.of(
                new ModernComparisonReportGenerator.RunInput("empty-1", run1Dir, xmlHelper.parseXmlDocument(f1)),
                new ModernComparisonReportGenerator.RunInput("empty-2", run2Dir, xmlHelper.parseXmlDocument(f2))),
            new File("config"),
            outDir);

        final Map<String, Object> meta = readJsBundle(new File(outDir, "js/report-meta.js"), "XLT_META");
        @SuppressWarnings("unchecked")
        final Map<String, Object> trCfg = (Map<String, Object>) meta.get("testReportConfig");
        assertEquals(List.of(), trCfg.get("runtimePercentiles"));
        @SuppressWarnings("unchecked")
        final Map<String, Object> caps = (Map<String, Object>) meta.get("capabilities");
        assertEquals(Boolean.FALSE, caps.get("hasPercentiles"));
    }

    @Test
    public void testGenerateTrendFromThreeTestReports() throws Exception
    {
        final File r1 = tempFolder.newFolder("trend-r1");
        final File r2 = tempFolder.newFolder("trend-r2");
        final File r3 = tempFolder.newFolder("trend-r3");
        final File outDir = tempFolder.newFolder("trend-out");

        final String xml1 = buildSampleTestReportXml("TrendProj", "Build 1", List.of("95"), 100.0, 0, true, false, 0);
        final String xml2 = buildSampleTestReportXml("TrendProj", "Build 2", List.of("95"), 120.0, 1, false, false, 1);
        final String xml3 = buildSampleTestReportXml("TrendProj", "Build 3", List.of("95"), 140.0, 2, true, false, 3);

        final File f1 = new File(r1, XltConstants.LOAD_REPORT_XML_FILENAME);
        final File f2 = new File(r2, XltConstants.LOAD_REPORT_XML_FILENAME);
        final File f3 = new File(r3, XltConstants.LOAD_REPORT_XML_FILENAME);
        Files.writeString(f1.toPath(), xml1, StandardCharsets.UTF_8);
        Files.writeString(f2.toPath(), xml2, StandardCharsets.UTF_8);
        Files.writeString(f3.toPath(), xml3, StandardCharsets.UTF_8);

        final ModernReportGenerator xmlHelper = new ModernReportGenerator();
        new ModernComparisonReportGenerator().generateFromTestReports(
            "trend",
            "TrendProj",
            List.of(
                new ModernComparisonReportGenerator.RunInput("Build 1", r1, xmlHelper.parseXmlDocument(f1)),
                new ModernComparisonReportGenerator.RunInput("Build 2", r2, xmlHelper.parseXmlDocument(f2)),
                new ModernComparisonReportGenerator.RunInput("Build 3", r3, xmlHelper.parseXmlDocument(f3))),
            new File("config"),
            outDir);

        final Map<String, Object> meta = readJsBundle(new File(outDir, "js/report-meta.js"), "XLT_META");
        assertEquals("trend", meta.get("reportKind"));
        assertEquals(3, ((List<?>) meta.get("runs")).size());

        final Map<String, Object> txBundle = readJsBundle(new File(outDir, "js/transactions-data.js"), "XLT_TRANSACTIONS");
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> txItems = (List<Map<String, Object>>) txBundle.get("items");
        final Map<String, Object> sparseItem = findByName(txItems, "TAdded");
        assertNotNull(sparseItem);
        @SuppressWarnings("unchecked")
        final List<Object> sparseRuns = (List<Object>) sparseItem.get("runs");
        assertEquals(3, sparseRuns.size());
        assertNotNull(sparseRuns.get(0));
        assertNull(sparseRuns.get(1));
        assertNotNull(sparseRuns.get(2));
    }

    @Test
    public void testGenerateFromDiffReportXmlAndUpdateReportUi() throws Exception
    {
        final File outDir = tempFolder.newFolder("legacy-diff-dir");
        final String diffXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <diffreport>
              <testReport1>
                <name>BaselineRun</name>
                <projectName>LegacyShop</projectName>
                <comments><string>Baseline baseline test</string></comments>
                <general>
                  <startTime>2026-08-31 10:00:00 UTC</startTime>
                  <endTime>2026-08-31 11:00:00 UTC</endTime>
                  <duration>3600</duration>
                  <hits>10000</hits>
                  <bytesSent>500000</bytesSent>
                  <bytesReceived>5000000</bytesReceived>
                </general>
                <loadProfile>
                  <testCase>
                    <userName>TBrowse</userName>
                    <testCaseClassName>com.example.TBrowse</testCaseClassName>
                    <numberOfUsers>10</numberOfUsers>
                    <arrivalRate>0</arrivalRate>
                    <rampUpPeriod>60</rampUpPeriod>
                    <measurementPeriod>3600</measurementPeriod>
                    <actionThinkTime>1000</actionThinkTime>
                    <actionThinkTimeDeviation>100</actionThinkTimeDeviation>
                  </testCase>
                </loadProfile>
                <properties>
                  <property name="xlt.test.threads" value="10"/>
                </properties>
              </testReport1>
              <testReport2>
                <name>TargetRun</name>
                <projectName>LegacyShop</projectName>
                <comments><string>200% load run</string></comments>
                <general>
                  <startTime>2026-09-01 10:00:00 UTC</startTime>
                  <endTime>2026-09-01 11:00:00 UTC</endTime>
                  <duration>3600</duration>
                  <hits>20000</hits>
                  <bytesSent>1000000</bytesSent>
                  <bytesReceived>10000000</bytesReceived>
                </general>
                <loadProfile>
                  <testCase>
                    <userName>TBrowse</userName>
                    <testCaseClassName>com.example.TBrowse</testCaseClassName>
                    <numberOfUsers>20</numberOfUsers>
                    <arrivalRate>0</arrivalRate>
                    <rampUpPeriod>60</rampUpPeriod>
                    <measurementPeriod>3600</measurementPeriod>
                    <actionThinkTime>1000</actionThinkTime>
                    <actionThinkTimeDeviation>100</actionThinkTimeDeviation>
                  </testCase>
                </loadProfile>
                <properties>
                  <property name="xlt.test.threads" value="20"/>
                </properties>
              </testReport2>
              <summary>
                <transactions>
                  <name>Totals</name>
                  <count><oldValue>1000</oldValue><newValue>2000</newValue></count>
                  <countPerSecond><oldValue>0.28</oldValue><newValue>0.56</newValue></countPerSecond>
                  <countPerHour><oldValue>1000</oldValue><newValue>2000</newValue></countPerHour>
                  <errors><oldValue>0</oldValue><newValue>4</newValue></errors>
                  <mean><oldValue>200.0</oldValue><newValue>280.0</newValue></mean>
                  <min><oldValue>50.0</oldValue><newValue>60.0</newValue></min>
                  <max><oldValue>900.0</oldValue><newValue>1400.0</newValue></max>
                  <median><oldValue>190.0</oldValue><newValue>260.0</newValue></median>
                  <percentiles>
                    <p50><oldValue>190.0</oldValue><newValue>260.0</newValue></p50>
                    <p95><oldValue>420.0</oldValue><newValue>680.0</newValue></p95>
                  </percentiles>
                </transactions>
              </summary>
              <transactions>
                <transaction>
                  <name>TBrowse</name>
                  <labels>catalog</labels>
                  <count><oldValue>1000</oldValue><newValue>2000</newValue></count>
                  <countPerSecond><oldValue>0.28</oldValue><newValue>0.56</newValue></countPerSecond>
                  <countPerHour><oldValue>1000</oldValue><newValue>2000</newValue></countPerHour>
                  <errors><oldValue>0</oldValue><newValue>4</newValue></errors>
                  <mean><oldValue>200.0</oldValue><newValue>280.0</newValue></mean>
                  <min><oldValue>50.0</oldValue><newValue>60.0</newValue></min>
                  <max><oldValue>900.0</oldValue><newValue>1400.0</newValue></max>
                  <median><oldValue>190.0</oldValue><newValue>260.0</newValue></median>
                  <percentiles>
                    <p50><oldValue>190.0</oldValue><newValue>260.0</newValue></p50>
                    <p95><oldValue>420.0</oldValue><newValue>680.0</newValue></p95>
                  </percentiles>
                </transaction>
              </transactions>
            </diffreport>
            """;
        Files.writeString(new File(outDir, XltConstants.DIFF_REPORT_XML_FILENAME).toPath(), diffXml, StandardCharsets.UTF_8);

        new ModernComparisonReportGenerator().updateExistingReportDir(new File("config"), outDir);

        final Map<String, Object> meta = readJsBundle(new File(outDir, "js/report-meta.js"), "XLT_META");
        assertEquals("diff", meta.get("reportKind"));
        assertEquals("LegacyShop", meta.get("projectName"));
        @SuppressWarnings("unchecked")
        final Map<String, Object> caps = (Map<String, Object>) meta.get("capabilities");
        assertEquals(Boolean.FALSE, caps.get("hasErrors"));
        assertEquals(Boolean.FALSE, caps.get("hasEvents"));
        assertEquals(Boolean.FALSE, caps.get("hasNetwork"));
        assertEquals(Boolean.TRUE, caps.get("hasConfiguration"));
        assertEquals(Boolean.TRUE, caps.get("hasCounts"));
        assertEquals(Boolean.TRUE, caps.get("hasPercentiles"));

        final Map<String, Object> cfg = readJsBundle(new File(outDir, "js/config-data.js"), "XLT_CONFIG");
        assertEquals(Boolean.TRUE, cfg.get("available"));
        assertFalse(((List<?>) cfg.get("loadProfiles")).isEmpty());
        assertFalse(((List<?>) cfg.get("properties")).isEmpty());
    }

    @Test
    public void testGenerateFromTrendReportXmlWithNullValuesAndCliMain() throws Exception
    {
        final File outDir = tempFolder.newFolder("legacy-trend-dir");
        final String trendXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <trendreport>
              <summary>
                <transactions>
                  <name>All Transactions</name>
                  <trendValues>
                    <trendValue>
                      <reportName>RunA</reportName>
                      <reportDate>2026-09-01 10:00:00 UTC</reportDate>
                      <reportComments>First</reportComments>
                      <throughput>2.0</throughput>
                      <errors>0</errors>
                      <min>10</min>
                      <max>100</max>
                      <mean>50.0</mean>
                      <median>45.0</median>
                    </trendValue>
                    <trendValue>
                      <reportName>RunB</reportName>
                      <reportDate>2026-09-02 10:00:00 UTC</reportDate>
                      <reportComments>null</reportComments>
                      <throughput>2.5</throughput>
                      <errors>1</errors>
                      <min>12</min>
                      <max>150</max>
                      <mean>60.0</mean>
                      <median>55.0</median>
                    </trendValue>
                    <trendValue>
                      <reportName>RunC</reportName>
                      <reportDate>2026-09-03 10:00:00 UTC</reportDate>
                      <reportComments>Third</reportComments>
                      <throughput>3.0</throughput>
                      <errors>2</errors>
                      <min>15</min>
                      <max>200</max>
                      <mean>75.0</mean>
                      <median>70.0</median>
                    </trendValue>
                  </trendValues>
                </transactions>
              </summary>
              <transactions>
                <transaction>
                  <name>TOptional</name>
                  <trendValues>
                    <trendValue>
                      <reportName>RunA</reportName>
                      <throughput>1.5</throughput>
                      <errors>0</errors>
                      <min>20</min>
                      <max>80</max>
                      <mean>40.0</mean>
                      <median>38.0</median>
                    </trendValue>
                    <trendValue>
                      <reportName>RunB</reportName>
                      <throughput>null</throughput>
                      <errors>null</errors>
                      <min>null</min>
                      <max>null</max>
                      <mean>null</mean>
                      <median>null</median>
                    </trendValue>
                    <trendValue>
                      <reportName>RunC</reportName>
                      <throughput>2.0</throughput>
                      <errors>1</errors>
                      <min>22</min>
                      <max>95</max>
                      <mean>52.0</mean>
                      <median>48.0</median>
                    </trendValue>
                  </trendValues>
                </transaction>
              </transactions>
            </trendreport>
            """;
        Files.writeString(new File(outDir, XltConstants.TREND_REPORT_XML_FILENAME).toPath(), trendXml, StandardCharsets.UTF_8);

        new ModernComparisonReportGenerator().updateExistingReportDir(new File("config"), outDir);

        final Map<String, Object> meta = readJsBundle(new File(outDir, "js/report-meta.js"), "XLT_META");
        assertEquals("trend", meta.get("reportKind"));
        assertEquals(3, ((List<?>) meta.get("runs")).size());
        @SuppressWarnings("unchecked")
        final Map<String, Object> caps = (Map<String, Object>) meta.get("capabilities");
        assertEquals(Boolean.FALSE, caps.get("hasCounts"));
        assertEquals(Boolean.FALSE, caps.get("hasPercentiles"));
        assertEquals(Boolean.FALSE, caps.get("hasConfiguration"));

        final Map<String, Object> txBundle = readJsBundle(new File(outDir, "js/transactions-data.js"), "XLT_TRANSACTIONS");
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> txItems = (List<Map<String, Object>>) txBundle.get("items");
        final Map<String, Object> opt = findByName(txItems, "TOptional");
        assertNotNull(opt);
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> runs = (List<Map<String, Object>>) opt.get("runs");
        assertEquals(3, runs.size());
        assertEquals(1.5 * 3600.0, ((Number) runs.get(0).get("cph")).doubleValue(), 0.001);
        assertNull(runs.get(1));
        assertEquals(52.0, ((Number) runs.get(2).get("mean")).doubleValue(), 0.001);
    }

    private static String buildSampleTestReportXml(
        final String projectName, final String comment, final List<String> pctKeys,
        final double txMean, final int errCount, final boolean includeAddedTx,
        final boolean includeLegacyReq, final int eventCount)
    {
        final StringBuilder pctCfg = new StringBuilder();
        final StringBuilder pctTags = new StringBuilder();
        if (!pctKeys.isEmpty())
        {
            pctCfg.append("<runtimePercentiles>");
            pctTags.append("<percentiles>");
            for (final String p : pctKeys)
            {
                pctCfg.append("<string>").append(p).append("</string>");
                final double v = Math.round(txMean * (1.0 + Double.parseDouble(p) / 200.0) * 10.0) / 10.0;
                pctTags.append("<p").append(p).append(">").append(v).append("</p").append(p).append(">");
            }
            pctCfg.append("</runtimePercentiles>");
            pctTags.append("</percentiles>");
        }
        else
        {
            pctCfg.append("<runtimePercentiles/>");
        }

        final String addedTx = includeAddedTx
            ? "<transaction><name>TAdded</name><count>10</count><countPerSecond>0.1</countPerSecond><countPerHour>360</countPerHour><errors>0</errors><errorPercentage>0</errorPercentage><mean>210</mean><min>100</min><max>300</max><median>200</median>" +
              pctTags + "</transaction>"
            : "";

        final String legacyReq = includeLegacyReq
            ? "<request><name>LegacyPing [GET]</name><count>50</count><countPerSecond>0.5</countPerSecond><countPerHour>1800</countPerHour><errors>0</errors><errorPercentage>0</errorPercentage><mean>15</mean><min>5</min><max>40</max><median>14</median>" +
              pctTags + "</request>"
            : "";

        final String errBlock = errCount > 0
            ? "<errors><error><count>" + errCount + "</count><testCaseName>TOrder</testCaseName><actionName>Checkout</actionName><requestName>POST</requestName><message>HTTP 500</message><trace>java.lang.RuntimeException: boom</trace></error></errors>"
            : "<errors/>";

        final String evBlock = eventCount > 0
            ? "<events><event><name>SlowStep</name><testCaseName>TOrder</testCaseName><totalCount>" + eventCount + "</totalCount><messages><message><info>Step exceeded budget</info></message></messages></event></events>"
            : "<events/>";

        return String.format(
            Locale.ROOT,
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <testreport>
              <configuration>
                <projectName>%s</projectName>
                <comments><string>%s</string></comments>
                <loadProfile>
                  <testCase>
                    <userName>TOrder</userName>
                    <testCaseClassName>com.example.TOrder</testCaseClassName>
                    <numberOfUsers>5</numberOfUsers>
                    <arrivalRate>0</arrivalRate>
                    <rampUpPeriod>0</rampUpPeriod>
                    <measurementPeriod>600</measurementPeriod>
                    <actionThinkTime>1000</actionThinkTime>
                    <actionThinkTimeDeviation>0</actionThinkTimeDeviation>
                  </testCase>
                </loadProfile>
                <properties>
                  <property name="xlt.custom.flag" value="%s"/>
                </properties>
              </configuration>
              <testReportConfig>
                %s
              </testReportConfig>
              <general>
                <startTime>2026-08-31 12:00:00 UTC</startTime>
                <endTime>2026-08-31 12:10:00 UTC</endTime>
                <duration>600</duration>
                <hits>1000</hits>
                <bytesSent>50000</bytesSent>
                <bytesReceived>500000</bytesReceived>
              </general>
              <summary>
                <transactions>
                  <name>Totals</name>
                  <count>200</count>
                  <countPerSecond>0.33</countPerSecond>
                  <countPerHour>1200</countPerHour>
                  <errors>%d</errors>
                  <errorPercentage>0</errorPercentage>
                  <mean>%.1f</mean>
                  <min>20</min>
                  <max>500</max>
                  <median>%.1f</median>
                  %s
                </transactions>
              </summary>
              <transactions>
                <transaction>
                  <name>TOrder</name>
                  <labels>guest checkout</labels>
                  <count>200</count>
                  <countPerSecond>0.33</countPerSecond>
                  <countPerHour>1200</countPerHour>
                  <errors>%d</errors>
                  <errorPercentage>0</errorPercentage>
                  <mean>%.1f</mean>
                  <min>20</min>
                  <max>500</max>
                  <median>%.1f</median>
                  %s
                </transaction>
                %s
              </transactions>
              <actions/>
              <requests>
                %s
              </requests>
              %s
              %s
              <responseCodes><responseCode><code>200</code><statusText>OK</statusText><count>100</count></responseCode></responseCodes>
              <requestMethods><requestMethod><method>GET</method><count>100</count></requestMethod></requestMethods>
              <contentTypes><contentType><contentType>text/html</contentType><count>100</count></contentType></contentTypes>
              <hosts><host><name>example.com</name><count>100</count></host></hosts>
            </testreport>
            """,
            projectName,
            comment,
            comment,
            pctCfg.toString(),
            errCount,
            txMean,
            txMean * 0.9,
            pctTags.toString(),
            errCount,
            txMean,
            txMean * 0.9,
            pctTags.toString(),
            addedTx,
            legacyReq,
            errBlock,
            evBlock);
    }

    private static Map<String, Object> findByName(final List<Map<String, Object>> items, final String name)
    {
        for (final Map<String, Object> item : items)
        {
            if (name.equals(item.get("name")))
            {
                return item;
            }
        }
        return null;
    }

    private Map<String, Object> readJsBundle(final File jsFile, final String globalVarName) throws Exception
    {
        assertTrue("Expected JS bundle file to exist: " + jsFile, jsFile.isFile());
        final String raw = Files.readString(jsFile.toPath(), StandardCharsets.UTF_8).trim();
        final String prefix = "window." + globalVarName + " = ";
        assertTrue("Expected prefix '" + prefix + "' in " + jsFile.getName(), raw.startsWith(prefix));
        assertTrue("Expected trailing semicolon in " + jsFile.getName(), raw.endsWith(";"));
        final String json = raw.substring(prefix.length(), raw.length() - 1);
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>()
        {
        });
    }

    private static void restoreSystemProperty(final String key, final String prevVal)
    {
        if (prevVal == null)
        {
            System.clearProperty(key);
        }
        else
        {
            System.setProperty(key, prevVal);
        }
    }
}
