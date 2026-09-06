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
package com.xceptance.xlt.report;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.transform.TransformerFactory;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xceptance.common.io.FileUtils;
import com.xceptance.common.xml.XSLTUtils;

/**
 * Tests for the AI data stylesheet, {@code config/xsl/loadreport/ai-data.xsl}.
 * <p>
 * The stylesheet is XSLT 3.0, which the JDK's built-in processor cannot run. These tests transform a small report
 * through the same {@link XSLTUtils} the report generator uses, so a processor swap fails here rather than silently
 * producing a broken file in a customer's report.
 */
public class AiDataStylesheetTest
{
    private static final File STYLESHEET = new File("config/xsl/loadreport/ai-data.xsl");

    private static String output;

    @BeforeClass
    public static void transformSampleReport() throws Exception
    {
        Assert.assertTrue("Stylesheet not found - are the tests running from the project root? " +
                          STYLESHEET.getAbsolutePath(), STYLESHEET.isFile());

        final Path testDir = Files.createTempDirectory("aidatatest-");
        try
        {
            final File input = testDir.resolve("testreport.xml").toFile();
            final File result = testDir.resolve("ai-data.md").toFile();

            Files.write(input.toPath(), sampleReport().getBytes(StandardCharsets.UTF_8));
            XSLTUtils.transform(input, result, STYLESHEET);

            output = Files.readString(result.toPath(), StandardCharsets.UTF_8);
        }
        finally
        {
            FileUtils.deleteDirectoryRelaxed(testDir.toFile());
        }
    }

    @Test
    public void testProcessorIsXslt3Capable()
    {
        // Saxon is what makes the 3.0 features in the stylesheet work. If this ever resolves to something else, the
        // stylesheet stops working, and it should be obvious here rather than in a generated report.
        final String factory = TransformerFactory.newInstance().getClass().getName();

        Assert.assertTrue("Expected a Saxon TransformerFactory but got " + factory, factory.startsWith("net.sf.saxon."));
    }

    @Test
    public void testSchemaVersionAndUnitsDeclared()
    {
        Assert.assertTrue("schemaVersion missing", output.contains("schemaVersion: 2"));
        Assert.assertTrue("units block missing", output.contains("units:"));
        Assert.assertTrue("response time unit missing", output.contains("responseTimes: ms"));
        Assert.assertTrue("period unit missing", output.contains("periods: s"));
    }

    @Test
    public void testRampUpPeriodInHeader()
    {
        Assert.assertTrue("rampUpPeriod missing from header", output.contains("rampUpPeriod: 300"));
    }

    @Test
    public void testSingleTopLevelHeading()
    {
        Assert.assertEquals("There should be exactly one h1", 1, output.lines().filter(l -> l.startsWith("# ")).count());
        Assert.assertTrue("Sections should be h2", output.contains("\n## Transactions\n"));
    }

    @Test
    public void testNoStrayWhitespaceLines()
    {
        Assert.assertEquals("Whitespace-only lines leaked from the stylesheet indentation", 0,
                            output.lines().filter(l -> !l.isEmpty() && l.isBlank()).count());
    }

    @Test
    public void testCommentsAreStrippedAndBlockquoted()
    {
        Assert.assertTrue("Comment text lost", output.contains("Load Test"));
        Assert.assertFalse("HTML markup survived into the comment", output.contains("<div"));
        Assert.assertFalse("HTML markup survived into the comment", output.contains("<strong>"));

        final String commentLine = output.lines().filter(l -> l.contains("Load Test") && l.startsWith(">")).findFirst()
                                         .orElse(null);
        Assert.assertNotNull("Comment is not blockquoted", commentLine);
    }

    @Test
    public void testMedianDroppedWhenP50Present()
    {
        final String header = tableHeader("## Transactions");

        Assert.assertTrue("P50 column missing", header.contains("P50"));
        Assert.assertFalse("Median duplicates P50 and should be dropped: " + header, header.contains("Median"));
    }

    @Test
    public void testResponseTimesRoundedToWholeMilliseconds()
    {
        // the fixture carries mean 31529.106, which must not reach the output with its decimals
        Assert.assertTrue("Mean not rounded", output.contains("| 31529 |"));
        Assert.assertFalse("Unrounded value in output", output.contains("31529.106"));
    }

    @Test
    public void testRatesKeepTwoDecimalsWithoutTrailingZeros()
    {
        Assert.assertTrue("Rate not formatted", output.contains("| 1.46 |"));
        Assert.assertFalse("Trailing zeros in output", output.contains("0.000"));
    }

