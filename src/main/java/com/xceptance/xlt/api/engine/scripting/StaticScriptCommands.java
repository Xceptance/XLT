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
package com.xceptance.xlt.api.engine.scripting;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.engine.scripting.TestContext;
import com.xceptance.xlt.engine.scripting.webdriver.WebDriverScriptCommands;

/**
 * Static versions of the Selenium-like commands supported by the XLT framework and the script developer. Inherit from
 * this class or import its methods statically to get access to all script commands from any class, not just from
 * subclasses of {@link AbstractWebDriverScriptTestCase} or {@link AbstractWebDriverModule}.
 * <p>
 * This class was introduced mainly to allow for other ways of structuring your reusable code than having to extend
 * {@link AbstractWebDriverModule}. For example, traditionally separate module classes could now be turned into (static)
 * methods of a special helper class.
 * <p>
 * Even though the methods of this class can now be used from everywhere, the scripting engine still needs to be set up
 * correctly before the test and shut down afterwards. That is why for the time being your test case class still needs
 * to be a subclass of {@link AbstractWebDriverScriptTestCase} as it prepares everything as needed.
 * 
 * @see ScriptCommands
 */
public class StaticScriptCommands
{
    /**
     * Adds the given option of the given select to the current selection.
     * 
     * @param select
     *            the select
     * @param option
     *            the option to be added to current selection
     */
    public static void addSelection(final String select, final String option)
    {
        getAdapter().addSelection(select, option);
    }

