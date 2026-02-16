package com.xceptance.xlt.report;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Parity test to ensure FreeMarker output matches legacy XSLT output.
 */
public class FreeMarkerParityTest
{
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private File testReportXml;
    private File outputDir;
    private ReportGeneratorConfiguration config;

    @Before
    public void setup() throws Exception
    {
        // Use an existing sample report for testing
        testReportXml = new File("reports/xlt-result-xc-advanced-posters-20250610-093403/testreport.xml");
        if (!testReportXml.exists())
        {
             // Fallback to demo-external-data if posters one is missing (though find showed it exists)
             testReportXml = new File("samples/demo-external-data/reports/20110621-101041/testreport.xml");
        }
        
        outputDir = tempFolder.newFolder("output");
        
        // Inject configuration into XML so XSLT engine finds chart dimensions
        String xmlContent = FileUtils.readFileToString(testReportXml, "UTF-8");
        String chartConfig = "<chartWidth>600</chartWidth><chartHeight>400</chartHeight>";
        
        if (xmlContent.contains("<configuration>"))
        {
             // Inject inside existing config
             xmlContent = xmlContent.replace("</configuration>", chartConfig + "</configuration>");
        }
        else
        {
             // Inject new config block
             String configXml = "<configuration>" + chartConfig + "</configuration>";
             // Insert after <testreport>
             xmlContent = xmlContent.replaceFirst("<testreport>", "<testreport>" + configXml);
        }
        
        // Save as new temp file
        File tempXml = tempFolder.newFile("testreport-with-config.xml");
        FileUtils.writeStringToFile(tempXml, xmlContent, "UTF-8");
        testReportXml = tempXml;
        
        Properties props = new Properties();
        // Force FreeMarker for the FM renderer test
        props.setProperty("com.xceptance.xlt.reportgenerator.renderingEngine", "freemarker");
        props.setProperty("com.xceptance.xlt.reportgenerator.charts.width", "600");
        props.setProperty("com.xceptance.xlt.reportgenerator.charts.height", "400");
        
        // Define only the overview transformation to avoid errors about missing templates for other sections
        props.setProperty("com.xceptance.xlt.reportgenerator.transformations.1.styleSheetFileName", "index.xsl");
        props.setProperty("com.xceptance.xlt.reportgenerator.transformations.1.templateFileName", "index.ftl");
        props.setProperty("com.xceptance.xlt.reportgenerator.transformations.1.outputFileName", "index.html");
        
        File homeDir = new File(".");
        File configDir = new File("config");
        
        // We need to make sure we don't load the default reportgenerator.properties from configDir
        // because it contains many other transformations that would fail.
        // But ReportGeneratorConfiguration might load it anyway.
        // Let's use a temporary config dir with only our properties.
        File tempConfigDir = tempFolder.newFolder("config");
        
        // Copy all existing properties to satisfy requirements like mastercontroller.properties
        File[] propFiles = configDir.listFiles((dir, name) -> name.endsWith(".properties"));
        if (propFiles != null)
        {
            for (File f : propFiles)
            {
                FileUtils.copyFileToDirectory(f, tempConfigDir);
            }
        }

        // Overwrite reportgenerator.properties with our minimal version
        File tempPropsFile = new File(tempConfigDir, "reportgenerator.properties");
        FileUtils.writeStringToFile(tempPropsFile, "com.xceptance.xlt.reportgenerator.transformations.1.styleSheetFileName = index.xsl\n" +
                                                   "com.xceptance.xlt.reportgenerator.transformations.1.templateFileName = index.ftl\n" +
                                                   "com.xceptance.xlt.reportgenerator.transformations.1.outputFileName = index.html\n" +
                                                   "com.xceptance.xlt.reportgenerator.charts.width = 600\n" +
                                                   "com.xceptance.xlt.reportgenerator.charts.height = 400\n" +
                                                   "com.xceptance.xlt.reportgenerator.runtimePercentiles = 50, 95, 99, 99.9\n" +
                                                   "com.xceptance.xlt.reportgenerator.runtimeIntervalBoundaries = 1000, 3000, 5000\n", "UTF-8");

        // Copy the xsl and testreport directories to the temp config dir so the renderers can find them
        FileUtils.copyDirectory(new File(configDir, "xsl"), new File(tempConfigDir, "xsl"));
        FileUtils.copyDirectory(new File(configDir, "testreport"), new File(tempConfigDir, "testreport"));
        FileUtils.copyDirectory(new File(configDir, "report-templates"), new File(tempConfigDir, "report-templates"));

        System.out.println("DEBUG: Using configDir=" + configDir.getAbsolutePath());
        System.out.println("DEBUG: tempConfigDir=" + tempConfigDir.getAbsolutePath());
        System.out.println("DEBUG: testReportXml=" + testReportXml.getAbsolutePath());

        config = new ReportGeneratorConfiguration(homeDir, tempConfigDir, props, null, null);
    }

