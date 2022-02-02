/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
import com.xceptance.xlt.showcases.actions.PromptString;

/**
 * This test case demonstrate the handling of prompt boxes. Our page contains a button which opens a prompt box. The
 * entered message is displayed on the page. To handle the prompt box we have to add a PromptHandler. Consider that some
 * pattern could be refused by the prompt, e.g. <script>
 */
public class TPrompt extends AbstractTestCase
{
    /**
     * Demonstrating prompt box handling
     */
    @Test
    public void prompting() throws Throwable
    {
        // read the start url from properties
        final String startUrl = XltProperties.getInstance().getProperty("com.xceptance.xlt.showcases.tests.showcases-url");

        // open showcases homepage
        final OpenPage homepage = new OpenPage(startUrl);
        homepage.run();

        // go to the prompt page
        final GoToShowCase promptPage = new GoToShowCase(homepage, "prompt");
        promptPage.run();

        // enter specified string
        final PromptString handlePromptPage = new PromptString(promptPage, "test");
        handlePromptPage.run();
    }
}