    @Test
    public void testNoProjectedCounts()
    {
        // per-minute, per-hour and per-day counts are linear extrapolations that read as measurements
        Assert.assertFalse("Projection leaked into the output", output.contains("Count/min"));
        Assert.assertFalse("Projection leaked into the output", output.contains("Count/h"));
        Assert.assertFalse("Projection leaked into the output", output.contains("Count/d"));
    }

    @Test
    public void testNoGroupingSeparatorsInNumbers()
    {
        // the fixture's user count is written as "1,021" by the load function converter
        Assert.assertTrue("User count missing", output.contains("| 1021 |"));
        Assert.assertFalse("Grouping separator in a numeric cell", output.contains("1,021"));
    }

    @Test
    public void testConstantLoadProfileColumnsHoisted()
    {
        // both fixture test cases share a shutdown period, so it is stated once instead of repeated
        Assert.assertTrue("Constants not hoisted", output.contains("Same for every test case:"));
        Assert.assertTrue("Shutdown not hoisted", output.contains("shutdown 0 s"));

        final String header = tableHeader("## Load Profile");
        Assert.assertFalse("Shutdown should not be a column: " + header, header.contains("Shutdown"));
    }

    @Test
    public void testVaryingLoadProfileColumnsKept()
    {
        // measurement and ramp-up differ between the two fixture cases, so they stay as columns
        final String header = tableHeader("## Load Profile");

        Assert.assertTrue("Measurement varies and must stay: " + header, header.contains("Measurement"));
        Assert.assertTrue("Ramp-Up varies and must stay: " + header, header.contains("Ramp-Up"));
    }

    @Test
    public void testLoadFunctionShapeKept()
    {
        // min and max alone cannot tell a step profile from a smooth ramp
        final String header = tableHeader("## Load Profile");

        Assert.assertTrue("Shape column missing: " + header, header.contains("Arrival Rate Over Time"));
        Assert.assertTrue("Shape lost", output.contains("| 0:1 600:500 1200:100 |"));
        Assert.assertTrue("Shape not explained", output.contains("second:value"));
    }

    @Test
    public void testZeroCountSummaryScopesDropped()
    {
        // "zero page loads were recorded" is a different claim from "page loads were not measured"
        Assert.assertTrue("Summary missing", output.contains("| All Transactions |"));
        Assert.assertFalse("Zero row emitted", output.contains("All Page Load Timings"));
    }

    @Test
    public void testLabelsExplanationOnlyWithTheColumn()
    {
        // the fixture labels its requests, so the paragraph belongs here and must not hedge
        Assert.assertTrue("Labels not explained", output.contains("The Labels column comes from"));
        Assert.assertFalse("Hedged wording survived", output.contains("When the run assigns labels"));
    }

    @Test
    public void testUnusedIterationsColumnDropped()
    {
        final String header = tableHeader("## Load Profile");

        Assert.assertFalse("Iterations is zero for every row and should be dropped: " + header,
                           header.contains("Iterations"));
    }

    @Test
    public void testEventsOrderedByCountDescending()
    {
        final int loud = output.indexOf("| LoudEvent |");
        final int quiet = output.indexOf("| QuietEvent |");

        Assert.assertTrue("Both events should be present", loud > 0 && quiet > 0);
        Assert.assertTrue("The more frequent event should come first", loud < quiet);
    }

    @Test
    public void testXtcIdentityOmittedWhenAbsent()
    {
        // the fixture has no XTC properties, so the block must not appear at all
        Assert.assertFalse("XTC block emitted for a non-XTC run", output.contains("xtc:"));
    }

    @Test
    public void testRequestLabelsColumn()
    {
        final String header = tableHeader("## Requests");

        Assert.assertTrue("Labels column missing: " + header, header.contains("| Labels |"));
        Assert.assertTrue("Label values missing", output.contains("| Homepage | checkout guest | 100 |"));
    }

    @Test
    public void testLabelsOnOtherTimerTables()
    {
        // labeling rules can label any timer, so the shared table carries the column too
        final String header = tableHeader("## Transactions");

        Assert.assertTrue("Labels column missing from transactions: " + header, header.contains("| Labels |"));
        Assert.assertTrue("Label value missing", output.contains("| TFixture | checkout | 5250 |"));
    }

