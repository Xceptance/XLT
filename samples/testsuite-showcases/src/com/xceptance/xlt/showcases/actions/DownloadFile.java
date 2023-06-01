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
package com.xceptance.xlt.showcases.actions;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.htmlunit.Page;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlPage;
import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;

/**
 * We will download the file and write it to disk.
 */
public class DownloadFile extends AbstractHtmlPageAction
{
    /**
     * The timer name to use. The timer name is used to log measurements associated with this action. It can be passed
     * to the super class by the constructor.
     */
    private static final String TIMERNAME = "DownloadFile";

    /**
     * the file anchor
     */
    private HtmlAnchor fileAnchor;

    /**
     * The page that contains the file
     */
    private Page filePage;

    /**
     * the name of the file anchor
     */
    private final String anchorName;

    /**
     * Constructor.
     * 
     * @param previousAction
     *            the action we come from
     * @param anchorName
     *            the name of the file anchor
     */
    public DownloadFile(final AbstractHtmlPageAction previousAction, final String anchorName)
    {
        super(previousAction, TIMERNAME);
        this.anchorName = anchorName;

    }

    /**
     * Verify all preconditions. The prevalidate method is a used to ensure that everything that is needed to execute
     * this action is present on the page.
     */
    @Override
    public void preValidate() throws Exception
    {
        final HtmlPage page = getPreviousAction().getHtmlPage();

        // let's get the anchor
        fileAnchor = page.getAnchorByText(anchorName);
        fileAnchor.getHrefAttribute();

    }

    /**
     * Here we will click the link and receive the file.
     */
    @Override
    protected void execute() throws Exception
    {
        // click the link
        filePage = fileAnchor.click();
    }

    /**
     * Validate the correctness of the result.
     */
    @Override
    protected void postValidate() throws Exception
    {
        // check if we have a TextPage
        // in this case we check the content
        if (filePage.getClass().toString().contains("TextPage"))
        {
            Assert.assertEquals(filePage.getWebResponse().getContentAsString(), "test");
        }

        // get Url of the file
        final URL fileUrl = filePage.getWebResponse().getWebRequest().getUrl();

        // now we extract the file name from the url to create our file on disk
        final File file = new File("results/TFileDownload" + fileUrl.getFile().substring(fileUrl.getFile().lastIndexOf('/')));

        // now save file to disk
        FileUtils.copyURLToFile(fileUrl, file);

    }
}
