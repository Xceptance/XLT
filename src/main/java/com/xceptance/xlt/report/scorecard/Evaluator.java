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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.common.XltConstants;

import net.sf.saxon.s9api.Processor;

/**
 * Main entry point for evaluating scorecards. Dispatches to either {@link StaticEvaluator} or {@link GroovyEvaluator}
 * based on the configuration file extension.
 */
public class Evaluator
{

    private final AbstractEvaluator delegate;

    public Evaluator(final File configFile)
    {
        ParameterCheckUtils.isReadableFile(configFile, "configFile");

        final Processor processor = new Processor(false);
        final String fileName = configFile.getName().toLowerCase();

        if (fileName.endsWith(".groovy"))
        {
            this.delegate = new GroovyEvaluator(configFile, processor);
        }
        else
        {
            this.delegate = new StaticEvaluator(configFile, processor);
        }
    }

    /**
     * Evaluates the given XML file.
     *
     * @param documentFile
     *                         the XML file to evaluate
     * @return resulting scorecard
     */
    public Scorecard evaluate(final File documentFile)
    {
        return delegate.evaluate(documentFile);
    }

    /**
     * Writes the given scorecard as serialized XML to the given output file.
     *
     * @param scorecard
     *                       the scorecard to be written
     * @param outputFile
     *                       the target output file
     * @throws IOException
     *                         thrown upon failure to write to given file
     */
    public void writeScorecardToFile(final Scorecard scorecard, final File outputFile) throws IOException
    {
        try (final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outputFile), XltConstants.UTF8_ENCODING))
        {
            writeScorecard(scorecard, osw);
        }
    }

    /**
     * Writes the given scorecard as serialized XML to the given destination writer.
     *
     * @param scorecard
     *                      the scorecard to be written
     * @param writer
     *                      the destination to write serialized XML to
     * @throws IOException
     *                         thrown if scorecard could not be written
     */
    public void writeScorecard(final Scorecard scorecard, final Writer writer) throws IOException
    {
        delegate.writeScorecard(scorecard, writer);
    }
}
