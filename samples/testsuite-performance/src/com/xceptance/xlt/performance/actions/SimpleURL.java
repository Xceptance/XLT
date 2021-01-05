/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.performance.actions;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.validators.HttpResponseCodeValidator;
import com.xceptance.xlt.performance.util.ParameterUtils;

/**
 * This is a simple test class for pulling urls. Fully configurable
 * using properties.
 *
 * @author  Rene Schwietzke
 * 
 */
public class SimpleURL extends AbstractHtmlPageAction
{
    private final String url;
    private final String xpath;
    private final String text;
    
    /**
     * @param previousAction
     * @param timerName
     */
    public SimpleURL(final String timerName, final String url, final String xpath, final String text)
    {
        super(timerName);

        this.url = url;
        this.xpath = xpath;
        this.text = text;
    }

    /**
     * @param previousAction
     * @param timerName
     */
    public SimpleURL(AbstractHtmlPageAction prevAction, final String timerName, final String url, final String xpath, final String text)
    {
        super(prevAction, timerName);

        this.url = url;
        this.xpath = xpath;
        this.text = text;
    }
    
    /* (non-Javadoc)
     * @see com.xceptance.xlt.api.actions.AbstractAction#preValidate()
     */
    @Override
    public void preValidate() throws Exception
    {
    }

    /* (non-Javadoc)
     * @see com.xceptance.xlt.api.actions.AbstractAction#execute()
     */
    @Override
    protected void execute() throws Exception
    {
        // replace a random value position if needed
        loadPage(ParameterUtils.replaceDynamicParameters(url));
    }

    /* (non-Javadoc)
     * @see com.xceptance.xlt.api.actions.AbstractAction#postValidate()
     */
    @Override
    protected void postValidate() throws Exception
    {
        // validate response code
        HtmlPage page = getHtmlPage();

        // response code = 200?
        HttpResponseCodeValidator.getInstance().validate(page);

        // does the length match?
        // ContentLengthValidator.getInstance().validate(page);
        
        // is the page closed </html>
        // HtmlEndTagValidator.getInstance().validate(page); 

        // check the special path
        if (xpath == null || text == null)
        {
            return;
        }
        if (xpath.length() == 0)
        {
            return;
        }
        
        // ok, do it
        List<HtmlElement> elements = page.getByXPath(xpath);
        Assert.assertFalse("xpath not found '" + xpath + "'", elements.isEmpty());
        
        final String got = elements.get(0).asText();
        final String expected = text.trim();
        
        final Pattern pattern = RegExUtils.getPattern(expected);
        final Matcher matcher = pattern.matcher(got);
        
        Assert.assertTrue("Expected '"+expected+"', got '"+got+"'.", matcher.matches());
    }

}
