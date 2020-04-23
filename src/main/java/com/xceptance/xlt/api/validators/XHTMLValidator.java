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
package com.xceptance.xlt.api.validators;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;

/**
 * This validator executes JTidy to check the returned HTML code for standard conformance. It supports different level
 * of stoppage in case of validation problems.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class XHTMLValidator
{
    /**
     * Property name.
     */
    private static final String propertyName = XHTMLValidator.class.getName() + ".enabled";

    /**
     * Keeps the information whether to break at errors or not.
     */
    private final boolean breakOnErrors;

    /**
     * Keeps the information whether to break at warnings or not.
     */
    private final boolean breakOnWarnings;

    /**
     * Keeps the state to allow control by an external property.
     */
    private final boolean enabled;

    /**
     * Constructor.
     * 
     * @param breakOnErrors
     *            should we issue an assertion in case of errors
     * @param breakOnWarnings
     *            should we issue an assertion in case of warnings
     */
    public XHTMLValidator(final boolean breakOnErrors, final boolean breakOnWarnings)
    {
        this.breakOnErrors = breakOnErrors;
        this.breakOnWarnings = breakOnWarnings;

        enabled = XltProperties.getInstance().getProperty(propertyName, true);
    }

    /**
     * Validates the specified HTML page.
     * 
     * @param page
     *            the page to check
     * @throws AssertionError
     *             if the page fails validation
     */
    public void validate(final HtmlPage page) throws Exception
    {
        validate(page.getWebResponse().getContentAsString());
    }

    /**
     * Validates the specified lightweight HTML page.
     * 
     * @param page
     *            the page to check
     * @throws AssertionError
     *             if the page fails validation
     */
    public void validate(final LightWeightPage page) throws Exception
    {
        validate(page.getContent());
    }

    /**
     * Does the validation and raises an exception if configured. You can use this method directly, but it is encouraged
     * to use the default validator method instead.
     * 
     * @param content
     *            the page to validate
     * @exception Exception
     *                an exception in case of an error
     */
    public void validate(final String content) throws Exception
    {
        // check active?
        if (!enabled)
        {
            return;
        }

        final LocalErrorHandler localErrorHandler = new LocalErrorHandler();
        try
        {
            // parse an XML document into a DOM tree
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setValidating(true);
            final DocumentBuilder parser = documentBuilderFactory.newDocumentBuilder();

            parser.setEntityResolver(new LocalEntityResolver());
            parser.setErrorHandler(localErrorHandler);

            parser.parse(new InputSource(new StringReader(content)));
        }
        catch (final ParserConfigurationException e)
        {
            XltLogger.runTimeLogger.error("Unable to setup parser for XHTML validation", e);
            throw e;
        }
        catch (final IOException e)
        {
            XltLogger.runTimeLogger.error("Problems handling I/O for XHTML validation", e);
            throw e;
        }
        catch (final SAXException e)
        {
            // ignore, handled internally
        }

        final List<String> errors = localErrorHandler.getErrors();
        final List<String> warnings = localErrorHandler.getWarnings();

        if (breakOnErrors && !errors.isEmpty())
        {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < errors.size(); i++)
            {
                sb.append("\n");
                sb.append(errors.get(i));
            }
            Assert.fail("XHTML Validation errors:" + sb.toString());
        }
        if (breakOnWarnings && !warnings.isEmpty())
        {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < warnings.size(); i++)
            {
                sb.append("\n");
                sb.append(warnings.get(i));
            }
            Assert.fail("XHTML Validation warnings: " + sb.toString());
        }

        for (int i = 0; i < errors.size(); i++)
        {
            XltLogger.runTimeLogger.warn(errors.get(i));
        }

        for (int i = 0; i < warnings.size(); i++)
        {
            XltLogger.runTimeLogger.warn(warnings.get(i));
        }
    }

    /**
     * Returns the default instance of this validator.
     * <p style="color:green">
     * Note, that the default validator will stop on ALL errors and ALL warnings.
     * </p>
     * 
     * @return the default instance
     */
    public static XHTMLValidator getInstance()
    {
        return XHTMLValidator_Singleton._instance;
    }

    /**
     * Singleton implementation of {@link XHTMLValidator}.
     */
    private static class XHTMLValidator_Singleton
    {
        /**
         * The singleton instance.
         */
        private static final XHTMLValidator _instance;

        // static initializer (synchronized by class loader)
        static
        {
            _instance = new XHTMLValidator(true, true);
        }
    }

    /**
     * An entity resolver to prevent the system from calling external resources.
     */
    private static class LocalEntityResolver implements EntityResolver
    {
        @Override
        public InputSource resolveEntity(final String publicID, final String systemID) throws SAXException, IOException
        {
            // system id might be http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd
            // tokenize it
            final String[] tokens = systemID.split("/");

            InputStream stream = null;
            if (tokens != null && tokens.length > 0)
            {
                stream = getClass().getResourceAsStream("/dtds/" + tokens[tokens.length - 1]);
            }

            if (stream != null)
            {
                return new InputSource(new InputStreamReader(stream));
            }
            else
            {
                // well, run the old, might cause external traffic, therefore
                // warn!
                XltLogger.runTimeLogger.warn("Could not find local representation of entity '" + systemID +
                                             "'. Taking fallback to online version.");
                return new InputSource(systemID);
            }
        }
    }

    /**
     * Local error handler for parsing problems.
     */
    private static class LocalErrorHandler implements ErrorHandler
    {
        /**
         * List of warnings.
         */
        private final List<String> warnings = new ArrayList<String>();

        /**
         * List of errors.
         */
        private final List<String> errors = new ArrayList<String>();

        /**
         * Process warning message.
         * 
         * @param exception
         *            exception context
         */
        @Override
        public void warning(final SAXParseException exception)
        {
            warnings.add(buildMessage(exception));
        }

        /**
         * Process error message.
         * 
         * @param exception
         *            exception context
         */
        @Override
        public void error(final SAXParseException exception)
        {
            errors.add(buildMessage(exception));
        }

        /**
         * Process fatal errors.
         * 
         * @param exception
         *            exception context
         */
        @Override
        public void fatalError(final SAXParseException exception)
        {
            errors.add(buildMessage(exception));
        }

        /**
         * Returns the errors as list.
         * 
         * @return list of errors
         */
        public List<String> getErrors()
        {
            return errors;
        }

        /**
         * Returns the warnings as list.
         * 
         * @return list of warnings
         */
        public List<String> getWarnings()
        {
            return warnings;
        }

        /**
         * Builds a message.
         * 
         * @param exception
         *            exception context
         * @return a readable message
         */
        private String buildMessage(final SAXParseException exception)
        {
            final StringBuilder msg = new StringBuilder(100);

            msg.append("Line:Column ");
            msg.append(exception.getLineNumber());
            msg.append(":");
            msg.append(exception.getColumnNumber());
            msg.append(" - ");
            msg.append(exception.getMessage());

            return msg.toString();
        }
    }
}
