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
package com.xceptance.xlt.engine.scripting.webdriver;

import java.net.URL;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.NoSuchElementException;

import com.xceptance.xlt.api.engine.scripting.ScriptCommands;
import com.xceptance.xlt.engine.scripting.util.CommonScriptCommands;

/**
 * The full set of script commands supported for WebDriver-based script test cases. Similar to {@link ScriptCommands},
 * but contains more parameter variants and helper methods.
 * 
 * @see ScriptCommands
 */
public interface WebDriverScriptCommands extends CommonScriptCommands
{
    /**
     * Returns the underlying web driver, which is used to execute the commands.
     * 
     * @return the web driver
     */
    public WebDriver getUnderlyingWebDriver();

    /**
     * Returns the first element matching the given locator
     * 
     * @param elementLocator
     *            the element locator
     * @return first element matching the given locator
     * @throws NoSuchElementException
     *             in case there is no such element
     */
    public WebElement findElement(final String elementLocator);

    /**
     * Returns all elements that match the given locator.
     * 
     * @param elementLocator
     *            the element locator
     * @return all elements that were found for the given locator
     */
    public List<WebElement> findElements(final String elementLocator);

    /**
     * Adds the given option of the given select to the current selection.
     * 
     * @param select
     *            the select
     * @param option
     *            the option to be added to current selection
     */
    public void addSelection(final String select, final String option);

    /**
     * Checks/toggles the given element.
     * 
     * @param elementLocator
     *            locator identifying the element to be checked/toggled
     */
    public void check(final String elementLocator);

    /**
     * Checks/toggles the given element and waits for some activity to complete.
     * 
     * @param elementLocator
     *            locator identifying the element to be checked/toggled
     */
    public void checkAndWait(final String elementLocator);

    /**
     * Clicks the given element.
     * 
     * @param elementLocator
     *            locator identifying the element to be clicked.
     */
    public void click(final String elementLocator);

    /**
     * Clicks the given element and waits for some activity to complete.
     * 
     * @param elementLocator
     *            locator identifying the element to be clicked
     */
    public void clickAndWait(final String elementLocator);

    /**
     * Simulates a right-click on the given element.
     * 
     * @param elementLocator
     *            locator identifying the element to fire events at
     */
    public void contextMenu(final String elementLocator);

    /**
     * Simulates a right-click at the given coordinates (relative to the given element).
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordinates
     *            the coordinates relative to the given element
     */
    public void contextMenuAt(final String elementLocator, final String coordinates);

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
    public void contextMenuAt(final String elementLocator, final int coordX, final int coordY);

    /**
     * Double-clicks the given element.
     * 
     * @param elementLocator
     *            locator identifying the element to be double-clicked
     */
    public void doubleClick(final String elementLocator);

    /**
     * Double-clicks the given element and waits for some activity to complete.
     * 
     * @param elementLocator
     *            locator identifying the element to be double-clicked
     */
    public void doubleClickAndWait(final String elementLocator);

    /**
     * Presses the left mouse button on an element, but does not release the button yet.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    public void mouseDown(final String elementLocator);

    /**
     * Presses the left mouse button at the given coordinates (relative to the given element), but does not release the
     * button yet.
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordinates
     *            the coordinates relative to the given element
     */
    public void mouseDownAt(final String elementLocator, final String coordinates);

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
    public void mouseDownAt(final String elementLocator, final int coordX, final int coordY);

    /**
     * Moves the mouse to the given element.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    public void mouseMove(final String elementLocator);

    /**
     * Moves the mouse to the given coordinates (relative to the given element).
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordinates
     *            the coordinates relative to the given element
     */
    public void mouseMoveAt(final String elementLocator, final String coordinates);

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
    public void mouseMoveAt(final String elementLocator, final int coordX, final int coordY);

