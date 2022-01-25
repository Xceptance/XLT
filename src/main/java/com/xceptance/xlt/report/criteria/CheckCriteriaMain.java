/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.criteria;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.jaxen.JaxenException;
import org.jaxen.dom.DOMXPath;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.io.Files;
import com.xceptance.common.util.ProcessExitCodes;
import com.xceptance.xlt.report.criteria.CriteriaDefinition.Criterion;

/**
 * Criteria validation main entry point.
 */
public class CheckCriteriaMain
{
    /**
     * Class logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CheckCriteriaMain.class);

    /**
     * Criteria file command line option.
     */
    private static final String COMMANDLINE_OPTION_CRITERIAFILE = "c";

    /**
     * Output file command line option.
     */
    private static final String COMMANDLINE_OPTION_OUTPUT = "o";

    /**
     * Runs the program.
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args)
    {
        final CheckCriteriaMain main = new CheckCriteriaMain();

        try
        {
            main.init(args);
        }
        catch (final Exception ex)
        {
            LOG.error("Initialization failed", ex);
            System.err.println("ERROR: " + ex.getMessage());
            main.printUsageInfo();
            System.exit(ProcessExitCodes.PARAMETER_ERROR);
        }

        try
        {
            if (main.run())
            {
                LOG.info("Criteria checks passed successfully.");
                System.exit(ProcessExitCodes.SUCCESS);
            }
            else
            {
                LOG.info("Criteria checks failed.");
                System.exit(3);
            }

        }
        catch (final Exception ex)
        {
            LOG.error("Failed to check criteria", ex);
            System.err.println("ERROR: " + ex.getMessage());
            System.exit(ProcessExitCodes.GENERAL_ERROR);
        }

    }

    /**
     * Known options.
     */
    private final Options options;

    /**
     * Files to validate.
     */
    private List<File> inputFiles;

    /**
     * Criteria definition file.
     */
    private File criteriaFile;

    /**
     * Output file.
     */
    private File outFile;

    /**
     * Default constructor.
     */
    private CheckCriteriaMain()
    {
        options = createCliOptions();
    }

    /**
     * Parses the given arguments.
     * 
     * @param args
     *            command line arguments
     */
    public void init(String[] args) throws Exception
    {
        final CommandLine cli = new DefaultParser().parse(options, args);

        final String[] remainingArgs = cli.getArgs();
        if (remainingArgs.length < 1)
        {
            throw new IllegalArgumentException("Please specify at least one input file");
        }

        final List<File> workList = new LinkedList<>();
        for (final String s : remainingArgs)
        {
            final File f = new File(s);
            validateFile(f);

            workList.add(f);
        }

        final String critFilePath = cli.getOptionValue(COMMANDLINE_OPTION_CRITERIAFILE);
        if (StringUtils.isBlank(critFilePath))
        {
            throw new IllegalArgumentException("Please specify path to criteria file");
        }
        criteriaFile = new File(critFilePath);

        validateFile(criteriaFile);

        inputFiles = workList;

        final String outFilePath = cli.getOptionValue(COMMANDLINE_OPTION_OUTPUT);
        if (StringUtils.isNotBlank(outFilePath))
        {
            outFile = new File(outFilePath);
            if (outFile.exists())
            {
                if (outFile.isDirectory())
                {
                    throw new IllegalArgumentException("Invalid value for option '-" + COMMANDLINE_OPTION_OUTPUT +
                                                       "':  given path denotes a directory");
                }
                if (!outFile.canWrite())
                {
                    throw new IllegalArgumentException("Cannot write to file '" + outFile.getAbsolutePath() + "'");
                }
            }
        }
    }

    /**
     * Performs the validation and returns whether or not validation has failed.
     * 
     * @return <code>true</code> if validation of all files has passed, <code>false</code> otherwise
     * @throws IOException
     * @throws FileNotFoundException
     */
    public boolean run() throws FileNotFoundException, IOException
    {
        final JSONObject criteriaJSON;
        try (final BufferedReader reader = Files.newReader(criteriaFile, StandardCharsets.UTF_8))
        {
            criteriaJSON = new JSONObject(new JSONTokener(reader));
        }
        catch (final JSONException je)
        {
            throw new RuntimeException("Could not parse criteria file '" + criteriaFile.getAbsolutePath() + "' as JSON", je);
        }

        final CriteriaDefinition criteriaDef;
        try
        {
            criteriaDef = CriteriaDefinition.fromJSON(criteriaJSON);
        }
        catch (final CriteriaDefinition.ValidationError t)
        {
            throw new RuntimeException("Criteria definition file '" + criteriaFile.getAbsolutePath() + "' is malformed\n => " +
                                       t.getMessage());
        }

        final DocumentBuilder docBuilder;
        try
        {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            docBuilder.setErrorHandler(new DefaultHandler());
        }
        catch (ParserConfigurationException pce)
        {
            throw new RuntimeException(pce);
        }

        final List<CriteriaResult> outcome = new LinkedList<>();
        boolean passed = true;
        for (final File f : inputFiles)
        {
            final Document doc;
            try
            {
                doc = docBuilder.parse(f);
            }
            catch (final SAXException saxe)
            {
                throw new RuntimeException("Failed to parse file '" + f.getAbsolutePath() + "' as XML", saxe);
            }

            final CriteriaResult result = new CriteriaResult(f.getAbsolutePath());
            checkCriteria(doc, criteriaDef, result);

            if (passed && !result.hasPassed())
            {
                passed = false;
            }

            outcome.add(result);
        }

        writeJson(criteriaDef, outcome);

        return passed;
    }

