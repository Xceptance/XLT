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
package com.xceptance.xlt.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.xml.sax.helpers.AttributesImpl;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.DefaultElementFactory;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * The {@link HtmlPageUtils} class provides some useful helper methods to make dealing with {@link HtmlPage} objects
 * easier. When using the plain HtmlUnit API, similar pieces of code have to be written again and again. Using this
 * class, test case actions are often shorter and easier to understand.
 * 
 * @see AbstractHtmlPageAction
 * @see LightweightHtmlPageUtils
 */
public class HtmlPageUtils extends BasicPageUtils
{
    /**
     * Checks the HTML radio button input element with the given name and index in the specified form. All other radio
     * buttons with the same name are left unchecked.
     * 
     * @param form
     *            the form with the radio buttons
     * @param radioButtonName
     *            the name of the radio button group
     * @param index
     *            the index of the radio button to check (starts with 0)
     */
    public static void checkRadioButton(final HtmlForm form, final String radioButtonName, final int index)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(form, "form");
        ParameterCheckUtils.isNotNullOrEmpty(radioButtonName, "radioButtonName");
        ParameterCheckUtils.isNotNegative(index, "index");

        // get the radio buttons
        final List<HtmlRadioButtonInput> buttons = form.getRadioButtonsByName(radioButtonName);
        Assert.assertTrue("No radio buttons found for name: " + radioButtonName, !buttons.isEmpty());
        Assert.assertTrue("Radio button index too large: " + index, index < buttons.size());

