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
package com.xceptance.xlt.report.util;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.math3.util.Precision;
import org.jaxen.JaxenException;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.xceptance.common.io.FileUtils;
import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.common.XltConstants;

import io.opencensus.stats.Aggregation.Count;

/**
 * A collection of functionality which is used throughout the report generator.
 */
public final class ReportUtils
{
    /**
     * XPath expression matching the test report's project name.
     */
    public static final String XPATH_PROJECT_NAME = "/testreport/configuration/projectName";

    /**
     * The default number of decimal places.
     */
    public static final int DEFAULT_DECIMAL_PLACES = 3;

    /**
     * Constructor.
     */
    private ReportUtils()
    {
    }

    /**
     * Converts the given double value to its corresponding {@link BigDecimal} equivalent, rounding it to have at most
     * {@value #DEFAULT_DECIMAL_PLACES} decimal places.
     * 
     * @param value
     *            the value to convert
     * @return the converted value
     */
    public static BigDecimal convertToBigDecimal(final double value)
    {
        return convertToBigDecimal(value, DEFAULT_DECIMAL_PLACES);
    }

    /**
     * Converts the given double value to its corresponding {@link BigDecimal} equivalent, rounding it to have at most
     * the specified number of decimal places.
     * 
     * @param value
     *            the value to convert
     * @param decimalPlaces
     *            the number of decimal places
     * @return the converted value
     */
    public static BigDecimal convertToBigDecimal(double value, final int decimalPlaces)
    {
        ParameterCheckUtils.isNotNegative(decimalPlaces, "decimalPlaces");

        // TODO: better use a null BigDecimal instead of falling back to 0
        if (Double.isNaN(value))
        {
            value = 0.0;
        }

        return BigDecimal.valueOf(value).setScale(decimalPlaces, RoundingMode.HALF_EVEN);
    }

    /**
     * Creates a new DOM element with the given tag name and text content and adds it as a child to the specified parent
     * element.
     * 
     * @param tagName
     *            the tag name
     * @param textContent
     *            the text content (maybe <code>null</code>)
     * @param parent
     *            the parent element
     * @return the child element just created
     */
    public static Element addTextElement(final String tagName, final String textContent, final Element parent)
    {
        final Element element = parent.getOwnerDocument().createElement(tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);

        return element;
    }

    @SuppressWarnings("unchecked")
    public static List<Node> evaluateXpathAsNodeSet(final Node node, final String xpathExpression)
    {
        try
        {
            final DOMXPath path = new DOMXPath(xpathExpression);
            return path.selectNodes(node);
        }
        catch (final JaxenException e)
        {
            throw new XltException("Failed to evaluate XPath expression: " + xpathExpression, e);
        }
    }

    public static String evaluateXpathAsString(final Node node, final String xpathExpression)
    {
        return evaluateXpathAsNodeSet(node, xpathExpression).get(0).getTextContent();
    }

    /**
     * @param document
     * @param spec
     * @return the filtered elements
     */
    public static Map<String, Element> filterElements(final Document document, final ElementSpecification spec)
    {
        final Map<String, Element> elementsByName = new LinkedHashMap<String, Element>();

        final List<Node> elements = evaluateXpathAsNodeSet(document, spec.rootElementXpath);
        for (final Node node : elements)
        {
            final Element element = (Element) node;
            final String elementId = spec.idElementTagName == null ? element.getTagName()
                                                                   : getChildElementText(element, spec.idElementTagName);
            elementsByName.put(elementId, element);
        }

        return elementsByName;
    }

    public static String formatValue(final Double value)
    {
        return (value == null) ? "null" : String.valueOf(Precision.round(value, DEFAULT_DECIMAL_PLACES));
    }

    /**
     * Returns a string representation of the given double value that does not contain a fractional part altogether if
     * the value is integral. For example, "5.0" becomes "5", but "5.1" remains as is.
     * 
     * @param value
     *            the value
     * @return the value as a string
     */
    public static String formatValue(final double value)
    {
        final long valueAsLong = (long) value;

        return (value == valueAsLong) ? String.valueOf(valueAsLong) : String.valueOf(value);
    }

