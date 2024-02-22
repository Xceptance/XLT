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
package posters.loadtest.tests;

import org.junit.Test;

import posters.loadtest.actions.Homepage;

import com.xceptance.xlt.api.tests.AbstractTestCase;

/**
 * This test case simulates a single click visit. The visitor opens the poster store landing page and will not do any
 * interaction.
 */
public class TVisit extends AbstractTestCase
{
    /**
     * Main test method
     */
    @Test
    public void visitPosterStore() throws Throwable
    {
        // Read the store URL from properties.
        final String url = getProperty("store-url", "http://localhost:8080/posters/");

        // Go to poster store homepage
        final Homepage homepage = new Homepage(url);
        // Disable JavaScript for the complete test case to reduce client side resource consumption.
        // If JavaScript executed functionality is needed to proceed with the scenario (i.e. AJAX calls)
        // we will simulate this in the related actions.
        homepage.getWebClient().getOptions().setJavaScriptEnabled(false);
        homepage.run();
    }
}
