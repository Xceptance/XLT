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
package com.xceptance.xlt.showcases.actions;

import java.util.List;

import org.htmlunit.html.FrameWindow;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlPage;
import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.validators.StandardValidator;

/**
 * Switch content of frames by clicking an anchor
 */
public class SwitchFrameContent extends AbstractHtmlPageAction
{
    /**
     * The timer name to use. The timer name is used to log measurements associated with this action. It can be passed
     * to the super class by the constructor.
     */
    private static final String TIMERNAME = "SwitchFrameContent";

    /**
     * Specifies the anchor
     */
    private final int anchorNumber;

    /**
     * the anchor
     */
    private HtmlAnchor anchor;

    /**
     * the name of the anchor for validation
     */
    private String anchorName;

    /**
     * Constructor
     * 
     * @param previousAction
     *            the action we come from
     * @param anchorNumber
     *            specifies the anchor
     */
    public SwitchFrameContent(final AbstractHtmlPageAction previousAction, final int anchorNumber)
    {
        super(previousAction, TIMERNAME);
        this.anchorNumber = anchorNumber;
    }

    /**
     * Verify all preconditions. The prevalidate method is a used to ensure that everything that is needed to execute
     * this action is present on the page.
     */
    @Override
    public void preValidate() throws Exception
    {
        // check if got a positive number
        Assert.assertTrue(anchorNumber >= 0);

        final HtmlPage previousPage = getPreviousAction().getHtmlPage();

        // check if there are any frames
        Assert.assertFalse(previousPage.getFrames().isEmpty());

        // now get the page of the "navigation" frame
        // alternative we could iterate through the list of frames and look for
        // anchors
        final FrameWindow frame = previousPage.getFrameByName("navigation");
        final HtmlPage framePage = (HtmlPage) frame.getEnclosedPage();

        // collect the anchors of this page
        final List<HtmlAnchor> anchors = framePage.getAnchors();

        // check if we got at least one
        Assert.assertFalse(anchors.isEmpty());

        // check if the have enough links for the request
        Assert.assertTrue(anchorNumber < anchors.size());

        // we have enough anchors so get the correct
        anchor = anchors.get(anchorNumber);

        // store the name of the anchor for the post validation
        anchorName = anchor.getNameAttribute();
    }

    /**
     * Execute the request. Once pre-execution conditions have been meet, the execute method can be called to load the
     * page.
     */
    @Override
    protected void execute() throws Exception
    {
        // load the page by clicking the anchor
        loadPageByClick(anchor);
    }

    /**
     * Validate the correctness of the result.
     */
    @Override
    protected void postValidate() throws Exception
    {
        final HtmlPage page = getHtmlPage();
        // First, we check all common criteria. This code can be bundled and
        // reused
        // if needed. For the purpose of the programming example, we leave it
        // here as
        // detailed as possible.
        // We add a catch block to the test running.
        // Messages are logged.
        StandardValidator.getInstance().validate(page);

        // check if we change the content successfully
        final HtmlPage framePage = (HtmlPage) page.getFrameByName("data").getEnclosedPage();
        Assert.assertTrue(framePage.asNormalizedText().contains(anchorName));
    }
}
