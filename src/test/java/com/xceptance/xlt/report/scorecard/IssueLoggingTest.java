package com.xceptance.xlt.report.scorecard;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import net.sf.saxon.s9api.Processor;

/**
 * Test for issue logging functionality.
 */
public class IssueLoggingTest
{
    @Test
    public void testIssueLogging() throws Exception
    {
        // Create a Groovy config with an intentional XPath error
        var groovy = """
            builder.rules {
                rule {
                    id 'rule1'
                    name 'Test Rule'
                    checks {
                        check {
                            selector '//nonexistent/path'
                            condition 'exists'
                        }
                    }
                }
            }

            builder.groups {
                group {
                    id 'G1'
                    name 'Group 1'
                    rules(['rule1'])
                }
            }

            return builder
            """;

        var tempFile = java.nio.file.Files.createTempFile("scorecard-issue-test", ".groovy").toFile();
        var xmlFile = java.nio.file.Files.createTempFile("test-report", ".xml").toFile();

        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);

            // Create a simple XML document for evaluation
            var xmlContent = """
                <?xml version="1.0"?>
                <testreport>
                    <summary>
                        <transactions>
                            <errorPercentage>0.0</errorPercentage>
                        </transactions>
                    </summary>
                </testreport>
                """;
            FileUtils.writeStringToFile(xmlFile, xmlContent, StandardCharsets.UTF_8);

            // Evaluate the scorecard
            var evaluator = new GroovyEvaluator(tempFile, new Processor(false));
            var scorecard = evaluator.evaluate(xmlFile);

            // Verify that issues were collected
            Assert.assertNotNull("Scorecard result should not be null", scorecard.result);
            var issues = scorecard.result.getIssues();
            Assert.assertTrue("Should have at least one issue logged", issues.size() > 0);

            // Verify issue details
            var firstIssue = issues.get(0);
            Assert.assertEquals("ERROR", firstIssue.getSeverity());
            Assert.assertTrue("Message should mention selector", firstIssue.getMessage().contains("No item found"));
            Assert.assertTrue("Location should mention rule1", firstIssue.getLocation().contains("rule1"));
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
            FileUtils.deleteQuietly(xmlFile);
        }
    }

    @Test
    public void testIssueXmlSerialization() throws Exception
    {
        // Create a Groovy config with multiple intentional errors
        var groovy = """
            builder.rules {
                rule {
                    id 'rule1'
                    name 'Test Rule 1'
                    checks {
                        check {
                            selector '//nonexistent/path1'
                            condition 'exists'
                        }
                    }
                }
                rule {
                    id 'rule2'
                    name 'Test Rule 2'
                    checks {
                        check {
                            selector '//nonexistent/path2'
                            condition 'exists'
                        }
                    }
                }
            }

            builder.groups {
                group {
                    id 'G1'
                    name 'Group 1'
                    rules(['rule1', 'rule2'])
                }
            }

            return builder
            """;

        var tempFile = java.nio.file.Files.createTempFile("scorecard-xml-test", ".groovy").toFile();
        var xmlFile = java.nio.file.Files.createTempFile("test-report", ".xml").toFile();

        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);

            // Create a simple XML document
            var xmlContent = """
                <?xml version="1.0"?>
                <testreport>
                    <summary>
                        <transactions>
                            <errorPercentage>0.0</errorPercentage>
                        </transactions>
                    </summary>
                </testreport>
                """;
            FileUtils.writeStringToFile(xmlFile, xmlContent, StandardCharsets.UTF_8);

            // Evaluate the scorecard
            var evaluator = new GroovyEvaluator(tempFile, new Processor(false));
            var scorecard = evaluator.evaluate(xmlFile);

            // Serialize to XML
            var writer = new StringWriter();
            evaluator.writeScorecard(scorecard, writer);
            var xml = writer.toString();

            // Verify XML contains issues
            Assert.assertTrue("XML should contain <issues> element", xml.contains("<issues>"));
            Assert.assertTrue("XML should contain <issue> elements", xml.contains("<issue "));
            Assert.assertTrue("XML should contain severity attribute", xml.contains("severity=\"ERROR\""));
            Assert.assertTrue("XML should contain location", xml.contains("<location>"));
            Assert.assertTrue("XML should contain message", xml.contains("<message>"));
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
            FileUtils.deleteQuietly(xmlFile);
        }
    }
}
