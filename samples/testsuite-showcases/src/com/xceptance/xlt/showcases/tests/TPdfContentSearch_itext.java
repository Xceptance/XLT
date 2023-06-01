/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.showcases.tests;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.htmlunit.html.HtmlAnchor;
import org.junit.Assert;
import org.junit.Test;

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

        // go to the pdf page
        final GoToShowCase pdfContentSearchPage = new GoToShowCase(homepage, "pdfcontentsearch");
        pdfContentSearchPage.run();

        // select pdf anchor on page
        final HtmlAnchor anchor = HtmlPageUtils.findSingleHtmlElementByXPath(pdfContentSearchPage.getHtmlPage(), "//a[@id='pdf']");

        // get absolute url from pdf file
        String url = anchor.getHrefAttribute();
        if (!url.contains("http"))
        {
            url = URLUtils.makeLinkAbsolute(pdfContentSearchPage.getHtmlPage().getWebResponse().getWebRequest().getUrl().toString(), url);
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