    /**
     * Moves the mouse out of the element's bounding box.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    public void mouseOut(final String elementLocator);

    /**
     * Hovers the mouse over an element.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    public void mouseOver(final String elementLocator);

    /**
     * Releases the left mouse button on an element.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    public void mouseUp(final String elementLocator);

    /**
     * Releases the left mouse button at the given coordinates (relative to the given element).
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordinates
     *            the coordinates relative to the given element
     */
    public void mouseUpAt(final String elementLocator, final String coordinates);

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
    public void mouseUpAt(final String elementLocator, final int coordX, final int coordY);

    /**
     * Opens the given URL.
     * 
     * @param pageUrlString
     *            the URL to open
     */
    public void open(final String pageUrlString);

    /**
     * Opens the given URL.
     * 
     * @param url
     *            the target URL
     */
    public void open(final URL url);

    /**
     * Waits the given time.
     * 
     * @param waitingTime
     *            the time in milliseconds to wait
     */
    public void pause(final long waitingTime);

    /**
     * Waits the given time.
     * 
     * @param waitingTime
     *            the time in milliseconds to wait
     */
    public void pause(final String waitingTime);

    /**
     * Removes the given option of the given select from the current selection.
     * 
     * @param select
     *            the select
     * @param option
     *            the option to be removed from the current selection
     */
    public void removeSelection(final String select, final String option);

    /**
     * Selects the given option of the given select.
     * 
     * @param select
     *            the select
     * @param option
     *            the option to select
     */
    public void select(final String select, final String option);

    /**
     * Selects the given option of the given select and waits for some activity to complete.
     * 
     * @param select
     *            the select
     * @param option
     *            the option to select
     */
    public void selectAndWait(final String select, final String option);

    /**
     * Selects the given frame.
     * 
     * @param frameTarget
     *            the frame to be selected
     */
    public void selectFrame(final String frameTarget);

    /**
     * Selects the top-level window.
     */
    public void selectWindow();

    /**
     * Selects the given window.
     * 
     * @param windowTarget
     *            the window to be selected
     */
    public void selectWindow(final String windowTarget);

    /**
     * Submits the given form.
     * 
     * @param form
     *            the form to submit
     */
    public void submit(final String form);

    /**
     * Submits the given form and waits for some activity to complete.
     * 
     * @param form
     *            the form to submit
     */
    public void submitAndWait(final String form);

    /**
     * Types the given text into the given input field.
     * 
     * @param elementLocator
     *            locator identifying the input field
     * @param text
     *            the text to be typed
     */
    public void type(final String elementLocator, final String text);

    /**
     * Types the given text into the given input field and waits for some activity to complete.
     * 
     * @param elementLocator
     *            locator identifying the input field
     * @param text
     *            the text to be typed
     */
    public void typeAndWait(final String elementLocator, final String text);

    /**
     * Unchecks the given checkbox/radio button.
     * 
     * @param elementLocator
     *            locator identifying the checkbox/radio button
     */
    public void uncheck(final String elementLocator);

    /**
     * Unchecks the given checkbox/radio button and waits for a page load.
     * 
     * @param elementLocator
     *            locator identifying the checkbox/radio button
     */
    public void uncheckAndWait(final String elementLocator);

    /**
     * Waits until the value of the attribute identified by the given attribute locator matches the given text pattern.
     * 
     * @param attributeLocator
     *            the attribute locator
     * @param textPattern
     *            the text pattern
     */
    public void waitForAttribute(final String attributeLocator, final String textPattern);

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
    public void waitForAttribute(final String elementLocator, final String attributeName, final String textPattern);

    /**
     * Waits until the given checkbox/radio button becomes checked.
     * 
     * @param elementLocator
     *            the checkbox/radio button element locator
     */
    public void waitForChecked(final String elementLocator);

    /**
     * Waits until the given element has the given class(es).
     * 
     * @param elementLocator
     *            the element locator
     * @param clazzString
     *            the class(es) string
     */
    public void waitForClass(final String elementLocator, final String clazzString);

