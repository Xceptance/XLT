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
import com.xceptance.xlt.showcases.actions.EnterTextClosePopup;
import com.xceptance.xlt.showcases.actions.GoToShowCase;
import com.xceptance.xlt.showcases.actions.OpenOnloadPopup;
import com.xceptance.xlt.showcases.actions.OpenPage;
import com.xceptance.xlt.showcases.actions.OpenPopup;

/**
 * In this test case we can see how to handle a popup. The test case page contains a button that opens a popup. On this
 * popup we enter a text and press apply. This will transfer the text to the start page and close the popup. Afterwards
 * we load a page with an onload popup. We show how to work with the different WebWindows and close the Popup at the end.
 * So we can see that such a popup doesn't disturb a normal flow.
 */
public class TPopup extends AbstractTestCase
{
    /**
     * Demonstrating popup handling
     */
    @Test
    public void popingUp() throws Throwable
    {
        // read the start url from properties
        final String startUrl = XltProperties.getInstance().getProperty("com.xceptance.xlt.showcases.tests.showcases-url");

        // open showcases homepage
        final OpenPage homepage = new OpenPage(startUrl);
        homepage.run();

        // go to the popup page
        final GoToShowCase popupHomePage = new GoToShowCase(homepage, "popup");
        popupHomePage.run();

        // open the popup
        final OpenPopup popupPage = new OpenPopup(popupHomePage);
        popupPage.run();

        // enter a text and close the popup
        final EnterTextClosePopup closePopupPage = new EnterTextClosePopup(popupPage, "test");
        closePopupPage.run();

        // open page with onload popup
        final OpenOnloadPopup openOnloadPopupPage = new OpenOnloadPopup(closePopupPage);
        openOnloadPopupPage.run();
    }
}