    @Test
    public void testLabelsColumnOmittedWhenUnused()
    {
        // the fixture's events have no labels, and nothing there should gain an empty column
        Assert.assertFalse("Empty labels column emitted", tableHeader("## Events").contains("Labels"));
    }

    @Test
    public void testUniformlyZeroNetworkColumnsDropped()
    {
        final String header = tableHeader("## Requests");

        // dns is 0.318 in the fixture - non-zero, but it renders as 0, and a column of zeros is
        // noise whatever the underlying value was
        Assert.assertFalse("DNS rounds to zero everywhere and should be dropped: " + header,
                           header.contains("DNS"));
        Assert.assertFalse("Connect should be dropped: " + header, header.contains("Connect"));
        Assert.assertTrue("ServerBusy carries data and must stay: " + header, header.contains("ServerBusy"));
    }

    @Test
    public void testTimeSeriesIsOneCombinedTable()
    {
        Assert.assertTrue("Time series section missing", output.contains("## Time Series"));

        final String header = tableHeader("## Time Series");

        Assert.assertTrue("Elapsed column missing: " + header, header.contains("Elapsed"));
        Assert.assertTrue("Wall clock column missing: " + header, header.contains("Time"));
        Assert.assertTrue("Transaction columns missing: " + header, header.contains("Transaction Mean"));
        Assert.assertTrue("Action column missing: " + header, header.contains("Action Mean"));
        Assert.assertTrue("Request columns missing: " + header, header.contains("Request Mean"));
        Assert.assertTrue("Error rate column missing: " + header, header.contains("Transaction Errors/s"));
    }

    @Test
    public void testTimeSeriesHeadersAreNotAbbreviated()
    {
        final String header = tableHeader("## Time Series");

        Assert.assertFalse("Abbreviated header: " + header, header.contains("Txn"));
        Assert.assertFalse("Abbreviated header: " + header, header.contains("Act "));
        Assert.assertFalse("Abbreviated header: " + header, header.contains("Req "));
    }

    @Test
    public void testTimeSeriesClockTimesAreDistinct()
    {
        // a short test gets sub-minute buckets, so minute resolution here would repeat itself
        Assert.assertTrue("First bucket clock time missing", output.contains("| 0 | 10:00:00 |"));
        Assert.assertTrue("Second bucket clock time missing", output.contains("| 30 | 10:00:30 |"));
    }

    @Test
    public void testTimeSeriesMetadataStated()
    {
        // the interval varies per report, so it has to be stated rather than inferred from the timestamps
        Assert.assertTrue("interval missing", output.contains("interval: 30"));
        Assert.assertTrue("buckets missing", output.contains("buckets: 2"));
        Assert.assertTrue("sourceResolution missing", output.contains("sourceResolution: 4"));
    }

    @Test
    public void testTimeSeriesRowsCarryAllThreeScopes()
    {
        Assert.assertTrue("Row not rendered as expected:\n" + output,
                          output.contains("| 30 | 10:00:30 | 41683 | 22.5 | 0.02 | 640 | 121 | 201.4 |"));
    }

    @Test
    public void testSummarySection()
    {
        Assert.assertTrue("Summary section missing", output.contains("## Summary"));
        Assert.assertTrue("Summary row missing", output.contains("| All Transactions | 5250 | 1.46 | 903 | 17.2 |"));
    }

    @Test
    public void testErrorsGroupedByMessage()
    {
        // two entries share "Common problem" and must appear as one group carrying their combined count
        Assert.assertEquals("Message should be grouped, not repeated", 1,
                            output.lines().filter(l -> l.startsWith("### ") && l.contains("Common problem")).count());
        Assert.assertTrue("Combined count missing", output.contains("Common problem - 900"));
    }

    @Test
    public void testErrorsOrderedByDescendingCount()
    {
        final int common = output.indexOf("### 1. Common problem");
        final int rare = output.indexOf("### 2. Rare problem");

        Assert.assertTrue("The larger group should be first, got:\n" + output, common > 0 && rare > common);
    }

    @Test
    public void testErrorCountersPresent()
    {
        Assert.assertTrue("totalErrors missing", output.contains("totalErrors: 903"));
        Assert.assertTrue("distinctEntries missing", output.contains("distinctEntries: 3"));
        Assert.assertTrue("distinctMessages missing", output.contains("distinctMessages: 2"));
        Assert.assertTrue("tracesIncludedFor missing", output.contains("tracesIncludedFor: 2"));
    }