    /**
     * Waits until the number of elements found by using the given element locator is equal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    public void waitForElementCount(final String elementLocator, final int count);

    /**
     * Waits until the number of elements found by using the given element locator is equal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    public void waitForElementCount(final String elementLocator, final String count);

    /**
     * Waits for the given element to appear.
     * 
     * @param elementLocator
     *            locator identifying the element to wait for
     */
    public void waitForElementPresent(final String elementLocator);

    /**
     * Waits until the result of evaluating the given expression matches the given text pattern.
     * 
     * @param expression
     *            the expression to evaluate
     * @param textPattern
     *            textPattern the text pattern the evaluation result must match
     */
    public void waitForEval(final String expression, final String textPattern);

    /**
     * Waits until the value of the attribute identified by the given attribute locator does NOT match the given text
     * pattern.
     * 
     * @param attributeLocator
     *            the attribute locator
     * @param textPattern
     *            the text pattern that must NOT match
     */
    public void waitForNotAttribute(final String attributeLocator, final String textPattern);

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
    public void waitForNotAttribute(final String elementLocator, final String attributeName, final String textPattern);

    /**
     * Waits until the given checkbox/radio button becomes unchecked.
     * 
     * @param elementLocator
     *            the checkbox/radio button element locator
     */
    public void waitForNotChecked(final String elementLocator);

    /**
     * Waits until the given element doesn't have the given class(es).
     * 
     * @param elementLocator
     *            the element locator
     * @param clazzString
     *            the class(es) string
     */
    public void waitForNotClass(final String elementLocator, final String clazzString);

    /**
     * Waits until the number of elements found by using the given element locator is unequal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    public void waitForNotElementCount(final String elementLocator, final int count);

    /**
     * Waits until the number of elements found by using the given element locator is unequal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    public void waitForNotElementCount(final String elementLocator, final String count);

    /**
     * Waits for the given element to disappear.
     * 
     * @param elementLocator
     *            locator identifying the element to disappear
     */
    public void waitForNotElementPresent(final String elementLocator);

    /**
     * Waits until the result of evaluating the given expression does NOT match the given text pattern.
     * 
     * @param expression
     *            the expression to evaluate
     * @param textPattern
     *            the text pattern that the evaluation result must NOT match
     */
    public void waitForNotEval(final String expression, final String textPattern);

    /**
     * Waits until no ID of all selected options of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param idPattern
     *            the ID pattern
     */
    public void waitForNotSelectedId(final String selectLocator, final String idPattern);

    /**
     * Waits until the option of the given select element at the given index is not selected.
     * 
     * @param selectLocator
     *            the select element locator
     * @param indexPattern
     *            the option index pattern
     */
    public void waitForNotSelectedIndex(final String selectLocator, final String indexPattern);

    /**
     * Waits until no label of all selected options of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param labelPattern
     *            the label pattern
     */
    public void waitForNotSelectedLabel(final String selectLocator, final String labelPattern);

    /**
     * Waits until no value of all selected options of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param valuePattern
     *            the value pattern
     */
    public void waitForNotSelectedValue(final String selectLocator, final String valuePattern);

    /**
     * Waits until the effective style of the element identified by the given element locator does NOT match the given
     * style.
     * 
     * @param elementLocator
     *            the element locator
     * @param styleText
     *            the style that must NOT match (e.g. <code>width: 10px; overflow: hidden;</code>)
     */
    public void waitForNotStyle(final String elementLocator, final String styleText);

    /**
     * Waits for the given text embedded in the given element to disappear/change.
     * 
     * @param elementLocator
     *            locator identifying the element whose embedded text should change
     * @param text
     *            the text that should change/disappear
     */
    public void waitForNotText(final String elementLocator, final String text);

    /**
     * Waits for the given text to disappear/change.
     * 
     * @param text
     *            the text that should disappear/change
     */
    public void waitForNotTextPresent(final String text);

    /**
     * Waits for the given page title change.
     * 
     * @param title
     *            the page title that should change
     */
    public void waitForNotTitle(final String title);

    /**
     * Waits for the given value in the given element to disappear/change.
     * 
     * @param elementLocator
     *            locator identifying the element whose value should change
     * @param value
     *            the value that should change/disappear
     */
    public void waitForNotValue(final String elementLocator, final String value);

