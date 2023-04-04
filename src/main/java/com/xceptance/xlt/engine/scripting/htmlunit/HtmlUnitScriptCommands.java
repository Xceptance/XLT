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
package com.xceptance.xlt.engine.scripting.htmlunit;

import java.io.IOException;
import java.net.URL;

import org.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.ScriptCommands;
import com.xceptance.xlt.engine.scripting.util.CommonScriptCommands;

/**
 * The full set of script commands supported for HtmlUnit-based script test cases. Similar to {@link ScriptCommands},
 * but contains more parameter variants and helper methods.
 */
public interface HtmlUnitScriptCommands extends CommonScriptCommands
{
    /**
     * Adds the given option of the given select to the current selection.
     * 
     * @param select
     *            the select
     * @param option
     *            the option to be added to current selection
     */
    public HtmlPage addSelection(final String select, final String option);

    /**
     * Checks/toggles the given element.
     * 
     * @param elementLocator
     *            locator identifying the element to be checked/toggled
     */
    public HtmlPage check(final String elementLocator) throws IOException;

    /**
     * Checks/toggles the given element and waits for some activity to complete.
     * 
     * @param elementLocator
     *            locator identifying the element to be checked/toggled
     */
    public HtmlPage checkAndWait(final String elementLocator) throws IOException;

    /**
     * Clicks the given element.
     * 
     * @param elementLocator
     *            locator identifying the element to be clicked.
     */
    public HtmlPage click(final String elementLocator) throws IOException;

    /**
     * Clicks the given element and waits for some activity to complete.
     * 
     * @param elementLocator
     *            locator identifying the element to be clicked
     */
    public HtmlPage clickAndWait(final String elementLocator) throws IOException;

    /**
     * Simulates a right-click on the given element.
     * 
     * @param elementLocator
     *            locator identifying the element to fire events at
     */
    public HtmlPage contextMenu(final String elementLocator);

    /**
     * Simulates a right-click at the given coordinates (relative to the given element).
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordinates
     *            the coordinates relative to the given element
     */
    public HtmlPage contextMenuAt(final String elementLocator, final String coordinates);

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
    public HtmlPage contextMenuAt(final String elementLocator, final int coordX, final int coordY);

    /**
     * Double-clicks the given element.
     * 
     * @param elementLocator
     *            locator identifying the element to be double-clicked
     */
    public HtmlPage doubleClick(final String elementLocator) throws IOException;

    /**
     * Double-clicks the given element and waits for some activity to complete.
     * 
     * @param elementLocator
     *            locator identifying the element to be double-clicked
     */
    public HtmlPage doubleClickAndWait(final String elementLocator) throws IOException;

    /**
     * Presses the left mouse button on an element, but does not release the button yet.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    public HtmlPage mouseDown(final String elementLocator);

    /**
     * Presses the left mouse button at the given coordinates (relative to the given element), but does not release the
     * button yet.
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordinates
     *            the coordinates relative to the given element
     */
    public HtmlPage mouseDownAt(final String elementLocator, final String coordinates);

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
    public HtmlPage mouseDownAt(final String elementLocator, final int coordX, final int coordY);

    /**
     * Moves the mouse to the given element.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    public HtmlPage mouseMove(final String elementLocator);

    /**
     * Moves the mouse to the given coordinates (relative to the given element).
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordinates
     *            the coordinates relative to the given element
     */
    public HtmlPage mouseMoveAt(final String elementLocator, final String coordinates);

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
    public HtmlPage mouseMoveAt(final String elementLocator, final int coordX, final int coordY);

    /**
     * Moves the mouse out of the element's bounding box.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    public HtmlPage mouseOut(final String elementLocator);

    /**
     * Hovers the mouse over an element.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    public HtmlPage mouseOver(final String elementLocator);

    /**
     * Releases the left mouse button on an element.
     * 
     * @param elementLocator
     *            locator identifying the target element
     */
    public HtmlPage mouseUp(final String elementLocator);

    /**
     * Releases the left mouse button at the given coordinates (relative to the given element).
     * 
     * @param elementLocator
     *            locator identifying the target element
     * @param coordinates
     *            the coordinates relative to the given element
     */
    public HtmlPage mouseUpAt(final String elementLocator, final String coordinates);

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
    public HtmlPage mouseUpAt(final String elementLocator, final int coordX, final int coordY);

