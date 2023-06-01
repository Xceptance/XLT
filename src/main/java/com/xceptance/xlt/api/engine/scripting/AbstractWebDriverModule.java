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

import java.net.URL;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.engine.scripting.TestContext;
import com.xceptance.xlt.engine.scripting.util.AbstractCommandAdapter;
import com.xceptance.xlt.engine.scripting.webdriver.WebDriverScriptCommands;

/**
 * Base class of all exported script modules that rely on the {@link WebDriver} API.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractWebDriverModule implements ScriptCommands
{
    /**
     * The command adapter.
     */
    private final WebDriverScriptCommands _adapter;

    /**
     * Default constructor.
     */
    public AbstractWebDriverModule()
    {
        _adapter = (WebDriverScriptCommands) TestContext.getCurrent().getAdapter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSelection(final String select, final String option)
    {
        _adapter.addSelection(select, option);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertAttribute(final String attributeLocator, final String textPattern)
    {
        _adapter.assertAttribute(attributeLocator, textPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertAttribute(String elementLocator, String attributeName, String textPattern)
    {
        _adapter.assertAttribute(elementLocator, attributeName, textPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertChecked(final String elementLocator)
    {
        _adapter.assertChecked(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertClass(final String elementLocator, final String clazzString)
    {
        _adapter.assertClass(elementLocator, clazzString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertElementCount(final String elementLocator, final int count)
    {
        _adapter.assertElementCount(elementLocator, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertElementCount(final String elementLocator, final String count)
    {
        _adapter.assertElementCount(elementLocator, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertElementPresent(final String elementLocator)
    {
        _adapter.assertElementPresent(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertEval(final String expression, final String textPattern)
    {
        _adapter.assertEval(expression, textPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertLoadTime(final long loadTime)
    {
        _adapter.assertLoadTime(loadTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertLoadTime(final String loadTime)
    {
        _adapter.assertLoadTime(loadTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotAttribute(final String attributeLocator, final String textPattern)
    {
        _adapter.assertNotAttribute(attributeLocator, textPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        _adapter.assertNotAttribute(elementLocator, attributeName, textPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotChecked(final String elementLocator)
    {
        _adapter.assertNotChecked(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotClass(final String elementLocator, final String clazzString)
    {
        _adapter.assertNotClass(elementLocator, clazzString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotElementCount(final String elementLocator, final int count)
    {
        _adapter.assertNotElementCount(elementLocator, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotElementCount(final String elementLocator, final String count)
    {
        _adapter.assertNotElementCount(elementLocator, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotElementPresent(final String elementLocator)
    {
        _adapter.assertNotElementPresent(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotEval(final String expression, final String textPattern)
    {
        _adapter.assertNotEval(expression, textPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotSelectedId(final String selectLocator, final String idPattern)
    {
        _adapter.assertNotSelectedId(selectLocator, idPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotSelectedIndex(final String selectLocator, final String indexPattern)
    {
        _adapter.assertNotSelectedIndex(selectLocator, indexPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotSelectedLabel(final String selectLocator, final String labelPattern)
    {
        _adapter.assertNotSelectedLabel(selectLocator, labelPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotSelectedValue(final String selectLocator, final String valuePattern)
    {
        _adapter.assertNotSelectedValue(selectLocator, valuePattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotStyle(final String elementLocator, final String styleText)
    {
        _adapter.assertNotStyle(elementLocator, styleText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotText(final String elementLocator, final String text)
    {
        _adapter.assertNotText(elementLocator, text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotTextPresent(final String text)
    {
        _adapter.assertNotTextPresent(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotTitle(final String title)
    {
        _adapter.assertNotTitle(title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotValue(final String elementLocator, final String value)
    {
        _adapter.assertNotValue(elementLocator, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotVisible(final String elementLocator)
    {
        _adapter.assertNotVisible(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotXpathCount(final String xpath, final int count)
    {
        _adapter.assertNotXpathCount(xpath, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotXpathCount(final String xpath, final String count)
    {
        _adapter.assertNotXpathCount(xpath, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertPageSize(final long pageSize)
    {
        _adapter.assertPageSize(pageSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertPageSize(final String pageSize)
    {
        _adapter.assertPageSize(pageSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertSelectedId(final String selectLocator, final String idPattern)
    {
        _adapter.assertSelectedId(selectLocator, idPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertSelectedIndex(final String selectLocator, final String indexPattern)
    {
        _adapter.assertSelectedIndex(selectLocator, indexPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertSelectedLabel(final String selectLocator, final String labelPattern)
    {
        _adapter.assertSelectedLabel(selectLocator, labelPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertSelectedValue(final String selectLocator, final String valuePattern)
    {
        _adapter.assertSelectedValue(selectLocator, valuePattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertStyle(final String elementLocator, final String styleText)
    {
        _adapter.assertStyle(elementLocator, styleText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertText(final String elementLocator, final String text)
    {
        _adapter.assertText(elementLocator, text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertTextPresent(final String text)
    {
        _adapter.assertTextPresent(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertTitle(final String title)
    {
        _adapter.assertTitle(title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertValue(final String elementLocator, final String value)
    {
        _adapter.assertValue(elementLocator, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertVisible(final String elementLocator)
    {
        _adapter.assertVisible(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertXpathCount(final String xpath, final int count)
    {
        _adapter.assertXpathCount(xpath, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertXpathCount(final String xpath, final String count)
    {
        _adapter.assertXpathCount(xpath, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void check(final String elementLocator)
    {
        _adapter.check(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkAndWait(final String elementLocator)
    {
        _adapter.checkAndWait(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void click(final String elementLocator)
    {
        _adapter.click(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clickAndWait(final String elementLocator)
    {
        _adapter.clickAndWait(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close()
    {
        _adapter.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextMenu(final String elementLocator)
    {
        _adapter.contextMenu(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextMenuAt(final String elementLocator, final String coordinates)
    {
        _adapter.contextMenuAt(elementLocator, coordinates);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextMenuAt(final String elementLocator, final int coordX, final int coordY)
    {
        _adapter.contextMenuAt(elementLocator, coordX, coordY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createCookie(final String cookie)
    {
        _adapter.createCookie(cookie);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createCookie(final String cookie, final String options)
    {
        _adapter.createCookie(cookie, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllVisibleCookies()
    {
        _adapter.deleteAllVisibleCookies();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteCookie(final String name)
    {
        _adapter.deleteCookie(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteCookie(final String name, final String options)
    {
        _adapter.deleteCookie(name, options);
    }

    /**
     * Executes this module's commands.
     * 
     * @param parameters
     *            The module parameters.
     * @throws Exception
     */
    protected abstract void doCommands(final String... parameters) throws Exception;

    /**
     * {@inheritDoc}
     */
    @Override
    public void doubleClick(final String elementLocator)
    {
        _adapter.doubleClick(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doubleClickAndWait(final String elementLocator)
    {
        _adapter.doubleClickAndWait(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void echo(final String message)
    {
        _adapter.echo(message);
    }

    /**
     * Returns the result of evaluating the given JavaScript expression.
     * 
     * @param jsExpression
     *            the JavaScript expression to evaluate
     * @return result of evaluation
     */
    public String evaluate(final String jsExpression)
    {
        return _adapter.evaluate(jsExpression);
    }

    /**
     * Returns whether or not the given expression evaluates to <code>true</code>.
     * 
     * @param jsExpression
     *            the JavaScript expression to evaluate
     * @return <code>true</code> if and only if the given JavaScript expression is not blank and evaluates to
     *         <code>true</code>
     */
    public boolean evaluatesToTrue(final String jsExpression)
    {
        return _adapter.evaluatesToTrue(jsExpression);
    }

    /**
     * Executes this module.
     * 
     * @param parameters
     *            The arguments for the module call.
     */
    public void execute(final String... parameters) throws Exception
    {
        // resolve any placeholder in the parameters
        for (int i = 0; i < parameters.length; i++)
        {
            parameters[i] = resolve(parameters[i]);
        }

        TestContext.getCurrent().pushScope(this);
        final String name = getClass().getCanonicalName();
        try
        {
            AbstractCommandAdapter.LOGGER.info("Calling module: " + name);
            doCommands(parameters);
        }
        finally
        {
            AbstractCommandAdapter.LOGGER.info("Returned from module: " + name);
            TestContext.getCurrent().popScope();
        }
    }

    /**
     * Returns the first element matching the given locator.
     * 
     * @param elementLocator
     *            the element locator
     * @return first element matching the given locator
     */
    public WebElement findElement(final String elementLocator)
    {
        return _adapter.findElement(elementLocator);
    }

    /**
     * Returns all elements that match the given locator.
     * 
     * @param elementLocator
     *            the element locator
     * @return all elements that were found for the given locator
     */
    public List<WebElement> findElements(final String elementLocator)
    {
        return _adapter.findElements(elementLocator);
    }

    /**
     * Returns the value of the given element attribute locator.
     * 
     * @param attributeLocator
     *            the element attribute locator
     * @return value of attribute specified by given element attribute locator
     */
    public String getAttribute(final String attributeLocator)
    {
        return _adapter.getAttribute(attributeLocator);
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
    public String getAttribute(final String elementLocator, final String attributeName)
    {
        return _adapter.getAttribute(elementLocator, attributeName);
    }

    /**
     * Returns the number of matching elements.
     * 
     * @param elementLocator
     *            the element locator
     * @return number of elements matching the given locator
     */
    public int getElementCount(String elementLocator)
    {
        return _adapter.getElementCount(elementLocator);
    }

    /**
     * Returns the (visible) text of the current page.
     * 
     * @return the page's (visible) text
     */
    public String getPageText()
    {
        return _adapter.getPageText();
    }

    /**
     * Returns the (visible) text of the given element. If the element is not visible, the empty string is returned.
     * 
     * @param elementLocator
     *            the element locator
     * @return the element's (visible) text
     */
    public String getText(final String elementLocator)
    {
        return _adapter.getText(elementLocator);
    }

    /**
     * Returns the title of the current page.
     * 
     * @return page title
     */
    public String getTitle()
    {
        return _adapter.getTitle();
    }

    /**
     * Returns the value of the given element. If the element doesn't have a value, the empty string is returned.
     * 
     * @param elementLocator
     *            the element locator
     * @return the element's value
     */
    public String getValue(final String elementLocator)
    {
        return _adapter.getValue(elementLocator);
    }

    /**
     * Returns the webdriver instance.
     * 
     * @return webdriver instance
     */
    protected final WebDriver getWebDriver()
    {
        return _adapter.getUnderlyingWebDriver();
    }

    /**
     * Returns the number of elements matching the given XPath expression.
     * 
     * @param xpath
     *            the XPath expression
     * @return number of matching elements
     */
    public int getXpathCount(final String xpath)
    {
        return _adapter.getXpathCount(xpath);
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
    public boolean hasAttribute(final String attributeLocator, final String textPattern)
    {
        return _adapter.hasAttribute(attributeLocator, textPattern);
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
    public boolean hasAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        return _adapter.hasAttribute(elementLocator, attributeName, textPattern);
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
     * @see com.xceptance.xlt.engine.scripting.util.CommonScriptCommands#hasClass(java.lang.String, java.lang.String)
     */
    public boolean hasClass(final String elementLocator, final String clazz)
    {
        return _adapter.hasClass(elementLocator, clazz);
    }

    /**
     * Returns whether or not the given element doesn't have the given class(es); that is, its class attribute doesn't
     * contain any of the given class(es).
     * 
     * @param elementLocator
     * @param clazz
     *            the class string (multiple CSS classes separated by whitespace)
     * @return <code>true</code> if the element's class attribute does not contains any of the given class(es),
     *         <code>false</code> otherwise
     */
    public boolean hasNotClass(final String elementLocator, final String clazz)
    {
        return _adapter.hasNotClass(elementLocator, clazz);
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
    public boolean hasNotStyle(final String elementLocator, final String style)
    {
        return _adapter.hasNotStyle(elementLocator, style);
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
    public boolean hasStyle(final String elementLocator, final String style)
    {
        return _adapter.hasStyle(elementLocator, style);
    }

    /**
     * Checks that the text embedded by the given element contains the given text.
     * 
     * @param elementLocator
     *            locator identifying the element whose text should contain the given text
     * @param textPattern
     *            the text that should be embedded in the given element
     * @return <code>true</code> if the text embedded by the given element contains the given text, <code>false</code>
     *         otherwise
     */
    public boolean hasText(final String elementLocator, final String textPattern)
    {
        return _adapter.hasText(elementLocator, textPattern);
    }

    /**
     * Checks that the given title matches the page title.
     * 
     * @param title
     *            the title that should match the page title
     * @return <code>true</code> if the given title matches the page title, <code>false</code> otherwise
     */
    public boolean hasTitle(final String title)
    {
        return _adapter.hasTitle(title);
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
    public boolean hasValue(final String elementLocator, final String valuePattern)
    {
        return _adapter.hasValue(elementLocator, valuePattern);
    }

    /**
     * Returns whether or not the element identified by the given element locator is checked.
     * 
     * @param elementLocator
     *            the element locator
     * @return <code>true</code> if the element identified by the given element locator is checked, <code>false</code>
     *         otherwise
     */
    public boolean isChecked(final String elementLocator)
    {
        return _adapter.isChecked(elementLocator);
    }

    /**
     * Returns whether or not there is an element for the given locator.
     * 
     * @param elementLocator
     *            the element locator
     * @return <code>true</code> if there at least one element has been found for the given locator, <code>false</code>
     *         otherwise
     */
    public boolean isElementPresent(final String elementLocator)
    {
        return _adapter.isElementPresent(elementLocator);
    }

    /**
     * Returns whether or not the given element is enabled.
     * 
     * @param elementLocator
     *            the element locator
     * @return <code>true</code> if the element identified by the given element locator is enabled, <code>false</code>
     *         otherwise
     */
    public boolean isEnabled(final String elementLocator)
    {
        return _adapter.isEnabled(elementLocator);
    }

    /**
     * Returns whether or not the result of evaluating the given expression matches the given text pattern.
     * 
     * @param expression
     *            the JavaScript expression to evaluate
     * @param textPattern
     *            the text pattern
     * @return <code>true</code> if the evaluation result matches the given pattern, <code>false</code> otherwise
     */
    public boolean isEvalMatching(final String expression, final String textPattern)
    {
        return _adapter.isEvalMatching(expression, textPattern);
    }

    /**
     * Checks that the given text is present.
     * 
     * @param textPattern
     *            the text that should be present
     * @return <code>true</code> if the given text is present, <code>false</code> otherwise
     */
    public boolean isTextPresent(final String textPattern)
    {
        return _adapter.isTextPresent(textPattern);
    }

    /**
     * Returns whether or not the given element is visible.
     * 
     * @param elementLocator
     *            the element locator
     * @return <code>true</code> if element was found and is visible, <code>false</code> otherwise
     */
    public boolean isVisible(final String elementLocator)
    {
        return _adapter.isVisible(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDown(final String elementLocator)
    {
        _adapter.mouseDown(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDownAt(final String elementLocator, final String coordinates)
    {
        _adapter.mouseDownAt(elementLocator, coordinates);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDownAt(final String elementLocator, final int coordX, final int coordY)
    {
        _adapter.mouseDownAt(elementLocator, coordX, coordY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseMove(final String elementLocator)
    {
        _adapter.mouseMove(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseMoveAt(final String elementLocator, final String coordinates)
    {
        _adapter.mouseMoveAt(elementLocator, coordinates);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseMoveAt(final String elementLocator, final int coordX, final int coordY)
    {
        _adapter.mouseMoveAt(elementLocator, coordX, coordY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseOut(final String elementLocator)
    {
        _adapter.mouseOut(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseOver(final String elementLocator)
    {
        _adapter.mouseOver(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseUp(final String elementLocator)
    {
        _adapter.mouseUp(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseUpAt(final String elementLocator, final String coordinates)
    {
        _adapter.mouseUpAt(elementLocator, coordinates);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseUpAt(final String elementLocator, final int coordX, final int coordY)
    {
        _adapter.mouseUpAt(elementLocator, coordX, coordY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(final String pageUrlString)
    {
        _adapter.open(pageUrlString);
    }

    /**
     * Opens the given URL.
     * 
     * @param url
     *            url the target URL
     */
    public void open(URL url)
    {
        _adapter.open(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pause(final long waitingTime)
    {
        _adapter.pause(waitingTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pause(final String waitingTime)
    {
        _adapter.pause(waitingTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeSelection(final String select, final String option)
    {
        _adapter.removeSelection(select, option);
    }

    /**
     * Resolves the given string.
     * 
     * @param resolvable
     *            the resolvable string containing one or more test data placeholders
     * @return resolved string
     */
    public String resolve(final String resolvable)
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
    public String resolveKey(final String key)
    {
        return TestContext.getCurrent().resolveKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void select(final String select, final String option)
    {
        _adapter.select(select, option);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectAndWait(final String select, final String option)
    {
        _adapter.selectAndWait(select, option);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectFrame(final String frameTarget)
    {
        _adapter.selectFrame(frameTarget);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectWindow()
    {
        _adapter.selectWindow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectWindow(final String windowTarget)
    {
        _adapter.selectWindow(windowTarget);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeout(final long timeout)
    {
        _adapter.setTimeout(timeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeout(final String timeout)
    {
        _adapter.setTimeout(timeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startAction(final String actionName)
    {
        Session.getCurrent().startAction(actionName);
    }

    /**
     * Stops the current action.
     *
     * @see #startAction(String)
     */
    public void stopAction()
    {
        Session.getCurrent().stopAction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store(final String text, final String variableName)
    {
        _adapter.store(text, variableName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeAttribute(final String attributeLocator, final String variableName)
    {
        _adapter.storeAttribute(attributeLocator, variableName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeAttribute(String elementLocator, String attributeName, String variableName)
    {
        _adapter.storeAttribute(elementLocator, attributeName, variableName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeElementCount(final String elementLocator, final String variableName)
    {
        _adapter.storeElementCount(elementLocator, variableName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeEval(final String expression, final String variableName)
    {
        _adapter.storeEval(expression, variableName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeText(final String elementLocator, final String variableName)
    {
        _adapter.storeText(elementLocator, variableName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeTitle(final String variableName)
    {
        _adapter.storeTitle(variableName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeValue(final String elementLocator, final String variableName)
    {
        _adapter.storeValue(elementLocator, variableName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeXpathCount(final String xpath, final String variableName)
    {
        _adapter.storeXpathCount(xpath, variableName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void submit(final String form)
    {
        _adapter.submit(form);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void submitAndWait(final String form)
    {
        _adapter.submitAndWait(form);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void type(final String elementLocator, final String text)
    {
        _adapter.type(elementLocator, text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void typeAndWait(final String elementLocator, final String text)
    {
        _adapter.typeAndWait(elementLocator, text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uncheck(final String elementLocator)
    {
        _adapter.uncheck(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uncheckAndWait(final String elementLocator)
    {
        _adapter.uncheckAndWait(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForAttribute(final String attributeLocator, final String textPattern)
    {
        _adapter.waitForAttribute(attributeLocator, textPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForAttribute(String elementLocator, String attributeName, String textPattern)
    {
        _adapter.waitForAttribute(elementLocator, attributeName, textPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForChecked(final String elementLocator)
    {
        _adapter.waitForChecked(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForClass(final String elementLocator, final String clazzString)
    {
        _adapter.waitForClass(elementLocator, clazzString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForElementCount(final String elementLocator, final int count)
    {
        _adapter.waitForElementCount(elementLocator, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForElementCount(final String elementLocator, final String count)
    {
        _adapter.waitForElementCount(elementLocator, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForElementPresent(final String elementLocator)
    {
        _adapter.waitForElementPresent(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForEval(final String expression, final String textPattern)
    {
        _adapter.waitForEval(expression, textPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotAttribute(final String attributeLocator, final String textPattern)
    {
        _adapter.waitForNotAttribute(attributeLocator, textPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotAttribute(String elementLocator, String attributeName, String textPattern)
    {
        _adapter.waitForNotAttribute(elementLocator, attributeName, textPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotChecked(final String elementLocator)
    {
        _adapter.waitForNotChecked(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotClass(final String elementLocator, final String clazzString)
    {
        _adapter.waitForNotClass(elementLocator, clazzString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotElementCount(final String elementLocator, final int count)
    {
        _adapter.waitForNotElementCount(elementLocator, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotElementCount(final String elementLocator, final String count)
    {
        _adapter.waitForNotElementCount(elementLocator, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotElementPresent(final String elementLocator)
    {
        _adapter.waitForNotElementPresent(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotEval(final String expression, final String textPattern)
    {
        _adapter.waitForNotEval(expression, textPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotSelectedId(final String selectLocator, final String idPattern)
    {
        _adapter.waitForNotSelectedId(selectLocator, idPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotSelectedIndex(final String selectLocator, final String indexPattern)
    {
        _adapter.waitForNotSelectedIndex(selectLocator, indexPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotSelectedLabel(final String selectLocator, final String labelPattern)
    {
        _adapter.waitForNotSelectedLabel(selectLocator, labelPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotSelectedValue(final String selectLocator, final String valuePattern)
    {
        _adapter.waitForNotSelectedValue(selectLocator, valuePattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotStyle(final String elementLocator, final String styleText)
    {
        _adapter.waitForNotStyle(elementLocator, styleText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotText(final String elementLocator, final String text)
    {
        _adapter.waitForNotText(elementLocator, text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotTextPresent(final String text)
    {
        _adapter.waitForNotTextPresent(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotTitle(final String title)
    {
        _adapter.waitForNotTitle(title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotValue(final String elementLocator, final String value)
    {
        _adapter.waitForNotValue(elementLocator, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotVisible(final String elementLocator)
    {
        _adapter.waitForNotVisible(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotXpathCount(final String xpath, final int count)
    {
        _adapter.waitForNotXpathCount(xpath, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotXpathCount(final String xpath, final String count)
    {
        _adapter.waitForNotXpathCount(xpath, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForPageToLoad()
    {
        _adapter.waitForPageToLoad();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForPopUp()
    {
        _adapter.waitForPopUp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForPopUp(final String windowID)
    {
        _adapter.waitForPopUp(windowID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForPopUp(final String windowID, final long maxWaitingTime)
    {
        _adapter.waitForPopUp(windowID, maxWaitingTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForPopUp(final String windowID, final String maxWaitingTime)
    {
        _adapter.waitForPopUp(windowID, maxWaitingTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForSelectedId(final String selectLocator, final String idPattern)
    {
        _adapter.waitForSelectedId(selectLocator, idPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForSelectedIndex(final String selectLocator, final String indexPattern)
    {
        _adapter.waitForSelectedIndex(selectLocator, indexPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForSelectedLabel(final String selectLocator, final String labelPattern)
    {
        _adapter.waitForSelectedLabel(selectLocator, labelPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForSelectedValue(final String selectLocator, final String valuePattern)
    {
        _adapter.waitForSelectedValue(selectLocator, valuePattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForStyle(final String elementLocator, final String styleText)
    {
        _adapter.waitForStyle(elementLocator, styleText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForText(final String elementLocator, final String text)
    {
        _adapter.waitForText(elementLocator, text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForTextPresent(final String text)
    {
        _adapter.waitForTextPresent(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForTitle(final String title)
    {
        _adapter.waitForTitle(title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForValue(final String elementLocator, final String value)
    {
        _adapter.waitForValue(elementLocator, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForVisible(final String elementLocator)
    {
        _adapter.waitForVisible(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForXpathCount(final String xpath, final int count)
    {
        _adapter.waitForXpathCount(xpath, count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForXpathCount(final String xpath, final String count)
    {
        _adapter.waitForXpathCount(xpath, count);
    }
}
