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
import com.xceptance.xlt.showcases.actions.ExecuteAlerts;
import com.xceptance.xlt.showcases.actions.GoToShowCase;
import com.xceptance.xlt.showcases.actions.OpenPage;

/**
 * This test case shows the way XLT handles alert boxes. The alert boxes are only caught by an alert handler. It isn't
 * needed to push the OK button of the alert box (neither a way to do this). In the action we will first see an onclick
 * alert and afterwards an onload alert. For both cases we have to add an alert handler before.
 */
public class TAlert extends AbstractTestCase
{
    /**
     * Demonstrating alert box handling
     */
    @Test
    public void alerting() throws Throwable
    {
        // read the start url from properties
        final String startUrl = XltProperties.getInstance().getProperty("com.xceptance.xlt.showcases.tests.showcases-url");

        // open showcases homepage
        final OpenPage homepage = new OpenPage(startUrl);
        homepage.run();

        // go the to alert page
        final GoToShowCase alertPage = new GoToShowCase(homepage, "alert");
        alertPage.run();

        // now execute the alerts
        final ExecuteAlerts clickAlertPage = new ExecuteAlerts(alertPage);
        clickAlertPage.run();
    }
}
