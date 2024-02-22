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
import com.xceptance.xlt.showcases.actions.GoToShowCase;
import com.xceptance.xlt.showcases.actions.OpenPage;
import com.xceptance.xlt.showcases.actions.SwitchFrameContent;

/**
 * This test case demonstrate the handling of frames. We have a simple page with two frames. The first frame contains
 * the navigation and the second the content. In our test we will click the different navigation links and check if the
 * content frame displays the correct content.
 */
public class TFrames extends AbstractTestCase
{
    /**
     * Demonstrating frame handling
     */
    @Test
    public void framing() throws Throwable
    {
        // read the start url from properties
        final String startUrl = XltProperties.getInstance().getProperty("com.xceptance.xlt.showcases.tests.showcases-url");

        // open showcases homepage
        final OpenPage homepage = new OpenPage(startUrl);
        homepage.run();

        // go to the frame page
        final GoToShowCase framePage = new GoToShowCase(homepage, "frames");
        framePage.run();

        // click the first link
        SwitchFrameContent switchFramePage = new SwitchFrameContent(framePage, 0);
        switchFramePage.run();

        // a loop for the other links
        for (int i = 1; i < 3; i++)
        {
            switchFramePage = new SwitchFrameContent(switchFramePage, i);
            switchFramePage.run();
        }
    }
}