    /**
     * Opens the given URL.
     * 
     * @param pageUrlString
     *            the URL to open
     */
    public HtmlPage open(final String pageUrlString) throws Exception;

    /**
     * Opens the given URL.
     * 
     * @param url
     *            the target URL
     */
    public HtmlPage open(final URL url) throws Exception;

    /**
     * Waits the given time.
     * 
     * @param waitingTime
     *            the time in milliseconds to wait
     */
    public HtmlPage pause(final long waitingTime);

    /**
     * Waits the given time.
     * 
     * @param waitingTime
     *            the time in milliseconds to wait
     */
    public HtmlPage pause(final String waitingTime);

    /**
     * Removes the given option of the given select from the current selection.
     * 
     * @param select
     *            the select
     * @param option
     *            the option to be removed from the current selection
     */
    public HtmlPage removeSelection(final String select, final String option);

    /**
     * Selects the given option of the given select.
     * 
     * @param select
     *            the select
     * @param option
     *            the option to select
     */
    public HtmlPage select(final String select, final String option);

    /**
     * Selects the given option of the given select and waits for some activity to complete.
     * 
     * @param select
     *            the select
     * @param option
     *            the option to select
     */
    public HtmlPage selectAndWait(final String select, final String option);

    /**
     * Selects the given frame.
     * 
     * @param frameTarget
     *            the frame to be selected
     */
    public HtmlPage selectFrame(final String frameTarget);

    /**
     * Selects the top-level window.
     */
    public HtmlPage selectWindow();

    /**
     * Selects the given window.
     * 
     * @param windowTarget
     *            the window to be selected
     */
    public HtmlPage selectWindow(final String windowTarget);

    /**
     * Submits the given form.
     * 
     * @param form
     *            the form to submit
     */
    public HtmlPage submit(final String form) throws Exception;

    /**
     * Submits the given form and waits for some activity to complete.
     * 
     * @param form
     *            the form to submit
     */
    public HtmlPage submitAndWait(final String form);

    /**
     * Types the given text into the given input field.
     * 
     * @param elementLocator
     *            locator identifying the input field
     * @param text
     *            the text to be typed
     */
    public HtmlPage type(final String elementLocator, final String text) throws IOException;

    /**
     * Types the given text into the given input field and waits for some activity to complete.
     * 
     * @param elementLocator
     *            locator identifying the input field
     * @param text
     *            the text to be typed
     */
    public HtmlPage typeAndWait(final String elementLocator, final String text);

    /**
     * Unchecks the given checkbox/radio button.
     * 
     * @param elementLocator
     *            locator identifying the checkbox/radio button
     */
    public HtmlPage uncheck(final String elementLocator) throws IOException;

    /**
     * Unchecks the given checkbox/radio button and waits for a page load.
     * 
     * @param elementLocator
     *            locator identifying the checkbox/radio button
     */
    public HtmlPage uncheckAndWait(final String elementLocator) throws IOException;

    /**
     * Waits until the value of the attribute identified by the given attribute locator matches the given text pattern.
     * 
     * @param attributeLocator
     *            the attribute locator
     * @param textPattern
     *            the text pattern
     */
    public HtmlPage waitForAttribute(final String attributeLocator, final String textPattern);

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
    public HtmlPage waitForAttribute(final String elementLocator, final String attributeName, final String textPattern);

    /**
     * Waits until the given checkbox/radio button becomes checked.
     * 
     * @param elementLocator
     *            the checkbox/radio button element locator
     */
    public HtmlPage waitForChecked(final String elementLocator);

    /**
     * Waits until the given element has the given class(es).
     * 
     * @param elementLocator
     *            the element locator
     * @param clazzString
     *            the class(es) string
     */
    public HtmlPage waitForClass(final String elementLocator, final String clazzString);

    /**
     * Waits until the number of elements found by using the given element locator is equal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    public HtmlPage waitForElementCount(final String elementLocator, final int count);

    /**
     * Waits until the number of elements found by using the given element locator is equal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    public HtmlPage waitForElementCount(final String elementLocator, final String count);

    /**
     * Waits for the given element to appear.
     * 
     * @param elementLocator
     *            locator identifying the element to wait for
     */
    public HtmlPage waitForElementPresent(final String elementLocator);