    private void debugParams(Map<String, Object> params) {
        System.out.println("DEBUG: params.productName=" + params.get("productName"));
        System.out.println("DEBUG: params.productVersion=" + params.get("productVersion"));
        System.out.println("DEBUG: params.productUrl=" + params.get("productUrl"));
    }

    @Test
    public void testOverviewParity() throws Exception
    {
        // 1. Render using XSLT
        ReportRenderer xsltRenderer = ReportRendererFactory.createRenderer(ReportRendererFactory.ENGINE_XSLT, config);
        File xsltDir = new File(outputDir, "xslt");
        xsltDir.mkdirs();
        
        Map<String, Object> params = new HashMap<>();
        params.put("productName", "Xceptance LoadTest");
        params.put("productVersion", "?.?.?");
        params.put("productUrl", "http://www.xceptance-loadtest.com/");
        params.put("scorecardPresent", false);
        
        xsltRenderer.render(testReportXml, xsltDir, params);
        
        // 2. Render using FreeMarker
        ReportRenderer fmRenderer = ReportRendererFactory.createRenderer(ReportRendererFactory.ENGINE_FREEMARKER, config);
        File fmDir = new File(outputDir, "freemarker");
        fmDir.mkdirs();
        
        fmRenderer.render(testReportXml, fmDir, params);
        
        // 3. Compare index.html
        File xsltIndex = new File(xsltDir, "index.html");
        File fmIndex = new File(fmDir, "index.html");
        
        Assert.assertTrue("XSLT output missing", xsltIndex.exists());
        Assert.assertTrue("FreeMarker output missing", fmIndex.exists());
        
        String xsltContent = FileUtils.readFileToString(xsltIndex, "UTF-8");
        String fmContent = FileUtils.readFileToString(fmIndex, "UTF-8");
        
        compareHtml(xsltContent, fmContent);
    }

    @Test
    public void testTransactionsParity() throws Exception
    {
        renderAndCompare("transactions.xsl", "transactions.ftl", "transactions.html");
    }

    @Test
    public void testActionsParity() throws Exception
    {
        renderAndCompare("actions.xsl", "actions.ftl", "actions.html");
    }

    @Test
    public void testRequestsParity() throws Exception
    {
        renderAndCompare("requests.xsl", "requests.ftl", "requests.html");
    }

    @Test
    public void testNetworkParity() throws Exception
    {
        Map<String, Object> params = new HashMap<>();
        params.put("productUrl", "http://www.xceptance-loadtest.com/");
        params.put("productName", "Xceptance LoadTest");
        params.put("productVersion", "?.?.?");
        renderAndCompare("network.xsl", "network.ftl", "network.html", params);
    }

    @Test
    public void testCustomTimersParity() throws Exception
    {
        renderAndCompare("custom-timers.xsl", "custom-timers.ftl", "custom-timers.html");
    }

