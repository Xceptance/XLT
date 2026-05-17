package com.xceptance.xlt.report.scorecard;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class EvaluatorTest
{
    @Test
    public void testParseConfigurationYaml() throws Exception
    {
        var yaml = """
            version: 2
            rules:
              - id: 'rule1'
                name: 'Rule 1'
                enabled: true
                checks: []
                points: 0
            groups:
              - id: 'G1'
                name: 'Group 1'
                enabled: true
                rules: ['rule1']
            """;

        var tempFile = java.nio.file.Files.createTempFile("scorecard-config", ".yaml").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, yaml, StandardCharsets.UTF_8);

            var evaluator = new Evaluator(tempFile);
            var config = evaluator.parseConfiguration();

            Assert.assertEquals(2, config.getVersion());
            Assert.assertTrue(config.containsRule("rule1"));
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    @Test
    public void testParseConfigurationYml() throws Exception
    {
        var yaml = """
            version: 2
            rules:
              - id: 'rule1'
                name: 'Rule 1'
                enabled: true
                checks: []
                points: 0
            groups:
              - id: 'G1'
                name: 'Group 1'
                enabled: true
                rules: ['rule1']
            """;

        var tempFile = java.nio.file.Files.createTempFile("scorecard-config", ".yml").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, yaml, StandardCharsets.UTF_8);

            var evaluator = new Evaluator(tempFile);
            var config = evaluator.parseConfiguration();

            Assert.assertEquals(2, config.getVersion());
            Assert.assertTrue(config.containsRule("rule1"));
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    @Test
    public void testParseConfigurationJson() throws Exception
    {
        var json = """
            {
              "version": 2,
              "rules": [
                {
                  "id": "rule1",
                  "name": "Rule 1",
                  "enabled": true,
                  "checks": [],
                  "points": 0
                }
              ],
              "groups": [
                {
                  "id": "G1",
                  "name": "Group 1",
                  "enabled": true,
                  "rules": ["rule1"]
                }
              ]
            }
            """;

        var tempFile = java.nio.file.Files.createTempFile("scorecard-config", ".json").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, json, StandardCharsets.UTF_8);

            var evaluator = new Evaluator(tempFile);
            var config = evaluator.parseConfiguration();

            Assert.assertEquals(2, config.getVersion());
            Assert.assertTrue(config.containsRule("rule1"));
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    @Test
    public void testParseConfigurationAdvancedYaml() throws Exception
    {
        var yaml = """
            # This is a YAML comment
            version: 2

            # Reusable values
            name: &ruleName TestRule

            rules:
              - id: 'rule1'
                name: *ruleName
                enabled: true
                checks:
                  - selector: >-
                      max(
                        //requests/request[matches(name, '^Homepage')]/percentiles/p95
                      )
                    condition: '< 500'
                messages:
                  success: 'Rule 1 runs fine'
                  fail: 'Something went wrong'
                points: 5
            groups:
              - id: 'G1'
                name: 'Group 1'
                enabled: true
                rules: ['rule1']
            """;

        var tempFile = java.nio.file.Files.createTempFile("scorecard-config", ".yaml").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, yaml, StandardCharsets.UTF_8);

            var evaluator = new Evaluator(tempFile);
            var config = evaluator.parseConfiguration();

            Assert.assertEquals(2, config.getVersion());
            Assert.assertTrue(config.containsRule("rule1"));

            var rule1 = config.getRule("rule1");
            Assert.assertEquals("TestRule", rule1.getName());
            Assert.assertEquals("Rule 1 runs fine", rule1.getSuccessMessage());

            // Check formatted XPath
            var selector = rule1.getChecks()[0].getSelector();
            Assert.assertTrue(selector.contains("max("));
            Assert.assertTrue(selector.contains("//requests/request"));
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
        }
    }
}
