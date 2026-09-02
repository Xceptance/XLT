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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.common.io.FileUtils;

public class ReportGeneratorMain_CommandLineOptionsTest
{
    private Path tempDir;

    @Before
    public void setUp() throws IOException
    {
        tempDir = Files.createTempDirectory("rgm-test-");
    }

    @After
    public void tearDown() throws IOException
    {
        FileUtils.deleteDirectoryRelaxed(tempDir.toFile());
    }

    @Test
    public void testRatingShortOption() throws Exception
    {
        final String[] args = new String[] {
            "-rating", "A",
            "-rating-summary", "Test summary short opt",
            tempDir.toString()
        };

        final ReportGeneratorMain rgm = new ReportGeneratorMain();
        rgm.init(args);

        final Properties props = rgm.getCommandLineProperties();
        Assert.assertNotNull(props);
        Assert.assertEquals("A", props.getProperty("com.xceptance.xtc.loadtest.rating"));
        Assert.assertEquals("Test summary short opt", props.getProperty("com.xceptance.xtc.loadtest.rating.summary"));
    }

    @Test
    public void testRatingLongOption() throws Exception
    {
        final String[] args = new String[] {
            "--rating", "B",
            "--rating-summary", "Test summary long opt",
            "--pdf",
            tempDir.toString()
        };

        final ReportGeneratorMain rgm = new ReportGeneratorMain();
        rgm.init(args);

        final Properties props = rgm.getCommandLineProperties();
        Assert.assertNotNull(props);
        Assert.assertEquals("B", props.getProperty("com.xceptance.xtc.loadtest.rating"));
        Assert.assertEquals("Test summary long opt", props.getProperty("com.xceptance.xtc.loadtest.rating.summary"));
        Assert.assertTrue(rgm.isPdfReport());
    }

    @Test
    public void testPropertyDefinitionOption() throws Exception
    {
        final String[] args = new String[] {
            "-Dcom.xceptance.xtc.loadtest.rating=C",
            "-Dcom.xceptance.xtc.loadtest.rating.summary=Injected via -D",
            tempDir.toString()
        };

        final ReportGeneratorMain rgm = new ReportGeneratorMain();
        rgm.init(args);

        final Properties props = rgm.getCommandLineProperties();
        Assert.assertNotNull(props);
        Assert.assertEquals("C", props.getProperty("com.xceptance.xtc.loadtest.rating"));
        Assert.assertEquals("Injected via -D", props.getProperty("com.xceptance.xtc.loadtest.rating.summary"));
    }
}
