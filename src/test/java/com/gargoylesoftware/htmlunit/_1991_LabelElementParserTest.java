/*
 * File: _1991_LabelElementParserTest.java
 * Created on: Jul 1, 2014
 * 
 * Copyright 2014
 * Xceptance Software Technologies GmbH, Germany.
 */
package com.gargoylesoftware.htmlunit;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.AbstractWebTestCase;

/**
 * Tests the correct parsing of HTML label elements. See issue 1991 for details.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class _1991_LabelElementParserTest extends AbstractWebTestCase
{
    @Test
    public void parseInline() throws Throwable
    {
        final String html = "<html>\n<body>\n<a href=\"http://foo.com\">\n<label>\nXL\n</label>\n</a>\n</body>\n</html>";
        final HtmlPage page = loadPage(html);
        final String xpathExpr = "/html/body/a/label";
        final List<?> labels = page.getByXPath(xpathExpr);
        Assert.assertFalse("No such element: " + xpathExpr,labels.isEmpty());
    }

    @Test
    public void parseBlock() throws Throwable
    {
        final String html = "<html>\n<body>\n<label><div>foo</div></label></html>";
        final HtmlPage page = loadPage(html);
        final String xpathExpr = "/html/body/label/div";
        final List<?> labels = page.getByXPath(xpathExpr);
        Assert.assertFalse("No such element: " + xpathExpr,labels.isEmpty());
    }

}