    /**
     * Waits until the given element becomes invisible.
     * 
     * @param elementLocator
     *            the element locator
     */
    public void waitForNotVisible(final String elementLocator);

    /**
     * Waits for the number of elements matching the given XPath expression change to a different value than the given
     * one.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements currently matching the given XPath expression
     */
    public void waitForNotXpathCount(final String xpath, final int count);

    /**
     * Waits for the number of elements matching the given XPath expression change to a different value than the given
     * one.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements currently matching the given XPath expression
     */
    public void waitForNotXpathCount(final String xpath, final String count);

    /**
     * Waits for the page to be loaded completely.
     */
    public void waitForPageToLoad();

    /**
     * Waits for any pop-up window to be loaded completely.
     */
    public void waitForPopUp();

    /**
     * Waits for some pop-up window to be loaded completely.
     * 
     * @param windowID
     *            the ID of the window to wait for
     */
    public void waitForPopUp(final String windowID);

    /**
     * Waits at most the given time for some pop-up window to be loaded completely.
     * 
     * @param windowID
     *            the ID of the window to wait for
     * @param maxWaitingTime
     *            the maximum waiting time
     */
    public void waitForPopUp(final String windowID, final long maxWaitingTime);

    /**
     * Waits at most the given time for some pop-up window to be loaded completely.
     * 
     * @param windowID
     *            the ID of the window to wait for
     * @param maxWaitingTime
     *            the maximum waiting time
     */
    public void waitForPopUp(final String windowID, final String maxWaitingTime);

    /**
     * Waits until the ID of at least one selected option of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param idPattern
     *            the ID pattern
     */
    public void waitForSelectedId(final String selectLocator, final String idPattern);

    /**
     * Waits until the option of the given select at the given index is selected.
     * 
     * @param selectLocator
     *            the select element locator
     * @param indexPattern
     *            the option index pattern
     */
    public void waitForSelectedIndex(final String selectLocator, final String indexPattern);

    /**
     * Waits until the label of at least one selected option of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param labelPattern
     *            the label pattern
     */
    public void waitForSelectedLabel(final String selectLocator, final String labelPattern);

    /**
     * Waits until the value of at least one selected option of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param valuePattern
     *            the value pattern
     */
    public void waitForSelectedValue(final String selectLocator, final String valuePattern);

    /**
     * Waits until the effective style of the element identified by the given element locator matches the given style.
     * 
     * @param elementLocator
     *            the element locator
     * @param styleText
     *            the style that must match (e.g. <code>width: 10px; overflow: hidden;</code>)
     */
    public void waitForStyle(final String elementLocator, final String styleText);

    /**
     * Waits for the given text embedded in the given element.
     * 
     * @param elementLocator
     *            locator identifying the element whose text should contain the given text
     * @param text
     *            the text to wait for
     */
    public void waitForText(final String elementLocator, final String text);

    /**
     * Waits for the given text to appear.
     * 
     * @param text
     *            the text to wait for
     */
    public void waitForTextPresent(final String text);

    /**
     * Waits for the given page title.
     * 
     * @param title
     *            the page title to wait for
     */
    public void waitForTitle(final String title);

    /**
     * Waits for the given value in the given element.
     * 
     * @param elementLocator
     *            locator identifying the element whose value should match the given value
     * @param value
     *            the value to wait for
     */
    public void waitForValue(final String elementLocator, final String value);

    /**
     * Waits until the given element becomes visible.
     * 
     * @param elementLocator
     *            the element locator
     */
    public void waitForVisible(final String elementLocator);

    /**
     * Waits for the number of elements matching the given XPath expression is equal to the given count.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements to wait for
     */
    public void waitForXpathCount(final String xpath, final int count);

    /**
     * Waits for the number of elements matching the given XPath expression is equal to the given count.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements to wait for
     */
    public void waitForXpathCount(final String xpath, final String count);
}
