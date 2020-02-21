package com.xceptance.xlt.engine.scripting.util;

import com.xceptance.xlt.engine.scripting.webdriver.WebDriverScriptCommands;

/**
 * Describes the script command methods that have the same signature for both WebDriver- and HtmlUnit-based backends.
 * 
 * @see WebDriverScriptCommands
 */
public interface CommonScriptCommands
{
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
     * Closes the browser.
     */
    public void close();

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
     * Prints the given message to the log.
     * 
     * @param message
     *            the message to print
     */
    public void echo(final String message);

    /**
     * Returns whether or not the given expression evaluates to <code>true</code>.
     * 
     * @param jsExpression
     *            the JavaScript expression to evaluate
     * @return <code>true</code> if and only if the given JavaScript expression is not blank and evaluates to
     *         <code>true</code>
     */
    public boolean evaluatesToTrue(final String jsExpression);

    /**
     * Returns the result of evaluating the given JavaScript expression.
     * 
     * @param jsExpression
     *            the JavaScript expression to evaluate
     * @return result of evaluation
     */
    public String evaluate(final String jsExpression);

    /**
     * Returns the value of the given element attribute locator.
     * 
     * @param attributeLocator
     *            the element attribute locator
     * @return value of given element attribute locator
     */
    public String getAttribute(final String attributeLocator);

    /**
     * Returns the value of the given element and attribute.
     * 
     * @param elementLocator
     *            the element locator
     * @param attributeName
     *            the name of the attribute
     * @return value of given element attribute locator
     */
    public String getAttribute(final String elementLocator, final String attributeName);

    /**
     * Returns the number of matching elements.
     * 
     * @param elementLocator
     *            the element locator
     * @return number of elements matching the given locator
     */
    public int getElementCount(final String elementLocator);

    /**
     * Returns the (visible) text of the current page.
     * 
     * @return the page's (visible) text
     */
    public String getPageText();

    /**
     * Returns the (visible) text of the given element. If the element is not visible, the empty string is returned.
     * 
     * @param elementLocator
     *            the element locator
     * @return the element's (visible) text
     */
    public String getText(final String elementLocator);

    /**
     * Returns the title of the current page.
     * 
     * @return page title
     */
    public String getTitle();

    /**
     * Returns the value of the given element. If the element doesn't have a value, the empty string is returned.
     * 
     * @param elementLocator
     *            the element locator
     * @return the element's value
     */
    public String getValue(final String elementLocator);

    /**
     * Returns the number of elements matching the given XPath expression.
     * 
     * @param xpath
     *            the XPath expression
     * @return number of matching elements
     */
    public int getXpathCount(final String xpath);

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
    public boolean hasAttribute(final String attributeLocator, final String textPattern);
    
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
    public boolean hasAttribute(final String elementLocator, final String attributeName, final String textPattern);

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
    public boolean hasClass(final String elementLocator, final String clazz);

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
    public boolean hasStyle(final String elementLocator, final String style);
    
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
    public boolean hasNotClass(final String elementLocator, final String clazz);

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
    public boolean hasNotStyle(final String elementLocator, final String style);
    
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
    public boolean hasText(final String elementLocator, final String textPattern);
    

    /**
     * Checks that the given title matches the page title.
     * 
     * @param title
     *            the title that should match the page title
     * @return <code>true</code> if the given title matches the page title, <code>false</code> otherwise
     */
    public boolean hasTitle(final String title);
    
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
    public boolean hasValue(final String elementLocator, final String valuePattern);
    
    /**
     * Returns whether or not the element identified by the given element locator is checked.
     * 
     * @param elementLocator
     *            the element locator
     * @return <code>true</code> if the element identified by the given element locator is checked, <code>false</code>
     *         otherwise
     */
    public boolean isChecked(final String elementLocator);

    /**
     * Returns whether or not there is an element for the given locator.
     * 
     * @param elementLocator
     *            the element locator
     * @return <code>true</code> if there at least one element has been found for the given locator, <code>false</code>
     *         otherwise
     */
    public boolean isElementPresent(final String elementLocator);

    /**
     * Returns whether or not the given element is enabled.
     * 
     * @param elementLocator
     *            the element locator
     * @return <code>true</code> if element was found and is enabled, <code>false</code> otherwise
     */
    public boolean isEnabled(final String elementLocator);

    /**
     * Returns whether or not the result of evaluating the given expression matches the given text pattern.
     * 
     * @param expression
     *            the expression to evaluate
     * @param textPattern
     *            the text pattern
     * @return <code>true</code> if the evaluation result matches the given pattern, <code>false</code> otherwise
     */
    public boolean isEvalMatching(final String expression, final String textPattern);

    
    /**
     * Checks that the given text is present.
     * 
     * @param textPattern
     *            the text that should be present
     * @return <code>true</code> if the given text is present, <code>false</code> otherwise
     */
    public boolean isTextPresent(final String textPattern);
    

    
    /**
     * Returns whether or not the given element is visible.
     * 
     * @param elementLocator
     *            the element locator
     * @return <code>true</code> if element was found and is visible, <code>false</code> otherwise
     */
    public boolean isVisible(final String elementLocator);

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
     * Stores the given text to the given variable.
     * 
     * @param text
     *            the text to store
     * @param variableName
     *            the variable name
     */
    public void store(final String text, final String variableName);

    /**
     * Stores the value of the attribute identified by the given attribute locator to the given variable.
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
}