    @Test
    public void testStackTracesTrimmedToLeadingFrames()
    {
        // the fixture's biggest group has 11 trace lines, the limit is 8
        Assert.assertTrue("Trace not trimmed", output.contains("... 3 more frames"));
        Assert.assertFalse("Frame beyond the limit survived", output.contains("b.B.f9"));
        Assert.assertTrue("Leading frame missing", output.contains("b.B.f1(B.java:1)"));
    }

    @Test
    public void testResponseCodesWithShare()
    {
        Assert.assertTrue("Response code section missing", output.contains("## Response Codes"));
        Assert.assertTrue("404 count or share wrong", output.contains("| 404 | Not Found | 250 | 4.76 |"));
    }

    /**
     * Returns the header row of the table that follows the given section heading.
     */
    private String tableHeader(final String heading)
    {
        final int start = output.indexOf(heading);
        Assert.assertTrue("Section not found: " + heading, start >= 0);

        return output.substring(start).lines().filter(l -> l.startsWith("| ")).findFirst().orElse("");
    }

    private static String sampleReport()
    {
        return """
               <?xml version="1.0" encoding="UTF-8"?>
               <testreport>
                 <configuration>
                   <projectName>Fixture</projectName>
                   <version>
                     <productName>Xceptance LoadTest</productName>
                     <version>10.0.0</version>
                   </version>
                   <comments>
                     <string>&lt;div class="markdown"&gt;&lt;strong&gt;Load Test&lt;/strong&gt;&lt;/div&gt;</string>
                   </comments>
                   <loadProfile>
                     <testCase>
                       <arrivalRateMin>1</arrivalRateMin>
                       <arrivalRateMax>3535</arrivalRateMax>
                       <arrivalRate>1...3,535</arrivalRate>
                       <numberOfUsersMin>1021</numberOfUsersMin>
                       <numberOfUsersMax>1021</numberOfUsersMax>
                       <numberOfUsers>1,021</numberOfUsers>
                       <numberOfIterations>0</numberOfIterations>
                       <measurementPeriod>3600</measurementPeriod>
                       <rampUpPeriod>300</rampUpPeriod>
                       <shutdownPeriod>0</shutdownPeriod>
                       <userName>TFixture</userName>
                     </testCase>
                     <testCase>
                       <arrivalRateMin>1</arrivalRateMin>
                       <arrivalRateMax>500</arrivalRateMax>
                       <arrivalRateProfile>0:1 600:500 1200:100</arrivalRateProfile>
                       <arrivalRate>1...500</arrivalRate>
                       <numberOfUsersMin>10</numberOfUsersMin>
                       <numberOfUsersMax>10</numberOfUsersMax>
                       <numberOfUsers>10</numberOfUsers>
                       <numberOfIterations>0</numberOfIterations>
                       <measurementPeriod>1800</measurementPeriod>
                       <rampUpPeriod>120</rampUpPeriod>
                       <shutdownPeriod>0</shutdownPeriod>
                       <userName>TStepped</userName>
                     </testCase>
                   </loadProfile>
                 </configuration>
                 <general>
                   <bytesSent>100</bytesSent>
                   <bytesReceived>200</bytesReceived>
                   <hits>5250</hits>
                   <startTime>2026-01-01 10:00:00 CET</startTime>
                   <endTime>2026-01-01 11:00:00 CET</endTime>
                   <duration>3600</duration>
                 </general>
                 <transactions>
                   <transaction>
                     <name>TFixture</name>
                     <labels>checkout</labels>
                     <count>5250</count>
                     <countPerSecond>1.458</countPerSecond>
                     <errors>0</errors>
                     <errorPercentage>0.000</errorPercentage>
                     <min>24184</min>
                     <max>46949</max>
                     <median>31525.000</median>
                     <mean>31529.106</mean>
                     <deviation>2361.687</deviation>
                     <percentiles>
                       <p50>31525.000</p50>
                       <p95>35420.000</p95>
                     </percentiles>
                   </transaction>
                 </transactions>
                 <events>
                   <event>
                     <testCaseName>TFixture</testCaseName>
                     <name>QuietEvent</name>
                     <totalCount>2</totalCount>
                   </event>
                   <event>
                     <testCaseName>TFixture</testCaseName>
                     <name>LoudEvent</name>
                     <totalCount>900</totalCount>
                   </event>
                 </events>
                 <requests>
                   <request>
                     <name>Homepage</name>
                     <labels>checkout guest</labels>
                     <count>100</count>
                     <countPerSecond>0.5</countPerSecond>
                     <errors>0</errors>
                     <errorPercentage>0.000</errorPercentage>
                     <min>10</min>
                     <max>90</max>
                     <median>40.000</median>
                     <mean>44.000</mean>
                     <deviation>5.000</deviation>
                     <percentiles>
                       <p50>40.000</p50>
                       <p95>80.000</p95>
                     </percentiles>
                     <bytesSent><mean>100</mean></bytesSent>
                     <bytesReceived><mean>200</mean></bytesReceived>
                     <dnsTime><mean>0.318</mean></dnsTime>
                     <connectTime><mean>0.000</mean></connectTime>
                     <sendTime><mean>0.000</mean></sendTime>
                     <serverBusyTime><mean>30.000</mean></serverBusyTime>
                     <receiveTime><mean>4.000</mean></receiveTime>
                     <timeToFirstBytes><mean>30.000</mean></timeToFirstBytes>
                   </request>
                 </requests>
                 <summary>
                   <transactions>
                     <name>All Transactions</name>
                     <count>5250</count>
                     <countPerSecond>1.458</countPerSecond>
                     <errors>903</errors>
                     <errorPercentage>17.2</errorPercentage>
                   </transactions>
                   <pageLoadTimings>
                     <name>All Page Load Timings</name>
                     <count>0</count>
                     <countPerSecond>0</countPerSecond>
                     <errors>0</errors>
                     <errorPercentage>0</errorPercentage>
                   </pageLoadTimings>
                   <timeSeries>
                     <interval>30</interval>
                     <buckets>2</buckets>
                     <sourceResolution>4</sourceResolution>
                     <rows>
                       <row>
                         <elapsed>0</elapsed>
                         <time>2026-01-01 10:00:00 CET</time>
                         <transactionMean>31529</transactionMean>
                         <transactionCountPerSecond>0.5</transactionCountPerSecond>
                         <transactionErrorsPerSecond>0.0</transactionErrorsPerSecond>
                         <actionMean>512</actionMean>
                         <requestMean>98</requestMean>
                         <requestCountPerSecond>20.1</requestCountPerSecond>
                       </row>
                       <row>
                         <elapsed>30</elapsed>
                         <time>2026-01-01 10:00:30 CET</time>
                         <transactionMean>41683</transactionMean>
                         <transactionCountPerSecond>22.5</transactionCountPerSecond>
                         <transactionErrorsPerSecond>0.02</transactionErrorsPerSecond>
                         <actionMean>640</actionMean>
                         <requestMean>121</requestMean>
                         <requestCountPerSecond>201.4</requestCountPerSecond>
                       </row>
                     </rows>
                   </timeSeries>
                 </summary>
                 <responseCodes>
                   <responseCode>
                     <code>200</code>
                     <statusText>OK</statusText>
                     <count>5000</count>
                   </responseCode>
                   <responseCode>
                     <code>404</code>
                     <statusText>Not Found</statusText>
                     <count>250</count>
                   </responseCode>
                 </responseCodes>
                 <errors>
                   <error>
                     <count>3</count>
                     <testCaseName>TFixture</testCaseName>
                     <actionName>RareAction</actionName>
                     <message>Rare problem</message>
                     <trace>java.lang.AssertionError: Rare problem
               	at a.A.one(A.java:1)
               	at a.A.two(A.java:2)</trace>
                   </error>
                   <error>
                     <count>500</count>
                     <testCaseName>TFixture</testCaseName>
                     <actionName>HotAction</actionName>
                     <message>Common problem</message>
                     <trace>java.lang.AssertionError: Common problem
               	at b.B.f1(B.java:1)
               	at b.B.f2(B.java:2)
               	at b.B.f3(B.java:3)
               	at b.B.f4(B.java:4)
               	at b.B.f5(B.java:5)
               	at b.B.f6(B.java:6)
               	at b.B.f7(B.java:7)
               	at b.B.f8(B.java:8)
               	at b.B.f9(B.java:9)
               	at b.B.f10(B.java:10)</trace>
                   </error>
                   <error>
                     <count>400</count>
                     <testCaseName>TOther</testCaseName>
                     <actionName>HotAction</actionName>
                     <message>Common problem</message>
                     <trace>java.lang.AssertionError: Common problem
               	at b.B.f1(B.java:1)</trace>
                   </error>
                 </errors>
               </testreport>
               """;
    }
}
