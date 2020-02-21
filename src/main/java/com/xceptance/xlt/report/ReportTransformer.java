package com.xceptance.xlt.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import com.xceptance.common.xml.XSLTUtils;
import com.xceptance.xlt.report.util.TaskManager;

/**
 * 
 */
public class ReportTransformer
{
    private final List<File> outputFiles;

    private final List<File> styleSheetFiles;

    private final Map<String, Object> parameters;

    public ReportTransformer(final List<File> outputFiles, final List<File> styleSheetFiles, final Map<String, Object> parameters)
    {
        this.outputFiles = outputFiles;
        this.styleSheetFiles = styleSheetFiles;
        this.parameters = parameters;
    }

    /**
     * Renders a set of file using one data source into one output directory.
     * 
     * @param inputXmlFile
     *            a single xml file as data source
     * @param outputDir
     *            a single output directory
     */
    public void run(final File inputXmlFile, final File outputDir)
    {
        System.out.printf("Transforming XML data file '%s' ...\n", inputXmlFile);

        for (int i = 0; i < outputFiles.size(); i++)
        {
            final File outputFile = outputFiles.get(i);
            final File styleSheetFile = styleSheetFiles.get(i);

            TaskManager.getInstance().addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    transformReport(inputXmlFile, outputFile, styleSheetFile);
                }
            });
        }
    }

    protected void transformReport(final File inputXmlFile, final File outputFile, final File xsltStyleSheet)
    {
        try
        {
            // System.out.printf("Transforming XML report using style sheet '%s' ...\n", xsltStyleSheet);

            XSLTUtils.transform(inputXmlFile, outputFile, xsltStyleSheet, parameters);
        }
        catch (final TransformerConfigurationException e)
        {
            System.err.println("Could not setup transformation engine: " + e.getMessage());
        }
        catch (final FileNotFoundException e)
        {
            System.err.println("Could not find file(s): " + e.getMessage());
        }
        catch (final TransformerException e)
        {
            System.err.println("Could not transform XML into target: " + e.getMessage());
        }
    }
}