    /**
     * Waits until the result of evaluating the given expression matches the given text pattern.
     * 
     * @param expression
     *            the expression to evaluate
     * @param textPattern
     *            textPattern the text pattern the evaluation result must match
     */
    public HtmlPage waitForEval(final String expression, final String textPattern);

    /**
     * Waits until the value of the attribute identified by the given attribute locator does NOT match the given text
     * pattern.
     * 
     * @param attributeLocator
     *            the attribute locator
     * @param textPattern
     *            the text pattern that must NOT match
     */
    public HtmlPage waitForNotAttribute(final String attributeLocator, final String textPattern);

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
    public HtmlPage waitForNotAttribute(final String elementLocator, final String attributeName, final String textPattern);

    /**
     * Waits until the given checkbox/radio button becomes unchecked.
     * 
     * @param elementLocator
     *            the checkbox/radio button element locator
     */
    public HtmlPage waitForNotChecked(final String elementLocator);

    /**
     * Waits until the given element doesn't have the given class(es).
     * 
     * @param elementLocator
     *            the element locator
     * @param clazzString
     *            the class(es) string
     */
    public HtmlPage waitForNotClass(final String elementLocator, final String clazzString);

    /**
     * Waits until the number of elements found by using the given element locator is unequal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    public HtmlPage waitForNotElementCount(final String elementLocator, final int count);

    /**
     * Waits until the number of elements found by using the given element locator is unequal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    public HtmlPage waitForNotElementCount(final String elementLocator, final String count);

    /**
     * Waits for the given element to disappear.
     * 
     * @param elementLocator
     *            locator identifying the element to disappear
     */
    public HtmlPage waitForNotElementPresent(final String elementLocator);

    /**
     * Waits until the result of evaluating the given expression does NOT match the given text pattern.
     * 
     * @param expression
     *            the expression to evaluate
     * @param textPattern
     *            the text pattern that the evaluation result must NOT match
     */
    public HtmlPage waitForNotEval(final String expression, final String textPattern);

    /**
     * Waits until no ID of all selected options of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param idPattern
     *            the ID pattern
     */
    public HtmlPage waitForNotSelectedId(final String selectLocator, final String idPattern);

    /**
     * Waits until the option of the given select element at the given index is not selected.
     * 
     * @param selectLocator
     *            the select element locator
     * @param indexPattern
     *            the option index pattern
     */
    public HtmlPage waitForNotSelectedIndex(final String selectLocator, final String indexPattern);

    /**
     * Waits until no label of all selected options of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param labelPattern
     *            the label pattern
     */
    public HtmlPage waitForNotSelectedLabel(final String selectLocator, final String labelPattern);

    /**
     * Waits until no value of all selected options of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param valuePattern
     *            the value pattern
     */
    public HtmlPage waitForNotSelectedValue(final String selectLocator, final String valuePattern);

    /**
     * Waits until the effective style of the element identified by the given element locator does NOT match the given
     * style.
     * 
     * @param elementLocator
     *            the element locator
     * @param styleText
     *            the style that must NOT match (e.g. <code>width: 10px; overflow: hidden;</code>)
     */
    public HtmlPage waitForNotStyle(final String elementLocator, final String styleText);

    /**
     * Waits for the given text embedded in the given element to disappear/change.
     * 
     * @param elementLocator
     *            locator identifying the element whose embedded text should change
     * @param text
     *            the text that should change/disappear
     */
    public HtmlPage waitForNotText(final String elementLocator, final String text);

    /**
     * Waits for the given text to disappear/change.
     * 
     * @param text
     *            the text that should disappear/change
     */
    public HtmlPage waitForNotTextPresent(final String text);

    /**
     * Waits for the given page title change.
     * 
     * @param title
     *            the page title that should change
     */
    public HtmlPage waitForNotTitle(final String title);

    /**
     * Waits for the given value in the given element to disappear/change.
     * 
     * @param elementLocator
     *            locator identifying the element whose value should change
     * @param value
     *            the value that should change/disappear
     */
    public HtmlPage waitForNotValue(final String elementLocator, final String value);

