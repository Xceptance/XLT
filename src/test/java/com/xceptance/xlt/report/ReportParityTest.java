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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Parity test for the report pipeline. Runs the report generator twice in the
 * same JVM invocation and verifies that both runs produce identical XML output
 * (after normalizing environment-specific JVM properties). This ensures that
 * the concurrent actor-based processing is deterministic across runs within
 *
 * @implNote Exclusively created by AI (Antigravity).
 * @author Xceptance
 * @since 10.0
 */
public final class ReportParityTest
{
    private static final String DATA_DIR = "samples/demo-external-data/results/20110621-101041";
    private static final String OUTPUT_DIR_A = "target/tests/ReportParityTest/run-a";
    private static final String OUTPUT_DIR_B = "target/tests/ReportParityTest/run-b";

    @Before
    public final void setup() throws Exception
    {
        for (final String dir : new String[] { OUTPUT_DIR_A, OUTPUT_DIR_B })
        {
            final File out = new File(dir);
            if (out.exists())
            {
                org.apache.commons.io.FileUtils.deleteDirectory(out);
            }
            out.mkdirs();
        }
    }

    @Test
    public final void testOutputParity() throws Exception
    {
        // Run A
        {
            final ReportGeneratorMain gen = new ReportGeneratorMain();
            gen.init(new String[] { "-o", OUTPUT_DIR_A, DATA_DIR });
            gen.run();
        }

        // Run B
        {
            final ReportGeneratorMain gen = new ReportGeneratorMain();
            gen.init(new String[] { "-o", OUTPUT_DIR_B, DATA_DIR });
            gen.run();
        }

        // Compare all XML fragments
        final List<Path> filesA = Files.walk(new File(OUTPUT_DIR_A).toPath())
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".xml"))
            .toList();

        Assert.assertFalse("No XML files generated", filesA.isEmpty());

        for (final Path pathA : filesA)
        {
            final String relativePath = new File(OUTPUT_DIR_A).toPath().relativize(pathA).toString();
            final Path pathB = new File(OUTPUT_DIR_B).toPath().resolve(relativePath);

            Assert.assertTrue("File missing in run B: " + relativePath, pathB.toFile().exists());

            final String contentA = normalizeXml(Files.readString(pathA));
            final String contentB = normalizeXml(Files.readString(pathB));

            Assert.assertEquals("File content differs: " + relativePath, contentA, contentB);
        }

        System.out.println("Parity verification passed for " + filesA.size() + " XML fragments.");
    }

    /**
     * Normalizes XML content by removing environment-specific values that change
     * between Maven test invocations (surefire temp paths, JNA paths, JVM property
     * snapshots, etc.).
     */
    private static final String normalizeXml(final String xml)
    {
        String n = xml;
        // Surefire bootstrapper paths
        n = n.replaceAll("surefirebooter\\d+\\.jar", "surefirebooter.jar");
        n = n.replaceAll("surefire\\d+tmp", "surefiretmp");
        n = n.replaceAll("surefire_\\d+", "surefire_X");
        // JNA temp paths
        n = n.replaceAll("jna\\d+\\.tmp", "jnaX.tmp");
        // Strip the entire <configuration> section which contains JVM property snapshots
        n = n.replaceAll("(?s)<configuration>.*?</configuration>", "<configuration/>");
        return n;
    }
}
