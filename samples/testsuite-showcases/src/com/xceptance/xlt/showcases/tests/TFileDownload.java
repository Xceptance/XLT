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

        // go to the download page
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
