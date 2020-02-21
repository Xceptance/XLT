package com.xceptance.xlt.showcases.tests;

import org.junit.Test;

import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.showcases.actions.DownloadFile;
import com.xceptance.xlt.showcases.actions.GoToShowCase;
import com.xceptance.xlt.showcases.actions.OpenPage;

/**
 * This test case will show how we can handle file downloads. As example we download a txt, a pdf, a png and a jpg file.
 * This files are written to disk and can be found in the result/TFileDownload folder.
 */
public class TFileDownload extends AbstractTestCase
{
    /**
     * Demonstrating File downloading
     */
    @Test
    public void downloading() throws Throwable
    {
        // read the start url from properties
        final String startUrl = XltProperties.getInstance().getProperty("com.xceptance.xlt.showcases.tests.showcases-url");

        // open showcases homepage
        final OpenPage homepage = new OpenPage(startUrl);
        homepage.run();

        // go the the download page
        final GoToShowCase downloadPage = new GoToShowCase(homepage, "download");
        downloadPage.run();

        DownloadFile downloadFilePage;

        // now load the files
        for (int i = 1; i <= 4; i++)
        {
            // now download the file
            downloadFilePage = new DownloadFile(downloadPage, "file" + i);
            downloadFilePage.run();
        }
    }
}