    /**
     * Asserts that the value of the attribute identified by the given attribute locator matches the given text pattern.
     * 
     * @param attributeLocator
     *            the attribute locator
     * @param textPattern
     *            the text pattern that the attribute value must match
     */
    public static void assertAttribute(final String attributeLocator, final String textPattern)
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
    public static void assertAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        getAdapter().assertAttribute(elementLocator, attributeName, textPattern);
    }

    /**
     * Asserts that the given checkbox/radio button is checked.
     * 
     * @param elementLocator
     *            the checkbox/radio button element locator
     */
    public static void assertChecked(final String elementLocator)
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
    public static void assertClass(final String elementLocator, final String clazzString)
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
    public static void assertElementCount(final String elementLocator, final int count)
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
    public static void assertElementCount(final String elementLocator, final String count)
    {
        getAdapter().assertElementCount(elementLocator, count);
    }

    /**
     * Asserts that the given element is present.
     * 
     * @param elementLocator
     *            locator identifying the element that should be present
     */
    public static void assertElementPresent(final String elementLocator)
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
    public static void assertEval(final String expression, final String textPattern)
    {
        getAdapter().assertEval(expression, textPattern);
    }

    /**
     * Asserts that the time needed to load a page does not exceed the given value.
     * 
     * @param loadTime
     *            maximum load time in milliseconds
     */
    public static void assertLoadTime(final long loadTime)
    {
        getAdapter().assertLoadTime(loadTime);
    }

    /**
     * Asserts that the time needed to load a page does not exceed the given value.
     * 
     * @param loadTime
     *            maximum load time in milliseconds
     */
    public static void assertLoadTime(final String loadTime)
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
    public static void assertNotAttribute(final String attributeLocator, final String textPattern)
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
    public static void assertNotAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        getAdapter().assertNotAttribute(elementLocator, attributeName, textPattern);
    }

    /**
     * Asserts that the given checkbox/radio button is unchecked.
     * 
     * @param elementLocator
     *            the checkbox/radio button element locator
     */
    public static void assertNotChecked(final String elementLocator)
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
    public static void assertNotClass(final String elementLocator, final String clazzString)
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
    public static void assertNotElementCount(final String elementLocator, final int count)
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
    public static void assertNotElementCount(final String elementLocator, final String count)
    {
        getAdapter().assertNotElementCount(elementLocator, count);
    }

    /**
     * Asserts that the given element is not present.
     * 
     * @param elementLocator
     *            locator identifying the element that should be NOT present
     */
    public static void assertNotElementPresent(final String elementLocator)
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
    public static void assertNotEval(final String expression, final String textPattern)
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
    public static void assertNotSelectedId(final String selectLocator, final String idPattern)
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
    public static void assertNotSelectedIndex(final String selectLocator, final String indexPattern)
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
    public static void assertNotSelectedLabel(final String selectLocator, final String labelPattern)
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
    public static void assertNotSelectedValue(final String selectLocator, final String valuePattern)
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
    public static void assertNotStyle(final String elementLocator, final String styleText)
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
    public static void assertNotText(final String elementLocator, final String text)
    {
        getAdapter().assertNotText(elementLocator, text);
    }

    /**
     * Asserts that the given text is not present on the page.
     * 
     * @param text
     *            the text that should NOT be present
     */
    public static void assertNotTextPresent(final String text)
    {
        getAdapter().assertNotTextPresent(text);
    }

    /**
     * Asserts that the page title does not match the given title.
     * 
     * @param title
     *            the title that should not match
     */
    public static void assertNotTitle(final String title)
    {
        getAdapter().assertNotTitle(title);
    }

    /**
     * Asserts that the value of the given element doesn't match the given value. If the element is a &lt;textarea&gt;
     * this method asserts that the containing text doesn't match the given value.
     * 
     * @param elementLocator
     *            locator identifying the element whose value doesn't match the given value
     * @param valuePattern
     *            the value that doesn't match the given element's value
     */
    public static void assertNotValue(final String elementLocator, final String valuePattern)
    {
        getAdapter().assertNotValue(elementLocator, valuePattern);
    }

    /**
     * Asserts that the given element is invisible.
     * 
     * @param elementLocator
     *            the element locator.
     */
    public static void assertNotVisible(final String elementLocator)
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
    public static void assertNotXpathCount(final String xpath, final int count)
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
    public static void assertNotXpathCount(final String xpath, final String count)
    {
        getAdapter().assertNotXpathCount(xpath, count);
    }

    /**
     * Asserts that the size of the actual page (including images etc.) does not exceed the given value.
     * 
     * @param pageSize
     *            the number of bytes the page size must not exceed
     */
    public static void assertPageSize(final long pageSize)
    {
        getAdapter().assertPageSize(pageSize);
    }

    /**
     * Asserts that the size of the actual page (including images etc.) does not exceed the given value.
     * 
     * @param pageSize
     *            the number of bytes the page size must not exceed
     */
    public static void assertPageSize(final String pageSize)
    {
        getAdapter().assertPageSize(pageSize);
    }

    /**
     * Asserts that the ID of at least one selected option of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param idPattern
     *            ID pattern
     */
    public static void assertSelectedId(final String selectLocator, final String idPattern)
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
    public static void assertSelectedIndex(final String selectLocator, final String indexPattern)
    {
        getAdapter().assertSelectedIndex(selectLocator, indexPattern);
    }

    /**
     * Asserts that the label of at least one selected option of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param labelPattern
     *            the label pattern
     */
    public static void assertSelectedLabel(final String selectLocator, final String labelPattern)
    {
        getAdapter().assertSelectedLabel(selectLocator, labelPattern);
    }

    /**
     * Asserts that the value of at least one selected option of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param valuePattern
     *            the value pattern
     */
    public static void assertSelectedValue(final String selectLocator, final String valuePattern)
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
    public static void assertStyle(final String elementLocator, final String styleText)
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
    public static void assertText(final String elementLocator, final String text)
    {
        getAdapter().assertText(elementLocator, text);
    }

    /**
     * Asserts that the given text is present.
     * 
     * @param text
     *            the text that should be present
     */
    public static void assertTextPresent(final String text)
    {
        getAdapter().assertTextPresent(text);
    }

    /**
     * Asserts that the given title matches the page title.
     * 
     * @param title
     *            the title that should match the page title
     */
    public static void assertTitle(final String title)
    {
        getAdapter().assertTitle(title);
    }

    /**
     * Asserts that the value of the given element matches the given value. If the element is a &lt;textarea&gt; this
     * method asserts that the containing text matches the given value.
     * 
     * @param elementLocator
     *            locator identifying the element whose value should match the given value
     * @param valuePattern
     *            the value that should match the given element's value
     */
    public static void assertValue(final String elementLocator, final String valuePattern)
    {
        getAdapter().assertValue(elementLocator, valuePattern);
    }

    /**
     * Asserts that the given element is visible.
     * 
     * @param elementLocator
     *            the element locator
     */
    public static void assertVisible(final String elementLocator)
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
    public static void assertXpathCount(final String xpath, final int count)
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
    public static void assertXpathCount(final String xpath, final String count)
    {
        getAdapter().assertXpathCount(xpath, count);
    }

    /**
     * Checks/toggles the given element.
     * 
     * @param elementLocator
     *            locator identifying the element to be checked/toggled
     */
    public static void check(final String elementLocator)
    {
        getAdapter().check(elementLocator);
    }

    /**
     * Checks/toggles the given element and waits for some activity to complete.
     * 
     * @param elementLocator
     *            locator identifying the element to be checked/toggled
     */
    public static void checkAndWait(final String elementLocator)
    {
        getAdapter().checkAndWait(elementLocator);
    }

    /**
     * Clicks the given element.
     * 
     * @param elementLocator
     *            locator identifying the element to be clicked.
     */
    public static void click(final String elementLocator)
    {
        getAdapter().click(elementLocator);
    }

    /**
     * Clicks the given element and waits for some activity to complete.
     * 
     * @param elementLocator
     *            locator identifying the element to be clicked
     */
    public static void clickAndWait(final String elementLocator)
    {
        getAdapter().clickAndWait(elementLocator);
    }

    /**
     * Closes the browser.
     */
    public static void close()
    {
        getAdapter().close();
    }

    /**
     * Simulates a right-click on the given element.
     * 
     * @param elementLocator
     *            locator identifying the element to fire events at
     */
    public static void contextMenu(String elementLocator)
    {
        getAdapter().contextMenu(elementLocator);
    }

    /**
     * Simulates a right-click at the given coordinates (relative to the given element).
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordinates
     *            the coordinates relative to the given element
     */
    public static void contextMenuAt(String elementLocator, String coordinates)
    {
        getAdapter().contextMenuAt(elementLocator, coordinates);
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
    public static void contextMenuAt(String elementLocator, int coordX, int coordY)
    {
        getAdapter().contextMenuAt(elementLocator, coordX, coordY);
    }

    /**
     * Creates a new cookie. The new cookie will be stored as session cookie for the current path and domain.
     * 
     * @param cookie
     *            name value pair of the new cookie
     */
    public static void createCookie(final String cookie)
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
    public static void createCookie(final String cookie, final String options)
    {
        getAdapter().createCookie(cookie, options);
    }

    /**
     * Removes all cookies visible to the current page.
     */
    public static void deleteAllVisibleCookies()
    {
        getAdapter().deleteAllVisibleCookies();
    }

    /**
     * Removes the cookie with the specified name.
     * 
     * @param name
     *            the cookie's name
     */
    public static void deleteCookie(final String name)
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
    public static void deleteCookie(final String name, final String options)
    {
        getAdapter().deleteCookie(name, options);
    }

    /**
     * Double-clicks the given element.
     * 
     * @param elementLocator
     *            locator identifying the element to be double-clicked
     */
    public static void doubleClick(final String elementLocator)
    {
        getAdapter().doubleClick(elementLocator);
    }

    /**
     * Double-clicks the given element and waits for some activity to complete.
     * 
     * @param elementLocator
     *            locator identifying the element to be double-clicked
     */
    public static void doubleClickAndWait(final String elementLocator)
    {
        getAdapter().doubleClickAndWait(elementLocator);
    }

    /**
     * Prints the given message to the log.
     * 
     * @param message
     *            the message to print
     */
    public static void echo(final String message)
    {
        getAdapter().echo(message);
    }

    /**
     * Presses the left mouse button on an element, but does not release the button yet.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    public static void mouseDown(final String elementLocator)
    {
        getAdapter().mouseDown(elementLocator);
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
    public static void mouseDownAt(final String elementLocator, final String coordinates)
    {
        getAdapter().mouseDownAt(elementLocator, coordinates);
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
    public static void mouseDownAt(final String elementLocator, final int coordX, final int coordY)
    {
        getAdapter().mouseDownAt(elementLocator, coordX, coordY);
    }

    /**
     * Moves the mouse to the given element.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    public static void mouseMove(final String elementLocator)
    {
        getAdapter().mouseMove(elementLocator);
    }

    /**
     * Moves the mouse to the given coordinates (relative to the given element).
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordinates
     *            the coordinates relative to the given element
     */
    public static void mouseMoveAt(final String elementLocator, final String coordinates)
    {
        getAdapter().mouseMoveAt(elementLocator, coordinates);
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
    public static void mouseMoveAt(final String elementLocator, final int coordX, final int coordY)
    {
        getAdapter().mouseMoveAt(elementLocator, coordX, coordY);
    }

    /**
     * Moves the mouse out of the element's bounding box.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    public static void mouseOut(final String elementLocator)
    {
        getAdapter().mouseOut(elementLocator);
    }

    /**
     * Hovers the mouse over an element.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    public static void mouseOver(final String elementLocator)
    {
        getAdapter().mouseOver(elementLocator);
    }

    /**
     * Releases the left mouse button on an element.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    public static void mouseUp(final String elementLocator)
    {
        getAdapter().mouseUp(elementLocator);
    }

    /**
     * Releases the left mouse button at the given coordinates (relative to the given element).
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordinates
     *            the coordinates relative to the given element
     */
    public static void mouseUpAt(final String elementLocator, final String coordinates)
    {
        getAdapter().mouseUpAt(elementLocator, coordinates);
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
    public static void mouseUpAt(final String elementLocator, final int coordX, final int coordY)
    {
        getAdapter().mouseUpAt(elementLocator, coordX, coordY);
    }

    /**
     * Opens the given URL.
     * 
     * @param pageUrlString
     *            the URL to open
     */
    public static void open(final String pageUrlString)
    {
        getAdapter().open(pageUrlString);
    }

    /**
     * Waits the given time.
     * 
     * @param waitingTime
     *            the time in milliseconds to wait
     */
    public static void pause(final long waitingTime)
    {
        getAdapter().pause(waitingTime);
    }

    /**
     * Waits the given time.
     * 
     * @param waitingTime
     *            the time in milliseconds to wait
     */
    public static void pause(final String waitingTime)
    {
        getAdapter().pause(waitingTime);
    }

    /**
     * Removes the given option of the given select from the current selection.
     * 
     * @param select
     *            the select
     * @param option
     *            the option to be removed from the current selection
     */
    public static void removeSelection(final String select, final String option)
    {
        getAdapter().removeSelection(select, option);
    }

    /**
     * Selects the given option of the given select.
     * 
     * @param select
     *            the select
     * @param option
     *            the option to select
     */
    public static void select(final String select, final String option)
    {
        getAdapter().select(select, option);
    }

    /**
     * Selects the given option of the given select and waits for some activity to complete.
     * 
     * @param select
     *            the select
     * @param option
     *            the option to select
     */
    public static void selectAndWait(final String select, final String option)
    {
        getAdapter().selectAndWait(select, option);
    }

    /**
     * Selects the given frame.
     * 
     * @param frameTarget
     *            the frame to be selected
     */
    public static void selectFrame(final String frameTarget)
    {
        getAdapter().selectFrame(frameTarget);
    }

    /**
     * Selects the top-level window.
     */
    public static void selectWindow()
    {
        getAdapter().selectWindow();
    }

    /**
     * Selects the given window.
     * 
     * @param windowTarget
     *            the window to be selected
     */
    public static void selectWindow(final String windowTarget)
    {
        getAdapter().selectWindow(windowTarget);
    }

    /**
     * Sets the timeout to the given value.
     * 
     * @param timeout
     *            the new timeout in milliseconds
     */
    public static void setTimeout(final long timeout)
    {
        getAdapter().setTimeout(timeout);
    }

    /**
     * Sets the timeout to the given value.
     * 
     * @param timeout
     *            the new timeout in milliseconds
     */
    public static void setTimeout(final String timeout)
    {
        getAdapter().setTimeout(timeout);
    }

    /**
     * Starts a new action using the given name.
     * 
     * @param actionName
     *            the name of the action
     */
    public static void startAction(final String actionName)
    {
        Session.getCurrent().startAction(actionName);
    }

    /**
     * Stores the given text to the given variable.
     * 
     * @param text
     *            the text to store
     * @param variableName
     *            the variable name
     */
    public static void store(final String text, final String variableName)
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
    public static void storeAttribute(final String attributeLocator, final String variableName)
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
    public static void storeAttribute(final String elementLocator, final String attributeName, final String variableName)
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
    public static void storeElementCount(final String elementLocator, final String variableName)
    {
        getAdapter().storeElementCount(elementLocator, variableName);
    }

    /**
     * Stores the result of evaluating the given expression to the given variable.
     * 
     * @param expression
     *            the expression to evaluate
     * @param variableName
     *            the variable name
     */
    public static void storeEval(final String expression, final String variableName)
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
    public static void storeText(final String elementLocator, final String variableName)
    {
        getAdapter().storeText(elementLocator, variableName);
    }

    /**
     * Stores the title of the currently active document to the given variable.
     * 
     * @param variableName
     *            the variable
     */
    public static void storeTitle(final String variableName)
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
    public static void storeValue(final String elementLocator, final String variableName)
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
    public static void storeXpathCount(final String xpath, final String variableName)
    {
        getAdapter().storeXpathCount(xpath, variableName);
    }

    /**
     * Submits the given form.
     * 
     * @param form
     *            the form to submit
     */
    public static void submit(final String form)
    {
        getAdapter().submit(form);
    }

    /**
     * Submits the given form and waits for some activity to complete.
     * 
     * @param form
     *            the form to submit
     */
    public static void submitAndWait(final String form)
    {
        getAdapter().submitAndWait(form);
    }

    /**
     * Types the given text into the given input field.
     * 
     * @param elementLocator
     *            locator identifying the input field
     * @param text
     *            the text to be typed
     */
    public static void type(final String elementLocator, final String text)
    {
        getAdapter().type(elementLocator, text);
    }

    /**
     * Types the given text into the given input field and waits for some activity to complete.
     * 
     * @param elementLocator
     *            locator identifying the input field
     * @param text
     *            the text to be typed
     */
    public static void typeAndWait(final String elementLocator, final String text)
    {
        getAdapter().typeAndWait(elementLocator, text);
    }

    /**
     * Unchecks the given checkbox/radio button.
     * 
     * @param elementLocator
     *            locator identifying the checkbox/radio button
     */
    public static void uncheck(final String elementLocator)
    {
        getAdapter().uncheck(elementLocator);
    }

    /**
     * Unchecks the given checkbox/radio button and waits for a page load.
     * 
     * @param elementLocator
     *            locator identifying the checkbox/radio button
     */
    public static void uncheckAndWait(final String elementLocator)
    {
        getAdapter().uncheckAndWait(elementLocator);
    }

    /**
     * Waits until the value of the attribute identified by the given attribute locator matches the given text pattern.
     * 
     * @param attributeLocator
     *            the attribute locator
     * @param textPattern
     *            the text pattern
     */
    public static void waitForAttribute(final String attributeLocator, final String textPattern)
    {
        getAdapter().waitForAttribute(attributeLocator, textPattern);
    }

    /**
     * Waits until the value of the attribute identified by the given attribute locator matches the given text pattern.
     * 
     * @param elementLocator
     *            the element locator
     * @param attributeName
     *            the name of the attribute
     * @param textPattern
     *            the text pattern
     */
    public static void waitForAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        getAdapter().waitForAttribute(elementLocator, attributeName, textPattern);
    }

    /**
     * Waits until the given checkbox/radio button becomes checked.
     * 
     * @param elementLocator
     *            the checkbox/radio button element locator
     */
    public static void waitForChecked(final String elementLocator)
    {
        getAdapter().waitForChecked(elementLocator);
    }

    /**
     * Waits until the given element has the given class(es).
     * 
     * @param elementLocator
     *            the element locator
     * @param clazzString
     *            the class(es) string
     */
    public static void waitForClass(final String elementLocator, final String clazzString)
    {
        getAdapter().waitForClass(elementLocator, clazzString);
    }

    /**
     * Waits until the number of elements found by using the given element locator is equal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    public static void waitForElementCount(final String elementLocator, final int count)
    {
        getAdapter().waitForElementCount(elementLocator, count);
    }

    /**
     * Waits until the number of elements found by using the given element locator is equal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    public static void waitForElementCount(final String elementLocator, final String count)
    {
        getAdapter().waitForElementCount(elementLocator, count);
    }

    /**
     * Waits for the given element to appear.
     * 
     * @param elementLocator
     *            locator identifying the element to wait for
     */
    public static void waitForElementPresent(final String elementLocator)
    {
        getAdapter().waitForElementPresent(elementLocator);
    }

    /**
     * Waits until the result of evaluating the given expression matches the given text pattern.
     * 
     * @param expression
     *            the expression to evaluate
     * @param textPattern
     *            textPattern the text pattern the evaluation result must match
     */
    public static void waitForEval(final String expression, final String textPattern)
    {
        getAdapter().waitForEval(expression, textPattern);
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
    public static void waitForNotAttribute(final String attributeLocator, final String textPattern)
    {
        getAdapter().waitForNotAttribute(attributeLocator, textPattern);
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
    public static void waitForNotAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        getAdapter().waitForNotAttribute(elementLocator, attributeName, textPattern);
    }

    /**
     * Waits until the given checkbox/radio button becomes unchecked.
     * 
     * @param elementLocator
     *            the checkbox/radio button element locator
     */
    public static void waitForNotChecked(final String elementLocator)
    {
        getAdapter().waitForNotChecked(elementLocator);
    }

    /**
     * Waits until the given element doesn't have the given class(es).
     * 
     * @param elementLocator
     *            the element locator
     * @param clazzString
     *            the class(es) string
     */
    public static void waitForNotClass(final String elementLocator, final String clazzString)
    {
        getAdapter().waitForNotClass(elementLocator, clazzString);
    }

    /**
     * Waits until the number of elements found by using the given element locator is unequal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    public static void waitForNotElementCount(final String elementLocator, final int count)
    {
        getAdapter().waitForNotElementCount(elementLocator, count);
    }

    /**
     * Waits until the number of elements found by using the given element locator is unequal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    public static void waitForNotElementCount(final String elementLocator, final String count)
    {
        getAdapter().waitForNotElementCount(elementLocator, count);
    }

    /**
     * Waits for the given element to disappear.
     * 
     * @param elementLocator
     *            locator identifying the element to disappear
     */
    public static void waitForNotElementPresent(final String elementLocator)
    {
        getAdapter().waitForNotElementPresent(elementLocator);
    }

    /**
     * Waits until the result of evaluating the given expression does NOT match the given text pattern.
     * 
     * @param expression
     *            the expression to evaluate
     * @param textPattern
     *            the text pattern that the evaluation result must NOT match
     */
    public static void waitForNotEval(final String expression, final String textPattern)
    {
        getAdapter().waitForNotEval(expression, textPattern);
    }

    /**
     * Waits until no ID of all selected options of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param idPattern
     *            the ID pattern
     */
    public static void waitForNotSelectedId(final String selectLocator, final String idPattern)
    {
        getAdapter().waitForNotSelectedId(selectLocator, idPattern);
    }

    /**
     * Waits until the option of the given select element at the given index is not selected.
     * 
     * @param selectLocator
     *            the select element locator
     * @param indexPattern
     *            the option index pattern
     */
    public static void waitForNotSelectedIndex(final String selectLocator, final String indexPattern)
    {
        getAdapter().waitForNotSelectedIndex(selectLocator, indexPattern);
    }

    /**
     * Waits until no label of all selected options of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param labelPattern
     *            the label pattern
     */
    public static void waitForNotSelectedLabel(final String selectLocator, final String labelPattern)
    {
        getAdapter().waitForNotSelectedLabel(selectLocator, labelPattern);
    }

    /**
     * Waits until no value of all selected options of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param valuePattern
     *            the value pattern
     */
    public static void waitForNotSelectedValue(final String selectLocator, final String valuePattern)
    {
        getAdapter().waitForNotSelectedValue(selectLocator, valuePattern);
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
    public static void waitForNotStyle(final String elementLocator, final String styleText)
    {
        getAdapter().waitForNotStyle(elementLocator, styleText);
    }

    /**
     * Waits for the given text embedded in the given element to disappear/change.
     * 
     * @param elementLocator
     *            locator identifying the element whose embedded text should change
     * @param text
     *            the text that should change/disappear
     */
    public static void waitForNotText(final String elementLocator, final String text)
    {
        getAdapter().waitForNotText(elementLocator, text);
    }

    /**
     * Waits for the given text to disappear/change.
     * 
     * @param text
     *            the text that should disappear/change
     */
    public static void waitForNotTextPresent(final String text)
    {
        getAdapter().waitForNotTextPresent(text);
    }

    /**
     * Waits for the given page title change.
     * 
     * @param title
     *            the page title that should change
     */
    public static void waitForNotTitle(final String title)
    {
        getAdapter().waitForNotTitle(title);
    }

    /**
     * Waits for the given value in the given element to disappear/change.
     * 
     * @param elementLocator
     *            locator identifying the element whose value should change
     * @param value
     *            the value that should change/disappear
     */
    public static void waitForNotValue(final String elementLocator, final String value)
    {
        getAdapter().waitForNotValue(elementLocator, value);
    }

    /**
     * Waits until the given element becomes invisible.
     * 
     * @param elementLocator
     *            the element locator
     */
    public static void waitForNotVisible(final String elementLocator)
    {
        getAdapter().waitForNotVisible(elementLocator);
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
    public static void waitForNotXpathCount(final String xpath, final int count)
    {
        getAdapter().waitForNotXpathCount(xpath, count);
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
    public static void waitForNotXpathCount(final String xpath, final String count)
    {
        getAdapter().waitForNotXpathCount(xpath, count);
    }

    /**
     * Waits for the page to be loaded completely.
     */
    public static void waitForPageToLoad()
    {
        getAdapter().waitForPageToLoad();
    }

    /**
     * Waits for any pop-up window to be loaded completely.
     */
    public static void waitForPopUp()
    {
        getAdapter().waitForPopUp();
    }

    /**
     * Waits for some pop-up window to be loaded completely.
     * 
     * @param windowID
     *            the ID of the window to wait for
     */
    public static void waitForPopUp(final String windowID)
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
    public static void waitForPopUp(final String windowID, final long maxWaitingTime)
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
    public static void waitForPopUp(final String windowID, final String maxWaitingTime)
    {
        getAdapter().waitForPopUp(windowID, maxWaitingTime);
    }

    /**
     * Waits until the ID of at least one selected option of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param idPattern
     *            the ID pattern
     */
    public static void waitForSelectedId(final String selectLocator, final String idPattern)
    {
        getAdapter().waitForSelectedId(selectLocator, idPattern);
    }

    /**
     * Waits until the option of the given select at the given index is selected.
     * 
     * @param selectLocator
     *            the select element locator
     * @param indexPattern
     *            the option index pattern
     */
    public static void waitForSelectedIndex(final String selectLocator, final String indexPattern)
    {
        getAdapter().waitForSelectedIndex(selectLocator, indexPattern);
    }

    /**
     * Waits until the label of at least one selected option of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param labelPattern
     *            the label pattern
     */
    public static void waitForSelectedLabel(final String selectLocator, final String labelPattern)
    {
        getAdapter().waitForSelectedLabel(selectLocator, labelPattern);
    }

    /**
     * Waits until the value of at least one selected option of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param valuePattern
     *            the value pattern
     */
    public static void waitForSelectedValue(final String selectLocator, final String valuePattern)
    {
        getAdapter().waitForSelectedValue(selectLocator, valuePattern);
    }

    /**
     * Waits until the effective style of the element identified by the given element locator matches the given style.
     * 
     * @param elementLocator
     *            the element locator
     * @param styleText
     *            the style that must match (e.g. <code>width: 10px; overflow: hidden;</code>)
     */
    public static void waitForStyle(final String elementLocator, final String styleText)
    {
        getAdapter().waitForStyle(elementLocator, styleText);
    }

    /**
     * Waits for the given text embedded in the given element.
     * 
     * @param elementLocator
     *            locator identifying the element whose text should contain the given text
     * @param text
     *            the text to wait for
     */
    public static void waitForText(final String elementLocator, final String text)
    {
        getAdapter().waitForText(elementLocator, text);
    }

    /**
     * Waits for the given text to appear.
     * 
     * @param text
     *            the text to wait for
     */
    public static void waitForTextPresent(final String text)
    {
        getAdapter().waitForTextPresent(text);
    }

    /**
     * Waits for the given page title.
     * 
     * @param title
     *            the page title to wait for
     */
    public static void waitForTitle(final String title)
    {
        getAdapter().waitForTitle(title);
    }

    /**
     * Waits for the given value in the given element.
     * 
     * @param elementLocator
     *            locator identifying the element whose value should match the given value
     * @param value
     *            the value to wait for
     */
    public static void waitForValue(final String elementLocator, final String value)
    {
        getAdapter().waitForValue(elementLocator, value);
    }

    /**
     * Waits until the given element becomes visible.
     * 
     * @param elementLocator
     *            the element locator
     */
    public static void waitForVisible(final String elementLocator)
    {
        getAdapter().waitForVisible(elementLocator);
    }

    /**
     * Waits for the number of elements matching the given XPath expression is equal to the given count.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements to wait for
     */
    public static void waitForXpathCount(final String xpath, final int count)
    {
        getAdapter().waitForXpathCount(xpath, count);
    }

    /**
     * Waits for the number of elements matching the given XPath expression is equal to the given count.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements to wait for
     */
    public static void waitForXpathCount(final String xpath, final String count)
    {
        getAdapter().waitForXpathCount(xpath, count);
    }

    // --- Additional methods ---

    /**
     * Returns whether or not the given expression evaluates to <code>true</code>.
     * 
     * @param jsExpression
     *            the JavaScript expression to evaluate
     * @return <code>true</code> if and only if the given JavaScript expression is not blank and evaluates to
     *         <code>true</code>
     */
    public static boolean evaluatesToTrue(final String jsExpression)
    {
        return getAdapter().evaluatesToTrue(jsExpression);
    }

    /**
     * Returns the webdriver instance.
     * 
     * @return webdriver instance
     */
    public static final WebDriver getWebDriver()
    {
        return getAdapter().getUnderlyingWebDriver();
    }

    /**
     * Resolves the given string.
     * 
     * @param resolvable
     *            the resolvable string containing one or more test data placeholders
     * @return resolved string
     */
    public static String resolve(final String resolvable)
    {
        return TestContext.getCurrent().resolve(resolvable);
    }

    /**
     * Resolves the given test data key
     * 
     * @param key
     *            the key string containing only the name of a test data field
     * @return resolved string or <code>null</code> if not found
     */
    public static String resolveKey(final String key)
    {
        return TestContext.getCurrent().resolveKey(key);
    }

    /**
     * Stops the current action.
     * 
     * @see #startAction(String)
     */
    public static void stopAction()
    {
        Session.getCurrent().stopAction();
    }

    /**
     * Returns the first element matching the given locator.
     * 
     * @param elementLocator
     *            the element locator
     * @return first element matching the given locator
     */
    public static WebElement findElement(String elementLocator)
    {
        return getAdapter().findElement(elementLocator);
    }

    /**
     * Returns all elements that match the given locator.
     * 
     * @param elementLocator
     *            the element locator
     * @return all elements that were found for the given locator
     */
    public static List<WebElement> findElements(String elementLocator)
    {
        return getAdapter().findElements(elementLocator);
    }

    /**
     * Returns the result of evaluating the given JavaScript expression.
     * 
     * @param jsExpression
     *            the JavaScript expression to evaluate
     * @return result of evaluation
     */
    public static String evaluate(String jsExpression)
    {
        return getAdapter().evaluate(jsExpression);
    }

    /**
     * Returns the value of the given element attribute locator.
     * 
     * @param attributeLocator
     *            the element attribute locator
     * @return value of attribute specified by given element attribute locator
     */
    public static String getAttribute(final String attributeLocator)
    {
        return getAdapter().getAttribute(attributeLocator);
    }

    /**
     * Returns the value of the given element and attribute.
     * 
     * @param elementLocator
     *            the element locator
     * @param attributeName
     *            the name of the attribute
     * @return value of given element attribute locator
     */
    public static String getAttribute(final String elementLocator, final String attributeName)
    {
        return getAdapter().getAttribute(elementLocator, attributeName);
    }

    /**
     * Returns the number of matching elements.
     * 
     * @param elementLocator
     *            the element locator
     * @return number of elements matching the given locator
     */
    public static int getElementCount(String elementLocator)
    {
        return getAdapter().getElementCount(elementLocator);
    }

    /**
     * Returns the (visible) text of the current page.
     * 
     * @return the page's (visible) text
     */
    public static String getPageText()
    {
        return getAdapter().getPageText();
    }

    /**
     * Returns the (visible) text of the given element. If the element is not visible, the empty string is returned.
     * 
     * @param elementLocator
     *            the element locator
     * @return the element's (visible) text
     */
    public static String getText(String elementLocator)
    {
        return getAdapter().getText(elementLocator);
    }

    /**
     * Returns the title of the current page.
     * 
     * @return page title
     */
    public static String getTitle()
    {
        return getAdapter().getTitle();
    }

    /**
     * Returns the value of the given element. If the element doesn't have a value, the empty string is returned.
     * 
     * @param elementLocator
     *            the element locator
     * @return the element's value
     */
    public static String getValue(String elementLocator)
    {
        return getAdapter().getValue(elementLocator);
    }

    /**
     * Returns the number of elements matching the given XPath expression.
     * 
     * @param xpath
     *            the XPath expression
     * @return number of matching elements
     */
    public static int getXpathCount(String xpath)
    {
        return getAdapter().getXpathCount(xpath);
    }

    /**
     * Returns whether or not the value of the attribute identified by the given attribute locator matches the given
     * text pattern.
     * 
     * @param attributeLocator
     *            the attribute locator
     * @param textPattern
     *            the text pattern
     * @return <code>true</code> if the attribute value matches the given pattern, <code>false</code> otherwise
     */
    public static boolean hasAttribute(final String attributeLocator, final String textPattern)
    {
        return getAdapter().hasAttribute(attributeLocator, textPattern);
    }

    /**
     * Returns whether or not the value of the given element and attribute matches the given text pattern.
     * 
     * @param elementLocator
     *            the element locator
     * @param attributeName
     *            the name of the attribute
     * @param textPattern
     *            the text pattern
     * @return <code>true</code> if the attribute value matches the given pattern, <code>false</code> otherwise
     */
    public static boolean hasAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        return getAdapter().hasAttribute(elementLocator, attributeName, textPattern);
    }

    /**
     * Returns whether or not the given element has the given class(es).
     * 
     * @param elementLocator
     *            the element locator
     * @param clazz
     *            the class string (multiple CSS classes separated by whitespace)
     * @return <code>true</code> if the element's class attribute contains all of the given class(es),
     *         <code>false</code> otherwise
     */
    public static boolean hasClass(final String elementLocator, final String clazz)
    {
        return getAdapter().hasClass(elementLocator, clazz);
    }

    /**
     * Returns whether or not the given element doesn't have the given class(es); that is, its class attribute doesn't
     * contain any of the given class(es).
     * 
     * @param elementLocator
     *            the element locator
     * @param clazz
     *            the class string (multiple CSS classes separated by whitespace)
     * @return <code>true</code> if the element's class attribute does not contains any of the given class(es),
     *         <code>false</code> otherwise
     */
    public static boolean hasNotClass(final String elementLocator, final String clazz)
    {
        return getAdapter().hasNotClass(elementLocator, clazz);
    }

    /**
     * Returns whether or not the given element doesn't have the given style; that is, none of the given CSS properties
     * must match the element's actual style.
     * 
     * @param elementLocator
     *            the element locator
     * @param style
     *            the CSS style text to check (e.g. <code>width: 10px; overflow: hidden;</code>)
     * @return <code>true</code> if NONE of the given CSS properties match the element's actual style,
     *         <code>false</code> otherwise
     */
    public static boolean hasNotStyle(final String elementLocator, final String style)
    {
        return getAdapter().hasNotStyle(elementLocator, style);
    }

    /**
     * Returns whether or not the given element has the given style; that is, all of the given CSS properties must match
     * the element's actual style.
     * 
     * @param elementLocator
     *            the element locator
     * @param style
     *            the CSS style text to check (e.g. <code>width: 10px; overflow: hidden;</code>)
     * @return <code>true</code> if ALL of the given CSS properties match the elements actual style, <code>false</code>
     *         otherwise
     */
    public static boolean hasStyle(final String elementLocator, final String style)
    {
        return getAdapter().hasStyle(elementLocator, style);
    }

    /**
     * Checks that the text embedded by the given element contains the given text.
     * 
     * @param elementLocator
     *            locator identifying the element whose text should contain the given text
     * @param textPattern
     *            the text that should be embedded in the given element
     * @return <code>true</code> the text embedded by the given element contains the given text, <code>false</code>
     *         otherwise
     */
    public static boolean hasText(final String elementLocator, final String textPattern)
    {
        return getAdapter().hasText(elementLocator, textPattern);
    }

    /**
     * Checks that the given title matches the page title.
     * 
     * @param title
     *            the title that should match the page title
     * @return <code>true</code> if the given title matches the page title, <code>false</code> otherwise
     */
    public static boolean hasTitle(final String title)
    {
        return getAdapter().hasTitle(title);
    }

    /**
     * Checks that the value of the given element matches the given value. If the element is a &lt;textarea&gt; this
     * method checks that the containing text matches the given value.
     * 
     * @param elementLocator
     *            locator identifying the element whose value should match the given value
     * @param valuePattern
     *            the value that should match the given element's value
     * @return <code>true</code> if the value of the given element matches the given value, <code>false</code> otherwise
     */
    public static boolean hasValue(final String elementLocator, final String valuePattern)
    {
        return getAdapter().hasValue(elementLocator, valuePattern);
    }

    /**
     * Returns whether or not the element identified by the given element locator is checked.
     * 
     * @param elementLocator
     *            the element locator
     * @return <code>true</code> if the element identified by the given element locator is checked, <code>false</code>
     *         otherwise
     */
    public static boolean isChecked(String elementLocator)
    {
        return getAdapter().isChecked(elementLocator);
    }

    /**
     * Returns whether or not there is an element for the given locator.
     * 
     * @param elementLocator
     *            the element locator
     * @return <code>true</code> if there at least one element has been found for the given locator, <code>false</code>
     *         otherwise
     */
    public static boolean isElementPresent(String elementLocator)
    {
        return getAdapter().isElementPresent(elementLocator);
    }

    /**
     * Returns whether or not the given element is enabled.
     * 
     * @param elementLocator
     *            the element locator
     * @return <code>true</code> if the element identified by the given element locator is enabled, <code>false</code>
     *         otherwise
     */
    public static boolean isEnabled(String elementLocator)
    {
        return getAdapter().isEnabled(elementLocator);
    }

    /**
     * Returns whether or not the result of evaluating the given expression matches the given text pattern.
     * 
     * @param expression
     *            the expression to evaluate
     * @param textPattern
     *            the text pattern
     * @return <code>true</code> if the evaluation result matches the given pattern, <code>false</code> otherwise
     */
    public static boolean isEvalMatching(final String expression, final String textPattern)
    {
        return getAdapter().isEvalMatching(expression, textPattern);
    }

    /**
     * Checks that the given text is present.
     * 
     * @param textPattern
     *            the text that should be present
     * @return <code>true</code> if the given text is present, <code>false</code> otherwise
     */
    public static boolean isTextPresent(final String textPattern)
    {
        return getAdapter().isTextPresent(textPattern);
    }

    /**
     * Returns whether or not the given element is visible.
     * 
     * @param elementLocator
     *            the element locator
     * @return <code>true</code> if element was found and is visible, <code>false</code> otherwise
     */
    public static boolean isVisible(String elementLocator)
    {
        return getAdapter().isVisible(elementLocator);
    }

    /**
     * Returns the underlying {@link WebDriverScriptCommands} object.
     * 
     * @return the commands implementation
     */
    private static WebDriverScriptCommands getAdapter()
    {
        final WebDriverScriptCommands adapter = (WebDriverScriptCommands) TestContext.getCurrent().getAdapter();

        if (adapter == null)
        {
            throw new IllegalStateException("Scripting engine not initialized. Make sure your test case inherits from " +
                                            AbstractWebDriverScriptTestCase.class.getName() + ".");
        }

        return adapter;
    }
}
