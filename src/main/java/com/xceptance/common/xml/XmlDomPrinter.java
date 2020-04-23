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
