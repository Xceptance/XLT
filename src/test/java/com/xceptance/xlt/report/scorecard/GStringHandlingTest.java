package com.xceptance.xlt.report.scorecard;

import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import net.sf.saxon.s9api.Processor;

/**
 * Test for GString handling in Groovy DSL.
 */
public class GStringHandlingTest
{
    @Test
    public void testGStringInterpolationInSelectors() throws Exception
    {
        // Test that GString interpolation works in selectors
        var groovy = """
            def pageName = "Homepage"
            def regex = "^Homepage"

            builder.selectors {
                selector {
                    id "${pageName.toLowerCase()}P95"
                    expression "max(//requests/request[matches(name, '${regex}')]/percentiles/p95)"
                }
            }

            builder.rules {
                rule {
                    id "rule1"
                    checks {
                        check {
                            selectorId "${pageName.toLowerCase()}P95"
                            condition "> 100"
                        }
                    }
                }
            }

            builder.groups {
                group {
                    id "G1"
                    rules(['rule1'])
                }
            }

            return builder
            """;

        var tempFile = java.nio.file.Files.createTempFile("scorecard-gstring", ".groovy").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);

            var config = new TestEvaluator(tempFile).parseConfiguration();

            // Verify the selector was created with the interpolated string
            Assert.assertTrue("Selector should exist", config.containsSelector("homepageP95"));
            var selector = config.getSelector("homepageP95");
            Assert.assertTrue("Expression should contain interpolated value", selector.getExpression().contains("^Homepage"));
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    @Test
    public void testGStringInRuleNames() throws Exception
    {
        // Test GString in rule names and messages
        var groovy = """
            def gradeName = "A"
            def limit = 500

            builder.rules {
                rule {
                    id "rule${gradeName}"
                    name "Grade ${gradeName}"
                    points 10
                    checks {
                        check {
                            selector "//foo"
                            condition "<= ${limit}"
                        }
                    }
                    messages {
                        success "${gradeName}"
                    }
                }
            }

            builder.groups {
                group {
                    id "G1"
                    rules(['ruleA'])
                }
            }

            return builder
            """;

        var tempFile = java.nio.file.Files.createTempFile("scorecard-gstring-rule", ".groovy").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);

            var config = new TestEvaluator(tempFile).parseConfiguration();

            // Verify the rule was created with interpolated strings
            Assert.assertTrue("Rule should exist", config.containsRule("ruleA"));
            var rule = config.getRule("ruleA");
            Assert.assertEquals("Rule name should be interpolated", "Grade A", rule.getName());
            Assert.assertEquals("Success message should be interpolated", "A", rule.getSuccessMessage());
        }
        finally
        {
            FileUtils.deleteQuietly(tempFile);
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
                var doc = proc.newDocumentBuilder()
                              .build(new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<dummy/>")));
                var compiler = proc.newXPathCompiler();
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