    /**
     * Waits until the given element becomes invisible.
     * 
     * @param elementLocator
     *            the element locator
     */
    public HtmlPage waitForNotVisible(final String elementLocator);

    /**
     * Waits for the number of elements matching the given XPath expression change to a different value than the given
     * one.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements currently matching the given XPath expression
     */
    public HtmlPage waitForNotXpathCount(final String xpath, final int count);

    /**
     * Waits for the number of elements matching the given XPath expression change to a different value than the given
     * one.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements currently matching the given XPath expression
     */
    public HtmlPage waitForNotXpathCount(final String xpath, final String count);

    /**
     * Waits for the page to be loaded completely.
     */
    public HtmlPage waitForPageToLoad();

    /**
     * Waits for any pop-up window to be loaded completely.
     */
    public HtmlPage waitForPopUp();

    /**
     * Waits for some pop-up window to be loaded completely.
     * 
     * @param windowID
     *            the ID of the window to wait for
     */
    public HtmlPage waitForPopUp(final String windowID);

    /**
     * Waits at most the given time for some pop-up window to be loaded completely.
     * 
     * @param windowID
     *            the ID of the window to wait for
     * @param maxWaitingTime
     *            the maximum waiting time
     */
    public HtmlPage waitForPopUp(final String windowID, final long maxWaitingTime);

    /**
     * Waits at most the given time for some pop-up window to be loaded completely.
     * 
     * @param windowID
     *            the ID of the window to wait for
     * @param maxWaitingTime
     *            the maximum waiting time
     */
    public HtmlPage waitForPopUp(final String windowID, final String maxWaitingTime);

    /**
     * Waits until the ID of at least one selected option of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param idPattern
     *            the ID pattern
     */
    public HtmlPage waitForSelectedId(final String selectLocator, final String idPattern);

    /**
     * Waits until the option of the given select at the given index is selected.
     * 
     * @param selectLocator
     *            the select element locator
     * @param indexPattern
     *            the option index pattern
     */
    public HtmlPage waitForSelectedIndex(final String selectLocator, final String indexPattern);

    /**
     * Waits until the label of at least one selected option of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param labelPattern
     *            the label pattern
     */
    public HtmlPage waitForSelectedLabel(final String selectLocator, final String labelPattern);

    /**
     * Waits until the value of at least one selected option of the given select matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param valuePattern
     *            the value pattern
     */
    public HtmlPage waitForSelectedValue(final String selectLocator, final String valuePattern);

    /**
     * Waits until the effective style of the element identified by the given element locator matches the given style.
     * 
     * @param elementLocator
     *            the element locator
     * @param styleText
     *            the style that must match (e.g. <code>width: 10px; overflow: hidden;</code>)
     */
    public HtmlPage waitForStyle(final String elementLocator, final String styleText);

    /**
     * Waits for the given text embedded in the given element.
     * 
     * @param elementLocator
     *            locator identifying the element whose text should contain the given text
     * @param text
     *            the text to wait for
     */
    public HtmlPage waitForText(final String elementLocator, final String text);

    /**
     * Waits for the given text to appear.
     * 
     * @param text
     *            the text to wait for
     */
    public HtmlPage waitForTextPresent(final String text);

    /**
     * Waits for the given page title.
     * 
     * @param title
     *            the page title to wait for
     */
    public HtmlPage waitForTitle(final String title);

    /**
     * Waits for the given value in the given element.
     * 
     * @param elementLocator
     *            locator identifying the element whose value should match the given value
     * @param value
     *            the value to wait for
     */
    public HtmlPage waitForValue(final String elementLocator, final String value);

    /**
     * Waits until the given element becomes visible.
     * 
     * @param elementLocator
     *            the element locator
     */
    public HtmlPage waitForVisible(final String elementLocator);

    /**
     * Waits for the number of elements matching the given XPath expression is equal to the given count.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements to wait for
     */
    public HtmlPage waitForXpathCount(final String xpath, final int count);

    /**
     * Waits for the number of elements matching the given XPath expression is equal to the given count.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements to wait for
     */
    public HtmlPage waitForXpathCount(final String xpath, final String count);

}
