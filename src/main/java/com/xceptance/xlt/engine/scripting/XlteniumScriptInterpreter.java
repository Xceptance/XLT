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
package com.xceptance.xlt.engine.scripting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.xceptance.xlt.engine.XltExecutionContext;

/**
 * A script interpreter that reads test case scripts from XML files.
 */
public class XlteniumScriptInterpreter extends AbstractScriptInterpreter
{
    /**
     * An error handler for errors in XML files.
     */
    private static class XmlErrorHandler implements ErrorHandler
    {
        /**
         * The number of errors encountered.
         */
        int errors;

        /**
         * The number of warnings encountered.
         */
        int warnings;

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
            if (LOG.isWarnEnabled())
            {
                LOG.warn(report(exception));
            }

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

    private static final String LINE_NUMBER = "lineNumber";

    /**
     * The log facility.
     */
    private static final Logger LOG = LoggerFactory.getLogger(XlteniumScriptInterpreter.class);

    /**
     * The directory where scripts are stored.
     */
    public static final File SCRIPTS_DIRECTORY;

    static
    {
        final File testSuiteDir = XltExecutionContext.getCurrent().getTestSuiteHomeDirAsFile();
        SCRIPTS_DIRECTORY = new File(testSuiteDir, "scripts");
    }

    /**
     * Returns the corresponding file for a script with the given name.
     * 
     * @param scriptName
     *            the name of the script
     * @return the script file
     * @throws FileNotFoundException
     *             if the file cannot be found
     */
    private static File findScriptFile(final String scriptName) throws IOException
    {
        final String scriptPath = scriptName.replace('.', '/') + ".xml";
        File scriptFile = new File(SCRIPTS_DIRECTORY, scriptPath);

        if (!scriptFile.isFile())
        {
            throw new FileNotFoundException("No script file found for script named: " + scriptName);
        }

        return scriptFile;
    }

    /**
     * Returns the corresponding file for a test case script with the given name.
     * 
     * @param scriptName
     *            the name of the script
     * @return the script file
     * @throws IOException
     *             if the file cannot be found
     */
    public static File findTestCaseScriptFile(final String scriptName) throws IOException
    {
        return findScriptFile(scriptName);
    }

    /**
     * Constructor.
     * 
     * @param baseUrl
     *            the base URL
     * @param webDriver
     *            the WebDriver instance to use
     */
    public XlteniumScriptInterpreter(final WebDriver webDriver)
    {
        super(webDriver);
    }

    /**
     * @param rootElement
     * @param scriptFile
     * @return a Script
     * @throws Exception
     */
    private Script parseJavaModule(final Element rootElement, final File scriptFile) throws Exception
    {
        final String className = rootElement.getAttribute("class");

        // get the module parameters
        final List<String> parameterNames = new ArrayList<String>();

        final NodeList childNodes = rootElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++)
        {
            final Node childNode = childNodes.item(i);

            if (childNode instanceof Element)
            {
                final Element element = (Element) childNode;
                if (element.getTagName().equals("parameter"))
                {
                    parameterNames.add(element.getAttribute("name"));
                }
            }
        }

        return new JavaModule(scriptFile, parameterNames, className);
    }

