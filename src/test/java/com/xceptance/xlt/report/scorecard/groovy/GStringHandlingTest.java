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
package com.xceptance.xlt.report.scorecard.groovy;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.report.scorecard.Configuration;
import com.xceptance.xlt.report.scorecard.GroovyEvaluator;
import com.xceptance.xlt.report.scorecard.ValidationException;

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
        final var groovy = """
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

        final var tempFile = Files.createTempFile("scorecard-gstring", ".groovy").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);

            final var config = new TestEvaluator(tempFile).parseConfiguration();

            // Verify the selector was created with the interpolated string
            Assert.assertTrue("Selector should exist", config.containsSelector("homepageP95"));
            final var selector = config.getSelector("homepageP95");
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
        final var groovy = """
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

        final var tempFile = Files.createTempFile("scorecard-gstring-rule", ".groovy").toFile();
        try
        {
            FileUtils.writeStringToFile(tempFile, groovy, StandardCharsets.UTF_8);

            final var config = new TestEvaluator(tempFile).parseConfiguration();

            // Verify the rule was created with interpolated strings
            Assert.assertTrue("Rule should exist", config.containsRule("ruleA"));
            final var rule = config.getRule("ruleA");
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

        public TestEvaluator(final File file)
        {
            super(file, new Processor(false));
        }

        public Configuration parseConfiguration() throws IOException, ValidationException
        {
            try
            {
                final var doc = proc.newDocumentBuilder()
                              .build(new StreamSource(new StringReader("<dummy/>")));
                final var compiler = proc.newXPathCompiler();
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
