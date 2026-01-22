package com.xceptance.xlt.report.scorecard;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XPathCompiler;

public class EvaluatorGroovyTest
{
    @Test
    public void testParseConfigurationGroovyValues() throws Exception
    {
        var groovy = """
            import com.xceptance.xlt.report.scorecard.builder.ScorecardBuilder

            def builder = new ScorecardBuilder()

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

            return builder.build()
            """;

        var tempFile = java.nio.file.Files.createTempFile("scorecard-config", ".groovy").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);

            var config = new TestEvaluator(tempFile).parseConfiguration();

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
        var groovy = """
            builder.selectors {
                selector {
                    id 'sel1'
                    expression '//foo'
                }
            }
            // implicitly returns builder? No, we likely need to return builder.build() or just configuration
            // current impl expects return value to be Configuration or ScorecardBuilder
            return builder;
            """;

        var tempFile = java.nio.file.Files.createTempFile("scorecard-config-binder", ".groovy").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);

            var config = new TestEvaluator(tempFile).parseConfiguration();
            Assert.assertTrue(config.containsSelector("sel1"));
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    @Test(expected = ValidationException.class)
    public void testSecurityBlock() throws Exception
    {
        var groovy = """
            import java.io.File
            new File("/tmp/foo")
            """;

        var tempFile = java.nio.file.Files.createTempFile("scorecard-security", ".groovy").toFile();
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
        var groovy = """
            import java.text.SimpleDateFormat
            import java.util.Date

            def sdf = new SimpleDateFormat("yyyy")
            return builder
            """;

        var tempFile = java.nio.file.Files.createTempFile("scorecard-whitelist", ".groovy").toFile();
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
        var groovy = """
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
            return builder
            """;

        var tempFile = java.nio.file.Files.createTempFile("scorecard-formatter", ".groovy").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);
            var config = new TestEvaluator(tempFile).parseConfiguration();

            var rule = config.getRule("rule1");
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
        var groovy = """
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
            return builder
            """;

        var tempFile = java.nio.file.Files.createTempFile("scorecard-manual", ".groovy").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);
            var config = new TestEvaluator(tempFile).parseConfiguration();

            var rule = config.getRule("rule1");
            var check = rule.getChecks()[0];
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
        var groovy = """
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
            return builder
            """;

        var tempFile = java.nio.file.Files.createTempFile("scorecard-logging", ".groovy").toFile();
        var xmlFile = java.nio.file.Files.createTempFile("dummy", ".xml").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(xmlFile, "<foo/>", StandardCharsets.UTF_8);

            var proc = new Processor(false);
            var evaluator = new GroovyEvaluator(tempFile, proc);

            var scorecard = evaluator.evaluate(xmlFile);
            List<String> logs = scorecard.result.getLogs();

            Assert.assertEquals(3, logs.size());
            Assert.assertEquals("[INFO] Info message", logs.get(0));
            Assert.assertEquals("[WARN] Warn message", logs.get(1));
            Assert.assertEquals("[ERROR] Error message", logs.get(2));
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

        public TestEvaluator(java.io.File file)
        {
            super(file, new Processor(false));
        }

        public Configuration parseConfiguration() throws java.io.IOException, ValidationException
        {
            try
            {
                XdmNode doc = proc.newDocumentBuilder()
                                  .build(new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<dummy/>")));
                XPathCompiler compiler = proc.newXPathCompiler();
                return super.parseGroovyConfiguration(doc, compiler);
            }
            catch (Exception e)
            {
                if (e instanceof ValidationException)
                    throw (ValidationException) e;
                if (e instanceof java.io.IOException)
                    throw (java.io.IOException) e;
                throw new RuntimeException(e);
            }
        }
    }
}
