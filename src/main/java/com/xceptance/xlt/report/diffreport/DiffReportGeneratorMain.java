/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.diffreport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.xceptance.common.util.ProcessExitCodes;
import com.xceptance.common.util.ProductInformation;
import com.xceptance.common.xml.DomUtils;
import com.xceptance.common.xml.XSLTUtils;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.report.util.ElementSpecification;
import com.xceptance.xlt.report.util.ReportUtils;

/**
 * 
 */
public class DiffReportGeneratorMain
{
    private static final Log log = LogFactory.getLog(DiffReportGeneratorMain.class);

    public static void main(final String[] args)
    {
        Locale.setDefault(Locale.US);

        final DiffReportGeneratorMain main = new DiffReportGeneratorMain();

        main.run(args);
    }

    public void run(final String[] args)
    {
        final Options options = createCommandLineOptions();

        try
        {
            final DiffReportGeneratorConfiguration config = new DiffReportGeneratorConfiguration();

            final CommandLine commandLine = new DefaultParser().parse(options, args);

            // get the command-line options
            final String outputDirName = commandLine.getOptionValue(XltConstants.COMMANDLINE_OPTION_OUTPUT_DIR);

            // check the remaining arguments
            final String[] remainingArgs = commandLine.getArgs();
            if (remainingArgs.length != 2)
            {
                printUsageInfoAndExit(options);
            }

            // read reports and create the difference report
            final File oldReportDir = new File(remainingArgs[0]);
            final Document oldTestReport = readTestReport(oldReportDir);

            final File newReportDir = new File(remainingArgs[1]);
            final Document newTestReport = readTestReport(newReportDir);

            // create the output directory
            final File outputDir;
            if (outputDirName == null)
            {
                // create an artificial name
                final File reportsDir = config.getReportsRootDirectory();
                outputDir = new File(reportsDir, oldReportDir.getName() + "-vs-" + newReportDir.getName());
            }
            else
            {
                // take it as specified
                outputDir = new File(outputDirName);
            }

            FileUtils.forceMkdir(outputDir);
            final String outputDirPath = outputDir.getCanonicalPath();
            System.out.println("Writing difference report to directory: " + outputDirPath);

            // create the difference report XML file
            final Document diffReport = createDiffReport(oldTestReport, oldReportDir.getName(), newTestReport, newReportDir.getName(),
                                                         config.getDiffElementSpecifications(), config.getCopyElementSpecifications());

            final File xmlFile = new File(outputDir, XltConstants.DIFF_REPORT_XML_FILENAME);
            writeDiffReport(diffReport, xmlFile);

            // create the difference report HTML file
            System.out.println("Rendering the HTML difference report ...");

            // the static set file name, does not yet read a dynamic property
            // set
            final File htmlFile = new File(outputDir, XltConstants.DIFF_REPORT_HTML_FILENAME);
            final File styleSheetFile = new File(config.getConfigDirectory(), XltConstants.DIFF_REPORT_XSL_PATH + File.separator +
                                                                              XltConstants.DIFF_REPORT_XSL_FILENAME);

            final HashMap<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("productName", ProductInformation.getProductInformation().getProductName());
            parameters.put("productVersion", ProductInformation.getProductInformation().getVersion());
            parameters.put("productUrl", ProductInformation.getProductInformation().getProductURL());
            parameters.put("projectName", ReportUtils.obtainProjectName(Arrays.asList(oldTestReport, newTestReport)));

            XSLTUtils.transform(xmlFile, htmlFile, styleSheetFile, parameters);

            // copy the report's static resources
            final File resourcesDir = new File(config.getConfigDirectory(), XltConstants.REPORT_RESOURCES_PATH);
            FileUtils.copyDirectory(resourcesDir, outputDir, FileFilterUtils.makeSVNAware(null), true);

            // output the path to the report either as file path (Win) or as clickable file URL
            final File reportFile = new File(outputDir, "index.html");
            final String reportPath = ReportUtils.toString(reportFile);

            System.out.println("\nReport: " + reportPath);

            System.exit(ProcessExitCodes.SUCCESS);
        }
        catch (final org.apache.commons.cli.ParseException pex)
        {
            printUsageInfoAndExit(options);
        }
        catch (final Exception ex)
        {
            log.fatal("Failed to run diff report generator.", ex);
            System.exit(ProcessExitCodes.GENERAL_ERROR);
        }
    }


