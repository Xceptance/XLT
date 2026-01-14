package com.xceptance.xlt.report.scorecard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

/**
 * Access to live data from the report XML via XPath. This class is intended to be used in Groovy scorecard
 * configurations.
 */
public class ScorecardData
{
    private final XdmNode document;

    private final XPathCompiler compiler;

    public ScorecardData(final XdmNode document, final XPathCompiler compiler)
    {
        this.document = Objects.requireNonNull(document);
        this.compiler = Objects.requireNonNull(compiler);
    }

    /**
     * Executes the given XPath expression and returns the string value of the first result.
     *
     * @param xpath
     *                  the XPath expression
     * @return the string value or null if not found
     */
    public String get(final String xpath)
    {
        try
        {
            final XdmValue result = compiler.evaluate(xpath, document);
            return result.isEmpty() ? null : result.itemAt(0).getStringValue();
        }
        catch (final SaxonApiException e)
        {
            return null;
        }
    }

    /**
     * Executes the given XPath expression and returns a list of string values.
     *
     * @param xpath
     *                  the XPath expression
     * @return list of string values
     */
    public List<String> getList(final String xpath)
    {
        final List<String> values = new ArrayList<>();
        try
        {
            final XdmValue result = compiler.evaluate(xpath, document);
            for (final XdmItem item : result)
            {
                values.add(item.getStringValue());
            }
        }
        catch (final SaxonApiException e)
        {
            // ignore
        }
        return values;
    }

    /**
     * Returns the raw document node.
     */
    public XdmNode getDocument()
    {
        return document;
    }
}
