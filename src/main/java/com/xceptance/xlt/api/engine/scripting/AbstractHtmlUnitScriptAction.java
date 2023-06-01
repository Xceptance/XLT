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
package com.xceptance.xlt.api.engine.scripting;

import java.io.IOException;
import java.net.URL;

import org.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.actions.AbstractWebAction;
import com.xceptance.xlt.engine.scripting.TestContext;
import com.xceptance.xlt.engine.scripting.htmlunit.HtmlUnitScriptCommands;

/**
 * Base class of HTML page actions supporting script commands.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractHtmlUnitScriptAction extends AbstractHtmlPageAction
{
    /**
     * Constructor.
     * 
     * @param previousAction
     *            previous action
     */
    public AbstractHtmlUnitScriptAction(final AbstractWebAction previousAction)
    {
        this(previousAction, null);
    }

    /**
     * Constructor.
     * 
     * @param prevAction
     *            previous action
     * @param timerName
     *            timer name
     */
    public AbstractHtmlUnitScriptAction(final AbstractWebAction prevAction, final String timerName)
    {
        super(prevAction, timerName);
        TestContext.getCurrent().setWebClient(getWebClient());
    }

    /**
     * Constructor.
     * 
     * @param timerName
     *            timer name
     */
    public AbstractHtmlUnitScriptAction(final String timerName)
    {
        this(null, timerName);
    }

    /**
     * Adds the given option of the given select to the current selection.
     * 
     * @param select
     *            the select
     * @param option
     *            the option to be added to current selection
     */
    protected HtmlPage addSelection(final String select, final String option)
    {
        return getAdapter().addSelection(select, option);
    }

    /**
     * Asserts that the value of the attribute identified by the given attribute locator matches the given text pattern.
     * 
     * @param attributeLocator
     *            the attribute locator
     * @param textPattern
     *            the text pattern that the attribute value must match
     */
    protected void assertAttribute(final String attributeLocator, final String textPattern)
    {
        getAdapter().assertAttribute(attributeLocator, textPattern);
    }

    /**
     * Asserts that the value of the attribute identified by the given element locator and attribute name matches the
     * given text pattern.
     * 
     * @param elementLocator
     *            the element locator
     * @param attributeName
     *            the name of the attribute
     * @param textPattern
     *            the text pattern that the attribute value must match
     */
    protected void assertAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        getAdapter().assertAttribute(elementLocator, attributeName, textPattern);
    }

    /**
     * Asserts that the given checkbox/radio button is checked.
     * 
     * @param elementLocator
     *            the checkbox/radio button element locator
     */
    protected void assertChecked(final String elementLocator)
    {
        getAdapter().assertChecked(elementLocator);
    }

    /**
     * Asserts that the given element has the given class(es).
     * 
     * @param elementLocator
     *            the element locator
     * @param clazzString
     *            the class(es) string
     */
    protected void assertClass(final String elementLocator, final String clazzString)
    {
        getAdapter().assertClass(elementLocator, clazzString);
    }

    /**
     * Asserts that the number of elements found by using the given element locator is equal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    protected void assertElementCount(final String elementLocator, final int count)
    {
        getAdapter().assertElementCount(elementLocator, count);
    }

    /**
     * Asserts that the number of elements found by using the given element locator is equal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    protected void assertElementCount(final String elementLocator, final String count)
    {
        getAdapter().assertElementCount(elementLocator, count);
    }

    /**
     * Asserts that the given element is present.
     * 
     * @param elementLocator
     *            locator identifying the element that should be present
     */
    protected void assertElementPresent(final String elementLocator)
    {
        getAdapter().assertElementPresent(elementLocator);
    }

    /**
     * Asserts that evaluating the given expression matches the given text pattern.
     * 
     * @param expression
     *            the expression to evaluate
     * @param textPattern
     *            the text pattern that the evaluation result must match
     */
    protected void assertEval(final String expression, final String textPattern)
    {
        getAdapter().assertEval(expression, textPattern);
    }

    /**
     * Asserts that the time needed to load a does not exceed the given value.
     * 
     * @param loadTime
     *            maximum load time in milliseconds
     */
    protected void assertLoadTime(final long loadTime)
    {
        getAdapter().assertLoadTime(loadTime);
    }

    /**
     * Asserts that the time needed to load a does not exceed the given value.
     * 
     * @param loadTime
     *            maximum load time in milliseconds
     */
    protected void assertLoadTime(final String loadTime)
    {
        getAdapter().assertLoadTime(loadTime);
    }

    /**
     * Asserts that the value of the attribute identified by the given attribute locator does NOT match the given text
     * pattern.
     * 
     * @param attributeLocator
     *            the attribute locator
     * @param textPattern
     *            the text pattern that the attribute value must NOT match
     */
    protected void assertNotAttribute(final String attributeLocator, final String textPattern)
    {
        getAdapter().assertNotAttribute(attributeLocator, textPattern);
    }

    /**
     * Asserts that the value of the attribute identified by the given element locator and attribute name does NOT match
     * the given text pattern.
     * 
     * @param elementLocator
     *            the element locator
     * @param attributeName
     *            the name of the attribute
     * @param textPattern
     *            the text pattern that the attribute value must NOT match
     */
    protected void assertNotAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        getAdapter().assertNotAttribute(elementLocator, attributeName, textPattern);
    }

    /**
     * Asserts that the given checkbox/radio button is unchecked.
     * 
     * @param elementLocator
     *            the checkbox/radio button element locator
     */
    protected void assertNotChecked(final String elementLocator)
    {
        getAdapter().assertNotChecked(elementLocator);
    }

    /**
     * Asserts that the given element doesn't have the given class(es).
     * 
     * @param elementLocator
     *            the element locator
     * @param clazzString
     *            the class(es) string
     */
    protected void assertNotClass(final String elementLocator, final String clazzString)
    {
        getAdapter().assertNotClass(elementLocator, clazzString);
    }

    /**
     * Asserts that the number of elements found by using the given element locator is unequal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    protected void assertNotElementCount(final String elementLocator, final int count)
    {
        getAdapter().assertNotElementCount(elementLocator, count);
    }

    /**
     * Asserts that the number of elements found by using the given element locator is unequal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    protected void assertNotElementCount(final String elementLocator, final String count)
    {
        getAdapter().assertNotElementCount(elementLocator, count);
    }

    /**
     * Asserts that the given element is not present.
     * 
     * @param elementLocator
     *            locator identifying the element that should be NOT present
     */
    protected void assertNotElementPresent(final String elementLocator)
    {
        getAdapter().assertNotElementPresent(elementLocator);
    }

    /**
     * Asserts that evaluating the given expression does NOT match the given text pattern.
     * 
     * @param expression
     *            the expression to evaluate
     * @param textPattern
     *            the text pattern that the evaluation result must NOT match
     */
    protected void assertNotEval(final String expression, final String textPattern)
    {
        getAdapter().assertNotEval(expression, textPattern);
    }

    /**
     * Asserts that no ID of all selected options of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param idPattern
     *            the ID pattern
     */
    protected void assertNotSelectedId(final String selectLocator, final String idPattern)
    {
        getAdapter().assertNotSelectedId(selectLocator, idPattern);
    }

    /**
     * Asserts that the option of the given select element at the given index is not selected.
     * 
     * @param selectLocator
     *            the select element locator
     * @param indexPattern
     *            the option index pattern
     */
    protected void assertNotSelectedIndex(final String selectLocator, final String indexPattern)
    {
        getAdapter().assertNotSelectedIndex(selectLocator, indexPattern);
    }

    /**
     * Asserts that no label of all selected options of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param labelPattern
     *            the label pattern
     */
    protected void assertNotSelectedLabel(final String selectLocator, final String labelPattern)
    {
        getAdapter().assertNotSelectedLabel(selectLocator, labelPattern);
    }

    /**
     * Asserts that no value of all selected options of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param valuePattern
     *            the value pattern
     */
    protected void assertNotSelectedValue(final String selectLocator, final String valuePattern)
    {
        getAdapter().assertNotSelectedValue(selectLocator, valuePattern);
    }

    /**
     * Asserts that the effective style of the element identified by the given element locator does NOT match the given
     * style.
     * 
     * @param elementLocator
     *            the element locator
     * @param styleText
     *            the style that must NOT match (e.g. <code>width: 10px; overflow: hidden;</code>)
     */
    protected void assertNotStyle(final String elementLocator, final String styleText)
    {
        getAdapter().assertNotStyle(elementLocator, styleText);
    }

    /**
     * Asserts that the embedded text of the given element does not contain the given text.
     * 
     * @param elementLocator
     *            locator identifying the element
     * @param text
     *            the text that should not be embedded in the given element
     */
    protected void assertNotText(final String elementLocator, final String text)
    {
        getAdapter().assertNotText(elementLocator, text);
    }

    /**
     * Asserts that the given text is not present on the .
     * 
     * @param text
     *            the text that should NOT be present
     */
    protected void assertNotTextPresent(final String text)
    {
        getAdapter().assertNotTextPresent(text);
    }

    /**
     * Asserts that the title does not match the given title.
     * 
     * @param title
     *            the title that should not match
     */
    protected void assertNotTitle(final String title)
    {
        getAdapter().assertNotTitle(title);
    }

    /**
     * Asserts that the value of the given element doesn't match the given value. If the element is a &lt;textarea&gt;
     * this method asserts that the containing text doesnt' match the given value.
     * 
     * @param elementLocator
     *            locator identifying the element whose value doesn't match the given value
     * @param value
     *            the value that doesn't match the given element's value
     */
    protected void assertNotValue(final String elementLocator, final String value)
    {
        getAdapter().assertNotValue(elementLocator, value);
    }

    /**
     * Asserts that the given element is invisible.
     * 
     * @param elementLocator
     *            the element locator
     */
    protected void assertNotVisible(final String elementLocator)
    {
        getAdapter().assertNotVisible(elementLocator);
    }

    /**
     * Asserts that the number of elements locatable by the given XPath expression is not equal to the given count.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements that should NOT be equal to the actual number of elements matching the given
     *            XPath expression
     */
    protected void assertNotXpathCount(final String xpath, final int count)
    {
        getAdapter().assertNotXpathCount(xpath, count);
    }

    /**
     * Asserts that the number of elements locatable by the given XPath expression is not equal to the given count.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements that should NOT be equal to the actual number of elements matching the given
     *            XPath expression
     */
    protected void assertNotXpathCount(final String xpath, final String count)
    {
        getAdapter().assertNotXpathCount(xpath, count);
    }

    /**
     * Asserts that the size of the actual (including images etc.) does not exceed the given value.
     * 
     * @param pageSize
     *            the number of bytes the size must not exceed
     */
    protected void assertPageSize(final long pageSize)
    {
        getAdapter().assertPageSize(pageSize);
    }

    /**
     * Asserts that the size of the actual (including images etc.) does not exceed the given value.
     * 
     * @param pageSize
     *            the number of bytes the size must not exceed
     */
    protected void assertPageSize(final String pageSize)
    {
        getAdapter().assertPageSize(pageSize);
    }

    /**
     * Asserts that the ID of at least one selected option of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param idPattern
     *            the ID pattern
     */
    protected void assertSelectedId(final String selectLocator, final String idPattern)
    {
        getAdapter().assertSelectedId(selectLocator, idPattern);
    }

    /**
     * Asserts that the option of the given select element at the given index is selected.
     * 
     * @param selectLocator
     *            the select element locator
     * @param indexPattern
     *            the option index pattern
     */
    protected void assertSelectedIndex(final String selectLocator, final String indexPattern)
    {
        getAdapter().assertSelectedIndex(selectLocator, indexPattern);
    }

    /**
     * Asserts that the label of at least one selected option of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param labelPattern
     *            the label pattern that must match
     */
    protected void assertSelectedLabel(final String selectLocator, final String labelPattern)
    {
        getAdapter().assertSelectedLabel(selectLocator, labelPattern);
    }

    /**
     * Asserts that the value of at least one selected option of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param valuePattern
     *            the value pattern that must match
     */
    protected void assertSelectedValue(final String selectLocator, final String valuePattern)
    {
        getAdapter().assertSelectedValue(selectLocator, valuePattern);
    }

    /**
     * Asserts that the effective style of the element identified by the given element locator matches the given style.
     * 
     * @param elementLocator
     *            the element locator
     * @param styleText
     *            the style to match (e.g. <code>width: 10px; overflow: hidden;</code>)
     */
    protected void assertStyle(final String elementLocator, final String styleText)
    {
        getAdapter().assertStyle(elementLocator, styleText);
    }

    /**
     * Asserts that the text embedded by the given element contains the given text.
     * 
     * @param elementLocator
     *            locator identifying the element whose text should contain the given text
     * @param text
     *            the text that should be embedded in the given element
     */
    protected void assertText(final String elementLocator, final String text)
    {
        getAdapter().assertText(elementLocator, text);
    }

    /**
     * Asserts that the given text is present.
     * 
     * @param text
     *            the text that should be present
     */
    protected void assertTextPresent(final String text)
    {
        getAdapter().assertTextPresent(text);
    }

    /**
     * Asserts that the given title matches the title.
     * 
     * @param title
     *            the title that should match the title
     */
    protected void assertTitle(final String title)
    {
        getAdapter().assertTitle(title);
    }

    /**
     * Asserts that the value of the given element matches the given value. If the element is a &lt;textarea&gt; this
     * method asserts that the containing text matches the given value.
     * 
     * @param elementLocator
     *            locator identifying the element whose value matches the given value
     * @param value
     *            the value that matches the given element's value
     */
    protected void assertValue(final String elementLocator, final String value)
    {
        getAdapter().assertValue(elementLocator, value);
    }

    /**
     * Asserts that the given element is visible.
     * 
     * @param elementLocator
     *            the element locator
     */
    protected void assertVisible(final String elementLocator)
    {
        getAdapter().assertVisible(elementLocator);
    }

    /**
     * Asserts that the number of elements locatable by the given XPath expression is equal to the given count.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements that must match the given XPath expression
     */
    protected void assertXpathCount(final String xpath, final int count)
    {
        getAdapter().assertXpathCount(xpath, count);
    }

    /**
     * Asserts that the number of elements locatable by the given XPath expression is equal to the given count.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements that must match the given XPath expression
     */
    protected void assertXpathCount(final String xpath, final String count)
    {
        getAdapter().assertXpathCount(xpath, count);
    }

    /**
     * Checks/toggles the given element.
     * 
     * @param elementLocator
     *            locator identifying the element to be checked/toggled
     * @throws IOException
     */
    protected HtmlPage check(final String elementLocator) throws IOException
    {
        return getAdapter().check(elementLocator);
    }

    /**
     * Checks/toggles the given element and waits for some activity to complete.
     * 
     * @param elementLocator
     *            locator identifying the element to be checked/toggled
     * @throws IOException
     */
    protected HtmlPage checkAndWait(final String elementLocator) throws IOException
    {
        return getAdapter().checkAndWait(elementLocator);
    }

    /**
     * Clicks the given element.
     * 
     * @param elementLocator
     *            locator identifying the element to be clicked.
     * @throws IOException
     */
    protected HtmlPage click(final String elementLocator) throws IOException
    {
        return getAdapter().click(elementLocator);
    }

    /**
     * Clicks the given element and waits for some activity to complete.
     * 
     * @param elementLocator
     *            locator identifying the element to be clicked
     * @throws IOException
     */
    protected HtmlPage clickAndWait(final String elementLocator) throws IOException
    {
        return getAdapter().clickAndWait(elementLocator);
    }

    /**
     * Closes the current window if it is a top-level window.
     */
    protected void close()
    {
        getAdapter().close();
    }

    /**
     * Simulates a right-click on the given element.
     * 
     * @param elementLocator
     *            locator identifying the element to fire events at
     */
    protected HtmlPage contextMenu(final String elementLocator)
    {
        return getAdapter().contextMenu(elementLocator);
    }

    /**
     * Simulates a right-click at the given coordinates (relative to the given element).
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordinates
     *            the coordinates relative to the given element
     */
    protected HtmlPage contextMenuAt(final String elementLocator, final String coordinates)
    {
        return getAdapter().contextMenuAt(elementLocator, coordinates);
    }

    /**
     * Simulates a right-click at the given coordinates (relative to the given element).
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordX
     *            the X coordinate relative to the given element
     * @param coordY
     *            the Y coordinate relative to the given element
     */
    protected HtmlPage contextMenuAt(final String elementLocator, final int coordX, final int coordY)
    {
        return getAdapter().contextMenuAt(elementLocator, coordX, coordY);
    }

    /**
     * Creates a new cookie. The new cookie will be stored as session cookie for the current path and domain.
     * 
     * @param cookie
     *            name value pair of the new cookie
     */
    protected void createCookie(final String cookie)
    {
        getAdapter().createCookie(cookie);
    }

    /**
     * Creates a new cookie.
     * 
     * @param cookie
     *            name value pair of the new cookie
     * @param options
     *            cookie creation options (path, max_age and domain)
     */
    protected void createCookie(final String cookie, final String options)
    {
        getAdapter().createCookie(cookie, options);
    }

    /**
     * Removes all cookies visible to the current .
     */
    protected void deleteAllVisibleCookies()
    {
        getAdapter().deleteAllVisibleCookies();
    }

    /**
     * Removes the cookie with the specified name.
     * 
     * @param name
     *            the cookie's name
     */
    protected void deleteCookie(final String name)
    {
        getAdapter().deleteCookie(name);
    }

    /**
     * Removes the cookie with the specified name.
     * 
     * @param name
     *            the cookie's name
     * @param options
     *            cookie removal options (path, domain and recurse)
     */
    protected void deleteCookie(final String name, final String options)
    {
        getAdapter().deleteCookie(name, options);
    }

    /**
     * Double-clicks the given element.
     * 
     * @param elementLocator
     *            locator identifying the element to be double-clicked
     */
    protected HtmlPage doubleClick(final String elementLocator) throws IOException
    {
        return getAdapter().doubleClick(elementLocator);
    }

    /**
     * Double-clicks the given element and waits for a page to be loaded.
     * 
     * @param elementLocator
     *            locator identifying the element to be double-clicked
     */
    protected HtmlPage doubleClickAndWait(final String elementLocator) throws IOException
    {
        return getAdapter().doubleClickAndWait(elementLocator);
    }

    /**
     * Prints the given message to the log.
     * 
     * @param message
     *            the message to print
     */
    protected void echo(final String message)
    {
        getAdapter().echo(message);
    }

    /**
     * Presses the left mouse button on an element, but does not release the button yet.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    protected HtmlPage mouseDown(final String elementLocator)
    {
        return getAdapter().mouseDown(elementLocator);
    }

    /**
     * Presses the left mouse button at the given coordinates (relative to the given element), but does not release the
     * button yet.
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordinates
     *            the coordinates relative to the given element
     */
    protected HtmlPage mouseDownAt(final String elementLocator, final String coordinates)
    {
        return getAdapter().mouseDownAt(elementLocator, coordinates);
    }

    /**
     * Presses the left mouse button at the given coordinates (relative to the given element), but does not release the
     * button yet.
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordX
     *            the X coordinate relative to the given element
     * @param coordY
     *            the Y coordinate relative to the given element
     */
    protected HtmlPage mouseDownAt(final String elementLocator, final int coordX, final int coordY)
    {
        return getAdapter().mouseDownAt(elementLocator, coordX, coordY);
    }

    /**
     * Moves the mouse to the given element.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    protected HtmlPage mouseMove(final String elementLocator)
    {
        return getAdapter().mouseMove(elementLocator);
    }

    /**
     * Moves the mouse to the given coordinates (relative to the given element).
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordinates
     *            the coordinates relative to the given element
     */
    protected HtmlPage mouseMoveAt(final String elementLocator, final String coordinates)
    {
        return getAdapter().mouseMoveAt(elementLocator, coordinates);
    }

    /**
     * Moves the mouse to the given coordinates (relative to the given element).
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordX
     *            the X coordinate relative to the given element
     * @param coordY
     *            the Y coordinate relative to the given element
     */
    protected HtmlPage mouseMoveAt(final String elementLocator, final int coordX, final int coordY)
    {
        return getAdapter().mouseMoveAt(elementLocator, coordX, coordY);
    }

    /**
     * Moves the mouse out of the element's bounding box.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    protected HtmlPage mouseOut(final String elementLocator)
    {
        return getAdapter().mouseOut(elementLocator);
    }

    /**
     * Hovers the mouse over an element.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    protected HtmlPage mouseOver(final String elementLocator)
    {
        return getAdapter().mouseOver(elementLocator);
    }

    /**
     * Releases the left mouse button on an element.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    protected HtmlPage mouseUp(final String elementLocator)
    {
        return getAdapter().mouseUp(elementLocator);
    }

    /**
     * Releases the left mouse button at the given coordinates (relative to the given element).
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordinates
     *            the coordinates relative to the given element
     */
    protected HtmlPage mouseUpAt(final String elementLocator, final String coordinates)
    {
        return getAdapter().mouseUpAt(elementLocator, coordinates);
    }

    /**
     * Releases the left mouse button at the given coordinates (relative to the given element).
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordX
     *            the X coordinate relative to the given element
     * @param coordY
     *            the Y coordinate relative to the given element
     */
    protected HtmlPage mouseUpAt(final String elementLocator, final int coordX, final int coordY)
    {
        return getAdapter().mouseUpAt(elementLocator, coordX, coordY);
    }

    /**
     * Opens the given URL.
     * 
     * @param urlToOpen
     *            the URL to open
     * @return HTML page located at the given URL
     * @throws Exception
     *             thrown if access to the given URL has failed
     */
    protected HtmlPage open(final String urlToOpen) throws Exception
    {
        return getAdapter().open(urlToOpen);
    }

    /**
     * Opens the given URL.
     * 
     * @param urlToOpen
     *            the URL to open
     * @return HTML page located at the given URL
     * @throws Exception
     *             thrown if access to the given URL has failed
     */
    protected HtmlPage open(final URL urlToOpen) throws Exception
    {
        return getAdapter().open(urlToOpen);
    }

    /**
     * Waits the given time.
     * 
     * @param waitingTime
     *            the time in milliseconds to wait
     */
    protected HtmlPage pause(final long waitingTime)
    {
        return getAdapter().pause(waitingTime);
    }

    /**
     * Waits the given time.
     * 
     * @param waitingTime
     *            the time in milliseconds to wait
     */
    protected HtmlPage pause(final String waitingTime)
    {
        return getAdapter().pause(waitingTime);
    }

    /**
     * Removes the given option of the given select from the current selection.
     * 
     * @param select
     *            the select
     * @param option
     *            the option to be removed from the current selection
     */
    protected HtmlPage removeSelection(final String select, final String option)
    {
        return getAdapter().removeSelection(select, option);
    }

    /**
     * Selects the given option of the given select.
     * 
     * @param select
     *            the select
     * @param option
     *            the option to select
     */
    protected HtmlPage select(final String select, final String option)
    {
        return getAdapter().select(select, option);
    }

    /**
     * Selects the given option of the given select and waits for some activity to complete.
     * 
     * @param select
     *            the select
     * @param option
     *            the option to select
     */
    protected HtmlPage selectAndWait(final String select, final String option)
    {
        return getAdapter().selectAndWait(select, option);
    }

    /**
     * Selects the given frame.
     * 
     * @param frameTarget
     *            the frame to be selected
     */
    protected HtmlPage selectFrame(final String frameTarget)
    {
        return getAdapter().selectFrame(frameTarget);
    }

    /**
     * Selects the top-level window.
     */
    protected HtmlPage selectWindow()
    {
        return getAdapter().selectWindow();
    }

    /**
     * Selects the given window.
     * 
     * @param windowTarget
     *            the window to be selected
     */
    protected HtmlPage selectWindow(final String windowTarget)
    {
        return getAdapter().selectWindow(windowTarget);
    }

    /**
     * Sets the timeout to the given value.
     * 
     * @param timeout
     *            the new timeout in milliseconds
     */
    protected void setTimeout(final long timeout)
    {
        TestContext.getCurrent().setTimeout(timeout);
    }

    /**
     * Sets the timeout to the given value.
     * 
     * @param timeout
     *            the new timeout in milliseconds
     */
    protected void setTimeout(final String timeout)
    {
        getAdapter().setTimeout(timeout);
    }

    /**
     * Stores the given text to the given variable.
     * 
     * @param text
     *            the text to store
     * @param variableName
     *            the variable name
     */
    protected void store(final String text, final String variableName)
    {
        getAdapter().store(text, variableName);
    }

    /**
     * Stores the value of the attribute identified by the given attribute locator to the given variable
     * 
     * @param attributeLocator
     *            the attribute locator
     * @param variableName
     *            the variable name
     */
    protected void storeAttribute(final String attributeLocator, final String variableName)
    {
        getAdapter().storeAttribute(attributeLocator, variableName);
    }

    /**
     * Stores the value of the given element and attribute to the given variable.
     * 
     * @param elementLocator
     *            the element locator
     * @param attributeName
     *            the name of the attribute
     * @param variableName
     *            the variable name
     */
    protected void storeAttribute(final String elementLocator, final String attributeName, final String variableName)
    {
        getAdapter().storeAttribute(elementLocator, attributeName, variableName);
    }

    /**
     * Stores that the number of elements found by using the given element locator to the given variable.
     * 
     * @param elementLocator
     *            the element locator
     * @param variableName
     *            the variable name
     */
    protected void storeElementCount(final String elementLocator, final String variableName)
    {
        getAdapter().storeElementCount(elementLocator, variableName);
    }

    /**
     * Stores the result of evaluating the given expression to the given variable.
     * 
     * @param expression
     *            the expression to evaluate
     * @param variableName
     *            the variable
     */
    protected void storeEval(final String expression, final String variableName)
    {
        getAdapter().storeEval(expression, variableName);
    }

    /**
     * Stores the text of the element identified by the given locator to the given variable.
     * 
     * @param elementLocator
     *            the element locator
     * @param variableName
     *            the variable
     */
    protected void storeText(final String elementLocator, final String variableName)
    {
        getAdapter().storeText(elementLocator, variableName);
    }

    /**
     * Stores the title of the currently active document to the given variable.
     * 
     * @param variableName
     *            the name of the variable
     */
    protected void storeTitle(final String variableName)
    {
        getAdapter().storeTitle(variableName);
    }

    /**
     * Stores the value (in case of a <code>&lt;textarea&gt;</code> the contained text) of the element identified by the
     * given locator to the given variable.
     * 
     * @param elementLocator
     *            the element locator
     * @param variableName
     *            the variable
     */
    protected void storeValue(final String elementLocator, final String variableName)
    {
        getAdapter().storeValue(elementLocator, variableName);
    }

    /**
     * Stores the number of elements matching the given XPath expression to the given variable.
     * 
     * @param xpath
     *            the XPath expression
     * @param variableName
     *            the variable
     */
    protected void storeXpathCount(final String xpath, final String variableName)
    {
        getAdapter().storeXpathCount(xpath, variableName);
    }

    /**
     * Submits the given form.
     * 
     * @param form
     *            the form to submit
     */
    protected HtmlPage submit(final String form) throws Exception
    {
        return getAdapter().submit(form);
    }

    /**
     * Submits the given form and waits for some activity to complete.
     * 
     * @param form
     *            the form to submit
     */
    protected HtmlPage submitAndWait(final String form)
    {
        return getAdapter().submitAndWait(form);
    }

    /**
     * Types the given text into the given input field.
     * 
     * @param elementLocator
     *            locator identifying the input field
     * @param text
     *            the text to be typed
     */
    protected HtmlPage type(final String elementLocator, final String text) throws IOException
    {
        return getAdapter().type(elementLocator, text);
    }

    /**
     * Types the given text into the given input field and waits for some activity to complete.
     * 
     * @param elementLocator
     *            locator identifying the input field
     * @param text
     *            the text to be typed
     */
    protected HtmlPage typeAndWait(final String elementLocator, final String text)
    {
        return getAdapter().typeAndWait(elementLocator, text);
    }

    /**
     * Unchecks the given checkbox/radio button.
     * 
     * @param elementLocator
     *            locator identifying the checkbox/radio button
     * @throws IOException
     */
    protected HtmlPage uncheck(final String elementLocator) throws IOException
    {
        return getAdapter().uncheck(elementLocator);
    }

    /**
     * Unchecks the given checkbox/radio button and waits for a load.
     * 
     * @param elementLocator
     *            locator identifying the checkbox/radio button
     * @throws IOException
     */
    protected HtmlPage uncheckAndWait(final String elementLocator) throws IOException
    {
        return getAdapter().uncheckAndWait(elementLocator);
    }

    /**
     * Waits until the value of the attribute identified by the given attribute locator matches the given text pattern.
     * 
     * @param attributeLocator
     *            the attribute locator
     * @param textPattern
     *            the text pattern
     */
    protected HtmlPage waitForAttribute(final String attributeLocator, final String textPattern)
    {
        return getAdapter().waitForAttribute(attributeLocator, textPattern);
    }

    /**
     * Waits until the value of the given element and attribute matches the given text pattern.
     * 
     * @param elementLocator
     *            the element locator
     * @param attributeName
     *            the name of the attribute
     * @param textPattern
     *            the text pattern
     */
    protected HtmlPage waitForAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        return getAdapter().waitForAttribute(elementLocator, attributeName, textPattern);
    }

    /**
     * Waits until the given checkbox/radio button becomes checked.
     * 
     * @param elementLocator
     *            the checkbox/radio button element locator
     */
    protected HtmlPage waitForChecked(final String elementLocator)
    {
        return getAdapter().waitForChecked(elementLocator);
    }

    /**
     * Waits until the given element has the given class(es).
     * 
     * @param elementLocator
     *            the element locator
     * @param clazzString
     *            the class(es) string
     */
    protected HtmlPage waitForClass(final String elementLocator, final String clazzString)
    {
        return getAdapter().waitForClass(elementLocator, clazzString);
    }

    /**
     * Waits until the number of elements found by using the given element locator is equal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    protected HtmlPage waitForElementCount(final String elementLocator, final int count)
    {
        return getAdapter().waitForElementCount(elementLocator, count);
    }

    /**
     * Waits until the number of elements found by using the given element locator is equal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    protected HtmlPage waitForElementCount(final String elementLocator, final String count)
    {
        return getAdapter().waitForElementCount(elementLocator, count);
    }

    /**
     * Waits for the given element to appear.
     * 
     * @param elementLocator
     *            locator identifying the element to wait for
     */
    protected HtmlPage waitForElementPresent(final String elementLocator)
    {
        return getAdapter().waitForElementPresent(elementLocator);
    }

    /**
     * Waits until the result of evaluating the given expression matches the given text pattern.
     * 
     * @param expression
     *            the expression to evaluate
     * @param textPattern
     *            textPattern the text pattern the evaluation result must match
     */
    protected HtmlPage waitForEval(final String expression, final String textPattern)
    {
        return getAdapter().waitForEval(expression, textPattern);
    }

    /**
     * Waits until the value of the attribute identified by the given attribute locator does NOT match the given text
     * pattern.
     * 
     * @param attributeLocator
     *            the attribute locator
     * @param textPattern
     *            the text pattern that must NOT match
     */
    protected HtmlPage waitForNotAttribute(final String attributeLocator, final String textPattern)
    {
        return getAdapter().waitForNotAttribute(attributeLocator, textPattern);
    }

    /**
     * Waits until the value of the given element and attribute does NOT match the given text pattern.
     * 
     * @param elementLocator
     *            the element locator
     * @param attributeName
     *            the name of the attribute
     * @param textPattern
     *            the text pattern
     */
    protected HtmlPage waitForNotAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        return getAdapter().waitForNotAttribute(elementLocator, attributeName, textPattern);
    }

    /**
     * Waits until the given checkbox/radio button becomes unchecked.
     * 
     * @param elementLocator
     *            the checkbox/radio button element locator
     */
    protected HtmlPage waitForNotChecked(final String elementLocator)
    {
        return getAdapter().waitForNotChecked(elementLocator);
    }

    /**
     * Waits until the given element doesn't have the given class(es).
     * 
     * @param elementLocator
     *            the element locator
     * @param clazzString
     *            the class(es) string
     */
    protected HtmlPage waitForNotClass(final String elementLocator, final String clazzString)
    {
        return getAdapter().waitForNotClass(elementLocator, clazzString);
    }

    /**
     * Waits until the number of elements found by using the given element locator is unequal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    protected HtmlPage waitForNotElementCount(final String elementLocator, final int count)
    {
        return getAdapter().waitForNotElementCount(elementLocator, count);
    }

    /**
     * Waits until the number of elements found by using the given element locator is unequal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    protected HtmlPage waitForNotElementCount(final String elementLocator, final String count)
    {
        return getAdapter().waitForNotElementCount(elementLocator, count);
    }

    /**
     * Waits for the given element to disappear.
     * 
     * @param elementLocator
     *            locator identifying the element to disappear
     */
    protected HtmlPage waitForNotElementPresent(final String elementLocator)
    {
        return getAdapter().waitForNotElementPresent(elementLocator);
    }

    /**
     * Waits until the result of evaluating the given expression does NOT match the given text pattern.
     * 
     * @param expression
     *            the expression to evaluate
     * @param textPattern
     *            the text pattern that the evaluation result must NOT match
     */
    protected HtmlPage waitForNotEval(final String expression, final String textPattern)
    {
        return getAdapter().waitForNotEval(expression, textPattern);
    }

    /**
     * Waits until no ID of all selected options of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param idPattern
     *            the ID pattern that must not match
     */
    protected HtmlPage waitForNotSelectedId(final String selectLocator, final String idPattern)
    {
        return getAdapter().waitForNotSelectedId(selectLocator, idPattern);
    }

    /**
     * Waits until the option of the given select element at the given index is not selected.
     * 
     * @param selectLocator
     *            the select element locator
     * @param indexPattern
     *            the option index pattern
     */
    protected HtmlPage waitForNotSelectedIndex(final String selectLocator, final String indexPattern)
    {
        return getAdapter().waitForNotSelectedIndex(selectLocator, indexPattern);
    }

    /**
     * Waits until no label of all selected options of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param labelPattern
     *            the label pattern
     */
    protected HtmlPage waitForNotSelectedLabel(final String selectLocator, final String labelPattern)
    {
        return getAdapter().waitForNotSelectedLabel(selectLocator, labelPattern);
    }

    /**
     * Waits until no value of all selected options of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param valuePattern
     *            the value pattern
     */
    protected HtmlPage waitForNotSelectedValue(final String selectLocator, final String valuePattern)
    {
        return getAdapter().waitForNotSelectedValue(selectLocator, valuePattern);
    }

    /**
     * Waits until the effective style of the element identified by the given element locator does NOT match the given
     * style.
     * 
     * @param elementLocator
     *            the element locator
     * @param styleText
     *            the style that must NOT match (e.g. <code>width: 10px; overflow: hidden;</code>)
     */
    protected HtmlPage waitForNotStyle(final String elementLocator, final String styleText)
    {
        return getAdapter().waitForNotStyle(elementLocator, styleText);
    }

    /**
     * Waits for the given text embedded in the given element to disappear/change.
     * 
     * @param elementLocator
     *            locator identifying the element whose embedded text should change
     * @param text
     *            the text that should change/disappear
     */
    protected HtmlPage waitForNotText(final String elementLocator, final String text)
    {
        return getAdapter().waitForNotText(elementLocator, text);
    }

    /**
     * Waits for the given text to disappear/change.
     * 
     * @param text
     *            the text that should disappear/change
     */
    protected HtmlPage waitForNotTextPresent(final String text)
    {
        return getAdapter().waitForNotTextPresent(text);
    }

    /**
     * Waits for the given title change.
     * 
     * @param title
     *            the title that should change
     */
    protected HtmlPage waitForNotTitle(final String title)
    {
        return getAdapter().waitForNotTitle(title);
    }

    /**
     * Waits for the given value in the given element to disappear/change.
     * 
     * @param elementLocator
     *            locator identifying the element whose value should disappear/change
     * @param value
     *            the value to disappear/change
     */
    protected HtmlPage waitForNotValue(final String elementLocator, final String value)
    {
        return getAdapter().waitForNotValue(elementLocator, value);
    }

    /**
     * Waits for the given element to become invisible.
     * 
     * @param elementLocator
     *            the element locator
     */
    protected HtmlPage waitForNotVisible(final String elementLocator)
    {
        return getAdapter().waitForNotVisible(elementLocator);
    }

    /**
     * Waits for the number of elements matching the given XPath expression change to a different value than the given
     * one.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements currently matching the given XPath expression
     */
    protected HtmlPage waitForNotXpathCount(final String xpath, final int count)
    {
        return getAdapter().waitForNotXpathCount(xpath, count);
    }

    /**
     * Waits for the number of elements matching the given XPath expression change to a different value than the given
     * one.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements currently matching the given XPath expression
     */
    protected HtmlPage waitForNotXpathCount(final String xpath, final String count)
    {
        return getAdapter().waitForNotXpathCount(xpath, count);
    }

    /**
     * Waits for the page to load.
     */
    protected HtmlPage waitForPageToLoad()
    {
        return getAdapter().waitForPageToLoad();
    }

    /**
     * Waits for any pop-up window to be loaded completely.
     */
    protected void waitForPopUp()
    {
        getAdapter().waitForPopUp();
    }

    /**
     * Waits for some pop-up window to be loaded completely.
     * 
     * @param windowID
     *            the ID of the window to wait for
     */
    protected void waitForPopUp(final String windowID)
    {
        getAdapter().waitForPopUp(windowID);
    }

    /**
     * Waits at most the given time for some pop-up window to be loaded completely.
     * 
     * @param windowID
     *            the ID of the window to wait for
     * @param maxWaitingTime
     *            the maximum waiting time
     */
    protected void waitForPopUp(final String windowID, final long maxWaitingTime)
    {
        getAdapter().waitForPopUp(windowID, maxWaitingTime);
    }

    /**
     * Waits at most the given time for some pop-up window to be loaded completely.
     * 
     * @param windowID
     *            the ID of the window to wait for
     * @param maxWaitingTime
     *            the maximum waiting time
     */
    protected void waitForPopUp(final String windowID, final String maxWaitingTime)
    {
        getAdapter().waitForPopUp(windowID, maxWaitingTime);
    }

    /**
     * Waits until the ID of at least one selected option of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param idPattern
     *            the ID pattern
     */
    protected HtmlPage waitForSelectedId(final String selectLocator, final String idPattern)
    {
        return getAdapter().waitForSelectedId(selectLocator, idPattern);
    }

    /**
     * Waits until the option of the given select element at the given index is selected.
     * 
     * @param selectLocator
     *            the select element locator
     * @param indexPattern
     *            the option index pattern
     */
    protected HtmlPage waitForSelectedIndex(final String selectLocator, final String indexPattern)
    {
        return getAdapter().waitForSelectedIndex(selectLocator, indexPattern);
    }

    /**
     * Waits until the label of at least one selected option of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param labelPattern
     *            the label pattern
     */
    protected HtmlPage waitForSelectedLabel(final String selectLocator, final String labelPattern)
    {
        return getAdapter().waitForSelectedLabel(selectLocator, labelPattern);
    }

    /**
     * Waits until the value of at least one selected option of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param valuePattern
     *            the value pattern
     */
    protected HtmlPage waitForSelectedValue(final String selectLocator, final String valuePattern)
    {
        return getAdapter().waitForSelectedValue(selectLocator, valuePattern);
    }

    /**
     * Waits until the effective style of the element identified by the given element locator matches the given style.
     * 
     * @param elementLocator
     *            the element locator
     * @param styleText
     *            the style that must match (e.g. <code>width: 10px; overflow: hidden;</code>)
     */
    protected HtmlPage waitForStyle(final String elementLocator, final String styleText)
    {
        return getAdapter().waitForStyle(elementLocator, styleText);
    }

    /**
     * Waits for the given text embedded in the given element.
     * 
     * @param elementLocator
     *            locator identifying the element whose text should contain the given text
     * @param text
     *            the text to wait for
     */
    protected HtmlPage waitForText(final String elementLocator, final String text)
    {
        return getAdapter().waitForText(elementLocator, text);
    }

    /**
     * Waits for the given text to appear.
     * 
     * @param text
     *            the text to wait for
     */
    protected HtmlPage waitForTextPresent(final String text)
    {
        return getAdapter().waitForTextPresent(text);
    }

    /**
     * Waits for the given title.
     * 
     * @param title
     *            the title to wait for
     */
    protected HtmlPage waitForTitle(final String title)
    {
        return getAdapter().waitForTitle(title);
    }

    /**
     * Waits for the given value in the given element.
     * 
     * @param elementLocator
     *            locator identifying the element whose value should match the given value
     * @param value
     *            the value to wait for
     */
    protected HtmlPage waitForValue(final String elementLocator, final String value)
    {
        return getAdapter().waitForValue(elementLocator, value);
    }

    /**
     * Waits until the given element becomes visible.
     * 
     * @param elementLocator
     *            the element locator
     */
    protected HtmlPage waitForVisible(final String elementLocator)
    {
        return getAdapter().waitForVisible(elementLocator);
    }

    /**
     * Waits for the number of elements matching the given XPath expression is equal to the given count.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements to wait for
     */
    protected HtmlPage waitForXpathCount(final String xpath, final int count)
    {
        return getAdapter().waitForXpathCount(xpath, count);
    }

    /**
     * Waits for the number of elements matching the given XPath expression is equal to the given count.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements to wait for
     */
    protected HtmlPage waitForXpathCount(final String xpath, final String count)
    {
        return getAdapter().waitForXpathCount(xpath, count);
    }

    /**
     * Returns the configured base URL as string.
     * 
     * @return base URL as string
     */
    protected String getBaseUrl()
    {
        return TestContext.getCurrent().getBaseUrl();
    }

    /**
     * Resolves the given string.
     * 
     * @param resolvable
     *            the string to be resolved
     * @return the resolved string
     */
    protected String resolve(final String resolvable)
    {
        return TestContext.getCurrent().resolve(resolvable);
    }

    /**
     * Resolves the given test data key.
     * 
     * @param key
     *            the key string containing only the name of a test data field
     * @return resolved string or <code>null</code> if not found
     */
    protected String resolveKey(final String key)
    {
        return TestContext.getCurrent().resolveKey(key);
    }

    /**
     * Returns whether or not the given expression evaluates to <code>true</code>.
     * 
     * @param jsExpression
     *            the JavaScript expression to evaluate
     * @return <code>true</code> if and only if the given JavaScript expression is not blank and evaluates to
     *         <code>true</code>
     */
    protected boolean evaluatesToTrue(final String jsExpression)
    {
        return getAdapter().evaluatesToTrue(jsExpression);
    }

    private HtmlUnitScriptCommands getAdapter()
    {
        return ((HtmlUnitScriptCommands) TestContext.getCurrent().getAdapter());
    }
}
