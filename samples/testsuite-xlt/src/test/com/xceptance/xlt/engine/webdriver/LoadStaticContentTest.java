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
package test.com.xceptance.xlt.engine.webdriver;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.WebDriver;

import com.xceptance.xlt.api.engine.NetworkData;
import com.xceptance.xlt.api.engine.RequestFilter;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * static content load checker
 * 
 *  loadStaticContent  |  jsEnabled  |  cssEnabled  |  to load
 * ------------------------------------------------------------
 *          0          |      0      |      0       |  /
 *          0          |      0      |      1       |  CSS
 *          0          |      1      |      0       |  JS
 *          0          |      1      |      1       |  CSS + JS
 * ------------------------------------------------------------
 *          1          |      0      |      0       |  CSS + JS + IMG
 *          1          |      0      |      1       |  CSS + JS + IMG
 *          1          |      1      |      0       |  CSS + JS + IMG
 *          1          |      1      |      1       |  CSS + JS + IMG
 *          
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
@RunWith(value = Parameterized.class)
public class LoadStaticContentTest
{
    private boolean loadStaticContent;
    private boolean jsEnabled;
    private boolean cssEnabled;
    
    private WebDriver driver;
    
    private String configAsString;
    
    @Parameters
    public static Collection<Object[]> data()
    {
        Object[][] data = new Object[][] {
                { false, false, false },
                { false, false, true }, 
                { false, true, false }, 
                { false, true, true },
                { true, false, false },
                { true, false, true }, 
                { true, true, false },
                { true, true, true } };
        return Arrays.asList(data);
    }
    
    /**
     * Constructor.
     */
    public LoadStaticContentTest(final boolean loadStaticContent, final boolean jsEnabled, final boolean cssEnabled)
    {
        this.loadStaticContent   = loadStaticContent;
        this.jsEnabled           = jsEnabled;
        this.cssEnabled          = cssEnabled;
    }

    /**
     * Executes the test.
     *
     * @throws Throwable if anything went wrong
     */
    @Test
    public void checkLoadBehavior()
    {
        // set properties
        XltProperties.getInstance().setProperty("com.xceptance.xlt.loadStaticContent", String.valueOf(loadStaticContent));
        XltProperties.getInstance().setProperty("com.xceptance.xlt.javaScriptEnabled", String.valueOf(jsEnabled));
        XltProperties.getInstance().setProperty("com.xceptance.xlt.cssEnabled",        String.valueOf(cssEnabled));
        
        // create driver and load page
        driver = new XltDriver();
        Session.getCurrent().clear();
        driver.get("http://localhost:8080/testpages/staticcontent/linked-static-content.html");
        
        configAsString = new StringBuilder()
                            .append("loadStaticContent:").append(loadStaticContent)
                            .append(", javaScriptEnabled:").append(jsEnabled)
                            .append(", cssEnabled:").append(cssEnabled)
                            .toString();

        // IMG
        Assert.assertTrue(getErrorMessage("Image", loadStaticContent), loadStaticContent == isLoaded(".*gif$"));
        
        // CSS
        Assert.assertTrue(getErrorMessage("CSS", cssEnabled), (loadStaticContent||cssEnabled) == isLoaded(".*css$"));
        
        // JS
        Assert.assertTrue(getErrorMessage("JavaScript", jsEnabled), (loadStaticContent||jsEnabled) == isLoaded(".*js$"));
    }

    @After
    public void after()
    {
        driver.quit();
    }

    /**
     * check if resource was loaded
     * @param urlFilterRegex
     */
    private boolean isLoaded(final String urlFilterRegex)
    {
        final RequestFilter filter = new RequestFilter();
        filter.setUrlPattern(urlFilterRegex);
        final List<NetworkData> networkData = Session.getCurrent().getNetworkDataManager().getData(filter);
        return !networkData.isEmpty();
    }
    
    /**
     * &lt;WHAT&gt; was (not) loaded<br>
     * example: getErrorMessage("CSS", true); -> "CSS was not loaded [&lt;CURRENT_CONFIG&gt;]"
     * @param what what was (not) loaded
     * @param notCondition not?
     * @return
     */
    private String getErrorMessage(final String what, final boolean notCondition)
    {
        return what+" was"+(notCondition?" not":"")+" loaded ["+configAsString+"]";
    }
}