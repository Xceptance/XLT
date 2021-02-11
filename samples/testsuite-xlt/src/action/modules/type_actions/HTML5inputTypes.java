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
package action.modules.type_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class HTML5inputTypes extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public HTML5inputTypes(final AbstractHtmlPageAction prevAction)
    {
        super(prevAction);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void preValidate() throws Exception
    {
        final HtmlPage page = getPreviousAction().getHtmlPage();
        Assert.assertNotNull("Failed to get page from previous action", page);

    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() throws Exception
    {
        HtmlPage page = getPreviousAction().getHtmlPage();
        page = type("id=in_email_1", "foo@bar.de");
        assertText("name=cc_keyup", "keyup (in_email_1) foo@bar.de");
        page = type("id=in_tel_1", "08001234567");
        assertText("name=cc_keyup", "keyup (in_tel_1) 08001234567");
        page = type("id=in_tel_1", "foo");
        assertText("name=cc_keyup", "keyup (in_tel_1) foo");
        page = type("id=in_url_1", "http://www.xceptance.de");
        assertText("name=cc_keyup", "glob:keyup (in_url_1) http://www.xceptance.de");
        page = type("id=in_url_1", "bar");
        assertText("name=cc_keyup", "keyup (in_url_1) bar");
        page = type("id=in_datetime_1", "01/01/2012 01:23:45");
        assertText("name=cc_keyup", "glob:keyup (in_datetime_1) 01/01/2012 01:23:45");
        page = type("id=in_datetime_1", "01.01.2012 01:23:45");
        assertText("name=cc_keyup", "glob:keyup (in_datetime_1) 01.01.2012 01:23:45");
        page = type("id=in_datetime_1", "foo");
        assertText("name=cc_keyup", "keyup (in_datetime_1) foo");
        // page = type("id=in_date_1","01");
        // assertText("name=cc_keyup","keyup (in_date_1) 01");
        // page = type("id=in_date_1","Monday");
        // assertText("name=cc_keyup","keyup (in_date_1) Monday");
        // page = type("id=in_date_1","bar");
        // assertText("name=cc_keyup","keyup (in_date_1) bar");
        // page = type("id=in_month_1","12");
        // assertText("name=cc_keyup","keyup (in_month_1) 12");
        // page = type("id=in_month_1","December");
        // assertText("name=cc_keyup","keyup (in_month_1) December");
        // page = type("id=in_month_1","foo");
        // assertText("name=cc_keyup","keyup (in_month_1) foo");
        // page = type("id=in_week_1","12");
        // assertText("name=cc_keyup","keyup (in_week_1) 12");
        // page = type("id=in_week_1","bar");
        // assertText("name=cc_keyup","keyup (in_week_1) bar");
        // page = type("id=in_time_1","01:23:45");
        // assertText("name=cc_keyup","glob:keyup (in_time_1) 01:23:45");
        // page = type("id=in_time_1","foo");
        // assertText("name=cc_keyup","keyup (in_time_1) foo");
        // page = type("id=in_datetime-local_1","bar");
        // assertText("name=cc_keyup","keyup (in_datetime-local_1) bar");
        // page = type("id=in_number_1","12345678901234567890");
        // assertText("name=cc_keyup","keyup (in_number_1) 12345678901234567890");
        // page = type("id=in_number_1","0");
        // assertText("name=cc_keyup","keyup (in_number_1) 0");
        // page = type("id=in_number_1","-12345");
        // assertText("name=cc_keyup","keyup (in_number_1) -12345");
        // page = type("id=in_number_1","foo");
        // assertText("name=cc_keyup","keyup (in_number_1) foo");
        // page = type("id=in_color_1","red");
        // assertText("name=cc_keyup","keyup (in_color_1) red");
        // page = type("id=in_color_1","#aabbcc");
        // assertText("name=cc_change","change (in_color_1) #aabbcc");
        // page = type("id=in_color_1","foo");
        setHtmlPage(page);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void postValidate() throws Exception
    {
        final HtmlPage page = getHtmlPage();
        Assert.assertNotNull("Failed to load page", page);

        // assertText("name=cc_keyup","keyup (in_color_1) foo");
    }
}