    /**
     * Writes the JSON output to file specified as command line argument or to standard out.
     * 
     * @param criteriaDef
     *            the criteria definitions
     * @param validationResults
     *            the validation results
     * @throws IOException
     */
    private void writeJson(final CriteriaDefinition criteriaDef, final List<CriteriaResult> validationResults) throws IOException
    {
        final JSONObject json = new JSONObject();
        json.put("criteria", new JSONArray(criteriaDef.getCriteria()));
        json.put("checks", new JSONArray(validationResults.stream().map(CriteriaResult::toJSON).toArray()));

        final String output = json.toString(2);
        if (outFile != null)
        {
            try (final Writer writer = Files.newWriter(outFile, StandardCharsets.UTF_8))
            {
                writer.write(output);
            }
        }
        else
        {
            System.out.println(output);
        }
    }

    /**
     * Checks if the given document meets the given criteria.
     * 
     * @param doc
     *            the document to check
     * @param criteriaDef
     *            the criteria definitions
     * @param result
     *            the document-wide validation result
     */
    private void checkCriteria(final Document doc, final CriteriaDefinition criteriaDef, final CriteriaResult result)
    {
        for (final Criterion c : criteriaDef.getCriteria())
        {
            if (!c.isEnabled())
            {
                result.add(CriterionResult.skipped(c.getId()));
            }
            else
            {
                try
                {
                    if (evaluateCondition(doc, c.getCondition()))
                    {
                        result.add(CriterionResult.passed(c.getId()));
                    }
                    else
                    {
                        result.add(CriterionResult.failed(c.getId(), StringUtils.defaultIfBlank(c.getMessage(), "Condition failed")));
                    }
                }
                catch (final Exception e)
                {
                    result.add(CriterionResult.error(c.getId(), e.getMessage()));
                }
            }
        }
    }

    /**
     * Evaluates the given XPath predicate on the given document as boolean expression and returns the result.
     * 
     * @param doc
     *            the document to evaluate the given expression on
     * @param xPathPredicate
     *            the predicate to evaluate
     * @return evaluation result
     * @throws Exception
     *             thrown in case the given expression is invalid or does not evaluate to a boolean
     */
    private boolean evaluateCondition(final Document doc, final String xPathPredicate) throws Exception
    {
        if (StringUtils.isBlank(xPathPredicate))
        {
            return false;
        }

        try
        {
            final Boolean b = (Boolean) new DOMXPath(xPathPredicate).evaluate(doc);
            return b.booleanValue();
        }
        catch (ClassCastException cce)
        {
            throw new Exception("Failed to evaluate expression '" + xPathPredicate + "' as boolean");
        }
        catch (JaxenException xee)
        {
            throw new Exception("Failed to evaluate expression '" + xPathPredicate + "': " + xee.getMessage());
        }
    }

    /**
     * Creates and returns the known command line options.
     * 
     * @return known command line options
     */
    private Options createCliOptions()
    {
        final Options opts = new Options();

        {
            final Option o = new Option(COMMANDLINE_OPTION_OUTPUT, true, "write output to the given file");
            o.setArgName("out.json");
            opts.addOption(o);
        }

        {
            final Option o = new Option(COMMANDLINE_OPTION_CRITERIAFILE, true, "criteria definition file");
            o.setArgName("criteria.json");
            o.setRequired(true);
            opts.addOption(o);
        }

        return opts;
    }

    /**
     * Prints the usage help text to standard out.
     */
    private void printUsageInfo()
    {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.setSyntaxPrefix("Usage: ");
        formatter.setWidth(79);

        System.out.println();
        formatter.printHelp("check_criteria [<options>] xmlfile1 [xmlfile2 ...]", options);
        System.out.println();
    }

    /**
     * Assures that the given file exists, is a regular file and is readable.
     * 
     * @param file
     *            the file to check
     */
    private static void validateFile(final File file)
    {
        final String absPath = file.getAbsolutePath();
        if (!file.exists())
        {
            throw new IllegalArgumentException("No such file '" + absPath + "'");
        }
        if (!file.isFile())
        {
            throw new IllegalArgumentException("Not a regular file: " + absPath);
        }
        if (!file.canRead())
        {
            throw new IllegalArgumentException("Cannot read from file '" + absPath + "'");
        }
    }

}