    @Test
    public void testCustomValuesParity() throws Exception
    {
        renderAndCompare("custom-values.xsl", "custom-values.ftl", "custom-values.html");
    }

    @Test
    public void testErrorsParity() throws Exception
    {
        renderAndCompare("errors.xsl", "errors.ftl", "errors.html");
    }

    @Test
    public void testErrorsParityWithLinks() throws Exception
    {
        // Create a config with links enabled
        Properties props = new Properties();
        props.setProperty("com.xceptance.xlt.reportgenerator.renderingEngine", "freemarker");
        props.setProperty("com.xceptance.xlt.reportgenerator.linkToResultBrowsers", "true");
        props.setProperty("com.xceptance.xlt.reportgenerator.resultsBaseUri", "http://example.com/results");
        
        // We reuse the temp config dir from setup
        File tempConfigDir = new File(outputDir.getParentFile(), "config");
        ReportGeneratorConfiguration linkConfig = new ReportGeneratorConfiguration(new File("."), tempConfigDir, props, null, null);

        // Create a test report XML with errors and resultsPathPrefix
        String xmlContent = FileUtils.readFileToString(testReportXml, "UTF-8");
        String errorsXml = "<errors>" +
                           "  <resultsPathPrefix>http://example.com/results/</resultsPathPrefix>" +
                           "  <error>" +
                           "    <message>Test Error</message>" +
                           "    <count>1</count>" +
                           "    <trace>Stacktrace...</trace>" +
                           "    <testCaseName>TTestCase</testCaseName>" +
                           "    <actionName>Action</actionName>" +
                           "    <detailChartID>0</detailChartID>" +
                           "    <directoryHints>" +
                           "      <string>hint1</string>" +
                           "    </directoryHints>" +
                           "  </error>" +
                           "</errors>";
        String modifiedXml = xmlContent.replace("</testreport>", errorsXml + "</testreport>");
        File tempReportXml = new File(outputDir.getParentFile(), "testreport-with-errors.xml");
        FileUtils.writeStringToFile(tempReportXml, modifiedXml, "UTF-8");
        
        // Render using mocked XML
        renderAndCompare("errors.xsl", "errors.ftl", "errors-links.html", null, linkConfig, tempReportXml);
    }

    @Test
    public void testEventsParity() throws Exception
    {
        renderAndCompare("events.xsl", "events.ftl", "events.html");
    }

    @Test
    public void testConfigurationParity() throws Exception
    {
        renderAndCompare("configuration.xsl", "configuration.ftl", "configuration.html");
    }

    @Test
    public void testAgentsParity() throws Exception
    {
        renderAndCompare("agents.xsl", "agents.ftl", "agents.html");
    }

    @Test
    public void testExternalParity() throws Exception
    {
        renderAndCompare("external.xsl", "external.ftl", "external.html");
    }

    @Test
    public void testPageLoadTimingsParity() throws Exception
    {
        renderAndCompare("page-load-timings.xsl", "page-load-timings.ftl", "page-load-timings.html");
    }

    @Test
    public void testSlowestRequestsParity() throws Exception
    {
        renderAndCompare("slowest-requests.xsl", "slowest-requests.ftl", "slowest-requests.html");
    }

    @Test
    public void testWebVitalsParity() throws Exception
    {
        renderAndCompare("web-vitals.xsl", "web-vitals.ftl", "web-vitals.html");
    }

