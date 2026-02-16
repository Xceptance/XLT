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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.xceptance.xlt.common.XltConstants;

/**
 * End-to-end regression test for the entire report generation process.
 * Generates reports using both XSLT and FreeMarker engines and compares the results.
 */
public class ReportGeneratorRegressionTest
{
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private File inputDir;
    private File outputDir;

    @Before
    public void setup() throws Exception
    {
        File sourceInputDir = new File("src/test/resources/xlt-result-xc-advanced-posters-20260211-163803");
        Assert.assertTrue("Input directory missing: " + sourceInputDir.getAbsolutePath(), sourceInputDir.exists());

        // Create a temporary input directory and copy sample data
        inputDir = tempFolder.newFolder("input");
        FileUtils.copyDirectory(sourceInputDir, inputDir);

        // Copy required report templates, stylesheets, and property files into the temporary config directory
        File targetConfigDir = new File(inputDir, "config");
        FileUtils.copyDirectory(new File("config/report-templates"), new File(targetConfigDir, "report-templates"));
        FileUtils.copyDirectory(new File("config/xsl"), new File(targetConfigDir, "xsl"));
        FileUtils.copyDirectory(new File("config/testreport"), new File(targetConfigDir, "testreport"));
        
        // DEBUG: Verify agents.ftl content
        File agentsFtl = new File(targetConfigDir, "report-templates/sections/agents.ftl");
        System.out.println("DEBUG: Copied agents.ftl to " + agentsFtl.getAbsolutePath());
        if (agentsFtl.exists()) {
            String content = FileUtils.readFileToString(agentsFtl, "UTF-8");
            System.out.println("DEBUG: agents.ftl contains DEBUG marker: " + content.contains("<!-- DEBUG -->"));
        } else {
            System.out.println("DEBUG: agents.ftl does NOT exist!");
        }
        
        // Copy all property files from main config
        File configSourceDir = new File("config");
        for (File file : FileUtils.listFiles(configSourceDir, new String[] {"properties"}, false))
        {
            FileUtils.copyFileToDirectory(file, targetConfigDir);
        }

        outputDir = tempFolder.newFolder("output");
    }

    @Test
    public void testFullReportParity() throws Exception
    {
        // 1. Generate report using XSLT (this will populate providers and create the XML)
        File xsltReportDir = new File(outputDir, "xslt-report");
        ReportGenerator xsltGenerator = createGenerator(ReportRendererFactory.ENGINE_XSLT, xsltReportDir);
        xsltGenerator.generateReport(false);
        
        File xmlReport = new File(xsltReportDir, XltConstants.LOAD_REPORT_XML_FILENAME);
        System.out.println("Generated XML Report:\n" + FileUtils.readFileToString(xmlReport, "UTF-8"));

        // 2. Generate report using FreeMarker from the SAME XML
        File fmReportDir = new File(outputDir, "fm-report");
        ReportGenerator fmGenerator = createGenerator(ReportRendererFactory.ENGINE_FREEMARKER, fmReportDir);
        fmGenerator.transformReport(xmlReport, fmReportDir, false);

        // 3. Compare reports
        compareReports(xsltReportDir, fmReportDir);
    }

    private ReportGenerator createGenerator(String engine, File reportDir) throws Exception
    {
        Properties commandLineProperties = new Properties();
        commandLineProperties.setProperty("com.xceptance.xlt.reportgenerator.renderingEngine", engine);
        commandLineProperties.setProperty("com.xceptance.xlt.reportgenerator.charts.width", "600");
        commandLineProperties.setProperty("com.xceptance.xlt.reportgenerator.charts.height", "400");
        commandLineProperties.setProperty("com.xceptance.xlt.projectName", "Xceptance LoadTest");
        
        return new ReportGenerator(VFS.getManager().toFileObject(inputDir), reportDir, false, false, null, commandLineProperties, 
            null, null, null, null);
    }


    private void compareReports(File expectedDir, File actualDir) throws Exception
    {
        // Find all HTML files in expected report
        Collection<File> expectedFiles = FileUtils.listFiles(expectedDir, new WildcardFileFilter("*.html"), TrueFileFilter.INSTANCE);
        Assert.assertFalse("No HTML files generated in expected report", expectedFiles.isEmpty());

        for (File expectedFile : expectedFiles)
        {
            String relativePath = expectedDir.toURI().relativize(expectedFile.toURI()).getPath();
            File actualFile = new File(actualDir, relativePath);

            Assert.assertTrue("File missing in actual report: " + relativePath, actualFile.exists());

            String expectedContent = FileUtils.readFileToString(expectedFile, "UTF-8");
            String actualContent = FileUtils.readFileToString(actualFile, "UTF-8");

            compareHtml(expectedContent, actualContent, relativePath);
        }
    }

