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
package com.xceptance.xlt.api.util;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.htmlunit.BrowserVersion;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.MockWebConnection;
import org.htmlunit.WebClient;
import org.htmlunit.WebWindow;
import org.htmlunit.html.DefaultElementFactory;
import org.htmlunit.html.DomNodeList;
import org.htmlunit.html.HtmlCheckBoxInput;
import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlRadioButtonInput;
import org.htmlunit.html.HtmlSelect;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.w3c.dom.Node;
import org.xml.sax.helpers.AttributesImpl;

import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.engine.htmlunit.DomNodeListImpl;
import com.xceptance.xlt.engine.util.TimerUtils;
import com.xceptance.xlt.util.HtmlTestViaFile;

/**
 * Test the implementation of {@link HtmlPageUtils}.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(
    {
        XltRandom.class, HtmlForm.class
    })
@PowerMockIgnore({"javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class HtmlPageUtilsTest
{
    /**
     * Tests the implementation of {@link HtmlPageUtils#checkRadioButton(HtmlForm, String, int)} by passing a an invalid
     * parameter.
     */
    @Test
    public void testCheckRadioButton_FormStringInt_InvalidParam()
    {
        try
        {
            HtmlPageUtils.checkRadioButton(null, "any Name", 0);
            Assert.fail("HtmlPageUtils#checkRadioButton(HtmlForm,String,int) should raise an IllegalArgumentException since passed HtmlForm parameter is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.checkRadioButton(Mockito.mock(HtmlForm.class), null, 0);
            Assert.fail("HtmlPageUtils#checkRadioButton(HtmlForm,String,int) should raise an IllegalArgumentException since passed radio button name is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.checkRadioButton(Mockito.mock(HtmlForm.class), "anyName", -1);
            Assert.fail("HtmlPageUtils#checkRadioButton(HtmlForm,String,int) should raise an IllegalArgumentException since passed index is negative.");
        }
        catch (final IllegalArgumentException e)
        {
        }
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#checkRadioButton(HtmlForm, String, int)} by passing a HtmlForm
     * object that doesn't contain a radio button input for the given name.
     */
    @Test(expected = Throwable.class)
    public void testCheckRadioButton_FormStringInt_NoButtonsForName()
    {
        final HtmlForm form = Mockito.mock(HtmlForm.class);
        Mockito.doReturn(Arrays.asList(new HtmlRadioButtonInput[0])).when(form).getRadioButtonsByName("anyName");

        HtmlPageUtils.checkRadioButton(form, "anyName", 0);
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#checkRadioButton(HtmlForm, String, int)} by passing an index
     * which is too big.
     */
    @Test(expected = Throwable.class)
    public void testCheckRadioButton_FormStringInt_IndexTooBig()
    {
        final HtmlForm form = Mockito.mock(HtmlForm.class);
        final HtmlRadioButtonInput input = Mockito.mock(HtmlRadioButtonInput.class);

        Mockito.doReturn(Arrays.asList(new HtmlRadioButtonInput[]
            {
                input
            })).when(form).getRadioButtonsByName("anyName");
        Mockito.doThrow(new TestException()).when(input).setChecked(true);

        HtmlPageUtils.checkRadioButton(form, "anyName", 1);

    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#checkRadioButton(HtmlForm, String, int)} by passing valid
     * parameters.
     */
    @Test(expected = TestException.class)
    public void testCheckRadioButton_FormStringInt()
    {
        final HtmlForm form = Mockito.mock(HtmlForm.class);
        final HtmlRadioButtonInput input = Mockito.mock(HtmlRadioButtonInput.class);

        Mockito.doReturn(Arrays.asList(new HtmlRadioButtonInput[]
            {
                input
            })).when(form).getRadioButtonsByName("anyName");
        Mockito.doReturn("anyForm").when(form).getId();
        Mockito.doThrow(new TestException()).when(input).setChecked(true);

        HtmlPageUtils.checkRadioButton(form, "anyName", 0);

    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#checkRadioButtonRandomly(HtmlForm, String, boolean, boolean)} by
     * passing an invalid parameter.
     */
    @Test
    public void testCheckRadioButtonRandomly_InvalidParam()
    {
        try
        {
            HtmlPageUtils.checkRadioButtonRandomly(null, "anyName", false, false);
            Assert.fail("HtmlPageUtils#checkRadioButtonRandomly(HtmlForm,String,boolean,boolean) should raise an IllegalArgumentException since passed HtmlForm is null!");
        }
        catch (final IllegalArgumentException e)
        {
        }

        final HtmlForm form = Mockito.mock(HtmlForm.class);

        try
        {
            HtmlPageUtils.checkRadioButtonRandomly(form, null, false, false);
            Assert.fail("HtmlPageUtils#checkRadioButtonRandomly(HtmlForm,String,boolean,boolean) should raise an IllegalArgumentException since passed radio button name is null!");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.checkRadioButtonRandomly(form, "", false, false);
            Assert.fail("HtmlPageUtils#checkRadioButtonRandomly(HtmlForm,String,boolean,boolean) should raise an IllegalArgumentException since passed radio button name is blank!");
        }
        catch (final IllegalArgumentException e)
        {
        }
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#checkRadioButtonRandomly(HtmlForm, String, boolean, boolean)} by
     * passing a HtmlForm object which doesn't contain any radio button input with the passed name.
     */
    @Test(expected = AssertionError.class)
    public void testCheckRadioButtonRandomly_NoRadioButtonForGivenName()
    {
        final HtmlForm form = Mockito.mock(HtmlForm.class);
        Mockito.doReturn(Arrays.asList(new HtmlRadioButtonInput[0])).when(form).getRadioButtonsByName("anyName");

        HtmlPageUtils.checkRadioButtonRandomly(form, "anyName", false, false);
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#checkRadioButton(HtmlForm, String, String)} by passing an
     * invalid parameter.
     */
    @Test
    public void testCheckRadioButton_FormStringString_InvalidParam()
    {
        try
        {
            HtmlPageUtils.checkRadioButton(null, "anyName", "anyValue");
            Assert.fail("HtmlPageUtils#checkRadioButton(HtmlForm,String,String) should raise an IllegalArgumentException since passed HtmlForm parameter is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.checkRadioButton(Mockito.mock(HtmlForm.class), null, "anyValue");
            Assert.fail("HtmlPageUtils#checkRadioButton(HtmlForm,String,String) should raise an IllegalArgumentException since passed radio button name is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.checkRadioButton(Mockito.mock(HtmlForm.class), "anyName", null);
            Assert.fail("HtmlPageUtils#checkRadioButton(HtmlForm,String,String) should raise an IllegalArgumentException since passed radio button value is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }
    }

    /**
     * Tests the implementation {@link HtmlPageUtils#checkRadioButton(HtmlForm, String, String)} by passing a HtmlForm
     * which doesn't contain an input field for the given value.
     */
    @Test(expected = AssertionError.class)
    public void testCheckRadioButton_FormStringString_NoInput4Value()
    {
        final HtmlForm form = Mockito.mock(HtmlForm.class);
        final HtmlRadioButtonInput button = Mockito.mock(HtmlRadioButtonInput.class);

        Mockito.doReturn(Arrays.asList(new HtmlRadioButtonInput[]
            {
                button
            })).when(form).getRadioButtonsByName("anyName");
        Mockito.doReturn("").when(button).getValue();

        HtmlPageUtils.checkRadioButton(form, "anyName", "anyValue");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#checkRadioButton(HtmlForm, String, String)} by passing a
     * HtmlForm which contains a HtmlCheckBoxInput for the given value instead of a HtmlRadioButtonInput.
     */
    @Test(expected = AssertionError.class)
    public void testCheckRadioButton_FormStringString_NotExpectedType()
    {
        final HtmlForm form = Mockito.mock(HtmlForm.class);
        final HtmlCheckBoxInput box = Mockito.mock(HtmlCheckBoxInput.class);
        Mockito.doReturn(box).when(form).getInputByValue("anyValue");

        HtmlPageUtils.checkRadioButton(form, "anyName", "anyValue");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#checkRadioButton(HtmlForm, String, String)} by passing a
     * HtmlForm which holds a HtmlRadioButtonInput named different to the expected one.
     */
    @Test(expected = AssertionError.class)
    public void testCheckRadioButton_FormStringString_InputHasDifferentName() throws Throwable
    {
        final HtmlForm form = Mockito.mock(HtmlForm.class);
        final HtmlPage page = getHtmlPage("");

        final AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(null, "name", "name", "", "differentName");
        atts.addAttribute(null, "value", "value", "", "anyValue");
        atts.addAttribute(null, "type", "type", "", "radio");
        final HtmlRadioButtonInput input = (HtmlRadioButtonInput) new DefaultElementFactory().createElement(page, HtmlInput.TAG_NAME, atts);

        Mockito.doReturn(input).when(form).getInputByValue("anyValue");

        HtmlPageUtils.checkRadioButton(form, "anyName", "anyValue");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#checkRadioButton(HtmlForm, String, String)}.
     */
    @Test(expected = TestException.class)
    public void testCheckRadioButton_FormStringString()
    {
        final HtmlForm form = Mockito.mock(HtmlForm.class);
        final HtmlRadioButtonInput button = Mockito.mock(HtmlRadioButtonInput.class);

        Mockito.doReturn(Arrays.asList(new HtmlRadioButtonInput[]
            {
                button
            })).when(form).getRadioButtonsByName("anyName");
        Mockito.doReturn("anyValue").when(button).getValue();
        Mockito.doThrow(new TestException()).when(button).setChecked(true);

        HtmlPageUtils.checkRadioButton(form, "anyName", "anyValue");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#checkRadioButtonRandomly(HtmlForm, String, boolean, boolean)} .
     */
    @Test(expected = TestException.class)
    public void testCheckRadioButtonRandomly()
    {
        final HtmlForm form = Mockito.mock(HtmlForm.class);
        final HtmlRadioButtonInput button = Mockito.mock(HtmlRadioButtonInput.class);

        Mockito.doReturn(Arrays.asList(new HtmlRadioButtonInput[]
            {
                button
            })).when(form).getRadioButtonsByName("anyButton");
        Mockito.doThrow(new TestException()).when(button).setChecked(true);

        HtmlPageUtils.checkRadioButtonRandomly(form, "anyButton", false, false);
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#checkRadioButtonRandomly(HtmlForm, String)} .
     */
    @Test(expected = TestException.class)
    public void testSimpleCheckRadioButtonRandomly()
    {
        final HtmlForm form = Mockito.mock(HtmlForm.class);
        final HtmlRadioButtonInput button = Mockito.mock(HtmlRadioButtonInput.class);

        Mockito.doReturn(Arrays.asList(new HtmlRadioButtonInput[]
            {
                button
            })).when(form).getRadioButtonsByName("anyButton");
        Mockito.doThrow(new TestException()).when(button).setChecked(true);

        HtmlPageUtils.checkRadioButtonRandomly(form, "anyButton");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#createHtmlElement(String, HtmlElement)} by passing an invalid
     * parameter.
     */
    @Test
    public void testCreateHtmlElement_InvalidParam()
    {
        try
        {
            HtmlPageUtils.createHtmlElement("anytag", null);
            Assert.fail("HtmlPageUtils#createHtmlElement(String,HtmlElement) should raise an IllegalArgumentException since passed HtmlElement is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        final HtmlElement el = Mockito.mock(HtmlElement.class);

        try
        {
            HtmlPageUtils.createHtmlElement(null, el);
            Assert.fail("HtmlPageUtils#createHtmlElement(String,HtmlElement) should raise an IllegalArgumentException since passed tag name is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.createHtmlElement("", el);
            Assert.fail("HtmlPageUtils#createHtmlElement(String,HtmlElement) should raise an IllegalArgumentException since passed tag name is blank.");
        }
        catch (final IllegalArgumentException e)
        {
        }

    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#createHtmlElement(String, HtmlElement)}.
     */
    @Test(expected = TestException.class)
    public void testCreateHtmlElement() throws Throwable
    {
        final HtmlElement parent = Mockito.mock(HtmlElement.class);
        final HtmlPage page = getHtmlPage("");

        Mockito.doReturn(page).when(parent).getPage();

        final HtmlElement child = HtmlPageUtils.createHtmlElement("dIv", parent);
        Assert.assertNotNull(child);
        Assert.assertEquals("div", child.getTagName());
        Assert.assertTrue("Not a HtmlDivision", child instanceof HtmlDivision);

        Mockito.doThrow(new TestException()).when(parent).appendChild((Node) ArgumentMatchers.any());

        HtmlPageUtils.createHtmlElement("anyTag", parent);
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#createInput(HtmlForm, String, String, String)} by passing an
     * invalid parameter.
     */
    @Test
    public void testCreateInput_InvalidParam()
    {
        try
        {
            HtmlPageUtils.createInput(null, "anyType", "anyName", null);
            Assert.fail("HtmlPageUtils#createInput(HtmlForm,String,String,String) should raise an InvalidArgumentException since passed HtmlForm parameter is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        final HtmlForm form = Mockito.mock(HtmlForm.class);

        try
        {
            HtmlPageUtils.createInput(form, null, "anyName", null);
            Assert.fail("HtmlPageUtils#createInput(HtmlForm,String,String,String) should raise an InvalidArgumentException since passed type parameter is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.createInput(form, "", "anyName", null);
            Assert.fail("HtmlPageUtils#createInput(HtmlForm,String,String,String) should raise an InvalidArgumentException since passed type parameter is blank.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.createInput(form, "anyType", null, null);
            Assert.fail("HtmlPageUtils#createInput(HtmlForm,String,String,String) should raise an InvalidArgumentException since passed name parameter is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.createInput(form, "anyType", "", null);
            Assert.fail("HtmlPageUtils#createInput(HtmlForm,String,String,String) should raise an InvalidArgumentException since passed name parameter is blank.");
        }
        catch (final IllegalArgumentException e)
        {
        }

    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#createInput(HtmlForm, String, String, String)} by passing valid
     * parameters.
     */
    @Test(expected = TestException.class)
    public void testCreateInput() throws FailingHttpStatusCodeException, MalformedURLException, IOException
    {
        final HtmlForm form = Mockito.mock(HtmlForm.class);
        final HtmlPage page = getHtmlPage("");
        Mockito.doReturn(page).when(form).getPage();

        final HtmlInput i = HtmlPageUtils.createInput(form, "radio", "anyName", "anyValue");
        Assert.assertEquals("anyName", i.getNameAttribute());
        Assert.assertEquals("radio", i.getTypeAttribute());
        Assert.assertEquals("anyValue", i.getValueAttribute());

        Mockito.doThrow(new TestException()).when(form).appendChild((Node) ArgumentMatchers.any());

        HtmlPageUtils.createInput(form, "radio", "anyName", "anyValue");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#findHtmlElements(HtmlElement, String)} by passing an invalid
     * parameter.
     */
    @Test
    public void testFindHtmlElements_HtmlElementString_InvalidParam()
    {
        try
        {
            HtmlPageUtils.findHtmlElements((HtmlElement) null, "./a[@name='anyName']");
            Assert.fail("HtmlPageUtils#findHtmlElements(HtmlElement,String) should raise an IllegalArgumentException since passed HtmlElement is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        final HtmlElement el = Mockito.mock(HtmlElement.class);

        try
        {
            HtmlPageUtils.findHtmlElements(el, null);
            Assert.fail("HtmlPageUtils#findHtmlElements(HtmlElement,String) should raise an IllegalArgumentException since passed xpath is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#findHtmlElements(HtmlElement, String)} by passing XPath that
     * doesn't match any element.
     */
    @Test(expected = AssertionError.class)
    public void testFindHtmlElements_HtmlElementString_NoElement4XPath()
    {
        final HtmlElement el = Mockito.mock(HtmlElement.class);
        Mockito.doReturn(Collections.EMPTY_LIST).when(el).getByXPath("./a[@name='anyName']");

        HtmlPageUtils.findHtmlElements(el, "./a[@name='anyName']");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#findHtmlElements(HtmlElement, String)} by passing valid
     * parameters (XPath matches exactly one element).
     */
    @Test
    public void testFindHtmlElements_HtmlElementString()
    {
        final HtmlElement el = Mockito.mock(HtmlElement.class);
        final List<Object> list = new ArrayList<Object>();
        list.add(new Object());

        Mockito.doReturn(list).when(el).getByXPath("./a[@name='anyName']");

        final List<HtmlElement> retVal = HtmlPageUtils.findHtmlElements(el, "./a[@name='anyName']");
        Assert.assertEquals(list, retVal);
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#findHtmlElements(HtmlPage, String)} by passing an invalid
     * parameter.
     */
    @Test
    public void testFindHtmlElements_HtmlPageString_InvalidParam()
    {
        try
        {
            HtmlPageUtils.findHtmlElements((HtmlPage) null, "//div[@class='test']");
            Assert.fail("HtmlPageUtils#findHtmlElements(HtmlPage,String) should raise an IllegalArgumentException since passed HtmlPage is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.findHtmlElements(new HtmlPage(null, Mockito.mock(WebWindow.class)), null);
            Assert.fail("HtmlPageUtils#findHtmlElements(HtmlPage,String) should raise an IllegalArgumentException since passed XPath expression is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#findHtmlElements(HtmlPage, String)}.
     */
    @Test(expected = AssertionError.class)
    public void testFindHtmlElements_HtmlPageString() throws Throwable
    {
        final HtmlPage page = getHtmlPage("<html><body><div class='test'>TEST</div></body></html>");

        Assert.assertNotNull(HtmlPageUtils.findHtmlElements(page, "//div[@class='test']"));

        HtmlPageUtils.findHtmlElements(page, "//div[@class='Test']");
    }

    /**
     * Tests the implementation of findHtmlElementsAndPickOne(HtmlPage, String, ..).
     *
     * @throws IOException
     */
    @Test
    public void testFindHtmlElementsAndPickOneFromHtmlPage() throws IOException
    {
        // always return the first element of the list
        mockStatic(XltRandom.class);
        expect(XltRandom.nextInt(3)).andReturn(0).times(3);
        expect(XltRandom.nextInt(2)).andReturn(0).times(3);
        expect(XltRandom.nextInt(1)).andReturn(0);
        PowerMock.replay(XltRandom.class);

        final HtmlPage page = getHtmlPage("<html><body><div class='first'>test</div><div class='second'>test</div><div class='third'>test</div></body></html>");
        // get all 'div' elements
        final String xPath = "//div";

        Assert.assertEquals("HtmlDivision[<div class=\"first\">]", HtmlPageUtils.findHtmlElementsAndPickOne(page, xPath).toString());
        Assert.assertEquals("HtmlDivision[<div class=\"first\">]", HtmlPageUtils.findHtmlElementsAndPickOne(page, xPath, false).toString());
        // exclude the first element, so get the second one
        Assert.assertEquals("HtmlDivision[<div class=\"second\">]", HtmlPageUtils.findHtmlElementsAndPickOne(page, xPath, true).toString());
        Assert.assertEquals("HtmlDivision[<div class=\"first\">]",
                            HtmlPageUtils.findHtmlElementsAndPickOne(page, xPath, false, false).toString());
        Assert.assertEquals("HtmlDivision[<div class=\"first\">]",
                            HtmlPageUtils.findHtmlElementsAndPickOne(page, xPath, false, true).toString());
        // exclude the first element, so get the second one
        Assert.assertEquals("HtmlDivision[<div class=\"second\">]",
                            HtmlPageUtils.findHtmlElementsAndPickOne(page, xPath, true, false).toString());
        Assert.assertEquals("HtmlDivision[<div class=\"second\">]",
                            HtmlPageUtils.findHtmlElementsAndPickOne(page, xPath, true, true).toString());
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#findSingleHtmlElementByID(HtmlPage, String)} by passing an
     * invalid parameter.
     */
    @Test
    public void testFindSingleHtmlElementByID_InvalidParam()
    {
        try
        {
            HtmlPageUtils.findSingleHtmlElementByID((HtmlPage) null, "anyID");
            Assert.fail("HtmlPageUtils#findSingleHtmlElementByID(HtmlPage,String) should raise an IllegalArgumentException since passed HtmlPage is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        final HtmlPage page = new HtmlPage(null, Mockito.mock(WebWindow.class));

        try
        {
            HtmlPageUtils.findSingleHtmlElementByID(page, null);
            Assert.fail("HtmlPageUtils#findSingleHtmlElementByID(HtmlPage,String) should raise an IllegalArgumentException since passed ID is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#findSingleHtmlElementByID(HtmlPage, String)}.
     *
     * @throws Throwable
     */
    @Test(expected = AssertionError.class)
    public void testFindSingleHtmlElementByID() throws Throwable
    {
        final HtmlPage page = getHtmlPage("<html><body><div id='anyID'>TEST!</div></body></html>");

        // should pass
        Assert.assertNotNull(HtmlPageUtils.findSingleHtmlElementByID(page, "anyID"));

        // should fail
        HtmlPageUtils.findSingleHtmlElementByID(page, "ID");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#findSingleHtmlElementByID(HtmlPage, String)}.
     *
     * @throws Throwable
     */
    @Test(expected = AssertionError.class)
    public void testFindSingleHtmlElementByID_multipleElementsWithSameID() throws Throwable
    {
        final HtmlPage page = getHtmlPage("<html><body><div id='anyID'>TEST!</div><div id='anyID'>TEST!</div></body></html>");

        // should fail
        HtmlPageUtils.findSingleHtmlElementByID(page, "anyID");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#findSingleHtmlElementByXPath(HtmlElement, String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFindSingleHtmlElementByXPathElementIsNull() throws Throwable
    {
        HtmlPageUtils.findSingleHtmlElementByXPath((HtmlElement) null, "test");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#findSingleHtmlElementByXPath(HtmlElement, String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFindSingleHtmlElementByXPathIsNotRelative() throws Throwable
    {
        final HtmlElement el = Mockito.mock(HtmlElement.class);
        HtmlPageUtils.findSingleHtmlElementByXPath(el, "/test");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#findSingleHtmlElementByXPath(HtmlElement, String)}.
     */
    @Test
    public void testFindSingleHtmlElementByXPath()
    {
        final HtmlElement element = Mockito.mock(HtmlElement.class);
        final HtmlElement returnedElement = Mockito.mock(HtmlElement.class);
        final List<HtmlElement> list = new ArrayList<HtmlElement>();
        list.add(returnedElement);

        Mockito.doReturn(list).when(element).getByXPath("./a[@name='anyName']");

        final HtmlElement retVal = HtmlPageUtils.findSingleHtmlElementByXPath(element, "./a[@name='anyName']");
        Assert.assertEquals(list.get(0), retVal);
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getAnchorWithText(HtmlPage, String)} by passing an invalid
     * parameter.
     */
    @Test
    public void testGetAnchorWithText_InvalidParam()
    {
        try
        {
            HtmlPageUtils.getAnchorWithText((HtmlPage) null, "someText");
            Assert.fail("HtmlPageUtils#getAnchorWithText(HtmlPage,String) should raise an IllegalArgumentException since passed HtmlPage is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.getAnchorWithText(new HtmlPage(null, Mockito.mock(WebWindow.class)), null);
            Assert.fail("HtmlPageUtils#getAnchorWithText(HtmlPage,String) should raise an IllegalArgumentException since passed anchor text is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getAnchorWithText(HtmlPage, String)}.
     *
     * @throws Throwable
     */
    @Test(expected = AssertionError.class)
    public void testGetAnchorWithText() throws Throwable
    {
        final HtmlPage page = getHtmlPage("<html><body><div><a href=\"\">someText</a></div></body></html>");

        // should pass
        Assert.assertNotNull(HtmlPageUtils.getAnchorWithText(page, "someText"));

        // should fail
        HtmlPageUtils.getAnchorWithText(page, "someTest");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getFormsByIDRegExp(HtmlPage, java.util.regex.Pattern)} by
     * passing an invalid parameter to provoke a NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void testGetFormsByIDRegExp_NPE()
    {
        HtmlPageUtils.getFormsByIDRegExp((HtmlPage) null, RegExUtils.getPattern(".*"));
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getFormsByIDRegExp(HtmlPage, java.util.regex.Pattern)} by
     * passing an invalid parameter to provoke another NullPointerException.
     *
     * @throws Throwable
     */
    @Test(expected = NullPointerException.class)
    public void testGetFormsByIDRegExp_NPE2() throws Throwable
    {
        final HtmlPage page = getHtmlPage("<html><body><form action=\"\"></form></body></html>");

        HtmlPageUtils.getFormsByIDRegExp(page, null);
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getFormsByIDRegExp(HtmlPage, java.util.regex.Pattern)} .
     *
     * @throws Throwable
     */
    @Test
    public void testGetFormsByIDRegExp() throws Throwable
    {
        final HtmlPage page = getHtmlPage("<html><body><form id=\"testID\" action=\"\"></form></body></html>");

        List<HtmlForm> forms = HtmlPageUtils.getFormsByIDRegExp(page, RegExUtils.getPattern("text.*"));
        Assert.assertNotNull(forms);
        Assert.assertTrue(forms.isEmpty());

        forms = HtmlPageUtils.getFormsByIDRegExp(page, RegExUtils.getPattern("test.*"));
        Assert.assertNotNull(forms);
        Assert.assertEquals(1, forms.size());
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getFormsByNameRegExp(HtmlPage, java.util.regex.Pattern)} by
     * passing an invalid parameter to provoke a NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void testGetFormsByNameRegExp_NPE()
    {
        HtmlPageUtils.getFormsByNameRegExp((HtmlPage) null, RegExUtils.getPattern(".*"));
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getFormsByNameRegExp(HtmlPage, java.util.regex.Pattern)} by
     * passing an invalid parameter to provoke another NullPointerException.
     *
     * @throws Throwable
     */
    @Test(expected = NullPointerException.class)
    public void testGetFormsByNameRegExp_NPE2() throws Throwable
    {
        final HtmlPage page = getHtmlPage("<html><body><form action=\"\"></form></body></html>");

        HtmlPageUtils.getFormsByNameRegExp(page, null);
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getFormsByIDRegExp(HtmlPage, java.util.regex.Pattern)} .
     *
     * @throws Throwable
     */
    @Test
    public void testGetFormsByNameRegExp() throws Throwable
    {
        final HtmlPage page = getHtmlPage("<html><body><form name=\"testName\" action=\"\"></form></body></html>");

        List<HtmlForm> forms = HtmlPageUtils.getFormsByNameRegExp(page, RegExUtils.getPattern("text.*"));
        Assert.assertNotNull(forms);
        Assert.assertTrue(forms.isEmpty());

        forms = HtmlPageUtils.getFormsByNameRegExp(page, RegExUtils.getPattern("test.*"));
        Assert.assertNotNull(forms);
        Assert.assertEquals(1, forms.size());
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getFramePage(HtmlPage, String...)} by passing an invalid
     * parameter.
     */
    @Test
    public void testGetFramePage_HtmlPageString_InvalidParam()
    {
        try
        {
            HtmlPageUtils.getFramePage((HtmlPage) null, "someFrame");
            Assert.fail("HtmlPageUtils#getFramePage(HtmlForm,String...) should raise an IllegalArgumentException since passed HtmlPage is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.getFramePage(new HtmlPage(null, Mockito.mock(WebWindow.class)), (String[]) null);
            Assert.fail("HtmlPageUtils#getFramePage(HtmlForm,String...) should raise an IllegalArgumentException since passed frame name set is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }
    }

    @Test
    public void testGetFramePage_HtmlPageString() throws Throwable
    {
        final String frameDef = "<frameset rows=\"300,*\">" + "<frame name=\"test1\" src=\"http://localhost/test1Header.html\">" +
                                "<frameset cols=\"100%\">" + "<frame name=\"test1.2\">" + "</frame>" + "</frameset>" + "</frame>" +
                                "<frame name=\"test2\">" + "</frame>" + "</frameset>";

        final String frameHeader = "<frameset rows=\"300,*\">" + "<frame name=\"header1\">" + "</frame>" + "<frame name=\"header2\">" +
                                   "</frame>" + "</frameset>";

        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            final MockWebConnection connection = new MockWebConnection();
            connection.setDefaultResponse("<html>" + frameDef + "</html>");
            connection.setResponse(new URL("http://localhost/test1Header.html"), "<html>" + frameHeader + "</html>");
            webClient.setWebConnection(connection);

            final HtmlPage page = webClient.getPage("http://localhost/");

            Assert.assertNotNull(HtmlPageUtils.getFramePage(page, "test1"));
            Assert.assertNotNull(HtmlPageUtils.getFramePage(page, "test2"));
            Assert.assertNotNull(HtmlPageUtils.getFramePage(page, "test1.2"));
            Assert.assertNotNull(HtmlPageUtils.getFramePage(page, "test1", "header1"));
            Assert.assertNotNull(HtmlPageUtils.getFramePage(page, "test1", "header2"));
        }
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getInputStartingWith(HtmlForm, String)} by passing a null
     * reference as HtmlForm to provoke a NPE.
     */
    @Test(expected = NullPointerException.class)
    public void testGetInputStartingWith_NPE()
    {
        HtmlPageUtils.getInputStartingWith(null, "anyPrefix");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getInputStartingWith(HtmlForm, String)}.
     */
    @Test
    public void testGetInputStartingWithNoElementFound()
    {
        final HtmlForm form = PowerMockito.mock(HtmlForm.class);
        PowerMockito.when(form.getElementsByTagName("input")).thenReturn(new DomNodeListImpl<>());

        Assert.assertNull(HtmlPageUtils.getInputStartingWith(form, "anySuffix"));
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getInputStartingWith(HtmlForm, String)}.
     */
    @Test
    public void testGetInputStartingWith()
    {
        // the html form with the 'input' tags
        final HtmlForm form = PowerMockito.mock(HtmlForm.class);

        // the 'input' tags
        final HtmlInput firstInput = PowerMockito.mock(HtmlInput.class);
        final HtmlInput secondInput = PowerMockito.mock(HtmlInput.class);
        final HtmlInput thirdInput = PowerMockito.mock(HtmlInput.class);

        final DomNodeList<HtmlElement> inputs = new DomNodeListImpl<>();
        // add all 'input' tags to the list
        inputs.add(firstInput);
        inputs.add(secondInput);
        inputs.add(thirdInput);

        // set some mocks for the 'input' tags
        PowerMockito.when(form.getElementsByTagName("input")).thenReturn(inputs);
        PowerMockito.when(firstInput.getNameAttribute()).thenReturn("otherPrefixTest");
        PowerMockito.when(secondInput.getNameAttribute()).thenReturn(null);
        PowerMockito.when(thirdInput.getNameAttribute()).thenReturn("thisPrefixTest");
        // the third 'input' tag should be returned
        Assert.assertEquals(thirdInput, HtmlPageUtils.getInputStartingWith(form, "thisPrefix"));
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getInputEndingWith(HtmlForm, String)} by passing a null
     * reference as HtmlForm to provoke a NPE.
     */
    @Test(expected = NullPointerException.class)
    public void testGetInputEndingWith_NPE()
    {
        HtmlPageUtils.getInputEndingWith(null, "anySuffix");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getInputEndingWith(HtmlForm, String)}.
     */
    @Test
    public void testGetInputEndingWithNoElementFound()
    {
        final HtmlForm form = PowerMockito.mock(HtmlForm.class);
        PowerMockito.when(form.getElementsByTagName("input")).thenReturn(new DomNodeListImpl<>());

        Assert.assertNull(HtmlPageUtils.getInputEndingWith(form, "anySuffix"));
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getInputEndingWith(HtmlForm, String)}.
     */
    @Test
    public void testGetInputEndingWith()
    {
        // the html form with the 'input' tags
        final HtmlForm form = PowerMockito.mock(HtmlForm.class);

        // the 'input' tags
        final HtmlInput firstInput = PowerMockito.mock(HtmlInput.class);
        final HtmlInput secondInput = PowerMockito.mock(HtmlInput.class);
        final HtmlInput thirdInput = PowerMockito.mock(HtmlInput.class);

        final DomNodeList<HtmlElement> inputs = new DomNodeListImpl<>();
        // add all 'input' tags to the list
        inputs.add(firstInput);
        inputs.add(secondInput);
        inputs.add(thirdInput);

        // set some mocks for the 'input' tags
        PowerMockito.when(form.getElementsByTagName("input")).thenReturn(inputs);
        PowerMockito.when(firstInput.getNameAttribute()).thenReturn("testOtherSuffix");
        PowerMockito.when(secondInput.getNameAttribute()).thenReturn(null);
        PowerMockito.when(thirdInput.getNameAttribute()).thenReturn("testThisSuffix");
        // the third 'input' tag should be returned
        Assert.assertEquals(thirdInput, HtmlPageUtils.getInputEndingWith(form, "ThisSuffix"));
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getSelectStartingWith(HtmlForm, String)} by passing a null
     * reference as HtmlForm to provoke a NPE.
     */
    @Test(expected = NullPointerException.class)
    public void testGetSelectStartingWith_NPE()
    {
        HtmlPageUtils.getSelectStartingWith(null, "anyPrefix");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getSelectStartingWith(HtmlForm, String)}.
     */
    @Test
    public void testGetSelectStartingWithNoElementFound()
    {
        final HtmlForm form = PowerMockito.mock(HtmlForm.class);
        PowerMockito.when(form.getElementsByTagName("select")).thenReturn(new DomNodeListImpl<>());

        Assert.assertNull(HtmlPageUtils.getSelectStartingWith(form, "anyPrefix"));
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getSelectEndingWith(HtmlForm, String)}.
     */
    @Test
    public void testGetSelectStartingWith()
    {
        // the html form with the 'select' tags
        final HtmlForm form = PowerMockito.mock(HtmlForm.class);

        // the 'select' tags
        final HtmlSelect firstSelect = PowerMockito.mock(HtmlSelect.class);
        final HtmlSelect secondSelect = PowerMockito.mock(HtmlSelect.class);
        final HtmlSelect thirdSelect = PowerMockito.mock(HtmlSelect.class);
        final HtmlSelect fourthSelect = PowerMockito.mock(HtmlSelect.class);

        final DomNodeList<HtmlElement> selects = new DomNodeListImpl<>();
        // add all 'select' tags to the list
        selects.add(firstSelect);
        selects.add(secondSelect);
        selects.add(thirdSelect);
        selects.add(fourthSelect);

        // set some mocks for the 'select' tags
        PowerMockito.when(form.getElementsByTagName("select")).thenReturn(selects);
        PowerMockito.when(firstSelect.getNameAttribute()).thenReturn("prefexio");
        PowerMockito.when(secondSelect.getNameAttribute()).thenReturn(null);
        PowerMockito.when(thirdSelect.getNameAttribute()).thenReturn("prefixio");
        PowerMockito.when(fourthSelect.getNameAttribute()).thenReturn("prefixIO");
        // the third 'select' tag should be returned
        Assert.assertEquals(thirdSelect, HtmlPageUtils.getSelectStartingWith(form, "prefixio"));
        Assert.assertEquals(thirdSelect, HtmlPageUtils.getSelectStartingWith(form, "prefix"));
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getSelectEndingWith(HtmlForm, String)} by passing a null
     * reference as HtmlForm to provoke a NPE.
     */
    @Test(expected = NullPointerException.class)
    public void testGetSelectEndingWith_NPE()
    {
        HtmlPageUtils.getSelectEndingWith(null, "anySuffix");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getSelectEndingWith(HtmlForm, String)}.
     */
    @Test
    public void testGetSelectEndingWithNoElementFound()
    {
        final HtmlForm form = PowerMockito.mock(HtmlForm.class);
        PowerMockito.when(form.getElementsByTagName("select")).thenReturn(new DomNodeListImpl<>());

        Assert.assertNull(HtmlPageUtils.getSelectEndingWith(form, "anySuffix"));
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#getSelectEndingWith(HtmlForm, String)}.
     */
    @Test
    public void testGetSelectEndingWith()
    {
        // the html form with the 'select' tags
        final HtmlForm form = PowerMockito.mock(HtmlForm.class);

        // the 'select' tags
        final HtmlSelect firstSelect = PowerMockito.mock(HtmlSelect.class);
        final HtmlSelect secondSelect = PowerMockito.mock(HtmlSelect.class);
        final HtmlSelect thirdSelect = PowerMockito.mock(HtmlSelect.class);

        final DomNodeList<HtmlElement> selects = new DomNodeListImpl<>();
        // add all 'select' tags to the list
        selects.add(firstSelect);
        selects.add(secondSelect);
        selects.add(thirdSelect);

        // set some mocks for the 'select' tags
        PowerMockito.when(form.getElementsByTagName("select")).thenReturn(selects);
        PowerMockito.when(firstSelect.getNameAttribute()).thenReturn("testOtherSuffix");
        PowerMockito.when(secondSelect.getNameAttribute()).thenReturn(null);
        PowerMockito.when(thirdSelect.getNameAttribute()).thenReturn("testThisSuffix");
        // the third 'select' tag should be returned
        Assert.assertEquals(thirdSelect, HtmlPageUtils.getSelectEndingWith(form, "ThisSuffix"));
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#select(HtmlForm, String, String)} by passing an invalid
     * parameter.
     */
    @Test
    public void testSelect_InvalidParam()
    {
        try
        {
            HtmlPageUtils.select(null, "anyName", "anyValue");
            Assert.fail("HtmlPageUtils#select(HtmlForm,String,String) should raise an IllegalArgumentException since passed HtmlForm is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        final HtmlForm form = Mockito.mock(HtmlForm.class);

        try
        {
            HtmlPageUtils.select(form, null, "anyValue");
            Assert.fail("HtmlPageUtils#select(HtmlForm,String,String) should raise an IllegalArgumentException since passed name of select is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.select(form, "anyName", null);
            Assert.fail("HtmlPageUtils#select(HtmlForm,String,String) should raise an IllegalArgumentException since passed option value is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#select(HtmlForm, String, String)} by passing a HtmlForm which
     * doesn't contain any select with the specified name.
     */
    @Test(expected = AssertionError.class)
    public void testSelect_NoSelect4Name()
    {
        final HtmlForm form = Mockito.mock(HtmlForm.class);
        Mockito.doReturn(Collections.EMPTY_LIST).when(form).getSelectsByName("anyName");

        HtmlPageUtils.select(form, "anyName", "anyValue");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#select(HtmlForm, String, String)}.
     */
    @Test(expected = TestException.class)
    public void testSelect()
    {
        final HtmlForm form = Mockito.mock(HtmlForm.class);
        final HtmlSelect select = Mockito.mock(HtmlSelect.class);

        Mockito.doReturn(Arrays.asList(new HtmlSelect[]
            {
                select
            })).when(form).getSelectsByName("anyName");
        Mockito.doThrow(new TestException()).when(select).setSelectedAttribute("anyValue", true);

        HtmlPageUtils.select(form, "anyName", "anyValue");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#selectRandomly(HtmlForm, String, boolean, boolean)} by passing
     * an invalid parameter.
     */
    @Test
    public void testSelectRandomly_HtmlFormStringBooleanBoolean_InvalidParam()
    {
        try
        {
            HtmlPageUtils.selectRandomly(null, "anyName", false, false);
            Assert.fail("HtmlPageUtils.selectRandomly(HtmlForm,String,boolean,boolean) should raise an IllegalArgumentException since passed HtmlForm is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.selectRandomly(Mockito.mock(HtmlForm.class), null, false, false);
            Assert.fail("HtmlPageUtils.selectRandomly(HtmlForm,String,boolean,boolean) should raise an IllegalArgumentException since passed name of select is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#selectRandomly(HtmlForm, String, boolean, boolean)} by passing a
     * HtmlForm which doesn't contain a HtmlSelect identified by the specified name.
     */
    @Test(expected = AssertionError.class)
    public void testSelectRandomly_HtmlFormStringBooleanBoolean_NoSelect4Name()
    {
        final HtmlForm form = Mockito.mock(HtmlForm.class);
        Mockito.doReturn(Collections.EMPTY_LIST).when(form).getSelectsByName("anyName");

        HtmlPageUtils.selectRandomly(form, "anyName", false, false);
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#selectRandomly(HtmlForm, String, boolean, boolean)}.
     *
     * @throws Exception
     */
    @Test
    public void testSelectRandomly_HtmlFormStringBooleanBoolean() throws Exception
    {
        final String SELECTNAME = "mySelect";

        // one option, do not exclude it
        {
            final HtmlPage page = HtmlTestViaFile.getHtmlPageByName(this);

            final HtmlForm form = page.getFormByName("testSelectRandomly_HtmlFormStringBooleanBoolean_size_1");
            Assert.assertNotNull(form);

            // first one is always selected
            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());

            HtmlPageUtils.selectRandomly(form, SELECTNAME, false, false);

            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());
        }

        // two options, exclude first
        {
            final HtmlPage page = HtmlTestViaFile.getHtmlPageByName(this);

            final HtmlForm form = page.getFormByName("testSelectRandomly_HtmlFormStringBooleanBoolean_size_2");
            Assert.assertNotNull(form);

            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value02").isSelected());

            HtmlPageUtils.selectRandomly(form, SELECTNAME, true, false);

            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());
            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value02").isSelected());
        }

        // two options, exclude last
        {
            final HtmlPage page = HtmlTestViaFile.getHtmlPageByName(this);

            final HtmlForm form = page.getFormByName("testSelectRandomly_HtmlFormStringBooleanBoolean_size_2");
            Assert.assertNotNull(form);

            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value02").isSelected());

            HtmlPageUtils.selectRandomly(form, SELECTNAME, false, true);

            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value02").isSelected());
        }

        // two options, exclude first and last, first one stays selected
        {
            final HtmlPage page = HtmlTestViaFile.getHtmlPageByName(this);

            final HtmlForm form = page.getFormByName("testSelectRandomly_HtmlFormStringBooleanBoolean_size_2");
            Assert.assertNotNull(form);

            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value02").isSelected());

            try
            {
                HtmlPageUtils.selectRandomly(form, SELECTNAME, true, true);
                Assert.fail("No exception issued.");
            }
            catch (final AssertionError e)
            {
                // we are good
            }

            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value02").isSelected());
        }

        // three options, exclude first and last
        {
            final HtmlPage page = HtmlTestViaFile.getHtmlPageByName(this);

            final HtmlForm form = page.getFormByName("testSelectRandomly_HtmlFormStringBooleanBoolean_size_3");
            Assert.assertNotNull(form);

            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value02").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value03").isSelected());

            HtmlPageUtils.selectRandomly(form, SELECTNAME, true, true);

            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());
            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value02").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value03").isSelected());
        }

        // four options, 2, 4 are disabled, 1 will be excluded
        {
            final HtmlPage page = HtmlTestViaFile.getHtmlPageByName(this);

            final HtmlForm form = page.getFormByName("testSelectRandomly_HtmlFormStringBooleanBoolean_size_4");
            Assert.assertNotNull(form);

            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value02").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value03").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value04").isSelected());

            HtmlPageUtils.selectRandomly(form, SELECTNAME, true, false);

            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value02").isSelected());
            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value03").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value04").isSelected());
        }

        // finally, a list of 6 and we pick us number 5 by mocking random
        {
            final HtmlPage page = HtmlTestViaFile.getHtmlPageByName(this);

            mockStatic(XltRandom.class);
            expect(XltRandom.nextInt(6)).andReturn(4); // 6 is size, 4 is position, starting at 0
            replayAll();

            final HtmlForm form = page.getFormByName("testSelectRandomly_HtmlFormStringBooleanBoolean_size_6");
            Assert.assertNotNull(form);

            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value02").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value03").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value04").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value05").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value06").isSelected());

            HtmlPageUtils.selectRandomly(form, SELECTNAME, false, false);

            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value02").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value03").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value04").isSelected());
            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value05").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value06").isSelected());
        }
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#selectRandomly(HtmlForm, String)}.
     *
     * @throws Exception
     */
    @Test
    public void testSelectRandomly_HtmlFormString() throws Exception
    {
        final String SELECTNAME = "mySelect";

        final HtmlPage page = HtmlTestViaFile.getHtmlPageByName(this);

        final HtmlForm form = page.getFormByName("testSelectRandomly_HtmlFormStringBooleanBoolean_size_1");
        Assert.assertNotNull(form);

        // first one is always selected
        Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());

        HtmlPageUtils.selectRandomly(form, SELECTNAME);

        Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#selectRandomly(HtmlForm, String, boolean)}.
     *
     * @throws Exception
     */
    @Test
    public void testSelectRandomly_HtmlFormStringBoolean() throws Exception
    {
        final String SELECTNAME = "mySelect";

        // one option, do not exclude it
        {
            final HtmlPage page = HtmlTestViaFile.getHtmlPageByName(this);

            final HtmlForm form = page.getFormByName("testSelectRandomly_HtmlFormStringBooleanBoolean_size_1");
            Assert.assertNotNull(form);

            // first one is always selected
            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());

            HtmlPageUtils.selectRandomly(form, SELECTNAME, false);

            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());
        }

        // two options, exclude first
        {
            final HtmlPage page = HtmlTestViaFile.getHtmlPageByName(this);

            final HtmlForm form = page.getFormByName("testSelectRandomly_HtmlFormStringBooleanBoolean_size_2");
            Assert.assertNotNull(form);

            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());
            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value02").isSelected());

            HtmlPageUtils.selectRandomly(form, SELECTNAME, true);

            Assert.assertFalse(form.getSelectByName(SELECTNAME).getOptionByValue("value01").isSelected());
            Assert.assertTrue(form.getSelectByName(SELECTNAME).getOptionByValue("value02").isSelected());
        }
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#setCheckBoxValue(HtmlForm, String, boolean)} by passing an
     * invalid parameter.
     */
    @Test
    public void testSetCheckboxValue_InvalidParam()
    {
        try
        {
            HtmlPageUtils.setCheckBoxValue(null, "anyName", false);
            Assert.fail("HtmlPageUtils#setCheckBoxValue(HtmlForm,String,boolean) should raise an IllegalArgumentException since passed HtmlForm is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.setCheckBoxValue(Mockito.mock(HtmlForm.class), null, false);
            Assert.fail("HtmlPageUtils#setCheckBoxValue(HtmlForm,String,boolean) should raise an IllegalArgumentException since passed checkbox name is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#setCheckBoxValue(HtmlForm, String, boolean)}.
     *
     * @throws Throwable
     */
    @Test
    public void testSetCheckboxValue() throws Throwable
    {
        final HtmlPage page = getHtmlPage("<html><body><form name=\"testForm\" action=\"\"><input name=\"anyName\" type=\"checkbox\" /></form></body></html>");

        final HtmlForm form = page.getFormByName("testForm");

        HtmlPageUtils.setCheckBoxValue(form, "anyName", true);

        final HtmlInput input = form.getInputByName("anyName");
        Assert.assertEquals(true, input.isChecked());
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#setInputValue(HtmlForm, String, String)} by passing an invalid
     * parameter.
     */
    @Test
    public void testSetInputValue_InvalidParam()
    {
        try
        {
            HtmlPageUtils.setInputValue(null, "anyName", "anyValue");
            Assert.fail("HtmlPageUtils#setInputValue(HtmlForm,String,String) should raise an IllegalArgumentException since passed HtmlForm is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.setInputValue(Mockito.mock(HtmlForm.class), null, "anyValue");
            Assert.fail("HtmlPageUtils#setInputValue(HtmlForm,String,String) should raise an IllegalArgumentException since passed name of input is null.");
        }
        catch (final IllegalArgumentException e)
        {
        }
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#setInputValue(HtmlForm, String, String)}.
     */
    @Test(expected = TestException.class)
    public void testSetInputValue()
    {
        final HtmlForm form = Mockito.mock(HtmlForm.class);
        final HtmlInput input = Mockito.mock(HtmlInput.class);

        Mockito.doReturn(Arrays.asList(new HtmlInput[]
            {
                input
            })).when(form).getInputsByName("anyName");
        Mockito.doThrow(new TestException()).when(input).setValue("anyValue");

        HtmlPageUtils.setInputValue(form, "anyName", "anyValue");
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#isElementPresent(HtmlPage, String)}.
     *
     * @throws Throwable
     *             thrown when URL could not be constructed
     */
    @Test
    public void testIsElementPresentHtmlPage() throws Throwable
    {
        final HtmlPage page = getHtmlPage("<html><body><form name=\"testForm\" action=\"\"><input name=\"anyName\" type=\"checkbox\" /></form></body></html>");

        try
        {
            HtmlPageUtils.isElementPresent(page, null);
            Assert.fail("HtmlPageUtils#isElementPresent(HtmlPage,String) should raise an IllegalArgumentException since passed XPath expression is null");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.isElementPresent(page, "");
            Assert.fail("HtmlPageUtils#isElementPresent(HtmlPage,String) should raise an IllegalArgumentException since passed XPath expression is empty");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.isElementPresent((HtmlPage) null, "//form[@name='testForm']");
            Assert.fail("HtmlPageUtils#isElementPresent(HtmlPage,String) should raise an IllegalArgumentException since passed HTML page is null");
        }
        catch (final IllegalArgumentException e)
        {
        }

        Assert.assertTrue("Could not find HTML form with name 'testForm'",
                          HtmlPageUtils.isElementPresent(page, "//form[@name='testForm']"));
        Assert.assertFalse("Found non-existent form with name 'MyForm'", HtmlPageUtils.isElementPresent(page, "//form[@name='MyForm']"));
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#isElementPresent(HtmlElement, String)}.
     *
     * @throws Throwable
     *             thrown when URL could not be constructed
     */
    @Test
    public void testIsElementPresentHtmlElement() throws Throwable
    {
        final HtmlElement element = Mockito.mock(HtmlElement.class);

        try
        {
            HtmlPageUtils.isElementPresent(element, null);
            Assert.fail("HtmlPageUtils#isElementPresent(HtmlPage,String) should raise an IllegalArgumentException since passed XPath expression is null");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.isElementPresent(element, "");
            Assert.fail("HtmlPageUtils#isElementPresent(HtmlPage,String) should raise an IllegalArgumentException since passed XPath expression is empty");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.isElementPresent((HtmlElement) null, "//form[@name='testForm']");
            Assert.fail("HtmlPageUtils#isElementPresent(HtmlElement,String) should raise an IllegalArgumentException since passed HTML element is null");
        }
        catch (final IllegalArgumentException e)
        {
        }

        Assert.assertFalse("Found non-existent form.", HtmlPageUtils.isElementPresent(element, ".//form[@name='MyForm']"));
    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#countElementsByXPath(HtmlPage, String)}.
     *
     * @throws Throwable
     *             thrown when URL could not be constructed
     */
    @Test
    public void testCountElementsByXPath() throws Throwable
    {
        final HtmlPage page = getHtmlPage("<html><body><ul class=\"myList\"><li>1st item</li><li>2nd item</li></ul><div id=\"emptyDiv\"></div></body></html> ");

        try
        {
            HtmlPageUtils.countElementsByXPath(page, null);
            Assert.fail("HtmlPageUtils#countElementsByXPath(HtmlPage,String) should raise an IllegalArgumentException since passed XPath expression is null");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.countElementsByXPath(page, "");
            Assert.fail("HtmlPageUtils#countElementsByXPath(HtmlPage,String) should raise an IllegalArgumentException since passed XPath expression is empty");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            HtmlPageUtils.countElementsByXPath(null, "id('emptyDiv')");
            Assert.fail("HtmlPageUtils#countElementsByXPath(HtmlPage,String) should raise an IllegalArgumentException since passed HTML page is null");
        }
        catch (final IllegalArgumentException e)
        {
        }

        Assert.assertEquals("No form present, but found one?!", 0, HtmlPageUtils.countElementsByXPath(page, "//form"));
        Assert.assertEquals("Invalid number of list items: ", 2, HtmlPageUtils.countElementsByXPath(page, "//ul[@class='myList']/li"));
        Assert.assertEquals("Invalid number of HTML divisions with ID 'emptyDiv': ", 1,
                            HtmlPageUtils.countElementsByXPath(page, "id('emptyDiv')"));

    }

    /**
     * Tests the implementation of {@link HtmlPageUtils#waitForHtmlElements(HtmlPage, String, long)}.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testWaitForHtmlElements() throws IOException, InterruptedException
    {
        final HtmlPage page = getHtmlPage("<html><body><div class='first'>test</div><div class='second'>test</div><div class='third'>test</div></body></html>");
        // no element should found for this xPath
        final String xPath = "//divs";
        final long startTime = TimerUtils.get().getStartTime();
        try
        {
            HtmlPageUtils.waitForHtmlElements(page, xPath, 2000);
            Assert.fail("An exception should be thrown!");
        }
        catch (final AssertionError err)
        {
            final long runTime = TimerUtils.get().getElapsedTime(startTime);
            // the runtime should be more than 2000 ms
            Assert.assertTrue("Unexpected runtime value: " + runTime, 2000 <= runTime && runTime < 2800);
        }
    }

    /**
     * Creates an {@link HtmlPage} object from the passed HTML source code.
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws FailingHttpStatusCodeException
     */
    public HtmlPage getHtmlPage(final String htmlSource) throws FailingHttpStatusCodeException, MalformedURLException, IOException
    {
        @SuppressWarnings("resource")
        final WebClient webClient = new WebClient(BrowserVersion.CHROME);

        final MockWebConnection connection = new MockWebConnection();
        connection.setDefaultResponse(htmlSource);
        webClient.setWebConnection(connection);

        return webClient.getPage("http://localhost/");
    }

    /**
     * Unchecked exception for testing purposes.
     */
    private static class TestException extends RuntimeException
    {
        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 1L;
    }
}
