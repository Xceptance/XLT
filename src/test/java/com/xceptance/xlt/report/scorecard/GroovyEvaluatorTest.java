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
package com.xceptance.xlt.report.scorecard;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmNode;

/**
 * Test the GroovyEvaluator.
 *
 * @author AI-generated: Antigravity
 * @author Xceptance GmbH 2026
 */
public class GroovyEvaluatorTest
{
    @Test
    public void testParseConfigurationGroovyValues() throws Exception
    {
        final var groovy = """
            builder.selectors {
                selector {
                    id 'sel1'
                    expression '//foo'
                }
            }

            builder.rules {
                rule {
                    id 'rule1'
                    name 'Rule 1'
                    enabled true
                    points 10
                    checks {
                        check {
                            selectorId 'sel1'
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
            """;

        final var tempFile = Files.createTempFile("scorecard-config", ".groovy").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);

            final var config = new TestEvaluator(tempFile).parseConfiguration();

            Assert.assertTrue(config.containsSelector("sel1"));
            Assert.assertTrue(config.containsRule("rule1"));
            Assert.assertEquals(10, config.getRule("rule1").getPoints());
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    @Test
    public void testParseConfigurationGroovyBinder() throws Exception
    {
        // Test usage of 'builder' variable exposed in binding
        final var groovy = """
            builder.rules {
                rule {
                    id 'rule1'
                }
            }

            builder.groups {
                group {
                    id 'G1'
                    name 'Group 1'
                    rules(['rule1'])
                }
            }
            """;

        final var tempFile = Files.createTempFile("scorecard-config-binder", ".groovy").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);

            final var config = new TestEvaluator(tempFile).parseConfiguration();
            Assert.assertTrue(config.containsRule("rule1"));
            Assert.assertTrue(config.containsGroup("G1"));
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    @Test(expected = ValidationException.class)
    public void testSecurityBlock() throws Exception
    {
        final var groovy = """
            import java.io.File
            new File("/tmp/foo")
            """;

        final var tempFile = Files.createTempFile("scorecard-security", ".groovy").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);
            new TestEvaluator(tempFile).parseConfiguration();
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    @Test
    public void testWhiteListedImports() throws Exception
    {
        // java.util and java.text should be allowed
        final var groovy = """
            import java.text.SimpleDateFormat
            import java.util.Date

            def sdf = new SimpleDateFormat("yyyy")

            builder.rules {
                rule {
                    id 'rule1'
                }
            }

            builder.groups {
                group {
                    id 'G1'
                    name 'Group 1'
                    rules(['rule1'])
                }
            }
            """;

        final var tempFile = Files.createTempFile("scorecard-whitelist", ".groovy").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);
            new TestEvaluator(tempFile).parseConfiguration();
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    @Test
    public void testParseConfigurationGroovyFormatter() throws Exception
    {
        final var groovy = """
            builder.rules {
                rule {
                    id 'rule1'
                    checks {
                        check {
                            selector '//foo'
                            condition 'exists'
                            formatter '%.2f ms'
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
            """;

        final var tempFile = Files.createTempFile("scorecard-formatter", ".groovy").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);
            final var config = new TestEvaluator(tempFile).parseConfiguration();

            final var rule = config.getRule("rule1");
            Assert.assertEquals("%.2f ms", rule.getChecks()[0].getFormatter());
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    @Test
    public void testParseConfigurationGroovyManualResult() throws Exception
    {
        final var groovy = """
            builder.rules {
                rule {
                    id 'rule1'
                    checks {
                        check {
                            status 'FAILED'
                            value 'Manual Value'
                            message 'Manual Error'
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
            """;

        final var tempFile = Files.createTempFile("scorecard-manual", ".groovy").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);
            final var config = new TestEvaluator(tempFile).parseConfiguration();