    private Element copyElements(final Document sourceDocument, final String tagName, final String reportName, final Document document,
                                 final List<ElementSpecification> copyElementSpecs)
    {
        final Element rootElement = createElement(tagName, null, document);

        final Element nameElement = createElement("name", reportName, document);
        rootElement.appendChild(nameElement);

        for (final ElementSpecification elementSpec : copyElementSpecs)
        {
            final Map<String, Element> elementsById = ReportUtils.filterElements(sourceDocument, elementSpec);

            for (final Element element : elementsById.values())
            {
                final Element copiedElement = (Element) DomUtils.cloneNode(element, document);
                rootElement.appendChild(copiedElement);
            }
        }

        return rootElement;
    }

    private Element createChangeElement(final Element oldElement, final Element newElement, final Document document)
    {
        Element element = null;

        try
        {
            final String tagName = oldElement != null ? oldElement.getTagName() : newElement.getTagName();
            element = document.createElement(tagName);

            final double oldValue = oldElement == null ? Double.NaN : Double.parseDouble(oldElement.getTextContent());
            final double newValue = newElement == null ? Double.NaN : Double.parseDouble(newElement.getTextContent());

            if (Double.isNaN(oldValue))
            {
                ReportUtils.addTextElement("newValue", ReportUtils.formatValue(newValue), element);
            }
            else if (Double.isNaN(newValue))
            {
                ReportUtils.addTextElement("oldValue", ReportUtils.formatValue(oldValue), element);
            }
            else
            {
                ReportUtils.addTextElement("oldValue", ReportUtils.formatValue(oldValue), element);
                ReportUtils.addTextElement("newValue", ReportUtils.formatValue(newValue), element);

                final double absoluteDifference = newValue - oldValue;
                double relativeDifference;

                if (oldValue == 0.0)
                {
                    if (newValue == 0.0)
                    {
                        relativeDifference = 0.0;
                    }
                    else
                    {
                        relativeDifference = Double.POSITIVE_INFINITY;
                    }
                }
                else
                {
                    relativeDifference = (newValue / oldValue - 1.0) * 100.0;
                }

                ReportUtils.addTextElement("absoluteDifference", ReportUtils.formatValue(absoluteDifference), element);
                ReportUtils.addTextElement("relativeDifference", ReportUtils.formatValue(relativeDifference), element);
            }
        }
        catch (final NumberFormatException e)
        {
            if (oldElement == null)
            {
                element = createElement(newElement.getTagName(), newElement.getTextContent(), document);
            }
            else
            {
                element = createElement(oldElement.getTagName(), oldElement.getTextContent(), document);
            }
        }

        return element;
    }

    private Map<String, Element> createChildElementMap(final Element rootElement)
    {
        final Map<String, Element> elementsByName = new LinkedHashMap<String, Element>();

        final NodeList childNodes = rootElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++)
        {
            final Node node = childNodes.item(i);
            if (node instanceof Element)
            {
                final Element element = (Element) node;
                elementsByName.put(element.getTagName(), element);
            }
        }

