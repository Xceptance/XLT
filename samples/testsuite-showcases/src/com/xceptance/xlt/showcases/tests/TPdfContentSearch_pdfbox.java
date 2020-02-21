package com.xceptance.xlt.showcases.tests;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.HtmlPageUtils;
import com.xceptance.xlt.api.util.URLUtils;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.showcases.actions.GoToShowCase;
import com.xceptance.xlt.showcases.actions.OpenPage;

/**
 * In this show case you can see how to search in a pdf file for a search word. In this case we use the pdfbox library.
 * We take the pdf file and search on each page sequential.
 */
public class TPdfContentSearch_pdfbox extends AbstractTestCase
{
    /**
     * Demonstrating pdf parsing with pdfBox
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

        // select anchors on page
        final HtmlAnchor anchor = HtmlPageUtils.findSingleHtmlElementByXPath(pdfContenSearchPage.getHtmlPage(), "//a[@id='pdf']");

        // get absolute url from pdf file
        String url = anchor.getHrefAttribute();
        if (!url.contains("http"))
        {
            url = URLUtils.makeLinkAbsolute(pdfContenSearchPage.getHtmlPage().getWebResponse().getWebRequest().getUrl().toString(), url);
        }

        // create an Url
        final URL urlFile = new URL(url);

        // create a parser for the file
        final PDFParser parser = new PDFParser(urlFile.openStream());

        // parse the file
        parser.parse();
        // get the document
        final PDDocument doc = parser.getPDDocument();

        // create a text stripper to search for the search word
        final PDFTextStripper stripper = new PDFTextStripper();
        // create a list for the search results
        final List<Integer> searchResult = new LinkedList<Integer>();

        // our search word
        final String searchWord = "Lorem";

        // now search on each page for the search word
        for (int i = 1; i <= doc.getNumberOfPages(); i++)
        {
            stripper.setStartPage(i);
            stripper.setEndPage(i);

            // if we found the word add the page to the result list
            if (stripper.getText(doc).contains(searchWord))
            {
                searchResult.add(i);
            }
        }
        doc.close();

        Assert.assertFalse(searchResult.isEmpty());
    }
}
