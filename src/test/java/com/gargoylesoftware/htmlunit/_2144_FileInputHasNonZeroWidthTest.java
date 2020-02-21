/*
 * File: _2144_FileInputHasNonZeroWidthTest.java
 * Created on: Jul 3, 2014
 * 
 * Copyright 2014
 * Xceptance Software Technologies GmbH, Germany.
 */
package com.gargoylesoftware.htmlunit;

import net.sourceforge.htmlunit.corejs.javascript.Context;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.ClientRect;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;
import com.xceptance.xlt.AbstractWebTestCase;

/**
 * Tests that the bounding box of HTML file inputs have non-zero height and width.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class _2144_FileInputHasNonZeroWidthTest extends AbstractWebTestCase
{
    @Test
    public void testNonZeroWidth() throws Throwable
    {
        final String html = "<html><body><input type='file' value='' name='foo' /></body></html>";
        final HtmlPage page = loadPage(html);
        final HtmlElement fileInput = page.getFirstByXPath(".//input[@type='file']");

        final HTMLElement scritable = (HTMLElement) fileInput.getScriptableObject();
        final ClientRect rectum;
        try
        {
            Context.enter();
            rectum = scritable.getBoundingClientRect();
        }
        finally
        {
            Context.exit();
        }

        // assure that element has non-zero height
        Assert.assertTrue("Height of file input is 0", rectum.getHeight() > 0);
        // assure that element has non-zero width
        Assert.assertTrue("Width of file input is 0", rectum.getWidth() > 0);
    }
}
