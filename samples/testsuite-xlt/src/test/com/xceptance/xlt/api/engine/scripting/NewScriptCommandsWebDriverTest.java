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
package test.com.xceptance.xlt.api.engine.scripting;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.engine.scripting.TestContext;

import scripting.modules.Open_ExamplePage;
import scripting.modules.StartAppear;
import scripting.modules.StartDisappear;

/**
 * Test implementation of new methods introduced to Scripting API as part of improvement #3007.
 */
public class NewScriptCommandsWebDriverTest extends AbstractWebDriverScriptTestCase
{

    public NewScriptCommandsWebDriverTest()
    {
        super(new XltDriver(true), "http://localhost:8080");
    }

    @Before
    public void setup()
    {
        TestContext.getCurrent().setTimeout(3000L);
    }

    @Test
    public void testAttribute() throws Throwable
    {
        new Open_ExamplePage().execute();

        startAction("assertAttribute");
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "foobar");
        assertAttribute("xpath=id('ws8_a')/input[1]", "value", "foobar");

        shouldFail(() -> assertAttribute("doesnotexist", "val", "foobar"));
        shouldFail(() -> assertAttribute("xpath=id('ws8_a')/input[1]", null, "foobar"));
        shouldFail(() -> assertAttribute("xpath=id('ws8_a')/input[1]", "value", null));
        shouldFail(() -> assertAttribute("xpath=id('ws8_a')/input[1]", "", "foobar"));
        shouldFail(() -> assertAttribute("xpath=id('ws8_a')/input[1]", "  ", "foobar"));

        startAction("assertNotAttribute");
        assertNotAttribute("xpath=id('ws8_a')/input[1]@value", "foo");
        assertNotAttribute("xpath=id('ws8_a')/input[1]", "value", "foo");

        shouldFail(() -> assertNotAttribute("doesnotexist", "val", "foobar"));
        shouldFail(() -> assertNotAttribute("xpath=id('ws8_a')/input[1]", null, "foobar"));
        shouldFail(() -> assertNotAttribute("xpath=id('ws8_a')/input[1]", "value", null));
        shouldFail(() -> assertNotAttribute("xpath=id('ws8_a')/input[1]", "", "foobar"));
        shouldFail(() -> assertNotAttribute("xpath=id('ws8_a')/input[1]", "  ", "foobar"));

        startAction("storeGetAttribute");
        storeAttribute("xpath=id('ws8_a')/input[1]", "value", "att");

        assertAttribute("xpath=id('ws8_a')/input[1]@value", "exact:${att}");

        Assert.assertEquals(getAttribute("xpath=id('ws8_a')/input[1]@value"), getAttribute("xpath=id('ws8_a')/input[1]", "value"));
        Assert.assertEquals(resolve("${att}"), getAttribute("xpath=id('ws8_a')/input[1]", "value"));

        shouldFail(() -> storeAttribute("xpath=id('ws8_a')/input[1]", null, "foobar"));
        shouldFail(() -> storeAttribute("xpath=id('ws8_a')/input[1]", "value", null));
        shouldFail(() -> storeAttribute("xpath=id('ws8_a')/input[1]", "", "foobar"));
        shouldFail(() -> storeAttribute("xpath=id('ws8_a')/input[1]", "  ", "foobar"));

        shouldFail(() -> getAttribute("xpath=id('ws8_a')/input[1]", null));
        shouldFail(() -> getAttribute("xpath=id('ws8_a')/input[1]", ""));
        shouldFail(() -> getAttribute("xpath=id('ws8_a')/input[1]", "  "));

        new StartAppear().execute("100");

        startAction("waitForAttribute");
        waitForAttribute("xpath=id('appear_9')@name", "text");
        waitForAttribute("xpath=id('appear_9')", "name", "text");

