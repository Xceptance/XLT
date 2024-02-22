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
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.xceptance.common.io.FileUtils;
import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.common.util.ProductInformation;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.scripting.MacroProcessor;
import com.xceptance.xlt.engine.scripting.ScriptException;
import com.xceptance.xlt.engine.scripting.TestDataUtils;
import com.xceptance.xlt.engine.util.ScriptingUtils;
import com.xceptance.xlt.report.util.TaskManager;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * Script Doc Generator.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class ScriptDocGenerator
{
    private static final Logger LOG = LoggerFactory.getLogger(ScriptDocGenerator.class);

    private static final Pattern VAR_EXPR_PATTERN = Pattern.compile("(?<!\\$)\\$\\{[^\\s{}$]+\\}");

    private static final String SUITE_DESCRIPTOR_FILENAME = "suite.xml";

    private final Configuration freeMarkerConfig;

    private final File outputDir;

    private final File scriptDir;

    private final String scriptDirPath;

    private PackageTree pkgTree;

    private TestSuiteInfo suiteInfo;

    private List<Step> worklist;

    private Set<String> calls;

    private final ScriptDocGeneratorConfiguration config;

    public ScriptDocGenerator(final File testSuiteDirectory, final File outputDirectory, final Properties commandLineProperties)
        throws IOException
    {
        ParameterCheckUtils.isNotNull(testSuiteDirectory, "testSuiteDirectory");
        ParameterCheckUtils.isNotNull(outputDirectory, "outputDirectory");

        if (!testSuiteDirectory.exists() || !testSuiteDirectory.canRead() || !testSuiteDirectory.isDirectory())
        {
            throw new IllegalArgumentException("Test suite path '" + testSuiteDirectory.getAbsolutePath() +
                                               "' does not denote an existent and readable directory.");
        }

        final File scriptsDirectory = new File(testSuiteDirectory, "scripts");
        if (!scriptsDirectory.canRead())
        {
            throw new IllegalArgumentException("Failed to access directory 'scripts' in test suite '" +
                                               testSuiteDirectory.getAbsolutePath() + "'. Please make sure it exists and can be read.");
        }
        if (outputDirectory.exists())
        {
            if (!outputDirectory.isDirectory())
            {
                throw new IllegalArgumentException("Cannot write to '" + outputDirectory.getAbsolutePath() +
                                                   "' because it already exists and is not a directory.");
            }
            if (!outputDirectory.canWrite())
            {
                throw new IllegalArgumentException("Directory '" + outputDirectory.getAbsolutePath() +
                                                   "' already exists and is read-only.");
            }
        }

        outputDir = outputDirectory;

        scriptDir = scriptsDirectory;
        scriptDirPath = scriptDir.getAbsolutePath();

        config = new ScriptDocGeneratorConfiguration(commandLineProperties);

        freeMarkerConfig = new Configuration(Configuration.VERSION_2_3_21);
        freeMarkerConfig.setDefaultEncoding(XltConstants.UTF8_ENCODING);
        freeMarkerConfig.setDirectoryForTemplateLoading(config.getTemplateDir());
        freeMarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public void run() throws Exception
    {
        final File suitePath = scriptDir.getParentFile().getCanonicalFile();
        suiteInfo = new TestSuiteInfo(suitePath);

        // Process packages first
        final PackageInfo pkgInfo = new PackageInfo();
        pkgInfo.setTestData(TestDataUtils.getPackageTestData(null, scriptDir.getAbsolutePath(), pkgInfo.name));

        suiteInfo.addPackage(pkgInfo);
        pkgTree = new PackageTree(pkgInfo);

        final Pattern scriptFilePattern = Pattern.compile("^.+(?<!_data(sets)?)\\.xml$");

        final File[] allFiles = FileUtils.listFiles(scriptDir, true, new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                final String pkgName = StringUtils.defaultString(getPkgName(dir.getAbsolutePath()));
                if (pkgName.length() > 0)
                {
                    _addPackageToTree(pkgName);
                }

                return scriptFilePattern.matcher(name).matches();
            }

        });

        worklist = new LinkedList<Step>();
        calls = new HashSet<String>();
        final ArrayList<File> failures = new ArrayList<File>();
        for (final File f : allFiles)
        {
            try
            {
                final BaseInfo info = parseScriptFile(f);
                if (info != null)
                {
                    suiteInfo.addScript(info);
                }
            }
            catch (final Exception e)
            {
                LOG.error("Failed to parse script", e);
                failures.add(f);
            }
        }

        readInfoFromSuiteFile(suitePath);
        postProcess();

        dumpToDisk();

        pkgTree = null;

        if (!failures.isEmpty())
        {
            final StringBuilder sb = new StringBuilder("The following files could not be parsed successfully:\n");
            for (final File f : failures)
            {
                sb.append(" - ").append(f.getAbsolutePath()).append('\n');
            }
            sb.append("\nPlease see the log for details.");

            throw new ScriptException(sb.toString());
        }
    }

    private void _addPackageToTree(String pkgName)
    {
        if (pkgTree.getNodeForName(pkgName) == null)
        {
            final String superPkgName = ScriptingUtils.getParentPackageName(pkgName);
            PackageTreeNode parent = pkgTree.getNodeForName(superPkgName);
            if (parent == null)
            {
                _addPackageToTree(superPkgName);
                parent = pkgTree.getNodeForName(superPkgName);
            }

            final PackageInfo info = new PackageInfo(pkgName);
            info.setTestData(TestDataUtils.getPackageTestData(null, scriptDir.getAbsolutePath(), info.name));

            parent.addChildPackage(info);
            suiteInfo.addPackage(info);
        }
    }

    /**
     * Post-process all module calls whose comment fields were empty and the module's description takes precedence then.
     */
    private void postProcess()
    {
        while (!worklist.isEmpty())
        {
            final Step step = worklist.remove(0);
            final String stepName = step.getName();
            final String packageName = ScriptingUtils.getScriptPackage(stepName);
            final PackageTreeNode pkgNode = pkgTree.getNodeForName(packageName);
            if (pkgNode == null)
            {
                LOG.error("No such script package '" + packageName + "'");
            }
            else if (pkgNode._info == null)
            {
                LOG.error("Script package '" + packageName + "' was found but no information about it.");
            }
            else
            {
                for (final BaseInfo info : pkgNode._info.getModules())
                {
                    if (info.name.equals(stepName))
                    {
                        step.setDescription(info.description);
                        break;
                    }
                }
            }
        }

        for (final String call : calls)
        {
            final PackageTreeNode node = pkgTree.getNodeForName(ScriptingUtils.getScriptPackage(call));
            if (node != null && node._info != null)
            {
                for (final BaseInfo info : node._info.getModules())
                {
                    if (info.name.equals(call))
                    {
                        if (info instanceof ModuleScriptInfo)
                        {
                            ((ModuleScriptInfo) info).setCalled(true);
                        }
                        else if (info instanceof JavaModuleInfo)
                        {
                            ((JavaModuleInfo) info).setCalled(true);
                        }
                        break;
                    }
                }
            }
        }
    }

    private String getPkgName(String absolutePath)
    {
        String s = StringUtils.substringAfter(absolutePath, scriptDirPath);
        s = s.replace('\\', '/').replace('/', '.');

        return s.startsWith(".") ? s.substring(1) : s;
    }

    /**
     * Writes all the output files to disk.
     */
    private void dumpToDisk() throws Exception
    {
        org.apache.commons.io.FileUtils.forceMkdir(outputDir);

        processTemplates();

        // Copy static resources to output directory.
        FileUtils.copyDirectory(config.getResourceDir(), outputDir, true);

        // Finally, create the report XML file.
        try (final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File(outputDir, "doc.xml")),
                                                                   XltConstants.UTF8_ENCODING))
        {
            final XStream xstream = new XStream(new DomDriver());
            xstream.autodetectAnnotations(true);
            xstream.setMode(XStream.NO_REFERENCES);

            xstream.toXML(suiteInfo, osw);
        }
    }

    /**
     * Processes the configured set of templates in parallel.
     * 
     * @throws InterruptedException
     *             thrown if submitting task to manager of waiting for tasks to complete has failed
     */
    private void processTemplates() throws InterruptedException
    {
        final HashMap<String, Object> dataModel = new HashMap<String, Object>();
        dataModel.put("suite", suiteInfo);
        dataModel.put("xlt_version", ProductInformation.getProductInformation().getVersion());

        final List<Throwable> failures = Collections.synchronizedList(new ArrayList<Throwable>());
        final TaskManager taskMgr = TaskManager.getInstance();
        for (final Map.Entry<String, String> entry : config.getTemplates().entrySet())
        {
            taskMgr.addTask(new Runnable()
            {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void run()
                {
                    try
                    {
                        try (final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File(outputDir,
                                                                                                                 entry.getValue())),
                                                                                   XltConstants.UTF8_ENCODING))
                        {
                            final Template template = freeMarkerConfig.getTemplate(entry.getKey());
                            template.process(dataModel, osw);
                        }
                    }
                    catch (final Throwable t)
                    {
                        LOG.error("Failed to process template", t);
                        failures.add(t);
                    }
                }
            });

        }

        taskMgr.waitForAllTasksToComplete();

        if (!failures.isEmpty())
        {
            final StringBuilder sb = new StringBuilder("Template processing has failed:");
            for (final Throwable t : failures)
            {
                sb.append("\n - ").append(t.getMessage());
            }
            throw new ScriptException(sb.toString());
        }
    }

    private void readInfoFromSuiteFile(final File pathToSuite) throws Exception
    {
        final String schemaFileName = "scriptdeveloper-project.xsd";
        final File descriptorFile = new File(pathToSuite, SUITE_DESCRIPTOR_FILENAME);
        if (descriptorFile.exists() && descriptorFile.isFile() && descriptorFile.canRead())
        {
            LOG.info("Parsing suite descriptor file: " + descriptorFile);
            final Document document = parseXMLFile(descriptorFile, TestDataUtils.class.getResource(schemaFileName));

            final NodeList list = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < list.getLength(); i++)
            {
                final Node n = list.item(i);
                if (n instanceof Element)
                {
                    final String tagName = ((Element) n).getTagName();
                    final String content = n.getTextContent();
                    if ("description".equals(tagName))
                    {
                        suiteInfo.description = content;
                    }
                    else if ("name".equals(tagName))
                    {
                        suiteInfo.name = content;
                    }
                    else
                    {
                        LOG.info("Don't know how to handle '" + tagName + "' elements");
                    }
                }
            }

        }
    }

    private BaseInfo parseScriptFile(final File scriptFile) throws Exception
    {
        final String schemaFileName = "xlt-script.xsd";
        LOG.info("Parsing script file: " + scriptFile);

        final String content = org.apache.commons.io.FileUtils.readFileToString(scriptFile, "UTF-8");
        final String rootElementTagName = RegExUtils.getFirstMatch(content, "<\\?xml.+?\\?>\\s*<(\\S+)[^>]*?>", 1);
        if (rootElementTagName == null)
        {
            throw new ScriptException("Failed to parse document element tag name of script file '" + scriptFile + "'");
        }

        final Document document = parseXMLFile(scriptFile, TestDataUtils.class.getResource(schemaFileName));

        // let us see what we have
        final Element rootElement = document.getDocumentElement();
        final String rootElemName = rootElement.getTagName();
        BaseInfo info = null;
        if ("javamodule".equals(rootElemName))
        {
            info = processJavaModule(rootElement, scriptFile);
        }
        else if ("testcase".equals(rootElemName))
        {
            info = processTestCase(rootElement, scriptFile);
        }
        else if ("scriptmodule".equals(rootElemName))
        {
            info = processModule(rootElement, scriptFile);
        }

        return info;
    }

    private ScriptInfo processModule(final Element rootElement, final File scriptFile) throws Exception
    {
        String name = StringUtils.substringBefore(scriptFile.getName(), ".xml");
        final String pkgName = getPkgName(scriptFile.getParentFile().getAbsolutePath());
        if (StringUtils.isNotEmpty(pkgName))
        {
            name = pkgName + "." + name;
        }
        final ModuleScriptInfo info;
        if (rootElement.hasAttribute("id"))
        {
            info = new ModuleScriptInfo(name, rootElement.getAttribute("id"));
        }
        else
        {
            info = new ModuleScriptInfo(name);
        }

        return processCommandScript(rootElement, info, scriptFile);
    }

    private ScriptInfo processTestCase(final Element rootElement, final File scriptFile) throws Exception
    {
        String name = StringUtils.substringBefore(scriptFile.getName(), ".xml");
        final String pkgName = getPkgName(scriptFile.getParentFile().getAbsolutePath());
        if (StringUtils.isNotEmpty(pkgName))
        {
            name = pkgName + "." + name;
        }

        final String baseURL = rootElement.getAttribute("baseURL");
        final boolean isDisabled = "true".equals(rootElement.getAttribute("disabled"));

        final TestScriptInfo info;
        if (rootElement.hasAttribute("id"))
        {
            info = new TestScriptInfo(name, rootElement.getAttribute("id"));
        }
        else
        {
            info = new TestScriptInfo(name);
        }
        info.disabled = isDisabled;
        info.baseUrl = baseURL;

        return processCommandScript(rootElement, info, scriptFile);
    }

    private JavaModuleInfo processJavaModule(final Element rootElement, final File scriptFile) throws Exception
    {
        String name = StringUtils.substringBefore(scriptFile.getName(), ".xml");
        final String pkgName = getPkgName(scriptFile.getParentFile().getAbsolutePath());
        if (StringUtils.isNotEmpty(pkgName))
        {
            name = pkgName + "." + name;
        }

        final String implClassName = rootElement.getAttribute("class");
        final JavaModuleInfo info;
        if (rootElement.hasAttribute("id"))
        {
            info = new JavaModuleInfo(name, rootElement.getAttribute("id"), implClassName);
        }
        else
        {
            info = new JavaModuleInfo(name, implClassName);
        }

        final NodeList children = rootElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            final Node n = children.item(i);
            if (n instanceof Element)
            {
                final Element e = (Element) n;
                final String tagName = e.getTagName();
                if ("tags".equals(tagName))
                {
                    info.tags = n.getTextContent();
                }
                else if ("description".equals(tagName))
                {
                    info.description = n.getTextContent();
                }
                else if ("parameter".equals(tagName))
                {
                    info.addParameter(e.getAttribute("name"), e.getAttribute("desc"));
                }
            }
        }

        return info;
    }

    private ScriptInfo processCommandScript(final Element rootElement, final ScriptInfo info, final File scriptFile)
    {
        final boolean isModule = info instanceof ModuleScriptInfo;
        final PackageTreeNode pkgNode = pkgTree.getNodeForName(ScriptingUtils.getScriptPackage(info.name));

        info.setTestData(TestDataUtils.getTestData(scriptFile));

        final NodeList children = rootElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            final Node n = children.item(i);
            if (n instanceof Element)
            {
                final Element e = (Element) n;
                final String tagName = e.getTagName();
                switch (tagName)
                {
                    case "description":
                        info.description = e.getTextContent();
                        break;
                    case "tags":
                        info.tags = e.getTextContent();
                        break;
                    case "parameter":
                        if (isModule)
                        {
                            ((ModuleScriptInfo) info).addParameter(e.getAttribute("name"), e.getAttribute("desc"));
                        }
                        break;
                    case "module":
                    case "action":
                    case "command":
                        handleScriptElement(e, info, pkgNode, false);
                        break;
                    case "postSteps":
                        if (isModule)
                        {
                            throw new ScriptException("Only test case scripts can have post-steps.");
                        }

                        final NodeList postStepItems = e.getChildNodes();
                        for (int j = 0; j < postStepItems.getLength(); j++)
                        {
                            final Node postStepItem = postStepItems.item(j);
                            if (postStepItem instanceof Element)
                            {
                                final Element postStep = (Element) postStepItem;
                                switch (postStep.getTagName())
                                {
                                    case "module":
                                    case "action":
                                    case "command":
                                        handleScriptElement(postStep, info, pkgNode, true);
                                        break;
                                    default:
                                        break;
                                }
                            }

                        }

                    default:
                        break;
                }
            }
        }

        return info;
    }

    /**
     * @param scriptElement
     * @param info
     * @param insidePostSteps
     */
    private void handleScriptElement(final Element scriptElement, final ScriptInfo info, final PackageTreeNode pkgNode,
                                     final boolean insidePostSteps)
    {
        final String tagName = scriptElement.getTagName();
        final boolean isDisabled = "true".equals(scriptElement.getAttribute("disabled"));
        String haystack = null;
        if ("module".equals(tagName))
        {
            final String stepName = scriptElement.getAttribute("name");
            String comment = null;
            String conditionExpression = null;
            boolean conditionDisabled = false;

            if (isDisabled)
            {
                info.addCall(stepName);
            }

            final NodeList moduleChilds = scriptElement.getChildNodes();
            for (int j = 0; j < moduleChilds.getLength(); j++)
            {
                final Node moduleChild = moduleChilds.item(j);
                if (moduleChild instanceof Element)
                {
                    final Element _el = (Element) moduleChild;
                    final String moduleChildName = _el.getTagName();
                    if ("parameter".equals(moduleChildName))
                    {
                        if (!isDisabled)
                        {
                            haystack = StringUtils.defaultString(haystack) + _el.getAttribute("value");
                        }
                    }
                    else if ("comment".equals(moduleChildName))
                    {
                        comment = _el.getTextContent();
                    }
                    else if ("condition".equals(moduleChildName))
                    {
                        conditionExpression = _el.getTextContent();
                        conditionDisabled = Boolean.valueOf(_el.getAttribute("disabled"));
                        if (!conditionDisabled)
                        {
                            haystack = StringUtils.defaultString(haystack) + conditionExpression;
                        }
                    }
                }
            }

            final Step step = new Step(stepName, comment, isDisabled, true, conditionDisabled, conditionExpression);
            if (insidePostSteps)
            {
                ((TestScriptInfo) info).addPostStep(step);
            }
            else
            {
                info.addStep(step);
            }

            if (StringUtils.isBlank(comment))
            {
                worklist.add(step);
            }

            calls.add(stepName);
        }
        else
        {
            if ("command".equals(tagName))
            {
                if (!isDisabled)
                {
                    final String cmdName = scriptElement.getAttribute("name");
                    String target = scriptElement.hasAttribute("target") ? scriptElement.getAttribute("target") : null;
                    if (target == null)
                    {
                        final NodeList targetElements = scriptElement.getElementsByTagName("target");
                        if (targetElements.getLength() > 0)
                        {
                            target = targetElements.item(0).getTextContent();
                        }
                    }

                    String value = scriptElement.hasAttribute("value") ? scriptElement.getAttribute("value") : null;
                    if (value == null)
                    {
                        final NodeList valueElements = scriptElement.getElementsByTagName("value");
                        if (valueElements.getLength() > 0)
                        {
                            value = valueElements.item(0).getTextContent();
                        }
                    }

                    if (cmdName.startsWith("store") && StringUtils.isNotBlank(value))
                    {
                        info.addStore(value);
                    }

                    haystack = StringUtils.defaultString(target) + StringUtils.defaultString(value);
                }
            }

            else if ("action".equals(tagName))
            {
                final String stepName = scriptElement.getAttribute("name");
                String comment = null;

                final NodeList stepChildren = scriptElement.getChildNodes();
                for (int j = 0; j < stepChildren.getLength(); j++)
                {
                    final Node stepChild = stepChildren.item(j);
                    if (stepChild instanceof Element && "comment".equals(((Element) stepChild).getTagName()))
                    {
                        comment = stepChild.getTextContent();
                        break;
                    }
                }

                final Step step = new Step(stepName, comment, isDisabled);
                if (insidePostSteps)
                {
                    ((TestScriptInfo) info).addPostStep(step);
                }
                else
                {
                    info.addStep(step);
                }
            }
        }

        if (StringUtils.isNotBlank(haystack))
        {
            for (final String match : RegExUtils.getAllMatches(haystack, VAR_EXPR_PATTERN))
            {
                final String varName = match.substring(2, match.length() - 1);
                if (!info.getTestData().containsKey(varName) && !info.hasStore(varName) && !MacroProcessor.getInstance().isMacro(varName))
                {
                    final Map<String, String> pkgData = pkgNode.getEffectiveTestData();
                    String value = pkgData.get(varName);
                    if (value == null)
                    {
                        value = suiteInfo.getGlobalTestData().get(varName);
                    }
                    info.addExternalParam(varName, value);
                }
            }
        }

    }

    private Document parseXMLFile(final File xmlFile, final URL schemaLocation) throws Exception
    {
        // parse the file
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = schemaFactory.newSchema(schemaLocation);

        final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxFactory.setSchema(schema);
        final SAXParser saxParser = saxFactory.newSAXParser();

        final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        final Document document = docBuilder.newDocument();

        final DefaultHandler handler = new DefaultHandler(document);
        saxParser.parse(xmlFile, handler);

        if (handler.errors > 0)
        {
            throw new SAXException(String.format("Parsing the XML file '%s' produced %d error(s) and %d warning(s)", xmlFile.toString(),
                                                 handler.errors, handler.warnings));
        }

        if (handler.warnings > 0)
        {
            if (LOG.isWarnEnabled())
            {
                LOG.warn(String.format("Parsing the XML file '%s' produced %d warning(s)", xmlFile.toString(), handler.warnings));
            }
        }
        return document;
    }

    private static class DefaultHandler extends org.xml.sax.helpers.DefaultHandler
    {
        private final Document doc;

        final Stack<Element> elementStack = new Stack<Element>();

        final StringBuilder textBuffer = new StringBuilder();

        private DefaultHandler(final Document document)
        {
            super();
            doc = document;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException
        {
            addTextIfNeeded();
            final Element element = doc.createElement(qName);
            for (int i = 0; i < attributes.getLength(); i++)
            {
                element.setAttribute(attributes.getQName(i), attributes.getValue(i));
            }
            elementStack.push(element);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void endElement(final String uri, final String localName, final String qName)
        {
            addTextIfNeeded();
            final Element closedElement = elementStack.pop();
            if (elementStack.isEmpty())
            { // root element
                doc.appendChild(closedElement);
            }
            else
            {
                final Element parentElement = elementStack.peek();
                parentElement.appendChild(closedElement);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void characters(final char ch[], final int start, final int length) throws SAXException
        {
            textBuffer.append(ch, start, length);
        }

        private void addTextIfNeeded()
        {
            if (textBuffer.length() > 0)
            {
                final Element element = elementStack.peek();
                final Node textNode = doc.createTextNode(textBuffer.toString());
                element.appendChild(textNode);
                textBuffer.delete(0, textBuffer.length());
            }
        }

        /**
         * The number of errors encountered.
         */
        private int errors;

        /**
         * The number of warnings encountered.
         */
        private int warnings;

        /**
         * {@inheritDoc}
         */
        @Override
        public void error(final SAXParseException exception) throws SAXException
        {
            LOG.error(report(exception));
            errors++;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void fatalError(final SAXParseException exception) throws SAXException
        {
            LOG.error(report(exception));
            errors++;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void warning(final SAXParseException exception) throws SAXException
        {
            LOG.warn(report(exception));
            warnings++;
        }

        /**
         * Logs the given exception.
         * 
         * @param exception
         *            the exception
         */
        private String report(final SAXParseException exception)
        {
            return String.format("### %d:%d - %s\n", exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
        }

    }

    private static class PackageTree
    {
        private final PackageTreeNode _root;

        private PackageTree(final PackageInfo rootInfo)
        {
            _root = new PackageTreeNode(rootInfo);
        }

        private PackageTreeNode getNodeForName(final String packageName)
        {
            final String name = StringUtils.defaultString(packageName);

            PackageTreeNode node = _root;
            if (node._info.name.equals(name))
            {
                return node;
            }

            node = getNodeForName(ScriptingUtils.getParentPackageName(name));
            if (node != null)
            {
                for (final PackageTreeNode c : node._children)
                {
                    if (c._info.name.equals(packageName))
                    {
                        return c;
                    }
                }

            }

            return null;
        }

    }

    private static class PackageTreeNode
    {
        private final PackageTreeNode _parent;

        private final List<PackageTreeNode> _children = new ArrayList<PackageTreeNode>();

        private final PackageInfo _info;

        private PackageTreeNode(final PackageInfo info)
        {
            _parent = null;
            _info = info;
        }

        private PackageTreeNode(final PackageTreeNode parentNode, final PackageInfo info)
        {
            _parent = parentNode;
            _info = info;
        }

        void addChildPackage(final PackageInfo info)
        {
            _children.add(new PackageTreeNode(this, info));
        }

        Map<String, String> getEffectiveTestData()
        {
            final Map<String, String> data;
            if (_parent != null)
            {
                data = _parent.getEffectiveTestData();
            }
            else
            {
                data = new HashMap<String, String>();
            }
            data.putAll(_info.getTestData());

            return data;
        }
    }
}