        return elementsByName;
    }

    /**
     * Creates and returns the command line options.
     * 
     * @return command line options
     */
    private Options createCommandLineOptions()
    {
        final Options options = new Options();

        final Option targetDir = new Option("o", true, "the difference report target directory");
        targetDir.setArgName("dir");
        options.addOption(targetDir);

        return options;
    }

    private Document createDiffReport(final Document oldTestReport, final String oldTestReportName, final Document newTestReport,
                                      final String newTestReportName, final List<ElementSpecification> diffElementSpecs,
                                      final List<ElementSpecification> copyElementSpecs)
        throws ParserConfigurationException
    {
        System.out.println("Creating the XML difference report ...");

        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document diffReport = builder.getDOMImplementation().createDocument(null, "testreport", null);

        final Element root = diffReport.getDocumentElement();

        final Element tr1 = copyElements(oldTestReport, "testReport1", oldTestReportName, diffReport, copyElementSpecs);
        root.appendChild(tr1);

        final Element tr2 = copyElements(newTestReport, "testReport2", newTestReportName, diffReport, copyElementSpecs);
        root.appendChild(tr2);

        for (final ElementSpecification elementSpec : diffElementSpecs)
        {
            final Map<String, Element> oldElementsById = ReportUtils.filterElements(oldTestReport, elementSpec);
            final Map<String, Element> newElementsById = ReportUtils.filterElements(newTestReport, elementSpec);

            for (final Entry<String, Element> entry : oldElementsById.entrySet())
            {
                final Element oldElement = entry.getValue();
                final Element newElement = newElementsById.remove(entry.getKey());

                final Element diffElement = diffElementTree(oldElement, newElement, diffReport);

                final Element parent = insertParentElements(oldElement, newElement, diffReport);
                parent.appendChild(diffElement);
            }

            for (final Element newElement : newElementsById.values())
            {
                final Element diffElement = diffElementTree(null, newElement, diffReport);

                final Element parent = insertParentElements(null, newElement, diffReport);
                parent.appendChild(diffElement);
            }
        }

        return diffReport;
    }

    private Element createElement(final String tagName, final String textContent, final Document document)
    {
        final Element element = document.createElement(tagName);

        if (textContent != null)
        {
            element.setTextContent(textContent);
        }

        return element;
    }

    private Element diffElementTree(final Element oldElement, final Element newElement, final Document document)
    {
        Element diffElement;

        if (oldElement == null)
        {
            diffElement = document.createElement(newElement.getTagName());
            final Map<String, Element> childElements = createChildElementMap(newElement);

            if (!childElements.isEmpty())
            {
                for (final Element childElement : childElements.values())
                {
                    diffElement.appendChild(diffElementTree(null, childElement, document));
                }
            }
            else
            {
                diffElement = createChangeElement(null, newElement, document);
            }
        }
        else if (newElement == null)
        {
            diffElement = document.createElement(oldElement.getTagName());
            final Map<String, Element> childElements = createChildElementMap(oldElement);

            if (!childElements.isEmpty())
            {
                for (final Element childElement : childElements.values())
                {
                    diffElement.appendChild(diffElementTree(childElement, null, document));
                }
            }
            else
            {
                diffElement = createChangeElement(oldElement, null, document);
            }
        }
        else
        {
            diffElement = document.createElement(oldElement.getTagName());

            final Map<String, Element> oldChildElements = createChildElementMap(oldElement);
            final Map<String, Element> newChildElements = createChildElementMap(newElement);

            final boolean isOldElementLeaf = oldChildElements.isEmpty();
            final boolean isNewElementLeaf = newChildElements.isEmpty();

            if (isOldElementLeaf && isNewElementLeaf)
            {
                diffElement = createChangeElement(oldElement, newElement, document);
            }
            else if (!isOldElementLeaf && !isNewElementLeaf)
            {
                for (final Entry<String, Element> entry : oldChildElements.entrySet())
                {
                    final Element oldChildElement = entry.getValue();
                    final Element newChildElement = newChildElements.remove(entry.getKey());

                    diffElement.appendChild(diffElementTree(oldChildElement, newChildElement, document));
                }

                for (final Element childElement : newChildElements.values())
                {
                    diffElement.appendChild(diffElementTree(childElement, null, document));
                }
            }
            else
            {
                // difference in structure -> ignore
                System.err.println("Difference in structure - cannot compare!");
            }
        }

        return diffElement;
    }

    private Element insertParentElements(final Element element, final Document document)
    {
        if (element.getTagName().equals("testreport"))
        {
            return document.getDocumentElement();
        }
        else
        {
            final Element e = insertParentElements((Element) element.getParentNode(), document);
            Element e2 = null;

            final NodeList elements = e.getElementsByTagName(element.getTagName());
            if (elements.getLength() == 0)
            {
                e2 = document.createElement(element.getTagName());
            }
            else
            {
                e2 = (Element) elements.item(0);
            }

            e.appendChild(e2);

            return e2;
        }
    }

    private Element insertParentElements(final Element oldElement, final Element newElement, final Document document)
    {
        final Element sourceElement = (oldElement != null) ? oldElement : newElement;

        return insertParentElements((Element) sourceElement.getParentNode(), document);
    }

    /**
     * Prints the usage information to stdout and quits.
     */
    private void printUsageInfoAndExit(final Options options)
    {
        System.out.println("\nCreates a difference report from two test reports.");

        final HelpFormatter formatter = new HelpFormatter();
        formatter.setSyntaxPrefix("Usage: ");
        formatter.setWidth(79);

        System.out.println();
        formatter.printHelp(XltConstants.DIFF_REPORT_EXECUTABLE_NAME + " [options] <testReportDir_1> <testReportDir_2>", "\nOptions:",
                            options, null);
        System.out.println();

        System.exit(ProcessExitCodes.PARAMETER_ERROR);
    }

    private Document readTestReport(final File dir) throws ParserConfigurationException, SAXException, IOException
    {
        System.out.println("Reading test report from directory: " + dir);

        final File file = new File(dir, XltConstants.LOAD_REPORT_XML_FILENAME);
        final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        return documentBuilder.parse(file);
    }

    private void writeDiffReport(final Document diffReport, final File file) throws IOException
    {
        try (final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), XltConstants.UTF8_ENCODING))
        {
            osw.write(XltConstants.XML_HEADER);
            DomUtils.prettyPrintNode(diffReport, osw);
        }
    }
}
