package com.xceptance.xlt.report.scorecard;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import net.sf.saxon.s9api.Processor;

public class EvaluatorTest
{
    @Test
    public void testParseConfiguration() throws Exception
    {
        var yaml = """
            version: 2
            selectors:
              - id: sel1
                expression: //foo
            rules:
              - id: rule1
                checks:
                  - selectorId: sel1
                    condition: exists
            groups:
              - id: G1
                rules: [rule1]
            """;

        var tempFile = java.nio.file.Files.createTempFile("scorecard-config", ".yaml").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, yaml, StandardCharsets.UTF_8);

            var config = new TestStaticEvaluator(tempFile).parseConfiguration();

            Assert.assertTrue(config.containsSelector("sel1"));
            Assert.assertTrue(config.containsRule("rule1"));
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    @Test
    public void testEvaluate() throws Exception
    {
        var yaml = """
            version: 2
            selectors:
              - id: sel1
                expression: //count
            rules:
              - id: rule1
                checks:
                  - selectorId: sel1
                    condition: "> 10"
            groups:
              - id: G1
                rules: [rule1]
            """;

        var xml = "<root><count>15</count></root>";

        var tempFile = java.nio.file.Files.createTempFile("scorecard-config", ".yaml").toFile();
        var xmlFile = java.nio.file.Files.createTempFile("scorecard-data", ".xml").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, yaml, StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(xmlFile, xml, StandardCharsets.UTF_8);

            var evaluator = new TestStaticEvaluator(tempFile);
            var scorecard = evaluator.evaluate(xmlFile);

            Assert.assertEquals(Status.PASSED, scorecard.result.getGroups().get(0).getRules().get(0).getStatus());
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
            FileUtils.deleteQuietly(xmlFile);
        }
    }

    @Test
    public void testEvaluatePoints() throws Exception
    {
        var yaml = """
            version: 2
            selectors:
              - id: sel1
                expression: //count
            rules:
              - id: rule1
                points: 10
                checks:
                  - selectorId: sel1
                    condition: "> 10"
            groups:
              - id: G1
                rules: [rule1]
            """;

        var xml = "<root><count>15</count></root>";

        var tempFile = java.nio.file.Files.createTempFile("scorecard-config", ".yaml").toFile();
        var xmlFile = java.nio.file.Files.createTempFile("scorecard-data", ".xml").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, yaml, StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(xmlFile, xml, StandardCharsets.UTF_8);

            var evaluator = new TestStaticEvaluator(tempFile);
            var scorecard = evaluator.evaluate(xmlFile);

            Assert.assertEquals(10, scorecard.result.getPoints().intValue());
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
            FileUtils.deleteQuietly(xmlFile);
        }
    }

    @Test
    public void testFormatting() throws Exception
    {
        var tempFile = java.nio.file.Files.createTempFile("scorecard-config", ".yaml").toFile();
        try
        {
            var evaluator = new TestStaticEvaluator(tempFile);

            // Test double formatting
            Assert.assertEquals("1234.57", evaluator.formatValue("1234.5678", "%.2f"));

            // Test long formatting
            Assert.assertEquals("1234", evaluator.formatValue("1234", "%d"));

            // Test string fallback
            Assert.assertEquals("Hello WORLD", evaluator.formatValue("world", "Hello %S"));

            // Test parsing failure fallback
            Assert.assertEquals("not-a-number", evaluator.formatValue("not-a-number", "%,.2f"));

            // Test null handling
            Assert.assertNull(evaluator.formatValue(null, "%.2f"));
            Assert.assertEquals("123", evaluator.formatValue("123", null));
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    @Test
    public void testManualResultBypass() throws Exception
    {
        var tempFile = java.nio.file.Files.createTempFile("scorecard-config", ".yaml").toFile();
        var dummyXml = java.nio.file.Files.createTempFile("dummy", ".xml").toFile();
        try
        {
            FileUtils.writeStringToFile(dummyXml, "<dummy/>", StandardCharsets.UTF_8);
            var evaluator = new TestStaticEvaluator(tempFile);

            // Create a manual check
            var manualCheck = new RuleDefinition.Check(0, "//none", null, null, true, true, null, Status.ERROR, "manual-value",
                                                       "manual-error");
            var ruleDef = new RuleDefinition("rule1", "Rule 1", new RuleDefinition.Check[]
                {
                    manualCheck
                });
            var groupDef = new GroupDefinition("G1", "Group 1", java.util.List.of("rule1"));
            groupDef.setEnabled(true);
            groupDef.setMode(GroupDefinition.Mode.allPassed);
            var config = new Configuration(2);
            config.addRule(ruleDef);
            config.addGroup(groupDef);

            var scorecard = evaluator.doEvaluate(config, dummyXml);
            var ruleResult = scorecard.result.getGroups().get(0).getRules().get(0);
            var checkResult = ruleResult.getChecks().get(0);

            Assert.assertEquals(Status.ERROR, checkResult.getStatus());
            Assert.assertEquals("manual-value", checkResult.getValue());
            Assert.assertEquals("manual-error", checkResult.getErrorMessage());
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
            FileUtils.deleteQuietly(dummyXml);
        }
    }

    @Test
    public void testEvaluateWithoutRatings() throws Exception
    {
        // Configuration with rules and groups but NO ratings
        var yaml = """
            version: 2
            selectors:
              - id: sel1
                expression: //count
            rules:
              - id: rule1
                failsTest: true
                points: 10
                checks:
                  - selectorId: sel1
                    condition: "> 100"
            groups:
              - id: G1
                rules: [rule1]
            """;

        var xml = "<root><count>5</count></root>";

        var tempFile = java.nio.file.Files.createTempFile("scorecard-config", ".yaml").toFile();
        var xmlFile = java.nio.file.Files.createTempFile("scorecard-data", ".xml").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, yaml, StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(xmlFile, xml, StandardCharsets.UTF_8);

            var evaluator = new TestStaticEvaluator(tempFile);
            var scorecard = evaluator.evaluate(xmlFile);

            // When no ratings are defined, rating should be null
            Assert.assertNull(scorecard.result.getRating());

            // Points should still be calculated
            Assert.assertEquals(0, scorecard.result.getPoints().intValue());
            Assert.assertEquals(10, scorecard.result.getTotalPoints().intValue());

            // Test can still fail due to rule with failsTest=true
            Assert.assertTrue(scorecard.result.isTestFailed());
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
            FileUtils.deleteQuietly(xmlFile);
        }
    }

    // Helper subclass to access protected method
    static class TestStaticEvaluator extends StaticEvaluator
    {
        public TestStaticEvaluator(java.io.File file)
        {
            super(file, new net.sf.saxon.s9api.Processor(false));
        }

        @Override
        public Configuration parseConfiguration() throws java.io.IOException, ValidationException
        {
            return super.parseConfiguration();
        }

        @Override
        public Scorecard doEvaluate(final Configuration config, final File documentFile) throws net.sf.saxon.s9api.SaxonApiException
        {
            return super.doEvaluate(config, documentFile);
        }
    }
}