    @Test
    public void testScorecardParity() throws Exception
    {
        Map<String, Object> params = new HashMap<>();
        params.put("scorecardPresent", true);
        params.put("projectName", "xltproject");
        params.put("productName", "Xceptance LoadTest");
        params.put("productVersion", "?.?.?");
        params.put("productUrl", "http://www.xceptance-loadtest.com/");
        
        // Create a minimal scorecard.xml with necessary structure
        // Note: configuration must precede outcome for XSLT's preceding-sibling lookup
        String scorecardXml = "<scorecard>" +
                              "  <configuration>" +
                              "    <ratings>" +
                              "      <rating id=\"r1\" description=\"Good\" failsTest=\"false\">" +
                              "        <description>Good</description>" +
                              "      </rating>" +
                              "    </ratings>" +
                              "    <groups>" +
                              "      <group id=\"g1\" name=\"Group 1\" description=\"Group 1 Description\" failsTest=\"false\" enabled=\"true\">" +
                              "        <description>Group 1 Description</description>" +
                              "        <rules>" +
                              "          <rule ref-id=\"ru1\"/>" +
                              "        </rules>" +
                              "      </group>" +
                              "    </groups>" +
                              "    <rules>" +
                              "      <rule id=\"ru1\" name=\"Rule 1\" description=\"Rule 1 Description\" failsTest=\"true\" enabled=\"true\">" +
                              "        <description>Rule 1 Description</description>" +
                              "      </rule>" +
                              "    </rules>" +
                              "  </configuration>" +
                              "  <outcome points=\"100\" totalPoints=\"100\" pointsPercentage=\"100\" rating=\"r1\" testFailed=\"false\">" +
                              "    <groups>" +
                              "      <group name=\"Group 1\" ref-id=\"g1\" description=\"Group desc\" enabled=\"true\" testFailed=\"false\">" +
                              "        <rules>" +
                              "          <rule ref-id=\"ru1\" testFailed=\"false\"/>" +
                              "        </rules>" +
                              "      </group>" +
                              "    </groups>" +
                              "    <rating>r1</rating>" +
                              "  </outcome>" +
                              "</scorecard>";
                              
        File scorecardFile = tempFolder.newFile("scorecard.xml");
        FileUtils.writeStringToFile(scorecardFile, scorecardXml, "UTF-8");

        // The XSLT file is in xsl/scorecard/index.xsl, relative to xsl/loadreport it is ../scorecard/index.xsl
        renderAndCompare("../scorecard/index.xsl", "sections/scorecard.ftl", "scorecard.html", params, config, scorecardFile);
    }

    private void renderAndCompare(String xslFile, String ftlFile, String outputFileName) throws Exception
    {
        renderAndCompare(xslFile, ftlFile, outputFileName, null, config);
    }

    private void renderAndCompare(String xslFile, String ftlFile, String outputFileName, Map<String, Object> extraParams) throws Exception
    {
        renderAndCompare(xslFile, ftlFile, outputFileName, extraParams, config);
    }

    private void renderAndCompare(String xslFile, String ftlFile, String outputFileName, Map<String, Object> extraParams, ReportGeneratorConfiguration config) throws Exception
    {
        renderAndCompare(xslFile, ftlFile, outputFileName, extraParams, config, this.testReportXml);
    }

    private void renderAndCompare(String xslFile, String ftlFile, String outputFileName, Map<String, Object> extraParams, ReportGeneratorConfiguration config, File xmlFile) throws Exception
    {
        // Render using XSLT
        File xsltDir = new File(outputDir, "xslt-" + outputFileName);
        xsltDir.mkdirs();

        Map<String, Object> params = new HashMap<>();
        params.put("productName", "Xceptance LoadTest");
        params.put("productVersion", "?.?.?");
        params.put("productUrl", "http://www.xceptance-loadtest.com/");
        params.put("projectName", "XLT Project");
        params.put("scorecardPresent", false);
        
        if (extraParams != null)
        {
            params.putAll(extraParams);
        }

        debugParams(params);

        ReportRenderer xsltRenderer = ReportRendererFactory.createRenderer(ReportRendererFactory.ENGINE_XSLT, config);
        File xslStyleSheet = new File(new File(config.getConfigDirectory(), "xsl/loadreport"), xslFile);
        xsltRenderer.render(xmlFile, new File(xsltDir, outputFileName), xslStyleSheet.getAbsolutePath(), params);

        // Render using FreeMarker
        File fmDir = new File(outputDir, "freemarker-" + outputFileName);
        fmDir.mkdirs();

        ReportRenderer fmRenderer = ReportRendererFactory.createRenderer(ReportRendererFactory.ENGINE_FREEMARKER, config);
        fmRenderer.render(xmlFile, new File(fmDir, outputFileName), ftlFile, params);

        // Compare
        File xsltFile = new File(xsltDir, outputFileName);
        File fmFile = new File(fmDir, outputFileName);

        Assert.assertTrue("XSLT output missing: " + xsltFile.getAbsolutePath(), xsltFile.exists());
        Assert.assertTrue("FreeMarker output missing: " + fmFile.getAbsolutePath(), fmFile.exists());

        String xsltContent = FileUtils.readFileToString(xsltFile, "UTF-8");
        String fmContent = FileUtils.readFileToString(fmFile, "UTF-8");

        // Debug: write both outputs
        FileUtils.writeStringToFile(new File("target/debug-xslt-" + outputFileName), xsltContent, "UTF-8");
        FileUtils.writeStringToFile(new File("target/debug-fm-" + outputFileName), fmContent, "UTF-8");

        compareHtml(xsltContent, fmContent);
    }

