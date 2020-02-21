package com.xceptance.xlt.showcases.tests;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.HtmlPageUtils;
import com.xceptance.xlt.api.util.URLUtils;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.showcases.actions.GoToShowCase;
import com.xceptance.xlt.showcases.actions.OpenPage;

/**
 * In this show case you can see how to search in a pdf file for a search word. In this case we use the itext library.
 * We take the pdf file and search on each page sequential.
 */
public class TPdfContentSearch_itext extends AbstractTestCase
{
    /**
     * Demonstrating pdf parsing with itext
     */

    @Test
    public void pdfParsing() throws Throwable
    {
        // read the start url from properties
        final String startUrl = XltProperties.getInstance().getProperty("com.xceptance.xlt.showcases.tests.showcases-url");

        // open showcases homepage
        final OpenPage homepage = new OpenPage(startUrl);
        homepage.run();

        // go the the pdf page
        final GoToShowCase pdfContenSearchPage = new GoToShowCase(homepage, "pdfcontentsearch");
        pdfContenSearchPage.run();

        // select pdf anchor on page
        final HtmlAnchor anchor = HtmlPageUtils.findSingleHtmlElementByXPath(pdfContenSearchPage.getHtmlPage(), "//a[@id='pdf']");

        // get absolute url from pdf file
        String url = anchor.getHrefAttribute();
        if (!url.contains("http"))
        {
            url = URLUtils.makeLinkAbsolute(pdfContenSearchPage.getHtmlPage().getWebResponse().getWebRequest().getUrl().toString(), url);
        }

        // read pdf from URL
        final PdfReader pdfFile = new PdfReader(new URL(url));

        // create text extractor
        final PdfTextExtractor textExtractor = new PdfTextExtractor(pdfFile);

        // specify search word
        final String searchWord = "Lorem";

        // list for the
        final List<Integer> searchResults = new LinkedList<Integer>();

        // now check each page for the search word
        for (int i = 1; i <= pdfFile.getNumberOfPages(); i++)
        {
            if (textExtractor.getTextFromPage(i).contains(searchWord))
            {
                searchResults.add(i);
            }
        }

        pdfFile.close();

        // check if we found at least 1 occurrences
        Assert.assertFalse(searchResults.isEmpty());

    }
}
