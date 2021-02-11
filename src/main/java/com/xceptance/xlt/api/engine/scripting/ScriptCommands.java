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
package com.xceptance.xlt.api.engine.scripting;

/**
 * Selenium-like commands supported by the XLT framework and the script developer.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public interface ScriptCommands
{
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
     * Asserts that the value of the attribute identified by the given attribute locator matches the given text pattern.
     * 
     * @param attributeLocator
     *            the attribute locator
     * @param textPattern
     *            the text pattern that the attribute value must match
     */
    public void assertAttribute(final String attributeLocator, final String textPattern);

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
    public void assertAttribute(final String elementLocator, final String attributeName, final String textPattern);

    /**
     * Asserts that the given checkbox/radio button is checked.
     * 
     * @param elementLocator
     *            the checkbox/radio button element locator
     */
    public void assertChecked(final String elementLocator);

    /**
     * Asserts that the given element has the given class(es).
     * 
     * @param elementLocator
     *            the element locator
     * @param clazzString
     *            the class(es) string
     */
    public void assertClass(final String elementLocator, final String clazzString);

    /**
     * Asserts that the number of elements found by using the given element locator is equal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    public void assertElementCount(final String elementLocator, final int count);

    /**
     * Asserts that the number of elements found by using the given element locator is equal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    public void assertElementCount(final String elementLocator, final String count);

    /**
     * Asserts that the given element is present.
     * 
     * @param elementLocator
     *            locator identifying the element that should be present
     */
    public void assertElementPresent(final String elementLocator);

    /**
     * Asserts that evaluating the given expression matches the given text pattern.
     * 
     * @param expression
     *            the expression to evaluate
     * @param textPattern
     *            the text pattern that the evaluation result must match
     */
    public void assertEval(final String expression, final String textPattern);

    /**
     * Asserts that the time needed to load a page does not exceed the given value.
     * 
     * @param loadTime
     *            maximum load time in milliseconds
     */
    public void assertLoadTime(final long loadTime);

    /**
     * Asserts that the time needed to load a page does not exceed the given value.
     * 
     * @param loadTime
     *            maximum load time in milliseconds
     */
    public void assertLoadTime(final String loadTime);

    /**
     * Asserts that the value of the attribute identified by the given attribute locator does NOT match the given text
     * pattern.
     * 
     * @param attributeLocator
     *            the attribute locator
     * @param textPattern
     *            the text pattern that the attribute value must NOT match
     */
    public void assertNotAttribute(final String attributeLocator, final String textPattern);

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
    public void assertNotAttribute(final String elementLocator, final String attributeName, final String textPattern);

    /**
     * Asserts that the given checkbox/radio button is unchecked.
     * 
     * @param elementLocator
     *            the checkbox/radio button element locator
     */
    public void assertNotChecked(final String elementLocator);

    /**
     * Asserts that the given element doesn't have the given class(es).
     * 
     * @param elementLocator
     *            the element locator
     * @param clazzString
     *            the class(es) string
     */
    public void assertNotClass(final String elementLocator, final String clazzString);

    /**
     * Asserts that the number of elements found by using the given element locator is unequal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    public void assertNotElementCount(final String elementLocator, final int count);

    /**
     * Asserts that the number of elements found by using the given element locator is unequal to the given count.
     * 
     * @param elementLocator
     *            the element locator
     * @param count
     *            the number of elements
     */
    public void assertNotElementCount(final String elementLocator, final String count);

    /**
     * Asserts that the given element is not present.
     * 
     * @param elementLocator
     *            locator identifying the element that should be NOT present
     */
    public void assertNotElementPresent(final String elementLocator);

    /**
     * Asserts that evaluating the given expression does NOT match the given text pattern.
     * 
     * @param expression
     *            the expression to evaluate
     * @param textPattern
     *            the text pattern that the evaluation result must NOT match
     */
    public void assertNotEval(final String expression, final String textPattern);

    /**
     * Asserts that no ID of all selected options of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param idPattern
     *            the ID pattern
     */
    public void assertNotSelectedId(final String selectLocator, final String idPattern);

    /**
     * Asserts that the option of the given select element at the given index is not selected.
     * 
     * @param selectLocator
     *            the select element locator
     * @param indexPattern
     *            the option index pattern
     */
    public void assertNotSelectedIndex(final String selectLocator, final String indexPattern);

    /**
     * Asserts that no label of all selected options of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param labelPattern
     *            the label pattern
     */
    public void assertNotSelectedLabel(final String selectLocator, final String labelPattern);

    /**
     * Asserts that no value of all selected options of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param valuePattern
     *            the value pattern
     */
    public void assertNotSelectedValue(final String selectLocator, final String valuePattern);

    /**
     * Asserts that the effective style of the element identified by the given element locator does NOT match the given
     * style.
     * 
     * @param elementLocator
     *            the element locator
     * @param styleText
     *            the style that must NOT match (e.g. <code>width: 10px; overflow: hidden;</code>)
     */
    public void assertNotStyle(final String elementLocator, final String styleText);

    /**
     * Asserts that the embedded text of the given element does not contain the given text.
     * 
     * @param elementLocator
     *            locator identifying the element
     * @param text
     *            the text that should not be embedded in the given element
     */
    public void assertNotText(final String elementLocator, final String text);

    /**
     * Asserts that the given text is not present on the page.
     * 
     * @param text
     *            the text that should NOT be present
     */
    public void assertNotTextPresent(final String text);

    /**
     * Asserts that the page title does not match the given title.
     * 
     * @param title
     *            the title that should not match
     */
    public void assertNotTitle(final String title);

    /**
     * Asserts that the value of the given element doesn't match the given value. If the element is a &lt;textarea&gt;
     * this method asserts that the containing text doesn't match the given value.
     * 
     * @param elementLocator
     *            locator identifying the element whose value doesn't match the given value
     * @param valuePattern
     *            the value that doesn't match the given element's value
     */
    public void assertNotValue(String elementLocator, String valuePattern);

    /**
     * Asserts that the given element is invisible.
     * 
     * @param elementLocator
     *            the element locator.
     */
    public void assertNotVisible(final String elementLocator);

    /**
     * Asserts that the number of elements locatable by the given XPath expression is not equal to the given count.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements that should NOT be equal to the actual number of elements matching the given
     *            XPath expression
     */
    public void assertNotXpathCount(final String xpath, final int count);

    /**
     * Asserts that the number of elements locatable by the given XPath expression is not equal to the given count.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements that should NOT be equal to the actual number of elements matching the given
     *            XPath expression
     */
    public void assertNotXpathCount(final String xpath, final String count);

    /**
     * Asserts that the size of the actual page (including images etc.) does not exceed the given value.
     * 
     * @param pageSize
     *            the number of bytes the page size must not exceed
     */
    public void assertPageSize(final long pageSize);

    /**
     * Asserts that the size of the actual page (including images etc.) does not exceed the given value.
     * 
     * @param pageSize
     *            the number of bytes the page size must not exceed
     */
    public void assertPageSize(final String pageSize);

    /**
     * Asserts that the ID of at least one selected option of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param idPattern
     *            ID pattern
     */
    public void assertSelectedId(final String selectLocator, final String idPattern);

    /**
     * Asserts that the option of the given select element at the given index is selected.
     * 
     * @param selectLocator
     *            the select element locator
     * @param indexPattern
     *            the option index pattern
     */
    public void assertSelectedIndex(final String selectLocator, final String indexPattern);

    /**
     * Asserts that the label of at least one selected option of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param labelPattern
     *            the label pattern
     */
    public void assertSelectedLabel(final String selectLocator, final String labelPattern);

    /**
     * Asserts that the value of at least one selected option of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param valuePattern
     *            the value pattern
     */
    public void assertSelectedValue(final String selectLocator, final String valuePattern);

    /**
     * Asserts that the effective style of the element identified by the given element locator matches the given style.
     * 
     * @param elementLocator
     *            the element locator
     * @param styleText
     *            the style to match (e.g. <code>width: 10px; overflow: hidden;</code>)
     */
    public void assertStyle(final String elementLocator, final String styleText);

    /**
     * Asserts that the text embedded by the given element contains the given text.
     * 
     * @param elementLocator
     *            locator identifying the element whose text should contain the given text
     * @param text
     *            the text that should be embedded in the given element
     */
    public void assertText(final String elementLocator, final String text);

    /**
     * Asserts that the given text is present.
     * 
     * @param text
     *            the text that should be present
     */
    public void assertTextPresent(final String text);

    /**
     * Asserts that the given title matches the page title.
     * 
     * @param title
     *            the title that should match the page title
     */
    public void assertTitle(final String title);

    /**
     * Asserts that the value of the given element matches the given value. If the element is a &lt;textarea&gt; this
     * method asserts that the containing text matches the given value.
     * 
     * @param elementLocator
     *            locator identifying the element whose value should match the given value
     * @param valuePattern
     *            the value that should match the given element's value
     */
    public void assertValue(String elementLocator, String valuePattern);

    /**
     * Asserts that the given element is visible.
     * 
     * @param elementLocator
     *            the element locator
     */
    public void assertVisible(final String elementLocator);

    /**
     * Asserts that the number of elements locatable by the given XPath expression is equal to the given count.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements that must match the given XPath expression
     */
    public void assertXpathCount(final String xpath, final int count);

    /**
     * Asserts that the number of elements locatable by the given XPath expression is equal to the given count.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements that must match the given XPath expression
     */
    public void assertXpathCount(final String xpath, final String count);

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
     * Closes the browser.
     */
    public void close();

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
     * Creates a new cookie. The new cookie will be stored as session cookie for the current path and domain.
     * 
     * @param cookie
     *            name value pair of the new cookie
     */
    public void createCookie(final String cookie);

    /**
     * Creates a new cookie.
     * 
     * @param cookie
     *            name value pair of the new cookie
     * @param options
     *            cookie creation options (path, max_age and domain)
     */
    public void createCookie(final String cookie, final String options);

    /**
     * Removes all cookies visible to the current page.
     */
    public void deleteAllVisibleCookies();

    /**
     * Removes the cookie with the specified name.
     * 
     * @param name
     *            the cookie's name
     */
    public void deleteCookie(final String name);

    /**
     * Removes the cookie with the specified name.
     * 
     * @param name
     *            the cookie's name
     * @param options
     *            cookie removal options (path, domain and recurse)
     */
    public void deleteCookie(final String name, final String options);

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
     * Prints the given message to the log.
     * 
     * @param message
     *            the message to print
     */
    public void echo(final String message);

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
     * Sets the timeout to the given value.
     * 
     * @param timeout
     *            the new timeout in milliseconds
     */
    public void setTimeout(final long timeout);

    /**
     * Sets the timeout to the given value.
     * 
     * @param timeout
     *            the new timeout in milliseconds
     */
    public void setTimeout(final String timeout);

    /**
     * Starts a new action using the given name.
     * 
     * @param actionName
     *            the name of the action
     */
    public void startAction(final String actionName);

    /**
     * Stores the given text to the given variable.
     * 
     * @param text
     *            the text to store
     * @param variableName
     *            the variable name
     */
    public void store(final String text, final String variableName);

    /**
     * Stores the value of the attribute identified by the given attribute locator to the given variable
     * 
     * @param attributeLocator
     *            the attribute locator
     * @param variableName
     *            the variable name
     */
    public void storeAttribute(final String attributeLocator, final String variableName);

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
    public void storeAttribute(final String elementLocator, final String attributeName, final String variableName);

    /**
     * Stores that the number of elements found by using the given element locator to the given variable.
     * 
     * @param elementLocator
     *            the element locator
     * @param variableName
     *            the variable name
     */
    public void storeElementCount(final String elementLocator, final String variableName);

    /**
     * Stores the result of evaluating the given expression to the given variable.
     * 
     * @param expression
     *            the expression to evaluate
     * @param variableName
     *            the variable name
     */
    public void storeEval(final String expression, final String variableName);

    /**
     * Stores the text of the element identified by the given locator to the given variable.
     * 
     * @param elementLocator
     *            the element locator
     * @param variableName
     *            the variable
     */
    public void storeText(final String elementLocator, final String variableName);

    /**
     * Stores the title of the currently active document to the given variable.
     * 
     * @param variableName
     *            the variable
     */
    public void storeTitle(final String variableName);

    /**
     * Stores the value (in case of a <code>&lt;textarea&gt;</code> the contained text) of the element identified by the
     * given locator to the given variable.
     * 
     * @param elementLocator
     *            the element locator
     * @param variableName
     *            the variable
     */
    public void storeValue(final String elementLocator, final String variableName);

    /**
     * Stores the number of elements matching the given XPath expression to the given variable.
     * 
     * @param xpath
     *            the XPath expression
     * @param variableName
     *            the variable
     */
    public void storeXpathCount(final String xpath, final String variableName);

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
