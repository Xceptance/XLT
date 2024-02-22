/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.scripting.docgen;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * ANT task for {@link ScriptDocGenerator}.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class ScriptDocAntTask extends Task
{
    private String testSuiteDir;

    private String outputDir;

    /**
     * @return the testSuiteDir
     */
    public String getTestSuiteDir()
    {
        return testSuiteDir;
    }

    /**
     * @param testSuiteDir
     *            the testSuiteDir to set
     */
    public void setTestSuiteDir(String testSuiteDir)
    {
        this.testSuiteDir = testSuiteDir;
    }

    /**
     * @return the outputDir
     */
    public String getOutputDir()
    {
        return outputDir;
    }

    /**
     * @param outputDir
     *            the outputDir to set
     */
    public void setOutputDir(String outputDir)
    {
        this.outputDir = outputDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws BuildException
    {
        if (testSuiteDir == null || "".equals(testSuiteDir.trim()))
        {
            throw new BuildException("Must specify a valid test-suite directory");
        }

        final File input = new File(testSuiteDir);
        final File output;
        if (StringUtils.isEmpty(outputDir))
        {
            output = new File(input, "scriptdoc");

        }
        else
        {
            output = new File(outputDir);
        }

        try
        {
            // Just delegate to ScriptDocGenerator which also checks whether given parameters are valid.
            new ScriptDocGenerator(input, output, null).run();
        }
        catch (final Throwable t)
        {
            throw new BuildException("Failed to generate script documentation using test-suite directory '" + testSuiteDir +
                                     "' and output directory '" + outputDir + "'.", t);
        }

    }
}
