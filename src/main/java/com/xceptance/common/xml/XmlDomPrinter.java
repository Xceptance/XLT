package com.xceptance.common.xml;

import java.io.PrintWriter;

import org.w3c.dom.Element;

/**
 * An {@link AbstractDomPrinter} implementation that generates XML output from a DOM. With this printer, empty tags will
 * always be closed with " /&gt;".
 * 
 * @see HtmlDomPrinter
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class XmlDomPrinter extends AbstractDomPrinter
{
    /**
     * Creates a new printer object with pretty-printing disabled.
     */
    public XmlDomPrinter()
    {
        super();
    }

    /**
     * Creates a new printer object. Whether pretty-printing is enabled depends on the value of the "spaces" parameter.
     * If the value is negative, then pretty-printing is disabled. Otherwise the elements are indented with the number
     * of spaces given.
     * 
     * @param spaces
     *            the number of spaces of one indentation level
     */
    public XmlDomPrinter(final int spaces)
    {
        super(spaces);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void printEmptyElementClosing(final Element element, final PrintWriter printWriter)
    {
        printWriter.print(" />");
    }
}
