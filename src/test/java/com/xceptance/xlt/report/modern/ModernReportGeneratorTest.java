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
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ModernReportGeneratorTest
{
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void generateReportBundles_fullMetadataLabelsPercentilesIntervalsColorizationAndSparklines() throws Exception
    {
        final File reportDir = tempFolder.newFolder("report");
        final File chartsTxDir = new File(reportDir, "charts/transactions");
        final File chartsReqDir = new File(reportDir, "charts/requests");
        final File chartsCvDir = new File(reportDir, "charts/customvalues");
        assertTrue(chartsTxDir.mkdirs());
        assertTrue(chartsReqDir.mkdirs());
        assertTrue(chartsCvDir.mkdirs());

        // Create ai-data.md and resultBrowsers directory to verify flag detection
        Files.writeString(new File(reportDir, "ai-data.md").toPath(), "# AI Data\n", StandardCharsets.UTF_8);
        assertTrue(new File(reportDir, "resultBrowsers").mkdirs());

        // Write 100-point chart JSON for transaction TOrder (timestamps across 5 hours: 1000000 .. 19000000)
        final StringBuilder txJson = new StringBuilder("[");
        for (int i = 0; i < 100; i++)
        {
            if (i > 0)
            {
                txJson.append(',');
            }
            final long ts = 1_000_000L + i * 180_000L;
            final double mean = 100.0 + (i / 5) * 10.0; // bucket b (0..19) has 5 points each with mean 100 + b*10
            txJson.append('[').append(ts).append(',').append(mean).append(",50,250,2.5]");
        }
        txJson.append(']');
        Files.writeString(new File(chartsTxDir, "TOrder.json").toPath(), txJson.toString(), StandardCharsets.UTF_8);

        // Write custom value chart JSON using UUID chartFilename
        Files.writeString(new File(chartsCvDir, "cv-uuid-1234.json").toPath(),
                          "[[1000,12.5,10,15,1],[2000,17.5,12,20,1],[3000,20.0,15,25,1]]",
                          StandardCharsets.UTF_8);

        // Deliberately omit charts/requests/*.json to test missing-chart empty spark fallback alongside existing charts
        final String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <testreport>
              <configuration>
                <projectName>ACME Storefront</projectName>
                <comments>
                  <string>&lt;div class="name"&gt;Peak Run #42&lt;/div&gt;Release candidate soak test</string>
                </comments>
                <properties>
                  <property name="com.xceptance.xlt.users" value="250"/>
                </properties>
                <loadProfile>
                  <entry>
                    <name>TOrder</name>
                    <users><int>50</int></users>
                    <arrivalRate><int>100</int></arrivalRate>
                  </entry>
                </loadProfile>
              </configuration>
              <testReportConfig>
                <runtimePercentiles>
                  <string>50</string>
                  <string>90</string>
                  <string>95</string>
                  <string>99.9</string>
                </runtimePercentiles>
                <runtimeIntervals>
                  <interval from="0" to="500"/>
                  <interval from="501" to="1500"/>
                  <interval from="1501" to=""/>
                </runtimeIntervals>
                <requestTableColorization>
                  <colorization groupName="default">
                    <rules>
                      <rule id="mean" type="mean" from="200" target="500" to="1000"/>
                      <null/>
                      <rule id="p99.9" type="percentile" from="500" target="1000" to="2000"/>
                    </rules>
                  </colorization>
                  <colorization groupName="checkout">
                    <rules>
                      <rule id="500" type="segmentation" from="5" target="15" to="30"/>
                    </rules>
                  </colorization>
                </requestTableColorization>
              </testReportConfig>
              <general>
                <startTime>2026-08-31 12:00:00 UTC</startTime>
                <endTime>2026-08-31 17:00:00 UTC</endTime>
                <duration>18000</duration>
                <hits>12000</hits>
                <bytesSent><totalCount>1024000</totalCount><countPerSecond>56.8</countPerSecond></bytesSent>
                <bytesReceived><totalCount>20480000</totalCount><countPerSecond>1137.7</countPerSecond></bytesReceived>
              </general>
              <summary>
                <transactions>
                  <name>Totals</name>
                  <count>500</count>
                  <countPerSecond>0.028</countPerSecond>
                  <errors>2</errors>
                  <errorPercentage>0.4</errorPercentage>
                  <mean>195.0</mean>
                  <min>50</min>
                  <max>900</max>
                  <deviation>42.5</deviation>
                  <median>190.0</median>
                  <percentiles>
                    <p50>190.0</p50>
                    <p90>260.0</p90>
                    <p95>280.0</p95>
                    <p99.9>850.0</p99.9>
                  </percentiles>
                </transactions>
                <actions><name>Totals</name><count>1000</count><mean>80.0</mean></actions>
                <requests><name>Totals</name><count>4000</count><mean>45.0</mean></requests>
                <pageLoadTimings><name>Totals</name><count>0</count></pageLoadTimings>
                <customTimers><name>Totals</name><count>0</count></customTimers>
              </summary>
              <transactions>
                <transaction>
                  <name>TOrder</name>
                  <labels>guest   checkout  critical</labels>
                  <count>500</count>
                  <countPerSecond>0.028</countPerSecond>
                  <errors>2</errors>
                  <errorPercentage>0.4</errorPercentage>
                  <mean>195.0</mean>
                  <min>50</min>
                  <max>900</max>
                  <deviation>42.5</deviation>
                  <median>190.0</median>
                  <percentiles>
                    <p50>190.0</p50>
                    <p90>260.0</p90>
                    <p95>280.0</p95>
                    <p99.9>850.0</p99.9>
                  </percentiles>
                </transaction>
              </transactions>
              <actions>
                <action>
                  <name>PlaceOrder</name>
                  <labels></labels>
                  <count>500</count>
                  <mean>120.5</mean>
                  <min>40</min>
                  <max>450</max>
                  <apdex>
                    <value>0.94</value>
                    <longValue>0.942 [1.0s / 4.0s]</longValue>
                  </apdex>
                </action>
              </actions>
              <requests>
                <request>
                  <name>POST /checkout</name>
                  <labels>api</labels>
                  <colorizationGroupName>checkout</colorizationGroupName>
                  <count>500</count>
                  <mean>310.0</mean>
                  <min>80</min>
                  <max>1800</max>
                  <percentiles>
                    <p50>280.0</p50>
                    <p90>600.0</p90>
                    <p95>750.0</p95>
                    <p99.9>1750.0</p99.9>
                  </percentiles>
                  <countPerInterval>
                    <int>400</int>
                    <int>80</int>
                    <int>20</int>
                  </countPerInterval>
                  <percentagePerInterval>
                    <big-decimal>80.0</big-decimal>
                    <big-decimal>16.0</big-decimal>
                    <big-decimal>4.0</big-decimal>
                  </percentagePerInterval>
                  <bytesSent><mean>512.0</mean><min>400</min><max>700</max><countPerSecond>14.2</countPerSecond><totalCount>256000</totalCount></bytesSent>
                  <bytesReceived><mean>4096.0</mean><min>2048</min><max>8192</max><countPerSecond>113.7</countPerSecond><totalCount>2048000</totalCount></bytesReceived>
                  <dnsTime><mean>2.5</mean><max>15</max></dnsTime>
                  <connectTime><mean>8.0</mean><max>35</max></connectTime>
                  <serverBusyTime><mean>290.0</mean><max>1720</max></serverBusyTime>
                  <timeToFirstBytes><mean>300.5</mean><max>1770</max></timeToFirstBytes>
                </request>
                <request>
                  <name>GET /ambiguous</name>
                  <count>500</count>
                  <mean>25.0</mean>
                </request>
              </requests>
              <customValues>
                <customValue>
                  <name>CartItems</name>
                  <chartFilename>cv-uuid-1234</chartFilename>
                  <description>Items per cart</description>
                  <labels>cart</labels>
                  <count>500</count>
                  <mean>3.4</mean>
                  <min>1</min>
                  <max>12</max>
                  <standardDeviation>1.8</standardDeviation>
                </customValue>
              </customValues>
              <errors>
                <resultsPathPrefix>./resultBrowsers/run-42/</resultsPathPrefix>
              </errors>
              <pageLoadTimings/>
              <webVitals/>
              <external/>
            </testreport>
            """;

        final File xmlFile = new File(reportDir, "testreport.xml");
        Files.writeString(xmlFile.toPath(), xml, StandardCharsets.UTF_8);

        final ModernReportGenerator generator = new ModernReportGenerator();
        generator.generateReportBundles(reportDir);

        // 1. Validate report-meta.js
        final Map<String, Object> meta = readJsBundle(new File(reportDir, "js/report-meta.js"), "XLT_META");
        assertEquals("ACME Storefront", meta.get("projectName"));
        assertEquals("Peak Run #42", meta.get("loadTestName"));
        assertEquals(Boolean.TRUE, meta.get("chartsGenerated"));
        assertEquals(Boolean.TRUE, meta.get("aiDataGenerated"));
        assertEquals(Boolean.TRUE, meta.get("linkToResultBrowser"));

        @SuppressWarnings("unchecked")
        final Map<String, Object> scorecardMeta = (Map<String, Object>) meta.get("scorecard");
        assertEquals(Boolean.FALSE, scorecardMeta.get("present"));
        assertFalse(new File(reportDir, "js/scorecard-data.js").exists());

        @SuppressWarnings("unchecked")
        final Map<String, Object> trConfig = (Map<String, Object>) meta.get("testReportConfig");
        assertEquals(List.of("50", "90", "95", "99.9"), trConfig.get("runtimePercentiles"));

        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> intervals = (List<Map<String, Object>>) trConfig.get("runtimeIntervals");
        assertEquals(3, intervals.size());
        assertEquals(0, ((Number) intervals.get(0).get("from")).intValue());
        assertEquals(500, ((Number) intervals.get(0).get("to")).intValue());
        assertEquals(1501, ((Number) intervals.get(2).get("from")).intValue());
        assertNull(intervals.get(2).get("to"));

        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> colorizations = (List<Map<String, Object>>) trConfig.get("requestTableColorization");
        assertEquals(2, colorizations.size());
        assertEquals("default", colorizations.get(0).get("groupName"));
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> defaultRules = (List<Map<String, Object>>) colorizations.get(0).get("rules");
        // <null/> rule must be skipped cleanly
        assertEquals(2, defaultRules.size());
        assertEquals("mean", defaultRules.get(0).get("id"));
        assertEquals("p99.9", defaultRules.get(1).get("id"));

        // 2. Validate transactions-data.js: labels & 20-bucket sparkline downsampling
        final Map<String, Object> txBundle = readJsBundle(new File(reportDir, "js/transactions-data.js"), "XLT_TRANSACTIONS");
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> txItems = (List<Map<String, Object>>) txBundle.get("items");
        assertEquals(2, txItems.size()); // Totals + TOrder
        final Map<String, Object> tOrder = txItems.get(1);
        assertEquals("TOrder", tOrder.get("name"));
        assertEquals(List.of("guest", "checkout", "critical"), tOrder.get("labels"));

        @SuppressWarnings("unchecked")
        final List<Number> spark = (List<Number>) tOrder.get("spark");
        assertEquals(20, spark.size());
        assertEquals(100.0, spark.get(0).doubleValue(), 0.01);
        assertEquals(290.0, spark.get(19).doubleValue(), 0.01);

        // 3. Validate actions-data.js: empty labels + apdex
        final Map<String, Object> actBundle = readJsBundle(new File(reportDir, "js/actions-data.js"), "XLT_ACTIONS");
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> actItems = (List<Map<String, Object>>) actBundle.get("items");
        final Map<String, Object> placeOrder = actItems.get(1);
        assertEquals(List.of(), placeOrder.get("labels"));
        @SuppressWarnings("unchecked")
        final Map<String, Object> apdex = (Map<String, Object>) placeOrder.get("apdex");
        assertNotNull(apdex);
        assertEquals(0.94, ((Number) apdex.get("value")).doubleValue(), 0.001);
        assertEquals("0.942 [1.0s / 4.0s]", apdex.get("longValue"));

        // 4. Validate requests-data.js: colorizationGroupName, segmentation, p99.9, network stats & missing chart spark []
        final Map<String, Object> reqBundle = readJsBundle(new File(reportDir, "js/requests-data.js"), "XLT_REQUESTS");
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> reqItems = (List<Map<String, Object>>) reqBundle.get("items");
        assertEquals(3, reqItems.size()); // Totals + 2 requests

        final Map<String, Object> postCheckout = reqItems.get(1);
        assertEquals("checkout", postCheckout.get("colorizationGroup"));
        assertEquals(List.of(), postCheckout.get("spark")); // missing JSON -> []
        @SuppressWarnings("unchecked")
        final Map<String, Number> reqPcts = (Map<String, Number>) postCheckout.get("percentiles");
        assertEquals(1750.0, reqPcts.get("p99.9").doubleValue(), 0.01);
        @SuppressWarnings("unchecked")
        final List<Map<String, Number>> segs = (List<Map<String, Number>>) postCheckout.get("segmentation");
        assertEquals(3, segs.size());
        assertEquals(400, segs.get(0).get("count").intValue());
        assertEquals(80.0, segs.get(0).get("pct").doubleValue(), 0.01);
        assertEquals(300.5, ((Number) postCheckout.get("ttfb")).doubleValue(), 0.01);
        assertEquals(2048000L, ((Number) postCheckout.get("recvTotal")).longValue());

        // Ambiguous request without <colorizationGroupName> when colorization is configured -> explicitly null
        final Map<String, Object> ambiguousReq = reqItems.get(2);
        assertTrue(ambiguousReq.containsKey("colorizationGroup"));
        assertNull(ambiguousReq.get("colorizationGroup"));

        // 5. Validate custom-values-data.js uses UUID chartFilename
        final Map<String, Object> cvBundle = readJsBundle(new File(reportDir, "js/custom-values-data.js"), "XLT_CUSTOM_VALUES");
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> cvItems = (List<Map<String, Object>>) cvBundle.get("items");
        assertEquals(1, cvItems.size());
        assertEquals("cv-uuid-1234", cvItems.get(0).get("chartBase"));
        @SuppressWarnings("unchecked")
        final List<Number> cvSpark = (List<Number>) cvItems.get(0).get("spark");
        assertEquals(3, cvSpark.size());

        // 6. Validate empty sections emit items: [] even when summary has zero-count totals
        final Map<String, Object> pltBundle = readJsBundle(new File(reportDir, "js/page-load-timings-data.js"), "XLT_PAGE_LOAD_TIMINGS");
        assertEquals(List.of(), pltBundle.get("items"));
    }

    @Test
    public void downsampleChartJson_handlesSinglePointShortSeriesLongSeriesAndMissingFile() throws Exception
    {
        final ModernReportGenerator generator = new ModernReportGenerator();

        // Missing / null file -> empty list
        assertEquals(List.of(), generator.downsampleChartJson(null));
        assertEquals(List.of(), generator.downsampleChartJson(new File(tempFolder.getRoot(), "does-not-exist.json")));

        // Single point -> 1 bucket
        final File singleFile = tempFolder.newFile("single.json");
        Files.writeString(singleFile.toPath(), "[[1700000000000, 42.34, 40, 45, 1.0]]", StandardCharsets.UTF_8);
        assertEquals(List.of(Double.valueOf(42.3)), generator.downsampleChartJson(singleFile));

        // Malformed JSON -> graceful empty list
        final File badFile = tempFolder.newFile("bad.json");
        Files.writeString(badFile.toPath(), "{not-an-array", StandardCharsets.UTF_8);
        assertEquals(List.of(), generator.downsampleChartJson(badFile));
    }

    @Test
    public void copyTemplateResourcesAndUpdateScorecardBundles_handlesScorecardPresenceAndUpdates() throws Exception
    {
        final File templateDir = tempFolder.newFolder("template");
        assertTrue(new File(templateDir, "js").mkdirs());
        assertTrue(new File(templateDir, "fonts").mkdirs());
        Files.writeString(new File(templateDir, "index.html").toPath(), "<html>index</html>", StandardCharsets.UTF_8);
        Files.writeString(new File(templateDir, "scorecard.html").toPath(), "<html>scorecard</html>", StandardCharsets.UTF_8);
        Files.writeString(new File(templateDir, "js/nav.js").toPath(), "// nav", StandardCharsets.UTF_8);
        Files.writeString(new File(templateDir, "js/report-utils.js").toPath(), "// utils", StandardCharsets.UTF_8);
        Files.write(new File(templateDir, "fonts/roboto-latin.woff2").toPath(), new byte[] { 'w', 'O', 'F', '2' });

        final File reportDir = tempFolder.newFolder("report-sc");
        Files.writeString(new File(reportDir, "testreport.xml").toPath(),
                          "<?xml version=\"1.0\" encoding=\"UTF-8\"?><testreport>"
                              + "<configuration><projectName>Demo</projectName></configuration>"
                              + "<general><startTime>2026-08-31 12:00:00 UTC</startTime>"
                              + "<endTime>2026-08-31 12:05:00 UTC</endTime><duration>300</duration></general>"
                              + "</testreport>",
                          StandardCharsets.UTF_8);

        final ModernReportGenerator generator = new ModernReportGenerator();

        // First copy without scorecard: scorecard.html must be omitted, fonts must be copied
        generator.copyTemplateResources(templateDir, reportDir, false);
        generator.generateReportBundles(reportDir);
        assertTrue(new File(reportDir, "index.html").isFile());
        assertTrue(new File(reportDir, "js/nav.js").isFile());
        assertTrue(new File(reportDir, "js/report-utils.js").isFile());
        assertTrue(new File(reportDir, "fonts/roboto-latin.woff2").isFile());
        assertFalse(new File(reportDir, "scorecard.html").exists());
        assertFalse(new File(reportDir, "js/scorecard-data.js").exists());

        // Now write scorecard.xml and run updateScorecardBundles
        final File scorecardXml = new File(reportDir, "scorecard.xml");
        Files.writeString(scorecardXml.toPath(), """
            <?xml version="1.0" encoding="UTF-8"?>
            <scorecard>
              <configuration>
                <groups>
                  <group id="grp-1" name="LatencySLAs" mode="default" points="10" failsTest="true"/>
                </groups>
                <rules>
                  <rule id="rule-1" group-id="grp-1" enabled="true" points="10" failsTest="true">
                    <checks>
                      <check>
                        <selector>$.requests[0].mean</selector>
                        <condition>&lt;= 500</condition>
                      </check>
                    </checks>
                  </rule>
                </rules>
                <ratings>
                  <rating id="success" name="Success" value="100.0" enabled="true" failsTest="false"/>
                </ratings>
              </configuration>
              <outcome points="10" totalPoints="10" pointsPercentage="100.0" testFailed="false">
                <rating>success</rating>
                <groups>
                  <group ref-id="grp-1" result="passed" points="10" totalPoints="10" testFailed="false">
                    <rules>
                      <rule ref-id="rule-1" result="passed" points="10" totalPoints="10" testFailed="false"/>
                    </rules>
                  </group>
                </groups>
              </outcome>
            </scorecard>
            """, StandardCharsets.UTF_8);

        generator.updateScorecardBundles(scorecardXml, reportDir, templateDir);

        assertTrue(new File(reportDir, "scorecard.html").isFile());
        assertTrue(new File(reportDir, "js/scorecard-data.js").isFile());

        final Map<String, Object> updatedMeta = readJsBundle(new File(reportDir, "js/report-meta.js"), "XLT_META");
        @SuppressWarnings("unchecked")
        final Map<String, Object> scSummary = (Map<String, Object>) updatedMeta.get("scorecard");
        assertEquals(Boolean.TRUE, scSummary.get("present"));
        assertEquals(Boolean.FALSE, scSummary.get("testFailed"));
        assertEquals("PASSED", scSummary.get("result"));
        assertEquals("Success", scSummary.get("ratingName"));

        final Map<String, Object> scFull = readJsBundle(new File(reportDir, "js/scorecard-data.js"), "XLT_SCORECARD");
        assertEquals("PASSED", scFull.get("result"));
    }

    @Test
    public void testExtractXtcCommentStrongHeadline() throws Exception
    {
        final File reportDir = tempFolder.newFolder("xtc-comment-report");
        final String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <testreport>
              <configuration>
                <projectName>Aritzia</projectName>
                <comments>
                  <string>&lt;div class='xtc_comment'&gt;&lt;strong&gt;25% of 840k user sessions/h + 32k bot sessions/h&lt;/strong&gt;&lt;p&gt;DY off, 14k authorizeCustomer/min&lt;/p&gt;&lt;/div&gt;</string>
                </comments>
              </configuration>
              <general>
                <startTime>2026-02-02 23:00:12 CET</startTime>
                <endTime>2026-02-02 23:15:13 CET</endTime>
                <duration>901</duration>
              </general>
              <summary/>
              <transactions/>
              <actions/>
              <requests/>
              <errors/>
              <events/>
              <agents/>
            </testreport>
            """;
        Files.writeString(new File(reportDir, "testreport.xml").toPath(), xml, StandardCharsets.UTF_8);

        final ModernReportGenerator generator = new ModernReportGenerator();
        generator.generateReportBundles(reportDir);

        final Map<String, Object> meta = readJsBundle(new File(reportDir, "js/report-meta.js"), "XLT_META");
        assertEquals("25% of 840k user sessions/h + 32k bot sessions/h", meta.get("loadTestName"));
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
}