        // check the requested button
        buttons.get(index).setChecked(true);

        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            XltLogger.runTimeLogger.info(String.format("Checking radio button: %s.%s", getIdOrName(form), radioButtonName));
        }
    }

    /**
     * Checks the HTML radio button input element with the given name and value in the specified form. All other radio
     * buttons with the same name are left unchecked.
     * 
     * @param form
     *            the form with the radio buttons
     * @param radioButtonName
     *            the name of the radio button group
     * @param value
     *            the value of the radio button to check
     */
    public static void checkRadioButton(final HtmlForm form, final String radioButtonName, final String value)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(form, "form");
        ParameterCheckUtils.isNotNullOrEmpty(radioButtonName, "radioButtonName");
        ParameterCheckUtils.isNotNullOrEmpty(value, "value");

        // get the requested radio button
        HtmlRadioButtonInput theRadioButton = null;

        final List<HtmlRadioButtonInput> radioButtons = form.getRadioButtonsByName(radioButtonName);
        for (final HtmlRadioButtonInput radioButton : radioButtons)
        {
            if (value.equals(radioButton.getValueAttribute()))
            {
                theRadioButton = radioButton;
                break;
            }
        }

        Assert.assertNotNull(String.format("No radio button input element found with name='%s' and value='%s'", radioButtonName, value),
                             theRadioButton);

        // check the requested radio button
        theRadioButton.setChecked(true);

        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            XltLogger.runTimeLogger.info(String.format("Checking radio button: %s.%s", getIdOrName(form), radioButtonName));
        }
    }

    /**
     * Checks one of the radio buttons with the given name in the specified form. All other radio buttons with the same
     * name are left unchecked.
     * 
     * @param form
     *            the form with the radio buttons
     * @param radioButtonName
     *            the name of the radio button group
     */
    public static void checkRadioButtonRandomly(final HtmlForm form, final String radioButtonName)
    {
        checkRadioButtonRandomly(form, radioButtonName, false, false);
    }

    /**
     * Checks one of the radio buttons with the given name in the specified form. All other radio buttons with the same
     * name are unchecked.
     * 
     * @param form
     *            the form with the radio buttons
     * @param radioButtonName
     *            the name of the radio button group
     * @param excludeFirst
     *            whether to exclude the first radio button in the group
     * @param excludeLast
     *            whether to exclude the last radio button in the group
     */
    public static void checkRadioButtonRandomly(final HtmlForm form, final String radioButtonName, final boolean excludeFirst,
                                                final boolean excludeLast)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(form, "form");
        ParameterCheckUtils.isNotNullOrEmpty(radioButtonName, "radioButtonName");

        // get the radio buttons
        final List<HtmlRadioButtonInput> buttons = form.getRadioButtonsByName(radioButtonName);
        Assert.assertTrue("No radio buttons found for name: " + radioButtonName, !buttons.isEmpty());

        // check one of them
        final HtmlRadioButtonInput button = pickOneRandomly(buttons, excludeFirst, excludeLast);
        button.setChecked(true);

        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            XltLogger.runTimeLogger.info(String.format("Checking radio button: %s.%s", getIdOrName(form), getIdOrName(button)));
        }
    }

    /**
     * Creates a new HTML element with the specified tag name and adds it as a child to the given parent element.
     * <p>
     * Note that input elements should be created using {@link #createInput(HtmlForm, String, String, String)}.
     * 
     * @param tagName
     *            the tag name
     * @param parent
     *            the parent
     * @return the HTML element just created
     */
    public static <T extends HtmlElement> T createHtmlElement(final String tagName, final HtmlElement parent)
    {
        // parameter check
        ParameterCheckUtils.isNotNullOrEmpty(tagName, "tagName");
        ParameterCheckUtils.isNotNull(parent, "parent");

        // T extends HtmlElement so it should be safe to cast
        @SuppressWarnings("unchecked")
        final T element = (T) ((HtmlPage) parent.getPage()).createElement(tagName);
        parent.appendChild(element);

        return element;
    }

    /**
     * Creates a new input HTML element, initializes it with the specified type, name, and value, and inserts it to the
     * given form.
     * 
     * @param form
     *            the form
     * @param type
     *            the type
     * @param name
     *            the name
     * @param value
     *            the value
     * @return the input element just created
     */
    public static HtmlInput createInput(final HtmlForm form, final String type, final String name, final String value)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(form, "form");
        ParameterCheckUtils.isNotNullOrEmpty(type, "type");
        ParameterCheckUtils.isNotNullOrEmpty(name, "name");

        final AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(null, "name", "name", "", name);
        attributes.addAttribute(null, "type", "type", "", type);
        attributes.addAttribute(null, "value", "value", "", value);

        final HtmlElement input = new DefaultElementFactory().createElement(form.getPage(), HtmlInput.TAG_NAME, attributes);
        form.appendChild(input);

        return (HtmlInput) input;
    }

    /**
     * Finds HTML elements using the given XPath expression on the specified page.
     * 
     * @param <T>
     *            the expected type of the elements found
     * @param page
     *            the page to search
     * @param xpath
     *            the XPath expression specifying the elements
     * @return the list of elements found
     * @throws AssertionError
     *             if no elements were found
     */
    @SuppressWarnings("unchecked")
    public static <T extends HtmlElement> List<T> findHtmlElements(final HtmlPage page, final String xpath)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(page, "page");
        ParameterCheckUtils.isNotNullOrEmpty(xpath, "xpath");

        // get the elements
        final List<?> elements = page.getByXPath(xpath);
        Assert.assertTrue("No elements found for XPath: " + xpath, !elements.isEmpty());

        return (List<T>) elements;
    }

    /**
     * Finds HTML elements using the given XPath expression on the specified page, selects one of them randomly and
     * returns it.
     * 
     * @param <T>
     *            the expected type of the elements found
     * @param page
     *            the page to search
     * @param xpath
     *            the XPath expression specifying the elements
     * @return the list of elements found
     * @throws AssertionError
     *             if no elements were found
     */
    @SuppressWarnings("unchecked")
    public static <T> T findHtmlElementsAndPickOne(final HtmlPage page, final String xpath)
    {
        return (T) findHtmlElementsAndPickOne(page, xpath, false, false);
    }

    /**
     * Finds HTML elements using the given XPath expression on the specified page, selects one of them randomly and
     * returns it.
     * 
     * @param <T>
     *            the expected type of the elements found
     * @param page
     *            the page to search
     * @param xpath
     *            the XPath expression specifying the elements
     * @param excludeFirst
     *            whether to exclude the first element found
     * @return the list of elements found
     * @throws AssertionError
     *             if no elements were found
     */
    @SuppressWarnings("unchecked")
    public static <T> T findHtmlElementsAndPickOne(final HtmlPage page, final String xpath, final boolean excludeFirst)
    {
        return (T) findHtmlElementsAndPickOne(page, xpath, excludeFirst, false);
    }

    /**
     * Finds HTML elements using the given XPath expression on the specified page, selects one of them randomly and
     * returns it.
     * 
     * @param <T>
     *            the expected type of the elements found
     * @param page
     *            the page to search
     * @param xpath
     *            the XPath expression specifying the elements
     * @param excludeFirst
     *            whether to exclude the first element found
     * @param excludeLast
     *            whether to exclude the last element found
     * @return the list of elements found
     * @throws AssertionError
     *             if no elements were found
     */
    public static <T extends HtmlElement> T findHtmlElementsAndPickOne(final HtmlPage page, final String xpath, final boolean excludeFirst,
                                                                       final boolean excludeLast)
    {
        final List<T> elements = findHtmlElements(page, xpath);

        return pickOneRandomly(elements, excludeFirst, excludeLast);
    }

    /**
     * Finds the HTML element with the given ID on the specified page.
     * 
     * @param <T>
     *            the expected type of the element found
     * @param page
     *            the page to search
     * @param id
     *            the ID of the element
     * @return the element found
     * @throws AssertionError
     *             if no or more than one element with this ID was found
     */
    @SuppressWarnings("unchecked")
    public static <T extends HtmlElement> T findSingleHtmlElementByID(final HtmlPage page, final String id)
    {
        // parameter check
        ParameterCheckUtils.isNotNullOrEmpty(id, "id");

        return (T) findSingleHtmlElementByXPath(page, "//*[@id='" + id + "']");
    }

    /**
     * Finds a single HTML elements using the given XPath expression on the specified page.
     * 
     * @param <T>
     *            the expected type of the elements found
     * @param page
     *            the page to search
     * @param xpath
     *            the XPath expression specifying the elements
     * @return the element found
     * @throws AssertionError
     *             if no or more than one element was found
     */
    @SuppressWarnings("unchecked")
    public static <T extends HtmlElement> T findSingleHtmlElementByXPath(final HtmlPage page, final String xpath)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(page, "page");
        ParameterCheckUtils.isNotNullOrEmpty(xpath, "xpath");

        // get the element
        final List<?> result = page.getByXPath(xpath);

        if (XltLogger.runTimeLogger.isDebugEnabled())
        {
            XltLogger.runTimeLogger.debug("Found " + result.size() + " element(s) for XPath: " + xpath);
        }

        Assert.assertEquals("No or too many elements found for XPath: " + xpath + " -", 1, result.size());

        return (T) result.get(0);
    }

    /**
     * Finds HTML elements using the given XPath expression within the specified HTML element.
     * 
     * @param <T>
     *            the expected type of the elements found
     * @param element
     *            the HTML element to search
     * @param xpath
     *            the XPath expression specifying the elements (must not contain leading slash)
     * @return the list of elements found
     * @throws AssertionError
     *             if no elements were found
     */
    @SuppressWarnings("unchecked")
    public static <T extends HtmlElement> List<T> findHtmlElements(final HtmlElement element, final String xpath)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(element, "element");
        ParameterCheckUtils.isRelativePath(xpath, "xpath");

        // get the elements
        final List<?> elements = element.getByXPath(xpath);
        Assert.assertTrue("No elements found for XPath: " + xpath, !elements.isEmpty());

        return (List<T>) elements;
    }

    /**
     * Finds HTML elements using the given XPath expression within the specified HTML element, selects one of them
     * randomly and returns it.
     * 
     * @param <T>
     *            the expected type of the elements found
     * @param element
     *            the HTML element to search
     * @param xpath
     *            the XPath expression specifying the elements
     * @return the list of elements found
     * @throws AssertionError
     *             if no elements were found
     */
    @SuppressWarnings("unchecked")
    public static <T extends HtmlElement> T findHtmlElementsAndPickOne(final HtmlElement element, final String xpath)
    {
        return (T) findHtmlElementsAndPickOne(element, xpath, false, false);
    }

    /**
     * Finds HTML elements using the given XPath expression within the specified HTML element, selects one of them
     * randomly and returns it.
     * 
     * @param <T>
     *            the expected type of the elements found
     * @param element
     *            the HTML element to search
     * @param xpath
     *            the XPath expression specifying the elements
     * @param excludeFirst
     *            whether to exclude the first element found
     * @return the list of elements found
     * @throws AssertionError
     *             if no elements were found
     */
    @SuppressWarnings("unchecked")
    public static <T extends HtmlElement> T findHtmlElementsAndPickOne(final HtmlElement element, final String xpath,
                                                                       final boolean excludeFirst)
    {
        return (T) findHtmlElementsAndPickOne(element, xpath, excludeFirst, false);
    }

    /**
     * Finds HTML elements using the given XPath expression within the specified HTML element, selects one of them
     * randomly and returns it.
     * 
     * @param <T>
     *            the expected type of the elements found
     * @param element
     *            the HTML element to search
     * @param xpath
     *            the XPath expression specifying the elements
     * @param excludeFirst
     *            whether to exclude the first element found
     * @param excludeLast
     *            whether to exclude the last element found
     * @return the list of elements found
     * @throws AssertionError
     *             if no elements were found
     */
    public static <T extends HtmlElement> T findHtmlElementsAndPickOne(final HtmlElement element, final String xpath,
                                                                       final boolean excludeFirst, final boolean excludeLast)
    {
        final List<T> elements = findHtmlElements(element, xpath);

        return pickOneRandomly(elements, excludeFirst, excludeLast);
    }

    /**
     * Finds a single HTML elements using the given XPath expression within the specified HTML element.
     * 
     * @param <T>
     *            the expected type of the elements found
     * @param element
     *            the HTML element to search
     * @param xpath
     *            the XPath expression specifying the elements
     * @return the element found
     * @throws AssertionError
     *             if no or more than one element was found
     */
    @SuppressWarnings("unchecked")
    public static <T extends HtmlElement> T findSingleHtmlElementByXPath(final HtmlElement element, final String xpath)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(element, "element");
        ParameterCheckUtils.isRelativePath(xpath, "xpath");

        // get the element
        final List<?> result = element.getByXPath(xpath);

        if (XltLogger.runTimeLogger.isDebugEnabled())
        {
            XltLogger.runTimeLogger.debug("Found " + result.size() + " element(s) for XPath: " + xpath);
        }

        Assert.assertEquals("No or too many elements found for XPath: " + xpath + " -", 1, result.size());

        return (T) result.get(0);
    }

    /**
     * Returns the HTML anchor element with the passed anchor text.
     * 
     * @param page
     *            the page to search
     * @param anchorText
     *            the anchor text
     * @return the anchor found
     * @throws AssertionError
     *             if no or more than one anchor was found
     */
    public static HtmlAnchor getAnchorWithText(final HtmlPage page, final String anchorText)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(page, "page");
        ParameterCheckUtils.isNotNullOrEmpty(anchorText, "anchorText");

        // get the anchor with the specified text
        return findSingleHtmlElementByXPath(page, "//a[contains(., '" + anchorText + "')]");
    }

    /**
     * Returns the HTML page contained in a nested frame window. The path to the respective frame is given as an array
     * of frame names, starting with the name of the outermost frame.
     * 
     * @param page
     *            the page to search
     * @param frameNames
     *            the list of frame names
     * @return the frame page found
     * @throws ElementNotFoundException
     *             if there is no frame with the given name at the respective nesting level
     */
    public static HtmlPage getFramePage(final HtmlPage page, final String... frameNames)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(page, "page");
        ParameterCheckUtils.isNotNullOrEmpty(frameNames, "frameNames");

        // get the frame page
        HtmlPage framePage = null;
        for (int i = 0; i < frameNames.length; i++)
        {
            FrameWindow frameWindow = null;

            if (i == 0)
            {
                frameWindow = page.getFrameByName(frameNames[i]);
            }
            else
            {
                frameWindow = framePage.getFrameByName(frameNames[i]);
            }

            framePage = (HtmlPage) frameWindow.getEnclosedPage();
        }

        return framePage;
    }

    /**
     * Returns the first input element that ends with this suffix.
     * 
     * @param form
     *            the form to use
     * @param suffix
     *            the suffix of the input elements to search
     * @return the first found input element or null otherwise
     */
    @SuppressWarnings("unchecked")
    public static <T extends HtmlInput> T getInputEndingWith(final HtmlForm form, final String suffix)
    {
        final List<HtmlElement> elements = form.getElementsByTagName("input");

        // go over all inputs and check the name
        for (final HtmlElement element : elements)
        {
            final T input = (T) element;
            final String name = input.getNameAttribute();

            if (name != null && name.endsWith(suffix))
            {
                return input;
            }
        }

        return null;
    }

    /**
     * Returns the first input element that starts with this prefix.
     * 
     * @param form
     *            the form to use
     * @param prefix
     *            the prefix of the input elements to search
     * @return the first found input element or null otherwise
     */
    @SuppressWarnings("unchecked")
    public static <T extends HtmlInput> T getInputStartingWith(final HtmlForm form, final String prefix)
    {
        final List<HtmlElement> elements = form.getElementsByTagName("input");

        // go over all inputs and check the name
        for (final HtmlElement element : elements)
        {
            final T input = (T) element;
            final String name = input.getNameAttribute();

            if (name != null && name.startsWith(prefix))
            {
                return input;
            }
        }

        return null;
    }

    /**
     * Returns the first select element that ends with this suffix.
     * 
     * @param form
     *            the form to use
     * @param suffix
     *            the suffix of the select elements to search
     * @return the first found select element or null otherwise
     */
    public static HtmlSelect getSelectEndingWith(final HtmlForm form, final String suffix)
    {
        final List<HtmlElement> elements = form.getElementsByTagName("select");

        // go over all selects and check the name
        for (final HtmlElement element : elements)
        {
            final HtmlSelect select = (HtmlSelect) element;
            final String name = select.getNameAttribute();

            if (name != null && name.endsWith(suffix))
            {
                return select;
            }
        }

        return null;
    }

    /**
     * Returns the first select element that starts with this prefix.
     * 
     * @param form
     *            the form to use
     * @param prefix
     *            the prefix of the select elements to search
     * @return the first found select element or null otherwise
     */
    public static HtmlSelect getSelectStartingWith(final HtmlForm form, final String prefix)
    {
        final List<HtmlElement> elements = form.getElementsByTagName("select");

        // go over all selects and check the name
        for (final HtmlElement element : elements)
        {
            final HtmlSelect select = (HtmlSelect) element;
            final String name = select.getNameAttribute();

            if (name != null && name.startsWith(prefix))
            {
                return select;
            }
        }

        return null;
    }

    /**
     * Finds the HTML select element with the given name in the specified form and selects the option with the passed
     * value.
     * 
     * @param form
     *            the form to search
     * @param selectName
     *            the name of the select element
     * @param optionValue
     *            the value of the option element to select
     */
    public static void select(final HtmlForm form, final String selectName, final String optionValue)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(form, "form");
        ParameterCheckUtils.isNotNullOrEmpty(selectName, "selectName");
        ParameterCheckUtils.isNotNull(optionValue, "optionValue");

        // get the select
        final List<HtmlSelect> selects = form.getSelectsByName(selectName);
        Assert.assertEquals("No or too many selects found for name: " + selectName + " -", 1, selects.size());

        // select the given option value
        selects.get(0).setSelectedAttribute(optionValue, true);
    }

    /**
     * Finds the HTML select element with the given name in the specified form and selects one of the options randomly.
     * 
     * @param form
     *            the form to search
     * @param selectName
     *            the name of the select element
     */
    public static void selectRandomly(final HtmlForm form, final String selectName)
    {
        selectRandomly(form, selectName, false, false);
    }

    /**
     * Finds the HTML select element with the given name in the specified form and selects one of the options randomly.
     * 
     * @param form
     *            the form to search
     * @param selectName
     *            the name of the select element
     * @param excludeFirst
     *            whether to exclude the first option element
     */
    public static void selectRandomly(final HtmlForm form, final String selectName, final boolean excludeFirst)
    {
        selectRandomly(form, selectName, excludeFirst, false);
    }

    /**
     * Finds the HTML select element with the given name in the specified form and set one of the options selected
     * randomly. Disabled option will be ignored.
     * 
     * @param form
     *            the form to search
     * @param selectName
     *            the name of the select element
     * @param excludeFirst
     *            whether to exclude the first option element
     * @param excludeLast
     *            whether to exclude the last option element
     */
    public static void selectRandomly(final HtmlForm form, final String selectName, final boolean excludeFirst, final boolean excludeLast)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(form, "form");
        ParameterCheckUtils.isNotNullOrEmpty(selectName, "selectName");

        // get the select
        final List<HtmlSelect> selects = form.getSelectsByName(selectName);
        Assert.assertEquals("No or too many selects found for name: " + selectName, 1, selects.size());

        final HtmlSelect select = selects.get(0);

        // get the enabled option elements
        final List<HtmlOption> origOptions = new ArrayList<HtmlOption>(select.getOptions());
        final List<HtmlOption> options = new ArrayList<HtmlOption>(origOptions.size());

        for (int i = 0; i < origOptions.size(); i++)
        {
            final HtmlOption option = origOptions.get(i);
            if (!option.isDisabled())
            {
                options.add(option);
            }
        }

        // select one of the options
        final HtmlOption option = pickOneRandomly(options, excludeFirst, excludeLast);
        select.setSelectedAttribute(option, true);

        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            XltLogger.runTimeLogger.info(String.format("Setting select value: %s.%s = %s", getIdOrName(form), getIdOrName(select),
                                                       option.getValueAttribute()));
        }
    }

    /**
     * Finds the HTML check box input element with the given name in the specified form and sets its value.
     * 
     * @param form
     *            the form to search
     * @param checkBoxName
     *            the name of the check box
     * @param isChecked
     *            the new check box value
     */
    public static void setCheckBoxValue(final HtmlForm form, final String checkBoxName, final boolean isChecked)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(form, "form");
        ParameterCheckUtils.isNotNullOrEmpty(checkBoxName, "checkBoxName");

        // get the input
        final HtmlInput input = form.getInputByName(checkBoxName);
        Assert.assertEquals("Found input element is not a check box", HtmlCheckBoxInput.class, input.getClass());

        // set the check box value
        ((HtmlCheckBoxInput) input).setChecked(isChecked);

        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            XltLogger.runTimeLogger.info(String.format("Setting check box value: %s.%s = %b", getIdOrName(form), checkBoxName, isChecked));
        }
    }

    /**
     * Finds the HTML input element with the given name in the specified form and sets its value.
     * 
     * @param form
     *            the form to search
     * @param inputName
     *            the name of the input element
     * @param value
     *            the new value
     */
    public static void setInputValue(final HtmlForm form, final String inputName, final String value)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(form, "form");
        ParameterCheckUtils.isNotNullOrEmpty(inputName, "inputName");

        // get the input
        final HtmlInput input = form.getInputByName(inputName);

        // set the input value
        input.setValueAttribute(value);

        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            XltLogger.runTimeLogger.info(String.format("Setting input value: %s.%s = %s", getIdOrName(form), inputName, value));
        }
    }

    /**
     * Returns a list of forms the ID of which matches the given regular expression.
     * 
     * @param page
     *            the page to search
     * @param pattern
     *            the regex pattern to match the form ID
     * @return the list of forms found
     */
    public static List<HtmlForm> getFormsByIDRegExp(final HtmlPage page, final Pattern pattern)
    {
        final List<HtmlForm> forms = page.getForms();
        final List<HtmlForm> matchingForms = new ArrayList<HtmlForm>();

        // go over all forms and check ID
        for (final HtmlForm form : forms)
        {
            if (pattern.matcher(form.getId()).matches())
            {
                matchingForms.add(form);
            }
        }

        return matchingForms;
    }

    /**
     * Returns a list of forms the name of which matches the given regular expression.
     * 
     * @param page
     *            the page to search
     * @param pattern
     *            the regex pattern to match the form name
     * @return the list of forms found
     */
    public static List<HtmlForm> getFormsByNameRegExp(final HtmlPage page, final Pattern pattern)
    {
        final List<HtmlForm> forms = page.getForms();
        final List<HtmlForm> matchingForms = new ArrayList<HtmlForm>();

        // go over all forms and check name
        for (final HtmlForm form : forms)
        {
            if (pattern.matcher(form.getNameAttribute()).matches())
            {
                matchingForms.add(form);
            }
        }

        return matchingForms;
    }

    /**
     * Returns whether or not a HTML element exists for the given XPath expression.
     * 
     * @param page
     *            the HTML page
     * @param xpath
     *            the XPath expression
     * @return <code>true</code> if at least one HTML element matches the given XPath expression, <code>false</code>
     *         otherwise
     */
    public static boolean isElementPresent(final HtmlPage page, final String xpath)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(page, "page");
        ParameterCheckUtils.isNotNullOrEmpty(xpath, "xpath");

        // get list of matching elements and check if it is empty
        return !page.getByXPath(xpath).isEmpty();
    }

    /**
     * Returns whether or not a HTML element exists for the given XPath expression.
     * 
     * @param element
     *            the HTML element
     * @param xpath
     *            the XPath expression
     * @return <code>true</code> if at least one HTML element matches the given XPath expression, <code>false</code>
     *         otherwise
     */
    public static boolean isElementPresent(final HtmlElement element, final String xpath)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(element, "element");
        ParameterCheckUtils.isNotNullOrEmpty(xpath, "xpath");

        // get list of matching elements and check if it is empty
        return !element.getByXPath(xpath).isEmpty();
    }

    /**
     * Returns the number of elements that match the given XPath expression.
     * 
     * @param page
     *            the HTML page
     * @param xpath
     *            the XPath expression
     * @return number of matching elements
     */
    public static int countElementsByXPath(final HtmlPage page, final String xpath)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(page, "page");
        ParameterCheckUtils.isNotNullOrEmpty(xpath, "xpath");

        // get list of matching elements and return its size
        return page.getByXPath(xpath).size();
    }

    /**
     * Waits until at least one HTML element can be located on the given page using the specified XPath expression and
     * returns the list of matching elements. If the waiting time exceeds the given timeout value, an
     * {@link AssertionError} is thrown. This method can be used to wait for HTML elements that appear on the page only
     * after some JavaScript code has finished to run.
     * 
     * @param page
     *            the HTML page
     * @param xpath
     *            the XPath expression
     * @param timeout
     *            the timeout value [ms]
     * @return the list of matching elements
     * @throws InterruptedException
     *             if the current thread is interrupted while sleeping
     * @throws AssertionError
     *             if no elements were found in the given time period
     */
    public static <T extends HtmlElement> List<T> waitForHtmlElements(final HtmlPage page, final String xpath, long timeout)
        throws InterruptedException
    {
        // parameter check
        timeout = Math.max(0, timeout);

        // wait for the elements to appear
        final long endTime = TimerUtils.getTime() + timeout;
        while (TimerUtils.getTime() < endTime)
        {
            try
            {
                return findHtmlElements(page, xpath);
            }
            catch (final AssertionError e)
            {
                // no elements found yet -> sleep a little
                Thread.sleep(500);
            }
        }

        // timed out
        Assert.fail(String.format("Timed-out after waiting %,d ms - Still no elements found for XPath: %s", timeout, xpath));

        // won't get here, but make the compiler happy
        return null;
    }

    /**
     * Returns the value of the "id" attribute or, if there is no such attribute, the "name" attribute for the given
     * HTML element.
     * 
     * @param element
     *            the HTML element in question
     * @return the ID or the name
     */
    private static String getIdOrName(final HtmlElement element)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(element, "element");

        // get the id or name
        String result = element.getId();

        if (result == null || result.length() == 0)
        {
            result = element.getAttribute("name");
        }

        if (result == null || result.length() == 0)
        {
            result = "<unnamed>";
        }

        return result;
    }
}
