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
package com.xceptance.xlt.showcases.tests;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.htmlunit.html.HtmlImage;
import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.HtmlPageUtils;
import com.xceptance.xlt.api.util.URLUtils;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.showcases.actions.GoToShowCase;
import com.xceptance.xlt.showcases.actions.OpenPage;

/**
 * In this test case we compare an image from a web page with an image from disk. First we load the image to a file.
 * Afterwards we compare both images.
 */
public class TCompareImages extends AbstractTestCase
{
    /**
     * Demonstrating jpg comparison
     */

    @Test
    public void jpgComparing() throws Throwable
    {
        // read the start url from properties
        final String startUrl = XltProperties.getInstance().getProperty("com.xceptance.xlt.showcases.tests.showcases-url");

        // open showcases homepage
        final OpenPage homepage = new OpenPage(startUrl);
        homepage.run();

        // go to the jpg compare page
        final GoToShowCase jpgComparePage = new GoToShowCase(homepage, "jpgcompare");
        jpgComparePage.run();

        // select sample image on page
        final HtmlImage image = HtmlPageUtils.findSingleHtmlElementByXPath(jpgComparePage.getHtmlPage(), "//img[@name='sample']");

        // get absolute url from sample file
        String url = image.getSrcAttribute();
        if (!url.contains("http"))
        {
            url = URLUtils.makeLinkAbsolute(jpgComparePage.getHtmlPage().getWebResponse().getWebRequest().getUrl().toString(), url);
        }

        // create new file to store the image form the page
        // because this test case might run concurrently, we have to create a random file
        // and remove it after the test again to avoid common problems
        File webImage = null;
        try
        {
            webImage = File.createTempFile("web", "jpg", new File("results"));

            // copy image to file
            FileUtils.copyURLToFile(new URL(url), webImage);

            Assert.assertTrue("File doesn't exist: " + webImage.getAbsolutePath(), webImage.exists());

            // load compare image
            final File compareImage = new File("config/data/sample.jpg");

            Assert.assertTrue("File doesn't exist: " + compareImage.getAbsolutePath(), compareImage.exists());

            // check if the images are equal
            Assert.assertTrue(FileUtils.contentEquals(webImage, compareImage));
        }
        finally
        {
            webImage.delete();
        }
    }
}