            final var rule = config.getRule("rule1");
            final var check = rule.getChecks()[0];
            Assert.assertEquals(Status.FAILED, check.getManualStatus());
            Assert.assertEquals("Manual Value", check.getManualValue());
            Assert.assertEquals("Manual Error", check.getManualErrorMessage());
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    @Test
    public void testLogging() throws Exception
    {
        final var groovy = """
            log.info("Info message")
            log.warn("Warn message")
            log.error("Error message")

            builder.rules {
                rule {
                    id 'rule1'
                    checks {
                        check {
                            selector '//foo'
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
            """;

        final var tempFile = Files.createTempFile("scorecard-logging", ".groovy").toFile();
        final var xmlFile = Files.createTempFile("dummy", ".xml").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(xmlFile, "<foo/>", StandardCharsets.UTF_8);

            final var proc = new Processor(false);
            final var evaluator = new GroovyEvaluator(tempFile, proc);

            final var scorecard = evaluator.evaluate(xmlFile);
            final List<Scorecard.LogEntry> logs = scorecard.result.getLogs();

            Assert.assertEquals(3, logs.size());
            Assert.assertEquals("INFO", logs.get(0).getLevel());
            Assert.assertEquals("Info message", logs.get(0).getMessage());
            Assert.assertEquals("WARN", logs.get(1).getLevel());
            Assert.assertEquals("Warn message", logs.get(1).getMessage());
            Assert.assertEquals("ERROR", logs.get(2).getLevel());
            Assert.assertEquals("Error message", logs.get(2).getMessage());
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
            FileUtils.deleteQuietly(xmlFile);
        }
    }

    @Test
    public void testExceptionLogging() throws Exception
    {
        final var groovy = """
            log.info("Before error")
            throw new RuntimeException("Hard failure")
            """;

        final var tempFile = Files.createTempFile("scorecard-exception", ".groovy").toFile();
        final var xmlFile = Files.createTempFile("dummy", ".xml").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(xmlFile, "<foo/>", StandardCharsets.UTF_8);

            final var proc = new Processor(false);
            final var evaluator = new GroovyEvaluator(tempFile, proc);

            final var scorecard = evaluator.evaluate(xmlFile);
            final List<Scorecard.LogEntry> logs = scorecard.result.getLogs();

            // Verify script-level logs (only "Before error" should be recorded)
            Assert.assertEquals(1, logs.size());
            Assert.assertEquals("INFO", logs.get(0).getLevel());
            Assert.assertEquals("Before error", logs.get(0).getMessage());

            // Verify framework-level crash details are correctly populated in errors
            final List<Scorecard.Error> errors = scorecard.result.getErrors();
            Assert.assertEquals(1, errors.size());
            Assert.assertTrue(errors.get(0).getMessage().contains("Failed to evaluate Groovy configuration"));
            Assert.assertTrue(errors.get(0).getLog().contains("java.lang.RuntimeException: Hard failure"));
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
            FileUtils.deleteQuietly(xmlFile);
        }
    }

    @Test
    public void testManualRatingActive() throws Exception
    {
        final var groovy = """
            builder.selectors {
                selector { id 'sel1'; expression '//foo' }
            }
            builder.rules {
                rule {
                    id 'rule1'
                    points 100
                    checks {
                        check { selectorId 'sel1'; condition 'exists(.)' }
                    }
                }
            }
            builder.groups {
                group { id 'G1'; rules(['rule1']) }
            }
            builder.ratings {
                rating { id 'A'; value 100.0 }
                rating { id 'B'; value 80.0 }
                rating { id 'F'; value 0.0; active true; failsTest true }
            }
            """;

        final var tempFile = Files.createTempFile("scorecard-active-rating", ".groovy").toFile();
        final var xmlFile = Files.createTempFile("dummy", ".xml").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(xmlFile, "<foo/>", StandardCharsets.UTF_8);

            final var proc = new Processor(false);
            final var evaluator = new GroovyEvaluator(tempFile, proc);

            final var scorecard = evaluator.evaluate(xmlFile);

            // Despite perfect score (rule passes), rating should be 'F' because it's marked active
            Assert.assertEquals("F", scorecard.result.getRating());
            Assert.assertTrue(scorecard.result.isTestFailed());
            // Points percentage should be null for manual rating
            Assert.assertNull(scorecard.result.getPointsPercentage());
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
            FileUtils.deleteQuietly(xmlFile);
        }
    }

    // Helper subclass to access protected method
    static class TestEvaluator extends GroovyEvaluator
    {
        private final Processor proc = new Processor(false);

        public TestEvaluator(final File file)
        {
            super(file, new Processor(false));
        }

        public Configuration parseConfiguration() throws IOException, ValidationException
        {
            try
            {
                final XdmNode doc = proc.newDocumentBuilder().build(new StreamSource(new StringReader("<dummy/>")));
                final XPathCompiler compiler = proc.newXPathCompiler();
                return super.parseGroovyConfiguration(doc, compiler);
            }
            catch (final Exception e)
            {
                if (e instanceof ValidationException)
                {
                    throw (ValidationException) e;
                }
                if (e instanceof IOException)
                {
                    throw (IOException) e;
                }
                throw new RuntimeException(e);
            }
        }
    }
}
