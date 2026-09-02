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
        Assert.assertTrue("schemaVersion missing", output.contains("schemaVersion: 1"));
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
                 <errors/>
               </testreport>
               """;
    }
}
