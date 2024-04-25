package com.xceptance.xlt.report.evaluation;

import java.io.File;
import java.io.StringWriter;

import com.xceptance.common.util.ParameterCheckUtils;

public final class Main
{
    public static void main(final String[] args)
    {
        final String definitionFilePath = args.length > 0 ? args[0] : null;
        final String inputXMLFilePath = args.length > 1 ? args[1] : null;
        if (definitionFilePath == null)
        {
            System.err.println("Must specify path to definition file");
        }
        else if (inputXMLFilePath == null)
        {
            System.err.println("Must specify path to XML file that should be evaluated");
        }
        else
        {
            try
            {
                final File inputXMLFile = new File(inputXMLFilePath);
                ParameterCheckUtils.isReadableFile(inputXMLFile, "inputXMLFile");

                final Evaluator evaluator = new Evaluator(new File(definitionFilePath));
                final Evaluation result = evaluator.evaluate(inputXMLFile);
                if (result != null)
                {
                    final StringWriter sw = new StringWriter();
                    evaluator.writeEvaluation(result, sw);
                    System.out.println(sw.toString());
                }
            }
            catch (final Throwable t)
            {
                System.err.println("Failed to evaluate '" + definitionFilePath + "'");
                t.printStackTrace(System.err);
            }
        }
    }
}