    /**
     * Executes an XPath query with the given node and returns the result converted to a date. Note that the query must
     * be formulated such that indeed a string will be the result (which is then converted) and not a node list, etc.
     * 
     * @param node
     *            the context node
     * @param xpath
     *            the XPath query
     * @return the result as a date
     * @throws ParseException
     */
    public static Date getAsDate(final Node node, final String xpath) throws ParseException
    {
        final String result = getAsString(node, xpath);

        // the date is always formatted using the English locale (XStream)
        return new SimpleDateFormat(XltConstants.REPORT_DATE_FORMAT, Locale.ENGLISH).parse(result);
    }

    /**
     * Executes an XPath query with the given node and returns the result as a string. Note that the query must be
     * formulated such that indeed a string will be the result and not a node list, etc.
     * 
     * @param node
     *            the context node
     * @param xpath
     *            the XPath query
     * @return the result as a string
     */
    public static String getAsString(final Node node, final String xpath)
    {
        return evaluateXpathAsString(node, xpath);
    }

    /**
     * Returns the text content of the first child element with the given tag name.
     * 
     * @param node
     *            the context node
     * @param tagName
     *            the tag name
     * @return the text content
     */
    public static String getChildElementText(final Node node, final String tagName)
    {
        final NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++)
        {
            final Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE)
            {
                final Element element = (Element) childNode;

                if (element.getTagName().equals(tagName))
                {
                    return element.getTextContent();
                }
            }
        }

        throw new XltException("No child element found with tag name: " + tagName);
    }

    /**
     * Determines the text content of the first child element with the given tag name and returns it converted to a
     * double.
     * 
     * @param node
     *            the context node
     * @param tagName
     *            the tag name
     * @return the result as a double
     */
    public static double getChildElementTextAsDouble(final Node node, final String tagName)
    {
        final String result = getChildElementText(node, tagName);

        return Double.parseDouble(result);
    }

    /**
     * Determines the text content of the first child element with the given tag name and returns it converted to an
     * integer.
     * 
     * @param node
     *            the context node
     * @param tagName
     *            the tag name
     * @return the result as an integer
     */
    public static int getChildElementTextAsInt(final Node node, final String tagName)
    {
        final String result = getChildElementText(node, tagName);

        return Integer.parseInt(result);
    }

    /**
     * Converts the given file either to a file URI string (on Unix) or to a simple file path string (on Windows). The
     * resulting string is intended for being printed to a console. Unix shells recognize file URIs and make them
     * clickable, while on Windows file paths can be copied to the command line and executed as commands.
     * 
     * @param file
     *            the file
     * @return the resulting string
     */
    public static String toString(final File file)
    {
        return SystemUtils.IS_OS_WINDOWS ? file.getAbsolutePath() : FileUtils.toUri(file).toString();
    }

    /**
     * Calculates summary statistics from the given value set. Note that only the mean of each min/max value is taken
     * into consideration.
     * 
     * @param valueSet
     *            the value set
     * @return the summary statistics
     */
    public static DoubleSummaryStatistics toSummaryStatistics(final DoubleMinMaxValueSet valueSet)
    {
        final DoubleSummaryStatistics stats = new DoubleSummaryStatistics();

        for (final DoubleMinMaxValue value : valueSet.getValues())
        {
            if (value != null)
            {
                stats.addValue(value.getAverageValue());
            }
        }

        return stats;
    }

    /**
     * Determines the project name of the given reports.
     * 
     * @param documents
     *            the parsed reports
     * @return last non-blank project name (in iteration order) of given reports
     */
    public static String obtainProjectName(final Iterable<Document> documents)
    {
        String projectName = null;
        for (final Document doc : documents)
        {
            List<Node> matches = ReportUtils.evaluateXpathAsNodeSet(doc, XPATH_PROJECT_NAME);
            if (!matches.isEmpty())
            {
                final String pName = matches.get(0).getTextContent();
                if (StringUtils.isNotBlank(pName))
                {
                    projectName = pName;
                }
            }
        }
        return StringUtils.defaultString(projectName);
    }

    /**
     * Calculates the percentage given by count/total, as a {@link BigDecimal} (rounding it to have at most
     * {@value #DEFAULT_DECIMAL_PLACES} decimal places). Returns 0 if total is 0.
     * 
     * @param count
     * @param total
     * @return
     */
    public static BigDecimal calculatePercentage(int count, int total)
    {
        if (count == 0)
        {
            return new BigDecimal(0);
        }
        else
        {
            return convertToBigDecimal((double) count * 100 / total);
        }
    }
}