    /**
     * @param scriptName
     * @return parsed script
     * @throws Exception
     */
    private Script parseScriptFile(final String scriptName) throws Exception
    {
        // find a matching script file
        final File scriptFile = findScriptFile(scriptName);
        LOG.info("Parsing script file: " + scriptFile);

        // parse the file
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = schemaFactory.newSchema(getClass().getResource("xlt-script.xsd"));

        final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxFactory.setSchema(schema);

        final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        final Document document = docBuilderFactory.newDocumentBuilder().newDocument();

        final XmlErrorHandler errorHandler = new XmlErrorHandler();

        final Stack<Element> elementStack = new Stack<Element>();
        final StringBuilder textBuffer = new StringBuilder();
        final DefaultHandler handler = new DefaultHandler()
        {
            private Locator locator;

            @Override
            public void setDocumentLocator(final Locator locator)
            {
                this.locator = locator;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
                throws SAXException
            {
                addTextIfNeeded();
                final Element element = document.createElement(qName);
                for (int i = 0; i < attributes.getLength(); i++)
                {
                    element.setAttribute(attributes.getQName(i), attributes.getValue(i));
                }
                element.setUserData(LINE_NUMBER, locator.getLineNumber(), null);
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
                    document.appendChild(closedElement);
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
                    final Node textNode = document.createTextNode(textBuffer.toString());
                    element.appendChild(textNode);
                    textBuffer.delete(0, textBuffer.length());
                }
            }

            //
            // Delegate error handling to our error handler

            /**
             * {@inheritDoc}
             */
            @Override
            public void warning(SAXParseException e) throws SAXException
            {
                errorHandler.warning(e);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void error(SAXParseException e) throws SAXException
            {
                errorHandler.error(e);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void fatalError(SAXParseException e) throws SAXException
            {
                errorHandler.fatalError(e);
            }
        };
        // create the parser
        final SAXParser saxParser = saxFactory.newSAXParser();
        // and parse the XML file
        saxParser.parse(scriptFile, handler);

        if (errorHandler.errors > 0)
        {
            throw new SAXException(String.format("Parsing the script file '%s' produced %d error(s) and %d warning(s)",
                                                 scriptFile.toString(), errorHandler.errors, errorHandler.warnings));
        }

        if (errorHandler.warnings > 0)
        {
            if (LOG.isWarnEnabled())
            {
                LOG.warn(String.format("Parsing the script file '%s' produced %d warning(s)", scriptFile.toString(),
                                       errorHandler.warnings));
            }
        }

        // let us see what we have
        final Element rootElement = document.getDocumentElement();

        final Script script;
        if ("javamodule".equals(rootElement.getTagName()))
        {
            script = parseJavaModule(rootElement, scriptFile);
        }
        else
        {
            script = parseTestCaseOrScriptModule(rootElement, scriptFile);
        }

        return script;
    }

    /**
     * Parses the XML document rooted at the given element as module or test case script.
     * 
     * @param rootElement
     *            the root element of the XML file
     * @param scriptFile
     *            the XML file to be parsed
     * @return the parsed script
     * @throws Exception
     */
    private Script parseTestCaseOrScriptModule(final Element rootElement, final File scriptFile) throws Exception
    {
        final boolean isTestCase = "testcase".equals(rootElement.getTagName());
        final List<ScriptElement> scriptElements = new ArrayList<ScriptElement>();
        final List<ScriptElement> postSteps = isTestCase ? new ArrayList<ScriptElement>() : null;
        final List<String> scriptParameters = isTestCase ? null : new ArrayList<String>();

        final String pathToScriptFile = scriptFile.getAbsolutePath();

        final NodeList childNodes = rootElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++)
        {
            final Node childNode = childNodes.item(i);

            if (childNode instanceof Element)
            {
                final Element element = (Element) childNode;
                final String tagName = element.getTagName();

                final ScriptElement scriptElement = parseElement(element, pathToScriptFile);
                if (scriptElement != null)
                {
                    scriptElements.add(scriptElement);
                }
                else
                {
                    if (isTestCase)
                    {
                        if ("postSteps".equals(tagName))
                        {
                            final NodeList postStepItems = element.getChildNodes();

                            for (int j = 0; j < postStepItems.getLength(); j++)
                            {
                                final Node postStepItem = postStepItems.item(j);

                                if (postStepItem instanceof Element)
                                {

                                    final ScriptElement postStep = parseElement((Element) postStepItem, pathToScriptFile);
                                    if (postStep == null)
                                    {
                                        continue;
                                    }

                                    postSteps.add(postStep);
                                }
                            }

                        }
                    }
                    else
                    {
                        if ("parameter".equals(tagName))
                        {
                            scriptParameters.add(element.getAttribute("name"));
                        }
                    }
                }

            }
        }

        final Script script;
        if (isTestCase)
        {
            script = new TestCase(scriptFile, scriptElements, postSteps, rootElement.getAttribute("baseURL"),
                                  rootElement.getAttribute("disabled"));
        }
        else
        {
            script = new ScriptModule(scriptFile, scriptElements, scriptParameters);
        }

        return script;
    }

    /**
     * Parses the given XML element node as {@link ScriptElement}.
     * 
     * @param element
     *            the XML element node
     * @param scriptFilename
     *            the name of the script file
     * @return parsed ScriptElement if the given XML element node is valid or <code>null</code> otherwise
     * @throws Exception
     */
    private ScriptElement parseElement(final Element element, final String scriptFilename) throws Exception
    {
        final String name = element.getAttribute("name");
        final boolean disabled = Boolean.valueOf(element.getAttribute("disabled"));
        final int lineNumber = (Integer) element.getUserData(LINE_NUMBER);

        final String tagName = element.getTagName();
        final ScriptElement scriptElement;
        if (tagName.equals("command"))
        {
            // get the target form either the attribute or the child node
            String target = element.getAttribute("target");
            if (target.length() == 0)
            {
                final NodeList targetElements = element.getElementsByTagName("target");
                if (targetElements.getLength() > 0)
                {
                    target = targetElements.item(0).getTextContent();
                }
            }

            // get the value from either the attribute or the child node
            String value = element.getAttribute("value");
            if (value.length() == 0)
            {
                final NodeList valueElements = element.getElementsByTagName("value");
                if (valueElements.getLength() > 0)
                {
                    value = valueElements.item(0).getTextContent();
                }
            }

            // build the command
            scriptElement = new Command(name, disabled, target, value, lineNumber);
        }
        else if (tagName.equals("module"))
        {
            // get the module parameters
            final Map<String, String> parameters = new HashMap<String, String>();

            final NodeList moduleChildNodes = element.getChildNodes();
            CallCondition condition = null;

            for (int j = 0; j < moduleChildNodes.getLength(); j++)
            {
                final Node moduleChildNode = moduleChildNodes.item(j);
                if (moduleChildNode instanceof Element)
                {
                    final Element moduleChildElement = (Element) moduleChildNode;
                    final String childTagName = moduleChildElement.getTagName();
                    if (childTagName.equals("parameter"))
                    {
                        parameters.put(moduleChildElement.getAttribute("name"), moduleChildElement.getAttribute("value"));
                    }
                    else if (childTagName.equals("condition"))
                    {
                        if (condition != null)
                        {
                            if (LOG.isErrorEnabled())
                            {
                                LOG.error(String.format("More than one condition found for module call '%s' in script '%s' at line %d",
                                                        name, scriptFilename, lineNumber));
                            }
                        }
                        condition = new CallCondition(Boolean.valueOf(moduleChildElement.getAttribute("disabled")),
                                                      moduleChildElement.getTextContent());
                    }
                }
            }

            // build the module
            scriptElement = new ModuleCall(name, disabled, condition, parameters, lineNumber);
        }
        else if (tagName.equals("action"))
        {
            scriptElement = new Action(name, disabled, lineNumber);
        }
        else if (tagName.equals("codecomment"))
        {
            // build the comment
            scriptElement = new CodeComment(element.getTextContent(), lineNumber);
        }
        else
        {
            scriptElement = null;
        }

        return scriptElement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Script parseModuleScriptFile(final String scriptName) throws Exception
    {
        return parseScriptFile(scriptName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Script parseTestCaseScriptFile(final String scriptName) throws Exception
    {
        return parseScriptFile(scriptName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected File getScriptsDirectory()
    {
        return SCRIPTS_DIRECTORY;
    }
}
