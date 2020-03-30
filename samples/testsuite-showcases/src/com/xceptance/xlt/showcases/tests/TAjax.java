/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
import com.xceptance.xlt.showcases.actions.ExecuteAjax;
import com.xceptance.xlt.showcases.actions.GoToShowCase;
import com.xceptance.xlt.showcases.actions.OpenPage;

/**
 * This test case will demonstrate how we can handle ajax requests. On our show case page the content of a div is
 * changed through a button click that executes an ajax request. We will check the content of the div before and
 * afterwards.
 */
public class TAjax extends AbstractTestCase
{
    /**
     * Demonstrating ajax handling
     */
    @Test
    public void usingAjax() throws Throwable
    {
        // read the start url from properties
        final String startUrl = XltProperties.getInstance().getProperty("com.xceptance.xlt.showcases.tests.showcases-url");

        // open showcases homepage
        final OpenPage homepage = new OpenPage(startUrl);
        homepage.run();

        // go the the ajax page
        final GoToShowCase ajaxPage = new GoToShowCase(homepage, "ajax");
        ajaxPage.run();

        // execute ajax
        final ExecuteAjax executeAjaxPage = new ExecuteAjax(ajaxPage);
        executeAjaxPage.run();
    }
}