    private void compareHtml(String expected, String actual)
    {
        // Normalize: remove all whitespace and newlines for parity check
        // Also remove comments as they might differ in formatting
        String normExpected = normalize(expected);
        String normActual = normalize(actual);
        
        if (!normExpected.equals(normActual))
        {
            // Find first difference for easier debugging
            int minLen = Math.min(normExpected.length(), normActual.length());
            for (int i = 0; i < minLen; i++)
            {
                if (normExpected.charAt(i) != normActual.charAt(i))
                {
                    int start = Math.max(0, i - 20);
                    int end = Math.min(minLen, i + 20);
                    Assert.assertEquals("Difference at index " + i + ": " + normExpected.substring(start, end) + " vs " + normActual.substring(start, end), normExpected, normActual);
                }
            }
            Assert.assertEquals("HTML output differs in length", normExpected, normActual);
        }
    }
    
    private String normalize(String html)
    {
        if (html == null) return null;
        String result = html.replaceAll("(?s)<!--.*?-->", "") // remove comments
                   .replaceAll("\\s+", "")        // remove all whitespace
                   .replaceAll("/>", ">")          // normalize self-closing tags (XHTML vs HTML5)
                   .replace("&#x2715;", "✕")      // normalize hex entity to char
                   .replace("&#x2003;", "")       // normalize em space entity to empty (matching XSLT where it's stripped)
                   .replace("\u2003", "")          // also strip actual Unicode em space (U+2003) that XSLT resolves
                   .replace("&#8212;", "—")        // normalize em-dash entity to char
                   .replace("&quot;", "\"")        // normalize quote entity to char
                   .replace("&QUOT;", "\"")        // also handle upper case if present
                   .replace("919.9999999999999px", "920px") // normalize floating point precision issue in XSLT output
                   .toLowerCase();

        // Canonicalize dynamic IDs from generate-id() (XSLT: d22e1, d25e2, etc.)
        // and FreeMarker counter-based IDs (id1, id2, etc.)
        // We normalize by replacing the ID portion within known prefix contexts
        java.util.Map<String, String> idMap = new java.util.LinkedHashMap<>();
        String prefixes = "chart-|overview-|dynamicoverview-|averages-|count-|arrivalrate-|concurrentusers-|distribution-|network-|tableentry-|url-listing-|#chart-|cpu-|memory-|threads-";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
            "(?:" + prefixes + ")(d\\d+e\\d+|gid\\d+|id\\d+|summary|agent_[\\w]+|\\d+)")
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
        java.util.List<java.util.Map.Entry<String, String>> sortedEntries = new java.util.ArrayList<>(idMap.entrySet());
        sortedEntries.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));
        for (java.util.Map.Entry<String, String> entry : sortedEntries)
        {
            String old = entry.getKey();
            String replacement = entry.getValue();
            for (String prefix : prefixes.split("\\|"))
            {
                result = result.replace(prefix + old, prefix + replacement);
            }
        }
        
        // Handle singleton description IDs independently (as XSLT might reuse IDs while FTL uses unique timestamps)
        // We replace them with a fixed suffix
        // IMPORTANT: Longer prefixes must come before shorter ones to avoid partial matching (e.g. more-request matching more-requesterrorcharts)
        String descPrefixes = "more-requesterrorcharts|more-transactionoverview|more-transactiondetails|more-transaction|more-action|more-request|more-network|more-httprequest|more-httpresponse|more-contenttype|more-errors|more-events|more-agents|more-slowest-requests|more-pageload|more-web-vitals|more-scorecard|more-ratings|more-rules|more-groups|more-rulechecks";
        for (String prefix : descPrefixes.split("\\|"))
        {
             // Match prefix followed by common ID patterns
             // We use replaceAll with regex to capture the id part
             // Update regex to catch timestamps or hex-like strings better if needed
             // The XSLT IDs are like "d440e1", FTL are "2026..." (digits)
             // The original regex was (d\\d+e\\d+|gid\\d+|id\\d+|summary|\\d+)
             // We should ensure it captures the FTL timestamp which is just digits.
             // But "errors" prefix is part of the ID in FTL: "errors2026..."
             // The prefix in replaceAll is "more-errors".
             // So we match "more-errors" then "2026...".
             // "more-errors" is in descPrefixes.
             // The regex is (prefix)(id_part).
             // XSLT: more-errorsd440e1 -> prefix "more-errors", id "d440e1"
             // FTL: more-errors2026... -> prefix "more-errors", id "2026..."
             // So I need to ensure "d440e1" (d\d+e\d+) and "\d+" are covered. They are.
             // But wait, "errors" in FTL is part of the ID string?
             // description_error_summary: local gid = "errors" + timestamp.
             // show_n_hide uses "more-${gid}". 
             // So result is "more-errors2026...".
             // Regex (more-errors)(...)
             // "errors" is repeated? No.
             // descriptions.ftl: <#local gid = "errors" + ...>
             // show_n_hide: id="more-${gid}" -> "more-errors2026..."
             // My prefix list has "more-errors".
             // So split "more-errors" from "2026...".
             // But wait, if XSLT produces "more-errorsd440e1", then "more-errors" is common.
             // If XSLT produces "more-d440e1" (without errors?), I need to check descriptions.xsl.
             // descriptions.xsl: variable gid = concat('errors', generate-id(.)) -> "errorsd440e1"
             // show-n-hide: id="more-{$gid}" -> "more-errorsd440e1".
             // So prefix "more-errors" is correct for both.
             // FTL gid = "errors" + timestamp.
             // So "more-errors" + timestamp.
             // So matching "more-errors" as prefix is correct.
             
             // One issue: "requestErrorCharts" ID in ftl is "requestErrorCharts" + timestamp.
             // In XSLT? descriptions.xsl doesn't have requestErrorCharts description macro?
             // XSLT errors.xsl calls description inline?
             // errors.xsl:
             // <div class="description"> ... <xsl:call-template name="show-n-hide"> ... with gid="requestErrorCharts". not generated?
             // No, in errors.xsl:
             // <xsl:variable name="gid" select="concat('requestErrorCharts', generate-id(.))"/>
             // So ID is "requestErrorChartsd...".
             // Prefix "more-requestErrorCharts" is correct. Wait, camelCase.
             // I added "more-requesterrorcharts" (lowercase) to the list.
             // But normalize() converts strict HTML to LOWERCASE!
             // .toLowerCase() at line 267.
             // So "more-requesterrorcharts" is correct.
             
             result = result.replaceAll("(" + prefix + ")(-)?(d\\d+e\\d+|gid\\d+|id\\d+|summary|\\d+|[a-f0-9]+)", "$1-fixed");
        }
        return result;
    }
}