        shouldFail(() -> waitForAttribute("doesnotexist", "value", "foobar"));
        shouldFail(() -> waitForAttribute("xpath=id('ws8_a')/input[1]", null, "foobar"));
        shouldFail(() -> waitForAttribute("xpath=id('ws8_a')/input[1]", "value", null));
        shouldFail(() -> waitForAttribute("xpath=id('ws8_a')/input[1]", "", "foobar"));
        shouldFail(() -> waitForAttribute("xpath=id('ws8_a')/input[1]", "  ", "foobar"));

        new StartDisappear().execute("100");

        startAction("waitForNotAttribute");
        waitForNotAttribute("xpath=id('disapp_10')@name", "disapp_10");
        waitForNotAttribute("xpath=id('disapp_10')", "name", "disapp_10");

        shouldFail(() -> waitForNotAttribute("doesnotexist", "value", "foobar"));
        shouldFail(() -> waitForNotAttribute("xpath=id('ws8_a')/input[1]", null, "foobar"));
        shouldFail(() -> waitForNotAttribute("xpath=id('ws8_a')/input[1]", "value", null));
        shouldFail(() -> waitForNotAttribute("xpath=id('ws8_a')/input[1]", "", "foobar"));
        shouldFail(() -> waitForNotAttribute("xpath=id('ws8_a')/input[1]", "  ", "foobar"));
    }

    @Test
    public void testElementCount() throws Throwable
    {
        new Open_ExamplePage().execute();

        startAction("elementCount");
        assertElementCount("css=.disapp_11", 1);
        assertElementCount("css=.disapp_11", "1");
        Assert.assertEquals(1, getElementCount("css=.disapp_11"));

        assertNotElementCount("link=xyz", 1);
        assertNotElementCount("link=xyz", "1");
        Assert.assertNotEquals(1, getElementCount("link=xyz"));

        shouldFailWithNumberFormatException(() -> assertElementCount("//*[contains(@class,'disapp_11')]", "zzz"));
        shouldFailWithNumberFormatException(() -> assertNotElementCount("//*[contains(@class,'disapp_11')]", "zzz"));
        shouldFailWithNumberFormatException(() -> waitForElementCount("//*[contains(@class,'disapp_11')]", "zzz"));
        shouldFailWithNumberFormatException(() -> waitForNotElementCount("//*[contains(@class,'disapp_11')]", "zzz"));

        shouldFail(() -> assertElementCount(null, 1));
        shouldFail(() -> assertElementCount(null, "1"));
        shouldFail(() -> assertElementCount("doesnotmatter", -4));
        shouldFail(() -> assertElementCount("doesnotmatter", "-4"));
        shouldFail(() -> assertElementCount("doobar", 1));
        shouldFail(() -> assertElementCount("doobar", "1"));
        shouldFail(() -> assertNotElementCount(null, 1));
        shouldFail(() -> assertNotElementCount(null, "1"));
        shouldFail(() -> assertNotElementCount("doesnotmatter", -21));
        shouldFail(() -> assertNotElementCount("doesnotmatter", "-21"));
        shouldFail(() -> assertNotElementCount("doobar", 0));
        shouldFail(() -> assertNotElementCount("doobar", "0"));
        shouldFail(() -> waitForElementCount(null, 1));
        shouldFail(() -> waitForElementCount(null, "1"));
        shouldFail(() -> waitForElementCount("doesnotmatter", -4));
        shouldFail(() -> waitForElementCount("doesnotmatter", "-4"));
        shouldFail(() -> waitForElementCount("doobar", 1));
        shouldFail(() -> waitForElementCount("doobar", "1"));
        shouldFail(() -> waitForNotElementCount(null, 1));
        shouldFail(() -> waitForNotElementCount(null, "1"));
        shouldFail(() -> waitForNotElementCount("doesnotmatter", -4));
        shouldFail(() -> waitForNotElementCount("doesnotmatter", "-4"));
        shouldFail(() -> waitForNotElementCount("doobar", 1));
        shouldFail(() -> waitForNotElementCount("doobar", "1"));
        shouldFail(() -> getElementCount(null));

        new StartAppear().execute("100");
        waitForNotElementCount("css=.appear_7", 0);
        waitForNotElementCount("css=.appear_7", "0");
        waitForElementCount("css=.appear_7", 1);
        waitForElementCount("css=.appear_7", "1");
    }

    @Test
    public void testXPathCount() throws Throwable
    {
        new Open_ExamplePage().execute();

        startAction("xpathCount");
        assertXpathCount("//*[contains(@class,'disapp_11')]", 1);
        assertXpathCount("//*[contains(@class,'disapp_11')]", "1");
        Assert.assertEquals(1, getXpathCount("//*[contains(@class,'disapp_11')]"));

        assertNotXpathCount("//a[text()='xyz']", 1);
        assertNotXpathCount("//a[text()='xyz']", "1");
        Assert.assertNotEquals(1, getXpathCount("//a[text()='xyz']"));

        shouldFailWithNumberFormatException(() -> assertXpathCount("//*[contains(@class,'disapp_11')]", "zzz"));
        shouldFailWithNumberFormatException(() -> assertNotXpathCount("//*[contains(@class,'disapp_11')]", "zzz"));
        shouldFailWithNumberFormatException(() -> waitForXpathCount("//*[contains(@class,'disapp_11')]", "zzz"));
        shouldFailWithNumberFormatException(() -> waitForNotXpathCount("//*[contains(@class,'disapp_11')]", "zzz"));

        shouldFail(() -> assertXpathCount(null, 1));
        shouldFail(() -> assertXpathCount(null, "1"));
        shouldFail(() -> assertXpathCount("doesnotmatter", -4));
        shouldFail(() -> assertXpathCount("doesnotmatter", "-4"));
        shouldFail(() -> assertXpathCount("doobar", 1));
        shouldFail(() -> assertXpathCount("doobar", "1"));
        shouldFail(() -> assertNotXpathCount(null, 1));
        shouldFail(() -> assertNotXpathCount(null, "1"));
        shouldFail(() -> assertNotXpathCount("doesnotmatter", -21));
        shouldFail(() -> assertNotXpathCount("doesnotmatter", "-21"));
        shouldFail(() -> assertNotXpathCount("doobar", 0));
        shouldFail(() -> assertNotXpathCount("doobar", "0"));
        shouldFail(() -> waitForXpathCount(null, 1));
        shouldFail(() -> waitForXpathCount(null, "1"));
        shouldFail(() -> waitForXpathCount("doesnotmatter", -4));
        shouldFail(() -> waitForXpathCount("doesnotmatter", "-4"));
        shouldFail(() -> waitForXpathCount("doobar", 1));
        shouldFail(() -> waitForXpathCount("doobar", "1"));
        shouldFail(() -> waitForNotXpathCount(null, 1));
        shouldFail(() -> waitForNotXpathCount(null, "1"));
        shouldFail(() -> waitForNotXpathCount("doesnotmatter", -4));
        shouldFail(() -> waitForNotXpathCount("doesnotmatter", "-4"));
        shouldFail(() -> waitForNotXpathCount("doobar", 1));
        shouldFail(() -> waitForNotXpathCount("doobar", "1"));
        shouldFail(() -> getXpathCount(null));

        new StartAppear().execute("100");
        waitForNotXpathCount("//*[contains(@class,'appear_7')]", "0");
        waitForNotXpathCount("//*[contains(@class,'appear_7')]", "0");
        waitForXpathCount("//*[contains(@class,'appear_7')]", "1");
        waitForXpathCount("//*[contains(@class,'appear_7')]", "1");
    }

    @Test
    public void testEvaluate() throws Throwable
    {
        new Open_ExamplePage().execute();

        startAction("evaluate");
        storeEval("document.title", "foo");
        final String r = evaluate("document.title");
        Assert.assertEquals(resolve("${foo}"), r);
        Assert.assertEquals("undefined", evaluate("document.titel"));

        shouldFail(() -> evaluate(null));
    }

    @Test
    public void testGetters() throws Throwable
    {
        new Open_ExamplePage().execute();

        startAction("getText");
        assertText("id=in_txt_1", "regexp:in_[tx]{3}_1");
        storeText("id=in_txt_1", "txt");
        Assert.assertEquals(resolve("${txt}"), getText("id=in_txt_1"));

        shouldFail(() -> getText(null));
        shouldFail(() -> getText("doenotexist"));

        startAction("getTitle");
        storeTitle("title");
        assertTitle("exact:${title}");
        Assert.assertEquals(resolve("${title}"), getTitle());

        startAction("getValue");
        Assert.assertEquals("in_ta_2", getValue("id=in_ta_2"));
        Assert.assertEquals("invisible_hidden_input", getValue("id=invisible_hidden_input"));

        shouldFail(() -> getValue(null));
        shouldFail(() -> getValue("doesnotexist"));

        startAction("getXPathCount");
        assertXpathCount("id('notexisting')", 0);
        Assert.assertEquals(0, getXpathCount("id('notexisting')"));

        shouldFail(() -> getXpathCount(null));
        shouldFail(() -> getXpathCount(""));
    }

    @Test
    public void testConditionals() throws Throwable
    {
        new Open_ExamplePage().execute();

        startAction("isChecked");
        Assert.assertFalse(isChecked("id=in_chk_1"));
        Assert.assertTrue(isChecked("id=in_chk_5"));
        check("in_chk_1");
        Assert.assertTrue(isChecked("in_chk_1"));

        shouldFail(() -> isChecked(null));
        shouldFail(() -> isChecked("doesnotexist"));
        shouldFail(() -> isChecked("anc_sel1"));

        startAction("isElementPresent");
        Assert.assertTrue(isElementPresent("id=in_txt_2"));
        Assert.assertFalse(isElementPresent("notexisting"));
        Assert.assertTrue(isElementPresent("id=invisible_hidden_input"));

        shouldFail(() -> isElementPresent(null));

        startAction("isEnabled");
        Assert.assertFalse(isEnabled("id=in_txt_12"));
        Assert.assertFalse(isEnabled("id=in_chk_12"));
        Assert.assertTrue(isEnabled("id=in_chk_1"));

        shouldFail(() -> isEnabled(null));
        shouldFail(() -> isEnabled("doesnotexist"));

        startAction("isVisible");
        Assert.assertFalse(isVisible("id=invisible_hidden_input"));
        Assert.assertTrue(isVisible("in_txt_1"));

        shouldFail(() -> isVisible(null));
        shouldFail(() -> isVisible("doesnotexist"));
    }

    @Test
    public void testFindElement() throws Throwable
    {
        new Open_ExamplePage().execute();

        startAction("findElements");
        Assert.assertEquals(0, findElements("xpath=id('notexisting')").size());

        final List<WebElement> matches = findElements("id=anc_sel1");
        Assert.assertEquals(1, matches.size());
        Assert.assertEquals(matches.get(0), findElement("id=anc_sel1"));

        try
        {
            findElement("foobarbaz");
            Assert.fail("Should throw expection");
        }
        catch (org.openqa.selenium.NoSuchElementException nse)
        {
            // Expected
        }
    }

    private void shouldFail(final Runnable r)
    {
        try
        {
            r.run();
            Assert.fail("Passed runnable should not succeed");
        }
        catch (final Throwable t)
        {
            // Expected
        }
    }

    private void shouldFailWithNumberFormatException(final Runnable r)
    {
        try
        {
            r.run();
            Assert.fail("Passed runnable should not succeed");
        }
        catch (final NumberFormatException t)
        {
            // Expected
        }
    }

}