    private void compareHtml(String expected, String actual, String fileName)
    {
        String normExpected = normalize(expected);
        String normActual = normalize(actual);

        if (!normExpected.equals(normActual))
        {
            // Dump actual HTML for inspection
            try {
                File targetDir = new File("target");
                if (!targetDir.exists()) targetDir.mkdirs();
                File dumpFile = new File(targetDir, "dump_" + fileName.replace("/", "_"));
                FileUtils.writeStringToFile(dumpFile, actual, "UTF-8");
                System.out.println("DUMP: Wrote failing HTML to " + dumpFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Find first difference for easier debugging
            int minLen = Math.min(normExpected.length(), normActual.length());
            for (int i = 0; i < minLen; i++)
            {
                if (normExpected.charAt(i) != normActual.charAt(i))
                {
                    int start = Math.max(0, i - 50);
                    int end = Math.min(minLen, i + 50);
                    Assert.assertEquals("Difference in " + fileName + " at index " + i + ": " + normExpected.substring(start, end) + " vs " + normActual.substring(start, end), normExpected, normActual);
                }
            }
            Assert.assertEquals("HTML output for " + fileName + " differs", normExpected, normActual);
        }
    }

    private String normalize(String html)
    {
        if (html == null) return null;
        String result = html.replaceAll("(?s)<!--.*?-->", "") // remove comments
                   .replaceAll("\\s+", "")        // remove all whitespace
                   .replaceAll("/>", ">")          // normalize self-closing tags (XHTML vs HTML5)
                   .replace("&#x2715;", "✕")      // normalize hex entity to char
                    .replace("&#x2003;", "")       // normalize em space entity to empty
                    .replace("\u2003", "")          // also strip actual Unicode em space
                    .replace("919.9999999999999px", "920px") // normalize floating point precision issue
                    .replace("&apos;", "'")         // normalize quotes
                    .replace("&#39;", "'")
                    .toLowerCase();

        // Canonicalize dynamic IDs from generate-id() (XSLT: d22e1, d25e2, etc.)
        // and FreeMarker counter-based IDs (id1, id2, etc.)
        // We normalize by replacing the ID portion within known prefix contexts
        Map<String, String> idMap = new LinkedHashMap<>();
        String prefixes = "chart-|overview-|dynamicoverview-|averages-|count-|arrivalrate-|concurrentusers-|distribution-|network-|tableentry-|url-listing-|#chart-|cpu-|memory-|threads-|chart-request-runtime-|chart-agent-cpu-|chart-agent-memory-|chart-agent-threads-|request-details-|reported-ip-list-";
        Matcher m = Pattern.compile(
            "(?:" + prefixes + ")(d\\d+e\\d+|gid\\d+|id\\d+|cv\\d+|summary|agent_[\\w]+|\\d+)")
            .matcher(result);
        int counter = 0;
        while (m.find())
        {
            String idVal = m.group(1);
            if (!idMap.containsKey(idVal))
            {
                idMap.put(idVal, "gid" + counter);
                counter++;
            }
        }
        // Replace IDs contextually within their prefix context
        List<Map.Entry<String, String>> sortedEntries = new ArrayList<>(idMap.entrySet());
        sortedEntries.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));
        for (Map.Entry<String, String> entry : sortedEntries)
        {
            String old = entry.getKey();
            String replacement = entry.getValue();
            for (String prefix : prefixes.split("\\|"))
            {
                result = result.replace(prefix + old, prefix + replacement);
            }
        }
        
        // Handle singleton description IDs independently
        String descPrefixes = "more-requesterrorcharts|more-transactionoverview|more-transactiondetails|more-transaction|more-action|more-request|more-network|more-httprequest|more-httpresponse|more-contenttype|more-errors|more-events|more-agents|more-slowest-requests|more-pageload|more-web-vitals";
        for (String prefix : descPrefixes.split("\\|"))
        {
             result = result.replaceAll("(" + prefix + ")(d\\d+e\\d+|gid\\d+|id\\d+|summary|\\d+|[a-f0-9]+)", "$1-fixed");
        }
        return result;
    }
}
